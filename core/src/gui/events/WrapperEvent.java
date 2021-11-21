/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gui.events;

import gui.circuit.ComponentWrapper;
import gui.circuit.management.CircuitManager;

/**
 *
 * @author sylvester
 */
public class WrapperEvent extends CircuitModificationEvent {

	public static final int TYPE_ADDITON = 0;
	public static final int TYPE_REMOVAL = 1;
	public static final int TYPE_CONFIGURED = 2;
	public static final int TYPE_MOVEMENT = 3;

	private ComponentWrapper wrapper;

	public WrapperEvent(CircuitManager manager, ComponentWrapper wrapper, int type) {
		super(manager, type);
		this.wrapper = wrapper;
	}

	public ComponentWrapper getWrapper() {
		return wrapper;
	}

}
