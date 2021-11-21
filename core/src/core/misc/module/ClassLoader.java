
/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *                                                                           *
 * Copyright 2009 Lars Hupel, Torben Maack, Sylvester Tremmel                *
 *                                                                           *
 * This file is part of Jamog.                                               *
 *                                                                           *
 * Jamog is free software: you can redistribute it and/or modify             *
 * it under the terms of the GNU General Public License as published by      *
 * the Free Software Foundation; version 3.                                  *
 *                                                                           *
 * Jamog is distributed in the hope that it will be useful,                  *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of            *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the              *
 * GNU General Public License for more details.                              *
 *                                                                           *
 * You should have received a copy of the GNU General Public License         *
 * along with Jamog. If not, see <http://www.gnu.org/licenses/>.             *
 *                                                                           *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

package core.misc.module;

import core.build.Component;
import core.misc.test.Testable;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.AccessControlContext;
import java.security.AccessControlException;
import java.security.AccessController;
import java.security.BasicPermission;
import java.security.Permission;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * The {@code ClassLoader} utility class provides functionality to load classes
 * in a sandbox with definable permission granting behaviour.
 *
 * @author torben
 */
public final class ClassLoader
{
	/**
	 * The {@code PermissionVerifier} interface is used to grant or deny a
	 * permission for a list of loaded classes. Every time one ore more loaded
	 * classes try to directly or indirectly perform an operation which
	 * requires a permission, the {@link
	 * #verifyPermission(java.security.Permission, java.util.List)
	 * verifyPermission(Permission, List)} method of the {@code
	 * PermissionVerifier} that was given to the {@link
	 * #initSecurity(core.misc.load.ClassLoader.PermissionVerifier)
	 * initSecurity(PermissionVerifier)} method is invoked to check if
	 * the permission is granted or refused.
	 */
	public static interface PermissionVerifier
	{
		/**
		 * Is invoked every time one ore more classes try to directly or
		 * indirectly perform an operation which requires a permission and the
		 * {@code PermissionVerifier} was given to {@link
		 * #initSecurity(core.misc.load.ClassLoader.PermissionVerifier)
		 * initSecurity(PermissionVerifier)} method.
		 *
		 * @param perm    The requested permission
		 * @param classes The loaded classes that directly or indirectly
		 *                requested the permission
		 *
		 * @return true if the permission should be granted, false otherwise
		 */
		public boolean verifyPermission(Permission perm, List<Class<?>> classes);
	}

	/**
	 * Initializes the class loading security. After calling this, a
	 * SecurityManager has been installed and classes can be loaded. Only
	 * classes loaded by the {@link #load(java.io.File[]) load(File[])} method
	 * are affected by the SecurityManager. For granting or refusing
	 * permissions to loaded classes, a {@link PermissionVerifier} has to be
	 * provided.
	 *
	 * @see PermissionVerifier
	 *
	 * @param pv The {@link PermissionVerifier} to use for granting or refusing
	 *           permissions to loaded classes.
	 *
	 * @throws SecurityException if called directly or indirectly by a loaded
	 *                           class
	 */
	public static final void initSecurity(final PermissionVerifier pv)
	{
		assert pv != null;

		checkCorePermission();

		System.setSecurityManager(new ClassLoadSecurityManager(pv));
	}

	/**
	 * Load a set of locations for classes, replacing all old loaded classes. A
	 * location can be a JAR file, a directory or a class file. All class files
	 * in each location are loaded into the program.
	 *
	 * @see #initSecurity(core.misc.load.ClassLoader.PermissionVerifier)
	 *      initSecurity(PermissionVerifier)
	 *
	 * @param locations A list of JAR files, directories or class files to load
	 *                  class files from
	 *
	 * @throws IOException       if an I/O error occurs
	 * @throws SecurityException if called directly or indirectly by a
	 *                           loaded class or if the class loading security
	 *                           hasn't been initialized
	 */
	public static final void load(final File... locations) throws IOException
	{
		assert locations != null;
		assert checkFiles(locations);

		final SecurityManager sm = System.getSecurityManager();
		if(sm != null)
			sm.checkPermission(core_perm);
		else
			throw new SecurityException("No SecurityManager installed - can't load classes");

		final Map<String, byte[]> buffer_map = new HashMap<String, byte[]>();

		for(final File file : locations)
			addFile(file, null, buffer_map);

		final Loader loader = new Loader(buffer_map);
		classes.clear();

		for(final String name : buffer_map.keySet())
		{
			try { classes.put(name, loader.loadClass(name)); }
			catch(final ClassNotFoundException ex) { continue; } // will not happen
		}

		Component.Handler.getHandler().rescanLoadedClasses();
		Testable.Handler.getHandler().rescanLoadedClasses();
	}

	/**
	 * Searches all loaded classes by a name, both program and loaded class.
	 *
	 * @param name The name of the class
	 *
	 * @return The corresponding {@link Class} object
	 *
	 * @throws ClassNotFoundException if a class with this name wasn't found
	 */
	public static final Class<?> getClass(final String name) throws ClassNotFoundException
	{
		assert name != null;

		if(classes.containsKey(name))
			return classes.get(name);
		else
			return Class.forName(name);
	}

