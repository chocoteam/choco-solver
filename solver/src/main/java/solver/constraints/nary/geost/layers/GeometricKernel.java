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

import choco.cp.solver.constraints.global.Geost_Constraint;
import choco.cp.solver.constraints.global.geost.Constants;
import choco.cp.solver.constraints.global.geost.Setup;
import choco.cp.solver.constraints.global.geost.externalConstraints.*;
import choco.cp.solver.constraints.global.geost.frames.ForbiddenRegionFrame;
import choco.cp.solver.constraints.global.geost.geometricPrim.Obj;
import choco.cp.solver.constraints.global.geost.geometricPrim.Point;
import choco.cp.solver.constraints.global.geost.geometricPrim.Region;
import choco.cp.solver.constraints.global.geost.internalConstraints.*;
import choco.kernel.common.logging.ChocoLogging;
import com.sun.tools.javac.util.Pair;
import choco.kernel.memory.IStateInt;
import choco.kernel.model.variables.geost.ShiftedBox;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.Solver;
import choco.kernel.solver.SolverException;
import choco.kernel.solver.variables.integer.IntDomainVar;

import java.util.*;
import java.util.logging.Logger;

import static java.text.MessageFormat.format;

class MemoStore {
    public boolean active;
    public int p = 1; //Maximum nbr of objects stored
    public List<List<Obj>> listObj; // actual list of objects
    Map<int[], Integer> m;// = new HashMap<int[],Integer>;
}

//TODO: check for consistency of the multiplications

/**
 * This is the Geometric kernel class. It implements the functionality of the sweep point algorithm.
 */
@SuppressWarnings({"AccessStaticViaInstance"})
public final class GeometricKernel {

    private static final Logger LOGGER = ChocoLogging.getEngineLogger();

    private static final int ONE_MILLION = 1000000;

    private final Constants cst;
    private final Setup stp;
    private final ExternalLayer externalLayer;
    private final IntermediateLayer intermediateLayer;
    private final MemoStore memo;
    private final Map<Pair<Integer, Integer>, Boolean> included;
    private int get_fr_ptr_a = 0;
    private int get_fr_ptr_b = 0;
    private IntDomainVar[] E = null;
    private IntDomainVar[] D = null;
    private GeostNumeric engine = null;
    private final Solver solver;
    private final Geost_Constraint constraint;


    /**
     * Creates an ExternalLayer instance for a specific Constants class, a specific Setup class, a specific ExternalLayer class and a specific
     * IntermediateLayer class.
     *
     * @param c           The constants class
     * @param s           The Setup class
     * @param extrL
     * @param aSolver
     * @param aConstraint
     */
    public GeometricKernel(Constants c, Setup s, ExternalLayer extrL, IntermediateLayer intermL, boolean memo_, Map<Pair<Integer, Integer>, Boolean> included_, Solver aSolver, final Geost_Constraint aConstraint) {
        cst = c;
        stp = s;
        externalLayer = extrL;
        intermediateLayer = intermL;
        memo = new MemoStore();
        memo.p = 1;
        memo.active = memo_;
        memo.listObj = new ArrayList<List<Obj>>(0);
        memo.m = new HashMap<int[], Integer>(16);
        included = included_;
        this.solver = aSolver;
        this.constraint = aConstraint;
        LOGGER.info("memo_active=" + memo_);
    }

    /**
     * It gets the forbidden region. Basically this answers the following question: Is point c infeasible according to any active internal
     * constraint? if yes, it also specifies the forbidden region.
     *
     * @param d        Indicates which coordinate dimension we want to prune
     * @param k        The total number of dimensions (The dimension of the space we  are working in)
     * @param o        The object in question                                      1
     * @param c        The current point in question (basically he sweep point)
     * @param ACTRS    A vector of all active internal constraints
     * @param increase A boolean specifying if we are pruning the min (true) or the max (false)
     * @return A vector of 2 elements. The first is a Boolean object indicating  the fact of whether there is a forbidden region or not and the
     *         second is a Region object indicating the forbidden region if it exists.
     */
    List getFR(int d, int k, Obj o, Point c, Point jump, List<InternalConstraint> ACTRS, boolean increase) {
        stp.opt.GetFRCalled++;

        List<Object> result = new ArrayList<Object>(2);
        List v;
        if (increase) {
            for (int rr = get_fr_ptr_a; rr < ACTRS.size() + get_fr_ptr_a; rr++) {
                int i = rr % ACTRS.size();
                long tmpTime = System.nanoTime() / ONE_MILLION;
                //LOGGER.info(ACTRS.get(i)   );
                v = intermediateLayer.isFeasible(ACTRS.get(i), true, d, k, o, c, jump);
                stp.opt.timeIsFeasible += (System.nanoTime() / ONE_MILLION) - tmpTime;

                if (!((Boolean) v.get(0))) {
                    get_fr_ptr_a = i;
                    result.clear();
                    result.add(0, true);
                    result.add(1, v.get(1));
                    return result;
                }
            }
            get_fr_ptr_a = 0;
            result.clear();
            result.add(0, false);
            result.add(1, new Region(cst.getDIM(), -1));
            return result;
        } else {
            for (int rr = get_fr_ptr_b; rr < ACTRS.size() + get_fr_ptr_b; rr++) {
                int i = rr % ACTRS.size();
                long tmpTime = System.nanoTime() / ONE_MILLION;

                v = intermediateLayer.isFeasible(ACTRS.get(i), false, d, k, o, c, jump);
                stp.opt.timeIsFeasible += (System.nanoTime() / ONE_MILLION) - tmpTime;

                if (!((Boolean) v.get(0))) {
                    get_fr_ptr_b = i;
                    result.clear();
                    result.add(0, true);
                    result.add(1, v.get(1));
                    return result;
                }
            }
            get_fr_ptr_b = 0;
            result.clear();
            result.add(0, false);
            result.add(1, new Region(cst.getDIM(), -1));
            return result;
        }
    }

    /**
     * This is the main filtering algorithm associated with the Geost_Constraint.
     *
     * @param k     The total number of dimensions (The dimension of the space we are working in)
     * @param oIDs  The list of object IDs
     * @param ectrs The list of external constraints
     * @return It return false if we couldn't prune anything, this means that we sweeped the whole space and couldn't find a placement. This cause
     *         a failure of the Geost_Constraint. Otherwise it returns true.
     */
    @SuppressWarnings({"PrimitiveArrayArgumentToVariableArgMethod"})
    public boolean filterCtrs(int k, int[] oIDs, List<ExternalConstraint> ectrs) throws ContradictionException {


        stp.opt.propag_failed = true;

        if (stp.opt.serial != null) {
            try {
//                List<Obj> tmp = new ArrayList<Obj>(oIDs.length);
//                for (int i = 0; i < oIDs.length; i++) {
//                    tmp.add(stp.getObject(i));
//                }
                stp.opt.serial.writeObject(Arrays.asList(oIDs));
            } catch (Exception e) {
                throw new SolverException("Prune:unable to serialize parameters");
            }
        }

        if (stp.opt.debug) {
            LOGGER.info("FilterCtrs:");
        }
        boolean nonFix = true;
        //?????
        //Ensure that all internal constraint containing a variable
        //update those variables based on the new domain of the objects.
        for (ExternalConstraint ectr : ectrs) {
            if (ectr instanceof DistLeq) {
                DistLeq dl = (DistLeq) ectr;
                ForbiddenRegionFrame f = (ForbiddenRegionFrame) externalLayer.InitFrameExternalConstraint(ectr, oIDs);
                DistLeqIC ic = new DistLeqIC(stp, f.q, f.D, f.s1, f.s2, f.o1, f.o2, dl.getDistanceVar());
                nonFix &= ic.updateDistance(k);
            }

            if (ectr instanceof DistGeq) {
                DistGeq dg = (DistGeq) ectr;
                ForbiddenRegionFrame f = (ForbiddenRegionFrame) externalLayer.InitFrameExternalConstraint(ectr, oIDs);
                DistGeqIC ic = new DistGeqIC(stp, f.q, f.D, f.s1, f.s2, f.o1, f.o2, dg.getDistanceVar());
                nonFix &= ic.updateDistance(k);
            }


        }

        while (nonFix) {

            nonFix = false;  //Suppose there will be no updates

            //?????
            //Ensure that all internal constraint containing a variable
            //update those variables based on the new domain of the objects.
            for (ExternalConstraint ectr : ectrs) {
                if (ectr instanceof DistLeq) {
                    DistLeq dl = (DistLeq) ectr;
                    ForbiddenRegionFrame f = (ForbiddenRegionFrame) externalLayer.InitFrameExternalConstraint(ectr, oIDs);
                    DistLeqIC ic = new DistLeqIC(stp, f.q, f.D, f.s1, f.s2, f.o1, f.o2, dl.getDistanceVar());
                    nonFix = nonFix || ic.updateDistance(k);
                }

                if (ectr instanceof DistGeq) {
                    DistGeq dg = (DistGeq) ectr;
                    ForbiddenRegionFrame f = (ForbiddenRegionFrame) externalLayer.InitFrameExternalConstraint(ectr, oIDs);
                    DistGeqIC ic = new DistGeqIC(stp, f.q, f.D, f.s1, f.s2, f.o1, f.o2, dg.getDistanceVar());
                    nonFix = nonFix || ic.updateDistance(k);
                }


            }

            //nonFix=nonFix || propagDistConstraints();

            for (int i = 0; i < ectrs.size(); i++) {
                ectrs.get(i).setFrame(externalLayer.InitFrameExternalConstraint(ectrs.get(i), oIDs));
            }
            for (int i = 0; i < oIDs.length; i++) {
                Obj o = stp.getObject(oIDs[i]);
                if (stp.opt.debug) {
                    LOGGER.info(String.format("Considering object %d %s --> ", oIDs[i], o));
                }
                int domainsSize = o.calculateDomainSize();
                if (!filterObjWP(k, oIDs[i])) {
                    if (stp.opt.debug) {
                        LOGGER.info("Returning false;");
                    }
                    return false;
                } else {
                    // need to check if Object attributes has been pruned
                    if (domainsSize != o.calculateDomainSize()) {
                        //update the relative forbidden regions attached to object o
                        for (int j = 0; j < o.getRelatedExternalConstraints().size(); j++) {
                            if (!((o.getRelatedExternalConstraints().get(j) instanceof DistLeq)
                                    || (o.getRelatedExternalConstraints().get(j) instanceof DistGeq)
                                    || (o.getRelatedExternalConstraints().get(j) instanceof DistLinear))) {
                                o.getRelatedExternalConstraints().get(j).getFrame().getRelForbidRegions().remove(o.getObjectId());
                                int[] oIDi = {oIDs[i]};
                                o.getRelatedExternalConstraints().get(j).getFrame().getRelForbidRegions().put(o.getObjectId(), externalLayer.InitFrameExternalConstraint(
                                        o.getRelatedExternalConstraints().get(j), oIDi).getRelForbidRegions(oIDs[i]));
                            }
                        }
                        //has to saturate once again
                        nonFix = true;
                    }
                    if (stp.opt.debug) {
                        LOGGER.info(String.format("***Result of FilterCstrs:%s", o));
                    }
                }

            }

            //?????
            //Ensure that all internal constraint containing a variable
            //update those variables based on the new domain of the objects.
            for (ExternalConstraint ectr : ectrs) {
                if (ectr instanceof DistLeq) {
                    DistLeq dl = (DistLeq) ectr;
                    ForbiddenRegionFrame f = (ForbiddenRegionFrame) externalLayer.InitFrameExternalConstraint(ectr, oIDs);
                    DistLeqIC ic = new DistLeqIC(stp, f.q, f.D, f.s1, f.s2, f.o1, f.o2, dl.getDistanceVar());
                    nonFix = nonFix || ic.updateDistance(k);
                }

                if (ectr instanceof DistGeq) {
                    DistGeq dg = (DistGeq) ectr;
                    ForbiddenRegionFrame f = (ForbiddenRegionFrame) externalLayer.InitFrameExternalConstraint(ectr, oIDs);
                    DistGeqIC ic = new DistGeqIC(stp, f.q, f.D, f.s1, f.s2, f.o1, f.o2, dg.getDistanceVar());
                    nonFix = nonFix || ic.updateDistance(k);
                }


            }

        }

        stp.opt.propag_failed = false;

        if (stp.opt.try_propagation) {
            stp.propagationEngine.raiseContradiction(null);
        }

        return true;
    }

    /**
     * Filters all the k coordinates and the shape of a given object o according to all external geometrical constraints where o occurs.
     *
     * @param k   The total number of dimensions (The dimension of the space we are working in)
     * @param oid The object id
     * @return It return false if we couldn't prune anything, this means that we  sweeped the whole space and couldn't find a placement. This cause
     *         a failure of the Geost_Constraint. Otherwise it returns true.
     */

    //WP in "FilterObjWP" means With Polymorphic. In fact there also FilterObj (see bellow) for filtering the coordinate of an object with fixed shape.
    boolean filterObjWP(int k, int oid) throws ContradictionException
    // In the technical report  we pass the Frame also however there is no need here since
    // the Frame is part of the external constraint
    {
        Obj o = stp.getObject(oid);
        if (o.getShapeId().isInstantiated()) {
            return filterObj(k, oid);
        } else {
            int[] minG = new int[k];
            int[] maxG = new int[k];
            for (int d = 0; d < k; d++) {
                // initialize generalization
                minG[d] = o.getCoord(d).getSup() + 1;
                maxG[d] = o.getCoord(d).getInf() - 1;
            }

            //
            for (int sid = o.getShapeId().getInf(); sid <= o.getShapeId().getSup(); sid = o.getShapeId().getNextDomainValue(sid)) {

                int[] max = new int[k];
                int[] min = new int[k];
                boolean b = false;

                // We call FilterObj with the fixed shape sid. To avoid the creation of another object we use worldPush and worldPop. Actually, by doing so, the object o
                //is modified between worldPush() and worldPop() (where we collect the information we interested to : b, max, min) and restored into its state after worldPop
                solver.worldPushDuringPropagation();
                o.getShapeId().instantiate(sid, this.constraint, true);


                b = filterObj(k, oid);

                if (b) {
                    for (int d = 0; d < k; d++) {
                        max[d] = o.getCoord(d).getSup();
                        min[d] = o.getCoord(d).getInf();
                    }
                }
                solver.worldPopDuringPropagation();

                if (!b) {
                    o.getShapeId().removeVal(sid, this.constraint, true);
                } else {
                    //Take the union of the pruning, that is consider the greatest forbidden region.
                    for (int d = 0; d < k; d++) {
                        minG[d] = Math.min(min[d], minG[d]);
                        maxG[d] = Math.max(max[d], maxG[d]);
                    }
                }
            }
            for (int d = 0; d < k; d++) {
                o.getCoord(d).updateInf(minG[d], this.constraint, true);
                o.getCoord(d).updateSup(maxG[d], this.constraint, true);
            }
            return true;
        }
    }

    /**
     * Filters all the k coordinates of a given object o with fixed shape according to all external geometrical constraints where o occurs.
     *
     * @param k   The total number of dimensions (The dimension of the space we  are working in)
     * @param oid The object id
     * @return It return false if we couldn't prune anything, this means that we  sweeped the whole space and couldn't find a placement. This cause
     *         a failure of the Geost_Constraint. Otherwise it returns true.
     */
    boolean filterObj(int k, int oid) throws ContradictionException
    // In the technical report we pass the Frame also however there is no need here since the Frame is part of the external constraint
    {
        if (stp.opt.debug) {
            LOGGER.info("GeometricKernel:FilterObj()");
        }
        Obj o = stp.getObject(oid);

        o.getRelatedInternalConstraints().clear();

        for (int i = 0; i < o.getRelatedExternalConstraints().size(); i++) {
            List<InternalConstraint> v = externalLayer.genInternalCtrs(o.getRelatedExternalConstraints().get(i), o);
            for (int j = 0; j < v.size(); j++) {
                o.addRelatedInternalConstraint(v.get(j));
            }
        }

        if ((stp.opt.processing)) {
            LOGGER.info("\n/*Processing*/endchunk();\n/*Processing*/break;" + "case " + (stp.opt.phase++) + ":\n/*Processing*/beginchunk();");
            LOGGER.info(String.format("\n/*Processing*/domain(%d,%d,%d,%d,%d);", o.getObjectId(), o.getCoord(0).getInf(), o.getCoord(0).getSup(), o.getCoord(1).getInf(), o.getCoord(1).getSup()));

            //Draw objects that are instantiated
            for (Integer i : stp.getObjectKeySet()) {
                Obj tmp = stp.getObject(i);
                if (tmp.coordInstantiated()) {
                    if (tmp.isSphere()) {
                        LOGGER.info(String.format("\n/*Processing*/sphere_object(%d,%d,%d,%d);", tmp.getCoord(0).getSup(), tmp.getCoord(1).getSup(), tmp.getRadius(), tmp.getObjectId()));
                    }
                }
            }

        }

        if (stp.opt.useNumericEngine) {
            if (engine == null) {
                LOGGER.info("engine==null");
                engine = new GeostNumeric(stp, 100);
            }
            engine.prune(o, k, o.getRelatedInternalConstraints()); //throws a contradiction exception in case of failure
        }

        for (int d = 0; d < k; d++) {

            if (!o.getRelatedInternalConstraints().isEmpty()) {
                if (stp.opt.boxModeOnly) {
                    if ((!pruneMin(o, d, k, o.getRelatedInternalConstraints())) || (!pruneMax(o, d, k, o.getRelatedInternalConstraints()))) {
                        return false; //means that a placement was not found
                    }
                } else if (stp.opt.propModeOnly) {
                    if ((!newPruneMin(o, d, k, o.getRelatedInternalConstraints())) || (!newPruneMax(o, d, k, o.getRelatedInternalConstraints()))) {
                        return false;
                    }
                } else if (stp.opt.deltaModeOnly) {
                    if ((!newDeltaPruneMin(o, d, k, o.getRelatedInternalConstraints())) || (!newDeltaPruneMax(o, d, k, o.getRelatedInternalConstraints()))) {
                        return false;
                    }
                }

            }
        }

        return true;
    }


    /**
     * Adjusts the lower bound of the d^th coordinate of the origin of the  object o according to the set of internal constraints associated with  object o.
     *
     * @param o     The object.
     * @param d     The dimension we want to prune.
     * @param k     The total number of dimensions (The dimension of the space we  are working in)
     * @param ictrs The internal constraints associated with o.
     * @return It return false if we couldn't prune the min of Object o, this means that we sweeped the whole space and couldn't prune the min of o.
     */
    boolean pruneMin(Obj o, int d, int k, List<InternalConstraint> ictrs) throws ContradictionException {
        //LOGGER.info("Prune in");
        if (stp.opt.serial != null) {
            try {
                stp.opt.serial.writeObject(o);
                stp.opt.serial.writeObject(d);
                stp.opt.serial.writeObject(k);
                stp.opt.serial.writeObject(ictrs);
            } catch (Exception e) {
                throw new SolverException("Prune:unable to serialize parameters param in");
            }
        }

        boolean b = true;
        Point c = new Point(k);
        Point n = new Point(k);

        for (int i = 0; i < o.getCoordinates().length; i++) {
            c.setCoord(i, o.getCoord(i).getInf()); // Initial position of point
            n.setCoord(i, o.getCoord(i).getSup() + 1); // Upper limits + 1 in the different dimensions
        }
//        LOGGER.info("A");
        List forbidRegion = getFR(d, k, o, c, n, ictrs, true);
        //LOGGER.info("GetFR(d:"+d+",k:"+k+",o:"+o+",c:"+c+",n:"+c+",ACTRS:,true -> "+forbidRegion);

        boolean infeasible = (Boolean) forbidRegion.get(0);
        Region f = (Region) forbidRegion.get(1);

        if (stp.opt.serial != null) {
            try {
                stp.opt.serial.writeObject(c);
                stp.opt.serial.writeObject(n);
                stp.opt.serial.writeObject(b);
                stp.opt.serial.writeObject(infeasible);
                stp.opt.serial.writeObject(f);
            } catch (Exception e) {
                throw new SolverException("Prune:unable to serialize parameters first iter");
            }
        }


        while (b && infeasible) {

            for (int i = 0; i < k; i++) {
                // update n according to f
                n.setCoord(i, Math.min(n.getCoord(i), f.getMaximumBoundary(i) + 1));
            }

//            Point initial_c = new Point(c);     //create a copy
//        LOGGER.info("C");
            List adjUp = adjustUp(c, n, o, d, k); // update the position of c to check
            //LOGGER.info("c:"+c+",n:"+n+",o:"+o+",d:"+d+",k:"+k);
            c = (Point) adjUp.get(0);
            n = (Point) adjUp.get(1);
            b = (Boolean) adjUp.get(2);

//            if (stp.opt.delta.get(ddeltadelt)==null) stp.opt.delta.put(d,new HashMap<Integer,Integer>());
//            HashMap<Integer,Integer> curDelta= stp.opt.delta.get(d);
//            int delta=Math.abs(c.getCoord(d)-initial_c.getCoord(d));
//            if (curDelta.get(delta)==null) curDelta.put(delta,0);
//            curDelta.put(delta,curDelta.get(delta)+1);

            //LOGGER.info("E:"+c);
            forbidRegion = getFR(d, k, o, c, n, ictrs, true);
            //LOGGER.info("GetFR(d:"+d+",k:"+k+",o:"+o+",c:"+c+",n:"+c+",ACTRS:,true -> "+forbidRegion);
            //LOGGER.info("F");
            infeasible = (Boolean) forbidRegion.get(0);
            f = (Region) forbidRegion.get(1);

            if (stp.opt.serial != null) {
                try {
                    stp.opt.serial.writeObject(c);
                    stp.opt.serial.writeObject(n);
                    stp.opt.serial.writeObject(b);
                    stp.opt.serial.writeObject(infeasible);
                    stp.opt.serial.writeObject(f);
                } catch (Exception e) {
                    throw new SolverException("Prune:unable to serialize parameters second iter");
                }
            }

        }

        if (b) {
//            LOGGER.info(initial_c+" -> "+c);
//
//            stp.opt.sum_jumps+=Math.abs(c.getCoord(d)-initial_c.getCoord(d));
//
//            stp.opt.GetFRCalls++;
//
            o.getCoord(d).updateInf(c.getCoord(d), this.constraint, true);
//            cst.nbOfUpdates++;
        }


        if (stp.opt.serial != null) {
            try {
                stp.opt.serial.writeObject(o);
                stp.opt.serial.writeObject(b);
            } catch (Exception e) {
                throw new SolverException("Prune:unable to serialize parameters param out");
            }
        }
        //LOGGER.info("Prune out");
        return b;
    }

    /**
     * Moves up to the next feasible point, this function is used by the PruneMin function.
     */


    static List adjustUp(Point c, Point n, Obj o, int d, int k) {
        //LOGGER.info("Adjust Up("+c+","+ n+",?,"+d+","+k  +")");
        List<Object> result = new ArrayList<Object>(3);
        int jPrime = 0;
        int j = k - 1;
        while (j >= 0) {
            jPrime = (j + d) % k;
            c.setCoord(jPrime, n.getCoord(jPrime));
            n.setCoord(jPrime, o.getCoord(jPrime).getSup() + 1);
            if (c.getCoord(jPrime) <= o.getCoord(jPrime).getSup()) {
                result.clear();
                result.add(0, c);
                result.add(1, n);
                result.add(2, true);
                return result;
            } else {
                c.setCoord(jPrime, o.getCoord(jPrime).getInf());
            }
            j--;
        }
        result.clear();
        result.add(0, c);
        result.add(1, n);
        result.add(2, false);
        return result;
    }

    /**
     * Adjusts the upper bound of the d^th coordinate of the origin of the  object o according to the set of internal constraints associated with object o.
     *
     * @param o     The object.
     * @param d     The dimension we want to prune.
     * @param k     The total number of dimensions (The dimension of the space we  are working in)
     * @param ictrs The internal constraints associated with o.
     * @return It return false if we couldn't prune the max of Object o, this means that we sweeped the whole space and couldn't prune the max  of o.
     */
    boolean pruneMax(Obj o, int d, int k, List<InternalConstraint> ictrs) throws ContradictionException {
        boolean b = true;
        Point c = new Point(k);
        Point n = new Point(k);


        for (int i = 0; i < o.getCoordinates().length; i++) {
            c.setCoord(i, o.getCoord(i).getSup()); // Initial position of point
            n.setCoord(i, o.getCoord(i).getInf() - 1); // Lower limits - 1 in the different dimensions
        }


        List forbidRegion = getFR(d, k, o, c, n, ictrs, false);
        boolean infeasible = (Boolean) forbidRegion.get(0);
        Region f = (Region) forbidRegion.get(1);
        while (b && infeasible) {
            for (int i = 0; i < k; i++) {
                // update n according to f
                n.setCoord(i, Math.max(n.getCoord(i), f.getMinimumBoundary(i) - 1));
            }
//            Point initial_c = new Point(c);     //create a copy

            List adjDown = adjustDown(c, n, o, d, k);// update the position of c to check
            c = (Point) adjDown.get(0);
            n = (Point) adjDown.get(1);
            b = (Boolean) adjDown.get(2);

//            if (stp.opt.delta.get(d)==null) stp.opt.delta.put(d,new HashMap<Integer,Integer>());
//            HashMap<Integer,Integer> curDelta= stp.opt.delta.get(d);
//            int delta=Math.abs(c.getCoord(d)-initial_c.getCoord(d));
//            if (curDelta.get(delta)==null) curDelta.put(delta,0);
//            curDelta.put(delta,curDelta.get(delta)+1);


            forbidRegion = getFR(d, k, o, c, n, ictrs, false);
            infeasible = (Boolean) forbidRegion.get(0);
            f = (Region) forbidRegion.get(1);
        }

        if (b) {
            o.getCoord(d).updateSup(c.getCoord(d), this.constraint, true);
            cst.nbOfUpdates++;
        }

        return b;
    }

    /**
     * Moves down to the next feasible point, this function is used by the PruneMax function.
     */
    static List adjustDown(Point c, Point n, Obj o, int d, int k) {
        List<Object> result = new ArrayList<Object>(3);
        int jPrime = 0;
        int j = k - 1;
        while (j >= 0) {
            jPrime = (j + d) % k;
            c.setCoord(jPrime, n.getCoord(jPrime));
            n.setCoord(jPrime, o.getCoord(jPrime).getInf() - 1);
            if (c.getCoord(jPrime) >= o.getCoord(jPrime).getInf()) {
                result.clear();
                result.add(0, c);
                result.add(1, n);
                result.add(2, true);
                return result;
            } else {
                c.setCoord(jPrime, o.getCoord(jPrime).getSup());
            }
            j--;
        }
        result.clear();
        result.add(0, c);
        result.add(1, n);
        result.add(2, false);
        return result;
    }

