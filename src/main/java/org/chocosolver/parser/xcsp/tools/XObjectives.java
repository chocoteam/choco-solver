package org.chocosolver.parser.xcsp.tools;

import org.chocosolver.parser.xcsp.tools.XEnums.TypeObjective;
import org.chocosolver.parser.xcsp.tools.XValues.SimpleValue;
import org.chocosolver.parser.xcsp.tools.XVariables.Var;

public class XObjectives {

	/** The root class for representing objectives. */
	public static abstract class Objective {
		/** The id (unique identifier) of the objective */
		public final String id;

		/** Indicates whether the objective must be minimized or maximized. */
		public final boolean minimize;

		/** The type (expression, sum, minimum, ...) of the objective. */
		public final TypeObjective type;

		/** Builds an objective with the specified id, minimize value and type. */
		public Objective(String id, boolean minimize, TypeObjective type) {
			this.id = id;
			this.minimize = minimize;
			this.type = type;
		}

		@Override
		public String toString() {
			return id + " " + (minimize ? "minimize" : "maximize") + " " + type;
		}
	}

	/** The class for representing objectives defined from functional expressions (can just be a variable). */
	public static final class ObjectiveExpr extends Objective {
		public final XNodeExpr rootNode;

		/** Builds an objective from the specified functional expression (given by the root of a syntactic tree). */
		public ObjectiveExpr(String id, boolean minimize, TypeObjective type, XNodeExpr rootNode) {
			super(id, minimize, type);
			this.rootNode = rootNode;
		}

		@Override
		public String toString() {
			return super.toString() + " " + rootNode.toString();
		}

	}

	/** The class for representing objectives defined from a list of variables, and possibly a list of coefficients. */
	public static final class ObjectiveSpecial extends Objective {
		/** The list of variables of the objective. */
		public final Var[] vars;

		/** The list of coefficients. Either this field is null, or there are as many coefficients as variables. */
		public final SimpleValue[] coeffs;

		/** Builds an objective from the specified arrays of variables and coefficients. */
		public ObjectiveSpecial(String id, boolean minimize, TypeObjective type, Var[] vars, SimpleValue[] coeffs) {
			super(id, minimize, type);
			this.vars = vars;
			this.coeffs = coeffs;
		}

		@Override
		public String toString() {
			return super.toString() + "\n" + XUtility.join(vars) + (coeffs != null ? "\n" + XUtility.join(coeffs) : "");
		}
	}
}
