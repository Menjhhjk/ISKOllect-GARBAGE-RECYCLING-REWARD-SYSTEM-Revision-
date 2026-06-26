package com.iskollect.controller;

import com.iskollect.AppContext;
import com.iskollect.AppNavigator;
import com.iskollect.model.Reward;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class RewardsCatalogController extends ControllerSupport {
    private static final DateTimeFormatter DATE_TIME_FORMAT =
        DateTimeFormatter.ofPattern("MMMM d, yyyy - h:mm a");
    private static final Image EDIT_ICON = new Image(
        RewardsCatalogController.class.getResource("/com/iskollect/assets/Edit.png").toExternalForm()
    );
    private static final Image DELETE_ICON = new Image(
        RewardsCatalogController.class.getResource("/com/iskollect/assets/delete.png").toExternalForm()
    );

    @FXML private Label dateTimeLabel;
    @FXML private Label redeemedCountLabel;
    @FXML private TableView<Reward> rewardTable;
    @FXML private TableColumn<Reward, String> rewardNameColumn;
    @FXML private TableColumn<Reward, String> descriptionColumn;
    @FXML private TableColumn<Reward, String> pointsColumn;
    @FXML private TableColumn<Reward, Reward> availabilityColumn;
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
            new ReadOnlyObjectWrapper<>(data.getValue())
        );
        availabilityColumn.setCellFactory(column -> new TableCell<>() {
            private final ToggleButton toggle = new ToggleButton();
            {
                toggle.getStyleClass().add("toggle-button");
                toggle.setOnAction(event -> toggleAvailability(getItem(), toggle.isSelected()));
            }
            @Override
            protected void updateItem(Reward reward, boolean empty) {
                super.updateItem(reward, empty);
                if (empty || reward == null) {
                    setGraphic(null);
                    return;
                }
                toggle.setSelected(reward.available());
                toggle.setText(reward.available() ? "YES" : "NO");
                setGraphic(toggle);
            }
        });

        rewardActionColumn.setCellValueFactory(data ->
            new ReadOnlyObjectWrapper<>(data.getValue())
        );
        rewardActionColumn.setCellFactory(column -> new TableCell<>() {
            private final ImageView editView = new ImageView(EDIT_ICON);
            private final ImageView deleteView = new ImageView(DELETE_ICON);
            private final HBox actions = new HBox(8, editView, deleteView);
            {
                editView.setFitWidth(20);
                editView.setFitHeight(20);
                editView.getStyleClass().add("simage-view");
                editView.setOnMouseClicked(event -> editReward(getItem()));

                deleteView.setFitWidth(20);
                deleteView.setFitHeight(20);
                deleteView.getStyleClass().add("simage-view");
                deleteView.setOnMouseClicked(event -> deleteReward(getItem()));

                actions.setAlignment(Pos.CENTER);
                actions.setPadding(new Insets(0, 4, 0, 4));
            }
            @Override
            protected void updateItem(Reward reward, boolean empty) {
                super.updateItem(reward, empty);
                setGraphic(empty || reward == null ? null : actions);
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

    private void deleteReward(Reward reward) {
        if (reward == null) {
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

    private void toggleAvailability(Reward reward, boolean available) {
        if (reward == null) {
            return;
        }
        try {
            AppContext.service().updateReward(
                reward,
                reward.name(),
                reward.description(),
                reward.pointsRequired().toPlainString(),
                available
            );
            refresh();
        } catch (Exception exception) {
            showError(exception);
            refresh();
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
