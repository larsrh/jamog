
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
import gui.circuit.LinkageGroup;
import gui.util.Utilities;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Point;
import java.awt.Stroke;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;

/**
 *
 * @author sylvester
 */
public class LinkageSegment {

	public static enum Ending {

		NONE,
		CONNECTION,
		ATTACHED,
		CLOCK,
		OUT
	}

	private LinkageGroup group;
	private Line2D line;
	private Ending ending1 = Ending.NONE;
	private Ending ending2 = Ending.NONE;

	public LinkageSegment(LinkageGroup group, Line2D line) {
		this(group, line.getP1(), line.getP2());
	}

	public LinkageSegment(LinkageGroup group, Point2D point1, Point2D point2) {
		this.group = group;
		this.line = new Line2D.Double(point1, point2);
	}

	public LinkageSegment(LinkageGroup group, Point2D point1, Point2D point2, Ending ending1, Ending ending2) {
		this(group, point1, point2);
		this.ending1 = ending1;
		this.ending2 = ending2;
	}

	public Point getCenter() {
		int x = (int) (this.line.getX1() + (this.line.getX2() - this.line.getX1()) / 2);
		int y = (int) (this.line.getY1() + (this.line.getY2() - this.line.getY1()) / 2);
		return new Point(x, y);
	}

	public Line2D getLine() {
		return (Line2D) this.line.clone();
	}

	public LinkageGroup getGroup() {
		return this.group;
	}

	public void paintSegment(Graphics2D graphics, Point offset) {
		Stroke oldStroke = graphics.getStroke();
		Paint oldPaint = graphics.getPaint();

		if (this.getGroup().isDirty()) {
			graphics.setPaint(Properties.getProfile().getSignalHighlightColor());
			graphics.setStroke(Properties.getProfile().getDefaultStroke().changeWidth(2));
		} else {
			graphics.setPaint(Properties.getProfile().getSignalColor());
			graphics.setStroke(Properties.getProfile().getDefaultStroke());
		}

		Point point1 = new Point((int) this.line.getP1().getX(), (int) this.line.getP1().getY());
		Point point2 = new Point((int) this.line.getP2().getX(), (int) this.line.getP2().getY());
		point1.translate(offset.x, offset.y);
		point2.translate(offset.x, offset.y);

		graphics.draw(new Line2D.Double(point1, point2));

		if (Properties.getProfile().isDrawSignalValues()) {
			int x, y;
			if (point1.x < point2.x)
				x = (point2.x + point1.x) / 2;
			else
				x = (point1.x + point2.x) / 2;
			if (point1.y < point2.y)
				y = (point2.y + point1.y) / 2;
			else
				y = (point1.y + point2.y) / 2;
			Font oldFont = graphics.getFont();
			graphics.setFont(oldFont.deriveFont(8));
			graphics.drawString(this.group.getSignalValues(), x, y);
			graphics.setFont(oldFont);
		}

		graphics.setStroke(Properties.getProfile().getDefaultStroke());
		this.paintEnding(graphics, point1, ending1);
		this.paintEnding(graphics, point2, ending2);

		graphics.setPaint(oldPaint);
		graphics.setStroke(oldStroke);
	}

	private void paintEnding(Graphics2D graphics, Point point, Ending ending) {
		if (ending == Ending.CONNECTION) {
			graphics.fillOval(point.x - 3, point.y - 3, 6, 6);
			graphics.drawOval(point.x - 3, point.y - 3, 6, 6);
		}
		if (ending == Ending.ATTACHED || ending == Ending.OUT || ending == Ending.CLOCK) {
			graphics.setPaint(Properties.getProfile().getBgColor());
			graphics.fillOval(point.x - 3, point.y - 3, 6, 6);
			graphics.setPaint(Properties.getProfile().getStrokeColor());
			graphics.drawOval(point.x - 3, point.y - 3, 6, 6);
			if (ending == Ending.CLOCK)
				graphics.drawImage(
						Utilities.getImage("signalClock.png", false),
						point.x + 3,
						point.y + 3,
						Utilities.imageObserver);
			if (ending == Ending.OUT)
				graphics.drawImage(
						Utilities.getImage("signalOutside.png", false),
						point.x + 3,
						point.y + 3,
						Utilities.imageObserver);
		}
	}

}
