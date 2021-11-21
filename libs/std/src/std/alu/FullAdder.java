
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
public class FullAdder extends Composite
{
	public FullAdder(ComponentCollection parent, String name)
	{
		super(parent, name);
	}

	public final FullAdder setAll(Signal x, Signal y, Signal carryIn, Signal sum, Signal carryOut)
	{
		return (FullAdder)useAndSet(Flavor.DEFAULT, new String[] {"x", "y", "carryIn", "sum", "carryOut"}, x, y, carryIn, sum, carryOut);
	}

	@Override public final Map<String, Flavor> getFlavors()
	{
		return flavors;
	}

	private static final Map<String, Flavor> flavors = Flavor.getMap(
		new Flavor(
			new Flavor.Buildable<FullAdder>()
			{
				@Override public void build(FullAdder me, Map<String, Integer> variables)
				{
					Signal xySum = new Signal(1);
					Signal xyCarry = new Signal(1);
					Signal yCarryCarry = new Signal(1);

					Signal innerCarry = new Signal(xyCarry,yCarryCarry);
					
					new HalfAdder(me,"x,y->c1,is").setAll(me.getSignal("x"), me.getSignal("y"), xySum, xyCarry);
					new HalfAdder(me,"ci,is->c2,s").setAll(me.getSignal("carryIn"), xySum, me.getSignal("sum"), yCarryCarry);
					new OR(me,"c1,c2->co").setAll(innerCarry, me.getSignal("carryOut"));
				}
			},
			"x[1], y[1], carryIn[1]",
			"sum[1], carryOut[1]"
		)
	);

	private FullAdder(DeserializingStream in) throws IOException, DeserializingException, InstantiationException
	{
		super(in);
	}
}
