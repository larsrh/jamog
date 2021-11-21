
package stdtest.logic;

import core.signal.Bit;
import core.signal.Signal;

import core.misc.test.TestSuite;
import core.misc.test.Testable;

import static core.signal.Bit.*;

public class ThreeState implements Testable
{
	@Override public final void test(TestSuite testsuite)
	{
		for(int i = 1; i < 15; ++i)
		{
			testsuite.createEnvironment();

			Signal in = new Signal(i);
			Signal ctrl = new Signal(i);
			Signal out = new Signal(i);
			Bit[] oout = new Bit[i];
			for(int j = 0; j < i; ++j)
				oout[j] = Z;

			testsuite.addComponent(std.logic.ThreeState.class).setAll(in, ctrl, out);

			testsuite.buildEnvironment();

			for(int j = 0; j < (1 << i); ++j)
			{
				testsuite.predictableRandomSignal(in);
				testsuite.predictableRandomSignal(ctrl);

				testsuite.doSimulation();

				for(int m = 0; m < i; ++m)
					if(ctrl.compareBit(m, H))
						oout[m] = in.getBit(m);

				testsuite.assertEquals("Check", oout, out);
			}
		}
	}
}
