
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

package std.gate;

import core.build.ComponentCollection;
import core.build.Flavor;
import core.exception.DeserializingException;
import core.exception.InstantiationException;
import core.exception.SerializingException;
import core.misc.serial.DeserializingStream;
import core.misc.serial.SerializingStream;
import gui.circuit.drawing.Drawable;
import gui.circuit.drawing.Diagram;
import core.sim.Calculator;
import core.signal.Signal;
import java.io.IOException;
import java.util.Map;

/**
 * @author lars
 */
public abstract class Gate extends Calculator implements Drawable
{
	protected Gate(ComponentCollection parent,String name)
	{
		super(parent, name);
	}

	protected Gate(DeserializingStream in) throws IOException, DeserializingException, InstantiationException
	{
		super(in);

		this.in = in.readObject(Signal.class);
		this.out = in.readObject(Signal.class);
	}

	public final Gate setAll(Signal in, Signal out)
	{
		return (Gate)useAndSet(Flavor.DEFAULT, new String[] {"in", "out"}, in, out);
	}

	@Override public Map<String, Flavor> getFlavors()
	{
		return flavors;
	}

	@Override public Diagram getDiagram()
	{
		return this.diagram;
	}

	@Override public void serialize(SerializingStream out) throws IOException, SerializingException
	{
		super.serialize(out);

		out.writeObject(this.in, false);
		out.writeObject(this.out, false);
	}

	protected Signal in;
	protected Signal out;
	protected Diagram diagram;

	private static final Map<String, Flavor> flavors = Flavor.getMap(
		new Flavor(
			new Flavor.Buildable<Gate>()
			{
				@Override public void build(Gate me, Map<String, Integer> variables)
				{
					me.in = me.getSignal("in");
					me.out = me.getSignal("out");
				}
			},
			"in[n:2]",
			"out[1]"
		)
	);
}
