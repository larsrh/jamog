
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

package std.mux;

import core.exception.DeserializingException;
import core.exception.InstantiationException;
import core.signal.Signal;
import core.build.Composite;
import java.io.IOException;
import std.convert.*;
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
	name		= "Binary Multiplexer",
	description	= "A binary multiplexer."
)
public final class BinaryMultiplexer extends Composite
{
	public BinaryMultiplexer(ComponentCollection parent,String name)
	{
		super(parent,name);
	}

	public final BinaryMultiplexer setAll(Signal in, Signal sel, Signal out)
	{
		return (BinaryMultiplexer)useAndSet(Flavor.DEFAULT, new String[] {"in", "sel", "out"}, in, sel, out);
	}

	public final BinaryMultiplexer setAll(Signal[] in, Signal sel, Signal out)
	{
		return (BinaryMultiplexer)useAndSet("multi", new String[] {"in", "sel", "out"}, in, sel, out);
	}

	@Override public Map<String, Flavor> getFlavors()
	{
		return flavors;
	}

	private static final Map<String, Flavor> flavors = Flavor.getMap(
		new Flavor(
			new Flavor.Buildable<BinaryMultiplexer>()
			{
				@Override public void build(BinaryMultiplexer me, Map<String, Integer> variables)
				{
					Signal in = me.getSignal("in");
					Signal[] ain = new Signal[in.size()];
					for(int i = 0; i < ain.length; ++i)
						ain[i] = in.get(i);

					me.build(ain, me.getSignal("sel"), me.getSignal("out"));
				}
			},
			"in[n], sel[m]",
			"out[1]",
			"(#n - 1)/(2^(#m - 1)) == 1"
		),
		new Flavor(
			"multi",
			new Flavor.Buildable<BinaryMultiplexer>()
			{
				@Override public void build(BinaryMultiplexer me, Map<String, Integer> variables)
				{
					me.build((Signal[])me.get("in"), me.getSignal("sel"), me.getSignal("out"));
				}
			},
			"in[n][k],sel[m]",
			"out[k]",
			"(#n - 1)/(2^(#m - 1)) == 1"
		)
	);

	private BinaryMultiplexer(DeserializingStream in) throws IOException, DeserializingException, InstantiationException
	{
		super(in);
	}

	private final void build(Signal[] in, Signal sel, Signal out)
	{
		Signal bsel = new Signal(in.length);
		new BinaryDecoder(this, "decoder").setAll(sel, bsel);
		new Multiplexer(this, "mux").setAll(in, bsel, out);
	}
}
