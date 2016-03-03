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
package org.chocosolver.solver.constraints.binary.element;

import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.variables.IntVar;

/**
 * A factory that selects the most adapted element propagator.
 * Created by cprudhom on 29/09/15.
 * Project: choco.
 */
public class ElementFactory {
    private ElementFactory() {
    }

    /**
     * Count the number of time the values in TABLE increase or decrease (ie, count picks and valleys).
     *
     * @param TABLE an array of values
     * @return the number of picks and valleys
     */
    private static int sawtooth(int[] TABLE) {
        int i = 0;
        while (i < TABLE.length - 1 && TABLE[i] == TABLE[i + 1]) {
            i++;
        }
        if (i == TABLE.length - 1) {
            return -1;
        }
        boolean up = TABLE[i] < TABLE[i + 1];
        int c = 0;
        i++;
        while (i < TABLE.length - 1) {
            if (up && TABLE[i] > TABLE[i + 1]) {
                c++;
                up = false;
            } else if (!up && TABLE[i] < TABLE[i + 1]) {
                c++;
                up = true;
            }
            i++;
        }
        return c;
    }

    /**
     * Detect and return the most adapted Element propagator wrt to the values in TABLE
     *
     * @param VALUE  the result variable
     * @param TABLE  the array of values
     * @param INDEX  the index variable
     * @param OFFSET the offset
     * @return an Element constraint
     */
    public static Constraint detect(IntVar VALUE, int[] TABLE, IntVar INDEX, int OFFSET) {
        // first chech the variables match
        int st = sawtooth(TABLE);
        if (st == -1) { // all values from TABLE are the same OR TABLE only contains one value
            assert TABLE[0] == TABLE[TABLE.length - 1];
            return VALUE.getModel().arithm(VALUE, "=", TABLE[0]);
        }
        return new Constraint("Element", new PropElement(VALUE, TABLE, INDEX, OFFSET));
    }
}
