package com.datastream;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Smoke test verifying the Spring application context loads successfully.
 */
@SpringBootTest
@ActiveProfiles("test")
class DatastreamApiApplicationIT {

    /**
     * Verifies the Spring application context loads without errors.
     */
    @Test
    void should_LoadApplicationContext_When_ApplicationStarts() {
        // Context loads successfully if this test passes
    }
}
