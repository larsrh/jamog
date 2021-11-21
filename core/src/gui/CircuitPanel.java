
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
import gui.dialogs.PinSelection;
import gui.dialogs.management.ManageSignalDialog;
import gui.dialogs.management.ManageDialog;
import gui.circuit.ComponentWrapper;
import gui.circuit.LinkageGroup;
import gui.circuit.drawing.Pin;
import gui.circuit.drawing.ComponentGhost;
import gui.circuit.management.SpecialConnector;
import gui.circuit.management.CircuitManager;
import gui.circuit.drawing.LinkageSegment;
import gui.circuit.management.Connector;
import gui.util.Utilities;
import gui.events.CircuitModificationEvent;
import core.build.ComponentCollection;
import static core.build.Component.Extension;
import core.misc.setable.Setable;
import core.signal.Signal;
import gui.diagrams.Eldritch;
import gui.circuit.drawing.ComponentDiagram;
import gui.circuit.drawing.Diagram;
import gui.circuit.drawing.RTreeNode;
import gui.circuit.drawing.TransformableDrawable;
import gui.circuit.management.Linkage;
import gui.events.WrapperEvent;
import gui.events.WrapperGeometryEvent;
import gui.support.ExtensionTransferable;
import gui.util.GuiColor;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Paint;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Path2D;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import javax.swing.*;

/**
 * A extension of a {@link JPanel} being able to manage a list of
 * {@link sim.Component}s and {@link Signal}s (not implemented yet). The
 * components are managed using {@code ComponentWrapper}, which can
 * build a list of themselves, without using an external {@link List}
 * implementation (see {@link ComponentWrapper} for details).
 * <br/><br/>
 * <em>Note, that for convenience in the JavaDoc of this class (and all
 * inner classes) there will be talked about {@code Component}s instead
 * of {@code ComponentWrapper}s which are meant most of the time. If uncertain
 * which one is meant method parameters can tell.</em>
 * <br/><br/>
 * The components and signals are drawn in colors which can be
 * managed using various getters and setters. The {@code CircuitPanel} know two
 * different drawing modes, managable using {@link #getSizeBehaviour()} and
 * {@link #setSizeBehaviour(gui.CircuitPanel.SizeBehaviour)}:
 * <ul>
 *	<li>
 *		{@link SizeBehaviour#KEEP_SIZE}
 *		<p>
 *			This mode, draws all elements in their original size, possibly the
 *			elements are not visible, because they are outside of the panel.
 *			(Use {@link CircuitPanel#setScrollOffset(java.awt.Point)} to move
 *			around the visible area.)
 *		</p>
 *	</li>
 *	<li>
 *		{@link SizeBehaviour#SHRINK_DRAWING}
 *		<p>
 *			This mode, draws all elements that are associated with the panel
 *			resizing all of them if needed to fit the panel.
 *		</p>
 *	</li>
 * </ul>
 * <br/><br/>
 * Additionally this class manages a {@link List} of {@link ComponentGhost}s
 * that should be draw, too. These ghosts are stored in {@link #ghosts} which
 * has neither getter not setter. Only classes extending {@code CircuitPanel}
 * can change this variable. Ghosts should be used if pending operations should
 * be visualized if directly working on the components whould be inefficient.
 * Normally the changes can easily be transferred from the ghost to the
 * component itself after the operation finished.
 *
 * @see sim.Component
 * @see ComponentWrapper
 * @see ComponentGhost
 *
 * @author sylvester
 */
public class CircuitPanel extends JPanel implements CircuitModificationEvent.Listener {

	private static final long serialVersionUID = -6709684167560272790L;

	/**
	 * Objects implementing this interface can (un-)register on an instance of
	 * {@code CircuitPanel} using
	 * {@link CircuitPanel#addListener(gui.CircuitPanel.ChangeListener)} and
	 * {@link CircuitPanel#removeListener(gui.CircuitPanel.ChangeListener)}.
	 * If an object is registered as a listner it will get notified about
	 * additions and removals of {@code Component}s as well as general layout
	 * changes like changed positions, rotations, etc.
	 *
	 * @see ClonedCircuitPanel
	 */
	public static interface ChangeListener {

		/**
		 * This method of a registered listener will be fired if a
		 * component is added to the {@code CircuitPanel} which is listened to.
		 *
		 * @param wrapper The component that has been added.
		 */
		public void notifyComponentAddition(ComponentWrapper wrapper);

		/**
		 * This method of a registered listener will be fired if a
		 * component is removed from the {@code CircuitPanel} which is listened
		 * to.
		 *
		 * @param wrapper The component which has be removed.
		 */
		public void notifyComponentRemoval(ComponentWrapper wrapper);

		/**
		 * This method of registered listener will be fired if the layout
		 * changed in general. One, many, or all components of the
		 * {@code CircuitPanel} which is listened to may affected, typical a
		 * resizement, rotation, zIndex change, etc. occured.
		 */
		public void notifyLayoutChange();
	}

	/**
	 * {@code PriorizedInputAdapter}s can be used to react on
	 * {@link MouseEvent}s occuring on a {@code CircuitPanel} (or child
	 * classes). They have the advantage, that they have a priority number which
	 * fixes the order in which they are called, and that a adapter method can
	 * prevent the (later) call of lower priorized adapters by returing
	 * {@code TRUE}.
	 *
	 * @see #addInputAdapter(gui.CircuitPanel.PriorizedInputAdapter)
	 * @see #removeInputAdapter(gui.CircuitPanel.PriorizedInputAdapter)
	 */
	public static abstract class PriorizedInputAdapter implements Comparable<PriorizedInputAdapter> {

		private int priority;

		public PriorizedInputAdapter(int priority) {
			this.priority = priority;
		}

		public int getPriority() {
			return this.priority;
		}

		@Override
		public int compareTo(PriorizedInputAdapter adapter) {
			return (this.priority < adapter.priority ? -1 : (this.priority == adapter.priority ? 0 : 1));
		}

		public boolean mouseClicked(MouseEvent e) {
			return false;
		}

		public boolean mouseDragged(MouseEvent e) {
			return false;
		}

		public boolean mouseEntered(MouseEvent e) {
			return false;
		}

		public boolean mouseExited(MouseEvent e) {
			return false;
		}

		public boolean mouseMoved(MouseEvent e) {
			return false;
		}

		public boolean mousePressed(MouseEvent e) {
			return false;
		}

		public boolean mouseReleased(MouseEvent e) {
			return false;
		}

		public boolean mouseWheelMoved(MouseWheelEvent e) {
			return false;
		}

		public boolean keyTyped(KeyEvent e) {
			return false;
		}

		public boolean keyPressed(KeyEvent e) {
			return false;
		}

		public boolean keyReleased(KeyEvent e) {
			return false;
		}
	}

	private static abstract class SignalMouseAdapter extends PriorizedInputAdapter implements PinSelection.Listener {

		public SignalMouseAdapter(int priority) {
			super(priority);
		}
	}

	private static class Tooltip {

		private Point point;
		private String text;

		public Tooltip(Point point, String text) {
			this.point = point;
			this.text = text;
		}

		public Point getPoint() {
			return point;
		}

		public String getText() {
			return text;
		}

		public int getWidth(Graphics2D graphics) {
			return graphics.getFontMetrics().stringWidth(this.text) + 4;
		}

		public int getHeight(Graphics2D graphics) {
			int lineCount = 1;
			while (this.text.indexOf("\n") != -1)
				lineCount++;
			return graphics.getFontMetrics().getHeight() * lineCount + 4;
		}

		public Dimension getDimension(Graphics2D graphics) {
			return new Dimension(this.getWidth(graphics), this.getHeight(graphics));
		}

		public void drawTip(Graphics2D graphics, Point point) {
			Rectangle tipRect = new Rectangle(point, this.getDimension(graphics));
			Stroke oldStroke = graphics.getStroke();
			Paint oldPaint = graphics.getPaint();
			graphics.setStroke(Properties.getProfile().getDefaultStroke().changeWidth(4));
			graphics.setPaint(Properties.getProfile().getBgColor().setAlpha(100));
			graphics.drawLine(this.point.x, this.point.y, tipRect.x + tipRect.width, tipRect.y + tipRect.height / 2);
			graphics.setStroke(Properties.getProfile().getDefaultStroke());
			graphics.setPaint(Properties.getProfile().getTooltipFGColor());
			graphics.drawLine(this.point.x, this.point.y, tipRect.x + tipRect.width, tipRect.y + tipRect.height / 2);
			graphics.fillOval(this.point.x - 1, this.point.y - 1, 3, 3);
			graphics.setPaint(Properties.getProfile().getTooltipBGColor());
			graphics.fill(tipRect);
			graphics.setPaint(Properties.getProfile().getTooltipFGColor());
			graphics.draw(tipRect);
			graphics.setPaint(Properties.getProfile().getTextColor());
			graphics.drawString(this.text, tipRect.x + 2, tipRect.y + 2 + graphics.getFontMetrics().getAscent());
			graphics.setStroke(oldStroke);
			graphics.setPaint(oldPaint);
		}
	}

	private static enum DropOverlay {

		ATTACH(Utilities.getImage("attach.png", true)),
		CLOCK(Utilities.getImage("clock.png", true)),
		OUT(Utilities.getImage("brick_go.png", true));
		private Connector connector = null;
		// TODO: Replace "20" by a property
		private Rectangle rectangle = new Rectangle(new Dimension(20, 20));
		private Image image;

		DropOverlay(Image image) {
			this.image = image;
		}

		Rectangle getRectangle() {
			return new Rectangle(this.rectangle);
		}

		void setLocation(Point point) {
			this.rectangle.setLocation(point);
		}

		Image getImage() {
			return image;
		}

		Connector getConnector() {
			switch (this) {
				case CLOCK:
					if (this.connector == null)
						this.connector = SpecialConnector.getConnectors(SpecialConnector.Type.CLOCK).get(0);
					return this.connector;
				case OUT:
					return SpecialConnector.giveAutoConnector(SpecialConnector.Type.OUT);
				case ATTACH:
				default:
					return null;
			}
		}

