/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.nary.automata.structure.regular;

import org.chocosolver.memory.structure.IndexedObject;
import org.chocosolver.solver.constraints.nary.automata.structure.Node;

/**
 * Created by IntelliJ IDEA.
 * User: julien
 * Date: Oct 30, 2009
 * Time: 3:48:11 PM
 */
public class Arc implements IndexedObject {

    public int id;
    public Node orig;
    public Node dest;
    public int value;


    public Arc(Node orig, Node dest, int value, int id) {
        this.id = id;
        this.orig = orig;
        this.dest = dest;
        this.value = value;
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
        arc.orig = orig.clone();
        arc.dest = dest.clone();
        arc.id = id;
        arc.value = value;
        return arc;
    }

}
