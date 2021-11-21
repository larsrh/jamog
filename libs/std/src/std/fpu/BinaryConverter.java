
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
import java.util.Map;


import std.alu.RipleNegator;
import std.gate.NOR;
import std.logic.Forward;
import std.mux.BinaryMultiplexer;
import static core.signal.Bit.*;
import static core.misc.BitConverter.*;

public class BinaryConverter extends Composite
{
	public BinaryConverter(ComponentCollection parent,String name)
	{
		super(parent,name);
	}

	@Override public final Map<String, Flavor> getFlavors()
	{
		return flavors;
	}

	private static final Map<String, Flavor> flavors = Flavor.getMap(
		new Flavor(
			new Flavor.Buildable<BinaryConverter>()
			{
				@Override public void build(BinaryConverter me, Map<String, Integer> matches)
				{
					Signal in = me.getSignal("in");

					Signal e = me.getSignal("e");
					Signal s = me.getSignal("s");

					int ms = matches.get("m");
					int es = matches.get("l");

					Signal iin = new Signal(in.size());
					new RipleNegator(me, "in_inv").setAll(in, iin);
					Signal m_in = new Signal(in.size());
					new Forward(me, "s_fw").setAll(in.get(in.size() - 1), s);
					new BinaryMultiplexer(me, "m_in_sel").setAll(new Signal[] {in, iin}, s, m_in);

					Signal i = new Signal(ms + es + 2);
					new Normalizer(me, "bc_norm").setAll(m_in, new Signal(integerToBits(e.size(), BigInteger.ONE.shiftLeft(e.size() - 1).subtract(BigInteger.ONE))), i.get(0, ms), i.get(ms, es), i.get(ms + es), i.get(ms + es + 1), 0);

					Signal nul = new Signal(1);
					new NOR(me, "null_test").setAll(in, nul);
					new BinaryMultiplexer(me, "mux").setAll(new Signal[] {i, new Signal(L, ms + es + 2)}, nul, new Signal(me.getSignal("m"), e, me.getSignal("uf"), me.getSignal("of")));
				}
			},
			"in[n]",
			"m[m], e[l], s[1], uf[1], of[1]"
		)
	);

	private BinaryConverter(DeserializingStream in) throws IOException, DeserializingException, InstantiationException
	{
		super(in);
	}
}
