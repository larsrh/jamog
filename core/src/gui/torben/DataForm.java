
package gui.torben;

import java.math.BigInteger;
import java.util.*;

import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;

import core.signal.Bit;

import static core.misc.BitConverter.*;

/**
 * @author torben
 */
public abstract class DataForm extends JTable implements TableModel
{
	protected abstract int bitCount();
	protected abstract Bit getBit(int i);
	protected abstract void setBit(int i, Bit b);

	private static enum ColumnType
	{
		BIT("Bits"), BINARY("Binary"), OCTAL("Octal"), HEXA("Hexadecimal"), UNSIGNED("Unsigned decimal"), SIGNED("Signed decimal"), FLOAT("Floating point");

		private final String name;

		private ColumnType(String name)
		{
			this.name = name;
		}
	}

	private final boolean editable;

	private int bpr;
	private int bpo;

	private final JPopupMenu menu;
	private int col;

	private final Set<TableModelListener> listeners = new HashSet<TableModelListener>();
	private final ArrayList<ColumnType> cols = new ArrayList<ColumnType>();

	protected DataForm(boolean editable)
	{
		this.editable = editable;

		// TODO: do something better
		if(bitCount() == 32)
			bpo = bpr = 32;
		else if(bitCount() % 8 == 0)
			bpo = bpr = 8;
		else
			bpo = bpr = 1;

		JMenu add_column = new JMenu("Add column");
		add_column.add(new JMenuItem(new AbstractAction("Add bit column") { @Override public void actionPerformed(ActionEvent e) { addColumn(ColumnType.BIT); } } ));
		add_column.add(new JMenuItem(new AbstractAction("Add binary column") { @Override public void actionPerformed(ActionEvent e) { addColumn(ColumnType.BINARY); } } ));
		add_column.add(new JMenuItem(new AbstractAction("Add octal column") { @Override public void actionPerformed(ActionEvent e) { addColumn(ColumnType.OCTAL); } } ));
		add_column.add(new JMenuItem(new AbstractAction("Add hexadecimal column") { @Override public void actionPerformed(ActionEvent e) { addColumn(ColumnType.HEXA); } } ));
		add_column.add(new JMenuItem(new AbstractAction("Add unsigned decimal column") { @Override public void actionPerformed(ActionEvent e) { addColumn(ColumnType.UNSIGNED); } } ));
		add_column.add(new JMenuItem(new AbstractAction("Add signed decimal column") { @Override public void actionPerformed(ActionEvent e) { addColumn(ColumnType.SIGNED); } } ));
		add_column.add(new JMenuItem(new AbstractAction("Add floating point column") { @Override public void actionPerformed(ActionEvent e) { addColumn(ColumnType.FLOAT); } } ));

		JMenu row = new JMenu("Bits per row");
		for(int i = 1; ; i *= 2)
			if(bitCount() >= i && bitCount() % i == 0)
			{
				final int j = i;
				row.add(new JMenuItem(new AbstractAction(i + " bits") { @Override public void actionPerformed(ActionEvent e) { bpr = j; notifyListeners(new TableModelEvent(DataForm.this, TableModelEvent.HEADER_ROW)); } } ));
			}
			else
				break;

		JMenu offset = new JMenu("Bits per offset");
		for(int i = 1; ; i *= 2)
			if(bitCount() >= i && bitCount() % i == 0)
			{
				final int j = i;
				offset.add(new JMenuItem(new AbstractAction(i + " bits") { @Override public void actionPerformed(ActionEvent e) { bpo = j; notifyListeners(new TableModelEvent(DataForm.this, TableModelEvent.HEADER_ROW)); } } ));
			}
			else
				break;

		menu = new JPopupMenu();
		menu.add(add_column);
		menu.addSeparator();
		menu.add(row);
		menu.add(offset);
		menu.addSeparator();
		menu.add(new JMenuItem(new AbstractAction("Remove column") { public void actionPerformed(ActionEvent e) { removeColumn(col); } } ));

		setModel(this);
		addColumn(ColumnType.SIGNED);
		addColumn(ColumnType.FLOAT);

		getTableHeader().addMouseListener(new MouseAdapter() { @Override public void mouseClicked(MouseEvent e) { if(e.getButton() == MouseEvent.BUTTON3 && (col = getTableHeader().columnAtPoint(e.getPoint())) != -1) { menu.show(DataForm.this, e.getX(), e.getY()); } } } );
	}

