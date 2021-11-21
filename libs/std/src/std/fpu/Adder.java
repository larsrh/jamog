
package std.fpu;

import core.build.ComponentCollection;
import core.build.Composite;
import core.build.Flavor;
import core.exception.DeserializingException;
import core.exception.InstantiationException;
import core.misc.serial.DeserializingStream;
import core.signal.Signal;
import java.io.IOException;
import std.gate.XOR;
import java.util.Map;
import std.alu.CLAAdder;
import std.alu.RipleNegator;
import std.alu.UnsignedComparator;
import std.gate.AND;
import std.gate.NOR;
import std.gate.NOT;
import std.gate.OR;
import std.logic.BooleanFunction;
import std.mux.BinaryMultiplexer;

import static core.signal.Bit.*;
import static std.logic.BooleanFunction.*;

public class Adder extends Composite
{
	public Adder(ComponentCollection parent,String name)
	{
		super(parent,name);
	}

	public final Adder setAll(Signal m1_in, Signal e1_in, Signal s1_in, Signal m2_in, Signal e2_in, Signal s2_in, Signal sub, Signal inf_in, Signal m_out, Signal e_out, Signal s_out, Signal nan_out)
	{
		return (Adder)setAll(new String[] {"m1_in", "e1_in", "s1_in", "m2_in", "e2_in", "s2_in", "sub", "inf_in", "m_out", "e_out", "s_out", "nan_out"}, m1_in, e1_in, s1_in, m2_in, e2_in, s2_in, sub, inf_in, m_out, e_out, s_out, nan_out);
	}

	@Override public final Map<String, Flavor> getFlavors()
	{
		return flavors;
	}

	private static final Map<String, Flavor> flavors = Flavor.getMap(
		new Flavor(
			new Flavor.Buildable<Adder>()
			{
				@Override public void build(Adder me, Map<String, Integer> matches)
				{
					Signal m1_in = me.getSignal("m1_in");
					Signal m2_in = me.getSignal("m2_in");

					Signal e1_in = me.getSignal("e1_in");
					Signal e2_in = me.getSignal("e2_in");
					Signal s1_in = me.getSignal("s1_in");

					int n = matches.get("n");
					int m = matches.get("m");

					Signal s2_in = new Signal(1);
					new XOR(me, "s2_xor").setAll(new Signal(me.getSignal("s2_in"), me.getSignal("sub")), s2_in);

					Signal inf_in = me.getSignal("inf_in");

					Signal s_out = me.getSignal("s_out");

					Signal e_diff = new Signal(m);
					Signal e_cmp = new Signal(2);
					new UnsignedComparator(me, "e_cmp").setAll(e1_in, e2_in, e_diff, e_cmp);

					Signal ne_diff = new Signal(e_diff.size());
					new RipleNegator(me, "e_diff_neg").setAll(e_diff, ne_diff);
					Signal sm1_in = me.shiftSignal("m1", m1_in, ne_diff);

					Signal sm2_in = me.shiftSignal("m2", m2_in, e_diff);

					Signal rm1_in = me.getSigned("m1", sm1_in, new Signal(new Signal(L, n), m1_in), e_cmp.get(0), s1_in);
					Signal rm2_in = me.getSigned("m2", new Signal(new Signal(L, n), m2_in), sm2_in, e_cmp.get(0), s2_in);

					Signal r_out = new Signal(rm1_in.size());
					Signal sr_out = new Signal(1);
					new CLAAdder(me, "adder").useAndSet("riple",new String[]{"x","y","carryIn","sum","carryOut"},rm1_in, rm2_in, new Signal(L), r_out, sr_out);

					Signal rmnn = new Signal(2);
					new OR(me, "rm1_notnull").setAll(rm1_in, rmnn.get(0));
					new OR(me, "rm2_notnull").setAll(rm2_in, rmnn.get(1));
					Signal rs = new Signal(2);
					new AND(me, "rs1").setAll(new Signal(rmnn.get(0), s1_in), rs.get(0));
					new AND(me, "rs2").setAll(new Signal(rmnn.get(1), s2_in), rs.get(1));
					new BooleanFunction(me, "s_bf").setAll(new Signal(inf_in, sr_out, rs), s_out, Type.KNF, new Value[][] {{Value.FALSE, Value.FALSE, Value.FALSE, Value.FALSE, Value.FALSE}, {Value.FALSE, Value.FALSE, Value.TRUE, Value.TRUE, Value.FALSE}, {Value.FALSE, Value.FALSE, Value.TRUE, Value.FALSE, Value.TRUE}, {Value.TRUE, Value.FALSE, Value.IGNORE, Value.FALSE, Value.IGNORE}, {Value.FALSE, Value.TRUE, Value.IGNORE, Value.IGNORE, Value.FALSE}});

					Signal nr_out = new Signal(r_out.size());
					new RipleNegator(me, "r_out_neg").setAll(r_out, nr_out);

					Signal m_out = me.getSignal("m_out");
					new BinaryMultiplexer(me, "m_out_sel").setAll(new Signal[] {r_out, nr_out}, s_out, m_out);

					Signal e_sel = new Signal(m);
					Signal m_out_zero = new Signal(1);
					new BinaryMultiplexer(me, "e_sel_sel").setAll(new Signal[] {e2_in, e1_in}, e_cmp.get(0), e_sel);
					new OR(me, "m_out_zero").setAll(m_out, m_out_zero);
					new BinaryMultiplexer(me, "e_out_sel").setAll(new Signal[] {new Signal(L, m), e_sel}, m_out_zero, me.getSignal("e_out"));

					Signal xs = new Signal(1);
					new XOR(me, "s_xor").setAll(new Signal(s1_in, s2_in), xs);
					new AND(me, "inf_and").setAll(new Signal(inf_in, xs), me.getSignal("nan_out"));
				}
			},
			"m1_in[n], e1_in[m], s1_in[1], m2_in[n], e2_in[m], s2_in[1], sub[1], inf_in[2]",
			"m_out[2*n+1], e_out[m], s_out[1], nan_out[1]"
		)
	);

