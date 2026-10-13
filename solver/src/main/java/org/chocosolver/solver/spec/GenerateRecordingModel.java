/*
 * This file is part of choco-solver, http://choco-solver.org/
 * Copyright (c) 1999, IMT Atlantique.
 * SPDX-License-Identifier: BSD-3-Clause.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.spec;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Asks the annotation processor {@code org.chocosolver.codegen.RecordingModelProcessor} (module choco-codegen) to
 * generate, at compile time, a subclass of the annotated class which journalizes every factory method.
 *
 * @author Charles Prud'homme
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
@interface GenerateRecordingModel {

    /**
     * @return the simple name of the generated class, in the package of the annotated class
     */
    String name();

    /**
     * @return the interface whose default methods are the factory methods to journalize
     */
    Class<?> factories();

    /**
     * @return names of the factory methods without effect on the model, which are not journalized
     */
    String[] excluded() default {};
}
