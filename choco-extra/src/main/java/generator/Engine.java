/*
 * Copyright (c) 1999-2012, Ecole des Mines de Nantes
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Ecole des Mines de Nantes nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package generator;

import solver.constraints.Propagator;
import solver.exception.ContradictionException;
import util.objects.IntCircularQueue;

import java.util.BitSet;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 12/10/12
 */
public class Engine {

    Propagator[] prop_0;
    BitSet toPropagate_0;

    Propagator[] prop_1;
    BitSet toPropagate_1;

    Propagator[] prop_2;
    IntCircularQueue toPropagate_2;

    Propagator[] prop_3;
    IntCircularQueue toPropagate_3;

    Propagator[][] prop_4n;
    BitSet[] toPropagate_4n;

    public void propagate() throws ContradictionException {
        prop_0();
    }

    private void prop_0() throws ContradictionException {
        // wone + 1 child
        for (int i = 0; i > -1; i = toPropagate_1.nextSetBit(0)) {
            switch (i) {
                case 0:
                    prop_1();
                    toPropagate_0.set(i, toPropagate_1.isEmpty());
                    break;
            }
        }
    }

    private void prop_1() throws ContradictionException {
        // wone + 2 child
        for (int i = 0; i > -1; i = toPropagate_1.nextSetBit(0)) {
            switch (i) {
                case 0:
                    prop_2();
                    toPropagate_1.set(0, toPropagate_2.isEmpty());
                    break;
                case 1:
                    prop_3();
                    toPropagate_1.set(1, toPropagate_3.isEmpty());
                    break;
            }
        }
    }


    private void prop_2() throws ContradictionException {
        // wone + n child
        while (!toPropagate_2.isEmpty()) {
            int i = toPropagate_2.pollLast();
            prop_4n(i);
            if (!toPropagate_4n[i].isEmpty()) {
                toPropagate_2.addFirst(i);
            }
        }
    }

    private void prop_3() throws ContradictionException {
        // one + no child
        if (!toPropagate_3.isEmpty()) {
            int i = toPropagate_3.pollLast();
            prop_3[i].propagate(-1, -1);
            if (!toPropagate_3.isEmpty()) {
                toPropagate_3.addFirst(i);
            }
        }
    }

    private void prop_4n(int n) throws ContradictionException {
        // wfor + no child
        int i = toPropagate_4n[n].nextSetBit(0);
        while (!toPropagate_4n[n].isEmpty()) {
            prop_4n[n][i].propagate(-1, -1);
            i = toPropagate_4n[n].nextSetBit(i + 1);
            if (i == -1) {
                i = toPropagate_4n[n].nextSetBit(0);
            }
        }
    }


}
