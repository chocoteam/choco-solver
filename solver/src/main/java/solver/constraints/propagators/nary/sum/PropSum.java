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

/**
 * Created by IntelliJ IDEA.
 * User: Jean-Guillaume Fages
 * Date: 18/06/12
 * Time: 18:32
 */

package solver.constraints.propagators.nary.sum;

import choco.kernel.ESat;
import choco.kernel.common.util.tools.ArrayUtils;
import choco.kernel.memory.IStateInt;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.propagators.Propagator;
import solver.constraints.propagators.PropagatorPriority;
import solver.exception.ContradictionException;
import solver.recorders.fine.AbstractFineEventRecorder;
import solver.variables.EventType;
import solver.variables.IntVar;

/**
 * Sum constraint that ensure that the sum of integer variables vars is equal
 * to the integer variable sum
 *
 * use a tree representation to get a log behavior
 *
 * Should be used for large cases (vars.length>>100)
 */
public class PropSum extends Propagator<IntVar> {

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	IntVar sum;
	final static int SIZE_GROUP = 20;
	int nbLayers;
	Node root;
	Node[] leafs;
	int index;

	//***********************************************************************************
	// CONSTRUCTORS
	//***********************************************************************************

	/**
	 * Sum constraint that ensure that the sum of integer variables vars is equal
	 * to the integer variable sum
	 *
	 * use a tree representation to get a log behavior
	 *
	 * Should be used for large cases (vars.length>>100)
	 * @param vars
	 * @param sum
	 * @param solver
	 * @param intVarPropagatorConstraint
	 */
	protected PropSum(IntVar[] vars, IntVar sum, Solver solver, Constraint<IntVar, Propagator<IntVar>> intVarPropagatorConstraint) {
		super(ArrayUtils.append(vars,new IntVar[]{sum}), solver, intVarPropagatorConstraint, PropagatorPriority.LINEAR, false);
		this.sum = sum;
		nbLayers = computeNbLayers(vars.length);
		leafs = new Node[vars.length];
		root = new Node(nbLayers,null);
	}

	private int computeNbLayers(int nbElements){
		if(nbElements<SIZE_GROUP){
			return 1;
		}
		int nb = nbElements/SIZE_GROUP;
		if(nbElements%SIZE_GROUP>0){
			nb++;
		}
		return computeNbLayers(nb)+1;
	}

	//***********************************************************************************
	// METHODS
	//***********************************************************************************

	@Override
	public void propagate(int evtmask) throws ContradictionException {
		if((evtmask&EventType.FULL_PROPAGATION.mask)!=0){
			// compute bounds
			for(int i=0;i<vars.length-1;i++){
				leafs[i].incLB(vars[i].getLB());
				leafs[i].decUB(vars[i].getUB());
			}
			// update sum variable
			sum.updateLowerBound(root.oldLB.get(),this);
			sum.updateUpperBound(root.oldUB.get(),this);
		}
		// filter from sum
		int max = root.oldUB.get();
		filter_min(root,root.oldLB.get(),max,sum.getLB(),sum.getUB());
		int min = root.oldLB.get();
		filter_max(root,root.oldLB.get(),max,sum.getLB(),sum.getUB());
		// todo verifier utilite
		while(max!=root.oldUB.get() || min!=root.oldLB.get()){
			if(max!=root.oldUB.get()){
				max = root.oldUB.get();
				filter_min(root,root.oldLB.get(),max,sum.getLB(),sum.getUB());
			}
			if(min!=root.oldLB.get()){
				min = root.oldLB.get();
				filter_max(root,root.oldLB.get(),max,sum.getLB(),sum.getUB());
			}
		}
	}

	@Override
	public void propagate(AbstractFineEventRecorder eventRecorder, int idxVarInProp, int mask) throws ContradictionException {
		if(idxVarInProp<vars.length-1){
			if((mask & EventType.DECUPP.mask) != 0){
				leafs[idxVarInProp].decUB(vars[idxVarInProp].getUB()-leafs[idxVarInProp].oldUB.get());
			}
			if((mask & EventType.INCLOW.mask) != 0){
				leafs[idxVarInProp].incLB(vars[idxVarInProp].getLB()-leafs[idxVarInProp].oldLB.get());
			}
			sum.updateLowerBound(root.oldLB.get(),this);
			sum.updateUpperBound(root.oldUB.get(),this);
		}
		forcePropagate(EventType.CUSTOM_PROPAGATION);
	}

	@Override
	public int getPropagationConditions() {
		return EventType.FULL_PROPAGATION.mask+EventType.CUSTOM_PROPAGATION.mask;
	}

	@Override
	public int getPropagationConditions(int vIdx) {
		return EventType.INCLOW.mask+EventType.DECUPP.mask+EventType.INSTANTIATE.mask;
	}

	@Override
	public ESat isEntailed() {
		int lb = 0;
		int ub = 0;
		for(int i=0;i<vars.length-1;i++){
			lb += vars[i].getLB();
			ub += vars[i].getUB();
		}
		if(lb>sum.getUB() || ub<sum.getLB()){
			return ESat.FALSE;
		}
		if(isCompletelyInstantiated()){
			return ESat.TRUE;
		}
		return ESat.UNDEFINED;
	}

	private void filter_min(Node node, int rootlb, int rootub, int sumlb, int sumub) throws ContradictionException {
		if(rootub-node.oldUB.get()+node.oldLB.get()<sumlb){
			int index = node.leafIndex;
			if(index==-1){
				for(int i=0;i<SIZE_GROUP;i++){
					filter_min(node.childs[i],rootlb,rootub,sumlb,sumub);
				}
			}else {
				IntVar v = vars[index];
				int lb = sumlb-rootub+node.oldUB.get();
				v.updateLowerBound(lb,this);
				node.incLB(lb-node.oldLB.get());
			}
		}
	}

	private void filter_max(Node node, int rootlb, int rootub, int sumlb, int sumub) throws ContradictionException {
		if(rootlb-node.oldLB.get()+node.oldUB.get()>sumub){
			int index = node.leafIndex;
			if(index==-1){
				for(int i=0;i<SIZE_GROUP;i++){
					filter_max(node.childs[i],rootlb,rootub,sumlb,sumub);
				}
			}else {
				IntVar v = vars[index];
				int ub = sumub-rootlb+node.oldLB.get();
				v.updateUpperBound(ub,this);
				node.decUB(ub-node.oldUB.get());
			}
		}
	}

	private class Node{

		Node father;
		Node[] childs;
		IStateInt oldLB,oldUB;
		int leafIndex;

		Node(int depth, Node father){
			this.father = father;
			leafIndex = -1;
			if(depth>0){
				childs = new Node[SIZE_GROUP];
				for(int i=0;i<SIZE_GROUP;i++){
					childs[i] = new Node(depth-1,this);
				}
			}else if(index<vars.length-1){
				leafIndex = index;
				leafs[index++] = this;
			}
			oldLB = environment.makeInt();
			oldUB = environment.makeInt();
		}

		void incLB(int delta) {
			oldLB.add(delta);
			father.incLB(delta);
		}

		void decUB(int delta) {
			oldUB.add(delta);
			father.decUB(delta);
		}
	}

	private class DeadNode extends Node{

		DeadNode(int depth, Node father){
			super(0,null);
			oldLB = oldUB = null;
		}

		void incLB(int delta) {
			throw new UnsupportedOperationException();
		}

		void decUB(int delta) {
			oldUB.add(delta);
			father.decUB(delta);
		}
	}
}