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
import choco.cp.solver.constraints.global.geost.Setup;
import choco.cp.solver.constraints.global.geost.externalConstraints.*;
import choco.cp.solver.constraints.global.geost.frames.DistLinearFrame;
import choco.cp.solver.constraints.global.geost.frames.ForbiddenRegionFrame;
import choco.cp.solver.constraints.global.geost.frames.Frame;
import choco.cp.solver.constraints.global.geost.frames.NonOverlappingFrame;
import choco.cp.solver.constraints.global.geost.geometricPrim.Obj;
import choco.cp.solver.constraints.global.geost.geometricPrim.Region;
import choco.cp.solver.constraints.global.geost.internalConstraints.*;
import choco.kernel.common.logging.ChocoLogging;
import com.sun.tools.javac.util.Pair;
import choco.kernel.model.variables.geost.ShiftedBox;
import choco.kernel.solver.SolverException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;


/**
 * This is the external layer class. It implements the functionality that each external constraint should have. For every external constraint we
 * should be able to create the corresponding FRAME and generate the corresponding internal constraints.
 */
public final class ExternalLayer {

    private static final Logger LOGGER = ChocoLogging.getEngineLogger();

    Constants cst;
    Setup stp;

    /**
     * Creates an ExternalLayer instance for a specific constants class and a specific setup class
     *
     * @param c The constants class
     * @param s The Setup class
     */
    public ExternalLayer(Constants c, Setup s) {
        cst = c;
        stp = s;
    }

    /**
     * @param ectr An externalConstraint object
     * @param oIDs The list of object IDs
     * @return The frame that correspond to the external constraint ectr.
     */
    public Frame InitFrameExternalConstraint(ExternalConstraint ectr, int[] oIDs) {
        Frame result;
        switch (ectr.getEctrID()) {
            case Constants.COMPATIBLE:
                result = initFrameExternalConstraintForCompatible();
                break;
            case Constants.INCLUDED:
                result = initFrameExternalConstraintForIncluded();
                break;
            case Constants.NON_OVERLAPPING:
                result = initFrameExternalConstraintForNonOverlapping(oIDs);
                break;
            case Constants.VISIBLE:
                result = initFrameExternalConstraintForVisible();
                break;
            case Constants.DIST_LEQ:
                result = InitFrameExternalConstraintForDistLeq((DistLeq) ectr, oIDs);
                break;
            case Constants.DIST_GEQ:
                result = initFrameExternalConstraintForDistGeq((DistGeq) ectr);
                break;
            case Constants.DIST_LINEAR:
                result = InitFrameExternalConstraintForDistLinear((DistLinear) ectr, oIDs);
                break;
            case Constants.NON_OVERLAPPING_CIRCLE:
                result = initFrameExternalConstraintForNonOverlappingCircle();
                break;

            default:
                throw new SolverException("A call to InitFrameExternalConstraint with incorrect ectr parameter");
        }
        return result;
    }

    /**
     * @param ectr An externalConstraint object
     * @param o    An object
     * @return A vector containing all the internal constraints that are applied to o caused by ectr
     */
    public List<InternalConstraint> genInternalCtrs(ExternalConstraint ectr, Obj o) {
        List<InternalConstraint> result;
        switch (ectr.getEctrID()) {
            case Constants.COMPATIBLE:
                result = genInternalCtrsForCompatible();
                break;
            case Constants.INCLUDED:
                result = genInternalCtrsForIncluded();
                break;
            case Constants.NON_OVERLAPPING:
                result = genInternalCtrsForNonOverlapping((NonOverlapping) ectr, o);
                break;
            case Constants.VISIBLE:
                result = genInternalCtrsForVisible();
                break;
            case Constants.DIST_LEQ:
                result = genInternalCtrsForDistLeq((DistLeq) ectr);
                break;
            case Constants.DIST_GEQ:
                result = genInternalCtrsForDistGeq((DistGeq) ectr);
                break;
            case Constants.DIST_LINEAR:
                result = genInternalCtrsForDistLinear((DistLinear) ectr);
                break;
            default:
                throw new SolverException("A call to GenInternalCstrs with incorrect ectr parameter");
        }
        return result;
    }

