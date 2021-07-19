package org.mitre.emd;

import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
// import java.nio.file.Path;
// import java.nio.file.Paths;

import org.apache.spark.ml.feature.*;
import org.apache.spark.ml.regression.RandomForestRegressionModel;
import org.apache.spark.ml.regression.RandomForestRegressor;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

public class FactorImportance {
    private String[] factors = null;
    private String output = null;
    private String filePath = null;
    // private Path topLevel = null;
    // private Path pathBase = null;
    
    private void setFactors(File ruleDirectory) {
        FilenameFilter filter = new FilenameFilter() {
            @Override
            public boolean accept(File f, String name) {
                return name.endsWith(".java");
            }
        };
        String[] factorFiles = ruleDirectory.list(filter);

        factors = new String[factorFiles.length];
        for (int i = 0; i < factorFiles.length; i++) {
            factors[i] = factorFiles[i].split(".java")[0];
        }
    }

    public void setup() { 
        // topLevel = Paths.get(".").toAbsolutePath();
        // pathBase = Paths.get(topLevel.getParent().toString(), "emd/src/main/java/org/mitre/emd");

        if (factors == null) {
            // File ruleDirectory = new File(pathBase + "/rules/");
            File ruleDirectory = new File("../emd/src/main/java/org/mitre/emd/rules/");
            
            setFactors(ruleDirectory);
        }

        output = "../output/output.csv";
        filePath = "../output/factorImportance.csv";
    }

    // assign values to factors and output here if they should not be found automatically
    public void manualSetup() {
        factors = new String[] {"anyInRadar", "averageHeadings", "booleanHeadingChoice", "booleanSpeedChoice", "doNothingTop", "findAllFlockmates", "findRadarFlockmates",
                                "findVizFlockmates", "inRadar", "jitterHeading", "jitterSpeed", "maxFlockmateSpeed", "maxRadarFlockmate", "meanFlockHeading", 
                                "meanFlockmateSpeed", "medianFlockmateSpeed", "minDistanceFlockmate", "minFlockmateSpeed", "myHeading", "noChangeSpeed", "oppositeHeading",
                                "randomCondition", "randomFlockmateHeading", "randomFlockmateSpeed", "randomSpreadSpeed", "setNewSpeedAndHeading", "slowdown", "speedup"};
        output = "output/radarOutput.csv";
        filePath = "output/factorImportance.csv";
    }

    public static void main(String[] args) {
        FactorImportance factorImportance = new FactorImportance();
        factorImportance.setup();

        SparkSession spark = SparkSession.builder()
            .appName("Simple Application")
            .config("spark.master", "local")
            .getOrCreate();

        Dataset<Row> df = spark.read().format("csv")
            .option("delimiter", ",")
            .option("header", true)
            .option("inferSchema", true)
            .load(factorImportance.output);

        VectorAssembler assembler = new VectorAssembler()
            .setInputCols(factorImportance.factors)
            .setOutputCol("Features");

        Dataset<Row> featureDf = assembler.transform(df);
        RandomForestRegressor rf = new RandomForestRegressor()
            .setFeaturesCol("Features")
            .setLabelCol("Fitness");

        RandomForestRegressionModel model = rf.train(featureDf);
        System.out.println(model.toDebugString());
        double featureImportances [] = model.featureImportances().toArray();

        try {
            FileWriter fw = new FileWriter(factorImportance.filePath);
            fw.write("Factor,Gini Importance\n");

            for (int i = 0; i < factorImportance.factors.length; i++) {
                fw.write(factorImportance.factors[i] + "," + featureImportances[i] + "\n");
            }
            
            fw.flush();
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
