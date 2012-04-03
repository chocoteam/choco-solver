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

package solver.constraints.nary.geost.internalConstraints;

import choco.cp.solver.constraints.global.geost.Constants;

/**
 * A class that represent the Outbox internal constraint. If this constraint is
 * applied to an object, it forces the origin of the object to be outside its
 * box defined by an offset and a size in each dimension
 */
public final class Outbox extends InternalConstraint {

	private int[] t;
	private int[] l;
	
	
	public Outbox(int[] t, int[] l)
	{
		super(Constants.OUTBOX);
		this.t = t;
		this.l = l;
	}


	public int[] getL() {
		return l;
	}
	
	public int getL(int index) {
		return this.l[index];
	}


	public void setL(int[] l) {
		this.l = l;
	}


	public int[] getT() {
		return t;
	}
	
	public int getT(int index)
	{
		return this.t[index];
	}


	public void setT(int[] t) {
		this.t = t;
	}

    public String toString() {
        StringBuilder res = new StringBuilder();
        for (int aT : t){
            res.append("[").append(aT).append("],");
        }
        for (int i=0; i<t.length; i++){
            res.append("[").append(l[i]).append("],");
        }

        return res.toString();
    }

    public int adjacent(Outbox ob)
        //returns the dimensions along which ob is adjacent to the object
        //otherwise returns -1
        //Condition for adjacency, regarding the origin:
        //1) adjacent in at most one dimension
        //2) the other dimensions must be equal
    {
        int already_found=-1;
        for (int i=0; i<t.length; i++) {
            if ((t[i]+l[i]==ob.getT(i)) || (ob.getT(i)+ob.getL(i)==t[i] ))
                if (already_found!=-1) return -1; else already_found=i;
            if ((i!=already_found) && (t[i]!=ob.getT(i))) return -1;
        }
        return already_found;
    }

    public void merge(Outbox ob, int dim)
    //PRE: dim=adjacent(ob) and sameSize(ob,dim)
    {
//        System.out.println("before computation dim:"+dim+" l:"+l[dim]);
//        System.out.println("before computation dim:"+dim+" ob.l:"+ob.getL(dim));

       if (ob.getT(dim)<t[dim]) {
           t[dim]=ob.getT(dim);
           l[dim]+=ob.getL(dim);
       }
       else {
           l[dim]+=ob.getL(dim);
       }
//        System.out.println("after computation dim:"+dim+" l:"+l[dim]);
        
    }

    public boolean sameSize(Outbox ob, int dim)
            //ob has the same size except for the dimension dim
    {
     for (int i=0; i<t.length; i++) {
         if (i==dim) continue;
         if (l[i]!=ob.getL(i)) return false;
     }
     return true;
    }

}
