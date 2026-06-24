package com.iskollect;

import javafx.application.Application;
import javafx.stage.Stage;

public final class IskollectApplication extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        AppContext.initialize(stage);
        AppNavigator.showLogin();
    }
}
