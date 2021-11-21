
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
package gui.util;

import java.awt.BasicStroke;

/**
 *
 * @author sylvester
 */
public class GuiStroke extends BasicStroke{

	public GuiStroke() {
	}

	public GuiStroke(float width) {
		super(width);
	}

	public GuiStroke(float width, int cap, int join) {
		super(width, cap, join);
	}

	public GuiStroke(float width, int cap, int join, float miterlimit) {
		super(width, cap, join, miterlimit);
	}

	public GuiStroke(float width, int cap, int join, float miterlimit, float[] dash, float dash_phase) {
		super(width, cap, join, miterlimit, dash, dash_phase);
	}

	public GuiStroke changeWidth(int width) {
		return new GuiStroke(width, this.getEndCap(), this.getLineJoin(), this.getMiterLimit(), this.getDashArray(), this.getDashPhase());
	}
}
