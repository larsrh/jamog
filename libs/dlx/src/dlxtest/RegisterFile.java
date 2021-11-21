
package dlxtest;

import core.misc.setable.Setable;
import core.signal.Bit;
import core.signal.Signal;

import core.misc.test.TestSuite;
import core.misc.test.Testable;

import static core.signal.Bit.*;

public class RegisterFile implements Testable
{
	@Override public final void test(TestSuite testsuite)
	{
		testsuite.createEnvironment();

		Signal clk = new Signal(1);
		Signal[] write = new Signal[33];
		for(int i = 0; i < 33; ++i)
			write[i] = new Signal(32);
		Signal[] write_en = new Signal[33];
		for(int i = 0; i < 33; ++i)
			write_en[i] = new Signal(1);
		Signal[] read = new Signal[32];
		for(int i = 0; i < 32; ++i)
			read[i] = new Signal(32);

		Signal[] write31 = new Signal[31];
		Signal[] write_en31 = new Signal[31];
		Signal[] write33 = new Signal[2];
		Signal[] write_en33 = new Signal[2];
		System.arraycopy(write, 0, write31, 0, 31);
		System.arraycopy(write_en, 0, write_en31, 0, 31);
		System.arraycopy(write, 31, write33, 0, 2);
		System.arraycopy(write_en, 31, write_en33, 0, 2);
		Setable reg = testsuite.addComponent(dlx.RegisterFile.class).setAll(clk, write31, write_en31, write33, write_en33, read);

		testsuite.buildEnvironment();

		Bit[][] bits = new Bit[32][32];
		for(int i = 0; i < 32; ++i)
			bits[0][i] = L;
		for(int i = 1; i < 32; ++i)
		{
			testsuite.predictableRandomBits(bits[i]);
			for(int j = 0; j < 32; ++j)
				reg.setSetableBit((i - 1) * 32 + j, bits[i][j]);
		}

		for(int i = 0; i < 100000; ++i)
		{
			Bit[] nbits = null;
			int we = testsuite.predictableRandomInteger(33);

			for(int j = 0; j < 33; ++j)
			{
				testsuite.predictableRandomSignal(write[j]);
				if(j == we)
				{
					nbits = write[j].getBits();
					write_en[j].setBits(H);
				}
				else
					write_en[j].setBits(L);
			}

			clk.setBits(H);
			testsuite.doSimulation();

			testsuite.assertEquals("Register Check 1", read, bits);

			clk.setBits(L);
			testsuite.doSimulation();

			if(we != 0 && we < 31)
				for(int j = 0; j < 32; ++j)
					bits[we][j] = nbits[j];
			else if(we > 30)
				for(int j = 0; j < 32; ++j)
					bits[31][j] = nbits[j];

			testsuite.assertEquals("Register Check 2", read, bits);
		}
	}
}
