package org.chocosolver.parser.xcsp.tools;

import org.chocosolver.parser.xcsp.tools.XDomains.DomBasic;
import org.chocosolver.parser.xcsp.tools.XDomains.DomInteger;
import org.chocosolver.parser.xcsp.tools.XParser.ModifiableBoolean;
import org.chocosolver.parser.xcsp.tools.XVariables.TypeVar;
import org.chocosolver.parser.xcsp.tools.XVariables.Var;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.chocosolver.parser.xcsp.tools.XConstants.*;
import static org.chocosolver.parser.xcsp.tools.XUtility.safeLong;

public class XValues {

	/** The enum type describing the different types of primitives that can be used for representing arrays of integer tuples. */
	public static enum TypePrimitive {
		BYTE,
		SHORT,
		INT,
		LONG;

		/** Returns the smallest primitive that can be used for representing values lying within the specified bounds. */
		public static TypePrimitive whichPrimitiveFor(long inf, long sup) {
			if (MIN_SAFE_BYTE <= inf && sup <= MAX_SAFE_BYTE)
				return BYTE;
			if (MIN_SAFE_SHORT <= inf && sup <= MAX_SAFE_SHORT)
				return SHORT;
			if (MIN_SAFE_INT <= inf && sup <= MAX_SAFE_INT)
				return INT;
			// if (MIN_SAFE_LONG <= inf && sup <= MAX_SAFE_LONG)
			return LONG; // else return null;
		}

		/** Returns the smallest primitive that can be used for representing the specified value. */
		public static TypePrimitive whichPrimitiveFor(long val) {
			return whichPrimitiveFor(val, val);
		}

		/**
		 * Returns the smallest primitive that can be used for representing any value of the domains of the specified variables. If one variable is not integer,
		 * null is returned.
		 */
		public static TypePrimitive whichPrimitiveFor(Var[] vars) {
			if (Stream.of(vars).anyMatch(x -> x.type != TypeVar.integer))
				return null;
			return TypePrimitive.values()[Stream.of(vars).mapToInt(x -> ((DomInteger) x.dom).whichPrimitive().ordinal()).max()
					.orElse(TypePrimitive.LONG.ordinal())];
		}

		/**
		 * Returns the smallest primitive that can be used for representing any value of the domains of the specified variables. If one variable is not integer,
		 * null is returned.
		 */
		public static TypePrimitive whichPrimitiveFor(Var[][] varss) {
			if (whichPrimitiveFor(varss[0]) == null)
				return null;
			return TypePrimitive.values()[Stream.of(varss).mapToInt(t -> whichPrimitiveFor(t).ordinal()).max().orElse(TypePrimitive.LONG.ordinal())];
		}

		/** Returns true iff the primitive can represent the specified value. */
		private boolean canRepresent(long val) {
			return this.ordinal() >= whichPrimitiveFor(val).ordinal();
		}

		/**
		 * Parse the specified string that denotes a sequence of values. In case, we have at least one interval, we just return an array of IntegerEntity (as
		 * for integer domains), and no validity test on values is performed. Otherwise, we return an array of integer (either long[] or int[]). It is possible
		 * that some values are discarded because either they do not belong to the specified domain (test performed if this domain is not null), or they cannot
		 * be represented by the primitive.
		 */
		protected Object parseSeq(String s, DomInteger dom) {
			if (s.indexOf("..") != -1)
				return IntegerEntity.parseSeq(s);
			int nbDiscarded = 0;
			List<Long> list = new ArrayList<>();
			for (String tok : s.split("\\s+")) {
				assert !tok.equals("*") : "STAR not handled in unary lists";
				long l = XUtility.safeLong(tok);
				if (canRepresent(l) && (dom == null || dom.contains(l)))
					list.add(l);
				else
					nbDiscarded++;
			}
			if (nbDiscarded > 0)
				System.out.println(nbDiscarded + " discarded values in the unary list " + s);
			if (this == LONG)
				return list.stream().mapToLong(i -> i).toArray();
			else
				return list.stream().mapToInt(i -> i.intValue()).toArray();
			// TODO possible refinement for returning byte[] and short[]
		}

