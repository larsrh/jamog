package gui.dialogs.management;

import core.misc.BitConverter;
import core.misc.setable.Setable;
import core.signal.Bit;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.AbstractAction;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.MenuElement;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

/**
 * @author torben
 */
class GenericSetableGUITable extends JTable implements TableModel {

	private static enum Column {

		BINARY("Binary", true),
		BIT("Bits", true),
		FLOAT("Floating point", false),
		HEXA("Hexadecimal", true),
		OCTAL("Octal", true),
		OFFSET("Offset", false),
		SIGNED("Signed decimal", true),
		UNSIGNED("Unsigned decimal", true);
		private final String name;
		private final boolean editable;

		Column(String name, boolean editable) {
			this.name = name;
			this.editable = editable;
		}

		public String getName() {
			return name;
		}

		public boolean isEditable() {
			return editable;
		}
	}

	private class ColumnMenuActionHandler extends AbstractAction {

		private Column column;

		public ColumnMenuActionHandler(Column column) {
			super(column.getName());
			this.column = column;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (GenericSetableGUITable.this.visibleColumns.size() > 1 ||
					!GenericSetableGUITable.this.visibleColumns.contains(this.column)) {
				if (!GenericSetableGUITable.this.visibleColumns.remove(this.column))
					GenericSetableGUITable.this.visibleColumns.add(column);
				GenericSetableGUITable.this.notifyListeners(new TableModelEvent(GenericSetableGUITable.this, TableModelEvent.HEADER_ROW));
			}
		}

		public Column getColumn() {
			return column;
		}
	}
	private Setable setable;
	private boolean editable;
	private int bitsPerRow = 1;
	private int bitsPerOffset = 1;
	private final Set<TableModelListener> listeners = new HashSet<TableModelListener>();
	private final List<Column> visibleColumns = new ArrayList<Column>();
	private final JPopupMenu columnMenu = new JPopupMenu();

	public GenericSetableGUITable(Setable setable, boolean editable) {
		super();

		this.editable = editable;

		showColumn(Column.OFFSET);
		showColumn(Column.SIGNED);
		showColumn(Column.FLOAT);

		for (Column column : Column.values())
			this.columnMenu.add(new JCheckBoxMenuItem(new ColumnMenuActionHandler(column)));
		this.getTableHeader().addMouseListener(new MouseAdapter() {

			@Override
			public void mousePressed(MouseEvent evt) {
				if (evt.isPopupTrigger())
					this.showPopup(evt.getX(), evt.getY());
			}

			@Override
			public void mouseReleased(MouseEvent evt) {
				if (evt.isPopupTrigger())
					this.showPopup(evt.getX(), evt.getY());
			}

			private void showPopup(int x, int y) {
				for (MenuElement element : GenericSetableGUITable.this.columnMenu.getSubElements())
					if (element instanceof JCheckBoxMenuItem) {
						JCheckBoxMenuItem item = (JCheckBoxMenuItem) element;
						if (item.getAction() instanceof ColumnMenuActionHandler)
							item.setSelected(GenericSetableGUITable.this.visibleColumns.contains(((ColumnMenuActionHandler) item.getAction()).getColumn()));
					}
				GenericSetableGUITable.this.columnMenu.show(GenericSetableGUITable.this.getTableHeader(), x, y);
			}
		});

		this.setSetable(setable);

		this.setModel(this);
	}

	public Setable getSetable() {
		return this.setable;
	}

	void setSetable(Setable setable) {
		this.setable = setable;

		this.notifyListeners(new TableModelEvent(this, TableModelEvent.HEADER_ROW));
	}

	public boolean isEditable() {
		return this.editable;
	}

	public void setEditable(boolean editable) {
		this.editable = editable;
	}

	private final void showColumn(Column column) {
		if (!this.visibleColumns.contains(column)) {
			this.visibleColumns.add(column);
			notifyListeners(new TableModelEvent(this, TableModelEvent.HEADER_ROW));
		}
	}

	private final void hideColumn(Column column) {
		if (this.visibleColumns.contains(column)) {
			this.visibleColumns.remove(column);
			notifyListeners(new TableModelEvent(this, TableModelEvent.HEADER_ROW));
		}
	}

	public int getBitsPerOffset() {
		return bitsPerOffset;
	}

