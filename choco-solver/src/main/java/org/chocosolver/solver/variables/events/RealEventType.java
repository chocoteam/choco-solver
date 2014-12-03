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

package org.chocosolver.solver.variables.events;

/**
 * An enum defining the real variable event types:
 * <ul>
 * <li><code>INCLOW</code>: lower bound increase event,</li>
 * <li><code>DECUPP</code>: upper bound decrease event,</li>
 * <li><code>BOUND</code>: lower bound increase and/or upper bound decrease event,</li>
 * </ul>
 * <p/>
 * @author Charles Prud'homme, Jean-Guillaume Fages
 */
public enum RealEventType implements IEventType {

    VOID(0),
    INCLOW(1),
    DECUPP(2),
    BOUND(3);

    private final int mask;

	private RealEventType(int mask) {
        this.mask = mask;
    }

    @Override
    public int getMask() {
        return mask;
    }

    @Override
    public int getStrengthenedMask() {
        return mask;
    }

    //******************************************************************************************************************
    //******************************************************************************************************************

    public static boolean isBound(int mask) {
        return (mask & BOUND.mask) != 0;
    }

    public static boolean isInclow(int mask) {
        return (mask & INCLOW.mask) != 0;
    }

    public static boolean isDecupp(int mask) {
        return (mask & DECUPP.mask) != 0;
    }
}
