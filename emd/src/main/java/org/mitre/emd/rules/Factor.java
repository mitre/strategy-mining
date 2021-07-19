package org.mitre.emd.rules;

import ec.EvolutionState;
import ec.Problem;
import ec.gp.ADFStack;
import ec.gp.GPData;
import ec.gp.GPIndividual;
import ec.gp.GPNode;

public class Factor extends GPNode {
//public class Factor {
    String description = "Unknown Factor";

    @Override
    public String toString() {
        return description;
    }

    @Override
    public void eval(EvolutionState state, int thread, GPData input, ADFStack stack, GPIndividual individual, Problem problem) {
        // Filled in from other methods
    }

    @Override
    public int expectedChildren() {
        return 0;
    }

    public void setDescription(String description){
        this.description = description;
    }
}
