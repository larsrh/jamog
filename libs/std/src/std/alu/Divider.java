
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

import core.build.ComponentCollection;
import core.build.Flavor;
import core.exception.DeserializingException;
import core.exception.InstantiationException;
import core.misc.serial.DeserializingStream;
import core.signal.Signal;
import java.io.IOException;
import java.util.Map;

/**
 * @author lars
 */
public class Divider extends SignedComponent
{
	public Divider(ComponentCollection parent,String name)
	{
		super(parent,name);
	}

	public final Divider setAll(Signal x, Signal y, Signal signed, Signal div, Signal mod)
	{
		return (Divider)useAndSet(Flavor.DEFAULT, new String[] {"x", "y", "signed", "div", "mod"}, x, y, signed, div, mod);
	}

	@Override public final Map<String, Flavor> getFlavors()
	{
		return flavors;
	}

	private static final Map<String, Flavor> flavors = Flavor.getMap(
		new Flavor(
			new Flavor.Buildable<Divider>()
			{
				@Override public void build(Divider me, Map<String, Integer> variables)
				{
					Signal x = me.getSignal("x");
					Signal y = me.getSignal("y");
					Signal div = me.getSignal("div");
					Signal mod = me.getSignal("mod");
					Signal signed = me.getSignal("signed");

					Signal divOutput = new Signal(div.size());
					Signal modOutput = new Signal(mod.size());

					new UnsignedDivider(me,"unsign-div")
						.setAll(new String[]{"x","y","div","mod"},
							me.unsignSignal("x-unsign",x,signed,new Signal(x.size())), // unsign x
							me.unsignSignal("y-unsign",y,signed,new Signal(y.size())), // unsign y
							divOutput,
							modOutput
						);

					me.restoreSignMSBXOR("div-restore",x,y,divOutput,signed,div);
					me.restoreSign("mod-restore",modOutput,x.get(x.size()-1),signed,mod);
				}
			},
			"x[n], y[m], signed[1]",
			"div[n], mod[m]"
		)
	);

	private Divider(DeserializingStream in) throws IOException, DeserializingException, InstantiationException
	{
		super(in);
	}
}
