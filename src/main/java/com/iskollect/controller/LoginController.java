package com.iskollect.controller;

import com.iskollect.AppContext;
import com.iskollect.AppNavigator;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.geometry.Insets;

public final class LoginController extends ControllerSupport {
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Button createAdminButton;

    @FXML
    private void initialize() {
        passwordField.setOnAction(event -> handleLogin());
        Platform.runLater(this::refreshInitialAdminOption);
    }

    @FXML
    private void handleLogin() {
        try {
            AppContext.service().verifyDatabase();
            if (!AppContext.service().authenticateAdminPassword(passwordField.getText())) {
                throw new IllegalArgumentException("Invalid administrator password.");
            }
            AppNavigator.showDashboard();
        } catch (Exception exception) {
            passwordField.clear();
            showError(exception);
        }
    }

    @FXML
    private void handleCreateInitialAdmin() {
        try {
            if (!AppContext.service().needsInitialAdmin()) {
                refreshInitialAdminOption();
                throw new IllegalStateException("An administrator account already exists.");
            }

            TextField usernameField = new TextField();
            usernameField.setPromptText("Administrator username");
            PasswordField newPasswordField = new PasswordField();
            newPasswordField.setPromptText("At least 10 characters");
            PasswordField confirmationField = new PasswordField();
            confirmationField.setPromptText("Repeat password");

            GridPane form = new GridPane();
            form.setHgap(12);
            form.setVgap(10);
            form.setPadding(new Insets(10));
            form.addRow(0, new Label("Username:"), usernameField);
            form.addRow(1, new Label("Password:"), newPasswordField);
            form.addRow(2, new Label("Confirm password:"), confirmationField);

            ButtonType createType = new ButtonType(
                    "Create administrator",
                    ButtonBar.ButtonData.OK_DONE
            );
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.initOwner(AppContext.stage());
            dialog.setTitle("First administrator setup");
            dialog.setHeaderText(
                    "Create the administrator account used to access ISKOllect."
            );
            dialog.getDialogPane().setContent(form);
            dialog.getDialogPane().getButtonTypes().addAll(
                    createType,
                    ButtonType.CANCEL
            );

            Button createButton = (Button) dialog.getDialogPane().lookupButton(createType);
            createButton.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
                try {
                    AppContext.service().createInitialAdmin(
                            usernameField.getText(),
                            newPasswordField.getText(),
                            confirmationField.getText()
                    );
                    showSuccess("The administrator account was created. You may now log in.");
                } catch (Exception exception) {
                    event.consume();
                    showError(exception);
                }
            });

            Platform.runLater(usernameField::requestFocus);
            dialog.showAndWait();
            refreshInitialAdminOption();
        } catch (Exception exception) {
            showError(exception);
        }
    }

    private void refreshInitialAdminOption() {
        try {
            boolean needed = AppContext.service().needsInitialAdmin();
            createAdminButton.setManaged(needed);
            createAdminButton.setVisible(needed);
            loginButton.setDisable(needed);
            passwordField.setDisable(needed);
        } catch (Exception exception) {
            System.err.println("[DEBUG] needsInitialAdmin() failed:");
            exception.printStackTrace();
            createAdminButton.setManaged(false);
            createAdminButton.setVisible(false);
        }
    }
}