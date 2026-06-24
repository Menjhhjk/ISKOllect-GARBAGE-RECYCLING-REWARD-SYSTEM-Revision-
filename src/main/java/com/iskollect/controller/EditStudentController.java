package com.iskollect.controller;

import com.iskollect.AppContext;
import com.iskollect.model.Student;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public final class EditStudentController extends ControllerSupport {
    @FXML private TextField studentNameField;
    @FXML private TextField bottleCountField;
    @FXML private Label pointsLabel;
    private Student student;

    @FXML
    private void initialize() {
        student = AppContext.selectedStudent();
        studentNameField.setText(student.name());
        bottleCountField.setText(String.valueOf(student.bottleCount()));
        bottleCountField.setEditable(false);
        pointsLabel.setText(student.points().toPlainString() + "pts");
    }

    @FXML
    private void saveStudent() {
        try {
            AppContext.service().renameStudent(student, studentNameField.getText());
            showSuccess("Student record updated.");
            close(studentNameField.getScene().getWindow());
        } catch (Exception exception) {
            showError(exception);
        }
    }

    @FXML
    private void clearForm() {
        studentNameField.clear();
    }

    @FXML
    private void closePopup() {
        close(studentNameField.getScene().getWindow());
    }
}
