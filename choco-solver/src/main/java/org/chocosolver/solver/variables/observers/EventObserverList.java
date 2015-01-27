/*
 * Copyright (c) 1999-2015, Ecole des Mines de Nantes
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
package org.chocosolver.solver.variables.observers;

import org.chocosolver.solver.ICause;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.EventObserver;
import org.chocosolver.solver.variables.IntVar;

import java.util.ArrayList;

/**
 * Created by cprudhom on 26/01/15.
 * Project: choco.
 */
public class EventObserverList implements EventObserver {

    ArrayList<EventObserver> observers = new ArrayList<>();

    @Override
    public void activePropagator(BoolVar var, Propagator propagator) {
        for (int i = observers.size() - 1; i >= 0; i--) {
            observers.get(i).activePropagator(var, propagator);
        }
    }

    @Override
    public void removeValue(IntVar var, int val, ICause cause) {
        for (int i = observers.size() - 1; i >= 0; i--) {
            observers.get(i).removeValue(var, val, cause);
        }
    }

    @Override
    public void updateLowerBound(IntVar var, int old, int value, ICause cause) {
        for (int i = observers.size() - 1; i >= 0; i--) {
            observers.get(i).updateLowerBound(var, old, value, cause);
        }
    }

    @Override
    public void updateUpperBound(IntVar var, int old, int value, ICause cause) {
        for (int i = observers.size() - 1; i >= 0; i--) {
            observers.get(i).updateUpperBound(var, old, value, cause);
        }
    }

    @Override
    public void instantiateTo(IntVar var, int val, ICause cause, int oldLB, int oldUB) {
        for (int i = observers.size() - 1; i >= 0; i--) {
            observers.get(i).instantiateTo(var, val, cause, oldLB, oldUB);
        }
    }

    public boolean add(EventObserver anObserver) {
        for (EventObserver eo : observers) {
            if (eo == anObserver) {
                return false;
            }
        }
        observers.add(anObserver);
        return true;
    }

}
