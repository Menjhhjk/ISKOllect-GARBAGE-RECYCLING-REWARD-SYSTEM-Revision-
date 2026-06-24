package com.iskollect.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record Student(
    int id,
    String name,
    int bottleCount,
    BigDecimal points,
    LocalDateTime registeredAt
) {
}
