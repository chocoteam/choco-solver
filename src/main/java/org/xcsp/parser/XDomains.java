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

import java.util.Arrays;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.xcsp.parser.XValues.IntegerEntity;
import org.xcsp.parser.XValues.IntegerValue;
import org.xcsp.parser.XValues.RealInterval;
import org.xcsp.parser.XValues.SimpleValue;
import org.xcsp.parser.XValues.TypePrimitive;
import org.xcsp.parser.XVariables.TypeVar;
import org.xcsp.parser.XVariables.XVar;

/** In this class, we find intern classes for managing all types of domains. */
public class XDomains {

	/** The root interface to tag domain objects. */
	public static interface XDom {
	}

	/** A class for representing basic domains, i.e. domains for integer, symbolic, real and stochastic variables. */
	public static class XDomBasic implements XDom {

		/** Returns the basic domain obtained by parsing the specified string, according to the value of the specified type. */
		public static XDomBasic parse(String s, TypeVar type) {
			return type == TypeVar.integer ? new XDomInteger(s) : type == TypeVar.symbolic ? new XDomSymbolic(s) : type == TypeVar.real ? new XDomReal(s)
					: XDomStochastic.parse(s, type);
		}

		/** Returns the sequence of basic domains for the variables in the specified array. */
		public static XDomBasic[] domainsFor(XVar[] vars) {
			return Stream.of(vars).map(x -> ((XDomBasic) x.dom)).toArray(XDomBasic[]::new);
		}

		/**
		 * Returns the sequence of basic domains for the variables in the first row of the specified two-dimensional array, provided that variables of the other
		 * rows have similar domains. Returns null otherwise.
		 */
		public static XDomBasic[] domainsFor(XVar[][] varss) {
			XDomBasic[] doms = domainsFor(varss[0]);
			for (XVar[] vars : varss)
				if (IntStream.range(0, vars.length).anyMatch(i -> doms[i] != vars[i].dom))
					return null;
			return doms;
		}

		/**
		 * The values of the domain: for an integer domain, values are IntegerEntity, for a symbolic domain, values are String, and for a float domain, values
		 * are RealInterval.
		 */
		public final Object[] values;

		/** Builds a basic domain, with the specified values. */
		protected XDomBasic(Object[] values) {
			this.values = values;
		}

		@Override
		public String toString() {
			return "Values: " + XUtility.join(values);
		}
	}

	/** The class for representing the domain of an integer variable. */
	public static class XDomInteger extends XDomBasic {

		/** Builds an integer domain, with the integer values (entities that are either integers or integer intervals) obtained by parsing the specified string. */
		protected XDomInteger(String seq) {
			super(IntegerEntity.parseSeq(seq)); // must be already sorted.
		}

		/** Returns the first (smallest) value of the domain. It may be VAL_M_INFINITY for -infinity. */
		public long getFirstValue() {
			return ((IntegerEntity) values[0]).smallest();
		}

		/** Returns the last (greatest) value of the domain. It may be VAL_P_INFINITY for +infinity. */
		public long getLastValue() {
			return ((IntegerEntity) values[values.length - 1]).greatest();
		}

		/** Returns the smallest (the most efficient in term of space consumption) primitive that can be used for representing any value of the domain. */
		public TypePrimitive whichPrimitive() {
			return TypePrimitive.whichPrimitiveFor(getFirstValue(), getLastValue());
		}

		/** Returns true iff the domain contains the specified value. */
		public boolean contains(long v) {
			for (int left = 0, right = values.length - 1; left <= right;) {
				int center = (left + right) / 2;
				int res = ((IntegerEntity) values[center]).compareContains(v);
				if (res == 0)
					return true;
				if (res == -1)
					left = center + 1;
				else
					right = center - 1;
			}
			return false;
		}

		private Long nbValues; // cache for lazy initialization

		/** Returns the number of values in the domain, if the domain is finite. Return -1 otherwise. */
		public long getNbValues() {
			if (nbValues != null)
				return nbValues;
			if (getFirstValue() == XConstants.VAL_MINUS_INFINITY || getLastValue() == XConstants.VAL_PLUS_INFINITY)
				return nbValues = -1L; // infinite number of values
			long cnt = 0;
			for (IntegerEntity entity : (IntegerEntity[]) values)
				if (entity instanceof IntegerValue)
					cnt++;
				else {
					long diff = entity.width(), l = cnt + diff;
					XUtility.control(cnt == l - diff, "Overflow");
					cnt = l;
				}
			return nbValues = cnt;
		}
	}

	/** The class for representing the domain of a symbolic variable. */
	public static final class XDomSymbolic extends XDomBasic {

