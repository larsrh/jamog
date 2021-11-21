
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

import core.build.checking.types.CollectionType;
import core.build.checking.Expression.Add;
import core.build.checking.Expression.And;
import core.build.checking.Expression.Call;
import core.build.checking.Expression.Child;
import core.build.checking.Expression.Constant;
import core.build.checking.Expression.Div;
import core.build.checking.Expression.Equal;
import core.build.checking.Expression.GreaterOrEqual;
import core.build.checking.Expression.GreaterThan;
import core.build.checking.Expression.Identifier;
import core.build.checking.Expression.LessOrEqual;
import core.build.checking.Expression.LessThan;
import core.build.checking.Expression.Mod;
import core.build.checking.Expression.Mul;
import core.build.checking.Expression.Neg;
import core.build.checking.Expression.Not;
import core.build.checking.Expression.NotEqual;
import core.build.checking.Expression.Or;
import core.build.checking.Expression.Pow;
import core.build.checking.Expression.Size;
import core.build.checking.Expression.Sub;
import core.build.checking.types.Type;
import core.exception.ParseException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;
import static core.build.checking.types.Type.*;

/**
 * Translates an expression given as string into a {@link Expression}
 * which can be easily computed. This class supports typing
 * via our own {@link Type type system}.
 * @author lars
 */
public final class ExpressionParser {

	/**
	 * Denotes the bracket type.
	 */
	private static enum Bracket {

		/**
		 * No bracket.
		 */
		NONE,

		/**
		 * Round brackets ('(' and ')')
		 */
		ROUND,

		/**
		 * Squared brackets ('[' and ']')
		 */
		SQUARED,

		/**
		 * Vector brackets ('{' and '}')
		 */
		VECTOR
	}

	/**
	 * Holds an {@link Expression} and its {@link Type}. Exists solely
	 * because we cannot return multiple values in a function.
	 */
	private static final class EvalPair {

		public final Expression<?> expression;
		public final Type type;

		public EvalPair(Expression<?> expression, Type type) {
			this.expression = expression;
			this.type = type;
		}

	}

	private final String expression;
	private int pos;

	private ExpressionParser(String expression) {
		this.expression = expression;
		this.pos = 0;
	}

	public static Expression<?> parse(String expression) throws ParseException {
		return new ExpressionParser(expression).parse();
	}

	public static Expression<?> parse(String expression,Type resultType) throws ParseException {
		return new ExpressionParser(expression).parse(resultType);
	}

	@SuppressWarnings("unchecked")
	public static <T> Expression<T> parse(String expression,Class<T> resultClass) throws ParseException {
		return (Expression<T>) parse(expression, getType(resultClass));
	}

	private final Expression<?> parse() throws ParseException {
		return innerEvaluate(Bracket.NONE, false).expression;
	}

	private final Expression<?> parse(Type resultType) throws ParseException {
		EvalPair pair = innerEvaluate(Bracket.NONE, false);
		if (resultType.canAssign(pair.type))
			return pair.expression;
		else
			throw new ParseException("type mismatch: could not match desired type with actual type",expression);
	}

	private boolean processOperator(String s, Stack<EvalPair> stack, Stack<String> operators, boolean unary) throws ParseException {
		int priority = getPriority(s,unary);
		if (priority != -1) {
			while (!operators.empty() && getPriority(operators.peek(),stack.get(stack.size()-2) == null) > priority) {
				String op = operators.pop();
				EvalPair second = stack.pop();
				stack.push(calculate(op, stack.pop(), second));
			}
			operators.push(s);
			return true;
		}
		return false;
	}

	private boolean processOperator(Stack<EvalPair> stack, Stack<String> operators) throws ParseException {
		String op = parseOperator();
		return op != null && processOperator(op, stack, operators, false);
	}

