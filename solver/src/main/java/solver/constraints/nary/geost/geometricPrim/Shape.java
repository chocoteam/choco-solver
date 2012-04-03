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

import choco.kernel.model.variables.geost.ShiftedBox;

import java.util.ArrayList;
import java.util.List;

/**
 * This is the class that represents a Shape. Each shape has a shape Id and a set of shifted boxes.
 */
public final class Shape {
	
	private int shapeId;
	
	private List<ShiftedBox> sb;
	
	public Shape()
	{
		this.sb = new ArrayList<ShiftedBox>();
		
	}
	
	public Shape(int id)
	{
		this.shapeId = id;
		this.sb = new ArrayList<ShiftedBox>();
	}

	public List<ShiftedBox> getShiftedBoxes() {
		return this.sb;
	}

	public void setShiftedBoxes(List<ShiftedBox> sb) {
		this.sb = sb;
	}

	public void addShiftedBox(ShiftedBox sb)
	{
		this.sb.add(sb);
	}
	
	public ShiftedBox getShiftedBox(int index)
	{
		return this.sb.get(index);
	}
	
	public void removeShiftedBox(int index)
	{
		this.sb.remove(index);
	}
	
	public void removeShiftedBox(ShiftedBox sb)
	{
		this.sb.remove(sb);
		
	}
	public int getShapeId() {
		return this.shapeId;
	}

	public void setShapeId(int shapeId) {
		this.shapeId = shapeId;
	}
	
	

}
