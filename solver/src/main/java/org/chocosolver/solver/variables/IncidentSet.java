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

import org.chocosolver.solver.ICause;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.util.objects.setDataStructures.ISet;

/**
 * Migrated from choco-graph
 * @author Jean-Guillaume Fages
 */
public interface IncidentSet {

    ISet getPotentialSet(GraphVar graph, int i);

    ISet getMandatorySet(GraphVar graph, int i);

    boolean enforce(GraphVar g, int from, int to, ICause cause) throws ContradictionException;

    boolean remove(GraphVar g, int from, int to, ICause cause) throws ContradictionException;

    class SuccessorsSet implements IncidentSet {

        @Override
        public ISet getPotentialSet(GraphVar graph, int i) {
            return graph.getPotentialSuccessorsOf(i);
        }

        @Override
        public ISet getMandatorySet(GraphVar graph, int i) {
            return graph.getMandatorySuccessorsOf(i);
        }

        @Override
        public boolean enforce(GraphVar g, int from, int to, ICause cause) throws ContradictionException {
            return g.enforceEdge(from, to, cause);
        }

        @Override
        public boolean remove(GraphVar g, int from, int to, ICause cause) throws ContradictionException {
            return g.removeEdge(from, to, cause);
        }
    }

    class PredecessorsSet implements IncidentSet {
        @Override
        public ISet getPotentialSet(GraphVar graph, int i) {
            return graph.getPotentialPredecessorOf(i);
        }

        @Override
        public ISet getMandatorySet(GraphVar graph, int i) {
            return graph.getMandatoryPredecessorsOf(i);
        }

        @Override
        public boolean enforce(GraphVar g, int from, int to, ICause cause) throws ContradictionException {
            return g.enforceEdge(to, from, cause);
        }

        @Override
        public boolean remove(GraphVar g, int from, int to, ICause cause) throws ContradictionException {
            return g.removeEdge(to, from, cause);
        }
    }
}
