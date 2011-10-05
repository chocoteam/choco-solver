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
package solver.constraints.nary;

import org.testng.Assert;
import org.testng.annotations.Test;
import solver.Cause;
import solver.Solver;
import solver.constraints.Constraint;
import solver.exception.ContradictionException;
import solver.variables.IntVar;
import solver.variables.VariableFactory;

import static org.testng.Assert.assertEquals;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 15/06/11
 */
public class GlobalCardinalityTest {


    @Test
    public void testGCC() {
        Solver solver = new Solver();

        IntVar peter = VariableFactory.enumerated("Peter", 0, 1, solver);
        IntVar paul = VariableFactory.enumerated("Paul", 0, 1, solver);
        IntVar mary = VariableFactory.enumerated("Mary", 0, 1, solver);
        IntVar john = VariableFactory.enumerated("John", 0, 1, solver);
        IntVar bob = VariableFactory.enumerated("Bob", 0, 2, solver);
        IntVar mike = VariableFactory.enumerated("Mike", 1, 4, solver);
        IntVar julia = VariableFactory.enumerated("Julia", 2, 4, solver);

        IntVar[] vars = new IntVar[]{peter, paul, mary, john, bob, mike, julia};

        Constraint gcc = GlobalCardinality.make(vars,
                new int[]{1, 1, 1, 0, 0}, new int[]{2, 2, 1, 2, 2}, 0,
                GlobalCardinality.Consistency.BC, solver);

        solver.post(gcc);
        try {
            solver.propagate();
            assertEquals(bob.getLB(), 2);
            assertEquals(bob.getUB(), 2);
            julia.removeValue(3, Cause.Null);
            solver.propagate();
        } catch (ContradictionException e) {
            Assert.fail();
        }
    }


}
