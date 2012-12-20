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

package solver;

import com.sun.istack.internal.Nullable;
import solver.constraints.Constraint;
import solver.explanations.Deduction;
import solver.explanations.Explanation;

import java.io.Serializable;

/**
 * This interface describes services of smallest element which can act on variables.
 * As an example, propagator is a cause because it filters values from variable domain.
 * So do decision, objective manager, etc.
 * It has an impact on domain variables and so it can fails.
 * <p/>
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 26 oct. 2010
 */
public interface ICause extends Serializable {

    /**
     * returns the constraint associated to <code>this</code>, if any.
     *
     * @return a constraint or null
     */
    Constraint getConstraint();

    /**
     * Feeds an explanation based on <code>this</code>.
     *
     * @param d the deduction
     * @param e explanation to feed
     */
    void explain(@Nullable Deduction d, Explanation e);


    /**
     * Returns the promomotion policy of <code>this</code>.
     * If <code>this</code> reacts on promotion, it must be informed of the promotion of an event it created.
     * (example: removing the lower bound of a variable is promoted in lower-bound modification)
     *
     * @return <code>true</code> if <code>this</code> must be informed of promotion
     */
    boolean reactOnPromotion();

    /**
     * Gets the propagation conditions of <code>this</code> on the variable at position <code>vIdx</code> in its internal
     * structure. A propagation condition defines on which event occurring on the <code>vIdx</code>^th
     * variable <code>this</code> can do filter values.
     *
     * @param vIdx index of the variable
     * @return a mask
     */
    int getPropagationConditions(int vIdx);

}
