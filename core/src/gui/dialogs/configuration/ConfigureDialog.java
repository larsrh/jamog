
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
package gui.dialogs.configuration;

import core.build.Flavor;
import core.build.checking.types.ArrayType;
import core.build.checking.types.CollectionType;
import core.build.checking.types.MapType;
import core.build.checking.types.NullableType;
import core.build.checking.types.SimpleType;
import core.build.checking.types.Type;
import core.signal.Signal;
import gui.circuit.ComponentWrapper;
import gui.circuit.management.Connector;
import gui.dialogs.configuration.ConfigureDialog.ParameterNode;
import gui.exception.ParameterCreationException;
import gui.util.Properties;
import java.awt.CardLayout;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JOptionPane;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

/**
 *
 * @author sylvester
 */
public class ConfigureDialog extends javax.swing.JDialog {

	class ParameterNode extends DefaultMutableTreeNode {

		private ConfigurationPanel.ParameterType parameterType;
		private String name;
		private Set<ConfigurationPanel> configurationPanels = new HashSet<ConfigurationPanel>();
		private Object value;

		ParameterNode(Type type, ConfigurationPanel.ParameterType parameterType, String name, Object value) throws ParameterCreationException {
			super(type);
			this.parameterType = parameterType;
			this.name = name;

			if (!(type instanceof NullableType) && value == null)
				this.value = TypesHelper.getDefaultValue(type);
			else
				this.value = value;
		}

		@Override
		public Type getUserObject() {
			return (Type) super.getUserObject();
		}

		public Type getType() {
			return this.getUserObject();
		}

		String getName() {
			return name;
		}

		ConfigurationPanel.ParameterType getParameterType() {
			return parameterType;
		}

		Set<ConfigurationPanel> getConfigurationPanels() {
			return Collections.unmodifiableSet(configurationPanels);
		}

		boolean addConfigurationPanel(ConfigurationPanel configurationPanel) {
			return this.configurationPanels.add(configurationPanel);
		}

		boolean removeConfigurationPanel(ConfigurationPanel configurationPanel) {
			return this.configurationPanels.remove(configurationPanel);
		}

		void setValue(Object newValue) {
			if (!this.value.equals(newValue)) {
				if (this.getType() instanceof ArrayType) {
					ArrayType arrayType = (ArrayType) this.getType();
					if ((Integer) this.value < (Integer) newValue) {
						List<ParameterNode> newChildNodes = new LinkedList<ParameterNode>();
						for (int i = (Integer) this.value; i < (Integer) newValue; i++)
							try {
								newChildNodes.add(new ParameterNode(arrayType.getElementType(), this.parameterType, this.name + "[" + i + "]", null));
							} catch (ParameterCreationException ex) {
								return; // Addition of child failed, do not add children or change value
							}
						for (ParameterNode childNode : newChildNodes)
							this.add(childNode);
					} else
						for (int i = (Integer) this.value - 1; i >= (Integer) newValue; i--)
							this.remove(i);
					ConfigureDialog.this.treeModel.reload(this);
				}

				this.value = newValue;
				for (ConfigurationPanel panel : this.configurationPanels)
					panel.updateValue();
				this.propagateValueChange(0);
			}
		}

		private void propagateValueChange(int depth) {
			if (depth > 0)
				for (ConfigurationPanel panel : this.configurationPanels)
					panel.updateDescendant(depth);
			if (this.parent instanceof ParameterNode)
				((ParameterNode) this.parent).propagateValueChange(depth + 1);
		}

		Object getValue() {
			return value;
		}

		Object getCastedValue() throws ParameterCreationException {
			if (this.getChildCount() > 0) {
				Object[] children = new Object[this.getChildCount()];
				for (int i = 0; i < children.length; i++)
					children[i] = this.getChildAt(i).getCastedValue();
				return TypesHelper.castValue(this.getType(), this.value, children);
			} else
				return TypesHelper.castValue(this.getType(), this.value, null);
		}

		@Override
		public void remove(int childIndex) {
			for (ConfigurationPanel panel : this.getChildAt(childIndex).getConfigurationPanels()) {
				ConfigureDialog.this.configurationPanelContainer.remove(panel);
				ConfigureDialog.this.advancedViewConfigurationPanels.remove(panel);
			}
			super.remove(childIndex);
		}

