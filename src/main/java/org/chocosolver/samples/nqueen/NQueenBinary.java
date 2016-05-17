/**
 * Copyright (c) 2016, chocoteam
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * * Neither the name of samples nor the names of its
 *   contributors may be used to endorse or promote products derived from
 *   this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.chocosolver.samples.nqueen;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.constraints.Constraint;

import java.util.HashSet;
import java.util.stream.IntStream;

import static org.chocosolver.solver.search.strategy.SearchStrategyFactory.minDomLBSearch;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 31/03/11
 */
public class NQueenBinary extends AbstractNQueen {

    HashSet<Constraint> set;

    @Override
    public void buildModel() {
        model = new Model("NQueen");
        vars = model.intVarArray("Q", n, 1, n);
        IntStream.range(0, n-1).forEach(i ->
                IntStream.range(i+1, n).forEach(j ->{
                    vars[i].ne(vars[j]).post();
                    vars[i].ne(vars[j].sub(j - i)).post();
                    vars[i].ne(vars[j].add(j - i)).post();
                })
        );
        model.getSolver().solve();
    }



    @Override
    public void configureSearch() {
        model.getSolver().set(minDomLBSearch(vars));
//        SearchMonitorFactory.log(solver, true, false);
    }

    public static void main(String[] args) {
        new NQueenBinary().execute(args);
    }
}
