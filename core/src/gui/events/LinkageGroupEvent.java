/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gui.events;

import gui.circuit.LinkageGroup;
import gui.circuit.management.CircuitManager;

/**
 *
 * @author sylvester
 */
public class LinkageGroupEvent extends CircuitModificationEvent {

	public static final int TYPE_ADDITON = 0;
	public static final int TYPE_REMOVAL = 1;

	private LinkageGroup group;

	public LinkageGroupEvent(CircuitManager manager, LinkageGroup group, int type) {
		super(manager, type);
		this.group = group;
	}

	public LinkageGroup getGroup() {
		return group;
	}

}
