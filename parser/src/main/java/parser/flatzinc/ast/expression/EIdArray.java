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

package parser.flatzinc.ast.expression;

import gnu.trove.THashMap;
import solver.Solver;
import solver.variables.BoolVar;
import solver.variables.IntVar;
import solver.variables.VariableFactory;

/*
* User : CPRUDHOM
* Mail : cprudhom(a)emn.fr
* Date : 11 janv. 2010
* Since : Choco 2.1.1
*
* Class for array index expressions definition based on flatzinc-like objects.
*/
public final class EIdArray extends Expression{

    final String name;
    final EInt index;
    final Object object;

    public EIdArray(THashMap<String, Object> map, String id, EInt i) {
        super(EType.IDA);
        this.name = id;
        this.index = i;

        Object array = map.get(name);
        if(int_arr.isInstance(array)){
            object = ((int[])array)[index.value-1];
        }else if(bool_arr.isInstance(array)){
            object = ((int[])array)[index.value-1];
        }else{
            object = ((Object[])array)[index.value-1];    
        }
    }

    @Override
    public String toString() {
        return name+ '[' +index.toString()+ ']';
    }

    @Override
    public int intValue() {
        return (Integer) object;
    }

    @Override
    public int[] toIntArray() {
        return (int[])object;
    }

    @Override
    public BoolVar boolVarValue(Solver solver) {
        if(Integer.class.isInstance(object)){
            return (BoolVar)VariableFactory.fixed((Integer)object);
        }else if(Boolean.class.isInstance(object)){
            return (BoolVar)VariableFactory.fixed(((Boolean)object)?1:0);
        }
        return (BoolVar)object;
    }

    @Override
    public BoolVar[] toBoolVarArray(Solver solver) {
        return (BoolVar[])object;
    }

    @Override
    public IntVar intVarValue(Solver solver) {
        if(Integer.class.isInstance(object)){
            return VariableFactory.fixed((Integer)object);
        }else if(Boolean.class.isInstance(object)){
            return VariableFactory.fixed(((Boolean)object)?1:0);
        }
        return (IntVar)object;
    }

    @Override
    public IntVar[] toIntVarArray(Solver solver) {
        return (IntVar[])object;
    }
}
