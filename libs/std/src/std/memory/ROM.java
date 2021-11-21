
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
import core.sim.Calculator;
import core.build.ComponentCollection;
import core.misc.module.Module.Description;
import core.build.Flavor;
import core.misc.serial.DeserializingStream;
import java.io.IOException;
import java.util.Map;

/**
 * @author torben
 */
@Description
(
	name		= "ROM",
	description	= "A ROM with raw output."
)
public final class ROM extends Calculator implements Setable
{
	public ROM(ComponentCollection parent,String name)
	{
		super(parent,name);
	}

	public final ROM setAll(Signal[] out)
	{
		return (ROM)useAndSet(Flavor.DEFAULT, new String[] {"out"}, (Object)out);
	}

	@Override public void serialize(SerializingStream out) throws IOException, SerializingException
	{
		super.serialize(out);

		out.writeInteger(data_size);
		out.writeObject(rom, false, false);
	}

	@Override public Map<String, Flavor> getFlavors()
	{
		return flavors;
	}

	@Override public void execute()
	{
	}

	@Override public final int getSetableCount()
	{
		return rom.length * data_size;
	}

	@Override public final Bit getSetableBit(int i)
	{
		return rom[i / data_size].getBit(i % data_size);
	}

	@Override public final void setSetableBit(int i, Bit v)
	{
		rom[i / data_size].setBit(i % data_size, v);
	}

	private static final Map<String, Flavor> flavors = Flavor.getMap(
		new Flavor(
			new Flavor.Buildable<ROM>()
			{
				@Override public void build(ROM me, Map<String, Integer> variables)
				{
					me.rom = (Signal[])me.get("out");
					me.data_size = variables.get("size");
				}
			},
			"",
			"out[n][size]"
		)
	);

	private int data_size;
	private Signal[] rom;

	private ROM(DeserializingStream in) throws IOException, DeserializingException, InstantiationException
	{
		super(in);

		data_size = in.readInteger();
		rom = in.readObject(Signal[].class, Signal.class);
	}
}
