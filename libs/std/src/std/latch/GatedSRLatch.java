
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
import core.exception.SerializingException;
import core.misc.setable.Setable;
import core.misc.serial.SerializingStream;
import core.signal.Bit;
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
	name		= "Gated SR-Latch",
	description	= "A gated SR-Latch."
)
public final class GatedSRLatch extends Composite implements Setable
{
	public GatedSRLatch(ComponentCollection parent,String name)
	{
		super(parent,name);
	}

	public final GatedSRLatch setAll(Signal e, Signal s, Signal r, Signal q)
	{
		return (GatedSRLatch)useAndSet(Flavor.DEFAULT, new String[] {"e", "s", "r", "q"}, e, s, r, q);
	}

	public final GatedSRLatch setAll(Signal e, Signal s, Signal r, Signal q, Signal nq)
	{
		return (GatedSRLatch)useAndSet("invert", new String[] {"e", "s", "r", "q", "nq"}, e, s, r, q, nq);
	}

	@Override public void serialize(SerializingStream out) throws IOException, SerializingException
	{
		super.serialize(out);

		out.writeObject(latch, false);
		out.writeObject(is, false);
		out.writeObject(ir, false);
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
		is.setBit(i, Bit.L);
		ir.setBit(i, Bit.L);
	}

	private static final Map<String, Flavor> flavors = Flavor.getMap(
		new Flavor(
			new Flavor.Buildable<GatedSRLatch>()
			{
				@Override public void build(GatedSRLatch me, Map<String, Integer> variables)
				{
					me.build(me.getSignal("e"), me.getSignal("s"), me.getSignal("r"), me.getSignal("q"), new Signal(me.getSignal("q").size()));
				}
			},
			"e[1|n], s[n], r[n]",
			"q[n]"
		),
		new Flavor(
			"invert",
			new Flavor.Buildable<GatedSRLatch>()
			{
				@Override public void build(GatedSRLatch me, Map<String, Integer> variables)
				{
					me.build(me.getSignal("e"), me.getSignal("s"), me.getSignal("r"), me.getSignal("q"), me.getSignal("nq"));
				}
			},
			"e[1|n], s[n], r[n]",
			"q[n], nq[n]"
		)
	);

	private SRLatch latch;
	private Signal is;
	private Signal ir;

	private GatedSRLatch(DeserializingStream in) throws IOException, DeserializingException, InstantiationException
	{
		super(in);

		latch = in.readObject(SRLatch.class);
		is = in.readObject(Signal.class);
		ir = in.readObject(Signal.class);
	}

	private final void build(Signal e, Signal s, Signal r, Signal q, Signal nq)
	{
		if(e.size() == 1)
		{
			addPriority(e, s);
			addPriority(e, r);
		}
		else
		{
			for(int i = 0; i < e.size(); ++i)
			{
				addPriority(e.get(i), s.get(i));
				addPriority(e.get(i), r.get(i));
			}
		}

		is = new Signal(q.size());
		ir = new Signal(q.size());

		latch = new SRLatch(this, "latch").setAll(is, ir, q, nq);

		for(int i = 0; i < is.size(); ++i)
		{
			new AND(this, "s,e->is:" + i).setAll(new Signal(s.get(i), e.get(e.size() == 1 ? 0 : i)), is.get(i));
			new AND(this, "r,e->ir:" + i).setAll(new Signal(r.get(i), e.get(e.size() == 1 ? 0 : i)), ir.get(i));
		}
	}
}
