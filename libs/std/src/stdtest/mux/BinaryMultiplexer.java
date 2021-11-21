
package stdtest.mux;

import core.signal.Signal;

import core.misc.test.TestSuite;
import core.misc.test.Testable;

import static core.misc.BitConverter.*;

public class BinaryMultiplexer implements Testable
{
	@Override public final void test(TestSuite testsuite)
	{
		for(int i = 1; i < 5; ++i)
		{
			for(int j = 1; j < 20; ++j)
			{
				testsuite.createEnvironment();

				Signal[] in = new Signal[1 << i];
				for(int k = 0; k < (1 << i); ++k)
					in[k] = new Signal(j);
				Signal sel = new Signal(i);
				Signal out = new Signal(j);

				testsuite.addComponent(std.mux.BinaryMultiplexer.class).setAll(in, sel, out);

				testsuite.buildEnvironment();

				for(int k = 0; k < (1 << i); ++k)
				{
					for(int m = 0; m < (1 << i); ++m)
						testsuite.predictableRandomSignal(in[m]);
					sel.setBits(longToBits(i, k));

					testsuite.doSimulation();

					testsuite.assertEquals("Check", in[k], out);
				}
			}
		}
	}
}
