/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2023, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.search.restart;

import org.chocosolver.solver.Solver;
import org.chocosolver.solver.exception.SolverException;

import java.util.function.IntSupplier;

/**
 * Defines the methods of a restart policy.
 * The main one is `mustRestart` which check wether a restort should be done.
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 14/10/2022
 */
public abstract class AbstractRestart {

    public static final AbstractRestart NO_RESTART = new AbstractRestart() {
        @Override
        public void setNext(AbstractRestart restart) {
            throw new UnsupportedOperationException("Cannot set 'next' restart policy to 'NO_RESTART'.");
        }
    };

    AbstractRestart next = null;

    public void init() {
    }

    public void setNext(AbstractRestart restart) {
        if(restart == null){
            throw new SolverException("Next restart policy cannot be null");
        }
        this.next = restart;
    }

    public AbstractRestart getNext() {
        return this.next;
    }

    public boolean mustRestart(Solver solver) {
        return false;
    }

    public void setGrower(IntSupplier grower) {
    }

}
