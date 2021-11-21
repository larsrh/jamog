
/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *                                                                           *
 * Copyright 2009 Lars Hupel, Torben Maack, Sylvester Tremmel                *
 *                                                                           *
 * This file is part of Jamog.                                               *
 *                                                                           *
 * Jamog is free software: you can redistribute it and/or modify             *
 * it under the terms of the GNU General Public License as published by      *
 * the Free Software Foundation; version 3.                                  *
 *                                                                           *
 * Jamog is distributed in the hope that it will be useful,                  *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of            *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the              *
 * GNU General Public License for more details.                              *
 *                                                                           *
 * You should have received a copy of the GNU General Public License         *
 * along with Jamog. If not, see <http://www.gnu.org/licenses/>.             *
 *                                                                           *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

package core.build;

import core.build.checking.DimensionMatcher;
import core.build.checking.Expression;
import core.build.checking.MatcherContext;
import core.build.checking.Parser;
import core.build.checking.types.Type;
import core.build.checking.Dimension;
import core.exception.ParameterException;
import core.exception.EvaluationException;
import core.exception.MatchingException;
import core.exception.ParseException;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The {@code Flavor} class provides checking and building of component
 * specifications from a {@link Component}. For building a {@link Component} a
 * flavor has to be specified along with input and output signals and other
 * parameters. The {@code Flavor} provides checking these signals and
 * parameters for allowed combinations and buildability with the {@link
 * Component} and the actually building the {@link Component}.
 * If a {@code Flavor} of a {@link Component} defines a parent {@code Flavor},
 * it will be handled like if they would be a single {@code Flavor} (which
 * means all constraints must be fulfilled, all {@link Signal}s must be
 * present, and the {@link Dimension}s must not contradict). However,
 * parameters of the child override equally named parameters of the parent.
 * If a parent {@code Flavor} is provided, this object will share it's
 * {@link MatcherContext} with it.
 * 
 * @author lars
 */
public final class Flavor
{
	/**
	 * The {@code Buildable} interface is used to provide the actual build
	 * method for a {@code Flavor}. After passing the parameter checking,
	 * the {@link #build(core.build.Component, java.util.Map)
	 * build(Component, Map)} method of the flavor is invoked with the {@link
	 * Component} that is being built.
	 *
	 * @param <T> The flavors {@link Component} subclass to build
	 */
	public static interface Buildable<T extends Component>
	{
		/**
		 * Is invoked to actually build a {@link Component} that has passed
		 * parameter checking.
		 *
		 * @param component The {@link Component} to build
		 * @param variables   The map from variable names to values
		 */
		public void build(T component, Map<String, Integer> variables);
	}

	/**
	 * @param flavors The {@code Flavor}s in the map
	 *
	 * @return A map from flavor names to {@code Flavor}s.
	 */
	public static final Map<String, Flavor> getMap(final Flavor... flavors)
	{
		final Map<String, Flavor> map = new LinkedHashMap<String, Flavor>();
		for(final Flavor flavor : flavors)
			map.put(flavor.name, flavor);
		return Collections.unmodifiableMap(map);
	}

	/**
	 * The default flavor name.
	 */
	public static final String DEFAULT = "default";

	/**
	 * Invokes {@link #Flavor(java.lang.String, core.build.Flavor.Buildable,
	 * java.lang.String, java.lang.String, java.lang.String, java.lang.String,
	 * core.build.Flavor) this(DEFAULT, buildable, inputs, outputs, "", "",
	 * null)}.
	 *
	 * @param buildable The {@link Buildable} to build {@link Component}s with
	 *                  this {@code Flavor}
	 * @param inputs    A string defining the inputs
	 * @param outputs   A string defining the outputs
	 *
	 * @see Parser
	 */
	public Flavor(final Buildable buildable, final String inputs, final String outputs)
	{
		this(DEFAULT, buildable, inputs, outputs, "", "", null);
	}

	/**
	 * Invokes {@link #Flavor(java.lang.String, core.build.Flavor.Buildable,
	 * java.lang.String, java.lang.String, java.lang.String, java.lang.String,
	 * core.build.Flavor) this(DEFAULT, buildable, inputs, outputs, "",
	 * constraint, null)}.
	 *
	 * @param buildable  The {@link Buildable} to build {@link Component}s with
	 *                   this {@code Flavor}
	 * @param inputs     A string defining the inputs
	 * @param outputs    A string defining the outputs
	 * @param constraint A string defining the constraint
	 *
	 * @see Parser
	 */
	public Flavor(final Buildable buildable, final String inputs, final String outputs, final String constraint)
	{
		this(DEFAULT, buildable, inputs, outputs, "", constraint, null);
	}

