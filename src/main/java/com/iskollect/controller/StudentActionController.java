package com.iskollect.controller;

import com.iskollect.AppContext;
import com.iskollect.AppNavigator;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;

public final class StudentActionController extends ControllerSupport {
    @FXML private Label studentNameLabel;
    @FXML private Label balanceLabel;
    @FXML private Pane addBottlePane;
    @FXML private Pane redeemPane;

    @FXML
    private void initialize() {
        var student = AppContext.selectedStudent();
        studentNameLabel.setText(student.name());
        balanceLabel.setText(student.points().toPlainString() + " pts");
    }

    @FXML
    private void openBottleSubmission() {
        try {
            AppNavigator.showModal("submitbottlepopup.fxml", "Submit Bottles");
            close(addBottlePane.getScene().getWindow());
        } catch (Exception exception) {
            showError(exception);
        }
    }

    @FXML
    private void openRewards() {
        try {
            AppNavigator.showModal("rewardsPOPUP.fxml", "Redeem Reward");
            close(redeemPane.getScene().getWindow());
        } catch (Exception exception) {
            showError(exception);
        }
    }

    @FXML
    private void closePopup() {
        close(studentNameLabel.getScene().getWindow());
    }
}
