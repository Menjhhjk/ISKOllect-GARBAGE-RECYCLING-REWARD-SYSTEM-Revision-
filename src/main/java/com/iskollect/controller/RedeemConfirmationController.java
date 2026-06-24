package com.iskollect.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public final class RedeemConfirmationController extends ControllerSupport {
    @FXML private Label rewardNameLabel;

    @FXML
    private void initialize() {
        var reward = PopupContext.reward();
        if (reward != null) {
            rewardNameLabel.setText(
                "Redeem " + reward.name() + " for "
                    + reward.pointsRequired().toPlainString() + " points?"
            );
        }
    }

    @FXML
    private void confirm() {
        PopupContext.confirmRedemption();
        close(rewardNameLabel.getScene().getWindow());
    }

    @FXML
    private void cancel() {
        close(rewardNameLabel.getScene().getWindow());
    }
}
