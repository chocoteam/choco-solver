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

package solver.constraints.nary.geost.frames;


import choco.cp.solver.constraints.global.geost.geometricPrim.Region;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.HashMap;
import java.util.List;

/**
 * A class that all Frames should extend. It contains info and functionality common to all frames.
 */
public class Frame implements Externalizable {
	
	/**
	 * Integer for the object id and the vector is the relative Forbidden Regions of every shifted box of the shapes of the object
	 */
	private HashMap<Integer, List<Region>> RelForbidRegions;
	
	public Frame()
	{
		RelForbidRegions = new HashMap<Integer, List<Region>>();
	}

	/**
	 * Gets the Relative forbidden regions of this frame. It return a hash table where the key is an Integer object representing the shape id and the value a vector of Region object.
	 */
	public final HashMap<Integer, List<Region>> getRelForbidRegions()
	{
		return RelForbidRegions;
	}
	
	/**
	 * Adds a given shape id and a Vector of regions to the Frame.
	 */
	public final void addForbidRegions(int oid, List<Region> regions)
	{
		this.RelForbidRegions.put(oid, regions);
	}
	
	/**
	 * Gets the Relative forbidden regions of a certain shape id. It returns Vector of Region object.
	 */
	public final List<Region> getRelForbidRegions(int oid)
	{
		return this.RelForbidRegions.get(oid);
	}
	
	/**
	 * Returns the size of the frame.
	 */
	public final int size()
	{
		return RelForbidRegions.size();
	}


    public final void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(RelForbidRegions);

    }

    @SuppressWarnings({"unchecked"})
    public final void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        //To change body of implemented methods use File | Settings | File Templates.
        RelForbidRegions=(HashMap<Integer, List<Region>>) in.readObject();        
    }
}