    /**
     * Tries to fix all the objects within one single propagation.
     *
     * @param k      The total number of dimensions (The dimension of the space we are working in)
     * @param oIDs   The list of object IDs
     * @param ectrs  The list of external constraints
     * @param ctrlVs The list of controlling vectors
     * @param idxLastFreeObject
     * @return It return true if we can fix all the objects. Otherwise it returns false.
     */
    public boolean fixAllObjs(int k, int[] oIDs, List<ExternalConstraint> ectrs, List<int[]> ctrlVs, IStateInt idxLastFreeObject) throws ContradictionException {
        //LOGGER.info("FixallObjs");
        for (int i = 0; i < ectrs.size(); i++) {
            ectrs.get(i).setFrame(externalLayer.InitFrameExternalConstraint(ectrs.get(i), oIDs));

        }
        int nbOfCtrlV = ctrlVs.size();
        int lastIdx = idxLastFreeObject.get();
        for (int i = 0; i < lastIdx; i++) {
            Obj o = stp.getObject(oIDs[i]);

            int m = i % nbOfCtrlV;

            //long tmpTimeFixObj = System.nanoTime() / ONE_MILLION;
            boolean b = fixObj(k, oIDs[i], ctrlVs.get(m));

            //stp.opt.timeFixObj += ((System.nanoTime() / ONE_MILLION) - tmpTimeFixObj);

            if (!b) {
                return false;
            } else {
                for (int j = 0; j < o.getRelatedExternalConstraints().size(); j++) {
                    if (!((o.getRelatedExternalConstraints().get(j) instanceof DistLeq)
                            || (o.getRelatedExternalConstraints().get(j) instanceof DistGeq)
                            || (o.getRelatedExternalConstraints().get(j) instanceof DistLinear))) {
                        int[] oIDi = {oIDs[i]};
                        o.getRelatedExternalConstraints().get(j).getFrame().getRelForbidRegions().remove(o.getObjectId());
                        o.getRelatedExternalConstraints().get(j).getFrame().getRelForbidRegions().put(o.getObjectId(), externalLayer.InitFrameExternalConstraint(o
                                .getRelatedExternalConstraints().get(j), oIDi).getRelForbidRegions(oIDs[i]));
                    }
                }
                // swap ids between new fixed object and last free object
                int tmp = oIDs[i];
                oIDs[i--] = oIDs[--lastIdx];
                oIDs[lastIdx] = tmp;
                idxLastFreeObject.add(-1);
            }
        }
        return true;
    }


    void relForbReg(Obj o)
    //Update the relative forbidden region of the object oID.
    {
        int oID = o.getObjectId();
        int[] oIDi = {oID};

        for (int j = 0; j < o.getRelatedExternalConstraints().size(); j++) {
            o.getRelatedExternalConstraints().get(j).getFrame().getRelForbidRegions().remove(o.getObjectId());
            o.getRelatedExternalConstraints().get(j).getFrame().getRelForbidRegions().put(o.getObjectId(),
                    externalLayer.InitFrameExternalConstraint(o.getRelatedExternalConstraints().get(j), oIDi).getRelForbidRegions(oID));
        }

    }

    List<InternalConstraint> absForbReg(Obj o)
    //returns the internal constraints of oID with itself
    {
        int oID = o.getObjectId();
        //o.getRelatedInternalConstraints().clear();
        //We will pretend there are two objects
        int[] oIDs = new int[2];
        oIDs[0] = oID;
        oIDs[1] = oID + 1;

        List<InternalConstraint> added = new ArrayList<InternalConstraint>(16);

        for (int ic = 0; ic < o.getRelatedExternalConstraints().size(); ic++) {
            ExternalConstraint ectr = o.getRelatedExternalConstraints().get(ic);
            NonOverlapping ectr_copy = new NonOverlapping(Constants.NON_OVERLAPPING, ectr.getDim(), oIDs);
            ectr_copy.getFrame().addForbidRegions(oID + 1, ectr.getFrame().getRelForbidRegions(oID));
            //Generate internal constraint for object o wrt to fake object oID+1 that contents rel forb reg of oID (that is itself)
            List<InternalConstraint> v = externalLayer.genInternalCtrs(ectr_copy, o);
            //merge here outboxes
            for (int j = 0; j < v.size(); j++) {
                added.add(v.get(j));
            }
        }

        return added;
    }

    private static boolean same_domain(int[][] old_domain, Obj o) {
        for (int i = 0; i < old_domain.length; i++) {
            if (old_domain[i][0] != o.getCoord(i).getDomainSize()) {
                return false;
            }
            if (old_domain[i][1] != o.getCoord(i).getInf()) {
                return false;
            }
            if (old_domain[i][2] != o.getCoord(i).getSup()) {
                return false;
            }
        }
        return true;
    }


    public boolean fixAllObjs_incr(int k, int[] oIDs, List<ExternalConstraint> ectrs, List<int[]> ctrlVs, IStateInt idxLastFreeObject) throws ContradictionException {

        Integer sid_prime = null;
        int[][] domain_prime = null;
        List<InternalConstraint> ICTRS = Collections.emptyList();// = new ArrayList<InternalConstraint>(16);

        for (int i = 0; i < ectrs.size(); i++) {
            ectrs.get(i).setFrame(externalLayer.InitFrameExternalConstraint(ectrs.get(i), oIDs));
        }

        int nbOfCtrlV = ctrlVs.size();
        int lastIdx = idxLastFreeObject.get();
        for (int i = 0; i < lastIdx; i++) {
            Obj o = stp.getObject(oIDs[i]);
            //LOGGER.info("Object "+oIDs[i]+" "+o);
            int m = i % nbOfCtrlV;

            int[] ctrlV = ctrlVs.get(m);

            if (ctrlV[0] < 0) {
                o.getShapeId().instantiate(o.getShapeId().getInf(), this.constraint, true);
            } else {
                o.getShapeId().instantiate(o.getShapeId().getSup(), this.constraint, true);
            }

            boolean has_same_sid = ((sid_prime != null) && (o.getShapeId().getVal() == sid_prime));
            boolean has_same_domain = ((domain_prime != null) && (same_domain(domain_prime, o)));

            if ((!has_same_sid) || (!has_same_domain)) {

                o.getRelatedInternalConstraints().clear();

                for (int ic = 0; ic < o.getRelatedExternalConstraints().size(); ic++) {
                    List<InternalConstraint> v = externalLayer.genInternalCtrs(o.getRelatedExternalConstraints().get(ic), o);
                    for (int j = 0; j < v.size(); j++) {
                        o.addRelatedInternalConstraint(v.get(j));
                    }
                }

                sid_prime = o.getShapeId().getVal();
                if (domain_prime == null) {
                    domain_prime = new int[o.getCoordinates().length][3];
                }
                for (int l = 0; l < o.getCoordinates().length; l++) {
                    domain_prime[l][0] = o.getCoord(l).getDomainSize();
                    domain_prime[l][1] = o.getCoord(l).getInf();
                    domain_prime[l][2] = o.getCoord(l).getSup();
                }

                ICTRS = o.getRelatedInternalConstraints();
            }

//            LOGGER.info(ICTRS_.size()+" "+ICTRS.size());
//            for (InternalConstraint ic : ICTRS_) LOGGER.info(ic.toString());
//            LOGGER.info("--");
//            for (InternalConstraint ic : ICTRS) LOGGER.info(ic.toString());

            long tmpTimePruneFix = System.nanoTime() / ONE_MILLION;
            boolean b = pruneFix(o, k, ctrlV, ICTRS);//do not use o internal constraints
            stp.opt.timePruneFix += ((System.nanoTime() / ONE_MILLION) - tmpTimePruneFix);
            if (!b) {
                return false;
            }

            //Update Relative Forbidden Region incrementally
            relForbReg(o);

            //Add the internal constraint of o with itself
//            LOGGER.info("--");
//            List<InternalConstraint> tmp = AbsForbReg(o);
//            for (InternalConstraint ic : tmp) LOGGER.info(ic.toString());

            List<InternalConstraint> incr_ICTRS = absForbReg(o);
            //Note that the merging has been done in AbsForReg

            if ((!ICTRS.isEmpty()) && (!incr_ICTRS.isEmpty())) {
                try {
                    Outbox old_ob = (Outbox) ICTRS.get(ICTRS.size() - 1);
                    Outbox new_ob = (Outbox) incr_ICTRS.get(0);
                    Pair<Outbox, Boolean> result;
                    result = externalLayer.mergeAdjacent(new_ob, old_ob);
                    if (result.snd) {
                        new_ob = result.fst;
                        incr_ICTRS.remove(0);
                        incr_ICTRS.add(0, new_ob);
                        ICTRS.remove(ICTRS.size() - 1);
                    }
                } catch (ClassCastException e) {/*Reached only if internal constraint is not an outbox*/}
            }
            ICTRS.addAll(incr_ICTRS);
            // swap ids between new fixed object and last free object
            int tmp = oIDs[i];
            oIDs[i--] = oIDs[--lastIdx];
            oIDs[lastIdx] = tmp;
            idxLastFreeObject.add(-1);

        }
        return true;
    }


    /**
     * Tries to fix the shape and the k coordinates of a given object o according to all external geometrical constraints where o occurs and according the
     * corresponding controlling vector v.
     *
     * @param k     The total number of dimensions (The dimension of the space we are working in)
     * @param oid   The object id
     * @param ctrlV The control vector
     * @return It return true if we can fin a feasible point for the object o. Otherwise it returns false.
     */

    boolean fixObj(int k, int oid, int[] ctrlV)
            throws ContradictionException
    // In the technical report we pass the Frame also however there is no need here since the Frame is part of the external constraint
    {
        Obj o = stp.getObject(oid);
        if (ctrlV[0] < 0) {
            o.getShapeId().instantiate(o.getShapeId().getInf(), this.constraint, true);
        } else {
            o.getShapeId().instantiate(o.getShapeId().getSup(), this.constraint, true);
        }

        o.getRelatedInternalConstraints().clear();
// Holes are not addded for now!
//		for (int d = 0; d < k; d++) {
//			// Add Possible outbox constraints corresponding to holes of o.coords[d]
//		}

        for (int i = 0; i < o.getRelatedExternalConstraints().size(); i++) {
            List<InternalConstraint> v = externalLayer.genInternalCtrs(o.getRelatedExternalConstraints().get(i), o);
            for (int j = 0; j < v.size(); j++) {
                o.addRelatedInternalConstraint(v.get(j));
            }
        }
        //LOGGER.info("before");
        //o.print();
        //long tmpTimePruneFix = System.nanoTime() / ONE_MILLION;
        //timePruneFix += ((System.nanoTime() / ONE_MILLION) - tmpTimePruneFix);
        return pruneFix(o, k, ctrlV, o.getRelatedInternalConstraints());
    }

    /**
     * Check if object o_1 dominates object o_2
     */

    boolean dominates(int k, int[] ctrlV, Point c, Obj o1, Obj o2) {

        Point old_c = new Point(k);
        for (int dim = 0; dim < k; dim++) {
            old_c.setCoord(dim, o1.getCoord(dim).getVal());
        }


        int x = o2.getShapeId().getDomain().getSup();
        int y = o1.getShapeId().getDomain().getSup();
        return (stp.opt.memo_objects[o1.getObjectId()][o2.getObjectId()]) &&
                included.get(new Pair<Integer, Integer>(x, y)) && old_c.lexGreaterThan(c, ctrlV);
    }

    /**
     * Return the previous sweep point correponding to the most recent dominated object
     */
    Point getPreviousIteration(int k, int[] d, Obj o, Point c, List<Obj> memo) {

        if (!memo.isEmpty()) { //&& (dominates(k,d,c,memo.firstElement(),o)) ) {
            for (int i = 0; i < memo.size(); i++) {
                //c.print(); LOGGER.info(" dominates "); memo.get(i).print(); LOGGER.info(" ?");
                boolean b = (dominates(k, d, c, memo.get(i), o));
                //LOGGER.info(b);
                if (b) {
                    Point new_sweep_point = new Point(k);
                    //Recall that previous objects are all fixed (instantiated)
                    for (int dim = 0; dim < k; dim++) {
                        new_sweep_point.setCoord(dim, memo.get(i).getCoord(dim).getVal());
                    }
                    return new_sweep_point;
                }
            }
        }
        return c;
    }

    /**
     * Fix completely all the coordinates of the origin of the object o according to the set of internal constraints associated with object o.
     *
     * @param o     The object.
     * @param k     The total number of dimensions (The dimension of the space we are working in)
     * @param ctrlV The control vector
     * @param ictrs The internal constraints associated with o.
     * @return It return false if we couldn't fix the coordinates of Object o  according to the order specified by the controlling vector ctrlV.
     */
    boolean pruneFix(Obj o, int k, int[] ctrlV, List<InternalConstraint> ictrs) throws ContradictionException {
        stp.opt.PruneFixCalled++;

        boolean printit = false;

        if (printit) {
            LOGGER.info("-- Entering PruneFix() for object " + o.getObjectId());
            o.print();
        }

        Point c = new Point(k);
        Point n = new Point(k);
        int dPrime = 0;
        for (int d = k - 1; d > -1; d--) {
            dPrime = Math.abs(ctrlV[d + 1]) - 2;
            if (ctrlV[d + 1] < 0) {
                c.setCoord(dPrime, o.getCoord(dPrime).getInf());
                n.setCoord(dPrime, o.getCoord(dPrime).getSup() + 1);
            } else {
                c.setCoord(dPrime, o.getCoord(dPrime).getSup());
                n.setCoord(dPrime, o.getCoord(dPrime).getInf() - 1);
            }
        }

        //Here Memoisation should be added
        if ((memo.active) && (memo.m.get(ctrlV) != null)) {
            c = getPreviousIteration(k, ctrlV, o, c, memo.listObj.get(memo.m.get(ctrlV)));
        }

        long tmpTimeGetFr = (System.nanoTime() / ONE_MILLION);
        List forbidRegion = getFR(Math.abs(ctrlV[1]) - 2, k, o, c, n, ictrs, true);
        stp.opt.timeGetFR += ((System.nanoTime() / ONE_MILLION) - tmpTimeGetFr);
        boolean infeasible = (Boolean) forbidRegion.get(0);
        Region f = (Region) forbidRegion.get(1);
        if (printit) {
            LOGGER.info("getFR region:" + f.toString());
        }
        while (infeasible) {
            if (printit) {
                LOGGER.info("Full n: ");
                n.print();
            }

            if (printit) {
                LOGGER.info("while infeasible");
            }
            for (int d = k - 1; d > -1; d--) {
                dPrime = Math.abs(ctrlV[d + 1]) - 2;

                if (ctrlV[d + 1] < 0) {
                    n.setCoord(dPrime, Math.min(n.getCoord(dPrime), f.getMaximumBoundary(dPrime) + 1));
                } else {
                    n.setCoord(dPrime, Math.max(n.getCoord(dPrime), f.getMinimumBoundary(dPrime) - 1));
                }
            }
            if (printit) {
                LOGGER.info("New n: ");
                n.print();
            }

            nextcand:
            {
                for (int d = k - 1; d > -1; d--) {
                    dPrime = Math.abs(ctrlV[d + 1]) - 2;
                    c.setCoord(dPrime, n.getCoord(dPrime));
                    if (ctrlV[d + 1] < 0) {
                        n.setCoord(dPrime, o.getCoord(dPrime).getSup() + 1);
                        if (c.getCoord(dPrime) < n.getCoord(dPrime)) {
                            break nextcand;
                        } else {
                            c.setCoord(dPrime, o.getCoord(dPrime).getInf());
                        }
                    } else {
                        n.setCoord(dPrime, o.getCoord(dPrime).getInf() - 1);
                        if (c.getCoord(dPrime) > n.getCoord(dPrime)) {
                            break nextcand;
                        } else {
                            c.setCoord(dPrime, o.getCoord(dPrime).getSup());
                        }
                    }
                }
                LOGGER.info("NO NEXT CANDIDATE WAS FOUND");
                return false;
            }

            //Here Memoisation should be added
            //if old object dominates current object o

            if ((memo.active) && (memo.m.get(ctrlV) != null)) {
                //LOGGER.info("getPreviousIteration 2 in:"+c);
                c = getPreviousIteration(k, ctrlV, o, c, memo.listObj.get(memo.m.get(ctrlV)));
                //LOGGER.info("getPreviousIteration 2 in:"+c);

                //Recall here that old_o is instantiated(fixed)
                //c = old_o.coord;
                //for (int d = 0; d < k; d++)
                //{
                //    c.setCoord(d, new_c.getCoord(d));
                //}
            }
            if (printit) {
                LOGGER.info("Next jump: ");
                c.print();
            }
            tmpTimeGetFr = (System.nanoTime() / ONE_MILLION);
            forbidRegion = getFR(Math.abs(ctrlV[1]) - 2, k, o, c, n, ictrs, true);
            stp.opt.timeGetFR += ((System.nanoTime() / ONE_MILLION) - tmpTimeGetFr);

            infeasible = (Boolean) forbidRegion.get(0);
            f = (Region) forbidRegion.get(1);
            if (printit) {
                LOGGER.info("region:" + f.toString());
            }

        }

        for (int d = 0; d < k; d++) {
            o.getCoord(d).instantiate(c.getCoord(d), this.constraint, true);
        }

        if (memo.active) {
            if (memo.m.get(ctrlV) == null) {
                memo.m.put(ctrlV, memo.listObj.size());
                memo.listObj.add(new ArrayList<Obj>(0));
            }

            List<Obj> currentList = memo.listObj.get(memo.m.get(ctrlV));

            if (currentList.size() == memo.p) {
                currentList.remove(currentList.size() - 1);
                currentList.add(0, o);
            } else {
                currentList.add(0, o);
            }

        }

        return true;
    }

    /* Implementation of the curve mode Section 7.7. "Speeding up the feasible point search when the forbidden regions do not correspond to boxes"*/

    static Region lexMore(Region box, Region best_box, int d, int k, boolean increase) {
        if (box == null) {
            return best_box;
        }
        if (best_box == null) {
            return box;
        }

        for (int i = 0; i < k; i++) {
            int j = (d - i) % k;
            if (j < 0) {
                j = j + k;
            }
            if (increase) {
                if (best_box.getMaximumBoundary(j) > box.getMaximumBoundary(j)) {
                    return best_box;
                }
                if (best_box.getMaximumBoundary(j) < box.getMaximumBoundary(j)) {
                    return box;
                }
            } else {
                if (best_box.getMinimumBoundary(j) > box.getMinimumBoundary(j)) {
                    return box;
                }
                if (best_box.getMinimumBoundary(j) < box.getMinimumBoundary(j)) {
                    return best_box;
                }
            }
        }

        return box;
    }

    static Region largestLexBox(int d, int k, boolean increase, Region box1, Region box2) {
        return lexMore_normal(box1, box2, d, k, increase);
    }

    static Region lexMore_normal(Region box, Region best_box, int d, int k, boolean increase) {
        if (box == null) {
            return best_box;
        }
        if (best_box == null) {
            return box;
        }

        for (int i = k - 1; i >= 0; i--) {
            int j = (d + i) % k;
            if (increase) {
                if (best_box.getMaximumBoundary(j) > box.getMaximumBoundary(j)) {
                    return best_box;
                }
                if (best_box.getMaximumBoundary(j) < box.getMaximumBoundary(j)) {
                    return box;
                }
            } else {
                if (best_box.getMinimumBoundary(j) > box.getMinimumBoundary(j)) {
                    return box;
                }
                if (best_box.getMinimumBoundary(j) < box.getMinimumBoundary(j)) {
                    return best_box;
                }
            }
        }

        return box;
    }

    static Region lexMore_volume(Region candidate, Region B, int d, int k, boolean increase) {

        if (candidate == null) {
            return B;
        }
        if (B == null) {
            return candidate;
        }
//        LOGGER.info("--")  ;

//        int[] lengthc = new int[3];
//        int[] lengthB = new int[3];
        int volumec = 1;
        int volumeB = 1;
        for (int i = 0; i < k; i++) {
//                        lengthc[i]=Math.abs(candidate.getMaximumBoundary(i)-candidate.getMinimumBoundary(i));
//                        lengthB[i]=Math.abs(B.getMaximumBoundary(i)-B.getMinimumBoundary(i));
            volumec *= Math.abs(candidate.getMaximumBoundary(i) - candidate.getMinimumBoundary(i));
            volumeB *= Math.abs(B.getMaximumBoundary(i) - B.getMinimumBoundary(i));
        }

        if (volumec > volumeB) {
            return candidate;
        }
        if (volumec == volumeB) {
            return lexMore_normal(candidate, B, d, k, increase);
        }

        return B;


    }

    static Region proportionalFBox(InternalConstraint ictr, int d, int k, Obj o, Point c, Point jump, boolean increase, double prop) {
        double[] prop_ = new double[1];
        prop_[0] = prop;
        return proportionalFBox(ictr, d, k, o, c, jump, increase, prop_);
    }

    static Region proportionalFBox(InternalConstraint ictr, int d, int k, Obj o, Point c, Point jump, boolean increase, double[] prop) {
        if (!(ictr instanceof ForbiddenRegion)) {
            throw new SolverException("proportionalFBox():ictr is not a distance internal constraint (not a subclass of Forbidden Region for MaximizeSizeOfFBox.");
        }
        ForbiddenRegion fr = (ForbiddenRegion) ictr;

        double localProp;
        Region f = new Region(k, o.getObjectId());
        for (int i = 0; i < k; i++) {
            int v = c.getCoord(i);
            f.setMinimumBoundary(i, v);
            f.setMaximumBoundary(i, v);
        }  /*line 1:f.min<-f.max<-p*/

        for (int i = k - 1, m = 0; i >= 0; i--, m++) {
            int j = (d + i) % k;
            if (i == 0) {
                localProp = 1.0;
            } else {
                localProp = prop[m];
            }
            if (increase) {
                int size = Math.abs(f.getMinimumBoundary(j) - fr.maximizeSizeOfFBox(true, j, k, f));
                //LOGGER.info("size:"+size+" "+f.getMinimumBoundary(j)+" "+fr.maximizeSizeOfFBox(true,j,k,f));
                size = (int) (((double) size) * localProp);

                int end = f.getMinimumBoundary(j) + size;
                //LOGGER.info("propSize:"+size+" end:"+end);
                f.setMaximumBoundary(j, Math.min(jump.getCoord(j) - 1, end));
            } else {
                /*CORRECT THIS PART*/
                int size = Math.abs(f.getMaximumBoundary(j) - fr.maximizeSizeOfFBox(false, j, k, f));
                size = (int) (((double) size) * localProp);
                int end = f.getMaximumBoundary(j) - size;
                f.setMinimumBoundary(j, Math.max(jump.getCoord(j) + 1, end));
            }
        }

        return f;
    }

    List /*(bool,region)*/ getBestFR(int d, int k, Obj o, Point c, Point n, List<InternalConstraint> ictrs, boolean increase, boolean mode, double prop) {
        double[][] prop_ = new double[1][1];
        prop_[0][0] = prop;
        return getBestFR(d, k, o, c, n, ictrs, increase, mode, prop_);
    }

    List /*(bool,region)*/ getBestFR(int d, int k, Obj o, Point c, Point n, List<InternalConstraint> ictrs, boolean increase, boolean mode, double[] prop) {
        double[][] prop_ = new double[1][prop.length];
        prop_[0] = prop;
        return getBestFR(d, k, o, c, n, ictrs, increase, mode, prop_);
    }

    List /*(bool,region)*/ getBestFR(int d, int k, Obj o, Point c, Point n, List<InternalConstraint> ictrs, boolean increase, boolean mode, double[][] prop) {
        boolean trace = false;
        if (!mode) {
            return getFR(d, k, o, c, n, ictrs, increase);
        }
//        LOGGER.info(); LOGGER.info("\\begin{itemize}");

        Region candidate = null;

        List<ForbiddenRegion> list = new ArrayList<ForbiddenRegion>(16);
        for (InternalConstraint ictr : ictrs) {
            if (!(ictr instanceof ForbiddenRegion)) {
                throw new SolverException("GetBestFR():not a ForviddenRegion constraint.");
            }
            ForbiddenRegion fr = (ForbiddenRegion) ictr;
            if (fr.insideForbidden(c)) {
                list.add(fr);
            }
        }
        if (trace) {
            LOGGER.info("list of cstrs:");
            for (ForbiddenRegion fr : list) {
                LOGGER.info(fr.getIctrID() + " ");
            }
        }

        int maxVolume = 0;
        List<Region> maxVolumeList = new ArrayList<Region>(16);
        for (ForbiddenRegion fr : list) {
            Region B, f = null;
            List r = fr.isFeasible(increase, d, k, o, c, n);
            if (trace) {
                LOGGER.info("\\item IsFeasible$(ictr,increase=" + increase + ",d=" + d + ",k=" + k + ",o=o3" + ",c=" + c + ",n=" + n + ")$ ");
            }
            f = (Region) r.get(1);
            if (trace) {
                LOGGER.info(String.format("returns $f=%s$ ", f));
                LOGGER.info(String.format("\\item ProportionalFBox$(ictr,d=%d,k=%d,o=o3,c=%s,n=%s,increase=%s,prop=%s)$ ", d, k, c, n, increase, prop));
            }
            if (f.volume() > (maxVolume * 1.20)) {
                maxVolumeList.clear();
                maxVolume = f.volume();
                maxVolumeList.add(f);
            } else {
                if ((f.volume() <= (maxVolume * 1.20)) && (f.volume() >= (maxVolume * 0.80))) {
                    maxVolumeList.add(f);
                }
            }

            //if (list.size()>1) {
            boolean firstTime = true;
            for (int p = 0; p < prop.length; p++) {
                B = proportionalFBox(fr, d, k, o, c, n, increase, prop[p]);
                if (trace) {
                    LOGGER.info("returns $B=" + B + "$ ");
                }
                if (B.volume() > (maxVolume * 1.20)) {
                    maxVolumeList.clear();
                    maxVolume = B.volume();
                    maxVolumeList.add(B);
                } else {
                    if ((B.volume() <= (maxVolume * 1.20)) && (B.volume() >= (maxVolume * 0.80))) {
                        maxVolumeList.add(B);
                    }
                }

                if (trace) {
                    LOGGER.info("fr(" + f.getMinimumBoundary(0) + ',' + f.getMinimumBoundary(1) + ',' + f.getMaximumBoundary(0) + ',' + f.getMaximumBoundary(1) + ");");
                }
                if (trace) {
                    LOGGER.info("fr(" + B.getMinimumBoundary(0) + ',' + B.getMinimumBoundary(1) + ',' + B.getMaximumBoundary(0) + ',' + B.getMaximumBoundary(1) + ");");
                }


                if (trace) {
                    LOGGER.info("\\item lexMore$(B=" + B + ",f=" + f + ",d=" + d + ",k=" + k + ",increase=" + increase + ")$ ");
                }
                if (firstTime) {
                    B = lexMore_volume(B, f, d, k, increase);
                    firstTime = false;
                }
                if (trace) {
                    LOGGER.info("\\item $B \\gets " + B + "$ ");
                }


                if (trace) {
                    LOGGER.info("\\item lexMore$(candidate=" + candidate + ",B=" + B + ",d=" + d + ",k=" + k + ",increase=" + increase + ")$ ");
                }
                candidate = lexMore_volume(candidate, B, d, k, increase);
            }
            //} else {candidate=f;}
            if (trace) {
                LOGGER.info("\\item $candidate \\gets " + candidate + " $");
            }


        }
        list = null;

        //choose the one with the best lex
        if (!maxVolumeList.isEmpty()) {
            candidate = maxVolumeList.get(0);
        } else {
            candidate = null;
        }
        for (int i = 1; i < maxVolumeList.size(); i++) {
            candidate = lexMore(candidate, maxVolumeList.get(i), d, k, increase);
        }

        List result = new ArrayList(2);
        result.add((candidate != null)); //true means infeasible
        result.add(candidate);
        return result;
    }


    static List adjustDown(Point c, Point n, Obj o, int d, int k, boolean mode) {
        int cd = c.getCoord(d);
        List<Object> result = new ArrayList<Object>(4);
        int jPrime = 0;
        int j = k - 1;
        while (j >= 0) {
            jPrime = (j + d) % k;
            c.setCoord(jPrime, n.getCoord(jPrime));
            n.setCoord(jPrime, o.getCoord(jPrime).getInf() - 1);

            if (c.getCoord(jPrime) >= o.getCoord(jPrime).getInf()) {
                if (cd == c.getCoord(d) + 1) {
                    mode = true;
                }
                result.clear();
                result.add(0, c);
                result.add(1, n);
                result.add(2, true);
                result.add(3, mode);
                return result;
            } else {
                c.setCoord(jPrime, o.getCoord(jPrime).getSup());
            }
            j--;
        }

        result.clear();
        result.add(0, c);
        result.add(1, n);
        result.add(2, false);
        result.add(3, mode);
        return result;
    }

