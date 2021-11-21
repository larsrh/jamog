
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
 * A {@code DeserializingException} is the superclass of all exceptions thrown
 * during deserializing an object if the object can't be deserialized, i.e. the
 * defining class wasn't found, the referenced static field wasn't found or the
 * class isn't deserializable because the class doesn't implement the {@link
 * core.misc.serial.Serializable Serializable} interface and is not natively
 * supported by the {@link core.misc.serial.DeserializingStream
 * DeserializingStream} or the constructor required by the
 * {@link core.misc.serial.Serializable Serializable} interface for
 * deserializing was not found.
 * 
 * @see ClassNotFound
 * @see StaticFieldNotFound
 * @see ClassNotDeserializable
 *
 * @author torben
 */
public abstract class DeserializingException extends Exception
{
	/**
	 * A {@code ClassNotFound} is thrown during deserializing an object if the
	 * defining class wasn't found.
	 */
	public static final class ClassNotFound extends DeserializingException
	{
		/**
		 * @param name The name of the unfound class
		 */
		public ClassNotFound(final String name)
		{
			assert name != null;

			this.class_name = name;
		}

		/**
		 * @return The name of the unfound class
		 */
		public final String getClassName()
		{
			return class_name;
		}

		@Override public final String getMessage()
		{
			return "The class " + class_name + " was not found";
		}

		private final String class_name;
	}

	/**
	 * A {@code ClassNotDeserializable} is thrown during deserializing an
	 * object if the class isn't deserializable because the class doesn't
	 * implement the {@link core.misc.serial.Serializable Serializable}
	 * interface and is not natively supported by the {@link
	 * core.misc.serial.DeserializingStream DeserializingStream} or the
	 * constructor required by the {@link core.misc.serial.Serializable
	 * Serializable} interface for deserializing was not found.
	 */
	public static final class ClassNotDeserializable extends DeserializingException
	{
		/**
		 * @param class_name The name of the undeserializable class
		 */
		public ClassNotDeserializable(final String class_name)
		{
			assert class_name != null;

			this.class_name = class_name;
		}

		/**
		 * @return The name of the undeserializable class
		 */
		public final String getClassName()
		{
			return class_name;
		}

		@Override public final String getMessage()
		{
			return "The class " + class_name + " is not deserializable";
		}

		private final String class_name;
	}

	/**
	 * A {@code CorruptStream} is thrown during deserializing an object if the
	 * state of the object is not recoverable from the stream, i.e. the stream
	 * is damaged or wasn't serialized from the current set of classes.
	 */
	public static final class CorruptStream extends DeserializingException
	{
		/**
		 * @param class_name The name of the class whose state wasn't
		 *                   recoverable or null if unknown
		 */
		public CorruptStream(final String class_name)
		{
			this.class_name = class_name;
		}

		/**
		 * @return The name of the class whose state wasn't recoverable or null
		 *         if unknown
		 */
		public final String getClassName()
		{
			return class_name;
		}

		@Override public final String getMessage()
		{
			return "The class " + class_name == null ? "[unspecified]" : class_name + " couldn't recover its state, i.e. the given stream is corrupt or wasn't serialized from the given set of classes.";
		}

		private final String class_name;
	}
}
