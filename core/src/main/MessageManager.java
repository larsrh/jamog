/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author sylvester
 */
public class MessageManager {

	/**
	 * This enum holds some constants used to describe the severeness of
	 * messages.
	 */
	public static enum MessageTypes {

		/**
		 * This @link{MessageType} should be used for messages reporting errors
		 * so severe, that the program will most likely not be able to continue
		 * correctly.
		 */
		CRITICAL_ERROR,
		/**
		 * This @link{MessageType} should be used for messages reporting errors
		 * that caused the program to fail at whatever it was doing, but which
		 * will not hinder the program at whole. For errors which are caused by
		 * and may easily be understood by the user (i.e. the user specified a
		 * non-existing file to open) {@link MessageTypes#WARNING}s should be
		 * used instead.
		 */
		ERROR,
		/**
		 * This @link{MessageType} should be used for messages reporting
		 * problems which endanger whatever the program was doing but whichs
		 * occurence <em>may</em> be correct nevertheless.<br/>
		 * This MessageType should also be used for non-critical errors which
		 * are caused by and may easily be understood by the user (i.e. the user
		 * specified a non-existing file to open).
		 */
		WARNING,
		/**
		 * This @link{MessageType} should be used for messages reporting
		 * incidents which are not a problem but of which the user should come
		 * to know, so that he can use the program accordingly. These messages
		 * may be presented in a non-immediate way, so use
		 * {@link MessageTypes#WARNING} for time-critical infos.
		 */
		INFO,
		/**
		 * This @link{MessageType} should be used for messages which may by of
		 * some interest to the user but of which he might as well not know.
		 */
		NOTE;

		@Override
		public String toString() {
			switch (this) {
				case CRITICAL_ERROR:
					return "Critical error";
				case ERROR:
					return "Error";
				case WARNING:
					return "Warning";
				case INFO:
					return "Information";
				case NOTE:
					return "Note";
				default:
					return super.toString();
			}
		}

	}

	/**
	 * This enum hold some constants used to describe the type of message i.e.
	 * if they have just an "OK" button of if the user can interact with them in
	 * any other way. Some of these constants will cause
	 * {@link MessageManager#createMessage(java.lang.String, java.lang.String,
	 * main.MessageManager.MessageTypes, main.MessageManager.MessageCategories,
	 * java.lang.Throwable)}
	 * to return a value not equal to {@code null} being the response of the
	 * user.
	 */
	public static enum MessageCategories {

		/**
		 * This results in a message simply informing and taking no information
		 * from the user.
		 * {@link MessageManager#createMessage(java.lang.String,
		 * java.lang.String, main.MessageManager.MessageTypes,
		 * main.MessageManager.MessageCategories, java.lang.Throwable)}
		 * will return {@code null}.
		 */
		MESSAGE,
		/**
		 * This results in a message forcing the user to answer with yes or no.
		 * The result is transmitted in the return value of
		 * {@link MessageManager#createMessage(java.lang.String,
		 * java.lang.String, main.MessageManager.MessageTypes,
		 * main.MessageManager.MessageCategories, java.lang.Throwable)},
		 * see in the JavaDoc of this function for details.
		 */
		YES_NO_QUESTION,
		/**
		 * This results in a message forcing the user to answer with yes or no.
		 * Additionally the user can tell that his answer (yes or no) should be
		 * valid for all future questions of the same type. The result is
		 * transmitted in the return value of
		 * {@link MessageManager#createMessage(java.lang.String,
		 * java.lang.String, main.MessageManager.MessageTypes,
		 * main.MessageManager.MessageCategories, java.lang.Throwable)},
		 * see in the JavaDoc of this function for details.
		 */
		YES_NO_REMEMBER_QUESTION,
		/**
		 * This results in a message enabling the user to give a String as an
		 * answer which may be empty. The result is transmitted in the return
		 * value of
		 * {@link MessageManager#createMessage(java.lang.String,
		 * java.lang.String, main.MessageManager.MessageTypes,
		 * main.MessageManager.MessageCategories, java.lang.Throwable)},
		 * see in the JavaDoc of this function for details.
		 */
		CUSTOM_QUESTION;

		@Override
		public String toString() {
			switch (this) {
				case MESSAGE:
					return "Message";
				case YES_NO_QUESTION:
					return "YN-question";
				case YES_NO_REMEMBER_QUESTION:
					return "Persistent YN-question";
				case CUSTOM_QUESTION:
					return "Custom question";
				default:
					return super.toString();
			}
		}

	}

