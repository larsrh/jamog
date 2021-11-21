
package test;

import core.build.Environment;
import core.exception.AnalyzeException;
import core.exception.BuildException;
import core.exception.DeserializingException;
import core.exception.SerializingException;
import core.misc.ClockSimulator;
import core.misc.serial.DeserializingStream;
import core.misc.serial.SerializingStream;
import core.misc.setable.GroupSetable;
import core.misc.setable.Setable;
import core.signal.Bit;
import core.signal.Signal;
import core.sim.Simulator;
import dlx.Processor;
import gui.DLXAssembler;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.filechooser.FileFilter;
import std.memory.RAM;

import static core.misc.BitConverter.*;

public class DLXGUIMandelbrot extends javax.swing.JFrame
{
    public DLXGUIMandelbrot()
	{
        initComponents();

		clock_simulator= null;
		save.setEnabled(false);
		change.setEnabled(false);

		result.setLayout(new BorderLayout());
		result.add(new Viewer(), BorderLayout.CENTER);

		create.addMouseListener(new MouseAdapter() { @Override public final void mouseClicked(MouseEvent e) { create(); } } );
		load.addMouseListener(new MouseAdapter() { @Override public final void mouseClicked(MouseEvent e) { load(); } } );
		save.addMouseListener(new MouseAdapter() { @Override public final void mouseClicked(MouseEvent e) { save(); } } );
		change.addMouseListener(new MouseAdapter() { @Override public final void mouseClicked(MouseEvent e) { change(); } } );
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        javax.swing.JPanel jPanel1 = new javax.swing.JPanel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        create.setText("New");

        load.setText("Load");

        save.setText("Save");

        change.setText("Run");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(create, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(load, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(save, javax.swing.GroupLayout.DEFAULT_SIZE, 57, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(change, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(create)
                    .addComponent(load)
                    .addComponent(save)
                    .addComponent(change))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout resultLayout = new javax.swing.GroupLayout(result);
        result.setLayout(resultLayout);
        resultLayout.setHorizontalGroup(
            resultLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 234, Short.MAX_VALUE)
        );
        resultLayout.setVerticalGroup(
            resultLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 164, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addGap(10, 10, 10)
                .addComponent(result, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(result, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

	public final void create()
	{
		if(clock_simulator != null)
		{
			clock_simulator.removeClockListener(clock_listener);
			clock_simulator.removeStateListener(state_listener);
			clock_simulator.shutdown();
		}

		String[] asm = new String[]
		{
			"mb:",
			"addi r4 r0 0",
			"addi r29 r0 2",
			"fcitf r29 r29",
			"mb_fx:",
			"slt r6 r4 r1",
			"add r0 r0 r0",
			"add r0 r0 r0",
			"beqz r6 mb_fx_end",
			"add r0 r0 r0",
			"addi r5 r0 0",
			"mb_fy:",
			"slt r6 r5 r2",
			"add r0 r0 r0",
			"add r0 r0 r0",
			"beqz r6 mb_fy_end",
			"add r0 r0 r0",
			"fcitf r21 r4",
			"fmult r21 r21 r19",
			"fadd r21 r21 r17",
			"fcitf r22 r5",
			"fmult r22 r22 r20",
			"fadd r22 r22 r18",
			"fcitf r23 r0",
			"addi r7 r0 0",
			"fcitf r24 r0",
			"fcitf r25 r0",
			"iter:",
			"slt r6 r7 r3",
			"fslt r8 r23 r16",
			"and r6 r6 r8",
			"add r0 r0 r0",
			"add r0 r0 r0",
			"beqz r6 iter_end",
			"add r0 r0 r0",
			"fmult r26 r24 r24",
			"fmult r27 r25 r25",
			"fsub r26 r26 r27",
			"fadd r26 r26 r21",
			"fmult r27 r24 r25",
			"fmult r27 r27 r29",
			"fadd r27 r27 r22",
			"add r24 r26 r0",
			"add r25 r27 r0",
			"addi r7 r7 1",
			"fmult r23 r24 r24",
			"fmult r26 r25 r25",
			"fadd r23 r23 r26",
			"j iter",
			"add r0 r0 r0",
			"iter_end:",
			"mult r8 r5 r1",
			"add r8 r8 r4",
			"sb 0(r8) r7",
			"j mb_fy",
			"addi r5 r5 1",
			"mb_fy_end:",
			"j mb_fx",
			"addi r4 r4 1",
			"mb_fx_end:",
			"jr r31",
			"add r0 r0 r0"
		};

		register = new int[31];
		ram = new byte[6];

		register[0] = 3;
		register[1] = 2;
		register[2] = 5;

		register[15] = Float.floatToRawIntBits(20.0f);
		register[16] = Float.floatToRawIntBits(-2.0f);
		register[17] = Float.floatToRawIntBits(1.0f);
		register[18] = Float.floatToRawIntBits(3.0f / (register[0] - 1));
		register[19] = Float.floatToRawIntBits(-2.0f / (register[1] - 1));

		Environment env = new Environment();

		Signal clk = new Signal(1);
		Signal[] mem_read = new Signal[ram.length];
		Signal[] mem_write = new Signal[ram.length];
		Signal[] mem_write_en = new Signal[ram.length];
		Signal mem_clk = new Signal(1);
		for(int i = 0; i < ram.length; ++i)
		{
			mem_read[i] = new Signal(8);
			mem_write[i] = new Signal(8);
			mem_write_en[i] = new Signal(1);
		}

		GroupSetable dlx_setable = new Processor(env, "dlx").setAll(clk, mem_read, mem_write, mem_write_en, mem_clk, asm.length + 4, true);
		ram_setable = new RAM(env, "ram").setAll(mem_clk, mem_write, mem_write_en, mem_read);

		Simulator s = null;
		try { s = env.build(); }
		catch(BuildException e) { }
		catch(AnalyzeException e) { }

		clock_simulator = new ClockSimulator(s, clk, Bit.H);

		if(clock_listener == null)
			clock_listener = new ClockListener();
		if(state_listener == null)
			state_listener = new StateListener();

		clock_simulator.addClockListener(clock_listener);
		clock_simulator.addStateListener(state_listener);

		DLXAssembler.parse(buildASM("mb", asm), ((GroupSetable)dlx_setable.getSetableGroup("instruction fetch")).getSetableGroup("instruction memory"));
		program_counter = ((GroupSetable)dlx_setable.getSetableGroup("instruction fetch")).getSetableGroup("program counter");
		register_setable = new Setable[31];
		for(int i = 0; i < 31; ++i)
		{
			register_setable[i] = ((GroupSetable)dlx_setable.getSetableGroup("register file")).getSetableGroup("register " + (i + 1));
			Bit[] b = longToBits(32, register[i]);
			for(int j = 0; j < 32; ++j)
				register_setable[i].setSetableBit(j, b[j]);
		}
		for(int i = 0; i < ram_setable.getSetableCount() / 8; ++i)
		{
			Bit[] b = longToBits(8, ram[i]);
			for(int j = 0; j < 8; ++j)
				ram_setable.setSetableBit(i * 8 + j, b[j]);
		}

		save.setEnabled(true);
		change.setEnabled(true);
	}

	public final void load()
	{
		File f = displayOpenDialog("Mandelbrot Simulator", "ms");

		if(f != null)
		{
			if(clock_simulator != null)
			{
				clock_simulator.removeClockListener(clock_listener);
				clock_simulator.removeStateListener(state_listener);
				clock_simulator.shutdown();
			}
			
			try
			{
				FileInputStream fis = new FileInputStream(f);
				BufferedInputStream bis = new BufferedInputStream(fis);
				DeserializingStream ds = new DeserializingStream(bis);
				
				clock_simulator = ds.readObject(ClockSimulator.class);
				program_counter = ds.readObject();
				ram_setable = ds.readObject();
				register_setable = ds.readObject();
				register = ds.readObject(int[].class);
				ram = ds.readObject(byte[].class);

				if(clock_listener == null)
					clock_listener = new ClockListener();
				if(state_listener == null)
					state_listener = new StateListener();

				clock_simulator.addClockListener(clock_listener);
				clock_simulator.addStateListener(state_listener);

				ds.close();
			}
			catch(IOException e) { e.printStackTrace(); }
			catch(DeserializingException e) { e.printStackTrace(); }
			catch(core.exception.InstantiationException e) { e.printStackTrace(); }

			save.setEnabled(true);
			change.setEnabled(true);

			result.repaint();
		}
	}

	public final void save()
	{
		File f = displaySaveDialog("Mandelbrot Simulator", "ms");

		if(f != null)
		{
			try
			{
				FileOutputStream fos = new FileOutputStream(f);
				BufferedOutputStream bos = new BufferedOutputStream(fos);
				SerializingStream ss = new SerializingStream(bos);

				clock_simulator.removeClockListener(clock_listener);
				clock_simulator.removeStateListener(state_listener);

				ss.writeObject(clock_simulator, false);
				ss.writeObject(program_counter);
				ss.writeObject(ram_setable);
				ss.writeObject(register_setable);
				ss.writeObject(register, false);
				ss.writeObject(ram, false);

				clock_simulator.addClockListener(clock_listener);
				clock_simulator.addStateListener(state_listener);

				ss.close();
			}
			catch(IOException e) { e.printStackTrace(); }
			catch(SerializingException e) { e.printStackTrace(); }
		}
	}

	public final void change()
	{
		if(clock_simulator.isSuspended())
		{
			create.setEnabled(false);
			load.setEnabled(false);
			save.setEnabled(false);
			clock_simulator.resume();
			change.setText("Pause");
		}
		else
			clock_simulator.suspend();
	}

    public static void main(String args[])
	{
        java.awt.EventQueue.invokeLater(new Runnable()
			{
				public void run()
				{
					new DLXGUIMandelbrot().setVisible(true);
				}
			}
		);
    }

	private ClockSimulator clock_simulator;
	private ClockSimulator.ClockListener clock_listener;
	private ClockSimulator.StateListener state_listener;
	private Setable program_counter;
	private Setable ram_setable;
	private Setable[] register_setable;
	private int[] register;
	private byte[] ram;

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private final javax.swing.JButton change = new javax.swing.JButton();
    private final javax.swing.JButton create = new javax.swing.JButton();
    private final javax.swing.JButton load = new javax.swing.JButton();
    private final javax.swing.JPanel result = new javax.swing.JPanel();
    private final javax.swing.JButton save = new javax.swing.JButton();
    // End of variables declaration//GEN-END:variables

	public final class Viewer extends JPanel
	{
		@Override protected final void paintComponent(Graphics g)
		{
			if(register != null && ram != null)
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
	}

	private final class ClockListener implements ClockSimulator.ClockListener
	{
		@Override public final void finishedClock()
		{
			Bit[] pc = new Bit[32];
			for(int i = 0; i < 32; ++i)
				pc[i] = program_counter.getSetableBit(i);

			if(bitsToLong(pc) == 2)
			{
				for(int i = 0; i < 31; ++i)
				{
					Bit[] b = new Bit[32];
					for(int j = 0; j < 32; ++j)
						b[j] = register_setable[i].getSetableBit(j);

					register[i] = bitsToInteger(true, b).intValue();
				}

				for(int i = 0; i < ram_setable.getSetableCount() / 8; ++i)
				{
					Bit[] b = new Bit[8];
					for(int j = 0; j < 8; ++j)
						b[j] = ram_setable.getSetableBit(i * 8 + j);

					ram[i] = bitsToInteger(true, b).byteValue();
				}

				clock_simulator.suspend();

				result.repaint();
			}
		}

		@Override public final void serialize(SerializingStream out) throws IOException, SerializingException { }
	}

	private final class StateListener implements ClockSimulator.StateListener
	{
		@Override public final void changedState()
		{
			if(clock_simulator.isSuspended())
			{
				create.setEnabled(true);
				load.setEnabled(true);
				save.setEnabled(true);
				change.setText("Run");
			}
		}

		@Override public final void serialize(SerializingStream out) throws IOException, SerializingException { }
	}

	private static final class ExtensionFileFilter extends FileFilter
	{
		private final String name;
		private final String ext;

		public ExtensionFileFilter(String N, String E)
		{
			name = N;
			ext = E;
		}

		@Override public boolean accept(File f)
		{
			if(f.isDirectory())
				return true;
			return checkFileExtension(f.getName(), ext);
		}

		@Override public String getDescription()
		{
			return name + " (*." + ext + ")";
		}
	}

	private static final boolean checkFileExtension(String name, String extension)
	{
		String[] s = name.split("\\.");

		return s[s.length - 1].equals(extension);
	}

	private static final String buildASM(String function, String... commands)
	{
		StringBuilder sb = new StringBuilder();

		sb.append("jal ").append(function).append('\n').append("add r0 r0 r0\nj 0\nadd r0 r0 r0\n");

		for(String c : commands)
		{
			sb.append(c);
			sb.append('\n');
		}

		return sb.toString();
	}

	private final File displayOpenDialog(String name, String ext)
	{
		JFileChooser file_chooser = getFileCooser(name, ext);

		if(file_chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
		{
			File f = file_chooser.getSelectedFile();

			if(!checkFileExtension(f.getName(), ext) || !f.exists() || !f.isFile())
				return null;

			return f;
		}

		return null;
	}

	private final File displaySaveDialog(String name, String ext)
	{
		JFileChooser file_chooser = getFileCooser(name, ext);

		if(file_chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION)
		{
			File f = file_chooser.getSelectedFile();

			if(!checkFileExtension(f.getName(), ext))
				f = new File(f.getPath() + "." + ext);

			return f;
		}

		return null;
	}

	private final JFileChooser getFileCooser(String name, String ext)
	{
		JFileChooser fc = new JFileChooser();
		fc.setAcceptAllFileFilterUsed(false);
		fc.addChoosableFileFilter(new ExtensionFileFilter(name, ext));

		return fc;
	}
}
