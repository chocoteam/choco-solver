/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.search.loop.monitors;

/**
 * An interface to monitor close operation of the search loop
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 13/12/12
 */
public interface IMonitorClose extends ISearchMonitor {
    /**
     * Actions to execute before closing the search
     */
    default void beforeClose(){
        // nothing to do by default
    }

    /**
     * Actions to execute after closing the search
     */
    default void afterClose(){
        // nothing to do by default
    }
}
