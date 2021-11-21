
package std.fpu;

import core.build.ComponentCollection;
import core.build.Composite;
import core.build.Flavor;
import core.exception.DeserializingException;
import core.exception.InstantiationException;
import core.misc.serial.DeserializingStream;
import core.signal.Signal;
import java.io.IOException;
import java.math.BigInteger;
import std.gate.XOR;
import java.util.Map;
import std.alu.CLAAdder;
import std.alu.UnsignedDivider;
import std.gate.AND;
import std.gate.NOT;
import std.gate.OR;
import std.logic.BooleanFunction;

import static core.misc.BitConverter.*;
import static core.signal.Bit.*;
import static std.logic.BooleanFunction.*;

public class Divider extends Composite
{
	public Divider(ComponentCollection parent,String name)
	{
		super(parent,name);
	}

	public final Divider setAll(Signal m1_in, Signal e1_in, Signal s1_in, Signal m2_in, Signal e2_in, Signal s2_in, Signal zero_in, Signal inf_in, Signal m_out, Signal e_out, Signal s_out, Signal uf, Signal of, Signal zero_out, Signal inf_out, Signal nan_out)
	{
		return (Divider)setAll(new String[] {"m1_in", "e1_in", "s1_in", "m2_in", "e2_in", "s2_in", "zero_in", "inf_in", "m_out", "e_out", "s_out", "uf", "of", "zero_out", "inf_out", "nan_out"}, m1_in, e1_in, s1_in, m2_in, e2_in, s2_in, zero_in, inf_in, m_out, e_out, s_out, uf, of, zero_out, inf_out, nan_out);
	}

	@Override public final Map<String, Flavor> getFlavors()
	{
		return flavors;
	}

	private static final Map<String, Flavor> flavors = Flavor.getMap(
		new Flavor(
			new Flavor.Buildable<Divider>()
			{
				@Override public void build(Divider me, Map<String, Integer> matches)
				{
					int n = matches.get("n");
					int m = matches.get("m");
					int l = matches.get("l");

					Signal e2_inv = new Signal(m);
					new NOT(me, "e2_not").setAll(me.getSignal("e2_in"), e2_inv);
					Signal be = new Signal(m + 1);
					new CLAAdder(me, "e_sub").useAndSet("riple",new String[]{"x","y","carryIn","sum","carryOut"},me.getSignal("e1_in"), e2_inv, new Signal(H), be.get(0, m), be.get(m));
					Signal high = new Signal(2);
					new CLAAdder(me, "be_add").useAndSet("riple",new String[]{"x","y","carryIn","sum","carryOut"},be, new Signal(integerToBits(m + 1, BigInteger.ONE.shiftLeft(m - 1).subtract(BigInteger.ONE))), new Signal(L), new Signal(me.getSignal("e_out"), high.get(0)), high.get(1));

					new AND(me, "of_test").setAll(high, me.getSignal("of"));
					new BooleanFunction(me, "uf_test").setAll(high, me.getSignal("uf"), Type.DNF, new Value[][] {{Value.FALSE, Value.TRUE}});

					Signal m_out = me.getSignal("m_out");
					Signal mod = new Signal(n);
					new UnsignedDivider(me, "div").setAll(new Signal(new Signal(L, l - n - 1), me.getSignal("m1_in")), me.getSignal("m2_in"), m_out.get(1, l - 1), mod);
					new OR(me, "mod-or").setAll(mod, m_out.get(0));

					new XOR(me, "s_xor").setAll(new Signal(me.getSignal("s1_in"), me.getSignal("s2_in")), me.getSignal("s_out"));

					Signal zero_in = me.getSignal("zero_in");
					Signal inf_in = me.getSignal("inf_in");
					Signal dbi = new Signal(1);
					Signal oi = new Signal(1);
					Signal zero_out = me.getSignal("zero_out");
					new BooleanFunction(me, "zero_test").setAll(inf_in, zero_out, Type.DNF, new Value[][] {{Value.FALSE, Value.TRUE}});
					new BooleanFunction(me, "inf_test_bf").setAll(zero_in, dbi, Type.DNF, new Value[][] {{Value.FALSE, Value.TRUE}});
					new OR(me, "inf_test_or").setAll(new Signal(inf_in, dbi), oi);
					new BooleanFunction(me, "inf_test").setAll(new Signal(zero_out, oi), me.getSignal("inf_out"), Type.DNF, new Value[][] {{Value.FALSE, Value.TRUE}});
					new BooleanFunction(me, "nan_test").setAll(new Signal(zero_in, inf_in), me.getSignal("nan_out"), Type.DNF, new Value[][] {{Value.TRUE, Value.TRUE, Value.IGNORE, Value.IGNORE}, {Value.IGNORE, Value.IGNORE, Value.TRUE, Value.TRUE}});
				}
			},
			"m1_in[n], e1_in[m], s1_in[1], m2_in[n], e2_in[m], s2_in[1], zero_in[2], inf_in[2]",
			"m_out[l], e_out[m], s_out[1], uf[1], of[1], zero_out[1], inf_out[1], nan_out[1]",
			"#l >= 2 * #n + 2"
		)
	);

	private Divider(DeserializingStream in) throws IOException, DeserializingException, InstantiationException
	{
		super(in);
	}
}