    private Frame initFrameExternalConstraintForCompatible() {
        // Should be changed for Compatible Frame
        return new NonOverlappingFrame();
    }

    private Frame initFrameExternalConstraintForIncluded() {
        // Should be changed for Included Frame

        return new NonOverlappingFrame();
    }

    private Frame initFrameExternalConstraintForNonOverlapping(int[] oIDs) {
        NonOverlappingFrame f = new NonOverlappingFrame();
        for (int i = 0; i < oIDs.length; i++) {
            Obj o = stp.getObject(oIDs[i]);
            int m = o.getShapeId().getDomainSize();

            List<Region> regions = new ArrayList<Region>();

            int[][] set = new int[m][];
            int ivalue = 0;
            for (int sid = o.getShapeId().getInf(); sid <= o.getShapeId().getSup(); sid = o.getShapeId().getNextDomainValue(sid)) {
                int nbOfSbox = stp.getShape(sid).size();
                set[ivalue] = new int[nbOfSbox];
                for (int j = 0; j < nbOfSbox; j++) {
                    set[ivalue][j] = j;
                }
                ivalue++;
            }

            int[] pointer = new int[m];
            boolean print = true;
            while (true) {
                Region r = new Region(cst.getDIM(), o.getObjectId());
                for (int j = 0; j < cst.getDIM(); j++) {
                    int max = stp.getShape(o.getShapeId().getInf()).get(set[0][pointer[0]]).getOffset(j);
                    int min = stp.getShape(o.getShapeId().getInf()).get(set[0][pointer[0]]).getOffset(j) + stp.getShape(o.getShapeId().getInf()).get(set[0][pointer[0]]).getSize(j);
                    int curDomVal = o.getShapeId().getNextDomainValue(o.getShapeId().getInf());
                    for (int s = 1; s < m; s++) {
                        max = Math.max(max, stp.getShape(curDomVal).get(set[s][pointer[s]]).getOffset(j));
                        min = Math.min(min, stp.getShape(curDomVal).get(set[s][pointer[s]]).getOffset(j) + stp.getShape(curDomVal).get(set[s][pointer[s]]).getSize(j));
                        curDomVal = o.getShapeId().getNextDomainValue(curDomVal);
                    }
                    r.setMinimumBoundary(j, o.getCoord(j).getSup() + max + 1);
                    r.setMaximumBoundary(j, o.getCoord(j).getInf() + min - 1);
                }
                regions.add(r);
                for (int j = m - 1; j >= 0; j--) {
                    if (pointer[j] == set[j].length - 1) {
                        if (j == 0) {
                            print = false;
                        }
                        pointer[j] = 0;
                    } else {
                        pointer[j] += 1;
                        break;
                    }
                }
                if (!print) {
                    break;
                }
            }
            f.addForbidRegions(o.getObjectId(), regions);
        }
        return f;
    }


    private Frame InitFrameExternalConstraintForDistLeq(DistLeq ectr, int[] oIDs) {
        /*No ploymorphism for now*/
        int s1 = stp.getObject(ectr.o1).getShapeId().getVal();
        int s2 = stp.getObject(ectr.o2).getShapeId().getVal();
        ForbiddenRegionFrame f = new ForbiddenRegionFrame(ectr.q, ectr.D, s1, s2, ectr.o1, ectr.o2);
        for (int i = 0; i < oIDs.length; i++) {
            Obj o = stp.getObject(oIDs[i]);
            List<Region> regions = new ArrayList<Region>();
            f.addForbidRegions(o.getObjectId(), regions);
        }

        return f;
    }

