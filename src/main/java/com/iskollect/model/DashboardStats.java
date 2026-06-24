package com.iskollect.model;

import java.math.BigDecimal;

public record DashboardStats(
    int students,
    int bottles,
    BigDecimal availablePoints,
    int redemptions
) {
}
