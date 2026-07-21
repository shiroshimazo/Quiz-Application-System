package quizapplicationsystem.admin.dashboard;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import quizapplicationsystem.shared.auth.UserSession;
import quizapplicationsystem.shared.data.database.databaseConnection;

/** Loads and presents the account overview shown on the admin dashboard. */
public final class DashboardController {

    private static final Logger LOGGER = Logger.getLogger(DashboardController.class.getName());
    private static final NumberFormat COUNT_FORMAT = NumberFormat.getIntegerInstance(Locale.US);
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern(
            "EEEE, MMMM d, uuuu", Locale.ENGLISH);

    private static final String ACCOUNT_COUNTS_SQL = """
            SELECT
                COUNT(*) AS total_accounts,
                SUM(CASE WHEN role = 'STUDENT' THEN 1 ELSE 0 END) AS students,
                SUM(CASE WHEN role = 'TEACHER' THEN 1 ELSE 0 END) AS teachers,
                SUM(CASE WHEN role = 'ADMIN' THEN 1 ELSE 0 END) AS administrators
            FROM users
            """;

    private static final String RECENT_ACCOUNTS_SQL = """
            SELECT display_name, username, role
            FROM users
            ORDER BY user_id DESC
            LIMIT 8
            """;

    @FXML private VBox dashboardContent;
    @FXML private Label welcomeLabel;
    @FXML private Label dateLabel;
    @FXML private Label dataStatusLabel;
    @FXML private Label totalAccountsLabel;
    @FXML private Label studentsLabel;
    @FXML private Label teachersLabel;
    @FXML private Label administratorsLabel;
    @FXML private Label studentBreakdownLabel;
    @FXML private Label teacherBreakdownLabel;
    @FXML private Label administratorBreakdownLabel;
    @FXML private ProgressBar studentProgressBar;
    @FXML private ProgressBar teacherProgressBar;
    @FXML private ProgressBar administratorProgressBar;
    @FXML private TableView<AccountRow> recentAccountsTable;
    @FXML private TableColumn<AccountRow, String> nameColumn;
    @FXML private TableColumn<AccountRow, String> usernameColumn;
    @FXML private TableColumn<AccountRow, String> roleColumn;

    private final ObservableList<AccountRow> recentAccounts = FXCollections.observableArrayList();
    private final FilteredList<AccountRow> filteredAccounts = new FilteredList<>(
            recentAccounts, account -> true);
    private Consumer<String> navigationHandler = destination -> { };
    private UserSession session;

    @FXML
    private void initialize() {
        configureTable();
        dateLabel.setText(LocalDate.now().format(DATE_FORMAT));
        playEntranceAnimation();
    }

    public void setSession(UserSession session) {
        this.session = Objects.requireNonNull(session, "session");
        welcomeLabel.setText(greetingForNow() + ", " + firstNameOf(session.displayName()));
        refreshData();
    }

    public void setNavigationHandler(Consumer<String> navigationHandler) {
        this.navigationHandler = Objects.requireNonNull(navigationHandler, "navigationHandler");
    }

    public void setSearchQuery(String searchQuery) {
        String query = searchQuery == null ? "" : searchQuery.strip().toLowerCase(Locale.ROOT);
        filteredAccounts.setPredicate(account -> query.isEmpty()
                || account.displayName().toLowerCase(Locale.ROOT).contains(query)
                || account.username().toLowerCase(Locale.ROOT).contains(query)
                || account.role().toLowerCase(Locale.ROOT).contains(query));
    }

    @FXML
    public void refreshData() {
        if (session == null) {
            return;
        }

        setDataStatus("Refreshing", false);
        try (Connection connection = databaseConnection.getConnection()) {
            AccountCounts counts = readAccountCounts(connection);
            List<AccountRow> accounts = readRecentAccounts(connection);
            showAccountCounts(counts);
            recentAccounts.setAll(accounts);
            setDataStatus("Live data", false);
        } catch (SQLException exception) {
            LOGGER.log(Level.SEVERE, "Admin dashboard data could not be loaded.", exception);
            clearAccountCounts();
            recentAccounts.clear();
            setDataStatus("Database unavailable", true);
        }
    }

    @FXML
    private void handleAddAccount() {
        navigationHandler.accept("STUDENTS");
    }

    @FXML
    private void handleManageStudents() {
        navigationHandler.accept("STUDENTS");
    }

    @FXML
    private void handleManageTeachers() {
        navigationHandler.accept("TEACHERS");
    }

    @FXML
    private void handleOpenQuizLibrary() {
        navigationHandler.accept("QUIZZES");
    }

    @FXML
    private void handleViewResults() {
        navigationHandler.accept("RESULTS");
    }

