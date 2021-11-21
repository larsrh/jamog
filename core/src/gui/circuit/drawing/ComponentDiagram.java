
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

package gui.circuit.drawing;

import gui.util.Properties;
import gui.support.*;
import java.awt.Point;
import java.awt.geom.AffineTransform;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

/**
 *
 * @author sylvester
 */
public abstract class ComponentDiagram extends Diagram {

	private HashMap<String, Pin> pinMap = new HashMap<String, Pin>();
	private Set<Entry<String, Pin>> pinSet = this.pinMap.entrySet();

	protected final void addPin(Pin pin) {
		this.pinMap.put(pin.getName(), pin);
	}

	protected final void addPins(Pin[] pins) {
		for (Pin pin : pins)
			this.addPin(pin);
	}

	protected final void removePin(Pin pin) {
		this.pinMap.remove(pin.getName());
	}

	protected final void removePin(String name) {
		this.pinMap.remove(name);
	}

	protected final void clearPins() {
		this.pinMap.clear();
	}

	public final int pinCount() {
		return pinMap.size();
	}

	public final Pin getPin(String name) {
		return this.pinMap.get(name);
	}

	public final Pin getPin(Point coordinates) {
		return this.getPin(coordinates, Properties.getProfile().getPinSize());
	}

	public final Pin getPin(Point coordinates, int uncertanity) {
		return this.getPin(coordinates, uncertanity, true);
	}

	public final Pin getPin(Point coordinates, int uncertanity, boolean configuredOnly) {
		java.awt.Rectangle uncertanityRect = new java.awt.Rectangle(coordinates.x - uncertanity, coordinates.y - uncertanity, uncertanity * 2, uncertanity * 2);
		Iterator<Entry<String, Pin>> iterator = this.pinSet.iterator();
		while (iterator.hasNext()) {
			Pin pin = iterator.next().getValue();
			if (uncertanityRect.contains(pin.getRealPoint()) && (!configuredOnly || pin.hasConnector()))
				return pin;
		}
		return null;
	}

	public final Collection<Pin> getPins() {
		return Collections.unmodifiableCollection(this.pinMap.values());
	}

	@Override
	public final void transformPath(AffineTransform transform, boolean reset) {
		super.transformPath(transform, reset);
		for (Pin pin : this.pinMap.values()) {
			pin.setRealPoint2D(transform.transform(pin.getRealPoint(), null));
			pin.setRealPinDirectionPoint2D(transform.transform(pin.getRealPinDirectionPoint(), null));
		}
	}

	@Override
	public final void reset() {
		super.reset();
		for (Pin pin : this.pinMap.values()) {
			pin.setRealPoint(pin.getPoint());
			pin.setRealPinDirectionPoint(pin.getPinDirectionPoint());
		}
	}
}
