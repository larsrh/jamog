
package core.exception;

/**
 *
 * @author lars
 */
public class ParameterException extends Exception {

    /**
     * Creates a new instance of <code>ParameterException</code> without detail message.
     */
    public ParameterException() {
    }


    /**
     * Constructs an instance of <code>ParameterException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public ParameterException(String msg) {
        super(msg);
    }

	public ParameterException(Throwable cause) {
		super(cause);
	}

	public ParameterException(String message, Throwable cause) {
		super(message, cause);
	}

	
}
