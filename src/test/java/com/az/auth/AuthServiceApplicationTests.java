package com.az.auth;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("local")
class AuthServiceApplicationTests {

    @Autowired(required = false)
    private TestRestTemplate restTemplate;

    @Test
    void contextLoads() {
        // this test will pass if the context loads successfully
    }

    @Test
    void healthEndpointReturnsOK() {
        if (restTemplate != null) {
            ResponseEntity<String> response = restTemplate.getForEntity("/api/v1/health", String.class);
            assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
            assertThat(response.getBody()).isEqualTo("OK");
        }
    }
}
