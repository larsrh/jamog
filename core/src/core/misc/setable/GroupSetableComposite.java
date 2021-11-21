
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

package core.misc.setable;

import core.build.ComponentCollection;
import core.exception.DeserializingException;
import core.exception.InstantiationException;
import core.exception.SerializingException;
import core.signal.Bit;
import core.build.Composite;
import core.misc.serial.DeserializingStream;
import core.misc.serial.SerializingStream;
import java.io.IOException;
import java.util.*;

/**
 *
 * @author lars
 */
public abstract class GroupSetableComposite extends Composite implements GroupSetable
{
	private final LinkedHashMap<String, Setable> group_map;

	protected GroupSetableComposite(ComponentCollection parent,String name)
	{
		super(parent,name);
		group_map = new LinkedHashMap<String, Setable>();
	}

	@SuppressWarnings("unchecked")
	public GroupSetableComposite(DeserializingStream in) throws IOException, DeserializingException, InstantiationException
	{
		super(in);

		group_map = in.readObject(LinkedHashMap.class, String.class);
	}

	protected final Setable addGroup(String name,Setable setable)
	{
		if (!isInConstructMode())
			throw new IllegalStateException("In class "+getClass()+": cannot add a setable group while not in construct mode");
		group_map.put(name, setable);
		return setable;
	}

	@Override public final Map<String, ? extends Setable> getSetableGroups()
	{
		return Collections.unmodifiableMap(group_map);
	}

	@Override public final Setable getSetableGroup(String name)
	{
		return group_map.get(name);
	}

	@Override public final int getSetableCount()
	{
		int count = 0;

		for(Setable s : group_map.values())
			count += s.getSetableCount();

		return count;
	}

	@Override public final Bit getSetableBit(int i)
	{
		int count = 0;

		for(Setable s : group_map.values())
		{
			if(count + s.getSetableCount() > i)
				return s.getSetableBit(i - count);

			count += s.getSetableCount();
		}

		return null;
	}

	@Override public final void setSetableBit(int i, Bit v)
	{
		int count = 0;

		for(Setable s : group_map.values())
		{
			if(count + s.getSetableCount() > i)
			{
				s.setSetableBit(i - count, v);
				return;
			}

			count += s.getSetableCount();
		}
	}

	@Override protected final void cleanup()
	{
		group_map.clear();
		super.cleanup();
	}

	@Override public void serialize(SerializingStream out) throws IOException, SerializingException
	{
		super.serialize(out);

		out.writeObject(group_map, false, false);
	}
}
