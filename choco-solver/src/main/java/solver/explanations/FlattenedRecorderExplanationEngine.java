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

package solver.explanations;

import solver.Solver;
import solver.explanations.antidom.AntiDomain;
import solver.variables.IntVar;

/**
 * Created by IntelliJ IDEA.
 * User: njussien
 * Date: 1 nov. 2010
 * Time: 09:27:43
 */
public class FlattenedRecorderExplanationEngine extends RecorderExplanationEngine {
    public FlattenedRecorderExplanationEngine(Solver slv) {
        super(slv);
    }

    @Override
    public Deduction explain(IntVar var, int val) {
        AntiDomain ad = getRemovedValues(var);
        return flatten(getValueRemoval(var, ad.getKeyValue(val)));
    }

    @Override
    public Deduction explain(Deduction deduction) {
        if (deduction.mType == Deduction.Type.DecRight) {
            return database.get(deduction.id);
        } else {
            return super.explain(deduction);
        }
    }

    @Override
    public Explanation flatten(IntVar var, int val) {
        AntiDomain ad = getRemovedValues(var);
        return flatten(getValueRemoval(var, ad.getKeyValue(val)));
    }


    @Override
    public Explanation flatten(Deduction deduction) {
        Explanation e = database.get(deduction.id);
        if (e == null) {
            e = Explanation.SYSTEM.get();
        }
        return e;
    }

}
