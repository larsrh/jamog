
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
package gui;

import gui.circuit.management.CircuitManager;
import gui.circuit.ComponentWrapper;
import java.awt.CardLayout;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import javax.swing.JMenuItem;

/**
 *
 * @author sylvester
 */
public class CircuitPanelGroup extends javax.swing.JPanel {

	public static interface ViewChangeListener {

		public void viewChanged(CircuitPanelGroup group, CircuitPanel visiblePanel);

	}

	/**
	 * Creates new form CircuitPanelGroup.
	 */
	public CircuitPanelGroup() {
		initComponents();
	}

	/** This method is called from within the constructor to
	 * initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is
	 * always regenerated by the Form Editor.
	 */
	@SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        viewOptionsPopupMenu = new javax.swing.JPopupMenu();
        renameViewMenuItem = new javax.swing.JMenuItem();
        renameGroupMenuItem = new javax.swing.JMenuItem();
        viewOptionsSeparator1 = new javax.swing.JSeparator();
        detachViewMenuItem = new javax.swing.JMenuItem();
        attachViewMenu = new javax.swing.JMenu();
        toolBar = new javax.swing.JToolBar();
        moveComponentFrontButton = new javax.swing.JButton();
        moveComponentForwardsButton = new javax.swing.JButton();
        moveComponentBackwardsButton = new javax.swing.JButton();
        moveComponentBackButton = new javax.swing.JButton();
        toolbarSeparator2 = new javax.swing.JToolBar.Separator();
        rotateComponentCcwButton = new javax.swing.JButton();
        rotateComponentLittleCcwButton = new javax.swing.JButton();
        rotateComponentLittleCwButton = new javax.swing.JButton();
        rotateComponentCwButton = new javax.swing.JButton();
        growComponentButton = new javax.swing.JButton();
        shrinkComponentButton = new javax.swing.JButton();
        resetComponentButton = new javax.swing.JButton();
        tabbedPane = new javax.swing.JTabbedPane();
        statusBarPanel = new javax.swing.JPanel();
        statusBarControlsPanel = new javax.swing.JPanel();
        viewsComboBox = new javax.swing.JComboBox(ViewManager.getInstance().getListModel());
        addViewButton = new javax.swing.JButton();
        viewOptionsButton = new javax.swing.JToggleButton();
        deleteViewButton = new javax.swing.JButton();
        statusLabel = new javax.swing.JLabel();

