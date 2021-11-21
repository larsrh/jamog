
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

import core.exception.SerializingException;
import core.misc.serial.SerializingStream;
import gui.dialogs.BuildProgressDialog;
import core.build.Component;
import core.build.ComponentCollection;
import core.build.Environment;
import core.build.Machine;
import core.exception.AnalyzeException;
import core.exception.BuildException;
import static core.build.Component.Extension;
import core.misc.ClockSimulator;
import core.signal.Bit;
import core.signal.Signal;
import core.sim.Simulator;
import gui.events.CircuitModificationEvent;
import gui.exception.IllegalLinkageException;
import gui.circuit.management.CircuitManager;
import gui.circuit.ComponentWrapper;
import gui.circuit.management.Connector;
import gui.util.Properties;
import gui.circuit.management.SpecialConnector;
import gui.events.LinkageEvent;
import gui.events.WrapperEvent;
import gui.util.Utilities;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

/**
 *
 * @author sylvester
 */
public class MachinePanel extends javax.swing.JPanel implements
	ClockSimulator.ClockListener,
	ClockSimulator.StepListener,
	ClockSimulator.StateListener,
	CircuitModificationEvent.Listener {

	private static final long serialVersionUID = -8196720903042778616L;

	private static enum State {

		DIRTY,
		CLEAN,
		BUILDING,
		SIMULATION_RUNNING,
		SIMULATION_WAITING;

		public boolean isDynamic() {
			switch (this) {
				case BUILDING:
				case SIMULATION_RUNNING:
				case SIMULATION_WAITING:
					return true;
				default:
					return false;
			}
		}

		public boolean isStatic() {
			return !this.isDynamic();
		}
	}

	private static class ViewStackTree extends JTree implements CircuitModificationEvent.Listener, PropertyChangeListener {

		private DefaultTreeModel model = new DefaultTreeModel(new DefaultMutableTreeNode());
		private CircuitPanel copiedView;

		ViewStackTree() {
			super();
			this.setModel(this.model);
			this.setRootVisible(false);
			this.addMouseListener(new MouseAdapter() {

				private JPopupMenu menu = new JPopupMenu();
				private ActionListener listener = new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent evt) {
						ViewStackTree tree = ViewStackTree.this;
						CircuitManager manager = tree.copiedView.getCircuitManager();
						if (tree.isSelectionEmpty())
							return;
						DefaultMutableTreeNode lastItem = (DefaultMutableTreeNode) tree.getSelectionPath().getLastPathComponent();
						LinkedList<ComponentCollection> newParents = new LinkedList<ComponentCollection>();
						for (ComponentCollection collection : manager.getParents()) {
							newParents.add(collection);
							if (collection == lastItem.getUserObject()) {
								tree.copiedView.setCircuitManager(CircuitManager.getManager(newParents, null));
								return;
							}
						}
						if (lastItem.getUserObject() instanceof ComponentWrapper) {
							ComponentWrapper wrapper = (ComponentWrapper) lastItem.getUserObject();
							if (!wrapper.isBuilt()) {
								JOptionPane.showMessageDialog(
									Properties.getProfile().getMainWindow(),
									"Cannot open this component since it is not built.",
									"Cannot open component",
									JOptionPane.INFORMATION_MESSAGE);
								return;
							}
							if (!(wrapper.getComponent() instanceof ComponentCollection)) {
								JOptionPane.showMessageDialog(
									Properties.getProfile().getMainWindow(),
									"Cannot open this component since it is 'atomic' i.e. has no internal structure simulated.",
									"Cannot open component",
									JOptionPane.INFORMATION_MESSAGE);
								return;
							}
							newParents.add((ComponentCollection) wrapper.getComponent());
							if (evt.getSource() == open)
								tree.copiedView.setCircuitManager(CircuitManager.getManager(newParents, null));
							else if (evt.getSource() == openInNewView)
								ViewManager.getInstance().addView(
									ViewManager.getInstance().getGroup(copiedView),
									new CircuitPanel(CircuitManager.getManager(newParents, null), "View of " + wrapper.getComponent().getShortName()));
						} else
							tree.copyFromManager();
					}
				};
				private JMenuItem open;
				private JMenuItem openInNewView;

				{
					this.open = new JMenuItem("Open component in current view");
					this.open.addActionListener(listener);
					this.menu.add(open);
					this.openInNewView = new JMenuItem("Open component in new view");
					this.openInNewView.addActionListener(listener);
					this.menu.add(openInNewView);
				}

				@Override
				public void mouseClicked(MouseEvent evt) {
					TreePath[] pathes;
					DefaultMutableTreeNode node;
					ComponentWrapper wrapper;

					ViewStackTree.this.copiedView.clearSelectedComponents();
					if ((pathes = ViewStackTree.this.getSelectionPaths()) != null)
						for (TreePath path : pathes)
							if (path.getLastPathComponent() instanceof DefaultMutableTreeNode) {
								node = (DefaultMutableTreeNode) path.getLastPathComponent();
								if (node.getUserObject() instanceof ComponentWrapper) {
									wrapper = (ComponentWrapper) node.getUserObject();
									ViewStackTree.this.copiedView.selectComponents(wrapper);
								}
							}
				}

				@Override
				public void mousePressed(MouseEvent evt) {
					if (evt.isPopupTrigger())
						showPopup(evt);
				}

				@Override
				public void mouseReleased(MouseEvent evt) {
					if (evt.isPopupTrigger())
						showPopup(evt);
				}

				private void showPopup(MouseEvent evt) {
					ViewStackTree tree = ViewStackTree.this;
					this.open.setEnabled(!tree.isSelectionEmpty());
					this.openInNewView.setEnabled(!tree.isSelectionEmpty());
					this.menu.show(tree, evt.getX(), evt.getY());
				}
			});
			ViewManager.getInstance().addPropertyChangeListener(this);
		}

		@Override
		public String convertValueToText(Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
			if (value instanceof DefaultMutableTreeNode) {
				value = ((DefaultMutableTreeNode) value).getUserObject();
				String text = null;
				if (value instanceof Component)
					text = ((Component) value).getShortName();
				else if (value instanceof Machine)
					text = ((Machine) value).getName();
				else if (value instanceof Environment)
					text = ((Environment) value).getName();
				if (text != null) {
					if (value == ViewStackTree.this.copiedView.getCircuitManager().getParent())
						text += " [in view]";
					return text;
				}
			}
			return super.convertValueToText(value, selected, expanded, leaf, row, hasFocus);
		}

		private void copyFromManager() {
			CircuitManager manager = this.copiedView.getCircuitManager();
			DefaultMutableTreeNode parent = (DefaultMutableTreeNode) this.model.getRoot();
			TreePath path = new TreePath(parent);
			parent.removeAllChildren();
			for (ComponentCollection child : manager.getParents()) {
				DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(child);
				parent.add(childNode);
				parent = childNode;
				path = path.pathByAddingChild(childNode);
			}
			ComponentWrapper child = manager.getComponentList().start;
			while (child != null) {
				parent.add(new DefaultMutableTreeNode(child));
				child = child.getNextComponent();
			}
			this.model.reload();
			this.expandPath(path);
		}

		@Override
		public void circuitChanged(CircuitModificationEvent evt) {
			if (!evt.isWrapperGeometryEvent())
				this.copyFromManager();
		}

		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			if (evt.getPropertyName().equals(ViewManager.PROP_FOCUSEDVIEW)) {
				if (this.copiedView != null)
					this.copiedView.getCircuitManager().removeModificationListener(this);
				this.copiedView = (CircuitPanel) evt.getNewValue();
				this.copiedView.getCircuitManager().addModificationListener(this);
				this.copyFromManager();
			}
		}
	}

	/**
	 * @deprecated only for the GUI builder
	 */
	public MachinePanel() {
		this(new Machine("New Machine"));
	}

	public MachinePanel(Machine machine) {
		this.machine = machine;
		this.circuitManager = CircuitManager.getManager(new LinkedList<ComponentCollection>(Collections.singletonList(machine)), this);
		this.initComponents();

		DefaultTreeCellRenderer viewStackRenderer = new DefaultTreeCellRenderer();
		viewStackRenderer.setClosedIcon(Utilities.getIcon("brick.png", true));
		viewStackRenderer.setLeafIcon(Utilities.getIcon("brick.png", true));
		viewStackRenderer.setOpenIcon(Utilities.getIcon("brick.png", true));
		this.viewStackTree.setCellRenderer(viewStackRenderer);

		this.setState(State.DIRTY, false);
		ViewManager.getInstance().focusView(this.circuitPanelGroup.getCurrentView());
	}

	/** This method is called from within the constructor to
	 * initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is
	 * always regenerated by the Form Editor.
	 */
	@SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        mainSplit = new javax.swing.JSplitPane();
        sideSplit = new javax.swing.JSplitPane();
        AvailableComponentsOuterPanel = new javax.swing.JPanel();
        availableComponentsLabel = new javax.swing.JLabel();
        AvailableComponentsInnerPanel = new javax.swing.JPanel();
        availableComponentsTreeScroll = new javax.swing.JScrollPane();
        availableComponentsRenderer = new javax.swing.tree.DefaultTreeCellRenderer();
        availableComponentsTree = new gui.ComponentTree(gui.util.Properties.getProfile().getAvailableComponentsTreeModel());
        viewStackOuterPanel = new javax.swing.JPanel();
        viewStackLabel = new javax.swing.JLabel();
        viewStackInnerPanel = new javax.swing.JPanel();
        viewStackTreeScroll = new javax.swing.JScrollPane();
        circuitPanelGroup = new gui.CircuitPanelGroup();
        toolBar = new javax.swing.JToolBar();
        addingModeButton = new javax.swing.JToggleButton();
        deletingModeButton = new javax.swing.JToggleButton();
        jSeparator1 = new javax.swing.JToolBar.Separator();
        buildButton = new javax.swing.JButton();
        jSeparator2 = new javax.swing.JToolBar.Separator();
        runButton = new javax.swing.JButton();
        runClockStepButton = new javax.swing.JButton();
        runStepButton = new javax.swing.JButton();
        pauseButton = new javax.swing.JButton();
        stopButton = new javax.swing.JButton();
        progressBar = new javax.swing.JProgressBar();
        abortButton = new javax.swing.JButton();

        mainSplit.setDividerLocation(600);
        mainSplit.setResizeWeight(1.0);

        sideSplit.setDividerLocation(201);
        sideSplit.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        sideSplit.setResizeWeight(1.0);
        sideSplit.setMinimumSize(new java.awt.Dimension(310, 79));
        sideSplit.setPreferredSize(new java.awt.Dimension(310, 625));

        availableComponentsLabel.setText("Available components:");
        availableComponentsLabel.setToolTipText("These components can be placed in a circuit view.");

        availableComponentsRenderer.setClosedIcon(Utilities.getIcon("box.png", true));
        availableComponentsRenderer.setLeafIcon(Utilities.getIcon("brick.png", true));
        availableComponentsRenderer.setOpenIcon(Utilities.getIcon("box.png", true));
        availableComponentsTree.setCellRenderer(availableComponentsRenderer);
        availableComponentsTree.setDragEnabled(true);
        availableComponentsTree.setRootVisible(false);
        availableComponentsTreeScroll.setViewportView(availableComponentsTree);

        javax.swing.GroupLayout AvailableComponentsInnerPanelLayout = new javax.swing.GroupLayout(AvailableComponentsInnerPanel);
        AvailableComponentsInnerPanel.setLayout(AvailableComponentsInnerPanelLayout);
        AvailableComponentsInnerPanelLayout.setHorizontalGroup(
            AvailableComponentsInnerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 323, Short.MAX_VALUE)
            .addGroup(AvailableComponentsInnerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(availableComponentsTreeScroll, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 323, Short.MAX_VALUE))
        );
        AvailableComponentsInnerPanelLayout.setVerticalGroup(
            AvailableComponentsInnerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 178, Short.MAX_VALUE)
            .addGroup(AvailableComponentsInnerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(availableComponentsTreeScroll, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 178, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout AvailableComponentsOuterPanelLayout = new javax.swing.GroupLayout(AvailableComponentsOuterPanel);
        AvailableComponentsOuterPanel.setLayout(AvailableComponentsOuterPanelLayout);
        AvailableComponentsOuterPanelLayout.setHorizontalGroup(
            AvailableComponentsOuterPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(AvailableComponentsInnerPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(availableComponentsLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 323, Short.MAX_VALUE)
        );
        AvailableComponentsOuterPanelLayout.setVerticalGroup(
            AvailableComponentsOuterPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(AvailableComponentsOuterPanelLayout.createSequentialGroup()
                .addComponent(availableComponentsLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(AvailableComponentsInnerPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        sideSplit.setLeftComponent(AvailableComponentsOuterPanel);

        viewStackLabel.setText("View stack:");
        viewStackLabel.setToolTipText("This shows the component-level which you are currently viewing, and it's children.");

        viewStackTreeScroll.setViewportView(viewStackTree);

        javax.swing.GroupLayout viewStackInnerPanelLayout = new javax.swing.GroupLayout(viewStackInnerPanel);
        viewStackInnerPanel.setLayout(viewStackInnerPanelLayout);
        viewStackInnerPanelLayout.setHorizontalGroup(
            viewStackInnerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(viewStackTreeScroll, javax.swing.GroupLayout.DEFAULT_SIZE, 323, Short.MAX_VALUE)
        );
        viewStackInnerPanelLayout.setVerticalGroup(
            viewStackInnerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(viewStackTreeScroll, javax.swing.GroupLayout.DEFAULT_SIZE, 215, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout viewStackOuterPanelLayout = new javax.swing.GroupLayout(viewStackOuterPanel);
        viewStackOuterPanel.setLayout(viewStackOuterPanelLayout);
        viewStackOuterPanelLayout.setHorizontalGroup(
            viewStackOuterPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(viewStackLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 323, Short.MAX_VALUE)
            .addComponent(viewStackInnerPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        viewStackOuterPanelLayout.setVerticalGroup(
            viewStackOuterPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(viewStackOuterPanelLayout.createSequentialGroup()
                .addComponent(viewStackLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(viewStackInnerPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        sideSplit.setRightComponent(viewStackOuterPanel);

        mainSplit.setRightComponent(sideSplit);

        circuitPanelGroup.setName("Main window view"); // NOI18N
        mainSplit.setLeftComponent(circuitPanelGroup);
        CircuitPanel mainPanel = new CircuitPanel(this.circuitManager, "Default view");
        ViewManager.getInstance().addView(this.circuitPanelGroup, mainPanel);

        toolBar.setFloatable(false);
        toolBar.setRollover(true);

        addingModeButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gui/icons/add.png"))); // NOI18N
        addingModeButton.setText("Add");
        addingModeButton.setToolTipText("Adds an instance of the component selected in available components tree with each click.");
        addingModeButton.setDisabledIcon(new javax.swing.ImageIcon(getClass().getResource("/gui/icons/addDisabled.png"))); // NOI18N
        addingModeButton.setFocusable(false);
        addingModeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addingModeButtonActionPerformed(evt);
            }
        });
        toolBar.add(addingModeButton);

        deletingModeButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gui/icons/remove.png"))); // NOI18N
        deletingModeButton.setText("Delete");
        deletingModeButton.setToolTipText("Delete components upon click.");
        deletingModeButton.setDisabledIcon(new javax.swing.ImageIcon(getClass().getResource("/gui/icons/removeDisabled.png"))); // NOI18N
        deletingModeButton.setFocusable(false);
        deletingModeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deletingModeButtonActionPerformed(evt);
            }
        });
        toolBar.add(deletingModeButton);
        toolBar.add(jSeparator1);

        buildButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gui/icons/build.png"))); // NOI18N
        buildButton.setText("Build machine");
        buildButton.setDisabledIcon(new javax.swing.ImageIcon(getClass().getResource("/gui/icons/buildDisabled.png"))); // NOI18N
        buildButton.setFocusable(false);
        buildButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buildButtonActionPerformed(evt);
            }
        });
        toolBar.add(buildButton);
        toolBar.add(jSeparator2);

        runButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gui/icons/run.png"))); // NOI18N
        runButton.setToolTipText("Run machine until stopped by user");
        runButton.setDisabledIcon(new javax.swing.ImageIcon(getClass().getResource("/gui/icons/runDisabled.png"))); // NOI18N
        runButton.setFocusable(false);
        runButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                runButtonActionPerformed(evt);
            }
        });
        toolBar.add(runButton);

        runClockStepButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gui/icons/runClockStep.png"))); // NOI18N
        runClockStepButton.setToolTipText("Run one clock step (i.e. Calculate all changes resulting from setting the clock to HIGH and back to LOW.)");
        runClockStepButton.setDisabledIcon(new javax.swing.ImageIcon(getClass().getResource("/gui/icons/runClockStepDisabled.png"))); // NOI18N
        runClockStepButton.setFocusable(false);
        runClockStepButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        runClockStepButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        runClockStepButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                runClockStepButtonActionPerformed(evt);
            }
        });
        toolBar.add(runClockStepButton);

        runStepButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gui/icons/runStep.png"))); // NOI18N
        runStepButton.setToolTipText("Run one machine step (i.e. Calculate the next set of simultaneous calculatable components.))");
        runStepButton.setDisabledIcon(new javax.swing.ImageIcon(getClass().getResource("/gui/icons/runStepDisabled.png"))); // NOI18N
        runStepButton.setFocusable(false);
        runStepButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                runStepButtonActionPerformed(evt);
            }
        });
        toolBar.add(runStepButton);

        pauseButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gui/icons/pause.png"))); // NOI18N
        pauseButton.setToolTipText("Pause running machine");
        pauseButton.setDisabledIcon(new javax.swing.ImageIcon(getClass().getResource("/gui/icons/pauseDisabled.png"))); // NOI18N
        pauseButton.setFocusable(false);
        pauseButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        pauseButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        pauseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pauseButtonActionPerformed(evt);
            }
        });
        toolBar.add(pauseButton);

        stopButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gui/icons/stop.png"))); // NOI18N
        stopButton.setToolTipText("Stop running machine");
        stopButton.setDisabledIcon(new javax.swing.ImageIcon(getClass().getResource("/gui/icons/stopDisabled.png"))); // NOI18N
        stopButton.setFocusable(false);
        stopButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        stopButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        stopButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                stopButtonActionPerformed(evt);
            }
        });
        toolBar.add(stopButton);

        progressBar.setIndeterminate(true);
        progressBar.setString("");
        progressBar.setStringPainted(true);
        progressBar.setVisible(false);
        toolBar.add(progressBar);

        abortButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gui/icons/abortBuild.png"))); // NOI18N
        abortButton.setText("Abort");
        abortButton.setToolTipText("Abort running machine (i.e. stop within step calculation)");
        abortButton.setFocusable(false);
        abortButton.setVisible(false);
        abortButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                abortButtonActionPerformed(evt);
            }
        });
        toolBar.add(abortButton);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(toolBar, javax.swing.GroupLayout.DEFAULT_SIZE, 929, Short.MAX_VALUE)
            .addComponent(mainSplit, javax.swing.GroupLayout.DEFAULT_SIZE, 929, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(toolBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(mainSplit, javax.swing.GroupLayout.DEFAULT_SIZE, 445, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

	private void addingModeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addingModeButtonActionPerformed
		if (this.circuitPanelGroup.getCurrentView().isEditable()) {
			this.circuitPanelGroup.getCurrentView().setInAddingMode(this.addingModeButton.isSelected());
			this.deletingModeButton.setSelected(this.circuitPanelGroup.getCurrentView().isInRemovingMode());
		}
	}//GEN-LAST:event_addingModeButtonActionPerformed

	private void deletingModeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deletingModeButtonActionPerformed
		if (this.circuitPanelGroup.getCurrentView().isEditable()) {
			this.circuitPanelGroup.getCurrentView().setInRemovingMode(this.deletingModeButton.isSelected());
			this.addingModeButton.setSelected(this.circuitPanelGroup.getCurrentView().isInAddingMode());
		}
	}//GEN-LAST:event_deletingModeButtonActionPerformed

	private void buildButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buildButtonActionPerformed
		MachinePanel.this.setState(MachinePanel.State.BUILDING, false);

		final BuildProgressDialog progressDialog = new BuildProgressDialog(Properties.getProfile().getMainWindow(), true);

		Thread builderThread = new Thread() {

			@Override
			public void run() {
				long linkingCount = 0;
				progressDialog.setTotalLinking(MachinePanel.this.circuitManager.getComponentCount());
				ComponentWrapper wrapper = MachinePanel.this.circuitManager.getComponentList().start;
				while (wrapper != null) {
					if (wrapper.isFullyConnected()) {
						if (!wrapper.getComponent().hasParent())
							MachinePanel.this.machine.addComponent(wrapper.getComponent());
						wrapper.configureComponent();
					} else
						MachinePanel.this.machine.removeComponent(wrapper.getComponent().getShortName());
					progressDialog.setCurrentLinking(++linkingCount);
					wrapper = wrapper.getNextComponent();
				}

				Simulator simulator = null;
				try {
					simulator = MachinePanel.this.machine.build(progressDialog, progressDialog);
				} catch (AnalyzeException ex) {
					setState(MachinePanel.State.DIRTY, false);
				} catch (BuildException ex) {
					setState(MachinePanel.State.DIRTY, false);
				} finally {
					progressDialog.setVisible(false);
					MachinePanel.this.circuitManager.fireEvent(
						new CircuitModificationEvent(MachinePanel.this.circuitManager, CircuitModificationEvent.TYPE_BUILT));
				}
				MachinePanel.this.setState(MachinePanel.State.CLEAN, false);
				if (simulator != null) {
					Signal clockSignal = null;
					if (SpecialConnector.getConnectors(SpecialConnector.Type.CLOCK).get(0).isConnected(0, 0))
						clockSignal = SpecialConnector.getConnectors(SpecialConnector.Type.CLOCK).get(0).getLinkage(0, 0).getSignal();
					if (clockSignal == null)
						clockSignal = new Signal(1);
					MachinePanel.this.clockSimulator = new ClockSimulator(simulator, clockSignal, Bit.L);
					MachinePanel.this.clockSimulator.addClockListener(MachinePanel.this);
					MachinePanel.this.clockSimulator.addStateListener(MachinePanel.this);
					MachinePanel.this.clockSimulator.addStepListener(MachinePanel.this);
				} else
					MachinePanel.this.clockSimulator = null;
			}
		};
		builderThread.start();
		try {
			builderThread.join(1000);
		} catch (InterruptedException ex) {
			// TODO: Do something sensible here
		}
		if (builderThread.isAlive())
			progressDialog.setVisible(true);
	}//GEN-LAST:event_buildButtonActionPerformed

	private void runButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_runButtonActionPerformed
		this.automaticSteps = true;
		this.automaticClock = true;
		this.runSimulation();
	}//GEN-LAST:event_runButtonActionPerformed

	private void runClockStepButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_runClockStepButtonActionPerformed
		this.automaticSteps = true;
		this.automaticClock = false;
		this.runSimulation();
	}//GEN-LAST:event_runClockStepButtonActionPerformed

	private void runStepButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_runStepButtonActionPerformed
		this.automaticSteps = false;
		this.automaticClock = false;
		this.runSimulation();
	}//GEN-LAST:event_runStepButtonActionPerformed

	private void stopButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_stopButtonActionPerformed
		if (this.clockSimulator != null)
			this.clockSimulator.shutdown();
	}//GEN-LAST:event_stopButtonActionPerformed

	private void abortButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_abortButtonActionPerformed
		/* Todo: Implement a routine here which kills the clocksimulator using
		 * stop(). Before this can be done the ClockSimulator and therefore the
		 * simulator muss support proper catching of ThreadDeath errors to kill
		 * it own worker threads. To get back to a clean state this routine must
		 * then replace all signals in the circuit, replace all components (but
		 * not the wrappers; implement a function in the wrappers) and make a
		 * complete rebuild.
		 */
	}//GEN-LAST:event_abortButtonActionPerformed

	private void pauseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pauseButtonActionPerformed
		if (this.clockSimulator != null)
			this.clockSimulator.suspend();
	}//GEN-LAST:event_pauseButtonActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel AvailableComponentsInnerPanel;
    private javax.swing.JPanel AvailableComponentsOuterPanel;
    private javax.swing.JButton abortButton;
    private javax.swing.JToggleButton addingModeButton;
    private javax.swing.JLabel availableComponentsLabel;
    private javax.swing.tree.DefaultTreeCellRenderer availableComponentsRenderer;
    private gui.ComponentTree availableComponentsTree;
    private javax.swing.JScrollPane availableComponentsTreeScroll;
    private javax.swing.JButton buildButton;
    private gui.CircuitPanelGroup circuitPanelGroup;
    private javax.swing.JToggleButton deletingModeButton;
    private javax.swing.JToolBar.Separator jSeparator1;
    private javax.swing.JToolBar.Separator jSeparator2;
    private javax.swing.JSplitPane mainSplit;
    private javax.swing.JButton pauseButton;
    private javax.swing.JProgressBar progressBar;
    private javax.swing.JButton runButton;
    private javax.swing.JButton runClockStepButton;
    private javax.swing.JButton runStepButton;
    private javax.swing.JSplitPane sideSplit;
    private javax.swing.JButton stopButton;
    private javax.swing.JToolBar toolBar;
    private javax.swing.JPanel viewStackInnerPanel;
    private javax.swing.JLabel viewStackLabel;
    private javax.swing.JPanel viewStackOuterPanel;
    private javax.swing.JScrollPane viewStackTreeScroll;
    // End of variables declaration//GEN-END:variables
	private Machine machine;
	private CircuitManager circuitManager;
	private State state;
	private State staticState;
	private ViewStackTree viewStackTree = new ViewStackTree();
	private ClockSimulator clockSimulator;
	private boolean automaticSteps = false;
	private boolean automaticClock = false;

	public final boolean notifyComponentAdditionAttempt(Extension extension, Point position) {
		// Set position
		position.setLocation(
			position.getX() - this.circuitPanelGroup.getCurrentView().getScrollOffset().x,
			position.getY() - this.circuitPanelGroup.getCurrentView().getScrollOffset().y);

		if (this.circuitManager.addComponent(extension, position))
			return true;
		else
			return false;
	}

	public final boolean notifyComponentAdditionAttempt(Point position) {
		TreePath path = this.availableComponentsTree.getSelectionPath();
		if (path == null)
			return false;
		DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) path.getLastPathComponent();
		if (selectedNode.getUserObject() instanceof Extension)
			return this.notifyComponentAdditionAttempt((Extension) selectedNode.getUserObject(), position);
		else
			return false;
	}

	public final boolean notifyComponentRemovalAttempt(ComponentWrapper wrapper) {
		// Remove from manager
		if (this.circuitManager.removeComponent(wrapper))
			return true;
		else
			return false;
	}

	public final boolean notifySignalConnectionAttempt(CircuitManager manager, Connector connector1, Connector connector2, int line1, int line2, int bit1, int bit2) {
		if (manager == this.circuitManager)
			if (connector2 != null)
				return manager.addLinkage(connector1, connector2, line1, line2, bit1, bit2);
			else
				try {
					return manager.addLinkage(connector1, line1, bit1);
				} catch (IllegalLinkageException ex) {
					JOptionPane.showMessageDialog(
						Properties.getProfile().getMainWindow(),
						ex.getLocalizedMessage(),
						"Linkage error",
						JOptionPane.ERROR_MESSAGE);
					return false;
				}
		else
			return false;
	}

	public Machine getMachine() {
		return this.machine;
	}

	public CircuitPanelGroup getCircuitPanelGroup() {
		return circuitPanelGroup;
	}

	private void setState(State state, boolean reluctant) {
		if (state.isStatic())
			this.staticState = state;
		if (!reluctant || this.state.isStatic() || (this.state.isDynamic() && state.isDynamic()))
			this.state = state;

		switch (state) {
			case CLEAN:
				this.addingModeButton.setEnabled(this.circuitPanelGroup.getCurrentView().isEditable());
				this.deletingModeButton.setEnabled(this.circuitPanelGroup.getCurrentView().isEditable());
				this.buildButton.setEnabled(true);
				this.runButton.setEnabled(true);
				this.runClockStepButton.setEnabled(true);
				this.runStepButton.setEnabled(true);
				this.pauseButton.setEnabled(false);
				this.stopButton.setEnabled(false);
				this.progressBar.setVisible(false);
				this.abortButton.setVisible(false);
				break;
			case DIRTY:
				this.addingModeButton.setEnabled(this.circuitPanelGroup.getCurrentView().isEditable());
				this.deletingModeButton.setEnabled(this.circuitPanelGroup.getCurrentView().isEditable());
				this.buildButton.setEnabled(true);
				this.runButton.setEnabled(false);
				this.runClockStepButton.setEnabled(false);
				this.runStepButton.setEnabled(false);
				this.pauseButton.setEnabled(false);
				this.stopButton.setEnabled(false);
				this.progressBar.setVisible(false);
				this.abortButton.setVisible(false);
				break;
			case BUILDING:
				this.addingModeButton.setEnabled(false);
				this.deletingModeButton.setEnabled(false);
				this.buildButton.setEnabled(false);
				this.runButton.setEnabled(false);
				this.runClockStepButton.setEnabled(false);
				this.runStepButton.setEnabled(false);
				this.pauseButton.setEnabled(false);
				this.stopButton.setEnabled(false);
				this.progressBar.setVisible(true);
				this.progressBar.setIndeterminate(true);
				this.progressBar.setString("Building");
				this.abortButton.setVisible(false);
				break;
			case SIMULATION_RUNNING:
				this.addingModeButton.setEnabled(false);
				this.deletingModeButton.setEnabled(false);
				this.buildButton.setEnabled(false);
				this.runButton.setEnabled(false);
				this.runClockStepButton.setEnabled(false);
				this.runStepButton.setEnabled(false);
				this.pauseButton.setEnabled(true);
				this.stopButton.setEnabled(true);
				this.progressBar.setVisible(true);
				this.progressBar.setIndeterminate(true);
				this.progressBar.setString("Simulation running");
				this.abortButton.setVisible(true);
				this.abortButton.setEnabled(true);
				break;
			case SIMULATION_WAITING:
				this.addingModeButton.setEnabled(false);
				this.deletingModeButton.setEnabled(false);
				this.buildButton.setEnabled(false);
				this.runButton.setEnabled(true);
				this.runClockStepButton.setEnabled(true);
				this.runStepButton.setEnabled(true);
				this.pauseButton.setEnabled(false);
				this.stopButton.setEnabled(true);
				this.progressBar.setVisible(true);
				this.progressBar.setIndeterminate(false);
				this.progressBar.setString("Simulation waiting");
				this.abortButton.setVisible(true);
				this.abortButton.setEnabled(true);
				break;
		}
	}

	private void setToStaticState() {
		if (this.staticState != this.state)
			this.setState(this.staticState, false);
	}

	@Override
	public void finishedClock() {
		if (!this.automaticClock)
			this.clockSimulator.suspend();
	}

	@Override
	public void finishedStep() {
		if (!this.automaticSteps)
			this.clockSimulator.suspend();
	}

	@Override
	public void changedState() {
		if (this.clockSimulator != null) {
			if (this.clockSimulator.isShutdowned()) {
				this.setToStaticState();
				ViewManager.getInstance().repaintAllViews();
				return;
			}
			if (this.clockSimulator.isSuspended()) {
				this.setState(State.SIMULATION_WAITING, false);
				ViewManager.getInstance().repaintAllViews();
				return;
			}
			this.setState(State.SIMULATION_RUNNING, false);
		}
	}

	// @sylvester: TODO
	@Override
	public void serialize(SerializingStream out) throws IOException, SerializingException {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void circuitChanged(CircuitModificationEvent evt) {
		if ((evt.isWrapperEvent() && (evt.getType() == WrapperEvent.TYPE_ADDITON || evt.getType() == WrapperEvent.TYPE_REMOVAL || evt.getType() == WrapperEvent.TYPE_CONFIGURED)) ||
			(evt.isLinkageEvent() && (evt.getType() == LinkageEvent.TYPE_ADDITON || evt.getType() == LinkageEvent.TYPE_REMOVAL)))
			this.setState(State.DIRTY, true);
	}

	private void runSimulation() {
		if (this.clockSimulator != null) {
			this.circuitManager.cleanLinkageGroups();
			if (this.clockSimulator.isShutdowned())
				this.clockSimulator.restart();
			if (this.clockSimulator.isSuspended())
				this.clockSimulator.resume();
		}
	}
}
