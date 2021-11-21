
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

import gui.circuit.ComponentWrapper;
import gui.support.*;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author sylvester
 */
public abstract class ComponentGhost implements TransformableDrawable {

	public static final class DefaultGhost extends ComponentGhost {

		public DefaultGhost(ComponentWrapper soul) {
			super(soul);
			this.diagram = new gui.diagrams.Rectangle(1);
		}
	}
	protected ComponentWrapper soul;
	protected Diagram diagram;
	protected Point position;
	protected Dimension dimension;
	protected float rotation;

	public ComponentGhost(ComponentWrapper soul) {
		this.soul = soul;
		this.position = soul.getPosition();
		this.dimension = soul.getDimension();
		this.rotation = soul.getRotation();
	}

	public final ComponentWrapper getSoul() {
		return this.soul;
	}

	@Override
	public Diagram getDiagram() {
		return this.diagram;
	}

	@Override
	public Point getPosition() {
		return new Point(this.position);
	}

	@Override
	public void setPosition(Point position) {
		if (!this.position.equals(position)) {
			this.position.setLocation(position);
			this.applyGeometry();
		}
	}

	@Override
	public Rectangle getBounds() {
		return new Rectangle(this.getDiagram().getTransformedBounds());
	}

	public void setBounds(Rectangle bounds) {
		this.setPosition(bounds.getLocation());
		this.setDimension(bounds.getSize());
	}

	@Override
	public Dimension getDimension() {
		return new Dimension(this.getDiagram().getTransformedBounds().getSize());
	}

	@Override
	public void setDimension(Dimension dimension) {
		if (!this.dimension.equals(dimension)) {
			this.dimension.setSize(dimension);
			this.applyGeometry();
		}
	}

	@Override
	public float getRotation() {
		return this.rotation;
	}

	@Override
	public void setRotation(float degrees) {
		degrees = degrees % 360;
		this.rotation = degrees;
		this.applyGeometry();
	}

	private void applyGeometry() {
		this.getDiagram().transformPath(AffineTransform.getScaleInstance(this.dimension.getWidth() / this.getDiagram().getBounds().getWidth(), this.dimension.getHeight() / this.getDiagram().getBounds().getHeight()), true);
		double rad = this.rotation * (Math.PI / 180);
		this.getDiagram().transformPath(AffineTransform.getRotateInstance(rad, this.getDiagram().getTransformedBounds().getWidth() / 2, this.getDiagram().getTransformedBounds().getHeight() / 2), false);
		this.getDiagram().transformPath(AffineTransform.getTranslateInstance(this.position.getX(), this.position.getY()), false);
	}
}
