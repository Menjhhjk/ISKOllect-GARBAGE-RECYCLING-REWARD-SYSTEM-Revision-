package com.iskollect.controller;

import com.iskollect.AppContext;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;

public final class BottleSubmitController extends ControllerSupport {
    @FXML private TextField bottleCountField;

    @FXML
    private void handleCancel() {
        bottleCountField.clear();
    }

    @FXML
    private void submitBottles() {
        try {
            var points = AppContext.service().submitBottles(
                AppContext.selectedStudent(), bottleCountField.getText()
            );
            showSuccess(
                bottleCountField.getText().trim() + " bottles recorded (+"
                    + points.toPlainString() + " pts)."
            );
            close(bottleCountField.getScene().getWindow());
        } catch (Exception exception) {
            showError(exception);
        }
    }

    @FXML
    private void closePopup() {
        close(bottleCountField.getScene().getWindow());
    }
}
