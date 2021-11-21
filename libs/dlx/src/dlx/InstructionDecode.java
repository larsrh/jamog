
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
import std.alu.*;
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
	name		= "Instruction Decode",
	description	= "The instruction decode component of the DLX pipeline."
)
public final class InstructionDecode extends Composite
{
	public InstructionDecode(ComponentCollection parent,String name)
	{
		super(parent,name);
	}

	public final InstructionDecode setAll(Signal ir, Signal pc, Signal[] reg_read, Signal pco, Signal pcoe, Signal r31_write, Signal r31_write_en, Signal opcode, Signal alu_imm, Signal alu_fw, Signal r1, Signal r2, Signal a1, Signal a2, Signal a3, Signal imm, Signal load, Signal store, Signal write, Signal write_en, boolean with_fpu)
	{
		return (InstructionDecode)useAndSet(with_fpu ? "with_fpu" : Flavor.DEFAULT, new String[] {"ir", "pc", "reg_read", "pco", "pcoe", "r31_write", "r31_write_en", "opcode", "alu_imm", "alu_fw", "r1", "r2", "a1", "a2", "a3", "imm", "load", "store", "write", "write_en"}, ir, pc, reg_read, pco, pcoe, r31_write, r31_write_en, opcode, alu_imm, alu_fw, r1, r2, a1, a2, a3, imm, load, store, write, write_en);
	}

	@Override public final Map<String, Flavor> getFlavors()
	{
		return flavors;
	}

	private static final Map<String, Flavor> flavors = Flavor.getMap(
		new Flavor(
			new Flavor.Buildable<InstructionDecode>()
			{
				@Override public void build(InstructionDecode me, Map<String, Integer> variables)
				{
					me.build(false);
				}
			},
			"ir[32], pc[32], reg_read[32][32]",
			"pco[32], pcoe[1], r31_write[32], r31_write_en[1], opcode[5], alu_imm[1], alu_fw[1], r1[32], r2[32], a1[5], a2[5], a3[5], imm[32], load[3], store[2], write[1], write_en[1]"
		),
		new Flavor(
			"with_fpu",
			new Flavor.Buildable<InstructionDecode>()
			{
				@Override public void build(InstructionDecode me, Map<String, Integer> variables)
				{
					me.build(true);
				}
			},
			"ir[32], pc[32], reg_read[32][32]",
			"pco[32], pcoe[1], r31_write[32], r31_write_en[1], opcode[6], alu_imm[1], alu_fw[1], r1[32], r2[32], a1[5], a2[5], a3[5], imm[32], load[3], store[2], write[1], write_en[1]"
		)
	);

	private static final Map<Bit[], Bit[]> op_map;
	private static final Map<Bit[], Bit[]> func_map;
	private static final Map<Bit[], Bit[]> fpu_op_map;
	private static final Map<Bit[], Bit[]> fpu_func_map;

