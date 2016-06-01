package org.xcsp.parser;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.xcsp.parser.XEnums.TypeChild;
import org.xcsp.parser.XEnums.TypeCtr;
import org.xcsp.parser.XEnums.TypeExpr;
import org.xcsp.parser.XEnums.TypeMeasure;
import org.xcsp.parser.XEnums.TypeReification;
import org.xcsp.parser.XNodeExpr.XNodeParent;
import org.xcsp.parser.XParser.AnyEntry;
import org.xcsp.parser.XParser.Condition;
import org.xcsp.parser.XParser.ConditionVar;
import org.xcsp.parser.XVariables.XVar;

/** In this class, we find intern classes for managing stand-alone constraints, groups of constraints, and meta-constraints. */
public class XConstraints {

	/** The class used for representing parameters (tokens of the form %i or %...) when handling constraint templates. */
	public static final class XParameter {
		/** The number associated with the parameter. We have -1 for %..., 0 for %0, 1 for %1, and so on. */
		public final int number;

		public XParameter(int number) {
			this.number = number;
		}

		@Override
		public String toString() {
			return "%" + (number == -1 ? "..." : number);
		}
	}

	/** The class used for representing reification. */
	public static final class XReification {
		public final TypeReification type;

		/** The 0-1 variable used for reification */
		public final XVar var;

		public XReification(TypeReification type, XVar var) {
			this.type = type;
			this.var = var;
		}

		@Override
		public String toString() {
			return "Reification:" + var + " (" + type + ")";
		}
	}

	/** The class used for representing softening. */
	public static final class XSoftening {
		public final TypeMeasure type;

		public final String parameters;

		public final Condition condition;

		/** Used for soft table constraints. Otherwise, it is null. */
		public final Integer defaultCost;

		public XSoftening(TypeMeasure type, String parameters, Condition condition, Integer defaultCost) {
			this.type = type;
			this.parameters = parameters;
			this.condition = condition;
			this.defaultCost = defaultCost;
		}

		public XSoftening(TypeMeasure type, String parameters, Condition condition) {
			this(type, parameters, condition, null);
		}

		@Override
		public String toString() {
			return "Softening:" + condition + " (" + type + " " + parameters + " " + defaultCost + ")";
		}
	}

	/**
	 * The class used for handling abstraction in constraint templates. Currently, it is possible to manage any number of abstract childs that are either
	 * totally abstract or abstract functional. Note that a child is totally abstract iff it only contains parameters (tokens of the form %i or %...), and that
	 * an abstract functional child is a child which has 'function' as type and which contains at least one parameter. When for a child a single value is
	 * expected, %... cannot be used. %... stands for all effective parameters that come after the one corresponding to the highest encountered numbered
	 * parameter.
	 * */
	public static final class XAbstraction {
		/** The abstract child elements from the list of child elements of a constraint template. */
		public final CChild[] abstractChilds;

		/** The initial values of the abstract childs. We stored them because the values are lost after a first concretization */
		private Object[] abstractChildValues;

		/** The mappings to be used when concretizing */
		private int[][] mappings;

		private int highestParameterNumber;

		private int[] mappingFor(CChild child) {
			if (child.type == TypeChild.function)
				return null;
			if (child.value.getClass().isArray())
				return IntStream.range(0, Array.getLength(child.value)).map(i -> ((XParameter) Array.get(child.value, i)).number).toArray();
			// XUtility.control(((XParameter) child.value).number != -1, "%... forbidden when a single value is expected.");
			return new int[] { ((XParameter) child.value).number };
		}

		public XAbstraction(CChild... abstractChilds) {
			this.abstractChilds = abstractChilds;
			this.abstractChildValues = Stream.of(abstractChilds).map(child -> child.value).toArray();
			mappings = Stream.of(abstractChilds).map(child -> mappingFor(child)).toArray(int[][]::new);
			highestParameterNumber = Math.max(
					0,
					IntStream
							.range(0, abstractChilds.length)
							.map(i -> abstractChilds[i].type == TypeChild.function ? ((XNodeExpr) abstractChilds[i].value).maxParameterNumber() : IntStream
									.of(mappings[i]).max().getAsInt()).max().getAsInt());
		}