		void setConnector(Connector connector) {
			this.connector = connector;
		}
	}
	/** @see #OVERLAY_ALL */
	public static final int OVERLAY_BROKEN = 1;
	/** @see #OVERLAY_ALL */
	public static final int OVERLAY_INCOMPLETE = 2;
	/** @see #OVERLAY_ALL */
	public static final int OVERLAY_NOFLAVOR = 4;
	/**
	 * These overlays are constants passed to
	 * {@link #paintDiagramComponent(java.awt.Graphics2D, gui.TransformableDrawable, gui.GuiColor, gui.GuiColor, int)}
	 * to indicate which icons should be drawn around the component.
	 */
	public static final int OVERLAY_ALL = 7;
	/**
	 * Holds an image for a special cursor that might be resued over
	 * time in a {@code CircuitPanel}.
	 */
	private static Image addCursor = Toolkit.getDefaultToolkit().getImage(CircuitPanel.class.getResource("/gui/icons/addCursor.png"));
	/**
	 * @see #addCursor
	 */
	private static Image removeCursor = Toolkit.getDefaultToolkit().getImage(CircuitPanel.class.getResource("/gui/icons/removeCursor.png"));
	/**
	 * The {@link WrapperManager} that manages the {@link ComponentWrapper}s
	 * that are displayed by this {@code CircuitPanel}. The Panel has access to
	 * the wrappers it displays only via this wrapper.
	 */
	private CircuitManager circuitManager;
	/**
	 * If set to {@code true} this panel shows all components by zooming out if
	 * neccessary and allows moving of the viewport ({@link #getVisibleRect()}).
	 * All key and mouse events are block as long as the component is in
	 * mapView.
	 */
	private boolean mapView = false;
	/**
	 * The scrollOffset of this {@code CircuitPanel}.
	 * @see #getScrollOffset()
	 * @see #setScrollOffset(java.awt.Point)
	 */
	private Point scrollOffset = new Point(0, 0);
	/**
	 * The {@link List} of {@link ComponentGhost}s that should be drawn by this
	 * {@code CircuitPanel}.
	 */
	private List<ComponentGhost> ghosts = new LinkedList<ComponentGhost>();
	/**
	 * A {@code Set} of components that are selected at the moment. (Mostly used
	 * for drawing.)
	 */
	private Set<ComponentWrapper> selectedWrappers = new HashSet<ComponentWrapper>();
	/**
	 * All {@link Tooltip} that should currently be drawn.
	 */
	private List<Tooltip> tooltips = new LinkedList<Tooltip>();
	/**
	 * The {@link List} of {@link PriorizedInputAdapter}s that are registered on
	 * this {@code CircuitPanel} (by itself or by child classes}.
	 *
	 * @see PriorizedInputAdapter
	 * @see #addInputAdapter(gui.CircuitPanel.PriorizedInputAdapter)
	 * @see #removeInputAdapter(gui.CircuitPanel.PriorizedInputAdapter)
	 */
	private List<PriorizedInputAdapter> inputAdapters = new ArrayList<PriorizedInputAdapter>();
	/**
	 * The single {@link MouseAdapter} that is listening on this Circuit panel
	 * and launching all fitting {@link PriorizedInputAdapter}s
	 */
	private MouseAdapter swingMouseAdapter;
	/**
	 * The single {@link KeyListener} that is listening on this Circuit panel
	 * and launching all fitting {@link PriorizedInputAdapter}s
	 */
	private KeyListener swingKeyListener;
	/**
	 * Tells if the {@code CircuitPanel} is in a special
	 * mode atm.
	 */
	private boolean inAddingMode = false;
	/**
	 * @see #inAddingMode
	 */
	private boolean inRemovingMode = false;
	/**
	 * Used to tell whether or not the {@link gui.diagrams.Pin}s of the
	 * components should be drawn for this {@code CircuitPanel}.
	 * @see #paintComponent(java.awt.Graphics)
	 * @see #paintDiagramComponent(java.awt.Graphics2D, gui.TransformableDrawable, gui.GuiColor, gui.GuiColor, int)
	 */
	private boolean drawPins = true;
	/**
	 * Used to tell whether or not the message icons of the components should be
	 * drawn for this {@code CircuitPanel}.
	 * @see #OVERLAY_ALL
	 * @see #paintComponent(java.awt.Graphics)
	 * @see #paintDiagramComponent(java.awt.Graphics2D, gui.TransformableDrawable, gui.GuiColor, gui.GuiColor, int)
	 */
	private boolean drawMessageIcons = true;
	/**
	 * {@link #dropOverlays} are only drawn when this is set to {@code true}.
	 * Normally the {@link #signalMouseAdapter} will set this property.
	 */
	private boolean drawDropOverlays = false;
	private boolean showTooltips = false;
	/**
	 * These are {@link DropOverlay}s that will be displayed when connecting
	 * a signal and can be used to configureComponent signals to special endings, like
	 * marking them for being attached to the clock, etc.<br/><br/>
	 * This array is normally not modified, but may be by internal methods or
	 * classes.
	 */
	private DropOverlay[] dropOverlays = new DropOverlay[]{DropOverlay.ATTACH, DropOverlay.CLOCK, DropOverlay.OUT};
	// <editor-fold defaultstate="collapsed" desc="Debug Code">
	// TODO: Remove debug code if no longer neccessary
	private boolean RTReeDrawing = false;
	// </editor-fold>
	/**
	 * This {@link Rectangle} always marks the exact part of the circuit
	 * the user is able to see, even if the {@link #sizeBehaviour} is set to
	 * {@link SizeBehaviour#SHRINK_DRAWING}.
	 */
	private Rectangle visibleRectangle = null;
	/**
	 * This {@link Rectangle} marks the are that should be visible if the panels
	 * {@link #sizeBehaviour} is set to {@link SizeBehaviour#SHRINK_DRAWING}.
	 * This is normally the area of the circuit's area unified with the
	 * {@link #visibleRectangle} but this may change due to scrolling of the
	 * user while using {@link SizeBehaviour#SHRINK_DRAWING}.
	 */
	private Rectangle boundingBox = null;
	/**
	 * This is the {@link AffineTransform} that is used each time this panel is
	 * painted. It is recalculated if the {@link #sizeBehaviour} changes via
	 * {@link #setSizeBehaviour(gui.CircuitPanel.SizeBehaviour)}.
	 */
	private AffineTransform transform = null;
	/**
	 * A {@code Rectangle} which is used to select several components be drawing
	 * it around them. This is only used for drawing, the selection is done in
	 * the MouseAdapters created in the constructor.
	 */
	private Rectangle selectionFrame = null;
	public PinSelection pinSelection = null;
	/**
	 * A line indicating a signal linkage that is currently being built.
	 */
	private Line2D linkageConstructionLine = null;
	/**
	 * The message that is displayed in the center of this {@code CircuitPanel}
	 * of there are no components to draw.
	 */
	private String emptyMessage;
	private SignalMouseAdapter signalMouseAdapter = new SignalMouseAdapter(20) {

		// TODO: Comment this class, so that the diverse if decisions are readable
		private boolean draggingScrollBar = false;
		private Pin srcPin = null;
		private List<Integer> srcLineIndexes = new ArrayList<Integer>();
		private List<List<Integer>> srcBitIndexes = new ArrayList<List<Integer>>();
		private Timer statusTimer = new Timer(200, new AbstractAction() {

			@Override
			public void actionPerformed(ActionEvent e) {
				String message = null;
				DropOverlay overlay = CircuitPanel.this.isWithinDropOverlay(mouseMoveCoords);
				if (overlay == null) {
					List<ComponentWrapper> elements = CircuitPanel.this.circuitManager.findElements(mouseMoveCoords);
					if (elements.size() > 0) {
						Diagram diagram = elements.get(0).getDiagram();
						if ((diagram instanceof ComponentDiagram) && ((ComponentDiagram) diagram).getPin(mouseMoveCoords) != null) {
							Pin pin = ((ComponentDiagram) diagram).getPin(mouseMoveCoords);
							message = "pin <strong>" + pin.getName() + "</strong>";
						} else
							message = "component <strong>" + elements.get(0).toString() + "</strong>";
					} else {
						LinkageSegment[] segments = CircuitPanel.this.circuitManager.getLinkageSegments(
								mouseMoveCoords,
								Properties.getProfile().getPinSelectionHaziness());
						if (segments.length > 0)
							message = "signal: <strong>" + segments[0].getGroup().getSignalValues() + "</strong>";
					}
				}
				if (message != null)
					ViewManager.getInstance().getGroup(CircuitPanel.this).setStatusMsg("<html><body>" +
							"Mouse hovering over " + message +
							"</body></html>");
			}
		});
		private Point mouseMoveCoords;

		{
			this.statusTimer.setRepeats(false);
		}

		@Override
		public void finishedSelection() {
			if (this.srcPin != null) {
				List<Integer> destLineIndexes = new ArrayList<Integer>();
				List<List<Integer>> destBitIndexes = new ArrayList<List<Integer>>();
				CircuitPanel.this.pinSelection.writeIndexes(destLineIndexes, destBitIndexes);
				int selectedBitCount = this.connect(CircuitPanel.this.pinSelection.getPin(), destLineIndexes, destBitIndexes);
				switch (selectedBitCount) {
					case 0:
						this.cleanup();
						break;
					case -1:
						JOptionPane.showMessageDialog(CircuitPanel.this, "Cannot connect an input with an input pin or an output with an output pin.", "Connecting error", JOptionPane.WARNING_MESSAGE);
						CircuitPanel.this.pinSelection.destroy();
						CircuitPanel.this.pinSelection = null;
						break;
					default:
						JOptionPane.showMessageDialog(CircuitPanel.this, "You have selected " + selectedBitCount + " pins in " + this.srcLineIndexes.size() + " lines to be connected. Please select an equal amount as destiantion pins.", "Connecting error", JOptionPane.WARNING_MESSAGE);
						CircuitPanel.this.pinSelection.destroy();
						CircuitPanel.this.pinSelection = null;
						break;
				}
			} else {
				CircuitPanel.this.pinSelection.writeIndexes(this.srcLineIndexes, this.srcBitIndexes);
				if (this.srcLineIndexes.size() > 0) {
					this.srcPin = CircuitPanel.this.pinSelection.getPin();
					this.createLinkageConstructionLine(this.srcPin.getRealPoint());
					CircuitPanel.this.pinSelection.destroy();
					CircuitPanel.this.pinSelection = null;
				} else
					this.cleanup();
			}
			CircuitPanel.this.repaint();
		}

		private void createLinkageConstructionLine(Point point) {
			CircuitPanel.this.linkageConstructionLine = new Line2D.Double(point, point);
			CircuitPanel.this.drawDropOverlays = true;
		}

		private int connect(Pin destPin, List<Integer> destLineIndexes, List<List<Integer>> destBitIndexes) {
			if (this.srcPin == null)
				return -2;
			int srcBitCount = 0;
			int destBitCount = 0;
			for (int i = 0; i < this.srcBitIndexes.size(); i++)
				srcBitCount += this.srcBitIndexes.get(i).size();
			for (int i = 0; i < destBitIndexes.size(); i++)
				destBitCount += destBitIndexes.get(i).size();
			if (srcBitCount == destBitCount) {
				int srcLineIndex = 0;
				int srcBitIndex = 0;
				int destLineIndex = 0;
				int destBitIndex = 0;
				for (int i = 0; i < srcBitCount; i++) {
					if (srcBitIndex == this.srcBitIndexes.get(srcLineIndex).size()) {
						srcLineIndex++;
						srcBitIndex = 0;
					}
					if (destBitIndex == this.srcBitIndexes.get(destLineIndex).size()) {
						destLineIndex++;
						destBitIndex = 0;
					}
					CircuitPanel.this.signalConnectionAttempt(
							this.srcPin.getConnector(),
							destPin.getConnector(),
							this.srcLineIndexes.get(srcLineIndex),
							destLineIndexes.get(destLineIndex),
							this.srcBitIndexes.get(srcLineIndex).get(srcBitIndex),
							destBitIndexes.get(destLineIndex).get(destBitIndex));
					srcBitIndex++;
					destBitIndex++;
				}
				return 0;
			} else
				return srcBitCount;
		}

		private void cleanup() {
			boolean repaint = CircuitPanel.this.pinSelection != null ||
					CircuitPanel.this.linkageConstructionLine != null;
			if (CircuitPanel.this.pinSelection != null)
				CircuitPanel.this.pinSelection.destroy();
			CircuitPanel.this.pinSelection = null;
			CircuitPanel.this.linkageConstructionLine = null;
			CircuitPanel.this.drawDropOverlays = false;
			this.srcPin = null;
			this.srcLineIndexes.clear();
			this.srcBitIndexes.clear();
			this.draggingScrollBar = false;
			if (repaint)
				CircuitPanel.this.repaint();
		}

		@Override
		public boolean mouseClicked(MouseEvent evt) {
			if (evt.getButton() != MouseEvent.BUTTON1)
				return false;
			Point position = CircuitPanel.this.correctByScrollOffset(evt.getPoint(), true);
			if (CircuitPanel.this.pinSelection != null)
				return CircuitPanel.this.pinSelection.mouseClicked(evt);
			else {
				DropOverlay overlay = CircuitPanel.this.isWithinDropOverlay(evt.getPoint());
				if (overlay != null) {
					if (this.srcPin != null)
						for (int i = 0; i < this.srcLineIndexes.size(); i++)
							for (int j = 0; j < this.srcBitIndexes.get(i).size(); j++)
								CircuitPanel.this.signalConnectionAttempt(
										this.srcPin.getConnector(),
										overlay.getConnector(),
										this.srcLineIndexes.get(i),
										0,
										this.srcBitIndexes.get(i).get(j),
										0);
					this.cleanup();
				} else {
					List<ComponentWrapper> elements = CircuitPanel.this.circuitManager.findElements(position);
					if (elements.size() == 0)
						this.cleanup();
					else {
						Diagram diagram = elements.get(0).getDiagram();
						if (!(diagram instanceof ComponentDiagram) || ((ComponentDiagram) diagram).getPin(position) == null)
							this.cleanup();
						else {
							Pin pin = ((ComponentDiagram) diagram).getPin(position);
							if (!pin.hasConnector())
								throw new IllegalStateException("Found pin without allocated connector. Cannot enable signal drawing.");
							if (pin.getConnector().getLineCount() == 1 && pin.getConnector().getBitWidth(0) == 1)
								if (this.srcPin == null) {
									this.srcPin = pin;
									this.srcLineIndexes.add(0);
									this.srcBitIndexes.add(new LinkedList<Integer>());
									this.srcBitIndexes.get(0).add(0);
									this.createLinkageConstructionLine(this.srcPin.getRealPoint());
									CircuitPanel.this.repaint();
								} else {
									List<Integer> destLineIndexes = new LinkedList<Integer>();
									destLineIndexes.add(0);
									List<List<Integer>> destBitIndexes = new LinkedList<List<Integer>>();
									destBitIndexes.add(new LinkedList<Integer>());
									destBitIndexes.get(0).add(0);
									this.connect(pin, destLineIndexes, destBitIndexes);
									this.cleanup();
								}
							else if (evt.isControlDown() && this.srcPin == null) {
								this.srcPin = pin;
								for (int lineIndex = 0; lineIndex < pin.getConnector().getLineCount(); lineIndex++) {
									this.srcLineIndexes.add(lineIndex);
									this.srcBitIndexes.add(new LinkedList<Integer>());
									for (int bitIndex = 0; bitIndex < pin.getConnector().getBitWidth(lineIndex); bitIndex++)
										this.srcBitIndexes.get(lineIndex).add(bitIndex);
									this.createLinkageConstructionLine(this.srcPin.getRealPoint());
									CircuitPanel.this.repaint();
								}
							} else {
								List<Integer> destLineIndexes = new ArrayList<Integer>();
								List<List<Integer>> destBitIndexes = new ArrayList<List<Integer>>();
								for (int i = 0; i < pin.getConnector().getLineCount(); i++) {
									destLineIndexes.add(i);
									destBitIndexes.add(new ArrayList<Integer>());
									for (int j = 0; j < pin.getConnector().getBitWidth(i); j++)
										destBitIndexes.get(i).add(j);
								}
								if (this.connect(pin, destLineIndexes, destBitIndexes) == 0)
									this.cleanup();
								else
									CircuitPanel.this.pinSelection = PinSelection.contructPinSelection(
											pin,
											CircuitPanel.this,
											this,
											PinSelection.Type.OVERLAY);
							}
							return true;
						}
					}
				}
				return false;
			}
		}

		@Override
		public boolean mouseDragged(MouseEvent evt) {
			if (CircuitPanel.this.pinSelection != null)
				return CircuitPanel.this.pinSelection.mouseDragged(evt);
			return false;
		}

		@Override
		public boolean mouseMoved(MouseEvent evt) {
			if (CircuitPanel.this.pinSelection != null)
				return CircuitPanel.this.pinSelection.mouseMoved(evt);
			if (this.srcPin != null) {
				Point position = CircuitPanel.this.correctByScrollOffset(evt.getPoint(), true);
				CircuitPanel.this.linkageConstructionLine.setLine(this.srcPin.getRealPoint(), position);
				CircuitPanel.this.repaint();
			}
			this.mouseMoveCoords = new Point(CircuitPanel.this.correctByScrollOffset(evt.getPoint(), true));
			this.statusTimer.restart();
			ViewManager.getInstance().getGroup(CircuitPanel.this).setStatusMsg(null);
			return false;
		}

		@Override
		public boolean mousePressed(MouseEvent evt) {
			if (CircuitPanel.this.pinSelection != null)
				return CircuitPanel.this.pinSelection.mousePressed(evt);
			return false;
		}

		@Override
		public boolean mouseReleased(MouseEvent evt) {
			if (CircuitPanel.this.pinSelection != null)
				return CircuitPanel.this.pinSelection.mouseReleased(evt);
			return false;
		}

		@Override
		public boolean mouseWheelMoved(MouseWheelEvent evt) {
			if (CircuitPanel.this.pinSelection != null)
				return CircuitPanel.this.pinSelection.mouseWheelMoved(evt);
			return false;
		}
	};

