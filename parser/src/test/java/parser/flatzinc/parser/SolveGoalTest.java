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

package parser.flatzinc.parser;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/*
* User : CPRUDHOM
* Mail : cprudhom(a)emn.fr
* Date : 13 janv. 2010
* Since : Choco 2.1.1
* 
*/
public class SolveGoalTest {

    FZNParser fzn;
    @BeforeMethod
    public void before(){
        fzn = new FZNParser();
    }

    @Test
    public void testSatisfy(){
        TerminalParser.parse(fzn.SOLVE_GOAL, "solve satisfy;");
    }

    @Test
    public void testMaximize(){
        TerminalParser.parse(fzn.PAR_VAR_DECL, "var 1 .. 10: a::output_var;");
        TerminalParser.parse(fzn.SOLVE_GOAL, "solve maximize a;");
    }
//
    @Test
    public void testMinimize(){
        TerminalParser.parse(fzn.PAR_VAR_DECL, "var 1 .. 10: a::output_var;");
        TerminalParser.parse(fzn.SOLVE_GOAL, "solve minimize a;");
    }

    @Test
    public void testSatisfy2(){
        TerminalParser.parse(fzn.PAR_VAR_DECL, "var 1 .. 10: a::output_var;");
        TerminalParser.parse(fzn.SOLVE_GOAL, "solve ::int_search([a],input_order,indomain_min, complete) satisfy;");
    }


    @Test
    public void testSatisfy3(){
        TerminalParser.parse(fzn.PAR_VAR_DECL, "array[1 .. 55] of var 1 .. 161: restdays;");
        TerminalParser.parse(fzn.PAR_VAR_DECL, "array[1 .. 161] of var 0 .. 3: restseq;");
        TerminalParser.parse(fzn.PAR_VAR_DECL, "var 1 .. 10: objective::output_var;");
        TerminalParser.parse(fzn.SOLVE_GOAL, "solve\n" +
                "  ::seq_search(\n" +
                "    [ int_search(restdays, input_order, indomain_min, complete),\n" +
                "      int_search(restseq, input_order, indomain_min, complete) ])\n" +
                "  minimize objective;");
    }

}
