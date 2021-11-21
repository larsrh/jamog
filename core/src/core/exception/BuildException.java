package core.exception;

/**
 *
 * @author lars
 */
public class BuildException extends Exception {

    /**
     * Creates a new instance of <code>BuildException</code> without detail message.
     */
    public BuildException() {
    }

    /**
     * Constructs an instance of <code>BuildException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public BuildException(String msg) {
        super(msg);
    }

	public BuildException(Throwable cause) {
		super(cause);
	}

	public BuildException(String message, Throwable cause) {
		super(message, cause);
	}

}