    static List adjustUp(Point c, Point n, Obj o, int d, int k, boolean mode) {
        int cd = c.getCoord(d);
        List<Object> result = new ArrayList<Object>(4);
        int jPrime = 0;
        int j = k - 1;
        while (j >= 0) {
            jPrime = (j + d) % k;
            c.setCoord(jPrime, n.getCoord(jPrime));
            n.setCoord(jPrime, o.getCoord(jPrime).getSup() + 1);
            if (c.getCoord(jPrime) <= o.getCoord(jPrime).getSup()) {
                if (cd == c.getCoord(d) - 1) {
                    mode = true;
                }
                result.clear();
                result.add(0, c);
                result.add(1, n);
                result.add(2, true);
                result.add(3, mode);
                return result;
            } else {
                c.setCoord(jPrime, o.getCoord(jPrime).getInf());
            }
            j--;
        }
        result.clear();
        result.add(0, c);
        result.add(1, n);
        result.add(2, false);
        result.add(3, mode);

        return result;
    }

    boolean newPruneMin(Obj o, int d, int k, List<InternalConstraint> ictrs) throws ContradictionException {
        boolean trace = false;
        if (trace) {
            LOGGER.info("//in:o" + o.getObjectId() + ':' + o + '(' + ictrs + ')');
        }
        boolean processing = true;
//        LOGGER.info("{sphereList.clear();   ");
//
//        for (Integer l : stp.getObjectKeySet()) {
//            Obj otmp=stp.getObject(l);
//            if (otmp.coordInstantiated()) {
//            LOGGER.info("sphere("+o.getObjectId()+","+otmp.getRadius()+",");
//            for (int i=0; i<k; i++) if (i!=k-1) LOGGER.info(otmp.getCoord(i).getInf()+","); else LOGGER.info(otmp.getCoord(i).getInf()+");");
//            }
//        }
//        LOGGER.info("}");

        if (stp.opt.serial != null) {
            LOGGER.info("COUCOU");
            try {
                stp.opt.serial.writeObject(o);
                stp.opt.serial.writeObject(d);
                stp.opt.serial.writeObject(k);
                stp.opt.serial.writeObject(ictrs);
            } catch (Exception e) {
                throw new SolverException("Prune:unable to serialize parameters param in");
            }
        }

        boolean b = true;
        boolean mode = false;
        Point c = new Point(k);
        Point n = new Point(k);

        for (int i = 0; i < o.getCoordinates().length; i++) {
            c.setCoord(i, o.getCoord(i).getInf()); // Initial position of point
            n.setCoord(i, o.getCoord(i).getSup() + 1); // Upper limits + 1 in the different dimensions
        }

        //LOGGER.info("In Figure \\ref{fig:jumpone:"+(phase)+"},%NEXT FIGURE--------"); phase++;
        //LOGGER.info("\\begin{itemize}");
        //LOGGER.info("\\item GetBestFR$(d="+d+",k="+k+", o=o3, c="+c+",n="+n+",ICTRS=\\{c_1,c_2\\},increase=\\TRUE,mode="+mode+",prop="+stp.opt.prop+")$ ");
        //double[] prop = { 0.66, 0.5, 0.33};
        List forbidRegion = getBestFR(d, k, o, c, n, ictrs, true, mode, stp.opt.prop);
        //LOGGER.info(" returns $[infeasible="+forbidRegion.get(0)+",f="+forbidRegion.get(1)+"]$ ");


        boolean infeasible = (Boolean) forbidRegion.get(0);
        Region f = (Region) forbidRegion.get(1);
        if (processing) {


            LOGGER.info("if (phase==" + (stp.opt.phase) + "){");
            LOGGER.info("sphereList.clear(); container_size(" + stp.getObject(3).getCoord(0).getInf() * 2 + ',' + stp.getObject(3).getCoord(1).getInf() * 2 + ',' + stp.getObject(3).getCoord(2).getInf() * 2 + ");");
            for (Integer l : stp.getObjectKeySet()) {
                Obj otmp = stp.getObject(l);
                if (otmp.coordInstantiated()) {
                    LOGGER.info(" sphere(" + o.getObjectId() + ',' + otmp.getRadius() + ',');
                    for (int i = 0; i < k; i++) {
                        if (i != k - 1) {
                            LOGGER.info(otmp.getCoord(i).getInf() + ",");
                        } else {
                            LOGGER.info(otmp.getCoord(i).getInf() + ");");
                        }
                    }
                }
            }
            //LOGGER.info("} ");
            //output constrained space
//            for (InternalConstraint ic : ictrs) {
//                if (ic instanceof DistLeqIC) {
//                    DistLeqIC icd = (DistLeqIC) ic;
//                    {
//                    IntDomainVar[] idv=stp.getObject(icd.o1).getCoordinates();
//                    LOGGER.info("fill(255,0,0,125); sphere(0,"+icd.D/2+","+idv[0].getInf()+","+idv[1].getInf()+","+idv[2].getInf()+");");}
//                    {
//                    IntDomainVar[] idv=stp.getObject(icd.o2).getCoordinates();
//                    LOGGER.info("fill(255,0,0,125); sphere(0,"+icd.D/2+","+idv[0].getInf()+","+idv[1].getInf()+","+idv[2].getInf()+");");}
//                }
//                if (ic instanceof DistGeqIC) {
//                    DistGeqIC icd = (DistGeqIC) ic;
//                    {
//                    IntDomainVar[] idv=stp.getObject(icd.o1).getCoordinates();
//                    LOGGER.info("fill(255,0,0,125); sphere(0,"+icd.D/2+","+idv[0].getInf()+","+idv[1].getInf()+","+idv[2].getInf()+");");}
//                    {
//                    IntDomainVar[] idv=stp.getObject(icd.o2).getCoordinates();
//                    LOGGER.info("fill(255,0,0,125); sphere(0,"+icd.D/2+","+idv[0].getInf()+","+idv[1].getInf()+","+idv[2].getInf()+");");}
//                }
//
//            }


            if (infeasible) {
                LOGGER.info(";mode=" + mode + ";d=" + d + ';');
                LOGGER.info("fr(");
                for (int i = 0; i < k; i++) {
                    if (i != k - 1) {
                        LOGGER.info(f.getMinimumBoundary(i) + "," + f.getMaximumBoundary(i) + ',');
                    } else {
                        LOGGER.info(f.getMinimumBoundary(i) + "," + f.getMaximumBoundary(i) + ");");
                    }
                }
            }
            LOGGER.info(" sweep_point(");
            for (int i = 0; i < k; i++) {
                if (i != k - 1) {
                    LOGGER.info(c.getCoord(i) + ",");
                } else {
                    LOGGER.info(c.getCoord(i) + ");");
                }
            }
            LOGGER.info(" jump_point(");
            for (int i = 0; i < k; i++) {
                if (i != k - 1) {
                    LOGGER.info(c.getCoord(i) + ",");
                } else {
                    LOGGER.info(c.getCoord(i) + ");");
                }
            }

            LOGGER.info("}");
            stp.opt.phase++;
        }


        if (stp.opt.serial != null) {
            try {
                stp.opt.serial.writeObject(c);
                stp.opt.serial.writeObject(n);
                stp.opt.serial.writeObject(b);
                stp.opt.serial.writeObject(infeasible);
                stp.opt.serial.writeObject(f);
            } catch (Exception e) {
                throw new SolverException("Prune:unable to serialize parameters first iter");
            }
        }


        while (b && infeasible) {

            for (int i = 0; i < k; i++) {
                // update n according to f
                n.setCoord(i, Math.min(n.getCoord(i), f.getMaximumBoundary(i) + 1));
            }

            Point initial_c = new Point(c);     //create a copy
            //LOGGER.info("\\item AdjutUp$(c="+c+",n="+n+", o=o3, d="+d+",k="+k+",mode="+mode+")$ ");
            List adjUp = adjustUp(c, n, o, d, k, mode); // update the position of c to check
            //LOGGER.info(" returns $[c="+adjUp.get(0)+",n="+adjUp.get(1)+",b="+adjUp.get(2)+",mode="+adjUp.get(3)+"]$ ");
            c = (Point) adjUp.get(0);
            n = (Point) adjUp.get(1);
            b = (Boolean) adjUp.get(2);
            mode = (Boolean) adjUp.get(3);
            adjUp = null;

            if (stp.opt.delta.get(d) == null) {
                stp.opt.delta.put(d, new HashMap<Integer, Integer>(16));
            }
            HashMap<Integer, Integer> curDelta = stp.opt.delta.get(d);
            int delta = Math.abs(c.getCoord(d) - initial_c.getCoord(d));
            if (curDelta.get(delta) == null) {
                curDelta.put(delta, 0);
            }
            curDelta.put(delta, curDelta.get(delta) + 1);

//            LOGGER.info("\\end{itemize}");
//            LOGGER.info("In Figure \\ref{fig:jumpone:"+(phase)+"},%NEXT FIGURE--------");phase++;
//            LOGGER.info("\\begin{itemize}");
            //LOGGER.info("\\item GetBestFR$(d="+d+",k="+k+", o=o3, c="+c+",n="+n+",ICTRS=\\{c_1,c_2\\},increase=\\TRUE,mode="+mode+",prop="+stp.opt.prop+")$ ");
            //double[] prop2 = { 0.66, 0.5, 0.33};
            forbidRegion = getBestFR(d, k, o, c, n, ictrs, true, mode, stp.opt.prop);
            //LOGGER.info(" returns $[infeasible="+forbidRegion.get(0)+",f="+forbidRegion.get(1)+"]$ ");
            if (processing) {

                LOGGER.info("if (phase==" + (stp.opt.phase) + "){");
                LOGGER.info("sphereList.clear();");

                for (Integer l : stp.getObjectKeySet()) {
                    Obj otmp = stp.getObject(l);
                    if (otmp.coordInstantiated()) {
                        LOGGER.info(" sphere(" + o.getObjectId() + ',' + otmp.getRadius() + ',');
                        for (int i = 0; i < k; i++) {
                            if (i != k - 1) {
                                LOGGER.info(otmp.getCoord(i).getInf() + ",");
                            } else {
                                LOGGER.info(otmp.getCoord(i).getInf() + ");");
                            }
                        }
                    }
                }
                LOGGER.info("}");

                if (infeasible) {
                    LOGGER.info(";mode=" + mode + ";d=" + d + ';');
                    LOGGER.info(" fr(");
                    for (int i = 0; i < k; i++) {
                        if (i != k - 1) {
                            LOGGER.info(f.getMinimumBoundary(i) + "," + f.getMaximumBoundary(i) + ',');
                        } else {
                            LOGGER.info(f.getMinimumBoundary(i) + "," + f.getMaximumBoundary(i) + ");");
                        }
                    }
                }
//                for (InternalConstraint ic : ictrs) {
//                    if (ic instanceof DistLeqIC) {
//                        DistLeqIC icd = (DistLeqIC) ic;
//                        {
//                        IntDomainVar[] idv=stp.getObject(icd.o1).getCoordinates();
//                        LOGGER.info("fill(255,0,0,125); sphere(0,"+icd.D/2+","+idv[0].getInf()+","+idv[1].getInf()+","+idv[2].getInf()+");");}
//                        {
//                        IntDomainVar[] idv=stp.getObject(icd.o2).getCoordinates();
//                        LOGGER.info("fill(255,0,0,125); sphere(0,"+icd.D/2+","+idv[0].getInf()+","+idv[1].getInf()+","+idv[2].getInf()+");");}
//                    }
//                    if (ic instanceof DistGeqIC) {
//                        DistGeqIC icd = (DistGeqIC) ic;
//                        {
//                        IntDomainVar[] idv=stp.getObject(icd.o1).getCoordinates();
//                        LOGGER.info("fill(255,0,0,125); sphere(0,"+icd.D/2+","+idv[0].getInf()+","+idv[1].getInf()+","+idv[2].getInf()+");");}
//                        {
//                        IntDomainVar[] idv=stp.getObject(icd.o2).getCoordinates();
//                        LOGGER.info("fill(255,0,0,125); sphere(0,"+icd.D/2+","+idv[0].getInf()+","+idv[1].getInf()+","+idv[2].getInf()+");");}
//                    }
//
//                }

                LOGGER.info(" sweep_point(");
                for (int i = 0; i < k; i++) {
                    if (i != k - 1) {
                        LOGGER.info(c.getCoord(i) + ",");
                    } else {
                        LOGGER.info(c.getCoord(i) + ");");
                    }
                }
                LOGGER.info(" jump_point(");
                for (int i = 0; i < k; i++) {
                    if (i != k - 1) {
                        LOGGER.info(c.getCoord(i) + ",");
                    } else {
                        LOGGER.info(c.getCoord(i) + ");");
                    }
                }

                LOGGER.info("}");
                stp.opt.phase++;
            }

            infeasible = (Boolean) forbidRegion.get(0);
            f = (Region) forbidRegion.get(1);
            forbidRegion = null;
            if (stp.opt.serial != null) {
                try {
                    stp.opt.serial.writeObject(c);
                    stp.opt.serial.writeObject(n);
                    stp.opt.serial.writeObject(b);
                    stp.opt.serial.writeObject(infeasible);
                    stp.opt.serial.writeObject(f);
                } catch (Exception e) {
                    throw new SolverException("Prune:unable to serialize parameters second iter");
                }
            }


        }

        if (b) {
            o.getCoord(d).updateInf(c.getCoord(d), this.constraint, true);
            if (trace) {
                LOGGER.info("//out:o:" + o);
            }
        }


        if (stp.opt.serial != null) {
            try {
                stp.opt.serial.writeObject(o);
                stp.opt.serial.writeObject(b);
            } catch (Exception e) {
                throw new SolverException("Prune:unable to serialize parameters param out");
            }
        }
//        LOGGER.info(" Prune out");

        return b;
    }

    boolean newPruneMax(Obj o, int d, int k, List<InternalConstraint> ictrs) throws ContradictionException {
        boolean b = true;
        boolean mode = false;
        Point c = new Point(k);
        Point n = new Point(k);


        for (int i = 0; i < o.getCoordinates().length; i++) {
            c.setCoord(i, o.getCoord(i).getSup()); // Initial position of point
            n.setCoord(i, o.getCoord(i).getInf() - 1); // Lower limits - 1 in the different dimensions
        }

        List forbidRegion = getBestFR(d, k, o, c, n, ictrs, false, mode, stp.opt.prop);
        boolean infeasible = (Boolean) forbidRegion.get(0);
        Region f = (Region) forbidRegion.get(1);
        while (b && infeasible) {
            for (int i = 0; i < k; i++) {
                // update n according to f
                n.setCoord(i, Math.max(n.getCoord(i), f.getMinimumBoundary(i) - 1));
            }

            Point initial_c = new Point(c);     //create a copy

            List adjDown = adjustDown(c, n, o, d, k, mode);// update the position of c to check

            c = (Point) adjDown.get(0);
            n = (Point) adjDown.get(1);
            b = (Boolean) adjDown.get(2);
            mode = (Boolean) adjDown.get(3);
            adjDown = null;

            if (stp.opt.delta.get(d) == null) {
                stp.opt.delta.put(d, new HashMap<Integer, Integer>(16));
            }
            HashMap<Integer, Integer> curDelta = stp.opt.delta.get(d);
            int delta = Math.abs(c.getCoord(d) - initial_c.getCoord(d));
            if (curDelta.get(delta) == null) {
                curDelta.put(delta, 0);
            }
            curDelta.put(delta, curDelta.get(delta) + 1);


            forbidRegion = getBestFR(d, k, o, c, n, ictrs, false, mode, stp.opt.prop);
            infeasible = (Boolean) forbidRegion.get(0);
            f = (Region) forbidRegion.get(1);
            forbidRegion = null;
        }

        if (b) {
            o.getCoord(d).updateSup(c.getCoord(d), this.constraint, true);
        }
        return b;
    }

    static boolean isFeasible(Point p, List<InternalConstraint> ictrs) {
        for (InternalConstraint ictr : ictrs) {
            if (!(ictr instanceof ForbiddenRegion)) {
                throw new SolverException("GeometricKernel:isFeasible():not a ForbiddenRegion constraint.");
            }
            ForbiddenRegion fr = (ForbiddenRegion) ictr;
            if (fr.insideForbidden(p)) {
                return false;
            }
        }
        return true;
    }


    //BEGIN NEW IMPLEM

    //7.7.2. Utility functions

    static List<ForbiddenRegion> setOfCstrsOnPt(Point c, List<InternalConstraint> ictrs) {
        List<ForbiddenRegion> r = new ArrayList<ForbiddenRegion>(ictrs.size());
        for (InternalConstraint ictr : ictrs) {
            if (!(ictr instanceof ForbiddenRegion)) {
                throw new SolverException("GeometricKernel:SetOfCstrsOnPt():not a ForbiddenRegion constraint.");
            }
            ForbiddenRegion fr = (ForbiddenRegion) ictr;
            if (fr.insideForbidden(c)) {
                r.add(fr);
            }
        }

        return r;
    }

    static int nbrOfCstrsOnPt(Point c, List<InternalConstraint> ictrs) {
        return setOfCstrsOnPt(c, ictrs).size();
    }

    static Point maxPt(Point p1, Point p2, int d, boolean second_pt_is_defined, boolean increase) {
        if ((!second_pt_is_defined) || ((p1.getCoord(d) >= p2.getCoord(d)) == increase)) {
            return p1;
        }
        return p2;

    }

    static Point slidePt(Point p, int d, int value) {
        Point r = new Point(p);
        r.setCoord(d, value);
        return r;
    }


    static int prev(boolean increase) {
        if (increase) {
            return -1;
        } else {
            return 1;
        }
    }

    static int succ(boolean increase) {
        if (increase) {
            return 1;
        } else {
            return -1;
        }
    }

    static int min(int a, int b, boolean increase) {
        if (increase) {
            return Math.min(a, b);
        }
        return Math.max(a, b);
    }

    static int max(int a, int b, boolean increase) {
        if (increase) {
            return Math.max(a, b);
        }
        return Math.min(a, b);
    }

    static Region buildBox(int k, Point p1, Point p2) {
        Region box = new Region(k, 0);
        for (int i = 0; i < k; i++) {
            box.setMinimumBoundary(i, Math.min(p1.getCoord(i), p2.getCoord(i)));
            box.setMaximumBoundary(i, Math.max(p1.getCoord(i), p2.getCoord(i)));
        }
        return box;
    }

    static double ratio(Region box) {
        return box.ratio();
    }


    Point extend(Point p, int d, int k, Point n, ForbiddenRegion ictr, boolean increase) {
        if (stp.opt.debug) {
            if (!(ictr.insideForbidden(p))) {
                throw new SolverException("GeometricKernel:Extend():Invariant 1 failed:p:" + p + " is not segInsideForbidden of ictr:" + ictr);
            }
        }
//        if (stp.opt.debug) {
//            int i=d;
//                boolean condition = (( increase && (p.getCoord(i)<n.getCoord(i) )) || ( (!increase) && (p.getCoord(i)>n.getCoord(i ))));
//                if (!(condition)) {
//                    LOGGER.info("GeometricKernel:Extend():Invariant 2 failed:p:"+p+" is larger than n:"+n+" on dim:"+i+",pruning dim:"+d+",increase:"+increase);
//                    System.exit(-1);
//                }
//
//
//        }

        Region box = new Region(p);
        Point result = new Point(p);
        int m = ictr.maximizeSizeOfFBox(increase, d, k, box);
        result.setCoord(d, min(n.getCoord(d) + prev(increase), m, increase));
        return result;
    }

    Point extend2(Point p, int d, int k, Point cut_point, ForbiddenRegion ictr1, ForbiddenRegion ictr2, boolean increase) {
        if (stp.opt.debug) {
            if (!(ictr1 != ictr2)) { //TODO: compare the contect of the internal constraints
                throw new SolverException("GeometricKernel:Extend2():Invariant 1 failed:(ictr!=ictr2)");

            }

            if (!(ictr1.insideForbidden(p))) {
                throw new SolverException("GeometricKernel:Extend2():Invariant 2 failed:(ictr.segInsideForbidden(p))");
            }

            if (!(ictr2.insideForbidden(p))) {
                throw new SolverException("GeometricKernel:Extend2():Invariant 3 failed:(ictr2.segInsideForbidden(p))");
            }

//            boolean condition = (( increase && (p.getCoord(d)<cut_point.getCoord(d) )) || ( (!increase) && (p.getCoord(d)>cut_point.getCoord(d))));
//            if (!condition) {
//                LOGGER.info("GeometricKernel:Extend2():Invariant 4 failed:(( increase && (p.getCoord(d)<=cut_point.getCoord(d) )) || ( (!increase) && (p.getCoord(d)>=cut_point.getCoord(d))))");
//                System.exit(-1);
//
//            }
        }//end stp.opt.debug

        Region box = new Region(p);
        Point result = new Point(p);
        result.setCoord(d, min(cut_point.getCoord(d), ictr1.maximizeSizeOfFBox(increase, d, k, box), increase));
        result.setCoord(d, min(result.getCoord(d), ictr2.maximizeSizeOfFBox(increase, d, k, box), increase));
        return result;
    }

    static Point extend_both(Point p, int d, int k, Point cut_point, ForbiddenRegion ictr, ForbiddenRegion ictr2, boolean increase) {
//        Region box= new Region(p);
        Point result = new Point(p);

        if (ictr.insideForbidden(result)) {
            result.setCoord(d, min(cut_point.getCoord(d), ictr.maximizeSizeOfFBox(increase, d, k, new Region(result)), increase));
        }
        if (ictr2.insideForbidden(result)) {
            result.setCoord(d, min(cut_point.getCoord(d), ictr2.maximizeSizeOfFBox(increase, d, k, new Region(result)), increase));
        }
        if (ictr.insideForbidden(result)) {
            result.setCoord(d, min(cut_point.getCoord(d), ictr.maximizeSizeOfFBox(increase, d, k, new Region(result)), increase));
        }
        if (ictr2.insideForbidden(result)) {
            result.setCoord(d, min(cut_point.getCoord(d), ictr2.maximizeSizeOfFBox(increase, d, k, new Region(result)), increase));
        }

        return result;
    }


    //7.7.2. Comparison Functions Between Two Boxes

    static boolean largestInverseLex(int d_prune, int k, boolean increase, Region box, Region best_box) {
        //invariant
        if (!(box != null && best_box != null)) {
            throw new SolverException("GeometricKernel:LargestInverseLex():invariant 1 failed");
        }
        int d = 0;
        while (d <= k + 1) {
            int d_prime = (d + d_prune) % k;
            int b, bb;
            if (increase) {
                b = box.getMaximumBoundary(d_prime);
                bb = best_box.getMaximumBoundary(d_prime);
            } else {
                b = box.getMinimumBoundary(d_prime);
                bb = best_box.getMinimumBoundary(d_prime);
            }
            if (b == bb) {
                d++;
            } else {
                return ((b > bb) == increase);
            }
        }
        return false;
    }

    static Region largestInvLexBox(int d_prune, int k, boolean increase, Region box, Region best_box) {
        //if box are the sames (regarding sizes in all dim.), returns the second one
        if (box == null) {
            return best_box;
        }
        if (best_box == null) {
            return box;
        }
        if (largestInverseLex(d_prune, k, increase, box, best_box)) {
            return box;
        }
        return best_box;
    }

    static boolean equalInverseLex(int k, Region box, Region best_box) {
        for (int i = 0; i < k; i++) {
            if (box.getSize(i) != best_box.getSize(i)) {
                return false;
            }
        }
        return true;
    }

//
//    Region BestVolume2(int d_prune,int d_prev_least, int d_least, int k, boolean increase, Region box, Region best_box) {
//        if (box==null) return best_box;
//        if (best_box==null) return box;
//        int a = Math.abs(box.getMaximumBoundary(d_prev_least)-box.getMinimumBoundary(d_prev_least))+1;
//        int b = Math.abs(box.getMaximumBoundary(d_least)-box.getMinimumBoundary(d_least))+1;
//        int c = Math.abs(best_box.getMaximumBoundary(d_prev_least)-best_box.getMinimumBoundary(d_prev_least))+1;
//        int d = Math.abs(best_box.getMaximumBoundary(d_least)-best_box.getMinimumBoundary(d_least))+1;
//        int volume_box=a*b;
//        int volume_best_box=c*d;
//        if (a!=0)   volume_box+=((int) (2*volume_box)*(1/a));
//        if (a!=0)   volume_best_box+=((int) (2*volume_best_box)*(1/c));
//        //volume_box+=(volume_box*(1/d_least));
//        //volume_best_box+=(volume_best_box*(1/d_least));
//        //if (box.volume()>best_box.volume()) return box;
//        if (volume_box>volume_best_box) return box;
//        return best_box;
//    }
//

    //
    static Region bestVolume(Region box, Region best_box) {
        if (box == null) {
            return best_box;
        }
        if (best_box == null) {
            return box;
        }
        int box_volume = box.volume();
        int best_box_volume = best_box.volume();
        if (box_volume > best_box_volume) {
            return box;
        }
        return best_box;
    }


//    boolean GreatestVolume(int d_prune,int k, boolean increase, Region box, Region best_box) {
//        return (box.volume()>best_box.volume());
//    }

    //
    static boolean equalVolume(Region box, Region best_box) {
        return (box.volume() == best_box.volume());
    }

    Region selectionCriteria(int d_prune, int k, boolean increase, Region box1, Region box2) {
        if (stp.opt.debug) {
            LOGGER.info("/*debug*/SelectionCriteria(d_prune=" + d_prune + ", k=" + k + ", increase=" + increase + " box1=" + box1 + " ,box2=" + box2 + ')');
        }
        if (box1 == null) {
            if (stp.opt.debug) {
                LOGGER.info("/*debug*/SelectionCriteria() returns null");
                return box2;
            }
        }
        if (box2 == null) {
            if (stp.opt.debug) {
                LOGGER.info("/*debug*/SelectionCriteria() returns null");
                return box1;
            }
        }
        int won1 = 0;
        int won2 = 0;
        assert box1 != null;
        assert box2 != null;
        if ((box1.ratio() <= 0.1) && (box2.ratio() <= 0.1) && ((!box1.included(box2)) && (!box2.included(box1)))) {
            Region tmp = largestLexBox(d_prune, k, increase, box1, box2);
            if (stp.opt.debug) {
                LOGGER.info("/*debug*/SelectionCriteria() returns " + tmp);
            }
            return tmp;
        }

        if (!equalVolume(box1, box2)) {
            if (bestVolume(box1, box2) == box1) {
                won1++;
            } else {
                won2++;
            }
        }

        boolean not_same = !equalInverseLex(k, box1, box2);

        boolean first_box = false;
        if (not_same) {
            first_box = (largestInvLexBox(d_prune, k, increase, box1, box2) == box1);
            if (first_box) {
                won1++;
            } else {
                won2++;
            }
        }

        if (k != 2) {
            throw new SolverException("GeometricKernel:SelectionCriteria():only k=2 is supported.");
        }

//        double max,min;
//        max=Math.max(box1.getSize(0),box1.getSize(1));
//        min=Math.min(box1.getSize(0),box1.getSize(1));
//        double ratio1 = min/max;
        double ratio1 = ratio(box1);

//        max=Math.max(box2.getSize(0),box2.getSize(1));
//        min=Math.min(box2.getSize(0),box2.getSize(1));
//        double ratio2 = min/max;
        double ratio2 = ratio(box2);

        if (ratio1 != ratio2) {
            if (ratio1 > ratio2) {
                won1++;
            } else {
                won2++;
            }
        }

        if (won1 == won2) {
            if (not_same) {
                if (first_box) {
                    if (stp.opt.debug) {
                        LOGGER.info("/*debug*/SelectionCriteria() returns " + box1);
                    }
                    return box1;
                } else {
                    if (stp.opt.debug) {
                        LOGGER.info("/*debug*/SelectionCriteria() returns " + box2);
                    }

                    return box2;
                }
            } else {
                Region tmp = largestInvLexBox(d_prune, k, increase, box1, box2);
                if (stp.opt.debug) {
                    LOGGER.info("/*debug*/SelectionCriteria() returns " + tmp);
                }
                return tmp;
            }
        }

        if (won1 > won2) {
            if (stp.opt.debug) {
                LOGGER.info("/*debug*/SelectionCriteria() returns " + box1);
            }
            return box1;
        }

        if (stp.opt.debug) {
            LOGGER.info("/*debug*/SelectionCriteria() returns " + box2);
        }
        return box2;

    }


