package org.chocosolver.parser.xcsp.tools;

public class XEnums {

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
		allIncomprable,
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
		cube,
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
		seqbin;

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
	 * (except for FOR and CASE that need to be managed apart, because they correspond to keywords).
	 */
	public static enum TypeAtt {
		format,
		type,
		id,
		as,
		size,
		measure,
		parameters,
		reifiedBy,
		hreifiedFrom,
		hreifiedTo,
		closed,
		FOR, // upper-cased because a keyword
		restriction,
		rank,
		startIndex,
		CASE, // upper-cased because a keyword
		order,
		circular,
		offset,
		collect,
		violable,
		optimization,
		unfiltered,
		starred;

		/** Returns true iff the element has a (full or half) reification nature. */
		public boolean isReifying() {
			return this == reifiedBy || this == hreifiedFrom || this == hreifiedTo;
		}

		/** Returns the constant that corresponds to the specified string (we need this method to manage the special constants FOR and CASE). */
		public static TypeAtt valOf(String s) {
			return s.equals("for") ? FOR : s.equals("case") ? TypeAtt.CASE : valueOf(s);
		}

	}

	/** The enum type describing the different types of operators that can be used in conditions. */
	public static enum TypeConditionOperator {
		lt,
		le,
		ge,
		gt,
		ne,
		eq,
		in,
		notin;

		/** Returns true iff the constant corresponds to a set operator. */
		public boolean isSet() {
			return this == in || this == notin;
		}
	}

	/** The enum type describing the different types of operators that can be used in elements <operator>. */
	public static enum TypeOperator {
		lt,
		le,
		ge,
		gt,
		subset,
		subseq,
		supseq,
		supset;

		/** Returns true iff the constant corresponds to a set operator. */
		public boolean isSet() {
			return this == subset || this == subseq || this == supseq || this == supset;
		}

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
		PAR(0);

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
	}

	/** The enum type describing the different types of measures used by elements <cost>. */
	public static enum TypeMeasure {
		var,
		dec,
		val,
		edit;
	}

	/** The enum type describing the different types of objectives. */
	public static enum TypeObjective {
		expression,
		sum,
		product,
		minimum,
		maximum,
		nValues,
		lex;
	}
}
