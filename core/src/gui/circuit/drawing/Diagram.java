
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

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.Rectangle;
import java.awt.geom.GeneralPath;

/**
 *
 * @author sylvester
 */
public abstract class Diagram {

	private Path2D transformedPath = new GeneralPath();
	private Path2D path = new GeneralPath();
	private boolean transformed = false;

	protected void setPath(Path2D path) {
		this.path = path;
		this.reset();
	}

	public Path2D getPath() {
		return (Path2D) this.path.clone();
	}

	public Path2D getTransformedPath() {
		return (Path2D) this.transformedPath.clone();
	}

	public boolean isTransformed() {
		return this.transformed;
	}

	public Rectangle getBounds() {
		return this.path.getBounds();
	}

	public Rectangle getTransformedBounds() {
		return this.transformedPath.getBounds();
	}

	public Point getCenter() {
		int x = (int) this.getBounds().getCenterX();
		int y = (int) this.getBounds().getCenterY();
		return new Point(x, y);
	}

	public Point getTransformedCenter() {
		int x = (int) this.getTransformedBounds().getCenterX();
		int y = (int) this.getTransformedBounds().getCenterY();
		return new Point(x, y);
	}

	public void draw(Graphics2D graphics) {
		// Children may override this method, the default behaviour is to do nothing.
	}

	public void transformPath(AffineTransform transform, boolean reset) {
		if (reset)
			this.reset();
		this.transformedPath.transform(transform);
		this.transformed = true;
	}

	public void reset() {
		this.transformedPath = (Path2D) this.path.clone();
		this.transformed = false;
	}
}
