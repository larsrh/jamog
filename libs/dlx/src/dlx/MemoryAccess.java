
/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *                                                                           *
 * Copyright 2009 Lars Hupel, Torben Maack, Sylvester Tremmel                *
 *                                                                           *
 * This file is part of the Jamog DLX Library.                               *
 *                                                                           *
 * The Jamog DLX Library is free software: you can redistribute it           *
 * and/or modify it under the terms of the GNU General Public License        *
 * as published by the Free Software Foundation; version 3.                  *
 *                                                                           *
 * The Jamog DLX Library is distributed in the hope that it will be          *
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty       *
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the           *
 * GNU General Public License for more details.                              *
 *                                                                           *
 * You should have received a copy of the GNU General Public License         *
 * along with the Jamog DLX Library. If not, see                             *
 * <http://www.gnu.org/licenses/>.                                           *
 *                                                                           *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

package dlx;

import core.exception.DeserializingException;
import core.exception.InstantiationException;
import core.signal.Signal;
import core.build.Composite;
import java.io.IOException;
import java.util.Map;
import java.util.HashMap;
import std.convert.*;
import std.gate.*;
import std.logic.*;
import std.mux.*;
import core.build.ComponentCollection;
import core.misc.module.Module.Description;
import core.build.Flavor;
import core.misc.serial.DeserializingStream;
import core.signal.Bit;

import static core.signal.Bit.*;

/**
 * @author torben
 */
@Description
(
	name		= "Memory Access",
	description	= "The memory access component of the DLX pipeline. Actual memory or memory mapped IO signals must be provided."
)
public final class MemoryAccess extends Composite
{
	public MemoryAccess(ComponentCollection parent,String name)
	{
		super(parent,name);
	}

	public final MemoryAccess setAll(Signal address, Signal load_en, Signal store_en, Signal store, Signal[] read, Signal clk, Signal load, Signal[] write, Signal[] write_en, Signal mem_clk)
	{
		return (MemoryAccess)useAndSet("default", new String[] {"address", "load_en", "store_en", "store", "read", "clk", "load", "write", "write_en", "mem_clk"}, address, load_en, store_en, store, read, clk, load, write, write_en, mem_clk);
	}

	@Override public final Map<String, Flavor> getFlavors()
	{
		return flavors;
	}

	private static final Map<String, Flavor> flavors = Flavor.getMap(
		new Flavor(
			new Flavor.Buildable<MemoryAccess>()
			{
				@Override public void build(MemoryAccess me, Map<String, Integer> variables)
				{
					Signal address = me.getSignal("address");
					Signal load_en = me.getSignal("load_en");
					Signal store_en = me.getSignal("store_en");
					Signal store = me.getSignal("store");
					Signal[] read = (Signal[])me.get("read");
					Signal clk = me.getSignal("clk");
					Signal load = me.getSignal("load");
					Signal[] write = (Signal[])me.get("write");
					Signal[] write_en = (Signal[])me.get("write_en");
					Signal mem_clk = me.getSignal("mem_clk");

					Signal dstore = new Signal(4);
					new Mapping(me, "store-map").setAll(store_en, dstore, map);

					Signal sel = new Signal(read.length);
					new BinaryDecoder(me, "address-decode").setAll(new Signal(address, 0, 32 - Integer.numberOfLeadingZeros(read.length - 1)), sel);

					Signal readres = new Signal(32);

					Signal lb = new Signal(new Signal(readres, 0, 8), new Signal(readres.get(7), 24));
					Signal lbu = new Signal(new Signal(readres, 0, 8), new Signal(L, 24));

					Signal lh = new Signal(new Signal(readres, 0, 16), new Signal(readres.get(15), 16));
					Signal lhu = new Signal(new Signal(readres, 0, 16), new Signal(L, 16));

					new BinaryMultiplexer(me, "load-signext-mux").setAll(new Signal[] {lb, lbu, lh, lhu, readres}, load_en, load);

					new NOT(me, "clk-inv").setAll(clk, mem_clk);

					Signal[] SMDR = new Signal[read.length];
					for(int i = 0; i < SMDR.length; ++i)
						SMDR[i] = new Signal(read[i], read[(i + 1) % read.length], read[(i + 2) % read.length], read[(i + 3) % read.length]);
					new Multiplexer(me, "read-mux").setAll(SMDR, sel, readres);

					Signal[] SMDWE = new Signal[write.length];
					for(int i = 0; i < write.length; ++i)
						SMDWE[i] = new Signal(4);
					new Demultiplexer(me, "write-demux").setAll(dstore, sel, SMDWE);

					for(int i = 0; i < write.length; ++i)
					{
						new ThreeState(me, "bus-" + i + ":0").setAll(store.get(0, 8), SMDWE[i].get(0), write[i]);
						new ThreeState(me, "bus-" + (i + 1) % write.length + ":2").setAll(store.get(8, 8), SMDWE[i].get(1), write[(i + 1) % write.length]);
						new ThreeState(me, "bus-" + (i + 2) % write.length + ":3").setAll(store.get(16, 8), SMDWE[i].get(2), write[(i + 2) % write.length]);
						new ThreeState(me, "bus-" + (i + 3) % write.length + ":4").setAll(store.get(24, 8), SMDWE[i].get(3), write[(i + 3) % write.length]);
						new OR(me, "store_en:" + i).setAll(new Signal(SMDWE[i].get(0), SMDWE[(i + write.length - 1) % write.length].get(1), SMDWE[(i + write.length - 2) % write.length].get(2), SMDWE[(i + write.length - 3) % write.length].get(3)), write_en[i]);
					}
				}
			},
			"address[32], load_en[3], store_en[2], store[32], read[n:4-][8], clk[1]",
			"load[32], write[n][8], write_en[n][1], mem_clk[1]",
			""
		)
	);

	private static final Map<Bit[], Bit[]> map;

	static
	{
		map = new HashMap<Bit[], Bit[]>();

		map.put(new Bit[]{L, L}, new Bit[]{L, L, L, L});
		map.put(new Bit[]{H, L}, new Bit[]{H, L, L, L});
		map.put(new Bit[]{L, H}, new Bit[]{H, H, L, L});
		map.put(new Bit[]{H, H}, new Bit[]{H, H, H, H});
	}

	private MemoryAccess(DeserializingStream in) throws IOException, DeserializingException, InstantiationException
	{
		super(in);
	}
}
