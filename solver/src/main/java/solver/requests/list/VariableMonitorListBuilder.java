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

package solver.requests.list;

import choco.kernel.common.util.objects.HalfBactrackableList;
import choco.kernel.common.util.objects.IList;
import choco.kernel.common.util.objects.RequestArrayList;
import choco.kernel.memory.IEnvironment;
import solver.variables.IVariableMonitor;

/**
 * A class declaring builder for IList<IVariableMonitor>.
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 24/02/11
 */
public class VariableMonitorListBuilder {

    public static int _DEFAULT = 0; // 0: RequestArrayList, 1: HalfBactrackableList

    protected VariableMonitorListBuilder() {
    }

    /**
     * Builds and returns the preset IRequestList object.
     *
     * @param environment bracktrackable environment
     * @param dim         dimension to set and get index
     * @param <M>         type of element, should extends IVariableMonitor
     * @return a implementation of IRequestList
     */
    public static <M extends IVariableMonitor> IList<M> preset(IEnvironment environment, int dim) {
        switch (_DEFAULT) {
            case 1:
                return halfBacktracakbleList(environment, dim);
            default:
                return arraylist(environment, dim);
        }
    }

    public static <M extends IVariableMonitor> IList<M> arraylist(IEnvironment environment, int dim) {
        return new RequestArrayList<M>(environment, dim);
    }

    public static <M extends IVariableMonitor> IList<M> halfBacktracakbleList(IEnvironment environment, int dim) {
        return new HalfBactrackableList<M>(environment, dim);
    }
}
