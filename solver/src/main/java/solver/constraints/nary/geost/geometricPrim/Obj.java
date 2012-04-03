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

import choco.cp.solver.constraints.global.geost.externalConstraints.ExternalConstraint;
import choco.cp.solver.constraints.global.geost.internalConstraints.InternalConstraint;
import choco.cp.solver.constraints.global.geost.internalConstraints.Outbox;
import choco.kernel.common.logging.ChocoLogging;
import choco.kernel.solver.variables.integer.IntDomainVar;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;


/**
 * This class represent an Object of our placement problem.
 */
public final class Obj implements Externalizable {

    private static final Logger LOGGER = ChocoLogging.getEngineLogger();

	private int oid;          //Object id
	private IntDomainVar sid; // the shape id that corresponds to this object
	private IntDomainVar[] coords;
	private IntDomainVar start;
	private IntDomainVar duration;
	private IntDomainVar end;
	private List<ExternalConstraint> relatedExternalConstraints;
	private List<InternalConstraint> relatedInternalConstraints;
	private int dim;
    private int radius;

    public Obj(){}
 

    /**
	 * Creates an object with the given parameters
	 * @param dim An integer representing the dimension of the placement problem
	 * @param objectId An integer representing the object id
	 * @param shapeId  An Integer Domain Variable representing the possible shape ids the Object can have
	 * @param coordinates An array of size k of Integer Domain Variables (where k is the dimension of the space we are working in) representing the Domain of our object origin
	 * @param startTime An Integer Domain Variable representing the time that the object start in
	 * @param durationTime An Integer Domain Variable representing the duration
	 * @param endTime An Integer Domain Variable representing the time that the object ends in
	 */
	public Obj(int dim, int objectId, IntDomainVar shapeId, IntDomainVar[] coordinates, IntDomainVar startTime , IntDomainVar durationTime, IntDomainVar endTime)
	{
		this.dim = dim;
		this.oid = objectId;
		this.sid = shapeId;
		this.coords = coordinates;
		this.start = startTime;
		this.duration = durationTime;
		this.end = endTime;
		this.relatedExternalConstraints = new ArrayList<ExternalConstraint>();
		this.relatedInternalConstraints = new ArrayList<InternalConstraint>();
        this.radius=-1;
	}

    public Obj(int dim, int objectId, IntDomainVar shapeId, IntDomainVar[] coordinates, IntDomainVar startTime , IntDomainVar durationTime, IntDomainVar endTime, int radius)
    {
        this.dim = dim;
        this.oid = objectId;
        this.sid = shapeId;
        this.coords = coordinates;
        this.start = startTime;
        this.duration = durationTime;
        this.end = endTime;
        this.relatedExternalConstraints = new ArrayList<ExternalConstraint>();
        this.relatedInternalConstraints = new ArrayList<InternalConstraint>();
        this.radius=radius;
    }


	/**
	 * Creates an object in a certain given dimension
	 */
	public Obj(int dim)
	{
		this.dim = dim;
		this.coords = new IntDomainVar[this.dim];
		this.relatedExternalConstraints = new ArrayList<ExternalConstraint>();
		this.relatedInternalConstraints = new ArrayList<InternalConstraint>();
	}

	/**
	 * Gets the Object id
	 */
	public int getObjectId()
	{
		return this.oid;
	}

	/**
	 * Sets the Object id
	 */
	public void setObjectId(int objectId)
	{
		this.oid = objectId;
	}

	/**
	 * Gets the Shape id domain variable
	 */
	public IntDomainVar getShapeId()
	{
		return this.sid;
	}

	/**
	 * Sets the Shape id domain variable
	 */
	public void setShapeId(IntDomainVar shapeId)
	{
		this.sid = shapeId;
	}

	/**
	 * Gets all the coordinate domain variables of the object origin
	 */
	public IntDomainVar[] getCoordinates()
	{
		return this.coords;
	}

	/**
	 * Sets all the coordinate domain variables of the object origin to the ones given as parameter
	 */
	public void setCoordinates(IntDomainVar[] coordinates)
	{
		this.coords = coordinates;
	}

