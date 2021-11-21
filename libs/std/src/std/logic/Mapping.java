
/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *                                                                           *
 * Copyright 2009 Lars Hupel, Torben Maack, Sylvester Tremmel                *
 *                                                                           *
 * This file is part of the Jamog Standard Library.                          *
 *                                                                           *
 * The Jamog Standard Library is free software: you can redistribute         *
 * it and/or modify it under the terms of the GNU General Public License     *
 * as published by the Free Software Foundation; version 3.                  *
 *                                                                           *
 * The Jamog Standard Library is distributed in the hope that it will        *
 * be useful, but WITHOUT ANY WARRANTY; without even the implied warranty    *
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the           *
 * GNU General Public License for more details.                              *
 *                                                                           *
 * You should have received a copy of the GNU General Public License         *
 * along with the Jamog Standard Library. If not, see                        *
 * <http://www.gnu.org/licenses/>.                                           *
 *                                                                           *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

package std.logic;

import core.exception.DeserializingException;
import core.exception.InstantiationException;
import core.signal.Signal;
import core.build.Composite;
import java.io.IOException;
import java.util.Map;
import java.util.HashMap;
import std.mux.*;
import core.build.ComponentCollection;
import core.misc.module.Module.Description;
import core.build.Flavor;
import core.misc.serial.DeserializingStream;
import core.signal.Bit;

import static core.signal.Bit.*;

/**
 * @author torben
 */
@Description
(
	name		= "Mapping",
	description	= "A fixed mapping from input bits to output bits."
)
public class Mapping extends Composite
{
	public Mapping(ComponentCollection parent,String name)
	{
		super(parent,name);
	}

	public final Mapping setAll(Signal in, Signal out, Map<Bit[], Bit[]> map)
	{
		return (Mapping)useAndSet(Flavor.DEFAULT, new String[] {"in", "out", "map"}, in, out, map);
	}

	@Override public Map<String, Flavor> getFlavors()
	{
		return flavors;
	}

	private static final Map<String, Flavor> flavors = Flavor.getMap(
		new Flavor(
			new Flavor.Buildable<Mapping>()
			{
				@Override public void build(Mapping me, Map<String, Integer> variables)
				{
					Signal in = me.getSignal("in");
					Signal out = me.getSignal("out");

					@SuppressWarnings("unchecked")
					Map<Bit[], Bit[]> map = ((Map<Bit[], Bit[]>)me.get("map"));

					Map<Integer, Bit[]> dec_mapping = new HashMap<Integer, Bit[]>();
					int h = 0;
					for(Bit[] a : map.keySet())
					{
						int p = 0;
						for(int j = 0; j < a.length; ++j)
							p += (a[j] == H ? (1 << j) : 0);

						if(p > h)
							h = p;
						dec_mapping.put(p, map.get(a));
					}

					Signal[] value = new Signal[h + 1];
					for(int i = 0; i < value.length; ++i)
					{
						value[i] = new Signal(out.size());
						if(dec_mapping.containsKey(i))
							value[i].setBits(dec_mapping.get(i));
					}

					new BinaryMultiplexer(me, "mux").setAll(value, in.get(0, 32 - Integer.numberOfLeadingZeros(h)), out);
				}
			},
			"in[n]",
			"out[m]",
			"java.util.Map<Bit[],Bit[]> map",
			"[#](keys(map)) == #n && [#](values(map)) == #m"
		)
	);

	private Mapping(DeserializingStream in) throws IOException, DeserializingException, InstantiationException
	{
		super(in);
	}
}