    //7.7.3. Single Constraint Box Generators

    static Region finishToExtendBox(int d_prune, int k, Point n, boolean increase, ForbiddenRegion ictr, int todim, Region box) {
        for (int d = k - 1; d >= todim; d--) {
            int d_prime = (d + d_prune) % k;

            if ((k != 3) || (d != d_prune)) {

                /*invariant*/
                if ((!(box.getMinimumBoundary(d_prime) == box.getMaximumBoundary(d_prime)))) {
                    throw new SolverException("GeometricKernel:FinishToExtendBox():box:" + box + ";d:" + d_prime);
                }

                if (increase) {
                    box.setMaximumBoundary(d_prime, Math.min(n.getCoord(d_prime) - 1, ictr.maximizeSizeOfFBox(increase, d_prime, k, box)));
                } else {
                    box.setMinimumBoundary(d_prime, Math.max(n.getCoord(d_prime) + 1, ictr.maximizeSizeOfFBox(increase, d_prime, k, box)));
                }
            }
        }
        return box;
    }

//    Region GetGreedyBoxFromPoint2(int d_prune,int d_prev_least, int d_least,int k, Point c, Point n, ForbiddenRegion ictr,boolean increase,int delta_prune) {
//        Region box = new Region(c);
//        int d = d_prev_least;
//        if (increase)
//            box.setMaximumBoundary(d,Math.min(n.getCoord(d)-1,ictr.maximizeSizeOfFBox(increase,d,k,box)));
//        else
//            box.setMinimumBoundary(d,Math.max(n.getCoord(d)+1,ictr.maximizeSizeOfFBox(increase,d,k,box)));
//        d = d_least;
//        if (increase)
//            box.setMaximumBoundary(d,Math.min(n.getCoord(d)-1,ictr.maximizeSizeOfFBox(increase,d,k,box)));
//        else
//            box.setMinimumBoundary(d,Math.max(n.getCoord(d)+1,ictr.maximizeSizeOfFBox(increase,d,k,box)));
//        return box;
//    }

    static Region getGreedyBoxFromPoint(int d_prune, int k, Point c, Point n, ForbiddenRegion ictr, boolean increase, int limit_prune) {
        Region box = new Region(c);
        if (k == 3) {
            if (increase) {
                if (!(box.getMinimumBoundary(d_prune) + limit_prune - 1 <= ictr.maximizeSizeOfFBox(increase, d_prune, k, box))) {
                    throw new SolverException("GeometricKernel:GetGreedyBoxFromPoint():invariant failed");
                }
                box.setMaximumBoundary(d_prune, box.getMinimumBoundary(d_prune) + limit_prune - 1);

            } else {
                if (!(box.getMaximumBoundary(d_prune) - limit_prune + 1 >= ictr.maximizeSizeOfFBox(increase, d_prune, k, box))) {
                    throw new SolverException("GeometricKernel:GetGreedyBoxFromPoint():invariant failed");
                }

                box.setMinimumBoundary(d_prune, box.getMaximumBoundary(d_prune) - limit_prune + 1);

            }
        }

        Region r = finishToExtendBox(d_prune, k, n, increase, ictr, 0, box);
        r.setType("Greedy");
        r.father = "GreedyPoint";
        return r;
    }


    static Region getGreedyBoxFromJumpVector(int d_prev_least, int d_prune, int k, Point c, Point n, int inter, ForbiddenRegion ictr, boolean increase, int limit_prune) {
        Region box = new Region(c);
        if (k == 3) {
            if (increase) {
                if (!(box.getMinimumBoundary(d_prune) + limit_prune - 1 <= ictr.maximizeSizeOfFBox(increase, d_prune, k, box))) {
                    throw new SolverException("GeometricKernel:GetGreedyBoxFromPoint():invariant failed");
                }
                box.setMaximumBoundary(d_prune, box.getMinimumBoundary(d_prune) + limit_prune - 1);

            } else {
                if (!(box.getMaximumBoundary(d_prune) - limit_prune + 1 >= ictr.maximizeSizeOfFBox(increase, d_prune, k, box))) {
                    throw new SolverException("GeometricKernel:GetGreedyBoxFromPoint():invariant failed");
                }
                box.setMinimumBoundary(d_prune, box.getMaximumBoundary(d_prune) - limit_prune + 1);

            }
        }

        if (increase) {
            int m = Math.min(n.getCoord(d_prev_least) - 1, inter);//tmp.getCoord(d_prev_least));
            m = Math.min(m, ictr.maximizeSizeOfFBox(increase, d_prev_least, k, box));
            box.setMaximumBoundary(d_prev_least, m);
        } else {
            int m = Math.max(n.getCoord(d_prev_least) + 1, inter);//tmp.getCoord(d_prev_least));
            m = Math.max(m, ictr.maximizeSizeOfFBox(increase, d_prev_least, k, box));
            box.setMinimumBoundary(d_prev_least, m);

        }
        Region r = finishToExtendBox(d_prune, k, n, increase, ictr, (d_prev_least + 1 - d_prune + k) % k, box);
        r.setType("Vector");
        r.father = "GreedyVector";
        return r;
    }

    //7.7.4. Multiple Constraints Box Generators (Semantic Boxes)

//    Region BuildInterBox(int d_prune,int d_least,int d_prev_least,int k, Point c,Point n,boolean increase,ForbiddenRegion ictr_c,int pos_p,int inter) {
//        Region box = new Region(c,0);
//        if (increase) {
//            if (k==3) box.setMaximumBoundary(d_prune,Math.min(n.getCoord(d_prune)-1,pos_p));
//            int min = Math.min(inter,ictr_c.maximizeSizeOfFBox(increase,d_prev_least,k,box));
//            box.setMaximumBoundary(d_prev_least,Math.min(n.getCoord(d_prev_least)-1,min));
//            box.setMaximumBoundary(d_least,Math.min(n.getCoord(d_least)-1,ictr_c.maximizeSizeOfFBox(increase,d_least,k,box)));
//        }
//        else {
//            if (k==3) box.setMinimumBoundary(d_prune,Math.max(n.getCoord(d_prune)+1,pos_p));
//            int max = Math.max(inter,ictr_c.maximizeSizeOfFBox(increase,d_prev_least,k,box));
//            box.setMinimumBoundary(d_prev_least,Math.max(n.getCoord(d_prev_least)+1,max));
//            box.setMinimumBoundary(d_least,Math.max(n.getCoord(d_least)+1,ictr_c.maximizeSizeOfFBox(increase,d_least,k,box)));
//        }
//        return box;
//    }

//    Region FindBoxInterIn(int d_prune, int d_least, int d_prev_least, int k, Point c, Point g, Point n, boolean increase, ForbiddenRegion ictr_c, ForbiddenRegion ictr_g, int pos_p) {
//        Region box_g = new Region(g);
//        int last_first = ictr_g.maximizeSizeOfFBox(!increase,d_least,k,box_g);
//        int low,up;
//        if (increase) {
//            low=Math.max(last_first,c.getCoord(d_least));
//            up=g.getCoord(d_least);
//        }
//        else {
//            up=Math.min(last_first,c.getCoord(d_least));
//            low=g.getCoord(d_least);
//        }
//
//        Pair r = FindBoxInterInLength(d_prune,d_least,d_prev_least,k,c,increase,ictr_c,ictr_g,pos_p,low,up);
//        boolean b = (Boolean) r.fst; int inter = (Integer) r.snd;  //inter is a position in d_prev_least
//
//        if (b) {
//            if (stp.opt.debug) { LOGGER.info("\n/*Processing*/intersection("+d_prune+","+inter+");"); };
//
//            return BuildInterBox(d_prune,d_least,d_prev_least,k,c,n,increase,ictr_c,pos_p,inter);
//        }
//
//        return null;
//    }

//    Pair<Integer,Integer> GetExtensions(int d_prune,int d_least,int d_prev_least,int k, Point c,boolean increase,ForbiddenRegion ictr_c, ForbiddenRegion ictr_g,int pos_p, int pos_l) {
//        if (stp.opt.debug) {
//
//        }
//        //if (k==3) {
//
//        //}
//        Region box=new Region(k,0);
//        box.setMinimumBoundary(d_least,pos_l); box.setMaximumBoundary(d_least,pos_l);
//        box.setMinimumBoundary(d_prev_least,c.getCoord(d_prev_least)); box.setMaximumBoundary(d_prev_least,c.getCoord(d_prev_least));
//        if (!(ictr_c.insideForbidden(box.pointMin()))) {LOGGER.info("GeometricKernel:GetExtensions():Invariant 1 failed"); System.exit(-1); }
//        int e_c=ictr_c.maximizeSizeOfFBox(increase,d_prev_least,k,box);
//        if (!(ictr_g.insideForbidden(box.pointMin()))) {LOGGER.info("GeometricKernel:GetExtensions():Invariant 2 failed"); System.exit(-1); }
//        int e_g=ictr_g.maximizeSizeOfFBox(increase,d_prev_least,k,box);
//        return new Pair<Integer,Integer>(e_c,e_g);
//    }

//    Pair<Boolean,Integer> FindBoxInterInLength(int d_prune, int d_least, int d_prev_least, int k, Point c, boolean increase, ForbiddenRegion ictr_c, ForbiddenRegion ictr_g, int pos_p, int low, int up) {
//        boolean one=false;
//        int elow_c,elow_g; int e=0;
//        Pair<Integer,Integer> r = GetExtensions(d_prune,d_least,d_prev_least,k,c,increase,ictr_c,ictr_g,pos_p,low);
//        elow_c=r.fst; elow_g=r.snd;
//        if (elow_c==elow_g) return new Pair<Boolean,Integer>(true,elow_c);
//        if ((Math.abs(elow_c-elow_g)==1) && (!one)) if (increase) e=Math.max(elow_c,elow_g); else e=Math.min(elow_c,elow_g);
//        int eup_c,eup_g;
//        r = GetExtensions(d_prune,d_least,d_prev_least,k,c,increase,ictr_c,ictr_g,pos_p,up);
//        eup_c=r.fst;eup_g=r.snd;
//        //LOGGER.info("/*Processing*/intersection("+d_least+","+low+");");
//        //LOGGER.info("/*Processing*/intersection("+d_least+","+up+");");
//
//        if (eup_c==eup_g) return new Pair<Boolean,Integer>(true,eup_c);
//        if (cross(increase,elow_c,elow_g,eup_c,eup_g)) {
//            while (low<=up) {
//                int mid=((low+up)/2);
//                //LOGGER.info("/*Processing*/intersection("+d_least+","+mid+");");
//                int emid_c,emid_g;
//                r=GetExtensions(d_prune,d_least,d_prev_least,k,c,increase,ictr_c,ictr_g,pos_p,mid);
//                emid_c=r.fst; emid_g=r.snd;
//                //LOGGER.info("/*Processing*/intersection("+d_prev_least+","+emid_c+");");
//                //LOGGER.info("/*Processing*/intersection("+d_prev_least+","+emid_g+");");
//
//                if (emid_c==emid_g) return new Pair<Boolean,Integer>(true,emid_c);
//                if ((Math.abs(emid_c-emid_g)==1) &&(!one)) {
//                    if (increase) e=Math.max(emid_c,emid_g); else e=Math.min(emid_c,emid_g); one=true;
//                }
//
//                boolean cross_low=cross(increase,elow_c,elow_g,emid_c,emid_g);
//                boolean cross_up=cross(increase,emid_c,emid_g,eup_c,eup_g);
//
//                if (cross_low) {
//                    up=mid-1;
//                    if (low<=up){
//                        r=GetExtensions(d_prune,d_least,d_prev_least,k,c,increase,ictr_c,ictr_g,pos_p,up);
//                        eup_c=r.fst;eup_g=r.snd;
//                        if (eup_c==eup_g) return new Pair<Boolean,Integer>(true,eup_c);
//                        if ((Math.abs(eup_c-eup_g)==1) &&(!one)) {
//                            if (increase) e=Math.max(eup_c,eup_g); else e=Math.min(eup_c,eup_g); one=true;
//                        }
//                    }
//                }
//                else if (cross_up) {
//                    low=mid+1;
//                    if (low<=up){
//                        r=GetExtensions(d_prune,d_least,d_prev_least,k,c,increase,ictr_c,ictr_g,pos_p,low);
//                        elow_c=r.fst;elow_g=r.snd;
//                        if (elow_c==elow_g) return new Pair<Boolean,Integer>(true,elow_c);
//                        if ((Math.abs(elow_c-elow_g)==1) &&(!one)) {
//                            if (increase) e=Math.max(elow_c,elow_g); else e=Math.min(elow_c,elow_g); one=true;
//                        }
//                    }
//                }
//                else {
//                    //Experimental: consider intersection has been found, since succession of crosses
//                    if (Math.abs(low-up)<=1)  {//&& (cross_low || cross_up)) {
//                        if (increase) e=Math.max(emid_c,emid_g); else e=Math.min(emid_c,emid_g);
//                        one=true;
//                    }
//                    return new Pair<Boolean,Integer>(one,e);
//                }
//            } //while
//        }//if cross
//
//        if (!one) {
//            Pair<Boolean,Integer> p = FindBoxInterOut(d_prune, d_least,d_prev_least, k, c, increase, ictr_c, ictr_g, pos_p, low,up);
//            one=p.fst; e=p.snd;
//        }
//
//        return new Pair<Boolean,Integer>(one,e);
//
//    }

    Pair<Boolean, Integer> findBoxInterIn(int d_least, int d_prev_least, int k, Point c, Point n, boolean increase, ForbiddenRegion ictr_c, ForbiddenRegion ictr_g, int low, int up) {
        if (low >= up) {
            return new Pair<Boolean, Integer>(false, 0);
        }
        Point low_pt = slidePt(c, d_least, low);
        Point up_pt = slidePt(c, d_least, up);
        if (!((ictr_c.insideForbidden(low_pt)) && (ictr_c.insideForbidden(up_pt)))) {
            throw new SolverException("GeometricKernel:FindBoxInterIn():invariant2");
        }
        if (!(min(up, ictr_c.maximizeSizeOfFBox(increase, d_least, k, new Region(low_pt)), increase) == up)) {
            throw new SolverException("GeometricKernel:FindBoxInterIn():invariant3");
        }

        //Is low_pt in ictr_c? = low_g
        Point maxl_pt = extend(low_pt, d_prev_least, k, n, ictr_c, increase);
        boolean low_g = ictr_g.insideForbidden(maxl_pt);

        //Is up_pt in ictr_c? = up_g
        Point maxu_pt = extend(up_pt, d_prev_least, k, n, ictr_c, increase);
        boolean up_g = ictr_g.insideForbidden(maxu_pt);

        int mid = -1;
        Point mid_pt;
        Point mid_ext = null;
        boolean mid_g;
        while ((low_g != up_g) && (low < up)) {
            mid = ((low + up) / 2);
            mid_pt = slidePt(c, d_least, mid);

            mid_ext = extend(mid_pt, d_prev_least, k, n, ictr_c, increase);
            mid_g = ictr_g.insideForbidden(mid_ext);

            if (mid_g == low_g) {
                low = mid + 1;
                low_pt = slidePt(c, d_least, low);
                maxl_pt = extend(low_pt, d_prev_least, k, n, ictr_c, increase);
                low_g = ictr_g.insideForbidden(maxl_pt);
            } else {
                up = mid - 1;
                up_pt = slidePt(c, d_least, up);
                maxu_pt = extend(up_pt, d_prev_least, k, n, ictr_c, increase);
                up_g = ictr_g.insideForbidden(maxu_pt);
            }
        }

        if (low >= up) {
            return new Pair<Boolean, Integer>(true, mid_ext.getCoord(d_prev_least));
        }

        return new Pair<Boolean, Integer>(false, 0);

    }

    Region findBoxInter(int d_prune, int d_least, int d_prev_least, int k, Point c, Point g, Point n, boolean increase, ForbiddenRegion ictr_c, ForbiddenRegion ictr_g, int pos_p) {
        if (stp.opt.debug) {
            LOGGER.info("/*debug*/FindBoxInter(d_prune=" + d_prune + ", d_least=" + d_least + ", d_prev_least=" + d_prev_least + ", k=" + k + ", c=" + c + ", g=" + g + ", n=" + n + ", increase" + increase + ", ictr_c=" + ictr_c + ", ictr_g=" + ictr_g + ", pos_p=" + pos_p + ')');
        }
        if (!((increase && (c.getCoord(d_least) <= g.getCoord(d_least))) || ((!increase) && (c.getCoord(d_least) >= g.getCoord(d_least))))) {
            throw new SolverException("GeometricKernel:Region FindBoxInterIn():invariant1");
        }
        int low = 0, up = 0;

        if (increase) {
            low = c.getCoord(d_least);
            up = g.getCoord(d_least);
        } else {
            up = c.getCoord(d_least);
            low = g.getCoord(d_least);
        }

        Pair p = findBoxInterIn(d_least, d_prev_least, k, c, n, increase, ictr_c, ictr_g, low, up);
        boolean b = (Boolean) p.fst;
        int inter = (Integer) p.snd;  //inter is a position in d_prev_least

//        if (!b) {
//            p=FindBoxInterOut(d_prune,d_least,d_prev_least,k,c,increase,ictr_c,ictr_g,pos_p, low, up);
//            b = (Boolean) p.fst; inter = (Integer) p.snd;  //inter is a position in d_prev_least
//        }

        if (b) {
            if (stp.opt.processing) {
                LOGGER.info("\n/*Processing*/intersection(" + d_prune + ',' + inter + ");");
            }
            //if ((d_prev_least!=d_prune) && (k==2)) {LOGGER.info("GeometricKernel:FindBoxInter():invariant"); }
            Region rbox = getGreedyBoxFromJumpVector(d_prev_least, d_prune, k, c, n, inter, ictr_c, increase, pos_p);
            rbox.father = "FindBoxInter";
            if (stp.opt.debug) {
                LOGGER.info("/*debug*/FindBoxInter() returns " + rbox);
            }
            return rbox;
        }
        if (stp.opt.debug) {
            LOGGER.info("/*debug*/FindBoxInter() returns null");
        }
        return null;
    }


//    Pair<Boolean,Integer> FindBoxInterOut(int d_prune, int d_least, int d_prev_least, int k, Point c, boolean increase, ForbiddenRegion ictr_c, ForbiddenRegion ictr_g, int pos_p, int low, int up) {
//        if (increase) { up=low; low=c.getCoord(d_least); } else {low=up; up=c.getCoord(d_least);  }
//        Region low_box= new Region(c,0);
//        low_box.setMinimumBoundary(d_least,low);
//        low_box.setMaximumBoundary(d_least,low);
//        int maxl=ictr_c.maximizeSizeOfFBox(increase,d_prev_least,k,low_box);
//        Region up_box= new Region(c,0);
//        up_box.setMinimumBoundary(d_least,up);
//        up_box.setMaximumBoundary(d_least,up);
//        int maxu=ictr_c.maximizeSizeOfFBox(increase,d_prev_least,k,up_box);
//
//        Point maxl_pt = low_box.pointMin();
//        maxl_pt.setCoord(d_prev_least,maxl);
//        boolean low_g = ictr_g.insideForbidden(maxl_pt);
//        Point maxu_pt = up_box.pointMin();
//        maxl_pt.setCoord(d_prev_least,maxu);
//        boolean up_g = ictr_g.insideForbidden(maxu_pt);
//
//        if ((low_g==up_g)) return new Pair(false,0);
//        do {
//            /*invariant*/ if (!(low_g!=up_g)) {LOGGER.info("GometricalKernel:FinBoxInterOut():invariant"); System.exit(-1); };
//            int mid=(low+up)/2;
//            if (mid==low)
//                if (low_g) up=low; else low=up;
//            else  {
//                Region mid_box= new Region(c,0);
//                mid_box.setMinimumBoundary(d_least,mid);
//                mid_box.setMaximumBoundary(d_least,mid);
//                int mid_ext=ictr_c.maximizeSizeOfFBox(increase,d_prev_least,k,mid_box);
//                Point mid_pt = mid_box.pointMin();
//                mid_pt.setCoord(d_prev_least,mid_ext);
//                boolean mid_g = ictr_g.insideForbidden(mid_pt);
//                if (mid_g)
//                    if (low_g) low=mid; else up=mid;
//                else
//                    if (low_g) up=mid; else low=mid;
//            }
//        } while (low<up);
//
//        low_box= new Region(c,0);
//        low_box.setMinimumBoundary(d_least,low);
//        low_box.setMaximumBoundary(d_least,low);
//        int result=ictr_c.maximizeSizeOfFBox(increase,d_prev_least,k,low_box);
//
//        Point intersection_pt=new Point(c);
//        intersection_pt.setCoord(d_prev_least,result);
//        intersection_pt.setCoord(d_least,low);
//        boolean in_ictr_c = ictr_c.insideForbidden(intersection_pt);
//        boolean in_ictr_g = ictr_g.insideForbidden(intersection_pt);
//
//        if (!(in_ictr_c && in_ictr_g))
//            return new Pair(false,result);
//
//        return new Pair<Boolean,Integer>(true,result);
//
//    }


    @SuppressWarnings({"unchecked"})
    List longestCommonInterval(int d, int k, Point p, ForbiddenRegion ictr, ForbiddenRegion ictr2, Point jump, boolean increase) {
        if (stp.opt.debug) {
            LOGGER.info("/*debug*/LongestCommonInterval(d=" + d + ", k=" + k + ", p=" + p + ", ictr=" + ictr + ", ictr2=" + ictr2 + ", jump=" + jump + ", increase=" + increase + ')');
        }
        List result = new ArrayList(3);
        boolean inIctr = ictr.insideForbidden(p);
        boolean inIctr2 = ictr2.insideForbidden(p);
        Point min, max;

        if (inIctr && inIctr2) {
            max = extend2(p, d, k, jump, ictr, ictr2, increase);
            min = new Point(p);
            result.add(true);
            result.add(min);
            result.add(max);
            if (stp.opt.debug) {
                LOGGER.info("/*debug*/LongestCommonInterval() returns " + result);
            }
            return result;
        }
        ForbiddenRegion startingIctr;
        ForbiddenRegion otherIctr;
        if (inIctr) {
            startingIctr = ictr;
            otherIctr = ictr2;
        } else {
            startingIctr = ictr2;
            otherIctr = ictr;
        }

        max = extend(p, d, k, jump, startingIctr, increase);
        min = new Point(max);
        if (otherIctr.insideForbidden(max)) {
            Region box = new Region(min);
            min.setCoord(d, max(p.getCoord(d), otherIctr.maximizeSizeOfFBox(!increase, d, k, box), increase));
        } else {
            result.add(false);
            result.add(min);
            result.add(max);
            if (stp.opt.debug) {
                LOGGER.info("/*debug*/LongestCommonInterval() returns " + result);
            }
            return result;
        }

        result.add(true);
        result.add(min);
        result.add(max);
        if (stp.opt.debug) {
            LOGGER.info("/*debug*/LongestCommonInterval() returns " + result);
        }
        return result;
    }

    boolean infeasibleTriangle(Point a, Point b, Point c, ForbiddenRegion ictr, int k) {
        if (stp.opt.debug) {
            LOGGER.info("/*debug*/InfeasibleTriangle(a=" + a + ", b=" + b + ", c=" + c + ", ictr=" + ictr + ", k=" + k + ')');
        }
        if (ictr instanceof DistGeqIC) {
            DistGeqIC dgeqic = (DistGeqIC) ictr;
            boolean r = (
                    dgeqic.insideForbidden(a) &&
                            dgeqic.insideForbidden(b) &&
                            dgeqic.insideForbidden(c)
            );
            if (stp.opt.debug) {
                LOGGER.info("/*debug*/InfeasibleTriangle() returns " + r);
            }

            return r;
        }

        if (ictr instanceof DistLeqIC) {
            DistLeqIC dleqic = (DistLeqIC) ictr;
            boolean r = (
                    dleqic.insideForbidden(a) &&
                            dleqic.insideForbidden(b) &&
                            dleqic.insideForbidden(c) &&
                            dleqic.segInsideForbidden(a, b) &&
                            dleqic.segInsideForbidden(b, c) &&
                            dleqic.segInsideForbidden(c, a)
            );
            if (stp.opt.debug) {
                LOGGER.info("/*debug*/InfeasibleTriangle() returns " + r);
            }

            return r;
        }

        throw new SolverException("GeometricKernel:InfeasibleTriangle():could not identify constraint.");
    }

    boolean checkBoxTriangle(Point Pra, Point Pdiag, Point h, Point f, ForbiddenRegion ictr, ForbiddenRegion ictr2) {
        int k = Pra.getCoords().length;
        return (infeasibleTriangle(Pra, Pdiag, f, ictr, k) && infeasibleTriangle(h, Pdiag, f, ictr2, k));
    }

//    Region BuildTriangleBox(Point p1, Point p2) {
//
//        int k=p1.getCoords().length;
//        Point leftBottomCorner = new Point(k);
//        Point rightTopCorner = new Point(k);
//        for (int i=0; i<k; i++) {
//            leftBottomCorner.setCoord(i,Math.min(p1.getCoord(i),p2.getCoord(i)));
//            rightTopCorner.setCoord(i,Math.max(p1.getCoord(i),p2.getCoord(i)));
//        }
//        Region box = new Region(leftBottomCorner,rightTopCorner);
//        box.setType("diagonal");
//        return box;
//    }

    static boolean infeasibleSegment(Point a, Point b, ForbiddenRegion ictr) {
        if (ictr instanceof DistLeqIC) {
            DistLeqIC dleqic = (DistLeqIC) ictr;
            boolean cond1 = (dleqic.segInsideForbidden(a, b));
            return (cond1);
        }

        if (ictr instanceof DistGeqIC) {
            DistGeqIC dgeqic = (DistGeqIC) ictr;
            boolean cond1 = (dgeqic.insideForbidden(a, b));
            return (cond1);
        }

        throw new SolverException("GeometricKernel:InfeasibleTriangle():could not identify constraint.");
    }


//    Pair<Boolean,Region> FindBoxTriangle(int d_prune, int d_least, int d_prev_least, int k, Point Pra, Point Pdiag, Point n, boolean increase, ForbiddenRegion ictr_pradiag, ForbiddenRegion ictr_pdiag, int pos_p) {
//        //Invariant
//        if ((!(ictr_pradiag.segInsideForbidden(Pra))) || (!(ictr_pradiag.segInsideForbidden(Pdiag))) || (!(ictr_pdiag.segInsideForbidden(Pdiag)))) {
//            LOGGER.info("Precondition of GeometricKernel.FindBoxTriangle() not verified."); System.exit(-1);
//        }
//        //End Invariant
//        Point h = Extend(Pdiag,d_prev_least,k,n,ictr_pdiag,increase);
//
//        //Compute f
//        Point f;
//        if (ictr_pdiag.segInsideForbidden(Pra))
//            f=Extend2(Pra,d_prev_least,k,h,ictr_pradiag,ictr_pdiag,increase);
//        else { //not in ictr_pdiag, please try to extend back; why?
//            f=Extend(Pra,d_prev_least,k,h,ictr_pradiag,increase);//f is bounded by h
//            if (ictr_pdiag.segInsideForbidden(f))
//                f.setCoord(d_prev_least, ictr_pdiag.maximizeSizeOfFBox(!increase,d_prev_least,k,new Region(f)));
//            else return new Pair(false,null);
//        }
//
//        //Invariant
//        boolean inv_1 = ((increase) && (f.getCoord(d_prev_least)<=h.getCoord(d_prev_least)));
//        boolean inv_2 = ((!increase) && (f.getCoord(d_prev_least)>=h.getCoord(d_prev_least)));
//
//        if (!(inv_1 || inv_2)) {
//            LOGGER.info("d_prev_least:"+d_prune);
//            LOGGER.info("increase:"+increase);
//            LOGGER.info("f:"+f);
//            LOGGER.info("h:"+h);
//            LOGGER.info("Second invariant of GeometricKernel.FindBoxTrianle() not verified."); System.exit(-1);
//        }
//        //End invariant
//
//        int t1 = h.getCoord(d_prev_least);
//        int t2 = f.getCoord(d_prev_least);
//        int m;
//        if (increase) m=Math.min(t1,t2); else m=Math.max(t1,t2);
//        h.setCoord(d_prev_least,m);
//
//
//        //Invariant
//        if ((!(ictr_pdiag.segInsideForbidden(h))) || (!(ictr_pradiag.segInsideForbidden(f))) || (!(ictr_pdiag.segInsideForbidden(f)))) {
//            LOGGER.info("Third Invariant of GeometricKernel.FindBoxTrianle() not verified."); System.exit(-1);
//        }
//        //End Invariant
//
//        Region box;
//        LOGGER.info("/*Processing*/diagonal("+Pdiag.getCoord(0)+","+Pdiag.getCoord(1)+","+f.getCoord(0)+","+f.getCoord(1)+");");
//        if (InfeasibleTriangle(Pra, Pdiag,f,ictr_pradiag) && InfeasibleTriangle(h, Pdiag,f, ictr_pdiag)) {
//            if (increase) box = new Region(Pra,h); else box = new Region(h, Pra);
//            return new Pair(true,box);
//        }
//
//        return new Pair(false,null);
//    }


