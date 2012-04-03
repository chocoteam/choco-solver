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

package solver.constraints.nary.geost.geometricPrim;

import choco.kernel.memory.trailing.StoredInt;

import static java.lang.System.arraycopy;


public final class Witness {
	
	private StoredInt[] coords;
	private int dim;
	
	public Witness(int dim)
	{	//creates a point at the origin of the coordinate base.
		this.dim = dim;
		coords =  new StoredInt[this.dim];
		for (int i = 0; i < this.dim; i++){
			this.coords[i].set(0);
        }
	}
	
	public Witness(Witness w)
	{
		//creates a point from another point.
		coords =  new StoredInt[this.dim];
		for(int i = 0; i < w.getCoords().length; i++){
			this.coords[i].set(w.getCoord(i));
        }
	}
	
	public StoredInt[] getCoords()
	{
		return this.coords;
	}
	
	public void setCoords(StoredInt coordinates[])
	{
        arraycopy(coordinates, 0, this.coords, 0, coordinates.length);
	}
	
	public int getCoord(int index)
	{
		return this.coords[index].get();
	}
	
	public void setCoord(int index, int value)
	{
		this.coords[index].set(value);
	}
	
}
