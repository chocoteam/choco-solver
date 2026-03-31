/*
 * This file is part of choco-solver, http://choco-solver.org/
 * Copyright (c) 1999, IMT Atlantique.
 * SPDX-License-Identifier: BSD-3-Clause.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.search.loop.monitors;

import org.chocosolver.solver.exception.ContradictionException;

/**
 * An interface to monitor contradiction in search loop
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 13/12/12
 */
public interface IMonitorContradiction extends ISearchMonitor {

    void onContradiction(ContradictionException cex);
}