		private Object concreteValueFor(CChild child, Object abstractChildValue, Object[] args, int[] mapping) {
			if (child.type == TypeChild.function)
				return ((XNodeParent) abstractChildValue).concretizeWith(args);
			else if (child.value.getClass().isArray()) {
				List<Object> list = new ArrayList<>();
				for (int i = 0; i < mapping.length; i++)
					if (mapping[i] != -1)
						list.add(args[mapping[i]]);
					else
						for (int j = highestParameterNumber; j < args.length; j++)
							list.add(args[j]);
				return XUtility.specificArrayFrom(list);
			} else {
				XUtility.control(mapping.length == 1, "Pb here");
				// System.out.println("args=" + args + " " + child + " " + mapping.length + " " + mapping[0]);
				return args[mapping[0]];
			}
		}

		public void concretize(Object[] args) {
			IntStream.range(0, abstractChilds.length).forEach(
					i -> abstractChilds[i].value = concreteValueFor(abstractChilds[i], abstractChildValues[i], args, mappings[i]));
		}
	}

	/**
	 * The root class of any element that is a (direct or indirect) entry in <constraints>. Also used for child elements of constraints (and constraint
	 * templates).
	 */
	public static abstract class CEntry extends AnyEntry {

		/** The set of variables involved in this element. This is used as a cache (lazy initialization, as seen in method vars()). */
		private XVar[] vars;

		/** Returns the set of variables involved in this element. */
		public XVar[] vars() {
			if (this instanceof XCtr && ((XCtr) this).abstraction != null)
				return collectVars(new LinkedHashSet<>()).toArray(new XVar[0]);
			return vars != null ? vars : (vars = collectVars(new LinkedHashSet<>()).toArray(new XVar[0]));
		}

		/** Collect the set of variables involved in this element, and add them to the specified set. */
		public abstract Set<XVar> collectVars(Set<XVar> set);

		/** Returns true iff this element is subject to abstraction, i.e., contains parameters (tokens of the form %i or %...). */
		public abstract boolean subjectToAbstraction();

		@Override
		public String toString() {
			return "(" + (attributes == null ? "" : XUtility.join(attributes, ":", " ")) + ")";
		}
	}

	/** The class used for elements <block>. */
	public static final class XBlock extends CEntry {
		/** The list of elements contained in this block. */
		public List<CEntry> subentries = new ArrayList<>();

		public XBlock(List<CEntry> subentries) {
			this.subentries = subentries;
		}

		@Override
		public Set<XVar> collectVars(Set<XVar> set) {
			subentries.stream().forEach(e -> e.collectVars(set));
			return set;
		}

		@Override
		public boolean subjectToAbstraction() {
			return subentries.stream().anyMatch(e -> e.subjectToAbstraction());
		}

		@Override
		public String toString() {
			return "Block " + super.toString() + " " + subentries.stream().map(e -> e.toString()).collect(Collectors.joining("\n"));
		}
	}

	/** The class for representing a group of constraints. */
	public final static class XGroup extends CEntry {
		/**
		 * The constraint template for the group or meta-constraint slide. It is either a stand-alone constraint template or an element <not> containing a
		 * stand-alone constraint template.
		 */
		public final CEntryReifiable template;

		/** A two-dimensional array representing the sequence of arguments that have to be passed to the template. */
		public final Object[][] argss;

		/** The scope of each constraint of the group. This is used as a cache (lazy initialization, as seen in method getScope(i)). */
		private XVar[][] scopes;

		/** Returns the scope of the ith constraint of the group. */
		public XVar[] getScope(int i) {
			if (scopes == null)
				scopes = new XVar[argss.length][];
			if (scopes[i] != null)
				return scopes[i];
			return scopes[i] = XUtility.collectVarsIn(argss[i], new LinkedHashSet<>(Arrays.asList(template.vars()))).toArray(new XVar[0]);
		}

		public XGroup(CEntryReifiable template, Object[][] argss) {
			this.template = template;
			this.argss = argss;
		}

		@Override
		public Set<XVar> collectVars(Set<XVar> set) {
			template.collectVars(set);
			Stream.of(argss).forEach(t -> XUtility.collectVarsIn(t, set));
			return set;
		}

		@Override
		public boolean subjectToAbstraction() {
			return true;
		}

		@Override
		public String toString() {
			return "Group " + super.toString() + "\n" + template.toString() + "\n\t" + XUtility.join(argss, "\n\t", " ");
		}
	}

	/** The class for representing any entry that is reifiable and softable (i.e., an entry that is not a <block>, a group or a child for a constraint). */
	public abstract static class CEntryReifiable extends CEntry {
		/** The object denoting reification. Of course, it is null if the entry is not (half) reified. */
		public XReification reification;

		/** The object denoting softening (type "soft' with element <cost>). Of course, it is null if the entry is not relaxed/softened. */
		public XSoftening softening;

