
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
package gui.circuit;

import gui.util.Properties;
import gui.circuit.drawing.Diagram;
import gui.circuit.drawing.TransformableDrawable;
import gui.circuit.drawing.Drawable;
import gui.circuit.drawing.ComponentDiagram;
import gui.circuit.drawing.Pin;
import gui.circuit.management.CircuitManager;
import gui.circuit.management.Connector;
import core.exception.InstantiationException;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import core.build.Component;
import core.build.Flavor;
import static core.build.Component.Extension;
import core.signal.Signal;
import gui.dialogs.configuration.ConfigureDialog;
import gui.events.WrapperEvent;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 *
 * @author sylvester
 */
public final class ComponentWrapper implements TransformableDrawable, Comparable<ComponentWrapper> {

	private boolean editable;
	private Component component;
	private boolean componentIsDrawable = false;
	private final Extension extension;
	private CircuitManager manager;
	private gui.diagrams.Rectangle defaultDiagram = new gui.diagrams.Rectangle(1);
	private Point position = new Point();
	private Dimension dimension = new Dimension();
	private float rotation = 0;
	private ComponentWrapper nextComponent = null;
	private ComponentWrapper previousComponent = null;
	private double zIndex = 0;
	private Connector[] connectors = null;
	private Map<String, Object> parameters = null;
	// TODO: remove this after appropriate name finding
	private static int counter = 0;
	private static Map<Extension, Integer> nameCounters = new HashMap<Extension, Integer>();

	public ComponentWrapper(Extension extension) throws InstantiationException {
		this.extension = extension;
		if (!ComponentWrapper.nameCounters.containsKey(extension))
			ComponentWrapper.nameCounters.put(extension, 1);
		else
			ComponentWrapper.nameCounters.put(extension, ComponentWrapper.nameCounters.get(extension) + 1);
		this.component = extension.newInstance(null, extension.getName() + " (" + ComponentWrapper.nameCounters.get(extension) + ")");

		if (this.component == null)
			throw new InstantiationException(Component.class.getName(), new Throwable("Got NULL while trying to construct a new component using Extension.newInstance()."));

		this.componentIsDrawable = this.component instanceof Drawable;
		this.editable = true;
	}

	public ComponentWrapper(Component component, boolean editable) throws InstantiationException {
		this.extension = null;
		this.component = component;

		if (this.component == null)
			throw new InstantiationException(Component.class.getName(), new Throwable("Got NULL as component."));

		this.componentIsDrawable = this.component instanceof Drawable;
		this.editable = editable;

		Flavor flavor;
		if ((flavor = this.component.getFlavors().get(this.component.getFlavor())) != null) {
			List<Pin> pins = new LinkedList<Pin>();
			List<Connector> connectors = new LinkedList<Connector>();


			for (Entry<String, Object> parameter : this.component.getParameters().entrySet())
				if (parameter.getValue() instanceof Signal) {
					pins.add(new Pin(new Point(), new Point(), parameter.getKey()));
					connectors.add(new Connector(parameter.getKey(), Connector.Type.UNDEFINED, 1, ((Signal) parameter.getValue()).size()));
				} else if (parameter.getValue() instanceof Signal[]) {
					pins.add(new Pin(new Point(), new Point(), parameter.getKey()));
					Signal[] signals = (Signal[]) parameter.getValue();
					int bits[] = new int[signals.length];
					for (int i = 0; i < signals.length; i++)
						bits[i] = signals[i].size();
					connectors.add(new Connector(parameter.getKey(), Connector.Type.UNDEFINED, signals.length, bits));
				}
			this.defaultDiagram = new gui.diagrams.Rectangle(pins.toArray(new Pin[0]), 1);
			this.defaultDiagram.distributePins(gui.diagrams.Rectangle.ALL);
			this.connectors = connectors.toArray(new Connector[connectors.size()]);
			this.associatePinConnectorPairs();
		}
	}

	public void setManager(CircuitManager manager) {
		if (this.manager != null)
			throw new UnsupportedOperationException("In class " + this.getClass() + ": Cannot reassign manager. There is already a CircuitManager assigned.");
		this.manager = manager;
	}

