
package stdtest.mux;

import core.signal.Signal;

import core.misc.test.TestSuite;
import core.misc.test.Testable;

import java.math.BigInteger;
import static core.misc.BitConverter.*;

public class Multiplexer implements Testable
{
	@Override public final void test(TestSuite testsuite)
	{
		for(int i = 2; i < 50; ++i)
		{
			for(int j = 1; j < 20; ++j)
			{
				testsuite.createEnvironment();

				Signal[] in = new Signal[i];
				for(int k = 0; k < i; ++k)
					in[k] = new Signal(j);
				Signal sel = new Signal(i);
				Signal out = new Signal(j);

				testsuite.addComponent(std.mux.Multiplexer.class).setAll(in, sel, out);

				testsuite.buildEnvironment();

				for(int k = 0; k < i; ++k)
				{
					for(int m = 0; m < i; ++m)
						testsuite.predictableRandomSignal(in[m]);
					sel.setBits(integerToBits(i, BigInteger.valueOf(2).pow(k)));

					testsuite.doSimulation();

					testsuite.assertEquals("Check", in[k], out);
				}
			}
		}
	}
}
