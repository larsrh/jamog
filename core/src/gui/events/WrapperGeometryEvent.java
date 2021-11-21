/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gui.events;

import gui.circuit.ComponentWrapper;
import gui.circuit.management.CircuitManager;
import java.awt.Rectangle;

/**
 *
 * @author sylvester
 */
public class WrapperGeometryEvent extends WrapperEvent {

	private Rectangle affectedSpace;

	public WrapperGeometryEvent(CircuitManager manager, ComponentWrapper wrapper, Rectangle affectedSpace) {
		super(manager, wrapper, CircuitModificationEvent.TYPE_GENERIC);
		this.affectedSpace = affectedSpace;
	}

	public Rectangle getAffectedSpace() {
		return affectedSpace;
	}

}
