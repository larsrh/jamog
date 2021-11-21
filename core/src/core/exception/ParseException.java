package core.exception;

/**
 *
 * @author lars
 */
public class ParseException extends Exception {

	public ParseException(Throwable cause, String code) {
		super(code,cause);
	}

	public ParseException(String message, Throwable cause, String code) {
		super(message+"\n"+code, cause);
	}

	public ParseException(String message, String code) {
		super(message+"\n"+code);
	}

    public ParseException() {
    }

    public ParseException(String msg) {
        super(msg);
    }

	public ParseException(Throwable cause) {
		super(cause);
	}

	public ParseException(String message, Throwable cause) {
		super(message, cause);
	}
}
