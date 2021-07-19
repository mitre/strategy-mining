package org.mitre.emd;

import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

import ec.EvolutionState;
import ec.Evolve;
import ec.util.*;

import java.io.*;
import java.util.*;

/**
 * TODO: Comment more thoroughly 
 * The GeneralProblemTesting class contains functions designed to test the correctness 
 * of the GeneralProblem class and related ECJ functionality. The private variables 
 * within the class are first populated by reading input from the custom .params file 
 * in the setup function and then running an instance of main() from the EMD class.
 */
public class GeneralProblemTesting extends ParentTest {
    private String[] args = null;
    private String netlogoDir = null;

    /**
     * The setup function must be called before test functions are run. Class
     * variables are populated here from the custom .params file similar to the way
     * in which they are populated within the EMD class. The call to main() from EMD
     * creates the files that are tested by the other functions in this class.
     */
    @Before
    public void setup() {
        init();
        this.setNewParams();
        this.args = new String[] { "-file", getNewParams() };
    }

    @Test
    public void testRunnable() {
        String testPath = "../emd/src/test/test_output/test_output.csv";

        runECJ(args, testPath);
        File testFile = new File(testPath);
        
        // assert test file exists
        assertTrue(testFile.exists());
        testFile.delete();
    }

    public void runECJ(String[] params, String output) {
        ParameterDatabase dbase = Evolve.loadParameterDatabase(params);
        ParameterDatabase child = new ParameterDatabase(); 

        child.set(new Parameter("outputPath"), output);
        child.set(new Parameter("generations"), "2");
        child.set(new Parameter("pop.subpop.0.size"), "2");
        child.set(new Parameter("breedthreads"), "auto");
        child.set(new Parameter("evalthreads"), "auto");
        child.set(new Parameter("ticks"), "100");
        child.addParent(dbase);
        Output out = Evolve.buildOutput();
        EvolutionState evaluatedState = Evolve.initialize(child, 0, out); 
        evaluatedState.run(EvolutionState.C_STARTED_FRESH);
    }
}