package org.chocosolver.parser.xcsp.tools;

import org.chocosolver.parser.xcsp.tools.XEnums.TypeAtt;
import org.chocosolver.parser.xcsp.tools.XEnums.TypeChild;
import org.chocosolver.parser.xcsp.tools.XEnums.TypeCtr;
import org.chocosolver.parser.xcsp.tools.XEnums.TypeExpr;
import org.chocosolver.parser.xcsp.tools.XVariables.Var;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;

import java.lang.reflect.Array;
import java.util.*;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * In this class, we find intern classes for managing stand-alone constraints, groups of constraints, and meta-constraints.
 */
public class XConstraints {


    /**
     * The class used for representing parameters (tokens of the form %i or %...) when handling constraint templates.
     */
    public static final class Parameter {
        /**
         * The number associated with the parameter. We have -1 for %..., 0 for %0, 1 for %1, and so on.
         */
        public final int number;

        public Parameter(int number) {
            this.number = number;
        }

        @Override
        public String toString() {
            return "%" + (number == -1 ? "..." : number);
        }
    }

    /**
     * The interface used for handling abstraction in constraint templates.
     */
    private static interface Abstraction {
    }

    /**
     * The class for basic abstraction. It is used for constraint templates when only one child element is abstract (i.e., contains parameters). In that case,
     * either the constraint is <intension> (and parameters can be anywhere in the predicate expression) or the unique abstract child element must have the form
     * %..., or %0 %1 ... %r, or %0 %1 ... %i %...
     */
    public static final class AbstractionBasic implements Abstraction {
        /**
         * The position of the unique abstract child element (in basic form) in the list of child elements of a constraint template.
         */
        public final int abstractChildPosition;

        protected AbstractionBasic(int abstractChildPosition) {
            this.abstractChildPosition = abstractChildPosition;
        }
    }

    /**
     * The class for any complex (i.e., not basic) form of abstraction. In practice, should be very rare. To be written
     */
    public static final class AbstractionComplex implements Abstraction {
        // TODO
    }

    /**
     * The root class of any element that is an entry in <constraints> (except for <block>) or of a child element of a constraint (template).
     */
    private static abstract class Root {

        /**
         * The attributes that are associated with the element. For example, we can have pairs such as (startIndex,integer) or (reifiedBy,variable). We just
         * collect XMl attributes into a map (using an enum type for keys, and String for values). Values will have to be handled by the parser (for example, by
         * using Integer.parseInt for an integer), which is rather immediate.
         */
        public final Map<TypeAtt, String> attributes = new HashMap<>();

        /**
         * The set of variables involved in the element. This is used as a cache (lazy initialization, as seen in method getVars()).
         */
        private Var[] vars;

        /**
         * Returns the set of variables involved in the element.
         */
        public Var[] getVars() {
            return vars != null ? vars : (vars = collectVars(new LinkedHashSet<>()).toArray(new Var[0]));
        }

        /**
         * Collect the set of variables involved in the element, and add them to the specified set.
         */
        public abstract Set<Var> collectVars(Set<Var> set);

        /**
         * Collect the XMl attributes of the specified element into a map (using an enum type for keys, and String for values).
         */
        public void copyAttributesOf(Element elt) {
            NamedNodeMap al = elt.getAttributes();
            IntStream.range(0, al.getLength()).forEach(i -> attributes.put(TypeAtt.valOf(al.item(i).getNodeName()), al.item(i).getNodeValue()));
        }

        /**
         * Returns true iff the element is subject to abstraction, i.e., contains parameters (tokens of the form %i or %...).
         */
        public abstract boolean subjectToAbstraction();

        @Override
        public String toString() {
            return "(" + (attributes == null ? "" : XUtility.join(attributes, ":", " ")) + ")";
        }
    }

    /**
     * The class for representing a child element of a constraint (template). It is used e.g. for representing an element <list> or an element <supports>.
     */
    public static final class Child extends Root {

        /**
         * The type of the child. For example list, supports, or transitions.
         */
        public final TypeChild type;

        /**
         * The value of the child. It is actually the parsed textual content of the child. After parsing, it may be a variable, an integer, an array of
         * variables, an array of parameters ...
         */
        public final Object value;

        /**
         * Build an object representing a child element of a constraint (template). The specified type corresponds to the tag name of the child, and the value
         * corresponds to the parsed textual content of the child.
         */
        protected Child(TypeChild type, Object value) {
            this.type = type;
            this.value = value;
        }

        /**
         * Returns true iff a set variable is involved in the (value field of the) element.
         */
        public boolean setVariableInvolved() {
            return XUtility.check(value, obj -> (obj instanceof Var && ((Var) obj).type.isSet()));
        }

        @Override
        public Set<Var> collectVars(Set<Var> set) {
            return XUtility.collectVarsIn(value, set);
        }

        @Override
        public boolean subjectToAbstraction() {
            return XUtility.check(value, obj -> obj instanceof Parameter);
        }

