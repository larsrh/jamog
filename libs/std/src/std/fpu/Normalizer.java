
package std.fpu;

import core.exception.InstantiationException;
import std.alu.CLAAdder;
import std.gate.AND;
import std.gate.NOR;
import std.logic.BooleanFunction;
import std.mux.Multiplexer;
import core.build.ComponentCollection;
import core.build.Composite;
import core.build.Flavor;
import core.exception.DeserializingException;
import core.misc.BitConverter;
import core.misc.serial.DeserializingStream;
import core.signal.Signal;
import java.io.IOException;
import std.gate.NOT;
import std.gate.OR;
import java.math.BigInteger;
import java.util.Map;

import static core.signal.Bit.*;
import static std.logic.BooleanFunction.*;

public final class Normalizer extends Composite
{
	public Normalizer(ComponentCollection parent,String name)
	{
		super(parent,name);
	}

	public final Normalizer setAll(Signal m_in, Signal e_in, Signal m_out, Signal e_out, Signal uf, Signal of, int comma)
	{
		return (Normalizer)setAll(new String[] {"m_in", "e_in", "m_out", "e_out", "uf", "of", "comma"}, m_in, e_in, m_out, e_out, uf, of, comma);
	}

	@Override public final Map<String, Flavor> getFlavors()
	{
		return flavors;
	}

	private static final Map<String, Flavor> flavors = Flavor.getMap(
		new Flavor(
			new Flavor.Buildable<Normalizer>()
			{
				@Override public void build(Normalizer me, Map<String, Integer> matches)
				{
					Signal m_in = me.getSignal("m_in");

					int n = matches.get("n");
					int m = matches.get("m");
					int l = matches.get("l");

					Signal sel = new Signal(new Signal(n - 1), m_in.get(n - 1));
					for(int i = 0; i < n - 1; ++i)
					{
						Signal nor = new Signal(1);
						if(i == n - 2)
							new NOT(me, "not").setAll(m_in.get(i + 1, 1), nor);
						else
							new NOR(me, "nor:" + i).setAll(m_in.get(i + 1, n - (i + 1)), nor);
						new AND(me, "and:" + i).setAll(new Signal(nor, m_in.get(i)), sel.get(i));
					}

					Signal em_in = new Signal(new Signal(n), m_in);
					Signal[] sh_m_in = new Signal[sel.size()];
					for(int i = 0; i < sel.size(); ++i)
						sh_m_in[i] = em_in.get(i, n);

					Signal unround = new Signal(n);
					new Multiplexer(me, "shift_sel").setAll(sh_m_in, sel, unround);

					Signal add = new Signal(unround.size());
					Signal round_co = new Signal(1);

					Signal low = new Signal(1);
					new OR(me, "round_low").setAll(unround.get(0, n - l - 1), low);
					new BooleanFunction(me, "round_bf").setAll(new Signal(low, unround.get(n - l - 1, 2)), add.get(n -l), Type.DNF, new Value[][] {{Value.TRUE, Value.TRUE, Value.IGNORE}, {Value.FALSE, Value.TRUE, Value.TRUE}});

					new CLAAdder(me, "rounder").useAndSet("riple",new String[]{"x","y","carryIn","sum","carryOut"},unround, add, new Signal(L), new Signal(new Signal(n - l), me.getSignal("m_out")), round_co);

					Signal[] sh_e = new Signal[sel.size()];
					int comma = (Integer)me.get("comma");
					for(int i = 0; i < sel.size(); ++i)
						sh_e[i] = new Signal(BitConverter.integerToBits(m, BigInteger.valueOf(i - comma)));

					Signal e_add = new Signal(m);
					new Multiplexer(me, "e_mul").setAll(sh_e, sel, e_add);

					Signal co = new Signal(1);
					new CLAAdder(me, "e_add").useAndSet("riple",new String[]{"x","y","carryIn","sum","carryOut"},me.getSignal("e_in"), e_add, round_co, me.getSignal("e_out"), co);

					Signal sd;
					if(comma > 1)
					{
						sd = new Signal(1);
						new OR(me, "sd_or").setAll(sel.get(0, comma), sd);
					}
					else if(comma == 1)
						sd = sel.get(0);
					else
						sd = new Signal(L);

					Signal test = new Signal(co, sd);
					new BooleanFunction(me, "of-test").setAll(test, me.getSignal("of"), Type.DNF, new Value[][] {{Value.TRUE, Value.FALSE}});
					new BooleanFunction(me, "uf-test").setAll(test, me.getSignal("uf"), Type.DNF, new Value[][] {{Value.FALSE, Value.TRUE}});
				}
			},
			"m_in[n], e_in[m]",
			"m_out[l], e_out[m], uf[1], of[1]",
			"int comma",
			"#n >= #l + 3"
		)
	);

	private Normalizer(DeserializingStream in) throws IOException, DeserializingException, InstantiationException
	{
		super(in);
	}
}
