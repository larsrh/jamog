
package core.misc.test;

import core.build.Component;
import core.build.ComponentCollection;
import core.build.Environment;
import core.exception.AnalyzeException;
import core.exception.BuildException;
import core.exception.SerializingException;
import core.misc.module.PrivilegedAction;
import core.misc.serial.SerializingStream;
import core.monitor.AnalyzeListener;
import core.monitor.EnvironmentListener;
import core.monitor.SimulationListener;
import core.signal.Bit;
import core.signal.Signal;
import core.sim.Calculator;
import core.sim.Simulator;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.math.BigInteger;
import java.util.LinkedList;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReferenceArray;

import static core.misc.BitConverter.*;

// TODO: implement test progress
public final class TestSuite
{
	public final void assertEquals(String check, Object expected, Object result)
	{
		assertEquals(check, expected, result, false, new int[0]);
	}
	public final void assertSignedEquals(String check, Object expected, Object result)
	{
		assertEquals(check, expected, result, true, new int[0]);
	}

	public final void predictableRandomSignal(Signal s)
	{
		predictableRandomSignal(s, 0, s.size());
	}

	public final void predictableRandomSignal(Signal s, int start, int length)
	{
		int j = 0;
		for(int i = 0; i < (length - 1) / 48 + 1; ++i)
		{
			seed = (seed * 0x5DEECE66DL + 0xBL) & ((1L << 48) - 1);
			for(int k = 0; k < 48 && j < length; ++j, ++k)
				s.setBit(start + j, (seed & (1L << k)) != 0 ? Bit.H : Bit.L);
		}
	}

	public final void predictableRandomBits(Bit[] bits)
	{
		for(int i = 0; i < (bits.length - 1) / 48 + 1; ++i)
		{
			seed = (seed * 0x5DEECE66DL + 0xBL) & ((1L << 48) - 1);
			for(int j = i * 48, k = 0; j < (i + 1) * 48 && j < bits.length; ++j, ++k)
				bits[j] = (seed & (1 << k)) != 0 ? Bit.H : Bit.L;
		}
	}

	public final int predictableRandomInteger(int n)
	{
        if((n & -n) == n)
		{
			seed = (seed * 0x5DEECE66DL + 0xBL) & ((1L << 48) - 1);
            return (int)((n * (seed >>> 17)) >> 31);
		}

        int bits, val;
        do
		{
			seed = (seed * 0x5DEECE66DL + 0xBL) & ((1L << 48) - 1);
            bits = (int)(seed >>> 17);
            val = bits % n;
        }
		while(bits - val + (n-1) < 0);

        return val;
	}

	public final void createEnvironment()
	{
		environment = new Environment();
		simulation_stack = new LinkedList<LinkedList<TreeMap<String, TestResult.CalculatorSnapshot>>>();
	}

	public final <T extends Component> T addComponent(final Class<T> cc)
	{
		return
			new PrivilegedAction<T>()
			{
				@Override public final T run()
				{
					try { return cc.getConstructor(ComponentCollection.class, String.class).newInstance(environment, "root:" + environment.size());}
					catch(IllegalAccessException ex) { throw new Failure("Creation of " + cc.getName() + " failed: " + ex.getMessage()); }
					catch(InstantiationException ex) { throw new Failure("Creation of " + cc.getName() + " failed: " + ex.getMessage()); }
					catch(InvocationTargetException ex) { throw new Failure("Creation of " + cc.getName() + " failed: " + ex.getMessage()); }
					catch(NoSuchMethodException ex) { throw new Failure("Creation of " + cc.getName() + " failed: " + ex.getMessage()); }
				}
			}.run();
	}

	public final void buildEnvironment()
	{
		if(simulator != null)
		{
			simulator.shutdown();
			simulator = null;
		}

		try
		{
			long t = System.nanoTime();
			simulator = environment.build(
				new EnvironmentListener()
				{
					@Override public final void increaseTotalCount(long delta)
					{
						built_count += delta;
					}

					@Override public final void increaseConstructedCount(long delta) { }

					@Override public final boolean exceptionOccured(Component c, Exception ex)
					{
						throw new Failure("Building of " + c.getName() + " failed",ex);
					}
				},
				new AnalyzeListener()
				{
					@Override public final void initTotals(int calculators, int priority_pairs)
					{
						step_stack = new AtomicReferenceArray<Calculator>(calculators);
					}

					@Override public final void increaseCalculators(int pass, int finished) { }

					@Override public final void increasePriorityPairs(int pass, int finished) { }
				}
			);
			build_time += System.nanoTime() - t;
			simulator.addSimulationListener(
				new SimulationListener()
				{
					@Override public final void calculatorFinished(Calculator finished_calculator)
					{
						step_count.getAndIncrement();
						//step_stack.set(step_count.getAndIncrement(), finished_calculator);
					}

					@Override public final void serialize(SerializingStream out) throws IOException, SerializingException { }
				}
			);
		}
		catch(AnalyzeException ex) { throw new Failure("Building failed: " + ex.getMessage()); }
		catch(BuildException ex) { throw new Failure("Building failed: " + ex.getMessage()); }
	}