	public void configure() {
		if (this.editable) {
			ConfigureDialog gui = new ConfigureDialog(Properties.getProfile().getMainWindow(), this);
			gui.setVisible(true);
		}
	}

	public void configureComponent() {
		if (!this.editable)
			return;
		Flavor flavor = this.component.getFlavors().get(this.component.getFlavor());
		for (Connector connector : this.connectors) {
			if (!connector.isCompletelyConnected())
				continue;
			core.build.checking.types.Type type;
			switch (connector.getType()) {
				case INPUT:
					type = flavor.getInputs().get(connector.getName());
					break;
				case OUTPUT:
					type = flavor.getOutputs().get(connector.getName());
					break;
				default:
					continue;
			}
			if (type instanceof core.build.checking.types.ArrayType) {
				Signal[] lineSignals = new Signal[connector.getLineCount()];
				for (int i = 0; i < lineSignals.length; i++) {
					Signal[] bitSignals = new Signal[connector.getBitWidth(i)];
					for (int j = 0; j < bitSignals.length; j++)
						bitSignals[j] = connector.getLinkage(i, j).getSignal();
					lineSignals[i] = new Signal(bitSignals);
				}
				this.component.set(connector.getName(), lineSignals);
			} else if (type instanceof core.build.checking.types.SimpleType) {
				Signal[] bitSignals = new Signal[connector.getBitWidth(0)];
				for (int i = 0; i < bitSignals.length; i++)
					bitSignals[i] = connector.getLinkage(0, i).getSignal();
				this.component.set(connector.getName(), new Signal(bitSignals));
			} else
				continue;
		}
		for (Entry<String, Object> parameter : this.parameters.entrySet())
			this.component.set(parameter.getKey(), parameter.getValue());
	}

	@Override
	public Diagram getDiagram() {
		if (this.componentIsDrawable)
			return ((Drawable) component).getDiagram();
		else
			return this.defaultDiagram;
	}

	public Component getComponent() {
		return this.component;
	}

	/*public String getComponentUniqueName() {
	return this.component.getShortName();
	}*/
	public Extension getExtension() {
		return extension;
	}

	public Connector getConnector(String name) {
		if (!this.isConfigured())
			return null;
		for (Connector connector : this.connectors)
			if (connector.getName().equals(name))
				return connector;
		return null;
	}

	public Connector[] getConnectors() {
		return connectors;
	}

	public void setConnectors(Connector[] connectors) {
		if (!this.editable)
			return;

		this.connectors = connectors;

		this.associatePinConnectorPairs();

		this.manager.fireEvent(new WrapperEvent(this.manager, this, WrapperEvent.TYPE_CONFIGURED));
	}

	public void setParameters(Map<String, Object> parameters) {
		if (!this.editable)
			return;

		this.parameters = parameters;

		this.manager.fireEvent(new WrapperEvent(this.manager, this, WrapperEvent.TYPE_CONFIGURED));
	}

	private void associatePinConnectorPairs() {
		ComponentDiagram diagram = null;
		if (this.getDiagram() instanceof ComponentDiagram)
			diagram = (ComponentDiagram) this.getDiagram();

		if (diagram != null)
			for (Connector connector : this.connectors) {
				Pin pin = null;
				connector.setWrapper(this);
				if ((pin = diagram.getPin(connector.getName())) != null) {
					connector.setPin(pin);
					pin.setConnector(connector);
				}
			}
	}

	public void setFalvor(String flavorname) {
		if (!this.editable)
			return;
		this.breakAllConnections();
		this.component.use(flavorname);

		Flavor flavor = this.component.getFlavors().get(flavorname);
		List<Pin> pins = new LinkedList<Pin>();

		for (String name : flavor.getInputs().keySet())
			pins.add(new Pin(new Point(), new Point(), name));
		for (String name : flavor.getOutputs().keySet())
			pins.add(new Pin(new Point(), new Point(), name));
		this.defaultDiagram.setPins(pins.toArray(new Pin[0]));
		this.defaultDiagram.distributePins(gui.diagrams.Rectangle.ALL);
		// Force diagram to position pins
		this.applyGeometry();
	}

