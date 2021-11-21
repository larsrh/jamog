
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

package core.build.checking;

import core.build.checking.types.NullableType;
import core.build.checking.types.ArrayType;
import core.build.checking.types.MapType;
import core.build.checking.types.CollectionType;
import core.build.checking.types.Type;
import core.build.checking.Dimension.Range;
import core.exception.ParseException;
import core.misc.module.ClassLoader;
import core.signal.Signal;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Parses a {@link String} consisting of a comma-separated list of parameter
 * descriptions into a bunch of names and corresponding {@link Type}s
 * and {@link Dimension}s.
 * <p>Syntax:<br /><pre>
 * parameters = (parameter ',')* parameter
 * parameter = [ type ] name dimension*
 * type = identifier [ '&lt;' generic-parameters '&gt;' ] [ suffixes ]
 * generic-parameters = (type ',')* type
 * suffixes = ('[]' | '~')*
 * dimension = '[' (sum '|')* sum [ number '-' [ number ] | '-' number ] ']'
 * sum = (factor '*')* factor
 * factor = number | identifier
 * </pre></p>
 * The suffix '[]' denotes an array and should be used only inside
 * {@code generic-paramters}. The suffix '~' denotes a nullable type.
 * @see Dimension
 * @see Type
 * @author lars
 */
public final class ParameterParser {

	private static final List<String> imports = Arrays.asList("","java.lang.","java.util.","core.build.","core.build.checking.","core.signal.");

	private final String parameters;
	private int pos;
	private final Map<String, Dimension[]> dimensions;
	private final MatcherContext ctx;
	private final Type defaultType;

	private ParameterParser(String parameters, Map<String, Dimension[]> dimensions, MatcherContext ctx, Type defaultType) {
		this.parameters = parameters;
		this.dimensions = dimensions;
		this.ctx = ctx;
		this.defaultType = defaultType;
		this.pos = 0;
	}

	/**
	 * Parses a {@link String} consisting of a comma-separated list of parameter
	 * descriptions into a bunch of names and corresponding {@link Type}s
	 * and {@link Dimension}s.
	 * @see ParameterParser
	 * @param dimensions map of dimensions, doesn't have to be empty
	 * @param ctx a {@link MatcherContext} against which {@link Dimension}
	 * objects are created
	 * @param str the {@link String} to be parsed
	 * @param defaultType pass {@code null} here, if the {@code type} must be omitted
	 * in syntax. Otherwise, pass a {@link Type} object to specify the default
	 * one. Note that, in this case, no attempt is done to identify an explicitely
	 * given type. Doing so could possibly lead to a {@link ParseException}.
	 * @return mapping between parameter names and corresponding {@link Type}s.
	 * @throws ParseException
	 */
	@SuppressWarnings("empty-statement")
	public static final Map<String, Type> parseParameters(Map<String, Dimension[]> dimensions, MatcherContext ctx, String str, Type defaultType) throws ParseException {		
		if ("".equals(str.trim()))
			return Collections.<String,Type> emptyMap();
		ParameterParser pp = new ParameterParser(str, dimensions, ctx, defaultType);
		Map<String,Type> map = new LinkedHashMap<String, Type>();
		try {
			while (pp.parseParameter(map));
		}
		catch (ParseException ex) {
			throw ex;
		}
		catch (Exception ex) {
			throw new ParseException(ex,str);
		}
		return map;
	}

	private boolean skip() {
		boolean skipped = false;
		while (pos < parameters.length() && Character.isWhitespace(parameters.charAt(pos))) {
			if (!skipped) skipped = true;
			++pos;
		}
		return skipped;
	}

	private char get() {
		return parameters.charAt(pos++);
	}

	private static final Type getArrayedType(Type type, int dimensions) {
		return (dimensions == 0) ? type : new ArrayType(getArrayedType(type, dimensions - 1));
	}

	private boolean parseParameter(Map<String, Type> map) throws ParseException {
		Type type;
		if (defaultType == null)
			type = parseType();
		else
			type = defaultType;
		
		int dimensionDelta;
		
		// TODO: auto-detect dimension delta
		if (type.canAssign(Signal.class))
			dimensionDelta = 1;
		else
			dimensionDelta = 0;

		String name = parseIdentifier(false);
		if (map.containsKey(name))
			throw new ParseException("name '" + name + "' already existing",parameters);

		Dimension dims[] = parseDimensions(dimensionDelta);
		if (dims == null) {
			map.put(name, type);
		}
		else {
			map.put(name, getArrayedType(type, dims.length - dimensionDelta));
			dimensions.put(name, dims);
		}

		skip();
		if (pos == parameters.length())
			return false;
		else if (get() != ',')
			throw new ParseException("illegal end of parameter description",parameters);
		else
			return true;
	}

	private Dimension parseDimension(String str) throws ParseException {
		String parts[] = str.split(":");
		if (parts.length == 1) {
			return parseDimensionExpression(str);
		}
		else if (parts.length == 2) {
			Dimension dim = parseDimensionExpression(parts[0].trim());
			String minmax[] = parts[1].split("-");
			int min;
			if ("".equals(minmax[0].trim()))
				min = 1;
			else
				min = Integer.parseInt(minmax[0].trim());

			int max;
			if (minmax.length == 1 || "".equals(minmax[1].trim()))
				max = Integer.MAX_VALUE;
			else
				max = Integer.parseInt(minmax[1].trim());

			return new Range(dim, min, max);
		}
		else
			throw new ParseException("illegal dimension description");
	}

