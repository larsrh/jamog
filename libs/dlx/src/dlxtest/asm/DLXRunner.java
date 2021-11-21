
package dlxtest.asm;

import core.misc.setable.GroupSetable;
import core.misc.setable.Setable;
import core.signal.Bit;
import core.signal.Signal;

import core.misc.test.TestSuite;
import core.misc.test.Testable;

import gui.DLXAssembler;
import static core.misc.BitConverter.*;

public abstract class DLXRunner implements Testable
{
	protected final void run(TestSuite testsuite, final String[] asm, final String function, final int[] register, final byte[] ram)
	{
		testsuite.createEnvironment();

		Signal clk = new Signal(1);
		Signal[] mem_read = new Signal[ram.length];
		Signal[] mem_write = new Signal[ram.length];
		Signal[] mem_write_en = new Signal[ram.length];
		Signal mem_clk = new Signal(1);
		for(int i = 0; i < ram.length; ++i)
		{
			mem_read[i] = new Signal(8);
			mem_write[i] = new Signal(8);
			mem_write_en[i] = new Signal(1);
		}

		Bit[] pc = new Bit[32];

		GroupSetable dlx_setable = testsuite.addComponent(dlx.Processor.class).setAll(clk, mem_read, mem_write, mem_write_en, mem_clk, asm.length + 4, true);
		Setable ram_setable = testsuite.addComponent(std.memory.RAM.class).setAll(mem_clk, mem_write, mem_write_en, mem_read);

		testsuite.buildEnvironment();

		DLXAssembler.parse(buildASM(function, asm), ((GroupSetable)dlx_setable.getSetableGroup("instruction fetch")).getSetableGroup("instruction memory"));
		Setable program_counter = ((GroupSetable)dlx_setable.getSetableGroup("instruction fetch")).getSetableGroup("program counter");
		Setable[] regs = new Setable[31];
		for(int i = 0; i < 31; ++i)
		{
			regs[i] = ((GroupSetable)dlx_setable.getSetableGroup("register file")).getSetableGroup("register " + (i + 1));
			Bit[] b = longToBits(32, register[i]);
			for(int j = 0; j < 32; ++j)
				regs[i].setSetableBit(j, b[j]);
		}
		for(int i = 0; i < ram_setable.getSetableCount() / 8; ++i)
		{
			Bit[] b = longToBits(8, ram[i]);
			for(int j = 0; j < 8; ++j)
				ram_setable.setSetableBit(i * 8 + j, b[j]);
		}

		do
		{
			clk.setBit(0, Bit.H);
			testsuite.doSimulation();
			clk.setBit(0, Bit.L);
			testsuite.doSimulation();

			for(int i = 0; i < 32; ++i)
				pc[i] = program_counter.getSetableBit(i);
		}
		while(bitsToLong(pc) != 2);

		for(int i = 0; i < 31; ++i)
		{
			Bit[] b = new Bit[32];
			for(int j = 0; j < 32; ++j)
				b[j] = regs[i].getSetableBit(j);

			register[i] = bitsToInteger(true, b).intValue();
		}

		for(int i = 0; i < ram_setable.getSetableCount() / 8; ++i)
		{
			Bit[] b = new Bit[8];
			for(int j = 0; j < 8; ++j)
				b[j] = ram_setable.getSetableBit(i * 8 + j);

			ram[i] = bitsToInteger(true, b).byteValue();
		}
	}

	private final String buildASM(String function, String... commands)
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
