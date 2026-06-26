package com.iskollect.controller;

import com.iskollect.AppContext;
import com.iskollect.model.Student;
import com.iskollect.service.IskollectService;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.math.BigDecimal;

public final class EditStudentController extends ControllerSupport {
    private static final String INCREASE_COLOR = "#0ea925";
    private static final String DECREASE_COLOR = "#c90e0e";

    @FXML private TextField studentNameField;
    @FXML private TextField bottleCountField;
    @FXML private Label pointsLabel;
    private Student student;

    @FXML
    private void initialize() {
        student = AppContext.selectedStudent();
        studentNameField.setText(student.name());
        bottleCountField.setText(String.valueOf(student.bottleCount()));
        updatePointsPreview();
        bottleCountField.textProperty().addListener((obs, oldValue, newValue) -> updatePointsPreview());
    }

    @FXML
    private void saveStudent() {
        try {
            Student updated = AppContext.service().updateStudent(
                student,
                studentNameField.getText(),
                bottleCountField.getText()
            );
            AppContext.selectStudent(updated);
            showSuccess("Student record updated.");
            close(studentNameField.getScene().getWindow());
        } catch (Exception exception) {
            showError(exception);
        }
    }

    @FXML
    private void clearForm() {
        studentNameField.clear();
        bottleCountField.setText("0");
    }

    @FXML
    private void closePopup() {
        close(studentNameField.getScene().getWindow());
    }

    private void updatePointsPreview() {
        BigDecimal previewedPoints = IskollectService.previewPoints(bottleCountField.getText());
        if (previewedPoints == null) {
            pointsLabel.setText("--");
            pointsLabel.setStyle("-fx-text-fill: " + DECREASE_COLOR + ";");
            return;
        }
        pointsLabel.setText(previewedPoints.toPlainString() + "pts");
        int comparison = previewedPoints.compareTo(student.points());
        String color = comparison < 0 ? DECREASE_COLOR : INCREASE_COLOR;
        pointsLabel.setStyle("-fx-text-fill: " + color + ";");
    }
}
