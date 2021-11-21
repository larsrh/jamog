
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

import core.exception.DeserializingException;
import core.exception.InstantiationException;
import core.misc.module.ClassLoader;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * The {@code DeserializingStream} class provides deserializing objects from an
 * {@link InputStream} It provides support for all Java native types, native
 * type wrapper classes, arrays, Strings, Enums, Collections, Maps and all
 * classes which implements the {@link Serializable} interface.
 *
 * @author torben
 */
public final class DeserializingStream extends FilterInputStream
{
	/**
	 * Creates a new {@code DeserializingStream} which reads from an {@link
	 * InputStream}.
	 *
	 * @param in The {@link InputStream} to read from
	 */
	public DeserializingStream(final InputStream in)
	{
		super(in);

		assert in != null;

		class_map = new HashMap<Integer, Class<?>>();
		object_map = new HashMap<Integer, Object>();
		constructor_cache = new HashMap<Class<?>, Constructor<?>>();
		static_cache = new HashMap<Class<?>, Method>();
		buffer = new byte[32];
	}

	/**
	 * Reads a raw boolean value.
	 *
	 * @return The boolean value
	 *
	 * @throws IOException            if the underlying stream throws an
	 *                                {@code IOException}
	 * @throws DeserializingException if the boolean can't be deserialized
	 */
	public final boolean readBoolean() throws IOException, DeserializingException
	{
		read(1);
		return buffer[0] == 1;
	}

	/**
	 * Reads a raw byte value.
	 *
	 * @return The byte value
	 *
	 * @throws IOException            if the underlying stream throws an
	 *                                {@code IOException}
	 * @throws DeserializingException if the byte can't be deserialized
	 */
	public final byte readByte() throws IOException, DeserializingException
	{
		read(1);
		return buffer[0];
	}

	/**
	 * Reads a raw short value.
	 *
	 * @return The short value
	 *
	 * @throws IOException            if the underlying stream throws an
	 *                                {@code IOException}
	 * @throws DeserializingException if the short can't be deserialized
	 */
	public final short readShort() throws IOException, DeserializingException
	{
		read(2);
		return (short)((buffer[0] & 0xff) | ((buffer[1] & 0xff) << 8));
	}

	/**
	 * Reads a raw character value.
	 *
	 * @return The character value
	 *
	 * @throws IOException            if the underlying stream throws an
	 *                                {@code IOException}
	 * @throws DeserializingException if the char can't be deserialized
	 */
	public final char readCharacter() throws IOException, DeserializingException
	{
		read(2);
		return (char)((buffer[0] & 0xff) | ((buffer[1] & 0xff) << 8));
	}

	/**
	 * Reads a raw integer value.
	 *
	 * @return The integer value
	 *
	 * @throws IOException            if the underlying stream throws an
	 *                                {@code IOException}
	 * @throws DeserializingException if the integer can't be deserialized
	 */
	public final int readInteger() throws IOException, DeserializingException
	{
		read(4);
		return (buffer[0] & 0xff) | ((buffer[1] & 0xff) << 8)| ((buffer[2] & 0xff) << 16)| ((buffer[3] & 0xff) << 24);
	}

	/**
	 * Reads a raw long value.
	 *
	 * @return The long value
	 *
	 * @throws IOException            if the underlying stream throws an
	 *                                {@code IOException}
	 * @throws DeserializingException if the long can't be deserialized
	 */
	public final long readLong() throws IOException, DeserializingException
	{
		read(8);
		return ((long)buffer[0] & 0xff) | (((long)buffer[1] & 0xff) << 8) | (((long)buffer[2] & 0xff) << 16) | (((long)buffer[3] & 0xff) << 24) | (((long)buffer[4] & 0xff) << 32) | (((long)buffer[5] & 0xff) << 40) | (((long)buffer[6] & 0xff) << 48) | (((long)buffer[7] & 0xff) << 56);
	}

	/**
	 * Reads a raw float value.
	 *
	 * @return The float value
	 *
	 * @throws IOException            if the underlying stream throws an
	 *                                {@code IOException}
	 * @throws DeserializingException if the float can't be deserialized
	 */
	public final float readFloat() throws IOException, DeserializingException
	{
		return Float.intBitsToFloat(readInteger());
	}

