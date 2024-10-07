create PROCEDURE PROCESS_BATCH_PAYMENTS(
    p_customer_id IN VARCHAR2,
    p_transaction_count OUT INTEGER,
    p_total_amount OUT DECIMAL,
    p_error_code OUT VARCHAR2,
    p_error_message OUT VARCHAR2
) AS
    v_total_amount DECIMAL := 0;
    TYPE t_transaction_rec IS RECORD
                              (
                                  id     RAW(16),
                                  amount DECIMAL
                              );
    TYPE t_transaction_table IS TABLE OF t_transaction_rec;
    v_transactions t_transaction_table;

BEGIN
    -- Initialize output parameters
    p_transaction_count := 0;
    p_error_code := NULL;
    p_error_message := NULL;

    -- Retrieve all pending transactions for the customer into a collection
    SELECT t.id,
           t.amount BULK COLLECT
    INTO v_transactions
    FROM transaction t
             JOIN account a ON t.account_id = a.id
    WHERE a.customer_id = p_customer_id
      AND t.status = 'PENDING';

-- Calculate total amount
    FOR i IN 1 .. v_transactions.COUNT
        LOOP
            v_total_amount := v_total_amount + v_transactions(i).amount;
        END LOOP;

    -- Check if the customer has sufficient balance
    DECLARE
        v_balance DECIMAL;
    BEGIN
        SELECT a.balance
        INTO v_balance
        FROM account a
        WHERE a.customer_id = p_customer_id;

        IF v_balance < v_total_amount THEN
            -- Insufficient balance, mark transactions as FAILED
            FOR i IN 1 .. v_transactions.COUNT
                LOOP
                    UPDATE transaction
                    SET status = 'FAILED'
                    WHERE id = v_transactions(i).id;
                END LOOP;

            p_error_code := 'INSUFFICIENT_BALANCE';
            p_error_message := 'Insufficient balance for processing the transactions.' || v_balance || v_total_amount;
            RETURN;
        END IF;
    EXCEPTION
        WHEN NO_DATA_FOUND THEN
            p_error_code := 'NO_ACCOUNT_FOUND';
            p_error_message := 'No account found for the customer.' || p_customer_id;
            RETURN;
    END;

    -- If sufficient, deduct the total amount from the account balance
    SAVEPOINT sp_deduct_balance;

    UPDATE account
    SET balance = balance - v_total_amount
    WHERE customer_id = p_customer_id;

-- Process each transaction and mark them as COMPLETED
    FOR i IN 1 .. v_transactions.COUNT
        LOOP
            UPDATE transaction
            SET status = 'COMPLETED'
            WHERE id = v_transactions(i).id;

            p_transaction_count := p_transaction_count + 1;
        END LOOP;

    p_total_amount := v_total_amount;

    COMMIT; -- Commit if everything is successful

EXCEPTION
    WHEN OTHERS THEN
        ROLLBACK TO sp_deduct_balance; -- Rollback on error
        p_error_code := SQLCODE;
        p_error_message := SQLERRM;
END PROCESS_BATCH_PAYMENTS;
/

