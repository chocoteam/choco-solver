/*
 * This file is part of examples, http://choco-solver.org/
 * Copyright (c) 1999, IMT Atlantique.
 * SPDX-License-Identifier: BSD-3-Clause.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.docs.modeling;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.DirectedGraphVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.OptionalTask;
import org.chocosolver.solver.variables.RealVar;
import org.chocosolver.solver.variables.SetVar;
import org.chocosolver.solver.variables.Task;
import org.chocosolver.solver.variables.UndirectedGraphVar;
import org.chocosolver.util.objects.graphs.DirectedGraph;
import org.chocosolver.util.objects.graphs.GraphFactory;
import org.chocosolver.util.objects.graphs.UndirectedGraph;
import org.chocosolver.util.objects.setDataStructures.SetType;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Examples shown in the user documentation, page "Modeling / Declaring variables"
 * (docs/content/docs/Modeling/Variables.md).
 * The regions between {@code tag::name[]} and {@code end::name[]} are included in the page.
 *
 * @author Charles Prud'homme
 */
public class VariablesExamples {

    @Test(groups = "1s")
    public void intVar() {
        Model model = new Model();
        // tag::intVar[]
        // Create a constant variable equal to 42
        IntVar v0 = model.intVar("v0", 42);
        // Create a variable taking its value in [1, 3] (the value is 1, 2 or 3)
        IntVar v1 = model.intVar("v1", 1, 3);
        // Create a variable taking its value in {1, 3} (the value is 1 or 3)
        IntVar v2 = model.intVar("v2", new int[]{1, 3});
        // end::intVar[]
        Assert.assertEquals(v0.getValue(), 42);
        Assert.assertEquals(v1.getDomainSize(), 3);
        Assert.assertEquals(v2.getDomainSize(), 2);
    }

    @Test(groups = "1s")
    public void intVarArray() {
        Model model = new Model();
        // tag::intVarArray[]
        // Create an array of 5 variables taking their value in [-1, 1]
        IntVar[] vs = model.intVarArray("vs", 5, -1, 1);
        // Create a matrix of 5x6 variables taking their value in [-1, 1]
        IntVar[][] ws = model.intVarMatrix("ws", 5, 6, -1, 1);
        // end::intVarArray[]
        Assert.assertEquals(vs.length, 5);
        Assert.assertEquals(ws[4].length, 6);
    }

    @Test(groups = "1s")
    public void bounded() {
        Model model = new Model();
        // tag::bounded[]
        IntVar v = model.intVar("v", 1, 12, true);
        // end::bounded[]
        Assert.assertFalse(v.hasEnumeratedDomain());
    }

    @Test(groups = "1s")
    public void enumerated() {
        Model model = new Model();
        // tag::enumerated[]
        IntVar v = model.intVar("v", 1, 4, false);
        IntVar w = model.intVar("w", new int[]{1, 2, 3, 4});
        // end::enumerated[]
        Assert.assertTrue(v.hasEnumeratedDomain());
        Assert.assertTrue(w.hasEnumeratedDomain());
    }

    @Test(groups = "1s")
    public void boolVar() {
        Model model = new Model();
        // tag::boolVar[]
        BoolVar b = model.boolVar("b");
        // A Boolean variable fixed to true
        BoolVar t = model.boolVar(true);
        // end::boolVar[]
        Assert.assertFalse(b.isInstantiated());
        Assert.assertEquals(t.getValue(), 1);
    }

    @Test(groups = "1s")
    public void setVar() {
        Model model = new Model();
        // tag::setVar[]
        // Constant SetVar equal to {2,3,12}
        SetVar x = model.setVar("x", 2, 3, 12);
        // SetVar representing a subset of {1,2,3,5,12}
        // possible values: {}, {2}, {1,3,5} ...
        SetVar y = model.setVar("y", new int[]{}, new int[]{1, 2, 3, 5, 12});
        // SetVar representing a superset of {2,3} and a subset of {1,2,3,5,12}
        // possible values: {2,3}, {2,3,5}, {1,2,3,5} ...
        SetVar z = model.setVar("z", new int[]{2, 3}, new int[]{1, 2, 3, 5, 12});
        // end::setVar[]
        Assert.assertTrue(x.isInstantiated());
        Assert.assertTrue(model.getSolver().solve());
        Assert.assertTrue(z.getValue().contains(2));
    }

