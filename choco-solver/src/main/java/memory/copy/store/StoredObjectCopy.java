/**
 *  Copyright (c) 1999-2014, Ecole des Mines de Nantes
 *  All rights reserved.
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 *      * Redistributions of source code must retain the above copyright
 *        notice, this list of conditions and the following disclaimer.
 *      * Redistributions in binary form must reproduce the above copyright
 *        notice, this list of conditions and the following disclaimer in the
 *        documentation and/or other materials provided with the distribution.
 *      * Neither the name of the Ecole des Mines de Nantes nor the
 *        names of its contributors may be used to endorse or promote products
 *        derived from this software without specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 *  EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 *  DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package memory.copy.store;

import memory.IStorage;
import memory.copy.RcObject;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 14/05/13
 */
public class StoredObjectCopy implements IStorage {

    RcObject[] objects;
    Object[][] values;
    int position;

    int lastSavedWorldIndex;

    public StoredObjectCopy(int worldIndex) {
        objects = new RcObject[64];
        lastSavedWorldIndex = worldIndex;
    }


    public void add(RcObject rc) {
        if (position == objects.length) {
            int newSize = objects.length * 3 / 2 + 1;
            RcObject[] oldElements = objects;
            objects = new RcObject[newSize];
            System.arraycopy(oldElements, 0, objects, 0, oldElements.length);
        }
        objects[position++] = rc;
    }

    public void worldPush(int worldIndex) {
        if (lastSavedWorldIndex >= worldIndex) lastSavedWorldIndex = 0;

        if (values.length >= worldIndex) {
            Object[][] tmp = values;
            values = new Object[worldIndex * 3 / 2 + 1][];
            System.arraycopy(tmp, 0, values, 0, worldIndex - 1);
        }
        Object[] tmpobj = new Object[objects.length];
        for (int i = objects.length; --i >= 0; ) {
//            tmpobj[i] = objects[i].deepCopy();
            if (worldIndex != 0 && lastSavedWorldIndex >= (objects[i]).getTimeStamp()) {
                tmpobj[i] = values[lastSavedWorldIndex][i];
            } else {
                tmpobj[i] = objects[i].deepCopy();
            }
        }
        values[worldIndex] = tmpobj;
    }

    public void worldPop(int worldIndex) {
        Object[] tmpobj = values[worldIndex];
        for (int i = tmpobj.length; --i >= 0; )
            objects[i]._set(tmpobj[i], worldIndex);
    }

    @Override
    public void worldCommit(int worldIndex) {
        throw new UnsupportedOperationException();
    }

}
