package quizapplicationsystem.shared.auth;

/**
 * The three access roles in the system. Used for role-based access control
 * (RBAC): after login, the user's role decides which area (admin, teacher, or
 * student) they are routed to and what they are allowed to do.
 */
public enum Role {
    ADMIN("Admin"),
    TEACHER("Teacher"),
    STUDENT("Student");

    private final String displayName;

    Role(String displayName) {
        this.displayName = displayName;
    }

    /** Human-friendly label shown in the UI (e.g. the login role selector). */
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