    @Test(groups = "1s")
    public void graphVar() {
        Model model = new Model();
        // tag::graphVar[]
        // A directed graph
        // first declare the lower bound
        DirectedGraph lb = GraphFactory.makeStoredDirectedGraph(model, 3, SetType.BITSET, SetType.BITSET);
        // then declare the upper bound
        DirectedGraph ub = GraphFactory.makeStoredAllNodesDirectedGraph(model, 3, SetType.BITSET, SetType.BITSET, false);
        ub.addEdge(0, 1);
        ub.addEdge(1, 2);
        ub.addEdge(2, 0);
        // finally declare the graph variable
        DirectedGraphVar dg = model.digraphVar("dg", lb, ub);

        // An undirected graph
        UndirectedGraph ulb = GraphFactory.makeStoredUndirectedGraph(model, 3, SetType.BITSET, SetType.BITSET);
        // the last parameter indicates that all nodes are mandatory
        UndirectedGraph uub = GraphFactory.makeCompleteStoredUndirectedGraph(model, 3, SetType.BITSET, SetType.BITSET, true);
        UndirectedGraphVar g = model.graphVar("g", ulb, uub);
        // end::graphVar[]
        Assert.assertTrue(model.getSolver().solve());
        Assert.assertEquals(g.getValue().getNodes().size(), 3);
    }

    @Test(groups = "1s")
    public void realVar() {
        Model model = new Model();
        // tag::realVar[]
        RealVar x = model.realVar("x", 0.2d, 3.4d, 0.001d);
        // end::realVar[]
        Assert.assertEquals(x.getLB(), 0.2d);
    }

    @Test(groups = "1s")
    public void task() {
        Model model = new Model();
        // tag::task[]
        IntVar start = model.intVar("start", 0, 10);
        int duration = 5;
        Task task = model.taskVar(start, duration);
        // end::task[]
        Assert.assertTrue(model.getSolver().solve());
        Assert.assertEquals(task.getEnd().getValue(), start.getValue() + duration);
    }

    @Test(groups = "1s")
    public void taskVariants() {
        Model model = new Model();
        // tag::taskVariants[]
        IntVar start = model.intVar("start", 0, 10);
        IntVar duration = model.intVar("duration", 1, 5);
        IntVar end = model.intVar("end", 0, 15);
        Task task = model.taskVar(start, duration, end);
        // start + duration = end only holds once the task is posted
        // (or given to a scheduling constraint, such as cumulative)
        task.post();
        // end::taskVariants[]
        Assert.assertTrue(model.getSolver().solve());
        Assert.assertEquals(end.getValue(), start.getValue() + duration.getValue());
    }

    @Test(groups = "1s")
    public void taskBounds() {
        Model model = new Model();
        // tag::taskBounds[]
        // earliest start 0, latest start 5, duration 3, earliest completion 3, latest completion 8
        Task task = model.taskVar(0, 5, 3, 3, 8);
        // end::taskBounds[]
        Assert.assertTrue(model.getSolver().solve());
    }

    @Test(groups = "1s")
    public void optionalTask() {
        Model model = new Model();
        // tag::optionalTask[]
        IntVar start = model.intVar("start", 0, 10);
        BoolVar performed = model.boolVar("performed");
        OptionalTask task = model.taskVar(start, 5, performed);
        // or with a variable duration
        IntVar duration = model.intVar("duration", 1, 5);
        OptionalTask task2 = model.taskVar(start, duration, performed);
        task2.post();
        // end::optionalTask[]
        Assert.assertTrue(model.getSolver().solve());
    }

    @Test(groups = "1s")
    public void taskArray() {
        Model model = new Model();
        // tag::taskArray[]
        IntVar[] starts = model.intVarArray("s", 10, 0, 20);
        IntVar[] durations = model.intVarArray("d", 10, 1, 5);
        IntVar[] ends = model.intVarArray("e", 10, 0, 25);
        Task[] tasks = model.taskVarArray(starts, durations, ends);
        for (Task t : tasks) {
            t.post();
        }
        // end::taskArray[]
        Assert.assertTrue(model.getSolver().solve());
        Assert.assertEquals(ends[3].getValue(), starts[3].getValue() + durations[3].getValue());
    }

