/**
 * Copyright (c) 2014,
 *       Charles Prud'homme (TASC, INRIA Rennes, LINA CNRS UMR 6241),
 *       Jean-Guillaume Fages (COSLING S.A.S.).
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the <organization> nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.chocosolver.solver.search.strategy.selectors;

import org.chocosolver.solver.variables.Variable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.stream.Collectors;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 17/03/2014
 */
public class VariableSelectorWithTies<V extends Variable> implements VariableSelector<V> {

    final VariableEvaluator<V>[] heuristics;
    ArrayList<V> oldv = new ArrayList<>();
    ArrayList<V> newv = new ArrayList<>();


    @SafeVarargs
    public VariableSelectorWithTies(VariableEvaluator<V>... heuristics) {
        this.heuristics = heuristics;
    }


    @Override
    public V getVariable(V[] variables) {
        oldv.clear();
        newv.clear();
        Collections.addAll(oldv, variables);
        // 1. remove instantied variables
        newv.addAll(oldv.stream().filter(v -> !v.isInstantiated()).collect(Collectors.toList()));
        if (newv.size() == 0) return null;

        // Then apply each heuristic one by one
        for (VariableEvaluator<V> h : heuristics) {
            double minValue = Double.MAX_VALUE - 1;
            oldv.clear();
            oldv.addAll(newv);
            newv.clear();
            for (V v : oldv) {
                double val = h.evaluate(v);
                if (val < minValue) {
                    newv.clear();
                    newv.add(v);
                    minValue = val;
                } else if (val == minValue) {
                    newv.add(v);
                }
            }
        }
        switch (oldv.size()) {
            case 0:
                return null;
            default:
            case 1:
                return oldv.get(0);
        }
    }
}
