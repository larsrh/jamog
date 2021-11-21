
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
	name		= "SR-Latch",
	description	= "A simple SR-Latch."
)
public final class SRLatch extends Composite implements Setable
{
	public SRLatch(ComponentCollection parent,String name)
	{
		super(parent,name);
	}

	public final SRLatch setAll(Signal s, Signal r, Signal q)
	{
		return (SRLatch)useAndSet(Flavor.DEFAULT, new String[] {"s", "r", "q"}, s, r, q);
	}

	public final SRLatch setAll(Signal s, Signal r, Signal q, Signal nq)
	{
		return (SRLatch)useAndSet("invert", new String[] {"s", "r", "q", "nq"}, s, r, q, nq);
	}

	@Override public void serialize(SerializingStream out) throws IOException, SerializingException
	{
		super.serialize(out);

		out.writeObject(q, false);
		out.writeObject(nq, false);
	}

	@Override public Map<String, Flavor> getFlavors()
	{
		return flavors;
	}

	@Override public final int getSetableCount()
	{
		return q.size();
	}

	@Override public final Bit getSetableBit(int i)
	{
		return q.getBit(i);
	}

	@Override public final void setSetableBit(int i, Bit v)
	{
		q.setBit(i, v);
		nq.setBit(i, v.not());
	}

	private static final Map<String, Flavor> flavors = Flavor.getMap(
		new Flavor(
			new Flavor.Buildable<SRLatch>()
			{
				@Override public void build(SRLatch me, Map<String, Integer> variables)
				{
					me.build(me.getSignal("s"), me.getSignal("r"), me.getSignal("q"), new Signal(me.getSignal("q").size()));
				}
			},
			"s[n], r[n]",
			"q[n]"
		),
		new Flavor(
			"invert",
			new Flavor.Buildable<SRLatch>()
			{
				@Override public void build(SRLatch me, Map<String, Integer> variables)
				{
					me.build(me.getSignal("s"), me.getSignal("r"), me.getSignal("q"), me.getSignal("nq"));
				}
			},
			"s[n], r[n]",
			"q[n], nq[n]"
		)
	);

	private Signal q;
	private Signal nq;

	private SRLatch(DeserializingStream in) throws IOException, DeserializingException, InstantiationException
	{
		super(in);

		q = in.readObject(Signal.class);
		nq = in.readObject(Signal.class);
	}

	private final void build(Signal s, Signal r, Signal q, Signal nq)
	{
		this.q = q;
		this.nq = nq;

		for(int i = 0; i < q.size(); ++i) {
			new NOR(this, "s,q->nq:" + i).setAll(new Signal(s.get(i), q.get(i)), nq.get(i));
			new NOR(this, "r,nq->q:" + i).setAll(new Signal(r.get(i), nq.get(i)), q.get(i));
		}
	}
}
