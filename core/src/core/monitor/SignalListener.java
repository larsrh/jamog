
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
import core.signal.Signal;
import core.signal.SignalBit;

/**
 * The {@code SignalListener} interface is used to get notified when a {@link
 * SignalBit} in the {@link Signal}'s array changes its {@link Bit}. If an
 * instance of this interface is registered to a {@link Signal} as listener via
 * the {@link Signal#addSignalListener(core.monitor.SignalListener)
 * addSignalListener(SignalListener)} method and the {@link Bit} of a {@link
 * SignalBit} in the {@link Signal}'s array changes, the {@link
 * #signalChanged(core.signal.Signal, core.signal.SignalBit, core.signal.Bit,
 * core.signal.Bit) signalChanged(Signal, SignalBit, Bit, Bit)} method is
 * invoked.
 *
 * @see Bit
 * @see Signal
 * @see SignalBit
 *
 * @author torben
 */
public interface SignalListener extends Serializable
{
	/**
	 * Is invoked from {@link Signal}s to which the {@code SignalListener}
	 * listens if the {@link Bit} of a {@link SignalBit} in the {@link
	 * Signal}'s array changes.
	 *
	 * @param changed_signal The {@link Signal} which {@link SignalBit} has
	 *                       changed
	 * @param changed_bit    The {@link SignalBit} which {@link Bit} has
	 *                       changed
	 * @param old_value      The {@link Bit} before the change happens
	 * @param new_value      The {@link Bit} after the change happens
	 */
	public void signalChanged(Signal changed_signal, SignalBit changed_bit, Bit old_value, Bit new_value);
}
