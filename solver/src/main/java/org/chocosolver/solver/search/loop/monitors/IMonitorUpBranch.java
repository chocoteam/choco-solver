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
 * An interface to monitor up branch actions in the search loop
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 13/12/12
 */
public interface IMonitorUpBranch extends ISearchMonitor {

    /**
     * Actions to execute before going up in the tree search
     */
    default void beforeUpBranch(){
        // nothing to do by default
    }

    /**
     * Actions to execute after going up in the tree search
     */
    default void afterUpBranch(){
        // nothing to do by default
    }
}
