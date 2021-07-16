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
 * An interface to monitor open node action in the search loop
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 13/12/12
 */
public interface IMonitorOpenNode extends ISearchMonitor {

    /**
     * Actions to execute before opening a node
     */
    default void beforeOpenNode(){
        // nothing to do by default
    }

    /**
     * Actions to execute after opening a node
     */
    default void afterOpenNode(){
        // nothing to do by default
    }

}