	private final EvalPair innerEvaluate(Bracket bracket, boolean forceList) throws ParseException {
		Stack<EvalPair> stack = new Stack<EvalPair>();
		Stack<String> operators = new Stack<String>();

		List<EvalPair> csv = new LinkedList<EvalPair>();

		boolean acceptUnary = true;
		boolean callFunction = false;
		boolean arrayAccess = false;

		while (pos < expression.length()) {
			char c = expression.charAt(pos);

			// white space
			if (Character.isWhitespace(c)) {
				++pos;
				continue;
			}

			// number
			if (isNumber() || c == '.') {
				stack.push(new EvalPair(new Constant<BigDecimal>(parseDecimal()),BIG_DECIMAL));
				acceptUnary = false;
				callFunction = false;
				arrayAccess = false;
				continue;
			}

			// open bracket
			if (c == '(') {
				++pos;
				if (callFunction) {
					EvalPair result = innerEvaluate(Bracket.ROUND, true);
					processOperator("~", stack, operators, false);
					stack.push(result);
				}
				else {
					stack.push(innerEvaluate(Bracket.ROUND, false));
				}
				acceptUnary = false;
				callFunction = false;
				arrayAccess = true;
				continue;
			}

			if (arrayAccess && c == '[') {
				++pos;
				EvalPair result = innerEvaluate(Bracket.SQUARED, false);
				processOperator("A", stack, operators, false);
				stack.push(result);
				acceptUnary = false;
				callFunction = false;
				continue;
			}

			if (c == '{') {
				++pos;
				stack.push(innerEvaluate(Bracket.VECTOR, true));
				acceptUnary = false;
				arrayAccess = true;
				callFunction = false;
				continue;
			}

			// unary operators, pt. 1
			if (acceptUnary && c == '[') {
				StringBuilder sb = new StringBuilder("[");
				int count = 1;
				while (expression.charAt(++pos) == '[') {
					++count;
					sb.append('[');
				}
				if (expression.charAt(pos) != '#')
					throw new ParseException("token error: expected # after ['s",expression);
				int closeCount = 0;
				while (expression.charAt(++pos) == ']')
					++closeCount;
				if (closeCount != count)
					throw new ParseException("token error: expected that number of ]'s is equal to number of ['s",expression);

				stack.push(null);
				processOperator(sb.toString(), stack, operators, true);

				callFunction = false;
				arrayAccess = false;
				continue;
			}

			// unary operators, pt. 2
			if (acceptUnary && (c == '+' || c == '-' || c == '#' || c == '!')) {
				stack.push(null);
				processOperator(String.valueOf(c), stack, operators, true);
				
				++pos;
				callFunction = false;
				arrayAccess = false;
				continue;
			}

			// operator
			if (c != '~' && processOperator(stack, operators)) {
				acceptUnary = true;
				callFunction = false;
				arrayAccess = false;
				continue;
			}

			// identifier
			if (isIdentifier()) {
				String s = parseIdentifier();
				stack.push(new EvalPair(new Identifier(s),OBJECT));
				callFunction = true;
				arrayAccess = true;
				acceptUnary = false;
				continue;
			}

			// next value
			if (c == ',') {
				++pos;
				while (stack.size() > 1) {
					EvalPair second = stack.pop();
					stack.push(calculate(operators.pop(), stack.pop(), second));
				}
				callFunction = false;
				acceptUnary = true;
				arrayAccess = false;
				csv.add(stack.pop());
				continue;
			}

			// closing bracket
			if (c == ')' && bracket == Bracket.ROUND) {
				++pos;
				break;
			}

			if (c == ']' && bracket == Bracket.SQUARED) {
				++pos;
				break;
			}

			if (c == '}' && bracket == Bracket.VECTOR) {
				++pos;
				break;
			}

			// parse error
			throw new ParseException("unrecognized character: "+c,expression);

		}

		// eol
		while (stack.size() > 1) {
			EvalPair second = stack.pop();
			stack.push(calculate(operators.pop(), stack.pop(), second));
		}
		if (stack.size() >= 1)
			csv.add(stack.pop());

		if (csv.size() == 1 && !forceList)
			return csv.get(0);
		else {
			if (csv.size() > 0) {
				List<Expression<?>> list = new ArrayList<Expression<?>>(csv.size());
				Type superType = csv.get(0).type;
				for (EvalPair ep : csv) {
					list.add(ep.expression);
					if (ep.type.canAssign(superType))
						superType = ep.type;
					else if (!superType.canAssign(ep.type))
						superType = OBJECT; // maybe this could be smarter -- future version
				}

				return new EvalPair(new Expression.ListEval<Object>(list),new CollectionType(List.class,superType));
			}
			else {
				return new EvalPair(new Constant<List>(Collections.emptyList()), LIST);
			}
		}
	}

