
package stdtest.flipflop;

import core.misc.setable.Setable;
import core.signal.Bit;
import core.signal.Signal;

import core.misc.test.TestSuite;
import core.misc.test.Testable;

import static core.signal.Bit.*;

public class DFlipflop implements Testable
{
	@Override public final void test(TestSuite testsuite)
	{
		for(int i = 1; i < 15; ++i)
		{
			testsuite.createEnvironment();

			Signal clk = new Signal(i);
			Signal d = new Signal(i);
			Signal q = new Signal(i);
			Signal nq = new Signal(i);
			Bit[] oclk = new Bit[i];
			Bit[] old = new Bit[i];
			Bit[] inv = new Bit[i];

			Setable flipflop = testsuite.addComponent(std.flipflop.DFlipFlop.class).setAll(clk, d, q, nq);

			testsuite.buildEnvironment();

			for(int j = 0; j < i; ++j)
			{
				old[j] = L;
				flipflop.setSetableBit(j, L);
			}

			for(int j = 0; j < (1 << i); ++j)
			{
				for(int m = 0; m < i; ++m)
					oclk[m] = clk.getBit(m);
				testsuite.predictableRandomSignal(clk);
				for(int m = 0; m < i; ++m)
					if(oclk[m] == L && clk.compareBit(m, H))
						old[m] = d.getBit(m);
				testsuite.predictableRandomSignal(d);

				testsuite.doSimulation();

				testsuite.assertEquals("UpdateCheck", old, q);
				for(int m = 0; m < i; ++m)
					inv[m] = nq.getBit(m).not();
				testsuite.assertEquals("InvertCheck", old, inv);
			}
		}
	}
}
