/*
 * This file is part of choco-solver, http://choco-solver.org/
 * Copyright (c) 1999, IMT Atlantique.
 * SPDX-License-Identifier: BSD-3-Clause.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.variables.delta;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 25/10/2021
 */
public interface IDeltaMonitor {
    /**
     * Activate the monitoring of this delta monitor.
     * A call to this method is required after initial propagation.
     * Indeed, calling this before the initial propagation may lead to error
     * since some removed values might be treated twice.
     * And not calling this method will throw an exception.
     */
    void startMonitoring();

}