	/**
	 * Classes which want to be informed if message are created using
	 * {@link MessageManager} must implement this interface and the register
	 * themselves on the singleton instance of {@link MessageManager} using the
	 * method {@link MessageManager#addReceiver(main.MessageManager.MessageReceiver)}
	 * (and {@link MessageManager#removeReceiver(main.MessageManager.MessageReceiver)}).
	 * <br /><br />
	 * The receivers are called in the order of the registration and can be
	 * requested to return an answer to the request (normally acquired by
	 * request to the user). If one Receiver answered the request the following
	 * receuvers will not be requested to answer the request and their answers
	 * will be ignored.
	 *
	 * @see MessageReceiver#receiveMessage(java.lang.String, java.lang.String, main.MessageManager.MessageTypes, main.MessageManager.MessageCategories, boolean)
	 * @see MessageManager#createMessage(java.lang.String, java.lang.String, main.MessageManager.MessageTypes, main.MessageManager.MessageCategories, java.lang.Throwable)
	 */
	public static interface MessageReceiver {

		/**
		 * By this method the {@code MessageReceiver} is informed about a new
		 * message. The first for parameter are passed just as they are given
		 * to {@link MessageManager#createMessage(java.lang.String, java.lang.String, main.MessageManager.MessageTypes, main.MessageManager.MessageCategories, java.lang.Throwable)}
		 * the last one indicates if the message has already been answered (or
		 * needs no answer). If this parameter {@code answered} is set to
		 * {@code false} the receiver is requested to return an answer using the
		 * return object.
		 * <br /><br />
		 * The possibilities for the answer object depend on the given category
		 * (see {@link MessageCategories}):
		 * <ul>
		 *	<li>{@link MessageCategories#MESSAGE}: The return object should be
		 * {@code null} but will be ignored anyway.</li>
		 * 	<li>{@link MessageCategories#YES_NO_QUESTION}: The return object
		 * must be a boolean being {@code true} for "yes" and {@code false} for
		 * "no".</li>
		 * 	<li>{@link MessageCategories#YES_NO_REMEMBER_QUESTION}: As in the
		 * above possibility the return object may be a boolean or it may be an
		 * int being 0, 1, 2 or 3 for "no", "yes", "no for all", "yes for all".
		 * </li>
		 * 	<li>{@link MessageCategories#CUSTOM_QUESTION}: The return object
		 * must be a String object.</li>
		 * </ul>
		 *
		 * @param title The title of the message
		 * @param text The body of the message
		 * @param type The type of the message
		 * @param category The category of the message
		 * @param exception The exception which cause the message or {@code null}
		 * @param answered Whether or not an answer is required,
		 * @return The answer object or {@code null} if appropriate.
		 *
		 * @see MessageManager#createMessage(java.lang.String, java.lang.String, main.MessageManager.MessageTypes, main.MessageManager.MessageCategories, java.lang.Throwable)
		 */
		public Object receiveMessage(String title,
									 String text,
									 MessageTypes type,
									 MessageCategories category,
									 Throwable exception,
									 boolean answered);

	}

	private static MessageManager instance = new MessageManager();

	/**
	 * This method returns the only instance of {@link MessageManager} that must
	 * exist. This instance is created in a static statement and will therefore
	 * be available on the first usage of this class.
	 *
	 * @return The {@code MessageManager} instance
	 */
	public static MessageManager getInstance() {
		return MessageManager.instance;
	}

	private List<MessageReceiver> receivers = new LinkedList<MessageReceiver>();
	private PrintStream logfileStream = null;

	private MessageManager() {
	}

