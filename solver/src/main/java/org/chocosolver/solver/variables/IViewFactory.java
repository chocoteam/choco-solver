/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2023, IMT Atlantique. All rights reserved.
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
import org.chocosolver.solver.variables.view.integer.IntAffineView;
import org.chocosolver.solver.variables.view.set.*;
import org.chocosolver.util.objects.graphs.IGraph;
import org.chocosolver.util.objects.setDataStructures.ISet;

import java.util.Arrays;

import static java.lang.Math.max;

/**
 * Interface to make views (BoolVar, IntVar, RealVar and SetVar)
 * <p>
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
     *
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
     * <p>
     * given v an integer, b = true iff S contains v.
     *
     * @param setVar The set variable to observe.
     * @param v      The value to observe in the set variable.
     * @return A BoolVar equals to S.contains(v).
     */
    default BoolVar setBoolView(SetVar setVar, int v) {
        return new BoolSetView<>(v, setVar);
    }

    /**
     * Creates an array of boolean views b over a set variable S such that:
     * <p>
     * b[i - offset] = true <=> i in S.
     *
     * @param setVar The set variable to observe
     * @param size   The size of the bool var array
     * @param offset The offset
     * @return A BoolVar array such that b[i - offset] = true <=> i in S.
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
     * Create an affine view based on <i>var</i>, i.e. <i>view = a * var + b</i>
     *
     * @param a   a coefficient
     * @param var an integer variable
     * @param b   a constant
     * @return an affine view
     */
    default IntVar intView(int a, IntVar var, int b) {
        if (a == 1 && b == 0) {
            return var;
        } else if (a == 0) {
            return ref().intVar(b);
        } else if (var.isInstantiated()) {
            return ref().intVar(var.getValue() * a + b);
        } else if (ref().getSettings().enableViews()) {
            for (int i = 0; i < var.getNbViews(); i++) {
                if (var.getView(i) instanceof IntAffineView) {
                    IntAffineView<?> v = (IntAffineView<?>) var.getView(i);
                    if (v.equals(var, a, b)) {
                        return var.getView(i).asIntVar();
                    }
                }
            }
            return IntAffineView.make(var, a, b);
        } else {
            int lb, ub;
            if (a > 0) {
                lb = var.getLB() * a + b;
                ub = var.getUB() * a + b;
            } else {
                lb = var.getUB() * a + b;
                ub = var.getLB() * a + b;
            }
            String name = "(" + var.getName() + "*" + a + "+" + b + ")";
            IntVar ov;
            if (var.hasEnumeratedDomain()) {
                ov = ref().intVar(name, lb, ub, false);
            } else {
                ov = ref().intVar(name, lb, ub, true);
            }
            if (a == 1) {
                ref().arithm(var, "-", ov, "=", -b).post();
            } else if (a == -1) {
                ref().arithm(var, "+", ov, "=", b).post();
            } else if (b == 0) {
                ref().scalar(new IntVar[]{var}, new int[]{a}, "=", ov).post();
            } else {
                ref().scalar(new IntVar[]{var, ov}, new int[]{a, -1}, "=", -b).post();
            }
            return ov;
        }
    }

    /**
     * Create a view based on <i>var</i>, i.e. <i>view = |var|</i>
     *
     * @param var an integer variable
     * @return an abs view
     */
    default IntVar abs(IntVar var) {
        if (var.isInstantiated()) {
            return ref().intVar(Math.abs(var.getValue()));
        } else if (var.getLB() >= 0) {
            return var;
        } else if (var.getUB() <= 0) {
            return intView(-1, var, 0);
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
     * Create a view based on <i>var</i>, i.e. <i>view = var + b</i>
     *
     * @param var an integer variable
     * @param b   a constant
     * @return a plus view
     * @see #intView(int, IntVar, int)
     */
    default IntVar offset(IntVar var, int b) {
        return intView(1, var, b);
    }

    /**
     * Create a view based on <i>var</i>, i.e. <i>view = (var == v)</i>
     *
     * @param var an integer variable
     * @param v   a constant
     * @return a boolean view
     */
    default BoolVar eqView(IntVar var, int v) {
        if (var.isInstantiatedTo(v)) {
            return ref().boolVar(true);
        } else if (!var.contains(v)) {
            return ref().boolVar(false);
        } else {
            if (ref().getSettings().enableViews()) {
                int p = checkDeclaredView(var, v, BoolEqView.class, ref().getSettings().checkDeclaredViews());
                if (p >= 0) {
                    return var.getView(p).asBoolVar();
                } else {
                    return new BoolEqView<>(var, v);
                }
            } else {
                BoolVar b = ref().boolVar();
                ref().reifyXeqC(var, v, b);
                return b;
            }
        }
    }

    /**
     * Create a view based on <i>var</i>, i.e. <i>view = (var >= v)</i>
     *
     * @param var an integer variable
     * @param v   a constant
     * @return a boolean view
     */
    default BoolVar geqView(IntVar var, int v) {
        if (var.getLB() >= v) {
            return ref().boolVar(true);
        } else if (var.getUB() < v) {
            return ref().boolVar(false);
        } else {
            if (ref().getSettings().enableViews()) {
                int p = checkDeclaredView(var, v - 1, BoolLeqView.class, ref().getSettings().checkDeclaredViews());
                if (p >= 0) {
                    return var.getView(p).asBoolVar().not();
                } else {
                    return new BoolLeqView<>(var, v - 1).not();
                }
            } else {
                BoolVar b = ref().boolVar();
                ref().reifyXgtC(var, v - 1, b);
                return b;
            }
        }
    }

    /**
     * Create a view based on <i>var</i>, i.e. <i>view = (var <= v)</i>
     *
     * @param var an integer variable
     * @param v   a constant
     * @return a boolean view
     */
    default BoolVar leqView(IntVar var, int v) {
        if (var.getUB() <= v) {
            return ref().boolVar(true);
        } else if (var.getLB() > v) {
            return ref().boolVar(false);
        } else {
            if (ref().getSettings().enableViews()) {
                int p = checkDeclaredView(var, v, BoolLeqView.class, ref().getSettings().checkDeclaredViews());
                if (p >= 0) {
                    return var.getView(p).asBoolVar();
                } else {
                    return new BoolLeqView<>(var, v);
                }
            } else {
                BoolVar b = ref().boolVar();
                ref().reifyXltC(var, v + 1, b);
                return b;
            }
        }
    }

    /**
     * Create a view based on <i>var</i>, i.e. <i>view = var * a</i>
     *
     * @param var an integer variable
     * @param a   a coefficient
     * @return a mul view
     * @see #intView(int, IntVar, int)
     */
    default IntVar mulView(IntVar var, int a) {
        return intView(a, var, 0);
    }

    /**
     * Create a view based on <i>var</i>, i.e. <i>view = (var != v)</i>
     *
     * @param var an integer variable
     * @param v   a constant
     * @return a boolean view
     */
    default BoolVar neqView(IntVar var, int v) {
        if (var.isInstantiatedTo(v)) {
            return ref().boolVar(false);
        } else if (!var.contains(v)) {
            return ref().boolVar(true);
        } else {
            if (ref().getSettings().enableViews()) {
                int p = checkDeclaredView(var, v, BoolEqView.class, ref().getSettings().checkDeclaredViews());
                if (p >= 0) {
                    return var.getView(p).asBoolVar().not();
                } else {
                    return new BoolEqView<>(var, v).not();
                }
            } else {
                BoolVar b = ref().boolVar();
                ref().reifyXneC(var, v, b);
                return b;
            }
        }
    }

    /**
     * Create a view based on <i>var</i>, i.e. <i>view = -var</i>
     *
     * @param var an integer variable
     * @return a neg view
     * @see #intView(int, IntVar, int)
     */
    default IntVar negView(IntVar var) {
        return intView(-1, var, 0);
    }

    /**
     * @see #intView(int, IntVar, int)
     * @deprecated
     */
    @Deprecated
    default IntVar intOffsetView(IntVar var, int cste) {
        return intView(1, var, cste);
    }

    /**
     * @see #intView(int, IntVar, int)
     * @deprecated
     */
    @Deprecated
    default IntVar intMinusView(IntVar var) {
        return intView(-1, var, 0);
    }

    /**
     * @see #intView(int, IntVar, int)
     * @deprecated
     */
    @Deprecated
    default IntVar intScaleView(IntVar var, int cste) {
        return intView(cste, var, 0);
    }

    /**
     * Creates a view over <i>var</i> such that: |<i>var</i>|.
     * <p>
     * <br/>- if <i>var</i> is already instantiated, returns a fixed variable;
     * <br/>- if the lower bound of <i>var</i> is greater or equal to 0, returns <i>var</i>;
     * <br/>- if the upper bound of <i>var</i> is less or equal to 0, return a minus view;
     * <br/>- otherwise, returns an absolute view;
     * <p>
     *
     * @param var an integer variable.
     * @return an IntVar equal to the absolute value of <i>var</i>
     * @see #abs(IntVar)
     * @deprecated
     */
    @Deprecated
    default IntVar intAbsView(IntVar var) {
        return abs(var);
    }

    /**
     * @see #intView(int, IntVar, int)
     * @deprecated
     */
    @Deprecated//(since = "4.11.0", forRemoval = true)
    default IntVar intAffineView(int a, IntVar x, int b) {
        return intView(a, x, b);
    }


    /**
     * Creates a view over <i>x</i> such that: <i>(x = c) &hArr; b</i>.
     * <p>
     *
     * @param x an integer variable.
     * @param c a constant
     * @return a BoolVar that reifies <i>x = c</i>
     * @deprecated
     * @see #eqView(IntVar, int)
     */
    @Deprecated
    default BoolVar intEqView(IntVar x, int c) {
        return eqView(x, c);
    }

    /**
     * Creates a view over <i>x</i> such that: <i>(x != c) &hArr; b</i>.
     * <p>
     *
     * @param x an integer variable.
     * @param c a constant
     * @return a BoolVar that reifies <i>x != c</i>
     * @deprecated
     * @see #neqView(IntVar, int)
     */
    @Deprecated
    default BoolVar intNeView(IntVar x, int c) {
        return neqView(x, c);
    }

    /**
     * Creates a view over <i>x</i> such that: <i>(x &le; c) &hArr; b</i>.
     * <p>
     *
     * @param x an integer variable.
     * @param c a constant
     * @return a BoolVar that reifies <i>x &le; c</i>
     * @deprecated
     * @see #leqView(IntVar, int)
     */
    @Deprecated
    default BoolVar intLeView(IntVar x, int c) {
        return leqView(x, c);
    }

    /**
     * Creates a view over <i>x</i> such that: <i>(x &ge; c) &hArr; b</i>.
     * <p>
     *
     * @param x an integer variable.
     * @param c a constant
     * @return a BoolVar that reifies <i>x &ge; c</i>
     * @see #geqView(IntVar, int)
     * @deprecated
     */
    @Deprecated
    default BoolVar intGeView(IntVar x, int c) {
        return geqView(x, c);
    }

    @SuppressWarnings("rawtypes")
    static int checkDeclaredView(IntVar x, int c, Class clazz, boolean check) {
        for (int i = 0; check && i < x.getNbViews(); i++)
            if (clazz.isInstance(x.getView(i))) {
                if (clazz == BoolEqView.class) {
                    BoolEqView v = (BoolEqView) x.getView(i);
                    if (v.cste == c) {
                        return i;
                    }
                } else if (clazz == BoolLeqView.class) {
                    BoolLeqView v = (BoolLeqView) x.getView(i);
                    if (v.cste == c) {
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
     *
     * @param var       the integer variable to be viewed as a RealVar
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
     *
     * @param ints      the array of integer variables to be viewed as real variables
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
     *
     * @param ints      the matrix of integer variables to be viewed as real variables
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
     *
     * @param boolVars observed boolean variables
     * @param offset   Offset between boolVars array indices and set elements
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
     * @param v       array of integers that "toggle" integer variables index inclusion in the set view
     * @param offset  offset between intVars indices and setViews elements
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
     * @param v       integer that "toggle" integer variables index inclusion in the set view
     * @param offset  offset between intVars indices and setViews elements
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
     * <p>
     * This view is equivalent to the {@link org.chocosolver.solver.constraints.set.PropIntChannel} constraint.
     *
     * @param intVars array of integer variables
     * @param nbSets  number of set views to create
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
     *
     * @param sets The set variables to observe.
     * @return A set union view.
     */
    default SetVar setUnionView(SetVar... sets) {
        return new SetUnionView("setUnion", sets);
    }

    /**
     * Creates a set view representing the intersection of a list of set variables.
     *
     * @param sets The set variables to observe.
     * @return A set intersection view.
     */
    default SetVar setIntersectionView(SetVar... sets) {
        return new SetIntersectionView("setIntersection", sets);
    }

    /**
     * Creates a set view z representing the set difference between x and y: z = x \ y.
     *
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
     *
     * @param g observed graph variable
     * @return a set view over the set of nodes of a graph variable
     */
    default SetVar graphNodeSetView(GraphVar<? extends IGraph> g) {
        return new SetNodeGraphView<>(g);
    }

    /**
     * Creates a set view over the set of successors of a node of a directed graph variable.
     *
     * @param g    observed graph variable
     * @param node observed node
     * @return a set view over the set of successors of a node of a directed graph variable.
     */
    default SetVar graphSuccessorsSetView(DirectedGraphVar g, int node) {
        return new SetSuccessorsGraphView<>(g, node);
    }

    /**
     * Creates a set view over the set of predecessors of a node of a directed graph variable.
     *
     * @param g    observed graph variable
     * @param node observed node
     * @return a set view over the set of predecessors of a node of a directed graph variable.
     */
    default SetVar graphPredecessorsSetView(DirectedGraphVar g, int node) {
        return new SetPredecessorsGraphView<>(g, node);
    }

    /**
     * Creates a set view over the set of neighbors of a node of an undirected graph variable.
     *
     * @param g    observed graph variable
     * @param node observed node
     * @return a set view over the set of neighbors of a node of an undirected graph variable.
     */
    default SetVar graphNeighborsSetView(UndirectedGraphVar g, int node) {
        return new SetSuccessorsGraphView<>(g, node);
    }

    //*************************************************************************************
    // GRAPH VARIABLES
    //*************************************************************************************

    /**
     * Creates a graph view G' = (V', E') from another graph G = (V, E) such that:
     * V' = V \ nodes (set difference) if exclude = true, else V' = V \cap nodes (set intersection)
     * E' = { (x, y) \in E | x \in V' \land y \in V' }.
     *
     * @param g       The graph variable to observe
     * @param nodes   the set of nodes to construct the view from (see exclude parameter)
     * @param exclude if true, V' = V \ nodes (set difference), else V' = V \cap nodes (set intersection)
     */
    default UndirectedGraphVar nodeInducedSubgraphView(UndirectedGraphVar g, ISet nodes, boolean exclude) {
        return new NodeInducedSubgraphView(g.getName() + "[" + nodes.toString() + "]", g, nodes, exclude);
    }

    /**
     * Creates a graph view G' = (V', E') from another graph G = (V, E) such that:
     * V' = V \ nodes (set difference) if exclude = true, else V' = V \cap nodes (set intersection)
     * E' = { (x, y) \in E | x \in V' \land y \in V' }.
     *
     * @param g       The graph variable to observe
     * @param nodes   the set of nodes to construct the view from (see exclude parameter)
     * @param exclude if true, V' = V \ nodes (set difference), else V' = V \cap nodes (set intersection)
     */
    default DirectedGraphVar nodeInducedSubgraphView(DirectedGraphVar g, ISet nodes, boolean exclude) {
        return new DirectedNodeInducedSubgraphView(g.getName() + "[" + nodes.toString() + "]", g, nodes, exclude);
    }

    /**
     * Construct an edge-induced subgraph view G = (V', E') from G = (V, E) such that:
     * V' = { x \in V | \exists y \in V s.t. (x, y) \in E' }
     * E' = E \ edges (set difference) if exclude = true, else E' = E \cap edges (set intersection).
     *
     * @param g       observed variable
     * @param edges   the set of edges (array of couples) to construct the subgraph from (see exclude parameter)
     * @param exclude if true, E' = E \ edges (set difference), else E' = E \cap edges (set intersection)
     */
    default UndirectedGraphVar edgeInducedSubgraphView(UndirectedGraphVar g, int[][] edges, boolean exclude) {
        return new EdgeInducedSubgraphView(g.getName() + "{" + Arrays.deepToString(edges) + "}", g, edges, exclude);
    }

    /**
     * Construct an edge-induced subgraph view G = (V', E') from G = (V, E) such that:
     * V' = { x \in V | \exists y \in V s.t. (x, y) \in E' }
     * E' = E \ edges (set difference) if exclude = true, else E' = E \cap edges (set intersection).
     *
     * @param g       observed variable
     * @param edges   the set of edges (array of couples) to construct the subgraph from (see exclude parameter)
     * @param exclude if true, E' = E \ edges (set difference), else E' = E \cap edges (set intersection)
     */
    default DirectedGraphVar edgeInducedSubgraphView(DirectedGraphVar g, int[][] edges, boolean exclude) {
        return new DirectedEdgeInducedSubgraphView(g.getName() + "{" + Arrays.deepToString(edges) + "}", g, edges, exclude);
    }

    /**
     * Construct an undirected graph union view G = (V, E) from a set of undirected graphs {G_1 = (V_1, E_1), ..., G_k = (V_k, E_k)} such that :
     * V = V_1 \cup ... \cup V_k (\cup = set union);
     * E = E_1 \cup ... \cup E_k.
     *
     * @param graphVars the graphs to construct the union view from
     * @return An undirected graph union view
     */
    default UndirectedGraphVar graphUnionView(UndirectedGraphVar... graphVars) {
        return new UndirectedGraphUnionView("GraphUnionView", graphVars);
    }

    /**
     * Construct a directed graph union view G = (V, E) from a set of directed graphs {G_1 = (V_1, E_1), ..., G_k = (V_k, E_k)} such that :
     * V = V_1 \cup ... \cup V_k (\cup = set union);
     * E = E_1 \cup ... \cup E_k.
     *
     * @param graphVars the graphs to construct the union view from
     * @return A directed graph union view
     */
    default DirectedGraphVar graphUnionView(DirectedGraphVar... graphVars) {
        return new DirectedGraphUnionView("GraphUnionView", graphVars);
    }
}