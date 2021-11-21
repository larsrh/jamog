/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

import java.beans.PropertyVetoException;
import core.misc.module.ClassLoader;
import core.misc.module.PrivilegedAction;
import gui.MainWindow;
import gui.util.Properties;
import java.awt.AWTPermission;
import java.io.File;
import java.io.FilePermission;
import java.net.SocketPermission;
import java.security.Permission;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.PropertyPermission;
import java.util.Set;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

/**
 *
 * @author sylvester
 */
public class Starter {

	public static void main(String[] arguments) {
		// Start the commandline output manager
		CommandlineOutputManager.initialize();

		// Initiate Security system
		ClassLoader.initSecurity(
				new ClassLoader.PermissionVerifier() {

					@Override
					public final boolean verifyPermission(Permission perm, List<Class<?>> extensions) {
						if (!(perm instanceof FilePermission || perm instanceof SocketPermission || perm instanceof PropertyPermission || perm instanceof AWTPermission))
							return false;

						synchronized (denied_permissions) {
							Set<Permission> sp = denied_permissions.get(extensions);
							if (sp != null)
								for (Permission p : sp)
									if (p.implies(perm))
										return false;
						}

						synchronized (granted_permissions) {
							Set<Permission> sp = granted_permissions.get(extensions);
							if (sp != null)
								for (Permission p : sp)
									if (p.implies(perm))
										return true;
						}

						final StringBuilder sb = new StringBuilder("The class");
						if (extensions.size() > 1)
							sb.append("es");
						for (int j = 0; j < extensions.size(); ++j) {
							if (j != 0)
								sb.append(", ");
							else
								sb.append(" ");
							sb.append(extensions.get(j).getName());
						}
						if (extensions.size() > 1)
							sb.append(" have requested permission to ");
						else
							sb.append(" has requested permission to ");
						if (perm instanceof FilePermission) {
							FilePermission fp = (FilePermission) perm;
							sb.append("access the file ");
							sb.append(fp.getName());
						} else if (perm instanceof SocketPermission) {
							SocketPermission sp = (SocketPermission) perm;
							sb.append("connect to ");
							sb.append(sp.getName());
						} else if (perm instanceof PropertyPermission) {
							PropertyPermission pp = (PropertyPermission) perm;
							sb.append(pp.getActions());
							sb.append(" the property ");
							sb.append(pp.getName());
							sb.append(".");
						} else {
							sb.append("gain the ");
							sb.append(perm.getName());
							sb.append(" ability.");
						}
						sb.append("\n\nWould you like to grant the permission?");

						int res = new PrivilegedAction<Integer>() {

							@Override
							public final Integer run() {
								return JOptionPane.showOptionDialog(null, splitLines(sb.toString(), 50), "Permission grant requested", JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, null, new String[]{"Deny permanently", "Deny once", "Grant once", "Grant permanently"}, "Deny once");
							}

						}.run();

						switch (res) {
							case 3:
								synchronized (granted_permissions) {
									Set<Permission> sp = granted_permissions.get(extensions);
									if (sp == null) {
										sp = new LinkedHashSet<Permission>();
										granted_permissions.put(extensions, sp);
									}
									sp.add(perm);
								}
							case 2:
								return true;
							case 0:
								synchronized (denied_permissions) {
									Set<Permission> sp = denied_permissions.get(extensions);
									if (sp == null) {
										sp = new LinkedHashSet<Permission>();
										granted_permissions.put(extensions, sp);
									}
									sp.add(perm);
								}
							case JOptionPane.CLOSED_OPTION:
							case 1:
							default:
								return false;
						}
					}

					private final Map<List<Class<?>>, Set<Permission>> granted_permissions = new HashMap<List<Class<?>>, Set<Permission>>();
					private final Map<List<Class<?>>, Set<Permission>> denied_permissions = new HashMap<List<Class<?>>, Set<Permission>>();

					private final String splitLines(String str, int len) {
						String[] words = str.split(" ");

						StringBuilder sb = new StringBuilder();
						for (int i = 0, j = 0; i < words.length; ++i) {
							j += words[i].length();

							if (j > len) {
								sb.append(words[i]);
								sb.append("\n");
								j = 0;
							} else {
								sb.append(words[i]);
								sb.append(" ");
							}
						}

						return sb.toString();
					}

				});

		parseCommandlineArguments(arguments);

		JFrame window = new MainWindow();
		Properties.getProfile().setMainWindow(window);
		window.setVisible(true);
	}

	private static void parseCommandlineArguments(String[] arguments) {
		String argument;
		String value;
		List<File> extensions = new ArrayList<File>();
		String logfileName = "jamog.log";

		for (int i = 0; i < arguments.length; i++) {
			if (arguments[i].startsWith("--")) {
				argument = arguments[i].substring(2);
				if (i < arguments.length - 1)
					value = arguments[++i];
				else {
					MessageManager.getInstance().createMessage("Command line parse error", "Exprected value for argument \"" + argument + "\" but no more command line parameters were supplied.", MessageManager.MessageTypes.WARNING, MessageManager.MessageCategories.MESSAGE, null);
					continue;
				}
			} else if (arguments[i].startsWith("-")) {
				argument = arguments[i].substring(1, 2);
				value = arguments[i].substring(2);
			} else {
				MessageManager.getInstance().createMessage("Command line parse error", "Found value \"" + arguments[i] + "\" but argument has been expected.", MessageManager.MessageTypes.WARNING, MessageManager.MessageCategories.MESSAGE, null);
				continue;
			}
			argument = argument.toLowerCase();
			if (argument.equals("e") || argument.equals("extension"))
				extensions.add(new File(value));
			else if (argument.equals("l") || argument.equals("logfile"))
				logfileName = value;
			else
				MessageManager.getInstance().createMessage("Command line parse error", "The command line argument \"" + argument + "\" is not known.", MessageManager.MessageTypes.WARNING, MessageManager.MessageCategories.MESSAGE, null);
		}
		try {
			Properties.getProfile().setExtensionSources(extensions.toArray(new File[0]));
		} catch (PropertyVetoException exc) {
			MessageManager.getInstance().createMessage("Error while loading extensions", "Loading the extension sources was blocked by another part of the program. You will have to load the extensions manually.", MessageManager.MessageTypes.WARNING, MessageManager.MessageCategories.MESSAGE, exc);
		}
		// Set log file
		MessageManager.getInstance().setLogfile(new File(logfileName));
	}

}
