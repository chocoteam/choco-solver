/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.variables;

import org.chocosolver.solver.ISelf;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.variables.view.RealView;
import org.chocosolver.solver.variables.view.bool.BoolEqView;
import org.chocosolver.solver.variables.view.bool.BoolLeqView;
import org.chocosolver.solver.variables.view.bool.BoolNotView;
import org.chocosolver.solver.variables.view.bool.BoolSetView;
import org.chocosolver.solver.variables.view.graph.directed.DirectedEdgeInducedSubgraphView;
import org.chocosolver.solver.variables.view.graph.directed.DirectedGraphUnionView;
import org.chocosolver.solver.variables.view.graph.directed.DirectedNodeInducedSubgraphView;
import org.chocosolver.solver.variables.view.graph.undirected.EdgeInducedSubgraphView;
import org.chocosolver.solver.variables.view.graph.undirected.NodeInducedSubgraphView;
import org.chocosolver.solver.variables.view.graph.undirected.UndirectedGraphUnionView;
import org.chocosolver.solver.variables.view.integer.IntMinusView;
import org.chocosolver.solver.variables.view.integer.IntOffsetView;
import org.chocosolver.solver.variables.view.integer.IntScaleView;
import org.chocosolver.solver.variables.view.set.*;
import org.chocosolver.util.objects.graphs.IGraph;
import org.chocosolver.util.objects.setDataStructures.ISet;

import java.util.Arrays;

import static java.lang.Math.max;

/**
 * Interface to make views (BoolVar, IntVar, RealVar and SetVar)
 *
 * A kind of factory relying on interface default implementation to allow (multiple) inheritance
 *
 * @author Jean-Guillaume FAGES
 */
public interface IViewFactory extends ISelf<Model> {

    //*************************************************************************************
    // BOOLEAN VARIABLES
    //*************************************************************************************

    /**
     * Creates a view over <i>bool</i> holding the logical negation of <i>bool</i> (ie, &not;BOOL).
     * @param bool a boolean variable.
     * @return a BoolVar equal to <i>not(bool)</i> (or 1-bool)
     */
    default BoolVar boolNotView(BoolVar bool) {
        if (bool.hasNot()) {
            return bool.not();
        } else {
            BoolVar not;
            if (bool.isInstantiated()) {
                not = bool.getValue() == 1 ? ref().boolVar(false) : ref().boolVar(true);
            } else {
                if (ref().getSettings().enableViews()) {
                    not = new BoolNotView<>(bool);
                } else {
                    not = ref().boolVar("not(" + bool.getName() + ")");
                    ref().arithm(not, "!=", bool).post();
                }
                not.setNot(true);
            }
            bool._setNot(not);
            not._setNot(bool);
            return not;
        }
    }

    /**
     * Creates a boolean view b over a set variable S such that:
     *
     * given v an integer, b = true iff S contains v.
     *
     * @param setVar The set variable to observe.
     * @param v The value to observe in the set variable.
     * @return A boolvar equals to S.contains(v).
     */
    default BoolVar setBoolView(SetVar setVar, int v) {
        return new BoolSetView<>(v, setVar);
    }

    /**
     * Creates an array of boolean views b over a set variable S such that:
     *
     * b[i - offset] = true <=> i in S.
     *
     * @param setVar The set variable to observe
     * @param size The size of the bool var array
     * @param offset The offset
     * @return A boolvar array such that b[i - offset] = true <=> i in S.
     */
    default BoolVar[] setBoolsView(SetVar setVar, int size, int offset) {
        BoolVar[] bools = new BoolVar[size];
        for (int i = 0; i < size; i++) {
            bools[i] = setBoolView(setVar, i + offset);
        }
        return bools;
    }

    //*************************************************************************************
    // INTEGER VARIABLES
    //*************************************************************************************

    /**
     * Creates a view based on <i>var</i>, equal to <i>var+cste</i>.
     * @param var  an integer variable
     * @param cste a constant (can be either negative or positive)
     * @return an IntVar equal to <i>var+cste</i>
     */
    default IntVar intOffsetView(IntVar var, int cste) {
        if (cste == 0) {
            return var;
        }
        String name = "(" + var.getName() + (cste >= 0 ? "+" : "-") + Math.abs(cste) + ")";
        if (var.isInstantiated()) {
            return ref().intVar(name, var.getValue() + cste);
        }
        if (ref().getSettings().enableViews()) {
            int p = checkDeclaredView(var, cste, IntOffsetView.class, ref().getSettings().checkDeclaredViews());
            if(p>-1){
                return var.getView(p).asIntVar();
            }else {
                return new IntOffsetView<>(var, cste);
            }
        } else {
            int lb = var.getLB() + cste;
            int ub = var.getUB() + cste;
            IntVar ov;
            if (var.hasEnumeratedDomain()) {
                ov = ref().intVar(name, lb, ub, false);
            } else {
                ov = ref().intVar(name, lb, ub, true);
            }
            ref().arithm(ov, "-", var, "=", cste).post();
            return ov;
        }
    }