		/**
		 * Parse the specified string, and builds a tuple of (long) integers put in the specified array t. If the tuple is not valid wrt the specified domains
		 * or the primitive, false is returned, in which case, the tuple can be discarded. If * is encountered, the specified modifiable boolean is set to true.
		 */
		protected boolean parseTuple(String s, long[] t, DomBasic[] doms, ModifiableBoolean mb) {
			String[] toks = s.split("\\s*,\\s*");
			assert toks.length == t.length : toks.length + " " + t.length;
			for (int i = 0; i < toks.length; i++) {
				if (toks[i].equals("*")) {
					t[i] = this == BYTE ? XConstants.STAR_BYTE : this == SHORT ? XConstants.STAR_SHORT : this == INT ? XConstants.STAR_INT : XConstants.STAR;
					mb.value = true;
				} else {
					long l = XUtility.safeLong(toks[i]);
					if (canRepresent(l) && (doms == null || ((DomInteger) doms[i]).contains(l)))
						t[i] = l;
					else
						return false; // because the tuple can be discarded
				}
			}
			return true;
		}
	}

	/** An interface used to denote simple values, i.e., rational, decimal or integer values. */
	public static interface SimpleValue {
		/**
		 * Returns a simple value obtained by parsing the specified string. The specified boolean allows us to indicate if special values (such as +infinity)
		 * must be checked.
		 */
		public static SimpleValue parse(String s, boolean checkSpecialValues) {
			String[] t = s.split("/");
			if (t.length == 2)
				return new Rational(safeLong(t[0]), safeLong(t[1]));
			t = s.split("\\.");
			if (t.length == 2)
				return new Decimal(safeLong(t[0]), safeLong(t[1]));
			return new IntegerValue(safeLong(s, checkSpecialValues));
		}

		/** Returns a simple value obtained by parsing the specified string. */
		public static SimpleValue parse(String s) {
			return parse(s, false);
		}

		/** Returns an array of SimpleValue objects, obtained by parsing the specified string. */
		public static SimpleValue[] parseSeq(String seq) {
			return Stream.of(seq.split("\\s+")).map(s -> SimpleValue.parse(s)).toArray(SimpleValue[]::new);
		}
	}

	/**
	 * An interface used to denote integer entities, i.e., either integer values or integer intervals. These entities are present when defining integer domains
	 * or unary integer extensional constraints.
	 */
	public static interface IntegerEntity extends Comparable<IntegerEntity> {
		/** Returns an integer entity (integer value or integer interval) obtained by parsing the specified string. */
		public static IntegerEntity parse(String s) {
			String[] t = s.split("\\.\\.");
			return t.length == 1 ? new IntegerValue(safeLong(t[0])) : new IntegerInterval(safeLong(t[0], true), safeLong(t[1], true));
		}

		/** Returns an array of integer entities (integer values or integer intervals) obtained by parsing the specified string. */
		public static IntegerEntity[] parseSeq(String seq) {
			return Stream.of(seq.split("\\s+")).map(tok -> IntegerEntity.parse(tok)).toArray(IntegerEntity[]::new);
		}

		@Override
		public default int compareTo(IntegerEntity p) {
			long l1 = this instanceof IntegerValue ? ((IntegerValue) this).v : ((IntegerInterval) this).inf;
			long l2 = p instanceof IntegerValue ? ((IntegerValue) p).v : ((IntegerInterval) p).inf;
			return l1 < l2 ? -1 : l1 > l2 ? 1 : 0; // correct because pieces do not overlap
		}

		/** Returns true iff the entity is an integer value or an integer interval containing only one value */
		public boolean isSingleton();

		/** Returns the smallest value of the entity (the value itself or the lower bound of the interval). */
		public long smallest();

		/** Returns the greatest value of the entity (the value itself or the upper bound of the interval). */
		public long greatest();

		/** Returns the number of values represented by the entity. */
		public long width();

