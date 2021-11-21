
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

package gui.diagrams;

import gui.circuit.drawing.ComponentDiagram;
import gui.circuit.drawing.Pin;
import java.awt.Point;
import java.awt.geom.GeneralPath;
import java.util.Arrays;
import java.util.Iterator;

/**
 *
 * @author sylvester
 */
public class Rectangle extends ComponentDiagram {

	public static final int TOP = 1;
	public static final int BOTTOM = 2;
	public static final int LEFT = 4;
	public static final int RIGHT = 8;
	public static final int TOP_BOTTOM = 3;
	public static final int LEFT_RIGHT = 12;
	public static final int ALL = 15;

	private Rectangle() {
		this(new Pin[0], 1);
	}

	public Rectangle(float widthToHeightRatio) {
		GeneralPath path = new GeneralPath();
		path.moveTo(0, 0);
		path.lineTo(100 * widthToHeightRatio, 0);
		path.lineTo(100 * widthToHeightRatio, 100);
		path.lineTo(0, 100);
		path.closePath();
		this.setPath(path);
	}

	public Rectangle(Pin[] pins, float widthToHeightRatio) {
		this(widthToHeightRatio);
		this.setPins(pins);
	}

	public void setPins(Pin[] pins) {
		this.clearPins();
		this.addPins(pins);
	}

	public void distributePins(int sidesToUse) {
		if (sidesToUse > Rectangle.ALL)
			sidesToUse = Rectangle.ALL;
		if (sidesToUse < 1)
			sidesToUse = 1;
		
		int[] pinsPerSide = new int[4]; // TOP, BOTTOM, LEFT, RIGHT (-1 = do not use, 0 = in use but empty)
		Arrays.fill(pinsPerSide, -1);
		if (sidesToUse > Rectangle.RIGHT) { // RIGHT = 8
			pinsPerSide[3] = 0;
			sidesToUse -= Rectangle.RIGHT;
		}
		if (sidesToUse > Rectangle.LEFT) { // LEFT = 4
			pinsPerSide[2] = 0;
			sidesToUse -= Rectangle.LEFT;
		}
		if (sidesToUse > Rectangle.BOTTOM) { // BOTTOM = 2
			pinsPerSide[1] = 0;
			sidesToUse -= Rectangle.BOTTOM;
		}
		if (sidesToUse > Rectangle.TOP) { // TOP = 1
			pinsPerSide[0] = 0;
			sidesToUse -= Rectangle.TOP;
		}
		for (int i = this.getPins().size(); i > 0;) {
			if (pinsPerSide[0] >= 0) {
				pinsPerSide[0]++;
				i--;
			}
			if (pinsPerSide[1] >= 0 && (i > 0)) {
				pinsPerSide[1]++;
				i--;
			}
			if (pinsPerSide[2] >= 0 && (i > 0)) {
				pinsPerSide[2]++;
				i--;
			}
			if (pinsPerSide[3] >= 0 && (i > 0)) {
				pinsPerSide[3]++;
				i--;
			}
		}
		int pinIndexOffset;
		int stepSize;
		Iterator<Pin> iterator = this.getPins().iterator();
		Pin pin = null;
		if (pinsPerSide[0] > 0) {
			pinIndexOffset = 0;
			stepSize = 80 / pinsPerSide[0];
			for (int i = 0; i < pinsPerSide[0]; i++) {
				pin = iterator.next();
				pin.setPoint(new Point(10 + i * stepSize, 10));
				pin.setPinDirectionPoint(new Point(10 + i * stepSize, 0));
			}
		}
		if (pinsPerSide[1] > 0) {
			pinIndexOffset = pinsPerSide[0];
			stepSize = 80 / pinsPerSide[1];
			for (int i = 0; i < pinsPerSide[1]; i++) {
				pin = iterator.next();
				pin.setPoint(new Point(10 + i * stepSize, 90));
				pin.setPinDirectionPoint(new Point(10 + i * stepSize, 100));
			}
		}
		if (pinsPerSide[2] > 0) {
			pinIndexOffset = pinsPerSide[0] + pinsPerSide[1];
			stepSize = 80 / pinsPerSide[2];
			for (int i = 0; i < pinsPerSide[2]; i++) {
				pin = iterator.next();
				pin.setPoint(new Point(10, 10 + i * stepSize));
				pin.setPinDirectionPoint(new Point(0, 10 + i * stepSize));
			}
		}
		if (pinsPerSide[3] > 0) {
			pinIndexOffset = pinsPerSide[0] + pinsPerSide[1] + pinsPerSide[2];
			stepSize = 80 / pinsPerSide[3];
			for (int i = 0; i < pinsPerSide[3]; i++) {
				pin = iterator.next();
				pin.setPoint(new Point(90, 10 + i * stepSize));
				pin.setPinDirectionPoint(new Point(100, 10 + i * stepSize));
			}
		}
	}
}
