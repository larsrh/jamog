
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
import std.gate.NAND;
import std.gate.XOR;
import std.mux.BinaryMultiplexer;
import core.build.ComponentCollection;
import core.build.Flavor;
import core.exception.DeserializingException;
import core.misc.serial.DeserializingStream;
import core.signal.Bit;
import core.signal.Signal;
import java.io.IOException;
import java.util.Map;

/**
 *
 * @author lars
 */
public class Comparator extends SignedComponent {

	public Comparator(ComponentCollection parent,String name)
	{
		super(parent,name);
	}

	public final Comparator setAll(Signal x, Signal y, Signal signed, Signal cmp)
	{
		return (Comparator)useAndSet(Flavor.DEFAULT, new String[] {"x", "y", "signed", "cmp"}, x, y, signed, cmp);
	}

	@Override public final Map<String, Flavor> getFlavors()
	{
		return flavors;
	}

	private static final Map<String, Flavor> flavors = Flavor.getMap(
		new Flavor(
			new Flavor.Buildable<Comparator>()
			{
				@Override public void build(Comparator me, Map<String, Integer> variables)
				{
					/* Comparision table:
					 * xSign ySign   cmp'[0]   cmp'[1]
					 *   0     0     =cmp[0]   =cmp[1]
					 *   1     0     =0        =1
					 *   0     1     =1        =1
					 *   1     1     =nand(cmp)=cmp[1]
					 */
					Signal x = me.getSignal("x");
					Signal y = me.getSignal("y");
					Signal cmp = me.getSignal("cmp");
					Signal signed = me.getSignal("signed");

					Signal cmpOutput = new Signal(2);
					Signal differentSign = new Signal(1);
					Signal signedCmp = new Signal(2);

					Signal one = new Signal(Bit.H);
					Signal nandC = new Signal(1);
					Signal modC0 = new Signal(1);

					new UnsignedComparator(me,"unsign-comp")
						.setAll(new String[]{"x","y","cmp"},
							me.unsignSignal("x-unsign",x,signed,new Signal(x.size())), // unsign x
							me.unsignSignal("y-unsign",y,signed,new Signal(y.size())), // unsign y
							cmpOutput
						);
					new XOR(me,"xor")
						.setAll(new Signal(x.get(x.size()-1),y.get(y.size()-1)),differentSign);
					new NAND(me,"nand")
						.setAll(cmpOutput,nandC);
					new BinaryMultiplexer(me,"mux1")
						.setAll(new Signal[]{cmpOutput.get(0),nandC}, x.get(x.size()-1), modC0);
					new BinaryMultiplexer(me,"mux2")
						.setAll(new Signal[]{modC0, y.get(y.size()-1)}, differentSign, signedCmp.get(0));
					new BinaryMultiplexer(me,"mux3")
						.setAll(new Signal[]{cmpOutput.get(1),one}, differentSign, signedCmp.get(1));
					new BinaryMultiplexer(me,"mux4")
						.setAll(new Signal[]{cmpOutput,signedCmp},signed,cmp);
				}
			},
			"x[n], y[n], signed[1]",
			"cmp[2]"
		)
	);

	private Comparator(DeserializingStream in) throws IOException, DeserializingException, InstantiationException
	{
		super(in);
	}
}
