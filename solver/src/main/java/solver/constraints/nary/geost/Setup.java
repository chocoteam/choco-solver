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

import choco.cp.solver.constraints.global.Geost_Constraint;
import choco.cp.solver.constraints.global.geost.dataStructures.HeapAscending;
import choco.cp.solver.constraints.global.geost.dataStructures.HeapDescending;
import choco.cp.solver.constraints.global.geost.externalConstraints.ExternalConstraint;
import choco.cp.solver.constraints.global.geost.geometricPrim.Obj;
import choco.kernel.common.logging.ChocoLogging;
import choco.kernel.model.constraints.geost.GeostOptions;
import choco.kernel.model.variables.geost.ShiftedBox;
import choco.kernel.solver.propagation.PropagationEngine;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

/**
 * This is a very important class. It contains all the variables and objects the constraint needs.
 * Also it contains functions that the user and the constraint use to access the shapes, objects as well as the external constraints in the Geost.
 */

public final class Setup {

    private static final Logger LOGGER = ChocoLogging.getEngineLogger();

	private final Constants cst;

    public GeostOptions opt = new GeostOptions();

    public final PropagationEngine propagationEngine;

    public final Geost_Constraint g_constraint;

    /**
	 * Creates a Setup instance for a given Constants class
	 * @param c An instance of the constants class
     * @param propagationEngine
     * @param constraint
     */
	public Setup(Constants c, PropagationEngine propagationEngine, final Geost_Constraint constraint)
	{
		cst = c;
        this.propagationEngine = propagationEngine;
        this.g_constraint = constraint;
	}

	/**
	 * A hashtable where the key is a shape_id. And for every shape_id there is a pointer to the set of shifted_boxes that belong to this shape.
	 * This hashtable contains all the shapes (and their shifted boxes) of all the objects in the geost constraint.
	 */
	public final Hashtable<Integer, List<ShiftedBox>> shapes = new Hashtable<Integer, List<ShiftedBox>>();
	/**
	 * A hashtable where the key is an object_id. And for every object_id there is a pointer to the actual object.
	 * This hashtable contains all the objects that goest needs to place.
	 */
	public final Hashtable<Integer, Obj> objects = new Hashtable<Integer, Obj>();
	/**
	 * A Vector containing ExternalConstraint objects. This vector constains all the external constraints that geost needs to deal with.
	 */
	private final List<ExternalConstraint> constraints = new ArrayList<ExternalConstraint>();
	/**
	 * A heap data structure containting elements in ascending order (lexicographically).
	 * This is not used anymore.
	 * It was used inside that pruneMin function and we used to store in it the internal constraints.
	 * This way we coulld extract the active internal constraints at a specific position
	 */
	private final transient HeapAscending  ictrMinHeap = new HeapAscending();
	/**
	 * A heap data structure containting elements in descending order (lexicographically).
	 * This is not used anymore.
	 * It was used inside that pruneMax function and we used to store in it the internal constraints.
	 * This way we coulld extract the active internal constraints at a specific position
	 */
	private final transient HeapDescending ictrMaxHeap = new HeapDescending();


	public void insertShape(int sid, List<ShiftedBox> shiftedBoxes)
	{
		shapes.put(sid, shiftedBoxes);
	}

	public void insertObject(int oid, Obj o)
	{
		objects.put(oid, o);
	}


	public List<ShiftedBox> getShape(int sid)
	{
		return shapes.get(sid);
	}

	public Obj getObject(int oid)
	{
		return objects.get(oid);
	}

	public int getNbOfObjects()
	{
		return objects.size();
	}


	public int getNbOfShapes()
	{
		return shapes.size();
	}

	/**
	 * This function calculates the number of the domain variables in our problem.
	 */
	public int getNbOfDomainVariables()
	{
		int originOfObjects = getNbOfObjects() * cst.getDIM(); //Number of domain variables to represent the origin of all objects
		int otherVariables = getNbOfObjects() * 4; //each object has 4 other variables: shapeId, start, duration; end
		return originOfObjects + otherVariables;
	}

