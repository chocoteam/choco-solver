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

import java.util.stream.Stream;

public class XEnums {

	public static <T extends Enum<T>> T valueOf(Class<T> enumType, String name) {
		try {
			return Enum.valueOf(enumType, name.toUpperCase()); // just for upper-case
		} catch (IllegalArgumentException e) {
			return null;
		}
	}

	/** The enum type describing the different types of frameworks. */
	public static enum TypeFramework {
		CSP,
		COP,
		WCSP,
		FCSP,
		QCSP,
		QCSP_PLUS,
		QCOP,
		QCOP_PLUS,
		SCSP,
		SCOP,
		QSTR,
		TCSP,
		NCSP,
		NCOP,
		DisCSP,
		DisWCSP;
	}

	/**
	 * The enum type describing the different types of constraints and meta-constraints. We use lower-case letters, so as to directly get the names of the
	 * elements (no need to define constants or make any transformations).
	 */
	public static enum TypeCtr {
		extension,
		intension,
		regular,
		grammar,
		mdd,
		allDifferent,
		allEqual,
		allDistant,
		ordered,
		lex,
		allIncomparable,
		sum,
		count,
		nValues,
		cardinality,
		balance,
		spread,
		deviation,
		sumCosts,
		stretch,
		noOverlap,
		cumulative,
		binPacking,
		knapsack,
		networkFlow,
		circuit,
		nCircuits,
		path,
		nPaths,
		tree,
		nTrees,
		arbo,
		nArbos,
		nCliques,
		clause,
		instantiation,
		allIntersecting,
		range,
		roots,
		partition,
		minimum,
		maximum,
		element,
		channel,
		permutation,
		precedence,
		and,
		or,
		not,
		slide,
		seqbin,
		smart; // future constraint to be taken into account

		/** Returns true if the element has a sliding nature. */
		public boolean isSliding() {
			return this == slide || this == seqbin;
		}

		/** Returns true if the element has a logical nature. */
		public boolean isLogical() {
			return this == and || this == or || this == not;
		}

		/** Returns true if the element corresponds to a meta-constraint. */
		public boolean isMeta() {
			return isSliding() || isLogical();
		}
	}

	/**
	 * The enum type describing the different types of child elements of constraints. We use lower-case letters, so as to directly get the names of the elements
	 * (except for FINAL that needs to be managed apart, because this is a keyword).
	 */
	public static enum TypeChild {
		list,
		set,
		mset,
		matrix,
		function,
		supports,
		conflicts,
		except,
		value,
		values,
		total,
		coeffs,
		condition,
		cost,
		operator,
		number,
		transition,
		start,
		FINAL, // upper-cased because a keyword
		terminal,
		rules,
		index,
		mapping,
		occurs,
		rowOccurs,
		colOccurs,
		widths,
		patterns,
		origins,
		lengths,
		ends,
		heights,
		machines,
		conditions,
		sizes,
		weights,
		profits,
		limit,
		size,
		root,
		image,
		graph;
	}

	/**
	 * The enum type describing the different types of attributes of constraints. We use lower-case letters, so as to directly get the names of the elements
	 * (except for CLASS, FOR and CASE that need to be managed apart, because they correspond to keywords).
	 */
	public static enum TypeAtt {
		format,
		type,
		id,
		CLASS, // upper-cased because a keyword
		note,
		as,
		size,
		measure,
		parameters,
		defaultCost,
		reifiedBy,
		hreifiedFrom,
		hreifiedTo,
		closed,
		FOR, // upper-cased because a keyword
		restriction,
		rank,
		startIndex,
		zeroIgnored,
		CASE, // upper-cased because a keyword
		order,
		circular,
		offset,
		collect,
		violable,
		optimization,
		combination;
		// unclean, // used for tuples of table constraints
		// starred; // used for tuples of table constraints

		/** Returns true iff the element has a (full or half) reification nature. */
		public boolean isReifying() {
			return this == reifiedBy || this == hreifiedFrom || this == hreifiedTo;
		}

		/** Returns the constant that corresponds to the specified string (we need this method to manage the special constants FOR and CASE). */
		public static TypeAtt valOf(String s) {
			return s.equals("class") ? CLASS : s.equals("for") ? FOR : s.equals("case") ? TypeAtt.CASE : valueOf(s);
		}
	}

	/** The enum type describing the different flags that may be associated with some elements (e.g., constraints). */
	public static enum TypeFlag {
		STARRED_TUPLES,
		UNCLEAN_TUPLES;
	}

	/** The enum type describing the different types of reification. */
	public static enum TypeReification {
		FULL,
		HALF_FROM,
		HALF_TO;
	}

	/** The enum type describing the different types of operators that can be used in conditions. */
	public static enum TypeConditionOperator {
		LT,
		LE,
		GE,
		GT,
		NE,
		EQ,
		IN,
		NOTIN;

		/** Returns true iff the constant corresponds to a set operator. */
		public boolean isSet() {
			return this == IN || this == NOTIN;
		}
	}

	/** The enum type describing the different types of classical relational operators that can be used in conditions. */
	public static enum TypeConditionOperatorRel {
		LT,
		LE,
		GE,
		GT,
		NE,
		EQ;

		public TypeConditionOperatorRel reverseForSwap() {
			return this == LT ? GT : this == LE ? GE : this == GE ? LE : this == GT ? LT : this; // no change for NE and EQ
		}
	}

