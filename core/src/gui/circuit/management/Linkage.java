
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
package gui.circuit.management;

import core.exception.SerializingException;
import core.misc.serial.SerializingStream;
import core.monitor.SignalListener;
import core.signal.Bit;
import core.signal.Signal;
import core.signal.SignalBit;
import gui.events.CircuitModificationEvent;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author sylvester
 */
public class Linkage {

	public static class Endpoint {

		private Connector connector;
		private int line;
		private int bit;

		Endpoint(Connector connector, int line, int bits) {
			this.connector = connector;
			this.line = line;
			this.bit = bits;
		}

		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof Endpoint))
				return false;
			Endpoint endpoint = (Endpoint) obj;
			return this.connector == endpoint.connector && this.line == endpoint.line && this.bit == endpoint.bit;
		}

		@Override
		public int hashCode() {
			return this.connector.hashCode() + this.line + this.bit;
		}

		/**
		 * @return the connector
		 */
		public Connector getConnector() {
			return connector;
		}

		/**
		 * @return the line
		 */
		public int getLine() {
			return line;
		}

		/**
		 * @return the bit
		 */
		public int getBit() {
			return bit;
		}

	}

	private CircuitManager manager;
	private Set<Endpoint> endpoints = new HashSet<Endpoint>();
	private Set<Connector> connectors = new HashSet<Connector>();
	private Signal signal = new Signal(1); // new Signal(Bit.Z)
	private boolean singleEnded;

	Linkage(CircuitManager manager, Set<Endpoint> endpoints) {
		this.manager = manager;
		this.singleEnded = endpoints.size() == 1;
		for (Endpoint endpoint : endpoints) {
			this.endpoints.add(endpoint);
			this.connectors.add(endpoint.getConnector());
			endpoint.getConnector().setLinkage(this, endpoint.getLine(), endpoint.getBit());
		}
	}

	void destruct() {
		for (Endpoint endpoint : this.endpoints)
			endpoint.getConnector().setLinkage(null, endpoint.getLine(), endpoint.getBit());
	}

	public Signal getSignal() {
		return signal;
	}

	public Set<Endpoint> getEndpoints() {
		return Collections.unmodifiableSet(this.endpoints);
	}

	public Set<Connector> getConnectors() {
		return connectors;
	}

	boolean isSingleEnded() {
		return singleEnded;
	}

	CircuitManager getManager() {
		return manager;
	}

}
