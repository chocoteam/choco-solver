/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.util.objects;

import org.chocosolver.memory.IEnvironment;

/**
 * Created by IntelliJ IDEA.
 * User: julien
 * Mail: julien.menana{at}emn.fr
 * Date: Nov 5, 2009
 * Time: 12:40:42 PM
 */
public class StoredIndexedBipartiteSetWithOffset extends StoredIndexedBipartiteSet {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    private int offset;

    //***********************************************************************************
    // CONSTRUCTOR
    //***********************************************************************************

    public StoredIndexedBipartiteSetWithOffset(IEnvironment environment, int[] values) {
        super(environment, values);
    }

    //***********************************************************************************
    // METHODS
    //***********************************************************************************

    public void buildList(IEnvironment environment, int[] values) {
        this.list = values;
        int maxElt = 0;
        int minElt = Integer.MAX_VALUE;
        for (int value : values) {
            if (value > maxElt) maxElt = value;
            if (value < minElt) minElt = value;
        }
        this.offset = minElt;
        this.position = new int[maxElt - offset + 1];
        for (int i = 0; i < values.length; i++) {
            position[values[i] - offset] = i;
        }
        this.last = environment.makeInt(list.length - 1);

    }

    public boolean contain(int object) {
        return position[object - offset] <= last.get();
    }

    public void remove(int object) {
        if (contain(object)) {
            int idxToRem = position[object - offset];
            if (idxToRem == last.get()) {
                last.add(-1);
            } else {
                int temp = list[last.get()];
                list[last.get()] = object;
                list[idxToRem] = temp;
                position[object - offset] = last.get();
                position[temp - offset] = idxToRem;
                last.add(-1);
            }
        }
    }

    public final int getOffset() {
        return offset;
    }
}
