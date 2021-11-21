
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
package gui.dialogs;

import gui.*;
import gui.circuit.drawing.AbstractPin;
import gui.circuit.management.Connector;
import gui.circuit.drawing.Pin;
import gui.util.Properties;
import gui.circuit.drawing.SingleBitPin;
import gui.circuit.drawing.SingleLinePin;
import gui.util.Utilities;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

/**
 *
 * @author sylvester
 */
public abstract class PinSelection extends CircuitPanel.PriorizedInputAdapter {

	public static interface Listener {

		public void finishedSelection();

	}

	public static enum Type {

		OVERLAY,
		DIALOG
	}

	public static PinSelection contructPinSelection(Pin pin, CircuitPanel panel, Listener listener, Type type) {
		switch (type) {
			case OVERLAY:
				return new PinSelectionOverlay(pin, panel, listener, 10, 5);
			case DIALOG:
			default:
				return new PinSelectionDialog(pin, panel, listener);
		}
	}

	protected Pin pin;
	protected CircuitPanel panel;
	protected boolean bitsMode;
	protected int lineIndex;
	protected List<AbstractPin> childPins = new LinkedList<AbstractPin>();
	protected Set<AbstractPin> selectedPins = new HashSet<AbstractPin>();
	protected Listener listener;
	protected boolean destroyed = false;

	private PinSelection(Pin pin, CircuitPanel panel, Listener listener) {
		super(0); // The priorizedInputAdapter is used to provide the methods, but does not compete with others, so tha prority does not matter.

		if (!pin.hasConnector())
			throw new IllegalArgumentException("Cannot construct pinSelection with a pin without assigned connector.");

		this.pin = pin;
		this.panel = panel;
		this.listener = listener;

		if (pin.getConnector().getLineCount() == 1) {
			this.bitsMode = true;
			this.lineIndex = 0;
		} else
			this.bitsMode = false;

		this.buildPins();
	}

	protected void buildPins() {
		this.childPins.clear();
		this.selectedPins.clear();
		int pinCount = this.bitsMode ? this.pin.getConnector().getBitWidth(this.lineIndex) : this.pin.getConnector().getLineCount();
		if (this.bitsMode)
			for (int i = 0; i < pinCount; i++)
				this.childPins.add(new SingleBitPin(new Point(), this.pin.getConnector(), this.lineIndex, i));
		else
			for (int i = 0; i < pinCount; i++)
				this.childPins.add(new SingleLinePin(new Point(), this.pin.getConnector(), i));
	}

	public abstract void paint(Graphics2D graphics);

	public void writeIndexes(List<Integer> lineIndexes, List<List<Integer>> bitIndexes) {
		lineIndexes.clear();
		bitIndexes.clear();
		if (!this.selectedPins.isEmpty())
			if (this.bitsMode) {
				lineIndexes.add(this.lineIndex);
				bitIndexes.add(new ArrayList<Integer>());
				for (AbstractPin pin : this.selectedPins)
					bitIndexes.get(0).add(((SingleBitPin) pin).getBitIndex());
			} else
				for (AbstractPin abstractPin : this.selectedPins) {
					SingleLinePin pin = (SingleLinePin) abstractPin;
					lineIndexes.add(pin.getLineIndex());
					int bitWidth = pin.getConnector().getBitWidth(pin.getLineIndex());
					List<Integer> currentBitIndexList = new ArrayList<Integer>(bitWidth);
					for (int i = 0; i < bitWidth; i++)
						currentBitIndexList.add(i);
					bitIndexes.add(currentBitIndexList);
				}
	}

	public Pin getPin() {
		return this.pin;
	}

	public CircuitPanel getPanel() {
		return panel;
	}

	protected int childPinCount() {
		return this.childPins.size();
	}

	public void destroy() {
		this.destroyed = true;
	}

	private static class PinSelectionOverlay extends PinSelection {

		private int columns;
		private int maxColumns;
		private int rows;
		private int visibleRows;
		private int maxVisibleRows;
		private int rowOffset = 0;
		private FontMetrics fontMetrics;
		private FontMetrics titleFontMetrics;
		private Rectangle bounds;
		private int sideSize;
		private int hSideSize;
		private int rowNumbersWidth;
		private int columnNumbersHeight;
		private boolean draggingScrollBar;
		private Rectangle scrollableArea;
		private Rectangle button;
		private BufferedImage proceedTick;

