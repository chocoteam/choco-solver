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

import choco.kernel.common.logging.ChocoLogging;

import java.io.Serializable;
import static java.lang.System.arraycopy;
import java.util.logging.Logger;

/**
 * This class represent a k dimensional Point.
 */
public final class Point implements Serializable {
	
    private static final Logger LOGGER = ChocoLogging.getEngineLogger();
	//this class serves as Point object. The dimension of the point is d Dimensional.
	
	private int coords[];
	private int dim;

    public void print() {
		for (int i = 0; i < this.dim; i++){
			LOGGER.info(this.coords[i]+ " ");
        }
    }

    /**
	 * Creates a point of dimension dim at the origin of the coordinate base.
	 * @param dim The dimension of the point object to create
	 */
	public Point(int dim)
	{
		this.dim = dim;
		coords =  new int[this.dim];
		for (int i = 0; i < this.dim; i++){
			this.coords[i] = 0;
        }
	}
	
	/**
	 * Creates a point object from an array of integers. The index of the array is actually the dimension number 
	 * and the value this index holds is the coordinate value of the point at that specified dimension. The length of the array 
	 * represents the total dimension of the point.
	 * @param coordinates An array of integers of length the dimension of the point.
	 */
	public Point(int coordinates[])
	{
		//creates a point from an array of integers.
        this.dim=coordinates.length;
		coords =  new int[this.dim];
        dim = coordinates.length;
        this.setCoords(coordinates);
	}
	
	/**
	 * Creates a point identical to the point given as parameter.
	 * @param p A point object
	 */
	public Point(Point p)
	{
		//creates a point from another point.
        this.dim=p.getCoords().length;
		coords =  new int[this.dim];
		for(int i = 0; i < p.getCoords().length; i++){
			this.coords[i] = p.getCoord(i);
        }
	}


	public int[] getCoords()                                        
	{
		return this.coords;
	}
	
	public void setCoords(int coordinates[])
	{
        arraycopy(coordinates, 0, this.coords, 0, coordinates.length);
	}
	
	public int getCoord(int index)
	{
		return this.coords[index];
	}
	
	public void setCoord(int index, int value)
	{
		this.coords[index] = value;
	}
	
	/**
	 * Tests whether this point is lexicographically greater than or equal to the other point (passed by parameter). The lexicographic ordering starts at dimension d (passed by parameter).
	 */
	public boolean lexGreaterThanOrEqual(Point other, int d) {
		int jPrime = 0;
		
		for(int j = 0; j < this.dim; j++)
		{
			jPrime = (j + d) % this.dim;
			if(this.getCoord(jPrime) != other.getCoord(jPrime))
			{
                return this.getCoord(jPrime) >= other.getCoord(jPrime);
			}
		}
		return true;
	}

    public boolean equalTo(Point other) {
        for(int j = 0; j < this.dim; j++)
        {
            if(this.getCoord(j) != other.getCoord(j))
            {
                return false;
            }
        }
        return true;
    }


    public boolean lexGreaterThan(Point other, int[] ctrlV) {
        for(int i = 0; i < this.dim; i++) {
            int actualDim = Math.abs(ctrlV[i+1]) - 2;
            boolean positive = (ctrlV[i+1]>0);
            if (this.getCoord(actualDim) == other.getCoord(actualDim)) continue;
            if (positive)
                return (this.getCoord(actualDim) < other.getCoord(actualDim));
            else            
                return (this.getCoord(actualDim) > other.getCoord(actualDim));            
        }
        return false;
    }

    /**
	 * Tests whether this point is lexicographically strictly greater than to the other point (passed by parameter). The lexicographic ordering starts at dimension d (passed by parameter).
	 */
	public boolean lexGreaterThan(Point other, int d) {
		int jPrime = 0;
		
		for(int j = 0; j < this.dim; j++)
		{
			jPrime = (j + d) % this.dim;
			if(this.getCoord(jPrime) != other.getCoord(jPrime))
			{
                return this.getCoord(jPrime) >= other.getCoord(jPrime);
			}
		}
		return false; //since they are equal
	}
	
	/**
	 * Tests whether this point is lexicographically smaller than or equal to the other point (passed by parameter). The lexicographic ordering starts at dimension d (passed by parameter).
	 */
	public boolean lexLessThanOrEqual(Point other, int d) {
		int jPrime = 0;
		
		for(int j = 0; j < this.dim; j++)
		{
			jPrime = (j + d) % this.dim;
			if(this.getCoord(jPrime) != other.getCoord(jPrime))
			{
                return this.getCoord(jPrime) <= other.getCoord(jPrime);
			}
		}
		return true;
	}
	
	/**
	 * Tests whether this point is lexicographically strictly smaller than the other point (passed by parameter). The lexicographic ordering starts at dimension d (passed by parameter).
	 */
	public boolean lexLessThan(Point other, int d) {
		int jPrime = 0;
		
		for(int j = 0; j < this.dim; j++)
		{
			jPrime = (j + d) % this.dim;
			if(this.getCoord(jPrime) != other.getCoord(jPrime))
			{
                return this.getCoord(jPrime) <= other.getCoord(jPrime);
			}
		}
		return false; //since they are equal
	}

    public String toString() {
        StringBuilder res = new StringBuilder();
        res.append("(");
        for (int i=0; i<dim; i++){
            if (i!=dim-1){
                res.append(coords[i]).append(",");
            }else{
                res.append(coords[i]).append(")");
            }
        }
        return res.toString();
    }
	
}