	/** The enum type describing the different types of operators that can be used in conditions. */
	public static enum TypeConditionOperatorSet {
		IN,
		NOTIN;
	}

	/** The enum type describing the different types of operators that can be used in elements <operator>. */
	public static enum TypeOperator {
		LT,
		LE,
		GE,
		GT,
		SUBSET,
		SUBSEQ,
		SUPSEQ,
		SUPSET;

		public static TypeOperator valOf(String s) {
			return TypeOperator.valueOf(s.trim().toUpperCase());
		}

		/** Returns true iff the constant corresponds to a set operator. */
		public boolean isSet() {
			return this == SUBSET || this == SUBSEQ || this == SUPSEQ || this == SUPSET;
		}
	}

	/** The enum type describing the different types of operators that can be used in elements <operator>. */
	public static enum TypeArithmeticOperator {
		ADD,
		SUB,
		MUL,
		DIV,
		MOD,
		DIST;
	}

	/** The enum type describing the different types of nodes that can be found in syntactic trees (built for intensional expressions). */
	public static enum TypeExpr {
		NEG(1),
		ABS(1),
		ADD(2, Integer.MAX_VALUE),
		SUB(2),
		MUL(2, Integer.MAX_VALUE),
		DIV(2),
		MOD(2),
		SQR(1),
		POW(2),
		MIN(1, Integer.MAX_VALUE),
		MAX(1, Integer.MAX_VALUE),
		DIST(2),
		LT(2),
		LE(2),
		GE(2),
		GT(2),
		NE(2),
		EQ(2, Integer.MAX_VALUE),
		SET(0, Integer.MAX_VALUE),
		IN(2),
		NOT(1),
		AND(2, Integer.MAX_VALUE),
		OR(2, Integer.MAX_VALUE),
		XOR(2, Integer.MAX_VALUE),
		IFF(2, Integer.MAX_VALUE),
		IMP(2),
		IF(3),
		CARD(1),
		UNION(2, Integer.MAX_VALUE),
		INTER(2, Integer.MAX_VALUE),
		DIFF(2),
		SDIFF(2, Integer.MAX_VALUE),
		HULL(1),
		DJOINT(2),
		SUBSET(2),
		SUBSEQ(2),
		SUPSET(2),
		SUPSEQ(2),
		CONVEX(1),
		FDIV(2),
		FMOD(2),
		SQRT(1),
		NROOT(2),
		EXP(1),
		LN(1),
		LOG(2),
		SIN(1),
		COS(1),
		TAN(1),
		ASIN(1),
		ACOS(1),
		ATAN(1),
		SINH(1),
		COSH(1),
		TANH(1),
		LONG(0),
		RATIONAL(0),
		DECIMAL(0),
		VAR(0),
		PAR(0),
		SYMBOL(0);

		/** The minimal and maximal arity (number of sons) of the node. */
		protected final int arityMin, arityMax;

		/** Builds a constant, while specifying its minimal and maximal arity (number of sons). */
		TypeExpr(int arityMin, int arityMax) {
			this.arityMin = arityMin;
			this.arityMax = arityMax;
		}

		/** Builds a constant, while specifying its arity (number of sons). */
		TypeExpr(int arity) {
			this(arity, arity);
		}

		// public TypeConditionOperator toConditionOperator() {
		// return this == LT || this == LE || this == GE || this == GT || this == EQ || this == NE;
		// }

	}

	/** The enum type describing the different types of measures used by elements <cost>. */
	public static enum TypeMeasure {
		VAR,
		DEC,
		VAL,
		EDIT;
	}

	/** The enum type describing the different types of objectives. */
	public static enum TypeObjective {
		EXPRESSION,
		SUM,
		PRODUCT,
		MINIMUM,
		MAXIMUM,
		NVALUES,
		LEX;
	}

	/** The enum type describing the different types of combination of objectives. */
	public static enum TypeCombination {
		LEXICO,
		PARETO;
	}

	/** The enum type describing the different types of ranking used by constraints <maximum>, <minimum>, <element>. */
	public static enum TypeRank {
		FIRST,
		LAST,
		ANY;
	}

	/** The interface that denotes a class that can be associated with an XCSP3 element */
	public interface TypeClass {
		public String name();

		/** Transforms String objects into TypeClass objects. */
		public static TypeClass[] classesFor(String... classes) {
			return Stream
					.of(classes)
					.map(s -> Stream.of(StandardClass.values()).map(c -> (TypeClass) c).filter(c -> c.name().equals(s)).findFirst().orElse(new SpecialClass(s)))
					.toArray(TypeClass[]::new);
		}

		/** Determines if the two specified arrays of TypeClass objects are disjoint or not. */
		public static boolean disjoint(TypeClass[] t1, TypeClass[] t2) {
			if (t1 == null || t2 == null)
				return true;
			for (TypeClass c1 : t1)
				for (TypeClass c2 : t2)
					if (c1.name().equals(c2.name()))
						return false;
			return true;
		}

	}

	/** The enum type describing the different standard classes that can be associated with XCSP3 elements. */
	public static enum StandardClass implements TypeClass {
		channeling,
		clues,
		rows,
		columns,
		diagonals,
		symmetryBreaking,
		redundantConstraints,
		nogoods;
	}

	/** The class that allows the user to define his own classes */
	public static class SpecialClass implements TypeClass {
		private String name;

		public SpecialClass(String name) {
			this.name = name;
		}

		public String name() {
			return name;
		}
	}
}
