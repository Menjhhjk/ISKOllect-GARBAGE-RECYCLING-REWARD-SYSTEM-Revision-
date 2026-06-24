package com.iskollect.model;

import java.math.BigDecimal;

public record Reward(
    int id,
    String name,
    String description,
    BigDecimal pointsRequired,
    boolean available
) {
}
