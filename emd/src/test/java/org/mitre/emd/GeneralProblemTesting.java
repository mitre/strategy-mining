package org.mitre.emd;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import ec.EvolutionState;
import ec.Evolve;
import ec.util.*;

import java.io.*;
import java.util.*;
import java.nio.file.*;

import org.mitre.emd.EvolutionaryModelDiscovery;

/**
 * TODO: Comment more thoroughly The GeneralProblemTesting class contains
 * functions designed to test the correctness of the GeneralProblem class and
 * related ECJ functionality. The private variables within the class are first
 * populated by reading input from the custom .params file in the setup function
 * and then running an instance of main() from the EMD class.
 */
public class GeneralProblemTesting extends ParentTest {
    private String[] newParams = null;

    /**
     * The setup function must be called before test functions are run. Class
     * variables are populated here from the custom .params file similar to the way
     * in which they are populated within the EMD class. The call to main() from EMD
     * creates the files that are tested by the other functions in this class.
     */
    @Before
    public void setup() {
        init();
        String ECJParams = "src/test/resources/testParams.apeFight_02.nlogo.params";
        this.newParams = new String[] { "-file", ECJParams };
        File outputFile = new File(getOutputPath());

        try {
            FileWriter fw = new FileWriter(outputFile, false);

            fw.write("Gen,Rule,Fitness,Fitdata\n");
            fw.flush();
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        runECJ(this.newParams);
    }

    @Test
    public void testFidelity() {
        File outputFile = new File(getOutputPath());
        File testFile = new File("src/test/test_output/fidelity_test.csv");

        // assert output file exists
        assertTrue(outputFile.exists());

        // assert test file exists
        assertTrue(testFile.exists());

        try {
            byte[] firstFile = Files.readAllBytes(testFile.toPath());
            byte[] secondFile = Files.readAllBytes(outputFile.toPath());

            System.out.println(firstFile);
            System.out.println(secondFile);

            // assert file contents are equal
            assertTrue(Arrays.equals(firstFile, secondFile));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void runECJ(String[] params) {
        ParameterDatabase dbase = Evolve.loadParameterDatabase(params);
        ParameterDatabase child = new ParameterDatabase(); 

        child.addParent(dbase);
        Output out = Evolve.buildOutput();
        EvolutionState evaluatedState = Evolve.initialize(child, 0, out); 
        evaluatedState.run(EvolutionState.C_STARTED_FRESH);
    }

    public static byte[] readFile(File file) throws IOException {   
        byte[] buffer = new byte[(int) file.length()];   
        FileInputStream fis = new FileInputStream(file);   

        fis.read(buffer);   
        fis.close();   

        return buffer;   
    }
}