    Region findBoxTriangleDicho1(int d_prune, int d_dicho_ext, int d_dicho_int, int k, Point Pra, Point Pdiag, Point n,
                                 boolean increase, ForbiddenRegion ictr_pradiag, ForbiddenRegion ictr_pdiag, int pos_p) {
        if (stp.opt.debug) {
            LOGGER.info("/*debug*/FindBoxTriangleDicho1(d_prune=" + d_prune + ", d_dicho_ext=" + d_dicho_ext + ", d_dicho_int" + d_dicho_int + "=, k=" + k + ", Pra=" + Pra + ", Pdiag=" + Pdiag + ", n=" + n + ", increase=" + increase + ", ictr_pradiag=" + ictr_pradiag + ", ictr_pdiag=" + ictr_pdiag + ", pos_p=" + pos_p + ')');
        }
        //Invariant
        if ((!(ictr_pradiag.insideForbidden(Pra))) || (!(ictr_pradiag.insideForbidden(Pdiag))) || (!(ictr_pdiag.insideForbidden(Pdiag)))) {
            throw new SolverException("Precondition of GeometricKernel.FindBoxTriangleDicho1() not verified. Pra in ictr_pradiag:" + ictr_pradiag.insideForbidden(Pra) + "; Pdiag in ictr_pradiag " + ictr_pradiag.insideForbidden(Pdiag) + "; Pdiag in ictr_pdiag:" + ictr_pdiag.insideForbidden(Pdiag));
        }
        //End Invariant
        boolean found_box = false;
        Point best_found = null;
        Point h = extend(Pdiag, d_dicho_int, k, n, ictr_pdiag, increase);

        List r = longestCommonInterval(d_dicho_int, k, Pra, ictr_pradiag, ictr_pdiag, h, increase);
        boolean found = (Boolean) r.get(0);
        Point low = (Point) r.get(1);
        Point up = (Point) r.get(2);
//        Point initial_low=new Point(low); Point initial_up=new Point(up);

        if (!found) {
            if (stp.opt.debug) {
                LOGGER.info("/*debug*/FindBoxTriangleDicho1() returns null");
            }
            return null;
        }
        String laststr = "";

        do {
//            LOGGER.info("/*Processing*/diagonal("+Pdiag.getCoord(0)+","+Pdiag.getCoord(1)+","+up.getCoord(0)+","+up.getCoord(1)+");/*up*/");
//            LOGGER.info("/*Processing*/diagonal("+Pdiag.getCoord(0)+","+Pdiag.getCoord(1)+","+low.getCoord(0)+","+low.getCoord(1)+");/*low*/");

            Point cut = new Point(h);
            cut.setCoord(d_dicho_int, min(cut.getCoord(d_dicho_int), up.getCoord(d_dicho_int), increase));
            boolean upOk = checkBoxTriangle(Pra, Pdiag, cut, up, ictr_pradiag, ictr_pdiag);
            String upstr = format("FindBoxTriangleDicho1:up:CheckBoxTriangle({0},{1},{2},{3},{4},{5},{6},{7}", Pra, Pdiag, cut, up, ictr_pradiag, ictr_pdiag, d_dicho_int, increase);
            cut = new Point(h);
            cut.setCoord(d_dicho_int, min(cut.getCoord(d_dicho_int), low.getCoord(d_dicho_int), increase));
            boolean lowOk = checkBoxTriangle(Pra, Pdiag, cut, low, ictr_pradiag, ictr_pdiag);
            String lowstr = format("FindBoxTriangleDicho1:low:CheckBoxTriangle({0},{1},{2},{3},{4},{5},{6},{7}", Pra, Pdiag, cut, low, ictr_pradiag, ictr_pdiag, d_dicho_int, increase);
            Point mid = new Point(low);
            mid.setCoord(d_dicho_int, ((low.getCoord(d_dicho_int) + up.getCoord(d_dicho_int)) / 2));
            cut = new Point(h);
            cut.setCoord(d_dicho_int, min(cut.getCoord(d_dicho_int), mid.getCoord(d_dicho_int), increase));
//            LOGGER.info("/*Processing*/diagonal("+Pdiag.getCoord(0)+","+Pdiag.getCoord(1)+","+mid.getCoord(0)+","+mid.getCoord(1)+");/*mid*/");
            boolean midOk = checkBoxTriangle(Pra, Pdiag, cut, mid, ictr_pradiag, ictr_pdiag);
            String midstr = format("FindBoxTriangleDicho1:mid:CheckBoxTriangle({0},{1},{2},{3},{4},{5},{6},{7}", Pra, Pdiag, cut, mid, ictr_pradiag, ictr_pdiag, d_dicho_int, increase);


            if ((!lowOk) && (!midOk) && (!upOk)) {
                break;
            }
            if (upOk) {
                best_found = maxPt(up, best_found, d_dicho_int, found_box, increase);
                found_box = true;
                laststr = upstr;
                break;
            }
            if (midOk) {
                best_found = maxPt(mid, best_found, d_dicho_int, found_box, increase);
                found_box = true;
                low.setCoord(d_dicho_int, mid.getCoord(d_dicho_int) + succ(increase));
                laststr = midstr;
            } else {
                if (lowOk) {
                    best_found = maxPt(low, best_found, d_dicho_int, found_box, increase);
                    found_box = true;
                    up.setCoord(d_dicho_int, mid.getCoord(d_dicho_int) + prev(increase));
                    laststr = lowstr;
                }
            }
        } while (!((increase && (low.getCoord(d_dicho_int) > up.getCoord(d_dicho_int)))
                ||
                ((!increase) && (low.getCoord(d_dicho_int) < up.getCoord(d_dicho_int)))));

        if (found_box) {
            //LOGGER.info("/*Processing*/diagonal("+Pdiag.getCoord(0)+","+Pdiag.getCoord(1)+","+best_found.getCoord(0)+","+best_found.getCoord(1)+");/*best_found*///low:"+initial_low+"up:"+initial_up);

            Region result = buildBox(k, Pdiag, best_found);
            result.setType("diagonal");
            result.father = "FindBoxTriangleDicho1";
            result.info = laststr;
            if (stp.opt.debug) {
                LOGGER.info("/*debug*/FindBoxTriangleDicho1() returns " + result);
            }
            return result;
        }
        if (stp.opt.debug) {
            LOGGER.info("/*debug*/FindBoxTriangleDicho1() returns null");
        }
        return null;
    }

    Region findBoxTriangleDicho2(int d_prune, int d_dicho_ext, int d_dicho_int, int k, Point Pra, Point Pdiag, Point n,
                                 boolean increase, ForbiddenRegion ictr_pradiag, ForbiddenRegion ictr_pdiag, int pos_p) {
        if (stp.opt.debug) {
            LOGGER.info("/*debug*/FindBoxTriangleDicho2(d_prune=" + d_prune + ", d_dicho_ext=" + d_dicho_ext + ", d_dicho_int=" + d_dicho_int + ", k=" + k + ", Pra=" + Pra + ", Pdiag=" + Pdiag + ", n=" + n + ", increase=" + increase + ", ictr_pradiag=" + ictr_pradiag + ", ictr_pdiag=" + ictr_pdiag + ", pos_p=" + pos_p + ')');
        }
        //Invariant
        if ((!(ictr_pradiag.insideForbidden(Pra))) || (!(ictr_pradiag.insideForbidden(Pdiag))) || (!(ictr_pdiag.insideForbidden(Pdiag)))) {
            throw new SolverException("Precondition of GeometricKernel.FindBoxTriangleDicho2() not verified.");
        }
        //End Invariant
        boolean case_a_or_c = (min(Pra.getCoord(d_dicho_ext), Pdiag.getCoord(d_dicho_ext), increase) == Pra.getCoord(d_dicho_ext));
        Point low, up;

        //if (case_a_or_c) {
        low = new Point(Pdiag);
        up = extend(Pdiag, d_dicho_int, k, n, ictr_pdiag, increase);
        //} else {
        //    low = new Point(Pdiag);
        //    up = Extend(Pdiag,d_dicho_int,k,n,ictr_pdiag,increase);
        //}
        boolean found = false;
        Point best_found = null;
        boolean upOk = false, lowOk = false, midOk = false;
        Point h_low, h_mid, h_up;
        String laststr = "";
        do {
            Point mid = new Point(low);
            mid.setCoord(d_dicho_int, (low.getCoord(d_dicho_int) + up.getCoord(d_dicho_int)) / 2);
            //if case A or C
            String upstr, lowstr, midstr = null;
            if (case_a_or_c) {
                h_up = slidePt(Pra, d_dicho_int, up.getCoord(d_dicho_int));
                upOk = (ictr_pradiag.insideForbidden(h_up) && ictr_pdiag.insideForbidden(h_up)
                        && checkBoxTriangle(Pra, Pdiag, up, h_up, ictr_pradiag, ictr_pdiag));
                upstr = format("FindBoxTriangleDicho2.1:up:CheckBoxTriangle({0},{1},{2},{3},{4},{5},{6},{7}", Pra, Pdiag, up, h_up, ictr_pradiag, ictr_pdiag, d_dicho_int, increase);

                h_low = slidePt(Pra, d_dicho_int, low.getCoord(d_dicho_int));
                lowOk = (ictr_pradiag.insideForbidden(h_low) && ictr_pdiag.insideForbidden(h_low)
                        && checkBoxTriangle(Pra, Pdiag, low, h_low, ictr_pradiag, ictr_pdiag));
                lowstr = format("FindBoxTriangleDicho2.1:low:CheckBoxTriangle({0},{1},{2},{3},{4},{5},{6},{7}", Pra, Pdiag, low, h_low, ictr_pradiag, ictr_pdiag, d_dicho_int, increase);

                h_mid = slidePt(Pra, d_dicho_int, mid.getCoord(d_dicho_int));
                midOk = ictr_pradiag.insideForbidden(h_mid);
                midOk = midOk && ictr_pdiag.insideForbidden(h_mid);
                midOk = midOk && checkBoxTriangle(Pra, Pdiag, mid, h_mid, ictr_pradiag, ictr_pdiag);
                midstr = format("FindBoxTriangleDicho2.1:mid:CheckBoxTriangle({0},{1},{2},{3},{4},{5},{6},{7}={8}", Pra, Pdiag, mid, h_mid, ictr_pradiag, ictr_pdiag, d_dicho_int, increase, midOk);

            } else {
                //if case B or D

                Point p;
                h_up = extend(up, d_dicho_ext, k, n, ictr_pdiag, increase);
                p = slidePt(Pra, d_dicho_ext, h_up.getCoord(d_dicho_ext));
                upOk = (ictr_pradiag.insideForbidden(p) && ictr_pradiag.insideForbidden(h_up)
                        && checkBoxTriangle(p, Pdiag, up, h_up, ictr_pradiag, ictr_pdiag));
                upstr = String.format("FindBoxTriangleDicho2.2:up:CheckBoxTriangle(%s,%s,%s,%s,%s,%s,%d,%s", p, Pdiag, up, h_up, ictr_pradiag, ictr_pdiag, d_dicho_int, increase);

                h_low = extend(low, d_dicho_ext, k, n, ictr_pdiag, increase);
                p = slidePt(Pra, d_dicho_ext, h_low.getCoord(d_dicho_ext));
                lowOk = (ictr_pradiag.insideForbidden(p) && ictr_pradiag.insideForbidden(h_low)
                        && checkBoxTriangle(p, Pdiag, low, h_low, ictr_pradiag, ictr_pdiag));
                lowstr = String.format("FindBoxTriangleDicho2.2:low:CheckBoxTriangle(%s,%s,%s,%s,%s,%s,%d,%s", p, Pdiag, low, h_low, ictr_pradiag, ictr_pdiag, d_dicho_int, increase);

                h_mid = extend(mid, d_dicho_ext, k, n, ictr_pdiag, increase);
                p = slidePt(Pra, d_dicho_ext, h_mid.getCoord(d_dicho_ext));
                midOk = (ictr_pradiag.insideForbidden(p) && ictr_pradiag.insideForbidden(h_mid)
                        && checkBoxTriangle(p, Pdiag, mid, h_mid, ictr_pradiag, ictr_pdiag));
                midstr = String.format("FindBoxTriangleDicho2.2:mid:CheckBoxTriangle(%s,%s,%s,%s,%s,%s,%d,%s", p, Pdiag, mid, h_mid, ictr_pradiag, ictr_pdiag, d_dicho_int, increase);

            }

            if ((!lowOk) && (!midOk) && (!upOk)) {
                break;
            }
            if (upOk) {
                best_found = maxPt(h_up, best_found, d_dicho_int, found, increase);
                found = true;
                laststr = upstr;
                break;
            }
            if (midOk) {
                best_found = maxPt(h_mid, best_found, d_dicho_int, found, increase);
                found = true;
                low.setCoord(d_dicho_int, mid.getCoord(d_dicho_int) + succ(increase));
                laststr = midstr;
            } else {
                best_found = maxPt(h_low, best_found, d_dicho_int, found, increase);
                found = true;
                up.setCoord(d_dicho_int, mid.getCoord(d_dicho_int) + prev(increase));
                laststr = lowstr;

            }
        } while (!((increase && (low.getCoord(d_dicho_int) > up.getCoord(d_dicho_int)))
                ||
                ((!increase) && (low.getCoord(d_dicho_int) < up.getCoord(d_dicho_int)))));

        if (found) {
            //LOGGER.info("/*Processing*/diagonal("+Pdiag.getCoord(0)+","+Pdiag.getCoord(1)+","+best_found.getCoord(0)+","+best_found.getCoord(1)+");/*best_found*///low:"+initial_low+"up:"+initial_up);

            Region result = buildBox(k, Pdiag, best_found);
            result.setType("diagonal");
            result.father = "FindBoxTriangleDicho2";
            result.info = laststr;
            result.case_a_or_c = case_a_or_c;
            if (stp.opt.debug) {
                LOGGER.info("/*debug*/FindBoxTriangleDicho2() returns " + result);
            }
            return result;
        }
        if (stp.opt.debug) {
            LOGGER.info("/*debug*/FindBoxTriangleDicho2() returns null");
        }
        return null;
    }

    Region findBoxTriangleDicho(int d_prune, int d_dicho_ext, int d_dicho_int,
                                int k, Point Pra, Point Pdiag, Point n,
                                boolean increase, ForbiddenRegion ictr_pradiag,
                                ForbiddenRegion ictr_pdiag, int pos_p) {

        if (!(ictr_pdiag.insideForbidden(Pdiag))) {
            throw new SolverException("GeometricKernel:FindBoxTriangleDicho():Pdiag not in ictr_pdiag");
        }

        Region b1 = findBoxTriangleDicho1(d_prune, d_dicho_ext, d_dicho_int, k, Pra, Pdiag, n, increase, ictr_pradiag, ictr_pdiag, pos_p);
        Region b2 = findBoxTriangleDicho2(d_prune, d_dicho_ext, d_dicho_int, k, Pra, Pdiag, n, increase, ictr_pradiag, ictr_pdiag, pos_p);
        return selectionCriteria(d_prune, k, increase, b1, b2);
    }


    Region checkTriangleDDicho(int d_prune, int d_dicho_ext, int d_dicho_int, int k, Point Pra, Point Pdiag, Point n,
                               boolean increase, ForbiddenRegion ictr_pradiag, ForbiddenRegion ictr_pdiag, int pos_p, int mid) {
        if (stp.opt.debug) {
            LOGGER.info("/*debug*/CheckTriangleDDicho(d_prune=" + d_prune + ", d_dicho_ext=" + d_dicho_ext + ", d_dicho_int=" + d_dicho_int + ", k=" + k + ", Pra=" + Pra + ", Pdiag=" + Pdiag + ", n=" + n + ", increase=" + increase + ", ictr_pradiag=" + ictr_pradiag + ", ictr_pdiag=" + ictr_pdiag + ", pos_p=" + pos_p + ", mid=" + mid + ')');
        }

        Region box = null;
        ForbiddenRegion ictr = null;
        Point P = null;
        if (min(Pra.getCoord(d_dicho_ext), Pdiag.getCoord(d_dicho_ext), increase) == Pra.getCoord(d_dicho_ext)) {
            P = new Point(Pra);
            Point Qra = slidePt(Pra, d_dicho_ext, mid);
            ictr = ictr_pradiag;


            if (!(ictr_pdiag.insideForbidden(Pdiag))) {
                throw new SolverException("GeometricKernel:CheckTriangleDDicho():Pdiag not in ictr_pdiag for m=2 (2)");
            }
            box = findBoxTriangleDicho(d_prune, d_dicho_ext, d_dicho_int, k, Qra, Pdiag, n, increase, ictr_pradiag, ictr_pdiag, pos_p);
//            if (box!=null) {
//                Region check_box = new Region(Pra);
//                if (increase) {
//                    if (box.getMinimumBoundary(d_dicho_ext)!=Pra.getCoord(d_dicho_ext)) {
//                        check_box.setMaximumBoundary(d_dicho_ext,mid-1);
//                        check_box.setMaximumBoundary(d_dicho_int,ictr_pradiag.maximizeSizeOfFBox(increase,d_dicho_int,k,check_box));
//                        if (check_box.getMaximumBoundary(d_dicho_int)>=box.getMaximumBoundary(d_dicho_int)) {
//                            box.setMinimumBoundary(d_dicho_ext,Pra.getCoord(d_dicho_ext));
//                        } else box=null;
//                    }
//                }
//                else {
//                    if (box.getMaximumBoundary(d_dicho_ext)!=Pra.getCoord(d_dicho_ext)) {
//                        check_box.setMinimumBoundary(d_dicho_ext,mid+1);
//                        check_box.setMinimumBoundary(d_dicho_int,ictr_pradiag.maximizeSizeOfFBox(increase,d_dicho_int,k,check_box));
//                        if (check_box.getMinimumBoundary(d_dicho_int)<=box.getMinimumBoundary(d_dicho_int)) {
//                            box.setMaximumBoundary(d_dicho_ext,Pra.getCoord(d_dicho_ext));
//                        } else box=null;
//                    }
//                }
//            }
        } else {
            P = new Point(Pdiag);
            Point Qdiag = slidePt(Pdiag, d_dicho_ext, mid);
            ictr = ictr_pdiag;
            if (!(ictr_pdiag.insideForbidden(Qdiag))) {
                throw new SolverException("GeometricKernel:CheckTriangleDDicho():Qdiag not in ictr_pdiag for m=2 (2)");
            }
            box = findBoxTriangleDicho(d_prune, d_dicho_ext, d_dicho_int, k, Pra, Qdiag, n, increase, ictr_pradiag, ictr_pdiag, pos_p);
        }

        if (box != null) {
            Region check_box = new Region(P);
            if (increase) {
                if (box.getMinimumBoundary(d_dicho_ext) != P.getCoord(d_dicho_ext)) {
                    check_box.setMaximumBoundary(d_dicho_ext, mid - 1);
                    check_box.setMaximumBoundary(d_dicho_int, ictr.maximizeSizeOfFBox(increase, d_dicho_int, k, check_box));
                    if (check_box.getMaximumBoundary(d_dicho_int) >= box.getMaximumBoundary(d_dicho_int)) {
                        box.setMinimumBoundary(d_dicho_ext, P.getCoord(d_dicho_ext));
                    } else {
                        box = null;
                    }
                }
            } else {
                if (box.getMaximumBoundary(d_dicho_ext) != P.getCoord(d_dicho_ext)) {

                    check_box.setMinimumBoundary(d_dicho_ext, mid + 1);
                    check_box.setMinimumBoundary(d_dicho_int, ictr.maximizeSizeOfFBox(increase, d_dicho_int, k, check_box));
                    if (check_box.getMinimumBoundary(d_dicho_int) <= box.getMinimumBoundary(d_dicho_int)) {
                        box.setMaximumBoundary(d_dicho_ext, P.getCoord(d_dicho_ext));
                    } else {
                        box = null;
                    }
                }
            }
        }
        if (stp.opt.debug) {
            LOGGER.info("/*debug*/CheckTriangleDDicho() returns " + box);
        }
        return box;
    }