	/**
	 * Creates the environment and sets up the problem for the geost constraint given a parser object.
	 */
//	public void createEnvironment(InputParser parser)
//	{
//		for(int i = 0; i < parser.getObjects().size(); i++)
//		{
//			insertObject(parser.getObjects().elementAt(i).getObjectId(), parser.getObjects().elementAt(i));
//		}
//
//		for(int i = 0; i < parser.getShapes().size(); i++)
//		{
//			insertShape(parser.getShapes().elementAt(i).getShapeId(), parser.getShapes().elementAt(i).getShiftedBoxes());
//		}
//	}

//	public void SetupTheProblem(Vector<Obj> objects, Vector<ShiftedBox> shiftedBoxes)
//	{
//		for(int i = 0; i < objects.size(); i++)
//		{
//			addObject(objects.elementAt(i));
//		}
//
//		for(int i = 0; i < shiftedBoxes.size(); i++)
//		{
//			addShiftedBox(shiftedBoxes.elementAt(i));
//		}
//
//	}

  	/**
	 * Given a Vector of Objects and a Vector of shiftedBoxes and a Vector of ExternalConstraints it sets up the problem for the geost constraint.
	 */
	public void SetupTheProblem(List<Obj> objects, List<ShiftedBox> shiftedBoxes, List<ExternalConstraint> ectr)
	{
		for(int i = 0; i < objects.size(); i++)
		{
			addObject(objects.get(i));
		}

		for(int i = 0; i < shiftedBoxes.size(); i++)
		{
			addShiftedBox(shiftedBoxes.get(i));
		}

		for(int i = 0; i < ectr.size(); i++)
		{
			addConstraint(ectr.get(i));
			for(int j = 0; j < ectr.get(i).getObjectIds().length; j++)
			{
				getObject(ectr.get(i).getObjectIds()[j]).addRelatedExternalConstraint(ectr.get(i));
			}                                                           
		}

	}

  	void addConstraint(ExternalConstraint ectr)
	{
		constraints.add(ectr);
	}

	public List<ExternalConstraint> getConstraints()
	{
		return constraints;
	}

	public HeapAscending getIctrMinHeap() {
		return ictrMinHeap;
	}

	public HeapDescending getIctrMaxHeap() {
		return ictrMaxHeap;
	}

	void addShiftedBox(ShiftedBox sb)
	{
		if (shapes.containsKey(sb.getShapeId())) {
			shapes.get(sb.getShapeId()).add(sb);
		} else
		{
			List<ShiftedBox> v = new ArrayList<ShiftedBox>();
			v.add(sb);
			shapes.put(sb.getShapeId(), v);
		}
	}

	void addObject(Obj o)
	{
		if (objects.containsKey(o.getObjectId())) {
			LOGGER.info("Trying to add an already existing object. In addObject in Setup");
		} else {
			objects.put(o.getObjectId(), o);
		}
	}

	public Enumeration<Integer> getObjectKeys()
	{
		return objects.keys();
	}

	public Enumeration<Integer> getShapeKeys()
	{
		return shapes.keys();
	}

	public Set<Integer> getObjectKeySet()
	{
		return objects.keySet();
	}

