/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.search.loop.monitors;

/**
 * An interface to monitor restart instruction in the search loop
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 13/12/12
 */
public interface IMonitorRestart extends ISearchMonitor {

    /**
     * Actions to execute before restarting the search
     */
    default void beforeRestart(){
        // nothing to do by default
    }

    /**
     * Actions to execute after restarting the search
     */
    default void afterRestart(){
        // nothing to do by default
    }
}
