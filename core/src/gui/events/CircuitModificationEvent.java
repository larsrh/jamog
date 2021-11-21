
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
package gui.events;

import gui.circuit.management.CircuitManager;
import java.util.EventObject;

/**
 *
 * @author sylvester
 */
public class CircuitModificationEvent extends EventObject {

	public static interface Listener {

		public void circuitChanged(CircuitModificationEvent evt);
	}

	public static final int TYPE_GENERIC = -1;
	public static final int TYPE_BUILT = 0;

	private CircuitManager circuitManager;
	private int type;

	public CircuitModificationEvent(CircuitManager manager, int type) {
		super(manager);
		this.circuitManager = manager;
	}

	public final CircuitManager getCircuitManager() {
		return circuitManager;
	}

	public final int getType() {
		return type;
	}

	public final boolean isWrapperEvent() {
		return this instanceof WrapperEvent;
	}

	public final boolean isWrapperGeometryEvent() {
		return this instanceof WrapperGeometryEvent;
	}

	public final boolean isLinkageGroupEvent() {
		return this instanceof LinkageGroupEvent;
	}

	public final boolean isLinkageEvent() {
		return this instanceof LinkageEvent;
	}

	@Override
	public String toString() {
		return this.getClass().getName() + "[Manager: " + this.circuitManager + ", Type: " + this.type + "]";
	}
}
