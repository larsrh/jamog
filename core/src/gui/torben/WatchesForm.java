
package gui.torben;

import core.build.Component;
import core.build.Composite;
import core.signal.Signal;
import core.sim.Calculator;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import javax.swing.AbstractAction;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

public class WatchesForm extends JSplitPane implements TreeModel
{
	private final class CellRenderer implements TreeCellRenderer
	{
		@Override public java.awt.Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus)
		{
			return new JLabel(((Component)value).getName());
		}
	}

	final Composite root;
	final JPopupMenu component_menu;
	final JPopupMenu watch_menu;
	Component current_component;
	TreeDataForm.Pair current_watch;

	TreeDataForm watches;
	JScrollPane scroll_pane;

	public WatchesForm(Composite root)
	{
		super(VERTICAL_SPLIT, true);
		//super(new BorderLayout());

		this.root = root;

		final List<TreeDataForm.Pair> watch_list = new ArrayList<TreeDataForm.Pair>();
		watches = new TreeDataForm(watch_list);
		setTopComponent(scroll_pane = new JScrollPane(watches));
		//add(scroll_pane = new JScrollPane(watches), BorderLayout.NORTH);

		final JTree component_tree = new JTree(this);
		component_tree.setCellRenderer(new CellRenderer());
		component_tree.setEditable(false);
		component_tree.setRootVisible(false);
		component_tree.setShowsRootHandles(true);
		component_tree.putClientProperty("JTree.lineStyle", "None");
		component_tree.setToggleClickCount(1);
		setBottomComponent(new JScrollPane(component_tree));
		//add(new JScrollPane(component_tree), BorderLayout.SOUTH);

		watch_menu = new JPopupMenu();
		watch_menu.add(new JMenuItem(new AbstractAction("Remove from watches") {

			public void actionPerformed(ActionEvent e)
			{
				watch_list.remove(current_watch);
			}
		}));

		component_menu = new JPopupMenu();
		component_menu.add(new JMenuItem(new AbstractAction("Add to watches") {

			public void actionPerformed(ActionEvent e)
			{
				List<TreeDataForm.Pair> signals = new ArrayList<TreeDataForm.Pair>();
				for(Entry<String, Object> en : current_component.getParameters().entrySet())
				{
					if(en.getValue() instanceof Object[])
					{
						List<TreeDataForm.Pair> childs = new ArrayList<TreeDataForm.Pair>();
						addSignals(childs, "", (Object[])en.getValue());
						if(childs.size() > 0)
							signals.add(new TreeDataForm.Pair(en.getKey(), childs));
					}
					else if(en.getValue() instanceof Signal)
						signals.add(new TreeDataForm.Pair(en.getKey(), SignalWatchForm.create((Signal)en.getValue())));
				}

				watch_list.add(new TreeDataForm.Pair(current_component.getName(), signals));

				watches = new TreeDataForm(watch_list);
				scroll_pane.setViewportView(watches);
			}
		}));

		watches.addMouseListener(new MouseAdapter() {

			@Override public void mouseClicked(MouseEvent e)
			{
				if(e.getButton() == MouseEvent.BUTTON3)
				{
					Object o = component_tree.getClosestPathForLocation(e.getX(), e.getY()).getLastPathComponent();
					if(o instanceof TreeDataForm.Pair)
					{
						current_watch = (TreeDataForm.Pair)o;
						if(watch_list.contains(current_watch))
							watch_menu.show(watches, e.getX(), e.getY());
					}
				}
			}
		});

		component_tree.addMouseListener(new MouseAdapter() {

			@Override public void mouseClicked(MouseEvent e)
			{
				if(e.getButton() == MouseEvent.BUTTON3)
				{
					current_component = (Component)component_tree.getClosestPathForLocation(e.getX(), e.getY()).getLastPathComponent();
					component_menu.show(component_tree, e.getX(), e.getY());
				}
			}
		});
	}

	public Object getRoot()
	{
		return root;
	}

	public Object getChild(Object parent, int index)
	{
		int j = 0;
		for(Iterator<Component> i = ((Composite)parent).getComponents().values().iterator(); i.hasNext(); i.next(), ++j)
			if(j == index)
				return i.next();
		return null;
	}

	public int getChildCount(Object parent)
	{
		return ((Composite)parent).getComponents().size();
	}

	public boolean isLeaf(Object node)
	{
		return node instanceof Calculator;
	}

	public void valueForPathChanged(TreePath path, Object newValue)
	{
	}

	public int getIndexOfChild(Object parent, Object child)
	{
		int j = 0;
		for(Iterator<Component> i = ((Composite)parent).getComponents().values().iterator(); i.hasNext(); ++j)
			if(i.next() == child)
				return j;
		return -1;
	}

	public void addTreeModelListener(TreeModelListener l)
	{
	}

	public void removeTreeModelListener(TreeModelListener l)
	{
	}

	private final void addSignals(List<TreeDataForm.Pair> list, String name, Object[] array)
	{
		int i = 0;
		for(Object o : array)
		{
			if(o instanceof Object[])
			{
				List<TreeDataForm.Pair> childs = new ArrayList<TreeDataForm.Pair>();
				addSignals(childs, name + ":" + i, (Object[])o);
				if(childs.size() > 0)
					list.add(new TreeDataForm.Pair(name + ":" + i++, childs));
			}
			else if(o instanceof Signal)
				list.add(new TreeDataForm.Pair(name + ":" + i++, SignalWatchForm.create((Signal)o)));
		}
	}
}
