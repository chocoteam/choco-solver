/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.binary.element;

import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.ESat;
import org.chocosolver.util.objects.setDataStructures.iterable.IntIterableBitSet;
import org.chocosolver.util.tools.ArrayUtils;

/**
 * VALUE = TABLE[INDEX-OFFSET], ensuring arc consistency on result and index.
 * <br/>
 *
 * @author Hadrien Cambazard
 * @author Fabien Hermenier
 * @author Charles Prud'homme
 * @author Jean-Guillaume Fages
 * @since 02/02/12
 */
//2015OCT - cprudhom : simplify the code, without changing the consistency, no Sort anymore
//2016AUG - jg : move filtering from bound/range to AC (add member reasoning)
public class PropElement extends Propagator<IntVar> {

    /**
     * Table of values
     */
    private final int[] values;

    /**
     * To match indices in {@link #values} and {@link #index}
     */
    private final int offset;

    /**
     * Index variable
     */
    private final IntVar index;

    /**
     * Resulting variable
     */
    private final IntVar result;

    /**
     * Set of forbidden indices and possible values
     */
    private final IntIterableBitSet fidx, pVals;

    /**
     * Create a propagator which ensures that VALUE = TABLE[INDEX-OFFSET] holds.
     * @param value integer variable
     * @param values array of ints
     * @param index integer variable
     * @param offset int
     */
    public PropElement(IntVar value, int[] values, IntVar index, int offset) {
        super(ArrayUtils.toArray(value, index), PropagatorPriority.BINARY, false);
        this.values = values;
        this.offset = offset;
        this.index = index;
        this.result = value;
        fidx = new IntIterableBitSet();
        fidx.setOffset(index.getLB());
        pVals = new IntIterableBitSet();
        pVals.setOffset(result.getLB());
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
		index.updateBounds(offset, values.length - 1 + offset, this);
		fidx.clear();
		pVals.clear();
		int iub = index.getUB();
		for (int i = index.getLB(); i <= iub; i = index.nextValue(i)) {
			int value = values[i - offset];
			if (result.contains(value)){
				pVals.add(value);
			}else{
				fidx.add(i);
			}
		}
		result.removeAllValuesBut(pVals,this);
		if (!fidx.isEmpty()) {
			index.removeValues(fidx, this);
		}
		if (result.isInstantiated() && index.hasEnumeratedDomain() && !index.isInstantiated()) {
			setPassive();
		}
	}

    @Override
    public ESat isEntailed() {
        if (index.getUB() < offset || index.getLB() >= offset + values.length) {
            return ESat.FALSE;
        }
        if (isCompletelyInstantiated()) {
            return ESat.eval(result.contains(values[index.getValue() - offset]));
        } else if(result.isInstantiated()){
            int val = result.getValue();
            boolean foundVal = false;
            boolean foundOther = false;
            for(int i:index){
                if(i>=offset && i<values.length + offset && values[i-offset] == val){
                    foundVal = true;
                    if(foundOther)break;
                }else{
                    foundOther = true;
                    if(foundVal)break;
                }
            }
            if(foundVal){
                if(foundOther){
                    return ESat.UNDEFINED;
                }else{
                    return ESat.TRUE;
                }
            }else{
                return ESat.FALSE;
            }
        } else {
            for(int i:index){
                if(i>=offset && i<values.length + offset && result.contains(values[i-offset])){
                    return ESat.UNDEFINED;
                }
            }
            return ESat.FALSE;
        }
    }

    public String toString() {
        StringBuilder sb = new StringBuilder(32);
        sb.append("element(").append(this.result).append(" = ");
        sb.append(" <");
        int i = 0;
        for (; i < Math.min(this.values.length - 1, 5); i++) {
            sb.append(this.values[i]).append(", ");
        }
        if (i == 5 && this.values.length - 1 > 5) sb.append("..., ");
        sb.append(this.values[values.length - 1]);
        sb.append("> [").append(this.index).append("])");
        return sb.toString();
    }

}
