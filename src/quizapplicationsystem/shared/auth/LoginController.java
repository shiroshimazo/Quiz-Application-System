package quizapplicationsystem.shared.auth;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

/**
 * Controller for LoginPage.fxml. Validates the entered credentials and, once
 * RBAC is wired up, routes the user to the matching area (admin, teacher, or
 * student) based on their {@link Role}.
 */
public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private ComboBox<Role> roleComboBox;
    @FXML private Label messageLabel;
    @FXML private Button loginButton;

    /** Called automatically by the FXMLLoader after the fields are injected. */
    @FXML
    private void initialize() {
        roleComboBox.setItems(FXCollections.observableArrayList(Role.values()));
        roleComboBox.getSelectionModel().selectFirst();
    }

    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        Role role = roleComboBox.getValue();

        if (username.isEmpty() || password.isEmpty()) {
            showMessage("Please enter both username and password.");
            return;
        }
        if (role == null) {
            showMessage("Please select a role.");
            return;
        }

        // TODO: replace with a real AuthService lookup against shared.data.
        // For now, accept any non-empty credentials so the page is runnable.
        routeToRole(role, username);
    }

    /**
     * RBAC entry point: send the user to the correct area for their role.
     * Wire each branch to load the matching shell FXML (admin.shell,
     * teacher.shell, student.shell) once those views exist.
     */
    private void routeToRole(Role role, String username) {
        switch (role) {
            case ADMIN:
                showMessage("Welcome, " + username + " (Admin). Admin area coming soon.");
                break;
            case TEACHER:
                showMessage("Welcome, " + username + " (Teacher). Teacher area coming soon.");
                break;
            case STUDENT:
                showMessage("Welcome, " + username + " (Student). Student area coming soon.");
                break;
        }
    }

    private void showMessage(String text) {
        messageLabel.setText(text);
    }
}
