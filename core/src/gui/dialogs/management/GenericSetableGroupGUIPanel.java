/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * GenericSetableGroupGUIPanel.java
 *
 * Created on 18.11.2009, 22:16:38
 */
package gui.dialogs.management;

import core.misc.setable.GroupSetable;
import core.misc.setable.Setable;
import java.util.Map.Entry;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;

/**
 *
 * @author sylvester
 */
public class GenericSetableGroupGUIPanel extends javax.swing.JPanel {

	private static class SetableNode extends DefaultMutableTreeNode {

		String name;

		public SetableNode(String name, Setable setable) {
			super(setable, true);
			this.name = name;

			if (setable instanceof GroupSetable)
				for (Entry<String, ? extends Setable> entry : ((GroupSetable) setable).getSetableGroups().entrySet())
					this.add(new SetableNode(entry.getKey(), entry.getValue()));
		}

		@Override
		public String toString() {
			return this.name;
		}

		public Setable getSetable() {
			return (Setable) this.getUserObject();
		}
	}

	/**
	 * Creates new form GenericSetableGroupGUIPanel
	 *
	 * @deprecated Only for GUI-builder
	 */
	public GenericSetableGroupGUIPanel() {
		this(null, false);
	}

	public GenericSetableGroupGUIPanel(GroupSetable groupSetable, boolean editable) {
		this.groupSetable = groupSetable;

		initComponents();

		TreeNode root = new SetableNode("root", groupSetable);
		this.tree.setModel(new DefaultTreeModel(root));

		this.setablePanel = new GenericSetableGUIPanel(null, editable);
		this.splitPane.setRightComponent(this.setablePanel);
	}

	/** This method is called from within the constructor to
	 * initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is
	 * always regenerated by the Form Editor.
	 */
	@SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        splitPane = new javax.swing.JSplitPane();
        treeScroll = new javax.swing.JScrollPane();
        tree = new javax.swing.JTree();

        splitPane.setDividerLocation(300);

        tree.setRootVisible(false);
        tree.addTreeSelectionListener(new javax.swing.event.TreeSelectionListener() {
            public void valueChanged(javax.swing.event.TreeSelectionEvent evt) {
                treeValueChanged(evt);
            }
        });
        treeScroll.setViewportView(tree);

        splitPane.setLeftComponent(treeScroll);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(splitPane, javax.swing.GroupLayout.DEFAULT_SIZE, 843, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(splitPane, javax.swing.GroupLayout.DEFAULT_SIZE, 472, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

	private void treeValueChanged(javax.swing.event.TreeSelectionEvent evt) {//GEN-FIRST:event_treeValueChanged
		if (evt.getNewLeadSelectionPath().getLastPathComponent() instanceof SetableNode) {
			SetableNode node = (SetableNode) evt.getNewLeadSelectionPath().getLastPathComponent();
			this.setablePanel.setSetable(node.getSetable());
		}
	}//GEN-LAST:event_treeValueChanged
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JSplitPane splitPane;
    private javax.swing.JTree tree;
    private javax.swing.JScrollPane treeScroll;
    // End of variables declaration//GEN-END:variables
	private GroupSetable groupSetable;
	private GenericSetableGUIPanel setablePanel;
}
