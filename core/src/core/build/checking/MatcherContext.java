
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

import java.math.BigInteger;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author lars
 */
public final class MatcherContext {

	final Map<String,Integer> lengths;
	final Map<Dimension,Integer> branches;
	BigInteger whateverCount;

	public MatcherContext() {
		this.lengths = new HashMap<String, Integer>();
		this.branches = new HashMap<Dimension, Integer>();
		this.whateverCount = BigInteger.ZERO;
	}

	public Map<String, Integer> getLengths() {
		return Collections.unmodifiableMap(lengths);
	}

	public void reset() {
		lengths.clear();
		branches.clear();
	}

	@Override
	public String toString() {
		return lengths.toString();
	}

}
