
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
import core.exception.SerializingException;
import core.misc.setable.GroupSetableComposite;
import core.misc.serial.SerializingStream;
import core.signal.Bit;
import core.signal.Signal;
import java.io.IOException;
import std.alu.*;
import std.flipflop.*;
import std.memory.*;
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
	name		= "Instruction Fetch",
	description	= "The instruction fetch component of the DLX pipeline."
)
public final class InstructionFetch extends GroupSetableComposite
{
	public InstructionFetch(ComponentCollection parent,String name)
	{
		super(parent,name);
	}

	public final InstructionFetch setAll(Signal clk, Signal pcoe, Signal pco, Signal ir, Signal pc, int ims)
	{
		return (InstructionFetch)useAndSet("default", new String[] {"clk", "pcoe", "pco", "ir", "pc", "ims"}, clk, pcoe, pco, ir, pc, ims);
	}

	@Override public final void serialize(SerializingStream out) throws IOException, SerializingException
	{
		super.serialize(out);

		out.writeObject(flipflop, false);
		out.writeObject(im, false);
	}

	@Override public final Map<String, Flavor> getFlavors()
	{
		return flavors;
	}

	@Override protected final void init()
	{
		super.init();
		for(int i = 0; i < 32; ++i)
			flipflop.setSetableBit(i, Bit.L);
		for(int i = 0; i < im.getSetableCount(); ++i)
			im.setSetableBit(i, Bit.L);
	}

	private static final Map<String, Flavor> flavors = Flavor.getMap(
		new Flavor(
			new Flavor.Buildable<InstructionFetch>()
			{
				@Override public void build(InstructionFetch me, Map<String, Integer> variables)
				{
					Signal clk = me.getSignal("clk");
					Signal pcoe = me.getSignal("pcoe");
					Signal pco = me.getSignal("pco");
					Signal ir = me.getSignal("ir");
					Signal pc = me.getSignal("pc");

					Signal[] ims = new Signal[(Integer)me.get("ims")];
					for(int i = 0; i < ims.length; ++i)
						ims[i] = new Signal(32);
					me.im = new ROM(me, "instruction-rom").setAll(ims);

					Signal next_pc = new Signal(32);
					Signal pc_plus_one = new Signal(32);

					new BinaryMultiplexer(me, "instruction-mux").setAll(ims, new Signal(pc, 0, 32 - Integer.numberOfLeadingZeros(ims.length - 1)), ir);
					me.flipflop = new DFlipFlop(me, "pc").setAll(clk, next_pc, pc);
					new RipleIncrementer(me, "inc").setAll(pc, pc_plus_one);
					new BinaryMultiplexer(me, "next-pc-mux").setAll(new Signal[] {pc_plus_one, pco}, pcoe, next_pc);

					me.addGroup("program counter", me.flipflop);
					me.addGroup("instruction memory", me.im);
				}
			},
			"clk[1], pcoe[1], pco[32]",
			"ir[32], pc[32]",
			"int ims",
			""
		)
	);

	private DFlipFlop flipflop;
	private ROM im;

	private InstructionFetch(DeserializingStream in) throws IOException, DeserializingException, InstantiationException
	{
		super(in);

		flipflop = in.readObject(DFlipFlop.class);
		im = in.readObject(ROM.class);
	}
}