	/**
	 * Reads a raw double value.
	 *
	 * @return The double value
	 *
	 * @throws IOException            if the underlying stream throws an
	 *                                {@code IOException}
	 * @throws DeserializingException if the double can't be deserialized
	 */
	public final double readDouble() throws IOException, DeserializingException
	{
		return Double.longBitsToDouble(readLong());
	}

	/**
	 * Reads a String by reading its content as an UTF-8 encoded byte-array.
	 *
	 * @return The String
	 *
	 * @throws IOException            if the underlying stream throws an
	 *                                {@code IOException}
	 * @throws DeserializingException if the string can't be deserialized
	 */
	public final String readString() throws IOException, DeserializingException
	{
		int len = readInteger();
		read(len);
		return new String(buffer, 0, len, UTF8);
	}

	/**
	 * Invokes {@link #readObject(java.lang.Class, java.lang.Class<?>[])
	 * readObject(null)}.
	 *
	 * @param <T> The type of the returned object
	 * 
	 * @return The readed object
	 *
	 * @throws IOException            if the underlying stream throws an
	 *                                {@code IOException}
	 * @throws DeserializingException if the object can't be deserialized
	 * @throws InstantiationException if the objects constructor has thrown an
	 *                                exception
	 */
	@SuppressWarnings("unchecked")
	public final <T> T readObject() throws IOException, DeserializingException, InstantiationException
	{
		return (T)readObject(null);
	}

