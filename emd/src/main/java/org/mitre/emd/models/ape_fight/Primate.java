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

