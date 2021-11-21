
package dlxtest.asm;

import core.misc.test.TestSuite;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.util.Arrays;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class Mandelbrot extends DLXRunner
{
	@Override public void test(TestSuite testsuite)
	{
		String[] asm = new String[]
		{
			// r1: width
			// r2: height
			// r3: max_iter
			// r16: max_quad
			// r17: min_cx
			// r18: min_cy
			// r19: dx
			// r20: dy
			"mb:",
			"addi r4 r0 0", // x = 0

			// r29 = 2.0
			"addi r29 r0 2",
			"fcitf r29 r29",

			// while(x < width)
			"mb_fx:",
			"slt r6 r4 r1",
			"add r0 r0 r0",
			"add r0 r0 r0",
			"beqz r6 mb_fx_end",
			"add r0 r0 r0",

			"addi r5 r0 0", // y = 0

			// while(y < height)
			"mb_fy:",
			"slt r6 r5 r2",
			"add r0 r0 r0",
			"add r0 r0 r0",
			"beqz r6 mb_fy_end",
			"add r0 r0 r0",

			// cx = min_cx + x * dx
			"fcitf r21 r4",
			"fmult r21 r21 r19",
			"fadd r21 r21 r17",

			// cy = min_cy + y * dy
			"fcitf r22 r5",
			"fmult r22 r22 r20",
			"fadd r22 r22 r18",

			"fcitf r23 r0", // quad = 0
			"addi r7 r0 0", // iter = 0
			"fcitf r24 r0", // fx = 0
			"fcitf r25 r0", // fy = 0

			// while(quad < max_quad && iter < max_iter)
			"iter:",
			"slt r6 r7 r3",
			"fslt r8 r23 r16",
			"and r6 r6 r8",
			"add r0 r0 r0",
			"add r0 r0 r0",
			"beqz r6 iter_end",
			"add r0 r0 r0",

			// xt = fx^2 - fy^2 + cx
			"fmult r26 r24 r24",
			"fmult r27 r25 r25",
			"fsub r26 r26 r27",
			"fadd r26 r26 r21",

			// yt = 2 * fx * fy + cy
			"fmult r27 r24 r25",
			"fmult r27 r27 r29",
			"fadd r27 r27 r22",

			"add r24 r26 r0", // fx = xt
			"add r25 r27 r0", // fy = yt
			"addi r7 r7 1", // ++iter

			// quad = fx * fx + fy * fy
			"fmult r23 r24 r24",
			"fmult r26 r25 r25",
			"fadd r23 r23 r26",

			"j iter",
			"add r0 r0 r0",

			"iter_end:",

			// save "color"
			"mult r8 r5 r1",
			"add r8 r8 r4",
			"sb 0(r8) r7",

			// ++y
			"j mb_fy",
			"addi r5 r5 1",

			"mb_fy_end:",

			// ++x
			"j mb_fx",
			"addi r4 r4 1",

			"mb_fx_end:",

			"jr r31",
			"add r0 r0 r0"
		};

		final int[] register = new int[31];
		final byte[] ram = new byte[256];

		register[0] = 3;
		register[1] = 2;
		register[2] = 3;

		register[15] = Float.floatToRawIntBits(10.0f);
		register[16] = Float.floatToRawIntBits(-2.0f);
		register[17] = Float.floatToRawIntBits(1.0f);
		register[18] = Float.floatToRawIntBits(3.0f / (register[0] - 1));
		register[19] = Float.floatToRawIntBits(-2.0f / (register[1] - 1));

		run(testsuite, asm, "mb", register, ram);

		EventQueue.invokeLater(new Runnable()
			{
				@Override public void run()
				{
					JFrame frame = new JFrame("Mandelbrot Viewer");

					frame.add(new JPanel()
						{
							@Override protected final void paintComponent(Graphics g)
							{
								g.clearRect(0, 0, getWidth(), getHeight());

								float dx = (float)getWidth() / register[0];
								float dy = (float)getHeight() / register[1];

								for(int y = 0; y < register[1]; ++y)
									for(int x = 0; x < register[0]; ++x)
									{
										float grey = 1.0f - (float)ram[y * register[0] + x] / register[2];
										g.setColor(new Color(grey, grey, grey));
										g.drawRect((int)(x * dx), (int)(y * dy), (int)dx, (int)dy);
										g.fillRect((int)(x * dx), (int)(y * dy), (int)dx, (int)dy);
									}
							}
						}
					);

					frame.setSize(600, 400);

					frame.setVisible(true);
				}
			}
		);

		for(int y = 0; y < register[1]; ++y)
		{
			for(int x = 0; x < register[0]; ++x)
				System.out.print(Integer.toString(ram[y * register[0] + x], register[2] + 1) + " ");
			System.out.println();
		}
	}
}
