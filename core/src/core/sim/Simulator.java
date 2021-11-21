
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

import core.exception.AnalyzeException;
import core.exception.DeserializingException;
import core.exception.InstantiationException;
import core.exception.SerializingException;
import core.misc.serial.Serializable;
import core.misc.serial.DeserializingStream;
import core.misc.serial.SerializingStream;
import core.monitor.SignalBitListener;
import core.monitor.AnalyzeListener;
import core.monitor.SimulationListener;
import core.signal.Bit;
import core.signal.Signal;
import core.signal.SignalBit;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.concurrent.locks.LockSupport;

/**
 * The {@code Simulator} class provides simulating an immutable set of {@link
 * Calculator}s with immutable input and output {@link Signal}s. Most methods
 * are not thread-safe.
 * <p>
 * The given {@link Calculator}s and their {@link Signal}s are analyzed by the
 * constructor to calculate an execution order to provide {@link Signal}
 * priorities, parallel execution in threads and graph depth ordering.
 * </p>
 * <p>
 * The {@link Signal} priorities provides a simple way to immitate time flow
 * without the need to specify any times at all. Priorities on {@link Signal}s
 * can form a directed graph without directed cycles, and low-priority {@link
 * Signal}s should not be written by any {@link Calculator} who directly writes
 * to the high-priority {@link Signal}, but can be if the reading {@link
 * Calculator}s are different, and vice versa.
 * </p>
 * <p>
 * After applying the priority ordering, the graphs are divided into connected
 * components and in each of them the {@link Calculator}s are ordered after
 * their maximum graph depth (ignoring cycles) outgoing from the subgraphs
 * sources. The {@link Calculator}s with lower graph depths are executed first,
 * but {@link Calculator}s with the same depth or who lying in another
 * connected component are calculated in parallel by worker threads.
 * </p>
 * <p>
 * The {@code Simulator} uses concurrent modifications to synchronize the
 * worker threads, and most internal structures are arrays. The ordering is
 * done by van Emde Boas trees and bitarrays. No objects are actually created
 * during simulation by the {@code Simulator}. Only {@link Calculator}s with
 * changed input {@link Signal}s are actually computed, they are called
 * scheduled {@link Calculator}s. The calculation of one {@link Calculator}
 * runs in log-logarithmic time due to the ordering in the van Emde Boas trees,
 * all other used operations run in constant time.
 * </p>
 *
 * @see SignalBit
 * @see Calculator
 *
 * @author torben
 */
public final class Simulator implements SignalBitListener, Serializable
{
	/**
	 * Invokes {@link Simulator#Simulator(java.util.Set, java.util.Map,
	 * core.monitor.AnalyzeListener) Simulator(calculators, priority_map,
	 * null)}.
	 *
	 * @param calculators  A set of {@link Calculator}s
	 * @param priority_map A map of {@link SignalBit} priorities
	 *
	 * @throws AnalyzeException if the priorities form a directed cycle or the
	 *                          readers and writers of a priority pair aren't
	 *                          disjunct
	 */
	public Simulator(final Set<Calculator> calculators, final Map<SignalBit, Set<SignalBit>> priority_map) throws AnalyzeException
	{
		this(calculators, priority_map, null);
	}

