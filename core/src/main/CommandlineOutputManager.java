/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package main;

import java.io.PrintStream;
import main.MessageManager.MessageCategories;
import main.MessageManager.MessageTypes;

/**
 *
 * @author sylvester
 */
public class CommandlineOutputManager implements MessageManager.MessageReceiver {

	private static CommandlineOutputManager instance;

	public static void initialize() {
		if (CommandlineOutputManager.instance == null) {
			CommandlineOutputManager.instance = new CommandlineOutputManager();
			MessageManager.getInstance().addReceiver(CommandlineOutputManager.instance);
			MessageManager.getInstance().createMessage(
					"Commandline output manager started",
					"The commandline output manager has been initialized.",
					MessageTypes.NOTE,
					MessageCategories.MESSAGE,
					null);
		}
	}

	private CommandlineOutputManager() {
	}

	@Override
	public Object receiveMessage(String title, String text, MessageTypes type, MessageCategories category, Throwable exception, boolean answered) {
		PrintStream outputStream;
		switch (type) {
			case NOTE:
			case INFO:
				outputStream = System.out;
				break;
			case WARNING:
			case ERROR:
			case CRITICAL_ERROR:
			default:
				outputStream = System.err;
		}

		title = title.replaceAll("\n", " ");
		text = "\t" + text.replaceAll("\n", "\n\t");

		outputStream.println(type.toString() + ": " + title);
		outputStream.println(text);
		if (category != MessageCategories.MESSAGE) {
			outputStream.println("This message requires an answer (of the " +
					"user) which this output manager does not support. If " +
					"the answer is not given by another message receiver " +
					"(additional) errors might occour.");
		}
		outputStream.println();

		return null;
	}

}
