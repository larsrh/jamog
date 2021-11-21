
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
import core.signal.Bit;
import core.signal.Signal;
import core.build.Composite;
import std.gate.AND;
import std.mux.BinaryMultiplexer;
import core.build.ComponentCollection;
import core.build.Flavor;
import core.exception.DeserializingException;
import core.misc.serial.DeserializingStream;
import java.io.IOException;
import java.util.Map;

/**
 * @author lars
 */
public class Shifter extends Composite
{
	public Shifter(ComponentCollection parent,String name)
	{
		super(parent,name);
	}

	public final Shifter setAll(Signal in, Signal shift, Signal arithmetic, Signal out)
	{
		return (Shifter)useAndSet(Flavor.DEFAULT, new String[] {"in", "shift", "arithmetic", "out"}, in, shift, arithmetic, out);
	}

	@Override public final Map<String, Flavor> getFlavors()
	{
		return flavors;
	}

	private static final Map<String, Flavor> flavors = Flavor.getMap(
		new Flavor(
			new Flavor.Buildable<Shifter>()
			{
				@Override public void build(Shifter me, Map<String, Integer> variables)
				{
					Signal in = me.getSignal("in");
					Signal fill = new Signal(1);
					Signal shift = me.getSignal("shift");

					Signal zero = new Signal(Bit.L);

					int i=0;
					int count = 1 << me.getSignal("shift").size();
					// calculate the multiplexer inputs for each possible shift value
					Signal multiplexerInput[][] = new Signal[count][in.size()];
					Signal multiplexer[] = new Signal[count];
					for (;i<in.size() && i<count/2;++i) { // positive values
						int j=0;
						for (;j<i;++j)
							multiplexerInput[i][j] = zero;
						for (;j<in.size();++j)
							multiplexerInput[i][j] = in.get(j-i);
						multiplexer[i] = new Signal(multiplexerInput[i]);
					}
					for (;i<count/2;++i) { // just zeros
						for (int j=0;j<in.size();++j)
							multiplexerInput[i][j] = zero;
						multiplexer[i] = new Signal(multiplexerInput[i]);
					}
					for (;i<count-in.size();++i) { // just fill's
						for (int j=0;j<in.size();++j)
							multiplexerInput[i][j] = fill.get(0);
						multiplexer[i] = new Signal(multiplexerInput[i]);
					}
					for (;i<count;++i) { // negative values
						int j=in.size()-1;
						for (int c=0;c < (count - i);--j,++c)
							multiplexerInput[i][j] = fill.get(0);
						for (int offset = in.size() - 1;j >= 0;--j,--offset)
							multiplexerInput[i][j] = in.get(offset);
						multiplexer[i] = new Signal(multiplexerInput[i]);
					}

					// put everything together
					new AND(me,"and") // determine whether we have to fill with H's when shifting arithmetic right
						.setAll(
							new Signal(me.getSignal("arithmetic").get(0),in.get(in.size()-1)),
							fill);
					new BinaryMultiplexer(me,"mux")
						.setAll(multiplexer,shift,me.getSignal("out"));
				}
			},
			"in[n], shift[m], arithmetic[1]",
			"out[n]"
		)
	);

	private Shifter(DeserializingStream in) throws IOException, DeserializingException, InstantiationException
	{
		super(in);
	}
}
