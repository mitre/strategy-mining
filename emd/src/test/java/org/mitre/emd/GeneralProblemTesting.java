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
    private File newParamsFile = null;

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
        this.newParamsFile = new File(ECJParams);
        //runECJ(this.newParamsFile);
    }

    @Test
    public void testFidelity() {
        File outputFile = new File(getOutputPath());
        File firstOutputFile = new File("first_" + getOutputPath());

        // assert output file exists
        assertTrue(outputFile.exists());
        if (outputFile.exists()) {
            outputFile.renameTo(firstOutputFile);
        }
        runECJ(this.newParamsFile);

        try {
            byte[] firstFile = readFile(firstOutputFile);
            byte[] secondFile = readFile(outputFile);

            // assert file contents are equal
            assertEquals(firstFile, secondFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void runECJ(File parameterFile) {
        try {
            ParameterDatabase dbase = new ParameterDatabase(parameterFile,
                    new String[] {"-file", parameterFile.getCanonicalPath() });
            ParameterDatabase child = new ParameterDatabase(); 
            child.addParent(dbase);
            Output out = Evolve.buildOutput();
            EvolutionState evaluatedState = Evolve.initialize(child, 0, out); 
            evaluatedState.run(EvolutionState.C_STARTED_FRESH);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static byte[] readFile(File file) throws IOException {   
        byte[] buffer = new byte[(int) file.length()];   
        FileInputStream fis = new FileInputStream(file);   

        fis.read(buffer);   
        fis.close();   

        return buffer;   
    }
}