
package core.misc.test;

import core.build.Flavor;
import core.signal.Signal;
import core.sim.Calculator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public final class TestResult
{
	public final boolean isSuccessful()
	{
		return success;
	}

	public long getSimulationCount()
	{
		return simulation_count;
	}

	public long getSimulationStepCount()
	{
		return simulation_step_count;
	}

	public final long getBuildedCount()
	{
		return builded_count;
	}

	public final long getSimulatedCount()
	{
		return simulated_count;
	}

	public final double getBuildTime()
	{
		return build_time / 1000000000.0;
	}

	public final double getSimulationTime()
	{
		return simulation_time / 1000000000.0;
	}

	public final LinkedList<LinkedList<TreeMap<String, CalculatorSnapshot>>> getSimulationStack()
	{
		return simulation_stack;
	}

	public TestResult(boolean success, long simulation_count, long simulation_step_count, long builded_count, long simulated_count, long build_time, long simulation_time, LinkedList<LinkedList<TreeMap<String, CalculatorSnapshot>>> simulation_stack)
	{
		this.success = success;

		this.simulation_count = simulation_count;
		this.simulation_step_count = simulation_step_count;
		this.builded_count = builded_count;
		this.simulated_count = simulated_count;
		this.build_time = build_time;
		this.simulation_time = simulation_time;

		this.simulation_stack = simulation_stack;
	}

	static final class CalculatorSnapshot
	{
		CalculatorSnapshot(Calculator calculator)
		{
			this.calculator = calculator;
			inputs = new LinkedHashMap<String, Object>();
			outputs = new LinkedHashMap<String, Object>();

			Flavor f = calculator.getFlavors().get(calculator.getFlavor());
			Set<String> in = f.getInputs().keySet();
			Set<String> out = f.getOutputs().keySet();
			for(Map.Entry<String, Object> entry : calculator.getParameters().entrySet())
			{
				if(in.contains(entry.getKey()))
					inputs.put(entry.getKey(), sigCopy(entry.getValue()));
				else if(out.contains(entry.getKey()))
					outputs.put(entry.getKey(), sigCopy(entry.getValue()));
			}
		}

		private static final Object sigCopy(Object o)
		{
			if(o instanceof Object[])
			{
				Object[] a = (Object[])o;
				Object[] r = new Object[a.length];
				for(int i = 0; i < a.length; ++i)
					r[i] = sigCopy(a[i]);
				return r;
			}
			else
				return ((Signal)o).getBits();
		}

		final Calculator calculator;
		final Map<String, Object> inputs;
		final Map<String, Object> outputs;
	}

	private final boolean success;

	private final long simulation_count;
	private final long simulation_step_count;
	private final long builded_count;
	private final long simulated_count;
	private final long build_time;
	private final long simulation_time;

	private final LinkedList<LinkedList<TreeMap<String, CalculatorSnapshot>>> simulation_stack;
}