	private Dimension parseDimensionExpression(String str) throws ParseException {
		String options[] = str.split("\\|");
		if (options.length == 1)
			return parseDimensionSum(str.trim());

		Dimension dims[] = new Dimension[options.length];
		for (int i = 0; i < options.length; ++i)
			dims[i] = parseDimensionSum(options[i].trim());

		return new Dimension.Option(ctx, dims);
	}

	private Dimension parseDimensionSum(String str) throws ParseException {
		String summands[] = str.split("\\+");
		if (summands.length == 1)
			return parseDimensionProduct(str.trim());

		Dimension dims[] = new Dimension[summands.length];
		for (int i = 0; i < summands.length; ++i)
			dims[i] = parseDimensionProduct(summands[i].trim());

		return new Dimension.Sum(dims);
	}

	private Dimension parseDimensionProduct(String str) throws ParseException {
		String factors[] = str.split("\\*");
		if (factors.length == 1)
			return parseDimensionAtomic(str);

		Dimension dims[] = new Dimension[factors.length];
		for (int i = 0; i < factors.length; ++i)
			dims[i] = parseDimensionAtomic(factors[i].trim());

		return new Dimension.Product(dims);
	}

	private final Dimension parseDimensionAtomic(String str) throws ParseException {
		try {
			int i = Integer.parseInt(str);
			if (i < 1) {
				throw new ParseException("dimension smaller than 1",parameters);
			}
			return new Dimension.Constant(i);
		}
		catch (NumberFormatException ex) {
			if ("?".equals(str))
				return new Dimension.Variable(ctx);
			for (char c : str.toCharArray())
				if (!Character.isLetterOrDigit(c))
					throw new ParseException("illegal variable name '"+str+"'",parameters);
			return new Dimension.Variable(str, ctx);
		}
	}

	private Dimension[] parseDimensions(int dimensionDelta) throws ParseException {
		skip();
		if (pos < parameters.length() && parameters.charAt(pos) == '[') {
			int endPos = parameters.indexOf(',', pos);
			if (endPos == -1) // until end of string...
				endPos = parameters.length();

			String dimStr = parameters.substring(pos,endPos);
			String array[] = dimStr.split("]");
			Dimension dims[] = new Dimension[array.length];
			for (int i = 0; i < array.length; ++i) {
				if (array[i].charAt(0) != '[')
					throw new ParseException("malformed parameter description in '" + dimStr + "'",parameters);
				dims[i] = parseDimension(array[i].substring(1).trim());
			}

			pos = endPos;

			if (dims.length < dimensionDelta)
				throw new ParseException("dimensions are missing, minimum " + dimensionDelta + " required",parameters);

			return dims;
		}
		else {
			if (dimensionDelta > 0)
				throw new ParseException("dimensions are missing, minimum " + dimensionDelta + " required",parameters);

			return null;
		}
	}

	private Type parseGenericParameter(char terminator) throws ParseException {
		Type type = parseType();
		char c = get();
		if (c != terminator)
			throw new ParseException("expected '"+terminator+"', got '"+c+"'",parameters);
		return type;
	}

	private Type parseType() throws ParseException {
		skip();
		Class<?> clazz = getClass(parseIdentifier(true));
		Type type;

		skip();
		if (get() == '<') {
			if (Map.class.isAssignableFrom(clazz)) {
				Type keyType = parseGenericParameter(',');
				Type valueType = parseGenericParameter('>');
				type = new MapType(clazz.asSubclass(Map.class), keyType, valueType);
			}
			else if (Collection.class.isAssignableFrom(clazz)) {
				Type valueType = parseGenericParameter('>');
				type = new CollectionType(clazz.asSubclass(Collection.class), valueType);
			}
				
			else
				throw new ParseException("only Collection and Map can have generic parameters",parameters);
		}
		else {
			--pos;
			type = Type.getType(clazz);
		}

		return parseTypeSuffix(type);
	}

	private Type parseTypeSuffix(Type originalType) throws ParseException {
		skip();
		char c = get();
		if (c == '~') {
			return parseTypeSuffix(new NullableType(originalType));
		}
		if (c == '[') {
			skip();
			if (get() == ']')
				return parseTypeSuffix(new ArrayType(originalType));
			else
				throw new ParseException("expected ']' after '['",parameters);
		}

		--pos;
		return originalType;
	}

	@SuppressWarnings("empty-statement")
	private String parseIdentifier(boolean allowDot) throws ParseException {
		skip();

		int start = pos;
		if (!Character.isJavaIdentifierStart(get()))
			throw new ParseException("invalid identifier start",parameters);

		while (pos < parameters.length() && isIdentifierPart(parameters.charAt(pos),allowDot))
			++pos;

		return parameters.substring(start,pos);
	}

	private boolean isIdentifierPart(char c,boolean allowDot) {
		return (allowDot && c == '.') || Character.isJavaIdentifierPart(c);
	}

	private Class<?> findClass(String str) throws ParseException {
		for (String prefix : imports) {
			try {
				return ClassLoader.getClass(prefix+str);
			}
			catch (ClassNotFoundException ex) {
				// do nothing
			}
		}

		throw new ParseException("class '"+str+"' not found",parameters);
	}

	private Class<?> getClass(String str) throws ParseException {
		if (str.equals("byte"))
			return Byte.class;
		if (str.equals("short"))
			return Short.class;
		if (str.equals("int"))
			return Integer.class;
		if (str.equals("long"))
			return Long.class;
		if (str.equals("float"))
			return Float.class;
		if (str.equals("double"))
			return Double.class;
		if (str.equals("boolean"))
			return Boolean.class;

		return findClass(str);
	}

}
