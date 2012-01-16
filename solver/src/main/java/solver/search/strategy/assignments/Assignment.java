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

package solver.search.strategy.assignments;

import solver.ICause;
import solver.exception.ContradictionException;
import solver.variables.IntVar;
import solver.variables.Variable;
import solver.variables.graph.GraphVar;

import java.io.Serializable;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 03/02/11
 */
public abstract class Assignment<V extends Variable> implements Serializable{

    public abstract void apply(V var, int value, ICause cause) throws ContradictionException;

    public abstract void unapply(V var, int value, ICause cause) throws ContradictionException;

    public abstract String toString();


    public static Assignment<IntVar> int_eq = new Assignment<IntVar>() {

        @Override
        public void apply(IntVar var, int value, ICause cause) throws ContradictionException {
            var.instantiateTo(value, cause);
        }

        @Override
        public void unapply(IntVar var, int value, ICause cause) throws ContradictionException {
            var.removeValue(value, cause);
        }

        @Override
        public String toString() {
            return " == ";
        }
    };

    public static Assignment<IntVar> int_neq = new Assignment<IntVar>() {

        @Override
        public void apply(IntVar var, int value, ICause cause) throws ContradictionException {
            var.removeValue(value, cause);
        }

        @Override
        public void unapply(IntVar var, int value, ICause cause) throws ContradictionException {
            var.instantiateTo(value, cause);
        }

        @Override
        public String toString() {
            return " != ";
        }
    };

    public static Assignment<IntVar> int_split = new Assignment<IntVar>() {

        @Override
        public void apply(IntVar var, int value, ICause cause) throws ContradictionException {
            var.updateUpperBound(value, cause);
        }

        @Override
        public void unapply(IntVar var, int value, ICause cause) throws ContradictionException {
            var.updateLowerBound(value + 1, cause);
        }

        @Override
        public String toString() {
            return " <= ";
        }
    };

    public static Assignment<IntVar> int_reverse_split = new Assignment<IntVar>() {

        @Override
        public void apply(IntVar var, int value, ICause cause) throws ContradictionException {
            var.updateLowerBound(value, cause);
        }

        @Override
        public void unapply(IntVar var, int value, ICause cause) throws ContradictionException {
            var.updateUpperBound(value - 1, cause);
        }

        @Override
        public String toString() {
            return " >= ";
        }
    };

    public static Assignment<GraphVar> graph_enforcer = new Assignment<GraphVar>() {

    	@Override
        public void apply(GraphVar var, int value, ICause cause) throws ContradictionException {
            int n = var.getEnvelopGraph().getNbNodes();
            if (value>=n){
            	int from = value/n-1;
            	int to   = value%n;
//            	System.out.println("enf "+value + " : "+from +" : "+to);
            	var.enforceArc(from, to, cause, false);
            }else{
//            	System.out.println("enf "+value);
            	var.enforceNode(value, cause);
            }
        }

        @Override
        public void unapply(GraphVar var, int value, ICause cause) throws ContradictionException {
        	int n = var.getEnvelopGraph().getNbNodes();
            if (value>=n){
            	int from = value/n-1;
            	int to   = value%n;
            	var.removeArc(from, to, cause);
            }else{
//            	System.out.println("rem "+value);
            	var.removeNode(value, cause);
            }
        }
        
        @Override
        public String toString() {
            return " enforcing ";
        }
    };
    
    public static Assignment<GraphVar> graph_remover = new Assignment<GraphVar>() {

    	@Override
        public void apply(GraphVar var, int value, ICause cause) throws ContradictionException {
    		graph_enforcer.unapply(var, value, cause);
        }

        @Override
        public void unapply(GraphVar var, int value, ICause cause) throws ContradictionException {
        	graph_enforcer.apply(var, value, cause);
        }

        @Override
        public String toString() {
            return " removal ";
        }
    };


}
