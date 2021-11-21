
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
import javax.swing.JComponent;
import std.flipflop.*;
import core.build.ComponentCollection;
import core.misc.module.Module.Description;
import core.build.Flavor;
import core.misc.serial.DeserializingStream;
import core.misc.setable.GroupSetable;
import gui.GUI;
import java.util.Map;

/**
 * @author torben
 */
@Description
(
	name		= "Processor",
	description	= "The complete DLX processor. Clock and memory must be provided."
)
public final class Processor extends GroupSetableComposite implements GUI
{
	public Processor(ComponentCollection parent,String name)
	{
		super(parent,name);
	}

	public final Processor setAll(Signal clk, Signal[] mem_read, Signal[] mem_write, Signal[] mem_write_en, Signal mem_clk, int ims, boolean with_fpu)
	{
		return (Processor)useAndSet(Flavor.DEFAULT, new String[] {"clk", "mem_read", "mem_write", "mem_write_en", "mem_clk", "ims", "with_fpu"}, clk, mem_read, mem_write, mem_write_en, mem_clk, ims, with_fpu);
	}

	@Override public final void serialize(SerializingStream out) throws IOException, SerializingException
	{
		super.serialize(out);

		out.writeObject(flipflops, false, false);
	}

	@Override public final Map<String, Flavor> getFlavors()
	{
		return flavors;
	}

	@Override protected final void init()
	{
		super.init();
		for(DFlipFlop flipflop : flipflops)
			for(int i = 0; i < flipflop.getSetableCount(); ++i)
				flipflop.setSetableBit(i, Bit.L);
	}

