
package gui.torben;

import java.util.Map.Entry;
import core.misc.setable.GroupSetable;
import core.misc.setable.Setable;
import java.util.ArrayList;
import java.util.List;

public final class GroupSetableForm
{
	public static final TreeDataForm create(GroupSetable setable)
	{
		List<TreeDataForm.Pair> root = new ArrayList<TreeDataForm.Pair>();
		addGroupSetable(root, setable);
		return new TreeDataForm(root);
	}

	private static final void addGroupSetable(List<TreeDataForm.Pair> list, GroupSetable setable)
	{
		for(Entry<String, ? extends Setable> e : setable.getSetableGroups().entrySet())
		{
			if(e.getValue() instanceof GroupSetable)
			{
				List<TreeDataForm.Pair> child_list = new ArrayList<TreeDataForm.Pair>();
				addGroupSetable(child_list, (GroupSetable)e.getValue());
				list.add(new TreeDataForm.Pair(e.getKey(), child_list));
			}
			else
				list.add(new TreeDataForm.Pair(e.getKey(), SetableForm.create(e.getValue())));
		}
	}
}
