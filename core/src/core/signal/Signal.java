
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

import core.exception.InstantiationException;
import core.exception.DeserializingException;
import core.exception.SerializingException;
import core.misc.BitConverter;
import core.misc.serial.Serializable;
import core.misc.serial.DeserializingStream;
import core.misc.serial.SerializingStream;
import core.monitor.SignalBitListener;
import core.monitor.SignalListener;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * The {@code Signal} class is an array of {@link SignalBit}s and provides
 * notification to registered {@link SignalListener}s if the value of these
 * {@link SignalBit}s changed. It provides several ways to combine and slice
 * the array to create new {@code Signal}s, the array of {@link SignalBit}s
 * itself is immutable. This class is thread-safe.
 *
 * @see SignalBit
 * @see SignalListener
 *
 * @author torben
 */
public final class Signal implements SignalBitListener, Serializable
{
	/**
	 * Creates a new array of {@link SignalBit}s.
	 *
	 * @param size The number of {@link SignalBit}s in this {@code Signal}
	 */
	public Signal(final int size)
	{
		assert size > 0;

		bits = new SignalBit[size];
		for(int i = 0; i < size; ++i)
			bits[i] = new SignalBit(Bit.Z);

		listeners = new SignalListener[0];
		lock = new AtomicBoolean(false);
	}

	/**
	 * Combines {@code Signal}s to one new {@code Signal}.
	 *
	 * @param signals The {@link Signal}s to combine.
	 */
	public Signal(final Signal... signals)
	{
		assert signals != null;
		assert signals.length > 0;

		int size = 0;
		for(int i = 0; i < signals.length; ++i)
			size += signals[i].bits.length;

		bits = new SignalBit[size];
		for(int i = 0, k = 0; i < signals.length; ++i)
			for(int j = 0; j < signals[i].bits.length; ++j, ++k)
				bits[k] = signals[i].bits[j];

		listeners = new SignalListener[0];
		lock = new AtomicBoolean(false);
	}

	/**
	 * Creates a new array of {@link SignalBit}s.
	 *
	 * @param bits The initial {@link Bit}s of the {@link SignalBit}s.
	 */
	public Signal(final Bit... bits)
	{
		assert bits != null;
		assert bits.length > 0;
		assert checkBits(bits);

		this.bits = new SignalBit[bits.length];
		for(int i = 0; i < bits.length; ++i)
			this.bits[i] = SignalBit.getStaticBit(bits[i]);

		listeners = new SignalListener[0];
		lock = new AtomicBoolean(false);
	}

	/**
	 * Slices a {@code Signal} to a new {@code Signal}.
	 *
	 * @param signal The {@code Signal} to slice.
	 * @param start  The index of the first {@link SignalBit} to include in
	 *               signal
	 *
	 * @param length The size of the new {@code Signal}
	 */
	public Signal(final Signal signal, final int start, final int length)
	{
		assert signal != null;
		assert start >= 0;
		assert length > 0;
		assert start + length <= signal.bits.length;

		bits = new SignalBit[length];
		for(int i = 0; i < length; ++i)
			bits[i] = signal.bits[start + i];

		listeners = new SignalListener[0];
		lock = new AtomicBoolean(false);
	}

	/**
	 * Combines multiple copies of a {@code Signal} to a new {@code Signal}.
	 *
	 * @param signal The {@code Signal} to copy
	 * @param count  The number of copies
	 */
	public Signal(final Signal signal, final int count)
	{
		assert signal != null;
		assert count > 0;

		bits = new SignalBit[signal.bits.length * count];
		for(int i = 0; i < count; ++i)
			for(int j = 0; j < signal.bits.length; ++j)
				bits[i * signal.bits.length + j] = signal.bits[j];

		listeners = new SignalListener[0];
		lock = new AtomicBoolean(false);
	}

	/**
	 * Creates a new {@code Signal} of a number of a single initial
	 * {@code Bit}s.
	 *
	 * @param bit   The initial {@link Bit}
	 * @param count The number of {@link Bit}s
	 */
	public Signal(final Bit bit, final int count)
	{
		assert bit != null;
		assert count > 0;

		bits = new SignalBit[count];
		for(int i = 0; i < count; ++i)
			bits[i] = SignalBit.getStaticBit(bit);

		listeners = new SignalListener[0];
		lock = new AtomicBoolean(false);
	}

