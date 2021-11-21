
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

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

/**
 * The {@code SerializingStream} class provides serializing of objects to an
 * {@link OutputStream}. It provides support for all Java native types, native
 * type wrapper classes, arrays, Strings, Enums, Collections, Maps and all
 * classes which implements the {@link Serializable} interface. It generates a
 * smaller file than Java Serialization by allow the user to prevent saving the
 * class information if the type is known at load time and by using class name
 * referencing. It is also faster than Java Serialization due to the lack of
 * reflection calls.
 *
 * @see Serializable
 * @see DeserializingStream
 *
 * @author torben
 */
public final class SerializingStream extends FilterOutputStream
{
	/**
	 * Creates a new {@code SerializingStream} which writes to an {@link
	 * OutputStream}.
	 *
	 * @param out The {@link OutputStream} to write to
	 */
	public SerializingStream(final OutputStream out)
	{
		super(out);

		assert out != null;

		class_map = new HashMap<Class<?>, Integer>();
		object_map = new IdentityHashMap<Object, Integer>();
	}

	/**
	 * Writes a raw boolean value.
	 *
	 * @param b The boolean value
	 *
	 * @throws IOException if the underlying stream throws an
	 *                     {@code IOException}
	 */
	public final void writeBoolean(final boolean b) throws IOException
	{
		out.write(b ? 1 : 0);
	}

	/**
	 * Writes a raw byte value.
	 *
	 * @param b The byte value
	 *
	 * @throws IOException if the underlying stream throws an
	 *                     {@code IOException}
	 */
	public final void writeByte(final byte b) throws IOException
	{
		out.write(b);
	}

	/**
	 * Writes a raw short value.
	 *
	 * @param s The short value
	 *
	 * @throws IOException if the underlying stream throws an
	 *                     {@code IOException}
	 */
	public final void writeShort(final short s) throws IOException
	{
		out.write(s);
		out.write(s >> 8);
	}

	/**
	 * Writes a raw character value.
	 *
	 * @param c The character value
	 *
	 * @throws IOException if the underlying stream throws an
	 *                     {@code IOException}
	 */
	public final void writeCharacter(final char c) throws IOException
	{
		out.write(c);
		out.write(c >> 8);
	}

	/**
	 * Writes a raw integer value.
	 *
	 * @param i The integer value
	 *
	 * @throws IOException if the underlying stream throws an
	 *                     {@code IOException}
	 */
	public final void writeInteger(final int i) throws IOException
	{
		out.write(i);
		out.write(i >> 8);
		out.write(i >> 16);
		out.write(i >> 24);
	}

	/**
	 * Writes a raw long value.
	 *
	 * @param l The long value
	 *
	 * @throws IOException if the underlying stream throws an
	 *                     {@code IOException}
	 */
	public final void writeLong(final long l) throws IOException
	{
		out.write((int)l);
		out.write((int)(l >> 8));
		out.write((int)(l >> 16));
		out.write((int)(l >> 24));
		out.write((int)(l >> 32));
		out.write((int)(l >> 40));
		out.write((int)(l >> 48));
		out.write((int)(l >> 56));
	}

	/**
	 * Writes a raw float value.
	 *
	 * @param f The float value
	 *
	 * @throws IOException if the underlying stream throws an
	 *                     {@code IOException}
	 */
	public final void writeFloat(final float f) throws IOException
	{
		writeInteger(Float.floatToRawIntBits(f));
	}

	/**
	 * Writes a raw double value.
	 *
	 * @param d The double value
	 *
	 * @throws IOException if the underlying stream throws an
	 *                     {@code IOException}
	 */
	public final void writeDouble(final double d) throws IOException
	{
		writeLong(Double.doubleToRawLongBits(d));
	}

	/**
	 * Writes a String by writing its content as an UTF-8 encoded byte-array.
	 *
	 * @param s The String to write
	 *
	 * @throws IOException if the underlying stream throws an
	 *                     {@code IOException}
	 */
	public final void writeString(final String s) throws IOException
	{
		assert s != null;

		final byte[] bytes = s.getBytes(UTF8);
		writeInteger(bytes.length);
		out.write(bytes);
	}

