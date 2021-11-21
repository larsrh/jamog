
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

package std.logic;

import core.exception.DeserializingException;
import core.exception.InstantiationException;
import core.signal.Signal;
import core.build.Composite;
import java.io.IOException;
import std.gate.*;
import core.build.ComponentCollection;
import core.misc.module.Module.Description;
import core.build.Flavor;
import core.misc.serial.DeserializingStream;
import java.util.Map;

/**
 * @author torben
 */
@Description
(
	name		= "Boolean Function",
	description	= "A boolean function from inputs to outputs, capable of accepting rules in different ways."
)
public final class BooleanFunction extends Composite
{
	public static enum Type
	{
		DNF, KNF, NDNF, NKNF;
	}

	public static enum Value
	{
		FALSE, TRUE, IGNORE;
	}

	public BooleanFunction(ComponentCollection parent,String name)
	{
		super(parent,name);
	}

	public final BooleanFunction setAll(Signal in, Signal out, Type type, Value[][] rules)
	{
		return (BooleanFunction)useAndSet("default", new String[] {"in", "out", "type", "rules"}, in, out, type, rules);
	}

	@Override public Map<String, Flavor> getFlavors()
	{
		return flavors;
	}

	private static final Map<String, Flavor> flavors = Flavor.getMap(
		new Flavor(
			new Flavor.Buildable<BooleanFunction>()
			{
				@Override public void build(BooleanFunction me, Map<String, Integer> variables)
				{
					Signal in = me.getSignal("in");
					Signal out = me.getSignal("out");
					Value[][] rules = (Value[][])me.get("rules");

					Signal[] s = new Signal[rules.length];

					switch((Type)me.get("type"))
					{
					case DNF:
						for(int i = 0; i < rules.length; ++i)
						{
							Signal c = createSelectByRule(me, in, Value.FALSE, rules[i], i);
							if(c.size() > 1)
								new AND(me, "and:" + i).setAll(c, s[i] = new Signal(1));
							else
								new Forward(me, "fw:" + i).setAll(c, s[i] = new Signal(1));
						}
						if(s.length > 1)
							new OR(me, "or").setAll(new Signal(s), out);
						else
							new Forward(me, "fw").setAll(s[0], out);
						break;
					case KNF:
						for(int i = 0; i < rules.length; ++i)
						{
							Signal c = createSelectByRule(me, in, Value.TRUE, rules[i], i);
							if(c.size() > 1)
								new OR(me, "or:" + i).setAll(c, s[i] = new Signal(1));
							else
								new Forward(me, "fw:" + i).setAll(c, s[i] = new Signal(1));
						}
						if(s.length > 1)
							new AND(me, "and").setAll(new Signal(s), out);
						else
							new Forward(me, "fw").setAll(s[0], out);
						break;
					case NDNF:
						for(int i = 0; i < rules.length; ++i)
						{
							Signal c = createSelectByRule(me, in, Value.FALSE, rules[i], i);
							if(c.size() > 1)
								new AND(me, "and:" + i).setAll(c, s[i] = new Signal(1));
							else
								new Forward(me, "fw:" + i).setAll(c, s[i] = new Signal(1));
						}
						if(s.length > 1)
							new NOR(me, "nor").setAll(new Signal(s), out);
						else
							new Forward(me, "fw").setAll(s[0], out);
						break;
					case NKNF:
						for(int i = 0; i < rules.length; ++i)
						{
							Signal c = createSelectByRule(me, in, Value.TRUE, rules[i], i);
							if(c.size() > 1)
								new OR(me, "or:" + i).setAll(c, s[i] = new Signal(1));
							else
								new Forward(me, "fw:" + i).setAll(c, s[i] = new Signal(1));
						}
						if(s.length > 1)
							new NAND(me, "nand").setAll(new Signal(s), out);
						else
							new Forward(me, "fw").setAll(s[0], out);
					}
				}
			},
			"in[n]",
			"out[1]",
			// TODO: Fix parameters
			"std.logic.BooleanFunction$Type type, std.logic.BooleanFunction$Value rules[m][n]",
			""
		)
	);

	private BooleanFunction(DeserializingStream in) throws IOException, DeserializingException, InstantiationException
	{
		super(in);
	}

	private static final Signal createSelectByRule(BooleanFunction me, Signal in, Value invert, Value[] rule, int num)
	{
		int len = 0;
		for(int i = 0; i < rule.length; ++i)
			if(rule[i] != Value.IGNORE)
				++len;

		Signal[] sig = new Signal[len];
		for(int i = 0, j = 0; i < rule.length; ++i)
		{
			if(rule[i] != Value.IGNORE)
			{
				if(rule[i] == invert)
					new NOT(me, "inv:" + num + ":" + i).setAll(in.get(i), sig[j++] = new Signal(1));
				else
					sig[j++] = in.get(i);
			}
		}

		return new Signal(sig);
	}
}
