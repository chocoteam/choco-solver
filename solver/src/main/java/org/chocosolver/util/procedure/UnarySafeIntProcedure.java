/*
 * This file is part of choco-solver, http://choco-solver.org/
 * Copyright (c) 1999, IMT Atlantique.
 * SPDX-License-Identifier: BSD-3-Clause.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.util.procedure;

public interface UnarySafeIntProcedure<A> extends SafeIntProcedure {
    UnarySafeIntProcedure<A> set(A a);
}
