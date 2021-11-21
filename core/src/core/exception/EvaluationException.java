
package core.exception;

/**
 *
 * @author lars
 */
public class EvaluationException extends Exception {

	public static enum Type {
		IDENTIFIER_NOT_FOUND,
		DIVISION_BY_ZERO,
		NEGATIVE_LOGARITHM,
		UNKNOWN
	}

	private final Type type;

	public EvaluationException(String message, Type type) {
		super(message);
		this.type = type;
	}

	public EvaluationException(String message) {
		this(message,Type.UNKNOWN);
	}

	public EvaluationException(Type type) {
		this.type = type;
	}

	public EvaluationException() {
		this(Type.UNKNOWN);
	}

	public EvaluationException(Throwable cause) {
		super(cause);
		this.type = Type.UNKNOWN;
	}

	public EvaluationException(String message, Throwable cause) {
		super(message, cause);
		this.type = Type.UNKNOWN;
	}

	@Override public String getMessage() {
		String superMsg = super.getMessage();
		if (type == Type.UNKNOWN)
			return superMsg;
		else if ("".equals(superMsg))
			return type.toString();
		else
			return type+": "+superMsg;
	}

}
