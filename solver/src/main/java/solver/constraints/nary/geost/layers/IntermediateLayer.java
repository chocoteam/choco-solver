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

package solver.constraints.nary.geost.layers;


import choco.cp.solver.constraints.global.geost.Constants;
import choco.cp.solver.constraints.global.geost.geometricPrim.Obj;
import choco.cp.solver.constraints.global.geost.geometricPrim.Point;
import choco.cp.solver.constraints.global.geost.geometricPrim.Region;
import choco.cp.solver.constraints.global.geost.internalConstraints.*;
import choco.kernel.common.logging.ChocoLogging;
import choco.kernel.solver.SolverException;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;


/**
 * This is the intermediate layer class. It implements the functionality that permits access to infeasible sets of points according to  
 * some Internal Constraint ictr
 */
public final class IntermediateLayer {

    private static final Logger LOGGER = ChocoLogging.getEngineLogger();

	/**
	 * Creates an IntermediateLayer instance. Actually this class just provides functionality so
	 * we could have just made all the functions in it static but we prefer to do it this way for later changes if needed.
	 */
	public IntermediateLayer(){}


	/**
	 * @param ictr An internalConstraint object
	 * @param minLex Specifies if the point to return is the smallest or largest infeasible lexicographical point
	 * @param d Indicates which coordinate dimension we want to prune
	 * @param k The total number of dimensions (The dimension of the space we are working in)
	 * @param o The object in question
	 * @return A vector of 2 elements. The first is a Boolean object indicating the fact of whether a point was found and the second is a Point object
	 */
	public List lexInFeasible(InternalConstraint ictr, boolean minLex, int d, int k, Obj o)
	{
		List result = new ArrayList();
		switch (ictr.getIctrID())
		{
			case Constants.INBOX:
				result = lexInFeasibleForInbox((Inbox)ictr, minLex, d, k, o);
				break;
			case Constants.OUTBOX:
				result = lexInFeasibleForOutbox((Outbox)ictr, minLex, k, o);
				break;
			case Constants.AVOID_HOLES:
				result = lexInFeasibleForAvoidHoles();
				break;
			default: LOGGER.severe("A call to LexFeasible with incorrect ictrID parameter");
		}
		return result;
	}

	/**
	 * @param ictr An internalConstraint object
	 * @param min Specifies whether we want to prune the minimum or maximum value of coordinate at dimension d
	 * @param d Indicates which coordinate dimension we want to prune
	 * @param k The total number of dimensions (The dimension of the space we are working in)
	 * @param o The object in question
	 * @param c The point in question (so if c is feasible or not using object o)
	 * @return A vector of 2 elements. The first is a Boolean object indicating whether c is
	 * feasible or not and the second is a Region object indicating a forbidden region
	 * if the point is not feasible
	 */
	public List isFeasible(InternalConstraint ictr, boolean min, int d, int k, Obj o, Point c, Point jump)
	{
		List result = new ArrayList();
		switch (ictr.getIctrID())
		{
			case Constants.INBOX:
				result = isFeasibleForInbox((Inbox)ictr, min, d, k, o, c);
				break;
			case Constants.OUTBOX:
				result = isFeasibleForOutbox((Outbox)ictr, min, k, o, c);
				break;
			case Constants.AVOID_HOLES:
				result = isFeasibleForAvoidHoles();
				break;
            case Constants.DIST_LEQ_FR:
                //System.out.println("DIST_LEQ_FR.isFeasible");
                result = ((DistLeqIC)ictr).isFeasible(min, d, k, o, c, jump);                
                break;
            case Constants.DIST_GEQ_FR:
                //System.out.println("DIST_GEQ_FR.isFeasible");
                result = ((DistGeqIC)ictr).isFeasible(min, d, k, o, c, jump);
                break;
            case Constants.DIST_LINEAR_FR:
                //System.out.println("DIST_LINEAR_FR.isFeasible");
                result = ((DistLinearIC)ictr).isFeasible(min, d, k, o, c, jump);
                break;
			default:
                throw new SolverException("A call to IsFeasible with incorrect ictrID parameter");
		}
		return result;
	}

