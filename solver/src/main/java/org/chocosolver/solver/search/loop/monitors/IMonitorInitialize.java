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
 * An interface to monitor the initialization action of the search loop
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 13/12/12
 */
public interface IMonitorInitialize extends ISearchMonitor {

    /**
     * Actions to execute before initialisation of the solver
     */
    default void beforeInitialize(){
        // nothing to do by default
    }

    /**
     * Actions to execute after initialisation of the solver
     * @param correct equals <i>false</i> if initialization failed, <i>true</i> otherwise.
     */
    default void afterInitialize(boolean correct){
        // nothing to do by default
    }
}