		@Override
		public Set<XVar> collectVars(Set<XVar> set) {
			if (reification != null)
				set.add(reification.var);
			if (softening != null && softening.condition instanceof ConditionVar)
				set.add(((ConditionVar) softening.condition).x);
			return set;
		}

		@Override
		public String toString() {
			return super.toString() + (reification != null ? "\n\t" + reification : "") + (softening != null ? "\n\t" + softening : "");
		}
	}

	/** The class for representing a stand-alone constraint, or a constraint template. */
	public final static class XCtr extends CEntryReifiable {
		/** The type of the constraint. For example, it may be intension, extension, or regular. */
		public final TypeCtr type;

		/** Returns the type of the constraint. For example, it may be intension, extension, or regular. We need an accessor for Scala. **/
		public final TypeCtr getType() {
			return type;
		}

		/** The child elements of the constraint. For example, we have a first child for <list> and a second child for <transitions> if the constraint is <mdd>. */
		public final CChild[] childs;

		/** The object for handling abstraction. Of course, it is null if the constraint is not abstract, i.e., is not a constraint template. */
		public XAbstraction abstraction;

		/** Build an object representing a stand-alone constraint (template). */
		protected XCtr(TypeCtr type, CChild... childs) {
			this.type = type;
			this.childs = childs;
			int[] abstractChildsPositions = IntStream.range(0, childs.length).filter(i -> childs[i].subjectToAbstraction()).toArray();
			if (abstractChildsPositions.length > 0) {
				XUtility.control(
						IntStream.of(abstractChildsPositions).mapToObj(i -> childs[i])
								.allMatch(child -> child.type == TypeChild.function || child.isTotallyAbstract()), "Abstraction Form not handled");
				abstraction = new XAbstraction(IntStream.of(abstractChildsPositions).mapToObj(i -> childs[i]).toArray(CChild[]::new));
			}
		}

		@Override
		public Set<XVar> collectVars(Set<XVar> set) {
			Stream.of(childs).forEach(child -> child.collectVars(set));
			return super.collectVars(set);
		}

		@Override
		public boolean subjectToAbstraction() {
			return abstraction != null;
		}

		@Override
		public String toString() {
			return type + super.toString() + "\n\t" + XUtility.join(childs, "\n\t");
		}
	}

	/** The class for representing the meta-constraint <slide>. */
	public final static class XSlide extends CEntryReifiable {

		/** Builds the scopes of the constraints involved in the meta-constraint. */
		public static XVar[][] buildScopes(XVar[][] varsOfLists, int[] offset, int[] collect, boolean circular) {
			int[] indexes = new int[collect.length];
			List<XVar[]> list = new ArrayList<>();
			XVar[] tmp = new XVar[Arrays.stream(collect).sum()];
			while (true) {
				if (!circular && indexes[0] + collect[0] > varsOfLists[0].length)
					break;
				boolean lastTurn = indexes[0] + offset[0] >= varsOfLists[0].length;
				for (int i = 0, cnt = 0; i < collect.length; i++) {
					for (int j = 0; j < collect[i]; j++)
						tmp[cnt++] = varsOfLists[i][(indexes[i] + j) % varsOfLists[i].length];
					indexes[i] += offset[i];
				}
				list.add(tmp.clone());
				if (lastTurn)
					break;
			}
			return list.toArray(new XVar[list.size()][]);
		}

		/** The sequence of child elements <list>. Usually, only one such child. */
		public final CChild[] lists;

		/** The values of the attributes offset and collect for each list. */
		public final int[] offsets, collects;
		/**
		 * The constraint template for the group or meta-constraint slide. It is either a stand-alone constraint template or an element <not> containing a
		 * stand-alone constraint template.
		 */
		public final CEntryReifiable template;

		/** A two-dimensional array representing the scopes of the slided constraints. */
		public final XVar[][] scopes;

		public XSlide(CChild[] lists, int[] offsets, int[] collects, XCtr template, XVar[][] scopes) {
			this.lists = lists;
			this.offsets = offsets;
			this.collects = collects;
			this.template = template;
			this.scopes = scopes;
		}

		@Override
		public Set<XVar> collectVars(Set<XVar> set) {
			Stream.of(lists).forEach(t -> t.collectVars(set));
			template.collectVars(set);
			return super.collectVars(set);
		}

		@Override
		public boolean subjectToAbstraction() {
			return true;
		}

		@Override
		public String toString() {
			return super.toString() + "\n\t" + XUtility.join(lists, "\n\t") + "\n\tcollect=" + Arrays.toString(collects) + " offset="
					+ Arrays.toString(offsets);
		}
	}

