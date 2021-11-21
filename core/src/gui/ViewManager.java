
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

import gui.util.Properties;
import gui.CircuitPanelGroup;
import gui.CircuitPanel;
import gui.circuit.management.CircuitManager;
import java.awt.CardLayout;
import java.awt.Container;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

/**
 *
 * @author sylvester
 */
public class ViewManager {

	private static ViewManager instance;

	public static ViewManager getInstance() {
		if (ViewManager.instance == null)
			ViewManager.instance = new ViewManager();
		return ViewManager.instance;
	}

	private static class GroupWindow extends JFrame implements CircuitPanelGroup.ViewChangeListener {

		private CircuitPanelGroup group;

		public GroupWindow(String title) {
			super(title);
			this.setName(title);
			this.setLayout(new CardLayout());
			this.group = new CircuitPanelGroup();
			this.setSize(Properties.getProfile().getViewWindowSize());
			this.group.addListener(this);
			this.group.setName(title);
			this.add(this.group, "TheOnlyComponent");
			this.addWindowFocusListener(new WindowFocusListener() {

				@Override
				public void windowGainedFocus(WindowEvent e) {
					ViewManager.getInstance().focusView(GroupWindow.this.group.getCurrentView());
				}

				@Override
				public void windowLostFocus(WindowEvent e) {
					// Do nothing
				}
			});
		}

		@Override
		public void viewChanged(CircuitPanelGroup group, CircuitPanel visiblePanel) {
			if (visiblePanel != null)
				this.setTitle(this.getName() + " - " + visiblePanel.getName());
		}

		public CircuitPanelGroup getGroup() {
			return group;
		}
	}
	private HashMap<CircuitPanel, CircuitPanelGroup> groups = new HashMap<CircuitPanel, CircuitPanelGroup>();
	private DefaultComboBoxModel listModel = new DefaultComboBoxModel();
	private CircuitPanelGroup initialGroup;
	private HashMap<CircuitPanelGroup, GroupWindow> windows = new HashMap<CircuitPanelGroup, GroupWindow>();
	private int createdWindowsCount = 0;

	private ViewManager() {
		// Do nothing
	}

	public void addView(CircuitPanelGroup group, CircuitPanel view) {
		if (this.initialGroup == null)
			this.initialGroup = group;
		this.groups.put(view, group);
		group.addView(view);
		this.listModel.addElement(view);
	}

	public void renameView(CircuitPanel view) {
		String newName = null;
		do {
			if (newName != null)
				JOptionPane.showMessageDialog(this.groups.get(view).getTopLevelAncestor(), "There is already a view with the same name, please choose another name.", "Name already in use", JOptionPane.WARNING_MESSAGE, null);
			Object returnMessage = JOptionPane.showInputDialog(this.groups.get(view).getTopLevelAncestor(), "Please enter the new name of the view.", "Rename view", JOptionPane.PLAIN_MESSAGE, null, null, view.getName());
			if (returnMessage == null || !(returnMessage instanceof String)) {
				newName = null;
				break;
			}
			newName = (String) returnMessage;
		} while (this.getView(newName) != null);
		if (newName != null) {
			view.setName(newName);
			int index = this.listModel.getIndexOf(view);
			this.listModel.removeElement(view);
			this.listModel.insertElementAt(view, index);
			this.groups.get(view).viewRenamed(view);
		}
	}

	public void focusView(CircuitPanel view) {
		this.groups.get(view).setCurrentView(view);
		view.requestFocusInWindow();
		Container window = this.groups.get(view).getTopLevelAncestor();
		if (window != null && window instanceof JFrame && !((JFrame) window).isActive())
			((JFrame) window).toFront();
		this.setFocusedView(view);
	}

	public void detachView(CircuitPanel view) {
		CircuitPanelGroup group = this.groups.get(view);
		if (group.getViewCount() == 1) {
			System.out.println("Will not detach this view. The main window must always contain at least one view and there is no sense in detaching an already detached view.");
			return;
		}
		GroupWindow window = new GroupWindow("Additional view window (" + ++this.createdWindowsCount + ")");
		this.windows.put(window.getGroup(), window);
		group.removeView(view);
		this.groups.put(view, window.getGroup());
		window.getGroup().addView(view);
		window.setVisible(true);
	}

