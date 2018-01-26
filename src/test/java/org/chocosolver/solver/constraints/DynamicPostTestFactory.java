/**
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2018, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints;

import org.chocosolver.solver.propagation.PropagationEngineFactory;
import org.testng.annotations.Factory;

import java.util.ArrayList;
import java.util.List;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 17/12/12
 */
public class DynamicPostTestFactory {

    PropagationEngineFactory[] engines = new PropagationEngineFactory[]{
            PropagationEngineFactory.TWOBUCKETPROPAGATIONENGINE,
            PropagationEngineFactory.PROPAGATORDRIVEN_7QD};

    @Factory
    public Object[] createInstances() {
        List<Object> lresult = new ArrayList<>(12);

        for (int e = 0; e < engines.length; e++) {
            PropagationEngineFactory engine = engines[e];
            lresult.add(new DynamicPostTest(engine));
        }
        return lresult.toArray();
    }
}
