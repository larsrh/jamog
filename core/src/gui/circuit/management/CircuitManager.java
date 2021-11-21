
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
package gui.circuit.management;

import gui.circuit.ComponentWrapper;
import gui.circuit.LinkageGroup;
import gui.circuit.drawing.RTreeNode;
import gui.circuit.drawing.TransformableDrawable;
import gui.circuit.drawing.SPHashMap;
import gui.events.CircuitModificationEvent;
import core.build.Component;
import core.build.ComponentCollection;
import core.exception.InstantiationException;
import static core.build.Component.Extension;
import core.signal.Signal;
import core.signal.SignalBit;
import gui.MachinePanel;
import gui.circuit.drawing.Diagram;
import gui.circuit.drawing.LinkageSegment;
import gui.events.LinkageEvent;
import gui.events.LinkageGroupEvent;
import gui.events.WrapperEvent;
import gui.events.WrapperGeometryEvent;
import gui.exception.IllegalLinkageException;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 *
 * @author sylvester
 */
public class CircuitManager {

	/**
	 * This is a container for an start and an end point of a list build of
	 * {@link ComponentWrapper}s. These are needed to managed such a list, but
	 * nevertheless most of the list handling is done be methods of
	 * {@code ComponentWrapper}.
	 *
	 * @see ComponentWrapper
	 */
	public static class ComponentList {

		public ComponentWrapper start = null;
		public ComponentWrapper end = null;
	}

	/**
	 * The types of layer movement (zIndex change) that can be passed to
	 * {@link CircuitManager#moveComponent(gui.ComponentWrapper, gui.CircuitPanel.ComponentLayerMovement)}.
	 */
	public static enum ComponentLayerMovement {

		START,
		FORWARDS,
		BACKWARDS,
		END
	}

	private static Map<ComponentCollection, CircuitManager> managers = new HashMap<ComponentCollection, CircuitManager>();

	private static class TemporaryEndpointDescription {

		private ComponentWrapper componentWrapper;
		private String connector;
		private int line;
		private int bit;

