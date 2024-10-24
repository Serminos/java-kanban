package exception;

public class TaskValidationException extends IllegalArgumentException {
    public TaskValidationException(String message) {
        super(message);
    }
}
