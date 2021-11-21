
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

import core.exception.DeserializingException;
import core.exception.InstantiationException;
import core.exception.SerializingException;
import core.misc.serial.Serializable;
import core.misc.serial.DeserializingStream;
import core.misc.serial.SerializingStream;
import core.monitor.SignalBitListener;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * The {@code SignalBit} class is an extension for {@link Bit} which provides
 * notification to registered {@link SignalBitListener}s if its value changes.
 * The {@link Bit} is represented as an internal state, so changing its value
 * doesn't break references to the {@code SignalBit}. This class is
 * thread-safe.
 *
 * @see Bit
 * @see SignalBitListener
 *
 * @author torben
 */
public final class SignalBit implements Serializable
{
	/**
	 * @return The current internal {@link Bit}
	 */
	public final Bit get()
	{
		return bit.get();
	}

	/**
	 * Sets the internal {@link Bit} to a new {@link Bit}. If the new value is
	 * different from the old, all registered {@link SignalBitListener}s are
	 * notified. This method is thread-safe.
	 *
	 * @param v The new {@link Bit}
	 */
	public final void set(final Bit v)
	{
		assert v != null;

		Bit old;

		while(!bit.compareAndSet(old = bit.get(), v));

		if(old != v)
		{
			final SignalBitListener[] current_listeners = listeners;
			for(final SignalBitListener listener : current_listeners)
				listener.bitChanged(this, old, v);
		}
	}

	/**
	 * Sets the internal {@link Bit} to {@link Bit#is(core.signal.Bit)
	 * internalBit.is(other)}. If the new value is different from the old, all
	 * registered {@link SignalBitListener}s are notified. This method is
	 * thread-safe.
	 *
	 * @param other The other {@link Bit}
	 */
	public final void is(final Bit other)
	{
		assert other != null;

		Bit old, actual;

		do
			actual = (old = bit.get()).is(other);
		while(!bit.compareAndSet(old, actual));

		if(old != actual)
		{
			final SignalBitListener[] current_listeners = listeners;
			for(final SignalBitListener listener : current_listeners)
				listener.bitChanged(this, old, actual);
		}
	}

	/**
	 * Sets the internal {@link Bit} to {@link Bit#and(core.signal.Bit)
	 * internalBit.and(other)}. If the new value is different from the old, all
	 * registered {@link SignalBitListener}s are notified. This method is
	 * thread-safe.
	 *
	 * @param other The other {@link Bit}
	 */
	public final void and(final Bit other)
	{
		assert other != null;

		Bit old, actual;

		do
			actual = (old = bit.get()).and(other);
		while(!bit.compareAndSet(old, actual));

		if(old != actual)
		{
			final SignalBitListener[] current_listeners = listeners;
			for(final SignalBitListener listener : current_listeners)
				listener.bitChanged(this, old, actual);
		}
	}

	/**
	 * Sets the internal {@link Bit} to {@link Bit#or(core.signal.Bit)
	 * internalBit.or(other)}. If the new value is different from the old, all
	 * registered {@link SignalBitListener}s are notified. This method is
	 * thread-safe.
	 *
	 * @param other The other {@link Bit}
	 */
	public final void or(final Bit other)
	{
		assert other != null;

		Bit old, actual;

		do
			actual = (old = bit.get()).or(other);
		while(!bit.compareAndSet(old, actual));

		if(old != actual)
		{
			final SignalBitListener[] current_listeners = listeners;
			for(final SignalBitListener listener : current_listeners)
				listener.bitChanged(this, old, actual);
		}
	}

	/**
	 * Sets the internal {@link Bit} to {@link Bit#xor(core.signal.Bit)
	 * internalBit.xor(other)}. If the new value is different from the old, all
	 * registered {@link SignalBitListener}s are notified. This method is
	 * thread-safe.
	 *
	 * @param other The other {@link Bit}
	 */
	public final void xor(final Bit other)
	{
		assert other != null;

		Bit old, actual;

		do
			actual = (old = bit.get()).xor(other);
		while(!bit.compareAndSet(old, actual));

		if(old != actual)
		{
			final SignalBitListener[] current_listeners = listeners;
			for(final SignalBitListener listener : current_listeners)
				listener.bitChanged(this, old, actual);
		}
	}

	/**
	 * Sets the internal {@link Bit} to {@link Bit#not() internalBit.not()}. If
	 * the new value is different from the old, all registered {@link
	 * SignalBitListener}s are notified. This method is thread-safe.
	 */
	public final void not()
	{
		Bit old, actual;

		do
			actual = (old = bit.get()).not();
		while(!bit.compareAndSet(old, actual));

		if(old != actual)
		{
			final SignalBitListener[] current_listeners = listeners;
			for(final SignalBitListener listener : current_listeners)
				listener.bitChanged(this, old, actual);
		}
	}

