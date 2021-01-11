package org.mitre.emd;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import java.io.*;
import java.nio.file.Path;
import java.util.*;
import java.nio.file.*;

import org.mitre.emd.EvolutionaryModelDiscovery;

/**
 * The EvolutionaryModelDiscoveryTesting class contains functions designed to test the correctness of the 
 * EvolutionaryModelDiscovery class. The private variables within the class are first populated by reading
 * input from the custom .params file in the setup function and then running an instance of main() from
 * the EMD class. The remaining functions contain tests using JUnit assertions to check for correctness in
 * the rules files and new .params file created within the EMD class for use in ECJ evolutions.
 */
public class EvolutionaryModelDiscoveryTesting extends ParentTest {
    /**
     * The setup function must be called before test functions are run. Class variables are populated 
     * here from the custom .params file similar to the way in which they are populated within the EMD 
     * class. The call to main() from EMD creates the files that are tested by the other functions
     * in this class.
     *
     * TODO Change this as needed with refactoring of EvolutionaryModelDiscovery. Use @Before decorator to set up what's needed for testing.
     */
    @Before
    public void setup() {
        init();
    }

    /**
     * This function tests the .java rule files that are generated by the EMD class from the .nls
     * factors file. It asserts that for each factor, a rule file is created in the correct directory
     * path and also that all required functions are found with expected input and output parameters.
     */
    @Test 
    public void testRuleFiles() {
        try {
            Scanner scan = new Scanner(getFactorsFile());

            while(scan.hasNextLine()) {
                String line = scan.nextLine();

                if(line.contains("@EMD @Factor") && scan.hasNextLine()) {
                    line = scan.nextLine();
                    String[] splitLine = line.split(" ");
                    String factor = splitLine[1];
                    File ruleFile = new File(Paths.get("").toAbsolutePath() + "/src/main/java/org/mitre/emd/rules/" + factor + ".java");

                    // assert rules files are created in correct directory
                    assertTrue(ruleFile.exists());
                    if (ruleFile.exists()) {
                        Scanner ruleScan = new Scanner(ruleFile);
                        String toString = "";
                        int numParams = 0;
                        Boolean toStringFound = false;
                        Boolean evalFound = false;
                        Boolean expectedChildrenFound = false;

                        if (splitLine.length > 2) {
                            numParams = EvolutionaryModelDiscovery.countParams(line);
                        }
                        if (numParams > 0) {
                            toString = "\" " + factor + " \"";
                        } else {
                            toString = "\" ( " + factor + " ) \"";
                        }
                        while (ruleScan.hasNextLine()) {
                            String ruleLine = ruleScan.nextLine();
                            if (ruleLine.contains("public String toString()")) {
                                while (!ruleLine.contains("return") && ruleScan.hasNextLine()) {
                                    ruleLine = ruleScan.nextLine();
                                }
                                if (ruleLine.contains("return")) {
                                    String subString = ruleLine.substring(ruleLine.indexOf("return")+7, ruleLine.indexOf(";"));
                                    toStringFound = true;
                                    // assert to string function returns function name
                                    assertEquals(subString, toString);
                                }
                            }
                            if (ruleLine.contains("public void eval(EvolutionState state, int thread, GPData input, ADFStack stack, GPIndividual individual, Problem problem)")) {
                                evalFound = true;
                            }
                            if (ruleLine.contains("public int expectedChildren()")) {
                                while (!ruleLine.contains("return") && ruleScan.hasNextLine()) {
                                    ruleLine = ruleScan.nextLine();
                                }
                                if (ruleLine.contains("return")) {
                                    String subString = ruleLine.substring(ruleLine.indexOf("return")+7, ruleLine.indexOf(";"));
                                    expectedChildrenFound = true;
                                    // assert expected children function returns number of funciton input parameters
                                    assertEquals(subString, Integer.toString(numParams));
                                }
                            }
                        }
                        // assert all required functions found
                        assertTrue(toStringFound);
                        assertTrue(evalFound);
                        assertTrue(expectedChildrenFound);
                        ruleScan.close();
                    }
                }
            }
            scan.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * This function tests the new .params file that is created by the EMD class from the original
     * .params file for use in ECJ. First it asserts that the file is created in the correct directory,
     * then checks for correctness.
     */
    @Test
    public void testNewParamsFile() {
        String newParamsName = getParamsPath().substring(0, getParamsPath().length()-7) + "." + getModelFile().getName() + ".params";
        File newParamsFile = new File(newParamsName);

        // assert new params file is created in correct directory
        assertTrue(newParamsFile.exists());
        if (newParamsFile.exists()) {
            try {
                Scanner scan = new Scanner(getFactorsFile());
                ArrayList<ArrayList<Object>> factors = new ArrayList<ArrayList<Object>>();

                while(scan.hasNextLine()) {
                    String line = scan.nextLine();

                    if(line.contains("@EMD @Factor @return-type") && scan.hasNextLine()) {
                        ArrayList<Object> factor = new ArrayList<Object>();
                        ArrayList<Object> params = new ArrayList<Object>();
                        String[] splitLine = line.split(" ");
                        String returnType = "";
                        int numParams = 0;

                        for (int i = 0; i < splitLine.length; i++) {
                            if (splitLine[i].contains("@return-type")) {
                                returnType = splitLine[i].split("=")[1];
                            }
                            if (splitLine[i].contains("@parameter-type")) {
                                params.add(splitLine[i].split("=")[1]);
                            }
                        }
                        line = scan.nextLine();
                        splitLine = line.split(" ");
                        if (splitLine.length > 2) {
                            numParams = EvolutionaryModelDiscovery.countParams(line);
                        }
                        factor.add(returnType);
                        factor.add(numParams);
                        factor.add(splitLine[1]);
                        if (params.size() > 0) {
                            factor.add(params);
                        }
                        factors.add(factor);
                    }
                }
                scan.close();
                scan = new Scanner(newParamsFile);
                ArrayList<Object> factor = factors.get(0);
                String foundFactorsStr = "0";
                int foundFactors = 0;
                int numParams = 0;
                
                while(scan.hasNextLine()) {
                    String line = scan.nextLine();

                    if (foundFactors < factors.size()) {
                        factor = factors.get(foundFactors);
                        if (line.contains("gp.nc." + foundFactorsStr)) {
                            // assert function set is correct
                            assertEquals(line, "gp.nc." + foundFactorsStr + " = ec.gp.GPNodeConstraints");
                            if (scan.hasNextLine()) {
                                line = scan.nextLine();
                                // assert factor name is correct
                                assertEquals(line, "gp.nc." + foundFactorsStr + ".name = nc" + foundFactorsStr);
                                if (scan.hasNextLine()) {
                                    line = scan.nextLine();
                                    // assert factor return type is correct
                                    assertEquals(line, "gp.nc." + foundFactorsStr + ".returns = " + factor.get(0));
                                    if (scan.hasNextLine()) {
                                        line = scan.nextLine();
                                        // assert number of factor parameters is correct
                                        assertEquals(line, "gp.nc." + foundFactorsStr + ".size = " + factor.get(1));
                                        if (factor.get(1) instanceof Number) {
                                            numParams = ((Number) factor.get(1)).intValue();
                                        } else {
                                            numParams = 0;
                                        }
                                        if (numParams > 0 && factor.get(3) instanceof ArrayList<?>) {
                                            ArrayList<?> params = (ArrayList<?>) factor.get(3);

                                            for (int i = 0; i < numParams && scan.hasNextLine(); i++) {
                                                line = scan.nextLine();
                                                // assert parameter type is correct if relevant
                                                assertEquals(line, "gp.nc." + foundFactorsStr + ".child." + Integer.toString(i) + " = " + params.get(i));
                                            }
                                        }
                                        if (scan.hasNextLine()) {
                                            String packageName = "org.mitre.emd";

                                            line = scan.nextLine();
                                            // assert function points to correct directory
                                            assertEquals(line, "gp.fs.0.func." + foundFactorsStr + " = " + packageName + ".rules." + factor.get(2));
                                            if (scan.hasNextLine()) {
                                                line = scan.nextLine();
                                                // assert function points to correct factor
                                                assertEquals(line, "gp.fs.0.func." + foundFactorsStr + ".nc = nc" + foundFactorsStr);
                                                if (scan.hasNextLine()) {
                                                    foundFactors++;
                                                    foundFactorsStr = Integer.toString(foundFactors);
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                scan.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * This function verifies that the output file is created in the correct directory.
     */
    @Test
    public void testOutputFile() {
        File outputFile = new File(getOutputPath());

        // assert output file is created in the correct directory
        assertTrue(outputFile.exists());
    }
}
