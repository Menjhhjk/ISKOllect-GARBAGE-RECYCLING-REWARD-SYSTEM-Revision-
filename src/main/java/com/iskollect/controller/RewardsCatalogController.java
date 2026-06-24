package com.iskollect.controller;

import com.iskollect.AppContext;
import com.iskollect.AppNavigator;
import com.iskollect.model.Reward;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseEvent;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class RewardsCatalogController extends ControllerSupport {
    private static final DateTimeFormatter DATE_TIME_FORMAT =
        DateTimeFormatter.ofPattern("MMMM d, yyyy - h:mm a");

    @FXML private Label dateTimeLabel;
    @FXML private Label redeemedCountLabel;
    @FXML private TableView<Reward> rewardTable;
    @FXML private TableColumn<Reward, String> rewardNameColumn;
    @FXML private TableColumn<Reward, String> descriptionColumn;
    @FXML private TableColumn<Reward, String> pointsColumn;
    @FXML private TableColumn<Reward, String> availabilityColumn;
    @FXML private TableColumn<Reward, Reward> rewardActionColumn;

    @FXML
    private void initialize() {
        rewardNameColumn.setCellValueFactory(data ->
            new ReadOnlyObjectWrapper<>(data.getValue().name())
        );
        descriptionColumn.setCellValueFactory(data ->
            new ReadOnlyObjectWrapper<>(data.getValue().description())
        );
        pointsColumn.setCellValueFactory(data ->
            new ReadOnlyObjectWrapper<>(data.getValue().pointsRequired().toPlainString())
        );
        availabilityColumn.setCellValueFactory(data ->
            new ReadOnlyObjectWrapper<>(data.getValue().available() ? "YES" : "NO")
        );
        rewardActionColumn.setCellValueFactory(data ->
            new ReadOnlyObjectWrapper<>(data.getValue())
        );
        rewardActionColumn.setCellFactory(column -> new TableCell<>() {
            private final Button edit = new Button("Edit");
            {
                edit.getStyleClass().add("secondary-button");
                edit.setOnAction(event -> editReward(getItem()));
            }
            @Override
            protected void updateItem(Reward reward, boolean empty) {
                super.updateItem(reward, empty);
                setGraphic(empty || reward == null ? null : edit);
            }
        });
        refresh();
    }

    @FXML
    private void addReward() {
        try {
            AppNavigator.showModal("AddcouponPOPUP.fxml", "Add Reward");
            refresh();
        } catch (Exception exception) {
            showError(exception);
        }
    }

    @FXML
    private void editSelectedReward(MouseEvent event) {
        editReward(rewardTable.getSelectionModel().getSelectedItem());
    }

    @FXML
    private void deleteSelectedReward(MouseEvent event) {
        Reward reward = rewardTable.getSelectionModel().getSelectedItem();
        if (reward == null) {
            showError(new IllegalArgumentException("Select a reward first."));
            return;
        }
        RewardEditorContext.select(reward);
        try {
            AppNavigator.showModal("removeRewardPOPUP.fxml", "Remove Reward");
            refresh();
        } catch (Exception exception) {
            showError(exception);
        }
    }

    private void editReward(Reward reward) {
        if (reward == null) {
            showError(new IllegalArgumentException("Select a reward first."));
            return;
        }
        RewardEditorContext.select(reward);
        try {
            AppNavigator.showModal("editcouponPOPUP.fxml", "Edit Reward");
            refresh();
        } catch (Exception exception) {
            showError(exception);
        }
    }

    private void refresh() {
        try {
            rewardTable.setItems(FXCollections.observableArrayList(
                AppContext.service().rewards()
            ));
            int redemptions = AppContext.service().dashboardStats().redemptions();
            redeemedCountLabel.setText(
                redemptions + (redemptions == 1 ? " Coupon" : " Coupons")
            );
            dateTimeLabel.setText(LocalDateTime.now().format(DATE_TIME_FORMAT));
        } catch (Exception exception) {
            showError(exception);
        }
    }
}
