
package dlxtest;

import core.misc.setable.Setable;
import core.signal.Bit;
import core.signal.Signal;

import core.misc.test.TestSuite;
import core.misc.test.Testable;

import static core.signal.Bit.*;
import static core.misc.BitConverter.*;

public class MemoryAccess implements Testable
{
	@Override public final void test(TestSuite testsuite)
	{
		for(int i = 4; i < 1024; i <<= 1)
		{
			testsuite.createEnvironment();

			Signal clk = new Signal(1);
			Signal mem_clk = new Signal(1);

			Signal address = new Signal(32);
			Signal load_en = new Signal(3);
			Signal store_en = new Signal(2);
			Signal store = new Signal(32);
			Signal[] read = new Signal[i];
			for(int j = 0; j < i; ++j)
				read[j] = new Signal(8);

			Signal load = new Signal(32);
			final Signal[] write = new Signal[i];
			final Signal[] write_en = new Signal[i];
			for(int j = 0; j < i; ++j)
			{
				write[j] = new Signal(8);
				write_en[j] = new Signal(1);
			}

			testsuite.addComponent(dlx.MemoryAccess.class).setAll(address, load_en, store_en, store, read, clk, load, write, write_en, mem_clk);
			Setable ram = testsuite.addComponent(std.memory.RAM.class).setAll(mem_clk, write, write_en, read);

			testsuite.buildEnvironment();

			Bit[][] bits = new Bit[i][8];
			for(int j = 0; j < i; ++j)
			{
				testsuite.predictableRandomBits(bits[j]);
				for(int k = 0; k < 8; ++k)
					ram.setSetableBit(j * 8 + k, bits[j][k]);
			}

			for(int j = 0; j < 100 * i; ++j)
			{
				final int a = testsuite.predictableRandomInteger(i);
				address.setBits(longToBits(32, a));
				testsuite.predictableRandomSignal(store);

				int ld = testsuite.predictableRandomInteger(5);
				load_en.setBits(longToBits(3, ld));
				final int s	= testsuite.predictableRandomInteger(4);
				store_en.setBits(longToBits(2, s));

				switch(s)
				{
				case 1:
					for(int k = 0; k < 8; ++k)
						bits[a][k] = store.getBit(k);
					break;
				case 2:
					for(int k = 0; k < 8; ++k)
					{
						bits[a][k] = store.getBit(k);
						bits[(a + 1) % i][k] = store.getBit(k + 8);
					}
					break;
				case 3:
					for(int k = 0; k < 8; ++k)
					{
						bits[a][k] = store.getBit(k);
						bits[(a + 1) % i][k] = store.getBit(k + 8);
						bits[(a + 2) % i][k] = store.getBit(k + 16);
						bits[(a + 3) % i][k] = store.getBit(k + 24);
					}
				}

				clk.setBits(H);
				testsuite.doSimulation();
				clk.setBits(L);
				testsuite.doSimulation();

				testsuite.assertEquals("Store Check", bits, read);

				switch(ld)
				{
				case 0:
					testsuite.assertSignedEquals("Load Check", read[a], load);
					break;
				case 1:
					testsuite.assertEquals("Load Check", read[a], load);
					break;
				case 2:
					testsuite.assertSignedEquals("Load Check", new Signal(read[a], read[(a + 1) % i]), load);
					break;
				case 3:
					testsuite.assertEquals("Load Check", new Signal(read[a], read[(a + 1) % i]), load);
					break;
				case 4:
					testsuite.assertEquals("Load Check", new Signal(read[a], read[(a + 1) % i], read[(a + 2) % i], read[(a + 3) % i]), load);
				}
			}
		}
	}
}
