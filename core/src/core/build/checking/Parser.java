
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

import core.build.checking.types.Type;
import core.build.checking.Expression.Constant;
import core.exception.ParseException;
import core.signal.Signal;
import java.util.Map;

/**
 *
 * @author lars
 */
public final class Parser {

	private Parser() {
	}
	
	public static final Map<String, Type> parseSignals(Map<String, Dimension[]> dimensions, MatcherContext ctx, String str) throws ParseException {
		return ParameterParser.parseParameters(dimensions, ctx, str, Type.getType(Signal.class));
	}

	public static final Map<String, Type> parseParameters(Map<String, Dimension[]> dimensions, MatcherContext ctx, String str) throws ParseException {
		return ParameterParser.parseParameters(dimensions, ctx, str, null);
	}

	@SuppressWarnings("unchecked")
	public static final Expression<Boolean> parseConstraint(String str) throws ParseException {
		if (str.trim().length() == 0)
			return new Constant<Boolean>(true);
		else
			return (Expression<Boolean>)ExpressionParser.parse(str, Type.getType(Boolean.class));
	}

}
