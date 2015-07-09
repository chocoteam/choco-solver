/**
 * 
 */

/**
 * @author Amine Balafrej
 *
 */

package org.chocosolver.util.objects.player;

import java.util.Random;

public class CPURandomPlayer extends CPUPlayer{
	private Random rand;
	
    public CPURandomPlayer(int nbArms) {
		super(nbArms);
		rand=new Random();
	}

    @Override
	public int chooseArm(){
    	return armsOrder[rand.nextInt(nbArms)];
//    	return armsOrder[(int)(armsTemps.length * Math.random())];
    }

    
	
}

