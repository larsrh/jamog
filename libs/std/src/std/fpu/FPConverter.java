
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


import std.alu.RipleNegator;
import std.convert.BinaryDecoder;
import std.mux.BinaryMultiplexer;
import std.mux.Multiplexer;
import static core.signal.Bit.*;

public class FPConverter extends Composite
{
	public FPConverter(ComponentCollection parent,String name)
	{
		super(parent,name);
	}

	@Override public final Map<String, Flavor> getFlavors()
	{
		return flavors;
	}

	private static final Map<String, Flavor> flavors = Flavor.getMap(
		new Flavor(
			new Flavor.Buildable<FPConverter>()
			{
				@Override public void build(FPConverter me, Map<String, Integer> matches)
				{
					int ms = matches.get("n");
					int es = matches.get("m");
					int os = matches.get("l");

					Signal emx = new Signal(new Signal(L, os - ms), me.getSignal("m"), new Signal(L, os - 1));
					Signal[] mx_sh = new Signal[os + 1];
					for(int i = 0; i < os; ++i)
						mx_sh[i] = emx.get(os - i, os - 1);
					mx_sh[os] = new Signal(H, os - 1);
					Signal eex = new Signal(1 << es);
					new BinaryDecoder(me, "fpc_bd").setAll(me.getSignal("e"), eex);
					Signal ui = new Signal(new Signal(os - 1), new Signal(L));
					new Multiplexer(me, "fpc_mux").setAll(mx_sh, eex.get((1 << (es - 1)) - 2, os + 1), ui.get(0, os - 1));
					Signal ni = new Signal(os);
					new RipleNegator(me, "fpc_neg").setAll(ui, ni);
					new BinaryMultiplexer(me, "fpc_smux").setAll(new Signal[] {ui, ni}, me.getSignal("s"), me.getSignal("out"));
				}
			},
			"m[n], e[m], s[1]",
			"out[l]"
		)
	);

	private FPConverter(DeserializingStream in) throws IOException, DeserializingException, InstantiationException
	{
		super(in);
	}
}
