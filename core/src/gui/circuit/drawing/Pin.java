
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
import java.awt.geom.Point2D;

public final class Pin extends AbstractPin {

	private Point realPinDirectionPoint = new Point();
	private Point pinDirectionPoint;
	private Connector connector = null;

	public Pin(Point point, Point pinDirectionPoint, String name) {
		super(point, name);
		this.pinDirectionPoint = pinDirectionPoint;
	}

	public Point getPinDirectionPoint() {
		return pinDirectionPoint;
	}

	public void setPinDirectionPoint(Point pinDirectionPoint) {
		this.pinDirectionPoint.setLocation(pinDirectionPoint);
	}

	public void setPinDirectionPoint2D(Point2D pinDirectionPoint) {
		this.pinDirectionPoint.setLocation(pinDirectionPoint);
	}

	public Point getRealPinDirectionPoint() {
		return realPinDirectionPoint;
	}

	public void setRealPinDirectionPoint(Point realPinDirectionPoint) {
		this.realPinDirectionPoint.setLocation(realPinDirectionPoint);
	}

	public void setRealPinDirectionPoint2D(Point2D realPinDirectionPoint) {
		this.realPinDirectionPoint.setLocation(realPinDirectionPoint);
	}

	public boolean hasConnector() {
		return this.connector != null;
	}

	public Connector getConnector() {
		return this.connector;
	}

	public void setConnector(Connector connector) {
		this.connector = connector;
	}

	@Override
	public boolean isConnected() {
		return this.connector != null && this.connector.isCompletelyConnected();
	}

	@Override
	public boolean isPartlyConnected() {
		return this.connector != null && !this.connector.isDisconnected();
	}

}
