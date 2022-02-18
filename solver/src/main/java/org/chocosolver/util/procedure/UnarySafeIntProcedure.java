/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.util.procedure;

public interface UnarySafeIntProcedure<A> extends SafeIntProcedure {
    UnarySafeIntProcedure<A> set(A a);
}
