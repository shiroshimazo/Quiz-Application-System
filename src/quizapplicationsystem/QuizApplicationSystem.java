/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package quizapplicationsystem;

import java.net.URL;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.text.Font;
import javafx.stage.Stage;

/**
 * Application entry point. Launches JavaFX and shows the login page first.
 *
 * @author shiro
 */
public class QuizApplicationSystem extends Application {

    private static final Logger LOGGER = Logger.getLogger(QuizApplicationSystem.class.getName());
    private static final String FONT_DIRECTORY =
            "/quizapplicationsystem/shared/assets/fonts/";
    private static final String[] MANROPE_FONTS = {
        "Manrope-Regular.ttf",
        "Manrope-SemiBold.ttf",
        "Manrope-Bold.ttf",
        "Manrope-ExtraBold.ttf"
    };

    @Override
    public void start(Stage stage) throws Exception {
        loadApplicationFont();

        // The login page lives in shared.auth — it is the shared entry point
        // for all three roles (admin, teacher, student).
        Parent root = FXMLLoader.load(
                getClass().getResource("/quizapplicationsystem/shared/auth/LoginPage.fxml"));

        stage.setScene(new Scene(root, 1140.0, 720.0));
        stage.setTitle("Quiz Application System - Login");
        stage.setMinWidth(920.0);
        stage.setMinHeight(690.0);
        stage.show();
    }

    private void loadApplicationFont() {
        for (String fontFile : MANROPE_FONTS) {
            URL fontResource = getClass().getResource(FONT_DIRECTORY + fontFile);
            if (fontResource == null || Font.loadFont(fontResource.toExternalForm(), 12.0) == null) {
                LOGGER.warning("The bundled font could not be loaded: " + fontFile);
            }
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

}
