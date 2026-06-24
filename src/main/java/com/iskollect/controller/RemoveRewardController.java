package com.iskollect.controller;

import com.iskollect.AppContext;
import com.iskollect.model.Reward;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public final class RemoveRewardController extends ControllerSupport {
    @FXML private Label rewardNameLabel;

    private Reward reward;

    @FXML
    private void initialize() {
        reward = RewardEditorContext.selected();
        if (reward == null) {
            throw new IllegalStateException("No reward was selected for deletion.");
        }
        rewardNameLabel.setText("'" + reward.name() + "'");
    }

    @FXML
    private void cancel() {
        close(rewardNameLabel.getScene().getWindow());
    }

    @FXML
    private void deleteReward() {
        try {
            AppContext.service().deleteReward(reward);
            close(rewardNameLabel.getScene().getWindow());
            showSuccess("Reward deleted.");
        } catch (Exception exception) {
            showError(exception);
        }
    }
}