		public PinSelectionOverlay(Pin pin, CircuitPanel panel, Listener listener, int maxColumns, int maxVisibleRows) {
			super(pin, panel, listener);

			this.maxColumns = maxColumns;
			this.maxVisibleRows = maxVisibleRows;

			this.proceedTick = Utilities.getImage("tick.png", true);

			this.columns = this.maxColumns < this.childPinCount() ? this.maxColumns : this.childPinCount();
			this.rows = (int) java.lang.Math.ceil(this.childPinCount() / (double) this.columns);
			if (this.rows < this.maxVisibleRows)
				this.visibleRows = this.rows;
			else
				this.visibleRows = this.maxVisibleRows;

			this.panel.repaint();
		}

		private void calculateView() {
			// Width and height of a single cell
			this.sideSize = this.fontMetrics.charWidth('0') * String.valueOf(this.columns).length();
			if (this.fontMetrics.getHeight() > sideSize)
				sideSize = this.fontMetrics.getHeight();
			this.hSideSize = sideSize / 2;

			// Text and size of the button
			String maxButtonText = "Finish";
			if (this.fontMetrics.stringWidth(maxButtonText) < this.fontMetrics.stringWidth("Proceed"))
				maxButtonText = "Proceed";
			this.button = new Rectangle(new Dimension(
					15 + this.proceedTick.getWidth() + this.fontMetrics.stringWidth(maxButtonText),
					5 + (this.proceedTick.getHeight() > this.fontMetrics.getHeight() ? this.proceedTick.getHeight() : this.fontMetrics.getHeight())));

			// Basic width
			int width = this.columns * this.sideSize;
			// plus border space
			width += 10;
			// plus possible scrollbar
			width += (this.visibleRows < this.rows ? 10 : 0);
			// plus space for line numbers
			if (this.rows > 1) {
				this.rowNumbersWidth = this.fontMetrics.charWidth('0') * String.valueOf((this.rows - 1) * this.columns + 1).length() + 5;
				width += rowNumbersWidth;
			}
			// Ensure there is enough space for the button and the title
			if (width < this.button.width + 10)
				width = this.button.width + 10;
			int titleWidth = this.titleFontMetrics.stringWidth("Select Bit(s)");
			if (titleWidth < this.titleFontMetrics.stringWidth("Select Line(s)"))
				titleWidth = this.titleFontMetrics.stringWidth("Select Line(s)");
			if (width < titleWidth + 10)
				width = titleWidth + 10;

			// Basic height
			int height = this.visibleRows * this.sideSize;
			// plus border space
			height += 10;
			// plus space for title
			height += this.fontMetrics.getHeight();
			// plus space for column numbers
			this.columnNumbersHeight = this.fontMetrics.getHeight() + 5;
			height += this.columnNumbersHeight;
			// plus space for "Next"/"Finish" button
			height += this.button.height + 10;

			// Position overlay
			this.bounds = new Rectangle(this.getPin().getRealPoint(), new Dimension(width, height));
			if (!this.getPanel().getVisibleRect().contains(this.bounds))
				this.bounds.translate(-width, 0);
			if (!this.getPanel().getVisibleRect().contains(this.bounds))
				this.bounds.translate(width, -height);
			if (!this.getPanel().getVisibleRect().contains(this.bounds))
				this.bounds.translate(-width, 0);

			// Position of the button
			this.button.setLocation(
					(int) (this.bounds.getMaxX() - this.button.getWidth() - 5),
					(int) (this.bounds.getMaxY() - this.button.getHeight() - 5));

			// Position of the pins
			int initialXOffset = this.bounds.x + 5 + this.rowNumbersWidth + this.hSideSize;
			int xOffset = initialXOffset;
			int yOffset = this.bounds.y + 5 + this.fontMetrics.getHeight() + this.columnNumbersHeight + this.hSideSize;
			int index = 0;
			int pinCount = this.childPinCount();
			for (int row = 0; row < this.rows; row++) {
				for (int col = 0; col < this.columns; col++) {
					if (index == pinCount)
						break;
					this.childPins.get(index).setRealPoint(new Point(xOffset, yOffset));
					index++;
					xOffset += this.sideSize;
				}
				xOffset = initialXOffset;
				yOffset += this.sideSize;
			}
		}

