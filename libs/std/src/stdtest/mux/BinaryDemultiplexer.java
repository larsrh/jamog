
package stdtest.mux;

import core.signal.Bit;
import core.signal.Signal;

import core.misc.test.TestSuite;
import core.misc.test.Testable;

import java.math.BigInteger;
import static core.misc.BitConverter.*;

public class BinaryDemultiplexer implements Testable
{
	@Override public final void test(TestSuite testsuite)
	{
		for(int i = 1; i < 5; ++i)
		{
			for(int j = 1; j < 20; ++j)
			{
				testsuite.createEnvironment();

				Signal in = new Signal(j);
				Signal sel = new Signal(i);
				Signal[] out = new Signal[1 << i];
				for(int k = 0; k < (1 << i); ++k)
					out[k] = new Signal(j);

				testsuite.addComponent(std.mux.BinaryDemultiplexer.class).setAll(in, sel, out);

				testsuite.buildEnvironment();

				for(int k = 0; k < (1 << i); ++k)
				{
					testsuite.predictableRandomSignal(in);
					sel.setBits(longToBits(i, k));

					testsuite.doSimulation();

					for(int m = 0; m < (1 << i); ++m)
						testsuite.assertEquals("Check[" + m + "]", k == m ? in : BigInteger.ZERO, out[m]);
				}
			}
		}
	}
}
