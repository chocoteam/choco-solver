/**
 * Copyright (c) 2016, Ecole des Mines de Nantes
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. All advertising materials mentioning features or use of this software
 *    must display the following acknowledgement:
 *    This product includes software developed by the <organization>.
 * 4. Neither the name of the <organization> nor the
 *    names of its contributors may be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY <COPYRIGHT HOLDER> ''AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.chocosolver.solver.variables.ranges;


import org.chocosolver.solver.exception.SolverException;
import org.chocosolver.util.objects.setDataStructures.ISetIterator;
import org.chocosolver.util.objects.setDataStructures.SetType;

import java.util.Arrays;
import java.util.Iterator;

/**
 * Concret implementation of {@link IntIterableSet} wherein values are stored in range set.
 * A range is made of two ints, the lower bound and the upper bound of the range.
 * A range can be a singleton, in that case, the lb and the ub are equal.
 * If the upper bound of range A is equal to lower bound of range B, then the two ranges can be merged into a single one.
 * <p>
 * Project: choco.
 *
 * @author Charles Prud'homme
 * @since 14/01/2016.
 */
public class IntIterableRangeSet implements IntIterableSet {

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

    /**
     * Store elements
     */
    protected int[] ELEMENTS;
    /**
     * Used size in {@link #ELEMENTS}.
     * To get the nmber of range simply divide by 2.
     */
    protected int SIZE;

    /**
     * Total number of elements in the set
     */
    protected int CARDINALITY;

	/** Create an ISet iterator */
    private ISetIterator iter = newIterator();

	//***********************************************************************************
	// CONSTRUCTOR
	//***********************************************************************************

    /**
     * Create an interval-based ordered set
     */
    public IntIterableRangeSet() {
        ELEMENTS = new int[10];
        SIZE = 0;
        CARDINALITY = 0;
    }

	/**
	 * Create an interval-based ordered set initialized to [a,b]
	 *
	 * @param a lower bound of the interval
	 * @param b upper bound of the interval
	 */
	public IntIterableRangeSet(int a, int b) {
		ELEMENTS = new int[10];
		SIZE = 2;
		CARDINALITY = b - a + 1;
		ELEMENTS[0] = a;
		ELEMENTS[1] = b;
	}

	/**
	 * Create an interval-based ordered set initialized to singleton {e}
	 * @param e singleton value
	 */
	public IntIterableRangeSet(int e) {
		ELEMENTS = new int[10];
		SIZE = 2;
		CARDINALITY = 1;
		ELEMENTS[0] = ELEMENTS[1] = e;
	}

	//***********************************************************************************
	// METHODS
	//***********************************************************************************

    @Override
    public String toString() {
        StringBuilder st = new StringBuilder();
        st.append("set={");
        for (int i = 0; i < SIZE - 1; i += 2) {
            if (ELEMENTS[i] == ELEMENTS[i + 1]) {
                st.append(ELEMENTS[i]).append(',');
            } else {
                for (int j = ELEMENTS[i]; j <= ELEMENTS[i + 1]; j++) {
                    st.append(j).append(',');
                }
            }
        }
        if (SIZE > 0) st.deleteCharAt(st.length() - 1);
        st.append("}");
        return st.toString();
    }


    @Override
    public void setOffset(int offset) {
        // nothing to do
    }

    @Override
    public int first() {
        return ELEMENTS[0];
    }

    @Override
    public int last() {
        return ELEMENTS[SIZE - 1];
    }

    @Override
    public boolean add(int e) {
        boolean modified = false;
        int p = rangeOf(e);
        // if e is not in a range
        if (p < 0) {
            grow(SIZE + 2);
            int i = (-p - 1) << 1;
            //if (i > 0) {
            int c = i > 0 && ELEMENTS[i - 1] + 1 == e ? 1 : 0;
            c += i < SIZE && e == ELEMENTS[i] - 1 ? 2 : 0;
            switch (c) {
                case 0:
                    // insert a new range
                    System.arraycopy(ELEMENTS, i, ELEMENTS, i + 2, SIZE - i);
                    ELEMENTS[i] = ELEMENTS[i + 1] = e;
                    SIZE += 2;
                    break;
                case 1:
                    // e is the new lower bound
                    assert ELEMENTS[i - 1] + 1 == e;
                    ELEMENTS[i - 1] = e;
                    break;
                case 2:
                    //  e is the new upper bound
                    assert ELEMENTS[i] - 1 == e;
                    ELEMENTS[i] = e;
                    break;
                case 3:
                    // merge two ranges
                    System.arraycopy(ELEMENTS, i + 1, ELEMENTS, i - 1, SIZE - i);
                    SIZE -= 2;
                    break;
                default: throw new SolverException("Unexpected mask "+c);
            }
            modified = true;
            CARDINALITY++;
        }
        return modified;
    }

