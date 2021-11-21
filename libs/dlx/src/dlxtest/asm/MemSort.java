
package dlxtest.asm;

import core.misc.test.TestSuite;
import java.util.Arrays;

public class MemSort extends DLXRunner
{
	@Override public void test(TestSuite testsuite)
	{
		int[] register = new int[31];

		for(int i = 4; i <= 16; ++i)
		{
			byte[] ram = new byte[i];
			byte[] values = new byte[i];

			String[] asm = new String[]
			{
				"mem_srt:",
				"add r2 r2 r1",

				"mem_srt_ol:",
				"slt r3 r1 r2",
				"add r0 r0 r0",
				"add r0 r0 r0",
				"beqz r3 mem_srt_end",
				"add r0 r0 r0",

				"lb r5 0(r1)",
				"addi r4 r1 1",

				"mem_srt_il:",
				"slt r3 r4 r2",
				"add r0 r0 r0",
				"add r0 r0 r0",
				"beqz r3 mem_srt_il_end",
				"add r0 r0 r0",

				"lb r6 0(r4)",
				"add r0 r0 r0",

				"slt r3 r6 r5",
				"add r0 r0 r0",
				"add r0 r0 r0",
				"beqz r3 mem_srt_il_next",
				"add r0 r0 r0",

				"sb 0(r4) r5",
				"add r5 r6 r0",

				"mem_srt_il_next:",
				"j mem_srt_il",
				"addi r4 r4 1",

				"mem_srt_il_end:",
				"sb 0(r1) r5",
				"j mem_srt_ol",
				"addi r1 r1 1",

				"mem_srt_end:",
				"jr r31",
				"add r0 r0 r0"
			};

			for(int j = 0; j < 3; ++j)
			{
				register[0] = 0;
				register[1] = i;
				for(int k = 0; k < i; ++k)
					values[k] = ram[k] = (byte)testsuite.predictableRandomInteger(256);
				run(testsuite, asm, "mem_srt", register, ram);

				Arrays.sort(values);

				testsuite.assertSignedEquals("Check", values, ram);
			}
		}
	}
}
