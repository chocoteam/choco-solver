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

package solver.search.strategy.enumerations.values.domains;

import solver.variables.IntVar;

import java.util.BitSet;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 31/01/11
 */
public class HeuristicValDomainImpl implements HeuristicValDomain {

    final IntVar ivar;

    BitSet bitset;

    int lower, upper;

    boolean updatedYet;
    boolean enumerated;

    public HeuristicValDomainImpl(IntVar ivar) {
        this.ivar = ivar;
        this.enumerated = ivar.hasEnumeratedDomain();

        updatedYet = false;
    }

    @Override
    public boolean contains(int val) {
        if (!updatedYet) {
            return ivar.contains(val);
        } else {
            if (enumerated) {
                val -= lower;
                return val >= 0 && bitset.get(val);
            } else {
                return lower <= val && val <= upper;
            }
        }
    }

    @Override
    public void update() {
        lower = ivar.getLB();
        upper = ivar.getUB();
        if (enumerated) {
            if (bitset == null) {
                bitset = new BitSet(ivar.getUB() - ivar.getLB() + 1);
            }
            bitset.clear();
            int ub = ivar.getUB();
            for (int val = ivar.getLB(); val <= ub; val = ivar.nextValue(val)) {
                bitset.set(val - lower, true);
            }
        }
        updatedYet = true;
    }
}
