package com.iskollect.controller;

import com.iskollect.AppContext;
import com.iskollect.AppNavigator;
import com.iskollect.model.Student;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;

import java.math.BigDecimal;

public final class BottleSubmitController extends ControllerSupport {
    @FXML private TextField bottleCountField;

    @FXML
    private void handleCancel() {
        bottleCountField.clear();
    }

    @FXML
    private void submitBottles() {
        try {
            Student student = AppContext.selectedStudent();
            int bottles = Integer.parseInt(bottleCountField.getText().trim());
            var points = AppContext.service().submitBottles(
                student, bottleCountField.getText()
            );
            BigDecimal balance = student.points().add(points);
            AppContext.selectStudent(new Student(
                student.id(),
                student.name(),
                student.bottleCount() + bottles,
                balance,
                student.registeredAt()
            ));
            PopupContext.bottleSubmission(bottles, points, balance);
            close(bottleCountField.getScene().getWindow());
            AppNavigator.showModal(
                "feedbackTYPOPUP.fxml",
                "Bottle Submission Recorded"
            );
        } catch (Exception exception) {
            showError(exception);
        }
    }

    @FXML
    private void closePopup() {
        close(bottleCountField.getScene().getWindow());
    }
}
