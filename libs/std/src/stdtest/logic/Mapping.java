
package stdtest.logic;

import core.signal.Bit;
import core.signal.Signal;

import core.misc.test.TestSuite;
import core.misc.test.Testable;

import java.util.LinkedHashMap;
import java.util.Map;
import static core.misc.BitConverter.*;

public class Mapping implements Testable
{
	@Override public final void test(TestSuite testsuite)
	{
		for(int i = 1; i < 10; ++i)
		{
			for(int j = 1; j < 10; ++j)
			{
				testsuite.createEnvironment();

				Signal in = new Signal(i);
				Signal out = new Signal(j);
				Map<Integer, Bit[]> imap = new LinkedHashMap<Integer, Bit[]>();
				Map<Bit[], Bit[]> map = new LinkedHashMap<Bit[], Bit[]>();

				for(int k = 0; k < (1 << i); ++k)
				{
					Bit[] bits = new Bit[j];
					testsuite.predictableRandomBits(bits);
					imap.put(k, bits);
					map.put(longToBits(i, k), imap.get(k));
				}

				testsuite.addComponent(std.logic.Mapping.class).setAll(in, out, map);

				testsuite.buildEnvironment();

				for(int k = 0; k < (1 << i); ++k)
				{
					in.setBits(longToBits(i, k));

					testsuite.doSimulation();

					testsuite.assertEquals("Check", imap.get(k), out);
				}
			}
		}
	}
}
