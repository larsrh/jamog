
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

package core.signal;

/**
 * The {@code Bit} enum type represents the different values a bit can have. It
 * provides a wide range of logical bit manipulation to provide consistend
 * logic. This class is thread-safe.
 * <ul>
 * <li>The value {@code L} stands for low or negative voltage and represents a
 * digital 0 or a logical false.</li>
 * <li>The value {@code H} stands for high or positive voltage and represents a
 * digital 1 or a logical true.</li>
 * <li>The value {@code H} stands for high or positive voltage and represents a
 * digital 1 or a logical true.</li>
 * </ul>
 *
 * @author torben
 */
public enum Bit
{
	/**
	 * Stands for low or negative voltage and represents a digital 0 or a
	 * logical false.
	 */
	L,
	/**
	 * Stands for high or positive voltage and represents a digital 1 or a
	 * logical true.
	 */
	H,
	/**
	 * Stands for zero voltage and represents a not disturbing value, i.e. for
	 * a bus.
	 */
	Z;

	/**
	 * Returns other if and only if other is not {@link #Z Z}, else returns
	 * this. This method is thread-safe.
	 *
	 * @param other The other {@code Bit}
	 *
	 * @return The new {@code Bit}
	 */
	public final Bit is(final Bit other)
	{
		assert other != null;

		return other != Z ? other : this;
	}

	/**
	 * Returns {@link #H H} if and only if both this and other are
	 * {@link #H H}, {@link #L L} otherwise. This method is thread-safe.
	 *
	 * @param other The other {@code Bit}
	 *
	 * @return The new {@code Bit}
	 */
	public final Bit and(final Bit other)
	{
		assert other != null;

		return this == H && other == H ? H : L;
	}

	/**
	 * Returns {@link #H H} if and only if this, other or this and other are
	 * {@link #H H}, {@link #L L} otherwise. This method is thread-safe.
	 *
	 * @param other The other {@code Bit}
	 *
	 * @return The new {@code Bit}
	 */
	public final Bit or(final Bit other)
	{
		assert other != null;

		return this == H || other == H ? H : L;
	}

	/**
	 * Returns {@link #H H} if and only if this or other are {@link #H H}, but
	 * not both, or {@link #L L} otherwise. This method is thread-safe.
	 *
	 * @param other The other {@code Bit}
	 * 
	 * @return The new {@code Bit}
	 */
	public final Bit xor(final Bit other)
	{
		assert other != null;

		return this == H && other != H || this != H && other == H ? H : L;
	}

	/**
	 * Returns {@link #H H} if and only if this is {@link #L L}, {@link #L L}
	 * if and only if this is {@link #H H}, and {@link #Z Z} otherwise. This
	 * method is thread-safe.
	 *
	 * @return The new {@code Bit}
	 */
	public final Bit not()
	{
		return this != Z ? this == H ? L : H : Z;
	}
}
