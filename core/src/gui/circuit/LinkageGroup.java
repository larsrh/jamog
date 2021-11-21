/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gui.circuit;

import core.exception.SerializingException;
import core.misc.serial.SerializingStream;
import core.monitor.SignalListener;
import core.signal.Bit;
import core.signal.Signal;
import core.signal.SignalBit;
import gui.circuit.drawing.Pin;
import gui.circuit.management.CircuitManager;
import gui.circuit.management.SpecialConnector;
import gui.circuit.drawing.LinkageSegment;
import gui.circuit.management.Connector;
import gui.circuit.management.Linkage;
import java.awt.Point;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 *
 * @author sylvester
 */
public class LinkageGroup implements SignalListener {

	private CircuitManager manager;
	private Set<Connector> connectors;
	private List<Linkage> linkages = new LinkedList<Linkage>();
	private Set<LinkageSegment> linkageSegments = new HashSet<LinkageSegment>();
	private double lastEventId = -1;
	private boolean dirty = false;

	public LinkageGroup(CircuitManager manager, Linkage linkage) {
		this.manager = manager;
		this.linkages.add(linkage);
		this.connectors = linkage.getConnectors();
		linkage.getSignal().addSignalListener(this);
		this.calculateSegments();
	}

	public void addLinkage(Linkage linkage) {
		this.linkages.add(linkage);
		this.connectors.addAll(linkage.getConnectors());
		linkage.getSignal().addSignalListener(this);
	}

	public void removeLinkage(Linkage linkage) {
		if (this.linkages.remove(linkage)) {
			linkage.getSignal().removeSignalListener(this);
			if (this.linkages.isEmpty())
				this.connectors.clear();
		}
	}

	public void calculateSegments() {
		LinkageSegment[] oldSegments = this.linkageSegments.toArray(new LinkageSegment[this.linkageSegments.size()]);
		SpecialConnector specialConnector = null;
		this.linkageSegments.clear();
		List<SpecialConnector> specialConnectors = SpecialConnector.getConnectors();
		for (Connector connector : this.connectors)
			try {
				specialConnector = (SpecialConnector) connector;
				break;
			} catch (ClassCastException exc) {
				continue;
			}
		if (this.connectors.size() == 1 || specialConnector != null)
			for (Connector connector : this.connectors) {
				if (connector == specialConnector)
					continue;
				// TODO: Find a better (i.e. more efficient and more precise) way to
				// determine the target point at (x,y).
				Point p1 = connector.getPin().getRealPoint();
				Point p2 = connector.getPin().getRealPinDirectionPoint();
				int x = p2.x;
				int y = p2.y;
				int xDistanceStep = p2.x - p1.x;
				int yDistanceStep = p2.y - p1.y;
				double distanceStep = java.lang.Math.sqrt(xDistanceStep * xDistanceStep + yDistanceStep * yDistanceStep);
				double distance = 0;
				while (distance < 50) {
					x += xDistanceStep;
					y += yDistanceStep;
					distance += distanceStep;
				}
				LinkageSegment.Ending ending = LinkageSegment.Ending.ATTACHED;
				if (specialConnector != null)
					switch (specialConnector.getSpecialType()) {
						case CLOCK:
							ending = LinkageSegment.Ending.CLOCK;
							break;
						case OUT:
							ending = LinkageSegment.Ending.OUT;
							break;
					}
				this.linkageSegments.add(new LinkageSegment(this, p1, new Point(x, y), LinkageSegment.Ending.NONE, ending));
			}
		else {
			int minX = Integer.MAX_VALUE;
			int maxX = Integer.MIN_VALUE;
			int minY = Integer.MAX_VALUE;
			int maxY = Integer.MIN_VALUE;
			for (Connector connector : this.connectors) {
				if (!connector.hasPin())
					break;
				Point point = connector.getPin().getRealPinDirectionPoint();
				if (point.x < minX)
					minX = point.x;
				if (point.y < minY)
					minY = point.y;
				if (point.x > maxX)
					maxX = point.x;
				if (point.y > maxY)
					maxY = point.y;
			}
			Point center = new Point(minX + (maxX - minX) / 2, minY + (maxY - minY) / 2);
			boolean first = true;
			for (Connector connector : this.connectors) {
				if (!connector.hasPin())
					break;
				Pin pin = connector.getPin();
				this.linkageSegments.add(new LinkageSegment(this, pin.getRealPoint(), pin.getRealPinDirectionPoint()));
				if (first && this.connectors.size() > 2)
					this.linkageSegments.add(new LinkageSegment(this,
							pin.getRealPinDirectionPoint(),
							center,
							LinkageSegment.Ending.NONE,
							LinkageSegment.Ending.CONNECTION));
				else
					this.linkageSegments.add(new LinkageSegment(this,
							pin.getRealPinDirectionPoint(),
							center));
				first = false;
			}
		}
		this.manager.recalculatedLinkageGroup(this, oldSegments);
	}

	public void calculateSegments(double eventId) {
		if (this.lastEventId != eventId) {
			this.lastEventId = eventId;
			this.calculateSegments();
		}
	}

	public Set<LinkageSegment> getSegments() {
		return Collections.unmodifiableSet(this.linkageSegments);
	}

	CircuitManager getManager() {
		return manager;
	}

	public boolean isEmpty() {
		return this.linkages.isEmpty();
	}

	public List<Linkage> getLinkages() {
		return Collections.unmodifiableList(this.linkages);
	}

	public int size() {
		return linkages.size();
	}

	public String getSignalValues() {
		String values = "";
		for (Linkage linkage : this.linkages)
			values += linkage.getSignal().toString();
		return values;
	}

	public final boolean isDirty() {
		return this.dirty;
	}

	public final void clean() {
		this.dirty = false;
	}

	@Override
	public void signalChanged(Signal changed_signal, SignalBit changed_bit, Bit old_value, Bit new_value) {
		this.dirty = true;
	}

	@Override
	public void serialize(SerializingStream out) throws IOException, SerializingException {
		// TODO: Fix when there is a serialisation for the GUI
	}
}