	public CircuitPanel(CircuitManager manager, String name) {
		this.setCircuitManager(manager);
		this.setName(name);

		this.emptyMessage = this.isEditable() ? "Drop components here" : "No components in this view";

		this.setBackground(Properties.getProfile().getBgColor());

		this.swingMouseAdapter = new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent e) {
				for (PriorizedInputAdapter adapter : CircuitPanel.this.inputAdapters)
					if (adapter.mouseClicked(e))
						break;
			}

			@Override
			public void mouseDragged(MouseEvent e) {
				for (PriorizedInputAdapter adapter : CircuitPanel.this.inputAdapters)
					if (adapter.mouseDragged(e))
						break;
			}

			@Override
			public void mouseEntered(MouseEvent e) {
				for (PriorizedInputAdapter adapter : CircuitPanel.this.inputAdapters)
					if (adapter.mouseEntered(e))
						break;
			}

			@Override
			public void mouseExited(MouseEvent e) {
				for (PriorizedInputAdapter adapter : CircuitPanel.this.inputAdapters)
					if (adapter.mouseExited(e))
						break;
			}

			@Override
			public void mouseMoved(MouseEvent e) {
				for (PriorizedInputAdapter adapter : CircuitPanel.this.inputAdapters)
					if (adapter.mouseMoved(e))
						break;
			}

			@Override
			public void mousePressed(MouseEvent e) {
				for (PriorizedInputAdapter adapter : CircuitPanel.this.inputAdapters)
					if (adapter.mousePressed(e))
						break;
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				for (PriorizedInputAdapter adapter : CircuitPanel.this.inputAdapters)
					if (adapter.mouseReleased(e))
						break;
			}

