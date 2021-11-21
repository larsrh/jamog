
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
/**
 * TODO:
 * List of things that should be possible to set here. Attention: Many of these
 * require non-minor code changes.
 *
 * - pin size
 * - all colors
 * - all strokes
 * - pin count from which on the advanced pinselection gui should be used
 * - extension pathes to automatically include on startup
 * - default size of diagrams
 *
 */
package gui.dialogs;

import gui.util.Properties;
import java.awt.CardLayout;
import java.beans.PropertyVetoException;
import java.io.File;
import java.util.List;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 *
 * @author sylvester
 */
public class SettingsDialog extends javax.swing.JDialog {

	private static enum Views {

		TABS,
		EXTENSIONSFILECHOOSER
	}

	/** Creates new form SettingsDialog */
	public SettingsDialog(java.awt.Frame parent, boolean modal) {
		super(parent, modal);
		initComponents();
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

        mainPanel = new javax.swing.JPanel();
        TabbedPane = new javax.swing.JTabbedPane();
        ExtensionsPanel = new javax.swing.JPanel();
        ExtensionsListScroll = new javax.swing.JScrollPane();
        extensionSourcesMirror = new javax.swing.DefaultListModel();
        ExtensionsList = new javax.swing.JList(extensionSourcesMirror);
        RemoveExtensionButton = new javax.swing.JButton();
        AddExtensionButton = new javax.swing.JButton();
        extensionsFileChooser = new javax.swing.JFileChooser();
        CloseButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Settings");
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosed(java.awt.event.WindowEvent evt) {
                writeExtensionSources(evt);
            }
        });
        addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentShown(java.awt.event.ComponentEvent evt) {
                readExtensionSources(evt);
            }
        });

        mainPanel.setLayout(new java.awt.CardLayout());

        ExtensionsListScroll.setViewportView(ExtensionsList);

        RemoveExtensionButton.setText("Remove extension");
        RemoveExtensionButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                RemoveExtensionButtonActionPerformed(evt);
            }
        });

        AddExtensionButton.setText("Add extension");
        AddExtensionButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                AddExtensionButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout ExtensionsPanelLayout = new javax.swing.GroupLayout(ExtensionsPanel);
        ExtensionsPanel.setLayout(ExtensionsPanelLayout);
        ExtensionsPanelLayout.setHorizontalGroup(
            ExtensionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, ExtensionsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(ExtensionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(ExtensionsListScroll, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 723, Short.MAX_VALUE)
                    .addGroup(ExtensionsPanelLayout.createSequentialGroup()
                        .addComponent(RemoveExtensionButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(AddExtensionButton)))
                .addContainerGap())
        );
        ExtensionsPanelLayout.setVerticalGroup(
            ExtensionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, ExtensionsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(ExtensionsListScroll, javax.swing.GroupLayout.DEFAULT_SIZE, 322, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(ExtensionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(AddExtensionButton)
                    .addComponent(RemoveExtensionButton))
                .addContainerGap())
        );

        TabbedPane.addTab("Extensions", ExtensionsPanel);

        mainPanel.add(TabbedPane, "tabbedPaneCard");

        extensionsFileChooser.setAcceptAllFileFilterUsed(false);
        extensionsFileChooser.setApproveButtonText("Load extension");
        extensionsFileChooser.setDialogType(javax.swing.JFileChooser.CUSTOM_DIALOG);
        extensionsFileChooser.setFileFilter(new FileNameExtensionFilter("Jamog extensions (*.jar)", "jar"));
        extensionsFileChooser.setFileSelectionMode(javax.swing.JFileChooser.FILES_AND_DIRECTORIES);
        extensionsFileChooser.setMultiSelectionEnabled(true);
        extensionsFileChooser.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loadExtensionFired(evt);
            }
        });
        mainPanel.add(extensionsFileChooser, "extensionsFileChooserCard");

        CloseButton.setText("Close settings");
        CloseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                CloseButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(mainPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 751, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(623, 623, 623)
                        .addComponent(CloseButton, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(mainPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 412, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(CloseButton, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

	private void AddExtensionButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_AddExtensionButtonActionPerformed
		this.switchView(Views.EXTENSIONSFILECHOOSER);
	}//GEN-LAST:event_AddExtensionButtonActionPerformed

	private void RemoveExtensionButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_RemoveExtensionButtonActionPerformed
		if (this.ExtensionsList.getSelectedValue() != null)
			for (Object file : this.ExtensionsList.getSelectedValues())
				this.extensionSourcesMirror.removeElement(file);
	}//GEN-LAST:event_RemoveExtensionButtonActionPerformed

	private void CloseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_CloseButtonActionPerformed
		this.dispose();
	}//GEN-LAST:event_CloseButtonActionPerformed

	private void readExtensionSources(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_readExtensionSources
		this.extensionSourcesMirror.clear();
		File[] sources = Properties.getProfile().getExtensionSources();
		for (File location : sources)
			this.extensionSourcesMirror.addElement(location);
	}//GEN-LAST:event_readExtensionSources

	private void writeExtensionSources(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_writeExtensionSources
		try {
			Object[] oa = this.extensionSourcesMirror.toArray();
			File[] fa = new File[oa.length];
			System.arraycopy(oa, 0, fa, 0, oa.length);
			Properties.getProfile().setExtensionSources(fa);
		} catch (PropertyVetoException ex) {
			// Do nothing
		}
	}//GEN-LAST:event_writeExtensionSources

	private void loadExtensionFired(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_loadExtensionFired
		if (evt.getActionCommand().equals(JFileChooser.APPROVE_SELECTION)) {
		for (File file : extensionsFileChooser.getSelectedFiles())
			if (!this.extensionSourcesMirror.contains(file))
				this.extensionSourcesMirror.addElement(file);
		}
		this.switchView(Views.TABS);
	}//GEN-LAST:event_loadExtensionFired
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton AddExtensionButton;
    private javax.swing.JButton CloseButton;
    private javax.swing.DefaultListModel extensionSourcesMirror;
    private javax.swing.JList ExtensionsList;
    private javax.swing.JScrollPane ExtensionsListScroll;
    private javax.swing.JPanel ExtensionsPanel;
    private javax.swing.JButton RemoveExtensionButton;
    private javax.swing.JTabbedPane TabbedPane;
    private javax.swing.JFileChooser extensionsFileChooser;
    private javax.swing.JPanel mainPanel;
    // End of variables declaration//GEN-END:variables

	private void switchView(Views view) {
		switch (view) {
			case TABS:
				((CardLayout) this.mainPanel.getLayout()).show(this.mainPanel, "tabbedPaneCard");
				this.setTitle("Settings");
				break;
			case EXTENSIONSFILECHOOSER:
				((CardLayout) this.mainPanel.getLayout()).show(this.mainPanel, "extensionsFileChooserCard");
				this.setTitle("Choose extension file to load");
				break;
		}
	}
}
