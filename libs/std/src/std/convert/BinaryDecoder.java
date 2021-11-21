
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

package std.convert;

import core.exception.DeserializingException;
import core.exception.InstantiationException;
import core.signal.Signal;
import core.build.Composite;
import java.io.IOException;
import std.gate.*;
import std.logic.*;
import core.build.ComponentCollection;
import core.misc.module.Module.Description;
import core.build.Flavor;
import core.misc.serial.DeserializingStream;
import java.util.Map;

/**
 * @author torben
 */
@Description
(
	name		= "Binary Decoder",
	description	= "A simple binary decoder."
)
public final class BinaryDecoder extends Composite
{
	public BinaryDecoder(ComponentCollection parent,String name)
	{
		super(parent,name);
	}

	public final BinaryDecoder setAll(Signal in, Signal out)
	{
		return (BinaryDecoder)useAndSet(Flavor.DEFAULT, new String[] {"in", "out"}, in, out);
	}

	@Override public Map<String, Flavor> getFlavors()
	{
		return flavors;
	}

	private static final Map<String, Flavor> flavors = Flavor.getMap(
		new Flavor(
			new Flavor.Buildable<BinaryDecoder>()
			{
				@Override public void build(BinaryDecoder me, Map<String, Integer> variables)
				{
					Signal in = me.getSignal("in");
					Signal out = me.getSignal("out");

					if(in.size() != 1)
					{
						for(int i = 0; i < out.size(); ++i)
						{
							Signal s[] = new Signal[in.size()];
							for(int j = 0; j < s.length; ++j)
							{
								if(((i >> j) & 1) == 0)
									new NOT(me, "not:" + i + ":" + j).setAll(in.get(j), s[j] = new Signal(1));
								else
									s[j] = in.get(j);
							}

							new AND(me, "and:" + i).setAll(new Signal(s), out.get(i));
						}
					}
					else
					{
						new NOT(me, "not").setAll(in, out.get(0));
						new Forward(me, "fw").setAll(in, out.get(1));
					}
				}
			},
			"in[m]",
			"out[n]",
			"(#n - 1) / (2^(#m - 1)) == 1"
		)
	);

	private BinaryDecoder(DeserializingStream in) throws IOException, DeserializingException, InstantiationException
	{
		super(in);
	}
}