	/**
	 * @param ictr An internalConstraint object
	 * @param k The total number of dimensions (The dimension of the space we are working in)
	 * @param o The object in question
	 * @return An integer indicating the number of infeasible points for the origin of the object o under the assumption that ictr holds
	 */
	public int  CardInfeasible(InternalConstraint ictr, int k, Obj o)
	{
		int result = 0;
		switch (ictr.getIctrID())
		{
			case Constants.INBOX:
				result = CardInfeasibleForInbox((Inbox)ictr, k, o);
				break;
			case Constants.OUTBOX:
				result = CardInfeasibleForOutbox((Outbox)ictr, k, o);
				break;
			case Constants.AVOID_HOLES:
				result = cardInfeasibleForAvoidHoles();
				break;
			default: LOGGER.severe("A call to CardInfeasible with incorrect ictr parameter");
		}
		return result;
	}


	private List lexInFeasibleForInbox(Inbox ictr, boolean minLex, int d, int k, Obj o)
	{

		//RETURNS a vector of 2 elements. The first is a Boolean object and the second is a Point object
		List<Object> result = new ArrayList<Object>();

		int[] t = new int[ictr.getT().length];
		t = ictr.getT();
		int[] l = new int[ictr.getL().length];
		l = ictr.getL();
		boolean in = true;
		Point p = new Point(k);
		for(int j = 0; j < k; j++)
		{
			if (minLex)
				p.setCoord(j, o.getCoord(j).getInf());
			else
				p.setCoord(j, o.getCoord(j).getSup());

			if((p.getCoord(j) < t[j]) || (p.getCoord(j) > t[j] + l[j] - 1))
				in = false;
		}

		if (in)
		{
			for(int j = k-1; j >= 0; j--)
			{
				int jPrime = (j + d) % k;
				if (minLex)
				{
					if(t[jPrime] + l[jPrime] <= o.getCoord(jPrime).getSup())
					{
						p.setCoord(jPrime, t[jPrime] + l[jPrime]);
						result.clear();
						result.add(0, true);
						result.add(1, p);
						return result;
					}
					else
					{
						if(t[jPrime] - 1 >= o.getCoord(jPrime).getInf())
						{
							p.setCoord(jPrime, t[jPrime] - 1);
							result.clear();
							result.add(0, true);
							result.add(1, p);
							return result;
						}
					}
				}
			}
			result.clear();
			result.add(0, false);
			result.add(1, p);
			return result;
		}
		else
		{
			result.clear();
			result.add(0, true);
			result.add(1, p);
			return result;
		}
	}

	private List lexInFeasibleForOutbox(Outbox ictr, boolean minLex, int k, Obj o)
	{
		//RETURNS a vector of 2 elements. The first is a Boolean object and the second is a Point object
		List<Object> result = new ArrayList<Object>();
		int[] t = new int[ictr.getT().length];
		t = ictr.getT();
		int[] l = new int[ictr.getL().length];
		l = ictr.getL();
		Point p = new Point(k);

		for (int j = 0; j < k; j++)
		{
			if((o.getCoord(j).getSup() < t[j]) || (o.getCoord(j).getInf() > t[j] + l[j] - 1))
			{
				result.clear();
				result.add(0, false);
				result.add(1, p);
				return result;
			}
			if (minLex)
				p.setCoord(j, Math.max(t[j], o.getCoord(j).getInf()));
			else
				p.setCoord(j, Math.max(t[j] + l[j] - 1, o.getCoord(j).getSup()));
		}
		result.clear();
		result.add(0, true);
		result.add(1, p);
		return result;
	}

	private List lexInFeasibleForAvoidHoles()
	{
		//RETURNS a vector of 2 elements. The first is a Boolean object and the second is a Point object

        return new ArrayList();
	}


