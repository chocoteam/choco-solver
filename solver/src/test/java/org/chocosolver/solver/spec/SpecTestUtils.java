/*
 * This file is part of choco-solver, http://choco-solver.org/
 * Copyright (c) 1999, IMT Atlantique.
 * SPDX-License-Identifier: BSD-3-Clause.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.spec;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.Variable;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * What the tests of this package compare between models built directly, recorded and replayed.
 *
 * @author Charles Prud'homme
 */
final class SpecTestUtils {

    private SpecTestUtils() {
    }

    /**
     * @return a description of the structure of the model: variables (with their domains), groups and constraints
     * (with their propagators), in order
     */
    static String structure(Model m) {
        StringBuilder sb = new StringBuilder();
        for (Variable v : m.getVars()) {
            sb.append(v.getClass().getSimpleName()).append(' ').append(v.getName());
            if (v instanceof IntVar iv) {
                sb.append(' ').append(iv.getLB()).append("..").append(iv.getUB()).append(" #").append(iv.getDomainSize());
            }
            sb.append('\n');
        }
        m.getGroups().forEach(g -> sb.append("group ").append(g.getName()).append(Arrays.stream(g.getVariables())
                .map(Variable::getName).collect(Collectors.joining(",", "[", "]"))).append('\n'));
        for (Constraint c : m.getCstrs()) {
            sb.append(c.getName()).append(Arrays.stream(c.getPropagators())
                    .map(p -> p.getClass().getSimpleName() + "/" + p.getNbVars())
                    .collect(Collectors.joining(",", "[", "]"))).append('\n');
        }
        return sb.toString();
    }

    /**
     * Solve with a deterministic search over all integer variables, limited in nodes.
     *
     * @return solutions, nodes, fails and best objective
     */
    static String solve(Model m, long nodes) {
        Solver s = m.getSolver();
        IntVar[] vars = m.retrieveIntVars(true);
        if (vars.length > 0) {
            s.setSearch(Search.inputOrderLBSearch(vars));
        }
        s.limitNode(nodes);
        Number best = null;
        while (s.solve()) {
            if (m.getObjective() != null) {
                best = s.getBestSolutionValue();
            }
        }
        return s.getSolutionCount() + " sols, " + s.getNodeCount() + " nodes, " + s.getFailCount()
                + " fails" + (best == null ? "" : ", best=" + best);
    }
}