		@Override
		public void paint(Graphics2D graphics) {
			if (this.fontMetrics == null) {
				this.fontMetrics = graphics.getFontMetrics();
				this.titleFontMetrics = graphics.getFontMetrics(graphics.getFont().deriveFont(Font.BOLD));
				this.calculateView();
			}

			Stroke oldStroke = graphics.getStroke();
			Paint oldPaint = graphics.getPaint();
			AffineTransform oldTransform = graphics.getTransform();

			graphics.translate(this.panel.getScrollOffset().x, this.panel.getScrollOffset().y);

			// Paint border and background
			graphics.setStroke(Properties.getProfile().getDefaultStroke());
			graphics.setPaint(Properties.getProfile().getTooltipBGColor());
			graphics.fill(bounds);
			graphics.setPaint(Properties.getProfile().getTooltipFGColor());
			graphics.draw(bounds);

			// Paint title
			graphics.setPaint(Properties.getProfile().getTextColor());
			int xOffset = this.bounds.x + 5;
			int yOffset = this.bounds.y + 5 + this.fontMetrics.getHeight() - this.fontMetrics.getDescent();
			Font oldFont = this.fontMetrics.getFont();
			graphics.setFont(this.titleFontMetrics.getFont());
			graphics.drawString(this.bitsMode ? "Select Bit(s)" : "Select Line(s)", xOffset, yOffset);
			graphics.setFont(oldFont);

			// Paint Column numbers
			xOffset = this.bounds.x + 5 + this.rowNumbersWidth;
			yOffset = this.bounds.y + 5 + this.fontMetrics.getHeight() * 2 - this.fontMetrics.getDescent();
			for (int i = 1; i <= this.columns; i++) {
				String columnNumber = String.valueOf(i);
				graphics.drawString(columnNumber, xOffset + (this.sideSize - this.fontMetrics.stringWidth(columnNumber)) / 2, yOffset);
				xOffset += this.sideSize;
			}

			// Paint pins and rows numbers
			xOffset = this.bounds.x + 5 + this.rowNumbersWidth;
			yOffset = this.bounds.y + 5 + this.fontMetrics.getHeight() + this.columnNumbersHeight + this.rowOffset * this.sideSize;
			ListIterator<AbstractPin> iterator = this.childPins.listIterator(this.rowOffset * this.columns);
			AffineTransform beforePin = graphics.getTransform();
			graphics.translate(0, -this.rowOffset * this.sideSize);
			for (int i = 0; i < this.visibleRows; i++) {
				String lineNumber = String.valueOf((i + this.rowOffset) * this.columns);
				graphics.drawString(lineNumber, xOffset - 5 - this.fontMetrics.stringWidth(lineNumber), yOffset + (this.sideSize + this.fontMetrics.getAscent()) / 2);
				for (int j = 0; j < this.columns; j++) {
					if (!iterator.hasNext())
						break;
					AbstractPin currentPin = iterator.next();
					currentPin.paintPin(graphics, new Point(), this.selectedPins.contains(currentPin));
				}
				yOffset += this.sideSize;
			}
			graphics.setTransform(beforePin);

			// Paint scrollbar
			if (this.rows > this.visibleRows) {
				graphics.setPaint(Properties.getProfile().getTooltipFGColor());
				this.scrollableArea = new Rectangle(
						this.bounds.x + this.bounds.width - 13,
						this.bounds.y + 5 + this.fontMetrics.getHeight() + this.columnNumbersHeight,
						7,
						this.visibleRows * this.sideSize);
				graphics.draw(this.scrollableArea);
				graphics.fillRect(
						this.scrollableArea.x + 2,
						this.scrollableArea.y + 2 + ((this.scrollableArea.height - 4) * this.rowOffset / this.rows),
						this.scrollableArea.width - 3,
						(this.scrollableArea.height - 3) * this.visibleRows / this.rows);
			}

			// Paint proceed button
			graphics.setPaint(Properties.getProfile().getTooltipFGColor());
			graphics.fill3DRect(this.button.x, this.button.y, this.button.width, this.button.height, true);
			graphics.drawImage(this.proceedTick, this.button.x + 5, this.button.y + 5, Utilities.imageObserver);
			graphics.setPaint(Properties.getProfile().getTextColor());
			String buttonText;
			if (this.bitsMode || this.selectedPins.size() != 1)
				buttonText = "Finish";
			else
				buttonText = "Proceed";
			graphics.drawString(buttonText, this.button.x + this.proceedTick.getWidth() + 10, this.button.y + (this.button.height + this.fontMetrics.getAscent()) / 2);

			graphics.setStroke(oldStroke);
			graphics.setPaint(oldPaint);
			graphics.setTransform(oldTransform);
		}

