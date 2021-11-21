
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
	name		= "XNOR Gate",
	description	= "A simple XNOR gate."
)
public final class XNOR extends Gate
{
	public XNOR(ComponentCollection parent,String name)
	{
		super(parent,name);
		this.diagram = new Diagram(true);
	}

	@Override public final void execute()
	{
		Bit r = in.getBit(0);

		for(int i = 1; i < in.size(); ++i)
			r = r.xor(in.getBit(i));

		out.getSignalBit(0).is(r.not());
	}

	private static final class Diagram extends ComponentDiagram implements Eldritch
	{
		Diagram(boolean p)
		{
			GeneralPath path = new GeneralPath();
			path.moveTo(0, 0);
			path.lineTo(4, 0);
			path.curveTo(20, 24, 20, 56, 4, 80);
			path.lineTo(0, 80);
			path.curveTo(16, 56, 16, 24, 0, 0);
			path.closePath();
			path.moveTo(12, 0);
			path.lineTo(40, 0);
			path.curveTo(56, 0, 77.6, 36, 80, 40);
			path.quadTo(90, 20, 100, 40);
			path.quadTo(90, 60, 80, 40);
			path.curveTo(77.6, 44, 56, 80, 40, 80);
			path.lineTo(12, 80);
			path.curveTo(28, 56, 28, 24, 12, 0);
			path.closePath();
			setPath(path);
			if(p)
			{
				this.addPin(new Pin(new Point(35, 40), new Point(0, 40), "in"));
				this.addPin(new Pin(new Point(90, 40), new Point(100, 40), "out"));
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

	private XNOR(DeserializingStream in) throws IOException, DeserializingException, InstantiationException
	{
		super(in);
	}
}
