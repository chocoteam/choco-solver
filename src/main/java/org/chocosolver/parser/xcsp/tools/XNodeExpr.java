package org.chocosolver.parser.xcsp.tools;

import org.chocosolver.parser.xcsp.tools.XEnums.TypeExpr;
import org.chocosolver.parser.xcsp.tools.XVariables.Var;

import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * The class used for representing a node of a syntactic tree (built for functional expressions, and used especially with <intension>). Also includes, as intern
 * classes, subclasses for managing parent and leaf nodes.
 */
public abstract class XNodeExpr {

	/** private static field just used in toString(). */
	private static boolean postfixed = true;

	/** The type of the node. For example add, not, or long. */
	public final TypeExpr type;

	/** Builds a node for a syntactic tree, with the specified type. */
	protected XNodeExpr(TypeExpr type) {
		this.type = type;
	}

	/** Collects the set of variables involved in the subtree whose root is this object, and add them to the specified set. */
	public abstract Set<Var> collectVars(Set<Var> set);

	/** Returns true iff a leaf in the subtree whose root is this object satisfies the specified predicate. */
	public boolean canFindLeafSuchThat(Predicate<XNodeLeaf> p) {
		return this instanceof XNodeParent ? Stream.of(((XNodeParent) this).sons).anyMatch(c -> c.canFindLeafSuchThat(p)) : p.test((XNodeLeaf) this);
	}

	/** Returns true iff a leaf in the subtree whose root is this object has the specified type. */
	public boolean canFindleafWith(TypeExpr type) {
		return canFindLeafSuchThat(n -> n.type == type);
	}

	/** Returns the maximum value of a parameter number in the subtree whose root is this object, or -1 if there is none. */
	public int maxParameterNumber() {
		if (this instanceof XNodeParent)
			return Stream.of(((XNodeParent) this).sons).mapToInt(c -> c.maxParameterNumber()).max().orElse(-1);
		else
			return type == TypeExpr.PAR ? ((Long) ((XNodeLeaf) this).value).intValue() : -1; // recall that %... is not possible for intension
	}

	/**
	 * Returns a textual description of the subtree whose root is this node. The specified effective arguments will be used if there are some parameters in the
	 * subtree. The specified boolean value indicates if a post-fixed or a functional form is wanted.
	 */
	public abstract String toString(Object[] args, boolean postfixed);

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
		public Set<Var> collectVars(Set<Var> set) {
			Stream.of(sons).forEach(s -> s.collectVars(set));
			return set;
		}

		@Override
		public String toString(Object[] args, boolean postfixed) {
			StringBuilder sb = new StringBuilder();
			Stream.of(sons).forEach(s -> sb.append(s.toString(args, postfixed)).append(s != sons[sons.length - 1] ? " " : ""));
			return postfixed ? sb.toString() + " " + (type == TypeExpr.SET || sons.length > 2 && type != TypeExpr.IF ? sons.length : "") + super.toString()
					: super.toString() + "(" + sb.toString() + ")";
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
		public Set<Var> collectVars(Set<Var> set) {
			if (value instanceof Var)
				set.add((Var) value);
			return set;
		}

		@Override
		public String toString(Object[] args, boolean postfixed) {
			return type == TypeExpr.PAR ? args[Integer.parseInt(value.toString())].toString() : type == TypeExpr.SET ? "set()" : value.toString();
		}

		@Override
		public String toString() {
			return type == TypeExpr.PAR ? "%" + value.toString() : type == TypeExpr.SET ? "set()" : value.toString();
		}
	}
}
