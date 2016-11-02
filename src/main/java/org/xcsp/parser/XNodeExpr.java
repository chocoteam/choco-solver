/**
 * Copyright (c) 2014, chocoteam
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * * Neither the name of the {organization} nor the names of its
 *   contributors may be used to endorse or promote products derived from
 *   this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.xcsp.parser;

import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.xcsp.parser.XEnums.TypeArithmeticOperator;
import org.xcsp.parser.XEnums.TypeExpr;
import org.xcsp.parser.XValues.Decimal;
import org.xcsp.parser.XVariables.XVar;
import org.xcsp.parser.XVariables.XVarInteger;

/**
 * The class used for representing a node of a syntactic tree (built for functional expressions, and used especially with <intension>). Also includes, as intern
 * classes, subclasses for managing parent and leaf nodes.
 */
public abstract class XNodeExpr {

	/** private static field just used in toString(). */
	private static boolean postfixed = true;

	/** The type of the node. For example add, not, or long. */
	protected final TypeExpr type;

	/** Returns the type of the node. For example add, not, or long. We need an accessor for scala. */
	public final TypeExpr getType() {
		return type;
	}

	/** Builds a node for a syntactic tree, with the specified type. */
	protected XNodeExpr(TypeExpr type) {
		this.type = type;
	}

	/** Collects the set of variables involved in the subtree whose root is this object, and add them to the specified set. */
	public abstract Set<XVar> collectVars(Set<XVar> set);

	/** Returns true iff a leaf in the subtree whose root is this object satisfies the specified predicate. */
	public boolean canFindLeafSuchThat(Predicate<XNodeLeaf> p) {
		return this instanceof XNodeParent ? Stream.of(((XNodeParent) this).sons).anyMatch(c -> c.canFindLeafSuchThat(p)) : p.test((XNodeLeaf) this);
	}

	/** Returns true iff a leaf in the subtree whose root is this object has the specified type. */
	public boolean canFindleafWith(TypeExpr type) {
		return canFindLeafSuchThat(n -> n.getType() == type);
	}

	/** Returns the maximum value of a parameter number in the subtree whose root is this object, or -1 if there is none. */
	public int maxParameterNumber() {
		if (this instanceof XNodeParent)
			return Stream.of(((XNodeParent) this).sons).mapToInt(c -> c.maxParameterNumber()).max().orElse(-1);
		else
			return type == TypeExpr.PAR ? ((Long) ((XNodeLeaf) this).value).intValue() : -1; // recall that %... is not possible in predicates
	}

	/** Replaces parameters with values of the specified array. */
	public abstract XNodeExpr concretizeWith(Object[] args);

	/** Returns true iff the tree has the form x, or the form x + k, or the form k + x or the form x - k, with x a variable and k a (long) integer. */
	public abstract boolean hasBasicForm();

	/** Returns true iff the tree has the form x <opa> y with <opa> an arithmetic operator in {+,-,*,/,%,dist}. */
	public abstract TypeArithmeticOperator arithmeticOperatorOnTwoVariables();

	/** Returns true iff the tree has two sons, one with a variable and the other with a (long) integer, in any order. */
	public abstract boolean hasVarAndCstSons();

	/** Returns true iff the tree has two sons, both with a variable. */
	public abstract boolean hasVarAndVarSons();

	public abstract Object getValueOfFirstLeafOfType(TypeExpr type);

	/**
	 * Returns a textual description of the subtree whose root is this node. The specified effective arguments will be used if there are some parameters in the
	 * subtree. The specified boolean value indicates if a post-fixed or a functional form is wanted.
	 */
	public abstract String toString(Object[] args, boolean postfixed);

	public abstract List<String> canonicalForm(List<String> tokens, XVarInteger[] scope);

	@Override
	public String toString() {
		return type.toString().toLowerCase();
	}

	/** The class used for representing a parent node in a syntactic tree. */
	public static final class XNodeParent extends XNodeExpr {
		/** The sons of the node. */
		public final XNodeExpr[] sons;

		/** Builds a parent node for a syntactic tree, with the specified type and the specified sons. */
		protected XNodeParent(TypeExpr type, XNodeExpr... sons) {
			super(type);
			this.sons = sons;
		}

		@Override
		public Set<XVar> collectVars(Set<XVar> set) {
			Stream.of(sons).forEach(s -> s.collectVars(set));
			return set;
		}

		public XNodeExpr concretizeWith(Object[] args) {
			return new XNodeParent(type, Stream.of(sons).map(s -> s.concretizeWith(args)).toArray(XNodeExpr[]::new));
		}

		@Override
		public boolean hasBasicForm() {
			return (type == TypeExpr.ADD && hasVarAndCstSons()) || (type == TypeExpr.SUB && sons[0].type == TypeExpr.VAR && sons[1].type == TypeExpr.LONG);
		}

