
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
import std.gate.AND;
import std.gate.XNOR;
import std.memory.RingBuffer;
import std.mux.BinaryMultiplexer;
import core.build.ComponentCollection;
import core.build.Composite;
import core.signal.Bit;
import core.build.Flavor;
import core.exception.DeserializingException;
import core.misc.serial.DeserializingStream;
import core.signal.Signal;
import java.io.IOException;
import java.util.Map;
import std.alu.Comparator;

/**
 * @author lars
 */
public class Executor extends Composite {

	public Executor(ComponentCollection parent,String name)
	{
		super(parent,name);
	}

	public final Executor setAll(Signal clk, Signal in1, Signal in2, Signal r1, Signal r2, Signal rdest, Signal imm, Signal opcode, Signal use_immediate, Signal write_forward, Signal rf, Signal forward, Signal rcheck, Signal check, Signal checkresult, Signal out, boolean with_fpu)
	{
		return (Executor)useAndSet(with_fpu ? "with_fpu" : Flavor.DEFAULT, new String[] {"clk", "in1", "in2", "r1", "r2", "rdest", "imm", "opcode", "use_immediate", "write_forward", "rf", "forward", "rcheck", "check", "checkresult", "out"}, clk, in1, in2, r1, r2, rdest, imm, opcode, use_immediate, write_forward, rf, forward, rcheck, check, checkresult, out);
	}

	@Override public final Map<String, Flavor> getFlavors()
	{
		return flavors;
	}

	private static final Map<String, Flavor> flavors = Flavor.getMap(
		new Flavor(
			new Flavor.Buildable<Executor>()
			{
				@Override
				public void build(Executor me, Map<String, Integer> variables) {
					me.build(false);
				}
			},
			"clk[1],in1[32],in2[32],r1[5],r2[5],rdest[5],imm[32],opcode[5],use_immediate[1],write_forward[1],rf[5],forward[32],rcheck[5],check[32]",
			"checkresult[32],out[32]"
		),
		new Flavor(
			"with_fpu",
			new Flavor.Buildable<Executor>()
			{
				@Override
				public void build(Executor me, Map<String, Integer> variables) {
					me.build(true);
				}
			},
			"clk[1],in1[32],in2[32],r1[5],r2[5],rdest[5],imm[32],opcode[6],use_immediate[1],write_forward[1],rf[5],forward[32],rcheck[5],check[32]",
			"checkresult[32],out[32]"
		)
	);

	private Executor(DeserializingStream in) throws IOException, DeserializingException, InstantiationException
	{
		super(in);
	}

	private final void build(boolean with_fpu)
	{
		Signal rdest = getSignal("rdest");

		final int width = getSignal("out").size();
		final int registerWidth = rdest.size();

		Signal rbOutput[] = new Signal[3];
		for (int i=0;i<rbOutput.length;++i)
			rbOutput[i] = new Signal(width+registerWidth);

		Signal xInput = buildSelector("r1",getSignal("r1"), rbOutput, getSignal("rf"), getSignal("forward"), getSignal("in1"), new Signal(width));
		Signal yInput = new Signal(width);
		Signal opcode = getSignal("opcode");

		new BinaryMultiplexer(this,"yin-mux")
			.setAll(new Signal[]{
						buildSelector("r2",getSignal("r2"), rbOutput, getSignal("rf"), getSignal("forward"), getSignal("in2"), new Signal(width)),
						getSignal("imm")
					},getSignal("use_immediate"),yInput);

		if(with_fpu)
		{
			Signal[] outs = new Signal[2];

			// ALU
			new ALU(this,"alu").setAll(
				xInput,
				yInput,
				opcode.get(0, 5),
				outs[0] = new Signal(32)
			);

			// FPU
			new FPU(this, "fpu").setAll(xInput, yInput, opcode.get(0, 5), outs[1] = new Signal(32));

			// Select ALU or FPU
			new BinaryMultiplexer(this, "u_sel").setAll(outs, opcode.get(5), getSignal("out"));
		}
		else
		{
			new ALU(this,"alu").setAll(
				xInput,
				yInput,
				opcode.get(0, 5),
				getSignal("out")
			);
		}

		// Passthrough to determine current Executor forwarding state without calculation
		buildSelector("passthrough", getSignal("rcheck"), rbOutput, getSignal("rf"), getSignal("forward"), getSignal("check"), getSignal("checkresult"));

		// RingBuffer
		Signal rbInput = new Signal(width+registerWidth);
		Signal zeros = new Signal(Bit.L, width+registerWidth);

		new BinaryMultiplexer(this,"fw-mux")
			.setAll(new Signal[]{zeros,new Signal(rdest,getSignal("out"))}, getSignal("write_forward"), rbInput);

		new RingBuffer(this,"ringbuffer").setAll(rbInput, getSignal("clk"), rbOutput, new Signal(2), true);
	}

	/**
	 * Builds a selector choosing between following signals (ordered after
	 * decreasing priority):
	 * <ul>
	 *   <li>{@code forwardRegister}</li>
	 *   <li>{@code buffer}</li>
	 *   <li>{@code fallback}</li>
	 * </ul>
	 * Selection is done based on register numbers. The output value therefore
	 * is the value of the highes priority input value with equal register
	 * number. If no such input exists, {@code fallbackValue} is the result.
	 * @param name name for the selector
	 * @param registerNr base register number for comparision
	 * @param buffer second priority register numbers and values
	 * @param forwardRegisterNr first priority register number
	 * @param forwardValue first priority value
	 * @param fallbackValue third priority value (with no corresponding register number)
	 * @param outputValue output signal to which should be written
	 * @return {@code outputValue}
	 */
	private Signal buildSelector(String name,Signal registerNr,Signal buffer[],Signal forwardRegisterNr,Signal forwardValue,Signal fallbackValue,Signal outputValue) {
		Signal currentSelection = new Signal(32);

		new BinaryMultiplexer(this,name+"-fw-sel")
			.setAll(new Signal[]{currentSelection,forwardValue}, // in[]
					buildBitwiseCmp(name + ":fw-sel", registerNr, forwardRegisterNr, new Signal(1)), // sel
					outputValue); // out

		for (int i=0;i<buffer.length;++i) {
			Signal newSelection = (i < buffer.length-1) ? new Signal(32) : fallbackValue;
			new BinaryMultiplexer(this,name+"-rb-sel:" + i)
				.setAll(new Signal[]{newSelection,buffer[i].get(registerNr.size(),fallbackValue.size())},
						buildBitwiseCmp(name + ":rb-sel:" + i, registerNr, buffer[i].get(0,registerNr.size()), new Signal(1)),
						currentSelection);
			currentSelection = newSelection;
		}

		return outputValue;
	}

	/**
	 * Builds a checker for equality, consisting of {@link XNOR}s coupled with
	 * an {@link AND}. Used because it's faster than creating a whole {@link Comparator}.
	 * @param name name of the comparator
	 * @param x first signal
	 * @param y second signal
	 * @param output output signal to which should be written (will be {@code H}
	 * if equal and {@code L} if not equal).
	 * @return {@code output}
	 */
	private Signal buildBitwiseCmp(String name,Signal x,Signal y,Signal output) {
		Signal xnorOutput = new Signal(5);
		for (int i=0;i<5;++i)
			new XNOR(this,name + ":xnor:" + i).setAll(new Signal(x.get(i),y.get(i)),xnorOutput.get(i));
		new AND(this,name + ":and").setAll(xnorOutput,output);
		return output;
	}
}