    /**
     * Creates a view over <i>var</i> equal to -<i>var</i>.
     * That is if <i>var</i> = [a,b], then this = [-b,-a].
     *
     * @param var an integer variable
     * @return an IntVar equal to <i>-var</i>
     */
    default IntVar intMinusView(IntVar var) {
        if (var.isInstantiated()) {
            return ref().intVar(-var.getValue());
        }
        if (ref().getSettings().enableViews()) {
            if (var instanceof IntMinusView) {
                //noinspection rawtypes
                return ((IntMinusView) var).getVariable();
            } else {
                int p = checkDeclaredView(var, -1, IntMinusView.class, ref().getSettings().checkDeclaredViews());
                if(p>-1){
                    return var.getView(p).asIntVar();
                }else {
                    return new IntMinusView<>(var);
                }
            }
        } else {
            int ub = -var.getLB();
            int lb = -var.getUB();
            String name = "-(" + var.getName() + ")";
            IntVar ov;
            if (var.hasEnumeratedDomain()) {
                ov = ref().intVar(name, lb, ub, false);
            } else {
                ov = ref().intVar(name, lb, ub, true);
            }
            ref().arithm(ov, "+", var, "=", 0).post();
            return ov;
        }
    }

    /**
     * Creates a view over <i>var</i> equal to <i>var*cste</i>.
     * Requires <i>cste</i> > -2
     * <p>
     * <br/>- if <i>cste</i> &lt; -1, throws an exception;
     * <br/>- if <i>cste</i> = -1, returns a minus view;
     * <br/>- if <i>cste</i> = 0, returns a fixed variable;
     * <br/>- if <i>cste</i> = 1, returns <i>var</i>;
     * <br/>- otherwise, returns a scale view;
     * <p>
     * @param var  an integer variable
     * @param cste a constant.
     * @return an IntVar equal to <i>var*cste</i>
     */
    default IntVar intScaleView(IntVar var, int cste) {
        if (cste == -1) {
            return intMinusView(var);
        }
        IntVar v2;
        if (cste == 0) {
            v2 = ref().intVar(0);
        } else if (cste == 1) {
            v2 = var;
        } else {
            if (var.isInstantiated()) {
                return ref().intVar(var.getValue() * cste);
            }
            if (ref().getSettings().enableViews()) {
                boolean rev = cste < 0;
                cste = Math.abs(cste);
                int p = checkDeclaredView(var, cste, IntScaleView.class, ref().getSettings().checkDeclaredViews());
                if(p>-1){
                    return var.getView(p).asIntVar();
                }else {
                    v2 = new IntScaleView<>(var, cste);
                }
                if(rev){
                    v2 = intMinusView(v2);
                }
        } else {
                int lb, ub;
                if (cste > 0) {
                    lb = var.getLB() * cste;
                    ub = var.getUB() * cste;
                } else {
                    lb = var.getUB() * cste;
                    ub = var.getLB() * cste;
                }
                String name = "(" + var.getName() + "*" + cste + ")";
                IntVar ov;
                if (var.hasEnumeratedDomain()) {
                    ov = ref().intVar(name, lb, ub, false);
                } else {
                    ov = ref().intVar(name, lb, ub, true);
                }
                ref().times(var, cste, ov).post();
                return ov;
            }
        }
        return v2;
    }