    Region findBoxTriangleDDicho(int d_prune, int d_dicho_ext, int d_dicho_int, int k, Point Pra, Point Pdiag, Point n,
                                 boolean increase, ForbiddenRegion ictr_pradiag, ForbiddenRegion ictr_pdiag, int pos_p) {
        if (stp.opt.debug) {
            LOGGER.info("/*debug*/FindBoxTriangleDDicho(int d_prune=" + d_prune + ", int d_dicho_ext=" + d_dicho_ext + ", int d_dicho_int=" + d_dicho_int + ", int k=" + k + ", Point Pra=" + Pra + ", Point Pdiag=" + Pdiag + ", Point n=" + n + ",boolean increase=" + increase + ", ForbiddenRegion ictr_pradiag=" + ictr_pradiag + ", ForbiddenRegion ictr_pdiag=" + ictr_pdiag + ", int pos_p=" + pos_p + ')');
        }

        Pdiag = new Point(Pdiag);//Ensure parameter caller is not modified
        Region best_box = null;
        if (!(ictr_pdiag.insideForbidden(Pdiag))) {
            throw new SolverException("GeometricKernel:FindBoxTriangleDDicho():Pdiag not in ictr_pdiag init");
        }

        Region best_sem = findBoxTriangleDicho(d_prune, d_dicho_ext, d_dicho_int, k, Pra, Pdiag, n, increase, ictr_pradiag, ictr_pdiag, pos_p);
        boolean CASE_A_OR_C = (min(Pra.getCoord(d_dicho_ext), Pdiag.getCoord(d_dicho_ext), increase) == Pra.getCoord(d_dicho_ext));

        boolean minimize = false;


        if (best_sem != null) {
            best_sem.setType("diagonal");
        }
        int low = 0, up = 0;
        int mid;
        for (int m = 1; m <= 2; m++) {
            if (m == 2) {
                if (CASE_A_OR_C) {
                    //NBR=7;F=10000;./run.sh --unarycirclepacking --nbr $NBR --radius 20.0 --factor $F --print --findboxinterout --findboxtriangle --all --maxsol 1 --mixmode --novectorbox  --performance | tail
                    if (!(ictr_pdiag.insideForbidden(Pdiag))) {
                        throw new SolverException("GeometricKernel:FindBoxTriangleDDicho():Pdiag not in ictr_pdiag for m=2aaaaaaa");
                    }
                    Point Pdiag_initial = new Point(Pdiag);
                    int pos = ictr_pdiag.maximizeSizeOfFBox(!increase, d_dicho_ext, k, new Region(Pdiag));
                    //if (Min(Pdiag.getCoord(d_dicho_ext),pos,increase)==Pdiag.getCoord(d_dicho_ext)) {
                    //    LOGGER.info("GeometricKernel:FindBoxTriangleDDicho():Pdiag smaller than extension m=2 increase="+increase+" d_dicho_ext="+d_dicho_ext+" Pra="+Pra+" "+" Pdiaginit:"+Pdiag_initial+" maxSize="+pos);System.exit(-1);
                    //}
                    Pdiag.setCoord(d_dicho_ext, max(Pra.getCoord(d_dicho_ext), pos, increase));
                    if (!(ictr_pdiag.insideForbidden(Pdiag))) {
                        throw new SolverException("GeometricKernel:FindBoxTriangleDDicho():Pdiag not in ictr_pdiag for m=2 increase=" + increase + " d_dicho_ext=" + d_dicho_ext + " Pra=" + Pra + ' ' + " Pdiaginit:" + Pdiag_initial + ' ' + Pdiag + " maxSize=" + ictr_pdiag.maximizeSizeOfFBox(!increase, d_dicho_ext, k, new Region(Pdiag)));
                    }
                }
            }
            //          if (m==1) { if (!(Pdiag_initial.equalTo(Pdiag))) {LOGGER.info("GeometricKernel:FindBoxTriangleDDicho():Pdiag not equal to Pdiag_initial");System.exit(-1);}}
            if ((CASE_A_OR_C) || (m == 1)) {

                mid = -1;
                minimize = false;
                do {
                    minimize ^= true;
                    best_box = null;

                    mid = -1;
                    //if (best_box==null) {
                    if (CASE_A_OR_C) {
                        low = Pra.getCoord(d_dicho_ext);
                        up = Pdiag.getCoord(d_dicho_ext);
                    } else {
                        low = Pdiag.getCoord(d_dicho_ext);
                        up = min(Pra.getCoord(d_dicho_ext),
                                ictr_pdiag.maximizeSizeOfFBox(increase, d_dicho_ext, k, new Region(Pdiag)),
                                increase);
                    }

                    int low_prev = low;
                    int up_prev = up;

                    do {
                        if (!((increase && (low >= low_prev && up <= up_prev)) || (!increase) && (up >= up_prev && low <= low_prev))) {
                            throw new SolverException("GeometricKernel:FindBoxTriangleDDicho():invariant1");
                        }
                        if (!((Math.abs(up_prev - low_prev) >= Math.abs(up - low)))) {
                            throw new SolverException("GeometricKernel:FindBoxTriangleDDicho():invariant2");
                        }
                        low_prev = low;
                        up_prev = up;
                        mid = (low + up) / 2;
                        if (!(ictr_pdiag.insideForbidden(Pdiag))) {
                            throw new SolverException("GeometricKernel:FindBoxTriangleDDicho():Pdiag not in ictr_pdiag for m=2 (2)");
                        }

                        Region box = checkTriangleDDicho(d_prune, d_dicho_ext, d_dicho_int, k, Pra, Pdiag, n, increase, ictr_pradiag, ictr_pdiag, pos_p, mid);


                        if (box != null) {
                            box.setType("diagonal+rect");
                            box.mid = mid;
                            box.dicho_ext = d_dicho_ext;
                            box.dicho_int = d_dicho_int;
                        }

                        //begin invariant
                        //At least one of the two diagonals of the boxes must belong to both constraints
                        if ((box != null) && (box.getType().equals("diagonal+rect"))) {
                            Region cbox = new Region(box);//box to Check
                            if (cbox.mid == -1) {
                                throw new SolverException("GeometricKernel:FinxBoxTriangleDDicho():box.mid is -1." +
                                        "Pra:" + Pra + " Pdiag:" + Pdiag + " increase:" + increase + " CASE_A_OR_C:" + CASE_A_OR_C + " m:" + m + ' ' + cbox + ' ' + cbox.father + ' ' + ictr_pdiag + ' ' + ictr_pradiag);
                            }

                            if (!(cbox.mid >= cbox.getMinimumBoundary(cbox.dicho_ext) && cbox.mid <= cbox.getMaximumBoundary(cbox.dicho_ext))) {
                                throw new SolverException("GeometricKernel:FinxBoxTriangleDDicho():box.mid is outside box. Pra:" + Pra + " Pdiag:" + Pdiag + " increase:" + increase + " CASE_A_OR_C:" + CASE_A_OR_C + " m:" + m + ' ' + cbox + ' ' + cbox.father + " mid:" + cbox.mid + ' ' + ictr_pdiag + ' ' + ictr_pradiag);
                            }

                            Region dbox = new Region(box); //dbox: box containing the diagonal.
                            if (increase) {
                                dbox.setMinimumBoundary(dbox.dicho_ext, mid);
                            } else {
                                dbox.setMaximumBoundary(dbox.dicho_ext, mid);
                            }

                            //d1=[p1,p2] left to right diagonal
                            Point p1 = new Point(2);
                            Point p2 = new Point(2);
                            for (int i = 0; i < k; i++) {
                                if (increase) {
                                    p1.setCoord(i, dbox.getMinimumBoundary(i));
                                } else {
                                    p1.setCoord(i, dbox.getMaximumBoundary(i));
                                }
                            }
                            for (int i = 0; i < k; i++) {
                                if (increase) {
                                    p2.setCoord(i, dbox.getMaximumBoundary(i));
                                } else {
                                    p2.setCoord(i, dbox.getMinimumBoundary(i));
                                }
                            }

                            //d2=[p3,p4] right to left diagonal
                            Point p3 = new Point(2);
                            Point p4 = new Point(2);
                            if (increase) {
                                p3.setCoord(dbox.dicho_ext, dbox.getMinimumBoundary(dbox.dicho_ext));
                                p3.setCoord(dbox.dicho_int, dbox.getMaximumBoundary(dbox.dicho_int));
                                p4.setCoord(dbox.dicho_ext, dbox.getMaximumBoundary(dbox.dicho_ext));
                                p4.setCoord(dbox.dicho_int, dbox.getMinimumBoundary(dbox.dicho_int));
                            } else {
                                p3.setCoord(dbox.dicho_ext, dbox.getMaximumBoundary(dbox.dicho_ext));
                                p3.setCoord(dbox.dicho_int, dbox.getMinimumBoundary(dbox.dicho_int));
                                p4.setCoord(dbox.dicho_ext, dbox.getMinimumBoundary(dbox.dicho_ext));
                                p4.setCoord(dbox.dicho_int, dbox.getMaximumBoundary(dbox.dicho_int));
                            }

                            boolean d1_in_both = ((infeasibleSegment(p1, p2, ictr_pdiag)) && (infeasibleSegment(p1, p2, ictr_pradiag)));
                            boolean d1_in_both_inv = ((infeasibleSegment(p2, p1, ictr_pdiag)) && (infeasibleSegment(p2, p1, ictr_pradiag)));
                            boolean d2_in_both = ((infeasibleSegment(p3, p4, ictr_pdiag)) && (infeasibleSegment(p3, p4, ictr_pradiag)));
                            boolean d2_in_both_inv = ((infeasibleSegment(p4, p3, ictr_pdiag)) && (infeasibleSegment(p4, p3, ictr_pradiag)));

                            if (!(d1_in_both == d1_in_both_inv)) {
                                StringBuilder st = new StringBuilder(128);
                                st.append("GeometricKernel:FinxBoxTriangleDDicho():(!(d1_in_both==d1_in_both_inv))");
                                st.append("Pra:").append(Pra).append(" Pdiag:").append(Pdiag).append(" increase:").append(increase).append(" CASE_A_OR_C:").append(CASE_A_OR_C).append(" m:").append(m).append(" cbox:").append(cbox).append(" dbox:").append(dbox).append(" dicho_ext:").append(dbox.dicho_ext).append(" info:").append(cbox.father).append(" mid:").append(cbox.mid).append(' ').append(ictr_pdiag).append(' ').append(ictr_pradiag);
                                st.append("d1_in_both:").append(d1_in_both).append(" d1_in_both_inv:").append(d1_in_both_inv).append(" d2_in_both:").append(d2_in_both).append(" d2_in_both_inv:").append(d2_in_both_inv);
                                st.append("p1:").append(p1).append(" p2:").append(p2).append(" p3:").append(p3).append(" p4:").append(p4);
                                throw new SolverException(st.toString());
                            }

                            if (!(d2_in_both == d2_in_both_inv)) {
                                StringBuilder st = new StringBuilder(128);
                                st.append("GeometricKernel:FinxBoxTriangleDDicho():(!(d2_in_both==d2_in_both_inv))");
                                st.append("Pra:").append(Pra).append(" Pdiag:").append(Pdiag).append(" increase:").append(increase).append(" CASE_A_OR_C:").append(CASE_A_OR_C).append(" m:").append(m).append(" cbox:").append(cbox).append(" dbox:").append(dbox).append(" dicho_ext:").append(dbox.dicho_ext).append(" info:").append(cbox.father).append(" mid:").append(cbox.mid).append(' ').append(ictr_pdiag).append(' ').append(ictr_pradiag);
                                st.append("d1_in_both:").append(d1_in_both).append(" d1_in_both_inv:").append(d1_in_both_inv).append(" d2_in_both:").append(d2_in_both).append(" d2_in_both_inv:").append(d2_in_both_inv);

                                st.append("p1:").append(p1).append(" p2:").append(p2).append(" p3:").append(p3).append(" p4:").append(p4);
                                st.append("InfeasibleSegment(").append(p3).append(',').append(p4).append(',').append(ictr_pdiag).append(")):").append(infeasibleSegment(p3, p4, ictr_pdiag)).append(" && (InfeasibleSegment(").append(p3).append(',').append(p4).append(',').append(ictr_pradiag).append("):").append(infeasibleSegment(p3, p4, ictr_pradiag)).append(')');
                                st.append("InfeasibleSegment(").append(p4).append(',').append(p3).append(',').append(ictr_pdiag).append(")):").append(infeasibleSegment(p4, p3, ictr_pdiag)).append(" && (InfeasibleSegment(").append(p4).append(',').append(p3).append(',').append(ictr_pradiag).append("):").append(infeasibleSegment(p4, p3, ictr_pradiag)).append(')');
//                                boolean b;
                                st.append('1');
//                                b=(InfeasibleSegment(p3,p4,ictr_pradiag));
                                st.append('2');
//                                b=(InfeasibleSegment(p4,p3,ictr_pradiag));
                                throw new SolverException(st.toString());
                            }

                            if (!(d1_in_both || d2_in_both)) {
                                StringBuilder st = new StringBuilder(128);
                                st.append("GeometricKernel:FinxBoxTriangleDDicho():(!(d1_in_both || d2_in_both))");
                                st.append("Pra:").append(Pra).append(" Pdiag:").append(Pdiag).append(" increase:").append(increase).append(" CASE_A_OR_C:").append(CASE_A_OR_C).append(" m:").append(m).append(" cbox:").append(cbox).append(" dbox:").append(dbox).append(" dicho_ext:").append(dbox.dicho_ext).append(" info:").append(cbox.info).append(" father:").append(cbox.father).append(" mid:").append(cbox.mid).append(' ').append(ictr_pdiag).append(' ').append(ictr_pradiag);
                                st.append("d1_in_both:").append(d1_in_both).append(" d1_in_both_inv:").append(d1_in_both_inv).append(" d2_in_both:").append(d2_in_both).append(" d2_in_both_inv:").append(d2_in_both_inv);

                                st.append("p1:").append(p1).append(" p2:").append(p2).append(" p3:").append(p3).append(" p4:").append(p4);
                                throw new SolverException(st.toString());
                            }


                        }
                        //end invariant

                        if (box != null) {
                            best_box = selectionCriteria(d_prune, k, increase, best_box, box);
                        }
                        if ((box != null) == minimize) {
                            up = mid + prev(increase);
                            //up=mid-1;
                        } else {
                            low = mid + succ(increase);
                            //low=mid+1;
                        }
                    } while (!(((increase && low > up) || ((!increase) && low < up))));
                    best_sem = selectionCriteria(d_prune, k, increase, best_sem, best_box);
                    //break;
                } while (minimize);
            }
        }
        //best_sem=BestVolume2(d_prune,d_prev_least,d_least,k,increase,best_sem,best_box);
        //Region BestVolume2(int d_prune,int d_prev_least, int d_least, int k, boolean increase, Region box, Region best_box) {


        //}
//        if (best_sem!=null) {
//            best_sem.setType("diagonal+rect");
//            best_sem.mid=mid;
//            best_sem.dicho_ext=d_dicho_ext;
//        }

//        Pdiag=Pdiag_initial;
        if (best_sem != null) {
            best_sem.dicho_ext = d_dicho_ext;
            best_sem.dicho_int = d_dicho_int;

            if (CASE_A_OR_C) {
                best_sem.orientation = 0;
            } else {
                best_sem.orientation = 1;
            }
            best_sem.case_a_or_c = CASE_A_OR_C;
        }
        if (stp.opt.debug) {
            LOGGER.info("/*debug*/FindBoxTriangleDDicho() returns " + best_sem);
        }

        return best_sem;
    }


//    boolean cross(boolean increase,int elow_c, int elow_g, int eup_c, int eup_g) {
//        if (((increase) && ((elow_c>=elow_g) || (eup_c>=eup_g)) && ((elow_g>=elow_c) ||(eup_g>=eup_c))) ||
//           (((!increase) && ((elow_c<=elow_g) || (eup_c<=eup_g)) && ((elow_g<=elow_c) ||(eup_g<=eup_c)))))
//        return true; else return false;
//    }


    static boolean feasiblePtInRegion() { //Is there any point in f which is Feasible?
//        Point scan = f.pointMin();
//        int k=scan.getCoords().length;
//        int curcounter=k-1; //by convention, we start with the greastest index.
//        while (curcounter!=-1) { //means there was a reset on dimension 0, hence we have seen all values.
//            //Invariant: scan is in the Region.
//
//            scan.setCoord(curcounter,scan.getCoord(curcounter)+1);//increment the current dimension
//
//            if (scan.getCoord(curcounter)==f.getMaximumBoundary(curcounter)+1) { //dim. exceeded max, reset it
//                scan.setCoord(curcounter,f.getMinimumBoundary(curcounter)); //reset
//                curcounter--; //next increment will be made on the preceding dimension
//                LOGGER.info(scan+" ");
//            }
//            else { //There was an increment without reset; this means we have a new point never explored
//                curcounter=k-1; //number on the least dimension
//
//                if (isFeasible(scan,ictrs)) return true; //check if point is feasible for all the internal constraints
//            }
//        }
        return false;
    }

    // 7.7.5. Sweep Algorithm Dealing with Curved Forbidden Regions

    static boolean nextPtIsFree(Point p, int d, Obj o, List<InternalConstraint> ictrs, boolean increase) {
        Point g_prime = new Point(p);
        g_prime.setCoord(d, g_prime.getCoord(d) + succ(increase));
        return (o.isInside(g_prime)) && (isFeasible(g_prime, ictrs));
    }

    Pair<Boolean, Region> getDeltaFR(int d_prune, int k, Obj o, Point c, Point n, List<InternalConstraint> ictrs, boolean increase, int delta_prune, int mode) {
        //stp.opt.state_FR=0 normal state
        //stp.opt.state_FR=1 trashing
        //stp.opt.state_FR=2 try to exit
        boolean infeasible;
        Region b;
        if ((mode == 1) || (stp.opt.singleboxonly)) {
            Pair<Boolean, Region> p = getDeltaFRSingle(d_prune, k, o, c, n, ictrs, increase, delta_prune);
            infeasible = p.fst;
            b = p.snd;
            if (!infeasible) {
                return p;
            }


        } else {
            Pair<Boolean, Region> p = getDeltaFRMultiple(d_prune, k, o, c, n, ictrs, increase, delta_prune);
            infeasible = p.fst;
            b = p.snd;
            if (!infeasible) {
                return p;
            }

            Point c_prime = new Point(c);
            int d_least = (k - 1 + d_prune) % k;
            if (increase) {
                c_prime.setCoord(d_least, b.getMaximumBoundary(d_least) + 1);
            } else {
                c_prime.setCoord(d_least, b.getMinimumBoundary(d_least) - 1);
            }

            Point c_prime_prime = new Point(c);
            if (increase) {
                c_prime_prime.setCoord(d_least, b.getMaximumBoundary(d_least));
            } else {
                c_prime_prime.setCoord(d_least, b.getMinimumBoundary(d_least));
            }


//            boolean cond1 = (b.getMaximumBoundary(d_least) - b.getMinimumBoundary(d_least) >= 2);//box is larger than 1 in d_least
//            boolean cond2 = ((increase && (c_prime.getCoord(d_least) <= o.getCoord(d_least).getSup()))
//                    || ((!increase) && (c_prime.getCoord(d_least) >= o.getCoord(d_least).getInf()))); //ensure c_prime is in the domain
//            boolean cond3 = true;//C_c.size() > 1; //more than one constraint on the current sweep point
            /*boolean cond4 = */setOfCstrsOnPt(c_prime, ictrs)/*.size() == 1*/; //update of c is on only one constraint

            if (
                    (b.getMaximumBoundary(d_least) - b.getMinimumBoundary(d_least) >= 2) &&/*box is larger than 1 in d_least*/
                            ((increase && (c_prime.getCoord(d_least) <= o.getCoord(d_least).getSup()))
                                    || ((!increase) && (c_prime.getCoord(d_least) >= o.getCoord(d_least).getInf()))) && /*ensure c_prime is in the domain*/
                            nbrOfCstrsOnPt(c_prime, ictrs) == 1 &&
                            nbrOfCstrsOnPt(c_prime_prime, ictrs) > 1) {
                //reduce forb box of 1 in d_least dimension, so that c_prime is included in two constraints
                if (increase) {
                    b.setMaximumBoundary(d_least, b.getMaximumBoundary(d_least) - 1);
                } else {
                    b.setMinimumBoundary(d_least, b.getMinimumBoundary(d_least) + 1);
                }
            }

        }

        if (stp.opt.debug) {
            LOGGER.info("best_b:" + b);
            LOGGER.info("/*example*/b:" + b);
        }
        writeBox(b, increase, false);


        //./run.sh --nbr 3 --radius 20.0 --smallradius 20.0 --circlerandom --print --findboxinterout --findboxtriangle --seed 1245717899423 --smallradiusfixed 3.57739868164 --mixmode --all --maxsol 1   --performance  | grep  "^\*\*\*"

        //dicho1 case_a_or_c dicho_ext=0 increase==true
        //*** box:([21798,36687],[21579,26216]);b.father:FindBoxTriangleDicho1;b.mid:35275;b.info:FindBoxTriangleDicho1:up:CheckBoxTriangle((35275,21579),(36687,21579),(36687,26216),(35275,26216),GeqIC(D=18240,q=2,s1=3,s2=1,o1=3,o2=1),LeqIC(D=16423,q=2,s1=3,s2=0,o1=3,o2=0),1,true;b.case_a_or_c:true;b.dicho_ext:0;volume:69059820;c=(21798,21579);increase=true

        //dicho1 case_a_or_c dicho_ext=1 increase==true
        //*** box:([3906,23339],[0,7944]);b.father:FindBoxTriangleDicho2;b.mid:992;b.info:FindBoxTriangleDicho2.1:up:CheckBoxTriangle((3906,992),(3906,7944),(23339,7944),(23339,992),LeqIC(D=16423,q=2,s1=3,s2=0,o1=3,o2=0),GeqIC(D=18240,q=2,s1=3,s2=1,o1=3,o2=1),0,true;b.case_a_or_c:true;b.dicho_ext:1;volume:154403130;c=(3906,0);increase=true

        //dicho2 case_a_r_c dicho_ext=0 increase==true
        //*** box:([0,8386],[3577,23265]);b.father:FindBoxTriangleDicho2;b.mid:1047;b.info:FindBoxTriangleDicho2.1:mid:CheckBoxTriangle((1047,3577),(8386,3577),(8386,23265),(1047,23265),LeqIC(D=16423,q=2,s1=3,s2=0,o1=3,o2=0),GeqIC(D=18240,q=2,s1=3,s2=1,o1=3,o2=1),1,true=true;b.case_a_or_c:true;b.dicho_ext:0;volume:165131643;c=(0,3577);increase=true

        //dicho2 case_a_r_c dicho_ext=1 increase==true
        //*** box:([15581,23535],[0,4181]);b.father:FindBoxTriangleDicho2;b.mid:1045;b.info:FindBoxTriangleDicho2.1:mid:CheckBoxTriangle((15581,1045),(15581,4182),(23535,4182),(23535,1045),LeqIC(D=16423,q=2,s1=3,s2=0,o1=3,o2=0),GeqIC(D=18240,q=2,s1=3,s2=1,o1=3,o2=1),0,true=true;b.case_a_or_c:true;b.dicho_ext:1;volume:33267810;c=(15581,0);increase=true

        //dicho1 !case_a_or_c dicho_ext=0 increase==true
        //none
        //dicho1 !case_a_or_c dicho_ext=1 increase==true
        //none
        //dicho2 !case_a_or_c dicho_ext=0 increase==true
        //*** box:([24008,36747],[4073,16071]);b.father:FindBoxTriangleDicho2;b.mid:29679;b.info:FindBoxTriangleDicho2.2:up:CheckBoxTriangle((36747,4073),(29679,4073),(29679,16071),(36747,16071),LeqIC(D=16423,q=2,s1=3,s2=0,o1=3,o2=0),GeqIC(D=18240,q=2,s1=3,s2=1,o1=3,o2=1),1,true;b.case_a_or_c:false;b.dicho_ext:0;volume:152867260;c=(24008,4073);increase=true
        //dicho2 !case_a_or_c dicho_ext=1 increase==true
        //*** box:([3906,15580],[29314,36602]);b.father:FindBoxTriangleDicho2;b.mid:29314;b.info:FindBoxTriangleDicho2.2:mid:CheckBoxTriangle((3906,36602),(3906,29314),(15580,29314),(15580,36602),LeqIC(D=16423,q=2,s1=3,s2=0,o1=3,o2=0),GeqIC(D=18240,q=2,s1=3,s2=1,o1=3,o2=1),0,true;b.case_a_or_c:false;b.dicho_ext:1;volume:85099075;c=(3906,29314);increase=true
        //greedy
        //*** box:([0,3899],[3900,40000]);b.father:GreedyPoint;b.mid:-1;b.info:;b.case_a_or_c:false;b.dicho_ext:-1;volume:140793900;c=(0,3900);increase=true
        //*** box:([29282,36100],[11890,28106]);b.father:GreedyPoint;b.mid:-1;b.info:;b.case_a_or_c:false;b.dicho_ext:-1;volume:110583723;c=(36100,28106);increase=false
        //vector
        //(21798,15683), fait le point entre deux diagonales

        //inter
        //./run.sh --nbr 3 --radius 20.0 --smallradius 20.0 --circlerandom --print --findboxinterout --findboxtriangle --seed 1245718173171 --smallradiusfixed 9.56355285645 --mixmode --all --maxsol 1 --performance
        //*** box:([11889,15672],[13431,18999]);b.father:FindBoxInter;b.mid:-1;b.info:;b.case_a_or_c:false;b.dicho_ext:-1;volume:21073096;c=(15672,18999);increase=false

        //vector
        //./run.sh --nbr 3 --radius 20.0 --smallradius 10.0 --circlerandom --print --findboxinterout --findboxtriangle --seed 1245713179703 --smallradiusfixed 11.4625793457 --mixmode --all --maxsol 1 --performance
        //*** tmpbox:([15438,28538],[28539,40000]);b.father:GreedyVector;b.mid:-1;b.info:;b.case_a_or_c:false;b.dicho_ext:-1;volume:150163662;c=(28538,40000);increase=false
        //*** box:([15438,28538],[28539,40000]);b.father:GreedyVector;b.mid:-1;b.info:;b.case_a_or_c:false;b.dicho_ext:-1;volume:150163662;c=(28538,40000);increase=false


        //if ((b.father.indexOf("Dicho2")!=-1) && (!b.case_a_or_c) && (b.dicho_ext==1) && (increase==true) ) {
        if (b.father.indexOf("GreedyPoint") != -1) {
            if (stp.opt.debug) {
                LOGGER.info("*** box:" + b + ";b.father:" + b.father + ";b.mid:" + b.mid + ";b.info:" + b.info + ";b.case_a_or_c:" + b.case_a_or_c + ";b.dicho_ext:" + b.dicho_ext + ";volume:" + b.volume() + ";c=" + c + ";increase=" + increase);
            }
        }


        return new Pair<Boolean, Region>(true, b);
    }

    Pair<Boolean, Region> getDeltaFRSingle(int d_prune, int k, Obj o, Point c, Point n, List<InternalConstraint> ictrs, boolean increase, int delta_prune) {
        if (stp.opt.processing) {
            LOGGER.info("\n/*Processing*/sweep_point(" + c.getCoord(0) + ',' + c.getCoord(1) + ");");
        }
        int d_least = (k - 1 + d_prune) % k;
        if (d_least < 0) {
            d_least = d_least + k;
        }
        int d_prev_least = (k - 2 + d_prune) % k;
        if (d_least < 0) {
            d_prev_least = d_prev_least + k;
        }
        Region best_greedy = null;
        int pos_p = 0;

        List<ForbiddenRegion> C_c = setOfCstrsOnPt(c, ictrs);
        if (stp.opt.debug) {
            LOGGER.info("Set Of Cstrs On c:");
            for (ForbiddenRegion fr : C_c) {
                LOGGER.info(fr + " ");
            }
        }
        if (C_c.isEmpty()) {
            return new Pair<Boolean, Region>(false, null);
        }

        for (ForbiddenRegion ictr_c : C_c) {
            Point g = extend(c, d_least, k, n, ictr_c, increase);
            if (nextPtIsFree(g, d_least, o, ictrs, increase)) {
                Region box = buildBox(k, c, g);
                box.setType("single");
                //writeBox(box,increase,false);
                return new Pair<Boolean, Region>(true, box);
            }

            Region box_greedy = getGreedyBoxFromPoint(d_prune, k, c, n, ictr_c, increase, pos_p);
            if (box_greedy != null) {
                box_greedy.setType("single");
            }
            writeBox(box_greedy, increase, true);
            best_greedy = selectionCriteria(d_prune, k, increase, box_greedy, best_greedy);
        }
        //writeBox(best_greedy,increase,false);
        if (stp.opt.debug) {
            LOGGER.info("/*example*/returns (true," + best_greedy + ')');
        }

        return new Pair<Boolean, Region>(true, best_greedy);
    }

