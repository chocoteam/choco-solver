/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2025, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.variables.impl.lazyness;

import org.chocosolver.sat.MiniSat;
import org.chocosolver.sat.Reason;
import org.chocosolver.solver.variables.impl.IntVarLazyLit;

/**
 * Interface to define the way to link a CP bounded variable to a SAT variable.
 * Two implementations are provided: {@link WeakBound} and {@link StrongBound}.
 * The former is faster but provides weaker reasons, the latter is slower but provides stronger reasons.
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 13/09/2024
 */
public interface ILazyBound {

    /**
     * @return the SAT variable that represents the current lower bound
     */
    int currentMinVar();

    /**
     * @return the SAT variable that represents the current upper bound
     */
    int currentMaxVar();

    /**
     * Return or create the SAT variable that represents the value of the variable
     * @param value the value to represent
     * @param cvar the CP variable to link
     * @param sat the SAT solver
     * @return the SAT variable that represents the value
     */
    int getSATVar(int value, IntVarLazyLit cvar, MiniSat sat);

    /**
     * Channel the lower bound of the variable to the SAT solver
     * @param value the new lower bound
     * @param sat the SAT solver
     * @param r the reason why the bound has been modified
     */
    void channelMin(int value, MiniSat sat, Reason r);

    /**
     * Channel the upper bound of the variable to the SAT solver
     * @param value the new upper bound
     * @param sat the SAT solver
     * @param r the reason why the bound has been modified
     */
    void channelMax(int value, MiniSat sat, Reason r);
}
