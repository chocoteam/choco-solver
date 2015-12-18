/**
 *  Copyright (c) 1999-2011, Ecole des Mines de Nantes
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

package org.chocosolver.parser.flatzinc.ast.constraints.global;

import org.chocosolver.parser.flatzinc.ast.Datas;
import org.chocosolver.parser.flatzinc.ast.constraints.IBuilder;
import org.chocosolver.parser.flatzinc.ast.expression.EAnnotation;
import org.chocosolver.parser.flatzinc.ast.expression.Expression;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.nary.geost.Constants;
import org.chocosolver.solver.constraints.nary.geost.GeostOptions;
import org.chocosolver.solver.constraints.nary.geost.PropGeost;
import org.chocosolver.solver.constraints.nary.geost.externalConstraints.ExternalConstraint;
import org.chocosolver.solver.constraints.nary.geost.externalConstraints.NonOverlapping;
import org.chocosolver.solver.constraints.nary.geost.geometricPrim.GeostObject;
import org.chocosolver.solver.constraints.nary.geost.geometricPrim.ShiftedBox;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.tools.ArrayUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 17/07/2015
 */
public class GeostBuilder implements IBuilder {

    @Override
    public void build(Solver solver, String name, List<Expression> exps, List<EAnnotation> annotations, Datas datas) {
        int dim = exps.get(0).intValue();
        int[] rect_size = exps.get(1).toIntArray();
        int[] rect_offset = exps.get(2).toIntArray();
        int[][] shape = exps.get(3).toIntMatrix();
        IntVar[] x = exps.get(4).toIntVarArray(solver);
        IntVar[] kind = exps.get(5).toIntVarArray(solver);


        //Create Objects
        int nbOfObj = x.length / dim;
        int[] objIds = new int[nbOfObj];
        List<GeostObject> objects = new ArrayList<>();
        for (int i = 0; i < nbOfObj; i++) {
            IntVar shapeId = kind[i];
            IntVar[] coords = Arrays.copyOfRange(x, i * dim, (i+1) * dim);
            objects.add(new GeostObject(dim, i, shapeId, coords, solver.ONE(), solver.ONE(), solver.ONE()));
            objIds[i] = i;
        }

        //create shiftedboxes and add them to corresponding shapes
        List<ShiftedBox> shapes = new ArrayList<>();
        for (int i = 0; i < shape.length; i++) {
            for (int j = 0; j < shape[i].length; j++) {
                int h = shape[i][j];
                int[] l = Arrays.copyOfRange(rect_size, (h-1) * dim, h * dim);
                int[] t = Arrays.copyOfRange(rect_offset, (h-1) * dim, h * dim);
                shapes.add(new ShiftedBox(i+1, t, l));
            }
        }

        //Create the external constraints vecotr
        List<ExternalConstraint> extcstr = new ArrayList<>(1);
        //add the external constraint of type non overlapping
        extcstr.add(new NonOverlapping(Constants.NON_OVERLAPPING, ArrayUtils.oneToN(dim), objIds));


        int originOfObjects = objects.size() * dim; //Number of domain variables to represent the origin of all objects
        int otherVariables = objects.size() * 4; //each object has 4 other variables: shapeId, start, duration; end

        //vars will be stored as follows: object 1 coords(so k coordinates), sid, start, duration, end,
        //                                object 2 coords(so k coordinates), sid, start, duration, end and so on ........
        IntVar[] vars = new IntVar[originOfObjects + otherVariables];
        for (int i = 0; i < objects.size(); i++) {
            for (int j = 0; j < dim; j++) {
                vars[(i * (dim + 4)) + j] = objects.get(i).getCoordinates()[j];
            }
            vars[(i * (dim + 4)) + dim] = objects.get(i).getShapeId();
            vars[(i * (dim + 4)) + dim + 1] = objects.get(i).getStart();
            vars[(i * (dim + 4)) + dim + 2] = objects.get(i).getDuration();
            vars[(i * (dim + 4)) + dim + 3] = objects.get(i).getEnd();
        }
        GeostOptions opt = new GeostOptions();
        PropGeost propgeost = new PropGeost(vars, dim, objects, shapes, extcstr, false, opt.included, solver);

        solver.post(new Constraint("Geost", propgeost));
        throw new UnsupportedOperationException("Geost is not robust");
    }
}