	/**
	 * Creates a new {@code Simulator} with the given set of {@link
	 * Calculator}s. This analyzes the {@link Calculator}s to create the
	 * ordering. To get informations of the current state of analysing, an
	 * {@link AnalyzeListener} can be provided to get notified about the
	 * analysing process.
	 *
	 * @param calculators  A set of {@link Calculator}s
	 * @param priority_map A map of {@link SignalBit} priorities
	 * @param listener     An {@link AnalyzeListener} to get notified about the
	 *                     analysing process
	 *
	 * @throws AnalyzeException if the priorities form a directed cycle or the
	 *                          readers and writers of a priority pair aren't
	 *                          disjunct
	 */
	public Simulator(final Set<Calculator> calculators, final Map<SignalBit, Set<SignalBit>> priority_map, final AnalyzeListener listener) throws AnalyzeException
	{
		assert calculators != null;
		assert priority_map != null;
		assert checkCalculators(calculators);
		assert checkPriorityMap(priority_map);

		if(listener != null)
		{
			int priority_pairs_count = 0;
			for(final Set<SignalBit> s : priority_map.values())
				priority_pairs_count += s.size();
			listener.initTotals(calculators.size(), priority_pairs_count);
		}

		final State state = new State(listener);

		final Set<SignalBit> bits = new HashSet<SignalBit>();
		for(final Calculator c : calculators)
		{
			for(final SignalBit b : c.getInputSignalBits())
			{
				if(!state.readers.containsKey(b))
					state.readers.put(b, new LinkedHashSet<Calculator>());
				state.readers.get(b).add(c);

				bits.add(b);
			}

			for(final SignalBit b : c.getOutputSignalBits())
			{
				if(!state.writers.containsKey(b))
					state.writers.put(b, new LinkedHashSet<Calculator>());
				state.writers.get(b).add(c);

				bits.add(b);
			}

			if(listener != null)
				listener.increaseCalculators(1, 1);
		}

		final Set<PriorityPair> priority_pairs = new LinkedHashSet<PriorityPair>();
		for(final Map.Entry<SignalBit, Set<SignalBit>> e : priority_map.entrySet())
		{
			for(final SignalBit s : e.getValue())
			{
				dr:
				{
					if(state.readers.containsKey(e.getKey()) && state.readers.containsKey(s))
						for(final Calculator c1 : state.readers.get(e.getKey()))
							for(final Calculator c2 : state.readers.get(s))
								if(c1 == c2)
									break dr;

					priority_pairs.add(new PriorityPair(PriorityPair.Type.DISJUNCT_READERS, e.getKey(), s, state.readers, state.writers));

					if(listener != null)
						listener.increasePriorityPairs(1, 1);

					continue;
				}

				if(state.writers.containsKey(e.getKey()) && state.writers.containsKey(s))
					for(final Calculator c1 : state.writers.get(e.getKey()))
						for(final Calculator c2 : state.writers.get(s))
							if(c1 == c2)
								throw new AnalyzeException.NondisjunctReadersWritersException();

				priority_pairs.add(new PriorityPair(PriorityPair.Type.DISJUNCT_WRITERS, e.getKey(), s, state.readers, state.writers));

				if(listener != null)
					listener.increasePriorityPairs(1, 1);
			}
		}

		final Map<Calculator, Integer> before_map = new HashMap<Calculator, Integer>();
		final Map<Calculator, Integer> after_map = new HashMap<Calculator, Integer>();

		final ArrayList<Set<PriorityPair>> priority_sets = new ArrayList<Set<PriorityPair>>();

		for(final PriorityPair p : priority_pairs)
		{
			int ins_before = Integer.MAX_VALUE;
			if(p.must_not != null)
				for(final Calculator c : p.must_not)
					if(before_map.containsKey(c) && before_map.get(c) < ins_before)
						ins_before = before_map.get(c);

			int ins_after = -1;
			if(p.must != null)
				for(final Calculator c : p.must)
					if(after_map.containsKey(c) && after_map.get(c) > ins_after)
						ins_after = after_map.get(c);

			if(ins_after > ins_before)
				throw new AnalyzeException.DirectedPriorityCycleException();

			if(ins_after == ins_before)
			{
				priority_sets.add(ins_before++, new LinkedHashSet<PriorityPair>());

				outer: for(final Iterator<PriorityPair> i  = priority_sets.get(ins_before).iterator(); i.hasNext();)
				{
					final PriorityPair pp = i.next();

					if(p.must_not != null && pp.must != null)
						for(final Calculator c1 : p.must_not)
							for(final Calculator c2 : pp.must)
								if(c1 == c2)
								{
									if(p.must != null && pp.must_not != null)
										for(final Calculator c3 : p.must)
											for(final Calculator c4 : pp.must_not)
												if(c3 == c4)
													throw new AnalyzeException.DirectedPriorityCycleException();
									break outer;
								}

					priority_sets.get(ins_after).add(pp);
					i.remove();
				}
			}

			if(++ins_after == ins_before)
			{
				priority_sets.add(ins_before, new LinkedHashSet<PriorityPair>());

				for(final Map.Entry<Calculator, Integer> e : before_map.entrySet())
					if(e.getValue() >= ins_before)
						e.setValue(e.getValue() + 1);
				for(final Map.Entry<Calculator, Integer> e : after_map.entrySet())
					if(e.getValue() >= ins_before)
						e.setValue(e.getValue() + 1);
			}
			else if(ins_after == priority_sets.size())
				priority_sets.add(new HashSet<PriorityPair>());

			priority_sets.get(ins_after).add(p);

			if(p.must != null)
				for(final Calculator c : p.must)
					before_map.put(c, ins_after);
			if(p.must_not != null)
				for(final Calculator c : p.must_not)
					after_map.put(c, ins_after);

			if(listener != null)
				listener.increasePriorityPairs(2, 1);
		}

		final Set<Calculator> unordered_calculators = new LinkedHashSet<Calculator>(calculators);
		for(int i = 0; i < priority_sets.size(); ++i)
		{
			final Set<Calculator> priority_calculators = new LinkedHashSet<Calculator>();
			final Queue<Calculator> queue = new ArrayDeque<Calculator>();

			for(final PriorityPair p : priority_sets.get(i))
				if(p.must != null)
					for(final Calculator c : p.must)
						if(unordered_calculators.remove(c))
						{
							priority_calculators.add(c);
							queue.add(c);

							if(listener != null)
								listener.increaseCalculators(2, 1);
						}

			Calculator h;
			while((h = queue.poll()) != null)
				for(final SignalBit sb : h.getInputSignalBits())
				{
					if(state.writers.containsKey(sb))
						dep: for(final Calculator c : state.writers.get(sb))
							if(unordered_calculators.contains(c))
							{
								for(int k = i; k < priority_sets.size(); ++k)
									for(final PriorityPair pp : priority_sets.get(i))
										if(pp.must_not != null)
											for(final Calculator cal : pp.must_not)
												if(c == cal)
													continue dep;

								unordered_calculators.remove(c);
								priority_calculators.add(c);
								queue.add(c);

								if(listener != null)
									listener.increaseCalculators(2, 1);
							}
				}

			orderGraph(state, priority_calculators, i);
		}

		if(listener != null)
			listener.increaseCalculators(2, unordered_calculators.size());
		orderGraph(state, unordered_calculators, priority_sets.size());

		connections = new HashMap<SignalBit, OrderedCalculator[]>();
		for(final Map.Entry<SignalBit, Set<OrderedCalculator>> e : state.connection_sets.entrySet())
		{
			final OrderedCalculator[] array = new OrderedCalculator[e.getValue().size()];
			int i = 0;
			for(final OrderedCalculator oc : e.getValue())
				array[i++] = oc;
			connections.put(e.getKey(), array);
			e.getKey().addSignalBitListener(this);
		}

		int p = priority_sets.size() + 1;
		priority_tree = VBETree.create(p);
		order_tree = new VBETree[p][];
		this.calculators = new OrderedCalculator[priority_sets.size() + 1][][][];

		dirty_stack = new AtomicReferenceArray<OrderedCalculator>(calculators.size());
		dirty_count = new AtomicInteger(0);

		ex_cnt = new AtomicInteger(0);

		calculate_stack = new AtomicReferenceArray<OrderedCalculator>(calculators.size());
		calculate_count = new AtomicInteger(0);

		for(int i = 0; i < p; ++i)
		{
			final int g = state.group_count.get(i) + 1;
			priority_tree.insert(i);
			order_tree[i] = new VBETree[g];
			this.calculators[i] = new OrderedCalculator[g][][];
			for(int j = 0; j < g; ++j)
			{
				final int o = state.order_count.get(new OrderKey(i, j)) + 1;
				order_tree[i][j] = VBETree.create(o);
				this.calculators[i][j] = new OrderedCalculator[o][];
				for(int k = 0; k < o; ++k)
				{
					final int n = state.number_count.get(new NumberKey(i, j, k)) + 1;
					order_tree[i][j].insert(k);
					this.calculators[i][j][k] = new OrderedCalculator[n];
					if(listener != null)
						listener.increaseCalculators(6, n);
				}
			}
		}

		for(final OrderedCalculator c : state.ordered_calculators)
			this.calculators[c.priority][c.group][c.order][c.number] = c;

		listeners = new SimulationListener[0];
		listener_lock = new AtomicBoolean(false);

		shutdown = false;
		parent = null;

		workers = new Thread[Runtime.getRuntime().availableProcessors()];
		suspended = new AtomicBoolean[workers.length];
		for(int i = 0; i < workers.length; ++i)
		{
			suspended[i] = new AtomicBoolean(true);

			workers[i] = new Thread(new Worker(i));
			workers[i].setDaemon(true);
			workers[i].start();
		}
	}