	static
	{
		/*
		 * 6 Bit Opcode Encode Table:
		 *
		 * LLLLLL -> Register-Register operation, use 11 Bit Function Table instead
		 *
		 * Data Transport
		 * HLLLLL -> LB
		 * LHLLLL -> LBU
		 * HHLLLL -> SB
		 * LLHLLL -> LH
		 * HLHLLL -> LHU
		 * LHHLLL -> SH
		 * HHHLLL -> LW
		 * LLLHLL -> SW
		 *
		 *
		 * Control
		 * HLLHLL -> BEQZ
		 * LHLHLL -> BNEZ
		 * HHLHLL -> J
		 * LLHHLL -> JR
		 * HLHHLL -> JAL
		 * LHHHLL -> JALR
		 *
		 *
		 * Arithmetic with Immediate:
		 * HHHHLL -> ADDI
		 * LLLLHL -> ADDUI
		 * HLLLHL -> SUBI
		 * LHLLHL -> SUBUI
		 * HHLLHL -> ANDI
		 * LLHLHL -> ORI
		 * HLHLHL -> XORI
		 * LHHLHL -> LHI
		 * HHHLHL -> SLLI
		 * LLLHHL -> SRLI
		 * HLLHHL -> SRAI
		 * LHLHHL -> SLTI
		 * HHLHHL -> SGTI
		 * LLHHHL -> SLEI
		 * HLHHHL -> SGEI
		 * LHHHHL -> SEQI
		 * HHHHHL -> SNEI
		 *
		 */

		/*
		 * 11 Bit Function Table:
		 *
		 * Arithmetic
		 * LLLLLLLLLLL -> ADD
		 * HLLLLLLLLLL -> SUB
		 * LHLLLLLLLLL -> MULT
		 * HHLLLLLLLLL -> MULTU
		 * LLHLLLLLLLL -> DIV
		 * HLHLLLLLLLL -> DIVU
		 * LHHLLLLLLLL -> AND
		 * HHHLLLLLLLL -> OR
		 * LLLHLLLLLLL -> XOR
		 * HLLHLLLLLLL -> SLL
		 * LHLHLLLLLLL -> SRL
		 * HHLHLLLLLLL -> SRA
		 * LLHHLLLLLLL -> SLT
		 * HLHHLLLLLLL -> SGT
		 * LHHHLLLLLLL -> SLE
		 * HHHHLLLLLLL -> SGE
		 * LLLLHLLLLLL -> SEQ
		 * HLLLHLLLLLL -> SNE
		 *
		 * FPU
		 * LLLLLHLLLLL -> FADD
		 * HLLLLHLLLLL -> FSUB
		 * LHLLLHLLLLL -> FMULT
		 * HHLLLHLLLLL -> FDIV
		 * LLHLLHLLLLL -> FSLT
		 * HLHLLHLLLLL -> FSGT
		 * LHHLLHLLLLL -> FSLE
		 * HHHLLHLLLLL -> FSGE
		 * LLLHLHLLLLL -> FSEQ
		 * HLLHLHLLLLL -> FSNE
		 * LHLHLHLLLLL -> FSIFN
		 * HHLHLHLLLLL -> FCITF
		 * LLHHLHLLLLL -> FCFTI
		 *
		 */

		/*
		 * 21/22 Bit Decode Table:
		 *
		 * 5/6 Bit ALU/FPU Operation:
		 *  LLLLL(L) -> ADD
		 *  HLLLL(L) -> SUB
		 *  LHLLL(L) -> MULT
		 *  HHLLL(L) -> MULTU
		 *  LLHLL(L) -> DIV
		 *  HLHLL(L) -> DIVU
		 *  LHHLL(L) -> AND
		 *  HHHLL(L) -> OR
		 *  LLLHL(L) -> XOR
		 *  HLLHL(L) -> SLL
		 *  LHLHL(L) -> SRL
		 *  HHLHL(L) -> SRA
		 *  LLHHL(L) -> SLT
		 *  HLHHL(L) -> SGT
		 *  LHHHL(L) -> SLE
		 *  HHHHL(L) -> SGE
		 *  LLLLH(L) -> SEQ
		 *  HLLLH(L) -> SNE
		 *	LHLLH(L) -> LH
		 *
		 *	LLLLLH -> FADD
		 *	HLLLLH -> FSUB
		 *	LHLLLH -> FMULT
		 *	LLHLLH -> FDIV
		 *	HHHLLH -> FSLT
		 *	HLLHLH -> FSGT
		 *	HHLHLH -> FSLE
		 *	HLHHLH -> FSGE
		 *	HHHHLH -> FSEQ
		 *	HLLLHH -> FSNE
		 *	LHLLHH -> FSIFN
		 *	LLHLHH -> FCITF
		 *	LHHLHH -> FCFTI
		 *
		 * 1 Bit ALU Use Immediate:
		 *	L -> Use Register
		 *  H -> Use Immediate
		 *
		 * 1 Bit ALU/FPU Forward Save:
		 *	L -> Not Forward
		 *	H -> Forward
		 *
		 * 3 Bit Load Memory Select:
		 *	LLL -> Load Signed Byte
		 *	HLL -> Load Unsigned Byte
		 *	LHL -> Load Signed Halfword
		 *	HHL -> Load Unsigned Halfword
		 *	LLH -> Load Word
		 *
		 * 2 Bit Store Memory Select:
		 *	LL -> Nothing
		 *	HL -> Store Byte
		 *	LH -> Store Halfword
		 *	HH -> Store Word
		 *
		 * 1 Bit Write Register Select:
		 *	L -> ALU/FPU Result
		 *	H -> Load Result
		 *
		 * 1 Bit Write Register Enable:
		 *	L -> Nothing
		 *	H -> Write Register
		 *
		 * 1 Bit Immediate Sign Extend Select:
		 *	L -> Signed Extend
		 *	H -> Unsigned Extend
		 *
		 * 2 Bit PC Overwrite Select:
		 *	LL -> PC+Offset
		 *	HL -> PC+Sign Immediate
		 *	LH -> Register
		 *
		 * 2 Bit PC Overwrite Enable:
		 *	LL -> Nothing
		 *	HL -> Overwrite
		 *	LH -> Overwrite if Branch
		 *
		 * 1 Bit Branch Select:
		 *	L -> Test if Zero
		 *	H -> Test if Not Zero
		 *
		 * 1 Bit Link:
		 *	L -> Nothing
		 *	H -> Overwrite R31 with PC+1
		 *
		 */

		op_map = new HashMap<Bit[], Bit[]>();
		func_map = new HashMap<Bit[], Bit[]>();

		fpu_op_map = new HashMap<Bit[], Bit[]>();
		fpu_func_map = new HashMap<Bit[], Bit[]>();

		/*LB*/		op_map.put(new Bit[] {H, L, L, L, L, L}, new Bit[] {L, L, L, L, L, H, L,  L, L, L,  L, L,  H, H,  L,  L, L, L, L,  L, L});
		/*LBU*/		op_map.put(new Bit[] {L, H, L, L, L, L}, new Bit[] {L, L, L, L, L, H, L,  H, L, L,  L, L,  H, H,  L,  L, L, L, L,  L, L});
		/*SB*/		op_map.put(new Bit[] {H, H, L, L, L, L}, new Bit[] {L, L, L, L, L, H, L,  L, L, L,  H, L,  L, L,  L,  L, L, L, L,  L, L});
		/*LH*/		op_map.put(new Bit[] {L, L, H, L, L, L}, new Bit[] {L, L, L, L, L, H, L,  L, H, L,  L, L,  H, H,  L,  L, L, L, L,  L, L});
		/*LHU*/		op_map.put(new Bit[] {H, L, H, L, L, L}, new Bit[] {L, L, L, L, L, H, L,  H, H, L,  L, L,  H, H,  L,  L, L, L, L,  L, L});
		/*SH*/		op_map.put(new Bit[] {L, H, H, L, L, L}, new Bit[] {L, L, L, L, L, H, L,  L, L, L,  L, H,  L, L,  L,  L, L, L, L,  L, L});
		/*LW*/		op_map.put(new Bit[] {H, H, H, L, L, L}, new Bit[] {L, L, L, L, L, H, L,  L, L, H,  L, L,  H, H,  L,  L, L, L, L,  L, L});
		/*SW*/		op_map.put(new Bit[] {L, L, L, H, L, L}, new Bit[] {L, L, L, L, L, H, L,  L, L, L,  H, H,  L, L,  L,  L, L, L, L,  L, L});

		/*BEQZ*/	op_map.put(new Bit[] {H, L, L, H, L, L}, new Bit[] {L, L, L, L, L, L, L,  L, L, L,  L, L,  L, L,  L,  H, L, L, H,  L, L});
		/*BNEZ*/	op_map.put(new Bit[] {L, H, L, H, L, L}, new Bit[] {L, L, L, L, L, L, L,  L, L, L,  L, L,  L, L,  L,  H, L, L, H,  H, L});
		/*J*/		op_map.put(new Bit[] {H, H, L, H, L, L}, new Bit[] {L, L, L, L, L, L, L,  L, L, L,  L, L,  L, L,  L,  L, L, H, L,  L, L});
		/*JR*/		op_map.put(new Bit[] {L, L, H, H, L, L}, new Bit[] {L, L, L, L, L, L, L,  L, L, L,  L, L,  L, L,  L,  L, H, H, L,  L, L});
		/*JAL*/		op_map.put(new Bit[] {H, L, H, H, L, L}, new Bit[] {L, L, L, L, L, L, L,  L, L, L,  L, L,  L, L,  L,  L, L, H, L,  L, H});
		/*JALR*/	op_map.put(new Bit[] {L, H, H, H, L, L}, new Bit[] {L, L, L, L, L, L, L,  L, L, L,  L, L,  L, L,  L,  L, H, H, L,  L, H});

		/*ADDI*/	op_map.put(new Bit[] {H, H, H, H, L, L}, new Bit[] {L, L, L, L, L, H, H,  L, L, L,  L, L,  L, H,  L,  L, L, L, L,  L, L});
		/*ADDUI*/	op_map.put(new Bit[] {L, L, L, L, H, L}, new Bit[] {L, L, L, L, L, H, H,  L, L, L,  L, L,  L, H,  H,  L, L, L, L,  L, L});
		/*SUBI*/	op_map.put(new Bit[] {H, L, L, L, H, L}, new Bit[] {H, L, L, L, L, H, H,  L, L, L,  L, L,  L, H,  L,  L, L, L, L,  L, L});
		/*SUBUI*/	op_map.put(new Bit[] {L, H, L, L, H, L}, new Bit[] {H, L, L, L, L, H, H,  L, L, L,  L, L,  L, H,  H,  L, L, L, L,  L, L});
		/*ANDI*/	op_map.put(new Bit[] {H, H, L, L, H, L}, new Bit[] {L, H, H, L, L, H, H,  L, L, L,  L, L,  L, H,  H,  L, L, L, L,  L, L});
		/*ORI*/		op_map.put(new Bit[] {L, L, H, L, H, L}, new Bit[] {H, H, H, L, L, H, H,  L, L, L,  L, L,  L, H,  H,  L, L, L, L,  L, L});
		/*XORI*/	op_map.put(new Bit[] {H, L, H, L, H, L}, new Bit[] {L, L, L, H, L, H, H,  L, L, L,  L, L,  L, H,  H,  L, L, L, L,  L, L});
		/*LHI*/		op_map.put(new Bit[] {L, H, H, L, H, L}, new Bit[] {L, H, L, L, H, H, H,  L, L, L,  L, L,  L, H,  L,  L, L, L, L,  L, L});
		/*SLLI*/	op_map.put(new Bit[] {H, H, H, L, H, L}, new Bit[] {H, L, L, H, L, H, H,  L, L, L,  L, L,  L, H,  L,  L, L, L, L,  L, L});
		/*SRLI*/	op_map.put(new Bit[] {L, L, L, H, H, L}, new Bit[] {L, H, L, H, L, H, H,  L, L, L,  L, L,  L, H,  L,  L, L, L, L,  L, L});
		/*SRAI*/	op_map.put(new Bit[] {H, L, L, H, H, L}, new Bit[] {H, H, L, H, L, H, H,  L, L, L,  L, L,  L, H,  L,  L, L, L, L,  L, L});
		/*SLTI*/	op_map.put(new Bit[] {L, H, L, H, H, L}, new Bit[] {L, L, H, H, L, H, H,  L, L, L,  L, L,  L, H,  L,  L, L, L, L,  L, L});
		/*SGTI*/	op_map.put(new Bit[] {H, H, L, H, H, L}, new Bit[] {H, L, H, H, L, H, H,  L, L, L,  L, L,  L, H,  L,  L, L, L, L,  L, L});
		/*SLEI*/	op_map.put(new Bit[] {L, L, H, H, H, L}, new Bit[] {L, H, H, H, L, H, H,  L, L, L,  L, L,  L, H,  L,  L, L, L, L,  L, L});
		/*SGEI*/	op_map.put(new Bit[] {H, L, H, H, H, L}, new Bit[] {H, H, H, H, L, H, H,  L, L, L,  L, L,  L, H,  L,  L, L, L, L,  L, L});
		/*SEQI*/	op_map.put(new Bit[] {L, H, H, H, H, L}, new Bit[] {L, L, L, L, H, H, H,  L, L, L,  L, L,  L, H,  L,  L, L, L, L,  L, L});
		/*SNEI*/	op_map.put(new Bit[] {H, H, H, H, H, L}, new Bit[] {H, L, L, L, H, H, H,  L, L, L,  L, L,  L, H,  L,  L, L, L, L,  L, L});

		/*ADD*/		func_map.put(new Bit[] {L, L, L, L, L, L, L, L, L, L, L}, new Bit[] {L, L, L, L, L, L, H,  L, L, L,  L, L,  L, H,  L,  L, L, L, L,  L, L});
		/*SUB*/		func_map.put(new Bit[] {H, L, L, L, L, L, L, L, L, L, L}, new Bit[] {H, L, L, L, L, L, H,  L, L, L,  L, L,  L, H,  L,  L, L, L, L,  L, L});
		/*MULT*/	func_map.put(new Bit[] {L, H, L, L, L, L, L, L, L, L, L}, new Bit[] {L, H, L, L, L, L, H,  L, L, L,  L, L,  L, H,  L,  L, L, L, L,  L, L});
		/*MULTU*/	func_map.put(new Bit[] {H, H, L, L, L, L, L, L, L, L, L}, new Bit[] {H, H, L, L, L, L, H,  L, L, L,  L, L,  L, H,  L,  L, L, L, L,  L, L});
		/*DIV*/		func_map.put(new Bit[] {L, L, H, L, L, L, L, L, L, L, L}, new Bit[] {L, L, H, L, L, L, H,  L, L, L,  L, L,  L, H,  L,  L, L, L, L,  L, L});
		/*DIVU*/	func_map.put(new Bit[] {H, L, H, L, L, L, L, L, L, L, L}, new Bit[] {H, L, H, L, L, L, H,  L, L, L,  L, L,  L, H,  L,  L, L, L, L,  L, L});
		/*AND*/		func_map.put(new Bit[] {L, H, H, L, L, L, L, L, L, L, L}, new Bit[] {L, H, H, L, L, L, H,  L, L, L,  L, L,  L, H,  L,  L, L, L, L,  L, L});
		/*OR*/		func_map.put(new Bit[] {H, H, H, L, L, L, L, L, L, L, L}, new Bit[] {H, H, H, L, L, L, H,  L, L, L,  L, L,  L, H,  L,  L, L, L, L,  L, L});
		/*XOR*/		func_map.put(new Bit[] {L, L, L, H, L, L, L, L, L, L, L}, new Bit[] {L, L, L, H, L, L, H,  L, L, L,  L, L,  L, H,  L,  L, L, L, L,  L, L});
		/*SLL*/		func_map.put(new Bit[] {H, L, L, H, L, L, L, L, L, L, L}, new Bit[] {H, L, L, H, L, L, H,  L, L, L,  L, L,  L, H,  L,  L, L, L, L,  L, L});
		/*SRL*/		func_map.put(new Bit[] {L, H, L, H, L, L, L, L, L, L, L}, new Bit[] {L, H, L, H, L, L, H,  L, L, L,  L, L,  L, H,  L,  L, L, L, L,  L, L});
		/*SRA*/		func_map.put(new Bit[] {H, H, L, H, L, L, L, L, L, L, L}, new Bit[] {H, H, L, H, L, L, H,  L, L, L,  L, L,  L, H,  L,  L, L, L, L,  L, L});
		/*SLT*/		func_map.put(new Bit[] {L, L, H, H, L, L, L, L, L, L, L}, new Bit[] {L, L, H, H, L, L, H,  L, L, L,  L, L,  L, H,  L,  L, L, L, L,  L, L});
		/*SGT*/		func_map.put(new Bit[] {H, L, H, H, L, L, L, L, L, L, L}, new Bit[] {H, L, H, H, L, L, H,  L, L, L,  L, L,  L, H,  L,  L, L, L, L,  L, L});
		/*SLE*/		func_map.put(new Bit[] {L, H, H, H, L, L, L, L, L, L, L}, new Bit[] {L, H, H, H, L, L, H,  L, L, L,  L, L,  L, H,  L,  L, L, L, L,  L, L});
		/*SGE*/		func_map.put(new Bit[] {H, H, H, H, L, L, L, L, L, L, L}, new Bit[] {H, H, H, H, L, L, H,  L, L, L,  L, L,  L, H,  L,  L, L, L, L,  L, L});
		/*SEQ*/		func_map.put(new Bit[] {L, L, L, L, H, L, L, L, L, L, L}, new Bit[] {L, L, L, L, H, L, H,  L, L, L,  L, L,  L, H,  L,  L, L, L, L,  L, L});
		/*SNE*/		func_map.put(new Bit[] {H, L, L, L, H, L, L, L, L, L, L}, new Bit[] {H, L, L, L, H, L, H,  L, L, L,  L, L,  L, H,  L,  L, L, L, L,  L, L});


		/*LB*/		fpu_op_map.put(new Bit[] {H, L, L, L, L, L}, new Bit[] {L, L, L, L, L, L, H, L,  L, L, L,  L, L,  H, H,  L,  L, L, L, L,  L, L});
		/*LBU*/		fpu_op_map.put(new Bit[] {L, H, L, L, L, L}, new Bit[] {L, L, L, L, L, L, H, L,  H, L, L,  L, L,  H, H,  L,  L, L, L, L,  L, L});
		/*SB*/		fpu_op_map.put(new Bit[] {H, H, L, L, L, L}, new Bit[] {L, L, L, L, L, L, H, L,  L, L, L,  H, L,  L, L,  L,  L, L, L, L,  L, L});
		/*LH*/		fpu_op_map.put(new Bit[] {L, L, H, L, L, L}, new Bit[] {L, L, L, L, L, L, H, L,  L, H, L,  L, L,  H, H,  L,  L, L, L, L,  L, L});
		/*LHU*/		fpu_op_map.put(new Bit[] {H, L, H, L, L, L}, new Bit[] {L, L, L, L, L, L, H, L,  H, H, L,  L, L,  H, H,  L,  L, L, L, L,  L, L});
		/*SH*/		fpu_op_map.put(new Bit[] {L, H, H, L, L, L}, new Bit[] {L, L, L, L, L, L, H, L,  L, L, L,  L, H,  L, L,  L,  L, L, L, L,  L, L});
		/*LW*/		fpu_op_map.put(new Bit[] {H, H, H, L, L, L}, new Bit[] {L, L, L, L, L, L, H, L,  L, L, H,  L, L,  H, H,  L,  L, L, L, L,  L, L});
		/*SW*/		fpu_op_map.put(new Bit[] {L, L, L, H, L, L}, new Bit[] {L, L, L, L, L, L, H, L,  L, L, L,  H, H,  L, L,  L,  L, L, L, L,  L, L});

		/*BEQZ*/	fpu_op_map.put(new Bit[] {H, L, L, H, L, L}, new Bit[] {L, L, L, L, L, L, L, L,  L, L, L,  L, L,  L, L,  L,  H, L, L, H,  L, L});
		/*BNEZ*/	fpu_op_map.put(new Bit[] {L, H, L, H, L, L}, new Bit[] {L, L, L, L, L, L, L, L,  L, L, L,  L, L,  L, L,  L,  H, L, L, H,  H, L});
		/*J*/		fpu_op_map.put(new Bit[] {H, H, L, H, L, L}, new Bit[] {L, L, L, L, L, L, L, L,  L, L, L,  L, L,  L, L,  L,  L, L, H, L,  L, L});
		/*JR*/		fpu_op_map.put(new Bit[] {L, L, H, H, L, L}, new Bit[] {L, L, L, L, L, L, L, L,  L, L, L,  L, L,  L, L,  L,  L, H, H, L,  L, L});
		/*JAL*/		fpu_op_map.put(new Bit[] {H, L, H, H, L, L}, new Bit[] {L, L, L, L, L, L, L, L,  L, L, L,  L, L,  L, L,  L,  L, L, H, L,  L, H});
		/*JALR*/	fpu_op_map.put(new Bit[] {L, H, H, H, L, L}, new Bit[] {L, L, L, L, L, L, L, L,  L, L, L,  L, L,  L, L,  L,  L, H, H, L,  L, H});

		/*ADDI*/	fpu_op_map.put(new Bit[] {H, H, H, H, L, L}, new Bit[] {L, L, L, L, L, L, H, H,  L, L, L,  L, L,  L, H,  L,  L, L, L, L,  L, L});
		/*ADDUI*/	fpu_op_map.put(new Bit[] {L, L, L, L, H, L}, new Bit[] {L, L, L, L, L, L, H, H,  L, L, L,  L, L,  L, H,  H,  L, L, L, L,  L, L});
		/*SUBI*/	fpu_op_map.put(new Bit[] {H, L, L, L, H, L}, new Bit[] {H, L, L, L, L, L, H, H,  L, L, L,  L, L,  L, H,  L,  L, L, L, L,  L, L});
		/*SUBUI*/	fpu_op_map.put(new Bit[] {L, H, L, L, H, L}, new Bit[] {H, L, L, L, L, L, H, H,  L, L, L,  L, L,  L, H,  H,  L, L, L, L,  L, L});
		/*ANDI*/	fpu_op_map.put(new Bit[] {H, H, L, L, H, L}, new Bit[] {L, H, H, L, L, L, H, H,  L, L, L,  L, L,  L, H,  H,  L, L, L, L,  L, L});
		/*ORI*/		fpu_op_map.put(new Bit[] {L, L, H, L, H, L}, new Bit[] {H, H, H, L, L, L, H, H,  L, L, L,  L, L,  L, H,  H,  L, L, L, L,  L, L});
		/*XORI*/	fpu_op_map.put(new Bit[] {H, L, H, L, H, L}, new Bit[] {L, L, L, H, L, L, H, H,  L, L, L,  L, L,  L, H,  H,  L, L, L, L,  L, L});
		/*LHI*/		fpu_op_map.put(new Bit[] {L, H, H, L, H, L}, new Bit[] {L, H, L, L, H, L, H, H,  L, L, L,  L, L,  L, H,  L,  L, L, L, L,  L, L});
		/*SLLI*/	fpu_op_map.put(new Bit[] {H, H, H, L, H, L}, new Bit[] {H, L, L, H, L, L, H, H,  L, L, L,  L, L,  L, H,  L,  L, L, L, L,  L, L});
		/*SRLI*/	fpu_op_map.put(new Bit[] {L, L, L, H, H, L}, new Bit[] {L, H, L, H, L, L, H, H,  L, L, L,  L, L,  L, H,  L,  L, L, L, L,  L, L});
		/*SRAI*/	fpu_op_map.put(new Bit[] {H, L, L, H, H, L}, new Bit[] {H, H, L, H, L, L, H, H,  L, L, L,  L, L,  L, H,  L,  L, L, L, L,  L, L});
		/*SLTI*/	fpu_op_map.put(new Bit[] {L, H, L, H, H, L}, new Bit[] {L, L, H, H, L, L, H, H,  L, L, L,  L, L,  L, H,  L,  L, L, L, L,  L, L});
		/*SGTI*/	fpu_op_map.put(new Bit[] {H, H, L, H, H, L}, new Bit[] {H, L, H, H, L, L, H, H,  L, L, L,  L, L,  L, H,  L,  L, L, L, L,  L, L});
		/*SLEI*/	fpu_op_map.put(new Bit[] {L, L, H, H, H, L}, new Bit[] {L, H, H, H, L, L, H, H,  L, L, L,  L, L,  L, H,  L,  L, L, L, L,  L, L});
		/*SGEI*/	fpu_op_map.put(new Bit[] {H, L, H, H, H, L}, new Bit[] {H, H, H, H, L, L, H, H,  L, L, L,  L, L,  L, H,  L,  L, L, L, L,  L, L});
		/*SEQI*/	fpu_op_map.put(new Bit[] {L, H, H, H, H, L}, new Bit[] {L, L, L, L, H, L, H, H,  L, L, L,  L, L,  L, H,  L,  L, L, L, L,  L, L});
		/*SNEI*/	fpu_op_map.put(new Bit[] {H, H, H, H, H, L}, new Bit[] {H, L, L, L, H, L, H, H,  L, L, L,  L, L,  L, H,  L,  L, L, L, L,  L, L});

		/*ADD*/		fpu_func_map.put(new Bit[] {L, L, L, L, L, L, L, L, L, L, L}, new Bit[] {L, L, L, L, L, L, L, H,  L, L, L,  L, L,  L, H,  L,  L, L, L, L,  L, L});
		/*SUB*/		fpu_func_map.put(new Bit[] {H, L, L, L, L, L, L, L, L, L, L}, new Bit[] {H, L, L, L, L, L, L, H,  L, L, L,  L, L,  L, H,  L,  L, L, L, L,  L, L});
		/*MULT*/	fpu_func_map.put(new Bit[] {L, H, L, L, L, L, L, L, L, L, L}, new Bit[] {L, H, L, L, L, L, L, H,  L, L, L,  L, L,  L, H,  L,  L, L, L, L,  L, L});
		/*MULTU*/	fpu_func_map.put(new Bit[] {H, H, L, L, L, L, L, L, L, L, L}, new Bit[] {H, H, L, L, L, L, L, H,  L, L, L,  L, L,  L, H,  L,  L, L, L, L,  L, L});
		/*DIV*/		fpu_func_map.put(new Bit[] {L, L, H, L, L, L, L, L, L, L, L}, new Bit[] {L, L, H, L, L, L, L, H,  L, L, L,  L, L,  L, H,  L,  L, L, L, L,  L, L});
		/*DIVU*/	fpu_func_map.put(new Bit[] {H, L, H, L, L, L, L, L, L, L, L}, new Bit[] {H, L, H, L, L, L, L, H,  L, L, L,  L, L,  L, H,  L,  L, L, L, L,  L, L});
		/*AND*/		fpu_func_map.put(new Bit[] {L, H, H, L, L, L, L, L, L, L, L}, new Bit[] {L, H, H, L, L, L, L, H,  L, L, L,  L, L,  L, H,  L,  L, L, L, L,  L, L});
		/*OR*/		fpu_func_map.put(new Bit[] {H, H, H, L, L, L, L, L, L, L, L}, new Bit[] {H, H, H, L, L, L, L, H,  L, L, L,  L, L,  L, H,  L,  L, L, L, L,  L, L});
		/*XOR*/		fpu_func_map.put(new Bit[] {L, L, L, H, L, L, L, L, L, L, L}, new Bit[] {L, L, L, H, L, L, L, H,  L, L, L,  L, L,  L, H,  L,  L, L, L, L,  L, L});
		/*SLL*/		fpu_func_map.put(new Bit[] {H, L, L, H, L, L, L, L, L, L, L}, new Bit[] {H, L, L, H, L, L, L, H,  L, L, L,  L, L,  L, H,  L,  L, L, L, L,  L, L});
		/*SRL*/		fpu_func_map.put(new Bit[] {L, H, L, H, L, L, L, L, L, L, L}, new Bit[] {L, H, L, H, L, L, L, H,  L, L, L,  L, L,  L, H,  L,  L, L, L, L,  L, L});
		/*SRA*/		fpu_func_map.put(new Bit[] {H, H, L, H, L, L, L, L, L, L, L}, new Bit[] {H, H, L, H, L, L, L, H,  L, L, L,  L, L,  L, H,  L,  L, L, L, L,  L, L});
		/*SLT*/		fpu_func_map.put(new Bit[] {L, L, H, H, L, L, L, L, L, L, L}, new Bit[] {L, L, H, H, L, L, L, H,  L, L, L,  L, L,  L, H,  L,  L, L, L, L,  L, L});
		/*SGT*/		fpu_func_map.put(new Bit[] {H, L, H, H, L, L, L, L, L, L, L}, new Bit[] {H, L, H, H, L, L, L, H,  L, L, L,  L, L,  L, H,  L,  L, L, L, L,  L, L});
		/*SLE*/		fpu_func_map.put(new Bit[] {L, H, H, H, L, L, L, L, L, L, L}, new Bit[] {L, H, H, H, L, L, L, H,  L, L, L,  L, L,  L, H,  L,  L, L, L, L,  L, L});
		/*SGE*/		fpu_func_map.put(new Bit[] {H, H, H, H, L, L, L, L, L, L, L}, new Bit[] {H, H, H, H, L, L, L, H,  L, L, L,  L, L,  L, H,  L,  L, L, L, L,  L, L});
		/*SEQ*/		fpu_func_map.put(new Bit[] {L, L, L, L, H, L, L, L, L, L, L}, new Bit[] {L, L, L, L, H, L, L, H,  L, L, L,  L, L,  L, H,  L,  L, L, L, L,  L, L});
		/*SNE*/		fpu_func_map.put(new Bit[] {H, L, L, L, H, L, L, L, L, L, L}, new Bit[] {H, L, L, L, H, L, L, H,  L, L, L,  L, L,  L, H,  L,  L, L, L, L,  L, L});

		/*FADD*/	fpu_func_map.put(new Bit[] {L, L, L, L, L, H, L, L, L, L, L}, new Bit[] {L, L, L, L, L, H, L, H,  L, L, L,  L, L,  L, H,  L,  L, L, L, L,  L, L});
		/*FSUB*/	fpu_func_map.put(new Bit[] {H, L, L, L, L, H, L, L, L, L, L}, new Bit[] {H, L, L, L, L, H, L, H,  L, L, L,  L, L,  L, H,  L,  L, L, L, L,  L, L});
		/*FMULT*/	fpu_func_map.put(new Bit[] {L, H, L, L, L, H, L, L, L, L, L}, new Bit[] {L, H, L, L, L, H, L, H,  L, L, L,  L, L,  L, H,  L,  L, L, L, L,  L, L});
		/*FDIV*/	fpu_func_map.put(new Bit[] {H, H, L, L, L, H, L, L, L, L, L}, new Bit[] {L, L, H, L, L, H, L, H,  L, L, L,  L, L,  L, H,  L,  L, L, L, L,  L, L});
		/*FSLT*/	fpu_func_map.put(new Bit[] {L, L, H, L, L, H, L, L, L, L, L}, new Bit[] {H, H, H, L, L, H, L, H,  L, L, L,  L, L,  L, H,  L,  L, L, L, L,  L, L});
		/*FSGT*/	fpu_func_map.put(new Bit[] {H, L, H, L, L, H, L, L, L, L, L}, new Bit[] {H, L, L, H, L, H, L, H,  L, L, L,  L, L,  L, H,  L,  L, L, L, L,  L, L});
		/*FSLE*/	fpu_func_map.put(new Bit[] {L, H, H, L, L, H, L, L, L, L, L}, new Bit[] {H, H, L, H, L, H, L, H,  L, L, L,  L, L,  L, H,  L,  L, L, L, L,  L, L});
		/*FSGE*/	fpu_func_map.put(new Bit[] {H, H, H, L, L, H, L, L, L, L, L}, new Bit[] {H, L, H, H, L, H, L, H,  L, L, L,  L, L,  L, H,  L,  L, L, L, L,  L, L});
		/*FSEQ*/	fpu_func_map.put(new Bit[] {L, L, L, H, L, H, L, L, L, L, L}, new Bit[] {H, H, H, H, L, H, L, H,  L, L, L,  L, L,  L, H,  L,  L, L, L, L,  L, L});
		/*FSNE*/	fpu_func_map.put(new Bit[] {H, L, L, H, L, H, L, L, L, L, L}, new Bit[] {H, L, L, L, H, H, L, H,  L, L, L,  L, L,  L, H,  L,  L, L, L, L,  L, L});
		/*FSINF*/	fpu_func_map.put(new Bit[] {L, H, L, H, L, H, L, L, L, L, L}, new Bit[] {L, H, L, L, H, H, L, H,  L, L, L,  L, L,  L, H,  L,  L, L, L, L,  L, L});
		/*FCITF*/	fpu_func_map.put(new Bit[] {H, H, L, H, L, H, L, L, L, L, L}, new Bit[] {L, L, H, L, H, H, L, H,  L, L, L,  L, L,  L, H,  L,  L, L, L, L,  L, L});
		/*FCFTI*/	fpu_func_map.put(new Bit[] {L, L, H, H, L, H, L, L, L, L, L}, new Bit[] {L, H, H, L, H, H, L, H,  L, L, L,  L, L,  L, H,  L,  L, L, L, L,  L, L});
	}

