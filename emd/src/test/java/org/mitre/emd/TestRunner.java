package org.mitre.emd;

import org.junit.runner.JUnitCore;
import org.junit.internal.TextListener;

public class TestRunner {
    public static void main(String[] args) {
        JUnitCore junit = new JUnitCore();
        junit.addListener(new TextListener(System.out));
        junit.run(EvolutionaryModelDiscoveryTesting.class);
        junit.run(GeneralProblemTesting.class);
    }
}
