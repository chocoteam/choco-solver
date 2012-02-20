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

package solver.search.solution;

import org.slf4j.LoggerFactory;
import solver.ICause;
import solver.Solver;
import solver.constraints.Constraint;
import solver.exception.ContradictionException;
import solver.exception.SolverException;
import solver.explanations.Deduction;
import solver.explanations.Explanation;
import solver.variables.IntVar;
import solver.variables.Variable;
import solver.variables.graph.GraphVar;

import java.util.LinkedList;

/**
 * <br/>
 *
 * @author Arnaud Malapert
 * @author Charles Prud'homme
 * @since 19 juil. 2010
 */
public class Solution implements ICause {


    /* Reference to the solver */
    private Solver solver;

    /* Values of integer variables, in Solver internal order */
    private int[] intvalues;

    /* Values of graph variables, in Solver internal order */
    private LinkedList<boolean[][]> graphValues;

    /* Statistics of the current solution (time, nodes, etc.) */
    private long[] measures;

    public static Solution empty() {
        return new Solution();
    }

    private Solution() {
    }

    public Solution(Solver solver) {
        replace(solver);
    }

    public void replace(Solver solver) {
        this.solver = solver;
        Variable[] vars = solver.getVars();
        intvalues = new int[vars.length];
        graphValues = new LinkedList<boolean[][]>();
        for (int i = 0; i < vars.length; i++) {
            assert (vars[i].instantiated()):vars[i]+" is not instantiated"; // BEWARE only decision variables should be instantiated
            switch(vars[i].getType()){
            case Variable.INTEGER : 
            	intvalues[i] = ((IntVar) vars[i]).getValue();break;
            case Variable.GRAPH : 
            	if(!vars[i].instantiated()){
            		System.out.println(((GraphVar) vars[i]).getEnvelopGraph());
            		System.out.println(((GraphVar) vars[i]).getKernelGraph());
            		throw new UnsupportedOperationException("solution graph not instantiated");
            	}
            	graphValues.add(((GraphVar) vars[i]).getValue());break;
            }
        }
//        measures = solver.getSearchLoop().getMeasures().
    }


    public void restore() {
        try {
            Variable[] vars = solver.getVars();
            int nbGV = 0;
            for (int i = 0; i < vars.length; i++) {
            	switch(vars[i].getType()){
                case Variable.INTEGER : 
                	((IntVar) vars[i]).instantiateTo(intvalues[i], this);break;
                case Variable.GRAPH :
					boolean[][] gv = graphValues.get(nbGV);
                	((GraphVar) vars[i]).instantiateTo(gv, this);
					nbGV++;
					break;
                }
                
            }
        } catch (ContradictionException ex) {
			ex.printStackTrace();
            LoggerFactory.getLogger("solver").error("BUG in restoring solution !!");
            throw new SolverException("Restored solution not consistent !!");
        }
    }

    public long[] measures() {
        return measures;
    }

    @Override
    public Constraint getConstraint() {
        return null;
    }

    @Override
    public Explanation explain(Deduction d) {
        return Explanation.SYSTEM;
    }

    @Override
    public boolean reactOnPromotion() {
        return false;
    }

    @Override
    public int getPropagationConditions(int vIdx) {
        return 0;
    }

    @Override
    public void incFail() {
    }

    @Override
    public long getFails() {
        return 0;
    }

    @Override
    public String toString() {
        return "Solution";
    }
}
