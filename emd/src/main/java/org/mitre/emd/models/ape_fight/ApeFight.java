package org.mitre.emd.models.ape_fight;

import org.mitre.emd.ApeFightMasonProblem;
import sim.engine.*;
import sim.util.*;
import sim.field.network.*;

import java.util.ArrayList;
import java.lang.Math;

public class ApeFight extends SimState {
    public Primate currentPrimate;
    private Integer fightCount = 0;
    private int totalTicks;
    private ArrayList<Integer> fightSizeList = new ArrayList<>();
    private Network primateSocialNetwork = new Network(false);
    public ApeFightMasonProblem problem = new ApeFightMasonProblem();  // TODO does this need to be instantiated here?
    private int previousFightCount = 0;
    private int numPrimates = 50;
    private double sizeThold = 0.4;
    private double hierThold = 1.9;

    public ApeFight(int totalTicks, long seed, String rules) {
        super(seed);
        createRules(rules);
        this.totalTicks = totalTicks;
        // round numPrimates to nearest square
        numPrimates = (int) Math.pow(Math.round(Math.sqrt(numPrimates)), 2);
    }

    private void createRules(String rules) {

        // TODO create the evaluation function based on the rules passed in from ECJ
    }

    // generate small world primate social network using Kleinberg model
    public void generateSmallWorld(double clusterExp) {
        Bag primates = primateSocialNetwork.getAllNodes();
        int numRows = (int) Math.round(Math.sqrt(numPrimates));
        int numColumns = (int) Math.round(Math.sqrt(numPrimates));

        // construct lattice graph
        for (int i = 0; i < numPrimates; i++) {
            Object primate = primates.get(i);

            // create link with right neighbor
            if ((i + 1) % numColumns != 0) {
                Object rightPrimate = primates.get(i + 1);
                double fightProb = random.nextDouble();

                primateSocialNetwork.addEdge(primate, rightPrimate, fightProb);
            }

            // create link with lower neighbor
            if (i < numColumns * (numRows - 1)) {
                Object lowerPrimate = primates.get(i + numColumns);
                double fightProb = random.nextDouble();

                primateSocialNetwork.addEdge(primate, lowerPrimate, fightProb);
            }
        }

        // add long range links
        for (int i = 0; i < numPrimates; i++) {
            for (int j = 0; j < numPrimates; j++) {
                // check that nodes are not adjacent
                if (i != j && i != j + 1 && i != j - 1 && i != j + numColumns && i != j - numColumns) {
                    // calculate Manhattan distance between nodes
                    int distance = Math.abs((i / numColumns) - (j / numColumns)) + Math.abs((i % numColumns) - (j % numColumns));
                    double linkProb = 1 / Math.pow(distance, clusterExp);

                    if (linkProb > random.nextDouble()) {
                        Object fromNode = primates.get(i);
                        Object toNode = primates.get(j);
                        Bag existingEdges = primateSocialNetwork.getEdges(fromNode, toNode, null);

                        // if no such edge already exists, create it
                        if (existingEdges.size() == 0) {
                            double fightProb = random.nextDouble();

                            primateSocialNetwork.addEdge(fromNode, toNode, fightProb);
                        }
                    }
                }
            }
        }
    }

    // create primate social hierarchy
    public void createHierarchy() {
        Bag primates = primateSocialNetwork.getAllNodes();
        int[] hierarchy = new int[numPrimates];

        // initalize int array from 1 to n
        for (int i = 1; i <= numPrimates; i++) {
            hierarchy[i - 1] = i;
        }

        // randomize array order
        for (int j = 0; j < numPrimates; j++) {
            int swapIndex = random.nextInt(numPrimates);
            int temp = hierarchy[j];
            
            hierarchy[j] = hierarchy[swapIndex];
            hierarchy[swapIndex] = temp;
        }

        // assign rank to primates
        for (int k = 0; k < numPrimates; k++) {
            Primate primate = (Primate) primates.get(k);
            primate.setMyRank(hierarchy[k]);
        }
    }