	public void breakAllConnections() {
		if (this.editable && this.isConfigured())
			for (Connector connector : this.connectors)
				connector.breakAllConnections();
	}

	public boolean isBuilt() {
		return (this.isConfigured() && this.component.checkReady() && this.isFullyConnected());
	}

	public boolean isConfigured() {
		return this.connectors != null;
	}

	public boolean isFullyConnected() {
		if (!this.isConfigured())
			return false;
		for (Connector connector : this.connectors)
			if (!connector.isCompletelyConnected())
				return false;
		return true;
	}

	@Override
	public Point getPosition() {
		return new Point(this.position);
	}

	@Override
	public void setPosition(Point position) {
		if (!this.position.equals(position)) {
			this.initiateGeometryChange();
			this.position.setLocation(position);
			this.completeGeometryChange();
		}
	}

	@Override
	public Rectangle getBounds() {
		return new Rectangle(this.getDiagram().getTransformedBounds());
	}

	public void scale(double factor, boolean scaleAroundCenter) {
		if (factor != 1) {
			this.initiateGeometryChange();
			double newWidth = this.dimension.getWidth() * factor;
			double newHeight = this.dimension.getHeight() * factor;
			if (scaleAroundCenter)
				this.position.setLocation(this.position.getX() - (newWidth - this.dimension.getWidth()) / 2, this.position.getY() - (newHeight - this.dimension.getHeight()) / 2);
			this.dimension.setSize(newWidth, newHeight);
			this.completeGeometryChange();
		}
	}

	@Override
	public Dimension getDimension() {
		return new Dimension(this.dimension);
	}

	@Override
	public void setDimension(Dimension dimension) {
		if (!this.dimension.equals(dimension)) {
			this.initiateGeometryChange();
			this.dimension.setSize(dimension);
			this.completeGeometryChange();
		}
	}

	public void rotate(float degree) {
		this.setRotation(this.rotation + degree);
	}

	@Override
	public float getRotation() {
		return this.rotation;
	}

	@Override
	public void setRotation(float degrees) {
		if (degrees != this.rotation) {
			this.initiateGeometryChange();
			degrees = degrees % 360;
			this.rotation = degrees;
			this.completeGeometryChange();
		}
	}

	public void resetGeometry(boolean scaleAroundCenter, boolean resetPosition) {
		this.resetGeometry(Properties.getProfile().getDefaultComponentArea(), scaleAroundCenter, resetPosition);
	}

	public void resetGeometry(int areaToCover, boolean scaleAroundCenter, boolean resetPosition) {
		this.initiateGeometryChange();
		double widthToHeightRatio = this.getDiagram().getBounds().getWidth() / this.getDiagram().getBounds().getHeight();
		int width = (int) (Math.sqrt(areaToCover * widthToHeightRatio));
		if (scaleAroundCenter && !resetPosition)
			this.position.setLocation(this.position.getX() - (width - this.dimension.getWidth()) / 2, this.position.getY() - (width / widthToHeightRatio - this.dimension.getHeight()) / 2);
		this.dimension.setSize(new Dimension(width, (int) (width / widthToHeightRatio)));
		this.rotation = 0;
		if (resetPosition)
			this.position.setLocation(0, 0);
		this.completeGeometryChange();
	}

	private void initiateGeometryChange() {
		if (this.manager != null)
			this.manager.beforeLayoutChange(this);
	}

	private void completeGeometryChange() {
		this.applyGeometry();
		if (this.manager != null)
			this.manager.notifyLayoutChange(this);
		if (this.isConfigured())
			for (Connector connector : this.connectors)
				connector.recalculateAllPathes(java.lang.Math.random());
	}

	private void applyGeometry() {
		this.getDiagram().transformPath(AffineTransform.getScaleInstance(this.dimension.getWidth() / this.getDiagram().getBounds().getWidth(), this.dimension.getHeight() / this.getDiagram().getBounds().getHeight()), true);
		double rad = this.rotation * (Math.PI / 180);
		this.getDiagram().transformPath(AffineTransform.getRotateInstance(rad, this.getDiagram().getTransformedBounds().getWidth() / 2, this.getDiagram().getTransformedBounds().getHeight() / 2), false);
		this.getDiagram().transformPath(AffineTransform.getTranslateInstance(this.position.getX(), this.position.getY()), false);
	}

