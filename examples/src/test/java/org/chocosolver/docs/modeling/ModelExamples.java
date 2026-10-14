/*
 * This file is part of examples, http://choco-solver.org/
 * Copyright (c) 1999, IMT Atlantique.
 * SPDX-License-Identifier: BSD-3-Clause.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.docs.modeling;

// tag::import[]
import org.chocosolver.solver.Model;
// end::import[]
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Examples shown in the user documentation, page "Modeling"
 * (docs/content/docs/Modeling/_index.md).
 * The regions between {@code tag::name[]} and {@code end::name[]} are included in the page.
 *
 * @author Charles Prud'homme
 */
public class ModelExamples {

    @Test(groups = "1s")
    public void model() {
        // tag::model[]
        Model model = new Model();
        // end::model[]
        Assert.assertNotNull(model.getSolver());
    }

    @Test(groups = "1s")
    public void namedModel() {
        // tag::namedModel[]
        Model model = new Model("my problem");
        // end::namedModel[]
        Assert.assertEquals(model.getName(), "my problem");
    }
}