	private static final Map<String, Flavor> flavors = Flavor.getMap(
		new Flavor(
			new Flavor.Buildable<Processor>()
			{
				@Override public void build(Processor me, Map<String, Integer> variables)
				{
					Signal clk = me.getSignal("clk");
					Signal[] mem_read = (Signal[])me.get("mem_read");
					Signal[] mem_write = (Signal[])me.get("mem_write");
					Signal[] mem_write_en = (Signal[])me.get("mem_write_en");
					Signal mem_clk = me.getSignal("mem_clk");

					boolean with_fpu = (Boolean)me.get("with_fpu");

					me.flipflops = new DFlipFlop[27];

					// InstructionFetch to InstructionDecode
					Signal ir_out = new Signal(32);
					Signal ir_in = new Signal(32);
					Signal pc_out = new Signal(32);
					Signal pc_in = new Signal(32);

					me.flipflops[0] = new DFlipFlop(me, "flipflop:0").setAll(clk, ir_out, ir_in);
					me.flipflops[1] = new DFlipFlop(me, "flipflop:1").setAll(clk, pc_out, pc_in);

					// InstructionDecode to Execution
					Signal opcode_out;
					Signal opcode_in;
					if(with_fpu)
					{
						opcode_out = new Signal(6);
						opcode_in = new Signal(6);
					}
					else
					{
						opcode_out = new Signal(5);
						opcode_in = new Signal(5);
					}
					Signal alu_imm_out = new Signal(1);
					Signal alu_imm_in = new Signal(1);
					Signal alu_fw_out = new Signal(1);
					Signal alu_fw_in = new Signal(1);
					Signal r1_out = new Signal(32);
					Signal r1_in = new Signal(32);
					Signal r2_out = new Signal(32);
					Signal r2_in = new Signal(32);
					Signal a1_out = new Signal(5);
					Signal a1_in = new Signal(5);
					Signal a2_out = new Signal(5);
					Signal a2_in = new Signal(5);
					Signal imm_out = new Signal(32);
					Signal imm_in = new Signal(32);

					me.flipflops[2] = new DFlipFlop(me, "flipflop:2").setAll(clk, opcode_out, opcode_in);
					me.flipflops[3] = new DFlipFlop(me, "flipflop:3").setAll(clk, alu_imm_out, alu_imm_in);
					me.flipflops[4] = new DFlipFlop(me, "flipflop:4").setAll(clk, alu_fw_out, alu_fw_in);
					me.flipflops[5] = new DFlipFlop(me, "flipflop:5").setAll(clk, r1_out, r1_in);
					me.flipflops[6] = new DFlipFlop(me, "flipflop:6").setAll(clk, r2_out, r2_in);
					me.flipflops[7] = new DFlipFlop(me, "flipflop:7").setAll(clk, a1_out, a1_in);
					me.flipflops[8] = new DFlipFlop(me, "flipflop:8").setAll(clk, a2_out, a2_in);
					me.flipflops[9] = new DFlipFlop(me, "flipflop:9").setAll(clk, imm_out, imm_in);

					// InstructionDecode to MemoryAccess
					Signal load_func_out = new Signal(3);
					Signal load_func_bp = new Signal(3);
					Signal load_func_in = new Signal(3);
					Signal store_func_out = new Signal(2);
					Signal store_func_bp = new Signal(2);
					Signal store_func_in = new Signal(2);

					me.flipflops[10] = new DFlipFlop(me, "flipflop:10").setAll(clk, load_func_out, load_func_bp);
					me.flipflops[11] = new DFlipFlop(me, "flipflop:11").setAll(clk, load_func_bp, load_func_in);
					me.flipflops[12] = new DFlipFlop(me, "flipflop:12").setAll(clk, store_func_out, store_func_bp);
					me.flipflops[13] = new DFlipFlop(me, "flipflop:13").setAll(clk, store_func_bp, store_func_in);

					// InstructionDecode to WriteBack
					Signal reg_write_sel_out = new Signal(1);
					Signal reg_write_sel_bp1 = new Signal(1);
					Signal reg_write_sel_bp2 = new Signal(1);
					Signal reg_write_sel_in = new Signal(1);
					Signal reg_write_sel_en_out = new Signal(1);
					Signal reg_write_sel_en_bp1 = new Signal(1);
					Signal reg_write_sel_en_bp2 = new Signal(1);
					Signal reg_write_sel_en_in = new Signal(1);
					Signal a3_out = new Signal(5);
					Signal a3_alu_in = new Signal(5);
					Signal a3_bp = new Signal(5);
					Signal a3_wb_in = new Signal(5);

					me.flipflops[14] = new DFlipFlop(me, "flipflop:14").setAll(clk, reg_write_sel_out, reg_write_sel_bp1);
					me.flipflops[15] = new DFlipFlop(me, "flipflop:15").setAll(clk, reg_write_sel_bp1, reg_write_sel_bp2);
					me.flipflops[16] = new DFlipFlop(me, "flipflop:16").setAll(clk, reg_write_sel_bp2, reg_write_sel_in);
					me.flipflops[17] = new DFlipFlop(me, "flipflop:17").setAll(clk, reg_write_sel_en_out, reg_write_sel_en_bp1);
					me.flipflops[18] = new DFlipFlop(me, "flipflop:18").setAll(clk, reg_write_sel_en_bp1, reg_write_sel_en_bp2);
					me.flipflops[19] = new DFlipFlop(me, "flipflop:19").setAll(clk, reg_write_sel_en_bp2, reg_write_sel_en_in);
					me.flipflops[20] = new DFlipFlop(me, "flipflop:20").setAll(clk, a3_out, a3_alu_in);
					me.flipflops[21] = new DFlipFlop(me, "flipflop:21").setAll(clk, a3_alu_in, a3_bp);
					me.flipflops[22] = new DFlipFlop(me, "flipflop:22").setAll(clk, a3_bp, a3_wb_in);

					// InstructionDecode to RegisterFile
					Signal r31_write_id = new Signal(32);
					Signal r31_write_en_id = new Signal(1);

					// Executor to MemoryAccess
					Signal alu_res_out = new Signal(32);
					Signal alu_res_in = new Signal(32);
					Signal rs_out = new Signal(32);
					Signal rs_in = new Signal(32);

					me.flipflops[23] = new DFlipFlop(me, "flipflop:23").setAll(clk, alu_res_out, alu_res_in);
					me.flipflops[24] = new DFlipFlop(me, "flipflop:24").setAll(clk, rs_out, rs_in);

					// Executor to WriteBack
					Signal alu_res_wb_in = new Signal(32);

					me.flipflops[25] = new DFlipFlop(me, "flipflop:25").setAll(clk, alu_res_in, alu_res_wb_in);

					// MemoryAccess to WriteBack
					Signal load_out = new Signal(32);
					Signal load_in = new Signal(32);

					me.flipflops[26] = new DFlipFlop(me, "flipflop:26").setAll(clk, load_out, load_in);

					// WriteBack to RegisterFile
					Signal[] reg_write = new Signal[32];
					Signal[] reg_write_en = new Signal[32];
					Signal[] reg_write_in = new Signal[31];
					Signal[] reg_write_en_in = new Signal[31];
					for(int i = 0; i < 32; ++i)
					{
						reg_write[i] = new Signal(32);
						reg_write_en[i] = new Signal(1);

						if(i != 31)
						{
							reg_write_in[i] = reg_write[i];
							reg_write_en_in[i] = reg_write_en[i];
						}
					}

					// InstructionDecode to InstructionFetch
					Signal pcoe = new Signal(1);
					Signal pco = new Signal(32);

					// WriteBack to Executor
					Signal alu_afw = new Signal(5);
					Signal alu_rfw = new Signal(32);

					// RegisterFile to InstructionDecode
					Signal[] reg_read = new Signal[32];
					for(int i = 0; i < 32; ++i)
						reg_read[i] = new Signal(32);

					InstructionFetch fetch = new InstructionFetch(me, "instruction-fetch").setAll(clk, pcoe, pco, ir_out, pc_out, (Integer)me.get("ims"));
					new InstructionDecode(me, "instruction-decode").setAll(ir_in, pc_in, reg_read, pco, pcoe, r31_write_id, r31_write_en_id, opcode_out, alu_imm_out, alu_fw_out, r1_out, r2_out, a1_out, a2_out, a3_out, imm_out, load_func_out, store_func_out, reg_write_sel_out, reg_write_sel_en_out, with_fpu);
					new Executor(me, "executor").setAll(clk, r1_in, r2_in, a1_in, a2_in, a3_alu_in, imm_in, opcode_in, alu_imm_in, alu_fw_in, alu_afw, alu_rfw, a2_in, r2_in, rs_out, alu_res_out, with_fpu);
					new MemoryAccess(me, "memory-access").setAll(alu_res_in, load_func_in, store_func_in, rs_in, mem_read, clk, load_out, mem_write, mem_write_en, mem_clk);
					new WriteBack(me, "write-back").setAll(alu_res_wb_in, load_in, reg_write_sel_in, reg_write_sel_en_in, a3_wb_in, alu_afw, alu_rfw, reg_write, reg_write_en);
					RegisterFile register = new RegisterFile(me, "register-file").setAll(clk, reg_write_in, reg_write_en_in, new Signal[] {r31_write_id, reg_write[31]}, new Signal[] {r31_write_en_id, reg_write_en[31]}, reg_read);

					me.addGroup("instruction fetch", fetch);
					me.addGroup("register file", register);
				}
			},
			"clk[1], mem_read[n:4-][8]",
			"mem_write[n][8], mem_write_en[n][1], mem_clk[1]",
			"int ims, boolean with_fpu",
			""
		)
	);

	private DFlipFlop[] flipflops;

	private Processor(DeserializingStream in) throws IOException, DeserializingException, InstantiationException
	{
		super(in);

		flipflops = in.readObject(DFlipFlop[].class, DFlipFlop.class);
	}

	@Override public JComponent getGUI()
	{
		return new gui.EditorGUI(((GroupSetable)getSetableGroup("instruction fetch")).getSetableGroup("instruction memory"));
	}
}
