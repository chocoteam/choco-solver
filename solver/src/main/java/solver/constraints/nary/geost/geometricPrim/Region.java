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

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * This class represents a k dimensional Region (where k is specified as a global constant in the global.Constants class).
 * Also each region should attached to an object therefore the Object id  should be specified in the constructor
 */
public final class Region implements Externalizable {
	
	private int oid;   //object id
	private int min[]; //boundary minimum for each dimension
	private int max[]; //boundary maximum for each dimension
	private int dim;
    private String type=""; //method that generated the constraint (single constraint, union intersection, union diagonal method)
    public int mid=-1;
    public int dicho_ext=-1; //external dimension, 0 for x, 1 for y.
    public int dicho_int=-1; //external dimension, 0 for x, 1 for y.
    public int orientation=0; //0: left to right diagonal; 1:right to left diagonal
    public String father="<not defined>";
    public String info="";
    public boolean case_a_or_c=false;



	/**
	 * @param objectId The object id that this region attached to.
	 * @param minimumBoundary an array of the minimum boundary of this region in every dimension
	 * @param maximumBoundary an array of the maximum boundary of this region in every dimension
	 */
	public Region(int dim, int objectId, int minimumBoundary[], int maximumBoundary[])
	{
		this.dim = dim;
		this.oid = objectId;
		this.min = new int[this.dim];
		this.max = new int[this.dim];
		this.min = minimumBoundary;
		this.max =  maximumBoundary;

	}
	
	/**
	 * Constructs an empty region for this object id.
	 * @param objectId The object id that this region belong to.
	 */
	public Region(int dim, int objectId)
	{
		this.dim = dim;
		this.oid = objectId;
		this.min = new int[this.dim];
		this.max = new int[this.dim];
	}

    public Region(int dim, Obj o)
    {
        this.dim = dim;
        this.oid = o.getObjectId();
        this.min = new int[this.dim];
        this.max = new int[this.dim];
        for (int i=0; i<this.dim; i++) {
            min[i]=o.getCoord(i).getInf();
            max[i]=o.getCoord(i).getSup();            
        }
    }


    public Region(Region toCopy)
    {
        this.dim = toCopy.dim;
        this.oid = toCopy.getObjectId();
        this.min = new int[this.dim];
        this.max = new int[this.dim];
        this.mid=toCopy.mid;
        this.dicho_ext = toCopy.dicho_ext;
        this.dicho_int = toCopy.dicho_int;
        this.info = toCopy.info;
        this.father = toCopy.father;
        this.case_a_or_c=toCopy.case_a_or_c;

        this.setType(toCopy.getType());
        for (int i=0; i<this.dim; i++)
            {this.min[i]=toCopy.getMinimumBoundary(i); this.max[i]=toCopy.getMaximumBoundary(i);}
    }


    public Region(Point p, int objectId)
    {
        this.dim = p.getCoords().length;
        this.oid = objectId;
        this.min = new int[this.dim];
        this.max = new int[this.dim];
        for (int i=0; i<dim; i++) {this.min[i]=p.getCoord(i); this.max[i]=p.getCoord(i);}
    }

    public Region(Point p)
    {
        this.dim = p.getCoords().length;
        this.oid = 0;
        this.min = new int[this.dim];
        this.max = new int[this.dim];
        for (int i=0; i<dim; i++) {this.min[i]=p.getCoord(i); this.max[i]=p.getCoord(i);}
    }

    public Region(Point minimum, Point maximum)
    {
        this.dim = minimum.getCoords().length;
        this.oid = 0;
        this.min = new int[this.dim];
        this.max = new int[this.dim];
        for (int i=0; i<dim; i++) {this.min[i]=minimum.getCoord(i); this.max[i]=maximum.getCoord(i);}
    }

	
	public void setObjectId(int objectId)
	{
		this.oid = objectId;
	}
	
	public int getObjectId()
	{
		return this.oid;
	}
	
	public void setMinimumBoundary(int index, int value)
	{
		this.min[index] = value;
	}
	
	public void setMinimumBoundary(int minimumBoundary[])
	{
		this.min = minimumBoundary.clone();
	}
	
	public int getMinimumBoundary(int index)
	{
		return this.min[index];
	}
	
	public void setMaximumBoundary(int index, int value)
	{
		this.max[index] = value;
	}
	
	public void setMaximumBoundary(int maximumBoundary[])
	{
		this.max = maximumBoundary.clone();
	}
	
	public int getMaximumBoundary(int index)
	{
		return this.max[index];
	}
    public String toString() {
        StringBuilder res = new StringBuilder();
        res.append("(");
//        for (int i=0; i<dim; i++)
//            res += min[i] +" ";
//        for (int i=0; i<dim; i++)
//            res += max[i] +" ";
        for (int i=0; i<dim; i++){
            if (i!=dim-1){
                res.append("[").append(min[i]).append(",").append(max[i]).append("],");
            }else{
                res.append("[").append(min[i]).append(",").append(max[i]).append("]");
            }
        }
        res.append(")");
        return res.toString();

    }

    public int volume() {
        int result=1;
        for (int i=0; i<dim; i++) {
            result*=Math.abs(min[i]-max[i])+1;
        }
        return result;        
    }


    public void writeExternal(ObjectOutput out) throws IOException {
        //To change body of implemented methods use File | Settings | File Templates.
        out.writeObject(oid);   //object id
        out.writeObject(min.length);
        for (int i=0; i<min.length; i++){
            out.writeObject(min[i]);
        }
        out.writeObject(max.length);
        for (int i=0; i<max.length; i++){
            out.writeObject(max[i]);
        }
        out.writeObject(dim);

    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        //To change body of implemented methods use File | Settings | File Templates.
        oid=(Integer) in.readObject();
        int n=(Integer) in.readObject();
        min=new int[n];
        for (int i=0; i<n; i++){
            min[i]=(Integer) in.readObject();
        }
        n=(Integer) in.readObject();
        max=new int[n];
        for (int i=0; i<n; i++){
            max[i]=(Integer) in.readObject();
        }
        dim=(Integer) in.readObject();        
    }

    public boolean isPoint() {
        for (int i=0; i<dim; i++){
            if (min[i]!=max[i]){
                return false;
            }
        }
        return true;
    }

    public Point point() {
        return pointMin(); 
    }
    public Point pointMin() {
        return new Point(min);
    }
    public Point pointMax() {
        return new Point(max);
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getSize(int i) {
        return getMaximumBoundary(i)-getMinimumBoundary(i)+1;
    }

    public double ratio() {
        double min=0,max=0;
        for (int i=0; i<dim; i++) {
            int s = getSize(i);
            if ((i==0) || (s<min)) min=s;
            if ((i==0) || (s>max)) max=s;
        }
        return min/max;
    }

    public boolean included(Region box) {
       //is this included in box?
        for (int i=0; i<dim; i++) {
            int a = min[i];
            int b = max[i];
            int c = box.getMinimumBoundary(i);
            int d = box.getMaximumBoundary(i);
            if (!( (a>=c) && (a<=d) && (b>=c) && (b<=d))) return false;
        }
        return true;
    }



}
