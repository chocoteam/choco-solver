/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2025, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.variables;

import java.util.Arrays;

/**
 * A simple class to group variables together.
 * </br>
 * This is useful to create a group of variables that are related to each other or have a specific semantic.
 * </br>
 * A name can be associated to a group, to help identifying it.
 * </br>
 * A variable should not belong to more than one group, but it is not enforced.
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 22/01/2025
 */
public class Group<V extends Variable> {

    /**
     * The name of the group
     */
    private final String name;

    /**
     * The variables in the group
     */
    private final V[] variables;

    /**
     * Create a group of variables
     *
     * @param name      name of the group
     * @param variables variables in the group
     */
    public Group(String name, V... variables) {
        this.name = name;
        this.variables = variables;
    }

    /**
     * @return the name of the group
     */
    public String getName() {
        return name;
    }

    /**
     * @return the variables in the group
     */
    public V[] getVariables() {
        return variables;
    }

    /**
     * @return the number of variables in the group
     */
    public int getSize() {
        return variables.length;
    }

    /**
     * @return a string representation of the group
     */
    @Override
    public String toString() {
        return name + ": " + Arrays.toString(variables);
    }
}
