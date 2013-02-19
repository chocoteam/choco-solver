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

import solver.IEventType;

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
public enum EventType implements IEventType {

    VOID(0),
    //PROPAGATORS
    CUSTOM_PROPAGATION(1),
    FULL_PROPAGATION(2, 3),
    // INTVAR EVENT
    REMOVE(4),
    INCLOW(8, 12),
    DECUPP(16, 20),
    BOUND(24, 28),
    INSTANTIATE(32, 60),

    // GRAPHVAR EVENT
    REMOVENODE(64),
    ENFORCENODE(128),
    REMOVEARC(256),
    ENFORCEARC(512),

    // SET VARIABLE
    ADD_TO_KER(1024),
	REMOVE_FROM_ENVELOPE(2048),

	// META VARIABLE
	META(4096),

	//ALL FINE EVENTS (INTVAR+GRAPHVAR+SETVAR+METAVAR)
	ALL_FINE_EVENTS(8188);

    public final int mask;
    public final int strengthened_mask;

    EventType(int mask, int fullmask) {
        this.mask = mask;
        this.strengthened_mask = fullmask;
    }

    EventType(int mask) {
        this(mask, mask);
    }


    @Override
    public int getMask() {
        return mask;
    }

    @Override
    public int getStrengthenedMask() {
        return strengthened_mask;
    }

    //******************************************************************************************************************
    //******************************************************************************************************************

    public static int INT_ALL_MASK() {
        return INSTANTIATE.strengthened_mask;
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

    public static boolean anInstantiationEvent(int mask) {
        return (mask & INSTANTIATE.mask) != 0;
    }
}
