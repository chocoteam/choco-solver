/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.search.loop.lns.neighbors;

import gnu.trove.list.array.TIntArrayList;
import org.chocosolver.solver.Solution;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.SetVar;

import java.util.Random;

/**
 * A random LNS neighbor for a set variable
 * This class serves mostly as an example on how to use LNS with set variables.
 * <br/>
 *
 * @author Jean-Guillaume Fages
 * @since 18/01/19
 */
public class SetRandomNeighbor implements INeighbor {

    protected final SetVar var;
    protected final TIntArrayList value;
    protected final Random rd = new Random(0);

    public SetRandomNeighbor(SetVar variable) {
        this.var = variable;
        this.value = new TIntArrayList();
    }

    @Override
    public void recordSolution() {
        value.clear();
        for (int i:var.getLB()) {
            value.add(i);
        }
    }

    @Override
    public void fixSomeVariables() throws ContradictionException {
        for(int k=0; k<value.size(); k++){
            if(rd.nextBoolean())var.force(value.get(k), this);
        }
        for (int i:var.getLB()) {
            if(!var.getLB().contains(i) && rd.nextBoolean()){
                var.remove(i, this);
            }
        }
    }

    @Override
    public void loadFromSolution(Solution solution) {
        value.clear();
        for(int i:solution.getSetVal(var)){
            value.add(i);
        }
    }
}