	/**
	 * Reads an object of type {@code T}. If the class information was not
	 * stored in the stream, the class must be provided instead as the first
	 * parameter. For arrays, Collections and Maps the child's classes must be
	 * also provided if they weren't saved in the stream.
	 *
	 * @param <T> The type of the returned object
	 * 
	 * @param obj_class    if the objects class was not written to the stream,
	 *                     it must be provided, otherwise null
	 * @param deep_classes if the child objects class was not written to the
	 *                     stream in the corresponding depth, it must be
	 *                     provided, otherwise null
	 *
	 * @return The readed object
	 *
	 * @throws IOException            if the underlying stream throws an
	 *                                {@code IOException}
	 * @throws DeserializingException if the object can't be deserialized
	 * @throws InstantiationException if the objects constructor has thrown an
	 *                                exception
	 */
	@SuppressWarnings("unchecked")
	public final <T> T readObject(Class<T> obj_class, final Class<?>... deep_classes) throws IOException, DeserializingException, InstantiationException
	{
		assert deep_classes != null;

		switch(readByte())
		{
		case DIRECT:
			final int object_id = readInteger();

			if(obj_class == null)
			{
				switch(readByte())
				{
				case DIRECT:
					final int class_id = readInteger();
					obj_class = (Class<T>)getClass(readString());
					class_map.put(class_id, obj_class);
					break;
				case REFERENCE:
					try { obj_class = (Class<T>)class_map.get(readInteger()); }
					catch(final ClassCastException ex) { throw new DeserializingException.CorruptStream(null); }
					if(obj_class == null)
						throw new DeserializingException.CorruptStream(null);
					break;
				default:
					throw new DeserializingException.CorruptStream(null);
				}
			}

			Object o;
			if(obj_class.isArray())
			{
				final int len = readInteger();
				final Class<?> c = obj_class.getComponentType();

				if(!c.isPrimitive())
				{
					final Object[] a = (Object[])Array.newInstance(c, len);

					if(deep_classes.length == 0)
						for(int i = 0; i < len; ++i)
							a[i] = readObject(null);
					else
					{
						final Class<?>[] deeper_classes = new Class<?>[deep_classes.length - 1];
						System.arraycopy(deep_classes, 1, deeper_classes, 0, deeper_classes.length);
						for(int i = 0; i < len; ++i)
							a[i] = readObject(deep_classes[0], deeper_classes);
					}

					o = a;
				}
				else if(c == Boolean.TYPE)
				{
					final boolean[] a = (boolean[])Array.newInstance(c, len);
					for(int i = 0; i < len; ++i)
							a[i] = readBoolean();
					o = a;
				}
				else if(c == Byte.TYPE)
				{
					final byte[] a = (byte[])Array.newInstance(c, len);
					for(int i = 0; i < len; ++i)
							a[i] = readByte();
					o = a;
				}
				else if(c == Short.TYPE)
				{
					final short[] a = (short[])Array.newInstance(c, len);
					for(int i = 0; i < len; ++i)
							a[i] = readShort();
					o = a;
				}
				else if(c == Character.TYPE)
				{
					final char[] a = (char[])Array.newInstance(c, len);
					for(int i = 0; i < len; ++i)
							a[i] = readCharacter();
					o = a;
				}
				else if(c == Integer.TYPE)
				{
					final int[] a = (int[])Array.newInstance(c, len);
					for(int i = 0; i < len; ++i)
							a[i] = readInteger();
					o = a;
				}
				else if(c == Long.TYPE)
				{
					final long[] a = (long[])Array.newInstance(c, len);
					for(int i = 0; i < len; ++i)
							a[i] = readLong();
					o = a;
				}
				else if(c == Float.TYPE)
				{
					final float[] a = (float[])Array.newInstance(c, len);
					for(int i = 0; i < len; ++i)
							a[i] = readFloat();
					o = a;
				}
				else
				{
					final double[] a = (double[])Array.newInstance(c, len);
					for(int i = 0; i < len; ++i)
							a[i] = readDouble();
					o = a;
				}
			}
			else if(obj_class == Boolean.class)
				o = Boolean.valueOf(readBoolean());
			else if(obj_class == Byte.class)
				o = Byte.valueOf(readByte());
			else if(obj_class == Short.class)
				o = Short.valueOf(readShort());
			else if(obj_class == Character.class)
				o = Character.valueOf(readCharacter());
			else if(obj_class == Integer.class)
				o = Integer.valueOf(readInteger());
			else if(obj_class == Long.class)
				o = Long.valueOf(readLong());
			else if(obj_class == Float.class)
				o = Float.valueOf(readFloat());
			else if(obj_class == Double.class)
				o = Double.valueOf(readDouble());
			else if(obj_class == String.class)
				o = readString();
			else if(obj_class.getSuperclass() == Enum.class)
			{
				try { o = obj_class.getEnumConstants()[readInteger()]; }
				catch(final IndexOutOfBoundsException ex) { throw new DeserializingException.CorruptStream(obj_class.getName()); }
			}
			else if(Collection.class.isAssignableFrom(obj_class))
			{
				o = newInstance(getConstructor(obj_class));
				final int s = readInteger();
				if(deep_classes.length == 0)
					for(int i = 0; i < s; ++i)
						((Collection)o).add(readObject(null));
				else
				{
					final Class<?>[] deeper_classes = new Class<?>[deep_classes.length - 1];
					System.arraycopy(deep_classes, 1, deeper_classes, 0, deeper_classes.length);
					for(int i = 0; i < s; ++i)
						((Collection)o).add(readObject(deep_classes[0], deeper_classes));
				}
			}
			else if(Map.class.isAssignableFrom(obj_class))
			{
				o = newInstance(getConstructor(obj_class));
				final int s = readInteger();
				if(deep_classes.length == 0)
					for(int i = 0; i < s; ++i)
						((Map)o).put(readObject(null), readObject(null));
				else if(deep_classes.length == 1)
					for(int i = 0; i < s; ++i)
						((Map)o).put(readObject(deep_classes[0]), readObject(null));
				else
				{
					final Class<?>[] deeper_classes = new Class<?>[deep_classes.length - 2];
					System.arraycopy(deep_classes, 2, deeper_classes, 0, deeper_classes.length);
					for(int i = 0; i < s; ++i)
						((Map)o).put(readObject(deep_classes[0], deeper_classes), readObject(deep_classes[1], deeper_classes));
				}
			}
			else if(Serializable.class.isAssignableFrom(obj_class))
			{
				final Method method = getStaticMethod(obj_class);

				if(method != null)
					o = getObject(method);
				else
					o = newInstance(getConstructor(obj_class, DeserializingStream.class), this);
			}
			else
				throw new DeserializingException.ClassNotDeserializable(obj_class.getName());

			object_map.put(object_id, o);
			return (T)o;
		case REFERENCE:
			try { return (T)object_map.get(readInteger()); }
			catch(final ClassCastException ex) { throw new DeserializingException.CorruptStream(obj_class == null ? null : obj_class.getName()); }
		case NULL:
			return null;
		default:
			throw new DeserializingException.CorruptStream(obj_class == null ? null : obj_class.getName());
		}
	}

