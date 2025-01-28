/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2025, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * To annotation propagator that are explained
 * <br/>
 * Optional parameter: partial, to indicate that the explanation is partial. The default value is false.
 *
 * @author Charles Prud'homme
 * @since 19/10/2023
 */
@Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Explained {
    boolean ignored() default false;
    boolean partial() default false;
    String comment() default "";

}
