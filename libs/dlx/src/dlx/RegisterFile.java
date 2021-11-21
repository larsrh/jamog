
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
import core.misc.setable.GroupSetableComposite;
import core.signal.Signal;
import java.io.IOException;
import std.flipflop.*;
import std.gate.*;
import std.logic.*;
import core.build.ComponentCollection;
import core.misc.module.Module.Description;
import core.build.Flavor;
import core.misc.serial.DeserializingStream;
import java.util.Map;

import static core.signal.Bit.*;

/**
 * @author torben
 */
@Description
(
	name		= "Register File",
	description	= "The register file of the DLX."
)
public final class RegisterFile extends GroupSetableComposite
{
	public RegisterFile(ComponentCollection parent,String name)
	{
		super(parent,name);
	}

	public final RegisterFile setAll(Signal clk, Signal[] write, Signal[] write_en, Signal[] write31, Signal[] write31_en, Signal[] read)
	{
		return (RegisterFile)useAndSet(Flavor.DEFAULT, new String[] {"clk", "write", "write_en", "write31", "write31_en", "read"}, clk, write, write_en, write31, write31_en, read);
	}

	@Override public final Map<String, Flavor> getFlavors()
	{
		return flavors;
	}

	private static final Map<String, Flavor> flavors = Flavor.getMap(
		new Flavor(
			new Flavor.Buildable<RegisterFile>()
			{
				@Override public void build(RegisterFile me, Map<String, Integer> variables)
				{
					Signal clk = me.getSignal("clk");
					Signal[] write = (Signal[])me.get("write");
					Signal[] write_en = (Signal[])me.get("write_en");
					Signal[] write31 = (Signal[])me.get("write31");
					Signal[] write31_en = (Signal[])me.get("write31_en");
					Signal[] read = (Signal[])me.get("read");

					GatedDFlipFlop[] register;

					Signal iclk = new Signal(1);
					new NOT(me, "clk-inv").setAll(clk, iclk);

					register = new GatedDFlipFlop[31];
					for(int i = 1; i < 31; ++i)
						register[i - 1] = new GatedDFlipFlop(me, "register:" + i).setAll(iclk, write_en[i], write[i], read[i]);

					new Forward(me, "register:0").setAll(new Signal(L, L, L, L, L, L, L, L, L, L, L, L, L, L, L, L, L, L, L, L, L, L, L, L, L, L, L, L, L, L, L, L), read[0]);

					Signal r31bw = new Signal(32);
					Signal r31bwe = new Signal(1);

					new ThreeState(me, "bus:0").setAll(write31[0], write31_en[0], r31bw);
					new ThreeState(me, "bus:1").setAll(write31[1], write31_en[1], r31bw);
					new OR(me, "bus-write").setAll(new Signal(write31_en), r31bwe);

					register[30] = new GatedDFlipFlop(me, "register:31").setAll(iclk, r31bwe, r31bw, read[31]);

					for(int i = 0; i < 31; ++i)
						me.addGroup("register " + (i + 1), register[i]);
				}
			},
			"clk[1], write[31][32], write_en[31][1], write31[2][32], write31_en[2][1]",
			"read[32][32]"
		)
	);

	private RegisterFile(DeserializingStream in) throws IOException, DeserializingException, InstantiationException
	{
		super(in);
	}
}
