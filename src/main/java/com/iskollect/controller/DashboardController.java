package com.iskollect.controller;

import com.iskollect.AppContext;
import com.iskollect.AppNavigator;
import com.iskollect.model.Student;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class DashboardController extends ControllerSupport {
    private static final DateTimeFormatter DATE_TIME_FORMAT =
        DateTimeFormatter.ofPattern("MMMM d, yyyy - h:mm a");

    @FXML private Label dateTimeLabel;
    @FXML private TextField searchField;
    @FXML private Button addStudentButton;
    @FXML private TableView<Student> studentTable;
    @FXML private TableColumn<Student, String> studentNameColumn;
    @FXML private TableColumn<Student, Integer> bottleColumn;
    @FXML private TableColumn<Student, String> pointsColumn;
    @FXML private TableColumn<Student, Student> actionColumn;

    @FXML
    private void initialize() {
        studentNameColumn.setCellValueFactory(data ->
            new ReadOnlyObjectWrapper<>(data.getValue().name())
        );
        bottleColumn.setCellValueFactory(data ->
            new ReadOnlyObjectWrapper<>(data.getValue().bottleCount())
        );
        pointsColumn.setCellValueFactory(data ->
            new ReadOnlyObjectWrapper<>(data.getValue().points().toPlainString())
        );
        actionColumn.setCellValueFactory(data ->
            new ReadOnlyObjectWrapper<>(data.getValue())
        );
        actionColumn.setCellFactory(column -> new TableCell<>() {
            private final Button assist = new Button("Assist");
            {
                assist.getStyleClass().add("secondary-button");
                assist.setOnAction(event -> openStudent(getItem()));
            }
            @Override
            protected void updateItem(Student student, boolean empty) {
                super.updateItem(student, empty);
                setGraphic(empty || student == null ? null : assist);
            }
        });
        studentTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                openStudent(studentTable.getSelectionModel().getSelectedItem());
            }
        });
        searchField.textProperty().addListener((obs, oldValue, newValue) -> refresh());
        refresh();
    }

    @FXML
    private void addStudent() {
        try {
            AppNavigator.showModal("AddstudentPOPUP.fxml", "Register Student");
            refresh();
        } catch (Exception exception) {
            showError(exception);
        }
    }

    @FXML
    private void editSelectedStudent(MouseEvent event) {
        Student student = studentTable.getSelectionModel().getSelectedItem();
        if (student == null) {
            showError(new IllegalArgumentException("Select a student first."));
            return;
        }
        AppContext.selectStudent(student);
        try {
            AppNavigator.showModal("editStudent.fxml", "Edit Student");
            refresh();
        } catch (Exception exception) {
            showError(exception);
        }
    }

    @FXML
    private void deleteSelectedStudent(MouseEvent event) {
        Student student = studentTable.getSelectionModel().getSelectedItem();
        if (student == null) {
            showError(new IllegalArgumentException("Select a student first."));
            return;
        }
        AppContext.selectStudent(student);
        try {
            AppNavigator.showModal("removeStudentPOPUP.fxml", "Remove Student");
            refresh();
        } catch (Exception exception) {
            showError(exception);
        }
    }

    private void openStudent(Student student) {
        if (student == null) {
            return;
        }
        AppContext.selectStudent(student);
        try {
            AppNavigator.showModal("clickstudentPOPUP.fxml", "Student Transaction");
            refresh();
        } catch (IOException exception) {
            showError(exception);
        }
    }

    private void refresh() {
        try {
            dateTimeLabel.setText(LocalDateTime.now().format(DATE_TIME_FORMAT));
            studentTable.setItems(FXCollections.observableArrayList(
                AppContext.service().students(
                    searchField == null ? null : searchField.getText()
                )
            ));
        } catch (Exception exception) {
            showError(exception);
        }
    }
}
