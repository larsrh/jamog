
package stdtest.latch;

import core.misc.setable.Setable;
import core.signal.Bit;
import core.signal.Signal;

import core.misc.test.TestSuite;
import core.misc.test.Testable;

import static core.signal.Bit.*;

public class GatedDLatch implements Testable
{
	@Override public final void test(TestSuite testsuite)
	{
		for(int i = 1; i < 15; ++i)
		{
			testsuite.createEnvironment();

			Signal e = new Signal(i);
			Signal d = new Signal(i);
			Signal q = new Signal(i);
			Signal nq = new Signal(i);
			Bit[] old = new Bit[i];
			Bit[] inv = new Bit[i];

			Setable latch = testsuite.addComponent(std.latch.GatedDLatch.class).setAll(e, d, q, nq);

			testsuite.buildEnvironment();

			for(int j = 0; j < i; ++j)
			{
				old[j] = L;
				latch.setSetableBit(j, L);
			}

			for(int j = 0; j < (1 << i); ++j)
			{
				testsuite.predictableRandomSignal(e);
				testsuite.predictableRandomSignal(d);
				for(int m = 0; m < i; ++m)
					if(e.compareBit(m, H))
						old[m] = d.getBit(m);

				testsuite.doSimulation();

				testsuite.assertEquals("UpdateCheck", old, q);
				for(int m = 0; m < i; ++m)
					inv[m] = nq.getBit(m).not();
				testsuite.assertEquals("InvertCheck", old, inv);
			}
		}
	}
}
