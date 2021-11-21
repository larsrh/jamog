
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

import core.exception.DeserializingException;
import core.exception.InstantiationException;
import core.exception.SerializingException;
import core.misc.serial.DeserializingStream;
import core.misc.serial.Serializable;
import core.misc.serial.SerializingStream;
import core.signal.Bit;
import core.signal.Signal;
import core.sim.Simulator;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.LockSupport;

/**
 * The {@code ClockSimulator} class provides a clock generator for a {@link
 * Simulator} and simplifies continous simulation in an additional thread with
 * notify listerns for each simulation and clock step. Some methods are not
 * thread-safe.
 *
 * @author torben
 */
public final class ClockSimulator implements Serializable
{
	/**
	 * The {@code StepListener} interface is used to get notified about a
	 * finished simulation step. If an instance of this interface is registered
	 * to a {@code ClockSimulator} as listener via the {@link
	 * ClockSimulator#addStepListener(core.misc.ClockSimulator.StepListener)
	 * addStepListener(StepListener)} method, the {@link #finishedStep()
	 * finishedStep()} is invoked after each simulation step.
	 */
	public static interface StepListener extends Serializable
	{
		/**
		 * Is invoked from {@code ClockSimulator}s to which the {@code
		 * StepListener} listens if a simulation step has been finished.
		 */
		public void finishedStep();
	}

	/**
	 * The {@code ClockListener} interface is used to get notified about a
	 * finished simulation clock cycle. If an instance of this interface is
	 * registered to a {@code ClockSimulator} as listener via the {@link
	 * ClockSimulator#addClockListener(core.misc.ClockSimulator.ClockListener)
	 * addClockListener(ClockListener)} method, the {@link #finishedClock()
	 * finishedClock()} is invoked after each simulation clock cycle.
	 */
	public static interface ClockListener extends Serializable
	{
		/**
		 * Is invoked from {@code ClockSimulator}s to which the {@code
		 * ClockListener} listens if a simulation clock cycle has been
		 * finished.
		 */
		public void finishedClock();
	}

	/**
	 * The {@code StateListener} interface is used to get notified about a
	 * actual state change in the worker thread. If an instance of this
	 * interface is registered to a {@code ClockSimulator} as listener via the
	 * {@link
	 * ClockSimulator#addStateListener(core.misc.ClockSimulator.StateListener)
	 * addStateListener(StateListener)} method, the {@link #changedState()
	 * changedState()} is invoked after the worker thread has changed its
	 * state.
	 */
	public static interface StateListener extends Serializable
	{
		/**
		 * Is invoked from {@code ClockSimulator}s to which the {@code
		 * StateListener} listens if the worker thread has changed its state.
		 */
		public void changedState();
	}

	/**
	 * Creates a new {@code ClockSimulator} and starts a new thread for the
	 * simulation. To start the simulation, invoke the {@link #resume()
	 * resume()} method.
	 *
	 * @param simulator The {@link Simulator} to simulate
	 * @param clk       The clock {@link Signal}
	 * @param clk_start The start {@link Bit} value for the clock
	 */
	public ClockSimulator(final Simulator simulator, final Signal clk, final Bit clk_start)
	{
		assert simulator != null;
		assert clk != null;
		assert clk_start != null;

		this.simulator = simulator;
		this.clk = clk;
		clk.setBits(clk_start);
		cur_clk = true;
		change_clk = false;

		step_listeners = new StepListener[0];
		clock_listeners = new ClockListener[0];
		state_listeners = new StateListener[0];
		step_lock = new AtomicBoolean(false);
		clock_lock = new AtomicBoolean(false);
		state_lock = new AtomicBoolean(false);

		suspend = true;
		shutdown = false;

		thread = new Thread(new Worker());
		thread.setDaemon(true);
		thread.start();
	}

	/**
	 * @return true if the {@code ClockSimulator} is shutdowned, false
	 *         otherwise
	 */
	public final boolean isShutdowned()
	{
		return shutdown;
	}

	/**
	 * Shuts down the simulation thread and the {@link Simulator}. After
	 * calling this, {@link #suspend() suspend()} and {@link #resume()
	 * resume()} have no effect.
	 *
	 * @see #restart() restart()
	 */
	public final void shutdown()
	{
		if(!shutdown)
		{
			shutdown = true;
			LockSupport.unpark(thread);
			simulator.shutdown();
		}
	}

