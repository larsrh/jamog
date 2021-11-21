
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
import std.logic.Forward;
import std.mux.BinaryMultiplexer;
import core.build.ComponentCollection;
import core.signal.Bit;
import core.build.Composite;
import core.build.Flavor;
import core.exception.DeserializingException;
import core.misc.serial.DeserializingStream;
import core.signal.Signal;
import java.io.IOException;
import java.util.Map;

/**
 * @author lars
 */
public class UnsignedDivider extends Composite
{
	public UnsignedDivider(ComponentCollection parent,String name)
	{
		super(parent,name);
	}

	public final UnsignedDivider setAll(Signal x, Signal y, Signal div, Signal mod)
	{
		return (UnsignedDivider)useAndSet(Flavor.DEFAULT, new String[] {"x", "y", "div", "mod"}, x, y, div, mod);
	}

	@Override public final Map<String, Flavor> getFlavors()
	{
		return flavors;
	}

	private static final Map<String, Flavor> flavors = Flavor.getMap(
		new Flavor(
			new Flavor.Buildable<UnsignedDivider>()
			{
				@Override public void build(UnsignedDivider me, Map<String, Integer> variables)
				{
					Signal x = me.getSignal("x");
					Signal y = me.getSignal("y");
					Signal div = me.getSignal("div");
					Signal mod = me.getSignal("mod");

					Signal zeros = new Signal(y.size()-1);
					for (int i=0;i<zeros.size();++i)
						zeros.setBit(i,Bit.L);

					Signal slicedX = new Signal(x.get(x.size()-1),zeros);

					for (int i=0;i<x.size();++i) {
						Signal cmp = new Signal(2);
						Signal diff = new Signal(y.size()+i);
						Signal extendedY = (i == 0) ? y : new Signal(y,new Signal(i));
						for (int j=0;j<i;++j)
							extendedY.setBit(y.size()+j,Bit.L);

						Signal newX = new Signal(y.size()+i);

						new UnsignedComparator(me,"unsign-cmp:" + i)
							.setAll(
								slicedX,
								extendedY,
								diff,
								cmp
							);
						new BinaryMultiplexer(me,"mux:" + i)
							.setAll(new Signal[]{slicedX,diff}, cmp.get(0), newX);
						new Forward(me,"fw:" + i)
							.setAll(cmp.get(0), div.get(x.size()-1-i));

						if (i < x.size()-1)
							slicedX = new Signal(x.get(x.size()-i-2),newX);
						else
							slicedX = newX;
					}

					new Forward(me,"fw").setAll(slicedX.get(0,mod.size()), mod);
				}
			},
			"x[n], y[m]",
			"div[n], mod[m]"
		)
	);

	private UnsignedDivider(DeserializingStream in) throws IOException, DeserializingException, InstantiationException
	{
		super(in);
	}
}
