
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

package core.misc;

import core.signal.*;
import static core.signal.Bit.*;

import java.math.BigInteger;


/**
 * The {@code BitConverter} utility class provides converting from and to
 * {@link Bit}s.
 *
 * @author torben
 */
public final class BitConverter
{
	/**
	 * The {@code Order} enum represents the bit order.
	 * <ul>
	 * <li>The value {@link #LITTLE_ENDIAN LITTLE_ENDIAN} stands for
	 * least-significant bit first.</li>
	 * <li>The value {@link #BIG_ENDIAN BIG_ENDIAN} stands for most-significant
	 * bit first.</li>
	 * </ul>
	 */
	public static enum Order
	{
		/**
		 * Stands for least-significant bit first.
		 */
		LITTLE_ENDIAN,
		/**
		 * Stands for most-significant bit first.
		 */
		BIG_ENDIAN;
	}

	/**
	 * Invokes {@link #bitsToInteger(core.misc.BitConverter.Order, boolean,
	 * core.signal.Bit[]) bitsToInteger(Order.LITTLE_ENDIAN, false,
	 * signal.getBits())} and returns the value as long.
	 *
	 * @param signal The {@link Signal} to get the {@link Bit}s for converting
	 *               from
	 *
	 * @return The numeric value of the {@link Signal}'s {@link Bit}s
	 */
	public static final long signalToLong(final Signal signal)
	{
		assert signal != null;

		return bitsToInteger(Order.LITTLE_ENDIAN, false, signal.getBits()).longValue();
	}

	/**
	 * Invokes {@link #bitsToInteger(core.misc.BitConverter.Order, boolean,
	 * core.signal.Bit[]) bitsToInteger(Order.LITTLE_ENDIAN, signed,
	 * signal.getBits())} and returns the value as long.
	 *
	 * @param signed true if the {@link Bit}s should be interpreted as
	 *               twos-complement, otherwise false
	 * @param signal The {@link Signal} to get the {@link Bit}s for converting
	 *               from
	 *
	 * @return The numeric value of the {@link Signal}'s {@link Bit}s
	 */
	public static final long signalToLong(final boolean signed, final Signal signal)
	{
		assert signal != null;

		return bitsToInteger(Order.LITTLE_ENDIAN, signed, signal.getBits()).longValue();
	}

	/**
	 * Invokes {@link #bitsToInteger(core.misc.BitConverter.Order, boolean,
	 * core.signal.Bit[]) bitsToInteger(order, signed, signal.getBits())} and
	 * returns the value as long.
	 *
	 * @param order  The {@link Order} of bits to use for interpreting
	 * @param signed true if the {@link Bit}s should be interpreted as
	 *               twos-complement, otherwise false
	 * @param signal The {@link Signal} to get the {@link Bit}s for converting
	 *               from
	 *
	 * @return The numeric value of the {@link Signal}'s {@link Bit}s
	 */
	public static final long signalToLong(final Order order, final boolean signed, final Signal signal)
	{
		assert signal != null;

		return bitsToInteger(order, signed, signal.getBits()).longValue();
	}

	/**
	 * Invokes {@link #bitsToInteger(core.misc.BitConverter.Order, boolean,
	 * core.signal.Bit[]) bitsToInteger(Order.LITTLE_ENDIAN, false,
	 * signal.getBits())}.
	 *
	 * @param signal The {@link Signal} to get the {@link Bit}s for converting
	 *               from
	 *
	 * @return The numeric value of the {@link Signal}'s {@link Bit}s
	 */
	public static final BigInteger signalToInteger(final Signal signal)
	{
		assert signal != null;

		return bitsToInteger(Order.LITTLE_ENDIAN, false, signal.getBits());
	}