    @Override
    public boolean addAll(int... values) {
        int c = CARDINALITY;
        for (int i = 0; i < values.length; i++) {
            add(values[i]);
        }
        return CARDINALITY - c > 0;
    }

    @Override
    public boolean addAll(IntIterableSet set) {
        int c = CARDINALITY;
        if (set.size() > 0) {
            int v = set.first();
            while (v < Integer.MAX_VALUE) {
                add(v);
                v = set.nextValue(v);
            }
        }
        return CARDINALITY > c;
    }

    @Override
    public boolean retainAll(IntIterableSet set) {
        int c = CARDINALITY;
        if (set.size() == 0) {
            this.clear();
        } else if (size() > 0) {
            int last = last();
            for (int i = first(); i <= last; i = nextValue(i)) {
                if (!set.contains(i)) {
                    remove(i);
                }
            }
        }
        return c - CARDINALITY > 0;
    }

    @Override
    public boolean remove(int e) {
        boolean modified = false;
        int p = rangeOf(e);
        // if e is not in a range
        if (p >= 0) {
            int i = (p - 1) << 1;
            int c = ELEMENTS[i] == e ? 1 : 0;
            c += ELEMENTS[i + 1] == e ? 2 : 0;
            switch (c) {
                case 0:
                    // split range in two ranges
                    grow(SIZE + 2);
                    System.arraycopy(ELEMENTS, i + 1, ELEMENTS, i + 3, SIZE - i - 1);
                    ELEMENTS[i + 1] = e - 1;
                    ELEMENTS[i + 2] = e + 1;
                    SIZE += 2;
                    break;
                case 1:
                    // increase lower of the range
                    ELEMENTS[i]++;
                    break;
                case 2:
                    // decrease upper of the range
                    ELEMENTS[i + 1]--;
                    break;
                case 3:
                    // delete a range
                    System.arraycopy(ELEMENTS, i + 2, ELEMENTS, i, SIZE - i - 2);
                    SIZE -= 2;
                    break;
                default: throw new SolverException("Unexpected mask "+c);
            }
            modified = true;
            CARDINALITY--;
        }
        return modified;
    }

    @Override
    public boolean removeAll(IntIterableSet set) {
        int c = CARDINALITY;
        if (set.size() > 0) {
            int v = set.first();
            while (v < Integer.MAX_VALUE) {
                remove(v);
                v = set.nextValue(v);
            }
        }
        return CARDINALITY < c;
    }

    @Override
    public void clear() {
        CARDINALITY = 0;
        SIZE = 0;
    }

    @Override
    public SetType getSetType() {
        return SetType.RANGESET;
    }

    @Override
    public boolean removeBetween(int f, int t) {
        boolean rem = false;
        if(f > t){
            return false;
        }
        int rf = rangeOf(f);
        if (rf < 0) {
            // find closest after
            rf *= -1;
            f = ELEMENTS[(rf - 1) << 1];
        }
        assert rf > 0;
        int rt = rangeOf(t);
        if (rt < 0) {
            // find closest range before
            rt = -rt - 1;
            t = ELEMENTS[((rt - 1) << 1) + 1];
        }
        assert rt > 0;
        int i = (rf - 1) << 1;
        int j = (rt - 1) << 1;
        if (rf <= rt) {
            int dcard = -(f - ELEMENTS[i] + ELEMENTS[j+1] - t);
            dcard += ELEMENTS[i + 1] - ELEMENTS[i] + 1;
            if (rf < rt) {
                for (int k = i + 2; k <= j + 1; k+=2) {
                    dcard += ELEMENTS[k + 1] - ELEMENTS[k] + 1;
                }
                if (rf < rt) {
                    // remove useless range
                    System.arraycopy(ELEMENTS, j + 1, ELEMENTS, i + 1, SIZE - (j + 1));
                }
                SIZE -= (rt - rf) << 1;
            }
            CARDINALITY -= dcard;
            int c = ELEMENTS[i] == f ? 1 : 0;
            c += ELEMENTS[i + 1] == t ? 2 : 0;
            switch (c) {
                case 0: // split the range into two ranges
                    grow(SIZE + 2);
                    System.arraycopy(ELEMENTS, i, ELEMENTS, i + 2, SIZE - i);
                    ELEMENTS[i + 1] = f - 1;
                    ELEMENTS[i + 2] = t + 1;
                    SIZE += 2;
                    break;
                case 1: // update the lower bound of the range
                    ELEMENTS[i] = t + 1;
                    break;
                case 2: // update the upper bound of the range
                    ELEMENTS[i + 1] = f - 1;
                    break;
                case 3: // remove the range
                    if (i < SIZE - 2) {
                        System.arraycopy(ELEMENTS, i + 2, ELEMENTS, i, SIZE - (i + 2));
                    }
                    SIZE -= 2;
                    break;
            }
            rem = true;
        }
        return rem;
    }

