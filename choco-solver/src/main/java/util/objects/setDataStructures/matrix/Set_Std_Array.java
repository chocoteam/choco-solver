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

/**
 * Created by IntelliJ IDEA.
 * User: Jean-Guillaume Fages
 * Date: 27/10/12
 * Time: 01:43
 */

package util.objects.setDataStructures.matrix;

import memory.IEnvironment;
import memory.IStateBool;
import memory.IStateInt;
import util.objects.setDataStructures.ISet;

/**
 * Set represented by an array of backtrable booleans
 *
 * @author Jean-Guillaume Fages
 * @since Oct 2012
 */
public class Set_Std_Array implements ISet {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    protected IStateBool[] elements;
    private IStateInt size;
    private int n;
    protected int current;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    /**
     * Creates a set represented by an array of backtrable booleans
     *
     * @param n maximal size of the set
     */
    public Set_Std_Array(IEnvironment environment, int n) {
        this.n = n;
        this.elements = new IStateBool[n];
        this.size = environment.makeInt(0);
        for (int i = 0; i < n; i++) {
            elements[i] = environment.makeBool(false);
        }
    }

    //***********************************************************************************
    // METHODS
    //***********************************************************************************

    @Override
    public boolean add(int element) {
        if (!elements[element].get()) {
            size.add(1);
            elements[element].set(true);
            return true;
        }
        return false;
    }

    @Override
    public boolean remove(int element) {
        if (elements[element].get()) {
            size.add(-1);
            elements[element].set(false);
            return true;
        }
        return false;
    }

    @Override
    public boolean contain(int element) {
        return elements[element].get();
    }

    @Override
    public boolean isEmpty() {
        return size.get() == 0;
    }

    @Override
    public int getSize() {
        return size.get();
    }

    @Override
    public void clear() {
        int s = size.get();
        size.set(0);
        for (int i = 0; i < n && s > 0; i++) {
            if (elements[i].get()) s--;
            elements[i].set(false);
        }
    }

    @Override
    public int getFirstElement() {
        current = 0;
        return getNextElement();
    }

    @Override
    public int getNextElement() {
        int i = current;
        while (i < n && !elements[i].get()) {
            i++;
        }
        if (i < n) {
            current = i + 1;
            return i;
        }
        return -1;
    }
}
