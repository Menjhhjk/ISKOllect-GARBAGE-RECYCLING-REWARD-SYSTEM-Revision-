package com.iskollect.controller;

import com.iskollect.AppContext;
import com.iskollect.model.Reward;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;

public final class EditRewardController extends ControllerSupport {
    @FXML private TextField rewardNameField;
    @FXML private TextField pointsField;
    @FXML private TextArea descriptionField;
    @FXML private ToggleButton availabilityToggle;
    private Reward reward;

    @FXML
    private void initialize() {
        reward = RewardEditorContext.selected();
        rewardNameField.setText(reward.name());
        pointsField.setText(reward.pointsRequired().toPlainString());
        descriptionField.setText(reward.description());
        availabilityToggle.setSelected(!reward.available());
        updateAvailabilityText();
    }

    @FXML
    private void saveReward() {
        try {
            AppContext.service().updateReward(
                reward,
                rewardNameField.getText(),
                descriptionField.getText(),
                pointsField.getText(),
                !availabilityToggle.isSelected()
            );
            showSuccess("Reward updated.");
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

    @FXML
    private void updateAvailabilityText() {
        availabilityToggle.setText(
            availabilityToggle.isSelected() ? "NO" : "YES"
        );
    }
}
