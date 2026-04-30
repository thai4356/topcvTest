package bbb.bbb.exception;

public class FieldValidationException extends RuntimeException {

    private final String field;
    private final String error;

    public FieldValidationException(String field, String error) {
        super(error);
        this.field = field;
        this.error = error;
    }

    public String getField() {
        return field;
    }

    public String getError() {
        return error;
    }
}