	/**
	 * Invokes {@link #bitsToInteger(core.misc.BitConverter.Order, boolean,
	 * core.signal.Bit[]) bitsToInteger(Order.LITTLE_ENDIAN, signed,
	 * signal.getBits())}.
	 *
	 * @param signed true if the {@link Bit}s should be interpreted as
	 *               twos-complement, otherwise false
	 * @param signal The {@link Signal} to get the {@link Bit}s for converting
	 *               from
	 *
	 * @return The numeric value of the {@link Signal}'s {@link Bit}s
	 */
	public static final BigInteger signalToInteger(final boolean signed, final Signal signal)
	{
		assert signal != null;

		return bitsToInteger(Order.LITTLE_ENDIAN, signed, signal.getBits());
	}

	/**
	 * Invokes {@link #bitsToInteger(core.misc.BitConverter.Order, boolean,
	 * core.signal.Bit[]) bitsToInteger(order, signed, signal.getBits())}.
	 *
	 * @param order  The {@link Order} of bits to use for interpreting
	 * @param signed true if the {@link Bit}s should be interpreted as
	 *               twos-complement, otherwise false
	 * @param signal The {@link Signal} to get the {@link Bit}s for converting
	 *               from
	 *
	 * @return The numeric value of the {@link Signal}'s {@link Bit}s
	 */
	public static final BigInteger signalToInteger(final Order order, final boolean signed, final Signal signal)
	{
		assert signal != null;

		return bitsToInteger(order, signed, signal.getBits());
	}

	/**
	 * Invokes {@link #bitsToInteger(core.misc.BitConverter.Order, boolean,
	 * core.signal.Bit[]) bitsToInteger(Order.LITTLE_ENDIAN, false, bits)} and
	 * returns the value as long.
	 *
	 * @param bits The {@link Bit}s to convert
	 *
	 * @return The numeric value of the {@link Bit}s
	 */
	public static final long bitsToLong(final Bit... bits)
	{
		return bitsToInteger(Order.LITTLE_ENDIAN, false, bits).longValue();
	}

	/**
	 * Invokes {@link #bitsToInteger(core.misc.BitConverter.Order, boolean,
	 * core.signal.Bit[]) bitsToInteger(Order.LITTLE_ENDIAN, signed, bits)} and
	 * returns the value as long.
	 *
	 * @param signed true if the {@link Bit}s should be interpreted as
	 *               twos-complement, otherwise false
	 * @param bits   The {@link Bit}s to convert
	 *
	 * @return The numeric value of the {@link Bit}s
	 */
	public static final long bitsToLong(final boolean signed, final Bit... bits)
	{
		return bitsToInteger(Order.LITTLE_ENDIAN, signed, bits).longValue();
	}

	/**
	 * Invokes {@link #bitsToInteger(core.misc.BitConverter.Order, boolean,
	 * core.signal.Bit[]) bitsToInteger(order, signed, bits)} and returns the
	 * value as long.
	 *
	 * @param order  The {@link Order} of bits to use for interpreting
	 * @param signed true if the {@link Bit}s should be interpreted as
	 *               twos-complement, otherwise false
	 * @param bits   The {@link Bit}s to convert
	 *
	 * @return The numeric value of the {@link Bit}s
	 */
	public static final long bitsToLong(final Order order, final boolean signed, final Bit... bits)
	{
		return bitsToInteger(order, signed, bits).longValue();
	}

	/**
	 * Invokes {@link #bitsToInteger(core.misc.BitConverter.Order, boolean,
	 * core.signal.Bit[]) bitsToInteger(Order.LITTLE_ENDIAN, false, bits)}.
	 *
	 * @param bits The {@link Bit}s to convert
	 *
	 * @return The numeric value of the {@link Bit}s
	 */
	public static final BigInteger bitsToInteger(final Bit... bits)
	{
		return bitsToInteger(Order.LITTLE_ENDIAN, false, bits);
	}

