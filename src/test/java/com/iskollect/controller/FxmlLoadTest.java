package com.iskollect.controller;

import com.iskollect.AppContext;
import com.iskollect.model.Reward;
import com.iskollect.model.Student;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FxmlLoadTest {
    private static final List<String> FILES = List.of(
        "login.fxml",
        "dashboard.fxml",
        "RewardsCatalog.fxml",
        "transactionhistory.fxml",
        "AddstudentPOPUP.fxml",
        "clickstudentPOPUP.fxml",
        "submitbottlepopup.fxml",
        "rewardsPOPUP.fxml",
        "AddcouponPOPUP.fxml",
        "editcouponPOPUP.fxml",
        "editStudent.fxml",
        "feedbackCongratsPOPUP.fxml",
        "feedbackRedeemPOPUP.fxml",
        "feedbackRegisterSuccessPOPUP.fxml",
        "feedbackTYPOPUP.fxml",
        "removeRewardPOPUP.fxml",
        "removeStudentPOPUP.fxml"
    );

    @Test
    void everyFxmlResourceLoadsWithItsControllerAndAssets() throws Exception {
        CountDownLatch finished = new CountDownLatch(1);
        AtomicReference<Throwable> failure = new AtomicReference<>();

        Platform.startup(() -> {
            try {
                AppContext.initialize(new Stage());
                AppContext.selectStudent(new Student(
                    1,
                    "FXML Test Student",
                    10,
                    new BigDecimal("5.00"),
                    LocalDateTime.now()
                ));
                RewardEditorContext.select(
                    new Reward(
                        1,
                        "FXML Test Reward",
                        "Test description",
                        new BigDecimal("5.00"),
                        true
                    )
                );

                for (String file : FILES) {
                    FXMLLoader loader = new FXMLLoader(
                        getClass().getResource("/com/iskollect/fxml/" + file)
                    );
                    loader.load();
                }
            } catch (Throwable throwable) {
                failure.set(throwable);
            } finally {
                finished.countDown();
                Platform.runLater(Platform::exit);
            }
        });

        assertTrue(finished.await(30, TimeUnit.SECONDS), "FXML loading timed out");
        assertNull(failure.get(), () -> "FXML loading failed: " + failure.get());
    }
}
