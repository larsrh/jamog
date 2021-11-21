/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gui.events;

import gui.circuit.management.CircuitManager;
import gui.circuit.management.Linkage;

/**
 *
 * @author sylvester
 */
public class LinkageEvent extends CircuitModificationEvent {

	public static final int TYPE_ADDITON = 0;
	public static final int TYPE_REMOVAL = 1;

	private Linkage linkage;

	public LinkageEvent(CircuitManager manager, Linkage linkage, int type) {
		super(manager, type);
		this.linkage = linkage;
	}

	public Linkage getLinkage() {
		return linkage;
	}
}
