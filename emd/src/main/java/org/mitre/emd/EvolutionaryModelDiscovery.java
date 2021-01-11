package org.mitre.emd;

import java.io.*;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.*;
import java.nio.file.*;

/**
 * The EvolutionaryModelDiscovery class should only be called to run the main method.
 * It contains the entire conversion code from a netlogo model and EMD factors
 * into an ECJ problem that can run distributed, and can run locally or on a 
 * cluster. 
 * 
 * Current known limitations:
 * - all setup code must be in a function called `setup`
 * - only one metric can be reported
 * - the metric must be called `metric`
 * - the metric must do all special analysis in netlogo before returning the value
 * - negative or zero metrics are interpreted as "perfect" and will end the simulation
 * - all ECJ programs are run to minimize the objective value (metric)
 */
public class EvolutionaryModelDiscovery {
    EvolutionaryModelDiscovery(){}
    private String packageName = "org.mitre.emd";
    private String treeReturnType = "";
    private String factorsPath = "";
    private String paramsPath = "";
    private String modelPath = "";
    private String outputPath = "";
    private File factorsFile = null;    
    private File paramsFile = null;
    private File modelFile = null;
    private boolean distributed = false;
    private Path pathBase = null;
    private List<Map<String, String>> data = null;
    /**
     * This main method parses the factors.nls file argument into a Java class for each EMD function. It also generates the 
     * full params file with all of the method parameters and return types specified. This full params file has the passed
     * params file as a parent, so it will be act as a continuation of that parameter file. 
     * 
     * @param args args must contain one String, the path to the params file (.params)
     */
    public static void main(String[] args) {
        //check for the proper number of arguments
        if (args.length != 1) {
            System.out.println("Please specify the relative path to your params file as program arguments");
            System.exit(-1);
        }  

        EvolutionaryModelDiscovery emd = new EvolutionaryModelDiscovery();
        emd.paramsPath = args[0];
        emd.setParams();
        emd.setTreeReturnType();
        emd.writeRulesFiles();
        emd.writeNewParamsFile();
        emd.createOutputFile();
    }

