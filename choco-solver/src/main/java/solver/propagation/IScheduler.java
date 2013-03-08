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
package solver.propagation;

import solver.exception.ContradictionException;

/**
 * An interface for objects that can schedule element.
 * An element is schedulable when it implements ISchedulable.
 * <br/>
 *
 * @author Charles Prud'homme
 * @revision 04/03/12 add update feature
 * @since 05/12/11
 */
public interface IScheduler<S extends ISchedulable> extends IExecutable {

    /**
     * Schedule an element
     *
     * @param element to schedule
     */
    void schedule(S element);


    /**
     * Remove a scheduled element
     *
     * @param element to remove
     */
    void remove(S element);

    /**
     * Flush all the scheduled elements
     */
    void flush();

    /**
     * Does this require to be informed of element modification
     *
     * @return <code>true</code> if this needs to be informed of any modification of its elements
     */
    boolean needUpdate();

    void update(S element);

    public static enum Default implements IScheduler {
        NONE() {
            @Override
            public void schedule(ISchedulable element) {
            }

            @Override
            public void remove(ISchedulable element) {
            }

            @Override
            public boolean execute() throws ContradictionException {
                return true;
            }

            @Override
            public boolean needUpdate() {
                return false;
            }

            @Override
            public void update(ISchedulable element) {
            }

            @Override
            public void flush() {
            }
        }

    }
}