	/**
	 * Simulates until all {@link Calculator}s aren't scheduled anymore. It
	 * will call {@link #doStep() doStep()} until it returns false.
	 */
	public final void doSimulation()
	{
		while(doStep());
	}

	/**
	 * Calculate the next set of {@link Calculator}s which can be computed in
	 * parallel, if there are any scheduled.
	 *
	 * @return true if some {@link Calculator}s were actually computed, false
	 *         otherwise
	 */
	public final boolean doStep()
	{
		if(shutdown)
			return false;

		reschedule();

		final int priority = priority_tree.min();
		if(priority == -1)
			return false;

		int stack = 0;
		boolean empty = true;
		for(int i = 0; i < calculators[priority].length; ++i)
		{
			final VBETree ot = order_tree[priority][i];
			final int order = ot.min();
			if(order != -1)
			{
				ot.delete(order);

				final OrderedCalculator[] ocs = calculators[priority][i][order];
				for(int j = 0; j < ocs.length; ++j)
				{
					if(ocs[j] != null)
					{
						calculate_stack.set(stack++, ocs[j]);
						ocs[j] = null;
					}
				}

				if(ot.size() != 0)
					empty = false;
			}
		}
		if(empty)
			priority_tree.delete(priority);

		ex_cnt.set(workers.length);
		calculate_count.set(stack);
		parent = Thread.currentThread();

		for(int i = 0; i < workers.length; ++i)
		{
			suspended[i].set(false);
			LockSupport.unpark(workers[i]);
		}

		do
			LockSupport.park();
		while(ex_cnt.get() > 0);

		parent = null;

		return true;
	}

