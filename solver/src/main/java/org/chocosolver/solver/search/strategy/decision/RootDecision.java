/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.search.strategy.decision;


import org.chocosolver.solver.exception.ContradictionException;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 01/06/12
 */
public class RootDecision extends Decision {
    private static final long serialVersionUID = -5266363788310578598L;
    public static final RootDecision ROOT = new RootDecision();

    private RootDecision() {
        super(1);
    }

    // FOR SERIALIZATION
    private Object readResolve() {
        return ROOT;
    }
    @Override
    public Object getDecisionValue() {
        return null;
    }


    @Override
    public void apply() throws ContradictionException {
    }

    @Override
    public void free() {
    }

    @Override
    public void reverse() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String toString() {
        return "ROOT";
    }
}