    /**
     * Creates a view over <i>var</i> such that: |<i>var</i>|.
     * <p>
     * <br/>- if <i>var</i> is already instantiated, returns a fixed variable;
     * <br/>- if the lower bound of <i>var</i> is greater or equal to 0, returns <i>var</i>;
     * <br/>- if the upper bound of <i>var</i> is less or equal to 0, return a minus view;
     * <br/>- otherwise, returns an absolute view;
     * <p>
     * @param var an integer variable.
     * @return an IntVar equal to the absolute value of <i>var</i>
     */
    default IntVar intAbsView(IntVar var) {
        if (var.isInstantiated()) {
            return ref().intVar(Math.abs(var.getValue()));
        } else if (var.getLB() >= 0) {
            return var;
        } else if (var.getUB() <= 0) {
            return intMinusView(var);
        } else {
            int ub = max(-var.getLB(), var.getUB());
            String name = "|" + var.getName() + "|";
            IntVar abs;
            if (var.hasEnumeratedDomain()) {
                abs = ref().intVar(name, 0, ub, false);
            } else {
                abs = ref().intVar(name, 0, ub, true);
            }
            ref().absolute(abs, var).post();
            return abs;
        }
    }

    /**
     * Creates an affine view over <i>x</i> such that: <i>a.x + b</i>.
     * <p>
     *
     * @param a a coefficient
     * @param x an integer variable.
     * @param b a constant
     * @return an IntVar equal to the absolute value of <i>var</i>
     */
    default IntVar intAffineView(int a, IntVar x, int b) {
        if (x.isInstantiated()) {
            return ref().intVar(a * x.getValue() + b);
        } else {
            return intOffsetView(intScaleView(x, a), b);
        }
    }


    /**
     * Creates an view over <i>x</i> such that: <i>(x = c) &hArr; b</i>.
     * <p>
     * @param x an integer variable.
     * @param c a constant
     * @return a BoolVar that reifies <i>x = c</i>
     */
    default BoolVar intEqView(IntVar x, int c) {
        if (x.isInstantiatedTo(c)) {
            return ref().boolVar(true);
        } else if (!x.contains(c)) {
            return ref().boolVar(false);
        } else {
            if (ref().getSettings().enableViews()) {
                int p = checkDeclaredView(x, c, BoolEqView.class, ref().getSettings().checkDeclaredViews());
                if (p >= 0) {
                    return x.getView(p).asBoolVar();
                } else {
                    return new BoolEqView<>(x, c);
                }
            }else{
                BoolVar b = ref().boolVar();
                ref().reifyXeqC(x, c, b);
                return b;
            }
        }
    }

    /**
     * Creates an view over <i>x</i> such that: <i>(x != c) &hArr; b</i>.
     * <p>
     * @param x an integer variable.
     * @param c a constant
     * @return a BoolVar that reifies <i>x != c</i>
     */
    default BoolVar intNeView(IntVar x, int c) {
        if (x.isInstantiatedTo(c)) {
            return ref().boolVar(false);
        } else if (!x.contains(c)) {
            return ref().boolVar(true);
        } else {
            if (ref().getSettings().enableViews()) {
                int p = checkDeclaredView(x, c, BoolEqView.class, ref().getSettings().checkDeclaredViews());
                if (p >= 0) {
                    return x.getView(p).asBoolVar().not();
                } else {
                    return new BoolEqView<>(x, c).not();
                }
            } else {
                BoolVar b = ref().boolVar();
                ref().reifyXneC(x, c, b);
                return b;
            }
        }
    }

    /**
     * Creates an view over <i>x</i> such that: <i>(x &le; c) &hArr; b</i>.
     * <p>
     * @param x an integer variable.
     * @param c a constant
     * @return a BoolVar that reifies <i>x &le; c</i>
     */
    default BoolVar intLeView(IntVar x, int c) {
        if (x.getUB() <= c) {
            return ref().boolVar(true);
        } else if (x.getLB() > c) {
            return ref().boolVar(false);
        } else {
            if (ref().getSettings().enableViews()) {
                int p = checkDeclaredView(x, c, BoolLeqView.class, ref().getSettings().checkDeclaredViews());
                if (p >= 0) {
                    return x.getView(p).asBoolVar();
                } else {
                    return new BoolLeqView<>(x, c);
                }
            }else {
                BoolVar b = ref().boolVar();
                ref().reifyXltC(x, c +1, b);
                return b;
            }
        }
    }

    /**
     * Creates an view over <i>x</i> such that: <i>(x &ge; c) &hArr; b</i>.
     * <p>
     * @param x an integer variable.
     * @param c a constant
     * @return a BoolVar that reifies <i>x &ge; c</i>
     */
    default BoolVar intGeView(IntVar x, int c) {
        if (x.getLB() >= c) {
            return ref().boolVar(true);
        } else if (x.getUB() < c) {
            return ref().boolVar(false);
        } else {
            if (ref().getSettings().enableViews()) {
                int p = checkDeclaredView(x, c - 1, BoolLeqView.class, ref().getSettings().checkDeclaredViews());
                if (p >= 0) {
                    return x.getView(p).asBoolVar().not();
                } else {
                    return new BoolLeqView<>(x, c - 1).not();
                }
            }else {
                BoolVar b = ref().boolVar();
                ref().reifyXgtC(x, c - 1, b);
                return b;
            }
        }
    }