		@Override
		public ParameterNode getChildAt(int index) {
			try {
				return (ParameterNode) super.getChildAt(index);
			} catch (ClassCastException exc) {
				return null;
			}
		}

		@Override
		public String toString() {
			String value = this.getName();
			if (this.getType() instanceof ArrayType)
				value += " [Array]";
			else if (this.getType() instanceof MapType)
				value += " [Map]";
			else if (this.getType() instanceof CollectionType)
				value += " [Collection]";
			return value;
		}

	}

	/**
	 * Creates new form ConfigureDialog (only for GUI Designer)
	 *
	 * @param parent
	 * @param modal
	 *
	 * @deprecated
	 */
	public ConfigureDialog(java.awt.Frame parent, boolean modal) {
		super(parent, modal);
		initComponents();
	}

	public ConfigureDialog(java.awt.Frame parent, ComponentWrapper wrapper) {
		this(parent, true);
		this.setLocationRelativeTo(Properties.getProfile().getMainWindow());
		this.labelErrorMessage.setVisible(false);
		if (!wrapper.isConfigured())
			this.labelWarningMessage.setVisible(false);
		this.wrapper = wrapper;
		String[] flavorNames = wrapper.getComponent().getFlavors().keySet().
				toArray(new String[0]);
		this.comboBoxFlavor.setModel(new DefaultComboBoxModel(flavorNames));
		this.comboBoxFlavor.setEnabled(this.comboBoxFlavor.getItemCount() > 1);
		this.selectFlavor();
		this.buildParameterTree();
	}

	/** This method is called from within the constructor to
	 * initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is
	 * always regenerated by the Form Editor.
	 */
	@SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        labelFlavor = new javax.swing.JLabel();
        comboBoxFlavor = new javax.swing.JComboBox();
        labelErrorMessage = new javax.swing.JLabel();
        labelWarningMessage = new javax.swing.JLabel();
        cardsPanel = new javax.swing.JPanel();
        simpleGuiPanel = new javax.swing.JPanel();
        scrollpaneParameterList = new javax.swing.JScrollPane();
        panelParameterList = new javax.swing.JPanel();
        advancedGuiPanel = new javax.swing.JSplitPane();
        parameterTreeScroll = new javax.swing.JScrollPane();
        rootNode = new javax.swing.tree.DefaultMutableTreeNode();
        treeModel = new javax.swing.tree.DefaultTreeModel(rootNode);
        parameterTree = new javax.swing.JTree();
        configurationPanelScroll = new javax.swing.JScrollPane();
        configurationPanelContainer = new javax.swing.JPanel();
        viewToggleButton = new javax.swing.JToggleButton();
        buttonCancel = new javax.swing.JButton();
        buttonOk = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Set component properties");

        labelFlavor.setLabelFor(comboBoxFlavor);
        labelFlavor.setText("Flavor:");

