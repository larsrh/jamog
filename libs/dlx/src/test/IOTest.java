
package test;

import core.build.Environment;
import core.exception.AnalyzeException;
import core.exception.BuildException;
import core.misc.setable.GroupSetable;
import core.misc.setable.Setable;
import core.signal.Bit;
import core.signal.Signal;
import core.sim.Simulator;
import dlx.Processor;
import gui.DLXAssembler;
import java.util.Arrays;
import std.io.InputStreamConnector;
import std.io.OutputStreamConnector;
import std.memory.RAM;

import static core.misc.BitConverter.*;

public class IOTest
{
	public static final void main(String[] args)
	{
		String[] asm = new String[]
		{
			"io:",
			"addi r1 r0 1",

			"loop:",
			"sb 4(r0) r1",
			"lb r2 4(r0)",
			"add r0 r0 r0",
			"add r0 r0 r0",
			"beqz r2 end",
			"add r0 r0 0r",

			"lb r2 5(r0)",
			"add r0 r0 r0",

			"sb 7(r0) r2",
			"sb 6(r0) r1",

			"j loop",
			"add r0 r0 r0",

			"end:",
			"jr r31",
			"add r0 r0 r0"
		};

		int[] register = new int[31];
		byte[] ram = new byte[4];

		register[0] = 30;
		register[1] = 20;
		register[2] = 20;

		register[15] = Float.floatToRawIntBits(100.0f);
		register[16] = Float.floatToRawIntBits(-2.0f);
		register[17] = Float.floatToRawIntBits(1.0f);
		register[18] = Float.floatToRawIntBits(3.0f / (register[0] - 1));
		register[19] = Float.floatToRawIntBits(-2.0f / (register[1] - 1));

		Environment env = new Environment();

		Signal clk = new Signal(1);
		Signal[] mem_read = new Signal[ram.length + 4];
		Signal[] mem_write = new Signal[ram.length + 4];
		Signal[] mem_write_en = new Signal[ram.length + 4];
		Signal mem_clk = new Signal(1);
		for(int i = 0; i < ram.length + 4; ++i)
		{
			mem_read[i] = new Signal(8);
			mem_write[i] = new Signal(8);
			mem_write_en[i] = new Signal(1);
		}

		GroupSetable dlx_setable = new Processor(env, "dlx").setAll(clk, mem_read, mem_write, mem_write_en, mem_clk, asm.length + 4, false);
		Setable ram_setable = new RAM(env, "ram").setAll(mem_clk, Arrays.copyOf(mem_write, ram.length), Arrays.copyOf(mem_write_en, ram.length), Arrays.copyOf(mem_read, ram.length));
		new InputStreamConnector(env, "in").setAll(mem_clk, Arrays.copyOfRange(mem_write, ram.length, ram.length + 2), Arrays.copyOfRange(mem_write_en, ram.length, ram.length + 2), Arrays.copyOfRange(mem_read, ram.length, ram.length + 2), System.in);
		new OutputStreamConnector(env, "out").setAll(mem_clk, Arrays.copyOfRange(mem_write, ram.length + 2, ram.length + 4), Arrays.copyOfRange(mem_write_en, ram.length + 2, ram.length + 4), Arrays.copyOfRange(mem_read, ram.length + 2, ram.length + 4), System.out);

		long t = System.nanoTime();
		Simulator s = null;
		try { s = env.build(); }
		catch(BuildException e) { e.printStackTrace(); }
		catch(AnalyzeException e) { e.printStackTrace(); }
		System.out.println("Time: " + ((System.nanoTime() - t) / 1000000000.0));

		DLXAssembler.parse(buildASM("io", asm), ((GroupSetable)dlx_setable.getSetableGroup("instruction fetch")).getSetableGroup("instruction memory"));
		Setable program_counter = ((GroupSetable)dlx_setable.getSetableGroup("instruction fetch")).getSetableGroup("program counter");
		Setable[] register_setable = new Setable[31];
		for(int i = 0; i < 31; ++i)
		{
			register_setable[i] = ((GroupSetable)dlx_setable.getSetableGroup("register file")).getSetableGroup("register " + (i + 1));
			Bit[] b = longToBits(32, register[i]);
			for(int j = 0; j < 32; ++j)
				register_setable[i].setSetableBit(j, b[j]);
		}
		for(int i = 0; i < ram_setable.getSetableCount() / 8; ++i)
		{
			Bit[] b = longToBits(8, ram[i]);
			for(int j = 0; j < 8; ++j)
				ram_setable.setSetableBit(i * 8 + j, b[j]);
		}

		Bit[] pc = new Bit[32];
		do
		{
			clk.setBit(0, Bit.H);
			s.doSimulation();
			clk.setBit(0, Bit.L);
			s.doSimulation();

			for(int i = 0; i < 32; ++i)
				pc[i] = program_counter.getSetableBit(i);
		}
		while(bitsToLong(pc) != 2);
	}

	private static final String buildASM(String function, String... commands)
	{
		StringBuilder sb = new StringBuilder();

		sb.append("jal ").append(function).append('\n').append("add r0 r0 r0\nj 0\nadd r0 r0 r0\n");

		for(String c : commands)
		{
			sb.append(c);
			sb.append('\n');
		}

		return sb.toString();
	}
}
