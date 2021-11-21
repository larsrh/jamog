
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
package gui.util;

import core.build.Component;
import core.build.Component.Extension;
import core.misc.module.ClassLoader;
import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.beans.VetoableChangeSupport;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import javax.swing.JFrame;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import main.MessageManager;

/**
 * TODO: Add suppport for adding/removing profiles. Since the listeners and
 * stuff must be preserved this should be done by managing the profiles as
 * Properties-Object in a map and on request copying the properies (and thus
 * fireing property change events).
 * 
 * @author sylvester
 */
public final class Properties {

	private static final Map<String, Properties> profiles = new HashMap<String, Properties>();
	private static String currentProfile;
	private static Properties settings = new Properties();

	public static final Properties getProfile() {
		return Properties.settings;
	}

	private Properties() {
		/*
		 * Add a VetoableChangeListener to the extensionsSources property, which
		 * will load the extensions and veto on error.
		 */
		this.addVetoableChangeListener(new VetoableChangeListener() {

			@Override
			public void vetoableChange(PropertyChangeEvent evt) throws PropertyVetoException {
				if (evt.getPropertyName().equals(Properties.PROP_EXTENSIONSOURCES))
					try {
						ClassLoader.load((File[]) evt.getNewValue());
					} catch (IOException exc1) {
						MessageManager.getInstance().createMessage(
								"Error while loading extensions",
								"An error occured while trying to load the new extensions. The old extensions have been restored.\nThe occured error is: " + exc1.toString(),
								MessageManager.MessageTypes.WARNING,
								MessageManager.MessageCategories.MESSAGE,
								exc1);
						try {
							ClassLoader.load((File[]) evt.getOldValue());
						} catch (IOException exc2) {
							MessageManager.getInstance().createMessage(
								"Error while loading extensions",
								"An error occured while trying to load the new extensions. Furthermore the old extensions could not be restored. The loaded extensions will be incomplete.\nThe occured errors are:\n1: " + exc1.toString() + "\n2:" + exc2.toString(),
								MessageManager.MessageTypes.ERROR,
								MessageManager.MessageCategories.MESSAGE,
								exc2);
						}
						throw new PropertyVetoException("Loading new extensions failed, property is blocked therefore.", evt);
					}
			}

		});
		/*
		 * Add a PropertyChangeListener to the extensionsSources property, to
		 * rebuild the availableComponentsTreeModel property accordingly
		 */
		this.addPropertyChangeListener(new PropertyChangeListener() {

			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				if (evt.getPropertyName().equals(Properties.PROP_EXTENSIONSOURCES))
					Properties.this.buildExtensionTree();
			}

		});
	}
	/*
	 *
	 * Property: mainWindow
	 */

	private JFrame mainWindow;

	/**
	 * Get the value of mainWindow
	 *
	 * @return the value of mainWindow
	 */
	public JFrame getMainWindow() {
		return mainWindow;
	}

	/**
	 * Set the value of mainWindow
	 *
	 * @param mainWindow new value of mainWindow
	 */
	public void setMainWindow(JFrame mainWindow) {
		if (this.mainWindow == null)
			this.mainWindow = mainWindow;
		else
			throw new IllegalStateException("Cannot reassign the mainWindow property.");
	}
	/*
	 *
	 * Property: defaultStroke
	 */

	private GuiStroke defaultStroke = new GuiStroke(1, GuiStroke.CAP_ROUND, GuiStroke.JOIN_MITER);
	public static final String PROP_DEFAULTSTROKE = "defaultStroke";

	/**
	 * Get the value of defaultStroke
	 *
	 * @return the value of defaultStroke
	 */
	public GuiStroke getDefaultStroke() {
		return defaultStroke;
	}

	/**
	 * Set the value of defaultStroke
	 *
	 * @param defaultStroke new value of defaultStroke
	 */
	public void setDefaultStroke(GuiStroke defaultStroke) {
		GuiStroke oldDefaultStroke = this.defaultStroke;
		this.defaultStroke = defaultStroke;
		propertyChangeSupport.firePropertyChange(PROP_DEFAULTSTROKE, oldDefaultStroke, defaultStroke);
	}
	/*
	 *
	 * Property: dashedStroke
	 */

	private GuiStroke dashedStroke = new GuiStroke(1, GuiStroke.CAP_ROUND, GuiStroke.JOIN_MITER, this.defaultStroke.getMiterLimit(), new float[]{5}, 0);
	public static final String PROP_DASHEDSTROKE = "dashedStroke";

	/**
	 * Get the value of dashedStroke
	 *
	 * @return the value of dashedStroke
	 */
	public GuiStroke getDashedStroke() {
		return dashedStroke;
	}

	/**
	 * Set the value of dashedStroke
	 *
	 * @param dashedStroke new value of dashedStroke
	 */
	public void setDashedStroke(GuiStroke dashedStroke) {
		GuiStroke oldDashedStroke = this.dashedStroke;
		this.dashedStroke = dashedStroke;
		propertyChangeSupport.firePropertyChange(PROP_DASHEDSTROKE, oldDashedStroke, dashedStroke);
	}
	/*
	 *
	 * Property: dottedStroke
	 */

	private GuiStroke dottedStroke = new GuiStroke(1, GuiStroke.CAP_ROUND, GuiStroke.JOIN_MITER, this.defaultStroke.getMiterLimit(), new float[]{1, 5}, 0);
	public static final String PROP_DOTTEDSTROKE = "dottedStroke";

	/**
	 * Get the value of dottedStroke
	 *
	 * @return the value of dottedStroke
	 */
	public GuiStroke getDottedStroke() {
		return dottedStroke;
	}

	/**
	 * Set the value of dottedStroke
	 *
	 * @param dottedStroke new value of dottedStroke
	 */
	public void setDottedStroke(GuiStroke dottedStroke) {
		GuiStroke oldDottedStroke = this.dottedStroke;
		this.dottedStroke = dottedStroke;
		propertyChangeSupport.firePropertyChange(PROP_DOTTEDSTROKE, oldDottedStroke, dottedStroke);
	}
	/*
	 *
	 * Property: bgColor
	 */

	private GuiColor bgColor = new GuiColor(255, 255, 255);
	public static final String PROP_BGCOLOR = "bgColor";

	/**
	 * Get the value of bgColor
	 *
	 * @return the value of bgColor
	 */
	public GuiColor getBgColor() {
		return bgColor;
	}

	/**
	 * Set the value of bgColor
	 *
	 * @param bgColor new value of bgColor
	 */
	public void setBgColor(GuiColor bgColor) {
		GuiColor oldBgColor = this.bgColor;
		this.bgColor = bgColor;
		propertyChangeSupport.firePropertyChange(PROP_BGCOLOR, oldBgColor, bgColor);
	}
	/*
	 *
	 * Property: textColor
	 */

	private GuiColor textColor = new GuiColor(0, 0, 0);
	public static final String PROP_TEXTCOLOR = "textColor";

	/**
	 * Get the value of textColor
	 *
	 * @return the value of textColor
	 */
	public GuiColor getTextColor() {
		return textColor;
	}

	/**
	 * Set the value of textColor
	 *
	 * @param textColor new value of textColor
	 */
	public void setTextColor(GuiColor textColor) {
		GuiColor oldTextColor = this.textColor;
		this.textColor = textColor;
		propertyChangeSupport.firePropertyChange(PROP_TEXTCOLOR, oldTextColor, textColor);
	}
	/*
	 *
	 * Property: strokeColor
	 */

	private GuiColor strokeColor = new GuiColor(0, 120, 0);
	public static final String PROP_STROKECOLOR = "strokeColor";

	/**
	 * Get the value of strokeColor
	 *
	 * @return the value of strokeColor
	 */
	public GuiColor getStrokeColor() {
		return strokeColor;
	}

	/**
	 * Set the value of strokeColor
	 *
	 * @param strokeColor new value of strokeColor
	 */
	public void setStrokeColor(GuiColor strokeColor) {
		GuiColor oldStrokeColor = this.strokeColor;
		this.strokeColor = strokeColor;
		propertyChangeSupport.firePropertyChange(PROP_STROKECOLOR, oldStrokeColor, strokeColor);
	}
	/*
	 *
	 * Property: fillColor
	 */

	private GuiColor fillColor = new GuiColor(120, 190, 115);
	public static final String PROP_FILLCOLOR = "fillColor";

	/**
	 * Get the value of fillColor
	 *
	 * @return the value of fillColor
	 */
	public GuiColor getFillColor() {
		return fillColor;
	}

	/**
	 * Set the value of fillColor
	 *
	 * @param fillColor new value of fillColor
	 */
	public void setFillColor(GuiColor fillColor) {
		GuiColor oldFillColor = this.fillColor;
		this.fillColor = fillColor;
		propertyChangeSupport.firePropertyChange(PROP_FILLCOLOR, oldFillColor, fillColor);
	}
	/*
	 *
	 * Property: ghostStrokeColor
	 */

	private GuiColor ghostStrokeColor = new GuiColor(0, 120, 0, 128);
	public static final String PROP_GHOSTSTROKECOLOR = "ghostStrokeColor";

	/**
	 * Get the value of ghostStrokeColor
	 *
	 * @return the value of ghostStrokeColor
	 */
	public GuiColor getGhostStrokeColor() {
		return ghostStrokeColor;
	}

	/**
	 * Set the value of ghostStrokeColor
	 *
	 * @param ghostStrokeColor new value of ghostStrokeColor
	 */
	public void setGhostStrokeColor(GuiColor ghostStrokeColor) {
		GuiColor oldGhostStrokeColor = this.ghostStrokeColor;
		this.ghostStrokeColor = ghostStrokeColor;
		propertyChangeSupport.firePropertyChange(PROP_GHOSTSTROKECOLOR, oldGhostStrokeColor, ghostStrokeColor);
	}
	/*
	 *
	 * Property: ghostFillColor
	 */

	private GuiColor ghostFillColor = new GuiColor(120, 190, 115, 128);
	public static final String PROP_GHOSTFILLCOLOR = "ghostFillColor";

	/**
	 * Get the value of ghostFillColor
	 *
	 * @return the value of ghostFillColor
	 */
	public GuiColor getGhostFillColor() {
		return ghostFillColor;
	}

	/**
	 * Set the value of ghostFillColor
	 *
	 * @param ghostFillColor new value of ghostFillColor
	 */
	public void setGhostFillColor(GuiColor ghostFillColor) {
		GuiColor oldGhostFillColor = this.ghostFillColor;
		this.ghostFillColor = ghostFillColor;
		propertyChangeSupport.firePropertyChange(PROP_GHOSTFILLCOLOR, oldGhostFillColor, ghostFillColor);
	}
	/*
	 *
	 * Property: selectionBGColor
	 */

	private GuiColor selectionBGColor = new GuiColor(35, 35, 240);
	public static final String PROP_SELECTIONBGCOLOR = "selectionBGColor";

	/**
	 * Get the value of selectionBGColor
	 *
	 * @return the value of selectionBGColor
	 */
	public GuiColor getSelectionBGColor() {
		return selectionBGColor;
	}

	/**
	 * Set the value of selectionColor
	 *
	 * @param selectionBGColor new value of selectionColor
	 */
	public void setSelectionBGColor(GuiColor selectionBGColor) {
		GuiColor oldSelectionBGColor = this.selectionBGColor;
		this.selectionBGColor = selectionBGColor;
		propertyChangeSupport.firePropertyChange(PROP_SELECTIONBGCOLOR, oldSelectionBGColor, selectionBGColor);
	}
	/*
	 *
	 * Property: selectionFGColor
	 */

	private GuiColor selectionFGColor = new GuiColor(255, 255, 255);
	public static final String PROP_SELECTIONFGCOLOR = "selectionFGColor";

	/**
	 * Get the value of selectionFGColor
	 *
	 * @return the value of selectionFGColor
	 */
	public GuiColor getSelectionFGColor() {
		return selectionFGColor;
	}

	/**
	 * Set the value of selectionFGColor
	 *
	 * @param selectionFGColor new value of selectionFGColor
	 */
	public void setSelectionFGColor(GuiColor selectionFGColor) {
		GuiColor oldSelectionFGColor = this.selectionFGColor;
		this.selectionFGColor = selectionFGColor;
		propertyChangeSupport.firePropertyChange(PROP_SELECTIONFGCOLOR, oldSelectionFGColor, selectionFGColor);
	}
	/*
	 *
	 * Property: pinColor
	 */

	private GuiColor pinColor = new GuiColor(0, 0, 0);
	public static final String PROP_PINCOLOR = "pinColor";

	/**
	 * Get the value of pinStrokeColor
	 *
	 * @return the value of pinColor
	 */
	public GuiColor getPinColor() {
		return pinColor;
	}

	/**
	 * Set the value of pinStrokeColor
	 *
	 * @param pinColor new value of pinColor
	 */
	public void setPinColor(GuiColor pinColor) {
		GuiColor oldPinColor = this.pinColor;
		this.pinColor = pinColor;
		propertyChangeSupport.firePropertyChange(PROP_PINCOLOR, oldPinColor, pinColor);
	}
	/*
	 * 
	 * Property: signalColor
	 */

	private GuiColor signalColor = new GuiColor(0, 120, 0);
	public static final String PROP_SIGNALCOLOR = "signalColor";

	/**
	 * Get the value of signalColor
	 *
	 * @return the value of signalColor
	 */
	public GuiColor getSignalColor() {
		return signalColor;
	}

	/**
	 * Set the value of signalColor
	 *
	 * @param signalColor new value of signalColor
	 */
	public void setSignalColor(GuiColor signalColor) {
		GuiColor oldSignalColor = this.signalColor;
		this.signalColor = signalColor;
		propertyChangeSupport.firePropertyChange(PROP_SIGNALCOLOR, oldSignalColor, signalColor);
	}
	/*
	 *
	 * Property: signalHighlightColor
	 */

	private GuiColor signalHighlightColor = new GuiColor(200, 0, 0);
	public static final String PROP_SIGNALHIGHLIGHTCOLOR = "signalHighlightColor";

	/**
	 * Get the value of signalHighlightColor
	 *
	 * @return the value of signalHighlightColor
	 */
	public GuiColor getSignalHighlightColor() {
		return signalHighlightColor;
	}

	/**
	 * Set the value of signalHighlightColor
	 *
	 * @param signalHighlightColor new value of signalHighlightColor
	 */
	public void setSignalHighlightColor(GuiColor signalHighlightColor) {
		GuiColor oldSignalHighlightColor = this.signalHighlightColor;
		this.signalHighlightColor = signalHighlightColor;
		propertyChangeSupport.firePropertyChange(PROP_SIGNALHIGHLIGHTCOLOR, oldSignalHighlightColor, signalHighlightColor);
	}
	/*
	 *
	 * Property: overlayColor
	 */

	private GuiColor overlayColor = new GuiColor(200, 200, 200);
	public static final String PROP_OVERLAYCOLOR = "overlayColor";

	/**
	 * Get the value of overlayColor
	 *
	 * @return the value of overlayColor
	 */
	public GuiColor getOverlayColor() {
		return overlayColor;
	}

	/**
	 * Set the value of overlayColor
	 *
	 * @param overlayColor new value of overlayColor
	 */
	public void setOverlayColor(GuiColor overlayColor) {
		GuiColor oldOverlayColor = this.overlayColor;
		this.overlayColor = overlayColor;
		propertyChangeSupport.firePropertyChange(PROP_OVERLAYCOLOR, oldOverlayColor, overlayColor);
	}
	/*
	 *
	 * Property: tooltipFGColor
	 */

	private GuiColor tooltipFGColor = new GuiColor(186, 186, 69);
	public static final String PROP_TOOLTIPFGCOLOR = "tooltipFGColor";

	/**
	 * Get the value of tooltipFGColor
	 *
	 * @return the value of tooltipFGColor
	 */
	public GuiColor getTooltipFGColor() {
		return tooltipFGColor;
	}

	/**
	 * Set the value of tooltipFGColor
	 *
	 * @param tooltipFGColor new value of tooltipFGColor
	 */
	public void setTooltipFGColor(GuiColor tooltipFGColor) {
		GuiColor oldTooltipFGColor = this.tooltipFGColor;
		this.tooltipFGColor = tooltipFGColor;
		propertyChangeSupport.firePropertyChange(PROP_TOOLTIPFGCOLOR, oldTooltipFGColor, tooltipFGColor);
	}
	/*
	 *
	 * Property: tooltipBGColor
	 */

	private GuiColor tooltipBGColor = new GuiColor(246, 246, 184);
	public static final String PROP_TOOLTIPBGCOLOR = "tooltipBGColor";

	/**
	 * Get the value of tooltipBGColor
	 *
	 * @return the value of tooltipBGColor
	 */
	public GuiColor getTooltipBGColor() {
		return tooltipBGColor;
	}

	/**
	 * Set the value of tooltipBGColor
	 *
	 * @param tooltipBGColor new value of tooltipBGColor
	 */
	public void setTooltipBGColor(GuiColor tooltipBGColor) {
		GuiColor oldTooltipBGColor = this.tooltipBGColor;
		this.tooltipBGColor = tooltipBGColor;
		propertyChangeSupport.firePropertyChange(PROP_TOOLTIPBGCOLOR, oldTooltipBGColor, tooltipBGColor);
	}
	/*
	 *
	 * Property: pinSize
	 */

	private int pinSize = 6;
	public static final String PROP_PINSIZE = "pinSize";

	/**
	 * Get the value of pinSize
	 *
	 * @return the value of pinSize
	 */
	public int getPinSize() {
		return pinSize;
	}

	/**
	 * Set the value of pinSize
	 *
	 * @param pinSize new value of pinSize
	 */
	public void setPinSize(int pinSize) {
		int oldPinSize = this.pinSize;
		this.pinSize = pinSize;
		propertyChangeSupport.firePropertyChange(PROP_PINSIZE, oldPinSize, pinSize);
	}
	/*
	 *
	 * Property: drawSignalValues
	 */

	private boolean drawSignalValues = false;
	public static final String PROP_DRAWSIGNALVALUES = "drawSignalValues";

	/**
	 * Get the value of drawSignalValues
	 *
	 * @return the value of drawSignalValues
	 */
	public boolean isDrawSignalValues() {
		return drawSignalValues;
	}

	/**
	 * Set the value of drawSignalValues
	 *
	 * @param drawSignalValues new value of drawSignalValues
	 */
	public void setDrawSignalValues(boolean drawSignalValues) {
		boolean oldDrawSignalValues = this.drawSignalValues;
		this.drawSignalValues = drawSignalValues;
		propertyChangeSupport.firePropertyChange(PROP_DRAWSIGNALVALUES, oldDrawSignalValues, drawSignalValues);
	}
	/*
	 *
	 * Property: pinSelectionHaziness
	 */

	private int pinSelectionHaziness = 10;

	/**
	 * Get the value of pinSelectionHaziness
	 *
	 * @return the value of pinSelectionHaziness
	 */
	public int getPinSelectionHaziness() {
		return pinSelectionHaziness;
	}

	/**
	 * Set the value of pinSelectionHaziness
	 *
	 * @param pinSelectionHaziness new value of pinSelectionHaziness
	 */
	public void setPinSelectionHaziness(int pinSelectionHaziness) {
		this.pinSelectionHaziness = pinSelectionHaziness;
	}
	/*
	 *
	 * Property: extensionSources
	 */

	private File[] extensionSources = new File[0];
	public static final String PROP_EXTENSIONSOURCES = "extensionSources";

	/**
	 * Get the value of extensionSources
	 *
	 * @return the value of extensionSources
	 */
	public File[] getExtensionSources() {
		return extensionSources;
	}

	/**
	 * Set the value of extensionSources
	 *
	 * @param extensionSources new value of extensionSources
	 * @throws java.beans.PropertyVetoException
	 */
	public void setExtensionSources(File[] extensionSources) throws java.beans.PropertyVetoException {
		File[] oldExtensionSources = this.extensionSources;
		vetoableChangeSupport.fireVetoableChange(PROP_EXTENSIONSOURCES, oldExtensionSources, extensionSources);
		this.extensionSources = extensionSources;
		propertyChangeSupport.firePropertyChange(PROP_EXTENSIONSOURCES, oldExtensionSources, extensionSources);
	}

	/*
	 *
	 * Property: availableComponentsTreeModel
	 */
	private DefaultTreeModel availableComponentsTreeModel = new DefaultTreeModel(new DefaultMutableTreeNode());

	/**
	 * Get the value of availableComponentsTreeModel
	 *
	 * @return the value of availableComponentsTreeModel
	 */
	public DefaultTreeModel getAvailableComponentsTreeModel() {
		return availableComponentsTreeModel;
	}

	public final void buildExtensionTree() {
		DefaultMutableTreeNode root = (DefaultMutableTreeNode) this.availableComponentsTreeModel.getRoot();
		root.removeAllChildren();
		for (Extension extension : Component.Handler.getHandler().getModules()) {
			DefaultMutableTreeNode parent = root;
			String[] path = Arrays.copyOf(extension.getPath(), extension.getPath().length + 1);
			path[path.length - 1] = extension.toString();
			int i = 0;
			while (i < path.length) {
				DefaultMutableTreeNode currentNode = null;
				Enumeration children = parent.children();
				int index = 0;
				while (true) {
					DefaultMutableTreeNode currentChild;
					try {
						currentChild = (DefaultMutableTreeNode) children.nextElement();
					} catch (NoSuchElementException ex) {
						currentChild = null;
					}
					if (currentChild == null || currentChild.getUserObject().toString().compareTo(path[i]) >= 0) {
						if (currentChild != null && currentChild.getUserObject().toString().equals(path[i]) && i != path.length - 1)
							currentNode = currentChild;
						else {
							if (i == path.length - 1)
								currentNode = new DefaultMutableTreeNode(extension);
							else
								currentNode = new DefaultMutableTreeNode(path[i]);
							parent.insert(currentNode, index);
						}
						break;
					} else
						index++;
				}
				parent = currentNode;
				i++;
			}
		}

		this.availableComponentsTreeModel.reload();
	}
	/*
	 *
	 * Property: defaultComponentArea
	 */

	private int defaultComponentArea = 2500;

	/**
	 * Get the value of defaultComponentArea
	 *
	 * @return the value of defaultComponentArea
	 */
	public int getDefaultComponentArea() {
		return defaultComponentArea;
	}

	/**
	 * Set the value of defaultComponentArea
	 *
	 * @param defaultComponentArea new value of defaultComponentArea
	 */
	public void setDefaultComponentArea(int defaultComponentArea) {
		this.defaultComponentArea = defaultComponentArea;
	}
	/*
	 *
	 * Property: debugMode
	 */

	private boolean debugMode = false;

	/**
	 * Get the value of debugMode
	 *
	 * @return the value of debugMode
	 */
	public boolean isDebugMode() {
		return debugMode;
	}

	/**
	 * Set the value of debugMode
	 *
	 * @param debugMode new value of debugMode
	 */
	public void setDebugMode(boolean debugMode) {
		this.debugMode = debugMode;
	}
	/*
	 *
	 * Property: viewWindowSize
	 */

	private Dimension viewWindowSize = new Dimension(600, 400);

	/**
	 * Get the value of viewWindowSize
	 *
	 * @return the value of viewWindowSize
	 */
	public Dimension getViewWindowSize() {
		return viewWindowSize;
	}

	/**
	 * Set the value of viewWindowSize
	 *
	 * @param viewWindowSize new value of viewWindowSize
	 */
	public void setViewWindowSize(Dimension viewWindowSize) {
		this.viewWindowSize = viewWindowSize;
	}

	/*
	 *
	 * Property Change Support
	 */
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
	/*
	 *
	 * VetoableChange Support
	 */

	private VetoableChangeSupport vetoableChangeSupport = new VetoableChangeSupport(this);

	/**
	 * Add VetoableChangeListener.
	 *
	 * @param listener
	 */
	public void addVetoableChangeListener(VetoableChangeListener listener) {
		vetoableChangeSupport.addVetoableChangeListener(listener);
	}

	/**
	 * Remove VetoableChangeListener.
	 *
	 * @param listener
	 */
	public void removeVetoableChangeListener(VetoableChangeListener listener) {
		vetoableChangeSupport.removeVetoableChangeListener(listener);
	}

}