	/**
	 * Invokes {@link #Flavor(java.lang.String, core.build.Flavor.Buildable,
	 * java.lang.String, java.lang.String, java.lang.String, java.lang.String,
	 * core.build.Flavor) this(DEFAULT, buildable, inputs, outputs, parameters,
	 * constraint, null)}.
	 *
	 * @param buildable  The {@link Buildable} to build {@link Component}s with
	 *                   this {@code Flavor}
	 * @param inputs     A string defining the inputs
	 * @param outputs    A string defining the outputs
	 * @param parameters A string defining the parameters
	 * @param constraint A string defining the constraint
	 *
	 * @see Parser
	 */
	public Flavor(final Buildable buildable, final String inputs, final String outputs, final String parameters, final String constraint)
	{
		this(DEFAULT, buildable, inputs, outputs, parameters, constraint, null);
	}

	/**
	 * Invokes {@link #Flavor(java.lang.String, core.build.Flavor.Buildable,
	 * java.lang.String, java.lang.String, java.lang.String, java.lang.String,
	 * core.build.Flavor) this(DEFAULT, buildable, inputs, outputs, parameters,
	 * constraint, parent)}.
	 *
	 * @param buildable  The {@link Buildable} to build {@link Component}s with
	 *                   this {@code Flavor}
	 * @param inputs     A string defining the inputs
	 * @param outputs    A string defining the outputs
	 * @param parameters A string defining the parameters
	 * @param constraint A string defining the constraint
	 * @param parent     The parent {@link Flavor}
	 *
	 * @see Parser
	 */
	public Flavor(final Buildable buildable, final String inputs, final String outputs, final String parameters, final String constraint, final Flavor parent)
	{
		this(DEFAULT, buildable, inputs, outputs, parameters, constraint, parent);
	}

	/**
	 * Invokes {@link #Flavor(java.lang.String, core.build.Flavor.Buildable,
	 * java.lang.String, java.lang.String, java.lang.String, java.lang.String,
	 * core.build.Flavor) this(name, buildable, inputs, outputs, "", "",
	 * parent)}.
	 *
	 * @param name       The name of the {@code Flavor}
	 * @param buildable  The {@link Buildable} to build {@link Component}s with
	 *                   this {@code Flavor}
	 * @param inputs     A string defining the inputs
	 * @param outputs    A string defining the outputs
	 *
	 * @see Parser
	 */
	public Flavor(final String name, final Buildable buildable, final String inputs, final String outputs)
	{
		this(name, buildable, inputs, outputs, "", "", null);
	}

	/**
	 * Invokes {@link #Flavor(java.lang.String, core.build.Flavor.Buildable,
	 * java.lang.String, java.lang.String, java.lang.String, java.lang.String,
	 * core.build.Flavor) this(name, buildable, inputs, outputs, "",
	 * constraint, null)}.
	 *
	 * @param name       The name of the {@code Flavor}
	 * @param buildable  The {@link Buildable} to build {@link Component}s with
	 *                   this {@code Flavor}
	 * @param inputs     A string defining the inputs
	 * @param outputs    A string defining the outputs
	 * @param constraint A string defining the constraint
	 *
	 * @see Parser
	 */
	public Flavor(final String name, final Buildable buildable, final String inputs, final String outputs, final String constraint)
	{
		this(name, buildable, inputs, outputs, "", constraint, null);
	}

	/**
	 * Invokes {@link #Flavor(java.lang.String, core.build.Flavor.Buildable,
	 * java.lang.String, java.lang.String, java.lang.String, java.lang.String,
	 * core.build.Flavor) this(name, buildable, inputs, outputs, parameters,
	 * constraint, null)}.
	 *
	 * @param name       The name of the {@code Flavor}
	 * @param buildable  The {@link Buildable} to build {@link Component}s with
	 *                   this {@code Flavor}
	 * @param inputs     A string defining the inputs
	 * @param outputs    A string defining the outputs
	 * @param parameters A string defining the parameters
	 * @param constraint A string defining the constraint
	 *
	 * @see Parser
	 */
	public Flavor(final String name, final Buildable buildable, final String inputs, final String outputs, final String parameters, final String constraint)
	{
		this(name, buildable, inputs, outputs, parameters, constraint, null);
	}

