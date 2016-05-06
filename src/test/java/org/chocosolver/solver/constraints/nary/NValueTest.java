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
/**
 * @author Jean-Guillaume Fages
 * @since 17/09/14
 * Created by IntelliJ IDEA.
 */
package org.chocosolver.solver.constraints.nary;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.search.loop.monitors.IMonitorSolution;
import org.chocosolver.solver.variables.IntVar;
import org.testng.annotations.Test;

import java.util.BitSet;

public class NValueTest {

	@Test(groups="1s", timeOut=60000)
	public void testAtLeast() {
        Model model = new Model();
        final IntVar[] XS = model.intVarArray("XS", 4, 0, 2, false);
        final IntVar N = model.intVar("N", 2, 3, false);
        model.atLeastNValues(XS, N, false).post();
        model.getSolver().showStatistics();
        model.getSolver().showSolutions();
        final BitSet values = new BitSet(3);
        model.getSolver().plugMonitor((IMonitorSolution) () -> {
            values.clear();
            for (IntVar v : XS) {
                if (!v.isInstantiated()) {
                    throw new UnsupportedOperationException();
                }
                values.set(v.getValue());
            }
            if (!N.isInstantiated()) {
                throw new UnsupportedOperationException();
            }
            if (values.cardinality() < N.getValue()) {
                throw new UnsupportedOperationException();
            }
        });
        while (model.getSolver().solve()) ;
    }

	@Test(groups="1s", timeOut=60000)
	public void testAtMost() {
        Model model = new Model();
        final IntVar[] XS = model.intVarArray("XS", 4, 0, 2, false);
        final IntVar N = model.intVar("N", 2, 3, false);
        model.atMostNValues(XS, N, false).post();
        model.getSolver().showStatistics();
        model.getSolver().showSolutions();
        final BitSet values = new BitSet(3);
        model.getSolver().plugMonitor((IMonitorSolution) () -> {
            values.clear();
            for (IntVar v : XS) {
                if (!v.isInstantiated()) {
                    throw new UnsupportedOperationException();
                }
                values.set(v.getValue());
            }
            if (!N.isInstantiated()) {
                throw new UnsupportedOperationException();
            }
            if (values.cardinality() > N.getValue()) {
                throw new UnsupportedOperationException();
            }
        });
        while (model.getSolver().solve()) ;
    }
}
