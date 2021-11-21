
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

package core.misc.setable;

import core.signal.Bit;

/**
 * The {@code Setable} interface extends a {@link core.build.Component
 * Component} with the capability to let the user manipulate an internal state.
 *
 * @see core.build.Component Component
 * @see Bit
 *
 * @author torben
 */
public interface Setable
{
	/**
	 * @return The number of setable {@link Bit}s.
	 */
	public int getSetableCount();

	/**
	 * Returns the current {@link Bit} of the i'th setable {@link Bit}.
	 * 
	 * @param i The number of the desired setable {@link Bit}.
	 * 
	 * @return The current {@link Bit}.
	 */
	public Bit getSetableBit(int i);

	/**
	 * Sets the i'th setable {@link Bit} to v.
	 *
	 * @param i The number of the desired setable {@link Bit}.
	 * @param v The new {@link Bit}.
	 */
	public void setSetableBit(int i, Bit v);
}
