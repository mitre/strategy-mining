package org.mitre.emd.rules;

import ec.EvolutionState;
import ec.Problem;
import ec.gp.ADFStack;
import ec.gp.GPData;
import ec.gp.GPIndividual;
import ec.gp.GPNode;
import org.mitre.emd.ApeFightMasonProblem;
import org.mitre.emd.models.ape_fight.ApeFight;
import org.mitre.emd.models.ape_fight.SimData;

public class ZandComboMason extends GPNode{
    public String toString() {
        return " zAndCombo ";
    }
    @Override
    public void eval(EvolutionState state, int thread, GPData input, ADFStack stack, GPIndividual individual, Problem problem) {
        SimData simData = (SimData) input;

        Boolean inputOne = false;
        Boolean inputTwo = false;
        children[0].eval(state,thread,input,stack,individual,problem);
        inputOne = simData.result;

        children[1].eval(state,thread,input,stack,individual,problem);
        inputTwo = simData.result;

        simData.result = inputOne && inputTwo;
    }
    public int expectedChildren() { return 2; }
}