	/**
	 * Writes an Object. If the same object was already written to this stream,
	 * a reference is written instead. A class information isn't written if and
	 * only if {@code save_class} is false in the corresponding depth.
	 *
	 * @param obj          The Object to write
	 * @param save_classes true in the corresponding depth if the objects class
	 *                     should be written, false otherwise
	 *
	 * @throws IOException          if the underlying stream throws an
	 *                              {@code IOException}
	 * @throws SerializingException if the object isn't serializable
	 */
	public final void writeObject(final Object obj, final boolean... save_classes) throws IOException, SerializingException
	{
		assert save_classes != null;

		if(obj == null)
			writeByte(NULL);
		else if(object_map.containsKey(obj))
		{
			writeByte(REFERENCE);
			writeInteger(object_map.get(obj));
		}
		else
		{
			final int i = object_map.size();
			object_map.put(obj, i);
			writeByte(DIRECT);
			writeInteger(i);

			if(save_classes.length == 0 || save_classes[0])
			{
				final Class<?> c = obj.getClass();
				if(class_map.containsKey(c))
				{
					writeByte(REFERENCE);
					writeInteger(class_map.get(c));
				}
				else
				{
					final int j = class_map.size();
					class_map.put(c, j);
					writeByte(DIRECT);
					writeInteger(j);
					writeString(c.getName());
				}
			}
			
			if(obj instanceof Object[])
			{
				final Object[] a = (Object[])obj;
				writeInteger(a.length);
				if(save_classes.length == 0)
					for(final Object o : a)
						writeObject(o, true);
				else
				{
					final boolean[] deep_save_class = new boolean[save_classes.length - 1];
					System.arraycopy(save_classes, 1, deep_save_class, 0, deep_save_class.length);
					for(final Object o : a)
						writeObject(o, deep_save_class);
				}
			}
			else if(obj instanceof boolean[])
			{
				final boolean[] a = (boolean[])obj;
				writeInteger(a.length);
				for(final boolean b : a)
					writeBoolean(b);
			}
			else if(obj instanceof byte[])
			{
				final byte[] a = (byte[])obj;
				writeInteger(a.length);
				for(final byte b : a)
					writeByte(b);
			}
			else if(obj instanceof short[])
			{
				final short[] a = (short[])obj;
				writeInteger(a.length);
				for(final short s : a)
					writeShort(s);
			}
			else if(obj instanceof char[])
			{
				final char[] a = (char[])obj;
				writeInteger(a.length);
				for(final char c : a)
					writeCharacter(c);
			}
			else if(obj instanceof int[])
			{
				final int[] a = (int[])obj;
				writeInteger(a.length);
				for(final int in : a)
					writeInteger(in);
			}
			else if(obj instanceof long[])
			{
				final long[] a = (long[])obj;
				writeInteger(a.length);
				for(final long l : a)
					writeLong(l);
			}
			else if(obj instanceof float[])
			{
				final float[] a = (float[])obj;
				writeInteger(a.length);
				for(final float f : a)
					writeFloat(f);
			}
			else if(obj instanceof double[])
			{
				final double[] a = (double[])obj;
				writeInteger(a.length);
				for(final double d : a)
					writeDouble(d);
			}
			else if(obj instanceof Boolean)
				writeBoolean((Boolean)obj);
			else if(obj instanceof Byte)
				writeByte((Byte)obj);
			else if(obj instanceof Short)
				writeShort((Short)obj);
			else if(obj instanceof Character)
				writeCharacter((Character)obj);
			else if(obj instanceof Integer)
				writeInteger((Integer)obj);
			else if(obj instanceof Long)
				writeLong((Long)obj);
			else if(obj instanceof Float)
				writeFloat((Float)obj);
			else if(obj instanceof Double)
				writeDouble((Double)obj);
			else if(obj instanceof String)
				writeString((String)obj);
			else if(obj instanceof Enum)
				writeInteger(((Enum)obj).ordinal());
			else if(obj instanceof Collection)
			{
				final Collection c = (Collection)obj;
				writeInteger(c.size());
				if(save_classes.length == 0)
					for(final Object o : c)
						writeObject(o, true);
				else
				{
					final boolean[] deep_save_class = new boolean[save_classes.length - 1];
					System.arraycopy(save_classes, 1, deep_save_class, 0, deep_save_class.length);
					for(final Object o : c)
						writeObject(o, deep_save_class);
				}
			}
			else if(obj instanceof Map)
			{
				final Map m = (Map)obj;
				writeInteger(m.size());
				if(save_classes.length == 0)
					for(final Iterator j = m.entrySet().iterator(); j.hasNext();)
					{
						final Entry e = (Entry)j.next();
						writeObject(e.getKey(), true);
						writeObject(e.getValue(), true);
					}
				else if(save_classes.length == 1)
					for(final Iterator j = m.entrySet().iterator(); j.hasNext();)
					{
						final Entry e = (Entry)j.next();
						writeObject(e.getKey(), save_classes[1]);
						writeObject(e.getValue(), true);
					}
				else
				{
					final boolean[] deep_save_class = new boolean[save_classes.length - 2];
					System.arraycopy(save_classes, 2, deep_save_class, 0, deep_save_class.length);
					for(final Iterator j = m.entrySet().iterator(); j.hasNext();)
					{
						final Entry e = (Entry)j.next();
						writeObject(e.getKey(), save_classes[1]);
						writeObject(e.getValue(), deep_save_class);
					}
				}
			}
			else if(obj instanceof Serializable)
				((Serializable)obj).serialize(this);
			else
				throw new SerializingException(obj);
		}
	}

	private static final byte DIRECT = 0;
	private static final byte REFERENCE = 1;
	private static final byte NULL = 2;
	private static final Charset UTF8 = Charset.forName("UTF-8");

	private final Map<Class<?>, Integer> class_map;
	private final Map<Object, Integer> object_map;
}