    private void configureTable() {
        nameColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(
                cell.getValue().displayName()));
        usernameColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(
                "@" + cell.getValue().username()));
        roleColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(
                cell.getValue().role()));
        roleColumn.setCellFactory(column -> new RoleBadgeCell());
        recentAccountsTable.setItems(filteredAccounts);
        recentAccountsTable.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
    }

    private AccountCounts readAccountCounts(Connection connection) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(ACCOUNT_COUNTS_SQL);
                ResultSet result = statement.executeQuery()) {
            if (!result.next()) {
                return new AccountCounts(0, 0, 0, 0);
            }
            return new AccountCounts(
                    result.getLong("total_accounts"),
                    result.getLong("students"),
                    result.getLong("teachers"),
                    result.getLong("administrators"));
        }
    }

    private List<AccountRow> readRecentAccounts(Connection connection) throws SQLException {
        List<AccountRow> accounts = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(RECENT_ACCOUNTS_SQL);
                ResultSet result = statement.executeQuery()) {
            while (result.next()) {
                accounts.add(new AccountRow(
                        result.getString("display_name"),
                        result.getString("username"),
                        titleCase(result.getString("role"))));
            }
        }
        return accounts;
    }

    private void showAccountCounts(AccountCounts counts) {
        totalAccountsLabel.setText(COUNT_FORMAT.format(counts.total()));
        studentsLabel.setText(COUNT_FORMAT.format(counts.students()));
        teachersLabel.setText(COUNT_FORMAT.format(counts.teachers()));
        administratorsLabel.setText(COUNT_FORMAT.format(counts.administrators()));

        studentBreakdownLabel.setText(COUNT_FORMAT.format(counts.students()) + " accounts");
        teacherBreakdownLabel.setText(COUNT_FORMAT.format(counts.teachers()) + " accounts");
        administratorBreakdownLabel.setText(
                COUNT_FORMAT.format(counts.administrators()) + " accounts");

        studentProgressBar.setProgress(ratio(counts.students(), counts.total()));
        teacherProgressBar.setProgress(ratio(counts.teachers(), counts.total()));
        administratorProgressBar.setProgress(ratio(counts.administrators(), counts.total()));
    }

    private void clearAccountCounts() {
        totalAccountsLabel.setText("—");
        studentsLabel.setText("—");
        teachersLabel.setText("—");
        administratorsLabel.setText("—");
        studentBreakdownLabel.setText("Unavailable");
        teacherBreakdownLabel.setText("Unavailable");
        administratorBreakdownLabel.setText("Unavailable");
        studentProgressBar.setProgress(0.0);
        teacherProgressBar.setProgress(0.0);
        administratorProgressBar.setProgress(0.0);
    }

    private void setDataStatus(String text, boolean error) {
        dataStatusLabel.setText(text);
        dataStatusLabel.getStyleClass().removeAll("data-status-live", "data-status-error");
        dataStatusLabel.getStyleClass().add(error ? "data-status-error" : "data-status-live");
    }

    private void playEntranceAnimation() {
        dashboardContent.setOpacity(0.0);
        dashboardContent.setTranslateY(12.0);

        FadeTransition fade = new FadeTransition(Duration.millis(360), dashboardContent);
        fade.setFromValue(0.0);
        fade.setToValue(1.0);

        TranslateTransition translate = new TranslateTransition(
                Duration.millis(420), dashboardContent);
        translate.setFromY(12.0);
        translate.setToY(0.0);
        fade.play();
        translate.play();
    }

    private static double ratio(long value, long total) {
        return total == 0 ? 0.0 : (double) value / total;
    }

    private static String greetingForNow() {
        int hour = LocalTime.now().getHour();
        if (hour < 12) {
            return "Good morning";
        }
        if (hour < 18) {
            return "Good afternoon";
        }
        return "Good evening";
    }

    private static String firstNameOf(String displayName) {
        return displayName.strip().split("\\s+")[0];
    }

    private static String titleCase(String value) {
        if (value == null || value.isBlank()) {
            return "Unknown";
        }
        String normalized = value.toLowerCase(Locale.ROOT);
        return Character.toUpperCase(normalized.charAt(0)) + normalized.substring(1);
    }

    private record AccountCounts(long total, long students, long teachers, long administrators) {
    }

    public record AccountRow(String displayName, String username, String role) {
    }

    private static final class RoleBadgeCell extends TableCell<AccountRow, String> {

        private final Label badge = new Label();

        @Override
        protected void updateItem(String role, boolean empty) {
            super.updateItem(role, empty);
            if (empty || role == null) {
                setGraphic(null);
                return;
            }

            badge.setText(role);
            badge.getStyleClass().setAll(
                    "role-badge", "role-" + role.toLowerCase(Locale.ROOT));
            setGraphic(badge);
            setText(null);
        }
    }
}
