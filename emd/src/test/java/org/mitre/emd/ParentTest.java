package org.mitre.emd;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class ParentTest {
    private String[] params = null;
    private String paramsPath = "";
    private String factorsPath = "";
    private String modelPath = "";
    private String outputPath = "";
    private String newParams = "";
    private File factorsFile = null;
    private File paramsFile = null;
    private File modelFile = null;
    private File newParamsFile = null;
    private Path pathBase = null;

    public void init() {
        this.paramsPath = System.getProperty("params");
        if(this.paramsPath == null){
            this.paramsPath = "../emd/src/test/resources/experiment.apeFight.nlogo.params";
        }
        this.paramsFile = new File(this.paramsPath);

        try (InputStream input = new FileInputStream(this.paramsPath)) {
            // open the original params file, and find the modelPath parameter
            // store the found parameter in appropriate variables
            this.paramsFile = new File(this.paramsPath);
            Path topLevel = Paths.get("").toAbsolutePath().getParent();
            this.pathBase = Paths.get(topLevel.toString() + File.separator + "emd", "src/main/java/org/mitre/emd");

            Properties prop = new Properties();

            // load the input params file
            prop.load(input);

            this.modelPath = prop.getProperty("modelPath");
            this.modelFile = new File(this.modelPath).getCanonicalFile();

            this.factorsPath = prop.getProperty("factorsPath");
            this.factorsFile = new File(this.factorsPath);

            this.outputPath = prop.getProperty("outputFileDirectory") + prop.getProperty("outputFileName");

        } catch (IOException ex) {
            System.err.println("Error loading params file." + ex.toString());
        }
    }

    public void setParams(String[] params) {
        this.params = params;
    }

    public String[] getParams() {
        return this.params;
    }

    public void setParamsPath(String paramsPath) {
        this.paramsPath = paramsPath;
    }

    public String getParamsPath() {
        return this.paramsPath;
    }

    public void setPathBase(Path pathBase) {
        this.pathBase = pathBase;
    }

    public Path getPathBase() {
        return this.pathBase;
    }

    public void setFactorsPath(String factorsPath) {
        this.factorsPath = paramsPath;
    }

    public String getFactorsPath() {
        return this.factorsPath;
    }

    public void setModelPath(String modelPath) {
        this.modelPath = modelPath;
    }

    public String getModelPath() {
        return this.modelPath;
    }

    public void setOutputPath(String outputPath) {
        this.outputPath = outputPath;
    }

    public String getOutputPath() {
        return this.outputPath;
    }

    public void setNewParams() {
        Path paramsFileNameFullPath = Path.of(this.paramsPath).toAbsolutePath();
        String[] splitName = paramsFileNameFullPath.getFileName().toString().split("\\.");
        String newName = "experiment." + splitName[1] + "." + splitName[2] + "." + splitName[3];
        this.newParams = paramsFileNameFullPath.getParent().toString() + File.separator + newName;
        this.newParamsFile = new File(this.newParams);
    }

    public String getNewParams() {
        return this.newParams;
    }

    public void setFactorsFile(File factorsFile) {
        this.factorsFile = factorsFile;
    }

    public File getFactorsFile() {
        return this.factorsFile;
    }

    public void setParamsFile(File paramsFile) {
        this.paramsFile = paramsFile;
    }

    public File getParamsFile() {
        return this.paramsFile;
    }

    public void setModelFile(File modelFile) {
        this.modelFile = modelFile;
    }

    public File getModelFile() {
        return this.modelFile;
    }

    public File getNewParamsFile() {
        return this.newParamsFile;
    }
}
