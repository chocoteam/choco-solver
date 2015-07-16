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
	
    public RandomPlayer(int nbArms) {
		super(nbArms);
		rand=new Random();
	}

    @Override
	public int chooseArm(){
    	return armsOrder[rand.nextInt(nbArms)];
    }

    
	
}