	/**
	 * Restart the simulation thread and the {@link Simulator}. After calling
	 * this, {@link #suspend() suspend()} and {@link #resume() resume()} will
	 * work again.
	 *
	 * @see #shutdown() shutdown()
	 * @see #resume() resume()
	 */
	public final void restart()
	{
		if(shutdown)
		{
			shutdown = false;
			suspend = true;
			simulator.restart();
			thread = new Thread(new Worker());
			thread.setDaemon(true);
			thread.start();
		}
	}

	/**
	 * @return true if the {@code ClockSimulator} is suspended, false otherwise
	 */
	public final boolean isSuspended()
	{
		return suspend;
	}

	/**
	 * Suspends the simulation thread. This method is thread-safe.
	 *
	 * @see #resume() resume()
	 */
	public final void suspend()
	{
		suspend = true;
	}

	/**
	 * Resumes the simulation thread. This method is thread-safe.
	 *
	 * @see #suspend() suspend()
	 */
	public final void resume()
	{
		suspend = false;
		LockSupport.unpark(thread);
	}

	/**
	 * Adds a {@link StepListener} to the registered listeners of this {@code 
	 * ClockSimulator}. The {@link StepListener} is then notified via the
	 * {@link StepListener#finishedStep() finishedStep()} method if a
	 * simulation step has been finished. This method is thread-safe.
	 *
	 * @param listener The {@link StepListener} to add
	 */
	public final void addStepListener(final StepListener listener)
	{
		assert listener != null;

		while(!step_lock.compareAndSet(false, true));

		outer:
		{
			final StepListener[] current_listeners = step_listeners;
			final StepListener[] new_listeners = new StepListener[current_listeners.length + 1];
			for(int i = 0; i < current_listeners.length; ++i)
				if(current_listeners[i] == listener)
					break outer;
				else
					new_listeners[i] = current_listeners[i];
			new_listeners[current_listeners.length] = listener;
			step_listeners = new_listeners;
		}

		step_lock.set(false);
	}

	/**
	 * Removes a {@link StepListener} from the registered listeners of this
	 * {@code ClockSimulator}. The {@link StepListener} is not longer notified
	 * via the {@link StepListener#finishedStep() finishedStep()} method if a
	 * simulation step has been finished. This method is thread-safe.
	 *
	 * @param listener The {@link StepListener} to remove
	 */
	public final void removeStepListener(final StepListener listener)
	{
		while(!step_lock.compareAndSet(false, true));

		final StepListener[] current_listeners = step_listeners;
		final StepListener[] new_listeners = new StepListener[current_listeners.length - 1];
		int i, j;
		for(i = 0, j = 0; i < current_listeners.length; ++i)
			if(current_listeners[i] != listener)
				new_listeners[j++] = current_listeners[i];
		if(j != i)
			step_listeners = new_listeners;

		step_lock.set(false);
	}

	/**
	 * Adds a {@link ClockListener} to the registered listeners of this {@code
	 * ClockSimulator}. The {@link ClockListener} is then notified via the
	 * {@link ClockListener#finishedClock() finishedClock()} method if a
	 * simulation clock cycle has been finished. This method is thread-safe.
	 *
	 * @param listener The {@link ClockListener} to add
	 */
	public final void addClockListener(final ClockListener listener)
	{
		assert listener != null;

		while(!clock_lock.compareAndSet(false, true));

		outer:
		{
			final ClockListener[] current_listeners = clock_listeners;
			final ClockListener[] new_listeners = new ClockListener[current_listeners.length + 1];
			for(int i = 0; i < current_listeners.length; ++i)
				if(current_listeners[i] == listener)
					break outer;
				else
					new_listeners[i] = current_listeners[i];
			new_listeners[current_listeners.length] = listener;
			clock_listeners = new_listeners;
		}

		clock_lock.set(false);
	}

	/**
	 * Removes a {@link ClockListener} from the registered listeners of this
	 * {@code ClockSimulator}. The {@link ClockListener} is not longer notified
	 * via the {@link ClockListener#finishedClock() finishedClock()} method if
	 * a simulation clock cycle has been finished. This method is thread-safe.
	 *
	 * @param listener The {@link ClockListener} to remove
	 */
	public final void removeClockListener(final ClockListener listener)
	{
		while(clock_lock.compareAndSet(false, true));

		final ClockListener[] current_listeners = clock_listeners;
		final ClockListener[] new_listeners = new ClockListener[current_listeners.length - 1];
		int i, j;
		for(i = 0, j = 0; i < current_listeners.length; ++i)
			if(current_listeners[i] != listener)
				new_listeners[j++] = current_listeners[i];
		if(j != i)
			clock_listeners = new_listeners;

		clock_lock.set(false);
	}

