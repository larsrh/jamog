/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * StringConfiguration.java
 *
 * Created on 07.11.2009, 15:23:33
 */
package gui.dialogs.configuration;

/**
 *
 * @author sylvester
 */
public class StringConfiguration extends ConfigurationPanel {

	/**
	 * Creates new form StringConfiguration
	 *
	 * @deprecated Only for GUI Designer
	 */
	public StringConfiguration() {
		this(null);
	}

	public StringConfiguration(ConfigureDialog.ParameterNode listeningNode) {
		super(listeningNode);
		initComponents();
		this.nameLabel.setText(this.getParameterName());
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

        nameLabel = new javax.swing.JLabel();
        valueField = new javax.swing.JTextField();

        setPreferredSize(new java.awt.Dimension(500, 27));

        nameLabel.setText("jLabel1");

        valueField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                valueFieldActionPerformed(evt);
            }
        });
        valueField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                valueFieldFocusLost(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(nameLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 219, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(valueField, javax.swing.GroupLayout.PREFERRED_SIZE, 257, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(valueField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(nameLabel))
        );
    }// </editor-fold>//GEN-END:initComponents

	private void valueFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_valueFieldActionPerformed
		this.setValue(this.valueField.getText());
	}//GEN-LAST:event_valueFieldActionPerformed

	private void valueFieldFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_valueFieldFocusLost
		this.setValue(this.valueField.getText());
	}//GEN-LAST:event_valueFieldFocusLost
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel nameLabel;
    private javax.swing.JTextField valueField;
    // End of variables declaration//GEN-END:variables

	protected void updateValue() {
		if (this.getValue() instanceof String)
			this.valueField.setText((String) this.getValue());
	}
}