	/** The class for representing the meta-constraint <seqbin>. */
	public final static class XSeqbin extends CEntryReifiable {

		/** The child element <list> of the meta-constraint. */
		public final CChild list;

		/** The two constraint templates for the meta-constraint. The first one is hard, and the second one can be violated. */
		public final XCtr template1, template2;

		/** The child element used for counting the number of violations. Its value is either an object Long or Var. */
		public final CChild number;

		public final XVar[][] scopes;

		public XSeqbin(CChild list, XCtr template1, XCtr template2, CChild number, XVar[][] scopes) {
			this.list = list;
			this.template1 = template1;
			this.template2 = template2;
			this.number = number;
			this.scopes = scopes;
		}

		@Override
		public Set<XVar> collectVars(Set<XVar> set) {
			list.collectVars(set);
			template1.collectVars(set);
			template2.collectVars(set);
			if (number.value instanceof XVar)
				set.add(((XVar) number.value));
			return super.collectVars(set);
		}

		@Override
		public boolean subjectToAbstraction() {
			return true;
		}

		@Override
		public String toString() {
			return "seqbin" + super.toString() + "\n\t" + list + "\n\t" + template1.toString() + "\n\t" + template2.toString() + "\n\t" + number + " "
					+ "\n\tscopes=" + XUtility.arrayToString(scopes);
		}
	}

	/** The class for representing a logical meta-constraint <and>, <or> or <not>. */
	public final static class XLogic extends CEntryReifiable {

		/** The type of the meta-constraint. */
		private final TypeCtr type;

		/** Returns the type of the meta-constraint. We need an accessor for Scala. **/
		public final TypeCtr getType() {
			return type;
		}

		/** The components involved in the logical meta-constraint. Usually, these components are stand-alone constraints. */
		public final CEntryReifiable[] components;

		public XLogic(TypeCtr type, CEntryReifiable... components) {
			this.type = type;
			this.components = components;
			XUtility.control(type.isLogical() && (type != TypeCtr.not || components.length == 1), "Bad logic construction");
		}

		@Override
		public Set<XVar> collectVars(Set<XVar> set) {
			Stream.of(components).forEach(c -> c.collectVars(set));
			return super.collectVars(set);
		}

		@Override
		public boolean subjectToAbstraction() {
			return Arrays.stream(components).anyMatch(c -> c.subjectToAbstraction());
		}

		@Override
		public String toString() {
			return type + super.toString() + "\n" + XUtility.join(components, "\n");
		}
	}

	/**
	 * The class for representing a child element of a constraint (or constraint template). For example, it is used to represent an element <list> or an element
	 * <supports>.
	 */
	public static final class CChild extends CEntry {

		/** The type of the child. For example list, supports, or transitions. */
		public final TypeChild type;

		/** Returns the type of the child. For example list, supports, or transitions. We need an accessor for Scala. **/
		public final TypeChild getType() {
			return type;
		}

		/**
		 * The value of the child. It is actually the parsed textual content of the child. After parsing, it may be a variable, an integer, an array of
		 * variables, an array of parameters ...
		 */
		public Object value;

		/**
		 * Build an object representing a child element of a constraint (template). The specified type corresponds to the tag name of the child, and the value
		 * corresponds to the parsed textual content of the child.
		 */
		protected CChild(TypeChild type, Object value) {
			this.type = type;
			this.value = value;
		}

		/** Returns true iff a set variable is involved in the (value field of the) element. */
		public boolean setVariableInvolved() {
			return XUtility.check(value, obj -> (obj instanceof XVar && ((XVar) obj).type.isSet()));
		}

		@Override
		public Set<XVar> collectVars(Set<XVar> set) {
			return XUtility.collectVarsIn(value, set);
		}

		@Override
		public boolean subjectToAbstraction() {
			if (type == TypeChild.function && ((XNodeExpr) value).canFindleafWith(TypeExpr.PAR))
				return true;
			return XUtility.check(value, obj -> obj instanceof XParameter); // check if a parameter somewhere inside the value
		}

		/** Returns true iff the value of the child only contains parameters (tokens of the form %i or %...). */
		public boolean isTotallyAbstract() {
			if (!value.getClass().isArray())
				return value instanceof XParameter;
			int size = Array.getLength(value);
			return size > 0 && IntStream.range(0, size).allMatch(i -> ((XParameter) Array.get(value, i)) instanceof XParameter);
		}

		@Override
		public String toString() {
			return type + super.toString() + " : " + (value == null ? "" : value.getClass().isArray() ? XUtility.arrayToString(value) : value);
		}
	}
}
