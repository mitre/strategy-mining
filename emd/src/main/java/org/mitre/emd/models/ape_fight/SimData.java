package org.mitre.emd.models.ape_fight;

import ec.gp.GPData;
import sim.engine.SimState;

public class SimData extends GPData {
    public Boolean result = false;

    public void copyTo(GPData other){
        ((SimData)other).result = result;
    }

    public Object clone(){
        SimData other = (SimData) (super.clone());
        other.result = result;
        return other;
    }

    public Boolean getResult() {
        return result;
    }

    public void setResult(Boolean result) {
        this.result = result;
    }
}
