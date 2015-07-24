/**
 * 
 */

/**
 * @author Amine Balafrej
 *
 */

package org.chocosolver.util.objects.player;

import java.util.Random;

public class RandomPlayer extends UCB1Player{
	private Random rand;
	
    public RandomPlayer(int nbArms, long seed) {
		super(nbArms, seed);
		rand=new Random(seed);
	}

    @Override
	public int chooseArm(){
    	return armsOrder[rand.nextInt(nbArms)];
    }

    
	
}

