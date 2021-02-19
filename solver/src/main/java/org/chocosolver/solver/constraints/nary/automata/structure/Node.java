/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.nary.automata.structure;

/**
 * Created by IntelliJ IDEA.
 * User: julien
 * Date: Oct 30, 2009
 * Time: 3:46:54 PM
 */
public class Node implements Cloneable {


    public int id;
    public int state;
    public int layer;

    public Node(int state, int layer, int id) {
        this.id = id;
        this.state = state;
        this.layer = layer;
    }

    @Override
    public Node clone() throws CloneNotSupportedException {
        Node node = (Node) super.clone();
        node.id = id;
        node.state = state;
        node.layer = layer;
        return node;
    }
}
