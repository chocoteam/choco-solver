/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.objective;

import org.chocosolver.solver.ResolutionPolicy;
import org.chocosolver.solver.variables.Variable;

import java.util.Objects;
import java.util.function.Function;

/**
 * This class defines common methods to COP based on maximization or minimization of integer or real variable
 * @author Jean-Guillaume Fages, Charles Prud'homme, Arnaud Malapert
 *
 */
abstract class AbstractObjManager<V extends Variable> implements IObjectiveManager<V> {

    private static final long serialVersionUID = 4330218142281861652L;

    /** The variable to optimize **/
    transient protected final V objective;

    /** Define how should the objective be optimize */
    protected final ResolutionPolicy policy;

    /** define the precision to consider a variable as instantiated **/
    protected final Number precision;

    /** best lower bound found so far **/
    protected Number bestProvedLB;

    /** best upper bound found so far **/
    protected Number bestProvedUB;

    /** Define how the cut should be update when posting the cut **/
    transient protected Function<Number, Number> cutComputer = n -> n; // walking cut by default

    public AbstractObjManager(AbstractObjManager<V> objman) {
        objective = objman.objective;
        policy = objman.policy;
        precision = objman.precision;
        bestProvedLB = objman.bestProvedLB;
        bestProvedUB = objman.bestProvedUB;
        cutComputer = objman.cutComputer;
    }


    public AbstractObjManager(V objective, ResolutionPolicy policy, Number precision) {
        super();
        assert Objects.nonNull(objective);
        this.objective = objective;
        assert Objects.nonNull(policy);
        this.policy = policy;
        assert Objects.nonNull(precision);
        this.precision = precision;
    }

    @Override
    public final V getObjective() {
        return objective;
    }

    @Override
    public final ResolutionPolicy getPolicy() {
        return policy;
    }

    @Override
    public final Number getBestLB() {
        return bestProvedLB;
    }

    @Override
    public final Number getBestUB() {
        return bestProvedUB;
    }

    @Override
    public final void setCutComputer(Function<Number, Number> cutComputer) {
        this.cutComputer = cutComputer;
    }

    @Override
    public final void setWalkingDynamicCut() {
        cutComputer = (obj) -> obj;
    }


}