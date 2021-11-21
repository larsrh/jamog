
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
import core.signal.Bit;
import core.build.ComponentCollection;
import core.misc.module.Module.Description;
import core.misc.serial.DeserializingStream;
import gui.diagrams.Eldritch;
import gui.circuit.drawing.ComponentDiagram;
import gui.circuit.drawing.ComponentGhost;
import gui.circuit.ComponentWrapper;
import gui.circuit.drawing.Pin;
import java.awt.Point;
import java.awt.geom.GeneralPath;
import java.io.IOException;

/**
 * @author torben
 */
@Description
(
	name		= "OR Gate",
	description	= "A simple OR gate."
)
public final class OR extends Gate
{
	public OR(ComponentCollection parent,String name)
	{
		super(parent,name);
		this.diagram = new Diagram(true);
	}

	@Override public final void execute()
	{
		Bit r = in.getBit(0);

		for(int i = 1; i < in.size(); ++i)
			r = r.or(in.getBit(i));

		out.getSignalBit(0).is(r);
	}

	private static final class Diagram extends ComponentDiagram implements Eldritch
	{
		Diagram(boolean p)
		{
			GeneralPath path = new GeneralPath();
			path.moveTo(0, 0);
			path.lineTo(50, 0);
			path.curveTo(70, 0, 97, 45, 100, 50);
			path.curveTo(97, 55, 70, 100, 50, 100);
			path.lineTo(0, 100);
			path.curveTo(20, 70, 20, 30, 0, 0);
			path.closePath();
			setPath(path);
			if(p)
			{
				this.addPin(new Pin(new Point(20, 50), new Point(0, 50), "in"));
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

	private OR(DeserializingStream in) throws IOException, DeserializingException, InstantiationException
	{
		super(in);
	}
}
