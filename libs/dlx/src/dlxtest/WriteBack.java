
package dlxtest;

import core.signal.Bit;
import core.signal.Signal;

import core.misc.test.TestSuite;
import core.misc.test.Testable;

import static core.misc.BitConverter.*;
import static core.signal.Bit.*;

public class WriteBack implements Testable
{
	@Override public final void test(TestSuite testsuite)
	{
		testsuite.createEnvironment();

		Signal alu = new Signal(32);
		Signal load = new Signal(32);
		Signal res_sel = new Signal(1);
		Signal we = new Signal(1);
		Signal ra = new Signal(5);
		Signal afw = new Signal(5);
		Signal rfw = new Signal(32);
		Signal[] write = new Signal[32];
		for(int i = 0; i < 32; ++i)
			write[i] = new Signal(32);
		Signal[] write_en = new Signal[32];
		for(int i = 0; i < 32; ++i)
			write_en[i] = new Signal(1);

		Bit[] az = new Bit[5];
		for(int i = 0; i < 5; ++i)
			az[i] = L;
		Bit[] rz = new Bit[32];
		for(int i = 0; i < 32; ++i)
			rz[i] = L;

		testsuite.addComponent(dlx.WriteBack.class).setAll(alu, load, res_sel, we, ra, afw, rfw, write, write_en);

		testsuite.buildEnvironment();

		for(int i = 0; i < 100000; ++i)
		{
			testsuite.predictableRandomSignal(ra);
			testsuite.predictableRandomSignal(we);
			int reg = (int)signalToLong(ra);

			testsuite.predictableRandomSignal(alu);
			testsuite.predictableRandomSignal(load);
			testsuite.predictableRandomSignal(res_sel);

			Bit[] result = (res_sel.compareBit(0, L) ? alu : load).getBits();

			testsuite.doSimulation();

			if(we.compareBits(H) && reg != 0)
			{
				testsuite.assertEquals("ALU Address Forward Check", ra, afw);
				testsuite.assertEquals("ALU Register Forward Check", result, rfw);
			}
			else
			{
				testsuite.assertEquals("ALU Address Block Check", az, afw);
				testsuite.assertEquals("ALU Register Block Check", rz, rfw);
			}

			for(int j = 0; j < 32; ++j)
			{
				if(j == reg && we.compareBit(0, H))
				{
					testsuite.assertEquals("RegisterWriteEnable[" + j + "] Check", H, write_en[j].getBit(0));
					testsuite.assertEquals("RegisterWrite Check", result, write[j]);
				}
				else
					testsuite.assertEquals("RegisterWriteEnable[" + j + "] Check", L, write_en[j].getBit(0));
			}
		}
	}
}
