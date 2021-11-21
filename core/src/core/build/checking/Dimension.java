
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

/**
 * Represents a length, size or width of a collection-style object. An
 * implementing class determines which concrete values are acceptable,
 * e. g. a {@link Range} or just a {@link Constant}.
 * @author lars
 */
public interface Dimension
{

	/**
	 * Tries to fix the size of this dimension. Can only succeed if
	 * {@link #acceptsValue(int) acceptsValue} returns {@code true} with
	 * the same size.
	 * If this method returned {@code true}, any subsequent call must return
	 * {@code true} if and only if the parameter is the same.
	 * @param length the desired length
	 * @return {@code true} if fixing was possible or if this dimension has
	 * been fixed before with the same length, or {@code false} if the length
	 * is not acceptable or fixing is undecidable (at the moment).
	 */
	boolean fixValue(int length);

	/**
	 * Returns the fixed value, if any. It is ensured that if {@link #fixValue(int) }
	 * returned {@code true}, any subsequent calls to this method will return
	 * the parameter passed to the fixing call.
	 * @return 0 if no value has been fixed (neither explicitly nor implicitly).
	 * A value > 0 will be returned if a value has been fixed.
	 * Implicit fixing is implementation dependent (e. g. {@link Constant}).
	 * @see #fixValue(int)
	 */
	int getFixedValue();

	/**
	 * Tries to figure out whether the given length is acceptable as
	 * size for this dimension. Note that a positive result doesn't
	 * imply that {@link #fixValue(int) fixing} the same length will
	 * succeed.
	 * @param length the length to be tested
	 * @return {@code false} if this length is definitely not acceptable,
	 * or {@code true} if it's probably acceptable.
	 */
	boolean acceptsValue(int length);

	/**
	 * This class can be used as a wrapper for an existing {@link Dimension}
	 * object. If the underlying object is already fixed and the fixed
	 * value is appropriate for this {@code Range}, this object is also fixed.
	 * If the value is not appropriate, fixing on this object will not
	 * succeed, even if the underlying object is fixed.
	 */
	public static final class Range implements Dimension {

		private final Dimension dim;
		private final int min;
		private final int max;

		/**
		 * Creates a new {@link Range} object.
		 * @param dim underlying {@link Dimension}, where the range should be checked
		 * @param min the minimum value, has to be greater than zero and smaller
		 * than or equal to {@code max}
		 * @param max the maximum value, hast to be greater than or equal to {@code min}
		 */
		public Range(Dimension dim, int min, int max) {
			assert !(min > max || min < 1 || max < 1);
			
			this.dim = dim;
			this.min = min;
			this.max = max;
		}

		@Override
		public boolean fixValue(int length) {
			if (length < min || length > max)
				return false;
			return dim.fixValue(length);
		}

		@Override
		public int getFixedValue() {
			int fixedValue = dim.getFixedValue();
			if (fixedValue < min || fixedValue > max)
				return -1;
			else
				return fixedValue;
		}

		@Override
		public boolean acceptsValue(int length) {
			return length >= min && length <= max && dim.acceptsValue(length);
		}

		@Override
		public String toString() {
			return "["+min+","+( (max == Integer.MAX_VALUE) ? "inf" : String.valueOf(max) )+"]";
		}

	}

	/**
	 * Represents a constant length and is already fixed.
	 */
	public static final class Constant implements Dimension {

		private final int length;

		/**
		 * Creates a new {@link Constant} object.
		 * @param length the only value this object will accept
		 */
		public Constant(int length) {
			assert (length >= 1);
				
			this.length = length;
		}

		@Override
		public boolean fixValue(int length) {
			return length == this.length;
		}

		@Override
		public int getFixedValue() {
			return length;
		}

		@Override
		public boolean acceptsValue(int length) {
			return length == this.length;
		}

		@Override
		public String toString() {
			return String.valueOf(length);
		}

	}

	/**
	 * Represents a variable and depends on a context. If and only if the
	 * variable is set in the context, it is fixed here.
	 */
	public static final class Variable implements Dimension {

		private final String name;
		private final MatcherContext context;

		/**
		 * Creates a new {@link Variable} object and binds it to a given
		 * {@link MatcherContext}.
		 * @param name the name of the {@link Variable}, must be unique per context
		 * @param context the {@link MatcherContext}, to which this object is bound
		 */
		public Variable(String name, MatcherContext context) {
			this.name = name;
			this.context = context;
		}

		/**
		 * Creates a new {@link Variable} object with a default name
		 * and binds it to a given {@link MatcherContext}.
		 * @param context the {@link MatcherContext}, to which this object is bound
		 */
		public Variable(MatcherContext context) {
			this.name = "_"+context.whateverCount;
			context.whateverCount = context.whateverCount.add(BigInteger.ONE);
			this.context = context;
		}

		@Override
		public boolean fixValue(int length) {
			if (context.lengths.containsKey(name))
				return length == context.lengths.get(name);
			else if (length >= 1) {
				context.lengths.put(name, length);
				return true;
			}
			else
				return false;
		}

		@Override
		public int getFixedValue() {
			if (context.lengths.containsKey(name))
				return context.lengths.get(name);
			else
				return 0;
		}

		@Override
		public boolean acceptsValue(int length) {
			if (context.lengths.containsKey(name))
				return length == context.lengths.get(name);
			else
				return length >= 1;
		}

		@Override
		public String toString() {
			return name;
		}

	}

	/**
	 * Represents a product of multiple dimension sizes. When only one
	 * factor is not fixed, this one will be calculated from the product,
	 * otherwise, ambiguity could not be prevented.
	 */
	public static final class Product implements Dimension {

		private final Dimension factors[];

