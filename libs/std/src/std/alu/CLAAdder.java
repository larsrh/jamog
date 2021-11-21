
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

package std.alu;

import core.exception.InstantiationException;
import core.build.ComponentCollection;
import core.build.Composite;
import core.build.Flavor;
import core.exception.DeserializingException;
import core.misc.serial.DeserializingStream;
import core.signal.Bit;
import core.signal.Signal;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import std.gate.AND;
import std.gate.OR;
import std.gate.XOR;
import std.logic.Forward;

/**
 * @author lars
 */
public final class CLAAdder extends Composite
{
	public CLAAdder(ComponentCollection parent,String name)
	{
		super(parent,name);
	}

	private final Map<String, Flavor> flavors = Flavor.getMap(
		new Flavor(
			new Flavor.Buildable<CLAAdder>()
			{
				@Override public void build(CLAAdder me, Map<String, Integer> variables)
				{
					me.buildCLAChunk("",me.getSignal("x"), me.getSignal("y"), me.getSignal("carryIn"), me.getSignal("sum"), me.getSignal("carryOut"), null);
				}
			},
			"x[n], y[n], carryIn[1]",
			"sum[n], carryOut[1]"
		),
		new Flavor(
			"riple",
			new Flavor.Buildable<CLAAdder>()
			{
				@Override public void build(CLAAdder me, Map<String, Integer> variables)
				{
					me.buildRipleChunk("",me.getSignal("x"), me.getSignal("y"), me.getSignal("carryIn"), me.getSignal("sum"), me.getSignal("carryOut"), null);
				}
			},
			"x[n], y[n], carryIn[1]",
			"sum[n], carryOut[1]"
		),
		new Flavor(
			"extended",
			new Flavor.Buildable<CLAAdder>()
			{
				@Override public void build(CLAAdder me, Map<String, Integer> variables)
				{
					me.buildChunkedCLAAdder((Integer)get("chunk_count"), me.getSignal("x"), me.getSignal("y"), me.getSignal("carryIn"), me.getSignal("sum"), me.getSignal("carryOut"), new Signal(1));
				}
			},
			"x[n], y[n], carryIn[1]",
			"sum[n], carryOut[1]",
			"int chunk_count",
			"#chunk_count > 0 && #chunk_count <= #n"
		),
		new Flavor(
			"multi",
			new Flavor.Buildable<CLAAdder>()
			{
				@Override public void build(CLAAdder me, Map<String, Integer> variables)
				{
					Signal in[] = (Signal[])get("in");

					Queue<Signal> summands = new LinkedList<Signal>();
					for (Signal s : in)
						summands.add(new Signal(s,new Signal(Bit.L,variables.get("c"))));

					int i = 0;
					while (summands.size() > 1) {
						Signal x = summands.poll();
						Signal y = summands.poll();

						Signal sum = summands.isEmpty() ? getSignal("sum") : new Signal(y.size());

						new CLAAdder(me, "chunk:"+(i++)).useAndSet("extended", new String[] {"x","y","carryIn","sum","carryOut","chunk_count"},
							x,
							y,
							new Signal(Bit.L),
							sum,
							new Signal(1),
							get("chunk_count")
						);

						summands.add(sum);
					}
				}
			},
			"in[m][n]",
			"sum[n+c]",
			"int chunk_count",
			"#chunk_count > 0 && #chunk_count <= (#n+#c) && #ceil(log(#m))==#c"
		)
	);

	private CLAAdder(DeserializingStream in) throws IOException, DeserializingException, InstantiationException
	{
		super(in);
	}

