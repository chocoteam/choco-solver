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
