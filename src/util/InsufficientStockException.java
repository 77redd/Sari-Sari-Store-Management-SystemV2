package util;

/** Thrown when a sale or stock deduction asks for more units than are on hand. */
public class InsufficientStockException extends Exception {
    public InsufficientStockException(String message) {
        super(message);
    }
}