	/**
	 * Returns the current {@link Bit} of the i'th {@link SignalBit}.
	 *
	 * @param i The index of the {@link SignalBit}
	 *
	 * @return The current {@link Bit}
	 */
	public final Bit getBit(final int i)
	{
		assert i >= 0;
		assert i < bits.length;

		return bits[i].bit.get();
	}

	/**
	 * @return An array of the current {@link Bit}s of the {@link SignalBit}s
	 */
	public final Bit[] getBits()
	{
		final Bit[] ret = new Bit[bits.length];

		for(int i = 0; i < ret.length; ++i)
			ret[i] = bits[i].bit.get();

		return ret;
	}

	/**
	 * Sets the {@link Bit} of the i'th {@link SignalBit}. This method is
	 * thread-safe.
	 *
	 * @param i   The index of the {@link SignalBit}
	 * @param bit The new {@link Bit}
	 */
	public final void setBit(final int i, final Bit bit)
	{
		assert i >= 0;
		assert i < bits.length;
		assert bit != null;

		bits[i].set(bit);
	}

	/**
	 * Sets the {@link Bit}s of all {@link SignalBit}s. This method is
	 * thread-safe for every single bit set, but concurrent modifications or
	 * reads during two sets are not blocked.
	 *
	 * @param bits The new {@link Bit}s.
	 */
	public final void setBits(final Bit... bits)
	{
		assert bits != null;
		assert bits.length == this.bits.length;
		assert checkBits(bits);

		for(int i = 0; i < bits.length; ++i)
			this.bits[i].set(bits[i]);
	}

	/**
	 * Compares the {@link Bit} of the i'th {@link SignalBit}. This methos is
	 * thread-safe.
	 *
	 * @param i   The index of the {@link SignalBit}
	 * @param bit The {@link Bit} to compare with
	 *
	 * @return true if and only if the two {@link Bit}s are equal, false
	 *         otherwise
	 */
	public final boolean compareBit(final int i, final Bit bit)
	{
		assert i >= 0;
		assert i < bits.length;
		assert bit != null;

		return bits[i].bit.get() == bit;
	}

	/**
	 * Compares the {@link Bit}s of all {@link SignalBit}s. This method is
	 * thread-safe for every single bit check, but concurrent modifications
	 * during two checks are not blocked.
	 *
	 * @param bits The {@link Bit}s to compare with
	 *
	 * @return true if and only if every pair of {@link Bit}s is equal, false
	 *         otherwise
	 */
	public final boolean compareBits(final Bit... bits)
	{
		assert bits != null;
		assert bits.length == this.bits.length;
		assert checkBits(bits);

		for(int i = 0; i < this.bits.length; ++i)
			if(this.bits[i].bit.get() != bits[i])
				return false;
		return true;
	}

	/**
	 * @return The length of the {@link SignalBit} array.
	 */
	public final int size()
	{
		return bits.length;
	}

	/**
	 * Returns a new {@code Signal} with the i'th {@code SignalBit}.
	 *
	 * @param i The index of the {@link SignalBit}
	 * @return The new {@code Signal}
	 */
	public final Signal get(final int i)
	{
		assert i >= 0;
		assert i < bits.length;

		return new Signal(bits[i]);
	}

	/**
	 * Returns a new {@code Signal} by slicing the {@code Signal}.
	 *
	 * @param start  The index of the first {@link SignalBit} to include in the
	 *               new {@code Signal}
	 * @param length The size of the new {@code Signal}
	 *
	 * @return The sliced {@code Signal}
	 */
	public final Signal get(final int start, final int length)
	{
		return new Signal(this, start, length);
	}

	/**
	 * Returns an array holding the {@link SignalListener}s registered on this
	 * {@code Signal}.
	 *
	 * @return An array of {@link SignalListener}s
	 */
	public final SignalListener[] getSignalListeners()
	{
		final SignalListener[] current_listeners = listeners;
		final SignalListener[] copy_listeners = new SignalListener[current_listeners.length];
		System.arraycopy(current_listeners, 0, copy_listeners, 0, current_listeners.length);
		return copy_listeners;
	}

