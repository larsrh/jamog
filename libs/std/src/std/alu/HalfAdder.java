
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
import core.signal.Signal;
import core.build.Composite;
import std.gate.*;
import core.build.ComponentCollection;
import core.build.Flavor;
import core.exception.DeserializingException;
import core.misc.serial.DeserializingStream;
import java.io.IOException;
import java.util.Map;

/**
 * @author lars
 */
public class HalfAdder extends Composite
{
	public HalfAdder(ComponentCollection parent, String name)
	{
		super(parent, name);
	}

	public final HalfAdder setAll(Signal x, Signal y, Signal sum, Signal carryOut)
	{
		return (HalfAdder)useAndSet(Flavor.DEFAULT, new String[] {"x", "y", "sum", "carryOut"}, x, y, sum, carryOut);
	}

	@Override public final Map<String, Flavor> getFlavors()
	{
		return flavors;
	}

	private static final Map<String, Flavor> flavors = Flavor.getMap(
		new Flavor(
			new Flavor.Buildable<HalfAdder>()
			{
				@Override public void build(HalfAdder me, Map<String, Integer> variables)
				{
					Signal input = new Signal(me.getSignal("x"), me.getSignal("y"));
					new XOR(me, "x,y->s").setAll(input, me.getSignal("sum"));
					new AND(me, "x,y->c").setAll(input, me.getSignal("carryOut"));
				}
			},
			"x[1], y[1]",
			"sum[1], carryOut[1]"
		)
	);

	private HalfAdder(DeserializingStream in) throws IOException, DeserializingException, InstantiationException
	{
		super(in);
	}
}
