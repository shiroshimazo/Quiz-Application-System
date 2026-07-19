package quizapplicationsystem.shared.auth;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * Authenticates an account and returns the role assigned to that account.
 *
 * <p>The local directory keeps the application runnable while the database
 * schema is being built. Replace the lookup in {@link #authenticate} with a
 * repository query once the users table exists; the controller will not need
 * to change because it already receives a server-owned {@link UserSession}.</p>
 */
public final class AuthService {

    private static final Map<String, DemoAccount> DEMO_ACCOUNTS = Map.of(
            "admin", new DemoAccount("admin123", Role.ADMIN),
            "teacher", new DemoAccount("teacher123", Role.TEACHER),
            "student", new DemoAccount("student123", Role.STUDENT)
    );

    /**
     * Validates credentials and resolves the account's role. The role is
     * deliberately never accepted as input from the login screen.
     *
     * @param username account username
     * @param password account password
     * @return an authenticated session, or empty when credentials are invalid
     */
    public Optional<UserSession> authenticate(String username, String password) {
        if (username == null || password == null) {
            return Optional.empty();
        }

        String normalizedUsername = username.trim().toLowerCase(Locale.ROOT);
        DemoAccount account = DEMO_ACCOUNTS.get(normalizedUsername);

        if (account == null || !passwordMatches(password, account.password())) {
            return Optional.empty();
        }

        return Optional.of(new UserSession(normalizedUsername, account.role()));
    }

    private static boolean passwordMatches(String supplied, String expected) {
        return MessageDigest.isEqual(
                supplied.getBytes(StandardCharsets.UTF_8),
                expected.getBytes(StandardCharsets.UTF_8));
    }

    private record DemoAccount(String password, Role role) {
    }
}
