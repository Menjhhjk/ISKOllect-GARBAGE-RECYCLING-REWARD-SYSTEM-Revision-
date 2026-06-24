package com.iskollect.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransactionEntry(
    LocalDateTime occurredAt,
    String studentName,
    String type,
    String details,
    BigDecimal pointsChange
) {
}
