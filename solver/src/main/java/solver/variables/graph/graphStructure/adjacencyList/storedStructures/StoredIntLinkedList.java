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

package solver.variables.graph.graphStructure.adjacencyList.storedStructures;

import choco.kernel.memory.IEnvironment;
import solver.variables.graph.graphStructure.adjacencyList.IntCell;
import solver.variables.graph.graphStructure.adjacencyList.IntLinkedList;

/**
 * Created by IntelliJ IDEA.
 * User: chameau
 * Date: 10 févr. 2011
 */
public class StoredIntLinkedList extends IntLinkedList {

    /**
     * The first cell of the linked list
     */
    IntCell first;

    final IEnvironment environment;

    public StoredIntLinkedList(IEnvironment environment) {
        this.environment = environment;
        this.first = null;
    }

    @Override
    public void add(int element) {
        this._add(element);
        new RemOperation(environment, this, element);
    }

    protected void _add(int element) {
        super.add(element);
    }


    @Override
    public boolean remove(int element) {
        boolean done = this._remove(element);
        if (done) {
            new AddOperation(environment, this, element);
        }
        return done;
    }

    protected boolean _remove(int element) {
        return super.remove(element);
    }

}
