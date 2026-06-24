package com.iskollect.controller;

import com.iskollect.AppContext;
import com.iskollect.model.Reward;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

public final class RewardRedemptionController extends ControllerSupport {
    @FXML private Label studentNameLabel;
    @FXML private Label balanceLabel;
    @FXML private TableView<Reward> rewardTable;
    @FXML private TableColumn<Reward, String> rewardNameColumn;
    @FXML private TableColumn<Reward, String> rewardDescriptionColumn;
    @FXML private TableColumn<Reward, String> rewardPointsColumn;
    @FXML private TableColumn<Reward, Reward> redeemActionColumn;

    @FXML
    private void initialize() {
        var student = AppContext.selectedStudent();
        studentNameLabel.setText(student.name());
        balanceLabel.setText(student.points().toPlainString() + " points");
        rewardNameColumn.setCellValueFactory(data ->
            new ReadOnlyObjectWrapper<>(data.getValue().name())
        );
        rewardDescriptionColumn.setCellValueFactory(data ->
            new ReadOnlyObjectWrapper<>(data.getValue().description())
        );
        rewardPointsColumn.setCellValueFactory(data ->
            new ReadOnlyObjectWrapper<>(data.getValue().pointsRequired().toPlainString())
        );
        redeemActionColumn.setCellValueFactory(data ->
            new ReadOnlyObjectWrapper<>(data.getValue())
        );
        redeemActionColumn.setCellFactory(column -> new TableCell<>() {
            private final Button redeem = new Button("Redeem");
            {
                redeem.getStyleClass().add("secondary-button");
                redeem.setOnAction(event -> redeem(getItem()));
            }
            @Override
            protected void updateItem(Reward reward, boolean empty) {
                super.updateItem(reward, empty);
                setGraphic(empty || reward == null ? null : redeem);
            }
        });
        refresh();
    }

    @FXML
    private void redeemSelected() {
        redeem(rewardTable.getSelectionModel().getSelectedItem());
    }

    @FXML
    private void closePopup() {
        close(rewardTable.getScene().getWindow());
    }

    private void redeem(Reward reward) {
        if (reward == null) {
            showError(new IllegalArgumentException("Select a reward first."));
            return;
        }
        try {
            AppContext.service().redeem(AppContext.selectedStudent(), reward);
            showSuccess("Reward redeemed successfully.");
            close(rewardTable.getScene().getWindow());
        } catch (Exception exception) {
            showError(exception);
        }
    }

    private void refresh() {
        try {
            rewardTable.setItems(FXCollections.observableArrayList(
                AppContext.service().rewards().stream()
                    .filter(Reward::available)
                    .toList()
            ));
        } catch (Exception exception) {
            showError(exception);
        }
    }
}
