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
 * Date: 03/10/11
 * Time: 19:56
 */

package solver.constraints.propagators.gary.vrp;

import choco.kernel.ESat;
import choco.kernel.memory.IStateInt;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.propagators.GraphPropagator;
import solver.constraints.propagators.PropagatorPriority;
import solver.exception.ContradictionException;
import solver.recorders.fine.AbstractFineEventRecorder;
import solver.variables.EventType;
import solver.variables.IntVar;
import solver.variables.graph.INeighbors;
import solver.variables.graph.directedGraph.DirectedGraphVar;

public class PropTruckCompatibility extends GraphPropagator {

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	DirectedGraphVar g;
	IStateInt[][] truckInCommom;
	int n;
	int nbTrucks;
	IntVar[] trucks;

	//***********************************************************************************
	// CONSTRUCTORS
	//***********************************************************************************

	public PropTruckCompatibility(DirectedGraphVar graph, IntVar[] trucks, int nbTrucks,
								  Constraint constraint, Solver solver) {
		super(trucks, solver, constraint, PropagatorPriority.LINEAR);
		g = graph;
		this.nbTrucks = nbTrucks;
		this.n = g.getEnvelopGraph().getNbNodes();
		this.trucks = trucks;
		truckInCommom = new IStateInt[n][n];
		IStateInt noArc = environment.makeInt(-1);
		for(int i=0;i<n;i++){
			for(int j=i+1;j<n;j++){
				if(g.getEnvelopGraph().arcExists(i,j)||g.getEnvelopGraph().arcExists(j,i)){
					truckInCommom[i][j] = truckInCommom[j][i] = environment.makeInt(getTruckInCommon(i,j));
				}else{
					truckInCommom[i][j] = truckInCommom[j][i] = noArc;
				}
			}
		}
	}

	private int getTruckInCommon(int i, int j) {
		int lb = Math.max(trucks[i].getLB(),trucks[j].getLB());
		int ub = Math.min(trucks[i].getUB(),trucks[j].getUB());
		for(int t=lb;t<=ub;t=trucks[i].nextValue(t+1)){
			if(trucks[j].contains(t)){
				return t;
			}
		}
		return -1;
	}

	//***********************************************************************************
	// METHODS
	//***********************************************************************************

	@Override
	public void propagate(int evtmask) throws ContradictionException {
		for(int i=0;i<n;i++){
			for(int j=i+1;j<n;j++){
				if(g.getEnvelopGraph().arcExists(i,j)||g.getEnvelopGraph().arcExists(j,i)){
					truckInCommom[i][j].set(getTruckInCommon(i,j));
					if(truckInCommom[i][j].get()==-1){
						g.removeArc(i,j,this);
						g.removeArc(j,i,this);
					}
				}else{
					truckInCommom[i][j].set(-1);
				}
			}
		}
	}

	@Override
	public void propagate(AbstractFineEventRecorder eventRecorder, int idxVarInProp, int mask) throws ContradictionException {
		checkNode(idxVarInProp);
	}

	private void checkNode(int i) throws ContradictionException {
		INeighbors nei = g.getEnvelopGraph().getPredecessorsOf(i);
		for(int j=nei.getFirstElement();j>=0;j=nei.getNextElement()){
			if(!trucks[i].contains(truckInCommom[i][j].get())){
				truckInCommom[i][j].set(getTruckInCommon(i,j));
				if(truckInCommom[i][j].get()==-1){
					g.removeArc(j,i,this);
					g.removeArc(i,j,this);
				}
			}
		}
		nei = g.getEnvelopGraph().getSuccessorsOf(i);
		for(int j=nei.getFirstElement();j>=0;j=nei.getNextElement()){
			if(!trucks[i].contains(truckInCommom[i][j].get())){
				truckInCommom[i][j].set(getTruckInCommon(i,j));
				if(truckInCommom[i][j].get()==-1){
					g.removeArc(i,j,this);
					g.removeArc(j,i,this);
				}
			}
		}
	}

	@Override
	public int getPropagationConditions(int vIdx) {
		return EventType.INT_ALL_MASK();
	}

	@Override
	public ESat isEntailed() {
		return ESat.UNDEFINED;
	}
}
