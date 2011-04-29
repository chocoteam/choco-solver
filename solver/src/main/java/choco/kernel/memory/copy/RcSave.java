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

package choco.kernel.memory.copy;

import gnu.trove.TIntObjectHashMap;

/*
 * Created by IntelliJ IDEA.
 * User: Julien
 * Date: 29 mars 2007
 * Since : Choco 2.0.0
 *
 */
public final class RcSave implements RecomputableElement {

    public RcInt[] currentElementI;
    public RcVector[] currentElementV;
    public RcIntVector[] currentElementIV;
    public RcBool[]currentElementB;
    public RcLong[] currentElementL;
    public RcDouble[] currentElementD;
    public RcObject[] currentElementO;

    private int lastSavedWorldIndex;



    private static final TIntObjectHashMap<int []> saveInt;
    private static final TIntObjectHashMap<Object[][]> saveVector;
    private static final TIntObjectHashMap<int[][]> saveIntVector;
    private static final TIntObjectHashMap<boolean[]> saveBool;
    private static final TIntObjectHashMap<long []> saveLong;
    private static final TIntObjectHashMap<double []> saveDouble;
    private static final TIntObjectHashMap<Object []> saveObject;    


    static {
        saveInt = new TIntObjectHashMap<int []>();
        saveVector = new TIntObjectHashMap<Object[][]>();
        saveIntVector = new TIntObjectHashMap<int[][]>();
        saveBool = new TIntObjectHashMap<boolean[]>();
        saveLong = new TIntObjectHashMap<long[]>();
        saveDouble = new TIntObjectHashMap<double[]>();
        saveObject = new TIntObjectHashMap<Object[]>();
    }


    public RcSave(EnvironmentCopying env) {
        lastSavedWorldIndex = env.getWorldIndex();

        saveInt.clear();
        saveVector.clear();
        saveIntVector.clear();
        saveBool.clear();
        saveLong.clear();
        saveDouble.clear();
        saveObject.clear();
    }


    public void save(int worldIndex) {
        if (lastSavedWorldIndex >= worldIndex)
            lastSavedWorldIndex = 0;

        boolean [] tmpbool =  new boolean[currentElementB.length];
        for (int i = currentElementB.length ; --i>=0; ) {
            tmpbool[i] = currentElementB[i].deepCopy();
        }
        saveBool.put(worldIndex,tmpbool);

        int [] tmpint = new int [currentElementI.length];
        for (int i = currentElementI.length ; --i>=0; ) {
            tmpint[i] = currentElementI[i].deepCopy();
        }
        saveInt.put(worldIndex,tmpint);

        if(currentElementV.length > 0){
            Object[][] tmpvec = new Object[currentElementV.length][];
            for (int i = currentElementV.length ; --i>=0; ) {
                if (worldIndex != 0 && lastSavedWorldIndex >= (currentElementV[i]).getTimeStamp() )
                    tmpvec[i] = saveVector.get(lastSavedWorldIndex)[i];
                else
                    tmpvec[i] = currentElementV[i].deepCopy();
            }
            saveVector.put(worldIndex,tmpvec);
        }else{
            saveVector.put(worldIndex,new Object[0][0]);    
        }

        if(currentElementIV.length>0){
            int[][] tmpintvec = new int [currentElementIV.length][];
            for (int i = currentElementIV.length ; --i>=0; ) {
                if (worldIndex != 0 && lastSavedWorldIndex >= (currentElementIV[i]).getTimeStamp() )
                    tmpintvec[i] = saveIntVector.get(lastSavedWorldIndex)[i];
                else
                    tmpintvec[i] = currentElementIV[i].deepCopy();
            }
            saveIntVector.put(worldIndex,tmpintvec);
        }else{
            saveIntVector.put(worldIndex,new int[0][0]);
        }

        long [] tmplong = new long [currentElementL.length];
        for (int i = currentElementL.length ; --i>=0; ) {
            tmplong[i] = currentElementL[i].deepCopy();
        }
        saveLong.put(worldIndex,tmplong);

        if(currentElementD.length>0){
            double[] tmpdouble = new double [currentElementD.length];
            for (int i = currentElementD.length ; --i>=0; ) {
                tmpdouble[i] = currentElementD[i].deepCopy();
            }
            saveDouble.put(worldIndex,tmpdouble);
        }else{
            saveDouble.put(worldIndex,new double[0]);
        }

        if(currentElementO.length > 0){
            Object[] tmpobject = new Object[currentElementO.length];
            for (int i = currentElementO.length ; --i>=0; ) {
                if (worldIndex != 0 && lastSavedWorldIndex >= (currentElementO[i]).getTimeStamp() )
                    tmpobject[i] = saveObject.get(lastSavedWorldIndex)[i];
                else
                    tmpobject[i] = currentElementO[i].deepCopy();
            }
            saveObject.put(worldIndex,tmpobject);
        }else{
            saveObject.put(worldIndex,new Object[0]);
        }


        lastSavedWorldIndex = worldIndex;


    }

    public void restore(int worldIndex) {
        boolean[] tmpbool = saveBool.get(worldIndex);
        int[] tmpint = saveInt.get(worldIndex);
        Object[][] tmpvec = saveVector.get(worldIndex);
        int[][] tmpintvec = saveIntVector.get(worldIndex);
        long[] tmplong = saveLong.get(worldIndex);
        double[] tmpdouble = saveDouble.get(worldIndex);
        Object[] tmpobject = saveObject.get(worldIndex);
        //saveVector.remove(worldIndex);

        for (int i = tmpbool.length ; --i>=0; )
            currentElementB[i]._set(tmpbool[i], worldIndex);
        for (int i = tmpint.length ; --i >=0 ; )
            currentElementI[i]._set(tmpint[i], worldIndex);
        for (int i = tmpvec.length ; --i>=0;)
            currentElementV[i]._set(tmpvec[i], worldIndex);
        for (int i = tmpintvec.length ; --i>=0;)
            currentElementIV[i]._set(tmpintvec[i], worldIndex);
        for (int i = tmplong.length ; --i>=0;)
            currentElementL[i]._set(tmplong[i], worldIndex);
        for (int i = tmpdouble.length ; --i>=0;)
            currentElementD[i]._set(tmpdouble[i], worldIndex);
        for (int i = tmpobject.length ; --i>=0;)
            currentElementO[i]._set(tmpobject[i], worldIndex);

//        if (worldIndex == 0)
//            clearMaps();
//        else
//            remove(worldIndex+1);
  //          removeLast();


    }

    public static void remove(int worldIndex) {
//        saveInt.remove(worldIndex);
//        saveVector.remove(worldIndex);
//        saveIntVector.remove(worldIndex);
//        saveBool.remove(worldIndex);
//        saveLong.remove(worldIndex);
//        saveDouble.remove(worldIndex);
//        saveObject.remove(worldIndex);
    }


    private static void clearMaps() {
        saveInt.clear();
        saveVector.clear();
        saveIntVector.clear();
        saveBool.clear();
        saveLong.clear();
        saveDouble.clear();
        saveObject.clear();
    }


    public int getType() {
        return -1;
    }

    public int getTimeStamp() {
        return 0;
    }
}