        viewOptionsPopupMenu.addPopupMenuListener(new javax.swing.event.PopupMenuListener() {
            public void popupMenuCanceled(javax.swing.event.PopupMenuEvent evt) {
            }
            public void popupMenuWillBecomeInvisible(javax.swing.event.PopupMenuEvent evt) {
                viewOptionsPopupMenuPopupMenuWillBecomeInvisible(evt);
            }
            public void popupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent evt) {
            }
        });

        renameViewMenuItem.setText("Rename view");
        renameViewMenuItem.setEnabled(false);
        renameViewMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                renameViewMenuItemActionPerformed(evt);
            }
        });
        viewOptionsPopupMenu.add(renameViewMenuItem);

        renameGroupMenuItem.setText("Rename window");
        renameGroupMenuItem.setEnabled(false);
        viewOptionsPopupMenu.add(renameGroupMenuItem);
        viewOptionsPopupMenu.add(viewOptionsSeparator1);

        detachViewMenuItem.setText("Detach view");
        detachViewMenuItem.setEnabled(false);
        detachViewMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                detachViewMenuItemActionPerformed(evt);
            }
        });
        viewOptionsPopupMenu.add(detachViewMenuItem);

        attachViewMenu.setText("Attach view to...");
        attachViewMenu.setEnabled(false);
        viewOptionsPopupMenu.add(attachViewMenu);

        // TODO: Find a better solution here, to make the menu calculate it's
        // size before being displayed. (doLayout() or validate() don't work)
        this.viewOptionsPopupMenu.setVisible(true);
        this.viewOptionsPopupMenu.setVisible(false);

        toolBar.setFloatable(false);
        toolBar.setRollover(true);

        moveComponentFrontButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gui/icons/famfamfam/shape_move_front.png"))); // NOI18N
        moveComponentFrontButton.setToolTipText("Display selected component topmost.");
        moveComponentFrontButton.setFocusable(false);
        moveComponentFrontButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        moveComponentFrontButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        moveComponentFrontButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                moveComponentFrontButtonActionPerformed(evt);
            }
        });
        toolBar.add(moveComponentFrontButton);

        moveComponentForwardsButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gui/icons/famfamfam/shape_move_forwards.png"))); // NOI18N
        moveComponentForwardsButton.setToolTipText("Move selected component forwards.");
        moveComponentForwardsButton.setFocusable(false);
        moveComponentForwardsButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        moveComponentForwardsButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        moveComponentForwardsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                moveComponentForwardsButtonActionPerformed(evt);
            }
        });
        toolBar.add(moveComponentForwardsButton);

        moveComponentBackwardsButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gui/icons/famfamfam/shape_move_backwards.png"))); // NOI18N
        moveComponentBackwardsButton.setToolTipText("Move selected component backwards.");
        moveComponentBackwardsButton.setFocusable(false);
        moveComponentBackwardsButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        moveComponentBackwardsButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        moveComponentBackwardsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                moveComponentBackwardsButtonActionPerformed(evt);
            }
        });
        toolBar.add(moveComponentBackwardsButton);

        moveComponentBackButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gui/icons/famfamfam/shape_move_back.png"))); // NOI18N
        moveComponentBackButton.setToolTipText("Display selected component below all other components.");
        moveComponentBackButton.setFocusable(false);
        moveComponentBackButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        moveComponentBackButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        moveComponentBackButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                moveComponentBackButtonActionPerformed(evt);
            }
        });
        toolBar.add(moveComponentBackButton);
        toolBar.add(toolbarSeparator2);

        rotateComponentCcwButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gui/icons/famfamfam/arrow_rotate_anticlockwise.png"))); // NOI18N
        rotateComponentCcwButton.setToolTipText("Rotate selected component by 90 degree counter clockwise.");
        rotateComponentCcwButton.setFocusable(false);
        rotateComponentCcwButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        rotateComponentCcwButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        rotateComponentCcwButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rotateComponentCcwButtonActionPerformed(evt);
            }
        });
        toolBar.add(rotateComponentCcwButton);

        rotateComponentLittleCcwButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gui/icons/famfamfam/shape_rotate_anticlockwise.png"))); // NOI18N
        rotateComponentLittleCcwButton.setToolTipText("Rotate selected component by 22.5 degree counter clockwise.");
        rotateComponentLittleCcwButton.setFocusable(false);
        rotateComponentLittleCcwButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        rotateComponentLittleCcwButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        rotateComponentLittleCcwButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rotateComponentLittleCcwButtonActionPerformed(evt);
            }
        });
        toolBar.add(rotateComponentLittleCcwButton);

        rotateComponentLittleCwButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gui/icons/famfamfam/shape_rotate_clockwise.png"))); // NOI18N
        rotateComponentLittleCwButton.setToolTipText("Rotate selected component by 22.5 degree clockwise.");
        rotateComponentLittleCwButton.setFocusable(false);
        rotateComponentLittleCwButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        rotateComponentLittleCwButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        rotateComponentLittleCwButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rotateComponentLittleCwButtonActionPerformed(evt);
            }
        });
        toolBar.add(rotateComponentLittleCwButton);

        rotateComponentCwButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gui/icons/famfamfam/arrow_rotate_clockwise.png"))); // NOI18N
        rotateComponentCwButton.setToolTipText("Rotate selected component by 90 degree clockwise.");
        rotateComponentCwButton.setFocusable(false);
        rotateComponentCwButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        rotateComponentCwButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        rotateComponentCwButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rotateComponentCwButtonActionPerformed(evt);
            }
        });
        toolBar.add(rotateComponentCwButton);

        growComponentButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gui/icons/famfamfam/arrow_out.png"))); // NOI18N
        growComponentButton.setToolTipText("Increase selected component in size by 10%.");
        growComponentButton.setFocusable(false);
        growComponentButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        growComponentButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        growComponentButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                growComponentButtonActionPerformed(evt);
            }
        });
        toolBar.add(growComponentButton);

        shrinkComponentButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gui/icons/famfamfam/arrow_in.png"))); // NOI18N
        shrinkComponentButton.setToolTipText("Decrease selected component in size by 10%.");
        shrinkComponentButton.setFocusable(false);
        shrinkComponentButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        shrinkComponentButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        shrinkComponentButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                shrinkComponentButtonActionPerformed(evt);
            }
        });
        toolBar.add(shrinkComponentButton);

        resetComponentButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gui/icons/famfamfam/shape_square.png"))); // NOI18N
        resetComponentButton.setToolTipText("Reset component to standard size and rotation.");
        resetComponentButton.setFocusable(false);
        resetComponentButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        resetComponentButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        resetComponentButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                resetComponentButtonActionPerformed(evt);
            }
        });
        toolBar.add(resetComponentButton);

        tabbedPane.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                tabbedPaneStateChanged(evt);
            }
        });
        tabbedPane.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                tabbedPaneFocusGained(evt);
            }
        });

        statusBarPanel.setLayout(new java.awt.CardLayout());

        viewsComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                viewsComboBoxActionPerformed(evt);
            }
        });

        addViewButton.setText("Add view");
        addViewButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addViewButtonActionPerformed(evt);
            }
        });

        viewOptionsButton.setText("View Options");
        viewOptionsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                viewOptionsButtonActionPerformed(evt);
            }
        });

        deleteViewButton.setText("Delete view");
        deleteViewButton.setEnabled(false);
        deleteViewButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteViewButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout statusBarControlsPanelLayout = new javax.swing.GroupLayout(statusBarControlsPanel);
        statusBarControlsPanel.setLayout(statusBarControlsPanelLayout);
        statusBarControlsPanelLayout.setHorizontalGroup(
            statusBarControlsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, statusBarControlsPanelLayout.createSequentialGroup()
                .addComponent(viewsComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 189, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 360, Short.MAX_VALUE)
                .addComponent(addViewButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(deleteViewButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(viewOptionsButton))
        );
        statusBarControlsPanelLayout.setVerticalGroup(
            statusBarControlsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(viewsComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGroup(statusBarControlsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(viewOptionsButton)
                .addComponent(deleteViewButton)
                .addComponent(addViewButton))
        );

        statusBarPanel.add(statusBarControlsPanel, "controls");

        statusLabel.setText("jLabel1");
        statusBarPanel.add(statusLabel, "status");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(toolBar, javax.swing.GroupLayout.DEFAULT_SIZE, 816, Short.MAX_VALUE)
            .addComponent(tabbedPane, javax.swing.GroupLayout.DEFAULT_SIZE, 816, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addGap(2, 2, 2)
                .addComponent(statusBarPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(toolBar, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(tabbedPane, javax.swing.GroupLayout.DEFAULT_SIZE, 408, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(statusBarPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
    }// </editor-fold>//GEN-END:initComponents

	private void tabbedPaneStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_tabbedPaneStateChanged
		this.viewGotFocus();
	}//GEN-LAST:event_tabbedPaneStateChanged

	private void addViewButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addViewButtonActionPerformed
		ViewManager.getInstance().addView(this, this.getCurrentView().copy());
	}//GEN-LAST:event_addViewButtonActionPerformed

	private void viewsComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_viewsComboBoxActionPerformed
		ViewManager.getInstance().focusView((CircuitPanel) this.viewsComboBox.getSelectedItem());
	}//GEN-LAST:event_viewsComboBoxActionPerformed

	private void deleteViewButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteViewButtonActionPerformed
		ViewManager.getInstance().removeView(this.getCurrentView());
	}//GEN-LAST:event_deleteViewButtonActionPerformed

	private void viewOptionsPopupMenuPopupMenuWillBecomeInvisible(javax.swing.event.PopupMenuEvent evt) {//GEN-FIRST:event_viewOptionsPopupMenuPopupMenuWillBecomeInvisible
		this.viewOptionsButton.setSelected(false);
	}//GEN-LAST:event_viewOptionsPopupMenuPopupMenuWillBecomeInvisible

	private void detachViewMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_detachViewMenuItemActionPerformed
		ViewManager.getInstance().detachView(this.getCurrentView());
	}//GEN-LAST:event_detachViewMenuItemActionPerformed

	private void viewOptionsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_viewOptionsButtonActionPerformed
		this.attachViewMenu.removeAll();
		Iterator<CircuitPanelGroup> iterator = ViewManager.getInstance().getAllGroups().iterator();
		while (iterator.hasNext()) {
			CircuitPanelGroup group = iterator.next();
			if (group != this) {
				JMenuItem item = new JMenuItem(group.getName());
				item.addActionListener(this.reattachMenuActionListener);
				this.attachViewMenu.add(item);
			}
		}
		this.attachViewMenu.setEnabled(this.attachViewMenu.getMenuComponentCount() > 0 && (this.tabbedPane.getTabCount() > 1 || ViewManager.getInstance().hasWindow(this)));
		this.viewOptionsPopupMenu.show(this.viewOptionsButton, 0, 0 - this.viewOptionsPopupMenu.getHeight());
	}//GEN-LAST:event_viewOptionsButtonActionPerformed

	private void tabbedPaneFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_tabbedPaneFocusGained
		this.viewGotFocus();
	}//GEN-LAST:event_tabbedPaneFocusGained

	private void renameViewMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_renameViewMenuItemActionPerformed
		ViewManager.getInstance().renameView(this.getCurrentView());
	}//GEN-LAST:event_renameViewMenuItemActionPerformed

	private void moveComponentFrontButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_moveComponentFrontButtonActionPerformed
		Set<ComponentWrapper> selection = this.getCurrentView().getSelectedComponents();
		for (ComponentWrapper wrapper : selection)
			this.getCurrentView().getCircuitManager().moveComponent(wrapper, CircuitManager.ComponentLayerMovement.END);
}//GEN-LAST:event_moveComponentFrontButtonActionPerformed

	private void moveComponentForwardsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_moveComponentForwardsButtonActionPerformed
		Set<ComponentWrapper> selection = this.getCurrentView().getSelectedComponents();
		for (ComponentWrapper wrapper : selection)
			this.getCurrentView().getCircuitManager().moveComponent(wrapper, CircuitManager.ComponentLayerMovement.BACKWARDS);
}//GEN-LAST:event_moveComponentForwardsButtonActionPerformed

	private void moveComponentBackwardsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_moveComponentBackwardsButtonActionPerformed
		Set<ComponentWrapper> selection = this.getCurrentView().getSelectedComponents();
		for (ComponentWrapper wrapper : selection)
			this.getCurrentView().getCircuitManager().moveComponent(wrapper, CircuitManager.ComponentLayerMovement.FORWARDS);
}//GEN-LAST:event_moveComponentBackwardsButtonActionPerformed

	private void moveComponentBackButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_moveComponentBackButtonActionPerformed
		Set<ComponentWrapper> selection = this.getCurrentView().getSelectedComponents();
		for (ComponentWrapper wrapper : selection)
			this.getCurrentView().getCircuitManager().moveComponent(wrapper, CircuitManager.ComponentLayerMovement.START);
}//GEN-LAST:event_moveComponentBackButtonActionPerformed

	private void rotateComponentCcwButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rotateComponentCcwButtonActionPerformed
		Set<ComponentWrapper> selection = this.getCurrentView().getSelectedComponents();
		for (ComponentWrapper wrapper : selection)
			wrapper.rotate(-90);
}//GEN-LAST:event_rotateComponentCcwButtonActionPerformed

	private void rotateComponentLittleCcwButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rotateComponentLittleCcwButtonActionPerformed
		Set<ComponentWrapper> selection = this.getCurrentView().getSelectedComponents();
		for (ComponentWrapper wrapper : selection)
			wrapper.rotate(-22.5f);
}//GEN-LAST:event_rotateComponentLittleCcwButtonActionPerformed

	private void rotateComponentLittleCwButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rotateComponentLittleCwButtonActionPerformed
		Set<ComponentWrapper> selection = this.getCurrentView().getSelectedComponents();
		for (ComponentWrapper wrapper : selection)
			wrapper.rotate(22.5f);
}//GEN-LAST:event_rotateComponentLittleCwButtonActionPerformed

	private void rotateComponentCwButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rotateComponentCwButtonActionPerformed
		Set<ComponentWrapper> selection = this.getCurrentView().getSelectedComponents();
		for (ComponentWrapper wrapper : selection)
			wrapper.rotate(90);
}//GEN-LAST:event_rotateComponentCwButtonActionPerformed

	private void growComponentButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_growComponentButtonActionPerformed
		Set<ComponentWrapper> selection = this.getCurrentView().getSelectedComponents();
		for (ComponentWrapper wrapper : selection)
			wrapper.scale(1.1, true);
}//GEN-LAST:event_growComponentButtonActionPerformed

	private void shrinkComponentButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_shrinkComponentButtonActionPerformed
		Set<ComponentWrapper> selection = this.getCurrentView().getSelectedComponents();
		for (ComponentWrapper wrapper : selection)
			wrapper.scale(0.9, true);
}//GEN-LAST:event_shrinkComponentButtonActionPerformed

	private void resetComponentButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_resetComponentButtonActionPerformed
		Set<ComponentWrapper> selection = this.getCurrentView().getSelectedComponents();
		for (ComponentWrapper wrapper : selection)
			wrapper.resetGeometry(true, false);
}//GEN-LAST:event_resetComponentButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addViewButton;
    private javax.swing.JMenu attachViewMenu;
    private javax.swing.JButton deleteViewButton;
    private javax.swing.JMenuItem detachViewMenuItem;
    private javax.swing.JButton growComponentButton;
    private javax.swing.JButton moveComponentBackButton;
    private javax.swing.JButton moveComponentBackwardsButton;
    private javax.swing.JButton moveComponentForwardsButton;
    private javax.swing.JButton moveComponentFrontButton;
    private javax.swing.JMenuItem renameGroupMenuItem;
    private javax.swing.JMenuItem renameViewMenuItem;
    private javax.swing.JButton resetComponentButton;
    private javax.swing.JButton rotateComponentCcwButton;
    private javax.swing.JButton rotateComponentCwButton;
    private javax.swing.JButton rotateComponentLittleCcwButton;
    private javax.swing.JButton rotateComponentLittleCwButton;
    private javax.swing.JButton shrinkComponentButton;
    private javax.swing.JPanel statusBarControlsPanel;
    private javax.swing.JPanel statusBarPanel;
    private javax.swing.JLabel statusLabel;
    private javax.swing.JTabbedPane tabbedPane;
    private javax.swing.JToolBar toolBar;
    private javax.swing.JToolBar.Separator toolbarSeparator2;
    private javax.swing.JToggleButton viewOptionsButton;
    private javax.swing.JPopupMenu viewOptionsPopupMenu;
    private javax.swing.JSeparator viewOptionsSeparator1;
    private javax.swing.JComboBox viewsComboBox;
    // End of variables declaration//GEN-END:variables
	private List<CircuitPanel> views = new LinkedList<CircuitPanel>();
	private Set<ViewChangeListener> listener = new HashSet<ViewChangeListener>();
	private ActionListener reattachMenuActionListener = new java.awt.event.ActionListener() {

		public void actionPerformed(java.awt.event.ActionEvent evt) {
			if (evt.getSource() instanceof JMenuItem) {
				JMenuItem item = (JMenuItem) evt.getSource();
				ViewManager.getInstance().reattachView(CircuitPanelGroup.this.getCurrentView(), ViewManager.getInstance().getGroup(item.getText()));
			}
		}

	};

	public void addView(CircuitPanel view) {
		this.tabbedPane.add(view.getName(), view);
		if (ViewManager.getInstance().hasWindow(this))
			this.renameGroupMenuItem.setEnabled(true);
		if (this.tabbedPane.getTabCount() > 0)
			this.renameViewMenuItem.setEnabled(true);
		if (this.tabbedPane.getTabCount() > 1 || ViewManager.getInstance().hasWindow(this)) {
			this.deleteViewButton.setEnabled(true);
			this.attachViewMenu.setEnabled(true);
		}
		if (this.tabbedPane.getTabCount() > 1)
			this.detachViewMenuItem.setEnabled(true);
	}

	public void removeView(CircuitPanel view) {
		this.tabbedPane.remove(view);
		if (this.tabbedPane.getTabCount() == 1)
			this.detachViewMenuItem.setEnabled(false);
		if (this.tabbedPane.getTabCount() == 1 && !ViewManager.getInstance().hasWindow(this)) {
			this.deleteViewButton.setEnabled(false);
			this.attachViewMenu.setEnabled(false);
		}
		if (this.tabbedPane.getTabCount() == 1)
			this.detachViewMenuItem.setEnabled(false);
	}

	public boolean addListener(ViewChangeListener listener) {
		return this.listener.add(listener);
	}

	public boolean removeListener(ViewChangeListener listener) {
		return this.listener.remove(listener);
	}

	public CircuitPanel getCurrentView() {
		return (CircuitPanel) this.tabbedPane.getSelectedComponent();
	}

	public boolean setCurrentView(CircuitPanel view) {
		try {
			this.tabbedPane.setSelectedComponent(view);
		} catch (IllegalArgumentException ex) {
			return false;
		}
		this.viewsComboBox.setSelectedItem(view.getName());
		return true;
	}

	public int getViewCount() {
		return this.tabbedPane.getTabCount();
	}

	private void viewGotFocus() {
		if (this.tabbedPane.getTabCount() > 0 && ViewManager.getInstance().getListModel().getSelectedItem() != this.tabbedPane.getSelectedComponent()) {
			ViewManager.getInstance().setFocusedView((CircuitPanel) this.tabbedPane.getSelectedComponent());
			Iterator<ViewChangeListener> iterator = this.listener.iterator();
			while (iterator.hasNext())
				iterator.next().viewChanged(this, (CircuitPanel) this.tabbedPane.getSelectedComponent());
		}
	}

	public void viewRenamed(CircuitPanel view) {
		for (int i = 0; i < this.tabbedPane.getTabCount(); i++)
			if (this.tabbedPane.getTabComponentAt(i) == view)
				this.tabbedPane.setTitleAt(i, view.getName());
	}

	public String getStatusMsg() {
		return this.statusLabel.getText();
	}

	public void setStatusMsg(String message) {
		this.statusLabel.setText(message);
		if (message != null && message != "")
			((CardLayout) this.statusBarPanel.getLayout()).show(this.statusBarPanel, "status");
		else
			((CardLayout) this.statusBarPanel.getLayout()).show(this.statusBarPanel, "controls");
	}

}
