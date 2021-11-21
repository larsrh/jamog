
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

package std.io;

import core.exception.DeserializingException;
import core.exception.InstantiationException;
import core.exception.SerializingException;
import core.misc.serial.SerializingStream;
import core.signal.Signal;
import core.sim.Calculator;
import core.build.ComponentCollection;
import core.build.Flavor;
import core.misc.BitConverter;
import core.misc.serial.DeserializingStream;
import core.signal.Bit;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

import static core.signal.Bit.*;

/**
 * @author torben
 */
public final class OutputStreamConnector extends Calculator
{
	public OutputStreamConnector(ComponentCollection parent,String name)
	{
		super(parent, name);
	}

	public final OutputStreamConnector setAll(Signal clk, Signal[] write, Signal[] write_en, Signal[] read, OutputStream os)
	{
		return (OutputStreamConnector)useAndSet(Flavor.DEFAULT, new String[] {"clk", "write", "write_en", "read", "os"}, clk, write, write_en, read, os);
	}

	@Override public Map<String, Flavor> getFlavors()
	{
		return flavors;
	}

	@Override public final void execute()
	{
		Bit new_clk = clk.getBit(0);
		if(new_clk == H && old_clk == L)
		{
			if(write_en[1].compareBit(0, H))
				value = write[1].getBits();

			if(write_en[0].compareBit(0, H) && write[0].compareBit(0, H))
				try
				{
					os.write((byte)BitConverter.bitsToLong(true, value));
					os.flush();
				}
				catch(IOException ioe)
				{
					ioe.printStackTrace();
				}
		}
		old_clk = new_clk;
	}

	@Override public void serialize(SerializingStream out) throws IOException, SerializingException
	{
		/*super.serialize(out);

		out.writeObject(this.clk, false);
		out.writeObject(this.flush, false);
		out.writeObject(this.write, false);
		out.writeObject(os);*/
	}

	private static final Map<String, Flavor> flavors = Flavor.getMap(
		new Flavor(
			new Flavor.Buildable<OutputStreamConnector>()
			{
				@Override public void build(OutputStreamConnector me, Map<String, Integer> variables)
				{
					me.clk = me.getSignal("clk");
					me.write = (Signal[])me.get("write");
					me.write_en = (Signal[])me.get("write_en");

					me.old_clk = me.clk.getBit(0);
					me.value = me.write[1].getBits();
					me.os = (OutputStream)me.get("os");
				}
			},
			"clk[1], write[2][8], write_en[2][1]",
			"read[2][8]",
			"java.io.OutputStream os",
			""
		)
	);

	private Signal clk;
	private Signal[] write;
	private Signal[] write_en;

	private Bit old_clk;
	private Bit[] value;
	private OutputStream os;

	/*private OutputStreamConnector(DeserializingStream in) throws IOException, DeserializingException, InstantiationException
	{
		super(in);

		this.clk = in.readObject(Signal.class);
		this.flush = in.readObject(Signal.class);
		this.write = in.readObject(Signal.class);
		this.os = in.readObject();
	}*/
}
