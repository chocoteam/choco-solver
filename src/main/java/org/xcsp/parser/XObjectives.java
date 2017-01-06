/**
 * This file is part of choco-parsers, https://github.com/chocoteam/choco-parsers
 *
 * Copyright (c) 2017-01-06T09:54:20Z, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 * See LICENSE file in the project root for full license information.
 */
package org.xcsp.parser;

import org.xcsp.parser.XEnums.TypeObjective;
import org.xcsp.parser.XParser.AnyEntry;
import org.xcsp.parser.XValues.SimpleValue;
import org.xcsp.parser.XVariables.XVar;

public class XObjectives {

	/** The root class for representing objectives. */
	public static abstract class OEntry extends AnyEntry {

		/** Indicates whether the objective must be minimized or maximized. */
		public final boolean minimize;

		/** The type (expression, sum, minimum, ...) of the objective. */
		public final TypeObjective type;

		/** Returns The type (expression, sum, minimum, ...) of the objective. We need an accessor for Scala. */
		public final TypeObjective getType() {
			return type;
		}

		/** Builds an objective with the specified minimize value and type. */
		public OEntry(boolean minimize, TypeObjective type) {
			this.minimize = minimize;
			this.type = type;
		}

		@Override
		public String toString() {
			return id + " " + (minimize ? "minimize" : "maximize") + " " + type;
		}
	}

	/** The class for representing objectives defined from functional expressions (can just be a variable). */
	public static final class OObjectiveExpr extends OEntry {
		public final XNodeExpr rootNode;

		/** Builds an objective from the specified functional expression (given by the root of a syntactic tree). */
		public OObjectiveExpr(boolean minimize, TypeObjective type, XNodeExpr rootNode) {
			super(minimize, type);
			this.rootNode = rootNode;
		}

		@Override
		public String toString() {
			return super.toString() + " " + rootNode.toString();
		}

	}

	/** The class for representing objectives defined from a list of variables, and possibly a list of coefficients. */
	public static final class OObjectiveSpecial extends OEntry {
		/** The list of variables of the objective. */
		public final XVar[] vars;

		/** The list of coefficients. Either this field is null, or there are as many coefficients as variables. */
		public final SimpleValue[] coeffs;

		/** Builds an objective from the specified arrays of variables and coefficients. */
		public OObjectiveSpecial(boolean minimize, TypeObjective type, XVar[] vars, SimpleValue[] coeffs) {
			super(minimize, type);
			this.vars = vars;
			this.coeffs = coeffs;
		}

		@Override
		public String toString() {
			return super.toString() + "\n" + XUtility.join(vars) + (coeffs != null ? "\n" + XUtility.join(coeffs) : "");
		}
	}
}
