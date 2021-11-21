
package dlxtest;

import core.misc.setable.Setable;
import core.signal.Bit;
import core.signal.Signal;

import core.misc.test.TestSuite;
import core.misc.test.Testable;

import static core.misc.BitConverter.*;
import static core.signal.Bit.*;

public class InstructionFetch implements Testable
{
	@Override public final void test(TestSuite testsuite)
	{
		for(int i = 2; i < 2048; i *= 2)
		{
			testsuite.createEnvironment();

			Signal clk = new Signal(1);
			Signal pcoe = new Signal(1);
			Signal pco = new Signal(32);
			Signal ir = new Signal(32);
			Signal pc = new Signal(32);

			Setable fetch = testsuite.addComponent(dlx.InstructionFetch.class).setAll(clk, pcoe, pco, ir, pc, i);

			testsuite.buildEnvironment();

			for(int j = 0; j < 32; ++j)
				fetch.setSetableBit(j, L);
			Bit[][] instructions = new Bit[i][32];
			for(int j = 0; j < i; ++j)
				testsuite.predictableRandomBits(instructions[j]);
			for(int j = 0; j < i; ++j)
				for(int k = 0; k < 32; ++k)
					fetch.setSetableBit(j * 32 + k + 32, instructions[j][k]);

			int counter = 0;
			for(int j = 0; j < 10000; ++j)
			{
				int oc = counter;
				testsuite.predictableRandomSignal(pco);
				testsuite.predictableRandomSignal(pcoe);
				counter = pcoe.compareBit(0, H) ? (int)signalToLong(pco) : counter + 1;

				clk.setBits(H);
				testsuite.doSimulation();

				testsuite.assertEquals("Programm Counter Check 1", oc, pc);
				testsuite.assertEquals("Instruction Check 1", instructions[oc % i], ir);

				clk.setBits(L);
				testsuite.doSimulation();

				testsuite.assertEquals("Programm Counter Check 2", oc, pc);
				testsuite.assertEquals("Instruction Check 2", instructions[oc % i], ir);
			}
		}
	}
}
