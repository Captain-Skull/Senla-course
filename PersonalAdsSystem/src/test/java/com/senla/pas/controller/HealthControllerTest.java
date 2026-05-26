package com.senla.pas.controller;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class HealthControllerTest {

    private final HealthController healthController = new HealthController();

    @Test
    void healthCheck_positive() {
        ResponseEntity<Map<String, Object>> result = healthController.healthCheck();

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals("UP", result.getBody().get("status"));
        assertEquals("Personal add system", result.getBody().get("service"));
    }

    @Test
    void healthCheck_negative_notCreated() {
        ResponseEntity<Map<String, Object>> result = healthController.healthCheck();

        assertNotEquals(HttpStatus.CREATED, result.getStatusCode());
    }

    @Test
    void healthCheck_npeSafety() {
        assertDoesNotThrow(healthController::healthCheck);
    }
}
