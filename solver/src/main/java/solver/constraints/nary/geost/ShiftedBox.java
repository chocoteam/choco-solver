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

package solver.constraints.nary.geost;

import java.io.Serializable;
import java.util.Arrays;

/**
 * This is the class that represents a Shifted Box. Each shifted box belongs to a shape (therefore the shape id variable) and has two lists
 * that specify its offset (basically origin, lower left corner) and its size in every dimension.
 */
public class ShiftedBox implements Serializable {

	private int sid; //shape Id
	private int[] t; //the offset
	private int[] l; //the size

	public ShiftedBox(int shapeId, int[] offset, int[] size)
	{
		this.sid = shapeId;
		this.t = offset;
		this.l = size;
	}

	public ShiftedBox(){}

	public void setOffset(int index, int value)
	{
		this.t[index] = value;
	}

	public void setOffset(int[] off)
	{
		this.t = off;
	}

	public int getOffset(int index)
	{
		return this.t[index];
	}

    public int[] getOffset()
    {
        return this.t;
    }


    public void setSize(int index, int value)
	{
		this.l[index] = value;
	}

	public void setSize(int[] s)
	{
		this.l = s;
	}

	public int getSize(int index)
	{
		return this.l[index];
	}

    public int[] getSize()
    {
        return this.l;
    }

    public int getShapeId()
	{
		return this.sid;
	}

	public void setShapeId(int id)
	{
		this.sid = id;
	}

    public void print()
    {
        System.out.print("sid="+sid+" ");
        for (int i=0; i<t.length; i++) System.out.print("t["+i+"]:"+t[i]+" ");
        for (int i=0; i<l.length; i++) System.out.print("l["+i+"]:"+l[i]+" ");
        System.out.println("");
    }

    @Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final ShiftedBox other = (ShiftedBox) obj;
		if (!Arrays.equals(l, other.l))
			return false;
		if (sid != other.sid)
			return false;
		if (!Arrays.equals(t, other.t))
			return false;
		return true;
	}



}