	/**
	 * Invokes {@link #bitsToInteger(core.misc.BitConverter.Order, boolean,
	 * core.signal.Bit[]) bitsToInteger(Order.LITTLE_ENDIAN, signed, bits)}.
	 *
	 * @param signed true if the {@link Bit}s should be interpreted as
	 *               twos-complement, otherwise false
	 * @param bits   The {@link Bit}s to convert
	 *
	 * @return The numeric value of the {@link Bit}s
	 */
	public static final BigInteger bitsToInteger(final boolean signed, final Bit... bits)
	{
		return bitsToInteger(Order.LITTLE_ENDIAN, signed, bits);
	}

	/**
	 * Converts a sequence of {@link Bit}s into a numeric representation.
	 *
	 * @param order  The {@link Order} of bits to use for interpreting
	 * @param signed true if the {@link Bit}s should be interpreted as
	 *               twos-complement, otherwise false
	 * @param bits   The {@link Bit}s to convert
	 *
	 * @return The numeric value of the {@link Bit}s
	 */
	public static final BigInteger bitsToInteger(final Order order, final boolean signed, final Bit... bits)
	{
		assert order != null;
		assert bits != null;
		assert checkBits(bits);

		BigInteger integer = BigInteger.ZERO;

		for(int i = 0; i < bits.length; ++i)
			if(bits[i] == H)
				integer = integer.setBit(order == Order.LITTLE_ENDIAN ? i : bits.length - i - 1);

		if(signed && (order == Order.LITTLE_ENDIAN ? bits[bits.length - 1] : bits[0]) == H)
			return integer.subtract(BigInteger.ONE.shiftLeft(bits.length));

		return integer;
	}

	/**
	 * Invokes {@link #integerToBits(core.misc.BitConverter.Order, int,
	 * java.math.BigInteger) integerToBits(Order.LITTLE_ENDIAN, size,
	 * BigInteger.valueOf(value))}.
	 *
	 * @param size  The length of the returned {@link Bit} array
	 * @param value The numeric value to convert
	 *
	 * @return A {@link Bit} array representing the numeric value
	 */
	public static final Bit[] longToBits(final int size, final long value)
	{
		return integerToBits(Order.LITTLE_ENDIAN, size, BigInteger.valueOf(value));
	}

	/**
	 * Invokes {@link #integerToBits(core.misc.BitConverter.Order, int,
	 * java.math.BigInteger) integerToBits(order, size,
	 * BigInteger.valueOf(value))}.
	 *
	 * @param order The {@link Order} of bits to use for representation
	 * @param size  The length of the returned {@link Bit} array
	 * @param value The numeric value to convert
	 *
	 * @return A {@link Bit} array representing the numeric value
	 */
	public static final Bit[] longToBits(final Order order, final int size, final long value)
	{
		return integerToBits(order, size, BigInteger.valueOf(value));
	}

	/**
	 * Invokes {@link #integerToBits(core.misc.BitConverter.Order, int,
	 * java.math.BigInteger) integerToBits(Order.LITTLE_ENDIAN, size,
	 * integer)}.
	 *
	 * @param size    The length of the returned {@link Bit} array
	 * @param integer The numeric value to convert
	 *
	 * @return A {@link Bit} array representing the numeric value
	 */
	public static final Bit[] integerToBits(final int size, final BigInteger integer)
	{
		return integerToBits(Order.LITTLE_ENDIAN, size, integer);
	}

	/**
	 * Converts a numeric number into a sequence of {@link Bit}s.
	 *
	 * @param order   The {@link Order} of bits to use for representation
	 * @param size    The length of the returned {@link Bit} array
	 * @param integer The numeric value to convert
	 *
	 * @return A {@link Bit} array representing the numeric value
	 */
	public static final Bit[] integerToBits(final Order order, final int size, final BigInteger integer)
	{
		assert order != null;
		assert size > 0;
		assert integer != null;

		final Bit[] bits = new Bit[size];

		for(int i = 0; i < size; ++i)
			bits[i] = integer.testBit(order == Order.LITTLE_ENDIAN ? i : size - i - 1) ? H : L;

		return bits;
	}