		/**
		 * Returns 0 if the entity contains the specified value, -1 if the values of the entity are strictly smaller than the specified value, and +1 if the
		 * values of the entity are strictly greater than the specified value.
		 */
		public int compareContains(long l);

	}

	/** A class to represent an integer value. */
	public static final class IntegerValue implements IntegerEntity, SimpleValue {
		/** The value of the integer. */
		public final long v;

		/** Builds an IntegerValue object with the specified value. */
		protected IntegerValue(long v) {
			this.v = v;
		}

		@Override
		public boolean isSingleton() {
			return true;
		}

		@Override
		public long smallest() {
			return v;
		}

		@Override
		public long greatest() {
			return v;
		}

		@Override
		public long width() {
			return 1L;
		}

		@Override
		public int compareContains(long l) {
			return Long.compare(v, l);
		}

		@Override
		public String toString() {
			return v + "";
		}
	}

	/** A class to represent an integer interval. */
	public static final class IntegerInterval implements IntegerEntity {
		/** The bounds of the interval. */
		public final long inf, sup;

		/** Builds an IntegerInterval object with the specified bounds. */
		protected IntegerInterval(long inf, long sup) {
			this.inf = inf;
			this.sup = sup;
			assert inf <= sup : "Pb with an interval " + this;
		}

		@Override
		public boolean isSingleton() {
			return inf == sup;
		}

		@Override
		public long smallest() {
			return inf;
		}

		@Override
		public long greatest() {
			return sup;
		}

		@Override
		public long width() {
			return sup - inf + 1;
		}

		@Override
		public int compareContains(long l) {
			return sup < l ? -1 : inf > l ? 1 : 0;
		}

		@Override
		public String toString() {
			return inf + ".." + sup;
		}
	}

	/** A class to represent rational values. */
	static final class Rational implements SimpleValue {
		/** The numerator and the denominator of the rational. */
		public final long numerator, denominator;

		/** Builds a rational with the specified numerator and denominator. */
		protected Rational(long num, long den) {
			this.numerator = num;
			this.denominator = den;
			assert den != 0 : "Pb with rational " + this;
		}

		@Override
		public String toString() {
			return numerator + "/" + denominator;
		}
	}

	/** A class to represent decimal values. */
	static final class Decimal implements SimpleValue {
		/** The integer and decimal parts of the decimal value. */
		public final long integerPart, decimalPart;

		/** Builds a decimal with the specified integer and decimal parts. */
		protected Decimal(long integerPart, long decimalPart) {
			this.integerPart = integerPart;
			this.decimalPart = decimalPart;
		}

		@Override
		public String toString() {
			return integerPart + "." + decimalPart;
		}
	}

	/** A class to represent real intervals. */
	static final class RealInterval {
		/** Returns a real interval by parsing the specified string. */
		public static RealInterval parse(String s) {
			boolean infClosed = s.charAt(0) == '[', supClosed = s.charAt(s.length() - 1) == '[';
			String[] t = s.split(",");
			SimpleValue inf = SimpleValue.parse(t[0].substring(1), true), sup = SimpleValue.parse(t[1].substring(0, t[1].length() - 1), true);
			return new RealInterval(inf, sup, infClosed, supClosed);
		}

		/** Returns an array of real intervals by parsing the specified string. */
		public static RealInterval[] parseSeq(String seq) {
			return Stream.of(seq.split("\\s+")).map(tok -> RealInterval.parse(tok)).toArray(RealInterval[]::new);
		}

		/** The bounds of the interval. */
		public final SimpleValue inf, sup;

		/** The status (open/closed) of the bounds of the interval. */
		public final boolean infClosed, supClosed;

		/** Builds a real interval with the specified bounds together with their status. */
		protected RealInterval(SimpleValue inf, SimpleValue sup, boolean infClosed, boolean supClosed) {
			this.inf = inf;
			this.sup = sup;
			this.infClosed = infClosed;
			this.supClosed = supClosed;
		}

		@Override
		public String toString() {
			return (infClosed ? "[" : "]") + inf + "," + sup + (supClosed ? "[" : "]");
		}
	}
}