	public void setBitsPerOffset(int bitsPerOffset) {
		this.bitsPerOffset = bitsPerOffset;
		notifyListeners(new TableModelEvent(this, TableModelEvent.HEADER_ROW));
	}

	public int getBitsPerRow() {
		return bitsPerRow;
	}

	public void setBitsPerRow(int bitsPerRow) {
		this.bitsPerRow = bitsPerRow;
		notifyListeners(new TableModelEvent(this, TableModelEvent.HEADER_ROW));
	}

	@Override
	public final int getRowCount() {
		if (this.setable != null)
			return this.setable.getSetableCount() / bitsPerRow;
		else
			return 0;
	}

	@Override
	public final int getColumnCount() {
		return this.visibleColumns.size();
	}

	@Override
	public final String getColumnName(int index) {
		return this.visibleColumns.get(index).getName();
	}

	@Override
	public final Class<?> getColumnClass(int index) {
		return String.class;
	}

	@Override
	public final boolean isCellEditable(int rowIndex, int columnIndex) {
		return this.visibleColumns.get(columnIndex).isEditable() && this.editable;
	}

	@Override
	public final Object getValueAt(int rowIndex, int columnIndex) {
		Bit[] rowValue = new Bit[this.bitsPerRow];
		for (int i = 0; i < rowValue.length; i++)
			rowValue[i] = this.setable.getSetableBit(rowIndex * this.bitsPerRow + i);

		switch (this.visibleColumns.get(columnIndex)) {
			case BINARY:
				return BitConverter.bitsToInteger(false, rowValue).toString(2);
			case BIT:
				return BitConverter.bitsToString(rowValue).toString();
			case FLOAT:
				return Float.toString(Float.intBitsToFloat((int) BitConverter.bitsToLong(rowValue)));
			case HEXA:
				return BitConverter.bitsToInteger(false, rowValue).toString(16).toUpperCase();
			case OCTAL:
				return BitConverter.bitsToInteger(false, rowValue).toString(8);
			case OFFSET:
				if ((int) Math.ceil(Math.log(this.setable.getSetableCount() / bitsPerRow) / Math.log(16.0)) == 0)
					return String.format("%01X", rowIndex * bitsPerRow / bitsPerOffset);
				else
					return String.format(
							"%0" + (int) Math.ceil(Math.log(this.setable.getSetableCount() / bitsPerRow) / Math.log(16.0)) + "X",
							rowIndex * bitsPerRow / bitsPerOffset);
			case SIGNED:
				return BitConverter.bitsToInteger(true, rowValue).toString(10);
			case UNSIGNED:
				return BitConverter.bitsToInteger(false, rowValue).toString(10);
			default:
				return "[unknown]";
		}
	}

	@Override
	public final void setValueAt(Object value, int rowIndex, int columnIndex) {
		Column column = visibleColumns.get(columnIndex);
		Bit[] newRowValue;

		if (this.editable && column.isEditable())
			try {
				switch (column) {
					case BINARY:
						newRowValue = BitConverter.integerToBits(bitsPerRow, new BigInteger((String) value, 2));
						break;
					case BIT:
						newRowValue = BitConverter.stringToBits((String) value);
						break;
					case OCTAL:
						newRowValue = BitConverter.integerToBits(bitsPerRow, new BigInteger((String) value, 8));
						break;
					case HEXA:
						newRowValue = BitConverter.integerToBits(bitsPerRow, new BigInteger((String) value, 16));
						break;
					case UNSIGNED:
					case SIGNED:
						newRowValue = BitConverter.integerToBits(bitsPerRow, new BigInteger((String) value, 10));
						break;
					default:
						newRowValue = new Bit[0];
				}

				if (newRowValue.length == bitsPerRow) {
					for (int i = 0; i < bitsPerRow; ++i)
						this.setable.setSetableBit(rowIndex * bitsPerRow + i, newRowValue[i]);
					notifyListeners(new TableModelEvent(this, rowIndex));
				}
			} catch (IllegalArgumentException e) {
				// Do nothing
			}
	}

	@Override
	public final void addTableModelListener(TableModelListener listener) {
		listeners.add(listener);
	}

	@Override
	public final void removeTableModelListener(TableModelListener listener) {
		listeners.remove(listener);
	}

	private final void notifyListeners(TableModelEvent evt) {
		for (TableModelListener listener : listeners)
			listener.tableChanged(evt);
	}
}
