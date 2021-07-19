package org.mitre.emd.rules;

import ec.EvolutionState;
import ec.Problem;
import ec.gp.ADFStack;
import ec.gp.GPData;
import ec.gp.GPIndividual;
import ec.gp.GPNode;
import org.mitre.emd.models.ape_fight.SimData;

public class ZorComboMason extends GPNode{
    public String toString() {
        return " zOrCombo ";
    }
    @Override
    public void eval(EvolutionState state, int thread, GPData input, ADFStack stack, GPIndividual individual, Problem problem) {
        SimData simData = (SimData) input;

        Boolean inputOne;
        Boolean inputTwo;
        children[0].eval(state,thread,input,stack,individual,problem);
        inputOne = simData.result;

        children[1].eval(state,thread,input,stack,individual,problem);
        inputTwo = simData.result;

        simData.result = inputOne || inputTwo;
    }
    public int expectedChildren() { return 2; }
}
