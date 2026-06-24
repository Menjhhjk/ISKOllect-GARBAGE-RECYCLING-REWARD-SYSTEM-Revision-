package com.iskollect;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;

public final class AppNavigator {
    private static final String FXML = "/com/iskollect/fxml/";

    private AppNavigator() {
    }

    public static void showLogin() throws IOException {
        showMain("login.fxml", "ISKOllect - Admin Login");
    }

    public static void showDashboard() throws IOException {
        showMain("dashboard.fxml", "ISKOllect - Dashboard");
    }

    public static void showRewards() throws IOException {
        showMain("RewardsCatalog.fxml", "ISKOllect - Rewards Catalog");
    }

    public static void showHistory() throws IOException {
        showMain("transactionhistory.fxml", "ISKOllect - Transaction History");
    }

    public static Stage showModal(String file, String title) throws IOException {
        FXMLLoader loader = loader(file);
        Parent root = loader.load();
        Stage dialog = new Stage(StageStyle.DECORATED);
        dialog.initOwner(AppContext.stage());
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle(title);
        dialog.setResizable(false);
        dialog.setScene(new Scene(root));
        dialog.showAndWait();
        return dialog;
    }

    private static void showMain(String file, String title) throws IOException {
        Parent root = loader(file).load();
        Stage stage = AppContext.stage();
        stage.setTitle(title);
        stage.setResizable(false);
        stage.setScene(new Scene(root));
        stage.show();
        stage.centerOnScreen();
    }

    private static FXMLLoader loader(String file) {
        return new FXMLLoader(AppNavigator.class.getResource(FXML + file));
    }
}
