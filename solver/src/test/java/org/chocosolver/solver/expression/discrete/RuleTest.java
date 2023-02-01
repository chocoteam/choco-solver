/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.expression.discrete;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.constraints.binary.PropGreaterOrEqualXY_C;
import org.chocosolver.solver.constraints.binary.PropScale;
import org.chocosolver.solver.constraints.unary.PropEqualXC;
import org.chocosolver.solver.expression.discrete.arithmetic.ArExpression;
import org.chocosolver.solver.expression.discrete.arithmetic.Rule;
import org.chocosolver.solver.expression.discrete.logical.LoExpression;
import org.chocosolver.solver.expression.discrete.relational.ReExpression;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 30/11/2022
 */
public class RuleTest {

    @Test(groups = "1s")
    public void testRewrite1() {
        Model model = new Model("rewrite1");
        IntVar x = model.intVar("x", 0, 10);
        IntVar y = model.intVar("y", 0, 10);

        List<Rule<ArExpression>> rules = new ArrayList<>();
        rules.add(new Rule<>(
                Rule.isOperator(ArExpression.Operator.ADD),
                Rule.twoIdentical(e -> e.mul(2))));
        rules.add(new Rule<>(
                Rule.isOperator(ArExpression.Operator.SUB),
                Rule.twoIdentical(e -> model.intVar(0))));

        ReExpression exp = x.add(x).eq(y.sub(y));
        Assert.assertTrue(exp.decompose().getPropagator(0) instanceof PropScale);
        exp = exp.rewrite(rules);
        System.out.printf("%s\n", exp);
        Assert.assertTrue(exp.decompose().getPropagator(0) instanceof PropEqualXC);
    }

    @Test(groups = "1s")
    public void testRewrite2() {
        Model model = new Model("rewrite2");
        BoolVar x = model.boolVar("x");
        BoolVar y = model.boolVar("y");

        List<Rule<ArExpression>> rules = new ArrayList<>();
        rules.add(new Rule<>(
                Rule.isOperator(LoExpression.Operator.OR),
                Rule.twoIdentical(e -> e)));

        ReExpression exp = x.or(x).or(x).or(y).rewrite(rules);
        System.out.printf("%s\n", exp);
        Assert.assertTrue(exp.decompose().getPropagator(0) instanceof PropGreaterOrEqualXY_C);
    }
}
