
package stdtest.gate;

import core.signal.Signal;

import core.misc.test.TestSuite;
import core.misc.test.Testable;

import static core.misc.BitConverter.*;

public class NOT implements Testable
{
	@Override public final void test(TestSuite testsuite)
	{
		for(int i = 1; i < 15; ++i)
		{
			testsuite.createEnvironment();

			Signal in = new Signal(i);
			Signal out = new Signal(i);

			testsuite.addComponent(std.gate.NOT.class).setAll(in, out);

			testsuite.buildEnvironment();

			for(int j = 0; j < (1 << i); ++j)
			{
				in.setBits(longToBits(i, j));

				testsuite.doSimulation();

				long l = ~j & ((1 << i) - 1);

				testsuite.assertEquals("Check", l, out);
			}
		}
	}
}
