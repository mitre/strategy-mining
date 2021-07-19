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
            this.paramsPath = "../input/apefight/experiment.apeFight.nlogo.params";
        }
        this.paramsFile = new File(this.paramsPath);

        this.pathBase = Paths.get("../emd/src/main/java/org/mitre/emd");
        try {
            // open the original params file, and find the modelPath parameter
            // store the found parameter in appropriate variables
            Scanner scan = new Scanner(this.paramsFile);

            while(scan.hasNextLine()) {
                String line = scan.nextLine();

                if(line.contains("modelPath")) {
                    this.modelPath = line.split("=")[1].strip();
                    this.modelFile = new File(this.modelPath);
                }

                if(line.contains("factorsPath")) {
                    this.factorsPath = line.split("=")[1].strip();
                    this.factorsFile = new File(this.factorsPath);
                }

                if(line.contains("outputPath")) {
                    this.outputPath = line.split("=")[1].strip();
                }
            }
            scan.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
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
