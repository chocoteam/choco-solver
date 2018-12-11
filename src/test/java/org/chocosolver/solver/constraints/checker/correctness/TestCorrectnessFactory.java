/**
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2018, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.checker.correctness;

import org.testng.annotations.Factory;

import java.util.ArrayList;
import java.util.List;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 1 oct. 2010
 */
public class TestCorrectnessFactory {

    @Factory
    public Object[] createInstances() {
        List<Object> lresult = new ArrayList<>(12);
        lresult.add(new TestCorrectness());
        return lresult.toArray();
    }

}