	private EvalPair getEvalPair(Expression<?> result, Type resultType,EvalPair first, EvalPair second, Type firstType, Type secondType) throws ParseException {
		if (secondType.canAssign(second.type) && (first == null || firstType.canAssign(first.type)))
			return new EvalPair(result, resultType);
		else
			throw new ParseException("type mismatch",expression);
	}

	private EvalPair getDecimalEvalPair(Expression<?> result, EvalPair first, EvalPair second) throws ParseException {
		return getEvalPair(result, BIG_DECIMAL, first, second, BIG_DECIMAL, BIG_DECIMAL);
	}

	private EvalPair getCmpEvalPair(Expression<?> result, EvalPair first, EvalPair second) throws ParseException {
		return getEvalPair(result, BOOLEAN, first, second, BIG_DECIMAL, BIG_DECIMAL);
	}

	private EvalPair getBoolEvalPair(Expression<?> result, EvalPair first, EvalPair second) throws ParseException {
		return getEvalPair(result, BOOLEAN, first, second, BOOLEAN, BOOLEAN);
	}

	@SuppressWarnings("unchecked")
	private EvalPair calculate(String s, EvalPair first, EvalPair second) throws ParseException {
		if (s.equals("+"))
			if (first == null)
				return second; // unary plus does nothing
			else
				return getDecimalEvalPair(new Add((Expression<BigDecimal>)first.expression,(Expression<BigDecimal>)second.expression), first, second);

		if (s.equals("-"))
			if (first == null)
				return getDecimalEvalPair(new Neg((Expression<BigDecimal>)second.expression), first, second);
			else
				return getDecimalEvalPair(new Sub((Expression<BigDecimal>)first.expression,(Expression<BigDecimal>)second.expression), first, second);

		if (s.equals("*"))
			return getDecimalEvalPair(new Mul((Expression<BigDecimal>)first.expression,(Expression<BigDecimal>)second.expression), first, second);
		if (s.equals("/"))
			return getDecimalEvalPair(new Div((Expression<BigDecimal>)first.expression,(Expression<BigDecimal>)second.expression), first, second);
		if (s.equals("%"))
			return getDecimalEvalPair(new Mod((Expression<BigDecimal>)first.expression,(Expression<BigDecimal>)second.expression), first, second);
		if (s.equals("^"))
			return getDecimalEvalPair(new Pow((Expression<BigDecimal>)first.expression,(Expression<BigDecimal>)second.expression), first, second);

		if (s.equals("<"))
			return getCmpEvalPair(new LessThan((Expression<BigDecimal>)first.expression,(Expression<BigDecimal>)second.expression), first, second);
		if (s.equals("<="))
			return getCmpEvalPair(new LessOrEqual((Expression<BigDecimal>)first.expression,(Expression<BigDecimal>)second.expression), first, second);
		if (s.equals(">"))
			return getCmpEvalPair(new GreaterThan((Expression<BigDecimal>)first.expression,(Expression<BigDecimal>)second.expression), first, second);
		if (s.equals(">="))
			return getCmpEvalPair(new GreaterOrEqual((Expression<BigDecimal>)first.expression,(Expression<BigDecimal>)second.expression), first, second);

		if (s.equals("=="))
			return getCmpEvalPair(new Equal((Expression<BigDecimal>)first.expression,(Expression<BigDecimal>)second.expression), first, second);
		if (s.equals("!="))
			return getCmpEvalPair(new NotEqual((Expression<BigDecimal>)first.expression,(Expression<BigDecimal>)second.expression), first, second);

		if (s.equals("!"))
			return getBoolEvalPair(new Not((Expression<Boolean>)second.expression), first, second);

		if (s.equals("&&"))
			return getBoolEvalPair(new And((Expression<Boolean>)first.expression,(Expression<Boolean>)second.expression), first, second);
		if (s.equals("||"))
			return getBoolEvalPair(new Or((Expression<Boolean>)first.expression,(Expression<Boolean>)second.expression), first, second);

		if (s.equals("~"))
			return getEvalPair(new Call((Expression<Object>)first.expression,(Expression<List<Object>>)second.expression), getType(Object.class), first, second, getType(Object.class), getType(List.class));

		// array access
		if (s.equals("A"))
			return getEvalPair(new Child((Expression<Object>)first.expression, (Expression<BigDecimal>)second.expression), getType(Object.class), first, second, getType(Object.class), getType(BigDecimal.class));

		// children-size-operator
		if (s.startsWith("["))
			return new EvalPair(new Size((Expression<Object>)second.expression,s.length()), BIG_DECIMAL);

		// nothing to ensure
		if (s.equals("#"))
			return new EvalPair(new Size((Expression<Object>)second.expression), BIG_DECIMAL);

		return null;
	}

