package com.iskollect.controller;

import com.iskollect.AppContext;
import com.iskollect.model.TransactionEntry;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class TransactionController extends ControllerSupport {
    private static final DateTimeFormatter FORMAT =
        DateTimeFormatter.ofPattern("MMM d, yyyy - h:mm a");
    private static final DateTimeFormatter DATE_TIME_FORMAT =
        DateTimeFormatter.ofPattern("MMMM d, yyyy - h:mm a");
    private static final String INCREASE_COLOR = "#0ea925";
    private static final String DECREASE_COLOR = "#c90e0e";

    @FXML private Label dateTimeLabel;
    @FXML private Label totalBottlesLabel;
    @FXML private Label totalRedemptionsLabel;
    @FXML private DatePicker fromDate;
    @FXML private DatePicker toDate;
    @FXML private TableView<TransactionEntry> transactionTable;
    @FXML private TableColumn<TransactionEntry, String> historyStudentColumn;
    @FXML private TableColumn<TransactionEntry, String> historyDateColumn;
    @FXML private TableColumn<TransactionEntry, String> historyTypeColumn;
    @FXML private TableColumn<TransactionEntry, TransactionEntry> historyPointsColumn;

    @FXML
    private void initialize() {
        historyStudentColumn.setCellValueFactory(data ->
            new ReadOnlyObjectWrapper<>(data.getValue().studentName())
        );
        historyDateColumn.setCellValueFactory(data ->
            new ReadOnlyObjectWrapper<>(FORMAT.format(data.getValue().occurredAt()))
        );
        historyTypeColumn.setCellValueFactory(data ->
            new ReadOnlyObjectWrapper<>(
                data.getValue().type() + " - " + data.getValue().details()
            )
        );

        historyPointsColumn.setCellValueFactory(data ->
            new ReadOnlyObjectWrapper<>(data.getValue())
        );
        historyPointsColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(TransactionEntry entry, boolean empty) {
                super.updateItem(entry, empty);
                if (empty || entry == null) {
                    setText(null);
                    setStyle("");
                    return;
                }
                BigDecimal change = entry.pointsChange();
                String sign = change.signum() > 0 ? "+" : "";
                setText(sign + change.toPlainString());
                String color = change.signum() < 0 ? DECREASE_COLOR : INCREASE_COLOR;
                setStyle("-fx-text-fill: " + color + "; -fx-font-weight: bold;");
            }
        });

        fromDate.valueProperty().addListener((obs, oldValue, newValue) -> refresh());
        toDate.valueProperty().addListener((obs, oldValue, newValue) -> refresh());
        refresh();
    }

    @FXML
    private void clearFilters() {
        fromDate.setValue(null);
        toDate.setValue(null);
        refresh();
    }

    private void refresh() {
        try {
            dateTimeLabel.setText(LocalDateTime.now().format(DATE_TIME_FORMAT));
            var all = AppContext.service().transactions(null);
            LocalDate from = fromDate.getValue();
            LocalDate to = toDate.getValue();
            var filtered = all.stream()
                .filter(entry -> from == null
                    || !entry.occurredAt().toLocalDate().isBefore(from))
                .filter(entry -> to == null
                    || !entry.occurredAt().toLocalDate().isAfter(to))
                .toList();
            transactionTable.setItems(FXCollections.observableArrayList(filtered));
            int bottles = filtered.stream()
                .filter(entry -> entry.type().equals("Bottle submission"))
                .mapToInt(entry -> Integer.parseInt(entry.details().split(" ")[0]))
                .sum();
            long redemptions = filtered.stream()
                .filter(entry -> entry.type().equals("Reward redemption"))
                .count();
            totalBottlesLabel.setText(String.valueOf(bottles));
            totalRedemptionsLabel.setText(String.valueOf(redemptions));
        } catch (Exception exception) {
            showError(exception);
        }
    }
}