	/**
	 * Shuts down all worker threads. After calling this, {@link #doStep()
	 * doStep()} will always return false and do nothing.
	 *
	 * @see #restart() restart()
	 */
	public final void shutdown()
	{
		if(!shutdown)
		{
			shutdown = true;
			for(int i = 0; i < workers.length; ++i)
			{
				suspended[i].set(false);
				LockSupport.unpark(workers[i]);
			}
		}
	}

	/**
	 * Restarts all worker threads after a shutdown. After calling this, {@link
	 * #doStep() doStep()} will work again.
	 *
	 * @see #shutdown() shutdown()
	 */
	public final void restart()
	{
		if(shutdown)
		{
			shutdown = false;
			for(int i = 0; i < workers.length; ++i)
			{
				suspended[i].set(true);

				workers[i] = new Thread(new Worker(i));
				workers[i].setDaemon(true);
				workers[i].start();
			}
		}
	}

	/**
	 * @return A set of all {@link Calculator}s which are currently scheduled.
	 */
	public final Set<Calculator> getScheduledCalculators()
	{
		final Set<Calculator> result = new HashSet<Calculator>();

		for(int i = 0; i < calculators.length; ++i)
			for(int j = 0; j < calculators[i].length; ++j)
				for(int k = 0; k < calculators[i][j].length; ++k)
					for(int l = 0; l < calculators[i][j][k].length; ++l)
						if(calculators[i][j][k][l] != null)
							result.add(calculators[i][j][k][l].calculator);

		return Collections.unmodifiableSet(result);
	}

	/**
	 * Adds a {@link SimulationListener} to the registered listeners of this
	 * {@code Simulator}. The {@link SimulationListener} is then notified via
	 * the {@link SimulationListener#calculatorFinished(core.sim.Calculator)
	 * calculatorFinished(Calculator)} method if the {@link Calculator}'s
	 * {@link #calculate() calculate()} method has finished. This method is
	 * thread-safe.
	 *
	 * @param listener The {@link SimulationListener} to add
	 */
	public final void addSimulationListener(final SimulationListener listener)
	{
		assert listener != null;

		while(!listener_lock.compareAndSet(false, true));

		outer:
		{
			final SimulationListener[] current_listeners = listeners;
			final SimulationListener[] new_listeners = new SimulationListener[current_listeners.length + 1];
			for(int i = 0; i < current_listeners.length; ++i)
				if(current_listeners[i] == listener)
					break outer;
				else
					new_listeners[i] = current_listeners[i];
			new_listeners[current_listeners.length] = listener;
			listeners = new_listeners;
		}

		listener_lock.set(false);
	}

