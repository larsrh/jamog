
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

import java.awt.Rectangle;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 *
 * @author sylvester
 */
public class SPHashMap {

	private HashMap<String, List<LinkageSegment>> map = new HashMap<String, List<LinkageSegment>>();
	private int stepSize;
	private int segmentCount = 0;

	public SPHashMap(int stepSize) {
		this.stepSize = stepSize;
	}

	public void put(LinkageSegment segment) {
		String[] affectedTiles = this.calculateAffectedTiles(segment.getLine());
		for (String tileName : affectedTiles) {
			List<LinkageSegment> tile = this.map.get(tileName);
			if (tile == null) {
				tile = new LinkedList<LinkageSegment>();
				this.map.put(tileName, tile);
			}
			tile.add(segment);
		}
		if (affectedTiles.length > 0)
			this.segmentCount++;
	}

	private String[] calculateAffectedTiles(Line2D line) {
		Set<String> affectedTiles = new HashSet<String>();
		Point2D p1 = line.getP1();
		Point2D p2 = line.getP2();
		double dX;
		double dY;

		dX = p2.getX() - p1.getX();
		dY = p2.getY() - p1.getY();

		if (java.lang.Math.abs(dY) <= java.lang.Math.abs(dX)) {
			// Ensure line is running from lower to higher x-values.
			if (line.getX1() > line.getX2()) {
				p1 = line.getP2();
				p2 = line.getP1();
				dX = p2.getX() - p1.getX();
				dY = p2.getY() - p1.getY();
			}
			double yFactor = dY / dX;
			int xHashPart = (int) java.lang.Math.floor(p1.getX() / this.stepSize);
			double yValue = p1.getY();
			int yHashPart = (int) java.lang.Math.floor(p1.getY() / this.stepSize);
			affectedTiles.add(String.valueOf(xHashPart) + "/" + String.valueOf(yHashPart));
			xHashPart++;
			yValue += yFactor * this.stepSize;
			yHashPart = (int) java.lang.Math.floor(yValue / this.stepSize);
			while (xHashPart * this.stepSize <= p2.getX()) {
				affectedTiles.add(String.valueOf(xHashPart - 1) + "/" + String.valueOf(yHashPart));
				affectedTiles.add(String.valueOf(xHashPart) + "/" + String.valueOf(yHashPart));
				xHashPart++;
				yValue += this.stepSize * yFactor;
				yHashPart = (int) java.lang.Math.floor(yValue / this.stepSize);
			}
		} else {
			// Ensure line is running from lower to higher y-values.
			if (line.getY1() > line.getY2()) {
				p1 = line.getP2();
				p2 = line.getP1();
				dX = p2.getX() - p1.getX();
				dY = p2.getY() - p1.getY();
			}
			double xFactor = dX / dY;
			double xValue = p1.getX();
			int xHashPart = (int) java.lang.Math.floor(p1.getX() / this.stepSize);
			int yHashPart = (int) java.lang.Math.floor(p1.getY() / this.stepSize);
			affectedTiles.add(String.valueOf(xHashPart) + "/" + String.valueOf(yHashPart));
			xValue += xFactor * this.stepSize;
			xHashPart = (int) java.lang.Math.floor(xValue / this.stepSize);
			yHashPart++;
			while (yHashPart * this.stepSize <= p2.getY()) {
				affectedTiles.add(String.valueOf(xHashPart - 1) + "/" + String.valueOf(yHashPart));
				affectedTiles.add(String.valueOf(xHashPart) + "/" + String.valueOf(yHashPart));
				xValue += this.stepSize * xFactor;
				xHashPart = (int) java.lang.Math.floor(xValue / this.stepSize);
				yHashPart++;
			}
		}

		return affectedTiles.toArray(new String[affectedTiles.size()]);
	}

	public LinkageSegment[] get(Point2D point, int uncertanity) {
		return this.get(new Rectangle((int) (point.getX() - uncertanity / 2), (int) (point.getY() - uncertanity / 2), uncertanity, uncertanity));
	}

	public LinkageSegment[] get(Rectangle rect) {
		int startX = (int) java.lang.Math.floor(rect.getMinX() / this.stepSize);
		int startY = (int) java.lang.Math.floor(rect.getMinY() / this.stepSize);
		int endX = (int) java.lang.Math.floor(rect.getMaxX() / this.stepSize);
		int endY = (int) java.lang.Math.floor(rect.getMaxY() / this.stepSize);
		Set<LinkageSegment> segments = new HashSet<LinkageSegment>();

		for (int i = startX; i <= endX; i++)
			for (int j = startY; j <= endY; j++) {
				List<LinkageSegment> currentSegments = this.map.get(String.valueOf(i) + "/" + String.valueOf(j));
				if (currentSegments != null)
					for (LinkageSegment segment : currentSegments)
						if (segment.getLine().intersects(rect))
							segments.add(segment);
			}

		return segments.toArray(new LinkageSegment[segments.size()]);
	}

	public void remove(LinkageSegment segment) {
		// TODO: Build a more efficient way to remove signals by saving the
		// key strings of the segments as values in a
		// Map<Signalsegment, String>. That way calculateAffectedTiles is only
		// called once (in put()).
		String[] affectedTiles = this.calculateAffectedTiles(segment.getLine());
		for (String tileName : affectedTiles) {
			List<LinkageSegment> tile = this.map.get(tileName);
			if (tile != null)
				tile.remove(segment);
		}
		if (affectedTiles.length > 0)
			this.segmentCount--;
	}

	public int getSegmentCount() {
		return this.segmentCount;
	}

}
