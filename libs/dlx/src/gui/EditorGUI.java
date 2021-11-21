/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * EditorGUI.java
 *
 * Created on 18.11.2009, 23:54:06
 */
package gui;

import core.misc.setable.Setable;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author sylvester
 */
public class EditorGUI extends javax.swing.JPanel {

	private static Map<Setable, String> codes = new HashMap<Setable, String>();

	public EditorGUI(Setable instructionMemory) {
		editor = new AssemblerEditor(instructionMemory);

		if (EditorGUI.codes.containsKey(instructionMemory))
			editor.setText(EditorGUI.codes.get(instructionMemory));

		loadButton = new javax.swing.JButton();
		titleLabel = new javax.swing.JLabel();

		loadButton.setText("Load code");

		titleLabel.setFont(titleLabel.getFont().deriveFont(titleLabel.getFont().getStyle() | java.awt.Font.BOLD));
		titleLabel.setText("Jamog Assembler Editor");

		javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
		this.setLayout(layout);
		layout.setHorizontalGroup(
				layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup().addContainerGap().addComponent(titleLabel).addGap(395, 395, 395).addComponent(loadButton)).addComponent(editor, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE));
		layout.setVerticalGroup(
				layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(layout.createSequentialGroup().addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(loadButton).addComponent(titleLabel)).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(editor, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
	}
	// Variables declaration - do not modify
	private javax.swing.JLabel titleLabel;
	private AssemblerEditor editor;
	private javax.swing.JButton loadButton;
	// End of variables declaration
}
