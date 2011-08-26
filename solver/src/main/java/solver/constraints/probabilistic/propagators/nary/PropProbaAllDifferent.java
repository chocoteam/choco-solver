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

package solver.constraints.probabilistic.propagators.nary;

import solver.Solver;
import solver.constraints.IntConstraint;
import solver.constraints.probabilistic.IProbaPropagator;
import solver.constraints.propagators.nary.PropAllDifferent;
import solver.requests.ConditionnalRequest;
import solver.requests.conditions.AbstractCondition;
import solver.requests.conditions.CompletlyInstantiated;
import solver.variables.IntVar;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 25 nov. 2010
 */
public class PropProbaAllDifferent extends PropAllDifferent implements IProbaPropagator {

    protected Union unionset;

    public PropProbaAllDifferent(IntVar[] vars, Solver solver, IntConstraint constraint) {
        super(vars, solver, constraint);
        unionset = new Union(vars, environment);
    }

    @Override
    public void linkToVariables() {
        //noinspection unchecked
        AbstractCondition condition = new CompletlyInstantiated(this.environment, vars.length / 2);
        for (int i = 0; i < vars.length; i++) {
            vars[i].updatePropagationConditions(this, i);
            ConditionnalRequest crequest =
                    new ConditionnalRequest<PropProbaAllDifferent>(this, vars[i], i, condition, this.environment);
            this.addRequest(crequest);
            vars[i].addRequest(requests[i]);
            condition.linkRequest(crequest);
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public boolean toPropagate() {
        return true;
    }

    @Override
    public void updateOnRem(int value) {
        unionset.remove(value);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


    /**
     * test for unionset => has to be executed in the search loop at the beginning of downBranch method
     *
     * @return true if unionset is ok
     */
    public boolean checkUnion() {
        int[] toCheck = unionset.getValues();
        Arrays.sort(toCheck);
        int[] computed = computeUnion();
        Arrays.sort(computed);
        if (toCheck.length != computed.length) {
            System.out.println(printTab("incr", toCheck));
            System.out.println("--------------------");
            System.out.println(printTab("comp", computed));
            return false;
        } else {
            int i = 0;
            while (i < toCheck.length && toCheck[i] == computed[i]) {
                i++;
            }
            if (i != toCheck.length) {
                System.out.println(printTab("incr", toCheck));
                System.out.println("--------------------");
                System.out.println(printTab("comp", computed));
                return false;
            } else {
                return true;
            }
        }
    }

    private int[] computeUnion() {
        Set<Integer> vals = new HashSet<Integer>();
        for (IntVar var : vars) {
            int ub = var.getUB();
            for (int i = var.getLB(); i <= ub; i = var.nextValue(i)) {
                vals.add(i);
            }
        }
        int[] res = new int[vals.size()];
        int j = 0;
        for (Integer i : vals) {
            res[j++] = i;
        }
        return res;
    }

    private String printTab(String s, int[] tab) {
        String res = s + " : [";
        for (int aTab : tab) {
            res += aTab + ", ";
        }
        res = res.substring(0, res.length() - 2);
        res += "]";
        return res;
    }
}
