/**
 *  Copyright (c) 1999-2010, Ecole des Mines de Nantes
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

package solver.constraints.nary.geost.dataStructures;

import choco.cp.solver.constraints.global.geost.geometricPrim.Point;
import choco.cp.solver.constraints.global.geost.internalConstraints.InternalConstraint;


public final class HeapKey implements Heapable {

	private Point p;
	private int d; //the internal dimension
	private InternalConstraint ictr;
	private int k;//the total dimension of the problem

	public HeapKey(Point p, int d, int dim, InternalConstraint ictr)
	{
		this.p = p;
		this.d = d;
		this.ictr= ictr;
		this.k = dim;
	}


	public int getD() {
		return d;
	}


	public InternalConstraint getIctr() {
		return ictr;
	}


	public Point getP() {
		return p;
	}


	public void setD(int d) {
		this.d = d;
	}


	public void setIctr(InternalConstraint ictr) {
		this.ictr = ictr;
	}


	public void setP(Point p) {
		this.p = p;
	}


	public boolean equalTo(Object other) {
		int jPrime = 0;

		for(int j = 0; j < k; j++)
		{
			jPrime = (j + this.d) % k;
			if(p.getCoord(jPrime) != (((HeapKey) other).getP()).getCoord(jPrime)) {
				return false;
			}
		}
		return true;
	}

	public boolean greaterThan(Object other) {
		int jPrime = 0;

		for(int j = 0; j < k; j++)
		{
			jPrime = (j + d) % k;
			if(this.p.getCoord(jPrime) != (((HeapKey) other).getP()).getCoord(jPrime))
			{
                return this.p.getCoord(jPrime) >= (((HeapKey) other).getP()).getCoord(jPrime);
			}
		}
		return false; //since they are equal
	}


	public boolean lessThan(Object other) {
		int jPrime = 0;

		for(int j = 0; j < k; j++)
		{
			jPrime = (j + d) % k;
			if(p.getCoord(jPrime) != (((HeapKey) other).getP()).getCoord(jPrime))
			{
                return p.getCoord(jPrime) <= (((HeapKey) other).getP()).getCoord(jPrime);
			}
		}
		return false; //since they are equal
	}

}
