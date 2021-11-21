
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
package gui.circuit.management;

import gui.circuit.ComponentWrapper;
import gui.circuit.drawing.Pin;
import gui.support.*;
import java.util.Collections;

/**
 *
 * @author sylvester
 */
public class Connector {

	public static enum Type {

		INPUT,
		OUTPUT,
		UNDEFINED
	}

	private ComponentWrapper wrapper = null;
	private Linkage[][] linkages;
	private String name;
	private Pin pin = null;
	private Type type;

	@SuppressWarnings({"unchecked"})
	public Connector(String name, Type type, int lineCount, int bitWidth) {
		this.name = name;
		this.linkages = new Linkage[lineCount][bitWidth];
		this.type = type;
	}

	public Connector(String name, Type type, int lineCount, int[] bitWidths) {
		if (bitWidths.length != lineCount)
			throw new IllegalArgumentException("Must have a bit width specified for each line.");
		this.name = name;
		this.linkages = new Linkage[lineCount][];
		for (int i = 0; i < lineCount; i++)
			this.linkages[i] = new Linkage[bitWidths[i]];
		this.type = type;
	}

	public Pin getPin() {
		return pin;
	}

	public void setPin(Pin pin) {
		if (this.pin == null)
			this.pin = pin;
		else
			throw new UnsupportedOperationException("Pin has already been assigned. Cannot reassign pin.");
	}

	public boolean hasPin() {
		return this.pin != null;
	}

	public String getName() {
		return this.name;
	}

	public ComponentWrapper getWrapper() {
		return wrapper;
	}

	public void setWrapper(ComponentWrapper wrapper) {
		if (this.wrapper == null)
			this.wrapper = wrapper;
		else
			throw new UnsupportedOperationException("Wrapper has already been assigned. Cannot reassign wrapper.");
	}

	public int getLineCount() {
		return this.linkages.length;
	}

	public int getBitWidth(int lineIndex) {
		return this.linkages[lineIndex].length;
	}

	public Type getType() {
		return type;
	}

	public Linkage getLinkage(int lineIndex, int bitIndex) {
		return this.linkages[lineIndex][bitIndex];
	}

	public Linkage[][] getLinkages() {
		return linkages;
	}

	public void setLinkage(Linkage newPath, int lineIndex, int bitIndex) {
		this.linkages[lineIndex][bitIndex] = newPath;
	}

	public void breakAllConnections() {
		for (int i = 0; i < this.linkages.length; i++)
			for (int j = 0; j < this.linkages[i].length; j++)
				if (this.isConnected(i, j)) {
					Linkage linkage = this.linkages[i][j];
					linkage.getManager().unlinkFromLinkage(linkage, Collections.singleton(new Linkage.Endpoint(this, i, j)));
				}
	}

	public void recalculateAllPathes(double eventId) {
		for (int i = 0; i < this.linkages.length; i++)
			for (int j = 0; j < this.linkages[i].length; j++)
				if (this.linkages[i][j] != null)
					this.linkages[i][j].getManager().recalculateLinkage(this.linkages[i][j], eventId);
	}

	public boolean isCompletelyConnected() {
		for (int i = 0; i < this.linkages.length; i++)
			if (!this.isLineCompletelyConnected(i))
				return false;
		return true;
	}

	public boolean isDisconnected() {
		for (int i = 0; i < this.linkages.length; i++)
			if (!this.isLineDisconnected(i))
				return false;
		return true;
	}

	public boolean isLineCompletelyConnected(int lineIndex) {
		for (int i = 0; i < this.linkages[lineIndex].length; i++)
			if (!this.isConnected(lineIndex, i))
				return false;
		return true;
	}

	public boolean isLineDisconnected(int lineIndex) {
		for (int i = 0; i < this.linkages[lineIndex].length; i++)
			if (this.isConnected(lineIndex, i))
				return false;
		return true;
	}

	public boolean isConnected(int lineIndex, int bitIndex) {
		return this.linkages[lineIndex][bitIndex] != null;
	}

	@Override
	public String toString() {
		return this.getName();
	}

}