	/**
	 * Adds a {@link SignalListener} to the registered listeners of this
	 * {@code Signal}. The {@link SignalListener} is then notified via the
	 * {@link SignalListener#signalChanged(core.signal.Signal,
	 * core.signal.SignalBit, core.signal.Bit, core.signal.Bit)
	 * signalChanged(Signal, SignalBit, Bit, Bit)} method if the value of one
	 * of the {@code SignalBit}s in the array changes. This method is
	 * thread-safe.
	 *
	 * @param listener The {@link SignalListener} to add
	 */
	public final void addSignalListener(final SignalListener listener)
	{
		assert listener != null;

		while(!lock.compareAndSet(false, true));

		outer:
		{
			final SignalListener[] current_listeners = listeners;
			final SignalListener[] new_listeners = new SignalListener[current_listeners.length + 1];
			for(int i = 0; i < current_listeners.length; ++i)
				if(current_listeners[i] == listener)
					break outer;
				else
					new_listeners[i] = current_listeners[i];
			new_listeners[current_listeners.length] = listener;

			if(current_listeners.length == 0)
				for(final SignalBit b : bits)
					b.addSignalBitListener(this);

			listeners = new_listeners;
		}

		lock.set(false);
	}

	/**
	 * Removes a {@link SignalListener} from the registered listeners of this
	 * {@code Signal}. The {@link SignalListener} is not longer notified via
	 * the {@link SignalListener#signalChanged(core.signal.Signal,
	 * core.signal.SignalBit, core.signal.Bit, core.signal.Bit)
	 * signalChanged(Signal, SignalBit, Bit, Bit)} method if the value of one
	 * of the {@code SignalBit}s in the array changes. This method is
	 * thread-safe.
	 *
	 * @param listener The {@link SignalListener} to remove
	 */
	public final void removeSignalListener(final SignalListener listener)
	{
		while(!lock.compareAndSet(false, true));

		final SignalListener[] current_listeners = listeners;
		final SignalListener[] new_listeners = new SignalListener[current_listeners.length - 1];
		int i, j;
		for(i = 0, j = 0; i < current_listeners.length; ++i)
			if(current_listeners[i] != listener)
				new_listeners[j++] = current_listeners[i];
		if(j != i)
		{
			if(new_listeners.length == 0)
				for(final SignalBit b : bits)
					b.removeSignalBitListener(this);

			listeners = new_listeners;
		}

		lock.set(false);
	}

	/**
	 * Returns the i'th {@link SignalBit} of the {@code Signal}.
	 *
	 * @param i The index of the {@code SignalBit}
	 * 
	 * @return The i'th {@link SignalBit}
	 */
	public final SignalBit getSignalBit(final int i)
	{
		assert i >= 0;
		assert i < bits.length;

		return bits[i];
	}

	@Override public final void bitChanged(final SignalBit changed_bit, final Bit old_value, final Bit new_value)
	{
		final SignalListener[] current_listeners = listeners;
		for(final SignalListener listener : current_listeners)
			listener.signalChanged(this, changed_bit, old_value, new_value);
	}

	@Override public final void serialize(final SerializingStream out) throws IOException, SerializingException
	{
		assert out != null;

		out.writeObject(bits, false, false);
		out.writeObject(listeners, false);
	}

	@Override public final String toString()
	{
		return BitConverter.bitsToString(getBits());
	}

	private static final boolean checkBits(final Bit[] bits)
	{
		for(final Bit b : bits)
			if(b == null)
				return false;
		return true;
	}

	private final SignalBit bits[];

	private volatile SignalListener[] listeners;
	private final AtomicBoolean lock;

	private Signal(final SignalBit bit)
	{
		bits = new SignalBit[1];
		bits[0] = bit;
		listeners = new SignalListener[0];
		lock = new AtomicBoolean(false);
	}

	private Signal(final DeserializingStream in) throws IOException, DeserializingException, InstantiationException
	{
		this.bits = in.readObject(SignalBit[].class, SignalBit.class);
		this.listeners = in.readObject(SignalListener[].class);
		this.lock = new AtomicBoolean(false);
	}
}
