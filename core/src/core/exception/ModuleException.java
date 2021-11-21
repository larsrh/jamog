/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package core.exception;

/**
 *
 * @author lars
 */
public class ModuleException extends Exception {

    /**
     * Creates a new instance of <code>ModuleException</code> without detail message.
     */
    public ModuleException() {
    }


    /**
     * Constructs an instance of <code>ModuleException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public ModuleException(String msg) {
        super(msg);
    }

	public ModuleException(Throwable cause) {
		super(cause);
	}

	public ModuleException(String message, Throwable cause) {
		super(message, cause);
	}
}
