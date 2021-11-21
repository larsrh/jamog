
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

package core.build;

import core.exception.AnalyzeException;
import core.exception.BuildException;
import core.exception.DeserializingException;
import core.exception.InstantiationException;
import core.exception.SerializingException;
import core.misc.serial.DeserializingStream;
import core.misc.serial.SerializingStream;
import core.monitor.EnvironmentListener;
import core.monitor.Monitor;
import core.monitor.AnalyzeListener;
import core.monitor.ComponentListener;
import core.signal.Signal;
import core.signal.SignalBit;
import core.sim.Calculator;
import core.sim.Simulator;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import static core.monitor.Monitor.ExceptionPolicy;

/**
 * This class is responsible for ordered construction of components, which means
 * it collects all top-level components, constructs them, and proceeds recursively.
 * A sample workflow looks like this:
 * <pre>
 *   {@link Environment} env = {@link Environment#Environment() new Environment}();
 *   {@link Component SomeComponent} comp = {@link Component#Component(core.build.ComponentCollection, java.lang.String) new SomeComponent}(env,"some-component-no-1");
 *   {@link Signal} in = {@link Signal#Signal(int) new Signal}(5);
 *   {@link Component#set(java.lang.String, java.lang.Object) comp.set}("in",in);
 *   // and so on...
 *   {@link Monitor} m = {@link Monitor#Monitor() new Monitor}();
 *   {@link Monitor#addEnvironmentListener(core.monitor.EnvironmentListener) m.addEnvironmentListener}({@link EnvironmentListener Monitor.DEFAULT_ENVIRONMENT_LISTENER});
 *   {@link Simulator} s = {@link Environment#build(java.lang.Object) env.build}(m);
 * </pre>
 * @author lars
 * @see ComponentCollection
 * @see Component
 * @see Simulator
 */
public class Environment implements ComponentCollection {

	private final Map<String,Component> components;

	/**
	 * Initializes a new environment with no components.
	 */
	public Environment() {
		this.components = new LinkedHashMap<String, Component>();
	}

	@SuppressWarnings("unchecked")
	protected Environment(DeserializingStream in) throws IOException, DeserializingException, InstantiationException
	{
		components = in.readObject(LinkedHashMap.class, String.class);
	}

	/**
	 * Removes a component from this environment. Further use of this component
	 * is permitted. Re-adding to a {@link ComponentCollection} which is not an
	 * {@link Environment} may produce arbitrary behaviour.
	 * NOTE: This implementation is not really good and may be changed in a
	 * future version, but you can rely on that removing and adding {@link Component}s
	 * to {@link Environment}s will work.
	 * This method will do nothing if the component is not found in this environment.
	 * @param name the short name of the component (which is the name you gave
	 * to the constructor of it)
	 */
	public final void removeComponent(String name) {
		Component c = components.remove(name);
		if (c == null)
			return;
		c.parent = null; // allow component to be added again
	}

	/**
	 * Changes the name of a component and, if existing, all its children.
	 * This method will do nothing if the component is not found in this environment.
	 * @param oldName the old short name of the component
	 * @param newName the new short name of the component
	 */
	public final void renameComponent(String oldName, String newName) {
		assert !components.containsKey(newName) : "name '"+newName+"' already in use";

		Component c = components.remove(oldName);
		if (c == null)
			return;
		
		components.put(newName, c);
		c.setName(newName);
	}

	/**
	 * Provides map-based access to all {@link Component}s in this environment.
	 * It is ensured that, for each map entry, {@code name.equals(component.getShortName())}
	 * and {@code name.equals(component.getName())} are both {@code true}.
	 * @return an unmodifiable map holding all {@link Component}s in this
	 * environment
	 */
	@Override
	public final Map<String,Component> getComponents() {
		return Collections.unmodifiableMap(components);
	}

