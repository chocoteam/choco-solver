/**
 * This file is part of choco-parsers, https://github.com/chocoteam/choco-parsers
 *
 * Copyright (c) 2017-01-06T09:54:20Z, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 * See LICENSE file in the project root for full license information.
 */
package org.xcsp.parser;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class EvaluationManager {

	/**********************************************************************************************
	 * Static
	 *********************************************************************************************/

	private static final Map<String, Class<?>> classMap = new HashMap<>();

	private static final Map<String, Integer> arityMap = new HashMap<>();

	private static final Set<String> symmetricEvaluators = new HashSet<>(), associativeEvaluators = new HashSet<>();

	static {
		for (Class<?> cl : Stream.of(EvaluationManager.class.getDeclaredClasses())
				.filter(c -> Evaluator.class.isAssignableFrom(c) && !Modifier.isAbstract(c.getModifiers())).toArray(Class<?>[]::new)) {
			String evaluatorToken = cl.getSimpleName().substring(0, 1).toLowerCase()
					+ cl.getSimpleName().substring(1, cl.getSimpleName().lastIndexOf(Evaluator.class.getSimpleName()));
			classMap.put(evaluatorToken, cl);
			// System.out.println(evaluatorToken + " " + clazz);
			int arity = -1;
			try {
				if (TagArity0.class.isAssignableFrom(cl))
					arity = 0;
				if (TagArity1.class.isAssignableFrom(cl))
					arity = 1;
				if (TagArity2.class.isAssignableFrom(cl))
					arity = 2;
				if (TagArity3.class.isAssignableFrom(cl))
					arity = 3;
				if (TagArityX.class.isAssignableFrom(cl))
					arity = Integer.MAX_VALUE;
				if (TagSymmetric.class.isAssignableFrom(cl))
					symmetricEvaluators.add(evaluatorToken);
				if (TagAssociative.class.isAssignableFrom(cl))
					associativeEvaluators.add(evaluatorToken);
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(1);
			}
			XUtility.control(arity != -1, "Pb with arity");
			arityMap.put(evaluatorToken, arity);
		}
	}

	public static Class<?> getClassOf(String evaluatorToken) {
		return classMap.get(evaluatorToken);
	}

	public static int getArityOf(String tok) {
		Integer a = arityMap.get(tok);
		if (a != null)
			return a; // arity of a basic operator
		int pos = IntStream.range(0, tok.length()).filter(i -> !Character.isDigit(tok.charAt(i))).findFirst().orElse(tok.length()) - 1;
		// either a token that is not an operator (return -1) or an eXtended operator (necessarily starts with an integer)
		return pos == -1 || pos == tok.length() - 1 ? -1 : Integer.parseInt(tok.substring(0, pos + 1));
	}

	public static boolean isSymmetric(String evaluatorToken) {
		return symmetricEvaluators.contains(evaluatorToken);
	}

	public static boolean isAssociative(String evaluatorToken) {
		return associativeEvaluators.contains(evaluatorToken);
	}

	/**********************************************************************************************
	 * Tags
	 *********************************************************************************************/

	public interface TagBoolean {
	}

	public interface TagInteger {
	}

	public interface TagArithmetic extends TagInteger {
	}

	public interface TagLogical extends TagBoolean {
	}

	public interface TagRelational extends TagBoolean {
	}

	public interface TagSet {
	}

	public interface TagTerminal {
	}

	public interface TagSymmetric {
	}

	public interface TagAssociative {
	}

	public interface TagArity0 {
	}

	public interface TagArity1 {
	}

	public interface TagArity2 {
	}

	public interface TagArity3 {
	}

	public interface TagArityX {
	}

	/**********************************************************************************************
	 * Root class for evaluators
	 *********************************************************************************************/

	public abstract class Evaluator {

		public int arity = -1;

		public void fixArity() {
			XUtility.control(arity == -1 || this instanceof TagArityX, "Pb with arity");
			if (arity == -1)
				arity = this instanceof TagArity0 ? 0 : this instanceof TagArity1 ? 1 : this instanceof TagArity2 ? 2 : this instanceof TagArity3 ? 3 : -1;
		}

		public abstract void evaluate();

		public String toString() {
			return getClass().getSimpleName();
		}
	}

	/**********************************************************************************************
	 * Arithmetic Evaluators
	 *********************************************************************************************/

	public class NegEvaluator extends Evaluator implements TagArity1, TagArithmetic {
		public void evaluate() {
			stack[top] = -stack[top];
		}
	}

	public class AbsEvaluator extends Evaluator implements TagArity1, TagArithmetic {
		public void evaluate() {
			stack[top] = Math.abs(stack[top]);
		}
	}

	public class AddEvaluator extends Evaluator implements TagArity2, TagArithmetic, TagSymmetric, TagAssociative {
		public void evaluate() {
			top--;
			stack[top] = stack[top] + stack[top + 1];
		}
	}

	public class AddxEvaluator extends Evaluator implements TagArityX, TagArithmetic, TagSymmetric, TagAssociative {
		public void evaluate() {
			top -= arity - 1;
			long sum = stack[top];
			for (int i = 1; i < arity; i++)
				sum += stack[top + i];
			stack[top] = sum;
		}
	}

	public class SubEvaluator extends Evaluator implements TagArity2, TagArithmetic {
		public void evaluate() {
			top--;
			stack[top] = stack[top] - stack[top + 1];
		}
	}

	public class MulEvaluator extends Evaluator implements TagArity2, TagArithmetic, TagSymmetric, TagAssociative {
		public void evaluate() {
			top--;
			stack[top] = stack[top] * stack[top + 1];
		}
	}

	public class MulxEvaluator extends Evaluator implements TagArityX, TagArithmetic, TagSymmetric, TagAssociative {
		public void evaluate() {
			top -= arity - 1;
			long product = stack[top];
			for (int i = 1; i < arity; i++)
				product *= stack[top + i];
			stack[top] = product;
		}
	}

	public class DivEvaluator extends Evaluator implements TagArity2, TagArithmetic {
		public void evaluate() {
			top--;
			stack[top] = stack[top] / stack[top + 1];
		}
	}

	public class ModEvaluator extends Evaluator implements TagArity2, TagArithmetic {
		public void evaluate() {
			top--;
			stack[top] = stack[top] % stack[top + 1];
		}
	}

	public class SqrEvaluator extends Evaluator implements TagArity1, TagArithmetic {
		public void evaluate() {
			stack[top] = stack[top] * stack[top];
		}
	}

	public class PowEvaluator extends Evaluator implements TagArity2, TagArithmetic {
		public void evaluate() {
			top--;
			stack[top] = (long) Math.pow(stack[top], stack[top + 1]);
		}
	}

	public class MinEvaluator extends Evaluator implements TagArity2, TagArithmetic, TagSymmetric, TagAssociative {
		public void evaluate() {
			top--;
			stack[top] = Math.min(stack[top], stack[top + 1]);
		}
	}

	public class MinxEvaluator extends Evaluator implements TagArityX, TagArithmetic, TagSymmetric, TagAssociative {
		public void evaluate() {
			top -= arity - 1;
			long min = stack[top];
			for (int i = 1; i < arity; i++)
				min = Math.min(min, stack[top + i]);
			stack[top] = min;
		}
	}

	public class MaxEvaluator extends Evaluator implements TagArity2, TagArithmetic, TagSymmetric, TagAssociative {
		public void evaluate() {
			top--;
			stack[top] = Math.max(stack[top], stack[top + 1]);
		}
	}

	public class MaxxEvaluator extends Evaluator implements TagArityX, TagArithmetic, TagSymmetric, TagAssociative {
		public void evaluate() {
			top -= arity - 1;
			long max = stack[top];
			for (int i = 1; i < arity; i++)
				max = Math.max(max, stack[top + i]);
			stack[top] = max;
		}
	}

	public class DistEvaluator extends Evaluator implements TagArity2, TagArithmetic, TagSymmetric {
		public void evaluate() {
			top--;
			stack[top] = Math.abs(stack[top] - stack[top + 1]);
		}
	}

	public interface ExternFunctionArity1 {
		public long evaluate(long l);
	}

	public interface ExternFunctionArity2 {
		public long evaluate(long l1, long l2);
	}

	public class F1Evaluator extends Evaluator implements TagArity1, TagArithmetic {
		public ExternFunctionArity1 function;

		public void evaluate() {
			stack[top] = function.evaluate(stack[top]);
		}
	}

	public class F2Evaluator extends Evaluator implements TagArity2, TagArithmetic {
		public ExternFunctionArity2 function;

		public void evaluate() {
			top--;
			stack[top] = function.evaluate(stack[top], stack[top + 1]);
		}
	}

	/**********************************************************************************************
	 * Relational Evaluators
	 *********************************************************************************************/

	public class LtEvaluator extends Evaluator implements TagArity2, TagRelational {
		public void evaluate() {
			top--;
			stack[top] = (stack[top] < stack[top + 1] ? 1 : 0);
		}
	}

	public class LtxEvaluator extends Evaluator implements TagArityX, TagRelational, TagSymmetric, TagAssociative {
		public void evaluate() {
			top -= arity - 1;
			for (int i = 1; i < arity; i++)
				if (stack[top + i - 1] >= stack[top + i]) {
					stack[top] = 0;
					return;
				}
			stack[top] = 1;
		}
	}

	public class LeEvaluator extends Evaluator implements TagArity2, TagRelational {
		public void evaluate() {
			top--;
			stack[top] = (stack[top] <= stack[top + 1] ? 1 : 0);
		}
	}

	public class LexEvaluator extends Evaluator implements TagArityX, TagRelational, TagSymmetric, TagAssociative {
		public void evaluate() {
			top -= arity - 1;
			for (int i = 1; i < arity; i++)
				if (stack[top + i - 1] > stack[top + i]) {
					stack[top] = 0;
					return;
				}
			stack[top] = 1;
		}
	}

	public class GeEvaluator extends Evaluator implements TagArity2, TagRelational {
		public void evaluate() {
			top--;
			stack[top] = (stack[top] >= stack[top + 1] ? 1 : 0);
		}
	}

	public class GexEvaluator extends Evaluator implements TagArityX, TagRelational, TagSymmetric, TagAssociative {
		public void evaluate() {
			top -= arity - 1;
			for (int i = 1; i < arity; i++)
				if (stack[top + i - 1] < stack[top + i]) {
					stack[top] = 0;
					return;
				}
			stack[top] = 1;
		}
	}

	public class GtEvaluator extends Evaluator implements TagArity2, TagRelational {
		public void evaluate() {
			top--;
			stack[top] = (stack[top] > stack[top + 1] ? 1 : 0);
		}
	}

	public class GtxEvaluator extends Evaluator implements TagArityX, TagRelational, TagSymmetric, TagAssociative {
		public void evaluate() {
			top -= arity - 1;
			for (int i = 1; i < arity; i++)
				if (stack[top + i - 1] <= stack[top + i]) {
					stack[top] = 0;
					return;
				}
			stack[top] = 1;
		}
	}

	public class NeEvaluator extends Evaluator implements TagArity2, TagRelational, TagSymmetric, TagAssociative {
		public void evaluate() {
			top--;
			stack[top] = (stack[top] != stack[top + 1] ? 1 : 0);
		}
	}

	public class NexEvaluator extends Evaluator implements TagArityX, TagRelational, TagSymmetric, TagAssociative {
		public void evaluate() {
			top -= arity - 1;
			for (int i = arity - 1; i > 0; i--)
				for (int j = i - 1; j >= 0; j--)
					if (stack[top + i] == stack[top + j]) {
						stack[top] = 0;
						return;
					}
			stack[top] = 1;
		}
	}

	public class EqEvaluator extends Evaluator implements TagArity2, TagRelational, TagSymmetric, TagAssociative {
		public void evaluate() {
			top--;
			stack[top] = (stack[top] == stack[top + 1] ? 1 : 0);
		}
	}

	public class EqxEvaluator extends Evaluator implements TagArityX, TagRelational, TagSymmetric, TagAssociative {
		public void evaluate() {
			top -= arity - 1;
			long value = stack[top];
			for (int i = 1; i < arity; i++)
				if (stack[top + i] != value) {
					stack[top] = 0;
					return;
				}
			stack[top] = 1;
		}
	}

	/**********************************************************************************************
	 * Set Evaluators
	 *********************************************************************************************/

	public class SetxEvaluator extends Evaluator implements TagArityX, TagSet {
		public void evaluate() {
			stack[++top] = arity; // to be used by next operator in or notin
		}
	}

	public class InEvaluator extends Evaluator implements TagArity2, TagSet, TagBoolean {
		public void evaluate() {
			int arity = (int) stack[top--]; // comes from operator set
			top -= arity;
			long value = stack[top];
			for (int i = 1; i < arity + 1; i++)
				if (stack[top + i] == value) {
					stack[top] = 1;
					return;
				}

			stack[top] = 0;
		}
	}

	public class NotinEvaluator extends Evaluator implements TagArity2, TagSet, TagBoolean {
		public void evaluate() {
			int arity = (int) stack[top--]; // comes from operator set
			top -= arity;
			long value = stack[top];
			for (int i = 1; i < arity + 1; i++)
				if (stack[top + i] == value) {
					stack[top] = 0;
					return;
				}

			stack[top] = 1;
		}
	}

	/**********************************************************************************************
	 * Logical Evaluators
	 *********************************************************************************************/

	public class NotEvaluator extends Evaluator implements TagArity1 {
		public void evaluate() {
			stack[top] = 1 - stack[top]; // (stack[nbStackElements - 1] == 1 ? 0 : 1);
		}
	}

	public class AndEvaluator extends Evaluator implements TagArity2, TagLogical, TagSymmetric, TagAssociative {
		public void evaluate() {
			top--;
			stack[top] = Math.min(stack[top], stack[top + 1]);
		}
	}

	public class AndxEvaluator extends Evaluator implements TagArityX, TagLogical, TagSymmetric, TagAssociative {
		public void evaluate() {
			top -= arity - 1;
			for (int i = 0; i < arity; i++)
				if (stack[top + i] == 0) {
					stack[top] = 0;
					return;
				}
			stack[top] = 1;
		}
	}

	public class OrEvaluator extends Evaluator implements TagArity2, TagLogical, TagSymmetric, TagAssociative {
		public void evaluate() {
			top--;
			stack[top] = Math.max(stack[top], stack[top + 1]);
		}
	}

	public class OrxEvaluator extends Evaluator implements TagArityX, TagLogical, TagSymmetric, TagAssociative {
		public void evaluate() {
			top -= arity - 1;
			for (int i = 0; i < arity; i++)
				if (stack[top + i] == 1) {
					stack[top] = 1;
					return;
				}
			stack[top] = 0;
		}
	}

	public class XorEvaluator extends Evaluator implements TagArity2, TagLogical, TagSymmetric, TagAssociative {
		public void evaluate() {
			top--;
			stack[top] = (stack[top] + stack[top + 1] == 1 ? 1 : 0);
		}
	}

	public class XorxEvaluator extends Evaluator implements TagArityX, TagLogical, TagSymmetric, TagAssociative {
		public void evaluate() {
			top -= arity - 1;
			int cnt = 0;
			for (int i = 0; i < arity; i++)
				if (stack[top + i] == 1)
					cnt++;
			stack[top] = cnt % 2;
		}
	}

	public class IffEvaluator extends Evaluator implements TagArity2, TagLogical, TagSymmetric, TagAssociative {
		public void evaluate() {
			top--;
			stack[top] = (stack[top] == stack[top + 1] ? 1 : 0);
		}
	}

	public class IffxEvaluator extends Evaluator implements TagArityX, TagLogical, TagSymmetric, TagAssociative {
		public void evaluate() {
			top -= arity - 1;
			long value = stack[top];
			for (int i = 1; i < arity; i++)
				if (stack[top + i] != value) {
					stack[top] = 0;
					return;
				}
			stack[top] = 1;
		}
	}

	public class ImpEvaluator extends Evaluator implements TagArity2, TagLogical {
		public void evaluate() {
			top--;
			stack[top] = (stack[top] == 0 || stack[top + 1] == 1 ? 1 : 0);
		}
	}

	public class IfEvaluator extends Evaluator implements TagArity3, TagArithmetic {
		public void evaluate() {
			top -= 2;
			stack[top] = stack[top] == 1 ? stack[top + 1] : stack[top + 2];
			// if (stack[top+2] == 1)
			// stack[top]=stack[top+1];
		}
	}

	/**********************************************************************************************
	 * Terminal Evaluators
	 *********************************************************************************************/

	public class FalseEvaluator extends Evaluator implements TagArity0, TagTerminal, TagBoolean {
		public void evaluate() {
			stack[++top] = 0;
		}
	}

	public class TrueEvaluator extends Evaluator implements TagArity0, TagTerminal, TagBoolean {
		public void evaluate() {
			stack[++top] = 1;
		}
	}

	public class LongEvaluator extends Evaluator implements TagArity0, TagTerminal, TagInteger {

		public final long value;

		public LongEvaluator(long value) {
			this.value = value;
		}

		public void evaluate() {
			stack[++top] = value;
		}

		public String toString() {
			return super.toString() + "(" + value + ")";
		}

	}

	public class VariableEvaluator extends Evaluator implements TagArity0, TagTerminal, TagInteger {

		public final int position;

		public VariableEvaluator(int position) {
			this.position = position;
		}

		public void evaluate() {
			stack[++top] = values[position];
		}
	}

	/**********************************************************************************************
	 * The class
	 *********************************************************************************************/

	/** The sequence of evaluators (built from a post-fixed expression) that can be called for evaluating a tuple of values (instantiation). */
	public final Evaluator[] evaluators;

	/** The current top value for the stack. */
	private int top;

	/** The stack used for evaluating a tuple of values (instantiation). */
	private long[] stack;

	/**
	 * 1D = index of evaluator; <br>
	 * value = 1 means that if the result of the evaluator is 1 it can be returned immediately, <br>
	 * value = 0 means that if the result of the evaluator is 0 it can be returned immediately, <br>
	 * value = -1 means that we have to keep evaluating
	 */
	private int[] shortCircuits;

	/** This field is inserted in order to avoid having systematically a tuple of values as parameter of methods evaluate() in Evaluator classes. */
	private int[] values;

	private int[] tmp = new int[1];

	private Evaluator buildEvaluator(String tok, List<String> varNames) {
		try {
			if (tok.matches("^(-?)\\d+$"))
				return new LongEvaluator(Long.parseLong(tok));
			if (tok.startsWith("%"))
				return new VariableEvaluator(Integer.parseInt(tok.substring(1)));
			if (getClassOf(tok) != null)
				return (Evaluator) getClassOf(tok).getDeclaredConstructor(EvaluationManager.class).newInstance(EvaluationManager.this);
			int pos = IntStream.range(0, tok.length()).filter(i -> !Character.isDigit(tok.charAt(i))).findFirst().orElse(tok.length()) - 1;
			if (pos == -1) {
				int varPos = varNames.indexOf(tok);
				if (varPos == -1) {
					varPos = varNames.size();
					varNames.add(tok);
				}
				return new VariableEvaluator(varPos);
			}
			Evaluator evaluator = (Evaluator) getClassOf(tok.substring(pos + 1) + "x").getDeclaredConstructor(EvaluationManager.class).newInstance(
					EvaluationManager.this);
			evaluator.arity = Integer.parseInt(tok.substring(0, pos + 1));
			return evaluator;
		} catch (Exception e) {
			(e.getCause() == null ? e : e.getCause()).printStackTrace();
			return null;
		}
	}

	// Reste a faire pour IF
	private void dealWithShortCircuits() {
		boolean useShortCircuits = true; // TODO
		if (!useShortCircuits)
			return;
		shortCircuits = new int[evaluators.length];
		useShortCircuits = false;
		for (int i = 0; i < evaluators.length - 1; i++) {
			if (evaluators[i] instanceof TagInteger)
				continue;
			// from a Boolean evaluator, we may find a short circuit
			int j = i + 1;
			int nbStackedElements = 1;
			while (j < evaluators.length) {
				nbStackedElements += 1 - evaluators[j].arity;
				if (nbStackedElements <= 1)
					break;
				j++;
			}
			if (j == i + 1)
				continue;
			if (evaluators[j] instanceof OrEvaluator) {
				shortCircuits[i] = j + 1;
				useShortCircuits = true;
			} else if (evaluators[j] instanceof AndEvaluator) {
				shortCircuits[i] = -j - 1;
				useShortCircuits = true;
			}
		}
		if (!useShortCircuits)
			shortCircuits = null;
	}

	public EvaluationManager(String[] universalPostfixExpression) {
		// System.out.println(Kit.join(universalPostfixExpression));
		List<String> varNames = new ArrayList<>(); // necessary to collect variable names when building the evaluators
		this.evaluators = Stream.of(universalPostfixExpression).map(s -> buildEvaluator(s, varNames)).peek(e -> e.fixArity()).toArray(Evaluator[]::new); // buildEvaluatorsFrom(universalPostfixExpression);
		dealWithShortCircuits();
		top = -1;
		stack = new long[evaluators.length];
		assert evaluators.length > 0;

	}

	/** Evaluates the specified tuple of values, by using the recorded so-called evaluators. */
	public final long evaluate(int[] values) {
		this.values = values;
		top = -1;
		if (shortCircuits == null)
			for (Evaluator evaluator : evaluators)
				evaluator.evaluate();
		else
			for (int i = 0; i < evaluators.length;) { // i = shortCircuits[i] == 0 ? i + 1 : nextEvaluator(i)) {
				evaluators[i].evaluate();
				if (shortCircuits[i] == 0)
					i++;
				else if (shortCircuits[i] > 0)
					i = stack[top] == 1 ? shortCircuits[i] : i + 1;
				else
					i = stack[top] == 0 ? -shortCircuits[i] : i + 1;
			}
		assert top == 0 : "" + top;
		return stack[top]; // == 1; // 1 means true while 0 means false
	}

	/** Evaluates the value, by using the recorded so-called evaluators. */
	public final long evaluate(int value) {
		tmp[0] = value;
		return evaluate(tmp);
	}

	public boolean controlArityOfEvaluators() {
		return Stream.of(evaluators).mapToInt(e -> 1 - e.arity).sum() == 1;
	}

	public boolean controlTypeOfEvaluators(boolean booleanType) {
		if (booleanType && !(evaluators[evaluators.length - 1] instanceof TagBoolean))
			return false;
		if (!booleanType && !(evaluators[evaluators.length - 1] instanceof TagInteger))
			return false;
		boolean[] booleanTypes = new boolean[evaluators.length];
		int top = -1;
		for (Evaluator evaluator : evaluators) {
			if (evaluator instanceof TagArithmetic) {
				if (evaluator instanceof IfEvaluator) {
					if (!booleanTypes[top] || booleanTypes[top - 1] || booleanTypes[top - 2])
						return false;
					top -= 3;
				} else {
					for (int j = 0; j < evaluator.arity; j++) {
						if (booleanTypes[top])
							return false;
						top--;
					}
				}
			} else if (evaluator instanceof TagLogical) {
				for (int j = 0; j < evaluator.arity; j++) {
					if (!booleanTypes[top])
						return false;
					top--;
				}
			} else if (evaluator instanceof TagRelational) {
				for (int j = 0; j < evaluator.arity; j++) {
					if (booleanTypes[top])
						return false;
					top--;
				}
			}
			booleanTypes[++top] = (evaluator instanceof TagBoolean);
		}
		return true;
	}
}