	/**
	 * Invokes {@link #bitsToString(core.misc.BitConverter.Order,
	 * core.signal.Bit[]) bitsToString(Order.LITTLE_ENDIAN, signal.getBits())}.
	 *
	 * @param signal The {@link Signal} to get the {@link Bit}s for converting
	 *               from
	 *
	 * @return A string representation of the {@link Signal}
	 */
	public static final String signalToString(final Signal signal)
	{
		assert signal != null;

		return bitsToString(Order.LITTLE_ENDIAN, signal.getBits());
	}

	/**
	 * Invokes {@link #bitsToString(core.misc.BitConverter.Order,
	 * core.signal.Bit[]) bitsToString(oder, signal.getBits())}.
	 *
	 * @param order  The {@link Order} of bits to use for representation
	 * @param signal The {@link Signal} to get the {@link Bit}s for converting
	 *               from
	 *
	 * @return A string representation of the {@link Signal}
	 */
	public static final String signalToString(final Order order, final Signal signal)
	{
		assert signal != null;

		return bitsToString(order, signal.getBits());
	}

	/**
	 * Invokes {@link #bitsToString(core.misc.BitConverter.Order,
	 * core.signal.Bit[]) bitsToString(Order.LITTLE_ENDIAN, bits)}.
	 *
	 * @param bits The {@link Bit}s to convert
	 *
	 * @return A string representation of the {@link Bit}s
	 */
	public static final String bitsToString(final Bit... bits)
	{
		return bitsToString(Order.LITTLE_ENDIAN, bits);
	}

	/**
	 * Converts a sequence of {@link Bit}s into a string representation.
	 *
	 * @param order The {@link Order} of bits to use for representation
	 * @param bits  The {@link Bit}s to convert
	 *
	 * @return A string representation of the {@link Bit}s
	 */
	public static final String bitsToString(final Order order, final Bit... bits)
	{
		assert order != null;
		assert bits != null;
		assert checkBits(bits);

		final StringBuilder sb = new StringBuilder();

		if(order == Order.LITTLE_ENDIAN)
			for(int i = 0; i < bits.length; ++i)
				sb.append(bits[i]);
		else
			for(int i = bits.length - 1; i >= 0; --i)
				sb.append(bits[i]);

		return sb.toString();
	}

	/**
	 * Invokes {@link #stringToBits(core.misc.BitConverter.Order,
	 * java.lang.String) stringToBits(Order.LITTLE_ENDIAN, str)}.
	 *
	 * @param str The string to convert
	 *
	 * @return A {@link Bit} array representing the string
	 */
	public static final Bit[] stringToBits(final String str)
	{
		return stringToBits(Order.LITTLE_ENDIAN, str);
	}

	/**
	 * Converts a string representation into a sequence of {@link Bit}s.
	 *
	 * @param order The {@link Order} of bits to use for interpretation
	 * @param str   The string to convert
	 *
	 * @return A {@link Bit} array representing the string
	 */
	public static final Bit[] stringToBits(final Order order, final String str)
	{
		assert order != null;
		assert str != null;
		assert checkStr(str);

		final Bit[] bits = new Bit[str.length()];

		if(order == Order.LITTLE_ENDIAN)
			for(int i = 0; i < bits.length; ++i)
				bits[i] = Bit.valueOf(String.valueOf(str.charAt(i)));
		else
			for(int i = bits.length - 1; i >= 0; --i)
				bits[i] = Bit.valueOf(String.valueOf(str.charAt(i)));

		return bits;
	}

	private static final boolean checkBits(final Bit[] bits)
	{
		for(final Bit b : bits)
			if(b == null)
				return false;
		return true;
	}

	private static final boolean checkStr(final String str)
	{
		for(int i = 0; i < str.length(); ++i)
		{
			final char c = str.charAt(i);
			if(c != 'L' && c != 'H' && c != 'Z')
				return false;
		}

		return true;
	}

	private BitConverter() { }
}
