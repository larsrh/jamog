
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

import core.exception.DeserializingException;
import core.exception.InstantiationException;
import core.exception.SerializingException;
import core.misc.serial.Serializable;
import core.misc.serial.DeserializingStream;
import core.misc.serial.SerializingStream;
import core.signal.Bit;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 *
 * @author lars
 */
public final class DefaultGroupSetable implements GroupSetable, Serializable
{
	private final LinkedHashMap<String, Setable> group_map;

	public DefaultGroupSetable(Map<String,Setable> map)
	{
		group_map = new LinkedHashMap<String, Setable>(map);
	}

	@SuppressWarnings("unchecked")
	public DefaultGroupSetable(DeserializingStream in) throws IOException, DeserializingException, InstantiationException
	{
		group_map = in.readObject(LinkedHashMap.class, String.class);
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

	@Override public void serialize(SerializingStream out) throws IOException, SerializingException
	{
		out.writeObject(group_map, false, false);
	}
}
