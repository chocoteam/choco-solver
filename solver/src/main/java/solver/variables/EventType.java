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

package solver.variables;

/**
 * An enum defining the variable event type:
 * <ul>
 * <li><code>REMOVE</code>: value removal event,</li>
 * <li><code>INSTANTIATE</code>: variable instantiation event,</li>
 * <li><code>PROPAGATE</code>: propagation event</li>
 * </ul>
 * <p/>
 * Each type includes a <code>mask</code> to speeds up comparisons between this <code>EventType</code> and
 * the filtered event mask of <code>Constraint</code> objects.
 *
 * @author Xavier Lorca
 * @author Charles Prud'homme
 * @version 0.01, june 2010
 * @see solver.constraints.Constraint
 * @since 0.01
 */
public enum EventType {

    VOID(0),
    PROPAGATE(1),
    // INTVAR EVENT
    REMOVE(2),
    INCLOW(4),
    DECUPP(8),
    BOUND(12),
    INSTANTIATE(16),
    // SETVAR
    REMENV( 1 << 5),
    ADDKER(1 << 6),
    SETINSTANTIATE(1 << 7),
    // GRAPHVAR EVENT
    REMOVENODE(1 << 8),
    ENFORCENODE(1 << 9),
    REMOVEARC(1 << 10),
    ENFORCEARC(1 << 11);

    public final int mask;

    EventType(int mask) {
        this.mask = mask;
    }

    public static int ALL_MASK() {
        return INSTANTIATE.mask + INCLOW.mask + DECUPP.mask + REMOVE.mask;
    }

    public static boolean isInstantiate(int mask) {
        return (mask & INSTANTIATE.mask) != 0;
    }

    public static boolean isRemove(int mask) {
        return (mask & REMOVE.mask) != 0;
    }

    public static boolean isBound(int mask) {
        return (mask & BOUND.mask) != 0;
    }

    public static boolean isInclow(int mask) {
        return (mask & INCLOW.mask) != 0;
    }

    public static boolean isDecupp(int mask) {
        return (mask & DECUPP.mask) != 0;
    }

    public static boolean isPropagate(int mask) {
        return (mask & PROPAGATE.mask) != 0;
    }

    public static boolean anInstantiationEvent(int mask){
        return (mask & (INSTANTIATE.mask + SETINSTANTIATE.mask)) != 0;
    }
}
