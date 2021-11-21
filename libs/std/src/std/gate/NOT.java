
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

import core.exception.DeserializingException;
import core.exception.InstantiationException;
import core.exception.SerializingException;
import core.misc.module.Module.Description;
import core.misc.serial.SerializingStream;
import core.signal.Signal;
import core.sim.Calculator;
import core.build.ComponentCollection;
import core.build.Flavor;
import core.misc.serial.DeserializingStream;
import gui.diagrams.Eldritch;
import gui.circuit.drawing.ComponentDiagram;
import gui.circuit.drawing.ComponentGhost;
import gui.circuit.ComponentWrapper;
import gui.circuit.drawing.Drawable;
import gui.circuit.drawing.Pin;
import java.awt.Point;
import java.awt.geom.GeneralPath;
import java.io.IOException;
import java.util.Map;

/**
 * @author torben
 */
@Description
(
	name		= "NOT Gate",
	description	= "A simple NOT gate."
)
public final class NOT extends Calculator implements Drawable
{
	public NOT(ComponentCollection parent,String name)
	{
		super(parent, name);
		this.diagram = new Diagram(true);
	}

	public final NOT setAll(Signal in, Signal out)
	{
		return (NOT)useAndSet(Flavor.DEFAULT, new String[] {"in", "out"}, in, out);
	}

	@Override public Map<String, Flavor> getFlavors()
	{
		return flavors;
	}

	@Override public final void execute()
	{
		for(int i = 0; i < in.size(); ++i)
			out.getSignalBit(i).is(in.getBit(i).not());
	}

	@Override public gui.circuit.drawing.Diagram getDiagram()
	{
		return this.diagram;
	}

	@Override public void serialize(SerializingStream out) throws IOException, SerializingException
	{
		super.serialize(out);

		out.writeObject(this.in, false);
		out.writeObject(this.out, false);
	}

	private static final class Diagram extends ComponentDiagram implements Eldritch
	{
		Diagram(boolean p)
		{
			GeneralPath path = new GeneralPath();
			path.moveTo(0, 0);
			path.lineTo(80, 50);
			path.quadTo(90, 30, 100, 50);
			path.quadTo(90, 70, 80, 50);
			path.lineTo(0, 100);
			path.closePath();
			setPath(path);
			if(p)
			{
				this.addPin(new Pin(new Point(10, 50), new Point(0, 50), "in"));
				this.addPin(new Pin(new Point(90, 50), new Point(100, 50), "out"));
			}
		}

		@Override public final ComponentGhost getGhost(ComponentWrapper soul)
		{
			return new Ghost(soul);
		}
	}

	private static final class Ghost extends ComponentGhost
	{
		Ghost(ComponentWrapper soul)
		{
			super(soul);
			this.diagram = new Diagram(false);
		}
	}

	private static final Map<String, Flavor> flavors = Flavor.getMap(
		new Flavor(
			new Flavor.Buildable<NOT>()
			{
				@Override public void build(NOT me, Map<String, Integer> variables)
				{
					me.in = me.getSignal("in");
					me.out = me.getSignal("out");
				}
			},
			"in[n]",
			"out[n]"
		)
	);

	private Signal in;
	private Signal out;
	private Diagram diagram;

	private NOT(DeserializingStream in) throws IOException, DeserializingException, InstantiationException
	{
		super(in);

		this.in = in.readObject(Signal.class);
		this.out = in.readObject(Signal.class);
	}
}
