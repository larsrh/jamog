
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

package std.convert;

import core.exception.DeserializingException;
import core.exception.InstantiationException;
import core.signal.Signal;
import core.build.Composite;
import java.io.IOException;
import std.gate.*;
import std.logic.*;
import core.build.ComponentCollection;
import core.misc.module.Module.Description;
import core.build.Flavor;
import core.misc.serial.DeserializingStream;
import java.util.Map;

import static core.signal.Bit.*;

/**
 * @author torben
 */
@Description
(
	name		= "Binary Encoder",
	description	= "A simple binary encoder."
)
public final class BinaryEncoder extends Composite
{
	public BinaryEncoder(ComponentCollection parent,String name)
	{
		super(parent,name);
	}

	public final BinaryEncoder setAll(Signal in, Signal out)
	{
		return (BinaryEncoder)useAndSet(Flavor.DEFAULT, new String[] {"in", "out"}, in, out);
	}

	@Override public Map<String, Flavor> getFlavors()
	{
		return flavors;
	}

	private static final Map<String, Flavor> flavors = Flavor.getMap(
		new Flavor(
			new Flavor.Buildable<BinaryEncoder>()
			{
				@Override public void build(BinaryEncoder me, Map<String, Integer> variables)
				{
					Signal in = me.getSignal("in");
					Signal out = me.getSignal("out");

					for(int i = 0; i < out.size(); ++i)
					{
						int size = 0;

						for(int j = 0; j < in.size(); ++j)
							if(((j & (1 << i)) >> i) == 1)
								++size;

						Signal s[] = new Signal[size];
						for(int j = 0, k = 0; j < in.size(); ++j)
							if(((j & (1 << i)) >> i) == 1)
								s[k++] = in.get(j);

						if(size > 1)
							new OR(me, "or:" + i).setAll(new Signal(s), out.get(i));
						else if(size == 1)
							new Forward(me, "fw:" + i).setAll(s[0], out.get(i));
						else
							new Forward(me, "fw:" + i).setAll(new Signal(L), out.get(i));
					}
				}
			},
			"in[n]",
			"out[m]",
			"(#n - 1) / (2^(#m - 1)) == 1"
		)
	);

	private BinaryEncoder(DeserializingStream in) throws IOException, DeserializingException, InstantiationException
	{
		super(in);
	}
}
