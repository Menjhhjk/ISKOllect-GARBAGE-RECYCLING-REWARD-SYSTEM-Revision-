package com.iskollect.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public final class RegistrationSuccessController extends ControllerSupport {
    @FXML private Label studentLineLabel;
    @FXML private Label bottlesLineLabel;
    @FXML private Label balanceLineLabel;

    @FXML
    private void initialize() {
        var student = PopupContext.registeredStudent();
        if (student == null) {
            return;
        }
        studentLineLabel.setText(
            "Welcome to ISKOllect! " + student.name() + " has been"
        );
        bottlesLineLabel.setText(
            "registered and their first " + student.bottleCount() + " bottles are"
        );
        balanceLineLabel.setText(
            "recorded. Their starting balance is "
                + student.points().toPlainString() + " pts."
        );
    }

    @FXML
    private void closePopup() {
        close(studentLineLabel.getScene().getWindow());
    }
}
