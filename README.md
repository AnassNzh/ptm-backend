## Oracle Database 21c Installation on Docker

## Prerequisites

Make sure you have Docker installed on your system.

## Installation Steps

1. Create an Oracle Database container using the following command:

   ```bash
   docker container create -it --name oracle-db -p 1521:1521 -e ORACLE_PWD=test123 container-registry.oracle.com/database/express:latest
   ```

   

2. Start the Oracle Database container:

   ```bash
   docker start oracle-db
   ```

   

3. Database Credentials:

   - **Host:** localhost
   - **Port:** 1521
   - **Username:** system
   - **Password:** test123
   - **SID:** xe

## Auth0 Configuration

Configuration:

- `<DOMAIN>`: 'dev-lyg6ninkdrdhmyzw'
- `<CLIENT_ID>`: 'knIxdXqtohcvBJhAYvN8QFJJYKsh5mff'
- `<CLIENT_SECRET>`: RW5U0ZVOBJzgm83cmI4Tko2grATYcL5PvXJfVSWTldZ2KuZUSliDQ-5pgI_IGH9w