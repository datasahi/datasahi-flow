package datasahi.flow.commons.error;

public class GenericException extends RuntimeException {

    private static final long serialVersionUID = 1L;
    private Integer errorCode;

    public GenericException(final String message, final Integer code) {
        super(message);
        this.errorCode = code;
    }

    public GenericException(final  String message, final  Throwable cause,
                            final  Integer code) {
        super(message, cause);
        this.errorCode = code;
    }

    public GenericException(final String message) {
        super(message);
    }

    public GenericException(final Throwable cause) {
        super(cause.getMessage(), cause);
    }
}