	/**
	 * Removes a {@link SimulationListener} from the registered listeners of
	 * this {@code Simulator}. The {@link SimulationListener} is not longer
	 * notified via the {@link
	 * SimulationListener#calculatorFinished(core.sim.Calculator)
	 * calculatorFinished(Calculator)} method if the {@link Calculator}'s
	 * {@link #calculate() calculate()} method has finished. This method is
	 * thread-safe.
	 *
	 * @param listener The {@link SimulationListener} to remove
	 */
	public final void removeSimulationListener(final SimulationListener listener)
	{
		while(!listener_lock.compareAndSet(false, true));

		final SimulationListener[] current_listeners = listeners;
		final SimulationListener[] new_listeners = new SimulationListener[current_listeners.length - 1];
		int i, j;
		for(i = 0, j = 0; i < current_listeners.length; ++i)
			if(current_listeners[i] != listener)
				new_listeners[j++] = current_listeners[i];
		if(j != i)
			listeners = new_listeners;

		listener_lock.set(false);
	}

	@Override public final void bitChanged(final SignalBit changed_bit, final Bit old_value, final Bit new_value)
	{
		assert changed_bit != null;
		assert connections.containsKey(changed_bit);

		for(final OrderedCalculator oc : connections.get(changed_bit))
			if(oc.dirty.compareAndSet(false, true))
				dirty_stack.set(dirty_count.getAndIncrement(), oc);
	}

	@Override public final void serialize(final SerializingStream out) throws IOException, SerializingException
	{
		assert out != null;

		reschedule();

		for(SignalBit b : connections.keySet())
			b.removeSignalBitListener(this);

		out.writeObject(connections, false, false, false, false);
		out.writeObject(priority_tree);
		out.writeObject(order_tree, false, false);
		out.writeObject(calculators, false, false, false, false, false);
		out.writeInteger(dirty_stack.length());
		out.writeObject(listeners, false);
		
		for(SignalBit b : connections.keySet())
			b.addSignalBitListener(this);
	}

	private final class Worker implements Runnable
	{
		@Override public final void run()
		{
			OrderedCalculator c;

			while(!shutdown)
			{
				while(suspended[number].get())
					LockSupport.park();

				if(shutdown)
					return;

				loop: while(true)
				{
					int i;
					do
						if((i = calculate_count.get()) == 0)
						{
							suspended[number].set(true);
							if(ex_cnt.decrementAndGet() == 0)
								LockSupport.unpark(parent);
							break loop;
						}
					while(!calculate_count.compareAndSet(i, i - 1));
					c = calculate_stack.get(i - 1);

					c.calculator.calculate();

					final SimulationListener[] current_listeners = listeners;
					for(final SimulationListener listener : current_listeners)
						listener.calculatorFinished(c.calculator);
				}
			}
		}

		Worker(int number)
		{
			this.number = number;
		}

		private int number;
	}

	private static final class OrderKey
	{
		final int priority;
		final int group;

		OrderKey(final int priority, final int group)
		{
			this.priority = priority;
			this.group = group;
		}

		@Override public final boolean equals(final Object obj)
		{
			if(obj == null || !(obj instanceof OrderKey))
				return false;
			final OrderKey ok = (OrderKey)obj;
			return priority == ok.priority && group == ok.group;
		}

		@Override public final int hashCode()
		{
			return (priority << 16) ^ group;
		}
	}

	private static final class NumberKey
	{
		final int priority;
		final int group;
		final int order;

		NumberKey(final int priority, final int group, final int order)
		{
			this.priority = priority;
			this.group = group;
			this.order = order;
		}

		@Override public final boolean equals(final Object obj)
		{
			if(obj == null || !(obj instanceof NumberKey))
				return false;
			final NumberKey nk = (NumberKey)obj;
			return priority == nk.priority && group == nk.group && order == nk.order;
		}

		@Override public final int hashCode()
		{
			return (priority << 22) ^ (group << 11) ^ order;
		}
	}

	private static final class State
	{
		final AnalyzeListener listener;
		final Map<SignalBit, Set<Calculator>> readers;
		final Map<SignalBit, Set<Calculator>> writers;
		final Set<OrderedCalculator> ordered_calculators;
		final Map<Integer, Integer> group_count;
		final Map<OrderKey, Integer> order_count;
		final Map<NumberKey, Integer> number_count;
		final Map<SignalBit, Set<OrderedCalculator>> connection_sets;

		State(final AnalyzeListener listener)
		{
			this.listener = listener;
			readers = new HashMap<SignalBit, Set<Calculator>>();
			writers = new HashMap<SignalBit, Set<Calculator>>();
			ordered_calculators = new HashSet<OrderedCalculator>();
			group_count = new HashMap<Integer, Integer>();
			order_count = new HashMap<OrderKey, Integer>();
			number_count = new HashMap<NumberKey, Integer>();
			connection_sets = new LinkedHashMap<SignalBit, Set<OrderedCalculator>>();
		}
	}

