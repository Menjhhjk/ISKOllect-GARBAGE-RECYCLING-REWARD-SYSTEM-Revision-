package com.iskollect.controller;

import com.iskollect.AppContext;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public final class AddRewardController extends ControllerSupport {
    @FXML private TextField rewardNameField;
    @FXML private TextField pointsField;
    @FXML private TextArea descriptionField;

    @FXML
    private void addReward() {
        try {
            AppContext.service().addReward(
                rewardNameField.getText(),
                descriptionField.getText(),
                pointsField.getText()
            );
            showSuccess("Reward added to the catalog.");
            close(rewardNameField.getScene().getWindow());
        } catch (Exception exception) {
            showError(exception);
        }
    }

    @FXML
    private void clearForm() {
        rewardNameField.clear();
        pointsField.clear();
        descriptionField.clear();
    }

    @FXML
    private void closePopup() {
        close(rewardNameField.getScene().getWindow());
    }
}