    private void setParams() {
        try {
            this.paramsFile = new File(this.paramsPath);
            // TODO Should pathBase be relative to the project's top level directory or a user configurable path?
            Path topLevel = Paths.get("").toAbsolutePath();
            this.pathBase = Paths.get(topLevel.toString(), "src/main/java/org/mitre/emd");
//          this.pathBase = Path.of(this.paramsFile.toPath().toAbsolutePath().getParent().getParent().getParent().toString(),"src/main/java/org/mitre/emd");
            //open the original params file, and find the modelPath parameter
            //store the found parameter in appropriate variables
            Scanner scan = new Scanner(this.paramsFile);

            while(scan.hasNextLine()) {
                String line = scan.nextLine();

                if(line.contains("modelPath")) {
                    this.modelPath = line.split("=")[1].strip();
                    this.modelFile = new File(this.modelPath).getCanonicalFile();
                }

                if(line.contains("factorsPath")) {
                    this.factorsPath = line.split("=")[1].strip();
                    this.factorsFile = new File(this.factorsPath);
                }

                if(line.contains("runDistributed")) {
                    this.distributed = Boolean.parseBoolean(line.split("=")[1].strip());
                }

                if(line.contains("outputPath")) {
                    this.outputPath = line.split("=")[1].strip();
                }
            }
            scan.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setTreeReturnType() {
        try {
            //open the model file (.nlogo) to find the type the tree must return at the top-level
            Scanner scan = new Scanner(this.modelFile);

            while(scan.hasNextLine()) {
                String line = scan.nextLine();

                if(line.contains("@EvolveNextLine")) {
                    Pattern pattern = Pattern.compile("(?:[@return\\-type=]{13})([\\w-]+)");
                    Matcher matcher = pattern.matcher(line);
                    matcher.find();
                    this.treeReturnType = matcher.group(1);
                }
            }    

            scan.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } 
    }

    private void writeRulesFiles() {
        try {
            //this data object will store all of the information needed for each method in the factors file
            this.data = new ArrayList<>();
            //loop through the factors file (.nls)
            Scanner scan = new Scanner(this.factorsFile);
            int count = 0; //the total number of methods we've seen
            boolean flag = false;

            while(scan.hasNextLine()) {
                String line = scan.nextLine();

                if(line.contains("@EMD @Factor")) {
                    //EMD line looks like: ;; @EMD @Factor @return-type=farmplot @parameter-type=farmplots @parameter-type=comparator
                    //return-type required, parameter-types optional, must match in number to the method signature
                    flag = true;

                    while(this.data.size() <= count) {
                        this.data.add(new HashMap<>());
                    }

                    Map<String, String> myMap = this.data.get(count);
                    Pattern pattern = Pattern.compile("(?:[@return\\-type=]{13})([\\w-]+)");
                    Matcher matcher = pattern.matcher(line);
                    matcher.find();
                    myMap.put("ReturnType", matcher.group(1));
                    pattern = Pattern.compile("(?:[@parameter\\-type=]{16})([\\w-]+)");
                    matcher = pattern.matcher(line);
                    int paramCount = 0;

                    while(matcher.find()) {
                        myMap.put("Param"+paramCount, matcher.group(1));
                        line = line.substring(matcher.end(1));
                        matcher = pattern.matcher(line);
                        paramCount++;
                    }
                    myMap.put("nParams", ""+paramCount);
                }
                else if(flag && line.contains("to")) {
                    //line looks like: to-report get-min-one-of [ p reporters ] 
                    //could be `to` or `to-report` and parameters (in square brackets) are optional
                    Pattern pattern = Pattern.compile("(?:(?:[to]{2}-[report]{6} )|(?:[to]{2} ))([\\w-]+)");
                    Matcher matcher = pattern.matcher(line);
                    int nParams = line.contains("[") ? countParams(line) : 0;

                    if(matcher.find()) {
                        Map<String, String> myMap = this.data.get(count);

                        String methodName = matcher.group(1);
                        //class names in Java cannot contain hyphens, so we eliminate those
                        String className = methodName.replaceAll("-", "");
                        myMap.put("methodName", methodName);
                        myMap.put("className", className);
                        
                        //create and write new file
                        File rules = new File(this.pathBase + "/rules/");
                        if (!rules.exists()) {
                            rules.mkdir();
                        }

                        // TODO consider using Java 13/14 text block to make this more readable
                        String contents = "package "+this.packageName+".rules;\nimport ec.*;\nimport ec.gp.*;\npublic class "+className+" extends GPNode {\npublic String toString() {\nreturn \" "+(nParams == 0 ? "( ": "")+methodName+(nParams == 0 ? " )" : "")+" \";\n}\n@Override\npublic void eval(EvolutionState state, int thread, GPData input, ADFStack stack, GPIndividual individual, Problem problem) {\n}\npublic int expectedChildren() { return "+nParams+"; }\n}";
                        // TODO need to force create <pathBase>/rules if it doesn't exist
                        FileWriter fw = new FileWriter(new File(this.pathBase + "/rules/" + className + ".java"));
                        fw.write(contents);
                        fw.flush();
                        fw.close();
                        count++;
                    }
                }
                else if(flag && line.contains("end")) {
                    //end of method, ready for next
                    flag = false;
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeNewParamsFile() {
        try {
            //new params file requires knowing all custom datatypes
            Set<String> allDataTypes = new HashSet<>();

            //need to know all unique datatypes used in a return-type or parameter-type
            for(int i = 0; i < this.data.size(); i++) {
                Map<String, String> map = this.data.get(i);
                allDataTypes.add(map.get("ReturnType"));
                for(int param = 0; param < Integer.parseInt(map.get("nParams")); param++) {
                    allDataTypes.add(map.get("Param"+param));
                }
            }

            //write the new params file
            String newName = this.paramsPath.substring(0, this.paramsPath.length()-7)+"."+this.modelFile.getName()+".params";
            File newFile = new File(newName);
            FileWriter fw = new FileWriter(newFile);

            fw.write("parent.0 = "+this.paramsFile.getName()+"\n\n");
            fw.write("gp.tc.size = 1\n");
            fw.write("gp.tc.0 = ec.gp.GPTreeConstraints\n");
            fw.write("gp.tc.0.name = tc0\n");
            fw.write("gp.tc.0.returns = "+this.treeReturnType +"\n");
            fw.write("gp.type.a.size = "+allDataTypes.size() + "\n");
            int dataTypeCount = 0;

            for(String s : allDataTypes) {
                fw.write("gp.type.a."+dataTypeCount+".name = "+s+"\n");
                dataTypeCount++;
            }

            fw.write("gp.fs.size = 1\n");
            fw.write("gp.fs.0 = ec.gp.GPFunctionSet\n");
            fw.write("gp.fs.0.name = fs0\n");
            fw.write("gp.fs.0.size = "+this.data.size()+"\n");
            fw.write("gp.tc.0.fset = fs0\n");
            fw.write("gp.nc.size = "+this.data.size()+"\n");

            for(int i = 0; i < this.data.size(); i++) { //for each method
                Map<String, String> map = this.data.get(i);
                fw.write("gp.nc."+i+" = ec.gp.GPNodeConstraints\ngp.nc."+i+".name = nc"+i+"\ngp.nc."+i+".returns = "+map.get("ReturnType")+"\ngp.nc."+i+".size = "+map.get("nParams")+"\n");
                for(int param = 0; param < Integer.parseInt(map.get("nParams")); param++) {
                    fw.write("gp.nc."+i+".child."+param+" = "+map.get("Param"+param)+"\n");
                }
                fw.write("gp.fs.0.func."+i+" = "+this.packageName + ".rules."+map.get("className")+"\ngp.fs.0.func."+i+".nc = nc"+i+"\n");
            }

            if(this.distributed) {
                fw.write("eval.masterproblem = ec.eval.MasterProblem\neval.master.port = 3000\neval.masterproblem.max-jobs-per-slave = 1\n");
                fw.write("eval.masterproblem.job-size = 10\neval.masterproblem.max-jobs-per-slave = 5\nevalthreads = 1");
            }
            //close all open resources
            fw.flush();
            fw.close();

            if(this.distributed) {
                fw = new FileWriter(this.paramsPath.substring(0, this.paramsPath.length()-7)+"."+this.modelFile.getName()+".slave.params");
                fw.write("parent.0 = "+newFile.getName()+"\neval.slave-name =  test\neval.master.host = 127.0.0.1\neval.master.port = 3000\nevalthreads = auto");
                fw.flush();
                fw.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createOutputFile() {
        try {
            File outputFile = new File(this.outputPath);

            if (!outputFile.getParentFile().exists()) {
                outputFile.getParentFile().mkdirs();
            }

            FileWriter fw = new FileWriter(outputFile, false);
            fw.write("Gen,Rule,Fitness,Fitdata\n");
            fw.flush();
            fw.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static int countParams(String line) {
        String sub = line.substring(line.indexOf("[")+1, line.indexOf("]"));
        sub = sub.strip();
        return sub.split(" ").length;
    }
    
}
