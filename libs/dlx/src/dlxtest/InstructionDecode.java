
package dlxtest;

import core.signal.Bit;
import core.signal.Signal;

import core.misc.test.TestSuite;
import core.misc.test.Testable;

import java.util.LinkedHashMap;
import java.util.Map;
import static core.misc.BitConverter.*;
import static core.signal.Bit.*;

public class InstructionDecode implements Testable
{
	@Override public final void test(TestSuite testsuite)
	{
		testsuite.createEnvironment();

		Signal ir = new Signal(32);
		Signal pc = new Signal(32);
		Signal[] reg_read = new Signal[32];
		for(int i = 0; i < 32; ++i)
			reg_read[i] = new Signal(32);
		Signal pco = new Signal(32);
		Signal pcoe = new Signal(1);
		Signal r31_write = new Signal(32);
		Signal r31_write_en = new Signal(1);
		Signal opcode = new Signal(6);
		Signal alu_imm = new Signal(1);
		Signal alu_fw = new Signal(1);
		Signal r1 = new Signal(32);
		Signal r2 = new Signal(32);
		Signal a1 = new Signal(5);
		Signal a2 = new Signal(5);
		Signal a3 = new Signal(5);
		Signal imm = new Signal(32);
		Signal load = new Signal(3);
		Signal store = new Signal(2);
		Signal write = new Signal(1);
		Signal write_en = new Signal(1);

		testsuite.addComponent(dlx.InstructionDecode.class).setAll(ir, pc, reg_read, pco, pcoe, r31_write, r31_write_en, opcode, alu_imm, alu_fw, r1, r2, a1, a2, a3, imm, load, store, write, write_en, true);

		testsuite.buildEnvironment();

		Bit[] opcode_zero = new Bit[] {L, L, L, L, L, L};
		Bit[] load_zero = new Bit[] {L, L, L};
		Bit[] store_zero = new Bit[] {L, L};
		Bit[] adr_zero = new Bit[] {L, L, L, L, L};

		Map<Bit[], Bit[]> load_map = new LinkedHashMap<Bit[], Bit[]>();
		Map<Bit[], Bit[]> store_map = new LinkedHashMap<Bit[], Bit[]>();
		Map<Bit[], Bit[]> br_map = new LinkedHashMap<Bit[], Bit[]>();
		Map<Bit[], Bit[]> j_map = new LinkedHashMap<Bit[], Bit[]>();
		Map<Bit[], Bit[]> jr_map = new LinkedHashMap<Bit[], Bit[]>();
		Map<Bit[], Bit[]> alui_map = new LinkedHashMap<Bit[], Bit[]>();
		Map<Bit[], Bit[]> spez_map = new LinkedHashMap<Bit[], Bit[]>();
		Map<Bit[], Bit[]> func_map = new LinkedHashMap<Bit[], Bit[]>();

		/*LB*/		load_map.put(new Bit[] {H, L, L, L, L, L}, new Bit[] {L, L, L, L, L, L, H, L,  L, L, L,  L, L,  H, H,  L,  L, L, L, L,  L, L});
		/*LBU*/		load_map.put(new Bit[] {L, H, L, L, L, L}, new Bit[] {L, L, L, L, L, L, H, L,  H, L, L,  L, L,  H, H,  L,  L, L, L, L,  L, L});
		/*LH*/		load_map.put(new Bit[] {L, L, H, L, L, L}, new Bit[] {L, L, L, L, L, L, H, L,  L, H, L,  L, L,  H, H,  L,  L, L, L, L,  L, L});
		/*LHU*/		load_map.put(new Bit[] {H, L, H, L, L, L}, new Bit[] {L, L, L, L, L, L, H, L,  H, H, L,  L, L,  H, H,  L,  L, L, L, L,  L, L});
		/*LW*/		load_map.put(new Bit[] {H, H, H, L, L, L}, new Bit[] {L, L, L, L, L, L, H, L,  L, L, H,  L, L,  H, H,  L,  L, L, L, L,  L, L});

		/*SB*/		store_map.put(new Bit[] {H, H, L, L, L, L}, new Bit[] {L, L, L, L, L, L, H, L,  L, L, L,  H, L,  L, L,  L,  L, L, L, L,  L, L});
		/*SH*/		store_map.put(new Bit[] {L, H, H, L, L, L}, new Bit[] {L, L, L, L, L, L, H, L,  L, L, L,  L, H,  L, L,  L,  L, L, L, L,  L, L});
		/*SW*/		store_map.put(new Bit[] {L, L, L, H, L, L}, new Bit[] {L, L, L, L, L, L, H, L,  L, L, L,  H, H,  L, L,  L,  L, L, L, L,  L, L});

		/*BEQZ*/	br_map.put(new Bit[] {H, L, L, H, L, L}, new Bit[] {L, L, L, L, L, L, L, L,  L, L, L,  L, L,  L, L,  L,  H, L, L, H,  L, L});
		/*BNEZ*/	br_map.put(new Bit[] {L, H, L, H, L, L}, new Bit[] {L, L, L, L, L, L, L, L,  L, L, L,  L, L,  L, L,  L,  H, L, L, H,  H, L});

		/*J*/		j_map.put(new Bit[] {H, H, L, H, L, L}, new Bit[] {L, L, L, L, L, L, L, L,  L, L, L,  L, L,  L, L,  L,  L, L, H, L,  L, L});
		/*JAL*/		j_map.put(new Bit[] {H, L, H, H, L, L}, new Bit[] {L, L, L, L, L, L, L, L,  L, L, L,  L, L,  L, L,  L,  L, L, H, L,  L, H});

		/*JR*/		jr_map.put(new Bit[] {L, L, H, H, L, L}, new Bit[] {L, L, L, L, L, L, L, L,  L, L, L,  L, L,  L, L,  L,  L, H, H, L,  L, L});
		/*JALR*/	jr_map.put(new Bit[] {L, H, H, H, L, L}, new Bit[] {L, L, L, L, L, L, L, L,  L, L, L,  L, L,  L, L,  L,  L, H, H, L,  L, H});

		/*ADDI*/	alui_map.put(new Bit[] {H, H, H, H, L, L}, new Bit[] {L, L, L, L, L, L, H, H,  L, L, L,  L, L,  L, H,  L,  L, L, L, L,  L, L});
		/*ADDUI*/	alui_map.put(new Bit[] {L, L, L, L, H, L}, new Bit[] {L, L, L, L, L, L, H, H,  L, L, L,  L, L,  L, H,  H,  L, L, L, L,  L, L});
		/*SUBI*/	alui_map.put(new Bit[] {H, L, L, L, H, L}, new Bit[] {H, L, L, L, L, L, H, H,  L, L, L,  L, L,  L, H,  L,  L, L, L, L,  L, L});
		/*SUBUI*/	alui_map.put(new Bit[] {L, H, L, L, H, L}, new Bit[] {H, L, L, L, L, L, H, H,  L, L, L,  L, L,  L, H,  H,  L, L, L, L,  L, L});
		/*ANDI*/	alui_map.put(new Bit[] {H, H, L, L, H, L}, new Bit[] {L, H, H, L, L, L, H, H,  L, L, L,  L, L,  L, H,  H,  L, L, L, L,  L, L});
		/*ORI*/		alui_map.put(new Bit[] {L, L, H, L, H, L}, new Bit[] {H, H, H, L, L, L, H, H,  L, L, L,  L, L,  L, H,  H,  L, L, L, L,  L, L});
		/*XORI*/	alui_map.put(new Bit[] {H, L, H, L, H, L}, new Bit[] {L, L, L, H, L, L, H, H,  L, L, L,  L, L,  L, H,  H,  L, L, L, L,  L, L});
		/*SLLI*/	alui_map.put(new Bit[] {H, H, H, L, H, L}, new Bit[] {H, L, L, H, L, L, H, H,  L, L, L,  L, L,  L, H,  L,  L, L, L, L,  L, L});
		/*SRLI*/	alui_map.put(new Bit[] {L, L, L, H, H, L}, new Bit[] {L, H, L, H, L, L, H, H,  L, L, L,  L, L,  L, H,  L,  L, L, L, L,  L, L});
		/*SRAI*/	alui_map.put(new Bit[] {H, L, L, H, H, L}, new Bit[] {H, H, L, H, L, L, H, H,  L, L, L,  L, L,  L, H,  L,  L, L, L, L,  L, L});
		/*SLTI*/	alui_map.put(new Bit[] {L, H, L, H, H, L}, new Bit[] {L, L, H, H, L, L, H, H,  L, L, L,  L, L,  L, H,  L,  L, L, L, L,  L, L});
		/*SGTI*/	alui_map.put(new Bit[] {H, H, L, H, H, L}, new Bit[] {H, L, H, H, L, L, H, H,  L, L, L,  L, L,  L, H,  L,  L, L, L, L,  L, L});
		/*SLEI*/	alui_map.put(new Bit[] {L, L, H, H, H, L}, new Bit[] {L, H, H, H, L, L, H, H,  L, L, L,  L, L,  L, H,  L,  L, L, L, L,  L, L});
		/*SGEI*/	alui_map.put(new Bit[] {H, L, H, H, H, L}, new Bit[] {H, H, H, H, L, L, H, H,  L, L, L,  L, L,  L, H,  L,  L, L, L, L,  L, L});
		/*SEQI*/	alui_map.put(new Bit[] {L, H, H, H, H, L}, new Bit[] {L, L, L, L, H, L, H, H,  L, L, L,  L, L,  L, H,  L,  L, L, L, L,  L, L});
		/*SNEI*/	alui_map.put(new Bit[] {H, H, H, H, H, L}, new Bit[] {H, L, L, L, H, L, H, H,  L, L, L,  L, L,  L, H,  L,  L, L, L, L,  L, L});

		/*LHI*/		spez_map.put(new Bit[] {L, H, H, L, H, L}, new Bit[] {L, H, L, L, H, L, H, H,  L, L, L,  L, L,  L, H,  L,  L, L, L, L,  L, L});

		/*ADD*/		func_map.put(new Bit[] {L, L, L, L, L, L, L, L, L, L, L}, new Bit[] {L, L, L, L, L, L, L, H,  L, L, L,  L, L,  L, H,  L,  L, L, L, L,  L, L});
		/*SUB*/		func_map.put(new Bit[] {H, L, L, L, L, L, L, L, L, L, L}, new Bit[] {H, L, L, L, L, L, L, H,  L, L, L,  L, L,  L, H,  L,  L, L, L, L,  L, L});
		/*MULT*/	func_map.put(new Bit[] {L, H, L, L, L, L, L, L, L, L, L}, new Bit[] {L, H, L, L, L, L, L, H,  L, L, L,  L, L,  L, H,  L,  L, L, L, L,  L, L});
		/*MULTU*/	func_map.put(new Bit[] {H, H, L, L, L, L, L, L, L, L, L}, new Bit[] {H, H, L, L, L, L, L, H,  L, L, L,  L, L,  L, H,  L,  L, L, L, L,  L, L});
		/*DIV*/		func_map.put(new Bit[] {L, L, H, L, L, L, L, L, L, L, L}, new Bit[] {L, L, H, L, L, L, L, H,  L, L, L,  L, L,  L, H,  L,  L, L, L, L,  L, L});
		/*DIVU*/	func_map.put(new Bit[] {H, L, H, L, L, L, L, L, L, L, L}, new Bit[] {H, L, H, L, L, L, L, H,  L, L, L,  L, L,  L, H,  L,  L, L, L, L,  L, L});
		/*AND*/		func_map.put(new Bit[] {L, H, H, L, L, L, L, L, L, L, L}, new Bit[] {L, H, H, L, L, L, L, H,  L, L, L,  L, L,  L, H,  L,  L, L, L, L,  L, L});
		/*OR*/		func_map.put(new Bit[] {H, H, H, L, L, L, L, L, L, L, L}, new Bit[] {H, H, H, L, L, L, L, H,  L, L, L,  L, L,  L, H,  L,  L, L, L, L,  L, L});
		/*XOR*/		func_map.put(new Bit[] {L, L, L, H, L, L, L, L, L, L, L}, new Bit[] {L, L, L, H, L, L, L, H,  L, L, L,  L, L,  L, H,  L,  L, L, L, L,  L, L});
		/*SLL*/		func_map.put(new Bit[] {H, L, L, H, L, L, L, L, L, L, L}, new Bit[] {H, L, L, H, L, L, L, H,  L, L, L,  L, L,  L, H,  L,  L, L, L, L,  L, L});
		/*SRL*/		func_map.put(new Bit[] {L, H, L, H, L, L, L, L, L, L, L}, new Bit[] {L, H, L, H, L, L, L, H,  L, L, L,  L, L,  L, H,  L,  L, L, L, L,  L, L});
		/*SRA*/		func_map.put(new Bit[] {H, H, L, H, L, L, L, L, L, L, L}, new Bit[] {H, H, L, H, L, L, L, H,  L, L, L,  L, L,  L, H,  L,  L, L, L, L,  L, L});
		/*SLT*/		func_map.put(new Bit[] {L, L, H, H, L, L, L, L, L, L, L}, new Bit[] {L, L, H, H, L, L, L, H,  L, L, L,  L, L,  L, H,  L,  L, L, L, L,  L, L});
		/*SGT*/		func_map.put(new Bit[] {H, L, H, H, L, L, L, L, L, L, L}, new Bit[] {H, L, H, H, L, L, L, H,  L, L, L,  L, L,  L, H,  L,  L, L, L, L,  L, L});
		/*SLE*/		func_map.put(new Bit[] {L, H, H, H, L, L, L, L, L, L, L}, new Bit[] {L, H, H, H, L, L, L, H,  L, L, L,  L, L,  L, H,  L,  L, L, L, L,  L, L});
		/*SGE*/		func_map.put(new Bit[] {H, H, H, H, L, L, L, L, L, L, L}, new Bit[] {H, H, H, H, L, L, L, H,  L, L, L,  L, L,  L, H,  L,  L, L, L, L,  L, L});
		/*SEQ*/		func_map.put(new Bit[] {L, L, L, L, H, L, L, L, L, L, L}, new Bit[] {L, L, L, L, H, L, L, H,  L, L, L,  L, L,  L, H,  L,  L, L, L, L,  L, L});
		/*SNE*/		func_map.put(new Bit[] {H, L, L, L, H, L, L, L, L, L, L}, new Bit[] {H, L, L, L, H, L, L, H,  L, L, L,  L, L,  L, H,  L,  L, L, L, L,  L, L});

		/*FADD*/	func_map.put(new Bit[] {L, L, L, L, L, H, L, L, L, L, L}, new Bit[] {L, L, L, L, L, H, L, H,  L, L, L,  L, L,  L, H,  L,  L, L, L, L,  L, L});
		/*FSUB*/	func_map.put(new Bit[] {H, L, L, L, L, H, L, L, L, L, L}, new Bit[] {H, L, L, L, L, H, L, H,  L, L, L,  L, L,  L, H,  L,  L, L, L, L,  L, L});
		/*FMULT*/	func_map.put(new Bit[] {L, H, L, L, L, H, L, L, L, L, L}, new Bit[] {L, H, L, L, L, H, L, H,  L, L, L,  L, L,  L, H,  L,  L, L, L, L,  L, L});
		/*FDIV*/	func_map.put(new Bit[] {H, H, L, L, L, H, L, L, L, L, L}, new Bit[] {L, L, H, L, L, H, L, H,  L, L, L,  L, L,  L, H,  L,  L, L, L, L,  L, L});
		/*FSLT*/	func_map.put(new Bit[] {L, L, H, L, L, H, L, L, L, L, L}, new Bit[] {H, H, H, L, L, H, L, H,  L, L, L,  L, L,  L, H,  L,  L, L, L, L,  L, L});
		/*FSGT*/	func_map.put(new Bit[] {H, L, H, L, L, H, L, L, L, L, L}, new Bit[] {H, L, L, H, L, H, L, H,  L, L, L,  L, L,  L, H,  L,  L, L, L, L,  L, L});
		/*FSLE*/	func_map.put(new Bit[] {L, H, H, L, L, H, L, L, L, L, L}, new Bit[] {H, H, L, H, L, H, L, H,  L, L, L,  L, L,  L, H,  L,  L, L, L, L,  L, L});
		/*FSGE*/	func_map.put(new Bit[] {H, H, H, L, L, H, L, L, L, L, L}, new Bit[] {H, L, H, H, L, H, L, H,  L, L, L,  L, L,  L, H,  L,  L, L, L, L,  L, L});
		/*FSEQ*/	func_map.put(new Bit[] {L, L, L, H, L, H, L, L, L, L, L}, new Bit[] {H, H, H, H, L, H, L, H,  L, L, L,  L, L,  L, H,  L,  L, L, L, L,  L, L});
		/*FSNE*/	func_map.put(new Bit[] {H, L, L, H, L, H, L, L, L, L, L}, new Bit[] {H, L, L, L, H, H, L, H,  L, L, L,  L, L,  L, H,  L,  L, L, L, L,  L, L});
		/*FSINF*/	func_map.put(new Bit[] {L, H, L, H, L, H, L, L, L, L, L}, new Bit[] {L, H, L, L, H, H, L, H,  L, L, L,  L, L,  L, H,  L,  L, L, L, L,  L, L});
		/*FCITF*/	func_map.put(new Bit[] {H, H, L, H, L, H, L, L, L, L, L}, new Bit[] {L, L, H, L, H, H, L, H,  L, L, L,  L, L,  L, H,  L,  L, L, L, L,  L, L});
		/*FCFTI*/	func_map.put(new Bit[] {L, L, H, H, L, H, L, L, L, L, L}, new Bit[] {L, H, H, L, H, H, L, H,  L, L, L,  L, L,  L, H,  L,  L, L, L, L,  L, L});

		/* LOAD */
		Bit[] exp_load = new Bit[3];
		for(Map.Entry<Bit[], Bit[]> e : load_map.entrySet())
		{
			for(int i = 0; i < 6; ++i)
				ir.setBit(i, e.getKey()[i]);

			for(int i = 0; i < 1000; ++i)
			{
				testsuite.predictableRandomSignal(ir, 6, 26);
				testsuite.predictableRandomSignal(pc);
				for(int j = 0; j < 32; ++j)
					testsuite.predictableRandomSignal(reg_read[j]);

				testsuite.doSimulation();

				testsuite.assertEquals("Load: Program Counter Overwrite Enable Check", L, pcoe.getBit(0));
				testsuite.assertEquals("Load: Register 31 Write Enable Check", L, r31_write_en.getBit(0));
				testsuite.assertEquals("Load: Opcode Check", opcode_zero, opcode);
				testsuite.assertEquals("Load: ALU Use Immediate Check", H, alu_imm.getBit(0));
				testsuite.assertEquals("Load: ALU Forward Check", L, alu_fw.getBit(0));
				testsuite.assertEquals("Load: Register 1 Check", reg_read[(int)signalToLong(ir.get(6, 5))], r1);
				testsuite.assertEquals("Load: Register 2 Check", reg_read[(int)signalToLong(ir.get(11, 5))], r2);
				testsuite.assertEquals("Load: Address 1 Check", ir.get(6, 5), a1);
				testsuite.assertEquals("Load: Address 2 Check", ir.get(11, 5), a2);
				testsuite.assertEquals("Load: Address 3 Check", ir.get(11, 5), a3);
				testsuite.assertSignedEquals("Load: Immediate Check", ir.get(16, 16), imm);
				for(int j = 0; j < 3; ++j)
					exp_load[j] = e.getValue()[8 + j];
				testsuite.assertEquals("Load: Load Check", exp_load, load);
				testsuite.assertEquals("Load: Store Check", store_zero, store);
				testsuite.assertEquals("Load: Register Write Check", H, write.getBit(0));
				testsuite.assertEquals("Load: Register Write Enable Check", H, write_en.getBit(0));
			}
		}

		/* STORE */
		Bit[] exp_store = new Bit[2];
		for(Map.Entry<Bit[], Bit[]> e : store_map.entrySet())
		{
			for(int i = 0; i < 6; ++i)
				ir.setBit(i, e.getKey()[i]);

			for(int i = 0; i < 1000; ++i)
			{
				testsuite.predictableRandomSignal(ir, 6, 26);
				testsuite.predictableRandomSignal(pc);
				for(int j = 0; j < 32; ++j)
					testsuite.predictableRandomSignal(reg_read[j]);

				testsuite.doSimulation();

				testsuite.assertEquals("Store: Program Counter Overwrite Enable Check", L, pcoe.getBit(0));
				testsuite.assertEquals("Store: Register 31 Write Enable Check", L, r31_write_en.getBit(0));
				testsuite.assertEquals("Store: Opcode Check", opcode_zero, opcode);
				testsuite.assertEquals("Store: ALU Use Immediate Check", H, alu_imm.getBit(0));
				testsuite.assertEquals("Store: ALU Forward Check", L, alu_fw.getBit(0));
				testsuite.assertEquals("Store: Register 1 Check", reg_read[(int)signalToLong(ir.get(6, 5))], r1);
				testsuite.assertEquals("Store: Register 2 Check", reg_read[(int)signalToLong(ir.get(11, 5))], r2);
				testsuite.assertEquals("Store: Address 1 Check", ir.get(6, 5), a1);
				testsuite.assertEquals("Store: Address 2 Check", ir.get(11, 5), a2);
				testsuite.assertEquals("Store: Address 3 Check", ir.get(11, 5), a3);
				testsuite.assertSignedEquals("Store: Immediate Check", ir.get(16, 16), imm);
				testsuite.assertEquals("Store: Load Check", load_zero, load);
				for(int j = 0; j < 2; ++j)
					exp_store[j] = e.getValue()[11 + j];
				testsuite.assertEquals("Store: Store Check", exp_store, store);
				testsuite.assertEquals("Store: Register Write Enable Check", L, write_en.getBit(0));
			}
		}

		/* BRANCH */
		for(Map.Entry<Bit[], Bit[]> e : br_map.entrySet())
		{
			for(int i = 0; i < 6; ++i)
				ir.setBit(i, e.getKey()[i]);

			for(int i = 0; i < 1000; ++i)
			{
				testsuite.predictableRandomSignal(ir, 6, 26);
				testsuite.predictableRandomSignal(pc);
				for(int j = 0; j < 32; ++j)
				{
					testsuite.predictableRandomSignal(reg_read[j], 0, 1);
					for(int k = 1; k < 32; ++k)
						reg_read[j].setBit(k, L);
				}

				testsuite.doSimulation();

				if(e.getValue()[20] == L && signalToLong(reg_read[(int)signalToLong(ir.get(6, 5))]) == 0 || e.getValue()[20] == H && signalToLong(reg_read[(int)signalToLong(ir.get(6, 5))]) != 0)
				{
					testsuite.assertEquals("Branch: Program Counter Overwrite Enable Check", H, pcoe.getBit(0));
					testsuite.assertEquals("Branch: Program Counter Overwrite Check", signalToLong(pc) + signalToLong(true, ir.get(16, 16)), pco);
				}
				else
					testsuite.assertEquals("Branch: Program Counter Overwrite Enable Check", L, pcoe.getBit(0));
				testsuite.assertEquals("Branch: Register 31 Write Enable Check", L, r31_write_en.getBit(0));
				testsuite.assertEquals("Branch: Opcode Check", opcode_zero, opcode);
				testsuite.assertEquals("Branch: ALU Use Immediate Check", L, alu_imm.getBit(0));
				testsuite.assertEquals("Branch: Address 3 Check", adr_zero, a3);
				testsuite.assertEquals("Branch: Load Check", load_zero, load);
				testsuite.assertEquals("Branch: Store Check", store_zero, store);
			}
		}

		/* JUMP */
		for(Map.Entry<Bit[], Bit[]> e : j_map.entrySet())
		{
			for(int i = 0; i < 6; ++i)
				ir.setBit(i, e.getKey()[i]);

			for(int i = 0; i < 1000; ++i)
			{
				testsuite.predictableRandomSignal(ir, 6, 26);
				testsuite.predictableRandomSignal(pc);
				for(int j = 0; j < 32; ++j)
					testsuite.predictableRandomSignal(reg_read[j]);

				testsuite.doSimulation();

				testsuite.assertEquals("Jump: Program Counter Overwrite Enable Check", H, pcoe.getBit(0));
				testsuite.assertEquals("Jump: Program Counter Overwrite Check", (int)(signalToLong(pc) + signalToLong(true, ir.get(6, 26))), pco);
				if(e.getValue()[21] == H)
				{
					testsuite.assertEquals("Jump: Register 31 Write Enable Check", H, r31_write_en.getBit(0));
					testsuite.assertEquals("Jump: Register 31 Write Check", (int)(signalToLong(pc) + 2), r31_write);
				}
				else
					testsuite.assertEquals("Jump: Register 31 Write Enable Check", L, r31_write_en.getBit(0));
				testsuite.assertEquals("Jump: Opcode Check", opcode_zero, opcode);
				testsuite.assertEquals("Jump: ALU Use Immediate Check", L, alu_imm.getBit(0));
				testsuite.assertEquals("Jump: Address 3 Check", adr_zero, a3);
				testsuite.assertEquals("Jump: Load Check", load_zero, load);
				testsuite.assertEquals("Jump: Store Check", store_zero, store);
			}
		}

		/* JUMP REGISTER */
		for(Map.Entry<Bit[], Bit[]> e : jr_map.entrySet())
		{
			for(int i = 0; i < 6; ++i)
				ir.setBit(i, e.getKey()[i]);

			for(int i = 0; i < 1000; ++i)
			{
				testsuite.predictableRandomSignal(ir, 6, 26);
				testsuite.predictableRandomSignal(pc);
				for(int j = 0; j < 32; ++j)
					testsuite.predictableRandomSignal(reg_read[j]);

				testsuite.doSimulation();

				testsuite.assertEquals("Jump Register: Program Counter Overwrite Enable Check", H, pcoe.getBit(0));
				testsuite.assertEquals("Jump Register: Program Counter Overwrite Check", reg_read[(int)signalToLong(ir.get(6, 5))], pco);
				if(e.getValue()[21] == H)
				{
					testsuite.assertEquals("Jump Register: Register 31 Write Enable Check", H, r31_write_en.getBit(0));
					testsuite.assertEquals("Jump Register: Register 31 Write Check", (int)(signalToLong(pc) + 2), r31_write);
				}
				else
					testsuite.assertEquals("Jump Register: Register 31 Write Enable Check", L, r31_write_en.getBit(0));
				testsuite.assertEquals("Jump Register: Opcode Check", opcode_zero, opcode);
				testsuite.assertEquals("Jump Register: ALU Use Immediate Check", L, alu_imm.getBit(0));
				testsuite.assertEquals("Jump Register: Address 3 Check", adr_zero, a3);
				testsuite.assertEquals("Jump Register: Load Check", load_zero, load);
				testsuite.assertEquals("Jump Register: Store Check", store_zero, store);
			}
		}

		/* ALUI */
		Bit[] exp_opcode = new Bit[6];
		for(Map.Entry<Bit[], Bit[]> e : alui_map.entrySet())
		{
			for(int i = 0; i < 6; ++i)
				ir.setBit(i, e.getKey()[i]);

			for(int i = 0; i < 1000; ++i)
			{
				testsuite.predictableRandomSignal(ir, 6, 26);
				testsuite.predictableRandomSignal(pc);
				for(int j = 0; j < 32; ++j)
					testsuite.predictableRandomSignal(reg_read[j]);

				testsuite.doSimulation();

				testsuite.assertEquals("ALUI: Program Counter Overwrite Enable Check", L, pcoe.getBit(0));
				testsuite.assertEquals("ALUI: Register 31 Write Enable Check", L, r31_write_en.getBit(0));
				for(int j = 0; j < 6; ++j)
					exp_opcode[j] = e.getValue()[j];
				testsuite.assertEquals("ALUI: Opcode Check", exp_opcode, opcode);
				testsuite.assertEquals("ALUI: ALU Use Immediate Check", H, alu_imm.getBit(0));
				testsuite.assertEquals("ALUI: ALU Forward Check", H, alu_fw.getBit(0));
				testsuite.assertEquals("ALUI: Register 1 Check", reg_read[(int)signalToLong(ir.get(6, 5))], r1);
				testsuite.assertEquals("ALUI: Address 1 Check", ir.get(6, 5), a1);
				testsuite.assertEquals("ALUI: Address 3 Check", ir.get(11, 5), a3);
				if(e.getValue()[15] == L)
					testsuite.assertSignedEquals("ALUI: Immediate Check", ir.get(16, 16), imm);
				else
					testsuite.assertEquals("ALUI: Immediate Check", ir.get(16, 16), imm);
				testsuite.assertEquals("ALUI: Load Check", load_zero, load);
				testsuite.assertEquals("ALUI: Store Check", store_zero, store);
				testsuite.assertEquals("ALUI: Register Write Check", L, write.getBit(0));
				testsuite.assertEquals("ALUI: Register Write Enable Check", H, write_en.getBit(0));
			}
		}

		/* SPEZ */
		for(Map.Entry<Bit[], Bit[]> e : spez_map.entrySet())
		{
			for(int i = 0; i < 6; ++i)
				ir.setBit(i, e.getKey()[i]);

			for(int i = 0; i < 1000; ++i)
			{
				testsuite.predictableRandomSignal(ir, 6, 26);
				testsuite.predictableRandomSignal(pc);
				for(int j = 0; j < 32; ++j)
					testsuite.predictableRandomSignal(reg_read[j]);

				testsuite.doSimulation();

				testsuite.assertEquals("Spez: Program Counter Overwrite Enable Check", L, pcoe.getBit(0));
				testsuite.assertEquals("Spez: Register 31 Write Enable Check", L, r31_write_en.getBit(0));
				for(int j = 0; j < 6; ++j)
					exp_opcode[j] = e.getValue()[j];
				testsuite.assertEquals("Spez: Opcode Check", exp_opcode, opcode);
				testsuite.assertEquals("Spez: ALU Use Immediate Check", H, alu_imm.getBit(0));
				testsuite.assertEquals("Spez: ALU Forward Check", H, alu_fw.getBit(0));
				testsuite.assertEquals("Spez: Address 3 Check", ir.get(11, 5), a3);
				testsuite.assertEquals("Spez: Immediate Check", ir.get(16, 16), imm.get(0, 16));
				testsuite.assertEquals("Spez: Load Check", load_zero, load);
				testsuite.assertEquals("Spez: Store Check", store_zero, store);
				testsuite.assertEquals("Spez: Register Write Check", L, write.getBit(0));
				testsuite.assertEquals("Spez: Register Write Enable Check", H, write_en.getBit(0));
			}
		}

		/* FUNC */
		for(Map.Entry<Bit[], Bit[]> e : func_map.entrySet())
		{
			for(int i = 0; i < 6; ++i)
				ir.setBit(i, L);
			for(int i = 0; i < 11; ++i)
				ir.setBit(21 + i, e.getKey()[i]);

			for(int i = 0; i < 1000; ++i)
			{
				testsuite.predictableRandomSignal(ir, 6, 15);
				testsuite.predictableRandomSignal(pc);
				for(int j = 0; j < 32; ++j)
					testsuite.predictableRandomSignal(reg_read[j]);

				testsuite.doSimulation();

				testsuite.assertEquals("ALU: Program Counter Overwrite Enable Check", L, pcoe.getBit(0));
				testsuite.assertEquals("ALU: Register 31 Write Enable Check", L, r31_write_en.getBit(0));
				for(int j = 0; j < 6; ++j)
					exp_opcode[j] = e.getValue()[j];
				testsuite.assertEquals("ALU: Opcode Check", exp_opcode, opcode);
				testsuite.assertEquals("ALU: ALU Use Immediate Check", L, alu_imm.getBit(0));
				testsuite.assertEquals("ALU: ALU Forward Check", H, alu_fw.getBit(0));
				testsuite.assertEquals("ALU: Register 1 Check", reg_read[(int)signalToLong(ir.get(6, 5))], r1);
				testsuite.assertEquals("ALU: Register 2 Check", reg_read[(int)signalToLong(ir.get(11, 5))], r2);
				testsuite.assertEquals("ALU: Address 1 Check", ir.get(6, 5), a1);
				testsuite.assertEquals("ALU: Address 2 Check", ir.get(11, 5), a2);
				testsuite.assertEquals("ALU: Address 3 Check", ir.get(16, 5), a3);
				testsuite.assertEquals("ALU: Load Check", load_zero, load);
				testsuite.assertEquals("ALU: Store Check", store_zero, store);
				testsuite.assertEquals("ALU: Register Write Check", L, write.getBit(0));
				testsuite.assertEquals("ALU: Register Write Enable Check", H, write_en.getBit(0));
			}
		}
	}
}
