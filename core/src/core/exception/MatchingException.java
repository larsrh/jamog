package core.exception;

/**
 *
 * @author lars
 */
public class MatchingException extends Exception {

    /**
     * Creates a new instance of <code>MatchingException</code> without detail message.
     */
    public MatchingException() {
    }


    /**
     * Constructs an instance of <code>MatchingException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public MatchingException(String msg) {
        super(msg);
    }
}