    @SuppressWarnings("rawtypes")
    static int checkDeclaredView(IntVar x, int c, Class clazz, boolean check){
        for(int i = 0; check && i < x.getNbViews(); i++)
            if (clazz.isInstance(x.getView(i))) {
                if(clazz  == BoolEqView.class){
                    BoolEqView v = (BoolEqView) x.getView(i);
                    if(v.cste == c){
                        return i;
                    }
                }else if(clazz  == BoolLeqView.class){
                    BoolLeqView v = (BoolLeqView) x.getView(i);
                    if(v.cste == c){
                        return i;
                    }
                }else if(clazz == IntMinusView.class){
                    return i;
                }else if(clazz == IntOffsetView.class){
                    IntOffsetView v = (IntOffsetView) x.getView(i);
                    if(v.cste == c){
                        return i;
                    }
                }else if(clazz == IntScaleView.class){
                    IntScaleView v = (IntScaleView) x.getView(i);
                    if(v.cste == c){
                        return i;
                    }
                }
            }
        return -1;
    }


    //*************************************************************************************
    // REAL VARIABLES
    //*************************************************************************************

    /**
     * Creates a real view of <i>var</i>, i.e. a RealVar of domain equal to the domain of <i>var</i>.
     * This should be used to include an integer variable in an expression/constraint requiring RealVar
     * @param var the integer variable to be viewed as a RealVar
     * @param precision double precision (e.g., 0.00001d)
     * @return a RealVar of domain equal to the domain of <i>var</i>
     */
    default RealVar realIntView(IntVar var, double precision) {
        if (ref().getSettings().enableViews()) {
            return new RealView<>(var, precision);
        } else {
            double lb = var.getLB();
            double ub = var.getUB();
            RealVar rv = ref().realVar("(real)" + var.getName(), lb, ub, precision);
            ref().realIbexGenericConstraint("{0} = {1}", rv, var).post();
            return rv;
        }
    }

    /**
     * Creates an array of real views for a set of integer variables
     * This should be used to include an integer variable in an expression/constraint requiring RealVar
     * @param ints the array of integer variables to be viewed as real variables
     * @param precision double precision (e.g., 0.00001d)
     * @return a real view of <i>ints</i>
     */
    default RealVar[] realIntViewArray(IntVar[] ints, double precision) {
        RealVar[] reals = new RealVar[ints.length];
        if (ref().getSettings().enableViews()) {
            for (int i = 0; i < ints.length; i++) {
                reals[i] = realIntView(ints[i], precision);
            }
        } else {
            for (int i = 0; i < ints.length; i++) {
                double lb = ints[i].getLB();
                double ub = ints[i].getUB();
                reals[i] = ref().realVar("(real)" + ints[i].getName(), lb, ub, precision);
                ref().realIbexGenericConstraint("{0} = {1}", reals[i], ints[i]).post();
            }
        }
        return reals;
    }

    // MATRIX

    /**
     * Creates a matrix of real views for a matrix of integer variables
     * This should be used to include an integer variable in an expression/constraint requiring RealVar
     * @param ints the matrix of integer variables to be viewed as real variables
     * @param precision double precision (e.g., 0.00001d)
     * @return a real view of <i>ints</i>
     */
    default RealVar[][] realIntViewMatrix(IntVar[][] ints, double precision) {
        RealVar[][] vars = new RealVar[ints.length][ints[0].length];
        for (int i = 0; i < ints.length; i++) {
            vars[i] = realIntViewArray(ints[i], precision);
        }
        return vars;
    }

    //*************************************************************************************
    // SET VARIABLES
    //*************************************************************************************

    // OVER ARRAY OF BOOLEAN VARIABLES

    /**
     * Create a set view over an array of boolean variables defined such that:
     * boolVars[x - offset] = True <=> x in setView
     * This view is equivalent to the {@link org.chocosolver.solver.constraints.set.PropBoolChannel} constraint.
     * @param boolVars observed boolean variables
     * @param offset Offset between boolVars array indices and set elements
     * @return a set view such that boolVars[x - offset] = True <=> x in setView
     */
    default SetVar boolsSetView(BoolVar[] boolVars, int offset) {
        return new SetBoolsView<>(offset, boolVars);
    }

