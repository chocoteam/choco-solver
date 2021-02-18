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

import org.chocosolver.memory.IStorage;

/**
 * An abstract segmented trail.
 * @author Fabien Hermenier
 * @author Charles Prud'homme
 * @since 29/05/2016
 */
public abstract class ChunckedTrail<W extends World> implements IStorage{

    /**
     * The worlds.
     */
    protected W[] worlds;

    /**
     * The current world.
     */
    protected W current;


    /**
     * Moving down to the previous world.
     *
     * @param worldIndex current world index
     */
    @Override
    public void worldPop(int worldIndex) {
        current.revert();
        if (worldIndex > 0) {
            current = worlds[worldIndex - 1];
        }else {
            current = null;
        }
    }

    @Override
    public void worldCommit(int worldIndex) {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns the allocated trail size.
     *
     * @return a positive number
     */
    public int allocated() {
        int n = 0;
        for (World w : worlds) {
            if (w != null) {
                n += w.allocated();
            }
        }
        return n;
    }

}