    private Frame initFrameExternalConstraintForDistGeq(DistGeq ectr) {
        /*No ploymorphism for now*/
        int s1 = stp.getObject(ectr.o1).getShapeId().getVal();
        int s2 = stp.getObject(ectr.o2).getShapeId().getVal();
        //     for (int i = 0; i < oIDs.length; i++)
//     {
//         Obj o = stp.getObject(oIDs[i]);
//         List<Region> regions = new ArrayList<Region>();
//         f.addForbidRegions(o.getObjectId(), regions);
//     }

        return new ForbiddenRegionFrame(ectr.q, ectr.D, s1, s2, ectr.o1, ectr.o2);
    }

    private Frame InitFrameExternalConstraintForDistLinear(DistLinear ectr, int[] oIDs) {
        /*No ploymorphism for now*/
        DistLinearFrame f = new DistLinearFrame(ectr.a, ectr.o1, ectr.b);
        for (int i = 0; i < oIDs.length; i++) {
            Obj o = stp.getObject(oIDs[i]);
            List<Region> regions = new ArrayList<Region>();
            f.addForbidRegions(o.getObjectId(), regions);
        }

        return f;
    }

    private Frame initFrameExternalConstraintForVisible() {
        // Should be changed for Visible Frame

        return new NonOverlappingFrame();
    }

    private Frame initFrameExternalConstraintForNonOverlappingCircle() {
        // Should be changed for Visible Frame

        return new NonOverlappingFrame();
    }


    private List<InternalConstraint> genInternalCtrsForCompatible() {

        return new ArrayList<InternalConstraint>();
    }

    private List<InternalConstraint> genInternalCtrsForIncluded() {

        return new ArrayList<InternalConstraint>();
    }

    public Pair<Outbox, Boolean> mergeAdjacent(Outbox new_ob, Outbox last_ob) {
        //true if merging has occured

        //Check if the last outbox is adjacent on a single dimension with the last outbox

        int dim = new_ob.adjacent(last_ob);
        if ((dim != -1) && (!new_ob.sameSize(last_ob, dim))) dim = -1;
        if (dim != -1) new_ob.merge(last_ob, dim); //merge the two objects
//        if (dim!=-1) LOGGER.info("after merge:"+new_ob);

        return new Pair<Outbox, Boolean>(new_ob, dim != -1);

    }


