/*
 * This file is part of choco-solver, http://choco-solver.org/
 * Copyright (c) 1999, IMT Atlantique.
 * SPDX-License-Identifier: BSD-3-Clause.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.spec;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.Task;

/**
 * Maps the identifiers of a {@link ModelSpec} to the objects of one instantiated model.
 *
 * @author Charles Prud'homme
 */
public interface Resolver {

    /**
     * @return the model under instantiation (or instantiated)
     */
    Model model();

    /**
     * @param id an identifier
     * @return the object bound to <i>id</i>
     * @throws SpecException if <i>id</i> is not bound
     */
    Object get(int id);

    /**
     * @param id   an identifier
     * @param type expected type of the object
     * @return the object bound to <i>id</i>
     * @throws SpecException if <i>id</i> is not bound or not of the expected type
     */
    default <T> T get(int id, Class<T> type) {
        Object o = get(id);
        if (!type.isInstance(o)) {
            throw new SpecException("Identifier " + id + " is bound to a " + o.getClass().getSimpleName()
                    + ", not a " + type.getSimpleName());
        }
        return type.cast(o);
    }

    /**
     * @return an upper bound (exclusive) of the identifiers
     */
    int capacity();

    /**
     * @param id an identifier
     * @return <i>true</i> if <i>id</i> is bound to an object
     */
    boolean isBound(int id);

    /**
     * @param o an object of the model
     * @return the (first) identifier bound to <i>o</i>, or -1 if none
     */
    int idOf(Object o);

    default IntVar intVar(int id) {
        return get(id, IntVar.class);
    }

    default IntVar[] intVars(int... ids) {
        IntVar[] vars = new IntVar[ids.length];
        for (int i = 0; i < ids.length; i++) {
            vars[i] = intVar(ids[i]);
        }
        return vars;
    }

    default BoolVar boolVar(int id) {
        return get(id, BoolVar.class);
    }

    default Constraint constraint(int id) {
        return get(id, Constraint.class);
    }

    default Task task(int id) {
        return get(id, Task.class);
    }
}