	/**
	 * Adds a {@link SignalBitListener} to the registered listeners of this
	 * {@code SignalBit}.  The {@link SignalBitListener} is then notified via
	 * the {@link SignalBitListener#bitChanged(core.signal.SignalBit,
	 * core.signal.Bit, core.signal.Bit) bitChanged(SignalBit, Bit, Bit)}
	 * method if the value of this {@code SignalBit} changes. This method is
	 * thread-safe.
	 *
	 * @param listener The {@link SignalBitListener} to add
	 */
	public final void addSignalBitListener(final SignalBitListener listener)
	{
		assert listener != null;

		while(!lock.compareAndSet(false, true));

		outer:
		{
			final SignalBitListener[] current_listeners = listeners;
			final SignalBitListener[] new_listeners = new SignalBitListener[current_listeners.length + 1];
			for(int i = 0; i < current_listeners.length; ++i)
				if(current_listeners[i] == listener)
					break outer;
				else
					new_listeners[i] = current_listeners[i];
			new_listeners[current_listeners.length] = listener;
			listeners = new_listeners;
		}

		lock.set(false);
	}

	/**
	 * Removes a {@link SignalBitListener} from the registered listeners of
	 * this {@code SignalBit}. The {@link SignalBitListener} is not longer
	 * notified via the {@link
	 * SignalBitListener#bitChanged(core.signal.SignalBit, core.signal.Bit,
	 * core.signal.Bit) bitChanged(SignalBit, Bit, Bit)} method if the value of
	 * this {@code SignalBit} changes. This method is thread-safe.
	 *
	 * @param listener The {@link SignalBitListener} to remove
	 */
	public final void removeSignalBitListener(final SignalBitListener listener)
	{
		while(!lock.compareAndSet(false, true));

		final SignalBitListener[] current_listeners = listeners;
		final SignalBitListener[] new_listeners = new SignalBitListener[current_listeners.length - 1];
		int i, j;
		for(i = 0, j = 0; i < current_listeners.length; ++i)
			if(current_listeners[i] != listener)
				new_listeners[j++] = current_listeners[i];
		if(j != i)
			listeners = new_listeners;

		lock.set(false);
	}

	@Override public final void serialize(final SerializingStream out) throws IOException, SerializingException
	{
		assert out != null;

		if(this == LOW_SIGNAL)
			out.writeByte((byte)1);
		else if(this == HIGH_SIGNAL)
			out.writeByte((byte)2);
		else if(this == ZERO_SIGNAL)
			out.writeByte((byte)3);
		else
		{
			out.writeByte((byte)0);
			out.writeObject(bit.get(), false);
			out.writeObject(listeners, false);
		}
	}

	static final SignalBit LOW_SIGNAL;
	static final SignalBit HIGH_SIGNAL;
	static final SignalBit ZERO_SIGNAL;

	static final SignalBit getStaticBit(Bit b)
	{
		switch(b)
		{
		case L:
			return LOW_SIGNAL;
		case H:
			return HIGH_SIGNAL;
		case Z:
			return ZERO_SIGNAL;
		}

		return null;
	}

	static
	{
		LOW_SIGNAL = new SignalBit(Bit.L);
		HIGH_SIGNAL = new SignalBit(Bit.H);
		ZERO_SIGNAL = new SignalBit(Bit.Z);

		assert addStaticListener();
	}

	private static final boolean addStaticListener()
	{
		SignalBitListener sa = new SignalBitListener()
			{
				@Override public final void bitChanged(final SignalBit changed_bit, final Bit old_value, final Bit new_value)
				{
					assert false;
				}

				@Override public final void serialize(final SerializingStream out) throws IOException, SerializingException { }
			};

		LOW_SIGNAL.addSignalBitListener(sa);
		HIGH_SIGNAL.addSignalBitListener(sa);
		ZERO_SIGNAL.addSignalBitListener(sa);

		return true;
	}

	private static final SignalBit getDeserializedObject(final DeserializingStream in) throws IOException, DeserializingException, InstantiationException
	{
		byte type = in.readByte();
		switch(type)
		{
		case 0:
			return new SignalBit(in);
		case 1:
			return LOW_SIGNAL;
		case 2:
			return HIGH_SIGNAL;
		case 3:
			return ZERO_SIGNAL;
		default:
			throw new DeserializingException.CorruptStream(SignalBit.class.getName());
		}
	}

	SignalBit(final Bit v)
	{
		bit = new AtomicReference<Bit>(v);
		listeners = new SignalBitListener[0];
		lock = new AtomicBoolean(false);
	}

	final AtomicReference<Bit> bit;

	private volatile SignalBitListener[] listeners;
	private final AtomicBoolean lock;

	private SignalBit(final DeserializingStream in) throws IOException, DeserializingException, InstantiationException
	{
		this.bit = new AtomicReference<Bit>(in.readObject(Bit.class));
		this.listeners = in.readObject(SignalBitListener[].class);
		this.lock = new AtomicBoolean(false);
	}
}