	private InstructionDecode(DeserializingStream in) throws IOException, DeserializingException, InstantiationException
	{
		super(in);
	}

	private final void build(boolean with_fpu)
	{
		Signal ir = getSignal("ir");
		Signal pc = getSignal("pc");
		Signal[] reg_read = (Signal[])get("reg_read");

		Signal pco = getSignal("pco");
		Signal pcoe = getSignal("pcoe");

		Signal r31_write = getSignal("r31_write");
		Signal r31_write_en = getSignal("r31_write_en");

		Signal opcode = getSignal("opcode");
		Signal alu_imm = getSignal("alu_imm");
		Signal alu_fw = getSignal("alu_fw");
		Signal r1 = getSignal("r1");
		Signal r2 = getSignal("r2");
		Signal a1 = getSignal("a1");
		Signal a2 = getSignal("a2");
		Signal a3 = getSignal("a3");
		Signal imm = getSignal("imm");

		Signal load = getSignal("load");
		Signal store = getSignal("store");

		Signal write = getSignal("write");
		Signal write_en = getSignal("write_en");

		Signal op_dec;
		Signal func_dec;
		if(with_fpu)
		{
			op_dec = new Signal(22);
			func_dec = new Signal(22);
		}
		else
		{
			op_dec = new Signal(21);
			func_dec = new Signal(21);
		}
		Signal op_null = new Signal(1);

		new Mapping(this, "op-map").setAll(new Signal(ir, 0, 6), op_dec, with_fpu ? fpu_op_map : op_map);
		new Mapping(this, "func-map").setAll(new Signal(ir, 21, 11), func_dec, with_fpu ? fpu_func_map : func_map);
		new NOR(this, "3reg-test").setAll(new Signal(ir, 0, 6), op_null);

		Signal pco_sel = new Signal(2);
		Signal pcoe_sel = new Signal(2);
		Signal br = new Signal(1);
		Signal imm_ext = new Signal(1);

		new BinaryMultiplexer(this, "opcode-mux").setAll(new Signal[] {op_dec, func_dec}, op_null, new Signal(opcode, alu_imm, alu_fw, load, store, write, write_en, imm_ext, pco_sel, pcoe_sel, br, r31_write_en));

		new Forward(this, "a1-fw").setAll(new Signal(ir, 6, 5), a1);
		new Forward(this, "a2-fw").setAll(new Signal(ir, 11, 5), a2);
		new BinaryMultiplexer(this, "r1-mux").setAll(reg_read, a1, r1);
		new BinaryMultiplexer(this, "r2-mux").setAll(reg_read, a2, r2);

		Signal a3_sel = new Signal(2);
		new OR(this, "sus-alu").setAll(pcoe_sel, a3_sel.get(1));
		new BooleanFunction(this, "a3-sel").setAll(new Signal(a3_sel.get(1), op_null), a3_sel.get(0), BooleanFunction.Type.DNF, new BooleanFunction.Value[][] {{BooleanFunction.Value.FALSE, BooleanFunction.Value.TRUE}});
		new BinaryMultiplexer(this, "ia3-mux").setAll(new Signal[] {new Signal(ir, 11, 5), new Signal(ir, 16, 5), new Signal(L, 5)}, a3_sel, a3);

		Signal imthisdiate = new Signal(ir, 16, 16);
		Signal simthisdiate = new Signal(imthisdiate, new Signal(imthisdiate.get(15), 16));
		new BinaryMultiplexer(this, "imm-signext-mux")
			.setAll(new Signal[] {simthisdiate, new Signal(imthisdiate, new Signal(L, 16))}, imm_ext, imm);

		Signal zero_test = new Signal(1);
		Signal nzero_test = new Signal(1);

		new OR(this, "reg-nzero-test").setAll(r1, nzero_test);
		new NOT(this, "reg-zero-test").setAll(nzero_test, zero_test);

		Signal branch = new Signal(1);
		Signal offset = new Signal(new Signal(ir, 6, 26), new Signal(ir.get(31), 6));
		Signal pcplusoffset = new Signal(32);
		Signal pcplussimthisdiate = new Signal(32);

		new BinaryMultiplexer(this, "branch-mux").setAll(new Signal[] {zero_test, nzero_test}, br, branch);
		new Multiplexer(this, "pcoe-mux").setAll(new Signal[] {new Signal(H), branch}, pcoe_sel, pcoe);
		new CLAAdder(this, "pc_add_off").useAndSet("riple",new String[]{"x","y","carryIn","sum","carryOut"},pc, offset, new Signal(L), pcplusoffset, new Signal(1));
		new CLAAdder(this, "pc_add_simm").useAndSet("riple",new String[]{"x","y","carryIn","sum","carryOut"},pc, simthisdiate, new Signal(L), pcplussimthisdiate, new Signal(1));
		new BinaryMultiplexer(this, "pco-mux").setAll(new Signal[] {pcplusoffset, pcplussimthisdiate, r1}, pco_sel, pco);
		new Forward(this, "link-fw").setAll(pc.get(0), r31_write.get(0));
		new RipleIncrementer(this, "link_inc").setAll(pc.get(1, 31), r31_write.get(1, 31));
	}
}
