
package gui.torben;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.util.EventObject;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.event.CellEditorListener;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeCellEditor;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import java.util.List;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

public class TreeDataForm extends JTree implements TreeModel, TableModelListener
{
	public static final class Pair
	{
		public final String name;
		public final Object obj;

		public Pair(String name, Object obj)
		{
			this.name = name;
			this.obj = obj;
		}

		public final boolean isLeaf()
		{
			return obj instanceof DataForm;
		}

		public final int getChildCount()
		{
			return ((List)obj).size();
		}

		public final Pair getChild(int index)
		{
			return (Pair)((List)obj).get(index);
		}

		public final int getIndex(Pair child)
		{
			return ((List)obj).indexOf(child);
		}
	}

	private final class CellRenderer implements TreeCellRenderer
	{
		@Override public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus)
		{
			if(value instanceof DataForm)
			{
				if(cells.containsKey((DataForm)value))
					return cells.get((DataForm)value);
				JScrollPane p = new JScrollPane((DataForm)value);
				p.setMinimumSize(new Dimension(300, 34));
				p.setMaximumSize(new Dimension(600, 273));
				setPrefSize(p);
				cells.put((DataForm)value, p);
				((DataForm)value).addTableModelListener(TreeDataForm.this);
				return p;
			}
			else if(value instanceof Pair)
				return new JLabel(((Pair)value).name);
			else
				return new JLabel("ROOT");
		}
	}

	private final class CellEditor implements TreeCellEditor
	{
		@Override public Component getTreeCellEditorComponent(JTree tree, Object value, boolean isSelected, boolean expanded, boolean leaf, int row)
		{
			if(value instanceof DataForm)
			{
				if(cells.containsKey((DataForm)value))
					return cells.get((DataForm)value);
				JScrollPane p = new JScrollPane((DataForm)value);
				p.setMinimumSize(new Dimension(300, 34));
				p.setMaximumSize(new Dimension(600, 273));
				setPrefSize(p);
				cells.put((DataForm)value, p);
				((DataForm)value).addTableModelListener(TreeDataForm.this);
				return p;
			}
			else if(value instanceof Pair)
				return new JLabel(((Pair)value).name);
			else
				return new JLabel("ROOT");
		}

		@Override public Object getCellEditorValue()
		{
			return null;
		}

		@Override public boolean isCellEditable(EventObject anEvent)
		{
			if(anEvent instanceof MouseEvent)
			{
				Object o = getPathForLocation(((MouseEvent)anEvent).getX(), ((MouseEvent)anEvent).getY()).getLastPathComponent();
				return o instanceof DataForm;
			}
			else
				return false;
		}

		@Override public boolean shouldSelectCell(EventObject anEvent)
		{
			return true;
		}

		@Override public boolean stopCellEditing()
		{
			return true;
		}

		@Override public void cancelCellEditing()
		{
		}

		@Override public void addCellEditorListener(CellEditorListener l)
		{
		}

		@Override public void removeCellEditorListener(CellEditorListener l)
		{
		}
	}

	private final List<Pair> root;
	private final Map<DataForm, JScrollPane> cells;

	protected TreeDataForm(List<Pair> root)
	{
		this.root = root;
		cells = new HashMap<DataForm, JScrollPane>();

		setModel(this);
		setCellRenderer(new CellRenderer());
		setCellEditor(new CellEditor());
		setEditable(true);

		setRootVisible(true);
		setShowsRootHandles(true);
		putClientProperty("JTree.lineStyle", "None");
		setToggleClickCount(1);
	}

	@Override public void tableChanged(TableModelEvent e)
	{
		DataForm form = (DataForm)e.getSource();
		setPrefSize(cells.get(form));
		repaint();
	}

	@Override public Object getRoot()
	{
		return root;
	}

	@Override public Object getChild(Object parent, int index)
	{
		if(parent instanceof List)
			return ((List)parent).get(index);
		else if(parent instanceof Pair && !((Pair)parent).isLeaf())
			return ((Pair)parent).getChild(index);
		else
			return ((Pair)parent).obj;
	}

	@Override public int getChildCount(Object parent)
	{
		if(parent instanceof List)
			return ((List)parent).size();
		else if(parent instanceof Pair && !((Pair)parent).isLeaf())
			return ((Pair)parent).getChildCount();
		else
			return 1;
	}

	@Override public boolean isLeaf(Object node)
	{
		return node instanceof DataForm;
	}

	@Override public void valueForPathChanged(TreePath path, Object newValue)
	{
	}

	@Override public int getIndexOfChild(Object parent, Object child)
	{
		if(parent instanceof List)
			return ((List)parent).indexOf(child);
		else if(parent instanceof Pair && !((Pair)parent).isLeaf())
			return ((Pair)parent).getIndex((Pair)child);
		else
			return 0;
	}

	@Override public void addTreeModelListener(TreeModelListener l)
	{
	}

	@Override public void removeTreeModelListener(TreeModelListener l)
	{
	}

	private final void setPrefSize(JScrollPane p)
	{
		Dimension d = p.getViewport().getViewSize();
		d.setSize(d.getWidth() < 300 ? 300 : d.getWidth() > 600 ? 600 : d.getWidth(), d.getHeight() > 256 ? 274 : d.getHeight() + 18);
		p.setPreferredSize(d);
	}
}