		private boolean isWithinOverlay(Point point) {
			return this.bounds.contains(point);
		}

		private boolean isWithinScrollArea(Point point) {
			if (this.scrollableArea == null)
				return false;
			return this.scrollableArea.contains(point);
		}

		private void scrollByPoint(Point point) {
			int y = point.y - this.scrollableArea.y;
			if (y < 0) {
				this.rowOffset = 0;
				return;
			}
			if (y > this.scrollableArea.height) {
				this.rowOffset = this.rows - this.visibleRows;
				return;
			}
			this.rowOffset = y * (this.rows - this.visibleRows) / this.scrollableArea.height;
			this.panel.repaint();
		}

		private void scrollBy(int rows) {
			this.rowOffset += rows;
			if (this.rowOffset < 0)
				this.rowOffset = 0;
			if (this.rowOffset > this.rows - this.visibleRows)
				this.rowOffset = this.rows - this.visibleRows;
		}

		@Override
		public boolean mouseClicked(MouseEvent evt) {
			Point point = this.panel.correctByScrollOffset(evt.getPoint(), true);

			if (this.scrollableArea != null && this.scrollableArea.contains(point))
				this.scrollByPoint(point);
			else if (this.button.contains(point))
				if (this.bitsMode || this.selectedPins.size() != 1)
					this.listener.finishedSelection();
				else {
					this.bitsMode = true;
					this.lineIndex = ((SingleLinePin) this.selectedPins.iterator().next()).getLineIndex();
					this.buildPins();
					int pinCount = this.childPinCount();
					this.columns = this.maxColumns < pinCount ? this.maxColumns : pinCount;
					this.rows = (int) java.lang.Math.ceil(pinCount / (double) this.columns);
					if (this.rows < this.maxVisibleRows)
						this.visibleRows = this.rows;
					else
						this.visibleRows = this.maxVisibleRows;
					this.fontMetrics = null; // Leads to recalculation of pin etc. on next paint
				}
			else
				for (AbstractPin pin : this.childPins)
					if (pin.hit(point)) {
						if (this.selectedPins.contains(pin))
							this.selectedPins.remove(pin);
						else
							this.selectedPins.add(pin);
						break;
					}
			this.panel.repaint();
			if (this.isWithinOverlay(point))
				return true;
			else {
				this.listener.finishedSelection();
				return false;
			}
		}

		@Override
		public boolean mouseDragged(MouseEvent evt) {
			if (this.draggingScrollBar) {
				this.scrollByPoint(this.panel.correctByScrollOffset(evt.getPoint(), true));
				this.getPanel().repaint();
				return true;
			}
			return false;
		}

		@Override
		public boolean mouseMoved(MouseEvent e) {
			return true;
		}

		@Override
		public boolean mousePressed(MouseEvent evt) {
			if (this.isWithinOverlay(this.panel.correctByScrollOffset(evt.getPoint(), true))) {
				if (this.scrollableArea != null)
					this.draggingScrollBar = true;
				return true;
			}
			return false;
		}

		@Override
		public boolean mouseReleased(MouseEvent evt) {
			if (this.isWithinOverlay(this.panel.correctByScrollOffset(evt.getPoint(), true))) {
				this.draggingScrollBar = false;
				return true;
			}
			return false;
		}

