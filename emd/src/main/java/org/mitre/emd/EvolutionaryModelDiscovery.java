package org.mitre.emd;

import com.google.gson.*;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.mitre.emd.utility.EmdConfiguration;
import org.mitre.emd.utility.EmdConfigurationAdapter;
import org.mitre.emd.utility.FactorsConfiguration;
import org.mitre.emd.utility.MethodReplacer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

import java.io.*;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.*;
import java.nio.file.*;

//import static org.objectweb.asm.Opcodes.ASM7;

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
    public static final Logger logger = LogManager.getLogger(EvolutionaryModelDiscovery.class);

    EvolutionaryModelDiscovery(){}

    /**
     * Holds the model properties read in from file
     */
    Properties prop = new Properties();
    private String packageName = "org.mitre.emd";
    private String treeReturnType = "";
    private String factorsPath = "";
    private String paramsPath = "";
    private String modelPath = "";
    private String outputPath = "";
    private File factorsFile = null;    
    private File paramsFile = null;
    private File modelFile = null;
    private Properties modelProperties = new Properties();
    private boolean distributed = false;
    private Path pathBase = null;
    private List<Map<String, String>> data = null;
    private EmdConfiguration emdConfiguration;

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
        try (InputStream input = new FileInputStream(this.paramsPath)) {
            this.paramsFile = new File(this.paramsPath);
            // TODO Should pathBase be relative to the project's top level directory or a user configurable path?
            Path topLevel = Paths.get("").toAbsolutePath().getParent();
            this.pathBase = Paths.get(topLevel.toString() + File.separator + "emd", "src/main/java/org/mitre/emd");

            Properties prop = new Properties();

            // load the input params file
            prop.load(input);

            this.modelPath = prop.getProperty("modelPath");
            this.modelFile = new File(this.modelPath).getCanonicalFile();

            this.factorsPath = prop.getProperty("factorsPath");
            this.factorsFile = new File(this.factorsPath);

            this.distributed = Boolean.parseBoolean(prop.getProperty("runDistributed"));

            this.outputPath = prop.getProperty("outputFileDirectory") + prop.getProperty("outputFileName");

        } catch (IOException ex) {
            logger.error("Error loading params file.",ex);
        }

    }

    private void setTreeReturnType() {
        String modelExtension = this.modelFile.getName().split("\\.")[1];
        // Check if it's a NetLogo file
        if(modelExtension.equals("nls") || modelExtension.equals("nlogo")){
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
        } else if(modelExtension.equals("properties")) {

            try (InputStream input = new FileInputStream(modelFile)) {
                modelProperties.load(input);
                this.treeReturnType = modelProperties.getProperty("returnType");

            } catch (IOException ex) {
                ex.printStackTrace();
            }

        } else if(modelExtension.equals("json")) {  // TODO fill this in for MASON models
            Gson gson = new GsonBuilder()
                    .setPrettyPrinting()
                    .registerTypeAdapter(EmdConfiguration.class, new EmdConfigurationAdapter())
                    .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES) // put it in Javascript variable format
                    .create();
            try {
                emdConfiguration = gson.fromJson(new FileReader(this.modelFile.getAbsoluteFile()), EmdConfiguration.class);
                for (FactorsConfiguration factor : emdConfiguration.getFactors()) {
                    this.treeReturnType = factor.getReturnType();
                    System.out.println(factor);
                }
            } catch (FileNotFoundException ex) {
                System.err.println("Configuration file not found at: " + this.modelFile.getAbsoluteFile().toString() + ex.getMessage() + ex.getStackTrace());
                return;
            }
        } else {
            System.err.println("Sorry, we don't know how to handle this type of file: " + modelFile.getName() + " . Exiting...");
            System.exit(-1);
        }

    }

    private void writeRulesFiles() {
        // Create factors files based on the EMD configuration file for MASON models
        // TODO this is not quite working yet
        if(emdConfiguration != null){
            for(FactorsConfiguration factor: emdConfiguration.getFactors()){
                String classToClone = "org.mitre.emd.Factor";  // This is the class you want to clone and put the method from the other class into.
                String classToRead = factor.getClassName();  // This is the class we read a method from
                String classToReadPackage = factor.getClassPackage();  // This is the package that the classToRead is in
                classToRead = classToReadPackage + "." + classToRead;
                String methodToRead = factor.getEvalMethod();  // This is the method we want to read in classToRead
                String classToWrite = factor.getClassName() + "Factor";  // Name of the new class we're writing
                try {
                    ClassWriter cw = new ClassWriter(0);
                    ClassReader cr = new ClassReader(classToClone);
                    ClassVisitor cv = new ClassVisitor(7 << 16 | 0 << 8, cw){};

                    ClassReader otherCr = new ClassReader(classToRead);
                    ClassVisitor studentCv = new ClassVisitor(7 << 16 | 0 << 8, cv) {
                    };
                    MethodReplacer mr = new MethodReplacer(studentCv, methodToRead);
                    otherCr.accept(mr, 0);

                    cr.accept(cv, 0);

                    Transformer transformer = new Transformer();
                    byte[] bytes = cw.toByteArray();
                    transformer.writeClass(bytes, classToWrite);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            return;
        }

        // Create factors files for NetLogo models
        try {
            //this data object will store all of the information needed for each method in the factors file
            this.data = new ArrayList<>();
            //loop through the factors file (.nls)
            Scanner scan = new Scanner(this.factorsFile);
            int count = 0; //the total number of methods we've seen
            boolean flag = false;
            File rules = new File(this.pathBase + "/rules/");

            // create rules directory if it doesn't exist
            if (!rules.exists()) {
                rules.mkdir();
            }

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
                        // TODO consider using Java 13/14 text block to make this more readable
                        String contents = "package "+this.packageName+".rules;\nimport ec.*;\nimport ec.gp.*;\npublic class "+className+" extends GPNode {\npublic String toString() {\nreturn \" "+(nParams == 0 ? "( ": "")+methodName+(nParams == 0 ? " )" : "")+" \";\n}\n@Override\npublic void eval(EvolutionState state, int thread, GPData input, ADFStack stack, GPIndividual individual, Problem problem) {\n}\npublic int expectedChildren() { return "+nParams+"; }\n}";
                        File newFactorFile = new File(this.pathBase + "/rules/" + className + ".java");
                        // Clear out any old factors files from prior runs before creating new ones.
                        if(newFactorFile.exists()){
                            newFactorFile.delete();
                        }
                        FileWriter fw = new FileWriter(newFactorFile);
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
            scan.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Creates a new params file based on the given factors.
     */
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

            //write the new factors params file
            Path paramsFileNameFullPath = Path.of(this.paramsPath).toAbsolutePath();
            String[] splitName = paramsFileNameFullPath.getFileName().toString().split("\\.");
            String newName = "factors." + splitName[1] + "." + splitName[2] + "." + splitName[3];

            File newFile = new File(paramsFileNameFullPath.getParent().toString() + File.separator + newName);
            FileWriter fw = new FileWriter(newFile);

            fw.write("parent.0 = parent.params\n");
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

//    private class FactorDeserializer implements JsonDeserializer<Factors> {
//        private Gson gson;
//
//        public FactorDeserializer(){
//            this.gson = new Gson();
//        }
//        public Factors deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
//                throws JsonParseException {
//            JsonObject factorObject = json.getAsJsonObject();
////            return new Factors(json.getAsJsonPrimitive().getAsJsonArray());
//            return gson.fromJson(factorObject,Factors.class);
//        }
//    }
    
}


