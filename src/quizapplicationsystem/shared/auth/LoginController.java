package quizapplicationsystem.shared.auth;

import java.util.Optional;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.shape.SVGPath;
import javafx.util.Duration;

/**
 * Controller for LoginPage.fxml. The authentication service owns role
 * resolution, so the login screen never asks users to choose their access.
 */
public class LoginController {

    private static final String MESSAGE_ERROR = "message-error";
    private static final String MESSAGE_SUCCESS = "message-success";
    private static final Interpolator ICON_INTERPOLATOR = Interpolator.EASE_BOTH;

    private final AuthService authService = new AuthService();
    private final GaussianBlur passwordShowIconBlur = new GaussianBlur(0.0);
    private final GaussianBlur passwordHideIconBlur = new GaussianBlur(4.0);

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private TextField visiblePasswordField;
    @FXML private Button passwordVisibilityButton;
    @FXML private SVGPath passwordShowIcon;
    @FXML private SVGPath passwordHideIcon;
    @FXML private Label messageLabel;
    @FXML private Button loginButton;

    private Timeline passwordIconTransition;

    /** Called automatically by the FXMLLoader after the fields are injected. */
    @FXML
    private void initialize() {
        visiblePasswordField.textProperty().bindBidirectional(passwordField.textProperty());
        passwordShowIcon.setEffect(passwordShowIconBlur);
        passwordHideIcon.setEffect(passwordHideIconBlur);

        usernameField.textProperty().addListener((observable, oldValue, newValue) -> clearMessage());
        passwordField.textProperty().addListener((observable, oldValue, newValue) -> clearMessage());
        clearMessage();
    }

    @FXML
    private void togglePasswordVisibility() {
        TextField previousField = activePasswordField();
        int caretPosition = previousField.getCaretPosition();
        boolean showPassword = !visiblePasswordField.isVisible();

        passwordField.setVisible(!showPassword);
        visiblePasswordField.setVisible(showPassword);
        passwordVisibilityButton.setAccessibleText(
                showPassword ? "Hide password" : "Show password");

        TextField activeField = activePasswordField();
        activeField.requestFocus();
        activeField.positionCaret(Math.min(caretPosition, activeField.getLength()));

        animatePasswordIcon(showPassword);
    }

    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (username.isEmpty()) {
            showError("Please enter your username.");
            usernameField.requestFocus();
            return;
        }
        if (password.isEmpty()) {
            showError("Please enter your password.");
            activePasswordField().requestFocus();
            return;
        }

        Optional<UserSession> authenticatedSession = authService.authenticate(username, password);
        if (authenticatedSession.isEmpty()) {
            passwordField.clear();
            showError("Incorrect username or password. Please try again.");
            activePasswordField().requestFocus();
            return;
        }

        routeToRole(authenticatedSession.get());
    }

    private TextField activePasswordField() {
        return visiblePasswordField.isVisible() ? visiblePasswordField : passwordField;
    }

    private void animatePasswordIcon(boolean passwordIsVisible) {
        if (passwordIconTransition != null) {
            passwordIconTransition.stop();
        }

        SVGPath incomingIcon = passwordIsVisible ? passwordHideIcon : passwordShowIcon;
        SVGPath outgoingIcon = passwordIsVisible ? passwordShowIcon : passwordHideIcon;
        GaussianBlur incomingBlur = passwordIsVisible
                ? passwordHideIconBlur : passwordShowIconBlur;
        GaussianBlur outgoingBlur = passwordIsVisible
                ? passwordShowIconBlur : passwordHideIconBlur;

        passwordIconTransition = new Timeline(new KeyFrame(
                Duration.millis(300),
                new KeyValue(incomingIcon.opacityProperty(), 1.0, ICON_INTERPOLATOR),
                new KeyValue(incomingIcon.scaleXProperty(), 1.0, ICON_INTERPOLATOR),
                new KeyValue(incomingIcon.scaleYProperty(), 1.0, ICON_INTERPOLATOR),
                new KeyValue(incomingBlur.radiusProperty(), 0.0, ICON_INTERPOLATOR),
                new KeyValue(outgoingIcon.opacityProperty(), 0.0, ICON_INTERPOLATOR),
                new KeyValue(outgoingIcon.scaleXProperty(), 0.25, ICON_INTERPOLATOR),
                new KeyValue(outgoingIcon.scaleYProperty(), 0.25, ICON_INTERPOLATOR),
                new KeyValue(outgoingBlur.radiusProperty(), 4.0, ICON_INTERPOLATOR)
        ));
        passwordIconTransition.play();
    }

    /**
     * RBAC entry point: send the user to the correct area for their role.
     * Wire each branch to load the matching shell FXML (admin.shell,
     * teacher.shell, student.shell) once those views exist.
     */
    private void routeToRole(UserSession authenticatedSession) {
        String username = authenticatedSession.username();

        switch (authenticatedSession.role()) {
            case ADMIN:
                showSuccess("Welcome, " + username + ". The admin workspace is coming soon.");
                break;
            case TEACHER:
                showSuccess("Welcome, " + username + ". The teacher workspace is coming soon.");
                break;
            case STUDENT:
                showSuccess("Welcome, " + username + ". The student workspace is coming soon.");
                break;
        }
    }

    private void showError(String text) {
        showMessage(text, MESSAGE_ERROR);
    }

    private void showSuccess(String text) {
        showMessage(text, MESSAGE_SUCCESS);
    }

    private void showMessage(String text, String stateClass) {
        messageLabel.getStyleClass().removeAll(MESSAGE_ERROR, MESSAGE_SUCCESS);
        messageLabel.getStyleClass().add(stateClass);
        messageLabel.setText(text);
        messageLabel.setManaged(true);
        messageLabel.setVisible(true);
    }

    private void clearMessage() {
        messageLabel.setText("");
        messageLabel.getStyleClass().removeAll(MESSAGE_ERROR, MESSAGE_SUCCESS);
        messageLabel.setManaged(false);
        messageLabel.setVisible(false);
    }
}
