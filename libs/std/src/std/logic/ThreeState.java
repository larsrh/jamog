
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

package std.logic;

import core.exception.DeserializingException;
import core.exception.InstantiationException;
import core.exception.SerializingException;
import core.signal.Bit;
import core.signal.Signal;
import core.sim.Calculator;
import core.build.ComponentCollection;
import core.misc.module.Module.Description;
import core.build.Flavor;
import core.misc.serial.DeserializingStream;
import core.misc.serial.SerializingStream;
import java.io.IOException;
import java.util.Map;

/**
 * @author torben
 */
@Description
(
	name		= "ThreeState",
	description	= "A imple ThreeState."
)
public final class ThreeState extends Calculator
{
	public ThreeState(ComponentCollection parent,String name)
	{
		super(parent,name);
	}

	public final ThreeState setAll(Signal in, Signal ctrl, Signal out)
	{
		return (ThreeState)useAndSet(Flavor.DEFAULT, new String[] {"in", "ctrl", "out"}, in, ctrl, out);
	}

	@Override public Map<String, Flavor> getFlavors()
	{
		return flavors;
	}

	@Override public final void execute()
	{
		for(int i = 0; i < in.size(); ++i)
		{
			if(ctrl.compareBit(ctrl.size() == 1 ? 0 : i, Bit.H))
				out.getSignalBit(i).is(in.getBit(i));
			else
				out.getSignalBit(i).is(Bit.Z);
		}
	}

	@Override public void serialize(SerializingStream out) throws IOException, SerializingException
	{
		super.serialize(out);

		out.writeObject(this.in, false);
		out.writeObject(this.ctrl, false);
		out.writeObject(this.out, false);
	}

	private static final Map<String, Flavor> flavors = Flavor.getMap(
		new Flavor(
			new Flavor.Buildable<ThreeState>()
			{
				@Override public void build(ThreeState me, Map<String, Integer> variables)
				{
					me.in = me.getSignal("in");
					me.ctrl = me.getSignal("ctrl");
					me.out = me.getSignal("out");
				}
			},
			"in[n], ctrl[1|n]",
			"out[n]"
		)
	);

	private Signal in;
	private Signal ctrl;
	private Signal out;

	private ThreeState(DeserializingStream in) throws IOException, DeserializingException, InstantiationException
	{
		super(in);

		this.in = in.readObject(Signal.class);
		this.ctrl = in.readObject(Signal.class);
		this.out = in.readObject(Signal.class);
	}
}
