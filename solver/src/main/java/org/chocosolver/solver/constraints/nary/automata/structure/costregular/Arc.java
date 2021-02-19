/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.nary.automata.structure.costregular;

import org.chocosolver.solver.constraints.nary.automata.structure.Node;

/**
 * Created by IntelliJ IDEA.
 * User: julien
 * Date: Oct 30, 2009
 * Time: 3:48:11 PM
 */
public class Arc extends org.chocosolver.solver.constraints.nary.automata.structure.regular.Arc {

    public double cost;

    public Arc(Node orig, Node dest, int value, int id, double cost) {
        super(orig, dest, value, id);
        this.cost = cost;
    }

    public Arc(Node orig, Node dest, int value) {
        this(orig, dest, value, Integer.MIN_VALUE, Double.POSITIVE_INFINITY);
    }

    public String toString() {
        return value + "";
    }

    @Override
    public int getObjectIdx() {
        return orig.state;
    }

    @Override
    public Arc clone() throws CloneNotSupportedException {
        Arc arc = (Arc) super.clone();
        arc.cost = cost;
        return arc;
    }
}
