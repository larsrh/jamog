
package test;

import core.exception.SerializingException;
import core.misc.setable.Setable;
import core.misc.setable.GroupSetable;
import core.misc.setable.DefaultGroupSetable;
import dlx.Processor;
import std.memory.RAM;
import core.misc.serial.SerializingStream;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import core.signal.*;
import core.build.*;
import core.exception.AnalyzeException;
import core.exception.BuildException;
import core.sim.*;
import core.monitor.*;
import core.misc.*;
import gui.AssemblerEditor;
import gui.torben.GroupSetableForm;
import gui.torben.WatchesForm;
import java.awt.BorderLayout;
import java.util.LinkedHashMap;
import java.util.Map;

public class DLXGUITest extends javax.swing.JFrame
{
	private ClockSimulator cs;
	private Processor dlx;
	private RAM ram;
	private long time;

	public DLXGUITest()
	{
		initComponents();

		build();

		next.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent e)
			{
				next.setEnabled(false);
				cs.resume();
			}
		});

		run.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent e)
			{
				time = System.nanoTime();
				pc.setEnabled(false);
				run.setEnabled(false);
				cs.resume();
			}
		});

		reset.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent e)
			{
				cs.shutdown();
				build();
			}
		});
	}

	private final void build()
	{
		Signal clk = new Signal(1);
		Signal[] mem_read = new Signal[64];
		Signal[] mem_write = new Signal[64];
		Signal[] mem_write_en = new Signal[64];
		Signal mem_clk = new Signal(1);
		for(int i = 0; i < 64; ++i)
		{
			mem_read[i] = new Signal(8);
			mem_write[i] = new Signal(8);
			mem_write_en[i] = new Signal(1);
		}

		Environment env = new Environment();
		env.addComponent((dlx = new Processor(null, "dlx")).setAll(clk, mem_read, mem_write, mem_write_en, mem_clk, 128, true));
		env.addComponent((ram = new RAM(null, "ram")).setAll(mem_clk, mem_write, mem_write_en, mem_read));

		Simulator s = null;
		try
		{
			long t = System.nanoTime();
			s = env.build(new AnalyzeListener() {

				@Override
				public void initTotals(int calculators, int priority_pairs)
				{
					System.out.println(calculators + " calculators");
				}

				@Override
				public void increaseCalculators(int pass, int finished)
				{

				}

				@Override
				public void increasePriorityPairs(int pass, int finished)
				{

				}
			});
			System.out.println("Build-Time: " + (System.nanoTime() - t) / 1000000000.0);
		}
		catch(AnalyzeException ae)
		{
			ae.printStackTrace();
			System.exit(1);
		}
		catch(BuildException be)
		{
		    be.printStackTrace();
		    System.exit(1);
		}
		cs = new ClockSimulator(s, clk, Bit.H);
		cs.addClockListener(
			new ClockSimulator.ClockListener()
			{
				@Override public void finishedClock()
				{
					if(!next.isEnabled())
					{
						next.setEnabled(true);
						cs.suspend();
					}
					else if(!run.isEnabled())
					{
						Setable s = ((GroupSetable)dlx.getSetableGroup("instruction fetch")).getSetableGroup("program counter");
						Bit[] bits = new Bit[32];
						for(int i = 0; i < 32; ++i)
							bits[i] = s.getSetableBit(i);
						if(BitConverter.bitsToLong(bits) == Long.valueOf(pc.getText()))
						{
							run.setEnabled(true);
							pc.setEnabled(true);
							cs.suspend();
							System.out.println("Time: " + (System.nanoTime() - time) / 1000.0 / 1000.0 / 1000.0 + " s");
						}
					}
				}

			@Override
			public void serialize(SerializingStream out) throws IOException, SerializingException
			{
				throw new UnsupportedOperationException("Not supported yet.");
			}

				
			}
		);

		ComponentListener cl = new ComponentListener() {

					public void componentChanged(Calculator changed, Component affected)
					{
						StringBuilder sb = new StringBuilder(affected.getName() + " changed at " + changed.getName() + ":\n\tparams:\n");
						for(Map.Entry<String, Object> e : affected.getParameters().entrySet())
						{
							if(e.getValue() instanceof Object[])
							{
								int i = 0;
								for(Object o : (Object[])e.getValue())
									sb.append("\t\t" + e.getKey() + ":" + i++ + " : " + o + "\n");
							}
							else
								sb.append("\t\t" + e.getKey() + " : " + e.getValue() + "\n");
						}

						System.out.print(sb.toString());
					}

			public void serialize(SerializingStream out) throws IOException
			{
				throw new UnsupportedOperationException("Not supported yet.");
			}
		};

		/*ram.getComponent("latch:0").addComponentListener(cl);
		dlx.getComponent("memory-access.store_en:0").addComponentListener(cl);
		dlx.getComponent("memory-access.write-demux").addComponentListener(cl);
		((Signal)dlx.getComponent("memory-access.store_en:0").getParameters().get("in")).addSignalListener(new SignalListener() {

			public void signalChanged(Signal changed_signal, SignalBit changed_bit, Bit old_value, Bit new_value)
			{
				System.out.println("in@memory-access.store_en:0 changed to " + changed_signal);
			}

			public void serialize(SerializingStream out) throws IOException
			{
				throw new UnsupportedOperationException("Not supported yet.");
			}
		});
		((Signal)dlx.getComponent("executor").getParameters().get("out")).addSignalListener(new SignalListener() {

			@Override public void signalChanged(Signal changed_signal, SignalBit changed_bit, Bit old_value, Bit new_value)
			{
				System.out.println("out@executor changed to " + changed_signal);
			}

			@Override public void serialize(SerializingStream out) throws IOException
			{
				throw new UnsupportedOperationException("Not supported yet.");
			}
		});*/

		asm.setViewportView(new AssemblerEditor(((GroupSetable)dlx.getSetableGroup("instruction fetch")).getSetableGroup("instruction memory")));

		Map<String, Setable> setables = new LinkedHashMap<String, Setable>();
		setables.put("dlx", dlx);
		setables.put("ram", ram);

		setable_form.setViewportView(GroupSetableForm.create(new DefaultGroupSetable(setables)));
		//watches_form.setViewportView(new WatchesForm(dlx));
		watches_form.setLayout(new BorderLayout());
		watches_form.add(new WatchesForm(dlx), BorderLayout.CENTER);
		watches_form.validate();
	}

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        next = new javax.swing.JButton();
        asm = new javax.swing.JScrollPane();
        reset = new javax.swing.JButton();
        setable_form = new javax.swing.JScrollPane();
        pc = new javax.swing.JTextField();
        run = new javax.swing.JButton();
        watches_form = new javax.swing.JPanel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        next.setText("Next Step");

        reset.setText("Reset");

        run.setText("Run");

        javax.swing.GroupLayout watches_formLayout = new javax.swing.GroupLayout(watches_form);
        watches_form.setLayout(watches_formLayout);
        watches_formLayout.setHorizontalGroup(
            watches_formLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 227, Short.MAX_VALUE)
        );
        watches_formLayout.setVerticalGroup(
            watches_formLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 463, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(asm, javax.swing.GroupLayout.PREFERRED_SIZE, 192, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(setable_form, javax.swing.GroupLayout.DEFAULT_SIZE, 293, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(watches_form, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(next)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(reset)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(run)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(pc, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(next)
                    .addComponent(reset)
                    .addComponent(run)
                    .addComponent(pc, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(watches_form, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(asm, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 463, Short.MAX_VALUE)
                    .addComponent(setable_form, javax.swing.GroupLayout.DEFAULT_SIZE, 463, Short.MAX_VALUE))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

	public static void main(String args[])
	{
		java.awt.EventQueue.invokeLater(
			new Runnable()
			{
				public void run()
				{
					new DLXGUITest().setVisible(true);
				}
			}
		);
	}

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane asm;
    private javax.swing.JButton next;
    private javax.swing.JTextField pc;
    private javax.swing.JButton reset;
    private javax.swing.JButton run;
    private javax.swing.JScrollPane setable_form;
    private javax.swing.JPanel watches_form;
    // End of variables declaration//GEN-END:variables

}
