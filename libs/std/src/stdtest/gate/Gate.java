
package stdtest.gate;

import core.signal.Signal;

import core.misc.test.TestSuite;
import core.misc.test.Testable;

import static core.misc.BitConverter.*;

public abstract class Gate implements Testable
{
	protected abstract Class<? extends std.gate.Gate> getComponent();
	protected abstract long expected(int i, int j);

	@Override public final void test(TestSuite testsuite)
	{
		for(int i = 2; i < 15; ++i)
		{
			testsuite.createEnvironment();

			Signal in = new Signal(i);
			Signal out = new Signal(1);

			testsuite.addComponent(getComponent()).setAll(in, out);

			testsuite.buildEnvironment();

			for(int j = 0; j < (1 << i); ++j)
			{
				in.setBits(longToBits(i, j));

				testsuite.doSimulation();

				testsuite.assertEquals("Check", expected(i, j), out);
			}
		}
	}
}
