/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2023, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.propagation;

import gnu.trove.list.array.TLongArrayList;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.variables.Variable;

import java.util.ArrayList;

/**
 * This interface manages the propagation information.
 * <p>
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 19/06/2023
 */
public interface PropagationInsight {

    PropagationInsight VOID = new PropagationInsight() {
    };

    default void clear() {
    }

    default void cardinality(Propagator<?> p) {

    }

    default void update(Propagator<?> p, Variable v, boolean onFailure) {

    }

    default void modifiy(Variable v) {

    }


    class PickOnDom implements PropagationInsight {
        private final ArrayList<Variable> Lvars;
        private final TLongArrayList Ldeltas;

        private long card;

        private boolean changed;

        public PickOnDom() {
            this.Lvars = new ArrayList<>();
            this.Ldeltas = new TLongArrayList();
        }

        @Override
        public void clear() {
            Lvars.clear();
            Ldeltas.resetQuick();
        }

        @Override
        public void cardinality(Propagator<?> p) {
            card = 0L;
            for (int j = 0; j < p.getNbVars(); j++) {
                card += p.getVar(j).getDomainSize();
            }
        }

        @Override
        public void update(Propagator<?> p, Variable v, boolean onFailure) {
            if (!changed) return;
            changed = false;
            if (!onFailure) {
                long card_ = card;
                cardinality(p);
                card = card_ - card;
            }
            Lvars.add(v);
            Ldeltas.add(card);
        }

        @Override
        public void modifiy(Variable v) {
            changed = true;
        }

        public ArrayList<Variable> getLvars() {
            return Lvars;
        }

        public TLongArrayList getLdeltas() {
            return Ldeltas;
        }
    }

    class PickOnFil implements PropagationInsight {
        private final ArrayList<Propagator<?>> Lcstrs;

        private final TLongArrayList Ldeltas;
        private long card;
        private boolean changed;

        public PickOnFil() {
            this.Lcstrs = new ArrayList<>();
            this.Ldeltas = new TLongArrayList();
        }

        @Override
        public void clear() {
            Lcstrs.clear();
            Ldeltas.resetQuick();
        }

        @Override
        public void cardinality(Propagator<?> p) {
            card = 0L;
            for (int j = 0; j < p.getNbVars(); j++) {
                card += p.getVar(j).getDomainSize();
            }
        }

        @Override
        public void update(Propagator<?> p, Variable v, boolean onFailure) {
            if (!changed) return;
            changed = false;
            if (!onFailure) {
                long card_ = card;
                cardinality(p);
                card = card_ - card;
            }
            Lcstrs.add(p);
            Ldeltas.add(card);
        }

        public void modifiy(Variable v) {
            changed = true;
        }

        public ArrayList<Propagator<?>> getLcstrs() {
            return Lcstrs;
        }

        public TLongArrayList getLdeltas() {
            return Ldeltas;
        }
    }

}
