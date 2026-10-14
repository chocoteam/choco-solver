/*
 * This file is part of examples, http://choco-solver.org/
 * Copyright (c) 1999, IMT Atlantique.
 * SPDX-License-Identifier: BSD-3-Clause.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.docs.modeling;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.SetVar;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Examples shown in the user documentation, page "Modeling / Constraints over set variables"
 * (docs/content/docs/Modeling/SetConstraints.md).
 * The regions between {@code tag::name[]} and {@code end::name[]} are included in the page.
 *
 * @author Charles Prud'homme
 */
public class SetConstraintsExamples {

    @Test(groups = "1s")
    public void member() {
        Model model = new Model();
        // tag::member[]
        SetVar s = model.setVar("s", new int[]{}, new int[]{0, 1, 2, 3, 4});
        model.member(2, s).post();       // 2 is in s
        model.notMember(3, s).post();    // 3 is not in s
        IntVar x = model.intVar("x", 0, 4);
        model.member(x, s).post();       // the value of x is in s
        // end::member[]
        Assert.assertTrue(model.getSolver().solve());
        Assert.assertTrue(s.getValue().contains(2));
        Assert.assertFalse(s.getValue().contains(3));
    }

    @Test(groups = "1s")
    public void subsetEq() {
        Model model = new Model();
        // tag::subsetEq[]
        SetVar s1 = model.setVar("s1", new int[]{}, new int[]{0, 1, 2, 3});
        SetVar s2 = model.setVar("s2", new int[]{0, 1}, new int[]{0, 1, 2, 3, 4});
        model.subsetEq(s1, s2).post();   // s1 ⊆ s2
        // end::subsetEq[]
        Assert.assertTrue(model.getSolver().solve());
    }

    @Test(groups = "1s")
    public void setLe() {
        Model model = new Model();
        // tag::setLe[]
        SetVar s1 = model.setVar("s1", new int[]{}, new int[]{0, 1, 2});
        SetVar s2 = model.setVar("s2", new int[]{}, new int[]{0, 1, 2});
        model.setLe(s1, s2).post();      // s1 ≤lex s2
        // end::setLe[]
        Assert.assertTrue(model.getSolver().solve());
    }

    @Test(groups = "1s")
    public void cardinality() {
        Model model = new Model();
        // tag::cardinality[]
        SetVar s = model.setVar("s", new int[]{}, new int[]{0, 1, 2, 3, 4});
        IntVar card = s.getCard();       // the number of elements of s
        model.arithm(card, "=", 2).post();
        // end::cardinality[]
        Assert.assertTrue(model.getSolver().solve());
        Assert.assertEquals(s.getValue().size(), 2);
    }

    @Test(groups = "1s")
    public void nbEmpty() {
        Model model = new Model();
        // tag::nbEmpty[]
        SetVar[] sets = model.setVarArray("S", 5, new int[]{}, new int[]{0, 1, 2, 3, 4});
        IntVar nbEmpty = model.intVar("nbEmpty", 2);
        model.nbEmpty(sets, nbEmpty).post();  // exactly 2 sets are empty
        model.notEmpty(sets[0]).post();       // sets[0] has at least one element
        // end::nbEmpty[]
        Assert.assertTrue(model.getSolver().solve());
    }

    @Test(groups = "1s")
    public void offSet() {
        Model model = new Model();
        // tag::offSet[]
        SetVar s1 = model.setVar("s1", new int[]{0, 2}, new int[]{0, 1, 2});
        SetVar s2 = model.setVar("s2", new int[]{}, new int[]{0, 1, 2, 3, 4, 5});
        model.offSet(s1, s2, 3).post();       // x in s1 <=> x + 3 in s2
        // end::offSet[]
        Assert.assertTrue(model.getSolver().solve());
        Assert.assertTrue(s2.getValue().contains(3));
        Assert.assertTrue(s2.getValue().contains(5));
    }

    @Test(groups = "1s")
    public void sum() {
        Model model = new Model();
        // tag::sum[]
        SetVar s = model.setVar("s", new int[]{}, new int[]{0, 1, 2, 3, 4});
        IntVar sum = model.intVar("sum", 0, 10);
        model.sum(s, sum).post();             // sum = Σ_{i ∈ s} i
        int[] weights = new int[]{1, 2, 3, 4, 5};
        IntVar weightedSum = model.intVar("weightedSum", 0, 15);
        model.sumElements(s, weights, weightedSum).post(); // weightedSum = Σ_{i ∈ s} weights[i]
        // end::sum[]
        Assert.assertTrue(model.getSolver().solve());
    }

    @Test(groups = "1s")
    public void unionIntersection() {
        Model model = new Model();
        // tag::union[]
        SetVar s1 = model.setVar("s1", new int[]{}, new int[]{0, 1, 2});
        SetVar s2 = model.setVar("s2", new int[]{}, new int[]{0, 1, 3});
        SetVar union = model.setVar("union", new int[]{}, new int[]{0, 1, 2, 3});
        model.union(new SetVar[]{s1, s2}, union).post();         // union = s1 ∪ s2
        SetVar inter = model.setVar("inter", new int[]{}, new int[]{0, 1, 2, 3});
        model.intersection(new SetVar[]{s1, s2}, inter).post();  // inter = s1 ∩ s2
        // end::union[]
        Assert.assertTrue(model.getSolver().solve());
    }