	/**
	 * Sets a coordinate domain variables of the object origin at the given dimension given by the parameter index, to another domain variable given by the parameter value.
	 */
	public void setCoord(int index, IntDomainVar value)
	{
		this.coords[index] = value;
	}

	/**
	 * Gets the index coordinate domain variable  of the object origin
	 */
	public IntDomainVar getCoord(int index)
	{
		return this.coords[index];
	}

	public IntDomainVar getDuration() {
		return duration;
	}

	public void setDuration(IntDomainVar duration) {
		this.duration = duration;
	}

	public IntDomainVar getEnd() {
		return end;
	}

	public void setEnd(IntDomainVar end) {
		this.end = end;
	}

	public IntDomainVar getStart() {
		return start;
	}

	public void setStart(IntDomainVar start) {
		this.start = start;
	}

	/**
	 * Gets all Related External Constraints to this object.
	 */
	public List<ExternalConstraint> getRelatedExternalConstraints() {
		return relatedExternalConstraints;
	}

	/**
	 * Gets all Related Internal Constraints to this object.
	 */
	public List<InternalConstraint> getRelatedInternalConstraints() {
		return relatedInternalConstraints;
	}


	/**
	 * Sets all Related External Constraints to this object.
	 */
	public void setRelatedExternalConstraints(List<ExternalConstraint> relatedExtConstraints) {
		this.relatedExternalConstraints = relatedExtConstraints;
	}

	/**
	 * Sets all Related Internal Constraints to this object.
	 */
	public void setRelatedInternalConstraints(List<InternalConstraint> relatedIntConstraints) {
		this.relatedInternalConstraints = relatedIntConstraints;
	}

	/**
	 * Adds a Related External Constraint to this object.
	 */
	public void addRelatedExternalConstraint(ExternalConstraint ectr)
	{
		this.relatedExternalConstraints.add(ectr);
	}

	/**
	 * Adds a Related Internal Constraint to this object.
	 */
	public void addRelatedInternalConstraint(InternalConstraint ictr)
	{
		this.relatedInternalConstraints.add(ictr);
	}




	/**
	 * Calculate the domain size (to check if we pruned the object at a certain iteration)
	 */
	public int calculateDomainSize()
	{
		int result = 0;
		for (int i = 0; i < this.coords.length; i++)
		{
			result = result + (this.getCoord(i).getSup() - this.getCoord(i).getInf()) + 1; // the coordinates are BoundIntVar
		}

		result = result + this.sid.getDomainSize();  // the shape is EnumIntVar

//      result = result + (this.start.getSup() - this.start.getInf()) + 1;
//      result = result + (this.duration.getSup() - this.duration.getSup()) + 1;
//      result = result + (this.end.getSup() - this.end.getInf()) + 1;

		return result;
	}
    
    public boolean coordInstantiated() {

        for (int i = 0; i < this.coords.length; i++){
            if (!this.getCoord(i).isInstantiated()){
            return false;
            }
        }
        return true;
        
    }

    public boolean sameDomain(Obj o) {
        IntDomainVar[] idv = getCoordinates();
        for (int i=0; i<idv.length; i++) {
//            IntDomainVar coord = getCoord(i);
//            IntDomainVar o_coord = o.getCoord(i);
//            int coord_inf = coord.getInf();
//            int coord_sup = coord.getSup();
//            int o_coord_inf = o_coord.getInf();
//            int o_coord_sup = o_coord.getSup();
//            int size = coord_sup - coord_inf -1;
//            int o_size = o_coord_sup - o_coord_inf -1;
//            if (size!=o_size) return false;
//            if (coord_inf!= o_coord_inf) return false;
//            if (coord_sup!= o_coord_sup) return false;
//            if (coord.getDomainSize()!=o_coord.getDomainSize()) return false;
//            if (coord.getInf()!=o_coord.getInf()) return false;
//            if (coord.getSup()!=o_coord.getSup()) return false;


            if (getCoord(i).getDomainSize()!=o.getCoord(i).getDomainSize()) return false;
            if (getCoord(i).getInf()!=o.getCoord(i).getInf()) return false;
            if (getCoord(i).getSup()!=o.getCoord(i).getSup()) return false;

        }
        return true;
    }