	private static int getPriority(String s, boolean unary) {
		if (unary) {
			if (s.equals("+") || s.equals("-") || s.equals("#") || s.startsWith("["))
				return 50;
			
			return -1;
		}
		else {
			Trie<Integer> trie = binaryOperators.get(s);
			if (trie != null)
				return trie.getValue();
			else
				return -1;
		}
	}

	private int toNumber() {
		char c = expression.charAt(pos);

		switch (c) {
			case '0': return 0;
			case '1': return 1;
			case '2': return 2;
			case '3': return 3;
			case '4': return 4;
			case '5': return 5;
			case '6': return 6;
			case '7': return 7;
			case '8': return 8;
			case '9': return 9;
			default:  return -1;
		}
	}

	private boolean isNumber() {
		return toNumber() != -1;
	}

	private boolean isIdentifier() {
		char c = expression.charAt(pos);
		return Character.isJavaIdentifierStart(c);
	}

	private String parseOperator() {
		Trie<Integer> trie = binaryOperators;
		StringBuilder op = new StringBuilder();
		while (trie.size() > 1 || trie.getValue() == null) {
			char c = expression.charAt(pos);
			trie = trie.get(c);
			if (trie == null)
				break;
			else {
				op.append(c);
				++pos;
			}
		}

		if (op.length() != 0)
			return op.toString();
		else
			return null;
	}

	private BigDecimal parseDecimal() {
		BigDecimal sum = BigDecimal.ZERO;
		for (;pos < expression.length() && isNumber();++pos)
			sum = (sum.multiply(BigDecimal.TEN)).add(BigDecimal.valueOf(toNumber()));
		if (pos >= expression.length() || expression.charAt(pos) != '.')
			return sum;
		++pos;
		int length = 0;
		BigInteger decimal = BigInteger.ZERO;
		for (;pos < expression.length() && isNumber();++pos,++length)
			decimal = (decimal.multiply(BigInteger.TEN)).add(BigInteger.valueOf(toNumber()));
		return sum.add(new BigDecimal(decimal, length));
	}

	private String parseIdentifier() {
		StringBuilder s = new StringBuilder();
		char c = expression.charAt(pos);
		s.append(c);
		++pos;
		while (pos < expression.length() && Character.isJavaIdentifierPart(c = expression.charAt(pos))) {
			s.append(c);
			++pos;
		}
		return s.toString();
	}

	private static final Trie<Integer> binaryOperators = new Trie<Integer>(null);

	static {
		binaryOperators.put("&&", -3);
		binaryOperators.put("||", -3);

		binaryOperators.put("<", -2);
		binaryOperators.put("<=", -2);
		binaryOperators.put(">", -2);
		binaryOperators.put(">=", -2);
		binaryOperators.put("==", -2);
		binaryOperators.put("!=", -2);

		binaryOperators.put("+", 0);
		binaryOperators.put("-", 0);
		binaryOperators.put("*", 1);
		binaryOperators.put("/", 1);
		binaryOperators.put("%", 1);
		binaryOperators.put("^", 2);

		binaryOperators.put("A", 98); // array access
		binaryOperators.put("~", 98); // function call
	}

}
