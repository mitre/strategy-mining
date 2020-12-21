package org.mitre.emd;

import ec.gp.*;

public class GeneralData extends GPData
{
    public double x;    // example data values
    public double y;
    public void copyTo(final GPData gpd)   // copy my stuff to another Vector
    {
        // example copy method
        // note: this copies from the current instance to the gpd instance
        ((GeneralData)gpd).x = x;
        ((GeneralData)gpd).y = y;
    }

    public GeneralData() { }

    public String toString() {
        // example toString() method - not required, but helpful for debugging
        String retMe = "(" + x + "," + y + ")";
        return retMe;
    }
}