	/**
	 * Major building method to build and construct all components and to
	 * create a {@link Simulator} which can be used to simulate the set of
	 * components in this {@link Environment}. Note that structural changes
	 * are reflected only if a new {@link Simulator} is built afterwards.
	 * It is possible that a component will be removed from this environment
	 * during build. This happens, when {@link EnvironmentListener#exceptionOccured(core.build.Component, java.lang.Exception)}
	 * returns {@code false}, or no {@link EnvironmentListener} is present,
	 * which means that the operation should be aborted.
	 * Please check {@link ExceptionPolicy} and {@link EnvironmentListener}
	 * for that issue.
	 * Calling this method may last really long due to several expensive
	 * operations. Any {@link ComponentListener} will disappear after that.
	 * @return a ready-to-use {@link Simulator}
	 * @see Monitor
	 * @see EnvironmentListener
	 * @see Simulator
	 * @see AnalyzeListener
	 */
	public final Simulator build(EnvironmentListener envListener,AnalyzeListener simListener) throws AnalyzeException, BuildException {
		Set<Calculator> allCalculators = new HashSet<Calculator>();
		Map<SignalBit,Set<SignalBit>> priorities = new LinkedHashMap<SignalBit, Set<SignalBit>>();

		// this will be called here (and not in Component.cleanup) because
		// there, removal would be done on every Component, but we need to
		// remove listeners only on top-level components.
		for (Component c : components.values())
			c.removeAllComponentListeners();

		build(components.values(),allCalculators,priorities,envListener);
		return new Simulator(allCalculators,priorities,simListener);
	}

	public final Simulator build(EnvironmentListener envListener) throws AnalyzeException, BuildException {
		return build(envListener,null);
	}

	public final Simulator build(AnalyzeListener simListener) throws AnalyzeException, BuildException {
		return build(null,simListener);
	}

	public final Simulator build() throws AnalyzeException, BuildException {
		return build(null,null);
	}

	/**
	 * Builds components recursively.
	 * @param components all {@link Component components} to be built
	 * @param calculators set of {@link Calculator calculators} to be passed to the {@link Simulator}
	 * @param priorities {@link Signal} priorities, see {@link Simulator} for further details
	 * @param listener a listener to which the build progress will be submitted
	 */
	private static final void build(Collection<Component> components,Set<Calculator> calculators,Map<SignalBit,Set<SignalBit>> priorities,EnvironmentListener listener) throws BuildException {
		if (components.isEmpty())
			return;

		if (listener != null)
		    listener.increaseTotalCount(components.size());

		for (Iterator<Component> iterator = components.iterator();iterator.hasNext();) {
			Component entry = iterator.next();
			try {
				Set<Component> children = new LinkedHashSet<Component>();

				// merge priorites
				for (Map.Entry<SignalBit,Set<SignalBit>> prio : entry.construct(children).entrySet())
					if (priorities.containsKey(prio.getKey()))
						priorities.get(prio.getKey()).addAll(prio.getValue());
					else
						priorities.put(prio.getKey(),new LinkedHashSet<SignalBit>(prio.getValue()));

				build(children,calculators,priorities,listener); // proceed recursively

				entry.init(); // construction done, init the component

				if (entry instanceof Calculator) // add to simulator
					calculators.add((Calculator)entry);

				if (listener != null)
				    listener.increaseConstructedCount(1);
			}
			catch (Exception ex) { // 'faulty' component
				entry.parent = null; // allow component to be added again
				iterator.remove();

				if (listener == null || !listener.exceptionOccured(entry,ex))
					if (ex instanceof BuildException)
						throw (BuildException)ex;
					else
						throw new BuildException(ex);
			}
		}
	}

	@Override
	public int size() {
		return components.size();
	}

	@Override
	public Component addComponent(Component component) {
		String name = component.getShortName();

		assert component.parent == null : "component with name '"+name+"' already in use in another collection";
		assert !components.containsKey(name) : "name '"+name+"' already in use";

		components.put(name,component);
		component.parent = this; // assignment is needed because component could have been removed earlier, so parent would not be set by constructor

		return component;
	}

	@Override
	public String getName() {
		return ""; // intentional empty string
	}

	@Override
	public void serialize(SerializingStream out) throws IOException, SerializingException {
		out.writeObject(components, false, false);
	}

}
