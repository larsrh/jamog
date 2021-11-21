
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

package core.misc.serial;

import core.exception.SerializingException;

import java.io.IOException;

/**
 * The {@code Serializable} interface is used to make a class serializable with
 * the {@link SerializingStream}. The serializing is done in the {@link
 * #serialize(core.misc.serial.SerializingStream) serialize(SerializingStream)}
 * function. To provide deserializing via the {@link DeserializingStream}, the
 * class must implement a constructor with a {@link DeserializingStream} as its
 * only parameter or a static method in the form {@code Object
 * getDeserializedObject(DeserializingStream)} which returns a deserialized
 * object. Both may trow {@link IOException}s, {@link
 * core.exception.DeserializingException DeserializingException}s and {@link
 * core.exception.InstantiationException InstantiationException}s.
 * 
 * @see SerializingStream
 * @see DeserializingStream
 *
 * @author torben
 */
public interface Serializable
{
	/**
	 * Is invoked to write the state of the object into a {@link
	 * SerializingStream}.
	 *
	 * @param out The {@link SerializingStream} to write the state to
	 *
	 * @throws IOException          if the {@link SerializingStream} throws an
	 *                              {@code IOException}
	 * @throws SerializingException if the {@link SerializingStream} throws an
	 *                              {@code SerializingException}
	 */
	public void serialize(SerializingStream out) throws IOException, SerializingException;
}