	public Set<Integer> getShapeKeySet()
	{
		return shapes.keySet();
	}
	/**
	 * Prints to the output console the objects and the shapes of the problem.
	 */
	public void print()
	{
		Iterator<Integer> itr;
		itr = objects.keySet().iterator();
		while(itr.hasNext())
		{
			int id = itr.next();
			Obj o = objects.get(id);
			LOGGER.info("object id: " + id);
			LOGGER.info("    shape id: " + o.getShapeId().getInf());
			for (int i = 0; i < cst.getDIM(); i++) {
				LOGGER.info("    Coords x" + i + " : " + o.getCoord(i).getInf() + "    " + o.getCoord(i).getSup());
			}
		}

		itr = shapes.keySet().iterator();
		while(itr.hasNext())
		{
			int sid = itr.next();
			List<ShiftedBox> sb = shapes.get(sid);
			LOGGER.info("shape id: " + sid);
			for(int i = 0; i < sb.size(); i++)
			{
				StringBuilder offset = new StringBuilder();
				StringBuilder size = new StringBuilder();
				for (int j = 0; j < cst.getDIM(); j++)
				{
                    offset.append(sb.get(i).getOffset(j)).append("  ");
                    size.append(sb.get(i).getSize(j)).append("  ");
				}
				LOGGER.info("    sb" + i + ": ");
				LOGGER.info("       Offset: " +  offset.toString());
				LOGGER.info("       Size: " +  size.toString());
			}
		}
	}
	/**
	 * Prints to a file that can be easily read by a person the objects and the shapes of the problem.
	 * The file to be written to is specified in the global variable OUTPUT_OF_RANDOM_GEN_PROB_TO_BE_READ_BY_HUMANS,
	 * present in the global.Constants class.
	 */
	public boolean printToFileHumanFormat(String path)
	{
	    try {
	        BufferedWriter out = new BufferedWriter(new FileWriter(path));

			Iterator<Integer> itr;
			itr = objects.keySet().iterator();
			while(itr.hasNext())
			{
				int id = itr.next();
				Obj o = objects.get(id);
				out.write("object id: " + id + '\n');
				out.write("    shape id: " + o.getShapeId().getInf() + '\n');
				for (int i = 0; i < cst.getDIM(); i++) {
					out.write("    Coords x" + i + " : " + o.getCoord(i).getInf() + "    " + o.getCoord(i).getSup() + '\n');
				}
			}

			itr = shapes.keySet().iterator();
			while(itr.hasNext())
			{
				int sid = itr.next();
				List<ShiftedBox> sb = shapes.get(sid);
				out.write("shape id: " + sid + '\n');
				for(int i = 0; i < sb.size(); i++)
				{
					StringBuilder offset = new StringBuilder();
					StringBuilder size = new StringBuilder();
					for (int j = 0; j < cst.getDIM(); j++)
					{
                        offset.append(sb.get(i).getOffset(j)).append("  ");
                        size.append(sb.get(i).getSize(j)).append("  ");
					}
					out.write("    sb" + i + ": " + '\n');
					out.write("       Offset: " +  offset.toString() + '\n');
					out.write("       Size: " +  size.toString() + '\n');
				}
			}
			out.close();
	    } catch (IOException ignored) {
	    }


		return true;
	}

	/**
	 * Prints to a file  the objects and the shapes of the problem. The written file can be read by the InputParser class.
	 * The file to be written to is specified in the global variable OUTPUT_OF_RANDOM_GEN_PROB_TO_BE_USED_AS_INPUT,
	 * present in the global.Constants class.
	 */
	public boolean printToFileInputFormat(String path)
	{
	    try {
	        BufferedWriter out = new BufferedWriter(new FileWriter(path));

			Iterator<Integer> itr;
			itr = objects.keySet().iterator();
			out.write("Objects" + '\n');
			while(itr.hasNext())
			{
				int id = itr.next();
				Obj o = objects.get(id);
				out.write(id + " ");
				out.write(o.getShapeId().getInf() + " " + o.getShapeId().getSup() + " ");
				for (int i = 0; i < cst.getDIM(); i++) {
					out.write(o.getCoord(i).getInf() + " " + o.getCoord(i).getSup() + " ");
				}
				//now write the time things
				out.write("1 1 1 1 1 1" + '\n');
			}

			itr = shapes.keySet().iterator();
			out.write("Shapes" + '\n');
			while(itr.hasNext())
			{
				int sid = itr.next();
				out.write(sid + "" + '\n');
			}



			itr = shapes.keySet().iterator();
			out.write("ShiftedBoxes" + '\n');
			while(itr.hasNext())
			{
				int sid = itr.next();
				List<ShiftedBox> sb = shapes.get(sid);

				for(int i = 0; i < sb.size(); i++)
				{
					StringBuilder offset = new StringBuilder();
					StringBuilder size = new StringBuilder();
					for (int j = 0; j < cst.getDIM(); j++)
					{
                        offset.append(sb.get(i).getOffset(j)).append(" ");
                        size.append(sb.get(i).getSize(j)).append(" ");
					}
					out.write(sid + " ");
					out.write(offset.toString() +  size.toString() + '\n');
				}
			}
			out.close();
	    } catch (IOException ignored) {
	    }


		return true;
	}

	/**
	 * Clears the Setup object. So basically it removes all the shapes, objects and constraints from the problem.
	 */
	public void clear()
	{
		shapes.clear();
		objects.clear();
		constraints.clear();
		ictrMinHeap.clear();
		ictrMaxHeap.clear();
	}
//	public static void setIctrHeap(HeapAscending ictrHeap) {
//		Setup.ictrHeap = ictrHeap;
//	}

}