    @Override
    public int nextValue(int e) {
        int p = rangeOf(e);
        int next = Integer.MAX_VALUE;
        if (p == -1 && SIZE > 0) {
            next = ELEMENTS[0];
        } else if (p >= 0) {
            int i = (p - 1) << 1;
            int c = ELEMENTS[i] == e ? 1 : 0;
            c += ELEMENTS[i + 1] == e ? 2 : 0;
            switch (c) {
                case 0:
                case 1:
                    // not last element of the range
                    next = e + 1;
                    break;
                case 2:
                case 3:
                    if (i + 2 < SIZE) {
                        next = ELEMENTS[i + 2];
                    }
            }
        } else if (p > -((SIZE >> 1) + 1)) {
            return ELEMENTS[(-p - 1) << 1];
        }
        return next;
    }

    @Override
    public int previousValue(int e) {
        int p = rangeOf(e);
        int prev = Integer.MIN_VALUE;
        if (p == -((SIZE >> 1) + 1) && SIZE > 0) {
            prev = ELEMENTS[SIZE - 1];
        } else if (p >= 0) {
            int i = (p - 1) << 1;
            int c = ELEMENTS[i] == e ? 1 : 0;
            c += ELEMENTS[i + 1] == e ? 2 : 0;
            switch (c) {
                case 0:
                case 2:
                    // not last element of the range
                    prev = e - 1;
                    break;
                case 1:
                case 3:
                    if (i > 1) {
                        prev = ELEMENTS[i - 1];
                    }
            }
        } else if (p < -1) {
            return ELEMENTS[((-p - 1) << 1) - 1];
        }
        return prev;
    }

    @Override
    public boolean contains(int o) {
        return rangeOf(o) >= 0;
    }

    @Override
    public IntIterableSet duplicate() {
        IntIterableRangeSet ir = new IntIterableRangeSet();
        ir.ELEMENTS = this.ELEMENTS.clone();
        ir.CARDINALITY = this.CARDINALITY;
        ir.SIZE = this.SIZE;
        return ir;
    }

    @Override
    public int size() {
        return CARDINALITY;
    }

    /**
     * add the value <i>x</i> to all integers stored in this set
     *
     * @param x value to add
     */
    public void plus(int x) {
        for (int i = 0; i < SIZE; i++) {
            ELEMENTS[i] += x;
        }
    }

   	@Override
   	public Iterator<Integer> iterator(){
   		iter.reset();
   		return iter;
   	}

    /**
     * subtract the value <i>x</i> to all integers stored in this set
     *
     * @param x value to add
     */
    public void minus(int x) {
        for (int i = 0; i < SIZE; i++) {
            ELEMENTS[i] -= x;
        }
    }

    /**
     * multiply by <i>x</i> to all integers stored in this set
     *
     * @param x value to add
     */
    public void times(int x) {
        for (int i = 0; i < SIZE; i++) {
            ELEMENTS[i] *= x;
        }
        CARDINALITY *= x;
    }

    /**
     * By convention, range are numbered starting from 1 (not 0).
     *
     * @param x a value
     * @return the range index if the value is in the set or -<i>range point</i> - 1 otherwise
     * where <i>range point</i> corresponds to the range directly greater than the key
     */
    int rangeOf(int x) {
        int p = Arrays.binarySearch(ELEMENTS, 0, SIZE, x);
        // if pos is positive, the value is a bound of a range
        if (p >= 0) {
            p >>= 1;
        } else if (p == -1) {
            p--;
        } else if (p == -(SIZE + 1)) {
            p = -((SIZE >> 1) + 2);// -2 because add 1 as last instruction
        } else {
            // is x in a range or not
            p = -(p + 1);
            p >>= 1;
            if (!(ELEMENTS[p << 1] < x && x < ELEMENTS[(p << 1) + 1])) {
                p = -(p + 2); // -2 because add 1 as last instruction
            }
        }
        return p + 1;
    }

    /**
     * Increases the capacity to ensure that it can hold at least the
     * number of elements specified by the minimum capacity argument.
     *
     * @param minCapacity the desired minimum capacity
     */
    void grow(int minCapacity) {
        if (minCapacity - ELEMENTS.length > 0) {
            // overflow-conscious code
            int oldCapacity = ELEMENTS.length;
            int newCapacity = oldCapacity + (oldCapacity >> 1);
            if (newCapacity - minCapacity < 0)
                newCapacity = minCapacity;
            // minCapacity is usually close to size, so this is a win:
            ELEMENTS = Arrays.copyOf(ELEMENTS, newCapacity);
        }
    }

    /**
     * Push a range at the end of this set
     * @param lb lower bound of the range
     * @param ub upper bound of the range
     */
    void pushRange(int lb, int ub){
        assert SIZE == 0 || ELEMENTS[SIZE-1] < lb - 1;
        assert lb <= ub;
        grow(SIZE + 2);
        ELEMENTS[SIZE++] = lb;
        ELEMENTS[SIZE++] = ub;
        CARDINALITY += ub - lb + 1;
    }
}