		/** Builds a symbolic domain, with the symbols obtained by parsing the specified string. */
		protected XDomSymbolic(String seq) {
			super(XUtility.sort(seq.split("\\s+")));
		}

		/** Returns true iff the domain contains the specified value. */
		protected boolean contains(String s) {
			return Arrays.binarySearch((String[]) values, s) >= 0;
		}
	}

	/** The class for representing the domain of a real variable. */
	public static class XDomReal extends XDomBasic {

		/** Builds a real domain, with the intervals obtained by parsing the specified string. */
		protected XDomReal(String seq) {
			super(RealInterval.parseSeq(seq));
		}
	}

	/** The class for representing the domain of a stochastic variable. */
	public static final class XDomStochastic extends XDomBasic {
		/** Returns the stochastic domain obtained by parsing the specified string, according to the specified type. */
		public static XDomStochastic parse(String s, TypeVar type) {
			String[] toks = s.split("\\s+");
			Object[] values = new Object[toks.length];
			SimpleValue[] probas = new SimpleValue[toks.length];
			for (int i = 0; i < toks.length; i++) {
				String[] t = toks[i].split(":");
				values[i] = type == TypeVar.symbolic_stochastic ? t[0] : IntegerEntity.parse(t[0]);
				probas[i] = SimpleValue.parse(t[1]);
			}
			return new XDomStochastic(values, probas);
		}

		/**
		 * The probabilities associated with the values of the domain: probas[i] is the probability of values[i]. Probabilities can be given as rational,
		 * decimal, or integer values (only, 0 and 1 for integer).
		 */
		public final SimpleValue[] probas;

		/** Builds a stochastic domain, with the specified values and the specified probabilities. */
		protected XDomStochastic(Object[] values, SimpleValue[] probas) {
			super(values);
			this.probas = probas;
			assert values.length == probas.length;
		}

		@Override
		public String toString() {
			return super.toString() + " Probas: " + XUtility.join(probas);
		}
	}

	/** The interface to tag complex domains, i.e. domains for set or graph variables. */
	public static interface XDomComplex extends XDom {
	}

	/** The class for representing the domain of a set variable. */
	public static final class XDomSet implements XDomComplex {
		/** Returns the set domain obtained by parsing the specified strings, according to the specified type. */
		public static XDomSet parse(String req, String pos, TypeVar type) {
			return type == TypeVar.set ? new XDomSet(IntegerEntity.parseSeq(req), IntegerEntity.parseSeq(pos))
					: new XDomSet(req.split("\\s+"), pos.split("\\s+"));
		}

		/** The required and possible values. For an integer set domain, values are IntegerEntity. For a symbolic set domain, values are String. */
		public final Object[] required, possible;

		/** Builds a set domain, with the specified required and possible values. */
		protected XDomSet(Object[] required, Object[] possible) {
			this.required = required;
			this.possible = possible;
		}

		@Override
		public String toString() {
			return "[{" + XUtility.join(required) + "},{" + XUtility.join(possible) + "}]";
		}
	}

	/** The class for representing the domain of a graph variable. */
	public static final class XDomGraph implements XDomComplex {
		/** Returns the graph domain obtained by parsing the specified strings, according to the specified type. */
		public static XDomGraph parse(String reqV, String reqE, String posV, String posE, TypeVar type) {
			String[] rV = reqV.split("\\s+"), pV = posV.split("\\s+");
			String[][] rE = Stream.of(reqE.split(XConstants.DELIMITER_LISTS)).skip(1).map(tok -> tok.split("\\s*,\\s*")).toArray(String[][]::new);
			String[][] pE = Stream.of(posE.split(XConstants.DELIMITER_LISTS)).skip(1).map(tok -> tok.split("\\s*,\\s*")).toArray(String[][]::new);
			return new XDomGraph(rV, pV, rE, pE);
		}

		/** The required and possible nodes (vertices). */
		public final String[] requiredV, possibleV;

		/** The required and possible edges or arcs. */
		public final String[][] requiredE, possibleE;

		/** Builds a graph domain, with the specified required and possible values (nodes and edges/arcs). */
		protected XDomGraph(String[] requiredV, String[] possibleV, String[][] requiredE, String[][] possibleE) {
			this.requiredV = requiredV;
			this.possibleV = possibleV;
			this.requiredE = requiredE;
			this.possibleE = possibleE;
		}

		@Override
		public String toString() {
			return "[{" + XUtility.join(requiredV) + "-" + XUtility.join(requiredE) + "},{" + XUtility.join(possibleV) + "-" + XUtility.join(possibleE) + "}]";
		}
	}
}
