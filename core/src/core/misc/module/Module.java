
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

import core.exception.ModuleException;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Modifier;

/**
 * The {@code Module} class provides information about loaded modules. It must
 * be subclassed to provide special handling of different kinds of modules.
 * Typical functionality in here could be a factoring method which creates
 * new instances of the underlying class, e.g. by invoking a constructor via
 * reflection.
 *
 * @param <T> The type of class that can be handled by the {@code Module}
 *
 * @author torben
 */
public abstract class Module<T>
{
	/**
	 * The {@code Description} annotation can be added to any module, to
	 * customize the information the {@code Module} will provide about
	 * it.
	 */
	@Retention(RetentionPolicy.RUNTIME) @Target(ElementType.TYPE) public static @interface Description
	{
		/**
		 * @return The name of the module, if different from the class name
		 */
		String name() default "";

		/**
		 * @return The description of the module
		 */
		String description() default "";

		/**
		 * @return The location of the module, if different from the package
		 */
		String location() default "";
	}

	/**
	 * @return The name of the module
	 */
	public final String getName()
	{
		return name;
	}

	/**
	 * @return The description of the module
	 */
	public final String getDescription()
	{
		return description;
	}

	/**
	 * @return The location of the module
	 */
	public final String getLocation()
	{
		return location;
	}

	/**
	 * @return The path of the module
	 */
	public final String[] getPath()
	{
		final String[] path_copy = new String[path.length];
		System.arraycopy(path, 0, path_copy, 0, path.length);
		return path_copy;
	}

	@Override public final String toString()
	{
		return name;
	}

	/**
	 * Creates a new {@code Module} and caches the informations about the
	 * class.
	 *
	 * @param c The loaded class
	 *
	 * @throws ModuleException if the class is not instancable
	 */
	protected Module(final Class<? extends T> c) throws ModuleException
	{
		assert c != null;

		final int mod = c.getModifiers();
		if(Modifier.isAbstract(mod) || Modifier.isInterface(mod))
			throw new ModuleException();

		final Description desc = c.getAnnotation(Description.class);
		final String cn = c.getName();
		final int last = cn.lastIndexOf(".");
		if(desc != null)
		{
			name = desc.name().length() == 0 ? cn.substring(last + 1) : desc.name();
			description = desc.description().length() == 0 ? "[No description]" : desc.description();
			location = desc.location().length() == 0 ? cn.substring(0, last) : desc.location();
		}
		else
		{
			name = cn.substring(last + 1);
			description = "[No description]";
			location = cn.substring(0, last);
		}

		path = location.split("\\.");
	}

	private final String name;
	private final String description;
	private final String location;
	private final String[] path;
}