	private Adder(DeserializingStream in) throws IOException, DeserializingException, InstantiationException
	{
		super(in);
	}

	private final Signal shiftSignal(String name, Signal in, Signal diff)
	{
		int slen = 32 - Integer.numberOfLeadingZeros(in.size());
		int sslen = 1 << slen;
		Signal ein = new Signal(new Signal(L, in.size()), in, new Signal(L, sslen));
		Signal[] shifted = new Signal[sslen];
		for(int i = 0; i < sslen; ++i)
			shifted[i] = ein.get(i, in.size() << 1);

		Signal sin = new Signal(in.size() << 1);
		new BinaryMultiplexer(this, "s" + name + "_mux").setAll(shifted, slen >= diff.size() ? diff : diff.get(0, slen), sin);

		if(slen < diff.size())
		{
			Signal out = new Signal(sin.size());
			Signal nuf = new Signal(1);
			if(slen == diff.size() - 1)
				new NOT(this, name + "_nuf").setAll(diff.get(slen), nuf);
			else
				new NOR(this, name + "_nuf").setAll(diff.get(slen, diff.size() - slen), nuf);

			for(int i = 0; i < sin.size(); ++i)
				new AND(this, name + "_and:" + i).setAll(new Signal(sin.get(i), nuf), out.get(i));

			return out;
		}
		else
			return sin;
	}

	private final Signal getSigned(String name, Signal first, Signal second, Signal shift_sel, Signal sign_sel)
	{
		Signal unsigned = new Signal(new Signal(first.size()), new Signal(L));
		new BinaryMultiplexer(this, "u" + name + "_in_sel").setAll(new Signal[] {first, second}, shift_sel.get(0), unsigned.get(0, first.size()));

		Signal neg = new Signal(unsigned.size());
		new RipleNegator(this, "n" + name + "_in_neg").setAll(unsigned, neg);

		Signal signed = new Signal(unsigned.size());
		new BinaryMultiplexer(this, "r" + name + "_in_sel").setAll(new Signal[]{unsigned, neg}, sign_sel, signed);

		return signed;
	}
}