    public void print() {
		for (int i = 0; i < this.coords.length; i++)
		{
            if (!this.getCoord(i).isInstantiated())
                LOGGER.info(this.getCoord(i).getInf() +" " + this.getCoord(i).getSup() +",");
            else
                LOGGER.info(this.getCoord(i).getInf() +" ");
        }
    }

    public String toString() {
        StringBuilder res = new StringBuilder();
		for (int i = 0; i < this.coords.length; i++)
		{
            if (i+1!=this.coords.length)
                if (!this.getCoord(i).isInstantiated())
                    res.append("[").append(this.getCoord(i).getInf()).append(",")
                            .append(this.getCoord(i).getSup()).append("],");
                else
                    res.append(this.getCoord(i).getInf()).append(",");
            else
                if (!this.getCoord(i).isInstantiated())
                    res.append("[").append(this.getCoord(i).getInf()).append(",").append(this.getCoord(i).getSup()).append("]");
                else
                    res.append(this.getCoord(i).getInf()).append("");

        }
        return res.toString();
    }

    public Outbox intersect(Outbox ob) {
        int[] ob_l = ob.getL();
        int[] ob_t = ob.getT();

        int nbDim = ob_l.length;        
        int[] t = new int[ob_l.length];
        int[] l = new int[ob_l.length];
        IntDomainVar[] dom = getCoordinates();
        for (int i=0; i<nbDim; i++) {
            l[i]=ob_l[i];

            if (ob_t[i]>dom[i].getSup()) return null;
            if (ob_t[i]<dom[i].getInf()) t[i]=dom[i].getInf();

            if (ob_t[i]+ob_l[i]<dom[i].getInf()) return null;
            if (ob_t[i]+ob_l[i]>dom[i].getSup()) {
                if (dom[i].getSup()>=ob_l[i]) t[i]=dom[i].getSup()-ob_l[i]; else t[i]=0;
            }

        }

        return new Outbox(t,l);

    }

    public boolean isSphere() { return (radius!=-1); }

    public int getRadius() { return radius; }

	public void clearInternalConstraints()
	{
		this.relatedInternalConstraints.clear();
	}

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(oid);
        out.writeObject(sid.getInf());
        out.writeObject(sid.getSup());
        out.writeObject(coords.length);
        for (int i=0; i<coords.length; i++) {
            out.writeObject(coords[i].getInf());
            out.writeObject(coords[i].getSup());
        }
        out.writeObject(start.getInf());
        out.writeObject(start.getSup());
        out.writeObject(duration.getInf());
        out.writeObject(duration.getSup());
        out.writeObject(end.getInf());
        out.writeObject(end.getSup());
        out.writeObject(relatedExternalConstraints);
        out.writeObject(relatedInternalConstraints);
        out.writeObject(dim);
        out.writeObject(radius);                
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
//        oid=(Integer) in.readObject();
//        sid = (IntDomainVar) new IntDomainVarImplFake((Integer) in.readObject(), (Integer) in.readObject());
//        int n = (Integer) in.readObject();
//        coords = new IntDomainVar[n];
//        for (int i=0; i<n; i++)
//            coords[i] = (IntDomainVar) new IntDomainVarImplFake((Integer) in.readObject(), (Integer) in.readObject());
//        start = (IntDomainVar) new IntDomainVarImplFake((Integer) in.readObject(), (Integer) in.readObject());
//        duration = (IntDomainVar) new IntDomainVarImplFake((Integer) in.readObject(), (Integer) in.readObject());
//        end = (IntDomainVar) new IntDomainVarImplFake((Integer) in.readObject(), (Integer) in.readObject());
//        relatedExternalConstraints = (List<ExternalConstraint>) in.readObject();
//        relatedInternalConstraints = (List<InternalConstraint>) in.readObject();
//        dim = (Integer) in.readObject();
//        radius = (Integer) in.readObject();        

    }

    //Is p inside domain of o?
    public boolean isInside(Point p) {
        for (int i=0; i<coords.length; i++)
            if (!coords[i].canBeInstantiatedTo(p.getCoord(i)))
                return false;
        return true;        
    }
}
