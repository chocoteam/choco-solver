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

package solver.variables.delta;

import choco.kernel.common.util.procedure.IntProcedure;
import solver.exception.ContradictionException;

public class GraphDelta implements IGraphDelta {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    private IntDelta nodeEnf, nodeRem, arcEnf, arcRem;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    public GraphDelta() {
        nodeEnf = new Delta();
        nodeRem = new Delta();
        arcEnf = new Delta();
        arcRem = new Delta();
    }

    //***********************************************************************************
    // METHODS
    //***********************************************************************************

    @Override
    public int size() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void forEach(IntProcedure proc, int from, int to) throws ContradictionException {
        throw new UnsupportedOperationException();
    }

    //***********************************************************************************
    // ACCESSORS
    //***********************************************************************************

    @Override
    public IntDelta getNodeRemovalDelta() {
        return nodeRem;
    }

    @Override
    public IntDelta getNodeEnforcingDelta() {
        return nodeEnf;
    }

    @Override
    public IntDelta getArcRemovalDelta() {
        return arcRem;
    }

    @Override
    public IntDelta getArcEnforcingDelta() {
        return arcEnf;
    }
}