    // OVER ARRAY OF INTEGER VARIABLES

    /**
     * Create a set view over an array of integer variables, such that:
     * intVars[x - offset] = v[x - offset] <=> x in set view.
     *
     * @param intVars array of integer variables
     * @param v array of integers that "toggle" integer variables index inclusion in the set view
     * @param offset offset between intVars indices and setViews elements
     * @return a set view such that intVars[x - offset] = v[x - offset] <=> x in setView.
     */
    default SetVar intsSetView(IntVar[] intVars, int[] v, int offset) {
        return new SetIntsView<>(v, offset, intVars);
    }

    /**
     * Create a set view over an array of integer variables, such that:
     * intVars[x - offset] = v <=> x in set view.
     *
     * @param intVars array of integer variables
     * @param v integer that "toggle" integer variables index inclusion in the set view
     * @param offset offset between intVars indices and setViews elements
     * @return a set view such that intVars[x - offset] = v <=> x in setView.
     */
    default SetVar intsSetView(IntVar[] intVars, int v, int offset) {
        int[] vals = new int[intVars.length];
        Arrays.fill(vals, v);
        return intsSetView(intVars, vals, offset);
    }

    /**
     * Instantiate an array of set views over an array of integer variables, such that:
     * x in setViews[y - offset1] <=> intVars[x - offset2] = y.
     *
     * This view is equivalent to the {@link org.chocosolver.solver.constraints.set.PropIntChannel} constraint.
     *
     * @param intVars array of integer variables
     * @param nbSets number of set views to create
     * @param offset1 offset between setViews indices and intVars values
     * @param offset2 offset between intVars indices and setViews elements
     * @return an array of set views such that x in setViews[y - offset1] <=> intVars[x - offset2] = y.
     */
    default SetVar[] intsSetsView(IntVar[] intVars, int nbSets, int offset1, int offset2) {
        SetVar[] setVars = new SetVar[nbSets];
        for (int i = 0; i < nbSets; i++) {
            setVars[i] = intsSetView(intVars, i + offset1, offset2);
        }
        return setVars;
    }

    //  OVER SET VARIABLES

    /**
     * Creates a set view representing the union of a list of set variables.
     * @param sets The set variables to observe.
     * @return A set union view.
     */
    default SetVar setUnionView(SetVar... sets) {
        return new SetUnionView("setUnion", sets);
    }

    /**
     * Creates a set view representing the intersection of a list of set variables.
     * @param sets The set variables to observe.
     * @return A set intersection view.
     */
    default SetVar setIntersectionView(SetVar... sets) {
        return new SetIntersectionView("setIntersection", sets);
    }

    /**
     * Creates a set view z representing the set difference between x and y: z = x \ y.
     * @param x A set variable.
     * @param y A set variable.
     * @return A set difference z view such that z = x \ y.
     */
    default SetVar setDifferenceView(SetVar x, SetVar y) {
        return new SetDifferenceView("setDifference", x, y);
    }

    //  OVER GRAPH VARIABLES

    /**
     * Creates a set view over the set of nodes of a graph variable.
     * @param g observed graph variable
     * @return a set view over the set of nodes of a graph variable
     */
    default SetVar graphNodeSetView(GraphVar<? extends IGraph> g) {
        return new SetNodeGraphView<>(g);
    }

    /**
     * Creates a set view over the set of successors of a node of a directed graph variable.
     * @param g observed graph variable
     * @param node observed node
     * @return a set view over the set of successors of a node of a directed graph variable.
     */
    default SetVar graphSuccessorsSetView(DirectedGraphVar g, int node) {
        return new SetSuccessorsGraphView<>(g, node);
    }

    /**
     * Creates a set view over the set of predecessors of a node of a directed graph variable.
     * @param g observed graph variable
     * @param node observed node
     * @return a set view over the set of predecessors of a node of a directed graph variable.
     */
    default SetVar graphPredecessorsSetView(DirectedGraphVar g, int node) {
        return new SetPredecessorsGraphView<>(g, node);
    }

    /**
     * Creates a set view over the set of neighbors of a node of an undirected graph variable.
     * @param g observed graph variable
     * @param node observed node
     * @return a set view over the set of neighbors of a node of an udirected graph variable.
     */
    default SetVar graphNeighborsSetView(UndirectedGraphVar g, int node) {
        return new SetSuccessorsGraphView<>(g, node);
    }