	/**
	 * Builds a new {@code Flavor} with the given parameters. The given strings
	 * are parsed by the appropriate methods in {@link Parser}, combined with
	 * the parents statements if parent is not null, and cached for fast
	 * checking.
	 *
	 * @param name       The name of the {@code Flavor}
	 * @param buildable  The {@link Buildable} to build {@link Component}s with
	 *                   this {@code Flavor}
	 * @param inputs     A string defining the inputs
	 * @param outputs    A string defining the outputs
	 * @param parameters A string defining the parameters
	 * @param constraint A string defining the constraint
	 * @param parent     The parent {@link Flavor}
	 *
	 * @see Parser
	 */
	public Flavor(final String name, final Buildable buildable, final String inputs, final String outputs, final String parameters, final String constraint, final Flavor parent)
	{
		this.name = name;
		this.buildable = buildable;

		this.dimension_map = new HashMap<String, Dimension[]>();

		try
		{
			if(parent != null)
			{
				// TODO: other direction? shouldn't parent override child to ensure correctness?
				this.matcher = parent.matcher;
				
				final Map<String, Type> in = new LinkedHashMap<String, Type>(parent.input_types);
				final Map<String, Type> out = new LinkedHashMap<String, Type>(parent.output_types);
				final Map<String, Type> para = new LinkedHashMap<String, Type>(parent.parameter_types);
				in.putAll(Parser.parseSignals(dimension_map, matcher, inputs));
				out.putAll(Parser.parseSignals(dimension_map, matcher, outputs));
				para.putAll(Parser.parseParameters(dimension_map, matcher, parameters));
				this.input_types = Collections.unmodifiableMap(in);
				this.output_types = Collections.unmodifiableMap(out);
				this.parameter_types = Collections.unmodifiableMap(para);
			}
			else
			{
				this.matcher = new MatcherContext();
				
				this.input_types = Collections.unmodifiableMap(Parser.parseSignals(dimension_map, matcher, inputs));
				this.output_types = Collections.unmodifiableMap(Parser.parseSignals(dimension_map, matcher, outputs));
				this.parameter_types = Collections.unmodifiableMap(Parser.parseParameters(dimension_map, matcher, parameters));
			}

			this.dimensions = new DimensionMatcher(dimension_map);
			this.matches = new HashMap<String, Integer>();
			this.constraint = Parser.parseConstraint(constraint);
			this.parent = parent;
		}
		catch(final ParseException ex)
		{
			assert false : "parse exception occured";

			// TODO: this is a compiler fix, better implementation?
			throw new IllegalArgumentException("parse exception occured",ex);
		}
	}

	/**
	 * @return The name of the {@code Flavor}
	 */
	public final String getName()
	{
		return name;
	}

	/**
	 * Builds a {@link Component} with the {@link Buildable} of this {@code
	 * Flavor} and its parent, if it has one.
	 *
	 * @param component The {@link Component} to build
	 */
	@SuppressWarnings("unchecked")
	final void build(final Component component)
	{
		if(parent != null)
			parent.build(component);
		buildable.build(component, matches);
	}

	/**
	 * @return A map from names to {@link Type}s for the inputs
	 */
	public final Map<String, Type> getInputs()
	{
		return input_types;
	}

	/**
	 * @return A map from names to {@link Type}s for the outputs
	 */
	public final Map<String, Type> getOutputs()
	{
		return output_types;
	}

	/**
	 * @return A map from names to {@link Type}s for the parameters
	 */
	public final Map<String, Type> getParameters()
	{
		return parameter_types;
	}

	/**
	 * @return The map from variable names to values
	 */
	public final Map<String, Integer> getVariables()
	{
		return Collections.unmodifiableMap(matches);
	}

	/**
	 * Checks the specified dimension restrictions against the given
	 * dimensions.
	 *
	 * @param dimensions A map from parameter names to a list of dimensions
	 *                   sizes
	 *
	 * @return true if the given dimensions are applicable, false otherwise
	 */
	public final boolean checkDimensions(final Map<String, List<Integer>> dimensions)
	{
		this.matcher.reset();

		return checkDimensions0(dimensions);
	}

