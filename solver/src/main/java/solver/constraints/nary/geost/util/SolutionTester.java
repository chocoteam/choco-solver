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

package solver.constraints.nary.geost.util;


import choco.cp.solver.constraints.global.geost.Constants;
import choco.cp.solver.constraints.global.geost.Setup;
import choco.cp.solver.constraints.global.geost.geometricPrim.Obj;
import choco.kernel.model.variables.geost.ShiftedBox;

import java.util.Iterator;
import java.util.List;

/**
 * After solving and finding a solution, this class provides a function that tests whether a given solution is a valid solution. 
 * It is only for the non_overlapping constraint. 
 */
public final class SolutionTester {
	Setup stp;
	Constants cst;
	public SolutionTester(Setup s, Constants c)
	{
		this.stp = s;
		this.cst = c; 
	}
	
	public boolean testSolution()
	{
		
		Iterator objItr1;
		Iterator objItr2;
		
		objItr1 = stp.getObjectKeySet().iterator();
		while(objItr1.hasNext())
		{
			int oid1 = (Integer) objItr1.next();
			Obj o1 = stp.getObject(oid1);
			objItr2 = stp.getObjectKeySet().iterator();
			while(objItr2.hasNext())
			{
				int oid2 = (Integer) objItr2.next();
				Obj o2 = stp.getObject(oid2);
				if(oid1 != oid2)
				{
					//check for intersection: Two objects do not intersect if there exist at least on dim where they do not intersect
					List<ShiftedBox> sb1 = stp.getShape(o1.getShapeId().getInf());
					List<ShiftedBox> sb2 = stp.getShape(o2.getShapeId().getInf());
					
					for(int i = 0; i < sb1.size(); i++)
					{
						for(int j = 0; j < sb2.size(); j++)
						{
							boolean intersect = true;
							for(int k = 0; k < cst.getDIM(); k++)
							{
								if(!(
										((sb2.get(j).getOffset(k) + o2.getCoord(k).getInf() >= sb1.get(i).getOffset(k) + o1.getCoord(k).getInf()) &&
										(sb2.get(j).getOffset(k) + o2.getCoord(k).getInf() < sb1.get(i).getOffset(k) + o1.getCoord(k).getInf()  + sb1.get(i).getSize(k)))
										||
										((sb2.get(j).getOffset(k) + o2.getCoord(k).getInf() + sb2.get(j).getSize(k) > sb1.get(i).getOffset(k) + o1.getCoord(k).getInf()) && 
										(sb2.get(j).getOffset(k) + o2.getCoord(k).getInf() + sb2.get(j).getSize(k) <= sb1.get(i).getOffset(k) + o1.getCoord(k).getInf() + sb1.get(i).getSize(k)))
									)
								   )
								{
									intersect = false;
									break;
								}
							}
							if(intersect)
								return false;
						}
					}
				}
			}
		}
		return true;
	}

}