	public ComponentWrapper getNextComponent() {
		return nextComponent;
	}

	public boolean hasNextComponent() {
		return this.nextComponent != null;
	}

	public ComponentWrapper getPreviousComponent() {
		return previousComponent;
	}

	public boolean hasPreviousComponent() {
		return this.previousComponent != null;
	}

	private void insertInList(ComponentWrapper previousComponent, ComponentWrapper nextComponent) {
		this.previousComponent = previousComponent;
		if (this.hasPreviousComponent())
			this.previousComponent.nextComponent = this;
		this.nextComponent = nextComponent;
		if (this.hasNextComponent())
			this.nextComponent.previousComponent = this;
		if ((previousComponent != null) && (nextComponent != null))
			this.zIndex = (previousComponent.zIndex + nextComponent.zIndex) / 2.0;
		else if (previousComponent != null)
			this.zIndex = previousComponent.zIndex + 1;
		else if (nextComponent != null)
			this.zIndex = nextComponent.zIndex - 1;
		this.ensureCorrectZIndexes();
	}

	public void removeFromList() {
		if (this.hasPreviousComponent())
			this.previousComponent.nextComponent = this.nextComponent;
		if (this.hasNextComponent())
			this.nextComponent.previousComponent = this.previousComponent;
	}

	public void moveComponentForwards() {
		if (!this.hasPreviousComponent())
			return;
		if (this.previousComponent.hasPreviousComponent()) {
			ComponentWrapper newPrevious = this.previousComponent.previousComponent;
			ComponentWrapper newNext = this.previousComponent;
			this.removeFromList();
			this.insertInList(newPrevious, newNext);
		} else {
			ComponentWrapper newNext = this.previousComponent;
			this.setAsListStart(newNext);
		}
	}

	public void moveComponentBackwards() {
		if (!this.hasNextComponent())
			return;
		if (this.nextComponent.hasNextComponent()) {
			ComponentWrapper newPrevious = this.nextComponent;
			ComponentWrapper newNext = this.nextComponent.nextComponent;
			this.removeFromList();
			this.insertInList(newPrevious, newNext);
		} else {
			ComponentWrapper newPrevious = this.nextComponent;
			this.setAsListEnd(newPrevious);
		}
	}

	public ComponentWrapper setAsListStart(ComponentWrapper oldListStart) {
		if (this != oldListStart) {
			this.removeFromList();
			if (oldListStart != null)
				this.insertInList(null, oldListStart);
			this.zIndex = oldListStart.zIndex - 1;
			this.ensureCorrectZIndexes();
		}
		return this;
	}

	public ComponentWrapper setAsListEnd(ComponentWrapper oldListEnd) {
		if (this != oldListEnd) {
			this.removeFromList();
			this.insertInList(oldListEnd, null);
			if (oldListEnd != null)
				this.zIndex = oldListEnd.zIndex + 1;
			this.ensureCorrectZIndexes();
		}
		return this;
	}

	private void ensureCorrectZIndexes() {
		boolean correct = true;
		if (this.hasPreviousComponent())
			if (this.previousComponent.zIndex >= this.zIndex)
				correct = false;
		if (this.hasNextComponent())
			if (this.nextComponent.zIndex <= this.zIndex)
				correct = false;
		if (!correct) {
			this.zIndex = 0;
			ComponentWrapper currentComponent = this;
			while (currentComponent.hasPreviousComponent()) {
				currentComponent.previousComponent.zIndex = currentComponent.zIndex - 1;
				currentComponent = currentComponent.previousComponent;
			}
			currentComponent = this;
			while (currentComponent.hasNextComponent()) {
				currentComponent.nextComponent.zIndex = currentComponent.zIndex + 1;
				currentComponent = currentComponent.nextComponent;
			}
		}
	}

	@Override
	public int compareTo(ComponentWrapper o) {
		if (this.zIndex < o.zIndex)
			return -1;
		else if (this.zIndex > o.zIndex)
			return 1;
		else
			return 0;
	}

	@Override
	public String toString() {
		return this.component.getShortName();
	}
}
