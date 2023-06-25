/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2023, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.variables.impl;

import org.chocosolver.solver.ICause;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.propagation.PropagationEngine;

import java.util.stream.Stream;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 15/11/2022
 */
interface IBipartiteList {

    IBipartiteList EMPTY = new IBipartiteList() {
        @Override
        public int getFirst() {
            return 0;
        }

        @Override
        public int getLast() {
            return 0;
        }

        @Override
        public int getSplitter() {
            return 0;
        }

        @Override
        public Propagator<?> get(int i) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int add(Propagator<?> propagator, int idxInVar) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void remove(Propagator<?> propagator, int idxInProp, AbstractVariable var) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void swap(Propagator<?> propagator, int idxInProp, AbstractVariable var) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void schedule(ICause cause, PropagationEngine engine, int mask) {
        }

        @Override
        public Stream<Propagator<?>> stream() {
            return Stream.empty();
        }
    };

    /**
     * @return an empty immutable bipartite list
     */
    static IBipartiteList empty() {
        return EMPTY;
    }

    /**
     * @return the position of the first (inclusive) active propagator stored in this list
     */
    int getFirst();

    /**
     * @return the position just after the last active propagator stored in this list
     */
    int getLast();

    /**
     * @return the number of passive propagators
     */
    int getSplitter();

    /**
     * @param i position of the propagator to get
     * @return the propagator in position <i>i</i> in this
     */
    Propagator<?> get(int i);

    /**
     * Add a propagator to this.
     *
     * @param propagator the propagator to add
     * @param idxInVar   position of the variable in the propagator
     * @return the number of propagators stored
     */
    int add(Propagator<?> propagator, int idxInVar);

    /**
     * Remove the propagator <i>p</i> from this.
     *
     * @param propagator the propagator to remove
     * @param idxInProp  position of the variable in the propagator
     * @param var        the variable
     */
    void remove(Propagator<?> propagator, int idxInProp, final AbstractVariable var);

    /**
     * Swap the <i>propagator</i> to passivate status.
     *
     * @param propagator the propagator to swap
     * @param idxInProp  position of the variable in the propagator
     * @param var        the variable
     */
    void swap(Propagator<?> propagator, int idxInProp, final AbstractVariable var);

    /**
     * Schedule all active propagators in this
     *
     * @param cause  the cause of the call to this method
     * @param engine the propagation engine to add propagators to
     * @param mask   the modification due to cause
     */
    void schedule(ICause cause, PropagationEngine engine, int mask);

    Stream<Propagator<?>> stream();
}