    //*************************************************************************************
    // GRAPH VARIABLES
    //*************************************************************************************

    /**
     * Creates a graph view G' = (V', E') from another graph G = (V, E) such that:
     *      V' = V \ nodes (set difference) if exclude = true, else V' = V \cap nodes (set intersection)
     *      E' = { (x, y) \in E | x \in V' \land y \in V' }.
     *
     * @param g The graph variable to observe
     * @param nodes the set of nodes to construct the view from (see exclude parameter)
     * @param exclude if true, V' = V \ nodes (set difference), else V' = V \cap nodes (set intersection)
     */
    default UndirectedGraphVar nodeInducedSubgraphView(UndirectedGraphVar g, ISet nodes, boolean exclude) {
        return new NodeInducedSubgraphView(g.getName() + "[" + nodes.toString() + "]", g, nodes, exclude);
    }

    /**
     * Creates a graph view G' = (V', E') from another graph G = (V, E) such that:
     *      V' = V \ nodes (set difference) if exclude = true, else V' = V \cap nodes (set intersection)
     *      E' = { (x, y) \in E | x \in V' \land y \in V' }.
     *
     * @param g The graph variable to observe
     * @param nodes the set of nodes to construct the view from (see exclude parameter)
     * @param exclude if true, V' = V \ nodes (set difference), else V' = V \cap nodes (set intersection)
     */
    default DirectedGraphVar nodeInducedSubgraphView(DirectedGraphVar g, ISet nodes, boolean exclude) {
        return new DirectedNodeInducedSubgraphView(g.getName() + "[" + nodes.toString() + "]", g, nodes, exclude);
    }

    /**
     * Construct an edge-induced subgraph view G = (V', E') from G = (V, E) such that:
     *     V' = { x \in V | \exists y \in V s.t. (x, y) \in E' }
     *     E' = E \ edges (set difference) if exclude = true, else E' = E \cap edges (set intersection).
     *
     * @param g observed variable
     * @param edges the set of edges (array of couples) to construct the subgraph from (see exclude parameter)
     * @param exclude if true, E' = E \ edges (set difference), else E' = E \cap edges (set intersection)
     */
    default UndirectedGraphVar edgeInducedSubgraphView(UndirectedGraphVar g, int[][] edges, boolean exclude) {
        return new EdgeInducedSubgraphView(g.getName() + "{" + Arrays.deepToString(edges) + "}", g, edges, exclude);
    }

    /**
     * Construct an edge-induced subgraph view G = (V', E') from G = (V, E) such that:
     *     V' = { x \in V | \exists y \in V s.t. (x, y) \in E' }
     *     E' = E \ edges (set difference) if exclude = true, else E' = E \cap edges (set intersection).
     *
     * @param g observed variable
     * @param edges the set of edges (array of couples) to construct the subgraph from (see exclude parameter)
     * @param exclude if true, E' = E \ edges (set difference), else E' = E \cap edges (set intersection)
     */
    default DirectedGraphVar edgeInducedSubgraphView(DirectedGraphVar g, int[][] edges, boolean exclude) {
        return new DirectedEdgeInducedSubgraphView(g.getName() + "{" + Arrays.deepToString(edges) + "}", g, edges, exclude);
    }

    /**
     * Construct an undirected graph union view G = (V, E) from a set of undirected graphs {G_1 = (V_1, E_1), ..., G_k = (V_k, E_k)} such that :
     *     V = V_1 \cup ... \cup V_k (\cup = set union);
     *     E = E_1 \cup ... \cup E_k.
     * @param graphVars the graphs to construct the union view from
     * @return An undirected graph union view
     */
    default UndirectedGraphVar graphUnionView(UndirectedGraphVar... graphVars) {
        return new UndirectedGraphUnionView("GraphUnionView", graphVars);
    }

    /**
     * Construct a directed graph union view G = (V, E) from a set of directed graphs {G_1 = (V_1, E_1), ..., G_k = (V_k, E_k)} such that :
     *     V = V_1 \cup ... \cup V_k (\cup = set union);
     *     E = E_1 \cup ... \cup E_k.
     * @param graphVars the graphs to construct the union view from
     * @return A directed graph union view
     */
    default DirectedGraphVar graphUnionView(DirectedGraphVar... graphVars) {
        return new DirectedGraphUnionView("GraphUnionView", graphVars);
    }
}