	public final void doSimulation()
	{
		//LinkedList<TreeMap<String, TestResult.CalculatorSnapshot>> l = new LinkedList<TreeMap<String, TestResult.CalculatorSnapshot>>();
		//simulation_stack.add(l);
		boolean s;
		do
		{
			//TreeMap<String, TestResult.CalculatorSnapshot> m = new TreeMap<String, TestResult.CalculatorSnapshot>();
			//l.add(m);
			step_count.set(0);

			long t = System.nanoTime();
			s = simulator.doStep();
			simulation_time += System.nanoTime() - t;

			/*for(int i = 0; i < step_count.get(); ++i)
			{
				Calculator c = step_stack.get(i);
				m.put(c.getName(), new TestResult.CalculatorSnapshot(c));
			}*/
			simulated_count += step_count.get();
			++simulation_step_count;
		}
		while(s);
		++simulation_count;
	}

	public long getSimulationCount()
	{
		return simulation_count_sum;
	}

	public long getSimulationStepCount()
	{
		return simulation_step_count_sum;
	}

	public final long getBuiltCount()
	{
		return built_count_sum;
	}

	public final long getSimulatedCount()
	{
		return simulated_count_sum;
	}

	public final double getBuildTime()
	{
		return build_time_sum / 1000000000.0;
	}

	public final double getSimulationTime()
	{
		return simulation_time_sum / 1000000000.0;
	}

	final TestResult runTest(Testable test)
	{
		seed = 8682522807148012L;

		simulation_count = 0;
		simulation_step_count = 0;
		built_count = 0;
		simulated_count = 0;
		build_time = 0;
		simulation_time = 0;

		step_count = new AtomicInteger(0);

		TestResult result;
		try
		{
			test.test(this);

			simulation_count_sum += simulation_count;
			simulation_step_count_sum += simulation_step_count;
			built_count_sum += built_count;
			simulated_count_sum += simulated_count;
			build_time_sum += build_time;
			simulation_time_sum += simulation_time;

			result = new TestResult(true, simulation_count, simulation_step_count, built_count, simulated_count, build_time, simulation_time, simulation_stack);
		}
		catch(Failure f)
		{
			result = new TestResult(false, simulation_count, simulation_step_count, built_count, simulated_count, build_time, simulation_time, simulation_stack);
			f.printStackTrace();
		}

		if(simulator != null)
		{
			simulator.shutdown();
			simulator = null;
		}

		return result;
	}

	private static final class Failure extends Error
	{
		public Failure(String msg)
		{
			super(msg);
		}

		public Failure(String message, Throwable cause)
		{
			super(message, cause);
		}

	}

