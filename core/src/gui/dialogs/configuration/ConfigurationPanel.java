/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gui.dialogs.configuration;

import gui.dialogs.configuration.ConfigureDialog.ParameterNode;

/**
 *
 * @author sylvester
 */
abstract class ConfigurationPanel extends javax.swing.JPanel {

	static enum ParameterType {

		INPUT,
		OUTPUT,
		PARAMETER
	}

	private String parameterName;
	private Object value;
	protected ConfigureDialog.ParameterNode listeningNode = null;
	private boolean controlsBlocked = true;

	public ConfigurationPanel(ConfigureDialog.ParameterNode listeningNode) {
		this.parameterName = listeningNode.getName();
		this.listeningNode = listeningNode;
	}

	String getParameterName() {
		return this.parameterName;
	}

	Object getValue() {
		return this.listeningNode.getValue();
	}

	void setValue(Object value) {
		this.listeningNode.setValue(value);
	}

	protected abstract void updateValue();

	protected void updateDescendant(int depth) {
		// Do nothing;
	}

	public ParameterNode getListeningNode() {
		return listeningNode;
	}

	protected void blockControls() {
		this.controlsBlocked = true;
	}

	protected void releaseControls() {
		this.controlsBlocked = false;
	}

	protected boolean controlsReleased() {
		return !this.controlsBlocked;
	}

}
