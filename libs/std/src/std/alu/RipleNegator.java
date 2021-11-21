
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
import std.gate.NOT;
import core.build.ComponentCollection;
import core.build.Flavor;
import core.exception.DeserializingException;
import core.misc.serial.DeserializingStream;
import java.io.IOException;
import java.util.Map;

/**
 * @author lars
 */
public final class RipleNegator extends Composite
{
	public RipleNegator(ComponentCollection parent,String name)
	{
		super(parent,name);
	}

	public final RipleNegator setAll(Signal in, Signal out)
	{
		return (RipleNegator)useAndSet(Flavor.DEFAULT, new String[] {"in", "out"}, in, out);
	}

	public final RipleNegator setAll(Signal in, Signal carryIn, Signal out, Signal carryOut)
	{
		return (RipleNegator)useAndSet("carry", new String[] {"in", "carryIn", "out", "carryOut"}, in, carryIn, out, carryOut);
	}

	@Override public final Map<String, Flavor> getFlavors()
	{
		return flavors;
	}

	private static final Map<String, Flavor> flavors = Flavor.getMap(
		new Flavor(
			new Flavor.Buildable<RipleNegator>()
			{
				@Override public void build(RipleNegator me, Map<String, Integer> variables)
				{
					me.build(me.getSignal("in"), new Signal(Bit.H) /*cIn=1*/, me.getSignal("out"), new Signal(1) /*dummy*/);
				}
			},
			"in[n]",
			"out[n]"
		),
		new Flavor(
			"carry",
			new Flavor.Buildable<RipleNegator>()
			{
				@Override public void build(RipleNegator me, Map<String, Integer> variables)
				{
					me.build(me.getSignal("in"), me.getSignal("carryIn"), me.getSignal("out"), me.getSignal("carryOut"));
				}
			},
			"in[n], carryIn[1]",
			"out[n], carryOut[1]"
		)
	);

	private RipleNegator(DeserializingStream in) throws IOException, DeserializingException, InstantiationException
	{
		super(in);
	}

	private final void build(Signal in, Signal carryIn, Signal out, Signal carryOut)
	{
		Signal negated = new Signal(in.size());
		Signal carryInput = carryIn;

		new NOT(this,"inv").setAll(in, negated);

		for(int i = 0; i < in.size(); ++i)
		{
			Signal carryOutput =
					(i == (in.size() - 1)) ? // at the end?
						carryOut :           // yes, that's the result
						new Signal(1);       // no, we need an additional signal

			new HalfAdder(this,"add:" + i).setAll(negated.get(i), carryInput, out.get(i), carryOutput);
			carryInput = carryOutput;
		}
	}
	
}