        comboBoxFlavor.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Default" }));
        comboBoxFlavor.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                comboBoxFlavorActionPerformed(evt);
            }
        });

        labelErrorMessage.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        labelErrorMessage.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gui/icons/famfamfam/exclamation.png"))); // NOI18N
        labelErrorMessage.setText("The bit widths you entered are not applicable. Please change them.");
        labelErrorMessage.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(253, 38, 38), 2, true));
        labelErrorMessage.setVisible(false);

        labelWarningMessage.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        labelWarningMessage.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gui/icons/famfamfam/error.png"))); // NOI18N
        labelWarningMessage.setText("Chaning the bit widths will delete all existing connections of this component.");
        labelWarningMessage.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(255, 213, 0), 2, true));
        labelErrorMessage.setVisible(false);

        cardsPanel.setLayout(new java.awt.CardLayout());

        scrollpaneParameterList.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollpaneParameterList.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        panelParameterList.setLayout(new java.awt.GridLayout(0, 1, 0, 20));
        scrollpaneParameterList.setViewportView(panelParameterList);

        javax.swing.GroupLayout simpleGuiPanelLayout = new javax.swing.GroupLayout(simpleGuiPanel);
        simpleGuiPanel.setLayout(simpleGuiPanelLayout);
        simpleGuiPanelLayout.setHorizontalGroup(
            simpleGuiPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(scrollpaneParameterList, javax.swing.GroupLayout.DEFAULT_SIZE, 771, Short.MAX_VALUE)
        );
        simpleGuiPanelLayout.setVerticalGroup(
            simpleGuiPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(scrollpaneParameterList, javax.swing.GroupLayout.DEFAULT_SIZE, 247, Short.MAX_VALUE)
        );

        cardsPanel.add(simpleGuiPanel, "simpleView");

        advancedGuiPanel.setDividerLocation(230);

        parameterTree.setModel(treeModel);
        parameterTree.setRootVisible(false);
        parameterTree.addTreeSelectionListener(new javax.swing.event.TreeSelectionListener() {
            public void valueChanged(javax.swing.event.TreeSelectionEvent evt) {
                parameterTreeValueChanged(evt);
            }
        });
        parameterTreeScroll.setViewportView(parameterTree);

        advancedGuiPanel.setLeftComponent(parameterTreeScroll);

        configurationPanelScroll.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        configurationPanelScroll.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        configurationPanelContainer.setLayout(new java.awt.GridLayout(0, 1, 0, 20));
        configurationPanelScroll.setViewportView(configurationPanelContainer);

        advancedGuiPanel.setRightComponent(configurationPanelScroll);

        cardsPanel.add(advancedGuiPanel, "advancedView");

        viewToggleButton.setText("Advanced view");
        viewToggleButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                viewToggleButtonActionPerformed(evt);
            }
        });

        buttonCancel.setText("Cancel");
        buttonCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonCancelActionPerformed(evt);
            }
        });

        buttonOk.setText("Ok");
        this.getRootPane().setDefaultButton(this.buttonOk);
        buttonOk.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonOkActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(cardsPanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 771, Short.MAX_VALUE)
                    .addComponent(labelErrorMessage, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 771, Short.MAX_VALUE)
                    .addComponent(labelWarningMessage, javax.swing.GroupLayout.DEFAULT_SIZE, 771, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(labelFlavor)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 117, Short.MAX_VALUE)
                        .addComponent(comboBoxFlavor, javax.swing.GroupLayout.PREFERRED_SIZE, 612, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(viewToggleButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 399, Short.MAX_VALUE)
                        .addComponent(buttonCancel, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(buttonOk, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(comboBoxFlavor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(labelFlavor))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(labelErrorMessage, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(labelWarningMessage, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(cardsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 247, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(buttonCancel)
                    .addComponent(buttonOk)
                    .addComponent(viewToggleButton))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

	private void buttonOkActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonOkActionPerformed
		Map<String, List<Integer>> signalDimensions = new HashMap<String, List<Integer>>(this.parameters.size());
		List<Connector> connectors = new LinkedList<Connector>();
		Map<String, Object> parameters = new HashMap<String, Object>();

		if (this.rootNode.getChildCount() > 0) {
			ParameterNode currentNode = (ParameterNode) this.rootNode.getFirstChild();
			do {
				ParameterNode stepDownNode = currentNode;
				List<Integer> dimensions = new LinkedList<Integer>();

				while (stepDownNode.getChildCount() > 0) {
					dimensions.add(stepDownNode.getChildCount());
					stepDownNode = (ParameterNode) stepDownNode.getFirstChild();
				}
				if (stepDownNode.getUserObject() instanceof SimpleType &&
						stepDownNode.getValue() instanceof Signal)
					dimensions.add(((Signal) stepDownNode.getValue()).size());
				signalDimensions.put(currentNode.getName(), dimensions);

				if (currentNode.getUserObject() instanceof SimpleType &&
						currentNode.getUserObject().canAssign(Signal.class))
					connectors.add(new Connector(currentNode.getName(),
							currentNode.getParameterType() == ConfigurationPanel.ParameterType.INPUT ? Connector.Type.INPUT : Connector.Type.OUTPUT,
							1,
							((Signal) currentNode.getValue()).size()));
				else if (currentNode.getUserObject() instanceof ArrayType &&
						currentNode.getUserObject().canAssign(Signal[].class) &&
						currentNode.getChildCount() > 0)
					connectors.add(new Connector(currentNode.getName(),
							currentNode.getParameterType() == ConfigurationPanel.ParameterType.INPUT ? Connector.Type.INPUT : Connector.Type.OUTPUT,
							currentNode.getChildCount(),
							((Signal) currentNode.getChildAt(0).getValue()).size()));
				else
					try {
						parameters.put(currentNode.getName(), currentNode.getCastedValue());
					} catch (ParameterCreationException ex) {
						// TODO: Use global error management
						new JOptionPane("Could not calculate values from given data.", JOptionPane.ERROR_MESSAGE);
						return;
					}
			} while ((currentNode = (ParameterNode) currentNode.getNextSibling()) != null);
		}

		if (this.flavor.checkDimensions(signalDimensions)) {
			this.wrapper.setFalvor(this.flavorName);
			this.wrapper.setConnectors(connectors.toArray(new Connector[connectors.size()]));
			this.wrapper.setParameters(parameters);
			this.dispose();
		} else
			this.labelErrorMessage.setVisible(true);
}//GEN-LAST:event_buttonOkActionPerformed

	private void buttonCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonCancelActionPerformed
		this.dispose();
	}//GEN-LAST:event_buttonCancelActionPerformed

	private void comboBoxFlavorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_comboBoxFlavorActionPerformed
		this.selectFlavor();
		this.buildParameterTree();
	}//GEN-LAST:event_comboBoxFlavorActionPerformed

	private void viewToggleButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_viewToggleButtonActionPerformed
		if (this.viewToggleButton.isSelected())
			((CardLayout) this.cardsPanel.getLayout()).show(this.cardsPanel, "advancedView");
		else
			((CardLayout) this.cardsPanel.getLayout()).show(this.cardsPanel, "simpleView");
	}//GEN-LAST:event_viewToggleButtonActionPerformed

	private void parameterTreeValueChanged(javax.swing.event.TreeSelectionEvent evt) {//GEN-FIRST:event_parameterTreeValueChanged
		for (ConfigurationPanel panel : this.advancedViewConfigurationPanels) {
			panel.getListeningNode().removeConfigurationPanel(panel);
			this.configurationPanelContainer.remove(panel);
		}
		if (!this.parameterTree.isSelectionEmpty())
			for (TreePath path : this.parameterTree.getSelectionPaths()) {
				ParameterNode node = (ParameterNode) path.getLastPathComponent();
				ConfigurationPanel panel = null;
				if (node.getUserObject() instanceof SimpleType) {
					if (node.getUserObject().canAssign(Boolean.class)) {
						panel = new BooleanConfiguration(node);
					} else if (node.getUserObject().canAssign(Byte.class))
						panel = new NumberConfiguration(NumberConfiguration.Type.BYTE, node);
					else if (node.getUserObject().canAssign(Short.class))
						panel = new NumberConfiguration(NumberConfiguration.Type.SHORT, node);
					else if (node.getUserObject().canAssign(Integer.class))
						panel = new NumberConfiguration(NumberConfiguration.Type.INTEGER, node);
					else if (node.getUserObject().canAssign(Long.class))
						panel = new NumberConfiguration(NumberConfiguration.Type.LONG, node);
					else if (node.getUserObject().canAssign(Float.class))
						panel = new NumberConfiguration(NumberConfiguration.Type.FLOAT, node);
					else if (node.getUserObject().canAssign(Double.class))
						panel = new NumberConfiguration(NumberConfiguration.Type.DOUBLE, node);
					else if (node.getUserObject().canAssign(String.class))
						panel = new StringConfiguration(node);
					else if (node.getUserObject().canAssign(Signal.class))
						panel = new NumberConfiguration(NumberConfiguration.Type.SIGNAL, node);
					else if (Type.getType(Enum.class).canAssign(((SimpleType) node.getUserObject()).getWrappedClass()))
						panel = new EnumConfiguration(node);
				} else if (node.getUserObject() instanceof ArrayType)
					panel = new ArrayConfiguration(node);
				node.addConfigurationPanel(panel);
				this.advancedViewConfigurationPanels.add(panel);
				this.configurationPanelContainer.add(panel);
			}
		this.configurationPanelContainer.revalidate();
	}//GEN-LAST:event_parameterTreeValueChanged
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JSplitPane advancedGuiPanel;
    private javax.swing.JButton buttonCancel;
    private javax.swing.JButton buttonOk;
    private javax.swing.JPanel cardsPanel;
    private javax.swing.JComboBox comboBoxFlavor;
    private javax.swing.JPanel configurationPanelContainer;
    private javax.swing.JScrollPane configurationPanelScroll;
    private javax.swing.JLabel labelErrorMessage;
    private javax.swing.JLabel labelFlavor;
    private javax.swing.JLabel labelWarningMessage;
    private javax.swing.JPanel panelParameterList;
    private javax.swing.tree.DefaultMutableTreeNode rootNode;
    private javax.swing.tree.DefaultTreeModel treeModel;
    private javax.swing.JTree parameterTree;
    private javax.swing.JScrollPane parameterTreeScroll;
    private javax.swing.JScrollPane scrollpaneParameterList;
    private javax.swing.JPanel simpleGuiPanel;
    private javax.swing.JToggleButton viewToggleButton;
    // End of variables declaration//GEN-END:variables
	private ComponentWrapper wrapper;
	private Flavor flavor;
	private String flavorName;
	private List<SignalArrayConfiguration> parameters = new LinkedList<SignalArrayConfiguration>();
	private List<ConfigurationPanel> advancedViewConfigurationPanels = new LinkedList<ConfigurationPanel>();

	private void buildParameterTree() {
		try {
			this.rootNode.removeAllChildren();
			for (Entry<String, Type> entry : this.flavor.getInputs().entrySet())
				this.rootNode.add(new ParameterNode(entry.getValue(), ConfigurationPanel.ParameterType.INPUT, entry.getKey(), null));
			for (Entry<String, Type> entry : this.flavor.getOutputs().entrySet())
				this.rootNode.add(new ParameterNode(entry.getValue(), ConfigurationPanel.ParameterType.OUTPUT, entry.getKey(), null));
			for (Entry<String, Type> entry : this.flavor.getParameters().entrySet())
				this.rootNode.add(new ParameterNode(entry.getValue(), ConfigurationPanel.ParameterType.PARAMETER, entry.getKey(), null));
			this.buildSimpleView();
			this.buildAdvancedView();
		} catch (ParameterCreationException ex) {
			// TODO: Use global error management
			new JOptionPane("Could not calculate values from given data.", JOptionPane.ERROR_MESSAGE);
			return;
		}
	}

	private void buildAdvancedView() {
		this.treeModel.reload();
	}

	private void buildSimpleView() {
		this.panelParameterList.removeAll();
		if (this.rootNode.getChildCount() > 0) {
			ParameterNode currentNode = (ParameterNode) this.rootNode.getFirstChild();
			do
				if (currentNode.getUserObject() instanceof SimpleType) {
					if (currentNode.getUserObject().canAssign(Signal.class)) {
						ConfigurationPanel panel = new SignalArrayConfiguration(currentNode);
						currentNode.addConfigurationPanel(panel);
						this.panelParameterList.add(panel);
					}
				} else if (currentNode.getUserObject() instanceof ArrayType) {
					Type innerType = ((ArrayType) currentNode.getUserObject()).getElementType();
					if (innerType instanceof SimpleType)
						if (innerType.canAssign(Signal.class)) {
							ConfigurationPanel panel = new SignalArrayConfiguration(currentNode);
							currentNode.addConfigurationPanel(panel);
							this.panelParameterList.add(panel);
						}
				}
			while ((currentNode = (ParameterNode) currentNode.getNextSibling()) != null);
		}
		this.panelParameterList.revalidate();
	}

	private void selectFlavor() {
		this.flavorName = (String) this.comboBoxFlavor.getSelectedItem();
		this.flavor = wrapper.getComponent().getFlavors().get(this.flavorName);
	}

}
