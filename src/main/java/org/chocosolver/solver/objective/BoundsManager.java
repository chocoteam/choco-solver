/**
 * Copyright (c) 2016, Ecole des Mines de Nantes
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
package org.chocosolver.solver.objective;

import org.chocosolver.solver.ResolutionPolicy;

import java.io.Serializable;

/**
 *
 * @author Arnaud Malapert
 *
 *  Class to monitor the objective bounds and avoid exploring "worse" solutions
 *
 */
public class BoundsManager<N extends Number> implements Serializable {

    // ***********************************************************************************
    // VARIABLES
    // ***********************************************************************************

    private static final long serialVersionUID = 288954025529846132L;

    /** Define how should the objective be optimize */
    protected ResolutionPolicy policy;
    /** best lower bound found so far **/
    protected N bestProvedLB;
    /** best upper bound found so far **/
    protected N bestProvedUB; // best bounds found so far

    //***********************************************************************************
    // CONSTRUCTOR
    //***********************************************************************************

    protected BoundsManager(ResolutionPolicy policy) {
        super();
        this.policy = policy;
    }

    public BoundsManager(ResolutionPolicy policy, N bestProvedLB, N bestProvedUB) {
        super();
        this.policy = policy;
        this.bestProvedLB = bestProvedLB;
        this.bestProvedUB = bestProvedUB;
    }

    public BoundsManager(BoundsManager<N> toCopy) {
        this(toCopy.policy, toCopy.bestProvedLB, toCopy.bestProvedUB);
    }


    //***********************************************************************************
    // METHODS
    //***********************************************************************************

    /**
     * @return true iff the problem is an optimization problem
     */
    public final boolean isOptimization() {
        return policy != ResolutionPolicy.SATISFACTION;
    }

    /**
     * @return true iff the problem is an optimization problem and the objective is not fixed.
     */
    public final boolean hasOptimizationSlack() {
        return policy == ResolutionPolicy.SATISFACTION || bestProvedUB.doubleValue() > bestProvedLB.doubleValue();
    }

    /**
     * @return the best solution value found so far (returns the initial bound if no solution has been found yet)
     */
    public final N getBestSolutionValue() {
        if (policy == ResolutionPolicy.MINIMIZE) {
            return bestProvedUB;
        }
        if (policy == ResolutionPolicy.MAXIMIZE) {
            return bestProvedLB;
        }
        throw new UnsupportedOperationException("There is no objective variable in satisfaction problems");
    }

    /**
     * States thatClass to monitor bounds lb is a global lower bound on the problem
     *
     * @param lb
     *            lower bound
     */
    public void updateBestLB(N lb) {
        if (bestProvedLB == null) {
            // this may happen with multi-thread resolution
            // when one thread find a solver before one other is being launched
            bestProvedLB = lb;
        }
        if (lb.doubleValue() > bestProvedLB.doubleValue()) {
            bestProvedLB = lb;
        }
    }

    /**
     * States that ub is a global upper bound on the problem
     *
     * @param ub
     *            upper bound
     */
    public void updateBestUB(N ub) {
        if (bestProvedUB == null) {
            // this may happen with multi-thread resolution
            // when one thread find a solver before one other is being launched
            bestProvedUB = ub;
        }
        if (ub.doubleValue() < bestProvedUB.doubleValue()) {
            bestProvedUB = ub;
        }
    }

    public final void updateBounds(BoundsManager<N> bounds) {
        if(bounds != null && isOptimization() ) {
            updateBestLB(bounds.getBestLB());
            updateBestUB(bounds.getBestUB());
        }
    }
    //***********************************************************************************
    // ACCESSORS
    //***********************************************************************************

    @Override
    public String toString() {
        return policy + ":[" + bestProvedLB + "," + bestProvedUB
                + "]";
    }

    /**
     * @return the ResolutionPolicy of the problem
     */
    public final ResolutionPolicy getPolicy() {
        return policy;
    }

    /**
     * @return the best lower bound computed so far
     */
    public final N getBestLB() {
        return bestProvedLB;
    }

    /**
     * @return the best upper bound computed so far
     */
    public final N getBestUB() {
        return bestProvedUB;
    }

}