package com.iskollect.controller;

import com.iskollect.AppContext;
import com.iskollect.AppNavigator;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;

public final class LoginController extends ControllerSupport {
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;

    @FXML
    private void initialize() {
        passwordField.setOnAction(event -> handleLogin());
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
}
