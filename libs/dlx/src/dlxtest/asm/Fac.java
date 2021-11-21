
package dlxtest.asm;

import core.misc.test.TestSuite;

public class Fac extends DLXRunner
{
	@Override public void test(TestSuite testsuite)
	{
		int[] register = new int[31];
		byte[] ram = new byte[4];

		for(int i = 20; i <= 20; ++i)
		{
			String[] asm = new String[]
			{
				"fac:",
				"addi r2 r0 1",
				"fac_loop:",
				"slei r3 r1 1",
				"add r0 r0 r0",
				"add r0 r0 r0",
				"bnez r3 fac_end",
				"mult r2 r2 r1",
				"subi r1 r1 1",
				"j fac_loop",
				"add r0 r0 r0",
				"fac_end:",
				"jr r31",
				"add r0 r0 r0"
			};

			register[0] = i;
			run(testsuite, asm, "fac", register, ram);

			int fac = 1;
			for(int j = i; j > 1; --j)
				fac *= j;

			testsuite.assertEquals("Check", fac, register[1]);
		}
	}
}
