package quizapplicationsystem.shared.auth;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Locale;
import java.util.Optional;
import quizapplicationsystem.shared.data.database.databaseConnection;

/**
 * Authenticates an account and returns the role assigned to that account.
 */
public final class AuthService {

    private static final String FIND_ACCOUNT_SQL = """
            SELECT user_id, display_name, username, password_hash, role
            FROM users
            WHERE username = ?
            LIMIT 1
            """;

    /**
     * Validates credentials and resolves the account's role. The role is
     * deliberately never accepted as input from the login screen.
     *
     * @param username account username
     * @param password account password
     * @return an authenticated session, or empty when credentials are invalid
     * @throws AuthenticationException when the database cannot be queried
     */
    public Optional<UserSession> authenticate(String username, String password)
            throws AuthenticationException {
        if (username == null || password == null) {
            return Optional.empty();
        }

        String normalizedUsername = username.trim();
        if (normalizedUsername.isEmpty() || password.isEmpty()) {
            return Optional.empty();
        }

        try (Connection connection = databaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(FIND_ACCOUNT_SQL)) {
            statement.setString(1, normalizedUsername);

            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()
                        || !PasswordHasher.matches(password, result.getString("password_hash"))) {
                    return Optional.empty();
                }

                Role role = parseRole(result.getString("role"));
                return Optional.of(new UserSession(
                        result.getLong("user_id"),
                        result.getString("username"),
                        result.getString("display_name"),
                        role));
            }
        } catch (SQLException | IllegalStateException exception) {
            throw new AuthenticationException(
                    "The account database could not be queried.", exception);
        }
    }

    private static Role parseRole(String databaseRole) throws AuthenticationException {
        try {
            return Role.valueOf(databaseRole.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException | NullPointerException exception) {
            throw new AuthenticationException(
                    "The account has an unsupported role.", exception);
        }
    }
}