		/**
		 * Creates a new {@link Product} object.
		 * @param factors all factors which compose this product, minimum 2
		 */
		public Product(Dimension... factors) {
			assert !(factors == null || factors.length < 2);
			
			this.factors = new Dimension[factors.length];
			System.arraycopy(factors, 0, this.factors, 0, factors.length);
		}

		@Override
		public boolean fixValue(int length) {
			Dimension toFix = null;
			int product = 1;
			for (Dimension d : factors) {
				int factor = d.getFixedValue();
				if (factor == 0)
					if (toFix == null)
						toFix = d;
					else
						return false;
				else
					product *= factor;
			}

			if (toFix == null)
				return length == product;
			if (length % product != 0)
				return false;

			int quot = length / product;

			if (!toFix.acceptsValue(quot))
				return false;
			return toFix.fixValue(quot);
		}

		@Override
		public int getFixedValue() {
			int product = 1;
			for (Dimension d : factors) {
				int factor = d.getFixedValue();
				if (factor > 0)
					product *= factor;
				else
					return 0;
			}
			return product;
		}

		@Override
		public boolean acceptsValue(int length) {
			int product = 1;
			boolean checkAccept = true;
			Dimension toAccept = null;
			for (Dimension d : factors) {
				int factor = d.getFixedValue();
				if (factor == 0) {
					if (toAccept == null)
						toAccept = d;
					else if (checkAccept)
						checkAccept = false;
				}
				else
					product *= factor;
			}
			if (checkAccept && toAccept == null)
				return length == product;
			else if (checkAccept)
				return length % product == 0 && toAccept.acceptsValue(length/product);
			else
				return length % product == 0;
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder("(");
			for (Dimension d : factors) {
				sb.append(d);
				sb.append("*");
			}
			sb.replace(sb.length()-1,sb.length(),")");
			return sb.toString();
		}

	}

	/**
	 * Represents a sum of multiple dimension sizes. When only one
	 * summand is not fixed, this one will be calculated from the sum,
	 * otherwise, ambiguity could not be prevented.
	 */
	public static final class Sum implements Dimension {

		private final Dimension summands[];

		/**
		 * Creates a new {@link Sum} object.
		 * @param summands all summands which compose this sum, minimum 2
		 */
		public Sum(Dimension... summands) {
			assert !(summands == null || summands.length < 2);
			
			this.summands = new Dimension[summands.length];
			System.arraycopy(summands, 0, this.summands, 0, summands.length);
		}

		@Override
		public boolean fixValue(int length) {
			Dimension toFix = null;
			int sum = 0;
			for (Dimension d : summands) {
				int add = d.getFixedValue();
				if (add == 0)
					if (toFix == null)
						toFix = d;
					else
						return false;
				else
					sum += add;
			}

			int diff = length-sum;

			if (toFix == null)
				return diff == 0;
			if (diff < 1 || !toFix.acceptsValue(diff))
				return false;
			return toFix.fixValue(diff);
		}

		@Override
		public int getFixedValue() {
			int sum = 0;
			for (Dimension d : summands) {
				int add = d.getFixedValue();
				if (add > 0)
					sum += add;
				else
					return 0;
			}
			return sum;
		}

		@Override
		public boolean acceptsValue(int length) {
			int sum = 0;
			boolean checkAccept = true;
			Dimension toAccept = null;
			for (Dimension d : summands) {
				int add = d.getFixedValue();
				if (add == 0) {
					if (toAccept == null)
						toAccept = d;
					else if (checkAccept)
						checkAccept = false;
					++sum; // every dimension has to be >= 1
				}
				else
					sum += add;
			}
			if (checkAccept && toAccept == null)
				return length == sum;
			else if (checkAccept)
				return toAccept.acceptsValue(length-sum+1); // ++'ed sum earlier
			else
				return length >= sum;
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder("(");
			for (Dimension d : summands) {
				sb.append(d);
				sb.append("+");
			}
			sb.replace(sb.length()-1,sb.length(),")");
			return sb.toString();
		}

	}

	/**
	 * Represents a {@link Dimension} which accepts multiple values.
	 */
	public static final class Option implements Dimension {

		private final MatcherContext context;
		private final Dimension branches[];

		/**
		 * Creates a new {@link Option} object and binds it to a given {@link MatcherContext}.
		 * @param context the {@link MatcherContext}, to which this object is bound
		 * @param branches multiple {@link Dimension} objects to which fixing
		 * is delegated
		 */
		public Option(MatcherContext context,Dimension... branches) {
			assert !(branches == null || branches.length < 2);
			
			this.branches = new Dimension[branches.length];
			this.context = context;
			System.arraycopy(branches, 0, this.branches, 0, branches.length);
		}

		private final int getFixedBranch() {
			if (context.branches.containsKey(this))
				return context.branches.get(this);
			else
				return -1;
		}

		@Override
		public boolean fixValue(int length) {
			if (getFixedBranch() != -1)
				return branches[getFixedBranch()].fixValue(length);

			for (int i=0;i<branches.length;++i)
				if (branches[i].acceptsValue(length) && branches[i].fixValue(length)) {
					context.branches.put(this, i);
					return true;
				}

			return false;
		}

		@Override
		public int getFixedValue() {
			if (getFixedBranch() != -1)
				return branches[getFixedBranch()].getFixedValue();
			else
				return 0;
		}

		@Override
		public boolean acceptsValue(int length) {
			if (getFixedBranch() != -1)
				return branches[getFixedBranch()].acceptsValue(length);

			for (Dimension d : branches)
				if (d.acceptsValue(length))
					return true;
			return false;
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder("(");
			for (Dimension d : branches) {
				sb.append(d);
				sb.append("|");
			}
			sb.replace(sb.length()-1,sb.length(),")");
			return sb.toString();
		}

	}

}