    Pair<Boolean, Region> getDeltaFRMultiple(int d_prune, int k, Obj o, Point c, Point n, List<InternalConstraint> ictrs, boolean increase, int delta_prune) {
        if (stp.opt.debug) {
            LOGGER.info("/*debug*/GetDeltaFRMultiple(d_prune=" + d_prune + ", k=" + k + ", o=" + o + ", c=" + c + ", n=" + n + ", ictrs=" + ictrs + ", increase=" + increase + ", delta_prune=" + delta_prune + ')');
        }

        if (stp.opt.processing) {
            LOGGER.info("\n/*Processing*/sweep_point(" + c.getCoord(0) + ',' + c.getCoord(1) + ");");
        }

        List<ForbiddenRegion> C_c = setOfCstrsOnPt(c, ictrs);
        if (stp.opt.debug) {
            LOGGER.info("Set Of Cstrs On c:");
            for (ForbiddenRegion fr : C_c) {
                LOGGER.info(fr + " ");
            }
        }
        if (C_c.isEmpty()) {
            if (stp.opt.debug) {
                LOGGER.info("/*debug*/GetDeltaFRMultiple() returns (" + false + ",null)");
            }
            return new Pair<Boolean, Region>(false, null);

        }

        int d_least = (k - 1 + d_prune) % k;
        if (d_least < 0) {
            d_least = d_least + k;
        }
        int d_prev_least = (k - 2 + d_prune) % k;
        if (d_least < 0) {
            d_prev_least = d_prev_least + k;
        }
        Region best_box = null;
        int pos_p = 0;


        for (ForbiddenRegion ictr_c : C_c) {
            Point g = extend(c, d_least, k, n, ictr_c, increase);
            if (nextPtIsFree(g, d_least, o, ictrs, increase)) {
                Region box = buildBox(k, c, g);
                box.setType("single");
                //writeBox(box,increase,false);
                if (stp.opt.debug) {
                    LOGGER.info("/*debug*/GetDeltaFRMultiple() returns (true" + box + ')');
                }
                return new Pair<Boolean, Region>(true, box);
            }
            Point gpl = extend(c, d_prev_least, k, n, ictr_c, increase);
            List<ForbiddenRegion> C_g = setOfCstrsOnPt(g, ictrs);
            if ((C_g.size() > 1) && stp.opt.firstTimeGetDeltaFR) {
                stp.opt.firstTimeGetDeltaFR = false;
                LOGGER.info("Relevant.");
            }

            for (ForbiddenRegion ictr_g : C_g) {
                if (ictr_g != ictr_c) {

                    Region box_inter = null;
                    if (stp.opt.useinterbox) {
                        box_inter = findBoxInter(d_prune, d_least, d_prev_least, k, c, g, n, increase, ictr_c, ictr_g, pos_p);
                    }
                    if (box_inter != null) {
                        box_inter.setType("inter_in");
                    }

                    //best_sem = LargestInvLexBox(d_prune,k,increase,best_sem,box_inter);
                    writeBox(box_inter, increase, true);

                    best_box = selectionCriteria(d_prune, k, increase, best_box, box_inter);

//                    if (/*(old_best_box!=null) &&*/ (box_inter!=null) && (best_box==box_inter)){// && (box_inter.volume() > old_best_box.volume())) {
//                        if (box_inter.father.indexOf("GreedyVector")!=-1) {
//                            LOGGER.info("*** tmpbox:"+box_inter+";b.father:"+box_inter.father+";b.mid:"+box_inter.mid+";b.info:"+box_inter.info+";b.case_a_or_c:"+box_inter.case_a_or_c+";b.dicho_ext:"+box_inter.dicho_ext+";volume:"+box_inter.volume()+";c="+c+";increase="+increase);
//                        }
//                    }


                    //best_sem = BestVolume2(d_prune,d_prev_least,d_least,k,increase,best_sem,box_inter);
                    //best_sem = BestVolume2(d_prune,d_prev_least,d_least,k,increase,box_inter,best_sem);

                    if (stp.opt.processing) {
                        LOGGER.info("/*Processing*///Dicho1");
                    }
                    if (!(ictr_g.insideForbidden(g))) {
                        throw new SolverException("GeometricKernel:GetBestFR():g not in ictr_g");
                    }

                    Region box_tri = findBoxTriangleDDicho(d_prune, d_least, d_prev_least, k, c, g, n, increase, ictr_c, ictr_g, pos_p);
//                    if ((box_tri!=null) &&(box_tri.father.indexOf("Dicho1")!=-1) && (!box_tri.case_a_or_c) && (box_tri.dicho_ext==0) && (increase==true)) {
//                        LOGGER.info("*** tmpbox:"+box_tri+";box_tri.father:"+box_tri.father+";box_tri.mid:"+box_tri.mid+";box_tri.info:"+box_tri.info+";box_tri.case_a_or_c:"+box_tri.case_a_or_c+";box_tri.dicho_ext:"+box_tri.dicho_ext+";volume:"+box_tri.volume()+";c="+c+";increase="+increase);
//                    }

                    writeBox(box_tri, increase, true);
//                    if (box_tri!=null) {
//                        box_tri.setType("diagonal");
//                        LOGGER.info("best_box_tri1:"+box_tri);
//                    }
//                    LOGGER.info("/*Processing*///exit Dicho1");
                    best_box = selectionCriteria(d_prune, k, increase, best_box, box_tri);

                    //best_sem = LargestInvLexBox(d_prune,k,increase,best_sem,box_tri);
                    //best_sem = BestVolume2(d_prune,d_prev_least,d_least,k,increase,best_sem,box_tri);
                    //best_sem = BestVolume2(d_prune,d_prev_least,d_least,k,increase,box_tri,best_sem);

                }
            }//endfor

            List<ForbiddenRegion> C_gpl = setOfCstrsOnPt(gpl, ictrs);//new ArrayList<ForbiddenRegion>();
            if ((C_gpl.size() > 1) && stp.opt.firstTimeGetDeltaFR) {
                stp.opt.firstTimeGetDeltaFR = false;
                LOGGER.info("Relevant.");
            }

            for (ForbiddenRegion ictr_gpl : C_gpl) {
                if (ictr_gpl != ictr_c) {
                    Region box_inter = null;
                    //if (stp.intersection) box_inter = FindBoxInter(d_prune,d_prev_least,d_least,k,c,gpl,n,increase,ictr_c,ictr_gpl,pos_p);
                    box_inter.setType("inter_in");
                    //best_sem = LargestInvLexBox(d_prune,k,increase,best_sem,box_inter);
                    writeBox(box_inter, increase, true);
                    best_box = selectionCriteria(d_prune, k, increase, best_box, box_inter);
                    //best_sem = BestVolume2(d_prune,d_prev_least,d_least,k,increase,best_sem,box_inter);
                    //best_sem = BestVolume2(d_prune,d_prev_least,d_least,k,increase,box_inter,best_sem);
                    if (!(ictr_gpl.insideForbidden(gpl))) {
                        throw new SolverException("GeometricKernel:GetBestFR():gpl not in ictr_gpl");
                    }

                    Region box_tri = findBoxTriangleDDicho(d_prune, d_prev_least, d_least, k, c, gpl, n, increase, ictr_c, ictr_gpl, pos_p);
//                    if ((box_tri!=null) &&(box_tri.father.indexOf("Dicho1")!=-1) && (!box_tri.case_a_or_c) && (box_tri.dicho_ext==0) && (increase==true)) {
//                        LOGGER.info("*** tmpbox:"+box_tri+";box_tri.father:"+box_tri.father+";box_tri.mid:"+box_tri.mid+";box_tri.info:"+box_tri.info+";box_tri.case_a_or_c:"+box_tri.case_a_or_c+";box_tri.dicho_ext:"+box_tri.dicho_ext+";volume:"+box_tri.volume()+";c="+c+";increase="+increase);
//                    }

                    writeBox(box_tri, increase, true);
                    best_box = selectionCriteria(d_prune, k, increase, best_box, box_tri);

                    //best_sem = LargestInvLexBox(d_prune,k,increase,best_sem,box_tri);
                    //if box_tri is better than best_sem, take it; otherwise keep best_sem
                    //best_sem = BestVolume2(d_prune,d_prev_least,d_least,k,increase,best_sem,box_tri);
                    //best_sem = BestVolume2(d_prune,d_prev_least,d_least,k,increase,box_tri,best_sem);


                }
            }//endfor.

            for (ForbiddenRegion ictr_c_prime : C_c) {//Point c plays the role of diagonal
                if (ictr_c != ictr_c_prime) {
                    if (stp.opt.processing) {
                        LOGGER.info("/*Processing*///Dicho3");
                    }
                    Region box = findBoxTriangleDDicho(d_prune, d_least, d_prev_least, k, g, c, n, increase, ictr_c, ictr_c_prime, pos_p);
                    writeBox(box, increase, true);
                    best_box = selectionCriteria(d_prune, k, increase, best_box, box);
                    //best_sem = LargestInvLexBox(d_prune,k,increase,best_sem,box);
                    //best_sem = BestVolume2(d_prune,d_prev_least,d_least,k,increase,best_sem,box);
                    //best_sem = BestVolume2(d_prune,d_prev_least,d_least,k,increase,box,best_sem);

                    if (stp.opt.processing) {
                        LOGGER.info("/*Processing*///Dicho4");
                    }
                    if (!(ictr_c_prime.insideForbidden(c))) {
                        throw new SolverException("GeometricKernel:GetBestFR():c not in ictr_c_prime");
                    }

                    box = findBoxTriangleDDicho(d_prune, d_prev_least, d_least, k, gpl, c, n, increase, ictr_c, ictr_c_prime, pos_p);
//                    if ((box!=null) && (box.father.indexOf("Dicho1")!=-1) && (!box.case_a_or_c) && (box.dicho_ext==0) && (increase==true)) {
//                        LOGGER.info("*** tmpbox:"+box+";box.father:"+box.father+";box.mid:"+box.mid+";box.info:"+box.info+";box.case_a_or_c:"+box.case_a_or_c+";box.dicho_ext:"+box.dicho_ext+";volume:"+box.volume()+";c="+c+";increase="+increase);
//                    }

                    writeBox(box, increase, true);
                    best_box = selectionCriteria(d_prune, k, increase, best_box, box);
                    //best_sem = LargestInvLexBox(d_prune,k,increase,best_sem,box);
                    //best_sem = BestVolume2(d_prune,d_prev_least,d_least,k,increase,best_sem,box);
                    //best_sem = BestVolume2(d_prune,d_prev_least,d_least,k,increase,box,best_sem);


                }
            }//endfor


            Region box_greedy = getGreedyBoxFromPoint(d_prune, k, c, n, ictr_c, increase, pos_p);
            if (box_greedy != null) {
                box_greedy.setType("single");
            }
            writeBox(box_greedy, increase, true);

            best_box = selectionCriteria(d_prune, k, increase, best_box, box_greedy);
            //best_greedy = LargestInvLexBox(d_prune,k,increase,best_greedy,box_greedy);
            //best_greedy = BestVolume2(d_prune,d_prev_least,d_least,k,increase,best_greedy,box_greedy);
            //best_greedy = BestVolume2(d_prune,d_prev_least,d_least,k,increase,box_greedy,best_greedy);

            //Inverse  greedy
//            Region box_greedy2 = GetGreedyBoxFromPoint2(d_prune,d_prev_least,d_least,k,c,n,ictr_c,increase,pos_p);
//            if (box_greedy2!=null) box_greedy2.setType("single");
//            best_greedy = BestVolume(d_prune,k,increase,best_greedy,box_greedy2);
//            best_volume = SelectionCriteria(d_prune,k,increase,box_greedy,best_volume);

            //best_volume = BestVolume2(d_prune,d_prev_least,d_least,k,increase,box_greedy,best_volume);
            //best_volume = BestVolume(d_prune,k,i  ncrease,best_volume,box_greedy);

            if ((d_prev_least != d_prune) && (k == 2)) {
                LOGGER.info("GeometricKernel:GetDeltaFRMultiple():invariant (d_prev_least!=d_prune) && (k==2)");
            }

            Region box_vector = null;
            if (stp.opt.usevectorbox) {
                box_vector = getGreedyBoxFromJumpVector(d_prev_least, d_prune, k, c, n, n.getCoord(d_prev_least), ictr_c, increase, pos_p);
            }


            //if ((box_vector!=null) && (Math.abs(box_vector.getMaximumBoundary(d_prev_least)-box_vector.getMinimumBoundary(d_prev_least))<5)) {
            if (box_vector != null) {
                box_vector.setType("vector");
            }
            writeBox(box_vector, increase, true);

            best_box = selectionCriteria(d_prune, k, increase, best_box, box_vector);
//            if ((old_best_box!=null) && (box_vector!=null) && (best_box==box_vector) && (box_vector.volume() > old_best_box.volume())) {
//                if (box_vector.father.indexOf("GreedyVector")!=-1) {
//                    LOGGER.info("*** tmpbox:"+box_vector+";b.father:"+box_vector.father+";b.mid:"+box_vector.mid+";b.info:"+box_vector.info+";b.case_a_or_c:"+box_vector.case_a_or_c+";b.dicho_ext:"+box_vector.dicho_ext+";volume:"+box_vector.volume()+";c="+c+";increase="+increase);
//                }
//            }

            //best_volume = BestVolume2(d_prune,d_prev_least,d_least,k,increase,best_volume,box_vector);
            //best_volume = BestVolume2(d_prune,d_prev_least,d_least,k,increase,box_vector,best_volume);
            //best_volume = BestVolume(d_prune,k,increase,best_volume,box_vector);

            //}

        }//endfor

        if (stp.opt.debug) {
            LOGGER.info("best_box:" + best_box);
        }


//        Region chosen_box=best_volume;
//
//        if (best_sem!=null)
//            chosen_box=SelectionCriteria(d_prune,k,increase,best_greedy,best_sem);
        //chosen_box=LargestInvLexBox(d_prune,k,increase,best_greedy,best_sem);
        //chosen_box=BestVolume2(d_prune,d_prev_least,d_least,k,increase,best_sem,best_greedy);
        //else chosen_box=best_volume;

        //if (stp.opt.debug) writeBox(best_box,increase,false);


        //Alternative shrink code
//        if ((chosen_box.getMaximumBoundary(d_least)-chosen_box.getMinimumBoundary(d_least))>1) {
//            if (increase)
//                chosen_box.setMaximumBoundary(d_least,chosen_box.getMaximumBoundary(d_least)-1);
//            else
//                chosen_box.setMinimumBoundary(d_least,chosen_box.getMinimumBoundary(d_least)+1);
//        }

        if (stp.opt.debug) {
            LOGGER.info("/*debug*/GetDeltaFRMultiple() returns (true," + best_box + ')');
        }
        return new Pair<Boolean, Region>(true, best_box);

    }


//    Pair<Boolean,Region> GetDeltaFROld(int d_prune, int k, Obj o, Point c, Point n, Vector<InternalConstraint> ictrs, boolean increase, int delta_prune) {
//        if (stp.opt.debug) { LOGGER.info("\n/*Processing*/sweep_point("+c.getCoord(0)+","+c.getCoord(1)+");"); };
//        boolean trace=false;
//        Region best_box=null;
//        boolean inter_in=false; boolean inter_out=false;
//
//        Vector<ForbiddenRegion> C_c = SetOfCstrsOnPt(c,k,ictrs);
//        if (C_c.size()==0) return new Pair<Boolean,Region>(false,best_box);
//
//        boolean no_box=true;
//        boolean foundBoxTriangle=false;
//        boolean found_inter=false;
//        boolean found_inter_once=false;
//
//        int d_least = (k-1+d_prune)%k; if (d_least<0) d_least=d_least+k;
//        int d_prev_least = (k-2+d_prune)%k; if (d_least<0) d_prev_least=d_prev_least+k;
//
//        for (ForbiddenRegion ictr_c : C_c) {
//            Region box_c = new Region(c,o.getObjectId());
//            //if (k==3) {
//            //TODO
//            //}
//            Point g = new Point(c);
//            g.setCoord(d_least,ictr_c.maximizeSizeOfFBox(increase,d_least,k,box_c));
//            Point g_prime = new Point(g);
//            if (increase) g_prime.setCoord(d_least,g.getCoord(d_least)+1); else g_prime.setCoord(d_least,g.getCoord(d_least)-1);
//            if ((o.isInside(g_prime)) && (isFeasible(g,ictrs))){
//                Region returned_box = new Region(c);
//                if (increase) returned_box.setMaximumBoundary(d_least,g.getCoord(d_least)); else returned_box.setMinimumBoundary(d_least,g.getCoord(d_least));
//                return new Pair(true,returned_box);
//
//            }
//            Vector<ForbiddenRegion> C_g = SetOfCstrsOnPt(g,k,ictrs);//new ArrayList<ForbiddenRegion>();
////            for (InternalConstraint ictr:ictrs) {
////                if (!(ictr instanceof ForbiddenRegion)) {LOGGER.info("GetDeltatFR():not a ForviddenRegion constraint."); System.exit(-1);}
////                ForbiddenRegion fr = (ForbiddenRegion) ictr;
////                if (fr.segInsideForbidden(g))  C_g.add(fr);
////            }
//
//            if ((C_g.size()>1) && stp.firstTimeGetDeltaFR) { stp.firstTimeGetDeltaFR=false; LOGGER.info("Relevant.");};
//
//
//            for (ForbiddenRegion ictr_g : C_g) {
//                if (ictr_g!=ictr_c) {
//                    Region box_g = new Region(g,o.getObjectId());
//                    int pos_p=0;
//                    //if (k==3) {
//                    //TODO
//                    //}
//                    int last_first = ictr_g.maximizeSizeOfFBox(!increase,d_least,k,box_g);
//                    int low,up;
//                    if (increase) {low=Math.max(last_first,c.getCoord(d_least)); up=g.getCoord(d_least);}
//                    else {up=Math.min(last_first,c.getCoord(d_least)); low=g.getCoord(d_least);};
//                    found_inter=false;int inter=0;
//
//                    Pair <Boolean,Integer> result = FindBoxInterInLength(d_prune,d_least,d_prev_least,k,c,increase,ictr_c,ictr_g,pos_p,low,up);
//                    found_inter=result.fst; if (result.fst) found_inter_once=true; inter=result.snd; inter_in=found_inter;
//
//                    if ((!found_inter) && (stp.findboxinterout)) {
//                        result = FindBoxInterOut(d_prune,d_least,d_prev_least,k,c,increase,ictr_c,ictr_g,pos_p,low,up);
//                        found_inter=result.fst;if (result.fst) found_inter_once=true;inter=result.snd; inter_out=found_inter;
//                    }
//                    //if ((!found_inter) && (stp.findboxtriangle)) {
//                    if (stp.findboxtriangle) {
////                        Pair<Boolean,Region> rresult = FindBoxTriangle(d_prune,d_least,d_prev_least,k,c,g,n,increase,ictr_c,ictr_g,pos_p);
////                        boolean found_triangle=rresult.fst;
////                        if (found_triangle) {
////                            Region new_box=rresult.snd;
////                            if ((no_box) || (CompareAndChooseBox(d_prune,k,increase,new_box,best_box))) {
////                                no_box=false; best_box=new_box; best_box.setType("diagonal");
////                            }
////                        }
//                        //Pair<Boolean,Region> rresult =
//                        Region reg=FindBoxTriangleDicho(d_prune,d_least,d_prev_least,k,c, g,n,increase,ictr_c,ictr_g,pos_p,no_box,best_box);
//                        if (best_box!=reg) {
//                            foundBoxTriangle=true;
//                            if (no_box || LargestInverseLex(d_prev_least,k,increase,reg,best_box))
//                            {no_box=false; best_box=reg;best_box.setType("diagonal"); };
//                        }
////                        no_box=rresult.fst;best_box=rresult.snd;
//
//                    }
//
//                    if (found_inter){
//                        Pair<Boolean,Region> r=BuildInterBox(d_prune,d_least,d_prev_least,k,c,n,increase,ictr_c,pos_p,no_box,best_box,inter);
//                        no_box=r.fst;
//                        if ((best_box!=r.snd)) {
//                            best_box=r.snd;
//                            if (inter_in) best_box.setType("inter_in"); if (inter_out) best_box.setType("inter_out");
//                            if (stp.opt.debug) { LOGGER.info("\n/*Processing*/intersection("+d_prune+","+inter+");"); };
//                        }
//                    }
//                }
//
//            }//endfor
//
//
//            for (ForbiddenRegion ictr_c_prime : C_c) {
//                if (ictr_c!=ictr_c_prime) {
////                    if (stp.opt.findboxtriangle) {
////                        Pair<Boolean,Region> rresult = FindBoxTriangle(d_prune,d_least,d_prev_least,k,g,c,n,increase,ictr_c,ictr_c_prime,0);
////                        boolean found_triangle=rresult.fst;
////                        if (found_triangle) {
////                            Region new_box=rresult.snd;
////                            if ((no_box) || (CompareAndChooseBox(d_prune,k,increase,new_box,best_box))) {
////                                no_box=false; best_box=new_box; best_box.setType("diagonal");
////                            }
////                        }
////                    }
//                    //Pair<Boolean,Region> rresult =
//                    Region reg=FindBoxTriangleDicho(d_prune,d_least,d_prev_least,k,g,c,n,increase,ictr_c,ictr_c_prime,0,no_box,best_box);
//                    if (best_box!=reg) {
//                        foundBoxTriangle=true;
//                        if (no_box || LargestInverseLex(d_prev_least,k,increase,reg,best_box))
//                        {no_box=false; best_box=reg;best_box.setType("diagonal"); };
//                    }
//
//                    //if (best_box!=rresult.snd) foundBoxTriangle=true;
//                    //no_box=rresult.fst;best_box=rresult.snd;
//                }
//            }
//
//        }
//
//    for (ForbiddenRegion ictr_c : C_c) {
//            Pair<Boolean,Region> r;
//
//            Region reg_std=GetGreedyBoxFromPoint(d_prune,k,c,n,ictr_c,increase,delta_prune,no_box,best_box);
//            //no_box=r.fst; best_box=r.snd;
//
//            if ((!foundBoxTriangle) && (!found_inter_once)) {
//                LOGGER.info("/*Processing*/ //not found");
//                Region reg_vector=GetGreedyBoxFromVector(d_prev_least,d_prune,k,c,n,ictr_c,increase,delta_prune,no_box,best_box);
//                if (no_box || GreatestVolume(d_prev_least,k,increase,reg_vector,best_box)) {no_box=false; best_box=reg_vector; best_box.setType("vector"); };
//                if (no_box || GreatestVolume(d_prev_least,k,increase,reg_std,best_box)) {no_box=false; best_box=reg_std; best_box.setType("single");};
//            }
//            else {
//                LOGGER.info("/*Processing*/ //found");
//                if (no_box || LargestInverseLex(d_prev_least,k,increase,reg_std,best_box))
//                {no_box=false; best_box=reg_std;best_box.setType("single"); };
//            }
//        }//endfor
//
//        if (stp.opt.debug) {
//            LOGGER.info("\n/*Processing*/fr("+best_box.getMinimumBoundary(0)+","+best_box.getMaximumBoundary(0)+","+best_box.getMinimumBoundary(1)+","+best_box.getMaximumBoundary(1)+",\""+best_box.getType()+"\");");
//        }
//
////        if ((!foundBoxTriangle) && (!found_inter_once)) {
////            if (best_box.getMaximumBoundary(d_prev_least)-best_box.getMinimumBoundary(d_prev_least)>5) {
////                best_box.setMaximumBoundary(d_prev_least,best_box.getMaximumBoundary(d_prev_least)-5);
////            }
////        }
//
//        return new Pair<Boolean,Region>(true,best_box);
//
//    }


    static boolean compareAndChooseBox(int d_prune, int k, boolean increase, Region box, Region best_box) {
        //if (EqualVolume(d_prune,k,increase,box,best_box))
        return largestInverseLex(d_prune, k, increase, box, best_box);
        //return GreatestVolume(d_prune,k,increase,box,best_box);
    }


    void writeBox(Region chosen_box, boolean increase, boolean temp) {
        if (stp.opt.processing) {
            //if (temp) return;
            String function = "";
            if (temp) {
                function = "fr_temp";
            } else {
                function = "fr";
            }
            if (chosen_box != null) {
                if (!chosen_box.getType().equals("diagonal+rect")) {
                    LOGGER.info("\n/*Processing*/" + function + '(' + chosen_box.getMinimumBoundary(0) + ',' + chosen_box.getMaximumBoundary(0) + ',' + chosen_box.getMinimumBoundary(1) + ',' + chosen_box.getMaximumBoundary(1) + ",\"" + chosen_box.getType() + "\",\"\",0);");
                } else {
                    if (chosen_box.mid == -1) {
                        LOGGER.info("\n/*Processing*/" + function + '(' + chosen_box.getMinimumBoundary(0) + ',' + chosen_box.getMaximumBoundary(0) + ',' + chosen_box.getMinimumBoundary(1) + ',' + chosen_box.getMaximumBoundary(1) + ",\""
                                + "diagonal\",\"\"," + chosen_box.orientation + ");");
                    } else {

                        if (increase) {
                            String info = "";
                            Region box = new Region(chosen_box);
                            box.setMaximumBoundary(box.dicho_ext, box.mid);
                            if (box.father.equals("FindBoxTriangleDicho1")) {
                                if (box.case_a_or_c) {
                                    if (box.dicho_ext == 1) {
                                        info = "1.A";
                                    } else {
                                        info = "1.C";
                                    }
                                } else if (box.dicho_ext == 1) {
                                    info = "1.B";
                                } else {
                                    info = "1.D";
                                }
                            }
                            if (box.father.equals("FindBoxTriangleDicho2")) {
                                if (box.case_a_or_c) {
                                    if (box.dicho_ext == 1) {
                                        info = "2.A";
                                    } else {
                                        info = "2.C";
                                    }
                                } else if (box.dicho_ext == 1) {
                                    info = "2.B";
                                } else {
                                    info = "2.D";
                                }
                            }


                            LOGGER.info("\n/*Processing*/" + function + '(' + box.getMinimumBoundary(0) + ',' + box.getMaximumBoundary(0) + ','
                                    + box.getMinimumBoundary(1) + ',' + box.getMaximumBoundary(1) + ",\""
                                    + "low_diag\",\"" + info + "\"," + chosen_box.orientation + ");");
                            box = new Region(chosen_box);
                            box.setMinimumBoundary(box.dicho_ext, box.mid);
                            LOGGER.info("\n/*Processing*/" + function + '(' + box.getMinimumBoundary(0) + ',' + box.getMaximumBoundary(0) + ','
                                    + box.getMinimumBoundary(1) + ',' + box.getMaximumBoundary(1) + ",\""
                                    + "diagonal\",\"" + info + "\"," + chosen_box.orientation + ");");
                        } else {
                            String info = "";
                            Region box = new Region(chosen_box);
                            box.setMinimumBoundary(box.dicho_ext, box.mid);
                            if (box.father.equals("FindBoxTriangleDicho1")) {
                                if (box.case_a_or_c) {
                                    if (box.dicho_ext == 1) {
                                        info = "1.A";
                                    } else {
                                        info = "1.C";
                                    }
                                } else if (box.dicho_ext == 1) {
                                    info = "1.B";
                                } else {
                                    info = "1.D";
                                }
                            }
                            if (box.father.equals("FindBoxTriangleDicho2")) {
                                if (box.case_a_or_c) {
                                    if (box.dicho_ext == 1) {
                                        info = "2.A";
                                    } else {
                                        info = "2.C";
                                    }
                                } else if (box.dicho_ext == 1) {
                                    info = "2.B";
                                } else {
                                    info = "2.D";
                                }
                            }

                            LOGGER.info("\n/*Processing*/" + function + '(' + box.getMinimumBoundary(0) + ',' + box.getMaximumBoundary(0) + ','
                                    + box.getMinimumBoundary(1) + ',' + box.getMaximumBoundary(1) + ",\""
                                    + "low_diag\",\"" + info + "\"," + chosen_box.orientation + ");");
                            box = new Region(chosen_box);
                            box.setMaximumBoundary(box.dicho_ext, box.mid);
                            LOGGER.info("\n/*Processing*/" + function + '(' + box.getMinimumBoundary(0) + ',' + box.getMaximumBoundary(0) + ','
                                    + box.getMinimumBoundary(1) + ',' + box.getMaximumBoundary(1) + ",\""
                                    + "diagonal\",\"" + info + "\"," + chosen_box.orientation + ");");
                        }
                    }

                }
            }
        }
    }

    Pair<Integer, Integer> dealWithSucc(int d, int last_dprune, int last_diff, int diff_counter, Point c, Point initial_c, int k) {
        int diff = -1;
        if (stp.opt.deltasucc) {
            //D�tecter la dim. la plus importante qui a �t� modif�e.
            int d_current = 0;
            //select the most important dim. that has changed (store it in d_current)
            for (int i = 0; i <= k - 1; i++) {
                d_current = (i + d) % k;
                if (Math.abs(c.getCoord(d_current) - initial_c.getCoord(d_current)) != 0) {
                    break;
                }

            }
            //here d_current is first most important dimension that has been modified


            diff = Math.abs(c.getCoord(d_current) - initial_c.getCoord(d_current));
            if (stp.opt.debug) {
                LOGGER.info("last_diff:" + last_diff + ";diff:" + diff + "d:" + d + ";d_current" + d_current);
            }


            //stp.delta stores the for each diff d the number of diff of size d jumped
            if (stp.opt.delta.get(d_current) == null) {
                stp.opt.delta.put(d_current, new HashMap<Integer, Integer>(16));
            }
            HashMap<Integer, Integer> curDelta = stp.opt.delta.get(d_current);
            if (curDelta.get(diff) == null) {
                curDelta.put(diff, 0);
            }
            curDelta.put(diff, curDelta.get(diff) + 1);


            if (diff != 0) { //if diff exists
                if ((diff == last_diff) && (last_dprune == d_current))  //Preceding diff is the same , on the same dim.
                {
                    diff_counter++;
                } else { //Preceding diff is different, or is the same but on a different dimension

                    if ((last_diff != -1) && (diff_counter != 0) && (last_dprune != -1)) { //There was a sucession on the same dim.
                        //stp.succDelta stores for each diff and for the current pruning dimension d,
                        //the list of the number of time a diff happened, for each diff.
                        if (stp.opt.succDelta.get(last_dprune) == null) {
                            stp.opt.succDelta.put(d, new HashMap<Integer, List<Integer>>(16));
                        }
                        HashMap<Integer, List<Integer>> curSuccDelta = stp.opt.succDelta.get(last_dprune);
                        if (curSuccDelta.get(last_diff) == null) {
                            curSuccDelta.put(last_diff, new ArrayList<Integer>(16));
                        }
                        List<Integer> succ_list = curSuccDelta.get(last_diff);
                        succ_list.add(diff_counter);
                        diff_counter = 0;
                    }

                }

                if (diff == 0) {
                    throw new SolverException("GeometricKernel:DealWithSucc():diff is zero, which should not happen since c and initial_c should always be different in at least one dimesion when AdjustUp is called.");
                }
                last_diff = diff;
                last_dprune = d_current;
            }


        } //if (stp.opt.debug)

        return new Pair<Integer, Integer>(diff, diff_counter);

    }

    @SuppressWarnings({"unchecked"})
    static List checkTrashingState(Point c, int d, int k, int cdpl, Region f, boolean bad_ratio, int nbr_steps, int mode) {
        int d_prev_least = (k - 2 + k + d) % k;
        bad_ratio = bad_ratio || (f.ratio() < 0.1);
        if (cdpl != c.getCoord(d_prev_least)) {
            if (mode == 0) {
                if (!bad_ratio) {
                    nbr_steps = 0;
                } else {
                    nbr_steps = nbr_steps + 1;
                    if (nbr_steps >= 3) {
                        mode = 1;
                        nbr_steps = 0;
                    }
                }
            } else {
                if (mode == 1) {
                    if (!bad_ratio) {
                        mode = 0;
                        nbr_steps = 0;
                    } else {
                        nbr_steps++;
                        if (nbr_steps >= 100) {
                            mode = 2;
                        }
                    }
                } else {
                    if (mode == 2) {
                        nbr_steps = 0;
                        if (bad_ratio) {
                            mode = 1;
                        } else {
                            mode = 0;
                        }
                    }
                }
            }
            bad_ratio = false;
        }
        List r = new ArrayList(3);
        r.add(bad_ratio);
        r.add(nbr_steps);
        r.add(mode);
        return r;
    }