        /**
         * Returns true iff the value of the child has the form %..., or %0 %1 ... %r, or %0 %1 ... %i %..., which we call candidate for basic abstraction.
         */
        public boolean isCandidateForBasicAbstraction() {
            if (!(value instanceof Object[]))
                return value instanceof Parameter && (((Parameter) value).number == -1 || ((Parameter) value).number == 0);
            int size = Array.getLength(value);
            if (size == 0
                    || IntStream.range(0, size - 1).anyMatch(i -> !(Array.get(value, i) instanceof Parameter) || ((Parameter) Array.get(value, i)).number != i))
                return false;
            Object last = Array.get(value, size - 1);
            return last instanceof Parameter && (((Parameter) last).number == -1 || (((Parameter) last).number == size - 1));
        }

        @Override
        public String toString() {
            return type + super.toString() + " : " + (value == null ? "" : value.getClass().isArray() ? XUtility.arrayToString(value) : value.toString());
        }
    }

    /**
     * The class for representing any entry in <constraints>, except for <block>.
     */
    public abstract static class Entry extends Root {
        /**
         * The variable associated with the entry, in case of (half) reification). Of course, it is null if the entry is not (half) reified.
         */
        public Var reificationVar;

        /**
         * The object representing the element <cost>, in case of relaxation. Of course, it is null if the entry is nor relaxed/softened.
         */
        public Child cost;

        /**
         * Returns the name of the variable used for (half) reification, or null.
         */
        public String getNameOfReificationVar() {
            return attributes.getOrDefault(TypeAtt.reifiedBy, attributes.getOrDefault(TypeAtt.hreifiedFrom, attributes.get(TypeAtt.hreifiedTo)));
        }

        /**
         * Returns the form of reification: full reification (reifiedBy), half reification (hreifiedFrom or hreifiedTo) or null (if no reification).
         */
        public TypeAtt getReificationForm() {
            return attributes.containsKey(TypeAtt.reifiedBy) ? TypeAtt.reifiedBy : attributes.containsKey(TypeAtt.hreifiedFrom) ? TypeAtt.hreifiedFrom
                    : attributes.containsKey(TypeAtt.hreifiedTo) ? TypeAtt.hreifiedTo : null;
        }

        @Override
        public Set<Var> collectVars(Set<Var> set) {
            if (reificationVar != null)
                set.add(reificationVar);
            if (cost != null)
                cost.collectVars(set);
            return set;
        }
    }

    /**
     * The class for representing a stand-alone constraint.
     */
    public final static class Ctr extends Entry {
        /**
         * The type of the constraint. For example intension, extension, or regular.
         */
        public final TypeCtr type;

        /**
         * The child elements of the constraint. For example, we have a first child for <list> and a second child for <transitions> if the constraint is <mdd>.
         */
        public final Child[] childs;

        /**
         * The object for handling abstraction. Of course, it is null if the constraint is not abstract, i.e., is not a constraint template.
         */
        public Abstraction abstraction;

        /**
         * Build an object representing a stand-alone constraint (template).
         */
        protected Ctr(TypeCtr type, Child... childs) {
            this.type = type;
            this.childs = childs;
            if (type == TypeCtr.intension) {
                if (((XNodeExpr) (childs[0].value)).canFindleafWith(TypeExpr.PAR))
                    abstraction = new AbstractionBasic(0);
            } else
                for (int i = 0; i < childs.length; i++)
                    if (childs[i].isCandidateForBasicAbstraction()) {
                        XUtility.control(abstraction == null, "Complex forms of abstraction (including multi basic abstractions) not currently handled");
                        abstraction = new AbstractionBasic(i);
                    } else
                        XUtility.control(!childs[i].subjectToAbstraction(), "Complex abstraction forms not implemented");
        }

        @Override
        public Set<Var> collectVars(Set<Var> set) {
            Stream.of(childs).forEach(child -> child.collectVars(set));
            return super.collectVars(set);
        }

        public int getBasicAbstractionPosition() {
            return !(abstraction instanceof AbstractionBasic) ? -1 : ((AbstractionBasic) abstraction).abstractChildPosition;
        }

        @Override
        public boolean subjectToAbstraction() {
            return abstraction != null;
        }

        @Override
        public String toString() {
            return type + super.toString() + "\n\t" + XUtility.join(childs, "\n\t") + (cost == null ? "" : "\n\t" + cost.toString());
        }
    }

    /**
     * The class for representing a group of constraints.
     */
    public final static class Group extends Entry {

        /**
         * The constraint template for the group. It is either a stand-alone constraint template or an element <not> containing a stand-alone constraint
         * template.
         */
        public final Entry template;

        /**
         * A two-dimensional array representing the sequence of arguments.
         */
        public final Object[][] argss;

        /**
         * The scope of each constraint of the group. This is used as a cache (lazy initialization, as seen in method getScope(i)).
         */
        private Var[][] scopes;

        /**
         * Returns the scope of the ith constraint of the group.
         */
        public Var[] getScope(int i) {
            if (scopes == null)
                scopes = new Var[argss.length][];
            if (scopes[i] != null)
                return scopes[i];
            return scopes[i] = XUtility.collectVarsIn(argss[i], new LinkedHashSet<>(Arrays.asList(template.getVars()))).toArray(new Var[0]);
        }