	/**
	 * Adds a {@link StateListener} to the registered listeners of this {@code
	 * ClockSimulator}. The {@link StateListener} is then notified via the
	 * {@link StateListener#changedState() changedState()} method if the worker
	 * thread actually changes its state. This method is thread-safe.
	 *
	 * @param listener The {@link StateListener} to add
	 */
	public final void addStateListener(final StateListener listener)
	{
		assert listener != null;

		while(!state_lock.compareAndSet(false, true));

		outer:
		{
			final StateListener[] current_listeners = state_listeners;
			final StateListener[] new_listeners = new StateListener[current_listeners.length + 1];
			for(int i = 0; i < current_listeners.length; ++i)
				if(current_listeners[i] == listener)
					break outer;
				else
					new_listeners[i] = current_listeners[i];
			new_listeners[current_listeners.length] = listener;
			state_listeners = new_listeners;
		}

		state_lock.set(false);
	}

	/**
	 * Removes a {@link StateListener} from the registered listeners of this
	 * {@code ClockSimulator}. The {@link StateListener} is not longer notified
	 * via the {@link StateListener#changedState() changedState()} method if
	 * the worker thread actually changes its state. This method is
	 * thread-safe.
	 *
	 * @param listener The {@link StateListener} to remove
	 */
	public final void removeStateListener(final StateListener listener)
	{
		while(state_lock.compareAndSet(false, true));

		final StateListener[] current_listeners = state_listeners;
		final StateListener[] new_listeners = new StateListener[current_listeners.length - 1];
		int i, j;
		for(i = 0, j = 0; i < current_listeners.length; ++i)
			if(current_listeners[i] != listener)
				new_listeners[j++] = current_listeners[i];
		if(j != i)
			state_listeners = new_listeners;

		state_lock.set(false);
	}

	@Override public final void serialize(final SerializingStream out) throws IOException, SerializingException
	{
		assert out != null;

		out.writeObject(simulator, false);
		out.writeObject(clk, false);
		out.writeBoolean(cur_clk);
		out.writeBoolean(change_clk);
		out.writeObject(step_listeners, false);
		out.writeObject(clock_listeners, false);
		out.writeObject(state_listeners, false);
	}

	private final class Worker implements Runnable
	{
		@Override public final void run()
		{
			{
				final StateListener[] current_listeners = state_listeners;
				for(final StateListener listener : current_listeners)
					listener.changedState();
			}

			while(!shutdown)
			{
				outer: while(!shutdown)
				{
					while(suspend)
					{
						{
							final StateListener[] current_listeners = state_listeners;
							for(final StateListener listener : current_listeners)
								listener.changedState();
						}

						LockSupport.park();

						if(shutdown)
							break outer;

						{
							final StateListener[] current_listeners = state_listeners;
							for(final StateListener listener : current_listeners)
								listener.changedState();
						}
					}

					if(change_clk)
					{
						change_clk = false;
						clk.setBit(0, clk.getBit(0).not());
					}

					if(!simulator.doStep())
						break;

					final StepListener[] current_listeners = step_listeners;
					for(final StepListener listener : current_listeners)
						listener.finishedStep();
				}

				cur_clk = !cur_clk;
				change_clk = true;

				if(cur_clk)
				{
					final ClockListener[] current_listeners = clock_listeners;
					for(final ClockListener listener : current_listeners)
						listener.finishedClock();
				}
			}

			{
				final StateListener[] current_listeners = state_listeners;
				for(final StateListener listener : current_listeners)
					listener.changedState();
			}
		}
	}

	private final Simulator simulator;
	private final Signal clk;

	private Thread thread;
	private boolean cur_clk;
	private boolean change_clk;

	private volatile StepListener[] step_listeners;
	private volatile ClockListener[] clock_listeners;
	private volatile StateListener[] state_listeners;
	private final AtomicBoolean step_lock;
	private final AtomicBoolean clock_lock;
	private final AtomicBoolean state_lock;

	private volatile boolean suspend;
	private volatile boolean shutdown;

	private ClockSimulator(DeserializingStream in) throws IOException, DeserializingException, InstantiationException
	{
		simulator = in.readObject(Simulator.class);
		clk = in.readObject(Signal.class);

		cur_clk = in.readBoolean();
		change_clk = in.readBoolean();

		step_listeners = in.readObject(StepListener[].class);
		clock_listeners = in.readObject(ClockListener[].class);
		state_listeners = in.readObject(StateListener[].class);
		step_lock = new AtomicBoolean(false);
		clock_lock = new AtomicBoolean(false);
		state_lock = new AtomicBoolean(false);

		suspend = true;
		shutdown = false;

		thread = new Thread(new Worker());
		thread.setDaemon(true);
		thread.start();
	}
}
