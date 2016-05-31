/**
 * Copyright (c) 2015, Ecole des Mines de Nantes
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. All advertising materials mentioning features or use of this software
 *    must display the following acknowledgement:
 *    This product includes software developed by the <organization>.
 * 4. Neither the name of the <organization> nor the
 *    names of its contributors may be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY <COPYRIGHT HOLDER> ''AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.chocosolver.memory.trailing.trail.chunck;


import org.chocosolver.memory.trailing.StoredInt;
import org.chocosolver.memory.trailing.trail.IStoredIntTrail;

/**
 * A trail for integers.
 *
 * @author Fabien Hermenier
 * @author Charles Prud'homme
 * @since 29/05/2016
 */
public class ChunckedIntTrail extends ChunckedTrail<IntWorld> implements IStoredIntTrail {

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
    public ChunckedIntTrail(int ws, int nbWorlds, double loadfactor) {
        worlds = new IntWorld[nbWorlds];
        this.ws = ws;
        this.loadfactor = loadfactor;
        worlds[0] = current = new IntWorld(ws, loadfactor);
    }

    @Override
    public void worldPush(int worldIndex) {
        if (worlds[worldIndex] == null) {
            current = new IntWorld(ws, loadfactor);
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
        IntWorld[] tmp = new IntWorld[newCapacity];
        System.arraycopy(worlds, 0, tmp, 0, worlds.length);
        worlds = tmp;
    }

    @Override
    public void savePreviousState(StoredInt v, int oldValue, int oldStamp) {
        current.savePreviousState(v, oldValue, oldStamp);
    }

    @Override
    public void buildFakeHistory(StoredInt v, int initValue, int olderStamp) {
        // first save the current state on the top of the stack
        savePreviousState(v, initValue, olderStamp - 1);
        // then rewrite older states
        for (int w = olderStamp; w > 1; w--) {
            IntWorld cur = worlds[olderStamp];
            cur.savePreviousState(v, initValue, w - 1);
        }
    }
}
