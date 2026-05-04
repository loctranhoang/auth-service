package com.az.auth;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("local")
class AuthServiceApplicationTests {

    @Test
    void contextLoads() {
    }

    @Test
    @ActiveProfiles("dev")
    void contextLoadsDev() {
    }

    @Test
    @ActiveProfiles("staging")
    void contextLoadsStaging() {
    }

    @Test
    @ActiveProfiles("prod")
    void contextLoadsProd() {
    }
}