	public void reattachView(CircuitPanel view, CircuitPanelGroup newGroup) {
		CircuitPanelGroup group = this.groups.get(view);
		if (group == this.initialGroup && group.getViewCount() == 1) {
			System.out.println("Cannot reattach this view. The main window must always contain at least one view.");
			return;
		}
		if (group.getViewCount() == 1 && this.windows.containsKey(group)) {
			JFrame window = this.windows.get(group);
			window.dispose();
			this.windows.remove(group);
		}
		this.groups.remove(view);
		group.removeView(view);
		this.groups.put(view, newGroup);
		newGroup.addView(view);
		this.focusView(view);
	}

	public void removeView(CircuitPanel view) {
		CircuitPanelGroup group = this.groups.get(view);
		if (group == this.initialGroup && group.getViewCount() == 1) {
			System.out.println("Cannot delete this view. The main window must always contain at least one view.");
			return;
		}
		this.groups.remove(view);
		group.removeView(view);
		if (group.getViewCount() == 0 && this.windows.containsKey(group)) {
			JFrame window = this.windows.get(group);
			window.dispose();
			this.windows.remove(group);
		}
		this.listModel.removeElement(view);
	}

	public CircuitPanel getView(String name) {
		for (CircuitPanel view : this.groups.keySet())
			if (view.getName().equals(name))
				return view;
		return null;
	}

	public Set<CircuitPanel> getAllViews() {
		return Collections.unmodifiableSet(this.groups.keySet());
	}

	public CircuitPanelGroup getGroup(String name) {
		for (CircuitPanelGroup group : this.groups.values())
			if (group.getName().equals(name))
				return group;
		return null;
	}

	public CircuitPanelGroup getGroup(CircuitPanel view) {
		return this.groups.get(view);
	}

	public Set<CircuitPanelGroup> getAllGroups() {
		return Collections.unmodifiableSet(new HashSet<CircuitPanelGroup>(this.groups.values()));
	}

	public Set<JFrame> getAllWindows() {
		return Collections.unmodifiableSet(new HashSet<JFrame>(this.windows.values()));
	}

	public void repaintAllViews() {
		Set<CircuitPanel> views = this.getAllViews();
		for (CircuitPanel view : views)
			view.repaint();
	}

	public boolean isInitialGroup(CircuitPanelGroup group) {
		return group == this.initialGroup;
	}

	public boolean hasWindow(CircuitPanelGroup group) {
		return this.windows.containsKey(group);
	}

	public DefaultComboBoxModel getListModel() {
		return this.listModel;
	}
	private CircuitPanel focusedView;
	public static final String PROP_FOCUSEDVIEW = "focusedView";

	/**
	 * Get the value of focusedView
	 *
	 * @return the value of focusedView
	 */
	public CircuitPanel getFocusedView() {
		return focusedView;
	}

	public void refreshFocusedView(CircuitPanel focusedView) {
		if (focusedView == this.focusedView)
			propertyChangeSupport.firePropertyChange(PROP_FOCUSEDVIEW, null, focusedView);
	}

	/**
	 * Set the value of focusedView
	 *
	 * @param focusedView new value of focusedView
	 */
	public void setFocusedView(CircuitPanel focusedView) {
		if (focusedView != this.focusedView) {
			CircuitPanel oldFocusedView = this.focusedView;
			this.focusedView = focusedView;
			this.listModel.setSelectedItem(focusedView);
			propertyChangeSupport.firePropertyChange(PROP_FOCUSEDVIEW, oldFocusedView, focusedView);
		}
	}
	private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

	/**
	 * Add PropertyChangeListener.
	 *
	 * @param listener
	 */
	public void addPropertyChangeListener(PropertyChangeListener listener) {
		propertyChangeSupport.addPropertyChangeListener(listener);
	}

	/**
	 * Remove PropertyChangeListener.
	 *
	 * @param listener
	 */
	public void removePropertyChangeListener(PropertyChangeListener listener) {
		propertyChangeSupport.removePropertyChangeListener(listener);
	}
}