    @Test(groups = "1s")
    public void arithmeticViews() {
        Model model = new Model();
        IntVar y = model.intVar("y", 0, 5);
        // tag::offsetView[]
        IntVar x = model.offset(y, 2);
        // end::offsetView[]
        // tag::negView[]
        IntVar x2 = model.neg(y);
        // end::negView[]
        // tag::mulView[]
        IntVar x3 = model.mul(y, 3);
        // end::mulView[]
        // tag::composition[]
        IntVar x4 = model.intView(2, y, 5);
        // end::composition[]
        model.arithm(y, "=", 4).post();
        Assert.assertTrue(model.getSolver().solve());
        Assert.assertEquals(x.getValue(), 6);
        Assert.assertEquals(x2.getValue(), -4);
        Assert.assertEquals(x3.getValue(), 12);
        Assert.assertEquals(x4.getValue(), 13);
    }

    @Test(groups = "1s")
    public void logicalViews() {
        Model model = new Model();
        IntVar x = model.intVar("x", 0, 10);
        // tag::isEq[]
        BoolVar b = model.isEq(x, 4);
        // end::isEq[]
        // tag::isNeq[]
        BoolVar b2 = model.isNeq(x, 4);
        // end::isNeq[]
        // tag::isLeq[]
        BoolVar b3 = model.isLeq(x, 4);
        // end::isLeq[]
        // tag::isGeq[]
        BoolVar b4 = model.isGeq(x, 4);
        // end::isGeq[]
        // tag::boolNot[]
        BoolVar d = model.boolNotView(b);
        // end::boolNot[]
        // tag::boolNotShort[]
        BoolVar d2 = b.not();
        // end::boolNotShort[]
        model.arithm(x, "=", 4).post();
        Assert.assertTrue(model.getSolver().solve());
        Assert.assertEquals(b.getValue() + b2.getValue() + b3.getValue() + b4.getValue(), 3);
        Assert.assertEquals(d.getValue() + d2.getValue(), 0);
    }

    @Test(groups = "1s")
    public void realIntView() {
        Model model = new Model();
        // tag::realIntView[]
        IntVar ivar = model.intVar("i", 0, 4);
        double precision = 0.001d;
        RealVar rvar = model.realIntView(ivar, precision);
        // end::realIntView[]
        Assert.assertEquals(rvar.getUB(), 4d);
    }

    @Test(groups = "1s")
    public void resultVariables() {
        Model model = new Model();
        IntVar[] vars = model.intVarArray("x", 5, 0, 10);
        // tag::sumResult[]
        // an IntVar constrained to be the sum of vars
        IntVar sum = model.sum("sum", vars);
        // equivalent to:
        // IntVar sum = model.intVar("sum", 0, 50);
        // model.sum(vars, "=", sum).post();
        // end::sumResult[]
        // tag::countResult[]
        // how many times value 5 appears in vars
        IntVar count = model.count("count", 5, vars);
        // how many times the value of val appears in vars
        IntVar val = model.intVar("val", 0, 10);
        IntVar count2 = model.count("count2", val, vars);
        // end::countResult[]
        // tag::minMaxResult[]
        IntVar min = model.min("min", vars);
        IntVar max = model.max("max", vars);
        // end::minMaxResult[]
        // tag::argminResult[]
        // index of the (first) smallest and largest value of vars
        IntVar argmin = model.argmin("argmin", vars);
        IntVar argmax = model.argmax("argmax", vars);
        // end::argminResult[]
        // tag::elementResult[]
        int[] table = {10, 20, 30, 40, 50};
        IntVar index = model.intVar("index", 0, 4);
        // the value of table at position index (0 is the offset of the first index)
        IntVar element = model.element("element", table, index, 0);
        // end::elementResult[]
        Assert.assertTrue(model.getSolver().solve());
        Assert.assertEquals(vars[argmin.getValue()].getValue(), min.getValue());
        Assert.assertEquals(element.getValue(), table[index.getValue()]);
    }

    @Test(groups = "1s")
    public void setResults() {
        Model model = new Model();
        // tag::setResults[]
        SetVar[] sets = model.setVarArray("s", 3, new int[]{}, new int[]{0, 1, 2, 3});
        // union and intersection of all sets
        SetVar union = model.union("union", sets);
        SetVar intersection = model.intersection("intersection", sets);
        // number of empty sets
        IntVar nbEmpty = model.nbEmpty("nbEmpty", sets);
        // sum of the weights indexed by the elements of s (0 is the offset of the first index)
        int[] weights = {1, 2, 3, 4};
        SetVar s = model.setVar("s", new int[]{}, new int[]{0, 1, 2, 3});
        IntVar weightedSum = model.sum("wsum", weights, 0, s);
        // end::setResults[]
        Assert.assertTrue(model.getSolver().solve());
    }
}