    @Test(groups = "1s")
    public void minMax() {
        Model model = new Model();
        // tag::minMax[]
        SetVar s = model.setVar("s", new int[]{}, new int[]{1, 2, 3, 4, 5});
        IntVar min = model.intVar("min", 1, 5);
        IntVar max = model.intVar("max", 1, 5);
        // true: s cannot be empty
        model.min(s, min, true).post();       // min = smallest element of s
        model.max(s, max, true).post();       // max = largest element of s
        // end::minMax[]
        Assert.assertTrue(model.getSolver().solve());
        Assert.assertEquals(min.getValue(), s.getValue().min());
    }

    @Test(groups = "1s")
    public void disjoint() {
        Model model = new Model();
        // tag::disjoint[]
        SetVar s1 = model.setVar("s1", new int[]{}, new int[]{0, 1, 2, 3});
        SetVar s2 = model.setVar("s2", new int[]{3}, new int[]{2, 3, 4, 5});
        model.disjoint(s1, s2).post();        // s1 ∩ s2 = ∅
        SetVar[] sets = model.setVarArray("S", 3, new int[]{}, new int[]{0, 1, 2, 3, 4, 5});
        model.allDisjoint(sets).post();       // the sets are pairwise disjoint
        // end::disjoint[]
        Assert.assertTrue(model.getSolver().solve());
    }

    @Test(groups = "1s")
    public void partition() {
        Model model = new Model();
        // tag::partition[]
        SetVar[] parts = model.setVarArray("parts", 3, new int[]{}, new int[]{0, 1, 2, 3, 4});
        SetVar universe = model.setVar("universe", 0, 1, 2, 3, 4);  // a constant set
        model.partition(parts, universe).post(); // parts are disjoint and their union is universe
        // end::partition[]
        Assert.assertTrue(model.getSolver().solve());
    }

    @Test(groups = "1s")
    public void allEqual() {
        Model model = new Model();
        // tag::allEqual[]
        SetVar[] sets = model.setVarArray("S", 3, new int[]{}, new int[]{0, 1, 2});
        model.allDifferent(sets).post();      // the sets are pairwise different (but not necessarily disjoint)
        SetVar[] others = model.setVarArray("T", 3, new int[]{}, new int[]{0, 1, 2});
        model.allEqual(others).post();        // the sets are all equal
        // end::allEqual[]
        Assert.assertTrue(model.getSolver().solve());
    }

    @Test(groups = "1s")
    public void setBoolsChanneling() {
        Model model = new Model();
        // tag::setBoolsChanneling[]
        SetVar s = model.setVar("s", new int[]{}, new int[]{0, 1, 2, 3});
        BoolVar[] bools = model.boolVarArray("b", 4);
        model.setBoolsChanneling(bools, s).post();    // bools[i] = 1 <=> i ∈ s
        // end::setBoolsChanneling[]
        Assert.assertTrue(model.getSolver().solve());
    }

    @Test(groups = "1s")
    public void setsIntsChanneling() {
        Model model = new Model();
        // tag::setsIntsChanneling[]
        IntVar[] x = model.intVarArray("x", 5, 0, 2);
        SetVar[] s = model.setVarArray("s", 3, new int[]{}, new int[]{0, 1, 2, 3, 4});
        model.setsIntsChanneling(s, x).post();         // i ∈ s[j] <=> x[i] = j
        // end::setsIntsChanneling[]
        Assert.assertTrue(model.getSolver().solve());
        Assert.assertTrue(s[x[0].getValue()].getValue().contains(0));
    }

    @Test(groups = "1s")
    public void sortedSetIntsChanneling() {
        Model model = new Model();
        // tag::sortedSetIntsChanneling[]
        SetVar s = model.setVar("s", new int[]{1, 3}, new int[]{0, 1, 2, 3, 4});
        IntVar[] x = model.intVarArray("x", 5, -1, 4);
        // x[i] is the i-th smallest element of s, or -1 if s has fewer than i+1 elements
        model.sortedSetIntsChanneling(s, x, -1, 0).post();
        // end::sortedSetIntsChanneling[]
        Assert.assertTrue(model.getSolver().solve());
    }

    @Test(groups = "1s")
    public void symmetric() {
        Model model = new Model();
        // tag::symmetric[]
        SetVar[] sets = model.setVarArray("S", 3, new int[]{}, new int[]{0, 1, 2});
        model.symmetric(sets).post();   // i ∈ sets[j] <=> j ∈ sets[i]
        // end::symmetric[]
        Assert.assertTrue(model.getSolver().solve());
    }

    @Test(groups = "1s")
    public void inverseSet() {
        Model model = new Model();
        // tag::inverseSet[]
        SetVar[] sets = model.setVarArray("S", 3, new int[]{}, new int[]{0, 1, 2});
        SetVar[] inverses = model.setVarArray("I", 3, new int[]{}, new int[]{0, 1, 2});
        model.inverseSet(sets, inverses, 0, 0).post();  // i ∈ sets[j] <=> j ∈ inverses[i]
        // end::inverseSet[]
        Assert.assertTrue(model.getSolver().solve());
    }

    @Test(groups = "1s")
    public void element() {
        Model model = new Model();
        // tag::element[]
        SetVar[] sets = model.setVarArray("sets", 4, new int[]{}, new int[]{0, 1, 2, 3});
        IntVar index = model.intVar("index", 0, 3);
        SetVar result = model.setVar("result", new int[]{}, new int[]{0, 1, 2, 3});
        model.element(index, sets, result).post();      // sets[index] = result
        // end::element[]
        Assert.assertTrue(model.getSolver().solve());
    }
}
