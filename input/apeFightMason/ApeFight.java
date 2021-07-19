import sim.engine.*;
import sim.util.*;
import sim.field.network.*;

import java.util.ArrayList;
import java.util.List;
import java.lang.Math;

public class ApeFight extends SimState {
    private List<Integer> fightSizeList = new ArrayList<>();
    private Network primateSocialNetwork = new Network(false);
    private int previousFightCount = 0;
    private int numPrimates = 50;
    private double sizeThold = 0.4;
    private double hierThold = 1.9;

    public ApeFight(long seed) {
        super(seed);
        
        // round numPrimates to nearest square
        numPrimates = (int) Math.pow(Math.round(Math.sqrt(numPrimates)), 2);
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
    
    public static void main(String[] args) {
        ApeFight state = new ApeFight(System.currentTimeMillis());
        int ticks = 1200;
        int fightCount = 0;

        state.start();

        while (state.schedule.getSteps() < ticks) {
            fightCount = state.countFighting();
            if (fightCount == 0) {
                state.pickFighters();
            }

            state.schedule.step(state);
            fightCount = state.countFighting();
            if (fightCount == state.previousFightCount) {
                state.fightSizeList.add(fightCount);
                state.resetFighting();
                System.out.print(fightCount + " ");
            } else {
                state.previousFightCount = fightCount;
            }
        }

        state.finish();
        System.exit(0);
    }

    public int getNumPrimates() {
        return numPrimates;
    }
}

public class Primate implements Steppable {
    private boolean andCombo = false;
    private boolean myFighting = false;
    private int mySize = 0;
    private int myRank = 0;
    private double mySizeThold = 0;
    private double myHierThold = 0;

    public Primate(int size, double sizeThold, double hierThold) {
        super();

        mySize = size;
        mySizeThold = sizeThold;
        myHierThold = hierThold;
    }

    public void step(SimState state) {
        // new rules go here
        if (zOrCombo(minorityFighting(state), sizeInThreshold(state))) {
            myFighting = true;
        }
    }

    public boolean zAndCombo(boolean cond1, boolean cond2) {
        return (cond1 && cond2);
    }

    public boolean zOrCombo(boolean cond1, boolean cond2) {
        return (cond1 || cond2);
    }

    public boolean majorityFighting(SimState state) {
        ApeFight sim = (ApeFight) state;

        if ((double) sim.countFighting() / sim.getNumPrimates() > 0.5) {
            return true;
        } else {
            return false;
        }
    }

    public boolean minorityFighting(SimState state) {
        ApeFight sim = (ApeFight) state;

        if ((double) sim.countFighting() / sim.getNumPrimates() < 0.5) {
            return true;
        } else {
            return false;
        }
    }

    public boolean majorityLinksFighting(SimState state) {
        ApeFight sim = (ApeFight) state;
        
        if (sim.proportionLinksFighting(this) > 0.5) {
            return true;
        } else {
            return false;
        }
    }

    public boolean minorityLinksFighting(SimState state) {
        ApeFight sim = (ApeFight) state;
        
        if (sim.proportionLinksFighting(this) < 0.5) {
            return true;
        } else {
            return false;
        }
    }

    public boolean sizeInThreshold(SimState state) {
        ApeFight sim = (ApeFight) state;

        if (sim.countFighting() > 0) {
            if (sim.meanFightingSize() - mySize < mySizeThold) {
                return true;
            } else {
                return false;
            }
        } else {
            return true;
        }
    }

    public boolean rankInThreshold(SimState state) {
        ApeFight sim = (ApeFight) state;

        if (sim.countFighting() > 0) {
            if (sim.meanFightingRank() - myRank < myHierThold) {
                return true;
            } else {
                return false;
            }
        } else {
            return true;
        }
    }

    public void setMyFighting(boolean fighting) {
        myFighting = fighting;
    }

    public boolean getMyFighting() {
        return myFighting;
    }

    public int getMySize() {
        return mySize;
    }

    public void setMyRank(int rank) {
        myRank = rank;
    }

    public int getMyRank() {
        return myRank;
    }
}