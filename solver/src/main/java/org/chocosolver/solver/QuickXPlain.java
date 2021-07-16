/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver;

import org.chocosolver.solver.constraints.Constraint;

import java.util.*;

/**
 * QuickXPlain is intended to find a minimum conflict set of constraints that's causing
 * a conflict in the solver execution [1].
 * This implementation is an improved version from that proposed in the discussion from issue #509.
 * <p>
 * 1. Ulrich Junker. 2004. QUICKXPLAIN: preferred explanations and relaxations for over-constrained
 * problems. In <i>Proceedings of the 19th national conference on Artifical intelligence</i>
 * AAAI Press, 167–172.
 *
 * @author Joao Pedro Schmitt
 * @since 03/12/2020
 */
public class QuickXPlain {

    private Model model;

    public QuickXPlain(Model model) {
        this.model = model;
    }

    /**
     * Given a set of conflicting constraints that block the solver from find a solution
     * for a problem, returns the minimum conflicting set to be relaxed in such a way to put
     * the solver back in a feasible search space.
     *
     * @param conflictingSet
     * @return minimumConflictingSet
     */
    public List<Constraint> findMinimumConflictingSet(List<Constraint> conflictingSet) {
        List<Constraint> allConstraints = getAllConstraints();
        List<Constraint> background = getBackground(allConstraints, conflictingSet);
        List<Constraint> minimumConflictSet;
        if (conflictingSet.isEmpty() || isConsistent(allConstraints))
            minimumConflictSet = Collections.emptyList();
        else {
            minimumConflictSet = qx(background, background, conflictingSet);
        }
        model.getSolver().reset();
        return minimumConflictSet;
    }

    /**
     * Execute quickXPlain algorithm to find the minimum conflicting set.
     *
     * @param background
     * @param conflict
     * @param constraints
     * @return
     */
    private List<Constraint> qx(List<Constraint> background, List<Constraint> conflict, List<Constraint> constraints) {
        if (!conflict.isEmpty() && !isConsistent(background)) {
            return Collections.emptyList();
        }
        if (constraints.size() == 1) {
            return constraints;
        }
        int k = constraints.size() / 2;
        List<Constraint> c1 = new ArrayList<>(constraints.subList(0, k));
        List<Constraint> c2 = new ArrayList<>(constraints.subList(k, constraints.size()));
        List<Constraint> prevB = new ArrayList<>(background);
        List<Constraint> d2 = qx(constraintsUnion(background, c1), c1, c2);
        List<Constraint> conflictSet = new ArrayList<>(d2);
        List<Constraint> d1 = qx(constraintsUnion(prevB, d2), d2, c1);
        Set<Constraint> constraintSetHash = new HashSet<>(conflictSet);
        for (int i = 0; i < d1.size(); i++) {
            if (!constraintSetHash.contains(d1.get(i))) {
                conflictSet.add(d1.get(i));
            }
        }
        return conflictSet;
    }

    private boolean isConsistent(List<Constraint> background) {
        model.getSolver().reset();
        Set<Constraint> constraintsHash = new HashSet<>(background);
        Constraint[] constraints = model.getCstrs();
        for (int i = 0; i < constraints.length; i++) {
            constraints[i].setEnabled(constraintsHash.contains(constraints[i]));
        }
        return model.getSolver().solve();
    }

    public List<Constraint> constraintsUnion(List<Constraint> c1, List<Constraint> c2) {
        c1.addAll(c2);
        return c1;
    }

    /**
     * Background is the set of constraints that can not be relaxed.
     *
     * @param allConstraints
     * @param conflictingSet
     * @return background
     */
    private List<Constraint> getBackground(List<Constraint> allConstraints, List<Constraint> conflictingSet) {
        List<Constraint> knowledgeBase = new ArrayList<>(allConstraints.size() - conflictingSet.size());
        Set<Constraint> conflictSetHash = new HashSet<>(conflictingSet);
        for (Constraint cstr : allConstraints) {
            if (!conflictSetHash.contains(cstr)) {
                knowledgeBase.add(cstr);
            }
        }
        return knowledgeBase;
    }

    private List<Constraint> getAllConstraints() {
        List<Constraint> allConstraints = new ArrayList<>(model.getNbCstrs());
        Constraint[] cstrs = model.getCstrs();
        for (Constraint cstr : cstrs) {
            allConstraints.add(cstr);
        }
        return allConstraints;
    }

}
