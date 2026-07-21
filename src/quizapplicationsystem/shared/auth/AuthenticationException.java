package quizapplicationsystem.shared.auth;

/** Indicates that authentication could not complete because of a system error. */
public final class AuthenticationException extends Exception {

    public AuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}
