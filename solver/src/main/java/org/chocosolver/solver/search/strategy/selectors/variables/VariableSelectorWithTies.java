/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.search.strategy.selectors.variables;

import org.chocosolver.solver.variables.Variable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 17/03/2014
 */
public class VariableSelectorWithTies<V extends Variable> implements VariableSelector<V> {

    private final VariableEvaluator<V>[] heuristics;
    private ArrayList<V> oldv = new ArrayList<>();
    private ArrayList<V> newv = new ArrayList<>();
    private Predicate<V> predicate = (v -> !v.isInstantiated());


    @SafeVarargs
    public VariableSelectorWithTies(VariableEvaluator<V>... heuristics) {
        this.heuristics = heuristics;
    }

    public void addPredicate(Predicate<V> predicate){
        this.predicate = this.predicate.and(predicate);
    }

    @Override
    public V getVariable(V[] variables) {
        oldv.clear();
        newv.clear();
        Collections.addAll(oldv, variables);
        // 1. remove instantied variables
        newv.addAll(oldv.stream().filter(this.predicate).collect(Collectors.toList()));
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
