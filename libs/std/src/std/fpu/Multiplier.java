
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

import std.alu.UnsignedMultiplier;
import std.gate.AND;
import std.logic.BooleanFunction;

import static core.misc.BitConverter.*;
import static core.signal.Bit.*;
import static std.logic.BooleanFunction.*;

public class Multiplier extends Composite
{
	public Multiplier(ComponentCollection parent,String name)
	{
		super(parent,name);
	}

	public final Multiplier setAll(Signal m1_in, Signal e1_in, Signal s1_in, Signal m2_in, Signal e2_in, Signal s2_in, Signal zero_in, Signal inf_in, Signal m_out, Signal e_out, Signal s_out, Signal uf, Signal of, Signal nan_out)
	{
		return (Multiplier)setAll(new String[] {"m1_in", "e1_in", "s1_in", "m2_in", "e2_in", "s2_in", "zero_in", "inf_in", "m_out", "e_out", "s_out", "uf", "of", "nan_out"}, m1_in, e1_in, s1_in, m2_in, e2_in, s2_in, zero_in, inf_in, m_out, e_out, s_out, uf, of, nan_out);
	}

	@Override public final Map<String, Flavor> getFlavors()
	{
		return flavors;
	}

	private static final Map<String, Flavor> flavors = Flavor.getMap(
		new Flavor(
			new Flavor.Buildable<Multiplier>()
			{
				@Override public void build(Multiplier me, Map<String, Integer> matches)
				{
					int m = matches.get("m");

					Signal be = new Signal(m + 1);
					new CLAAdder(me, "e_adder").useAndSet("riple",new String[]{"x","y","carryIn","sum","carryOut"},me.getSignal("e1_in"), me.getSignal("e2_in"), new Signal(L), be.get(0, m), be.get(m));
					Signal high = new Signal(2);
					new CLAAdder(me, "be_del").useAndSet("riple",new String[]{"x","y","carryIn","sum","carryOut"},be, new Signal(integerToBits(m + 1, BigInteger.ONE.shiftLeft(m - 1).subtract(BigInteger.ONE).negate())), new Signal(L), new Signal(me.getSignal("e_out"), high.get(0)), high.get(1));

					new AND(me, "of_test").setAll(high, me.getSignal("of"));
					new BooleanFunction(me, "uf_test").setAll(high, me.getSignal("uf"), Type.DNF, new Value[][] {{Value.TRUE, Value.FALSE}});

					new UnsignedMultiplier(me, "mul").setAll(me.getSignal("m1_in"), me.getSignal("m2_in"), me.getSignal("m_out"));
					new XOR(me, "s_xor").setAll(new Signal(me.getSignal("s1_in"), me.getSignal("s2_in")), me.getSignal("s_out"));

					new BooleanFunction(me, "nan_test").setAll(new Signal(me.getSignal("zero_in"), me.getSignal("inf_in")), me.getSignal("nan_out"), Type.DNF, new Value[][] {{Value.TRUE, Value.IGNORE, Value.IGNORE, Value.TRUE}, {Value.IGNORE, Value.TRUE, Value.TRUE, Value.IGNORE}});
				}
			},
			"m1_in[n], e1_in[m], s1_in[1], m2_in[n], e2_in[m], s2_in[1], zero_in[2], inf_in[2]",
			"m_out[2 * n], e_out[m], s_out[1], uf[1], of[1], nan_out[1]"
		)
	);

	private Multiplier(DeserializingStream in) throws IOException, DeserializingException, InstantiationException
	{
		super(in);
	}
}
