
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

/**
 * The {@code PrivilegedAction} interface provides a way to run privileged code
 * in a core class method what was invoked directly or indirectly by a loaded
 * class.
 *
 * @param <T> The return type of {@link #run() run()}
 * 
 * @author torben
 */
public interface PrivilegedAction<T>
{
	/**
	 * The code inside the {@link #run() run()} method will not throw a
	 * SecurityException because of loaded classes on the call stack above the
	 * caller.
	 *
	 * @return A custom return value
	 */
	public T run();
}
