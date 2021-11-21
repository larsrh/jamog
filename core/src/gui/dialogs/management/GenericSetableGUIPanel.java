/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * GenericSetableGUIPanel.java
 *
 * Created on Nov 18, 2009, 10:42:28 AM
 */
package gui.dialogs.management;

import core.misc.setable.Setable;
import javax.swing.DefaultComboBoxModel;

/**
 *
 * @author sylvester
 */
public class GenericSetableGUIPanel extends javax.swing.JPanel {

	private javax.swing.JComboBox bitsPerOffsetCombo;
	private javax.swing.JLabel bitsPerOffsetLabel;
	private javax.swing.JComboBox bitsPerRowCombo;
	private javax.swing.JLabel bitsPerRowLabel;
	private javax.swing.JScrollPane setableTableScroll;
	private GenericSetableGUITable setableTable;

	public GenericSetableGUIPanel(Setable setable, boolean editable) {
		initComponents();
		this.setSetable(setable);
		this.setEditable(editable);
	}

	private void initComponents() {

		setableTableScroll = new javax.swing.JScrollPane();
		this.setableTable = new GenericSetableGUITable(null, false);
		bitsPerOffsetCombo = new javax.swing.JComboBox();
		bitsPerRowCombo = new javax.swing.JComboBox();
		bitsPerOffsetLabel = new javax.swing.JLabel();
		bitsPerRowLabel = new javax.swing.JLabel();

		this.setableTableScroll.setViewportView(this.setableTable);

		this.bitsPerOffsetCombo.setEditable(true);
		bitsPerOffsetCombo.addActionListener(new java.awt.event.ActionListener() {

			public void actionPerformed(java.awt.event.ActionEvent evt) {
				bitsPerOffsetComboActionPerformed(evt);
			}
		});

		this.bitsPerRowCombo.setEditable(true);
		bitsPerRowCombo.addActionListener(new java.awt.event.ActionListener() {

			public void actionPerformed(java.awt.event.ActionEvent evt) {
				bitsPerRowComboActionPerformed(evt);
			}
		});

		bitsPerOffsetLabel.setText("Bits per offset:");

		bitsPerRowLabel.setText("Bits per row:");

		javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
		this.setLayout(layout);
		layout.setHorizontalGroup(
				layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(layout.createSequentialGroup().addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(setableTableScroll, javax.swing.GroupLayout.DEFAULT_SIZE, 775, Short.MAX_VALUE).addGroup(layout.createSequentialGroup().addComponent(bitsPerRowLabel).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(bitsPerRowCombo, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE).addGap(18, 18, 18).addComponent(bitsPerOffsetLabel).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(bitsPerOffsetCombo, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)))));
		layout.setVerticalGroup(
				layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(layout.createSequentialGroup().addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(bitsPerRowLabel).addComponent(bitsPerRowCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE).addComponent(bitsPerOffsetLabel).addComponent(bitsPerOffsetCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(setableTableScroll, javax.swing.GroupLayout.DEFAULT_SIZE, 421, Short.MAX_VALUE)));
	}

	private void bitsPerRowComboActionPerformed(java.awt.event.ActionEvent evt) {
		int value = 1;
		try {
			value = Integer.parseInt((String) this.bitsPerRowCombo.getSelectedItem());
		} catch (Exception exc) {
			value = 1;
		}
		this.setableTable.setBitsPerRow(value);
	}

	private void bitsPerOffsetComboActionPerformed(java.awt.event.ActionEvent evt) {
		int value = 1;
		try {
			value = Integer.parseInt((String) this.bitsPerOffsetCombo.getSelectedItem());
		} catch (Exception exc) {
			value = 1;
		}
		this.setableTable.setBitsPerOffset(value);
	}

	public Setable getSetable() {
		return this.setableTable.getSetable();
	}

	public void setSetable(Setable setable) {

		DefaultComboBoxModel bprModel = new DefaultComboBoxModel();
		DefaultComboBoxModel bpoModel = new DefaultComboBoxModel();

		if (setable != null)
			for (int i = 1; i <= setable.getSetableCount(); i *= 2) {
				bprModel.addElement(String.valueOf(i));
				bpoModel.addElement(String.valueOf(i));
			}

		bprModel.setSelectedItem(this.setableTable.getBitsPerRow());
		bpoModel.setSelectedItem(this.setableTable.getBitsPerOffset());

		this.bitsPerRowCombo.setModel(bprModel);
		this.bitsPerOffsetCombo.setModel(bpoModel);

		this.setableTable.setSetable(setable);
	}

	public boolean isEditable() {
		return this.setableTable.isEditable();
	}

	public void setEditable(boolean editable) {
		this.setableTable.setEditable(editable);
	}
}
