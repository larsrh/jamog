
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
 * A {@code SerializingException} is thrown during serializing an object if the
 * object isn't serializable, i.e. the object doesn't implement the {@link
 * core.misc.serial.Serializable Serializable} interface and is not natively
 * supported by the {@link core.misc.serial.SerializingStream
 * SerializingStream}.
 *
 * @see core.misc.serial.SerializingStream SerializingStream
 * @see core.misc.serial.Serializable Serializable
 *
 * @author torben
 */
public final class SerializingException extends Exception
{
	/**
	 * @param obj The object that can't be serialized
	 */
	public SerializingException(final Object obj)
	{
		assert obj != null;

		this.obj = obj;
	}

	/**
	 * @return The object that can't be serialized
	 */
	public final Object getObject()
	{
		return obj;
	}

	@Override public final String getMessage()
	{
		return "The instance " + obj.toString() + " from class " + obj.getClass().getName() + " can't be serialized";
	}

	private final Object obj;
}
