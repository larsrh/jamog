
package stdtest.mux;

import core.signal.Signal;

import core.misc.test.TestSuite;
import core.misc.test.Testable;

import java.math.BigInteger;
import static core.misc.BitConverter.*;

public class Demultiplexer implements Testable
{
	@Override public final void test(TestSuite testsuite)
	{
		for(int i = 2; i < 50; ++i)
		{
			for(int j = 1; j < 20; ++j)
			{
				testsuite.createEnvironment();

				Signal in = new Signal(j);
				Signal sel = new Signal(i);
				Signal[] out = new Signal[i];
				for(int k = 0; k < i; ++k)
					out[k] = new Signal(j);

				testsuite.addComponent(std.mux.Demultiplexer.class).setAll(in, sel, out);

				testsuite.buildEnvironment();

				for(int k = 0; k < i; ++k)
				{
					testsuite.predictableRandomSignal(in);
					sel.setBits(integerToBits(i, BigInteger.valueOf(2).pow(k)));

					testsuite.doSimulation();

					for(int m = 0; m < i; ++m)
						testsuite.assertEquals("Check[" + m + "]", k == m ? in : BigInteger.ZERO, out[m]);
				}
			}
		}
	}
}