    @SuppressWarnings({"unchecked"})
    static List checkTrashingState_dl(Point c, int d, int k, int cdpl, Region f, boolean bad_ratio, int nbr_steps, int mode) {
        int d_prev_least = (k - 2 + d) % k;
        bad_ratio = (f.ratio() < 0.1);
        if (cdpl != c.getCoord(d_prev_least)) {
            if (mode == 0) {
                if (!bad_ratio) {
                    nbr_steps = 0;
                } else {
                    nbr_steps = nbr_steps + 1;
                    if (nbr_steps >= 3) {
                        mode = 1;
                        nbr_steps = 0;
                    }
                }
            } else {
                if (mode == 1) {
//                    if (!bad_ratio) {
//                        mode=0;nbr_steps=0;
//                    }
//                    else {
                    nbr_steps++;
                    if (nbr_steps >= 100) {
                        mode = 2;
                    }
//                    }
                } else {
                    if (mode == 2) {
                        nbr_steps = 0;
                        if (bad_ratio) {
                            mode = 1;
                        } else {
                            mode = 0;
                        }
                    }
                }
            }
            bad_ratio = false;
        }
        List r = new ArrayList(3);
        r.add(bad_ratio);
        r.add(nbr_steps);
        r.add(mode);
        return r;
    }


    boolean newDeltaPruneMin(Obj o, int d, int k, List<InternalConstraint> ICTRS) throws ContradictionException {
        int local_nbr_jumps = 0; //local nbr of jumps for the current propagation step (i.e. to get the new lower bound)
        int last_diff = -1;
        int diff_counter = 0;
        int last_dprune = -1;

        boolean b = true;
        Point c = new Point(k);
        for (int i = 0; i < k; i++) {
            c.setCoord(i, o.getCoord(i).getInf());
        }
        Point n = new Point(k);
        for (int i = 0; i < k; i++) {
            n.setCoord(i, o.getCoord(i).getSup() + 1);
        }
        if ((stp.opt.processing)) {
            LOGGER.info("\n/*Processing*/endchunk();\n/*Processing*/break;" + "case " + (stp.opt.phase++) + ":\n/*Processing*/beginchunk();");
            //Draw objects that are instantiated
            for (Integer i : stp.getObjectKeySet()) {
                Obj tmp = stp.getObject(i);
                if (tmp.coordInstantiated()) {
                    if (tmp.isSphere()) {
                        LOGGER.info("\n/*Processing*/sphere_object(" + tmp.getCoord(0).getSup() + ',' + tmp.getCoord(1).getSup() + ',' + tmp.getRadius() + ',' + tmp.getObjectId() + ");");
                    }
                }
            }

            for (InternalConstraint ictr : ICTRS) {
                if (ictr instanceof ForbiddenRegion) {
                    ForbiddenRegion fr;
                    fr = (ForbiddenRegion) ictr;
                    if (fr instanceof DistLeqIC) {
                        DistLeqIC dlic = (DistLeqIC) fr;
                        if (stp.getObject(dlic.o2).coordInstantiated()) {
                            if (dlic.hasDistanceVar()) {
                                LOGGER.info("\n/*Processing*/constraint(" + stp.getObject(dlic.o2).getCoord(0).getSup() + ',' + stp.getObject(dlic.o2).getCoord(1).getSup() + ',' + dlic.getDistanceVar().getSup() + ",\"LeqVar\");");
                            } else {
                                LOGGER.info("\n/*Processing*/constraint(" + stp.getObject(dlic.o2).getCoord(0).getSup() + ',' + stp.getObject(dlic.o2).getCoord(1).getSup() + ',' + dlic.D + ",\"Leq\");");
                            }
                        }
                    }
                    if (fr instanceof DistGeqIC) {
                        DistGeqIC dlic = (DistGeqIC) fr;
                        if (stp.getObject(dlic.o2).coordInstantiated()) {
                            if (dlic.hasDistanceVar()) {
                                LOGGER.info("\n/*Processing*/constraint(" + stp.getObject(dlic.o2).getCoord(0).getSup() + ',' + stp.getObject(dlic.o2).getCoord(1).getSup() + ',' + dlic.getDistanceVar().getInf() + ",\"GeqVar\");");
                            } else {
                                LOGGER.info("\n/*Processing*/constraint(" + stp.getObject(dlic.o2).getCoord(0).getSup() + ',' + stp.getObject(dlic.o2).getCoord(1).getSup() + ',' + dlic.D + ",\"Geq\");");
                            }
                        }
                    }

                    if (fr instanceof DistLinearIC) {
                        DistLinearIC dlic = (DistLinearIC) fr;
                        LOGGER.info("\n/*Processing*/constraint(" + dlic.a[0] + ',' + dlic.a[1] + ',' + dlic.b + ",\"Linear\");");
                    }

                }
            }
            LOGGER.info("\n/*Processing*/sweep_point(" + c.getCoord(0) + ',' + c.getCoord(1) + ");");

        }

        int mode = 0;
        int nbr_steps = 0;
        boolean bad_ratio = false;
        int delta = 1;
        int cd = c.getCoord(d);
        int nj = 0;
        boolean first = true;
        int delta_prev = delta, nj_prev = nj;
        int d_prev_least = (k - 2 + k + d) % k;


        int cdpl = c.getCoord(d_prev_least);
        Pair<Boolean, Region> r = getDeltaFR(d, k, o, c, n, ICTRS, true, c.getCoord(d) + delta, mode);
        boolean infeasible = r.fst;
        Region f = r.snd;
        //INVARIANT:forbidden region contains no feasible pt
        if ((stp.opt.debug) && (infeasible) && (feasiblePtInRegion())) {
            throw new SolverException("GeometricKernel:NewDeltaPruneMin():feasiblePtInRegion is true");
        }
        //END INVARIANT
        Point initial_c = new Point(c);

        if (infeasible) {
            stp.opt.nbr_propagations++;
        }


        while (b && infeasible) {
            for (int i = 0; i < k; i++) {
                n.setCoord(i, Math.min(n.getCoord(i), f.getMaximumBoundary(i) + 1));
            }
            initial_c = new Point(c);     //create a copy


            if (stp.opt.debug) {
                LOGGER.info("/*example*/Adjustup(c=" + c + ",n=" + n + ",o=" + o + ",d=" + d + ",k=" + k + ')');
            }
            List adjUp = adjustUp(c, n, o, d, k); // update the position of c to check
            if (stp.opt.debug) {
                LOGGER.info("/*example*/returns c=" + c + ",n=" + n + ",b=" + b);
            }

            c = (Point) adjUp.get(0);
            n = (Point) adjUp.get(1);
            b = (Boolean) adjUp.get(2);
            stp.opt.nbr_jumps++;
            local_nbr_jumps++;
            if (stp.opt.mixmode) {
                List rcts = checkTrashingState_dl(c, d, k, cdpl, f, bad_ratio, nbr_steps, mode);
                bad_ratio = (Boolean) rcts.get(0);
                nbr_steps = (Integer) rcts.get(1);
                mode = (Integer) rcts.get(2);
                cdpl = c.getCoord(d_prev_least);
            }
            if ((stp.opt.processing) && (Math.abs(c.getCoord(d) - initial_c.getCoord(d)) != 0)) {
                LOGGER.info("\n/*Processing*/endchunk();\n/*Processing*/break;" + "case " + (stp.opt.phase++) + ":\n/*Processing*/beginchunk();");
                //Draw objects that are instantiated
                for (Integer i : stp.getObjectKeySet()) {
                    Obj tmp = stp.getObject(i);
                    if (tmp.coordInstantiated()) {
                        if (tmp.isSphere()) {
                            LOGGER.info("\n/*Processing*/sphere_object(" + tmp.getCoord(0).getSup() + ',' + tmp.getCoord(1).getSup() + ',' + tmp.getRadius() + ',' + tmp.getObjectId() + ");");
                        }
                    }
                }

                for (InternalConstraint ictr : ICTRS) {

                    if (ictr instanceof ForbiddenRegion) {
                        ForbiddenRegion fr;
                        fr = (ForbiddenRegion) ictr;
                        if (fr instanceof DistLeqIC) {
                            DistLeqIC dlic = (DistLeqIC) fr;
                            if ((stp.getObject(dlic.o2).coordInstantiated()) && (!(stp.getObject(dlic.o1).coordInstantiated()))) {
                                LOGGER.info("\n/*Processing*/constraint(" + stp.getObject(dlic.o2).getCoord(0).getSup() + ',' + stp.getObject(dlic.o2).getCoord(1).getSup() + ',' + dlic.D + ",\"Leq\");");
                            }
                        }
                        if (fr instanceof DistGeqIC) {
                            DistGeqIC dlic = (DistGeqIC) fr;
                            if ((stp.getObject(dlic.o2).coordInstantiated()) && (!(stp.getObject(dlic.o1).coordInstantiated()))) {

                                LOGGER.info("\n/*Processing*/constraint(" + stp.getObject(dlic.o2).getCoord(0).getSup() + ',' + stp.getObject(dlic.o2).getCoord(1).getSup() + ',' + dlic.D + ",\"Geq\");");
                            }
                        }
                        if (fr instanceof DistLinearIC) {
                            DistLinearIC dlic = (DistLinearIC) fr;
                            LOGGER.info("\n/*Processing*/constraint(" + dlic.a[0] + ',' + dlic.a[1] + ',' + dlic.b + ",\"Linear\");");
                        }


                    }
                }
                LOGGER.info("\n/*Processing*/new_position(" + c.getCoord(0) + ',' + c.getCoord(1) + ");");
            }


            Pair<Integer, Integer> p = dealWithSucc(d, last_dprune, last_diff, diff_counter, c, initial_c, k);
            last_diff = p.fst;
            last_dprune = d;
            diff_counter = p.snd;

            if (cd != c.getCoord(d)) {
                delta = c.getCoord(d) - cd;
                long s = ((o.getCoord(d).getSup() - c.getCoord(d) + delta - 1) / delta) * nj;
                long s_prev = ((o.getCoord(d).getSup() - c.getCoord(d) + delta_prev - 1) / delta_prev) * nj_prev;
                nj = nj_prev;
                //Limitation; it is supposed here that there is only one shiftedbox per object
                int maxDiameter = 0;
                for (Integer i : stp.getShapeKeySet()) {
                    for (ShiftedBox sb : stp.getShape(i)) {
                        if (sb.getSize(d) + sb.getOffset(d) >= maxDiameter) {
                            maxDiameter = sb.getSize(d) + sb.getOffset(d);
                        }
                    }
                }

                if (first || (s < s_prev)) {
                    delta_prev = delta;

                    delta = Math.min(delta + delta, o.getRadius() + maxDiameter);
                    first = false;
                } else {
                    int tmp = Math.min(((delta + delta_prev) / 2), o.getRadius() + maxDiameter);
                    delta_prev = delta;
                    delta = tmp;
                }
                cd = c.getCoord(d);

            } else {
                nj++;
            }

            Pair forbidRegion = getDeltaFR(d, k, o, c, n, ICTRS, true, c.getCoord(d) + delta, mode);
            infeasible = (Boolean) forbidRegion.fst;
            f = (Region) forbidRegion.snd;
            if ((stp.opt.debug) && (infeasible) && (feasiblePtInRegion())) {
                throw new SolverException("GeometricKernel:NewDeltaPruneMin():feasiblePtInRegion is true");
            }

        } //end while

        Pair<Integer, Integer> p = dealWithSucc(d, last_dprune, last_diff, diff_counter, c, initial_c, k);
        last_diff = p.fst;
        last_dprune = d;
        diff_counter = p.snd;

        if (local_nbr_jumps > stp.opt.max_nbr_jumps) {
            stp.opt.max_nbr_jumps = local_nbr_jumps;
            stp.opt.worst_increase = true; /*stp.opt.worst_point=new Point(c0);*/
        }
        stp.opt.sum_jumps += local_nbr_jumps;
        stp.opt.sum_square_jumps += (local_nbr_jumps * local_nbr_jumps);


        if (b) {
            o.getCoord(d).updateInf(c.getCoord(d), this.constraint, true);
        }
//        if (stp.opt.debug) {
//            LOGGER.info("\n/*Processing*/break;"+"case "+(stp.phase++)+":");
//        }

        return b;
    }

    boolean newDeltaPruneMax(Obj o, int d, int k, List<InternalConstraint> ICTRS) throws ContradictionException {
        int local_nbr_jumps = 0; //local nbr of jumps for the current propagation step (i.e. to get the new lower bound)
        int last_diff = -1;
        int diff_counter = 0;
        int last_dprune = -1;
        boolean b = true;
        Point c = new Point(k);
        for (int i = 0; i < k; i++) {
            c.setCoord(i, o.getCoord(i).getSup());
        }
        Point n = new Point(k);
        for (int i = 0; i < k; i++) {
            n.setCoord(i, o.getCoord(i).getInf() - 1);
        }

        if ((stp.opt.processing)) {
            LOGGER.info("\n/*Processing*/endchunk();\n/*Processing*/break;" + "case " + (stp.opt.phase++) + ":\n/*Processing*/beginchunk();");
            //Draw objects that are instantiated
            for (Integer i : stp.getObjectKeySet()) {
                Obj tmp = stp.getObject(i);
                if (tmp.coordInstantiated()) {
                    if (tmp.isSphere()) {
                        LOGGER.info("\n/*Processing*/sphere_object(" + tmp.getCoord(0).getSup() + ',' + tmp.getCoord(1).getSup() + ',' + tmp.getRadius() + ',' + tmp.getObjectId() + ");");
                    }
                }
            }

            for (InternalConstraint ictr : ICTRS) {
                if (ictr instanceof ForbiddenRegion) {
                    ForbiddenRegion fr;
                    fr = (ForbiddenRegion) ictr;
                    if (fr instanceof DistLeqIC) {
                        DistLeqIC dlic = (DistLeqIC) fr;
                        if (stp.getObject(dlic.o2).coordInstantiated()) {
                            LOGGER.info("\n/*Processing*/constraint(" + stp.getObject(dlic.o2).getCoord(0).getSup() + ',' + stp.getObject(dlic.o2).getCoord(1).getSup() + ',' + dlic.D + ",\"Leq\");");
                        }
                    }
                    if (fr instanceof DistGeqIC) {
                        DistGeqIC dlic = (DistGeqIC) fr;
                        if (stp.getObject(dlic.o2).coordInstantiated()) {
                            LOGGER.info("\n/*Processing*/constraint(" + stp.getObject(dlic.o2).getCoord(0).getSup() + ',' + stp.getObject(dlic.o2).getCoord(1).getSup() + ',' + dlic.D + ",\"Geq\");");
                        }
                    }

                    if (fr instanceof DistLinearIC) {
                        DistLinearIC dlic = (DistLinearIC) fr;
                        LOGGER.info("\n/*Processing*/constraint(" + dlic.a[0] + ',' + dlic.a[1] + ',' + dlic.b + ",\"Linear\");");
                    }


                }
            }
            LOGGER.info("\n/*Processing*/sweep_point(" + c.getCoord(0) + ',' + c.getCoord(1) + ");");

        }

        int d_least = (k - 1 + d) % k;
        int d_prev_least = (k - 2 + d) % k;
        int cdpl = c.getCoord(d_least);
        int nbr_steps = 0;
        boolean bad_ratio = false;
        int mode = 0;
        int delta = 1;
        int cd = c.getCoord(d);
        int nj = 0;
        boolean first = true;
        int delta_prev = delta, nj_prev = nj;
        Pair<Boolean, Region> r = getDeltaFR(d, k, o, c, n, ICTRS, false, c.getCoord(d) - delta, mode);
        boolean infeasible = r.fst;
        Region f = r.snd;
        if ((stp.opt.debug) && (infeasible) && (feasiblePtInRegion())) {
            throw new SolverException("GeometricKernel:NewDeltaPruneMax():feasiblePtInRegion is true");
        }

        Point initial_c = new Point(c);

        if (infeasible) {
            stp.opt.nbr_propagations++;
        }

        while (b && infeasible) {
            for (int i = 0; i < k; i++) {
                n.setCoord(i, Math.max(n.getCoord(i), f.getMinimumBoundary(i) - 1));
            }
            initial_c = new Point(c);     //create a copy

            List adjUp = adjustDown(c, n, o, d, k); // update the position of c to check
            stp.opt.nbr_jumps++;
            local_nbr_jumps++;
            //LOGGER.info(" returns $[c="+adjUp.get(0)+",n="+adjUp.get(1)+",b="+adjUp.get(2)+",mode="+adjUp.get(3)+"]$ ");
            c = (Point) adjUp.get(0);
            n = (Point) adjUp.get(1);
            b = (Boolean) adjUp.get(2);
            if (stp.opt.mixmode) {
                List rcts = checkTrashingState_dl(c, d, k, cdpl, f, bad_ratio, nbr_steps, mode);
                bad_ratio = (Boolean) rcts.get(0);
                nbr_steps = (Integer) rcts.get(1);
                mode = (Integer) rcts.get(2);
                cdpl = c.getCoord(d_prev_least);
            }


            if ((stp.opt.processing) && (Math.abs(c.getCoord(d) - initial_c.getCoord(d)) != 0)) {
                LOGGER.info("\n/*Processing*/endchunk();\n/*Processing*/break;" + "case " + (stp.opt.phase++) + ":\n/*Processing*/beginchunk();");

                //Draw objects that are instantiated
                for (Integer i : stp.getObjectKeySet()) {
                    Obj tmp = stp.getObject(i);
                    if (tmp.coordInstantiated()) {
                        if (tmp.isSphere()) {
                            LOGGER.info("\n/*Processing*/sphere_object(" + tmp.getCoord(0).getSup() + ',' + tmp.getCoord(1).getSup() + ',' + tmp.getRadius() + ',' + tmp.getObjectId() + ");");
                        }
                    }
                }

                for (InternalConstraint ictr : ICTRS) {
                    if (ictr instanceof ForbiddenRegion) {
                        ForbiddenRegion fr;
                        fr = (ForbiddenRegion) ictr;
                        if (fr instanceof DistLeqIC) {
                            DistLeqIC dlic = (DistLeqIC) fr;
                            if (stp.getObject(dlic.o2).coordInstantiated()) {
                                LOGGER.info("\n/*Processing*/constraint(" + stp.getObject(dlic.o2).getCoord(0).getSup() + ',' + stp.getObject(dlic.o2).getCoord(1).getSup() + ',' + dlic.D + ",\"Leq\");");
                            }
                        }
                        if (fr instanceof DistGeqIC) {
                            DistGeqIC dlic = (DistGeqIC) fr;
                            if (stp.getObject(dlic.o2).coordInstantiated()) {
                                LOGGER.info("\n/*Processing*/constraint(" + stp.getObject(dlic.o2).getCoord(0).getSup() + ',' + stp.getObject(dlic.o2).getCoord(1).getSup() + ',' + dlic.D + ",\"Geq\");");
                            }
                        }
                        if (fr instanceof DistLinearIC) {
                            DistLinearIC dlic = (DistLinearIC) fr;
                            LOGGER.info("\n/*Processing*/constraint(" + dlic.a[0] + ',' + dlic.a[1] + ',' + dlic.b + ",\"Linear\");");
                        }


                    }
                }
                LOGGER.info("\n/*Processing*/new_position(" + c.getCoord(0) + ',' + c.getCoord(1) + ");");

            }


            Pair<Integer, Integer> p = dealWithSucc(d, last_dprune, last_diff, diff_counter, c, initial_c, k);
            last_diff = p.fst;
            last_dprune = d;
            diff_counter = p.snd;


            if (cd != c.getCoord(d)) {
                delta = cd - c.getCoord(d);
                long s = ((c.getCoord(d) - o.getCoord(d).getInf() - delta + 1) / delta) * nj;
                long s_prev = ((c.getCoord(d) - o.getCoord(d).getInf() - delta_prev + 1) / delta_prev) * nj_prev;
                nj = nj_prev;
                //Limitation; it is supposed here that there is only one shiftedbox per object
                int maxDiameter = 0;
                for (Integer i : stp.getShapeKeySet()) {
                    for (ShiftedBox sb : stp.getShape(i)) {
                        if (sb.getSize(d) + sb.getOffset(d) >= maxDiameter) {
                            maxDiameter = sb.getSize(d) + sb.getOffset(d);
                        }
                    }
                }

                if (first || (s < s_prev)) {
                    delta_prev = delta;
                    delta = Math.min(delta + delta, o.getRadius() + maxDiameter);
                    first = false;
                } else {
                    int tmp = Math.min(((delta + delta_prev) / 2), o.getRadius() + maxDiameter);
                    delta_prev = delta;
                    delta = tmp;
                }
                cd = c.getCoord(d);

            } else {
                nj++;
            }

            Pair forbidRegion = getDeltaFR(d, k, o, c, n, ICTRS, false, c.getCoord(d) - delta, mode);
            infeasible = (Boolean) forbidRegion.fst;
            f = (Region) forbidRegion.snd;
            if ((stp.opt.debug) && (infeasible) && (feasiblePtInRegion())) {
                throw new SolverException("GeometricKernel:NewDeltaPruneMax():feasiblePtInRegion is true");
            }

        }
        Pair<Integer, Integer> p = dealWithSucc(d, last_dprune, last_diff, diff_counter, c, initial_c, k);
        last_diff = p.fst;
        last_dprune = d;
        diff_counter = p.snd;

        if (b) {
            o.getCoord(d).updateSup(c.getCoord(d), this.constraint, true);
        }
        //       if (stp.opt.debug) {
        //           LOGGER.info("\n/*Processing*/break;"+"case "+(stp.phase++)+":");
        //      }

        if (local_nbr_jumps > stp.opt.max_nbr_jumps) {
            stp.opt.max_nbr_jumps = local_nbr_jumps;
            stp.opt.worst_increase = false; /*stp.opt.worst_point=new Point(c0);*/
        }
        stp.opt.sum_jumps += local_nbr_jumps;
        stp.opt.sum_square_jumps += local_nbr_jumps * local_nbr_jumps;

        return b;
    }


//    Pair<Boolean,Region> GetAnalyticFR(int d, int k,Obj o,Point c,Point n,Vector<ForbiddenRegion> ictrs,boolean increase){
//        Vector<ForbiddenRegion> C_c = new ArrayList<ForbiddenRegion>();
//        for (InternalConstraint ictr:ictrs) {
//            if (!(ictr instanceof ForbiddenRegion)) {LOGGER.info("GetDeltatFR():not a ForviddenRegion constraint."); System.exit(-1);}
//            ForbiddenRegion fr = (ForbiddenRegion) ictr;
//            if (fr.segInsideForbidden(c))  C_c.add(fr);
//        }
//
//        if (C_c.size()==0) return new Pair<Boolean,Region>(false,null);
//
//
//
//
//    }


    public static List<Point> circleIntersectiont(double x1, double y1, double r1, double x2, double y2, double r2) {

        double distance = Math.sqrt(Math.pow((x2 - x1), 2) + Math.pow((y2 - y1), 2));
        if ((distance > r1 + r2) || (distance < Math.abs(r1 - r2))) {
            return null;
        }
        double x = ((x2 + x1) / 2) + (((x2 - x1) * ((r1 * r1) - (r2 * r2))) / (2 * Math.pow(distance, 2)));
        double xvar = ((y2 - y1) / (2 * distance * distance)) * Math.sqrt((Math.pow(r1 + r2, 2) - (distance * distance)) * ((distance * distance) - Math.pow(r2 - r1, 2)));
        double x_1 = x + xvar;
        double x_2 = x - xvar;

        double y = ((y2 + y1) / 2) + (((y2 - y1) * ((r1 * r1) - (r2 * r2))) / (2 * Math.pow(distance, 2)));
        double yvar = ((x2 - x1) / (2 * distance * distance)) * Math.sqrt((Math.pow(r1 + r2, 2) - (distance * distance)) * ((distance * distance) - Math.pow(r2 - r1, 2)));
        double y_1 = y - yvar;
        double y_2 = y + yvar;

        //Conversion to the discrete world

        int x_1i = ((int) Math.floor(x_1));
        int x_2i = ((int) Math.floor(x_2));
        int y_1i = ((int) Math.floor(y_1));
        int y_2i = ((int) Math.floor(y_2));

        List<Point> listOfPoints = new ArrayList<Point>(2);
        Point p1 = new Point(2);
        p1.setCoord(0, x_1i);
        p1.setCoord(1, y_1i);
        listOfPoints.add(p1);
        Point p2 = new Point(2);
        p2.setCoord(0, x_2i);
        p2.setCoord(1, y_2i);
        if (distance != Math.abs(r1 - r2)) {
            listOfPoints.add(p2); //Otherwise there is only one intersection point (tangent case)
        }

        return listOfPoints;

    }

    IntDomainVar getE(int oid) {
        //Check for a variable E associated with a constraint <=(c_{oid},.,E)
        //stops if no exists and if two exists
        //creates a caching in order to compute the search only once

        if (this.E == null) {
            this.E = new IntDomainVar[stp.getObjectKeySet().size()];
            for (int i = 0; i < stp.getObjectKeySet().size(); i++) {
                this.E[i] = null;
            }
        }

        IntDomainVar found = null;

        if (this.E[oid] != null) {
            found = this.E[oid];
        } else {


            for (ExternalConstraint ectr : stp.getConstraints()) {
                if (ectr instanceof DistLeq) {
                    DistLeq dl = (DistLeq) ectr;
                    if ((dl.hasDistanceVar() && (dl.getObjectIds()[0] == oid))) {
                        if (found == null) {
                            found = dl.getDistanceVar();
                        } else {
                            throw new SolverException("GeometricKernel:getE():Two E variables for variable oid " + oid + '.');
                        }
                    }
                }
            }
        }

        if (found == null) {
            throw new SolverException("GeometricKernel:getE():No E variables for variable oid " + oid + '.');
        }

        this.E[oid] = found;

        return found;
    }

    IntDomainVar getD(int oid) {
        //Check for a variable E associated with a constraint <=(c_{oid},.,E)
        //stops if no exists and if two exists
        //creates a caching in order to compute the search only once

        if (this.D == null) {
            this.D = new IntDomainVar[stp.getObjectKeySet().size()];
            for (int i = 0; i < stp.getObjectKeySet().size(); i++) {
                this.D[i] = null;
            }
        }

        IntDomainVar found = null;

        if (this.D[oid] != null) {
            found = this.D[oid];
        } else {
            for (ExternalConstraint ectr : stp.getConstraints()) {
                if (ectr instanceof DistGeq) {
                    DistGeq dl = (DistGeq) ectr;
                    if ((dl.hasDistanceVar() && (dl.getObjectIds()[0] == oid))) {
                        if (found == null) {
                            found = dl.getDistanceVar();
                        } else {
                            throw new SolverException("GeometricKernel:getD():Two D variables for variable oid " + oid + '.');
                        }
                    }
                }
            }
        }

        if (found == null) {
            throw new SolverException("GeometricKernel:getD():No D variables for variable oid " + oid + '.');
        }

        this.D[oid] = found;

        return found;
    }

    boolean propagDistConstraints() throws ContradictionException {
        //Propagate |E_{i-1}-D_i|>=r_small, returns true if any update

        boolean nonFix = false;

        for (int i = 1; i < stp.getObjectKeySet().size() - 1; i++) { //last one is the center circle, supp. a strict order on objects!
            IntDomainVar Dprec = getD(i - 1);
            if (Dprec.isInstantiated()) {
                LOGGER.info("D of oid:" + (i - 1) + " is instantiated:" + Dprec);
                IntDomainVar D = getD(i);
                int oldSup = D.getSup();
                int newSup = Dprec.getVal() - (stp.getObject(i).getRadius() + stp.getObject(i - 1).getRadius());
                LOGGER.info("D:[" + D.getInf() + ',' + D.getSup() + "] oldSup:"+oldSup+" newSup:"+newSup);
                if (newSup>=oldSup) {
                    continue;
                }
                D.setSup(newSup);
                nonFix |= true;
            }

        }

        return nonFix;

    }

}