    private List<InternalConstraint> genInternalCtrsForNonOverlapping(NonOverlapping ectr, Obj o) {

        // Since non_overlapping constraint then we will generate outbox constraints
        List<InternalConstraint> ictrs = new ArrayList<InternalConstraint>();
        List<ShiftedBox> sb = stp.getShape(o.getShapeId().getInf());
        Iterator<Integer> itr;
        itr = ectr.getFrame().getRelForbidRegions().keySet().iterator();
        boolean printit = false;
        while (itr.hasNext()) {
            int i = itr.next();
            if (!(o.getObjectId() == i)) {
                for (int k = 0; k < sb.size(); k++) {
                    // We will generate an outbox constraint corresponding to each relative forbidden region we already generated
                    // for the shifted boxes of the shape corresponding to the Obj o

                    // here we go into the relative forbidden regions
                    loop:
                    for (int l = 0; l < ectr.getFrame().getRelForbidRegions(i).size(); l++) {
                        int[] t = new int[cst.getDIM()];
                        int[] s = new int[cst.getDIM()];
                        for (int j = 0; j < cst.getDIM(); j++) {
                            int min = ectr.getFrame().getRelForbidRegions(i).get(l).getMinimumBoundary(j) - sb.get(k).getOffset(j) - sb.get(k).getSize(j);
                            int max = ectr.getFrame().getRelForbidRegions(i).get(l).getMaximumBoundary(j) - sb.get(k).getOffset(j);

                            s[j] = max - min + 1; // length of the jth coordinate
                            if (s[j] <= 0) // since the length is negative
                                continue loop;
                            t[j] = min; // It is the offset. lower left corner.

                            if (printit) LOGGER.info(o.getObjectId() + " " + j + " " + o);
                            int supDom = o.getCoord(j).getSup();// + sb.get(k).getOffset(j) + sb.get(k).getSize(j);
                            int infDom = o.getCoord(j).getInf();// + sb.get(k).getOffset(j) ;
                            int maxObj = o.getCoord(j).getSup() + sb.get(k).getOffset(j) + sb.get(k).getSize(j) - 1;
                            if (maxObj > o.getCoord(j).getSup()) maxObj = o.getCoord(j).getSup();
                            int minObj = o.getCoord(j).getInf() + sb.get(k).getOffset(j);
                            if (minObj < o.getCoord(j).getInf()) minObj = o.getCoord(j).getInf();

                            if (printit) LOGGER.info("box: " + t[j] + " " + s[j]);
                            if (printit) LOGGER.info("dom: " + minObj + " " + maxObj);


                            if ((supDom < t[j]) || (infDom > t[j] + s[j])) {
                                // this means the intersection of dom(o.x) and the region forbidden region associated with Outbox(t,s) is empty. In the other words all
                                // the placement space is feasible for o.x according to the constraint Outbox(t,s)
                                if (printit) LOGGER.info("skip");
                                continue loop;
                            }
                            if ((maxObj < t[j]) || (minObj > t[j] + s[j])) {
                                // this means the intersection of dom(o.x) and the region forbidden region associated with Outbox(t,s) is empty. In the other words all
                                // the placement space is feasible for o.x according to the constraint Outbox(t,s)
                                if (printit) LOGGER.info("skip2");
                                continue loop;
                            }

                            //clipping
                            if (stp.opt.clipping) {
                                //   t[j] = Math.max(minObj, t[j]);
                                //     s[j] = Math.min(maxObj, t[j] + s[j]) - t[j]  ;
                            }

                            if (printit) LOGGER.info("result box: " + t[j] + " " + s[j]);


                        }

                        Outbox new_ob = new Outbox(t, s);


                        Pair<Outbox, Boolean> result;
                        if (ictrs.size() != 0) {
                            Outbox last_ob = (Outbox) ictrs.get(ictrs.size() - 1);
                            result = mergeAdjacent(new_ob, last_ob);

                            new_ob = result.fst;

                            if (result.snd) ictrs.remove(ictrs.size() - 1);

                        }

                        ictrs.add(new_ob);
                    }
                }
            }
        }
        return ictrs;
    }


    private List<InternalConstraint> genInternalCtrsForVisible() {

        return new ArrayList<InternalConstraint>();
    }

    private List<InternalConstraint> genInternalCtrsForDistGeq(DistGeq ectr) {
        List<InternalConstraint> ictrs = new ArrayList<InternalConstraint>();
        ForbiddenRegionFrame f = ((ForbiddenRegionFrame) ectr.getFrame());
        DistGeqIC ic = new DistGeqIC(stp, f.q, f.D, f.s1, f.s2, f.o1, f.o2, ectr.getDistanceVar());
        ictrs.add(ic);
        return ictrs;
    }

    private List<InternalConstraint> genInternalCtrsForDistLeq(DistLeq ectr) {
        List<InternalConstraint> ictrs = new ArrayList<InternalConstraint>();
        ForbiddenRegionFrame f = ((ForbiddenRegionFrame) ectr.getFrame());
        DistLeqIC ic = new DistLeqIC(stp, f.q, f.D, f.s1, f.s2, f.o1, f.o2, ectr.getDistanceVar());
        ictrs.add(ic);
        return ictrs;
    }

    private List<InternalConstraint> genInternalCtrsForDistLinear(DistLinear ectr) {
        List<InternalConstraint> ictrs = new ArrayList<InternalConstraint>();
        DistLinearFrame f = ((DistLinearFrame) ectr.getFrame());
        DistLinearIC ic = new DistLinearIC(stp, f.a, f.o1, f.b);
        ictrs.add(ic);
		return ictrs;
	}

}