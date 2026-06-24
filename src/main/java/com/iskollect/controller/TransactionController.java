package com.iskollect.controller;

import com.iskollect.AppContext;
import com.iskollect.model.TransactionEntry;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class TransactionController extends ControllerSupport {
    private static final DateTimeFormatter FORMAT =
        DateTimeFormatter.ofPattern("MMM d, yyyy - h:mm a");
    private static final DateTimeFormatter DATE_TIME_FORMAT =
        DateTimeFormatter.ofPattern("MMMM d, yyyy - h:mm a");

    @FXML private Label dateTimeLabel;
    @FXML private Label totalBottlesLabel;
    @FXML private Label totalRedemptionsLabel;
    @FXML private DatePicker fromDate;
    @FXML private DatePicker toDate;
    @FXML private TableView<TransactionEntry> transactionTable;
    @FXML private TableColumn<TransactionEntry, String> historyStudentColumn;
    @FXML private TableColumn<TransactionEntry, String> historyDateColumn;
    @FXML private TableColumn<TransactionEntry, String> historyTypeColumn;
    @FXML private TableColumn<TransactionEntry, String> historyPointsColumn;

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
            new ReadOnlyObjectWrapper<>(data.getValue().pointsChange().toPlainString())
        );
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
