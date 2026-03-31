/*
 * This file is part of choco-solver, http://choco-solver.org/
 * Copyright (c) 1999, IMT Atlantique.
 * SPDX-License-Identifier: BSD-3-Clause.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.variables.view.delta;

import org.chocosolver.solver.variables.delta.IGraphDeltaMonitor;

/**
 * Delta monitor dedicated to graph views.
 *
 * @author Dimitri Justeau-Allaire
 * @since 20/08/2021
 */
public abstract class GraphViewDeltaMonitor implements IGraphDeltaMonitor {

    protected IGraphDeltaMonitor[] deltaMonitors;

    public GraphViewDeltaMonitor(IGraphDeltaMonitor... deltaMonitors) {
        this.deltaMonitors = deltaMonitors;
    }

    @Override
    public void startMonitoring() {
        for (IGraphDeltaMonitor d : deltaMonitors) {
            d.startMonitoring();
        }
    }
}