	private final boolean checkDimensions0(final Map<String, List<Integer>> dimensions)
	{
		if (parent != null && !parent.checkDimensions0(dimensions))
			return false;

		try { return this.dimensions.matchLengths(dimensions); }
		catch(final MatchingException ex) { return false; }
	}

	/**
	 * Checks the specified object restrictions against the given objects.
	 *
	 * @param values A map from parameter names to objects
	 *
	 * @return true if the objects are applicable for building, false otherwise
	 */
	public final boolean check(final Map<String, Object> values)
	{
		try { checkAndThrow(values); }
		catch(final ParameterException ex) { return false; }
		catch(final EvaluationException ex) { return false; }
		catch(final MatchingException ex) { return false; }

		return true;
	}

	/**
	 * Checks the specified object restrictions against the given objects.
	 *
	 * @param values A map from parameter names to objects
	 *
	 * @throws ParameterException  if a needed parameter was not given or of
	 *                             wrong type
	 * @throws EvaluationException if the constraint evaluation failed
	 * @throws MatchingException   if the dimension matching failed
	 */
	public final void checkAndThrow(final Map<String, Object> values) throws ParameterException, EvaluationException, MatchingException
	{
		for(final Map.Entry<String, ? extends Type> e : input_types.entrySet())
		{
			if(!values.containsKey(e.getKey()))
				throw new ParameterException("input signal '" + e.getKey() + "' not given");
			if(!e.getValue().canAssign(values.get(e.getKey())))
				throw new ParameterException("input signal '" + e.getKey() + "' has wrong type");
		}
		for(final Map.Entry<String, ? extends Type> e : output_types.entrySet())
		{
			if(!values.containsKey(e.getKey()))
				throw new ParameterException("output signal '" + e.getKey() + "' not given");
			if(!e.getValue().canAssign(values.get(e.getKey())))
				throw new ParameterException("output signal '" + e.getKey() + "' has wrong type");
		}
		for(final Map.Entry<String, ? extends Type> e : parameter_types.entrySet())
		{
			if(!values.containsKey(e.getKey()))
				throw new ParameterException("parameter '" + e.getKey() + "' not given");
			if(!e.getValue().canAssign(values.get(e.getKey())))
				throw new ParameterException("parameter '" + e.getKey() + "' has wrong type");
		}

		if(!matchDimensions(values))
			throw new MatchingException("dimension match couldn't be completed");

		checkConstraint(values);
	}

	private final boolean matchDimensions(final Map<String, Object> values) throws MatchingException
	{
		matcher.reset();
		return matchDimensions0(values);
	}

	private final boolean matchDimensions0(final Map<String, Object> values) throws MatchingException
	{
		matches.clear();

		if(parent != null)
		{
			if(!parent.matchDimensions0(values))
				return false;
			else if(dimensions.matchParameters(values))
			{
				matches.putAll(matcher.getLengths());
				return true;
			}
		}
		else
			if(dimensions.matchParameters(values))
			{
				matches.putAll(matcher.getLengths());
				return true;
			}

		return false;
	}

	private final void checkConstraint(final Map<String, Object> values) throws EvaluationException
	{
		final Map<String, Object> all = new HashMap<String, Object>(values);
		all.putAll(matches);

		if(parent != null)
			parent.checkConstraint(values);

		if(!constraint.evaluate(all))
			throw new EvaluationException("constraint check failed: constraint '"+constraint+"', expected true");
	}

	@Override public String toString()
	{
		StringBuilder sb = new StringBuilder(name)
			.append("\ninputs:\n").append(input_types)
			.append("\noutputs:\n").append(output_types)
			.append("\nparameters:\n").append(parameter_types);
		
		sb.append("\ndimensions:\n");
		for (Map.Entry<String,Dimension[]> entry : dimension_map.entrySet()) {
			sb.append(entry.getKey()).append("=");
			for (Dimension d : entry.getValue())
				sb.append("[").append(d).append("]\n");
		}
		return sb.append("\nconstraint:\n").append(constraint).toString();
	}

	private final String name;
	private final Buildable buildable;
	private final MatcherContext matcher;
	private final DimensionMatcher dimensions;
	private final Map<String, Dimension[]> dimension_map;
	private final Map<String, Type> input_types;
	private final Map<String, Type> output_types;
	private final Map<String, Type> parameter_types;
	private final Map<String, Integer> matches;
	private final Expression<Boolean> constraint;
	private final Flavor parent;
}
