
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
public class UnsignedMultiplier extends Composite {

	public UnsignedMultiplier(ComponentCollection parent,String name)
	{
		super(parent,name);
	}

	public final UnsignedMultiplier setAll(Signal x, Signal y, Signal out)
	{
		return (UnsignedMultiplier)useAndSet(Flavor.DEFAULT, new String[] {"x", "y", "out"}, x, y, out);
	}

	@Override public final Map<String, Flavor> getFlavors()
	{
		return flavors;
	}

	private static final Map<String, Flavor> flavors = Flavor.getMap(
		new Flavor(
			new Flavor.Buildable<UnsignedMultiplier>()
			{
				@Override public void build(UnsignedMultiplier me, Map<String, Integer> variables)
				{
					Signal x = me.getSignal("x");
					Signal y = me.getSignal("y");
					Signal out = me.getSignal("out");

					Signal adderInput[] = new Signal[x.size()];
					Signal zeros = new Signal(Bit.L,out.size());

					for (int i=0;i<x.size();++i)
						new BinaryMultiplexer(me,"mux:" + i)
							.setAll(new Signal[]{zeros,
							                     i == 0 ?
						                             new Signal(y,new Signal(x.size())) :
							                         new Signal(new Signal(Bit.L,i),y,new Signal(Bit.L,x.size()-i))},
									x.get(i),
									adderInput[i] = new Signal(out.size()));

					new CLAAdder(me, "add").useAndSet("multi", new String[] {"in","sum","chunk_count"},
						adderInput,
						new Signal(out,new Signal((int)Math.ceil(Math.log(x.size())/Math.log(2)))),
						x.size() / 4
					);
				}
			},
			"x[n], y[m]",
			"out[n+m]"
		)
	);

	private UnsignedMultiplier(DeserializingStream in) throws IOException, DeserializingException, InstantiationException
	{
		super(in);
	}
}
