
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

package std.memory;

import core.exception.DeserializingException;
import core.exception.InstantiationException;
import core.exception.SerializingException;
import core.misc.setable.Setable;
import core.misc.serial.SerializingStream;
import core.signal.Bit;
import core.signal.Signal;
import core.build.Composite;
import java.io.IOException;
import std.flipflop.GatedDFlipFlop;
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
	name		= "RAM",
	description	= "A clocked RAM with raw read and write."
)
public final class RAM extends Composite implements Setable
{
	private int data_size;
	private GatedDFlipFlop[] flipflop;

	public RAM(ComponentCollection parent,String name)
	{
		super(parent,name);
	}

	public final RAM setAll(Signal clk, Signal[] write, Signal[] write_en, Signal[] read)
	{
		return (RAM)useAndSet(Flavor.DEFAULT, new String[] {"clk", "write", "write_en", "read"}, clk, write, write_en, read);
	}

	@Override public void serialize(SerializingStream out) throws IOException, SerializingException
	{
		super.serialize(out);

		out.writeInteger(data_size);
		out.writeObject(flipflop, false, false);
	}

	@Override public Map<String, Flavor> getFlavors()
	{
		return flavors;
	}

	@Override public final int getSetableCount()
	{
		return flipflop.length * data_size;
	}

	@Override public final Bit getSetableBit(int i)
	{
		return flipflop[i / data_size].getSetableBit(i % data_size);
	}

	@Override public final void setSetableBit(int i, Bit v)
	{
		flipflop[i / data_size].setSetableBit(i % data_size, v);
	}

	private static final Map<String, Flavor> flavors = Flavor.getMap(
		new Flavor(
			new Flavor.Buildable<RAM>()
			{
				@Override public void build(RAM me, Map<String, Integer> variables)
				{
					Signal clk = me.getSignal("clk");
					Signal[] write = (Signal[])me.get("write");
					Signal[] write_en = (Signal[])me.get("write_en");
					Signal[] read = (Signal[])me.get("read");

					me.data_size = variables.get("size");
					me.flipflop = new GatedDFlipFlop[read.length];
					for(int i = 0; i < me.flipflop.length; ++i)
						me.flipflop[i] = new GatedDFlipFlop(me, "latch:" + i).setAll(clk, write_en[i], write[i], read[i]);
				}
			},
			"clk[1], write[n][size], write_en[n][1]",
			"read[n][size]"
		)
	);

	private RAM(DeserializingStream in) throws IOException, DeserializingException, InstantiationException
	{
		super(in);

		data_size = in.readInteger();
		flipflop = in.readObject(GatedDFlipFlop[].class, GatedDFlipFlop.class);
	}
}