    // randomly select two primates to start fighting
    public void pickFighters() {
        Bag primates = primateSocialNetwork.getAllNodes();
        int randomFirst = random.nextInt(numPrimates);
        int randomSecond = random.nextInt(numPrimates);

        // ensure two unique values
        while (randomFirst == randomSecond) {
            randomSecond = random.nextInt(numPrimates);
        }

        Primate firstFighter = (Primate) primates.get(randomFirst);
        Primate secondFighter = (Primate) primates.get(randomSecond);

        firstFighter.setMyFighting(true);
        secondFighter.setMyFighting(true);
    }

    // set all fighting to false
    public void resetFighting() {
        Bag primates = primateSocialNetwork.getAllNodes();

        for (int i = 0; i < numPrimates; i++) {
            Primate primate = (Primate) primates.get(i);

            primate.setMyFighting(false);
        }
    }

    // count number of primates fighting
    public int countFighting() {
        Bag primates = primateSocialNetwork.getAllNodes();
        int fightCount = 0;

        for (int i = 0; i < numPrimates; i++) {
            Primate primate = (Primate) primates.get(i);

            if (primate.getMyFighting()) {
                fightCount += 1;
            }
        }

        return fightCount;
    }

    // calculate proportion of a primate's links fighting
    public double proportionLinksFighting(Primate primate) {
        Bag links = primateSocialNetwork.getEdgesIn(primate);
        double linksFighting = 0;
        int numLinks = links.size();

        for (int i = 0; i < links.size(); i++) {
            Edge link = (Edge) links.get(i);
            Primate otherNode = (Primate) link.getOtherNode(primate);

            if (otherNode.getMyFighting()) {
                linksFighting += 1;
            }
        }

        return (linksFighting / numLinks);
    }

    // calculate mean size of primates fighting
    public double meanFightingSize() {
        Bag primates = primateSocialNetwork.getAllNodes();
        double totalSize = 0;
        int fightCount = 0;

        for (int i = 0; i < numPrimates; i++) {
            Primate primate = (Primate) primates.get(i);

            if (primate.getMyFighting()) {
                totalSize += primate.getMySize();
                fightCount += 1;
            }
        }

        return (totalSize / fightCount);
    }

    // calculate mean rank of primates fighting
    public double meanFightingRank() {
        Bag primates = primateSocialNetwork.getAllNodes();
        double totalRank = 0;
        int fightCount = 0;

        for (int i = 0; i < numPrimates; i++) {
            Primate primate = (Primate) primates.get(i);

            if (primate.getMyFighting()) {
                totalRank += primate.getMyRank();
                fightCount += 1;
            }
        }

        return (totalRank / fightCount);
    }

    public void start() {
        super.start();

        for (int i = 0; i < numPrimates; i++) {
            int newSize = random.nextInt(2) + 1;
            double newSizeThold = sizeThold + (random.nextGaussian() / 2);
            double newHierThold = hierThold + random.nextGaussian();
            Primate primate = new Primate(newSize, newSizeThold, newHierThold);

            primateSocialNetwork.addNode(primate);
            schedule.scheduleRepeating(primate);
        }
        double clusterExp = 2;

        generateSmallWorld(clusterExp);
        createHierarchy();
    }

    public void runSim(){
        while (schedule.getSteps() < totalTicks) {
            fightCount = this.countFighting();
            if (fightCount == 0) {
                this.pickFighters();
            }

            schedule.step(this);
            fightCount = this.countFighting();
            if (fightCount == this.previousFightCount) {
                this.fightSizeList.add(fightCount);
                this.resetFighting();
//                System.out.println(fightCount + " ");
            } else {
                this.previousFightCount = fightCount;
            }
        }

        this.finish();
    }
    
    public static void main(String[] args) {
        // TODO set parameters from input args
        Long seed = System.currentTimeMillis();
        Integer totalTicks = 1200;

        ApeFight state = new ApeFight(totalTicks, seed, "");  // TODO set rules to something useful
        state.start();
        state.runSim();
    }

    public int getNumPrimates() {
        return numPrimates;
    }

    public ArrayList<Integer> getFightSizeList() {
        return fightSizeList;
    }

    public void setFightSizeList(ArrayList<Integer> fightSizeList) {
        this.fightSizeList = fightSizeList;
    }
}