		public TemporaryEndpointDescription(ComponentWrapper componentWrapper, String connector, int line, int bit) {
			this.componentWrapper = componentWrapper;
			this.connector = connector;
			this.line = line;
			this.bit = bit;
		}

		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof TemporaryEndpointDescription))
				return false;
			TemporaryEndpointDescription endpointDescription = (TemporaryEndpointDescription) obj;
			return this.componentWrapper == endpointDescription.componentWrapper &&
					this.connector.equals(endpointDescription.connector) &&
					this.line == endpointDescription.line &&
					this.bit == endpointDescription.bit;
		}

		@Override
		public int hashCode() {
			return this.componentWrapper.hashCode() + connector.hashCode() + this.line + this.bit;
		}

		public int getBit() {
			return bit;
		}

		public ComponentWrapper getComponentWrapper() {
			return componentWrapper;
		}

		public String getConnector() {
			return connector;
		}

		public int getLine() {
			return line;
		}

	}

	public static CircuitManager getManager(LinkedList<ComponentCollection> parents, MachinePanel supervisor) {
		CircuitManager manager;
		ComponentCollection parent = parents.getLast();
		if ((manager = CircuitManager.managers.get(parent)) == null) {
			manager = new CircuitManager(parents, supervisor);
			CircuitManager.managers.put(parent, manager);
			int stepSize = 100;
			boolean increasedMaxStepsizeInLastRun = false;
			int maxStepsPerDirection = 0;
			int stepsPerDirection = -1;
			int direction = -1; //r,o,l,u
			Point position = new Point();
			Map<SignalBit, Set<TemporaryEndpointDescription>> signalBits = new HashMap<SignalBit, Set<TemporaryEndpointDescription>>();
			for (Component component : parent.getComponents().values()) {
				ComponentWrapper wrapper;
				try {
					wrapper = new ComponentWrapper(component, false);
				} catch (InstantiationException ex) {
					System.out.println("Could not construct componentWrapper.");
					ex.printStackTrace();
					System.out.println();
					continue;
				}

				// Add signals bits
				for (Entry<String, Object> signalEntry : component.getParameters().entrySet()) {
					Signal[] signals;
					if (signalEntry.getValue() instanceof Signal[])
						signals = (Signal[]) signalEntry.getValue();
					else if (signalEntry.getValue() instanceof Signal)
						signals = new Signal[]{(Signal) signalEntry.getValue()};
					else
						continue;
					for (int i = 0; i < signals.length; i++)
						for (int j = 0; j < signals[i].size(); j++) {
							if (!signalBits.containsKey(signals[i].getSignalBit(j)))
								signalBits.put(signals[i].getSignalBit(j), new HashSet<TemporaryEndpointDescription>());
							signalBits.get(signals[i].getSignalBit(j)).add(new TemporaryEndpointDescription(wrapper, signalEntry.getKey(), i, j));
						}
				}

				// Add to circuit manager
				manager.registerWrapper(wrapper);

				// Set default geometry
				wrapper.resetGeometry(true, false);
				// Set position
				switch (direction) {
					case 0:
						position.translate(stepSize, 0);
						break;
					case 1:
						position.translate(0, -stepSize);
						break;
					case 2:
						position.translate(-stepSize, 0);
						break;
					case 3:
						position.translate(0, stepSize);
						break;
				}
				stepsPerDirection++;
				if (stepsPerDirection == maxStepsPerDirection) {
					stepsPerDirection = 0;
					if (direction < 3)
						direction++;
					else
						direction = 0;
					if (!increasedMaxStepsizeInLastRun)
						maxStepsPerDirection++;
					increasedMaxStepsizeInLastRun = !increasedMaxStepsizeInLastRun;
				}
				wrapper.setPosition(position);
			}
			for (Set<TemporaryEndpointDescription> endpointDescriptions : signalBits.values()) {
				Set<Linkage.Endpoint> endpoints = new HashSet<Linkage.Endpoint>();
				for (TemporaryEndpointDescription endpointDescription : endpointDescriptions) {
					Connector connector = endpointDescription.getComponentWrapper().getConnector(endpointDescription.getConnector());
					endpoints.add(new Linkage.Endpoint(connector, endpointDescription.getLine(), endpointDescription.getBit()));
				}
				if (endpoints.size() == 1)
					endpoints.add(new Linkage.Endpoint(SpecialConnector.giveAutoConnector(SpecialConnector.Type.OUT), 0, 0));
				manager.addLinkage(new Linkage(manager, endpoints));
			}
		}
		return manager;
	}

	/**
	 * A {@link List} of {@link ComponentCollection}s which are the 'parent'
	 * chain of this CircuitManager. The last collection in the list is the
	 * direct parent used by this Manager, i.e. the collection where the
	 * components of this manager come from and where components might be added
	 * to or removed from.
	 */
	private LinkedList<ComponentCollection> parents;
	/**
	 * The {@link MachinePanel} which acts as supervisor for this
	 * {@code CircuitManager}, i.e. desides if wrappers are added, deleted, etc.
	 */
	private MachinePanel supervisor;
	/**
	 * The list of {@link core.build.Component}s really
	 * {@link ComponentWrapper}s this {@code WrapperManager} manages.
	 * @see #addComponent(gui.ComponentWrapper)
	 * @see #removeComponent(gui.ComponentWrapper)
	 * @see ComponentList
	 */
	private ComponentList componentList = new ComponentList();
	private Map<Set<Connector>, LinkageGroup> linkageGroupsMap = new HashMap<Set<Connector>, LinkageGroup>();
	private Collection<LinkageGroup> linkageGroupsCollection = linkageGroupsMap.values();
	/**
	 * A list of {@link LinkageSegment}s which will be drawn.
	 */
	private SPHashMap linkageSegments = new SPHashMap(50);
	/**
	 * The root of the RTree holding the components
	 */
	// TODO: Determine intelligent values for MIN and MAX children numbers for
	// this RTree.
	private RTreeNode<ComponentWrapper> componentsTree = new RTreeNode<ComponentWrapper>(3, 6);
	/**
	 * The current number of {@link core.build.Component}s really
	 * {@link ComponentWrapper}s this {@code WrapperManager} manages.
	 * @see #addComponent(gui.ComponentWrapper)
	 * @see #removeComponent(gui.ComponentWrapper)
	 * @see #componentList
	 */
	private int componentCount = 0;
	/**
	 * Holds components the {@code WrapperManager} received an
	 * {@link TransformableDrawable.ChangeListener#beforeLayoutChange(gui.TransformableDrawable)}
	 * message from. This is neccessary, because after the layout changed the
	 * RTree must be adjusted but will not be able to get the components any
	 * more (because it has to be adjusted).
	 *
	 * @see TransformableDrawable.ChangeListener
	 */
	private Set<ComponentWrapper> componentsAboutToReposition = new HashSet<ComponentWrapper>();
	private Set<CircuitModificationEvent.Listener> modificationListener = new HashSet<CircuitModificationEvent.Listener>();

	private CircuitManager(LinkedList<ComponentCollection> parents, MachinePanel supervisor) {
		this.parents = parents;
		this.supervisor = supervisor;
	}

	/**
	 * Addes the given wrapper at the end of the {@link #componentList} and to
	 * the internal R-Tree, registers itself as listener on the wrapper to be
	 * informed about changes that might require ajustion of the tree and/or
	 * repainting of the affected CircuitPanels.
	 *
	 * @param wrapper The component to be added.
	 *
	 * @see ComponentWrapper#addListener(gui.support.CircuitManager)
	 */
	public final boolean addComponent(Extension extension, Point position) {
		ComponentWrapper wrapper;
		try {
			wrapper = new ComponentWrapper(extension);
		} catch (InstantiationException ex) {
			System.out.println("Could not construct componentWrapper.");
			ex.printStackTrace();
			System.out.println();
			return false;
		}

		// Set default geometry
		wrapper.resetGeometry(true, false);
		// Set position
		position.setLocation(
				position.getX() - wrapper.getDimension().getWidth() / 2,
				position.getY() - wrapper.getDimension().getHeight() / 2);
		wrapper.setPosition(position);

		this.registerWrapper(wrapper);

		return true;
	}

	private void registerWrapper(ComponentWrapper wrapper) {
		wrapper.setManager(this);
		wrapper.setAsListEnd(this.componentList.end);
		if (this.componentList.start == null)
			this.componentList.start = wrapper;
		this.componentList.end = wrapper;
		this.componentCount++;
		this.componentsTree.addElement(wrapper);
		this.fireEvent(new WrapperEvent(this, wrapper, WrapperEvent.TYPE_ADDITON));
	}

	/**
	 * Removes the given wrapper from the {@link #componentList} and the
	 * internal R-Tree, unregisteres itself as listener of the wrapper.
	 *
	 * @param wrapper The component to be removed.
	 * @return
	 */
	public final boolean removeComponent(ComponentWrapper wrapper) {
		wrapper.breakAllConnections();
		this.componentsTree.removeElement(wrapper);
		if (wrapper == this.componentList.start)
			this.componentList.start = wrapper.getNextComponent();
		if (wrapper == this.componentList.end)
			this.componentList.end = wrapper.getPreviousComponent();
		wrapper.removeFromList();
		this.componentCount--;
		this.fireEvent(new WrapperEvent(this, wrapper, WrapperEvent.TYPE_REMOVAL));
		return true;
	}

	public final boolean addLinkage(Connector connector, int line, int bit) throws IllegalLinkageException {
		if (connector.isConnected(line, bit))
			throw new IllegalLinkageException("Cannot construct single ended linkage with an endpoint that is already connected. Linkage wouldn't be single ended.");

		this.addLinkage(new Linkage(this, Collections.singleton(new Linkage.Endpoint(connector, line, bit))));
		return true;
	}

	public final boolean addLinkage(Connector connector1, Connector connector2, int line1, int line2, int bit1, int bit2) {
		List<Linkage> unusedLinkages = new LinkedList<Linkage>();
		Set<Linkage.Endpoint> endpoints = new HashSet<Linkage.Endpoint>();
		Linkage currentPath;
		if ((currentPath = connector1.getLinkage(line1, bit1)) != null) {
			unusedLinkages.add(currentPath);
			endpoints.addAll(currentPath.getEndpoints());
		}
		if ((currentPath = connector2.getLinkage(line2, bit2)) != null) {
			unusedLinkages.add(currentPath);
			endpoints.addAll(currentPath.getEndpoints());
		}
		endpoints.add(new Linkage.Endpoint(connector1, line1, bit1));
		endpoints.add(new Linkage.Endpoint(connector2, line2, bit2));

		for (Linkage linkage : unusedLinkages)
			this.removeLinkage(linkage);

		this.addLinkage(new Linkage(this, endpoints));
		return true;
	}

	private final void addLinkage(Linkage linkage) {
		this.fireEvent(new LinkageEvent(this, linkage, LinkageEvent.TYPE_ADDITON));

		LinkageGroup group;
		Set<Connector> connectors = linkage.getConnectors();
		if ((group = this.linkageGroupsMap.get(connectors)) == null) {
			group = new LinkageGroup(this, linkage);
			this.linkageGroupsMap.put(connectors, group);
			this.fireEvent(new LinkageGroupEvent(this, group, LinkageGroupEvent.TYPE_ADDITON));
		} else
			group.addLinkage(linkage);
	}

	public final boolean removeLinkage(Linkage linkage) {
		if (this.linkageGroupsMap.containsKey(linkage.getConnectors())) {
			this.fireEvent(new LinkageEvent(this, linkage, LinkageEvent.TYPE_REMOVAL));

			linkage.destruct();
			Set<Connector> connectors = linkage.getConnectors();
			LinkageGroup group = this.linkageGroupsMap.get(connectors);
			if (group != null)
				group.removeLinkage(linkage);
			if (group.isEmpty()) {
				group.calculateSegments();
				this.linkageGroupsCollection.remove(group);
				this.fireEvent(new LinkageGroupEvent(this, group, LinkageGroupEvent.TYPE_REMOVAL));
			}
			return true;
		} else
			return false;
	}

	void unlinkFromLinkage(Linkage linkage, Set<Linkage.Endpoint> endpointsToRemove) {
		if (linkage.getEndpoints().containsAll(endpointsToRemove)) {
			Set<Linkage.Endpoint> endpoints = new HashSet<Linkage.Endpoint>(linkage.getEndpoints());
			this.removeLinkage(linkage);
			if (endpoints.size() - endpointsToRemove.size() > 1) {
				endpoints.removeAll(endpointsToRemove);
				this.addLinkage(new Linkage(this, endpoints));
			}
		}
	}

	void recalculateLinkage(Linkage linkage, double eventId) {
		LinkageGroup group = this.linkageGroupsMap.get(linkage.getConnectors());
		if (group != null)
			group.calculateSegments(eventId);
	}

	/**
	 * Gives the given component another zIndex and position in the
	 * {@link #componentList}. This method internally uses methods of
	 * {@link ComponentWrapper} which do most of the list management.
	 *
	 * @param component The component to be moved
	 * @param type The type of movement.
	 */
	public final void moveComponent(ComponentWrapper component, ComponentLayerMovement type) {
		if (this.componentList.start == this.componentList.end)
			return;

		switch (type) {
			case START:
				if (this.componentList.end == component)
					this.componentList.end = component.getPreviousComponent();
				this.componentList.start = component.setAsListStart(this.componentList.start);
				break;
			case FORWARDS:
				if (component.getPreviousComponent() == this.componentList.start)
					this.componentList.start = component;
				if (component == this.componentList.end)
					this.componentList.end = component.getPreviousComponent();
				component.moveComponentForwards();
				break;
			case BACKWARDS:
				if (component == this.componentList.start)
					this.componentList.start = component.getNextComponent();
				if (component.getNextComponent() == this.componentList.end)
					this.componentList.end = component;
				component.moveComponentBackwards();
				break;
			case END:
				if (this.componentList.start == component)
					this.componentList.start = component.getNextComponent();
				this.componentList.end = component.setAsListEnd(this.componentList.end);
				break;
		}
		this.fireEvent(new WrapperEvent(this, component, WrapperEvent.TYPE_MOVEMENT));
	}

	// TODO: Find a way to expose the component list as 'unmodifiable'.
	public ComponentList getComponentList() {
		return this.componentList;
	}

	/**
	 * Returns the current componentCount
	 *
	 * @see #componentCount
	 *
	 * @return
	 */
	public int getComponentCount() {
		return componentCount;
	}

	public int getLinkageSegmentCount() {
		return this.linkageSegments.getSegmentCount();
	}

	public LinkageSegment[] getSignalSegments(Rectangle rect) {
		return this.linkageSegments.get(rect);
	}

	public LinkageSegment[] getLinkageSegments(Point2D point, int uncertanity) {
		return this.linkageSegments.get(point, uncertanity);
	}

	/**
	 * @deprecated Exists only for debugging reasons.
	 * @return
	 */
	public RTreeNode<ComponentWrapper> getRTree() {
		return this.componentsTree;
	}

	/**
	 * Returns a Rectangle that encloses all {@link ComponentWrapper}s in this
	 * circuit (to be correct: it really encloses their {@link Diagram}s).
	 *
	 * @return
	 */
	public final Rectangle getBoundingRect() {
		return new Rectangle(this.componentsTree.getBounds().x, this.componentsTree.getBounds().y, this.componentsTree.getBounds().width, this.componentsTree.getBounds().height);
	}

	public ComponentCollection getParent() {
		return this.parents.getLast();
	}

	public List<ComponentCollection> getParents() {
		return Collections.unmodifiableList(this.parents);
	}

	/**
	 * Returns all wrappers that lie under the given position. The internal
	 * R-Tree is used to do this.
	 *
	 * @param position The position at which to search.
	 * @return The matching components.
	 */
	public final List<ComponentWrapper> findElements(Point position) {
		return this.componentsTree.findElements(position);
	}

	/**
	 * Returns all components that lie completely within the given frame or just
	 * intersect with it. The internal R-Tree is used to do this.
	 *
	 * @param frame The frame, which is to search.
	 * @param mustCompletelyContain If this is set to {@code true} the component
	 * must lie completly within the frame, otherwise it must only intersect it.
	 * @return The matching components.
	 */
	public final List<ComponentWrapper> findElements(Rectangle frame, boolean mustCompletelyContain) {
		return this.componentsTree.findElements(frame, mustCompletelyContain);
	}

	/**
	 * This is called, if there was the request to add an unknown component at
	 * the given position. This method simply informs the {@link #supervisor} of
	 * this {@code CircuitManager}.
	 *
	 * @param position
	 * @return Whether or not the addition was successfull.
	 */
	public final boolean componentAdditionAttempt(Point position) {
		if (this.supervisor != null)
			return this.supervisor.notifyComponentAdditionAttempt(position);
		else
			return false;
	}

	/**
	 * This is called, if there was the request to add a given component at the
	 * given position. This method simply informs the {@link #supervisor} of
	 * this {@code CircuitManager}.
	 *
	 * @param extension
	 * @param wrapper
	 * @param position
	 * @return Whether or not the addition was successfull.
	 */
	public final boolean componentAdditionAttempt(Extension extension, Point position) {
		if (this.supervisor != null)
			return this.supervisor.notifyComponentAdditionAttempt(extension, position);
		else
			return false;
	}

	/**
	 * This is called, if there was the request to remove given component. This
	 * method simply informs the {@link #supervisor} of this
	 * {@code CircuitManager}.
	 *
	 * @param wrapper
	 * @return Whether or not the removal was successfull.
	 */
	public final boolean componentRemovalAttempt(ComponentWrapper wrapper) {
		if (this.supervisor != null)
			return this.supervisor.notifyComponentRemovalAttempt(wrapper);
		else
			return false;
	}

	public final boolean signalConnectionAttempt(Connector connector1, Connector connector2, int line1, int line2, int bit1, int bit2) {
		if (this.supervisor != null)
			return this.supervisor.notifySignalConnectionAttempt(this, connector1, connector2, line1, line2, bit1, bit2);
		else
			return false;
	}

	public final boolean hasSupervisor() {
		return this.supervisor != null;
	}

	public void beforeLayoutChange(TransformableDrawable drawable) {
		Iterator<ComponentWrapper> iterator = this.componentsTree.findElements(drawable.getBounds(), true).iterator();
		while (iterator.hasNext()) {
			ComponentWrapper candidate = iterator.next();
			if (candidate == drawable) {
				this.componentsAboutToReposition.add(candidate);
				this.componentsTree.removeElement(candidate);
				break;
			}
		}
	}

	public void notifyLayoutChange(TransformableDrawable drawable) {
		boolean found = false;
		ComponentWrapper candidate = null;
		Iterator<ComponentWrapper> iterator = this.componentsAboutToReposition.iterator();
		while (iterator.hasNext()) {
			candidate = iterator.next();
			if (candidate == drawable) {
				this.componentsAboutToReposition.remove(candidate);
				this.componentsTree.addElement(candidate);
				found = true;
				break;
			}
		}
		if (!found)
			throw new RTreeNode.RTreeException("Element changed layout without notifying doing so before. Cannot locate Element; RTree is broken.");
		else
			this.fireEvent(new WrapperGeometryEvent(this, candidate, null));
	}

	public void recalculatedLinkageGroup(LinkageGroup group, LinkageSegment[] oldSegments) {
		for (LinkageSegment segment : oldSegments)
			this.linkageSegments.remove(segment);
		for (LinkageSegment segment : group.getSegments())
			this.linkageSegments.put(segment);
	}

	public void fireEvent(CircuitModificationEvent evt) {
		if (evt.getCircuitManager() == this)
			for (CircuitModificationEvent.Listener listener : this.modificationListener)
				listener.circuitChanged(evt);
	}

	public void addModificationListener(CircuitModificationEvent.Listener listener) {
		this.modificationListener.add(listener);
	}

	public void removeModificationListener(CircuitModificationEvent.Listener listener) {
		this.modificationListener.remove(listener);
	}

	public void cleanLinkageGroups() {
		for (LinkageGroup group : this.linkageGroupsCollection)
			group.clean();
	}

}
