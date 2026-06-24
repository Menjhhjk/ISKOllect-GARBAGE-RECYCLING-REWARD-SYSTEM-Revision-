package com.iskollect.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class RedemptionSuccessController extends ControllerSupport {
    private static final DateTimeFormatter FORMAT =
        DateTimeFormatter.ofPattern("MMMM d, yyyy - h:mm a");

    @FXML private Label rewardNameLabel;
    @FXML private Label dateTimeLabel;
    @FXML private Label balanceLabel;

    @FXML
    private void initialize() {
        var reward = PopupContext.reward();
        if (reward != null) {
            rewardNameLabel.setText(reward.name());
        }
        var occurredAt = PopupContext.redemptionTime();
        dateTimeLabel.setText(
            (occurredAt == null ? LocalDateTime.now() : occurredAt).format(FORMAT)
        );
        if (PopupContext.currentBalance() != null) {
            balanceLabel.setText(
                "The student's current balance is "
                    + PopupContext.currentBalance().toPlainString() + " pts."
            );
        }
    }

    @FXML
    private void closePopup() {
        close(rewardNameLabel.getScene().getWindow());
    }
}
