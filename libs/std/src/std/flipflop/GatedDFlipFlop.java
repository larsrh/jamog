
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

package std.flipflop;

import core.exception.DeserializingException;
import core.exception.InstantiationException;
import core.exception.SerializingException;
import core.misc.setable.Setable;
import core.misc.serial.SerializingStream;
import core.signal.Bit;
import core.signal.Signal;
import core.build.Composite;
import java.io.IOException;
import std.gate.*;
import std.latch.GatedDLatch;
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
	name		= "Gated D-Flipflop",
	description	= "A gated D-Flipflop."
)
public final class GatedDFlipFlop extends Composite implements Setable
{
	public GatedDFlipFlop(ComponentCollection parent,String name)
	{
		super(parent,name);
	}

	public final GatedDFlipFlop setAll(Signal clk, Signal e, Signal d, Signal q)
	{
		return (GatedDFlipFlop)useAndSet(Flavor.DEFAULT, new String[] {"clk", "e", "d", "q"}, clk, e, d, q);
	}

	public final GatedDFlipFlop setAll(Signal clk, Signal e, Signal d, Signal q, Signal nq)
	{
		return (GatedDFlipFlop)useAndSet("invert", new String[] {"clk", "e", "d", "q", "nq"}, clk, e, d, q, nq);
	}

	@Override public void serialize(SerializingStream out) throws IOException, SerializingException
	{
		super.serialize(out);

		out.writeObject(flipflop, false);
	}

	@Override public Map<String, Flavor> getFlavors()
	{
		return flavors;
	}

	@Override public final int getSetableCount()
	{
		return flipflop.getSetableCount();
	}

	@Override public final Bit getSetableBit(int i)
	{
		return flipflop.getSetableBit(i);
	}

	@Override public final void setSetableBit(int i, Bit v)
	{
		flipflop.setSetableBit(i, v);
	}

	private static final Map<String, Flavor> flavors = Flavor.getMap(
		new Flavor(
			new Flavor.Buildable<GatedDFlipFlop>()
			{
				@Override public void build(GatedDFlipFlop me, Map<String, Integer> variables)
				{
					me.build(me.getSignal("clk"), me.getSignal("e"), me.getSignal("d"), me.getSignal("q"), new Signal(me.getSignal("q").size()));
				}
			},
			"clk[1|n], e[m], d[n]",
			"q[n]",
			"#m == #clk"
		),
		new Flavor(
			"invert",
			new Flavor.Buildable<GatedDFlipFlop>()
			{
				@Override public void build(GatedDFlipFlop me, Map<String, Integer> variables)
				{
					me.build(me.getSignal("clk"), me.getSignal("e"), me.getSignal("d"), me.getSignal("q"), me.getSignal("nq"));
				}
			},
			"clk[1|n], e[m], d[n]",
			"q[n], nq[n]",
			"#m == #clk"
		)
	);

	private DFlipFlop flipflop;

	private GatedDFlipFlop(DeserializingStream in) throws IOException, DeserializingException, InstantiationException
	{
		super(in);

		flipflop = in.readObject(DFlipFlop.class);
	}

	private final void build(Signal clk, Signal e, Signal d, Signal q, Signal nq)
	{
		Signal iclk = new Signal(clk.size());
		Signal se = new Signal(e.size());
		Signal seclk = new Signal(clk.size());

		new NOT(this, "clk-inv").setAll(clk, iclk);
		new GatedDLatch(this, "e-gate").setAll(iclk, e, se);

		for(int i = 0; i < seclk.size(); ++i)
			new AND(this, "clk,ge->e:" + i).setAll(new Signal(clk.get(i), se.get(i)), seclk.get(i));

		flipflop = new DFlipFlop(this, "flipflop").setAll(seclk, d, q, nq);
	}
}
