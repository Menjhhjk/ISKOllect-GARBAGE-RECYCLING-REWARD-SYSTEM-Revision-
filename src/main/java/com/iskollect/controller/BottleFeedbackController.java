package com.iskollect.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public final class BottleFeedbackController extends ControllerSupport {
    @FXML private Label submissionLabel;
    @FXML private Label balanceLabel;

    @FXML
    private void initialize() {
        if (PopupContext.awardedPoints() == null) {
            return;
        }
        submissionLabel.setText(
            PopupContext.submittedBottles() + " bottles recorded (+"
                + PopupContext.awardedPoints().toPlainString()
                + " pts). One step closer"
        );
        balanceLabel.setText(
            "Student's current balance "
                + PopupContext.currentBalance().toPlainString() + " pts"
        );
    }

    @FXML
    private void closePopup() {
        close(submissionLabel.getScene().getWindow());
    }
}
