package org.mitre.emd;

import ec.util.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
// import java.nio.file.Path;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ThreadLocalRandom;

import ec.*;
import ec.gp.*;
import ec.gp.koza.*;

import org.nlogo.headless.HeadlessWorkspace;
/**
 * The GeneralProblem class is the ECJ class that contains methods for evaluating the fitness of an individual
 * In this case, the evaluate method writes a new Netlogo model with the test rules, runs it, and calculates
 * the fitness of that model, using the metrics the user specifies. This class is designed to be a superclass,
 * so if there's any method you want to change, create a subclass and override that/those method(s). If you 
 * use the superclass to change the default behavior, you will need to specify your custom Problem class in the 
 * params file instead of GeneralProblem
 */
public class GeneralProblem extends GPProblem {
    private static final long serialVersionUID = 1;
    private List<String> factors = null;
    public String fileContents;
    public String outputPath;

    /**
     * called once at the beginning of each generation
     * can access the params file parameters using the state
     * 
     * opens the netlogo model and stores its contents in the global variable fileContents
     */
    public void setup(final EvolutionState state, final Parameter base) {
        super.setup(state, base);

        String path = state.parameters.getString(new Parameter("modelPath"), null);
        File f = new File(path).getAbsoluteFile();
        StringBuilder contents = new StringBuilder();

        String outputFileName = state.parameters.getString(new Parameter("outputFileName"), null);
        String outputFileDirectory = state.parameters.getString(new Parameter("outputFileDirectory"), null);
        outputPath = outputFileDirectory + File.separator + outputFileName;

        try {
            BufferedReader br = new BufferedReader(new FileReader(f));

            while (br.ready()) {
                contents.append(br.readLine() + "\n");
            }

            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        fileContents = contents.toString();
        // store model name from base into a state variable
        // topLevel = Paths.get(".").toAbsolutePath();
        // pathBase = Paths.get(topLevel.getParent().toString(), "src/main/java/org/mitre/emd");

        if (factors == null) {
            // File ruleDirectory = new File(pathBase + "/rules/");
            // File ruleDirectory = new File("../emd/src/main/java/org/mitre/emd/rules/");

            setFactors(state);
        }

        createOutputFile(outputPath);
    }

    /**
     * creates new temporary netlogo file
     * copies over every line of the model other than the replaced EMD line
     * the replaced EMD line comes from the individual's tree
     * runs the netlogo model and retrieves the metrics
     * 
     * You can, but should not need to change this method
     * 
     * evaluates one individual tree and sets its fitness
     */
    public void evaluate(EvolutionState state, Individual ind, int subpopulation, int threadnum) {
        String rules = ((GPIndividual) ind).trees[0].child.makeLispTree().replaceAll(",", "");

        /**
        * get model path from state.parameters read / write netlogo file from model
        * find @EMD tag, replace next line with rules, run netlogo model, get back metric(s)
        */
        
        String path = state.parameters.getString(new Parameter("modelPath"), null);
        String basePath = path.substring(0, path.lastIndexOf("/"));

        File file = new File(path);
        String fName = file.getName();
        String generatedModelPath = basePath + File.separator +"models";
        String newPath = generatedModelPath + File.separator + fName.substring(0, fName.lastIndexOf("."))
                + ThreadLocalRandom.current().nextLong() + fName.substring(fName.lastIndexOf("."));
        try {
            Scanner scan = new Scanner(fileContents);
            Files.createDirectories(Path.of(generatedModelPath));
            FileWriter fw = new FileWriter(new File(newPath));
            String line = "";
            boolean flag = false;
            while (scan.hasNextLine()) {
                line = scan.nextLine();
                if (flag) {
                    fw.write(rules + "\n");
                    flag = false;
                } else {
                    fw.write(line + "\n");
                }
                if (line.contains("@EvolveNextLine")) {
                    flag = true;
                }
            }
            fw.flush();
            fw.close();
            scan.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        String netlogo_extensions = state.parameters.getString(new Parameter("netlogo.extensions.dir"), null);
        if(netlogo_extensions != null){
            System.setProperty("netlogo.extensions.dir", netlogo_extensions);
        }

        runCustomModels(state, ind, newPath, rules);
    }

    /**
     * You can override this method in your custom subclass!
     * 
     * @return an array of strings containing the names of the variables we need to access
     * to determine the fitness of an individual
     */
    public String[] metricNames() {
        return new String[] { "metric" };
    }

    /**
     * You can override this method in your custom subclass!
     * 
     * @return an array of strings containing the names of the commands we need to run
     * to setup our model. note that these commands will only be run once per individual
     */
    public String[] setupCommands(EvolutionState state) {
        return new String[] { "setup" };
    }

    /**
     * You will probably want to override this method in your custom subclass!
     * 
     * Some data types that might come in handy include LogoList and Double. The default code
     * shows how to convert a numeric metric into a Java double datatype to return, but you
     * might also have your metric be a NetLogo list. In this case, the following code might
     * come in handy for converting the list to a Java array.
     * 
     * LogoList list1 = (LogoList) metrics.get(0);
     * double[] list1arr = new double[list1.size()];
     * for (int i = 0; i &lt; list1.size(); i++) {
     *      list1arr[i] = (Double) list1.get(i);
     * }
     * 
     * If your metricNames() method returns 3 Strings, the corresponding values can be accessed
     * by calling metrics.get(0), metrics.get(1), and metrics.get(2) in this method.
     * 
     * @param metrics the list containing the values returned by the reporters listed in metricNames()
     * @param state allows access to parameters if needed
     * @return the fitness of this individual, calculated based on the metrics reported
     */
    public double calculateFitness(List<Object> metrics, EvolutionState state) {
        //the default code assumes the first (or only) metric listed contains
        //the fitness of that model
        return (Double) metrics.get(0);
    }

    private void setFactors(EvolutionState state) {
        factors = new ArrayList<String>();
        String curFactor = "";

        for (int i = 0; curFactor != null; i++) {
            curFactor = state.parameters.getString(new Parameter("gp.fs.0.func." + i), null);

            if (curFactor != null) {
                String[] splitFactor = curFactor.split("\\.");
                String factorName = splitFactor[splitFactor.length - 1];
                factors.add(factorName);
            }
        }
    }

    private void setFactors(File ruleDirectory) {
        FilenameFilter filter = new FilenameFilter() {
            @Override
            public boolean accept(File f, String name) {
                return name.endsWith(".java");
            }
        };
        String[] factorFiles = ruleDirectory.list(filter);

        factors = new ArrayList<>(factorFiles.length);
        for (int i = 0; i < factorFiles.length; i++) {
            factors.add(factorFiles[i].split(".java")[0]);
        }

        factors.sort(Comparator.naturalOrder());
    }

    public void runCustomModels(EvolutionState state, Individual ind, String newPath, String rules) {
        int ticks = state.parameters.getInt(new Parameter("ticks"), null);
        HeadlessWorkspace workspace = HeadlessWorkspace.newInstance();
            
        try {
            workspace.open(newPath, true);
            for (String command : setupCommands(state)) {
                workspace.command(command);
            }
            workspace.command("repeat " + ticks + " [ go ]");
            List<Object> metrics = new ArrayList<>();
            for (String metric : metricNames()) {
                metrics.add(workspace.report(metric));
            }
                
            double realFitness = calculateFitness(metrics, state);
            KozaFitness kfitness = ((KozaFitness) ind.fitness);
            kfitness.setStandardizedFitness(state, realFitness);
            ind.evaluated = true;

            OutputWriter outputWriter = new OutputWriter(factors);
            //write info to the output file
            outputWriter.writeFile(outputPath, state.generation, rules, realFitness, metrics);
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            new File(newPath).delete();
            try {
                workspace.dispose();
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.gc();
        }
    }

    public void createOutputFile(String outputPath) {
        try {
            File outputFile = new File(outputPath);

            if (!outputFile.getParentFile().exists()) {
                outputFile.getParentFile().mkdirs();
            }

            FileWriter fw = new FileWriter(outputFile, false);
            fw.write("Gen,Rule,Fitness,Fitdata," + String.join(",", factors) + "\n");
            fw.flush();
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<String> getFactors() {
        return factors;
    }
}
