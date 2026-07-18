/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package quizapplicationsystem;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Application entry point. Launches JavaFX and shows the login page first.
 *
 * @author shiro
 */
public class QuizApplicationSystem extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        // The login page lives in shared.auth — it is the shared entry point
        // for all three roles (admin, teacher, student).
        Parent root = FXMLLoader.load(
                getClass().getResource("/quizapplicationsystem/shared/auth/LoginPage.fxml"));

        stage.setScene(new Scene(root));
        stage.setTitle("Quiz Application System - Login");
        stage.show();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

}
