package com.iskollect.controller;

import com.iskollect.AppContext;
import com.iskollect.model.Student;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public final class RemoveStudentController extends ControllerSupport {
    @FXML private Label studentNameLabel;

    private Student student;

    @FXML
    private void initialize() {
        student = AppContext.selectedStudent();
        if (student == null) {
            throw new IllegalStateException("No student was selected for deletion.");
        }
        studentNameLabel.setText("'" + student.name() + "'");
    }

    @FXML
    private void cancel() {
        close(studentNameLabel.getScene().getWindow());
    }

    @FXML
    private void deleteStudent() {
        try {
            AppContext.service().deleteStudent(student);
            close(studentNameLabel.getScene().getWindow());
            showSuccess("Student record deleted.");
        } catch (Exception exception) {
            showError(exception);
        }
    }
}
