package com.iskollect;

import com.iskollect.dao.IskollectRepository;
import com.iskollect.model.Student;
import com.iskollect.service.IskollectService;
import com.iskollect.util.DBConnection;
import javafx.stage.Stage;

public final class AppContext {
    private static Stage primaryStage;
    private static IskollectService service;
    private static Student selectedStudent;

    private AppContext() {
    }

    public static void initialize(Stage stage) {
        primaryStage = stage;
        service = new IskollectService(
            new IskollectRepository(DBConnection::getConnection)
        );
    }

    public static Stage stage() {
        return primaryStage;
    }

    public static IskollectService service() {
        return service;
    }

    public static Student selectedStudent() {
        return selectedStudent;
    }

    public static void selectStudent(Student student) {
        selectedStudent = student;
    }
}