		@Override
		public boolean mouseWheelMoved(MouseWheelEvent evt) {
			if (this.isWithinOverlay(this.panel.correctByScrollOffset(evt.getPoint(), true))) {
				this.scrollBy(evt.getWheelRotation());
				this.getPanel().repaint();
				return true;
			} else
				return false;
		}

		@Override
		public void destroy() {
			super.destroy();
		}

	}

	private static class PinSelectionDialog extends PinSelection {

		private JDialog dialog = new JDialog() {

			private JPanel mainPanel;
			private JScrollPane mainPanelScroll;
			private JComboBox pinSelectionComboBox;
			private JLabel pinSelectionLabel;
			private JLabel statusLabel;
			private JButton proceedButton;
			private JTextField selectionFormulaTextField;
			private Map<String, Pin> pins = new HashMap<String, Pin>();
			private int sideSize = 10;

			{
				pinSelectionLabel = new javax.swing.JLabel();
				pinSelectionComboBox = new javax.swing.JComboBox();
				selectionFormulaTextField = new javax.swing.JTextField();
				statusLabel = new JLabel("No pin under mouse");
				mainPanelScroll = new javax.swing.JScrollPane();
				mainPanel = new javax.swing.JPanel() {

					private FontMetrics fontMetrics;
					private int sideSize;
					private int hSideSize;
					private int rows;
					private int columns;
					private int rowNumbersWidth;

					{
						MouseAdapter mouseAdapter = new MouseAdapter() {

							@Override
							public void mouseClicked(MouseEvent evt) {
								int index = evt.getY() / sideSize * columns + (evt.getX() - rowNumbersWidth) / sideSize;
								if (PinSelectionDialog.this.childPins.size() <= index)
									statusLabel.setText("No pin under mouse");
								else {
									AbstractPin pin = PinSelectionDialog.this.childPins.get(index);
									if (pin.hit(evt.getPoint())) {
										if (PinSelectionDialog.this.selectedPins.contains(pin))
											PinSelectionDialog.this.selectedPins.remove(pin);
										else
											PinSelectionDialog.this.selectedPins.add(pin);
										adaptButton();
										repaint();
									}
								}
							}

							@Override
							public void mouseMoved(MouseEvent evt) {
								int index = evt.getY() / sideSize * columns + (evt.getX() - rowNumbersWidth) / sideSize;
								if (index < 0 || PinSelectionDialog.this.childPins.size() <= index)
									statusLabel.setText("No pin under mouse");
								else {
									AbstractPin pin = PinSelectionDialog.this.childPins.get(index);
									if (pin.hit(evt.getPoint()))
										if (pin instanceof SingleLinePin)
											statusLabel.setText("Mouse is over line number " + (((SingleLinePin) pin).getLineIndex() + 1));
										else
											statusLabel.setText("Mouse is over bit number " + (((SingleBitPin) pin).getBitIndex() + 1) + " (Line: " + (((SingleBitPin) pin).getLineIndex() + 1) + ")");
									else
										statusLabel.setText("No pin under mouse");
								}
							}

						};

						this.addMouseListener(mouseAdapter);
						this.addMouseMotionListener(mouseAdapter);
					}

					private void calculateView(FontMetrics fontMetrics) {
						// Width and height of a single cell
						this.sideSize = fontMetrics.charWidth('0') * String.valueOf(this.columns).length();
						if (fontMetrics.getHeight() > sideSize)
							sideSize = fontMetrics.getHeight();
						this.hSideSize = sideSize / 2;

						this.rowNumbersWidth = fontMetrics.charWidth('0') * String.valueOf(PinSelectionDialog.this.childPinCount()).length() + 5;

						int preferredWidth = mainPanelScroll.getViewport().getWidth();
						this.columns = (preferredWidth - this.rowNumbersWidth) / this.sideSize;
						if (this.columns > PinSelectionDialog.this.childPinCount())
							this.columns = PinSelectionDialog.this.childPinCount();
						this.rows = (int) java.lang.Math.ceil(PinSelectionDialog.this.childPinCount() / (double) this.columns);
						this.setPreferredSize(new Dimension(preferredWidth, this.rows * this.sideSize));
						this.revalidate();

						// Position of the pins
						int initialXOffset = this.rowNumbersWidth + this.hSideSize;
						int xOffset = initialXOffset;
						int yOffset = this.hSideSize;
						int index = 0;
						int pinCount = PinSelectionDialog.this.childPinCount();
						for (int row = 0; row < this.rows; row++) {
							for (int col = 0; col < this.columns; col++) {
								if (index == pinCount)
									break;
								PinSelectionDialog.this.childPins.get(index).setRealPoint(new Point(xOffset, yOffset));
								index++;
								xOffset += this.sideSize;
							}
							xOffset = initialXOffset;
							yOffset += this.sideSize;
						}
					}

					@Override
					public void paint(Graphics g) {
						super.paint(g);
						Graphics2D graphics = (Graphics2D) g;
						graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

						if (this.fontMetrics == null) {
							// Do NOT switch the following two lines, since calculateView calls revalidate which sets this.fontMetrics to null
							this.calculateView(graphics.getFontMetrics());
							this.fontMetrics = graphics.getFontMetrics();
						}

						// Paint pins and rows numbers
						int xOffset = this.rowNumbersWidth;
						int yOffset = 0;
						ListIterator<AbstractPin> iterator = PinSelectionDialog.this.childPins.listIterator();
						for (int i = 0; i < this.rows; i++) {
							String rowNumber = String.valueOf(i * this.columns + 1);
							graphics.drawString(rowNumber, xOffset - 5 - this.fontMetrics.stringWidth(rowNumber), yOffset + (this.sideSize + this.fontMetrics.getAscent()) / 2);
							for (int j = 0; j < this.columns; j++) {
								if (!iterator.hasNext())
									break;
								AbstractPin currentPin = iterator.next();
								currentPin.paintPin(graphics, new Point(), PinSelectionDialog.this.selectedPins.contains(currentPin));
							}
							yOffset += this.sideSize;
						}
					}

					@Override
					public void revalidate() {
						this.fontMetrics = null;
						super.revalidate();
						this.repaint();
					}

				};
				proceedButton = new javax.swing.JButton();

				this.setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
				WindowAdapter windowAdapter = new java.awt.event.WindowAdapter() {

					@Override
					public void windowClosing(java.awt.event.WindowEvent evt) {
						if (!PinSelectionDialog.this.destroyed)
							PinSelectionDialog.this.listener.finishedSelection();
					}

					@Override
					public void windowLostFocus(WindowEvent e) {
						if (!PinSelectionDialog.this.destroyed)
							PinSelectionDialog.this.listener.finishedSelection();
					}

				};
				this.addWindowListener(windowAdapter);
				this.addWindowFocusListener(windowAdapter);

				pinSelectionLabel.setText("Pin:");

				pinSelectionComboBox.addActionListener(new java.awt.event.ActionListener() {

					public void actionPerformed(java.awt.event.ActionEvent evt) {
						PinSelectionDialog.this.pin = pins.get(pinSelectionComboBox.getSelectedItem());
						if (PinSelectionDialog.this.pin.getConnector().getLineCount() == 1) {
							PinSelectionDialog.this.bitsMode = true;
							PinSelectionDialog.this.lineIndex = 0;
							setTitle("Select bit(s)");
							adaptButton();
						} else {
							PinSelectionDialog.this.bitsMode = false;
							setTitle("Select line(s)");
							adaptButton();
						}
						PinSelectionDialog.this.buildPins();
						mainPanel.revalidate();
					}

				});

				selectionFormulaTextField.setToolTipText("Enter here a expression in the style of \"4,10-22,d13,s25-27\" to select ('s'/default) and deselect ('d') the given pins. Press enter to execute.");
				selectionFormulaTextField.addKeyListener(new java.awt.event.KeyAdapter() {

					@Override
					public void keyPressed(java.awt.event.KeyEvent evt) {
						if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
							if (selectionFormulaTextField.getText().equalsIgnoreCase("ALL"))
								for (AbstractPin pin : PinSelectionDialog.this.childPins)
									PinSelectionDialog.this.selectedPins.add(pin);
							else if (selectionFormulaTextField.getText().equalsIgnoreCase("NONE"))
								PinSelectionDialog.this.selectedPins.clear();
							else
								Utilities.selectByString(
										PinSelectionDialog.this.childPins,
										PinSelectionDialog.this.selectedPins,
										selectionFormulaTextField.getText());
							mainPanel.repaint();
							adaptButton();
							selectionFormulaTextField.setText("");
						}
					}

				});

				javax.swing.GroupLayout mainPanelLayout = new javax.swing.GroupLayout(mainPanel);
				mainPanel.setLayout(mainPanelLayout);
				mainPanelLayout.setHorizontalGroup(
						mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGap(0, 484, Short.MAX_VALUE));
				mainPanelLayout.setVerticalGroup(
						mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGap(0, 188, Short.MAX_VALUE));

				mainPanelScroll.setViewportView(mainPanel);
				mainPanelScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
				mainPanelScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

				proceedButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gui/icons/famfamfam/tick.png")));
				if (PinSelectionDialog.this.bitsMode) {
					proceedButton.setText("Finish");
					this.setTitle("Select bit(s)");
				} else {
					proceedButton.setText("Proceed");
					this.setTitle("Select line(s)");
				}
//				this.getRootPane().setDefaultButton(proceedButton);
				proceedButton.addActionListener(new java.awt.event.ActionListener() {

					@Override
					public void actionPerformed(java.awt.event.ActionEvent evt) {
						if (PinSelectionDialog.this.bitsMode || PinSelectionDialog.this.selectedPins.size() != 1)
							PinSelectionDialog.this.listener.finishedSelection();
						else {
							PinSelectionDialog.this.bitsMode = true;
							PinSelectionDialog.this.buildPins();
							mainPanel.revalidate();
						}
					}

				});

				javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
				getContentPane().setLayout(layout);
				layout.setHorizontalGroup(
						layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(layout.createSequentialGroup().addContainerGap().addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(mainPanelScroll, javax.swing.GroupLayout.DEFAULT_SIZE, 486, Short.MAX_VALUE).addComponent(statusLabel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 486, Short.MAX_VALUE).addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup().addComponent(selectionFormulaTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 360, Short.MAX_VALUE).addGap(18, 18, 18).addComponent(proceedButton, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)).addGroup(layout.createSequentialGroup().addComponent(pinSelectionLabel).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(pinSelectionComboBox, 0, 451, Short.MAX_VALUE))).addContainerGap()));
				layout.setVerticalGroup(
						layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(layout.createSequentialGroup().addContainerGap().addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(pinSelectionComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE).addComponent(pinSelectionLabel)).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(mainPanelScroll, javax.swing.GroupLayout.DEFAULT_SIZE, 190, Short.MAX_VALUE).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(statusLabel).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(selectionFormulaTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE).addComponent(proceedButton)).addContainerGap()));

				pack();

				this.setLocationRelativeTo(Properties.getProfile().getMainWindow());

				initialize(PinSelectionDialog.this.getPin());
			}

			private void initialize(Pin selectedPin) {
				Connector[] connectors = PinSelectionDialog.this.getPin().getConnector().getWrapper().getConnectors();
				List<String> pinNames = new LinkedList<String>();
				for (int i = 0; i < connectors.length; i++)
					if (connectors[i].hasPin()) {
						Pin pin = connectors[i].getPin();
						pinNames.add(pin.getName());
						this.pins.put(pin.getName(), pin);
					}

				this.pinSelectionComboBox.setModel(new DefaultComboBoxModel(pinNames.toArray(new String[pinNames.size()])));
				this.pinSelectionComboBox.getModel().setSelectedItem(selectedPin.getName());
				this.selectionFormulaTextField.requestFocusInWindow();
			}

			private void adaptButton() {
				if (PinSelectionDialog.this.bitsMode || PinSelectionDialog.this.selectedPins.size() != 1)
					proceedButton.setText("Finish");
				else
					proceedButton.setText("Proceed");
			}

		};

		public PinSelectionDialog(Pin pin, CircuitPanel panel, Listener listener) {
			super(pin, panel, listener);
			this.dialog.setVisible(true);
		}

		@Override
		public void destroy() {
			super.destroy();
			this.dialog.dispose();
		}

		@Override
		public void paint(Graphics2D graphics) {
			return;
		}

	}

}
