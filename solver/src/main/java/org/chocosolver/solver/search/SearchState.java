/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.search;

import java.io.Serializable;

/**
 * Indication about the search status
 * <p>
 * Project: choco-solver.
 *
 * @author Charles Prud'homme
 * @since 04/05/2016.
 */
public enum SearchState implements Serializable {

    /**
     * A search that has not yet started is in this state.
     */
    NEW,
    /**
     * A launched search is in this state.
     */
    RUNNING,
    /**
     * A search that ends normally is in this state.
     */
    TERMINATED,
    /**
     * A search that ends on a stop criterion is in this state.
     */
    STOPPED,
    /**
     * A search that is killed from the outside (eg, thread interruption, JVM killed)is in this state.
     */
    KILLED

}
