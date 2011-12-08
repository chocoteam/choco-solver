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
package solver.recorders.fine;

import solver.variables.EventType;
import solver.variables.IntVar;
import solver.variables.Variable;

/**
 * An event adaptor, that permits to transform and adapt , on the fly, an event for views.
 * For instance, the view MINUS requires to inverse bound events.
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 06/12/11
 */
public interface EventAdaptor<V extends Variable> {

    abstract EventType update(V var, EventType e);

    public static enum IntFactory implements EventAdaptor<IntVar> {
        NONE() {
            @Override
            public EventType update(IntVar var, EventType e) {
                return e;
            }
        },
        MINUS() {
            @Override
            public EventType update(IntVar var, EventType eventType) {
                if (eventType == EventType.INCLOW || eventType == EventType.DECUPP) {
                    return (eventType == EventType.INCLOW ? EventType.DECUPP : EventType.INCLOW);
                }
                return eventType;
            }
        },
        ABS {
            @Override
            public EventType update(IntVar var, EventType eventType) {
                if (var.instantiated()) {
                    return EventType.INSTANTIATE;
                } else {
                    if (var.getDomainSize() == 2 && Math.abs(var.getLB()) == var.getUB()) {
                        return EventType.INSTANTIATE;
                    }
                }
                return eventType;
            }
        };
    }

}
