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
 * An interface to monitor down branch actions in the search loop
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 13/12/12
 */
public interface IMonitorDownBranch extends ISearchMonitor {


    /**
     * Action to perform <u>before</u> going down in the tree search
     * @param left set to <tt>true</tt> to specify that this is a left branch
     */
    default void beforeDownBranch(boolean left){
        // nothing to do by default
    }

    /**
     * Action to perform <u>after</u> going down in the tree search
     * @param left set to <tt>true</tt> to specify that this is a left branch
     */
    default void afterDownBranch(boolean left){
        // nothing to do by default
    }

}
