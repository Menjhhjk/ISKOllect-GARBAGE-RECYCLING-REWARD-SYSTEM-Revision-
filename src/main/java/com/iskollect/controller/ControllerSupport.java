package com.iskollect.controller;

import com.iskollect.AppNavigator;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Window;

import java.io.IOException;

abstract class ControllerSupport {
    protected void showError(Exception exception) {
        String message = exception.getMessage();
        if (message == null || message.isBlank()) {
            message = "The requested action could not be completed.";
        }
        Alert alert = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
        alert.setHeaderText("Unable to complete the action");
        alert.showAndWait();
    }

    protected void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, message, ButtonType.OK);
        alert.setHeaderText("Success");
        alert.showAndWait();
    }

    protected void close(Window window) {
        if (window != null) {
            window.hide();
        }
    }

    public void goDashboard() {
        navigate(AppNavigator::showDashboard);
    }

    public void goToBottleRecords() {
        goDashboard();
    }

    public void goRewards() {
        navigate(AppNavigator::showRewards);
    }

    public void goHistory() {
        navigate(AppNavigator::showHistory);
    }

    public void handleLogout() {
        navigate(AppNavigator::showLogin);
    }

    private void navigate(Navigation navigation) {
        try {
            navigation.run();
        } catch (IOException exception) {
            showError(exception);
        }
    }

    @FunctionalInterface
    private interface Navigation {
        void run() throws IOException;
    }
}
