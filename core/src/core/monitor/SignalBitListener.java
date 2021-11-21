
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

package core.monitor;

import core.misc.serial.Serializable;
import core.signal.Bit;
import core.signal.SignalBit;

/**
 * The {@code SignalBitListener} interface is used to get notified when a
 * {@link SignalBit} changes its {@link Bit}. If an instance of this interface
 * is registered to a {@link SignalBit} as listener via the {@link
 * SignalBit#addSignalBitListener(core.monitor.SignalBitListener)
 * addSignalBitListener(SignalBitListener)} method and the {@link Bit} of the
 * {@link SignalBit} changes, the {@link #bitChanged(SignalBit, Bit, Bit)}
 * method is invoked.
 *
 * @see Bit
 * @see SignalBit
 *
 * @author torben
 */
public interface SignalBitListener extends Serializable
{
	/**
	 * Is invoked from {@link SignalBit}s to which the {@code
	 * SignalBitListener} listens if the {@link Bit} of a {@link SignalBit}
	 * changes.
	 *
	 * @param changed_bit The {@link SignalBit} which {@link Bit} has changed
	 * @param old_value   The {@link Bit} before the change happens
	 * @param new_value   The {@link Bit} after the change happens
	 */
	public void bitChanged(SignalBit changed_bit, Bit old_value, Bit new_value);
}
