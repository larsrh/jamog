
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

/*
 * ManageDialog.java
 *
 * Created on Oct 13, 2009, 3:07:27 PM
 */
package gui.dialogs.management;

import core.misc.setable.GroupSetable;
import core.misc.setable.Setable;
import gui.util.Properties;
import javax.swing.JComponent;

/**
 *
 * @author sylvester
 */
public class ManageDialog extends javax.swing.JDialog {

	public static ManageDialog createWithGenericGUI(Setable setable, boolean editable) {
		if (setable instanceof GroupSetable) {
			return new ManageDialog(new GenericSetableGroupGUIPanel((GroupSetable) setable, editable));
		} else {
			return new ManageDialog(new GenericSetableGUIPanel(setable, editable));
		}
	}

	public static ManageDialog createWithCustomGUI(JComponent customGUI) {
		return new ManageDialog(customGUI);
	}

	/**
	 * Creates new form ManageDialog
	 * 
	 * @param parent
	 * @param modal
	 * @deprecated For GUI Builder only
	 */
	public ManageDialog(java.awt.Frame parent, boolean modal) {
		super(parent, modal);
		initComponents();
	}

	private ManageDialog(JComponent managingComponent) {
		super(Properties.getProfile().getMainWindow(), true);
		this.initComponents();
		this.containerPanel.add(managingComponent, "onlyChild");
		this.setLocationRelativeTo(Properties.getProfile().getMainWindow());
	}

	/** This method is called from within the constructor to
	 * initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is
	 * always regenerated by the Form Editor.
	 */
	@SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        closeButton = new javax.swing.JButton();
        containerPanel = new javax.swing.JPanel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Manage Component");

        closeButton.setText("Close");
        closeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closeButtonActionPerformed(evt);
            }
        });

        containerPanel.setLayout(new java.awt.CardLayout());

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(closeButton, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(containerPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 660, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(containerPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 382, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(closeButton)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

	private void closeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closeButtonActionPerformed
		this.dispose();
	}//GEN-LAST:event_closeButtonActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton closeButton;
    private javax.swing.JPanel containerPanel;
    // End of variables declaration//GEN-END:variables
}
