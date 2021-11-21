
package dlx;

import core.build.ComponentCollection;
import core.build.Composite;
import core.build.Flavor;
import core.exception.DeserializingException;
import core.exception.InstantiationException;
import core.misc.serial.DeserializingStream;
import core.signal.Signal;
import java.io.IOException;
import std.fpu.Adder;
import std.fpu.Multiplier;
import std.fpu.Normalizer;
import java.util.Map;

import std.fpu.BinaryConverter;
import std.fpu.Divider;
import std.fpu.FPConverter;
import std.fpu.Flags;
import std.gate.NOR;
import std.gate.NOT;
import std.gate.OR;
import std.logic.BooleanFunction;
import std.mux.BinaryMultiplexer;

import std.mux.Multiplexer;
import static core.signal.Bit.*;
import static std.logic.BooleanFunction.*;

public class FPU extends Composite
{
	public FPU(ComponentCollection parent,String name)
	{
		super(parent,name);
	}

	public final FPU setAll(Signal x, Signal y, Signal opcode, Signal out)
	{
		return (FPU)setAll(new String[] {"x", "y", "opcode", "out"}, x, y, opcode, out);
	}

	@Override public final Map<String, Flavor> getFlavors()
	{
		return flavors;
	}

	private static final Map<String, Flavor> flavors = Flavor.getMap(
		new Flavor(
			new Flavor.Buildable<FPU>()
			{
				@Override public void build(FPU me, Map<String, Integer> matches)
				{
					Signal x = me.getSignal("x");
					Signal y = me.getSignal("y");

					Signal opcode = me.getSignal("opcode");

					Signal mx = new Signal(x.get(0, 23), new Signal(1));
					Signal ex = x.get(23, 8);
					Signal sx = x.get(31);

					Signal my = new Signal(y.get(0, 23), new Signal(1));
					Signal ey = y.get(23, 8);
					Signal sy = y.get(31);

					new OR(me, "x_ext").setAll(x.get(23, 8), mx.get(23));
					new OR(me, "y_ext").setAll(y.get(23, 8), my.get(23));

					Signal[] c_out = new Signal[12];

					Signal izero = new Signal(2);
					Signal iinf = new Signal(2);
					Signal inan = new Signal(2);
					new Flags(me, "i1_fl").setAll(x.get(0, 23), ex, izero.get(0), iinf.get(0), inan.get(0));
					new Flags(me, "i2_fl").setAll(y.get(0, 23), ey, izero.get(1), iinf.get(1), inan.get(1));

					{
						Signal out = new Signal(32);
						Signal stat = new Signal(3);
						Signal den_m_out = new Signal(49);
						Signal den_e_out = new Signal(8);
						new Adder(me, "add").setAll(mx, ex, sx, my, ey, sy, opcode.get(0), iinf, den_m_out, den_e_out, out.get(31), stat.get(0));
						new Normalizer(me, "add_norm").setAll(den_m_out, den_e_out, out.get(0, 23), out.get(23, 8), stat.get(1), stat.get(2), 47);
						c_out[0] = me.handleSpecialCases("add", out, stat.get(1), new Signal(stat.get(2), iinf), new Signal(stat.get(0), inan));
					}

					{
						Signal out = new Signal(32);
						Signal stat = new Signal(5);
						Signal den_m_out = new Signal(48);
						Signal den_e_out = new Signal(8);
						new Multiplier(me, "mul").setAll(mx, ex, sx, my, ey, sy, izero, iinf, den_m_out, den_e_out, out.get(31), stat.get(0), stat.get(1), stat.get(2));
						new Normalizer(me, "mul_norm").setAll(den_m_out, den_e_out, out.get(0, 23), out.get(23, 8), stat.get(3), stat.get(4), 46);
						c_out[1] = me.handleSpecialCases("mul", out, new Signal(stat.get(0), stat.get(3), izero), new Signal(stat.get(1), stat.get(4), iinf), new Signal(stat.get(2), inan));
					}

					{
						Signal out = new Signal(32);
						Signal stat = new Signal(7);
						Signal den_m_out = new Signal(50);
						Signal den_e_out = new Signal(8);
						new Divider(me, "div").setAll(mx, ex, sx, my, ey, sy, izero, iinf, den_m_out, den_e_out, out.get(31), stat.get(0), stat.get(1), stat.get(2), stat.get(3), stat.get(4));
						new Normalizer(me, "div_norm").setAll(den_m_out, den_e_out, out.get(0, 23), out.get(23, 8), stat.get(5), stat.get(6), 26);
						c_out[2] = me.handleSpecialCases("div", out, new Signal(stat.get(0), stat.get(2), stat.get(5)), new Signal(stat.get(1), stat.get(3), stat.get(6)), new Signal(stat.get(4), inan));
					}

					{
						Signal cmp = new Signal(c_out[0].get(31), new Signal(1));
						new NOR(me, "cmp_zero_test").setAll(c_out[0].get(0, 31), cmp.get(1));

						Signal out = new Signal(new Signal(4), cmp.get(1), new Signal(1));
						new BooleanFunction(me, "lt_test").setAll(cmp, out.get(0), Type.DNF, new Value[][] {{Value.TRUE, Value.FALSE}});
						new NOR(me, "gt_test").setAll(cmp, out.get(1));
						new OR(me, "le_test").setAll(cmp, out.get(2));
						new BooleanFunction(me, "ge_test").setAll(cmp, out.get(3), Type.DNF, new Value[][] {{Value.FALSE, Value.IGNORE}, {Value.IGNORE, Value.TRUE}});
						new NOT(me, "ne_test").setAll(cmp.get(1), out.get(5));

						Signal sel = new Signal(5);
						new BooleanFunction(me, "cmp_nrm_test").setAll(sel.get(1, 4), sel.get(0), Type.DNF, new Value[][] {{Value.FALSE, Value.FALSE, Value.FALSE, Value.FALSE}});
						Signal cinf = new Signal(1);
						new OR(me, "cmp_cinf").setAll(iinf, cinf);
						new BooleanFunction(me, "cmp_inf_eq_test").setAll(new Signal(cinf, sx, sy), sel.get(1), Type.DNF, new Value[][] {{Value.TRUE, Value.TRUE, Value.TRUE}, {Value.TRUE, Value.FALSE, Value.FALSE}});
						new BooleanFunction(me, "cmp_inf_lt_test").setAll(new Signal(cinf, sx, sy), sel.get(2), Type.DNF, new Value[][] {{Value.TRUE, Value.TRUE, Value.FALSE}});
						new BooleanFunction(me, "cmp_inf_gt_test").setAll(new Signal(cinf, sx, sy), sel.get(3), Type.DNF, new Value[][] {{Value.TRUE, Value.FALSE, Value.TRUE}});
						new OR(me, "cmp_nan_test").setAll(inan, sel.get(4));

						for(int i = 3; i < 9; ++i)
							c_out[i] = new Signal(new Signal(1), new Signal(L, 31));
						new Multiplexer(me, "cmp_mux").setAll(new Signal[] {out, new Signal(L, L, H, H, H, L), new Signal(H, L, H, L, L, H), new Signal(L, H, L, H, L, H), new Signal(L, 6)}, sel, new Signal(c_out[3].get(0), c_out[4].get(0), c_out[5].get(0), c_out[6].get(0), c_out[7].get(0), c_out[8].get(0)));
					}

					c_out[9] = new Signal(iinf.get(0), new Signal(L, 31));

					c_out[10] = new Signal(32);
					new BinaryConverter(me, "bc").setAll(new String[] {"in", "m", "e", "s", "uf", "of"}, x, c_out[10].get(0, 23), c_out[10].get(23, 8), c_out[10].get(31), new Signal(1), new Signal(1));

					c_out[11] = new Signal(32);
					new FPConverter(me, "fpc").setAll(new String[] {"m", "e", "s", "out"}, mx, ex, sx, c_out[11]);

					new BinaryMultiplexer(me, "op_mux").setAll(c_out, opcode.get(1, 4), me.getSignal("out"));
				}
			},
			/* Opcode:
			 *	LLLLL x + y
			 *	HLLLL x - y
			 *	LHLLL x * y
			 *	LLHLL x / y
			 *	HHHLL x < y
			 *	HLLHL x > y
			 *	HHLHL x <= y
			 *	HLHHL x >= y
			 *	HHHHL x == y
			 *	HLLLH x != y
			 *  LHLLH inf(x)
			 *	LLHLH int2float(x)
			 *	LHHLH float2int(x)
			 *
			 *  nan(x) <=> x != x
			 *  neg(x) <=> x < 0 <=> x < r0
			 *  -x <=> 0 - x <=> r0 - x
			 *
			 */
			"x[32], y[32], opcode[5]",
			"out[32]"
		)
	);

