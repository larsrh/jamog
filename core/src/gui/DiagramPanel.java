
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

package gui;

import gui.circuit.drawing.Diagram;
import gui.util.GuiColor;
import java.awt.BasicStroke;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import javax.swing.JPanel;

/**
 *
 * @author sylvester
 */
public class DiagramPanel extends JPanel {
	private Diagram diagram;

	public DiagramPanel() {
		super();
	}

	public DiagramPanel(Diagram diagram) {
		super();
		this.diagram = diagram;
	}

	@Override
	protected void paintComponent(Graphics g) {
		Graphics2D graphics = (Graphics2D) g;

		super.paintComponent(g);

		graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		graphics.setStroke(new BasicStroke(2));
		if (this.diagram == null)
			graphics.drawString("No diagram supplied", (this.getWidth() - graphics.getFontMetrics().stringWidth("No diagram supplied")) / 2, this.getHeight() / 2);
		else {
			AffineTransform oldTransform = graphics.getTransform();
			double scale = this.getHeight() / (this.diagram.getBounds().getMaxY() - this.diagram.getBounds().getMinY());
			if (scale > this.getWidth() / (this.diagram.getBounds().getMaxX() - this.diagram.getBounds().getMinX()))
				scale = this.getWidth() / (this.diagram.getBounds().getMaxX() - this.diagram.getBounds().getMinX());
			if (scale > 1)
				scale = 1;
			/* This transform scales the diagram to fit in the available place
			 (if neccessary) and centers it if there is space
			 */
			graphics.transform(new AffineTransform(scale, 0, 0, scale, (this.getWidth() - (this.diagram.getBounds().getMaxX() - this.diagram.getBounds().getMinX()) * scale) / 2, (this.getHeight() - (this.diagram.getBounds().getMaxY() - this.diagram.getBounds().getMinY()) * scale) / 2));
			graphics.setPaint(new GuiColor(0));
			graphics.setClip(0, 0, this.getWidth(), this.getHeight());
			graphics.draw(this.diagram.getPath());
			graphics.setTransform(oldTransform);
		}
	}

	public Diagram getDiagram() {
		return diagram;
	}

	public void setDiagram(Diagram diagram) {
		this.diagram = diagram;
	}


}
