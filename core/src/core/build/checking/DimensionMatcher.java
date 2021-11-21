
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

import core.exception.MatchingException;
import core.signal.Signal;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class DimensionMatcher {

	private final Map<String,Dimension[]> dimensions;

	public DimensionMatcher(Map<String, Dimension[]> dimensions) {
		this.dimensions = dimensions;
	}

	private static final boolean processMatch(Map<Dimension,Integer> lengths) throws MatchingException {
		int itemsDone;

		do {
			itemsDone = 0;
			for (Iterator<Map.Entry<Dimension,Integer>> iter = lengths.entrySet().iterator();
				 iter.hasNext();) {
				Map.Entry<Dimension,Integer> entry = iter.next();

				int length = entry.getValue();
				Dimension dim = entry.getKey();

				assert length >= 0 : "illegal length "+length;
				
				if (!dim.acceptsValue(length)) {
					int fixedValue = dim.getFixedValue();
					String s = (fixedValue == -1) ? "" : " , previously fixed with "+fixedValue;
					throw new MatchingException("dimension "+dim+" doesn't accept length "+length+s);
				}
				if (dim.fixValue(length)) {
					++itemsDone;
					iter.remove();
				}
			}
		}
		while (itemsDone > 0);

		return lengths.size() == 0;
	}

	public boolean matchLengths(Map<String,List<Integer>> parameterLengths) throws MatchingException {
		Map<Dimension,Integer> workingMap = new LinkedHashMap<Dimension,Integer>();
		boolean complete = true;

		for (Map.Entry<String,Dimension[]> entry : dimensions.entrySet()) {
			if (!parameterLengths.containsKey(entry.getKey())) {
				complete = false;
				continue;
			}
			List<Integer> lengths = parameterLengths.get(entry.getKey());
			Dimension dims[] = entry.getValue();
			if (lengths.size() != dims.length)
				throw new MatchingException("expected "+dims.length+" dimensions, but got "+lengths.size());
			int i = 0;
			for (Integer length : lengths) {
				if (length == null)
					complete = false;
				else
					workingMap.put(dims[i], length);
				++i;
			}
		}

		return processMatch(workingMap) && complete; // evaluate function first, because exception could occur
	}

	public boolean matchParameters(Map<String,Object> parameters) throws MatchingException {
		// yes, this is redundant to matchLengths ;)

		Map<Dimension,Integer> workingMap = new LinkedHashMap<Dimension,Integer>();
		boolean complete = true;

		for (Map.Entry<String,Dimension[]> entry : dimensions.entrySet()) {
			if (!parameters.containsKey(entry.getKey())) {
				complete = false;
				continue;
			}
			List<Integer> lengths = getDimensions(parameters.get(entry.getKey()));
			Dimension dims[] = entry.getValue();
			if (lengths.size() != dims.length)
				throw new MatchingException("expected "+dims.length+" dimensions, but got "+lengths.size());
			int i = 0;
			for (Integer length : lengths) {
				if (length == null)
					complete = false;
				else
					workingMap.put(dims[i], length);
				++i;
			}
		}

		return processMatch(workingMap) && complete; // evaluate function first, because exception could occur
	}

	public static final List<Integer> getDimensions(Object value) throws MatchingException {
		List<Object> toExamine = Arrays.asList(value);
		List<Integer> dimensions = new LinkedList<Integer>();

		while (toExamine != null && toExamine.size() > 0) {
			int commonLength = -1;
			List<Object> children = new LinkedList<Object>();
			for (Object o : toExamine) {
				int length = getDimension(o);
				if (commonLength == -1)
					commonLength = length;
				else if (length != commonLength)
					throw new MatchingException("jagged arrays are not allowed: expected length "+commonLength+" and got "+length);

				// here, length == commonLength is true

				if (o instanceof Object[])
					children.addAll(Arrays.asList((Object[])o));
				else if (o instanceof Collection)
					children.addAll((Collection<?>)o);
			}
			if (commonLength != 0) {
				dimensions.add(commonLength);
				toExamine = children;
			}
			else
				toExamine = null;
		}

		return dimensions;
	}

	public static final int getDimension(Object value) {
		if (value instanceof Signal)
			return ((Signal)value).size();
		else if (value instanceof Collection)
			return ((Collection)value).size();
		else if (value instanceof Map)
			return ((Map)value).size();
		else if (value instanceof Object[])
			return ((Object[])value).length;
		else
			return 0;
	}

}
