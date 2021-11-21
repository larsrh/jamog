
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

import core.exception.InstantiationException;
import std.alu.Comparator;
import std.alu.Divider;
import std.alu.Multiplier;
import std.alu.RipleNegator;
import std.alu.Shifter;
import std.alu.UnsignedComparator;
import std.gate.AND;
import std.gate.NAND;
import std.gate.NOT;
import std.gate.OR;
import std.gate.XOR;
import std.logic.Forward;
import std.mux.BinaryMultiplexer;
import core.build.ComponentCollection;
import core.signal.Bit;
import core.build.Composite;
import core.build.Flavor;
import core.exception.DeserializingException;
import core.misc.serial.DeserializingStream;
import core.signal.Signal;
import java.io.IOException;
import java.util.Map;
import std.alu.CLAAdder;

/*
 * 5 Bit ALU Operation:
 *   0 -> LLLLL -> ADD
 *   1 -> HLLLL -> SUB
 *   2 -> LHLLL -> MULT
 *   3 -> HHLLL -> MULTU
 *   4 -> LLHLL -> DIV
 *   5 -> HLHLL -> DIVU
 *   6 -> LHHLL -> AND
 *   7 -> HHHLL -> OR
 *   8 -> LLLHL -> XOR
 *   9 -> HLLHL -> SLL
 *  10 -> LHLHL -> SRL
 *  11 -> HHLHL -> SRA
 *  12 -> LLHHL -> SLT
 *  13 -> HLHHL -> SGT
 *  14 -> LHHHL -> SLE
 *  15 -> HHHHL -> SGE
 *  16 -> LLLLH -> SEQ
 *  17 -> HLLLH -> SNE
 *	18 -> LHLLH -> LH
 */

/**
 * @author lars
 */
public class ALU extends Composite {

	public ALU(ComponentCollection parent,String name)
	{
		super(parent,name);
	}

	public final ALU setAll(Signal x, Signal y, Signal opcode, Signal out)
	{
		return (ALU)useAndSet(Flavor.DEFAULT, new String[] {"x", "y", "opcode", "out"}, x, y, opcode, out);
	}

	@Override public Map<String, Flavor> getFlavors()
	{
		return flavors;
	}

	private static final Map<String, Flavor> flavors = Flavor.getMap(
		new Flavor(
			new Flavor.Buildable<ALU>()
			{
				@Override public void build(ALU me, Map<String, Integer> variables)
				{
					Signal x = me.getSignal("x");
					Signal y = me.getSignal("y");
					Signal opcode = me.getSignal("opcode");
					Signal outs[] = new Signal[19];

					// 0-5
					{
						Signal notSigned = opcode.get(0);
						Signal signed = new Signal(1);

						new NOT(me,"inv").setAll(notSigned,signed);

						// 0
						new CLAAdder(me,"add").useAndSet("riple",new String[]{"x","y","carryIn","sum","carryOut"},x, y, new Signal(Bit.L), outs[0] = new Signal(32), new Signal(1));
						// 1
						new UnsignedComparator(me,"sub").setAll(x,y,outs[1] = new Signal(32),new Signal(2));
						// 2+3
						new Multiplier(me,"mult").setAll(x,y,signed,new Signal(outs[2] = outs[3] = new Signal(32),new Signal(32)));
						// 4+5
						new Divider(me,"div").setAll(x,y,signed,outs[4] = outs[5] = new Signal(32),new Signal(32));

					}

					// 6+7+8
					outs[6] = new Signal(32);
					outs[7] = new Signal(32);
					outs[8] = new Signal(32);
					for (int i=0;i<32;++i) {
						new AND(me,"and:" + i).setAll(new Signal(x.get(i), y.get(i)),outs[6].get(i)); // 6
						new OR(me,"or:" + i) .setAll(new Signal(x.get(i), y.get(i)),outs[7].get(i));  // 7
						new XOR(me,"xor:" + i).setAll(new Signal(x.get(i), y.get(i)),outs[8].get(i)); // 8
					}

					// 9+10+11
					{
						Signal arithmetic = new Signal(1);
						Signal right = opcode.get(1);
						Signal negated = new Signal(32);
						Signal shift = new Signal(32);
						new AND(me,"sh-ar").setAll(opcode.get(0,2),arithmetic);
						new RipleNegator(me,"sh-neg").setAll(y,negated);
						new BinaryMultiplexer(me,"sh-lr").setAll(new Signal[]{y,negated},right,shift);
						// TODO: check slicing of shift signal
						new Shifter(me,"shift").setAll(x,shift.get(0, 6),arithmetic,outs[9] = outs[10] = outs[11] = new Signal(32));
					}

					// 12-17
					{
						Signal cmp = new Signal(2);
						Signal zeros = new Signal(32-1);
						for (int i=0;i<zeros.size();++i)
							zeros.setBit(i,Bit.L);
						for (int i=12;i<=17;++i)
							outs[i] = new Signal(new Signal(1),zeros);

						new Comparator(me,"comp").setAll(x,y,new Signal(Bit.H),cmp);
						new NOT(me,"c-lt").setAll(cmp.get(0),outs[12].get(0));             // 12: lt = !gt-or-eq
						new AND(me,"c-gt").setAll(cmp,outs[13].get(0));                    // 13: gt = not-eq /\ gt-or-eq
						new NAND(me,"c-le").setAll(cmp,outs[14].get(0));                   // 14: lt-or-eq = !(not-eq /\ gt-or-eq)
						new Forward(me,"c-ge").setAll(cmp.get(0),outs[15].get(0));         // 15: gt-or-eq
						new NOT(me,"c-eq").setAll(cmp.get(1),outs[16].get(0));             // 16: eq = !not-eq
						new Forward(me,"c-ne").setAll(cmp.get(1),outs[17].get(0));         // 17: not-eq
					}

					// 18
					{
						Signal zeros = new Signal(32/2);
						for (int i=0;i<zeros.size();++i)
							zeros.setBit(i,Bit.L);
						// ignore y
						new Forward(me,"lh").setAll(new Signal(zeros,x.get(0,32-zeros.size())),outs[18] = new Signal(32));
					}

					// big multiplexer
					new BinaryMultiplexer(me,"out-mux").setAll(outs, opcode, me.getSignal("out"));
				}
			},
			"x[32],y[32],opcode[5]",
			"out[32]"
		)
	);

	private ALU(DeserializingStream in) throws IOException, DeserializingException, InstantiationException
	{
		super(in);
	}
}