	private static final class PriorityPair
	{
		static enum Type
		{
			DISJUNCT_WRITERS, DISJUNCT_READERS;
		}

		final SignalBit high;
		final SignalBit low;
		final Set<Calculator> must;
		final Set<Calculator> must_not;

		PriorityPair(final Type type, final SignalBit high, final SignalBit low, final Map<SignalBit, Set<Calculator>> readers, final Map<SignalBit, Set<Calculator>> writers)
		{
			this.high = high;
			this.low = low;

			must = type == Type.DISJUNCT_READERS ? readers.get(high) : writers.get(high);
			must_not = type == Type.DISJUNCT_READERS ? readers.get(low) : writers.get(low);
		}
	}

	private static final class OrderedCalculator implements Serializable
	{
		final Calculator calculator;
		final int priority;
		final int group;
		final int order;
		final int number;
		final AtomicBoolean dirty;

		OrderedCalculator(final Calculator calculator, final int priority, final int group, final int dependency, final int number)
		{
			this.calculator = calculator;
			this.priority = priority;
			this.group = group;
			this.order = dependency;
			this.number = number;

			dirty = new AtomicBoolean(false);
		}

		@Override public final void serialize(final SerializingStream out) throws IOException, SerializingException
		{
			out.writeObject(calculator);
			out.writeInteger(priority);
			out.writeInteger(group);
			out.writeInteger(order);
			out.writeInteger(number);
		}

		private OrderedCalculator(final DeserializingStream in) throws IOException, DeserializingException, InstantiationException
		{
			calculator = in.readObject();
			priority = in.readInteger();
			group = in.readInteger();
			order = in.readInteger();
			number = in.readInteger();

			dirty = new AtomicBoolean(false);
		}
	}

	private static abstract class VBETree implements Serializable
	{
		abstract int next(int i);
		abstract boolean insert(int i);
		abstract boolean delete(int i);

		abstract int size();
		abstract int min();
		abstract int max();

		static final class BitVector extends VBETree
		{
			long bitvector;

			BitVector()
			{
				bitvector = 0;
			}

			@Override final int next(final int i)
			{
				final int r = Long.numberOfTrailingZeros(bitvector >> (i + 1)) + i + 1;
				return r >= 64 ? -1 : r;
			}

			@Override final boolean insert(final int i)
			{
				final long s = 1 << i;
				final boolean r = (bitvector & s) == 0;
				bitvector |= s;
				return r;
			}

			@Override final boolean delete(final int i)
			{
				final long s = 1 << i;
				final boolean r = (bitvector & s) != 0;
				bitvector &= ~s;
				return r;
			}

			@Override final int size()
			{
				return Long.bitCount(bitvector);
			}

			@Override final int min()
			{
				final int r = Long.numberOfTrailingZeros(bitvector);
				return r == 64 ? -1 : r;
			}

			@Override final int max()
			{
				return 63 - Long.numberOfLeadingZeros(bitvector);
			}

			@Override public final void serialize(final SerializingStream out) throws IOException, SerializingException
			{
				out.writeLong(bitvector);
			}

			private BitVector(final DeserializingStream in) throws IOException, DeserializingException, InstantiationException
			{
				bitvector = in.readLong();
			}
		}

		static final class Recursive extends VBETree
		{
			final VBETree[] sub;
			final VBETree nonempty;
			int min;
			int max;
			int size;

			final int low;
			final int high;

			Recursive(final int n)
			{
				final int m = 32 - Integer.numberOfLeadingZeros(n - 1);
				final int l = 1 << (m >> 1);
				final int h = l << (m & 1);

				sub = new VBETree[h];
				for(int i = 0; i < sub.length; ++i)
					sub[i] = create(l);

				nonempty = create(h);

				min = -1;
				max = -1;
				size = 0;

				low = l - 1;
				high = m >> 1;
			}

			@Override final int next(final int i)
			{
				if(i < min)
					return min;
				final int a = i >> high;
				final int b = i & low;
				if(i <= sub[a].max())
					return sub[a].next(b);
				return sub[nonempty.next(a + 1)].min();
			}

			@Override final boolean insert(int i)
			{
				if(min != -1)
				{
					if(min > i)
					{
						int t = min;
						min = i;
						i = t;
					}
					if(max < i)
					{
						int t = max;
						max = i;
						i = t;
					}
					final int a = i >> high;
					final int b = i & low;
					if(sub[a].size() == 0 && nonempty.insert(a) || sub[a].insert(b))
					{
						++size;
						return true;
					}
					return false;
				}
				else
				{
					min = i;
					max = i;
					++size;
					return true;
				}
			}

