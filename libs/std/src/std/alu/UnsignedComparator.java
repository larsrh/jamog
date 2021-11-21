
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
import std.gate.NOT;
import std.gate.OR;
import core.build.ComponentCollection;
import core.build.Composite;
import core.build.Flavor;
import core.exception.DeserializingException;
import core.misc.serial.DeserializingStream;
import core.signal.Signal;
import java.io.IOException;
import java.util.Map;
import static core.signal.Bit.*;

/**
 * @author lars
 */
public class UnsignedComparator extends Composite
{
	public UnsignedComparator(ComponentCollection parent,String name)
	{
		super(parent,name);
	}
	
	// cmp = [ x >= y; x != y ]

	public final UnsignedComparator setAll(Signal x, Signal y, Signal cmp)
	{
		return (UnsignedComparator)useAndSet(Flavor.DEFAULT, new String[] {"x", "y", "cmp"}, x, y, cmp);
	}

	public final UnsignedComparator setAll(Signal x, Signal y, Signal diff, Signal cmp)
	{
		return (UnsignedComparator)useAndSet("extended", new String[] {"x", "y", "diff", "cmp"}, x, y, diff, cmp);
	}

	@Override public final Map<String, Flavor> getFlavors()
	{
		return flavors;
	}

	private static final Map<String, Flavor> flavors = Flavor.getMap(
		new Flavor(
			new Flavor.Buildable<UnsignedComparator>()
			{
				@Override public void build(UnsignedComparator me, Map<String, Integer> variables)
				{
					me.build(me.getSignal("x"), me.getSignal("y"), new Signal(me.getSignal("x").size()), me.getSignal("cmp"));
				}
			},
			"x[n], y[n]",
			"cmp[2]"
		),
		new Flavor(
			"extended",
			new Flavor.Buildable<UnsignedComparator>()
			{
				@Override public void build(UnsignedComparator me, Map<String, Integer> variables)
				{
					me.build(me.getSignal("x"), me.getSignal("y"), me.getSignal("diff"), me.getSignal("cmp"));
				}
			},
			"x[n], y[n]",
			"diff[n], cmp[2]"
		)
	);

	private UnsignedComparator(DeserializingStream in) throws IOException, DeserializingException, InstantiationException
	{
		super(in);
	}

	private final void build(Signal x,Signal y,Signal diff,Signal cmp)
	{
		final int width = x.size();
		Signal negatorOutput = new Signal(width);

		new NOT(this,"inv")
			.setAll(y,negatorOutput);
		new CLAAdder(this,"add")
			.useAndSet("riple",new String[]{"x","y","carryIn","sum","carryOut"},
				x,
				negatorOutput,
				new Signal(H),
				diff,
				cmp.get(0)
			);
		new OR(this,"or")
			.setAll(diff,cmp.get(1));
	}
}
