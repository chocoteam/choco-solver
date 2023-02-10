/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2023, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver;

/**
 * An interface to allow adding labels to variables or constraints.
 *
 * @author Charles Prud'homme
 * @since 10/02/2023
 */
public interface Ilabelable {
    /**
     * Add a label
     *
     * @param label to add
     */
    void addLabel(String label);

    /**
     * Clear a label
     *
     * @param label to clear
     */
    void remLabel(String label);

    /**
     * Clear all labels
     */
    void clearLabels();

    /**
     * @param label a label
     * @return <i>true</i> if this is labeled with <i>label</i>, false otherwise
     */
    boolean isLabeled(String label);
}
