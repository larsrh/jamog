
package stdtest.logic;

import core.signal.Signal;

import core.misc.test.TestSuite;
import core.misc.test.Testable;

import static core.misc.BitConverter.*;

public class Forward implements Testable
{
	@Override public final void test(TestSuite testsuite)
	{
		for(int i = 1; i < 15; ++i)
		{
			testsuite.createEnvironment();

			Signal in = new Signal(i);
			Signal out = new Signal(i);

			testsuite.addComponent(std.logic.Forward.class).setAll(in, out);

			testsuite.buildEnvironment();

			for(int j = 0; j < (1 << i); ++j)
			{
				in.setBits(longToBits(i, j));

				testsuite.doSimulation();

				testsuite.assertEquals("Check", j, out);
			}
		}
	}
}
