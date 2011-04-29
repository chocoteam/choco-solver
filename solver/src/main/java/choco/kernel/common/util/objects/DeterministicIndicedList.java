/**
*  Copyright (c) 1999-2011, Ecole des Mines de Nantes
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

package choco.kernel.common.util.objects;

import choco.kernel.common.IIndex;
import choco.kernel.common.util.iterators.ArrayIterator;
import choco.kernel.common.util.iterators.DisposableIterator;
import gnu.trove.TLongIntHashMap;

import java.io.Serializable;
import static java.lang.reflect.Array.newInstance;
import java.util.Arrays;

/*
* User : charles
* Mail : cprudhom(a)emn.fr
* Date : 11 juin 2009
* Since : Choco 2.0.1
* Update : Choco 2.0.1
*
* Structure created to recorded data of the model.
* It includes an array of indiced objects,
* a hashmap of indices.
*
* It allows deterministic iteration.
*/
public class DeterministicIndicedList<O extends IIndex> implements Serializable{

    private final Class clazz;

    /**
     * Speed hash set of constraint indices
     */
    private final TLongIntHashMap indices;

    /**
     * All the object
     */
    private O[] objects;

    /**
     * indice of the last object
     */
    private int last;

    /**
     * Constructor
     * @param clazz the super Class of include object
     * @param initialSize initial size of the structure
     */
    public DeterministicIndicedList(final Class clazz, final int initialSize) {
        this.clazz = clazz;
        indices = new TLongIntHashMap(initialSize);
        objects = (O[]) newInstance(clazz, initialSize);
        last = 0;
    }

    /**
     * Constructor
     * @param clazz the super Class of include object
     */
    public DeterministicIndicedList(final Class clazz) {
        this(clazz, 32);
    }

    public void clear(){
        indices.clear();
        Arrays.fill(objects, null);
        objects = null;
    }

    /**
     * Add object to the structure
     * @param object
     */
    public void add(final O object){
        if(!indices.containsKey(object.getIndex())){
            ensureCapacity();
            objects[last] = object;
            indices.put(object.getIndex(), last++);
        }
    }

    /**
     * Ensure that the array has a correct size
     */
    @SuppressWarnings({"unchecked"})
    private void ensureCapacity(){
        if(last >= objects.length){
            // treat the case where intial value = 1
            final int cindT = objects.length * 3/2+1;
            final O[] oldObjects = objects;
            objects = (O[]) newInstance(clazz, cindT);
            System.arraycopy(oldObjects, 0, objects, 0, last);
        }
    }


    /**
     * Remove object from the structure
     * We just swap the last object and the removed object 
     * @param object to remove
     */
    public int remove(final O object){
        if(indices.containsKey(object.getIndex())){
            final int ind = indices.get(object.getIndex());
            indices.remove(object.getIndex());
            if(last > 0 && ind < last-1){
                objects[ind] = objects[last-1];
                indices.adjustValue(objects[ind].getIndex(), -last+ind+1);
            }
            objects[--last] = null;
            return ind;
        }
        return -1;
    }

    /**
     * Indicates wether the structure contains the object
     * @param object
     * @return
     */
    public boolean contains(final O object){
        return indices.containsKey(object.getIndex());
    }

    /**
     * Get the number of objects contained
     * @return
     */
    public int size(){
        return last;
    }

    /**
     * Get the object in position i
     * @param i position of the object
     * @return the ith object
     */
    public O get(final int i){
        return objects[i];
    }

    /**
     * Get the position of the object
     * @param object required
     * @return its position
     */
    public int get(final O object){
        return indices.get(object.getIndex());
    }


    public O getLast(){
        if(last>0){
            return objects[last-1];
        }else{
            return null;
        }
    }

    /**
     * Iterator over objects
     * @return
     */
    public DisposableIterator<O> iterator(){
        return ArrayIterator.getIterator(objects, last);
    }
}