	private final void buildChunkedCLAAdder(int chunkCount, Signal x, Signal y, Signal carryIn, Signal sum, Signal carryOut, Signal propagateOut) {
		final int width = sum.size();
		final int innerWidth = width / chunkCount;

		Signal generate[] = new Signal[chunkCount];
		Signal carry[] = new Signal[chunkCount+1];
		Signal propagate[] = new Signal[chunkCount];

		carry[0] = carryIn;

		if (chunkCount == 1) {
			buildRipleChunk("",x, y, carryIn, sum, carryOut, propagateOut);
			return;
		}

		for (int i=0;i<=chunkCount;++i) {
			if (i >= 1) {
				carry[i] = (i < chunkCount) ? new Signal(1) : carryOut;
				buildCLA("cla:", i, carry, propagate, generate);
			}

			if (i < chunkCount) {
				generate[i] = new Signal(1);
				propagate[i] = new Signal(1);
				int offset = i * innerWidth;

				int selection = (i < chunkCount-1) ? innerWidth : width-offset;

				buildRipleChunk("chunk:"+i+":",x.get(offset,selection), y.get(offset,selection), carry[i], sum.get(offset,selection), generate[i], propagate[i]);
			}
		}
	}

	private final void buildCLA(String name,int i,Signal carry[],Signal propagate[],Signal generate[]) {
		Signal or[] = new Signal[i+1];
		for (int j = 0;j <= i;++j) {
			Signal and[] = new Signal[j+1];
			for (int k = 0;k < j;++k)
				and[k] = propagate[i-1-k];

			and[j] = (j == i) ? carry[0] : generate[i-j-1];

			if (and.length >= 2) {
				or[j] = new Signal(1);
				new AND(this,name+"and:" + i + ":" + j).setAll(new Signal(and), or[j]);
			}
			else { // minimum length 1
				or[j] = and[0];
			}
		}
		new OR(this,name+"or:" + i).setAll(new Signal(or), carry[i]);
	}

	private final void buildRipleChunk(String name, Signal x, Signal y, Signal carryIn, Signal sum, Signal carryOut, Signal propagateOut) {
		final int width = sum.size();

		if (propagateOut != null)
			if (width > 1) {
				Signal prop = new Signal(width);
				for (int j=0;j<width;++j)
					new XOR(this, name+"prop-xor:"+j).setAll(new Signal(x.get(j),x.get(j)), prop.get(j));
				new AND(this, name+"prop-and").setAll(prop, propagateOut);
			}
			else {
				new XOR(this, name+"prop-xor").setAll(new Signal(x,y), propagateOut);
			}

		Signal input = carryIn;
		for(int i = 0; i < width; ++i) {
			Signal output =
					(i == (width - 1)) ?
						carryOut :
						new Signal(1);

			new FullAdder(this, name+"add:" + i).setAll(x.get(i), y.get(i), input, sum.get(i), output);

			input = output;
		}
	}

	private final void buildCLAChunk(String name, Signal x, Signal y, Signal carryIn, Signal sum, Signal carryOut, Signal propagateOut) {
		final int width = sum.size();

		Signal generate[] = new Signal[width];
		Signal carry[] = new Signal[width+1];
		Signal propagate[] = new Signal[width];
		Signal result[] = new Signal[width];
		carry[0] = carryIn;

		for (int i=0;i<=width;++i) {
			if (i >= 1) {
				carry[i] = (i < width) ? new Signal(1) : carryOut;
				buildCLA(name+"cla:", i, carry, propagate, generate);
			}

			if (i < width) {
				generate[i] = new Signal(1);
				propagate[i] = new Signal(1);
				result[i] = sum.get(i);

				new AND(this,name+"generate-and:" + i).setAll(new Signal(x.get(i),y.get(i)),generate[i]);
				new XOR(this,name+"propagate-xor:" + i).setAll(new Signal(x.get(i),y.get(i)),propagate[i]);
				new XOR(this,name+"result-xor:" + i).setAll(new Signal(propagate[i],carry[i]),result[i]);
			}
		}

		if (propagateOut != null)
			if (width > 1)
				new AND(this,name+"propagate-out").setAll(new Signal(propagate), propagateOut);
			else
				new Forward(this,name+"propagate-out").setAll(propagate[0], propagateOut);

	}

	@Override
	public Map<String, Flavor> getFlavors() {
		return flavors;
	}
	
}
