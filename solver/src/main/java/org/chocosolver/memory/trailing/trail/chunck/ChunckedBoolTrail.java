/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.memory.trailing.trail.chunck;


import org.chocosolver.memory.trailing.StoredBool;
import org.chocosolver.memory.trailing.trail.IStoredBoolTrail;

/**
 * A trail for integers.
 *
 * @author Fabien Hermenier
 * @author Charles Prud'homme
 * @since 29/05/2016
 */
public class ChunckedBoolTrail extends ChunckedTrail<BoolWorld> implements IStoredBoolTrail {

    private final int ws;

    /**
     * Load factor
     */
    private final double loadfactor;

    /**
     * Constructs a trail with predefined size and loadfactor
     * @param ws the initial world size
     * @param nbWorlds the initial number of worlds
     * @param loadfactor how to resize world
     */
    public ChunckedBoolTrail(int ws, int nbWorlds, double loadfactor) {
        worlds = new BoolWorld[nbWorlds];
        this.ws = ws;
        this.loadfactor = loadfactor;
        worlds[0] = current = new BoolWorld(ws, loadfactor);
    }

    @Override
    public void worldPush(int worldIndex) {
        if (worlds[worldIndex] == null) {
            current = new BoolWorld(ws, loadfactor);
            worlds[worldIndex] = current;
        } else {
            current = worlds[worldIndex];
            current.clear();
        }
        if (worldIndex == worlds.length - 1) {
            resizeWorlds();
        }
    }

    private void resizeWorlds() {
        int newCapacity = (int) (worlds.length * loadfactor);
        BoolWorld[] tmp = new BoolWorld[newCapacity];
        System.arraycopy(worlds, 0, tmp, 0, worlds.length);
        worlds = tmp;
    }

    @Override
    public void savePreviousState(StoredBool v, boolean oldValue, int oldStamp) {
        current.savePreviousState(v, oldValue, oldStamp);
    }

    @Override
    public void buildFakeHistory(StoredBool v, boolean initValue, int olderStamp) {
        // rewrite older states
        for (int w = olderStamp; w > 0; w--) {
            BoolWorld cur = worlds[w];
            cur.savePreviousState(v, initValue, w - 1);
        }
    }
}
