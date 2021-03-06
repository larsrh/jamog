/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * EnumConfiguration.java
 *
 * Created on 07.11.2009, 15:37:26
 */
package gui.dialogs.configuration;

import core.build.checking.types.SimpleType;
import gui.exception.ParameterCreationException;
import javax.swing.DefaultComboBoxModel;

/**
 *
 * @author sylvester
 */
public class EnumConfiguration extends ConfigurationPanel {

	/**
	 * Creates new form EnumConfiguration
	 * 
	 * @deprecated Only for GUI Designer
	 */
	public EnumConfiguration() {
		this(null);
	}

	public EnumConfiguration(ConfigureDialog.ParameterNode listeningNode) {
		super(listeningNode);
		if (!(listeningNode.getType() instanceof SimpleType));// TODO: Implement assertion here
		this.type = ((SimpleType) listeningNode.getUserObject()).getWrappedClass();
		initComponents();
		this.nameLabel.setText(this.getParameterName());
		if (type != null && type.isEnum())
			this.valueField.setModel(new DefaultComboBoxModel(type.getEnumConstants()));
		this.updateValue();
	}

	/** This method is called from within the constructor to
	 * initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is
	 * always regenerated by the Form Editor.
	 */
	@SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        valueField = new javax.swing.JComboBox();
        nameLabel = new javax.swing.JLabel();

        setPreferredSize(new java.awt.Dimension(500, 27));

        valueField.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                valueFieldItemStateChanged(evt);
            }
        });

        nameLabel.setText("jLabel1");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(nameLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 246, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(valueField, javax.swing.GroupLayout.PREFERRED_SIZE, 230, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(valueField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(nameLabel))
        );
    }// </editor-fold>//GEN-END:initComponents

	private void valueFieldItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_valueFieldItemStateChanged
		if (this.controlsReleased())
			this.setValue(this.valueField.getSelectedIndex());
	}//GEN-LAST:event_valueFieldItemStateChanged
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel nameLabel;
    private javax.swing.JComboBox valueField;
    // End of variables declaration//GEN-END:variables
	private Class type;

	protected void updateValue() {
		if (this.getValue() instanceof Integer) {
			this.blockControls();
			this.valueField.setSelectedItem(this.type.getEnumConstants()[(Integer) this.getValue()]);
			this.releaseControls();
		}
	}

}
