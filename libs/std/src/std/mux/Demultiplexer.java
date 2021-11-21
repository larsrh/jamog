
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
import std.gate.*;
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
	name		= "Demultiplexer",
	description	= "A simple demultiplexer."
)
public final class Demultiplexer extends Composite
{
	public Demultiplexer(ComponentCollection parent,String name)
	{
		super(parent,name);
	}

	public final Demultiplexer setAll(Signal in, Signal sel, Signal out)
	{
		return (Demultiplexer)useAndSet(Flavor.DEFAULT, new String[] {"in", "sel", "out"}, in, sel, out);
	}

	public final Demultiplexer setAll(Signal in, Signal sel, Signal[] out)
	{
		return (Demultiplexer)useAndSet("multi", new String[] {"in", "sel", "out"}, in, sel, out);
	}

	@Override public Map<String, Flavor> getFlavors()
	{
		return flavors;
	}

	private static final Map<String, Flavor> flavors = Flavor.getMap(
		new Flavor(
			new Flavor.Buildable<Demultiplexer>()
			{
				@Override public void build(Demultiplexer me, Map<String, Integer> variables)
				{
					Signal out = me.getSignal("out");
					Signal[] aout = new Signal[out.size()];
					for(int i = 0; i < aout.length; ++i)
						aout[i] = out.get(i);

					me.build(me.getSignal("in"), me.getSignal("sel"), aout);
				}
			},
			"in[1], sel[n]",
			"out[n]"
		),
		new Flavor(
			"multi",
			new Flavor.Buildable<Demultiplexer>()
			{
				@Override public void build(Demultiplexer me, Map<String, Integer> variables)
				{
					me.build(me.getSignal("in"), me.getSignal("sel"), (Signal[])me.get("out"));
				}
			},
			"in[m], sel[n]",
			"out[n][m]"
		)
	);

	private Demultiplexer(DeserializingStream in) throws IOException, DeserializingException, InstantiationException
	{
		super(in);
	}

	private final void build(Signal in, Signal sel, Signal[] out)
	{
		for(int i = 0; i < in.size(); ++i)
		{
			Signal b[] = new Signal[out.length];
			for(int j = 0; j < b.length; ++j)
				b[j] = out[j].get(i);

			for(int j = 0; j < b.length; ++j)
				new AND(this, "and:" + i + ":" + j).setAll(new Signal(sel.get(j), in.get(i)), b[j]);
		}
	}
}