	/**
	 * This method tries to print the specified message into the logfile (if
	 * a logfile is specified) and then notifies all registered
	 * {@link MessageReceiver}s. If the category ({@link MessageCategories}) of
	 * the message requires an answer each of these receivers is given the
	 * chance to answer in the order of their registration until one answers.
	 * Possible answers of the later receivers are ignored and not requested.
	 * The answers is then written into the logfile, too.
	 * <br /><br />
	 * If the writing into the logfile fails for any reason the logfile is
	 * deactivated and this method calles itself with a new error message,
	 * enabling the receivers (and therefore the user) to react.
	 * <br /><br />
	 * If the message category requires an answer but no receiver returns a
	 * valid answer (i.e. an answer with fitting type) {@code null} is returned
	 * as answer. For more information about the possibilities of the answer
	 * object see
	 * {@link MessageReceiver#receiveMessage(java.lang.String, java.lang.String, main.MessageManager.MessageTypes, main.MessageManager.MessageCategories, boolean)}.
	 *
	 * @param title The title of the message
	 * @param text The body of the message
	 * @param type The type ({@link MessageTypes}) of the message
	 * @param category The category ({@link MessageCategories}) of the message
	 * @param exception This may be set to a throwable (noramlly exception)
	 * which is the cause for the message. It will be passed to all receivers
	 * which might be able to use the information
	 * @return The answer of the first answering receiver (or {@code null}).
	 *
	 * @see #setLogfile(java.io.File)
	 */
	public Object createMessage(String title, String text, MessageTypes type, MessageCategories category, Throwable exception) {
		Object returnValue = null;

		if (this.logfileStream != null)
			this.logfileStream.print(new Date().toString() +
					" [" + type.toString() + "] " +
					"(" + category.toString() + ") " +
					title.replace("\n", "") + "\n\t" +
					text.replace("\n", "\n\t") +
					"\n\t--> Answer: ");

		for (MessageReceiver receiver : this.receivers)
			if (returnValue == null && category != MessageCategories.MESSAGE) {
				returnValue = receiver.receiveMessage(title, text, type, category, exception, false);
				switch (category) {
					case YES_NO_QUESTION:
						if (!(returnValue instanceof Boolean))
							returnValue = null;
						break;
					case YES_NO_REMEMBER_QUESTION:
						if (!(returnValue instanceof Boolean) &&
								!(returnValue instanceof Integer && (Integer) returnValue >= 0 && (Integer) returnValue <= 3))
							returnValue = null;
						break;
					case CUSTOM_QUESTION:
						if (!(returnValue instanceof String))
							returnValue = null;
						break;
					case MESSAGE:
					default:
						returnValue = null;
						break;
				}
			} else
				receiver.receiveMessage(title, text, type, category, exception, true);

		if (this.logfileStream != null)
			this.logfileStream.print(returnValue == null ? "NULL\n\n" : returnValue.toString() + "\n\n");

		return returnValue;
	}

	/**
	 * This adds the given {@link MessageReceiver} to the list of receivers
	 * notfied if a message is created.
	 *
	 * @param receiver The Receiver to add
	 * @return by default {@code True}
	 *
	 * @see MessageReceiver
	 * @see #createMessage(java.lang.String, java.lang.String, main.MessageManager.MessageTypes, main.MessageManager.MessageCategories, java.lang.Throwable)
	 * @see #removeReceiver(main.MessageManager.MessageReceiver)
	 */
	public boolean addReceiver(MessageReceiver receiver) {
		return this.receivers.add(receiver);
	}

	/**
	 * This removed the given {@link MessageReceiver} from the list of
	 * receivers.
	 *
	 * @param receiver The receiver to remove
	 * @return {@code True} if the receiver was registered and could be removed,
	 * {@code false} otherwise.
	 * 
	 * @see #addReceiver(main.MessageManager.MessageReceiver)
	 */
	public boolean removeReceiver(MessageReceiver receiver) {
		return this.receivers.remove(receiver);
	}

	/**
	 * Tries the set the logfile to the given file. If there are any problems
	 * (file not existing, being a folder, not being writable, etc.) the logfile
	 * is not replaced (and may therefore stay {@code null}). Additonally
	 * {@code null} may be passed as new value, thus disabling the log file.
	 *
	 * @param file The new logfile
	 * @return Whether or not the setting of the logfile was successfull.
	 */
	public boolean setLogfile(File file) {
		PrintStream newLogfileStream;
		if (file == null) {
			this.logfileStream = null;
			return true;
		} else {
			try {
				newLogfileStream = new PrintStream(file);
			} catch (FileNotFoundException exc) {
				this.createMessage(
						"Error while setting error log file",
						"Could not set error log file, because the file could not be found.",
						MessageTypes.ERROR,
						MessageCategories.MESSAGE,
						exc);
				return false;
			}
			this.createMessage("Closing log file",
					"Closing log file, switching to " + file.getAbsolutePath(),
					MessageTypes.INFO,
					MessageCategories.MESSAGE,
					null);
			this.logfileStream = newLogfileStream;
			this.createMessage("Opened log file",
					"Opened log file",
					MessageTypes.NOTE,
					MessageCategories.MESSAGE,
					null);
			return true;
		}
	}

}
