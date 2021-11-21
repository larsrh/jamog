
/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *                                                                           *
 * Copyright 2009 Lars Hupel, Torben Maack, Sylvester Tremmel                *
 *                                                                           *
 * This file is part of the Jamog Standard Library.                          *
 *                                                                           *
 * The Jamog Standard Library is free software: you can redistribute         *
 * it and/or modify it under the terms of the GNU General Public License     *
 * as published by the Free Software Foundation; version 3.                  *
 *                                                                           *
 * The Jamog Standard Library is distributed in the hope that it will        *
 * be useful, but WITHOUT ANY WARRANTY; without even the implied warranty    *
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the           *
 * GNU General Public License for more details.                              *
 *                                                                           *
 * You should have received a copy of the GNU General Public License         *
 * along with the Jamog Standard Library. If not, see                        *
 * <http://www.gnu.org/licenses/>.                                           *
 *                                                                           *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

package std.memory;

import core.exception.DeserializingException;
import core.exception.InstantiationException;
import java.io.IOException;
import std.alu.CLAAdder;
import std.alu.RipleIncrementer;
import std.flipflop.DFlipFlop;
import std.flipflop.GatedDFlipFlop;
import std.gate.OR;
import std.logic.Forward;
import std.mux.BinaryDemultiplexer;
import std.mux.BinaryMultiplexer;
import core.build.ComponentCollection;
import core.build.Flavor;
import java.util.LinkedHashMap;
import core.signal.Bit;
import core.misc.BitConverter;
import core.misc.serial.DeserializingStream;
import core.misc.setable.DefaultGroupSetable;
import core.misc.setable.GroupSetableComposite;
import core.misc.setable.Setable;
import core.signal.Signal;
import java.util.Map;

/**
 * @author lars
 */
public final class RingBuffer extends GroupSetableComposite {

	public RingBuffer(ComponentCollection parent,String name)
	{
		super(parent,name);
	}

	public final RingBuffer setAll(Signal in, Signal clk, Signal[] out, Signal counter)
	{
		return (RingBuffer)useAndSet(Flavor.DEFAULT, new String[] {"in", "clk", "out", "counter"}, in, clk, out, counter);
	}

	public final RingBuffer setAll(Signal in, Signal clk, Signal[] out, Signal counter, boolean reverse)
	{
		return (RingBuffer)useAndSet("shifted-output", new String[] {"in", "clk", "out", "counter", "reverse"}, in, clk, out, counter, reverse);
	}

	@Override public final Map<String, Flavor> getFlavors()
	{
		return flavors;
	}

	@Override protected final void init()
	{
		super.init();
		Setable s = getSetableGroup("pointer");
		for (int i=0;i<s.getSetableCount();++i)
			s.setSetableBit(i,Bit.H);
		Setable cells = getSetableGroup("cells");
		for (int i=0;i<cells.getSetableCount();++i)
			cells.setSetableBit(i,Bit.L);
	}

	private static final Map<String, Flavor> flavors = Flavor.getMap(
		new Flavor(
			new Flavor.Buildable<RingBuffer>()
			{
				@Override public void build(RingBuffer me, Map<String, Integer> variables)
				{
					Signal clk = me.getSignal("clk");
					Signal in = me.getSignal("in");
					Signal out[] = (Signal[])me.get("out");

					// prevent counter from being updated from outside
					new Forward(me,"fw").setAll(me.build(clk, in, out), me.getSignal("counter"));
				}
			},
			"in[n], clk[1]",
			"out[m][n], counter[c]",
			"#ceil(log(#m))==#c"
		),
		new Flavor(
			"shifted-output",
			new Flavor.Buildable<RingBuffer>()
			{
				@Override public void build(RingBuffer me, Map<String, Integer> variables)
				{
					Signal clk = me.getSignal("clk");
					Signal in = me.getSignal("in");
					Signal out[] = (Signal[])me.get("out");
					boolean reverse = (Boolean)me.get("reverse");

					final int count = out.length;

					Signal directOutput[] = new Signal[count];
					for (int i=0;i<out.length;++i)
						directOutput[i] = new Signal(in.size());

					Signal counter = me.build(clk, in, directOutput);

					// prevent counter from being updated from outside
					new Forward(me,"fw").setAll(counter, me.getSignal("counter"));

					for (int i=0;i<count;++i) {
						Signal multiplexerInput[] = new Signal[count];
						if (!reverse)
							for (int j=0;j<count;++j)
								multiplexerInput[j] = directOutput[(i+j)%count];
						else
							for (int j=0;j<count;++j)
								multiplexerInput[j] = directOutput[positiveMod(j-i-1,count)];

						new BinaryMultiplexer(me,"mux:"+i).setAll(multiplexerInput,counter,out[i]);
					}
				}
			},
			"in[n], clk[1]",
			"out[m][n], counter[c]",
			"boolean reverse",
			"#ceil(#log(#m))==#c"
		)
	);

	private RingBuffer(DeserializingStream in) throws IOException, DeserializingException, InstantiationException
	{
		super(in);
	}

	private static final int positiveMod(int x,int y)
	{
		return (x%=y) < 0 ? x + y : x;
	}

	private final Signal build(Signal clk,Signal in,Signal out[])
	{
		final int count = out.length;
		final int countersize = 32 - Integer.numberOfLeadingZeros(count - 1);

		Signal enable[] = new Signal[count];
		Signal ramInput[] = new Signal[count];
		Signal counter = new Signal(countersize);

		for (int i=0;i<count;++i) {
			enable[i] = new Signal(1);
			ramInput[i] = in;
		}

		Signal incrementedCounter = new Signal(countersize);
		Signal sub = new Signal(BitConverter.longToBits(countersize, -count));
		Signal subCounter = new Signal(countersize);
		Signal nzeroTest = new Signal(1);
		Signal modCounter = new Signal(countersize);

		new BinaryDemultiplexer(this,"demux").setAll(new Signal(Bit.H), counter, enable);
		new RipleIncrementer(this,"inc").setAll(counter,incrementedCounter);
		new CLAAdder(this,"add").useAndSet("riple",new String[]{"x","y","carryIn","sum","carryOut"},counter, sub, new Signal(Bit.H), subCounter, new Signal(1));
		new OR(this,"or").setAll(subCounter, nzeroTest);
		new BinaryMultiplexer(this,"mod-mux").setAll(new Signal[] {subCounter, incrementedCounter}, nzeroTest, modCounter);
		DFlipFlop flipflop = new DFlipFlop(this,"flipflop").setAll(clk, modCounter, counter);

		LinkedHashMap<String,Setable> cells = new LinkedHashMap<String, Setable>();
		for (int i=0;i<count;++i)
			cells.put("cell:"+i,new GatedDFlipFlop(this,"cell:"+i).setAll(clk,enable[i], in, out[i]));

		addGroup("pointer", flipflop);
		addGroup("cells", new DefaultGroupSetable(cells));
		
		return counter;
	}
}
