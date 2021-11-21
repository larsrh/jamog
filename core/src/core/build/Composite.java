
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

import core.exception.DeserializingException;
import core.exception.InstantiationException;
import core.exception.SerializingException;
import core.misc.serial.DeserializingStream;
import core.misc.serial.SerializingStream;
import core.monitor.CalculatorListener;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 *
 * @author lars
 */
public abstract class Composite extends Component implements ComponentCollection
{
	private final Map<String,Component> components;

	protected Composite(ComponentCollection parent,String name) {
		super(parent,name);
		this.components = new LinkedHashMap<String, Component>();
	}

	@SuppressWarnings("unchecked")
	protected Composite(DeserializingStream in) throws IOException, DeserializingException, InstantiationException
	{
		super(in);

		components = in.readObject(LinkedHashMap.class, String.class);
	}

	/**
	 * Takes an arbitrary {@link Component} name (relative to this {@link Composite})
	 * and returns the corresponding {@link Component} instance.
	 * @param name a {@link #NAME_SEPARATOR}-separated relative full name
	 * @return a {@link Component} object {@code c} which holds the following property:
	 * <pre>c.{@link #getName() getName()}.equals(this.{@link #getName() getName()} + {@link #NAME_SEPARATOR NAME_SEPARATOR} + name)</pre>
	 */
	public final Component getComponent(String name) {
		int pos = name.indexOf(NAME_SEPARATOR);
		if (pos != -1) {
			Component first = components.get(name.substring(0,pos));
			if (first != null && first instanceof Composite)
				return ((Composite)first).getComponent(name.substring(pos+1));
			else
				return null;
		}
		return components.get(name);
	}

	@Override
	public final int size() {
		return components.size();
	}
	
	@Override
	public final Map<String,Component> getComponents() {
		return Collections.unmodifiableMap(components);
	}

	@Override
	public final Component addComponent(Component component) {
		String name = component.getShortName();

		assert component.parent == null : "component with name '"+name+"' already in use in another collection";
		assert isInConstructMode();
		assert !components.containsKey(name) : "name '"+name+"' already in use";

		components.put(name,component);

		return component;
	}

	/**
	 * Removes all {@link Component}s from this {@link Composite}. All sub-classes
	 * which need further operations in {@link Component#cleanup()} must ensure
	 * to call {@code super.cleanup()}, otherwise arbitrary behaviour may be
	 * produced.
	 */
	@Override
	protected void cleanup() {
		components.clear();
		super.cleanup();
	}

	/**
	 * Adds a {@link CalculatorListener} to this object, propagating it down
	 * to all inner {@link Component}s. Note that this object itself doesn't
	 * store these listeners.
	 * @param listener the listener to add
	 */
	@Override
	protected final void addCalculatorListener(CalculatorListener listener) {
		for (Component c : components.values())
			c.addCalculatorListener(listener);
	}

	/**
	 * Removes a {@link CalculatorListener} from this object, propagating it down
	 * to all inner {@link Component}s. Note that this object itself doesn't
	 * store these listeners.
	 * @param listener the listener to remove
	 */
	@Override
	protected final void removeCalculatorListener(CalculatorListener listener) {
		for (Component c : components.values())
			c.removeCalculatorListener(listener);
	}

	@Override public void serialize(SerializingStream out) throws IOException, SerializingException
	{
		super.serialize(out);

		out.writeObject(components, false, false);
	}
}
