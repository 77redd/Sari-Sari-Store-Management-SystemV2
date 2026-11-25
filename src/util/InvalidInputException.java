package util;

/** Thrown when user-supplied data is missing, malformed, or out of range. */
public class InvalidInputException extends Exception {
    public InvalidInputException(String message) {
        super(message);
    }
}