	private static final byte DIRECT = 0;
	private static final byte REFERENCE = 1;
	private static final byte NULL = 2;
	private static final Charset UTF8 = Charset.forName("UTF-8");

	private final Map<Integer, Class<?>> class_map;
	private final Map<Integer, Object> object_map;
	private final Map<Class<?>, Constructor<?>> constructor_cache;
	private final Map<Class<?>, Method> static_cache;
	private byte[] buffer;

	private final void read(final int len) throws IOException, DeserializingException
	{
		while(buffer.length < len)
			buffer = new byte[Integer.highestOneBit(len) << 1];

		for(int r = 0, o = 0; (r = in.read(buffer, o, len - o)) > 0; o += r)
			if(r == -1)
				throw new DeserializingException.CorruptStream(null);
	}

	private final Class<?> getClass(final String name) throws DeserializingException
	{
		try { return ClassLoader.getClass(name); }
		catch(final ClassNotFoundException ex) { throw new DeserializingException.ClassNotFound(name); }
	}

	private final Constructor<?> getConstructor(final Class<?> c, final Class<?>... parameters) throws DeserializingException
	{
		if(!constructor_cache.containsKey(c))
		{
			Constructor<?> constructor = null;
			try { constructor = c.getDeclaredConstructor(parameters); }
			catch(final NoSuchMethodException ex) { throw new DeserializingException.ClassNotDeserializable(c.getName()); }
			if(constructor != null)
				constructor.setAccessible(true);
			constructor_cache.put(c, constructor);

			return constructor;
		}
		else
			return constructor_cache.get(c);
	}

	private final Method getStaticMethod(final Class<?> c) throws DeserializingException
	{
		if(!static_cache.containsKey(c))
		{
			Method method = null;
			try { method = c.getDeclaredMethod("getDeserializedObject", DeserializingStream.class); }
			catch(final NoSuchMethodException ex) { }
			if(method != null)
				method.setAccessible(true);
			static_cache.put(c, method);

			return method;
		}
		else
			return static_cache.get(c);
	}

	private final Object newInstance(final Constructor<?> constructor, final Object... parameters) throws IOException, DeserializingException, InstantiationException
	{
		try { return constructor.newInstance(parameters); }
		catch(final IllegalAccessException ex) { return null; } // will not happen
		catch(final java.lang.InstantiationException ex) { throw new DeserializingException.ClassNotDeserializable(constructor.getDeclaringClass().getName()); }
		catch(final InvocationTargetException ex)
		{
			Throwable t = ex.getCause();
			if(t instanceof IOException)
				throw (IOException)t;
			else if(t instanceof DeserializingException)
				throw (DeserializingException)t;
			else if(t instanceof InstantiationException)
				throw (InstantiationException)t;
			else
				throw new InstantiationException(constructor.getDeclaringClass().getName(), t);
		}
	}

	private final Object getObject(Method method) throws IOException, DeserializingException, InstantiationException
	{
		try { return method.invoke(null, this); }
		catch(final IllegalAccessException ex) { return null; } // will not happen
		catch(final InvocationTargetException ex)
		{
			Throwable t = ex.getCause();
			if(t instanceof IOException)
				throw (IOException)t;
			else if(t instanceof DeserializingException)
				throw (DeserializingException)t;
			else if(t instanceof InstantiationException)
				throw (InstantiationException)t;
			else
				throw new InstantiationException(method.getDeclaringClass().getName(), t);
		}
	}
}
