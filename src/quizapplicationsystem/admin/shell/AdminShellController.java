package quizapplicationsystem.admin.shell;

import java.io.IOException;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import quizapplicationsystem.admin.dashboard.DashboardController;
import quizapplicationsystem.shared.auth.Role;
import quizapplicationsystem.shared.auth.UserSession;

/** Owns the persistent admin sidebar, top bar, and feature content host. */
public final class AdminShellController {

    private static final Logger LOGGER = Logger.getLogger(AdminShellController.class.getName());

    private static final Map<String, PageCopy> PAGE_COPY = Map.of(
            "STUDENTS", new PageCopy(
                    "Students",
                    "Student accounts",
                    "Manage student access",
                    "Create, search, and maintain student accounts from one focused workspace."),
            "TEACHERS", new PageCopy(
                    "Teachers",
                    "Teacher accounts",
                    "Manage teacher access",
                    "Create teacher accounts and review who can author quizzes."),
            "QUIZZES", new PageCopy(
                    "Quiz library",
                    "Quiz library",
                    "Organize every quiz",
                    "A shared library for reviewing, publishing, and archiving quizzes is next."),
            "RESULTS", new PageCopy(
                    "Results",
                    "Results and reports",
                    "Understand performance",
                    "Review attempts, scores, and participation across the platform."),
            "SETTINGS", new PageCopy(
                    "Settings",
                    "System settings",
                    "Configure the workspace",
                    "Control application preferences and account policies from this module."));

    @FXML private ToggleButton dashboardNavButton;
    @FXML private ToggleButton studentsNavButton;
    @FXML private ToggleButton teachersNavButton;
    @FXML private ToggleButton quizzesNavButton;
    @FXML private ToggleButton resultsNavButton;
    @FXML private ToggleButton settingsNavButton;
    @FXML private Label breadcrumbLabel;
    @FXML private Label accountNameLabel;
    @FXML private Label accountInitialsLabel;
    @FXML private TextField globalSearchField;
    @FXML private Node dashboardView;
    @FXML private DashboardController dashboardViewController;
    @FXML private VBox modulePlaceholder;
    @FXML private Label placeholderEyebrowLabel;
    @FXML private Label placeholderTitleLabel;
    @FXML private Label placeholderDescriptionLabel;
    @FXML private Button logoutButton;

    private UserSession session;

    @FXML
    private void initialize() {
        dashboardViewController.setNavigationHandler(this::navigateTo);
        globalSearchField.textProperty().addListener(
                (observable, oldValue, newValue) -> dashboardViewController.setSearchQuery(newValue));
        navigateTo("DASHBOARD");
    }

    /** Supplies the authenticated identity after this shell is loaded. */
    public void setSession(UserSession session) {
        this.session = Objects.requireNonNull(session, "session");
        if (session.role() != Role.ADMIN) {
            throw new IllegalArgumentException("The admin shell requires an ADMIN session.");
        }

        accountNameLabel.setText(session.displayName());
        accountInitialsLabel.setText(initialsFor(session.displayName()));
        dashboardViewController.setSession(session);
    }

    @FXML
    private void handleNavigation(ActionEvent event) {
        if (event.getSource() instanceof ToggleButton button) {
            button.setSelected(true);
            navigateTo(String.valueOf(button.getUserData()));
        }
    }

    @FXML
    private void handleBackToDashboard() {
        navigateTo("DASHBOARD");
    }

    @FXML
    private void handleLogout() {
        try {
            Parent loginRoot = FXMLLoader.load(getClass().getResource(
                    "/quizapplicationsystem/shared/auth/LoginPage.fxml"));
            Stage stage = (Stage) logoutButton.getScene().getWindow();
            stage.setMaximized(false);
            stage.setScene(new Scene(loginRoot, 1140.0, 720.0));
            stage.setTitle("Quiz Application System - Login");
            stage.setMinWidth(920.0);
            stage.setMinHeight(690.0);
            stage.centerOnScreen();
        } catch (IOException exception) {
            LOGGER.log(Level.SEVERE, "The login page could not be opened.", exception);
        }
    }

    private void navigateTo(String destination) {
        String page = destination == null
                ? "DASHBOARD" : destination.toUpperCase(Locale.ROOT);
        ToggleButton navigationButton = buttonFor(page);
        if (navigationButton != null) {
            navigationButton.setSelected(true);
        }

        boolean isDashboard = "DASHBOARD".equals(page);
        dashboardView.setManaged(isDashboard);
        dashboardView.setVisible(isDashboard);
        modulePlaceholder.setManaged(!isDashboard);
        modulePlaceholder.setVisible(!isDashboard);
        globalSearchField.setDisable(!isDashboard);

        if (isDashboard) {
            breadcrumbLabel.setText("Dashboard");
            if (session != null) {
                dashboardViewController.refreshData();
            }
        } else {
            PageCopy copy = PAGE_COPY.getOrDefault(page, PAGE_COPY.get("SETTINGS"));
            breadcrumbLabel.setText(copy.breadcrumb());
            placeholderEyebrowLabel.setText(copy.eyebrow().toUpperCase(Locale.ROOT));
            placeholderTitleLabel.setText(copy.title());
            placeholderDescriptionLabel.setText(copy.description());
        }

        Platform.runLater(() -> updateWindowTitle(isDashboard
                ? "Dashboard" : PAGE_COPY.getOrDefault(page, PAGE_COPY.get("SETTINGS")).breadcrumb()));
    }

    private ToggleButton buttonFor(String page) {
        return switch (page) {
            case "DASHBOARD" -> dashboardNavButton;
            case "STUDENTS" -> studentsNavButton;
            case "TEACHERS" -> teachersNavButton;
            case "QUIZZES" -> quizzesNavButton;
            case "RESULTS" -> resultsNavButton;
            case "SETTINGS" -> settingsNavButton;
            default -> null;
        };
    }

    private void updateWindowTitle(String pageName) {
        if (dashboardView.getScene() != null
                && dashboardView.getScene().getWindow() instanceof Stage stage) {
            stage.setTitle(pageName + " - Quiz Application System Admin");
        }
    }

    private static String initialsFor(String displayName) {
        String[] nameParts = displayName.trim().split("\\s+");
        if (nameParts.length == 1) {
            return nameParts[0].substring(0, Math.min(2, nameParts[0].length()))
                    .toUpperCase(Locale.ROOT);
        }
        return (nameParts[0].substring(0, 1)
                + nameParts[nameParts.length - 1].substring(0, 1)).toUpperCase(Locale.ROOT);
    }

    private record PageCopy(
            String breadcrumb, String eyebrow, String title, String description) {
    }
}