	private final void addColumn(ColumnType type)
	{
		cols.add(type);
		notifyListeners(new TableModelEvent(this, TableModelEvent.HEADER_ROW));
	}

	private final void removeColumn(int col)
	{
		cols.remove(col - 1);
		notifyListeners(new TableModelEvent(this, TableModelEvent.HEADER_ROW));
	}

	@Override public final int getRowCount()
	{
		return bitCount() / bpr;
	}

	@Override public final int getColumnCount()
	{
		return cols.size() + 1;
	}

	@Override public final String getColumnName(int columnIndex)
	{
		return columnIndex != 0 ? cols.get(columnIndex - 1).name : "Offset";
	}

	@Override public final Class<?> getColumnClass(int columnIndex)
	{
		return String.class;
	}

	@Override public final boolean isCellEditable(int rowIndex, int columnIndex)
	{
		return columnIndex != 0 && editable;
	}

	@Override public final Object getValueAt(int rowIndex, int columnIndex)
	{
		if(columnIndex != 0)
		{
			Bit[] bits = new Bit[bpr];
			for(int i = 0; i < bpr; ++i)
				bits[i] = getBit(rowIndex * bpr + i);

			switch(cols.get(columnIndex - 1))
			{
			case BIT:
				return bitsToString(bits);
			case BINARY:
				return bitsToInteger(false, bits).toString(2);
			case OCTAL:
				return bitsToInteger(false, bits).toString(8);
			case HEXA:
				return bitsToInteger(false, bits).toString(16).toUpperCase();
			case UNSIGNED:
				return bitsToInteger(false, bits).toString(10);
			case SIGNED:
				return bitsToInteger(true, bits).toString(10);
			case FLOAT:
				return Float.toString(Float.intBitsToFloat((int)bitsToLong(bits)));
			}
		}
		else if((int)Math.ceil(Math.log(bitCount() / bpr) / Math.log(16.0)) == 0)
			return String.format("%01X", rowIndex * bpr / bpo);
		else
			return String.format("%0" + (int)Math.ceil(Math.log(bitCount() / bpr) / Math.log(16.0)) + "X", rowIndex * bpr / bpo);

		return null;
	}

	@Override public final void setValueAt(Object aValue, int rowIndex, int columnIndex)
	{
		if(editable)
		{
			Bit[] bits;

			try
			{
				switch(cols.get(columnIndex - 1))
				{
				case BIT:
					bits = stringToBits((String)aValue);
					break;
				case BINARY:
					bits = integerToBits(bpr, new BigInteger((String)aValue, 2));
					break;
				case OCTAL:
					bits = integerToBits(bpr, new BigInteger((String)aValue, 8));
					break;
				case HEXA:
					bits = integerToBits(bpr, new BigInteger((String)aValue, 16));
					break;
				case UNSIGNED:
				case SIGNED:
					bits = integerToBits(bpr, new BigInteger((String)aValue, 10));
					break;
				default:
					bits = null;
				}

				if(bits.length == bpr)
					for(int i = 0; i < bpr; ++i)
						setBit(rowIndex * bpr + i, bits[i]);

				notifyListeners(new TableModelEvent(this, rowIndex));
			}
			catch(IllegalArgumentException e)
			{

			}
		}
	}

	@Override public final void addTableModelListener(TableModelListener l)
	{
		listeners.add(l);
	}

	@Override public final void removeTableModelListener(TableModelListener l)
	{
		listeners.remove(l);
	}

	private final void notifyListeners(TableModelEvent e)
	{
		for(TableModelListener listener : listeners)
			listener.tableChanged(e);
	}
}