			@Override
			public void mouseWheelMoved(MouseWheelEvent e) {
				for (PriorizedInputAdapter adapter : CircuitPanel.this.inputAdapters)
					if (adapter.mouseWheelMoved(e))
						break;
			}
		};
		this.addMouseListener(this.swingMouseAdapter);
		this.addMouseMotionListener(this.swingMouseAdapter);
		this.addMouseWheelListener(this.swingMouseAdapter);
		this.swingKeyListener = new KeyListener() {

			@Override
			public void keyTyped(KeyEvent e) {
				for (PriorizedInputAdapter adapter : CircuitPanel.this.inputAdapters)
					if (adapter.keyTyped(e))
						break;
			}

			@Override
			public void keyPressed(KeyEvent e) {
				for (PriorizedInputAdapter adapter : CircuitPanel.this.inputAdapters)
					if (adapter.keyPressed(e))
						break;
			}

			@Override
			public void keyReleased(KeyEvent e) {
				for (PriorizedInputAdapter adapter : CircuitPanel.this.inputAdapters)
					if (adapter.keyReleased(e))
						break;
			}
		};
		this.addKeyListener(this.swingKeyListener);

		// Selection handling mouse adapter
		this.addInputAdapter(new PriorizedInputAdapter(100) {

			private List<ComponentWrapper> componentsToBeUnselected = new ArrayList<ComponentWrapper>();
			private Point selectionFrameStart = null;

			@Override
			public boolean mousePressed(MouseEvent evt) {
				Point position = CircuitPanel.this.correctByScrollOffset(evt.getPoint(), true);
				List<ComponentWrapper> elements = CircuitPanel.this.circuitManager.findElements(position);
				if (elements.size() == 0) {
					if (!evt.isControlDown())
						CircuitPanel.this.clearSelectedComponents();
					if (evt.getButton() == MouseEvent.BUTTON1) {
						this.selectionFrameStart = position;
						return true;
					}
				} else {
					this.componentsToBeUnselected.clear();
					ComponentWrapper element = elements.get(0);
					if (evt.isControlDown())
						if (CircuitPanel.this.selectedWrappers.contains(element))
							this.componentsToBeUnselected.add(element);
						else
							CircuitPanel.this.selectComponents(element);
					else {
						if (CircuitPanel.this.selectedWrappers.size() > 1) {
							this.componentsToBeUnselected.addAll(CircuitPanel.this.selectedWrappers);
							this.componentsToBeUnselected.remove(element);
						} else
							CircuitPanel.this.clearSelectedComponents();
						CircuitPanel.this.selectComponents(element);
					}
				}
				CircuitPanel.this.repaint();
				return false;
			}

			@Override
			public boolean mouseReleased(MouseEvent evt) {
				this.selectionFrameStart = null;

				if (CircuitPanel.this.selectionFrame != null) {
					List<ComponentWrapper> elements = CircuitPanel.this.circuitManager.findElements(CircuitPanel.this.selectionFrame, true);
					if (elements.size() == 0) {
						if (!evt.isControlDown())
							CircuitPanel.this.clearSelectedComponents();
					} else {
						if (!evt.isControlDown())
							CircuitPanel.this.clearSelectedComponents();
						CircuitPanel.this.selectComponents(elements.toArray(new ComponentWrapper[0]));
					}
					CircuitPanel.this.selectionFrame = null;
					CircuitPanel.this.repaint();
				}

				return false;
			}

			@Override
			public boolean mouseClicked(MouseEvent evt) {
				CircuitPanel.this.deselectComponents(this.componentsToBeUnselected.toArray(new ComponentWrapper[0]));
				this.componentsToBeUnselected.clear();
				CircuitPanel.this.repaint();
				CircuitPanel.this.requestFocusInWindow();
				return false;
			}

			@Override
			public boolean mouseDragged(MouseEvent evt) {
				Point position = CircuitPanel.this.correctByScrollOffset(evt.getPoint(), true);
				if (this.selectionFrameStart != null) {
					if (CircuitPanel.this.selectionFrame == null)
						CircuitPanel.this.selectionFrame = new Rectangle();
					int x1 = this.selectionFrameStart.x;
					int y1 = this.selectionFrameStart.y;
					int x2 = position.x;
					int y2 = position.y;
					if (x1 > x2) {
						int tmp = x1;
						x1 = x2;
						x2 = tmp;
					}
					if (y1 > y2) {
						int tmp = y1;
						y1 = y2;
						y2 = tmp;
					}
					CircuitPanel.this.selectionFrame.setBounds(x1, y1, x2 - x1, y2 - y1);
					CircuitPanel.this.repaint();
					return true;
				}
				return false;
			}
		});
		// Panel moving mouse adapter
		this.addInputAdapter(new PriorizedInputAdapter(90) {

			private Point panelMovePosition = null;
			private Cursor originalCursor = null;

			@Override
			public boolean mousePressed(MouseEvent evt) {
				Point position = CircuitPanel.this.correctByScrollOffset(evt.getPoint(), true);
				if (CircuitPanel.this.circuitManager.findElements(position).size() == 0 && evt.isShiftDown()) {
					/*
					 * Do not use position corrected by offset but original position
					 * here. Otherwise the offset would be added up strangely.
					 */
					this.panelMovePosition = evt.getPoint();
					this.originalCursor = CircuitPanel.this.getCursor();
					CircuitPanel.this.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
					return true;
				}
				return false;
			}

			@Override
			public boolean mouseReleased(MouseEvent evt) {
				this.panelMovePosition = null;
				if (CircuitPanel.this.getCursor().getType() == Cursor.MOVE_CURSOR) {
					CircuitPanel.this.setCursor(this.originalCursor);
					this.originalCursor = null;
				}
				return false;
			}

			@Override
			public boolean mouseDragged(MouseEvent evt) {
				if (this.panelMovePosition != null) {
					/*
					 * Do not use position corrected by offset but original position
					 * here. Otherwise the offset would be added up strangely.
					 */
					Point offset = CircuitPanel.this.getScrollOffset();
					offset.translate(evt.getX() - this.panelMovePosition.x, evt.getY() - this.panelMovePosition.y);
					CircuitPanel.this.setScrollOffset(offset);
					this.panelMovePosition.setLocation(evt.getPoint());
					return true;
				}
				return false;
			}
		});
		// Popup menu mouse adapter
		this.addInputAdapter(new PriorizedInputAdapter(70) {

			private ComponentWrapper wrapper = null;
			private LinkageGroup linkageGroup = null;
			private JPopupMenu menu = new JPopupMenu();
			private ActionListener listener = new ActionListener() {

				// TODO: Find a way to explicitly use the variables of the
				// anonymous mother class (wrapper)
				@Override
				public void actionPerformed(ActionEvent evt) {
					if (evt.getSource() == configureComponent)
						wrapper.configure();
					else if (evt.getSource() == manageComponent)
						if (wrapper.getComponent() instanceof GUI)
							ManageDialog.createWithCustomGUI(((GUI) wrapper.getComponent()).getGUI()).setVisible(true);
						else if (wrapper.getComponent() instanceof Setable)
							ManageDialog.createWithGenericGUI((Setable) wrapper.getComponent(), true).setVisible(true);
						else;// TODO: Use global error handling here
					else if (evt.getSource() == pinSelector)
						if (wrapper.getConnectors()[0].hasPin())
							CircuitPanel.this.pinSelection = PinSelection.contructPinSelection(
									wrapper.getConnectors()[0].getPin(),
									CircuitPanel.this,
									CircuitPanel.this.signalMouseAdapter,
									PinSelection.Type.DIALOG);
						else
							throw new IllegalStateException("Found connector without assigned pin in a configured component.");
					else if (evt.getSource() == deleteComponent)
						CircuitPanel.this.componentRemovalAttempt(wrapper);
					else if (evt.getSource() == openComponent) {
						if (wrapper.isConfigured() && wrapper.isBuilt() && wrapper.getComponent() instanceof ComponentCollection) {
							LinkedList<ComponentCollection> parents = new LinkedList<ComponentCollection>(
									CircuitPanel.this.getCircuitManager().getParents());
							parents.add((ComponentCollection) wrapper.getComponent());
							CircuitPanel.this.setCircuitManager(CircuitManager.getManager(parents, null));
						}
					} else if (evt.getSource() == openComponentNewView) {
						if (wrapper.isConfigured() && wrapper.isBuilt() && wrapper.getComponent() instanceof ComponentCollection) {
							LinkedList<ComponentCollection> parents = new LinkedList<ComponentCollection>(
									CircuitPanel.this.getCircuitManager().getParents());
							parents.add((ComponentCollection) wrapper.getComponent());
							ViewManager.getInstance().addView(
									ViewManager.getInstance().getGroup(CircuitPanel.this),
									new CircuitPanel(CircuitManager.getManager(parents, null), "View of " + wrapper.getComponent().getShortName()));
						}
					} else if (evt.getSource() == manageSignal)
						new ManageSignalDialog(linkageGroup).setVisible(true);
					else if (evt.getSource() == deleteSignal)
						while (!linkageGroup.isEmpty()) {
							Linkage linkage = linkageGroup.getLinkages().get(0);
							CircuitPanel.this.getCircuitManager().removeLinkage(linkage);
						}
					else if (evt.getSource() == toggleMapView)
						CircuitPanel.this.toggleMapView();
					else if (evt.getSource() == showTooltips) {
						CircuitPanel.this.showTooltips = !CircuitPanel.this.showTooltips;
						CircuitPanel.this.repaint();
					}
				}
			};
			private JMenuItem configureComponent;
			private JMenuItem manageComponent;
			private JMenuItem manageSignal;
			private JMenuItem deleteSignal;
			private JMenuItem pinSelector;
			private JMenuItem deleteComponent;
			private JMenuItem openComponent;
			private JMenuItem openComponentNewView;
			private JCheckBoxMenuItem toggleMapView;
			private JCheckBoxMenuItem showTooltips;

			{
				this.configureComponent = new JMenuItem("Configure component", Utilities.getIcon("wrench.png", true));
				this.configureComponent.addActionListener(listener);
				this.pinSelector = new JMenuItem("Pin selector", Utilities.getIcon("pinSelector.png", false));
				this.pinSelector.addActionListener(listener);
				this.deleteComponent = new JMenuItem("Delete component", Utilities.getIcon("delete.png", true));
				this.deleteComponent.addActionListener(listener);
				this.manageComponent = new JMenuItem("Manage component");
				this.manageComponent.addActionListener(listener);
				this.openComponent = new JMenuItem("Open component");
				this.openComponent.addActionListener(listener);
				this.openComponentNewView = new JMenuItem("Open component in new view");
				this.openComponentNewView.addActionListener(listener);

				this.manageSignal = new JMenuItem("Manage signal", Utilities.getIcon("pencil.png", true));
				this.manageSignal.addActionListener(listener);
				this.deleteSignal = new JMenuItem("Delete signal", Utilities.getIcon("delete.png", true));
				this.deleteSignal.addActionListener(listener);

				this.toggleMapView = new JCheckBoxMenuItem("View in Map Mode");
				this.toggleMapView.addActionListener(listener);
				this.showTooltips = new JCheckBoxMenuItem("Show tooltips");
				this.showTooltips.addActionListener(listener);

				this.menu.add(this.configureComponent);
				this.menu.add(this.manageComponent);
				this.menu.addSeparator();
				this.menu.add(this.pinSelector);
				this.menu.addSeparator();
				this.menu.add(this.deleteComponent);
				this.menu.addSeparator();
				this.menu.add(this.openComponent);
				this.menu.add(this.openComponentNewView);
				this.menu.addSeparator();
				this.menu.add(this.manageSignal);
				this.menu.add(this.deleteSignal);
				this.menu.addSeparator();
				this.menu.add(this.toggleMapView);
				this.menu.add(this.showTooltips);
			}

			@Override
			public boolean mousePressed(MouseEvent evt) {
				if (evt.isPopupTrigger()) {
					showPopup(evt);
					return true;
				}
				return false;
			}

			@Override
			public boolean mouseReleased(MouseEvent evt) {
				if (evt.isPopupTrigger()) {
					showPopup(evt);
					return true;
				}
				return false;
			}

			private void showPopup(MouseEvent evt) {
				Point position = CircuitPanel.this.correctByScrollOffset(evt.getPoint(), true);
				List<ComponentWrapper> elements = CircuitPanel.this.circuitManager.findElements(position);
				this.wrapper = null;

				if (elements.size() != 0) {
					this.wrapper = elements.get(0);
					Diagram diagram = this.wrapper.getDiagram();
					this.configureComponent.setEnabled(true);
					this.deleteComponent.setEnabled(true);
					this.openComponent.setEnabled(
							this.wrapper.isConfigured() &&
							this.wrapper.isBuilt() &&
							this.wrapper.getComponent() instanceof ComponentCollection);
					this.openComponentNewView.setEnabled(
							this.wrapper.isConfigured() &&
							this.wrapper.isBuilt() &&
							this.wrapper.getComponent() instanceof ComponentCollection);
					if ((diagram instanceof ComponentDiagram) && ((ComponentDiagram) diagram).getPin(position) == null)
						this.pinSelector.setEnabled(elements.get(0).isConfigured());
					else
						this.pinSelector.setEnabled(false);
					if (this.wrapper.isBuilt() && this.wrapper.getComponent() instanceof Setable)
						this.manageComponent.setEnabled(elements.get(0).isConfigured());
					else
						this.manageComponent.setEnabled(false);
				} else {
					this.configureComponent.setEnabled(false);
					this.manageComponent.setEnabled(false);
					this.pinSelector.setEnabled(false);
					this.deleteComponent.setEnabled(false);
					this.openComponent.setEnabled(false);
					this.openComponentNewView.setEnabled(false);
				}

				LinkageSegment[] segments = CircuitPanel.this.circuitManager.getLinkageSegments(
						position,
						Properties.getProfile().getPinSelectionHaziness());
				if (segments.length > 0) {
					Set<LinkageGroup> linkageGroups = new HashSet<LinkageGroup>();
					for (LinkageSegment segment : segments)
						linkageGroups.add(segment.getGroup());
					this.linkageGroup = linkageGroups.iterator().next();
					this.manageSignal.setEnabled(true);
					this.deleteSignal.setEnabled(true);
				} else {
					this.manageSignal.setEnabled(false);
					this.deleteSignal.setEnabled(false);
				}

				this.toggleMapView.setSelected(CircuitPanel.this.mapView);
				this.showTooltips.setSelected(CircuitPanel.this.showTooltips);

				this.menu.show(CircuitPanel.this, evt.getX(), evt.getY());
			}
		});
		// Wrapper moving mouse adapter
		this.addInputAdapter(new PriorizedInputAdapter(60) {

			private Set<ComponentGhost> dragGhosts = new HashSet<ComponentGhost>();
			private Point mouseDragPosition = null;
			private Cursor originalCursor = null;

			@Override
			public boolean mousePressed(MouseEvent evt) {
				if (evt.getButton() == MouseEvent.BUTTON1) {
					Point position = CircuitPanel.this.correctByScrollOffset(evt.getPoint(), true);
					List<ComponentWrapper> elements = CircuitPanel.this.circuitManager.findElements(position);
					if (elements.size() != 0) {
						Diagram diagram = elements.get(0).getDiagram();
						if ((diagram instanceof ComponentDiagram) && ((ComponentDiagram) diagram).getPin(position) == null) {
							this.mouseDragPosition = position;
							this.originalCursor = CircuitPanel.this.getCursor();
							CircuitPanel.this.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
						}
					}
				}
				return false;
			}

			@Override
			public boolean mouseReleased(MouseEvent evt) {
				this.mouseDragPosition = null;
				if (CircuitPanel.this.getCursor().getType() == Cursor.MOVE_CURSOR) {
					CircuitPanel.this.setCursor(this.originalCursor);
					this.originalCursor = null;
				}

				if (!this.dragGhosts.isEmpty()) {
					for (ComponentGhost dragGhost : this.dragGhosts)
						dragGhost.getSoul().setPosition(dragGhost.getPosition());
					CircuitPanel.this.ghosts.removeAll(this.dragGhosts);
					this.dragGhosts.clear();
				}
				return false;
			}

			@Override
			public boolean mouseDragged(MouseEvent evt) {
				Point position = CircuitPanel.this.correctByScrollOffset(evt.getPoint(), true);
				// TODO: Keep scrollOffset in mind while calculating areaToDraw
				Rectangle areaToRedraw = new Rectangle();
				if (this.mouseDragPosition != null) {
					if (this.dragGhosts.isEmpty())
						for (ComponentWrapper selectedComponent : CircuitPanel.this.selectedWrappers) {
							ComponentGhost dragGhost;
							if (selectedComponent.getDiagram() instanceof Eldritch)
								dragGhost = ((Eldritch) selectedComponent.getDiagram()).getGhost(selectedComponent);
							else
								dragGhost = new ComponentGhost.DefaultGhost(selectedComponent);
							this.dragGhosts.add(dragGhost);
							CircuitPanel.this.ghosts.add(dragGhost);
						}
					int stepTranslationX = position.x - this.mouseDragPosition.x;
					int stepTranslationY = position.y - this.mouseDragPosition.y;
					for (ComponentGhost dragGhost : this.dragGhosts) {
						areaToRedraw = areaToRedraw.union(dragGhost.getBounds());
						dragGhost.setPosition(new Point(
								dragGhost.getPosition().x + stepTranslationX,
								dragGhost.getPosition().y + stepTranslationY));
						areaToRedraw = areaToRedraw.union(dragGhost.getBounds());
					}
					this.mouseDragPosition = position;
					// TODO: Find a better way than this to ensure that there
					// are no small borderlines which aren't drawn over.
					areaToRedraw.grow(2, 2);
					CircuitPanel.this.repaint(areaToRedraw, true);
					return true;
				}
				return false;
			}
		});
		// Wrapper deleting key adapter
		this.addInputAdapter(new PriorizedInputAdapter(50) {

			@Override
			public boolean keyReleased(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_DELETE) {
					Set<ComponentWrapper> wrappersToDelete = new HashSet<ComponentWrapper>(CircuitPanel.this.selectedWrappers);
					for (ComponentWrapper wrapper : wrappersToDelete)
						CircuitPanel.this.componentRemovalAttempt(wrapper);
					return true;
				}
				return false;
			}
		});
		// Special Mode Mouse Adapter
		if (this.isEditable())
			this.addInputAdapter(new PriorizedInputAdapter(40) {

				@Override
				public boolean mousePressed(MouseEvent e) {
					return CircuitPanel.this.isInSpecialMode();
				}

				@Override
				public boolean mouseReleased(MouseEvent e) {
					return CircuitPanel.this.isInSpecialMode();
				}

				@Override
				public boolean mouseClicked(MouseEvent evt) {
					if (!CircuitPanel.this.isInSpecialMode())
						return false;
					Point position = CircuitPanel.this.correctByScrollOffset(evt.getPoint(), true);
					if (CircuitPanel.this.isInAddingMode())
						CircuitPanel.this.componentAdditionAttempt(evt.getPoint());
					else if (CircuitPanel.this.isInRemovingMode()) {
						List<ComponentWrapper> elements = CircuitPanel.this.circuitManager.findElements(position);
						if (elements.size() > 0)
							CircuitPanel.this.componentRemovalAttempt(elements.get(0));
					}
					return true;
				}
			});
		// Signal drawing mouse adapter
		this.addInputAdapter(this.signalMouseAdapter); // (Priority 20)
		// Tool tip mouse adapter
		this.addInputAdapter(new PriorizedInputAdapter(10) {

			@Override
			public boolean keyTyped(KeyEvent evt) {
				if (evt.getKeyChar() == 't') {
					CircuitPanel.this.showTooltips = !CircuitPanel.this.showTooltips;
					CircuitPanel.this.repaint();
					return true;
				} else
					return false;
			}
		});
		// Mapview managing mouse adapter
		this.addInputAdapter(new PriorizedInputAdapter(0) {

			@Override
			public boolean mouseClicked(MouseEvent e) {
				return CircuitPanel.this.isMapView();
			}

			@Override
			public boolean mouseDragged(MouseEvent e) {
				return CircuitPanel.this.isMapView();
			}

			@Override
			public boolean mouseEntered(MouseEvent e) {
				return CircuitPanel.this.isMapView();
			}

			@Override
			public boolean mouseExited(MouseEvent e) {
				return CircuitPanel.this.isMapView();
			}

			@Override
			public boolean mouseMoved(MouseEvent e) {
				if (CircuitPanel.this.mapView) {
					Point newScrollOffset = new Point();
					try {
						CircuitPanel.this.transform.inverseTransform(e.getPoint(), newScrollOffset);
					} catch (NoninvertibleTransformException ex) {
						// Must not occur ;)
						}
					newScrollOffset.setLocation(newScrollOffset.getX() * -1, newScrollOffset.getY() * -1);
					newScrollOffset.translate(CircuitPanel.this.getWidth() / 2, CircuitPanel.this.getHeight() / 2);
					CircuitPanel.this.setScrollOffset(newScrollOffset);
					CircuitPanel.this.repaint();
					return true;
				} else
					return false;
			}

			@Override
			public boolean mousePressed(MouseEvent e) {
				return CircuitPanel.this.isMapView();
			}

			@Override
			public boolean mouseReleased(MouseEvent e) {
				return CircuitPanel.this.isMapView();
			}

			@Override
			public boolean keyReleased(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_M && e.isAltDown()) {
					CircuitPanel.this.toggleMapView();
					return true;
				} else
					return CircuitPanel.this.isMapView();
			}

			@Override
			public boolean keyPressed(KeyEvent e) {
				return CircuitPanel.this.isMapView();
			}

			@Override
			public boolean keyTyped(KeyEvent e) {
				return CircuitPanel.this.isMapView();
			}
		});

		if (this.isEditable())
			this.setTransferHandler(new TransferHandler() {

				private static final long serialVersionUID = 656852438749542359L;

				@Override
				public boolean canImport(TransferSupport support) {
					try {
						return support.isDataFlavorSupported(new DataFlavor(ExtensionTransferable.EXTENSION_MIME_TYPE));
					} catch (java.lang.ClassNotFoundException e) {
						return false;
					}
				}

				@Override
				public boolean canImport(JComponent comp, DataFlavor[] transferFlavors) {
					return true;
				}

				@Override
				public int getSourceActions(JComponent c) {
					return NONE;
				}

				@Override
				public boolean importData(TransferSupport support) {
					CircuitPanel panel;
					Extension extension;
					try {
						panel = (CircuitPanel) support.getComponent();
					} catch (java.lang.ClassCastException e) {
						System.out.println(e.getMessage());
						return false;
					}
					try {
						extension = (Extension) support.getTransferable().getTransferData(new DataFlavor(ExtensionTransferable.EXTENSION_MIME_TYPE));
					} catch (IOException e) {
						// TODO: Find better error messages by determining under which circumstances these error might occur
						System.out.println("Error: CircuitPanel.importData 1");
						return false;
					} catch (ClassNotFoundException e) {
						/* Falvor broke */
						// TODO: Find better error messages
						System.out.println("Error: CircuitPanel.importData 2");
						return false;
					} catch (UnsupportedFlavorException e) {
						// TODO: Find better error messages by determining under which circumstances these error might occur
						System.out.println("Error: CircuitPanel.importData 3");
						return false;
					} catch (java.lang.ClassCastException e) {
						// TODO: Find better error messages by determining under which circumstances these error might occur
						System.out.println("Error: CircuitPanel.importData 4");
						System.out.println(e.getMessage());
						return false;
					}
					panel.componentAdditionAttempt(extension, support.getDropLocation().getDropPoint());
					return true;
				}
			});

		// Recalculates the visibleRect on resize
		this.addComponentListener(new ComponentAdapter() {

			@Override
			public void componentResized(ComponentEvent evt) {
				CircuitPanel.this.calculateVisibleRect();
			}
		});
	}

	public CircuitPanel copy() {
		CircuitPanel panel = new CircuitPanel(this.circuitManager, "Copy of " + this.getName());
		panel.scrollOffset = new Point(this.scrollOffset);
		return panel;
	}

	private final boolean addInputAdapter(PriorizedInputAdapter adapter) {
		if (this.inputAdapters.contains(adapter))
			return false;
		else {
			this.inputAdapters.add(adapter);
			Collections.sort(this.inputAdapters);
			return true;
		}
	}

	private final boolean removeInputAdapter(PriorizedInputAdapter adapter) {
		return this.inputAdapters.remove(adapter);
	}

	/**
	 * Recalculate the {@link AffineTransform} that is stored in
	 * {@link #transform}. The transformation is calculated depeding on the
	 * {@link SizeBehaviour} currently set in {@link #sizeBehaviour}:
	 * <ul>
	 *	<li>For {@code KEEP_SIZE} the tranformation does nothing at all.</li>
	 *  <li>For {@code SHRINK_DRAWING} the transformation scales and translates
	 * the drawing so that the whole circuit <em>and</em> the area currently
	 * viewed by the user (which may lay outside the circuit) can be seen.</li>
	 * </ul>
	 * This also sets the {@link #visibleRectangle}.
	 */
	private final void calculateTransformation() {
		/*
		 * Affine Matrix tranformation:
		 *	x' = m00x + m01y + m02
		 *	y' = m10x + m11y + m12
		 * Affine Matrix Parameters:
		 *	m00, m10, m01, m11, m02, m12
		 */
		if (this.mapView && (this.circuitManager.getComponentCount() > 0)) {
			this.boundingBox = this.circuitManager.getBoundingRect().union(
					new Rectangle(-this.scrollOffset.x, -this.scrollOffset.y, this.getWidth(), this.getHeight()));
			double scale = this.getHeight() / (this.boundingBox.getMaxY() - this.boundingBox.getMinY());
			Dimension offset = new Dimension();
			if (scale > this.getWidth() / (this.boundingBox.getMaxX() - this.boundingBox.getMinX()))
				scale = this.getWidth() / (this.boundingBox.getMaxX() - this.boundingBox.getMinX());
			if (scale > 0.75)
				scale = 0.75;
			offset.setSize(
					(this.getWidth() - this.boundingBox.getWidth() * scale) / 2 - this.boundingBox.getMinX() * scale,
					(this.getHeight() - this.boundingBox.getHeight() * scale) / 2 - this.boundingBox.getMinY() * scale);
			this.transform = new AffineTransform(scale, 0, 0, scale, offset.getWidth(), offset.getHeight());
		} else
			this.transform = new AffineTransform(1, 0, 0, 1, 0, 0);
	}

	private final void calculateVisibleRect() {
		this.visibleRectangle = new Rectangle(-this.scrollOffset.x, -this.scrollOffset.y, this.getWidth(), this.getHeight());
	}

	@Override
	@Deprecated
	public void repaint() {
		super.repaint();
	}

	public void repaint(Rectangle rectangle, boolean usesCircuitCoordinates) {
		if (rectangle == null)
			rectangle = new Rectangle(this.getSize());
		else if (usesCircuitCoordinates)
			this.correctByScrollOffset(rectangle, false);
		super.repaint(rectangle);
	}

	@Override
	public final void paintComponent(Graphics g) {
		Graphics2D graphics = (Graphics2D) g.create();

		graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		graphics.setBackground(Properties.getProfile().getBgColor());

		if (this.isOpaque())
			graphics.clearRect(0, 0, this.getWidth(), this.getHeight());

		if (this.transform == null)
			this.calculateTransformation();

		graphics.transform(this.transform);

		this.paintCircuit(graphics);
		this.paintOverlays(graphics);
	}

	/**
	 * Paints the circuit, i.e. paints all components and ghosts of this
	 * {@code CircuitPanel}. Pins and message icons are painted according to
	 * {@link #drawPins} and {@link #drawMessageIcons}.
	 * <br/><br/>
	 * This method internally uses
	 * {@link #paintDiagramComponent(java.awt.Graphics2D, gui.TransformableDrawable, gui.GuiColor, gui.GuiColor, int)}.
	 * <br/><br/>
	 * Classes extending {@code CircuitPanel} may override this method but
	 * <em>must</em> call this method internally <em>before</em> doing anything
	 * else.
	 * <br/><br/>
	 * Due to the above rule it is not possible to draw anything ontop of the
	 * drawings of a child class. Use {@link #paintOverlays(java.awt.Graphics)}
	 * to do this.
	 *
	 * @param graphics The {@code Graphics2D} object in which should drawn. This
	 * object is already transformed as required by {@link #sizeBehaviour}, so
	 * do not transform it yourself.
	 */
	private void paintCircuit(Graphics2D graphics) {
		graphics.setPaint(Properties.getProfile().getStrokeColor());
		graphics.setStroke(Properties.getProfile().getDefaultStroke());

		Rectangle areaToDraw = this.correctByScrollOffset(graphics.getClipBounds(), true);

		if (this.circuitManager.getComponentCount() == 0) {
			graphics.setPaint(Properties.getProfile().getTextColor());
			graphics.drawString(this.emptyMessage,
					(this.getWidth() - graphics.getFontMetrics().stringWidth(this.emptyMessage)) / 2,
					this.getHeight() / 2);
		} else {
			for (LinkageSegment segment : this.circuitManager.getSignalSegments(areaToDraw))
				segment.paintSegment(graphics, this.getScrollOffset(true));
			/* TODO: Determine a useful number of components that should drawn
			regularly instead of using the R-Tree
			 */
			if (this.circuitManager.getComponentCount() < 10 && areaToDraw.contains(this.circuitManager.getBoundingRect())) {
				// <editor-fold defaultstate="collapsed" desc="Debug Code">
				// TODO: Remove debug code if no longer neccessary
				this.RTReeDrawing = true;
				// </editor-fold>
				for (ComponentWrapper currentComponent = this.circuitManager.getComponentList().start;
						currentComponent != null;
						currentComponent = currentComponent.getNextComponent())
					this.paintDiagramComponent(graphics, currentComponent);
			} else {
				// <editor-fold defaultstate="collapsed" desc="Debug Code">
				// TODO: Remove debug code if no longer neccessary
				this.RTReeDrawing = false;
				// </editor-fold>
				List<ComponentWrapper> componentsToDraw = this.circuitManager.findElements(areaToDraw, false);
				Collections.sort(componentsToDraw);
				for (int i = 0; i < componentsToDraw.size(); i++) {
					ComponentWrapper currentComponent = componentsToDraw.get(i);
					this.paintDiagramComponent(graphics, currentComponent);
				}
			}
			for (ComponentGhost ghost : this.ghosts)
				this.paintDiagramComponent(graphics, ghost);
		}
		// Draw selection frames of selected components
		graphics.setStroke(Properties.getProfile().getDashedStroke());
		graphics.setPaint(Properties.getProfile().getSelectionBGColor());
		for (ComponentWrapper selectedWrapper : this.selectedWrappers) {
			Rectangle selection = this.correctByScrollOffset(new Rectangle(selectedWrapper.getBounds()), false);
			selection.grow(2, 2);
			graphics.draw(selection);
		}
		// Draw signal linkage line
		if (this.linkageConstructionLine != null) {
			graphics.setStroke(Properties.getProfile().getDottedStroke());
			graphics.setPaint(Properties.getProfile().getStrokeColor());
			graphics.draw(this.correctByScrollOffset((Line2D) this.linkageConstructionLine.clone(), false));
			graphics.setStroke(Properties.getProfile().getDefaultStroke());
		}
	}

	/**
	 * Paints the overlays of this circuit, e.g. the shade shown when
	 * {@link #sizeBehaviour} is set to {@link SizeBehaviour#SHRINK_DRAWING}.
	 * This method is called after {@link #paintCircuit(java.awt.Graphics2D)}
	 * has been called. (Keep in mind, that this means
	 * {@link #paintCircuit(java.awt.Graphics2D)} of all ancestors has been
	 * called, too.
	 * <br/><br/>
	 * Classes overwriting this method <em>must</em> call this method internally
	 * <em>after</em> doing everything else.
	 *
	 * @param graphics The {@code Graphics2D} object in which should drawn. This
	 * object is already transformed as required by {@link #sizeBehaviour}, so
	 * do not transform it yourself.
	 */
	private void paintOverlays(Graphics2D graphics) {
		// Draw user dragged selection frame
		if (this.selectionFrame != null) {
			graphics.setStroke(Properties.getProfile().getDefaultStroke());
			graphics.setPaint(Properties.getProfile().getSelectionBGColor());
			Rectangle translatedFrame = this.correctByScrollOffset(new Rectangle(this.selectionFrame), false);
			graphics.setPaint(Properties.getProfile().getSelectionBGColor().scaleAlpha(0.25));
			graphics.fill(translatedFrame);
			graphics.setPaint(Properties.getProfile().getSelectionBGColor());
			graphics.draw(translatedFrame);
		}

		// Draw overlays for displaying singal bit pins
		if (this.pinSelection != null)
			this.pinSelection.paint(graphics);

		// <editor-fold defaultstate="collapsed" desc="Debug Code">
		// TODO: Remove debug code if no longer neccessary
		// Toggle "true" "false" in if clause to show hide debugging info
		if (Properties.getProfile().isDebugMode() && !this.mapView) {
			graphics.setStroke(Properties.getProfile().getDashedStroke().changeWidth(3));
			graphics.setPaint(Properties.getProfile().getSelectionBGColor());
			graphics.draw(this.correctByScrollOffset(new Rectangle(this.visibleRectangle), false));
			graphics.setStroke(Properties.getProfile().getDefaultStroke());
			graphics.setPaint(Properties.getProfile().getTextColor());
			graphics.drawString("Debug info:", 5, 15);
			if (this.circuitManager.getComponentList().start != null) {
				graphics.drawString("Drawing of R-Tree: ON", 5, 30);
				this.paintTree(graphics, this.circuitManager.getRTree(), 0);
				graphics.setPaint(new GuiColor(0));
			} else
				graphics.drawString("Drawing of R-Tree: OFF", 5, 30);
			graphics.drawString("Scroll offset: X=" + this.scrollOffset.x + " Y=" + this.scrollOffset.y, 5, 45);
			graphics.drawString("Visible rect: " + this.visibleRectangle.toString(), 5, 60);
			graphics.drawString("Drawing mechanism: " + (this.RTReeDrawing ? "Regular" : "R-Tree"), 5, 75);
		}
		// </editor-fold>

		// Draw tooltips
		if (this.showTooltips) {
			int height = 0;
			Collections.sort(this.tooltips, new Comparator<Tooltip>() {

				@Override
				public int compare(Tooltip t1, Tooltip t2) {
					return t1.point.y - t2.point.y;
				}
			});
			for (Tooltip tip : this.tooltips) {
				tip.drawTip(graphics, new Point(0, height));
				height += 2 + tip.getHeight(graphics);
			}
		}

		if (this.mapView) {
			graphics.setStroke(Properties.getProfile().getDefaultStroke());
			graphics.setPaint(Properties.getProfile().getOverlayColor().setAlpha(100));

			Rectangle window = new Rectangle(this.getSize());
			window.setLocation(-this.getScrollOffset(false).x, -this.getScrollOffset(false).y);

			GeneralPath mask = new GeneralPath(GeneralPath.WIND_EVEN_ODD);
			mask.moveTo(this.visibleRectangle.getMinX(), this.visibleRectangle.getMinY());
			mask.lineTo(this.visibleRectangle.getMaxX(), this.visibleRectangle.getMinY());
			mask.lineTo(this.visibleRectangle.getMaxX(), this.visibleRectangle.getMaxY());
			mask.lineTo(this.visibleRectangle.getMinX(), this.visibleRectangle.getMaxY());
			mask.closePath();
			mask.append(window, false);

			graphics.fill(mask);
			graphics.setPaint(Properties.getProfile().getOverlayColor());
			graphics.draw(window);
		}

		if (this.drawDropOverlays) {
			int yOffset = 0;
			for (DropOverlay overlay : this.dropOverlays) {
				overlay.setLocation(new Point(this.getWidth() - overlay.getRectangle().width, yOffset));
				yOffset += overlay.getRectangle().height;
				graphics.setStroke(Properties.getProfile().getDefaultStroke());
				graphics.setPaint(Properties.getProfile().getBgColor().setAlpha(130));
				graphics.fill(overlay.getRectangle());
				graphics.setPaint(Properties.getProfile().getBgColor());
				graphics.draw(overlay.getRectangle());
				graphics.drawImage(
						overlay.getImage(),
						overlay.getRectangle().x + (overlay.getRectangle().width - overlay.getImage().getWidth(this)) / 2,
						overlay.getRectangle().y + (overlay.getRectangle().height - overlay.getImage().getHeight(this)) / 2,
						Utilities.imageObserver);
			}
		}
	}

	/**
	 * Draws a single given component in the given {@link Graphics2D} object
	 * using the given {@link GuiColor}s and additionally drawing the message
	 * icons corresponding to the given {@code overlay}s.
	 *
	 * @param graphics The {@code Graphics2D} to drw into
	 * @param component The component to draw given as {@code TransformableDrawable}.
	 */
	// TODO: Move this method in TransformableDrawable to keep style with LiunkageSegement
	private void paintDiagramComponent(Graphics2D graphics, TransformableDrawable component) {
		Diagram diagram = component.getDiagram();
		ComponentWrapper wrapper = null;
		if (component instanceof ComponentWrapper)
			wrapper = (ComponentWrapper) component;

		Path2D path = diagram.getTransformedPath();
		path.transform(AffineTransform.getTranslateInstance(this.getScrollOffset(true).getX(), this.getScrollOffset(true).getY()));
		graphics.setStroke(Properties.getProfile().getDefaultStroke());
		if (component instanceof ComponentGhost)
			graphics.setPaint(Properties.getProfile().getGhostFillColor());
		else
			graphics.setPaint(Properties.getProfile().getFillColor());
		graphics.fill(path);
		if (component instanceof ComponentGhost)
			graphics.setPaint(Properties.getProfile().getGhostStrokeColor());
		else
			graphics.setPaint(Properties.getProfile().getStrokeColor());
		graphics.draw(path);

		if (this.drawPins && wrapper != null && wrapper.isConfigured() && diagram instanceof ComponentDiagram)
			for (Pin pin : ((ComponentDiagram) diagram).getPins())
				pin.paintPin(graphics, this.getScrollOffset(true), false);

		if (this.drawMessageIcons && wrapper != null) {
			Rectangle transformedComponentBounds = this.correctByScrollOffset(new Rectangle(component.getBounds()), false);
			int overlayXOffset = 0;
			graphics.setPaint(new GuiColor(255, 255, 255, 200));
			if (!wrapper.isConfigured()) {
				graphics.fill(new Rectangle(
						new Point(transformedComponentBounds.x + overlayXOffset, transformedComponentBounds.y - 16),
						new Dimension(16, 16)));
				graphics.drawImage(
						Utilities.getImage("wrench.png", true),
						transformedComponentBounds.x + overlayXOffset,
						transformedComponentBounds.y - 16,
						Utilities.imageObserver);
				overlayXOffset += 16;
			}
			if (!wrapper.isBuilt()) {
				graphics.fill(new Rectangle(
						new Point(transformedComponentBounds.x + overlayXOffset, transformedComponentBounds.y - 16),
						new Dimension(16, 16)));
				graphics.drawImage(
						Utilities.getImage("error.png", true),
						transformedComponentBounds.x + overlayXOffset,
						transformedComponentBounds.y - 16,
						Utilities.imageObserver);
				overlayXOffset += 16;
			}
		}
		// <editor-fold defaultstate="collapsed" desc="Debug Code">
		// Toggle "true" "false" in if clause to show hide debugging info
		if (Properties.getProfile().isDebugMode()) {
			Rectangle transformedComponentBounds = this.correctByScrollOffset(new Rectangle(component.getBounds()), false);
			graphics.setPaint(new GuiColor(0));
			graphics.drawRect((int) transformedComponentBounds.getMinX() - 15, (int) transformedComponentBounds.getMinY() - 15, 5, 5);
			graphics.drawRect((int) transformedComponentBounds.getMaxX() + 15, (int) transformedComponentBounds.getMinY() - 15, 5, 5);
			graphics.drawRect((int) transformedComponentBounds.getMaxX() + 15, (int) transformedComponentBounds.getMaxY() + 15, 5, 5);
			graphics.drawRect((int) transformedComponentBounds.getMinX() - 15, (int) transformedComponentBounds.getMaxY() + 15, 5, 5);
		}
		// </editor-fold>
	}

	/**
	 * This is a pure debugging function, it draws the boxes used by the
	 * R-Tree.
	 *
	 * @param g
	 * @param current
	 * @param depth
	 */
	private final void paintTree(Graphics2D g, RTreeNode<ComponentWrapper> current, int depth) {
		if (!current.isLeaf()) {
			switch (depth % 3) {
				case 0:
					g.setPaint(new GuiColor(255, 0, 0));
					break;
				case 1:
					g.setPaint(new GuiColor(0, 255, 0));
					break;
				case 2:
					g.setPaint(new GuiColor(0, 0, 255));
					break;
			}
			Rectangle frame = this.correctByScrollOffset(new Rectangle(current.getBounds()), false);
			g.draw(frame);
			for (RTreeNode<ComponentWrapper> entry : current.getChildren())
				this.paintTree(g, entry, depth + 1);
		}

	}

	public CircuitManager getCircuitManager() {
		return this.circuitManager;
	}

	public void setCircuitManager(CircuitManager circuitManager) {
		if (circuitManager != this.circuitManager) {
			if (this.circuitManager != null)
				this.circuitManager.removeModificationListener(this);
			this.selectedWrappers.clear();
			this.setScrollOffset(new Point(this.getWidth() / 2, this.getHeight() / 2));
			this.circuitManager = circuitManager;
			this.circuitManager.addModificationListener(this);
			ViewManager.getInstance().refreshFocusedView(this);
			this.repaint();
		}
	}

	public boolean isEditable() {
		return this.circuitManager.hasSupervisor();
	}

	@Override
	public void circuitChanged(CircuitModificationEvent evt) {
		if (evt.isWrapperEvent() && evt.getType() == WrapperEvent.TYPE_REMOVAL)
			this.selectedWrappers.remove(((WrapperEvent) evt).getWrapper());
		if (evt.isWrapperGeometryEvent())
			this.repaint(((WrapperGeometryEvent) evt).getAffectedSpace(), true);
		else
			this.repaint();
	}

	/**
	 * This is called internally, if there was the request to add a given
	 * component at the given position. This method simply informs the
	 * {@link CircuitManager} of this Panel.
	 *
	 * @param extension
	 * @param position
	 * @return Whether or not the addition was successfull.
	 */
	private final boolean componentAdditionAttempt(Extension extension, Point position) {
		return this.circuitManager.componentAdditionAttempt(extension, position);
	}

	/**
	 * This is called internally, if there was the request to add an unknown
	 * component at the given position. This method simply informs the
	 * {@link CircuitManager} of this Panel.
	 *
	 * @param position
	 * @return Whether or not the addition was successfull.
	 */
	private final boolean componentAdditionAttempt(Point position) {
		return this.circuitManager.componentAdditionAttempt(position);
	}

	/**
	 * This is called internally, if there was the request to remove the given
	 * component. This method simply informs the {@link CircuitManager} of this
	 * Panel.
	 *
	 * @param wrapper
	 * @return Whether or not the removeal was successfull.
	 */
	private final boolean componentRemovalAttempt(ComponentWrapper wrapper) {
		return this.circuitManager.componentRemovalAttempt(wrapper);
	}

	/**
	 * This is called internally, if there was the request to create a
	 * connection between the two given points.
	 * 
	 * @param connector1
	 * @param connector2
	 * @param line1
	 * @param line2
	 * @param bit1
	 * @param bit2
	 * @return
	 */
	private final boolean signalConnectionAttempt(Connector connector1, Connector connector2, int line1, int line2, int bit1, int bit2) {
		return this.circuitManager.signalConnectionAttempt(connector1, connector2, line1, line2, bit1, bit2);
	}

	public final void selectComponents(ComponentWrapper... wrappers) {
		for (ComponentWrapper wrapper : wrappers)
			this.selectedWrappers.add(wrapper);
		this.updateTooltips();
	}

	public final void deselectComponents(ComponentWrapper... wrappers) {
		this.selectedWrappers.removeAll(Arrays.asList(wrappers));
	}

	public final Set<ComponentWrapper> getSelectedComponents() {
		return Collections.unmodifiableSet(this.selectedWrappers);
	}

	public final void clearSelectedComponents() {
		this.selectedWrappers.clear();
		this.updateTooltips();
	}

	private void updateTooltips() {
		this.tooltips.clear();
		for (ComponentWrapper wrapper : this.selectedWrappers) {
			Diagram diagram = wrapper.getDiagram();
			this.tooltips.add(new Tooltip(
					this.correctByScrollOffset(diagram.getTransformedCenter(), false),
					wrapper.getComponent().getShortName() +
					(wrapper.getExtension() != null ? " (" + wrapper.getExtension().getName() + ")" : "")));
			if (diagram instanceof ComponentDiagram && wrapper.isConfigured())
				for (Pin pin : ((ComponentDiagram) diagram).getPins()) {
					String text = "Pin: " + pin.getName();
					if (pin.getConnector().getLineCount() > 1)
						text += " (Lines: " + pin.getConnector().getLineCount() + ")";
					else
						text += " (Bits: " + pin.getConnector().getBitWidth(0) + ")";
					this.tooltips.add(
							new Tooltip(this.correctByScrollOffset(new Point(pin.getRealPoint()), false), text));
				}
		}
		this.repaint();
	}

	/**
	 * Returns if the {@link #mapView} is set.
	 *
	 * @return {@code True} if mapView is set.
	 */
	public boolean isMapView() {
		return this.mapView;
	}

	/**
	 * Toggles the {@link #mapView}. Causes a the recalculation of
	 * {@link #transform} and a repaint.
	 *
	 * @see #calculateTransformation()
	 */
	public void toggleMapView() {
//		this.setMapView(!this.mapView);
	}

	/**
	 * Sets the {@link #mapView}. Causes a the recalculation of
	 * {@link #transform} and a repaint if the map view really changed.
	 *
	 * @param mapView
	 * @param boolean The new map view.
	 *
	 * @see #calculateTransformation()
	 */
	public void setMapView(boolean mapView) {
		if (this.mapView != mapView) {
			this.mapView = mapView;
			this.calculateTransformation();
			this.repaint();
		}
	}

	/**
	 * Returns the {@link #scrollOffset} currently applied to this
	 * {@code CircuitPanel}. The offset can be used to 'move' components around
	 * since all components are drawn translated by this offset.
	 *
	 * This function ignores the currently set {@link SizeBehaviour}.
	 *
	 * @return The {@code scrollOffset}
	 *
	 * @see #getScrollOffset(boolean)
	 */
	public Point getScrollOffset() {
		return this.getScrollOffset(false);
	}

	/**
	 * Returns the {@link #scrollOffset} currently applied to this
	 * {@code CircuitPanel}. The offset can be used to 'move' components around
	 * since all components are drawn translated by this offset.
	 *
	 * @param respectSizeBehaviour If this is set to {@code TRUE} the scroll
	 * offset is simulated to be zero if the {@link #sizeBehaviour} is set to
	 * {@link SizeBehaviour#SHRINK_DRAWING}
	 * @return
	 */
	public Point getScrollOffset(boolean respectSizeBehaviour) {
		if (!respectSizeBehaviour || !this.isMapView())
			return new Point(this.scrollOffset);
		else
			return new Point();
	}

	/**
	 * Sets the {@link #scrollOffset} currently applied to this
	 * {@code CircuitPanel}. The offset can be used to 'move' components around
	 * since all components are drawn translated by this offset. Also sets the
	 * {@link #visibleRectangle}.
	 * @param scrollOffset
	 */
	public void setScrollOffset(Point scrollOffset) {
		this.scrollOffset.setLocation(scrollOffset);
		this.calculateVisibleRect();
//		if (!this.isMapView())
//			this.visibleRectangle = new Rectangle(this.scrollOffset.x, this.scrollOffset.y, this.getWidth(), this.getHeight());
		// TODO: Check what this should notify. - Possibly nothing?
//		for (ChangeListener listener : this.changeListeners)
//			listener.notifyLayoutChange();
		this.repaint();
	}

	/**
	 * Adds the current {@link #scrollOffset} to the given Point. For
	 * convenience the modified Point is returned. If inverse is set to true,
	 * the scroll offset is subtracted.
	 *
	 * @param point The Point to modify
	 * @param inverse Whether or not to inverse the operation.
	 * @param respectSizeBehaviour If this is set to {@code TRUE} the scroll
	 * offset is simulated to be zero if the {@link #sizeBehaviour} is set to
	 * {@link SizeBehaviour#SHRINK_DRAWING}
	 * @return The modified Point
	 */
	public Point correctByScrollOffset(Point point, boolean inverse) {
		if (inverse)
			point.translate(-this.scrollOffset.x, -this.scrollOffset.y);
		else
			point.translate(this.scrollOffset.x, this.scrollOffset.y);
		return point;
	}

	/**
	 * Adds the current scrollOffset to the given Line2D. For convenience the
	 * modified Line2D is returned. If inverse is set to true, the scrollOffset
	 * is subtracted.
	 *
	 * @param line The Line2D to modify
	 * @param inverse Whether or not to inverse the operation.
	 * @param respectSizeBehaviour If this is set to {@code TRUE} the scroll
	 * offset is simulated to be zero if the {@link #sizeBehaviour} is set to
	 * {@link SizeBehaviour#SHRINK_DRAWING}
	 * @return The modified Line2D
	 */
	public Line2D correctByScrollOffset(Line2D line, boolean inverse) {
		double x1 = line.getX1();
		double x2 = line.getX2();
		double y1 = line.getY1();
		double y2 = line.getY2();
		if (inverse) {
			x1 -= this.scrollOffset.x;
			x2 -= this.scrollOffset.x;
			y1 -= this.scrollOffset.y;
			y2 -= this.scrollOffset.y;
		} else {
			x1 += this.scrollOffset.x;
			x2 += this.scrollOffset.x;
			y1 += this.scrollOffset.y;
			y2 += this.scrollOffset.y;
		}
		line.setLine(x1, y1, x2, y2);
		return line;
	}

	/**
	 * Adds the current scrollOffset to the given Rectangle. For convenience the
	 * modified Rectangle is returned. If inverse is set to true, the scrollOffset
	 * is subtracted.
	 *
	 * @param rect The Rectangle to modify
	 * @param inverse Whether or not to inverse the operation.
	 * @param respectSizeBehaviour If this is set to {@code TRUE} the scroll
	 * offset is simulated to be zero if the {@link #sizeBehaviour} is set to
	 * {@link SizeBehaviour#SHRINK_DRAWING}
	 * @return The modified Rectangle
	 */
	public Rectangle correctByScrollOffset(Rectangle rect, boolean inverse) {
		if (inverse)
			rect.translate(-this.scrollOffset.x, -this.scrollOffset.y);
		else
			rect.translate(this.scrollOffset.x, this.scrollOffset.y);
		return rect;
	}

	private DropOverlay isWithinDropOverlay(Point point) {
		if (this.drawDropOverlays)
			for (DropOverlay overlay : this.dropOverlays)
				if (overlay.getRectangle().contains(point))
					return overlay;
		return null;
	}

	public boolean isInAddingMode() {
		return inAddingMode;
	}

	public void setInAddingMode(boolean inAddingMode) {
		if (this.isEditable() && this.inAddingMode != inAddingMode) {
			this.inAddingMode = inAddingMode;
			if (this.inAddingMode)
				this.setCursor(Toolkit.getDefaultToolkit().createCustomCursor(addCursor, new Point(8, 8), "add cursor"));
			else
				this.setCursor(null);
			if (this.inRemovingMode) {
				this.inRemovingMode = false;
				this.firePropertyChange("inRemovingMode", true, false);
			}
			this.firePropertyChange("inAddingMode", !inAddingMode, inAddingMode);
		}
	}

	public boolean isInRemovingMode() {
		return inRemovingMode;
	}

	public void setInRemovingMode(boolean inRemovingMode) {
		if (this.isEditable() && this.inRemovingMode != inRemovingMode) {
			this.inRemovingMode = inRemovingMode;
			if (this.inRemovingMode)
				this.setCursor(Toolkit.getDefaultToolkit().createCustomCursor(removeCursor, new Point(8, 8), "remove cursor"));
			else
				this.setCursor(null);
			if (this.inAddingMode) {
				this.inAddingMode = false;
				this.firePropertyChange("inAddingMode", true, false);
			}
			this.firePropertyChange("inRemovingMode", !inRemovingMode, inRemovingMode);
		}
	}

	public boolean isInSpecialMode() {
		return this.isInAddingMode() || this.isInRemovingMode();
	}

	@Override
	public String toString() {
		return this.getName();
	}
}
