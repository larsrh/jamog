
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

package std.alu;

import core.exception.InstantiationException;
import core.signal.Bit;
import core.signal.Signal;
import core.build.Composite;
import core.build.ComponentCollection;
import core.build.Flavor;
import core.exception.DeserializingException;
import core.misc.serial.DeserializingStream;
import java.io.IOException;
import java.util.Map;

/**
 * @author lars
 */
public final class RipleIncrementer extends Composite
{
	public RipleIncrementer(ComponentCollection parent,String name)
	{
		super(parent,name);
	}

	public final RipleIncrementer setAll(Signal in, Signal out)
	{
		return (RipleIncrementer)useAndSet(Flavor.DEFAULT, new String[] {"in", "out"}, in, out);
	}

	@Override public final Map<String, Flavor> getFlavors()
	{
		return flavors;
	}

	private static final Map<String, Flavor> flavors = Flavor.getMap(
		new Flavor(
			new Flavor.Buildable<RipleIncrementer>()
			{
				@Override public void build(RipleIncrementer me, Map<String, Integer> variables)
				{
					Signal carry_in = new Signal(Bit.H);
					Signal in = me.getSignal("in");
					Signal out = me.getSignal("out");

					for (int i = 0; i < in.size(); ++i)
					{
						Signal carry_out = new Signal(1);

						new HalfAdder(me,"add:" + i).setAll(in.get(i), carry_in, out.get(i), carry_out);
						carry_in = carry_out;
					}
				}
			},
			"in[n]",
			"out[n]"
		)
	);

	private RipleIncrementer(DeserializingStream in) throws IOException, DeserializingException, InstantiationException
	{
		super(in);
	}
}
