
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
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.geom.GeneralPath;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;

/**
 *
 * @author sylvester
 */
public abstract class AbstractPin {

	protected Point realPoint = new Point();
	protected Point point;
	protected final String name;

	public AbstractPin(Point point, String name) {
		this.point = point;
		this.name = name;
	}

	public final Point getPoint() {
		return this.point;
	}

	public final Point getRealPoint() {
		return this.realPoint;
	}

	public final void setPoint(Point point) {
		this.point.setLocation(point);
	}

	public final void setPoint2D(Point2D point) {
		this.point.setLocation(point);
	}

	public final void setRealPoint(Point point) {
		this.setRealPoint2D(point);
	}

	public final void setRealPoint2D(Point2D point) {
		this.realPoint.setLocation(point);
	}

	public final String getName() {
		return this.name;
	}

	public abstract boolean isConnected();

	public abstract boolean isPartlyConnected();

	public boolean hit(Point point) {
		Rectangle uncertanityRect = new Rectangle(
				realPoint.x - Properties.getProfile().getPinSelectionHaziness() / 2,
				realPoint.y - Properties.getProfile().getPinSelectionHaziness() / 2,
				Properties.getProfile().getPinSelectionHaziness(),
				Properties.getProfile().getPinSelectionHaziness());
		return uncertanityRect.contains(point);
	}

	public void paintPin(Graphics2D graphics, Point offset, boolean selected) {
		Stroke oldStroke = graphics.getStroke();
		Paint oldPaint = graphics.getPaint();

		graphics.setStroke(Properties.getProfile().getDefaultStroke());
		Point point = new Point(this.realPoint);
		point.translate(offset.x, offset.y);
		Rectangle rect = new Rectangle(
				point.x - Properties.getProfile().getPinSize() / 2,
				point.y - Properties.getProfile().getPinSize() / 2,
				Properties.getProfile().getPinSize(),
				Properties.getProfile().getPinSize());
		Path2D triangle = new GeneralPath();
		triangle.moveTo(point.x + Properties.getProfile().getPinSize() / 2,
				point.y - Properties.getProfile().getPinSize() / 2);
		triangle.lineTo(point.x + Properties.getProfile().getPinSize() / 2,
				point.y + Properties.getProfile().getPinSize() / 2);
		triangle.lineTo(point.x - Properties.getProfile().getPinSize() / 2,
				point.y + Properties.getProfile().getPinSize() / 2);
		triangle.closePath();
		if (selected) {
			graphics.setPaint(Properties.getProfile().getSelectionBGColor());
			Rectangle selection = new Rectangle(rect);
			selection.grow(-1, -1);
			graphics.draw(selection);
			selection.grow(2, 2);
			graphics.draw(selection);
			graphics.setPaint(Properties.getProfile().getSelectionFGColor());
		} else
			graphics.setPaint(Properties.getProfile().getPinColor());
		if (this.isConnected())
			graphics.fill(rect);
		else {
			graphics.draw(rect);
			if (this.isPartlyConnected())
				graphics.fill(triangle);
		}

		graphics.setStroke(oldStroke);
		graphics.setPaint(oldPaint);
	}

}
