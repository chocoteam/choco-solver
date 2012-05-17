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
import solver.variables.graph.GraphVar;
import solver.variables.graph.INeighbors;

import java.io.Serializable;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 03/02/11
 */
public abstract class GraphAssignment implements Serializable{

	public abstract void apply(GraphVar var, int node, ICause cause) throws ContradictionException;

	public abstract void unapply(GraphVar var, int node, ICause cause) throws ContradictionException;

	public abstract void apply(GraphVar var, int from, int to, ICause cause) throws ContradictionException;

	public abstract void unapply(GraphVar var, int from, int to, ICause cause) throws ContradictionException;

	public abstract String toString();

	public static GraphAssignment graph_enforcer = new GraphAssignment() {

		@Override
		public void apply(GraphVar var, int node, ICause cause) throws ContradictionException {
			if (node<var.getEnvelopGraph().getNbNodes()){
				var.enforceNode(node, cause);
			}else{
				throw new UnsupportedOperationException();
			}
		}

		@Override
		public void unapply(GraphVar var, int node, ICause cause) throws ContradictionException {
			if (node<var.getEnvelopGraph().getNbNodes()){
				var.removeNode(node, cause);
			}else{
				throw new UnsupportedOperationException();
			}
		}

		@Override
		public void apply(GraphVar var, int from, int to, ICause cause) throws ContradictionException {
			if(from == -1 || to == -1){
				throw new UnsupportedOperationException();
			}
			var.enforceArc(from, to, cause);
		}

		@Override
		public void unapply(GraphVar var, int from, int to, ICause cause) throws ContradictionException {
			if(from == -1 || to == -1){
				throw new UnsupportedOperationException();
			}
			var.removeArc(from, to, cause);
		}

		@Override
		public String toString() {
			return " enforcing ";
		}
	};

	public static GraphAssignment graph_remover = new GraphAssignment() {
		@Override
		public void apply(GraphVar var, int value, ICause cause) throws ContradictionException {
			graph_enforcer.unapply(var, value, cause);
		}
		@Override
		public void unapply(GraphVar var, int value, ICause cause) throws ContradictionException {
			graph_enforcer.apply(var, value, cause);
		}
		@Override
		public void apply(GraphVar var, int from, int to, ICause cause) throws ContradictionException {
			graph_enforcer.unapply(var, from, to, cause);
		}
		@Override
		public void unapply(GraphVar var, int from, int to, ICause cause) throws ContradictionException {
			graph_enforcer.apply(var, from, to, cause);
		}
		@Override
		public String toString() {
			return " removal ";
		}
	};

	public static GraphAssignment graph_split = new GraphAssignment() {
		@Override
		public void apply(GraphVar var, int node, int highestIdx, ICause cause) throws ContradictionException {
			INeighbors nei = var.getEnvelopGraph().getSuccessorsOf(node);
			for(int j=nei.getFirstElement();j>=0;j=nei.getNextElement()){
				if(j>highestIdx){
					var.removeArc(node,j,cause);
				}
			}
		}
		@Override
		public void unapply(GraphVar var, int node, int highestIdx, ICause cause) throws ContradictionException {
			INeighbors nei = var.getEnvelopGraph().getSuccessorsOf(node);
			for(int j=nei.getFirstElement();j>=0;j=nei.getNextElement()){
				if(j<=highestIdx){
					var.removeArc(node,j,cause);
				}
			}
		}
		@Override
		public void apply(GraphVar var, int node, ICause cause) throws ContradictionException {
			throw new UnsupportedOperationException();
		}
		@Override
		public void unapply(GraphVar var, int node, ICause cause) throws ContradictionException {
			throw new UnsupportedOperationException();
		}
		@Override
		public String toString() {
			return " split ";
		}
	};
}
