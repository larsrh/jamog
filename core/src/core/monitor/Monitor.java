
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

import core.build.Environment;
import core.build.Component;
import core.exception.DeserializingException;
import core.exception.InstantiationException;
import core.exception.SerializingException;
import core.misc.serial.DeserializingStream;
import core.misc.serial.SerializingStream;
import core.sim.Calculator;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 *
 * @author lars
 */
public final class Monitor implements EnvironmentListener,
	                                  SimulationListener,
                                      AnalyzeListener {

	public static final EnvironmentListener DEFAULT_ENVIRONMENT_LISTENER = new EnvironmentListener() {
		@Override public void increaseTotalCount(long delta) {}
		@Override public void increaseConstructedCount(long delta) {}

		@Override
		public boolean exceptionOccured(Component c, Exception ex) {
			ex.printStackTrace();
			return true;
		}
	};

	public static interface SimulationFormatter {

		public String format(Component component);

	}
	
	/**
	 * If an exception occures while building the {@link Environment} and two or more
	 * {@link EnvironmentListener}s are associated with this {@link Monitor},
	 * {@code ExceptionPolicy} decides whether the processing should be continued if
	 * all listeners say that processing should be continued or if any listener says that.
	 * Note that this is not lazy so all listeners will be notified although
	 * the result could be determined earlier (e. g. false and x is always false,
	 * but x will be evaluated nevertheless), because {@link EnvironmentListener#exceptionOccured(core.build.Component, java.lang.Exception) }
	 * also notifies the observer that an exception occured.
	 */
	public static enum ExceptionPolicy {
		AND,
		OR
	}

	private final ExceptionPolicy exceptionPolicy;

	private Set<EnvironmentListener> constructionListeners;
	private Set<SimulationListener> simulationListeners;
	private Set<AnalyzeListener> analyzeListeners;

	private long totalCount;
	private long constructedCount;
	private long analyzeCount;
	private long leafCount;

	public Monitor(ExceptionPolicy exceptionPolicy) {
		this.exceptionPolicy = exceptionPolicy;
		constructionListeners = new LinkedHashSet<EnvironmentListener>();
		simulationListeners = new LinkedHashSet<SimulationListener>();
		analyzeListeners = new LinkedHashSet<AnalyzeListener>();

		totalCount = 0;
		constructedCount = 0;
		analyzeCount = 0;
		leafCount = 0;
	}

	@SuppressWarnings("unchecked")
	public Monitor(DeserializingStream in) throws IOException, DeserializingException, InstantiationException
	{
		exceptionPolicy = in.readObject(ExceptionPolicy.class);
		constructionListeners = in.readObject(LinkedHashSet.class);
		simulationListeners = in.readObject(LinkedHashSet.class);
		analyzeListeners = in.readObject(LinkedHashSet.class);

		totalCount = in.readLong();
		constructedCount = in.readLong();
		analyzeCount = in.readLong();
		leafCount = in.readLong();
	}

	public Monitor() {
		this(ExceptionPolicy.AND);
	}

	@Override public void serialize(SerializingStream out) throws IOException, SerializingException
	{
		out.writeObject(exceptionPolicy, false);
		out.writeObject(constructionListeners, false);
		out.writeObject(simulationListeners, false);
		out.writeObject(analyzeListeners, false);

		out.writeLong(totalCount);
		out.writeLong(constructedCount);
		out.writeLong(analyzeCount);
		out.writeLong(leafCount);
	}

	// LISTENER MANIPULATION METHODS

	public Monitor addSimulationListener(SimulationListener listener) {
		simulationListeners.add(listener);
		return this;
	}

	public Monitor addAnalyzeListener(AnalyzeListener listener) {
		analyzeListeners.add(listener);
		return this;
	}

	public Monitor addEnvironmentListener(EnvironmentListener listener) {
		constructionListeners.add(listener);
		return this;
	}

	// STATUS REQUESTS

	public long getTotalCount()       { return totalCount; }
	public long getBuiltCount()       { return analyzeCount; }
	public long getConstructedCount() { return constructedCount; }

	public double getConstructionStatus() { return ((double)constructedCount)/totalCount; }
	public double getAnalyzeStatus()      { return (analyzeCount / 6.0)/leafCount; }

	// CONSTRUCTION LISTENER

	@Override
	public void increaseTotalCount(long delta) {
		this.totalCount += delta;
		for (EnvironmentListener cl : constructionListeners)
			cl.increaseTotalCount(delta);
	}

	@Override
	public void increaseConstructedCount(long delta) {
		this.constructedCount += delta;
		for (EnvironmentListener cl : constructionListeners)
			cl.increaseConstructedCount(delta);
	}

	@Override
	public boolean exceptionOccured(Component c, Exception ex) {
		boolean cont;
		if (exceptionPolicy == ExceptionPolicy.AND) {
			cont = true;
			for (EnvironmentListener cl : constructionListeners)
				cont &= cl.exceptionOccured(c,ex);
		}
		else {
			cont = false;
			for (EnvironmentListener cl : constructionListeners)
				cont |= cl.exceptionOccured(c,ex);
		}
		return cont;
	}

	// SIMULATION LISTENER

	@Override
	public void calculatorFinished(Calculator finished_calculator) {
		for (SimulationListener sl : simulationListeners)
			sl.calculatorFinished(finished_calculator);
	}

	// BUILD LISTENER

	@Override public void initTotals(int calculators, int priority_pairs)
	{
		leafCount = calculators;
		for(AnalyzeListener al : analyzeListeners)
			al.initTotals(calculators, priority_pairs);
	}

	@Override public void increaseCalculators(int state, int finished)
	{
		this.analyzeCount += finished;
		for(AnalyzeListener al : analyzeListeners)
			al.increaseCalculators(state, finished);
	}

	@Override public void increasePriorityPairs(int state, int finished)
	{
		for(AnalyzeListener al : analyzeListeners)
			al.increasePriorityPairs(state, finished);
	}
}
