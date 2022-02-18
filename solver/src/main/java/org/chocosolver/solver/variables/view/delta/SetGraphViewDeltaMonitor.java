/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.variables.view.delta;

import org.chocosolver.solver.variables.delta.IGraphDeltaMonitor;
import org.chocosolver.solver.variables.delta.ISetDeltaMonitor;
/**
 * Delta monitor dedicated to set views over graph variables.
 *
 * @author Dimitri Justeau-Allaire
 * @since 20/08/2021
 */
public abstract class SetGraphViewDeltaMonitor implements ISetDeltaMonitor {

    protected IGraphDeltaMonitor deltaMonitor;

    public SetGraphViewDeltaMonitor(IGraphDeltaMonitor deltaMonitor) {
        this.deltaMonitor = deltaMonitor;
    }

    @Override
    public void startMonitoring() {
        deltaMonitor.startMonitoring();
    }
}