	private FPU(DeserializingStream in) throws IOException, DeserializingException, InstantiationException
	{
		super(in);
	}

	private final Signal handleSpecialCases(String name, Signal in, Signal zero_ind, Signal inf_ind, Signal nan_ind)
	{
		Signal c = new Signal(zero_ind.size() == 1 ? zero_ind : new Signal(1), inf_ind.size() == 1 ? inf_ind : new Signal(1), nan_ind.size() == 1 ? nan_ind : new Signal(1));
		if(zero_ind.size() != 1)
			new OR(this, name + "_sel_zero_cmb").setAll(zero_ind, c.get(0));
		if(inf_ind.size() != 1)
			new OR(this, name + "_sel_inf_cmb").setAll(inf_ind, c.get(1));
		if(nan_ind.size() != 1)
			new OR(this, name + "_sel_nan_cmb").setAll(nan_ind, c.get(2));

		Signal sel = new Signal(new Signal(3), c.get(2));
		new NOR(this, name + "_sel_out").setAll(c, sel.get(0));
		new BooleanFunction(this, name + "_sel_zero").setAll(c, sel.get(1), Type.DNF, new Value[][] {{Value.TRUE, Value.FALSE, Value.FALSE}});
		new BooleanFunction(this, name + "_sel_inf").setAll(c.get(1, 2), sel.get(2), Type.DNF, new Value[][] {{Value.TRUE, Value.FALSE}});

		Signal out = new Signal(32);
		new Multiplexer(this, name + "_mux").setAll(new Signal[] {in, new Signal(new Signal(L, 31), in.get(31)), new Signal(new Signal(L, 23), new Signal(H, 8), in.get(31)), new Signal(H, 32)}, sel, out);

		return out;
	}
}
