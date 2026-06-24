package com.iskollect.controller;

import com.iskollect.AppContext;
import com.iskollect.AppNavigator;
import com.iskollect.model.Reward;
import com.iskollect.model.Student;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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
            PopupContext.redemptionConfirmation(reward);
            AppNavigator.showModal(
                "feedbackRedeemPOPUP.fxml",
                "Confirm Reward Redemption"
            );
            if (!PopupContext.redemptionConfirmed()) {
                return;
            }

            Student student = AppContext.selectedStudent();
            AppContext.service().redeem(student, reward);
            BigDecimal balance = student.points().subtract(reward.pointsRequired());
            AppContext.selectStudent(new Student(
                student.id(),
                student.name(),
                student.bottleCount(),
                balance,
                student.registeredAt()
            ));
            PopupContext.redemptionReceipt(reward, balance, LocalDateTime.now());
            close(rewardTable.getScene().getWindow());
            AppNavigator.showModal(
                "feedbackCongratsPOPUP.fxml",
                "Reward Redeemed"
            );
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
