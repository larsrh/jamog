
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

package core.sim;

import core.build.Component;
import core.build.ComponentCollection;
import core.exception.DeserializingException;
import core.exception.InstantiationException;
import core.exception.SerializingException;
import core.misc.serial.DeserializingStream;
import core.misc.serial.SerializingStream;
import core.monitor.CalculatorListener;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * The {@code Calculator} class is the supertype of all simulation primitives
 * which provides notification to registered {@link CalculatorListener}s when
 * the {@link #execute() execute()} method has finished the actual calculation.
 * For implementation details, see {@link #execute() execute()}. The
 * {@code Calculator} is thread-safe.
 * 
 * @see Component
 * @see CalculatorListener
 *
 * @author torben
 */
public abstract class Calculator extends Component
{
	/**
	 * The {@code execute()} method must provide the actual calculation code
	 * for a subclass of {@code Calculator}.
	 * <p>
	 * Implementation notes:
	 * <ul>
	 * <li>This method is triggered by {@link Simulator} when an input {@link
	 * core.signal.Signal Signal} changes, but not for each change and not
	 * directly or immediatly, so the output should not depend on it.</li>
	 * <li>The calculation is expected to be directed, so input {@link
	 * core.signal.Signal Signal}s should be handled read-only and output
	 * {@link core.signal.Signal Signal}s should be handled write-only.</li>
	 * <li>The {@code execute()} method is never called by multiple threads in
	 * parallel for the same instance, so it doesn't have to be thread-safe in
	 * generel. See next note.</li>
	 * <li>The {@code execute()} method is called by multiple threads in
	 * sequence for the same instance, so if the {@code execute()} method
	 * relies on other inputs than its input {@link core.signal.Signal
	 * Signal}s, be aware of details in the Java Memory Model.</li>
	 * </ul>
	 * </p>
	 */
	protected abstract void execute();

	/**
	 * Invokes {@link #execute() execute()} and notify all registered
	 * {@link CalculatorListener}s via the {@link
	 * CalculatorListener#calculateFinished(core.sim.Calculator)
	 * calculateFinished(Calculator)} method that the calculation is done. See
	 * {@link #execute() execute()} for implementation details. This method is
	 * thread-safe.
	 */
	public final void calculate()
	{
		execute();

		final CalculatorListener[] current_listeners = listeners;
		for(final CalculatorListener listener : current_listeners)
			listener.calculateFinished(this);
	}

	/**
	 * Adds a {@link CalculatorListener} to the registered listeners of this
	 * {@code Calculator}. The {@link CalculatorListener} is then notified via
	 * the {@link CalculatorListener#calculateFinished(core.sim.Calculator)
	 * calculateFinished(Calculator)} method if the {@link #calculate()
	 * calculate()} method has finished the actual calculation. This method is
	 * thread-safe.
	 *
	 * @param listener The {@link CalculatorListener} to add
	 */
	@Override public final void addCalculatorListener(final CalculatorListener listener)
	{
		assert listener != null;

		while(!lock.compareAndSet(false, true));

		outer:
		{
			final CalculatorListener[] current_listeners = listeners;
			final CalculatorListener[] new_listeners = new CalculatorListener[current_listeners.length + 1];
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
	 * Removes a {@link CalculatorListener} from the registered listeners of
	 * this {@code Calculator}. The {@link CalculatorListener} is not longer
	 * notified via the {@link
	 * CalculatorListener#calculateFinished(core.sim.Calculator)
	 * calculateFinished(Calculator)} method if the {@link #calculate()
	 * calculate()} method has finished the actual calculation. This method is
	 * thread-safe.
	 *
	 * @param listener The {@link CalculatorListener} to remove
	 */
	@Override public final void removeCalculatorListener(final CalculatorListener listener)
	{
		while(!lock.compareAndSet(false, true));

		final CalculatorListener[] current_listeners = listeners;
		final CalculatorListener[] new_listeners = new CalculatorListener[current_listeners.length - 1];
		int i, j;
		for(i = 0, j = 0; i < current_listeners.length; ++i)
			if(current_listeners[i] != listener)
				new_listeners[j++] = current_listeners[i];
		if(j != i)
			listeners = new_listeners;

		lock.set(false);
	}

	@Override public void serialize(final SerializingStream out) throws IOException, SerializingException
	{
		super.serialize(out);

		assert out != null;

		out.writeObject(listeners, false);
	}

	/**
	 * Invokes {@link Component#Component(core.build.ComponentCollection,
	 * java.lang.String) super(ComponentCollection, String)}.
	 *
	 * @param parent The parent {@link ComponentCollection} of this {@code
	 *               Calculator}
	 * @param name   The name of this {@code Calculator}
	 */
	protected Calculator(final ComponentCollection parent, final String name)
	{
		super(parent,name);

		listeners = new CalculatorListener[0];
		lock = new AtomicBoolean(false);
	}

	/**
	 * Invokes {@link Component#Component(core.misc.serial.DeserializingStream)
	 * super(DeserializingStream)}.
	 *
	 * @param in The {@link DeserializingStream} to deserialize from
	 *
	 * @throws IOException            if the {@link DeserializingStream} throws
	 *                                an {@code IOException}
	 * @throws DeserializingException if the {@link DeserializingStream} throws
	 *                                an {@code DeserializingException}
	 * @throws InstantiationException if the {@link DeserializingStream} throws
	 *                                an {@code InstantiationException}
	 */
	protected Calculator(final DeserializingStream in) throws IOException, DeserializingException, InstantiationException
	{
		super(in);

		assert in != null;

		listeners = in.readObject(CalculatorListener[].class);
		lock = new AtomicBoolean(false);
	}

	private volatile CalculatorListener[] listeners;
	private final AtomicBoolean lock;
}
