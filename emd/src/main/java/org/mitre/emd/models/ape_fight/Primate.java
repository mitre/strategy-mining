package org.mitre.emd.models.ape_fight;

import ec.gp.GPIndividual;
import org.mitre.emd.ApeFightMasonProblem;
import sim.engine.SimState;
import sim.engine.Steppable;

public class Primate implements Steppable{
    private boolean andCombo = false;
    private boolean myFighting = false;
    public int mySize = 0;
    public int myRank = 0;
    public double mySizeThold = 0;
    public double myHierThold = 0;

    public Primate(int size, double sizeThold, double hierThold) {
        super();
        mySize = size;
        mySizeThold = sizeThold;
        myHierThold = hierThold;
    }

    @Override
    public void step(SimState simState) {
        ApeFight model = (ApeFight) simState;
        model.currentPrimate = this;
        SimData data = (SimData) model.problem.input;
        GPIndividual ind = model.problem.ind;
        ind.trees[0].child.eval(model.problem.evolutionState,
                model.problem.threadNum,
                model.problem.input,
                model.problem.stack,
                ind,
                model.problem);
        myFighting = data.result;
    }

    /**
     *
     * The methods below aren't used anymore. Instead factors have been created for each in the rules package
     */

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

