/*
 * This file is part of examples, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.examples.integer;

import org.chocosolver.examples.AbstractProblem;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.variables.IntVar;

/**
 * Simple example which solve Zebra puzzle
 * <br/>
 *
 * @author GK
 * @since 29/01/19
 */
public class Zebra extends AbstractProblem {

    private final String[] sHouse = {"House 1", "House 2", "House 3", "House 4", "House 5"}; // 1. five houses
    private final int SIZE = sHouse.length;
    private final int NATIONALITY = 0, COLOR = 1, CIGARETTE = 2, PET = 3, DRINK = 4;
    private final String [] sAttrTitle = {"Nationality", "Color", "Cigarette", "Pet", "Drink"};
    private final String [][] sAttr = {
      {"Ukranian", "Norwegian", "Englishman", "Spaniard", "Japanese"},
      {"Red", "Blue", "Yellow", "Green", "Ivory"},
      {"Old Gold", "Parliament", "Kools", "Lucky Strike", "Chesterfield"},
      {"Zebra", "Dog", "Horse", "Fox", "Snails"},
      {"Coffee", "Tea", "Water", "Milk", "Orange juice"}
    };
    private IntVar[][] attr;
    private IntVar zebra;

    @Override
    public void buildModel() {
    
        model = new Model();
        
        attr = model.intVarMatrix("attr", SIZE, SIZE, 1, SIZE);
        
        IntVar ukr   = attr[NATIONALITY][0];
        IntVar norge = attr[NATIONALITY][1];
        IntVar eng   = attr[NATIONALITY][2];
        IntVar spain = attr[NATIONALITY][3];
        IntVar jap   = attr[NATIONALITY][4];
    
        IntVar red    = attr[COLOR][0];
        IntVar blue   = attr[COLOR][1];
        IntVar yellow = attr[COLOR][2];
        IntVar green  = attr[COLOR][3];
        IntVar ivory  = attr[COLOR][4];
      
        IntVar oldGold = attr[CIGARETTE][0];
        IntVar parly   = attr[CIGARETTE][1];
        IntVar kools   = attr[CIGARETTE][2];
        IntVar lucky   = attr[CIGARETTE][3];
        IntVar chest   = attr[CIGARETTE][4];
      
        zebra  = attr[PET][0];
        IntVar dog    = attr[PET][1];
        IntVar horse  = attr[PET][2];
        IntVar fox    = attr[PET][3];
        IntVar snails = attr[PET][4];
      
        IntVar coffee = attr[DRINK][0];
        IntVar tea    = attr[DRINK][1];
        IntVar h2o    = attr[DRINK][2];
        IntVar milk   = attr[DRINK][3];
        IntVar oj     = attr[DRINK][4];
      
        model.allDifferent(attr[COLOR]).post();
        model.allDifferent(attr[CIGARETTE]).post();
        model.allDifferent(attr[NATIONALITY]).post();
        model.allDifferent(attr[PET]).post();
        model.allDifferent(attr[DRINK]).post();
      
        eng.eq(red).post(); // 2. the Englishman lives in the red house
        spain.eq(dog).post(); // 3. the Spaniard owns a dog
        coffee.eq(green).post(); // 4. coffee is drunk in the green house
        ukr.eq(tea).post(); // 5. the Ukr drinks tea    
        ivory.add(1).eq(green).post(); // 6. green house is to right of ivory house
        oldGold.eq(snails).post(); // 7. oldGold smoker owns snails
        kools.eq(yellow).post(); // 8. kools are smoked in the yellow house
        milk.eq(3).post(); // 9. milk is drunk in the middle house
        norge.eq(1).post(); // 10. Norwegian lives in first house on the left
        chest.dist(fox).eq(1).post(); // 11. chesterfield smoker lives next door to the fox owner
        kools.dist(horse).eq(1).post(); // 12. kools smoker lives next door to the horse owner
        lucky.eq(oj).post(); // 13. lucky smoker drinks orange juice
        jap.eq(parly).post(); // 14. Japanese smokes parliament
        norge.dist(blue).eq(1).post(); // 15. Norwegian lives next to the blue house
    }

    @Override
    public void configureSearch() {
    }

    @Override
    public void solve() {
        while (model.getSolver().solve()) {
            int z = zebra.getValue();
            int n = -1;
            for (int i = 0; i < SIZE; i++) {
                if (z == attr[NATIONALITY][i].getValue()) {
                    n = i;
                }
            }
            if (n >= 0) {
                System.out.printf("%n%-13s%s%s%s%n", "",
                    "============> The Zebra is owned by the ", sAttr[NATIONALITY][n], " <============");
            }
            print(attr);
        }
    }
    private void print(IntVar[][] pos) {
        System.out.printf("%-13s%-13s%-13s%-13s%-13s%-13s%n", "", 
            sHouse[0], sHouse[1], sHouse[2], sHouse[3], sHouse[4]);
        for (int i = 0; i < SIZE; i++) {
            String[] sortedLine = new String[SIZE];
            for (int j = 0; j < SIZE; j++) {
                sortedLine[pos[i][j].getValue() - 1] = sAttr[i][j];
            }
            System.out.printf("%-13s", sAttrTitle[i]);
            for (int j = 0; j < SIZE; j++) {
                System.out.printf("%-13s", sortedLine[j]);
            }
            System.out.println();
        }
    }

    public static void main(String[] args) {
        new Zebra().execute(args);
    }
}
