
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

import gui.circuit.management.Connector;
import java.awt.Point;

public final class SingleBitPin extends AbstractPin {

	private final int lineIndex;
	private final int bitIndex;
	private final Connector connector;

	public SingleBitPin(Point point, Connector connector, int lineIndex, int bitIndex) {
		super(point, connector.getName() + " (Line: " + lineIndex + "; Bit: " + bitIndex + ")");
		this.lineIndex = lineIndex;
		this.bitIndex = bitIndex;
		this.connector = connector;
	}

	public SingleBitPin(Point point, Point realPoint, Connector connector, int lineIndex, int bitIndex) {
		this(point, connector, lineIndex, bitIndex);
		this.setRealPoint(realPoint);
	}

	public Connector getConnector() {
		return this.connector;
	}

	public int getBitIndex() {
		return this.bitIndex;
	}

	public int getLineIndex() {
		return this.lineIndex;
	}

	@Override
	public final boolean isConnected() {
		return this.connector.isConnected(this.lineIndex, this.bitIndex);
	}

	@Override
	public boolean isPartlyConnected() {
		return this.isConnected();
	}
}