	private List isFeasibleForInbox(Inbox ictr, boolean min, int d, int k, Obj o, Point c)
	{
		//RETURNS a vector of 2 elements. The first is a Boolean object and the second is a Region object
		List<Object> result = new ArrayList<Object>();
		int[] t = new int[ictr.getT().length];
		t = ictr.getT();
		int[] l = new int[ictr.getL().length];
		l = ictr.getL();

		boolean before = false;
		boolean after = false;
		boolean feasible = false;
		Region f = new Region(k,o.getObjectId());

		for(int j = 0; j < k; j++)
		{
			int jPrime = (j + d) % k;
			if(min)
			{
				f.setMinimumBoundary(jPrime, c.getCoord(jPrime));
				if((c.getCoord(jPrime) < t[jPrime]) && (!before))
				{
					f.setMaximumBoundary(jPrime, t[jPrime] -1);
					before = true;
				}
				else
				{
					f.setMaximumBoundary(jPrime, o.getCoord(jPrime).getSup());
					if(c.getCoord(jPrime) > t[jPrime] + l[jPrime] - 1)
						after = true;
				}
			}
			else
			{
				f.setMaximumBoundary(jPrime, c.getCoord(jPrime));
				if(c.getCoord(jPrime) < t[jPrime])
					before = true;
			}
		}
		feasible = !(before || after);
		result.clear();
		result.add(0, feasible);
		result.add(1, f);
		return result;
	}

	private List isFeasibleForOutbox(Outbox ictr, boolean min, int k, Obj o, Point c)
	{
		//RETURNS a vector of 2 elements. The first is a Boolean object and the second is a Region object
		List<Object> result = new ArrayList<Object>();
		int[] t = new int[ictr.getT().length];
		t = ictr.getT();
		int[] l = new int[ictr.getL().length];
		l = ictr.getL();
		Region f = new Region(k, o.getObjectId());

		for(int j = 0; j < k; j++)
		{
			if((c.getCoord(j) < t[j]) || (c.getCoord(j) > t[j] + l[j] - 1))
			{
				result.clear();                                                                         
				result.add(0, true);
				result.add(1, f);
				return result;
			}

			if (min)
			{
				f.setMinimumBoundary(j, c.getCoord(j));
				f.setMaximumBoundary(j, Math.min(o.getCoord(j).getSup(), t[j] + l[j] - 1));
			}
			else
			{
				f.setMaximumBoundary(j, c.getCoord(j));
				f.setMinimumBoundary(j, Math.max(o.getCoord(j).getInf(), t[j]));
			}
		}
		result.clear();
		result.add(0, false);
		result.add(1, f);
		return result;
	}


	private List isFeasibleForAvoidHoles()
	{
		//RETURNS a vector of 2 elements. The first is a Boolean object and the second is a Region object
        return new ArrayList<Object>();
	}



	private int CardInfeasibleForInbox(Inbox ictr, int k, Obj o)
	{
		//RETURNS an interger indicating the number of infeasible points for the origin of the object o
		int n = 1;
		int[] t = new int[ictr.getT().length];
		t = ictr.getT();
		int[] l = new int[ictr.getL().length];
		l = ictr.getL();

		for(int j = 0; j < k; j++){
			n = n * (o.getCoord(j).getSup() - o.getCoord(j).getInf() + 1);
        }

		int m = 1;
		for(int j = 0; j < k; j++){
			m = m * Math.max(0, ((Math.min(o.getCoord(j).getSup(), t[j] + l[j] - 1) - Math.max(o.getCoord(j).getInf(), t[j])) + 1));
        }

		return n - m;
	}

	private int CardInfeasibleForOutbox(Outbox ictr, int k, Obj o)
	{
		//RETURNS an interger indicating the number of infeasible points for the origin of the object o
		int n = 1;
		int[] t = new int[ictr.getT().length];
		t = ictr.getT();
		int[] l = new int[ictr.getL().length];
		l = ictr.getL();

		for(int j = 0; j < k; j++){
			n = n * ((Math.min(o.getCoord(j).getSup(), t[j] + l[j] -1) - Math.max(o.getCoord(j).getInf(), t[j])) + 1);
        }

		return n;
	}

	private int cardInfeasibleForAvoidHoles()
	{
		//RETURNS an interger indicating the number of infeasible points for the origin of the object o

        return 0;
	}



    

}