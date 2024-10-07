package com.anazih.ptmback.auth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.client.RestTemplate;

import javax.naming.AuthenticationException;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class AuthService {
    @Value("${auth0.audience}")
    private String audience;

    @Value("${auth0.clientId}")
    private String clientId;

    @Value("${auth0.clientSecret}")
    private String clientSecret;

    @Value("${auth0.domain}")
    private String domain;

    public AuthResponse login(@RequestBody AuthRequest authRequest) throws AuthenticationException {
        RestTemplate restTemplate = new RestTemplate();

        String url = domain + "/oauth/token";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, String> body = new HashMap<>();
        body.put("grant_type", "password");
        body.put("username", authRequest.email());
        body.put("password", authRequest.password());
        body.put("audience", audience);
        body.put("client_id", clientId);
        body.put("client_secret", clientSecret);

        HttpEntity<Map<String, String>> entity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(response.getBody());

            log.info("User logged in: {}", authRequest.email());
            return new AuthResponse(jsonNode.get("access_token").asText());
        } catch (Exception e) {
            log.error("Failed to authenticate user: {}", authRequest.email(), e);
            throw new AuthenticationException("Invalid username or password");
        }

    }

}