	/**
	 * @param c The class to check
	 *
	 * @return true if the class is a loaded class, false otherwise
	 */
	public static final boolean isLoaded(final Class<?> c)
	{
		assert c != null;

		final java.lang.ClassLoader l = c.getClassLoader();
		return l != null && l instanceof Loader;
	}

	/**
	 * @return A collection of all loaded classes
	 */
	public static final Collection<Class<?>> getLoaded()
	{
		return Collections.unmodifiableCollection(classes.values());
	}

	/**
	 * @throws SecurityException if directly or indirectly called by a loaded
	 *                           class
	 */
	public static final void checkCorePermission()
	{
		final SecurityManager sm = System.getSecurityManager();
		if(sm != null)
			sm.checkPermission(core_perm);
	}

	private static final class Loader extends java.lang.ClassLoader
	{
		Loader(final Map<String, byte[]> map)
		{
			this.map = map;
		}

		@Override public final Class<?> findClass(final String name) throws ClassNotFoundException
		{
			if(!map.containsKey(name))
				throw new ClassNotFoundException();
			final byte[] bytes = map.get(name);
			return defineClass(null, bytes, 0, bytes.length, pd);
		}

		private final Map<String, byte[]> map;
	}

	private static final class ClassLoadSecurityManager extends SecurityManager
	{
		ClassLoadSecurityManager(final PermissionVerifier pv)
		{
			this.pv = pv;
		}

		@Override public final void checkPermission(final Permission perm)
		{
			try
			{
				AccessController.getContext().checkPermission(perm);
			}
			catch(final AccessControlException ace)
			{
				if(!isPrivileged() && !verify(perm))
					throw ace;
			}
		}

		@Override public final void checkPermission(final Permission perm, final Object context)
		{
			if(!(context instanceof AccessControlContext))
				throw new SecurityException();

			try
			{
				((AccessControlContext)context).checkPermission(perm);
			}
			catch(final AccessControlException ace)
			{
				if(!isPrivileged() && !verify(perm))
					throw ace;
			}
		}

		private final PermissionVerifier pv;

		private final boolean isPrivileged()
		{
			Class[] stack = getClassContext();

			for(int i = 0; i < stack.length; ++i)
				if(!isLoaded(stack[i]))
				{
					if(i < stack.length - 1 && !isLoaded(stack[i + 1]) && (pa.isAssignableFrom(stack[i]) && !pa.isAssignableFrom(stack[i + 1]) || jpa.isAssignableFrom(stack[i]) && !jpa.isAssignableFrom(stack[i + 1]) || jpae.isAssignableFrom(stack[i]) && !jpae.isAssignableFrom(stack[i + 1])))
						return true;
				}
				else
					return false;

			return true;
		}

		private final boolean verify(final Permission perm)
		{
			List<Class<?>> classes = new ArrayList<Class<?>>();
			for(Class<?> c : getClassContext())
				if(isLoaded(c))
					classes.add(c);

			return pv.verifyPermission(perm, classes);
		}

		private static final Class<PrivilegedAction> pa = PrivilegedAction.class;
		private static final Class<java.security.PrivilegedAction> jpa = java.security.PrivilegedAction.class;
		private static final Class<java.security.PrivilegedExceptionAction> jpae = java.security.PrivilegedExceptionAction.class;
	}

	private static final Map<String, Class<?>> classes = new HashMap<String, Class<?>>();
	private static final ProtectionDomain pd = new ProtectionDomain(null, null);
	private static final Permission core_perm = new BasicPermission("invokeCoreMethod") {};

	private static final boolean checkFiles(final File[] files)
	{
		for(final File f : files)
			if(f == null)
				return false;
		return true;
	}

	private static final void addFile(final File file, final String parent, final Map<String, byte[]> buffer_map) throws IOException
	{
		if(!file.exists())
				throw new FileNotFoundException(file.getName());

		if(file.isFile())
		{
			final String name = file.getName();
			final String suffix = name.toLowerCase(Locale.ENGLISH).substring(name.lastIndexOf('.'));

			if(suffix.equals(".jar"))
			{
				final JarFile jar = new JarFile(file);

				try
				{
					for(final Enumeration<JarEntry> e = jar.entries(); e.hasMoreElements();)
					{
						final JarEntry je = e.nextElement();

						final String entry_name = je.getName();
						if(!entry_name.toLowerCase(Locale.ENGLISH).endsWith(".class"))
							continue;

						read(entry_name.substring(0, entry_name.length() - 6).replace('/', '.'), jar.getInputStream(je), je.getSize(), buffer_map);
					}
				}
				finally
				{
					jar.close();
				}

				return;
			}
			else if(!suffix.equals(".class"))
				return;

			final InputStream in = new FileInputStream(file);

			try
			{
				read((parent == null ? "" : parent) + name.substring(0, name.length() - 6), in, file.length(), buffer_map);
			}
			finally
			{
				in.close();
			}
		}
		else
			for(final File child : file.listFiles())
				addFile(child, parent == null ? "" : parent + file.getName() + ".", buffer_map);
	}

	private static final void read(final String name, final InputStream in, final long size, final Map<String, byte[]> buffer_map) throws IOException
	{
		final byte[] bytes = new byte[(int)size];
		for(int off = 0, read; off < bytes.length && (read = in.read(bytes, off, bytes.length - off)) != -1; off += read);
		buffer_map.put(name, bytes);
	}

	private ClassLoader()
	{
	}
}
