package quizapplicationsystem.shared.auth;

import java.util.Objects;

/** The authenticated identity and role used for RBAC routing. */
public record UserSession(long userId, String username, String displayName, Role role) {

    public UserSession {
        if (userId <= 0) {
            throw new IllegalArgumentException("userId must be positive");
        }
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("username must not be blank");
        }
        if (displayName == null || displayName.isBlank()) {
            throw new IllegalArgumentException("displayName must not be blank");
        }
        Objects.requireNonNull(role, "role");
    }
}