		@Override
		public TypeArithmeticOperator arithmeticOperatorOnTwoVariables() {
			TypeArithmeticOperator op = XEnums.valueOf(TypeArithmeticOperator.class, type.name());
			return op != null && hasVarAndVarSons() ? op : null;
		}

		@Override
		public boolean hasVarAndCstSons() {
			return sons.length == 2
					&& ((sons[0].type == TypeExpr.VAR && sons[1].type == TypeExpr.LONG) || (sons[0].type == TypeExpr.LONG && sons[1].type == TypeExpr.VAR));
		}

		@Override
		public boolean hasVarAndVarSons() {
			return sons.length == 2 && sons[0].type == TypeExpr.VAR && sons[1].type == TypeExpr.VAR;
		}

		@Override
		public Object getValueOfFirstLeafOfType(TypeExpr type) {
			for (XNodeExpr son : sons) {
				Object o = son.getValueOfFirstLeafOfType(type);
				if (o != null)
					return o;
			}
			return null;
		}

		@Override
		public String toString(Object[] args, boolean postfixed) {
			StringBuilder sb = new StringBuilder();
			Stream.of(sons).forEach(s -> sb.append(s.toString(args, postfixed)).append(s != sons[sons.length - 1] ? " " : ""));
			return postfixed ? sb.toString() + " " + (type == TypeExpr.SET || sons.length > 2 && type != TypeExpr.IF ? sons.length : "") + super.toString()
					: super.toString() + "(" + sb.toString() + ")";
		}

		@Override
		public List<String> canonicalForm(List<String> tokens, XVarInteger[] scope) {
			Stream.of(sons).forEach(s -> s.canonicalForm(tokens, scope));
			tokens.add((type == TypeExpr.SET || sons.length > 2 && type != TypeExpr.IF ? sons.length : "") + super.toString());
			return tokens;
		}

		@Override
		public String toString() {
			String s = XUtility.join(sons, postfixed ? " " : ",");
			return postfixed ? s + " " + super.toString() : super.toString() + "(" + s + ")";
		}
	}

	/** The class used for representing a leaf node in a syntactic tree. */
	public static final class XNodeLeaf extends XNodeExpr {
		/** The (parsed) value of the node. it may be a variable, a decimal, a long, a parameter, or an empty set. */
		public final Object value;

		/** Builds a leaf node for a syntactic tree, with the specified type and the specified value. */
		protected XNodeLeaf(TypeExpr type, Object value) {
			super(type);
			this.value = value;
		}

		@Override
		public Set<XVar> collectVars(Set<XVar> set) {
			if (value instanceof XVar)
				set.add((XVar) value);
			return set;
		}

		public XNodeExpr concretizeWith(Object[] args) {
			if (type != TypeExpr.PAR)
				return new XNodeLeaf(type, value);
			Object arg = args[((Long) value).intValue()];
			if (arg instanceof XVar)
				return new XNodeLeaf(TypeExpr.VAR, arg);
			if (arg instanceof Long)
				return new XNodeLeaf(TypeExpr.LONG, arg);
			if (arg instanceof XNodeExpr)
				return (XNodeExpr) arg;
			if (arg instanceof Decimal)
				return new XNodeLeaf(TypeExpr.DECIMAL, arg);
			if (arg instanceof String)
				return new XNodeLeaf(TypeExpr.SYMBOL, arg);
			return (XNodeExpr) XUtility.control(false, "Another case need to be implemented for " + arg);
		}

		@Override
		public boolean hasBasicForm() {
			return type == TypeExpr.VAR;
		}

		@Override
		public TypeArithmeticOperator arithmeticOperatorOnTwoVariables() {
			return null;
		}

		@Override
		public boolean hasVarAndCstSons() {
			return false;
		}

		@Override
		public boolean hasVarAndVarSons() {
			return false;
		}

		@Override
		public Object getValueOfFirstLeafOfType(TypeExpr type) {
			return this.type == type ? value : null;
		}

		@Override
		public String toString(Object[] args, boolean postfixed) {
			return type == TypeExpr.PAR ? args[((Long) value).intValue()].toString() : type == TypeExpr.SET ? "set()" : value.toString();
		}

		@Override
		public List<String> canonicalForm(List<String> tokens, XVarInteger[] scope) {
			if (type == TypeExpr.VAR)
				tokens.add("%" + IntStream.range(0, scope.length).filter(i -> scope[i] == (XVarInteger) value).findFirst().orElse(-1));
			else if (type == TypeExpr.SET)
				tokens.add("0set");
			else
				tokens.add(value.toString());
			return tokens;
		}

		@Override
		public String toString() {
			return type == TypeExpr.PAR ? "%" + value.toString() : type == TypeExpr.SET ? "set()" : value.toString();
		}
	}
}
