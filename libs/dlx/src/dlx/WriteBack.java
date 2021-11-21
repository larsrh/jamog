
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
import core.signal.Bit;
import core.signal.Signal;
import core.build.Composite;
import java.io.IOException;
import std.logic.*;
import std.gate.*;
import std.mux.*;
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
	name		= "Write Back",
	description	= "The write back component of the DLX pipeline."
)
public final class WriteBack extends Composite
{
	public WriteBack(ComponentCollection parent,String name)
	{
		super(parent,name);
	}

	public final WriteBack setAll(Signal alu, Signal load, Signal res_sel, Signal we, Signal ra, Signal afw, Signal rfw, Signal[] write, Signal[] write_en)
	{
		return (WriteBack)useAndSet(Flavor.DEFAULT, new String[] {"alu", "load", "res_sel", "we", "ra", "afw", "rfw", "write", "write_en"}, alu, load, res_sel, we, ra, afw, rfw, write, write_en);
	}

	@Override public final Map<String, Flavor> getFlavors()
	{
		return flavors;
	}

	private static final Map<String, Flavor> flavors = Flavor.getMap(
		new Flavor(
			new Flavor.Buildable<WriteBack>()
			{
				@Override public void build(WriteBack me, Map<String, Integer> variables)
				{
					Signal alu = me.getSignal("alu");
					Signal load = me.getSignal("load");
					Signal res_sel = me.getSignal("res_sel");
					Signal we = me.getSignal("we");
					Signal ra = me.getSignal("ra");
					Signal afw = me.getSignal("afw");
					Signal rfw = me.getSignal("rfw");
					Signal[] write = (Signal[])me.get("write");
					Signal[] write_en = (Signal[])me.get("write_en");

					Signal ires = new Signal(32);
					Signal nn = new Signal(1);
					Signal v = new Signal(1);

					new BinaryMultiplexer(me, "res-sel").setAll(new Signal[] {alu, load}, res_sel, ires);
					new BinaryDemultiplexer(me, "address-demux").setAll(we, ra, write_en);
					new OR(me, "null-test").setAll(ra, nn);
					new AND(me, "en-fw").setAll(new Signal(nn, we, res_sel), v); // also test res-sel cause alu-res is known to ALU (and may be updated)
					new BinaryMultiplexer(me, "afw-mux").setAll(new Signal[] {new Signal(Bit.L, 5), ra}, v, afw);
					new BinaryMultiplexer(me, "rfw-mux").setAll(new Signal[] {new Signal(Bit.L, 32), ires}, v, rfw);

					for(int i = 0; i < 32; ++i)
						new Forward(me, "res-fw:" + i).setAll(ires, write[i]);
				}
			},
			"alu[32], load[32], res_sel[1], we[1], ra[5]",
			"afw[5], rfw[32], write[32][32], write_en[32][1]"
		)
	);

	private WriteBack(DeserializingStream in) throws IOException, DeserializingException, InstantiationException
	{
		super(in);
	}
}
