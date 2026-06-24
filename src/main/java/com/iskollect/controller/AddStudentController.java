package com.iskollect.controller;

import com.iskollect.AppContext;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public final class AddStudentController extends ControllerSupport {
    @FXML private TextField studentNameField;
    @FXML private TextField bottleCountField;
    @FXML private Label startingBalanceLabel;

    @FXML
    private void initialize() {
        bottleCountField.textProperty().addListener((obs, oldValue, value) -> {
            try {
                int bottles = Integer.parseInt(value.trim());
                startingBalanceLabel.setText(
                    "Starting Balance: "
                        + AppContext.service().calculatePoints(bottles).toPlainString()
                        + " pts"
                );
            } catch (Exception exception) {
                startingBalanceLabel.setText("Starting Balance: 0.00 pts");
            }
        });
    }

    @FXML
    private void registerStudent() {
        try {
            var student = AppContext.service().registerStudent(
                studentNameField.getText(), bottleCountField.getText()
            );
            AppContext.selectStudent(student);
            showSuccess(
                student.name() + " was registered with "
                    + student.bottleCount() + " bottles and "
                    + student.points().toPlainString() + " points."
            );
            close(studentNameField.getScene().getWindow());
        } catch (Exception exception) {
            showError(exception);
        }
    }

    @FXML
    private void clearForm() {
        studentNameField.clear();
        bottleCountField.clear();
    }

    @FXML
    private void closePopup() {
        close(studentNameField.getScene().getWindow());
    }
}