        public Group(Entry template, Object[][] argss) {
            this.template = template;
            this.argss = argss;
        }

        @Override
        public Set<Var> collectVars(Set<Var> set) {
            template.collectVars(set);
            Stream.of(argss).forEach(t -> XUtility.collectVarsIn(t, set));
            return super.collectVars(set);
        }

        @Override
        public boolean subjectToAbstraction() {
            return true;
        }

        @Override
        public String toString() {
            return "group" + super.toString() + "\n" + template.toString() + "\n\t" + XUtility.join(argss, "\n\t", " ")
                    + (cost == null ? "" : "\n\t" + cost.toString());
        }
    }

    /**
     * The class for representing the meta-constraint <slide>.
     */
    public final static class Slide extends Entry {

        /**
         * Builds the scopes of the constraints involved in the meta-constraint.
         */
        public static Var[][] buildScopes(Var[][] varsOfLists, int[] offset, int[] collect, boolean circular) {
            int[] indexes = new int[collect.length];
            List<Var[]> list = new ArrayList<>();
            Var[] tmp = new Var[Arrays.stream(collect).sum()];
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
            return list.toArray(new Var[list.size()][]);
        }

        /**
         * The sequence of child elements <list>. Usually, only one such child.
         */
        public final Child[] lists;

        /**
         * The values of the attributes offset and collect for each list.
         */
        public final int[] offset, collect;

        /**
         * The constraint template for slide. It is a stand-alone constraint template.
         */
        public final Ctr template;

        /**
         * A two-dimensional array representing the scopes of the constraints involved in the meta-constraint.
         */
        public final Var[][] scopes;

        public Slide(Child[] lists, int[] offset, int[] collect, Ctr template, Var[][] scopes) {
            this.lists = lists;
            this.offset = offset;
            this.collect = collect;
            this.template = template;
            this.scopes = scopes;
        }

        @Override
        public Set<Var> collectVars(Set<Var> set) {
            Stream.of(lists).forEach(t -> XUtility.collectVarsIn(t, set));
            template.collectVars(set);
            return super.collectVars(set);
        }

        @Override
        public boolean subjectToAbstraction() {
            return true;
        }

        @Override
        public String toString() {
            return "slide" + super.toString() + "\n\t" + XUtility.join(lists, "\n\t") + "\n\tcollect=" + Arrays.toString(collect) + " offset="
                    + Arrays.toString(offset) + "\n\t" + template.toString() + "\n\tscopes=" + XUtility.arrayToString(scopes)
                    + (cost == null ? "" : "\n\t" + cost.toString());
        }
    }

    /**
     * The class for representing the meta-constraint <seqbin>.
     */
    public final static class Seqbin extends Entry {

        /**
         * The child element <list> of the meta-constraint.
         */
        public final Child list;

        /**
         * The two constraint templates for the meta-constraint. The first one is hard, and the second one can be violated.
         */
        public final Ctr template1, template2;

        /**
         * The child element used for counting the number of violations. Its value is either an object Long or Var.
         */
        public final Child number;

        public final Var[][] scopes;

        public Seqbin(Child list, Ctr template1, Ctr template2, Child number, Var[][] scopes) {
            this.list = list;
            this.template1 = template1;
            this.template2 = template2;
            this.number = number;
            this.scopes = scopes;
        }

        @Override
        public Set<Var> collectVars(Set<Var> set) {
            XUtility.collectVarsIn(list, set);
            template1.collectVars(set);
            template2.collectVars(set);
            if (number.value instanceof Var)
                set.add(((Var) number.value));
            return super.collectVars(set);
        }

        @Override
        public boolean subjectToAbstraction() {
            return true;
        }

        @Override
        public String toString() {
            return "seqbin" + super.toString() + "\n\t" + list + "\n\t" + template1.toString() + "\n\t" + template2.toString() + "\n\t" + number + " "
                    + "\n\tscopes=" + XUtility.arrayToString(scopes) + (cost == null ? "" : "\n\t" + cost.toString());
        }
    }

    /**
     * The class for representing a logical meta-constraint <and>, <or> or <not>.
     */
    public final static class Logic extends Entry {

        /**
         * The type of the meta-constraint.
         */
        public final TypeCtr type;

        /**
         * The components involved in the logical meta-constraint. Usually, these components are stand-alone constraints.
         */
        public final Entry[] components;

        public Logic(TypeCtr type, Entry... components) {
            this.type = type;
            this.components = components;
            XUtility.control(type.isLogical() && (type != TypeCtr.not || components.length == 1), "Bad logic construction");
        }

        @Override
        public Set<Var> collectVars(Set<Var> set) {
            Stream.of(components).forEach(c -> c.collectVars(set));
            return super.collectVars(set);
        }

        @Override
        public boolean subjectToAbstraction() {
            return Arrays.stream(components).anyMatch(c -> c.subjectToAbstraction());
        }

        @Override
        public String toString() {
            return type + super.toString() + "\n" + XUtility.join(components, "\n") + (cost == null ? "" : "\n" + cost.toString());
        }
    }
}
