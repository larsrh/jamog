
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

package core.exception;

/**
 * A {@code InstantiationException} is thrown during instantiation of a class
 * if the underlying constructor throws an exception.
 *
 * @author torben
 */
public final class InstantiationException extends Exception
{
	/**
	 * @param class_name The name of the class which failed instantiation
	 * @param cause      The cause of the failed instantiation
	 */
	public InstantiationException(final String class_name, final Throwable cause)
	{
		super(cause);

		assert class_name != null;
		assert cause != null;

		this.class_name = class_name;
	}

	/**
	 * @return The name of the class which failed instantiation
	 */
	public final String getClassName()
	{
		return class_name;
	}

	@Override public final String getMessage()
	{
		return "The constructor of class " + class_name + " threw an exception: " + getCause().getMessage();
	}

	private final String class_name;
}
