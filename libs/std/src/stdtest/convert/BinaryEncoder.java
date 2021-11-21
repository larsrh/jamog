
package stdtest.convert;

import core.signal.Signal;

import core.misc.test.TestSuite;
import core.misc.test.Testable;

import java.math.BigInteger;
import static core.misc.BitConverter.*;

public class BinaryEncoder implements Testable
{
	@Override public final void test(TestSuite testsuite)
	{
		for(int i = 1; i < 10; ++i)
		{
			testsuite.createEnvironment();

			Signal in = new Signal(1 << i);
			Signal out = new Signal(i);

			testsuite.addComponent(std.convert.BinaryEncoder.class).setAll(in, out);

			testsuite.buildEnvironment();

			for(int j = 0; j < (1 << i); ++j)
			{
				in.setBits(integerToBits(1 << i, BigInteger.ZERO.setBit(j)));

				testsuite.doSimulation();
				
				testsuite.assertEquals("Check", j, out);
			}
		}
	}
}
