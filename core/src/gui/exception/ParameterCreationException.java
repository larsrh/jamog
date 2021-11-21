/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gui.exception;

/**
 *
 * @author sylvester
 */
public class ParameterCreationException extends GUIException {

	public ParameterCreationException() {
	}

	public ParameterCreationException(String message) {
		super(message);
	}

	public ParameterCreationException(String message, Throwable cause) {
		super(message, cause);
	}

	public ParameterCreationException(Throwable cause) {
		super(cause);
	}

}