			@Override final boolean delete(int i)
			{
				if(min != max)
				{
					if(min == i)
					{
						if(nonempty.size() == 0)
						{
							min = max;
							--size;
							return true;
						}
						i = sub[nonempty.min()].min();
						min = i;
					}
					if(max == i)
					{
						if(nonempty.size() == 0)
						{
							max = min;
							--size;
							return true;
						}
						i = sub[nonempty.max()].max();
						max = i;
					}
					final int a = i >> high;
					final int b = i & low;
					if(sub[a].delete(b))
					{
						if(sub[a].size() == 0)
							nonempty.delete(a);
						--size;
						return true;
					}
					return false;
				}
				else if(min == i)
				{
					min = -1;
					max = -1;
					--size;
					return true;
				}
				return false;
			}

			@Override final int size()
			{
				return size;
			}

			@Override final int min()
			{
				return min;
			}

			@Override final int max()
			{
				return max;
			}

			@Override public final void serialize(final SerializingStream out) throws IOException, SerializingException
			{
				out.writeObject(sub, false);
				out.writeObject(nonempty);
				out.writeInteger(min);
				out.writeInteger(max);
				out.writeInteger(size);
				out.writeInteger(low);
				out.writeInteger(high);
			}

			private Recursive(final DeserializingStream in) throws IOException, DeserializingException, InstantiationException
			{
				sub = in.readObject(VBETree[].class);
				nonempty = in.readObject();
				min = in.readInteger();
				max = in.readInteger();
				size = in.readInteger();
				low = in.readInteger();
				high = in.readInteger();
			}
		}

		static final VBETree create(final int n)
		{
			if(n <= 64)
				return new BitVector();
			else
				return new Recursive(n);
		}
	}

	private static final boolean checkCalculators(Set<Calculator> calculators)
	{
		for(final Calculator c : calculators)
			if(c == null)
				return false;
		return true;
	}

	private static final boolean checkPriorityMap(Map<SignalBit, Set<SignalBit>> priority_map)
	{
		for(final Map.Entry<SignalBit, Set<SignalBit>> e : priority_map.entrySet())
		{
			if(e.getKey() == null)
				return false;
			for(final SignalBit sb : e.getValue())
				if(sb == null)
					return false;
		}

		return true;
	}

	private static final void orderGraph(final State state, final Set<Calculator> calculators, final int priority)
	{
		int lg = -1;
		final Map<Calculator, Integer> group_map = new HashMap<Calculator, Integer>();
		final Map<Calculator, Integer> order_map = new HashMap<Calculator, Integer>();
		final Queue<Calculator> group_queue = new ArrayDeque<Calculator>();
		final Queue<Calculator> order_queue = new ArrayDeque<Calculator>();

		outer: for(final Calculator c : calculators)
		{
			for(final SignalBit b : c.getInputSignalBits())
				if(state.writers.containsKey(b))
					for(final Calculator cal : state.writers.get(b))
						if(calculators.contains(cal))
							continue outer;

			if(state.listener != null)
				state.listener.increaseCalculators(3, 1);

			order_map.put(c, 0);
			order_queue.add(c);
		}

		while(!order_queue.isEmpty())
		{
			final Calculator c = order_queue.poll();
			final int dep = order_map.get(c);

			for(final SignalBit b : c.getOutputSignalBits())
				if(state.readers.containsKey(b))
					for(final Calculator cal : state.readers.get(b))
						if(calculators.contains(cal) && !order_map.containsKey(cal))
						{
							if(state.listener != null)
								state.listener.increaseCalculators(3, 1);

							order_map.put(cal, dep + 1);
							order_queue.add(cal);
						}
		}

		for(final Calculator c : calculators)
		{
			if(!group_map.containsKey(c))
			{
				if(state.listener != null)
					state.listener.increaseCalculators(4, 1);

				group_map.put(c, ++lg);
				group_queue.add(c);

				do
				{
					final Calculator ca = group_queue.poll();

					for(final SignalBit b : ca.getInputSignalBits())
						if(state.writers.containsKey(b))
							for(final Calculator cal : state.writers.get(b))
								if(calculators.contains(cal) && !group_map.containsKey(cal))
								{
									if(state.listener != null)
										state.listener.increaseCalculators(4, 1);

									group_map.put(cal, lg);
									group_queue.add(cal);
								}

					for(final SignalBit b : ca.getOutputSignalBits())
						if(state.readers.containsKey(b))
							for(final Calculator cal : state.readers.get(b))
								if(calculators.contains(cal) && !group_map.containsKey(cal))
								{
									if(state.listener != null)
										state.listener.increaseCalculators(4, 1);

									group_map.put(cal, lg);
									group_queue.add(cal);
								}
				}
				while(!group_queue.isEmpty());
			}

			final int g = group_map.get(c);
			final int o = order_map.containsKey(c) ? order_map.get(c) : 0;

			if(!state.group_count.containsKey(priority) || state.group_count.get(priority) < g)
				state.group_count.put(priority, g);

			final OrderKey ok = new OrderKey(priority, g);
			if(!state.order_count.containsKey(ok) || state.order_count.get(ok) < o)
				state.order_count.put(ok, o);

			final int n;
			final NumberKey nk = new NumberKey(priority, g, o);
			if(!state.number_count.containsKey(nk))
				state.number_count.put(nk, n = 0);
			else
				state.number_count.put(nk, n = (state.number_count.get(nk) + 1));

			final OrderedCalculator oc = new OrderedCalculator(c, priority, g, o, n);
			state.ordered_calculators.add(oc);

			for(final SignalBit b : c.getInputSignalBits())
			{
				if(!state.connection_sets.containsKey(b))
					state.connection_sets.put(b, new LinkedHashSet<OrderedCalculator>());
				state.connection_sets.get(b).add(oc);
			}

			if(state.listener != null)
					state.listener.increaseCalculators(5, 1);
		}

		state.group_count.put(priority, lg);
	}

