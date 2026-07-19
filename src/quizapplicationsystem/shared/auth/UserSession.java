package quizapplicationsystem.shared.auth;

import java.util.Objects;

/** The authenticated identity and role used for RBAC routing. */
public record UserSession(String username, Role role) {

    public UserSession {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("username must not be blank");
        }
        Objects.requireNonNull(role, "role");
    }
}
