
package std.fpu;

import core.build.ComponentCollection;
import core.build.Composite;
import core.build.Flavor;
import core.exception.DeserializingException;
import core.exception.InstantiationException;
import core.misc.serial.DeserializingStream;
import core.signal.Signal;
import java.io.IOException;
import java.util.Map;

import std.gate.AND;
import std.gate.NOR;
import std.gate.OR;
import std.logic.BooleanFunction;
import static std.logic.BooleanFunction.*;

public class Flags extends Composite
{
	public Flags(ComponentCollection parent,String name)
	{
		super(parent,name);
	}

	public final Flags setAll(Signal m, Signal e, Signal zero, Signal inf, Signal nan)
	{
		return (Flags)setAll(new String[] {"m", "e", "zero", "inf", "nan"}, m, e, zero, inf, nan);
	}

	@Override public final Map<String, Flavor> getFlavors()
	{
		return flavors;
	}

	private static final Map<String, Flavor> flavors = Flavor.getMap(
		new Flavor(
			new Flavor.Buildable<Flags>()
			{
				@Override public void build(Flags me, Map<String, Integer> matches)
				{
					Signal m = me.getSignal("m");
					Signal e = me.getSignal("e");
					
					Signal p = new Signal(2);
					new OR(me, "pm").setAll(m, p.get(0));
					new AND(me, "pe").setAll(e, p.get(1));

					new BooleanFunction(me, "inf-test").setAll(p, me.getSignal("inf"), Type.DNF, new Value[][] {{Value.FALSE, Value.TRUE}});
					new AND(me, "nan-test").setAll(p, me.getSignal("nan"));
					new NOR(me, "zero-test").setAll(new Signal(p.get(0), e), me.getSignal("zero"));
				}
			},
			"m[n], e[m]",
			"zero[1], inf[1], nan[1]"
		)
	);

	private Flags(DeserializingStream in) throws IOException, DeserializingException, InstantiationException
	{
		super(in);
	}
}