	private final Map<SignalBit, OrderedCalculator[]> connections;

	private final VBETree priority_tree;
	private final VBETree[][] order_tree;
	private final OrderedCalculator[][][][] calculators;

	private final AtomicReferenceArray<OrderedCalculator> dirty_stack;
	private final AtomicInteger dirty_count;
	private final AtomicInteger ex_cnt;

	private final AtomicReferenceArray<OrderedCalculator> calculate_stack;
	private final AtomicInteger calculate_count;

	private volatile boolean shutdown;
	private volatile Thread parent;

	private final Thread[] workers;
	private final AtomicBoolean[] suspended;

	private volatile SimulationListener[] listeners;
	private final AtomicBoolean listener_lock;

	@SuppressWarnings("unchecked")
	private Simulator(final DeserializingStream in) throws IOException, DeserializingException, InstantiationException
	{
		connections = in.readObject(HashMap.class, SignalBit.class, OrderedCalculator[].class, OrderedCalculator.class);

		for(SignalBit b : connections.keySet())
			b.addSignalBitListener(this);

		priority_tree = in.readObject();
		order_tree = in.readObject(VBETree[][].class, VBETree[].class);
		calculators = in.readObject(OrderedCalculator[][][][].class, OrderedCalculator[][][].class, OrderedCalculator[][].class, OrderedCalculator[].class, OrderedCalculator.class);

		dirty_stack = new AtomicReferenceArray<OrderedCalculator>(in.readInteger());
		dirty_count = new AtomicInteger(0);
		ex_cnt = new AtomicInteger(0);

		calculate_stack = new AtomicReferenceArray<OrderedCalculator>(dirty_stack.length());
		calculate_count = new AtomicInteger(0);

		shutdown = false;
		parent = null;

		workers = new Thread[Runtime.getRuntime().availableProcessors()];
		suspended = new AtomicBoolean[workers.length];
		for(int i = 0; i < workers.length; ++i)
		{
			suspended[i] = new AtomicBoolean(true);

			workers[i] = new Thread(new Worker(i));
			workers[i].setDaemon(true);
			workers[i].start();
		}

		listeners = in.readObject(SimulationListener[].class);
		listener_lock = new AtomicBoolean(false);
	}

	private final void reschedule()
	{
		final int cnt = dirty_count.get();

		if(cnt != 0)
		{
			int i = 0;
			do
			{
				final OrderedCalculator c = dirty_stack.get(i);
				c.dirty.set(false);

				if(calculators[c.priority][c.group][c.order][c.number] == null)
				{
					calculators[c.priority][c.group][c.order][c.number] = c;

					priority_tree.insert(c.priority);
					order_tree[c.priority][c.group].insert(c.order);
				}
			}
			while(++i < cnt);
			dirty_count.set(0);
		}
	}
}
