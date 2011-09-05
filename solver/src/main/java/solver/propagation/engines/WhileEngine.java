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

package solver.propagation.engines;

import solver.exception.ContradictionException;
import solver.propagation.engines.group.Group;
import solver.requests.IRequest;

import java.util.BitSet;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 24/04/11
 */
public class WhileEngine implements IEngine {

    protected Group[] groups;

    protected BitSet notEmpty;

    protected int nbGroup;

    public void setGroups(Group[] groups){
        this.groups = groups;
        nbGroup = groups.length;
        notEmpty = new BitSet(nbGroup);
    }

    @Override
    public void fixPoint() throws ContradictionException {
        int gidx = notEmpty.nextSetBit(0);
        while (gidx > -1) {
            if (groups[gidx].fixpoint()) {
                notEmpty.set(gidx, false);
            }
            gidx = notEmpty.nextSetBit(0);
        }
    }

    @Override
    public void update(IRequest request) {
        int gidx = request.getGroup();
        groups[gidx].update(request);
        notEmpty.set(gidx, true);

    }

    @Override
    public void remove(IRequest request) {
        int gidx = request.getGroup();
        if (groups[gidx].remove(request)) {
            notEmpty.set(gidx, false);
        }
    }

    @Override
    public void flushAll() {
        for (int i = nbGroup - 1; i >= 0; i--) {
            groups[i].flushAll();
        }
        notEmpty.clear();
    }
}
