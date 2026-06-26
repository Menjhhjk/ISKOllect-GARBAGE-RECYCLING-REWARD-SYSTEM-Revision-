package com.iskollect.controller;

import com.iskollect.AppContext;
import com.iskollect.AppNavigator;
import com.iskollect.model.Student;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class DashboardController extends ControllerSupport {
    private static final DateTimeFormatter DATE_TIME_FORMAT =
        DateTimeFormatter.ofPattern("MMMM d, yyyy - h:mm a");
    private static final Image EDIT_ICON =
        new Image(DashboardController.class.getResource("/com/iskollect/assets/Edit.png").toExternalForm());
    private static final Image DELETE_ICON =
        new Image(DashboardController.class.getResource("/com/iskollect/assets/delete.png").toExternalForm());

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
            private final ImageView editView = new ImageView(EDIT_ICON);
            private final ImageView deleteView = new ImageView(DELETE_ICON);
            private final HBox actions = new HBox(8, editView, deleteView);
            {
                editView.setFitWidth(20);
                editView.setFitHeight(20);
                editView.getStyleClass().add("simage-view");
                editView.setOnMouseClicked(event -> editStudent(getItem()));

                deleteView.setFitWidth(20);
                deleteView.setFitHeight(20);
                deleteView.getStyleClass().add("simage-view");
                deleteView.setOnMouseClicked(event -> deleteStudent(getItem()));

                actions.setAlignment(Pos.CENTER);
                actions.setPadding(new Insets(0, 4, 0, 4));
            }
            @Override
            protected void updateItem(Student student, boolean empty) {
                super.updateItem(student, empty);
                setGraphic(empty || student == null ? null : actions);
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

    private void editStudent(Student student) {
        if (student == null) {
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

    private void deleteStudent(Student student) {
        if (student == null) {
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