	private static final void assertEquals(String check, Object expected, Object result, boolean signed, int[] place)
	{
		if(expected instanceof Bit && result instanceof Bit)
		{
			if(expected != result)
				throw new Failure(check + placeToString(place) + " expected " + expected + ", but result was " + result);
		}
		else if((expected instanceof Signal || expected instanceof Bit[] || expected instanceof Byte || expected instanceof Integer || expected instanceof Long || expected instanceof BigInteger) && (result instanceof Signal || result instanceof Bit[] || result instanceof Byte || result instanceof Integer || result instanceof Long || result instanceof BigInteger))
		{
			BigInteger e;
			BigInteger r;

			if(expected instanceof Signal)
				e = signalToInteger(signed, (Signal)expected);
			else if(expected instanceof Bit[])
				e = bitsToInteger(signed, (Bit[])expected);
			else if(expected instanceof Byte)
			{
				e = BigInteger.valueOf((Byte)expected);
				if(!signed && e.signum() == -1)
					e = e.add(BigInteger.ONE.shiftLeft(8));
			}
			else if(expected instanceof Integer)
			{
				e = BigInteger.valueOf((Integer)expected);
				if(!signed && e.signum() == -1)
					e = e.add(BigInteger.ONE.shiftLeft(32));
			}
			else if(expected instanceof Long)
			{
				e = BigInteger.valueOf((Long)expected);
				if(!signed && e.signum() == -1)
					e = e.add(BigInteger.ONE.shiftLeft(64));
			}
			else
				e = (BigInteger)expected;

			if(result instanceof Signal)
				r = signalToInteger(signed, (Signal)result);
			else if(result instanceof Bit[])
				r = bitsToInteger(signed, (Bit[])result);
			else if(result instanceof Byte)
			{
				r = BigInteger.valueOf((Byte)result);
				if(!signed && r.signum() == -1)
					r = r.add(BigInteger.ONE.shiftLeft(8));
			}
			else if(result instanceof Integer)
			{
				r = BigInteger.valueOf((Integer)result);
				if(!signed && r.signum() == -1)
					r = r.add(BigInteger.ONE.shiftLeft(32));
			}
			else if(result instanceof Long)
			{
				r = BigInteger.valueOf((Long)result);
				if(!signed && r.signum() == -1)
					r = r.add(BigInteger.ONE.shiftLeft(64));
			}
			else
				r = (BigInteger)result;

			if(!e.equals(r))
				throw new Failure(check + placeToString(place) + " expected " + e + ", but result was " + r);
		}
		else if((expected instanceof byte[] || expected instanceof int[] || expected instanceof long[] || expected instanceof Object[]) && (result instanceof byte[] || result instanceof int[] || result instanceof long[] || result instanceof Object[]))
		{
			Object[] ae;
			if(expected instanceof byte[])
			{
				byte[] a = (byte[])expected;
				ae = new Object[a.length];
				for(int i = 0; i < a.length; ++i)
					ae[i] = a[i];
			}
			else if(expected instanceof int[])
			{
				int[] a = (int[])expected;
				ae = new Object[a.length];
				for(int i = 0; i < a.length; ++i)
					ae[i] = a[i];
			}
			else if(expected instanceof long[])
			{
				long[] a = (long[])expected;
				ae = new Object[a.length];
				for(int i = 0; i < a.length; ++i)
					ae[i] = a[i];
			}
			else
				ae = (Object[])expected;

			Object[] ar;
			if(result instanceof byte[])
			{
				byte[] a = (byte[])result;
				ar = new Object[a.length];
				for(int i = 0; i < a.length; ++i)
					ar[i] = a[i];
			}
			else if(result instanceof int[])
			{
				int[] a = (int[])result;
				ar = new Object[a.length];
				for(int i = 0; i < a.length; ++i)
					ar[i] = a[i];
			}
			else if(result instanceof long[])
			{
				long[] a = (long[])result;
				ar = new Object[a.length];
				for(int i = 0; i < a.length; ++i)
					ar[i] = a[i];
			}
			else
				ar = (Object[])result;

			if(ae.length != ar.length)
				throw new Failure(check + placeToString(place) + " expected and result length mismatched");

			for(int i = 0; i < ae.length; ++i)
			{
				int[] dp = new int[place.length + 1];
				System.arraycopy(place, 0, dp, 0, place.length);
				dp[dp.length - 1] = i;
				assertEquals(check, ae[i], ar[i], signed, dp);
			}
		}
		else
			throw new Failure(check + placeToString(place) + " expected and result depth mismatched");
	}

	private static final String placeToString(int... place)
	{
		StringBuilder sb = new StringBuilder();

		for(int i = 0; i < place.length; ++i)
			sb.append("[").append(place[i]).append("]");

		return sb.toString();
	}

	private Environment environment;
	private Simulator simulator = null;
	private long seed;

	private long simulation_count;
	private long simulation_step_count;
	private long built_count;
	private long simulated_count;
	private long build_time;
	private long simulation_time;

	private AtomicReferenceArray<Calculator> step_stack;
	private AtomicInteger step_count;

	private LinkedList<LinkedList<TreeMap<String, TestResult.CalculatorSnapshot>>> simulation_stack;

	private long simulation_count_sum = 0;
	private long simulation_step_count_sum = 0;
	private long built_count_sum = 0;
	private long simulated_count_sum = 0;
	private long build_time_sum = 0;
	private long simulation_time_sum = 0;
}
