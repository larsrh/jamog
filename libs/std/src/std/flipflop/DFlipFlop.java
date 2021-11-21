
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
import std.latch.*;
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
	name		= "D-Flipflop",
	description	= "A simple D-Flipflop."
)
public final class DFlipFlop extends Composite implements Setable
{
	public DFlipFlop(ComponentCollection parent,String name)
	{
		super(parent,name);
	}

	public final DFlipFlop setAll(Signal clk, Signal d, Signal q)
	{
		return (DFlipFlop)useAndSet(Flavor.DEFAULT, new String[] {"clk", "d", "q"}, clk, d, q);
	}

	public final DFlipFlop setAll(Signal clk, Signal d, Signal q, Signal nq)
	{
		return (DFlipFlop)useAndSet("invert", new String[] {"clk", "d", "q", "nq"}, clk, d, q, nq);
	}

	@Override public void serialize(SerializingStream out) throws IOException, SerializingException
	{
		super.serialize(out);

		out.writeObject(latch, false);
		out.writeObject(ns, false);
		out.writeObject(nr, false);
		out.writeObject(i1, false);
		out.writeObject(i2, false);
	}

	@Override public Map<String, Flavor> getFlavors()
	{
		return flavors;
	}

	@Override public final int getSetableCount()
	{
		return latch.getSetableCount();
	}

	@Override public final Bit getSetableBit(int i)
	{
		return latch.getSetableBit(i);
	}

	@Override public final void setSetableBit(int i, Bit v)
	{
		latch.setSetableBit(i, v);
		ns.setBit(i, Bit.H);
		nr.setBit(i, Bit.H);
		i1.setBit(i, v.not());
		i2.setBit(i, v);
	}

	private static final Map<String, Flavor> flavors = Flavor.getMap(
		new Flavor(
			new Flavor.Buildable<DFlipFlop>()
			{
				@Override public void build(DFlipFlop me, Map<String, Integer> variables)
				{
					me.build(me.getSignal("clk"), me.getSignal("d"), me.getSignal("q"), new Signal(me.getSignal("q").size()));
				}
			},
			"clk[1|n], d[n]",
			"q[n]"
		),
		new Flavor(
			"invert",
			new Flavor.Buildable<DFlipFlop>()
			{
				@Override public void build(DFlipFlop me, Map<String, Integer> variables)
				{
					me.build(me.getSignal("clk"), me.getSignal("d"), me.getSignal("q"), me.getSignal("nq"));
				}
			},
			"clk[1|n], d[n]",
			"q[n], nq[n]"
		)
	);

	private NSNRLatch latch;
	private Signal ns;
	private Signal nr;
	private Signal i1;
	private Signal i2;

	private DFlipFlop(DeserializingStream in) throws IOException, DeserializingException, InstantiationException
	{
		super(in);

		latch = in.readObject(NSNRLatch.class);
		ns = in.readObject(Signal.class);
		nr = in.readObject(Signal.class);
		i1 = in.readObject(Signal.class);
		i2 = in.readObject(Signal.class);
	}

	private final void build(Signal clk, Signal d, Signal q, Signal nq)
	{
		addPriority(clk, d);

		ns = new Signal(d.size());
		nr = new Signal(d.size());
		i1 = new Signal(d.size());
		i2 = new Signal(d.size());

		latch = new NSNRLatch(this, "latch").setAll(ns, nr, q, nq);

		for(int i = 0; i < d.size(); ++i)
		{
			new NAND(this, "i1,ns->i2:" + i).setAll(new Signal(i1.get(i), ns.get(i)), i2.get(i));
			new NAND(this, "i2,clk->ns:" + i).setAll(new Signal(i2.get(i), clk.get(clk.size() == 1 ? 0 : i)), ns.get(i));
			new NAND(this, "ns,clk,i1->nr:" + i).setAll(new Signal(ns.get(i), clk.get(clk.size() == 1 ? 0 : i), i1.get(i)), nr.get(i));
			new NAND(this, "nr,d->i1:" + i).setAll(new Signal(nr.get(i), d.get(i)), i1.get(i));
		}
	}
}
