/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2019, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 15/10/2019
 */
module org.chocosolver.solver {
    exports org.chocosolver.solver;
    exports org.chocosolver.solver.constraints;
    exports org.chocosolver.solver.constraints.binary;
    exports org.chocosolver.solver.constraints.extension;
    exports org.chocosolver.solver.constraints.extension.binary;
    exports org.chocosolver.solver.constraints.extension.nary;
    exports org.chocosolver.solver.constraints.nary.alldifferent;
    exports org.chocosolver.solver.constraints.nary.alldifferent.conditions;
    exports org.chocosolver.solver.constraints.nary.automata.FA;
    exports org.chocosolver.solver.constraints.nary.circuit;
    exports org.chocosolver.solver.constraints.nary.clauses;
    exports org.chocosolver.solver.constraints.nary.cnf;
    exports org.chocosolver.solver.constraints.nary.count;
    exports org.chocosolver.solver.constraints.nary.cumulative;
    exports org.chocosolver.solver.constraints.nary.element;
    exports org.chocosolver.solver.constraints.nary.sum;
    exports org.chocosolver.solver.constraints.ternary;
    exports org.chocosolver.solver.constraints.real;
    exports org.chocosolver.solver.constraints.set;
    exports org.chocosolver.solver.exception;
    exports org.chocosolver.solver.expression.discrete.arithmetic;
    exports org.chocosolver.solver.expression.discrete.logical;
    exports org.chocosolver.solver.expression.discrete.relational;
    exports org.chocosolver.solver.learn;
    exports org.chocosolver.solver.objective;
    exports org.chocosolver.solver.search.limits;
    exports org.chocosolver.solver.search.loop.move;
    exports org.chocosolver.solver.search.measure;
    exports org.chocosolver.solver.search.strategy;
    exports org.chocosolver.solver.search.strategy.assignments;
    exports org.chocosolver.solver.search.strategy.selectors.values;
    exports org.chocosolver.solver.search.strategy.selectors.variables;
    exports org.chocosolver.solver.search.strategy.strategy;
    exports org.chocosolver.solver.variables;
    exports org.chocosolver.solver.variables.impl;
    exports org.chocosolver.solver.variables.view;
    exports org.chocosolver.util;
    exports org.chocosolver.util.criteria;
    exports org.chocosolver.util.objects.setDataStructures.iterable;
    exports org.chocosolver.util.objects.graphs;
    exports org.chocosolver.util.objects.queues;
    exports org.chocosolver.util.objects.setDataStructures;
    exports org.chocosolver.util.tools;

    requires trove4j;
    requires transitive org.chocosolver.cutoffseq;
    requires org.chocosolver.sat;
    requires org.jgrapht.core;
    requires cpprof.java;
    requires java.desktop;
    requires xchart;
    requires java.management;
    requires automaton;
}