
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

package std.latch;

import core.exception.DeserializingException;
import core.exception.InstantiationException;
import core.misc.setable.Setable;
import core.signal.Bit;
import core.signal.Signal;
import core.build.Composite;
import std.gate.*;
import core.build.ComponentCollection;
import core.misc.module.Module.Description;
import core.build.Flavor;
import core.exception.SerializingException;
import core.misc.serial.DeserializingStream;
import core.misc.serial.SerializingStream;
import java.util.Map;
import java.io.IOException;

/**
 * @author torben
 */
@Description
(
	name		= "Gated D-Latch",
	description	= "A gated D-Latch."
)
public final class GatedDLatch extends Composite implements Setable
{
	public GatedDLatch(ComponentCollection parent,String name)
	{
		super(parent,name);
	}

	public final GatedDLatch setAll(Signal e, Signal d, Signal q)
	{
		return (GatedDLatch)useAndSet(Flavor.DEFAULT, new String[] {"e", "d", "q"}, e, d, q);
	}

	public final GatedDLatch setAll(Signal e, Signal d, Signal q, Signal nq)
	{
		return (GatedDLatch)useAndSet("invert", new String[] {"e", "d", "q", "nq"}, e, d, q, nq);
	}

	@Override public void serialize(SerializingStream out) throws IOException, SerializingException
	{
		super.serialize(out);

		out.writeObject(latch, false);
		out.writeObject(ns, false);
		out.writeObject(nr, false);
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
	}

	private static final Map<String, Flavor> flavors = Flavor.getMap(
		new Flavor(
			new Flavor.Buildable<GatedDLatch>()
			{
				@Override public void build(GatedDLatch me, Map<String, Integer> variables)
				{
					me.build(me.getSignal("e"), me.getSignal("d"), me.getSignal("q"), new Signal(me.getSignal("q").size()));
				}
			},
			"e[1|n], d[n]",
			"q[n]"
		),
		new Flavor(
			"invert",
			new Flavor.Buildable<GatedDLatch>()
			{
				@Override public void build(GatedDLatch me, Map<String, Integer> variables)
				{
					me.build(me.getSignal("e"), me.getSignal("d"), me.getSignal("q"), me.getSignal("nq"));
				}
			},
			"e[1|n], d[n]",
			"q[n], nq[n]"
		)
	);

	private NSNRLatch latch;
	private Signal ns;
	private Signal nr;

	private GatedDLatch(DeserializingStream in) throws IOException, DeserializingException, InstantiationException
	{
		super(in);

		latch = in.readObject(NSNRLatch.class);
		ns = in.readObject(Signal.class);
		nr = in.readObject(Signal.class);
	}

	private final void build(Signal e, Signal d, Signal q, Signal nq)
	{
		if(e.size() == 1)
			addPriority(e, d);
		else
			for(int i = 0; i < e.size(); ++i)
				addPriority(e.get(i), d.get(i));

		ns = new Signal(d.size());
		nr = new Signal(d.size());

		latch = new NSNRLatch(this,"latch").setAll(ns, nr, q, nq);

		for(int i = 0; i < ns.size(); ++i)
		{
			new NAND(this, "d,e->ns:" + i).setAll(new Signal(d.get(i), e.get(e.size() == 1 ? 0 : i)), ns.get(i));
			new NAND(this, "ns,e->nr:" + i).setAll(new Signal(ns.get(i), e.get(e.size() == 1 ? 0 : i)), nr.get(i));
		}
	}
}
