
package stdtest.latch;

import core.misc.setable.Setable;
import core.signal.Bit;
import core.signal.Signal;

import core.misc.test.TestSuite;
import core.misc.test.Testable;

import static core.signal.Bit.*;

public class SRLatch implements Testable
{
	@Override public final void test(TestSuite testsuite)
	{
		for(int i = 1; i < 15; ++i)
		{
			testsuite.createEnvironment();

			Signal s = new Signal(i);
			Signal r = new Signal(i);
			Signal q = new Signal(i);
			Signal nq = new Signal(i);
			Bit[] old = new Bit[i];
			Bit[] inv = new Bit[i];

			Setable latch = testsuite.addComponent(std.latch.SRLatch.class).setAll(s, r, q, nq);

			testsuite.buildEnvironment();

			for(int j = 0; j < i; ++j)
			{
				old[j] = L;
				latch.setSetableBit(j, L);
			}

			for(int j = 0; j < (1 << i); ++j)
			{
				testsuite.predictableRandomSignal(s);
				testsuite.predictableRandomSignal(r);
				for(int m = 0; m < i; ++m)
					if(s.compareBit(m, H) && r.compareBit(m, H))
					{
						s.setBit(m, L);
						r.setBit(m, L);
					}
					else if(!s.compareBit(m, r.getBit(m)))
					{
						if(s.compareBit(m, H))
							old[m] = H;
						else
							old[m] = L;
					}

				testsuite.doSimulation();

				testsuite.assertEquals("UpdateCheck", old, q);
				for(int m = 0; m < i; ++m)
					inv[m] = nq.getBit(m).not();
				testsuite.assertEquals("InvertCheck", old, inv);
			}
		}
	}
}
