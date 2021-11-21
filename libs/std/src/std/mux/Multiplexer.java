
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

import core.exception.InstantiationException;
import core.signal.Signal;
import core.build.Composite;
import std.gate.*;
import core.build.ComponentCollection;
import core.build.Flavor;
import core.exception.DeserializingException;
import core.misc.module.Module.Description;
import core.misc.serial.DeserializingStream;
import java.util.Map;
import java.io.IOException;

/**
 * @author torben
 */
@Description
(
	name		= "Multiplexer",
	description	= "A simple multiplexer."
)
public final class Multiplexer extends Composite
{
	public Multiplexer(ComponentCollection parent,String name)
	{
		super(parent,name);
	}

	public final Multiplexer setAll(Signal in, Signal sel, Signal out)
	{
		return (Multiplexer)useAndSet(Flavor.DEFAULT, new String[] {"in", "sel", "out"}, in, sel, out);
	}

	public final Multiplexer setAll(Signal[] in, Signal sel, Signal out)
	{
		return (Multiplexer)useAndSet("multi", new String[] {"in", "sel", "out"}, in, sel, out);
	}

	@Override public Map<String, Flavor> getFlavors()
	{
		return flavors;
	}

	private static final Map<String, Flavor> flavors = Flavor.getMap(
		new Flavor(
			new Flavor.Buildable<Multiplexer>()
			{
				@Override public void build(Multiplexer me, Map<String, Integer> variables)
				{
					Signal in = me.getSignal("in");
					Signal[] ain = new Signal[in.size()];
					for(int i = 0; i < ain.length; ++i)
						ain[i] = in.get(i);

					me.build(ain, me.getSignal("sel"), me.getSignal("out"));
				}
			},
			"in[n], sel[n]",
			"out[1]",
			"#n >= 2"
		),
		new Flavor(
			"multi",
			new Flavor.Buildable<Multiplexer>()
			{
				@Override public void build(Multiplexer me, Map<String, Integer> variables)
				{
					me.build((Signal[])me.get("in"), me.getSignal("sel"), me.getSignal("out"));
				}
			},
			"in[n][m], sel[n]",
			"out[m]",
			"#n >= 2"
		)
	);

	private Multiplexer(DeserializingStream in) throws IOException, DeserializingException, InstantiationException
	{
		super(in);
	}

	private final void build(Signal[] in, Signal sel, Signal out)
	{
		for(int i = 0; i < out.size(); ++i)
		{
			Signal b[] = new Signal[in.length];
			for(int j = 0; j < b.length; ++j)
				b[j] = in[j].get(i);

			Signal s[] = new Signal[b.length];
			for(int j = 0; j < b.length; ++j)
				new AND(this, "and:" + i + ":" + j).setAll(new Signal(sel.get(j), b[j]), s[j] = new Signal(1));

			new OR(this, "or:" + i).setAll(new Signal(s), out.get(i));
		}
	}
}
