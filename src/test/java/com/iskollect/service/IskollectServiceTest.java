package com.iskollect.service;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class IskollectServiceTest {
    @Test
    void calculatesHalfPointPerBottle() {
        assertEquals(new BigDecimal("2.50"), IskollectService.calculatePoints(5));
        assertEquals(new BigDecimal("25.00"), IskollectService.calculatePoints(50));
    }

    @Test
    void rejectsNegativeBottleCounts() {
        assertThrows(
            IllegalArgumentException.class,
            () -> IskollectService.calculatePoints(-1)
        );
    }
}
