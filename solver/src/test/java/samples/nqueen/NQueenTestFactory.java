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

package samples.nqueen;

import org.testng.annotations.Factory;

import java.util.ArrayList;
import java.util.List;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 30 sept. 2010
 */
public class NQueenTestFactory {

    int[] propagation = {
            0, 1, 2     // PropagatorEngine OR VariableEngine OR PropagatorEngine
    };

    private int[] size = {4, 8, 12};//,5,6,7,8,9,10,11,12};
    private int[] searchloop = {0, 1};   // simple binary, advanced binary
    private int[] pilot = {0, 1};       // basic, schulte-like

    @Factory
    public Object[] createInstances() {
        List<Object> lresult = new ArrayList<Object>(12);

        for (int s = 0; s < size.length; s++) {
            int _size = size[s];
            for (int sl = 0; sl < searchloop.length; sl++) {
                int _searchloop = searchloop[sl];
                for (int p = 0; p < pilot.length; p++) {
                    int _pilot = pilot[p];
                    for (int pe = 0; pe < propagation.length; pe++) {
                        int _propagation = propagation[pe];
                        lresult.add(new NQueenTest(_propagation, _pilot, _searchloop, _size));
                    }
                }
            }
        }
        return lresult.toArray();
    }


}
