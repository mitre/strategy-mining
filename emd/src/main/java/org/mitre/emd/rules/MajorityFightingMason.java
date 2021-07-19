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

public class MajorityFightingMason extends GPNode {
    public MajorityFightingMason() {
    }

    public String toString() {
        return " ( majorityFighting ) ";
    }

    public void eval(EvolutionState state, int thread, GPData input, ADFStack stack, GPIndividual individual, Problem problem) {
        SimData simData = (SimData) input;

        ApeFight sim = ((ApeFightMasonProblem)problem).simstate;

        if ((double) sim.countFighting() / sim.getNumPrimates() > 0.5) {
            simData.result = true;
        } else {
            simData.result = false;
        }
    }

    public int expectedChildren() {
        return 0;
    }
}
