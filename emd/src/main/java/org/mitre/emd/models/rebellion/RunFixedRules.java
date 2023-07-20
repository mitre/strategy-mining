package org.mitre.emd.rebellion;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.nlogo.core.LogoList;
import org.nlogo.headless.HeadlessWorkspace;
import org.apache.commons.math3.stat.inference.KolmogorovSmirnovTest;

public class RunFixedRules {
    private String[] newRules = {"( greaterThan  ( subtract  ( subtract  ( subtract  ( multiply   ( reportGrievance )  ( multiply   ( reportAversion )  ( multiply   ( reportAversion )   ( reportAversion ) ))) ( multiply   ( reportGrievance )  ( multiply   ( reportAversion )   ( reportAversion ) ))) ( multiply   ( reportGrievance )   ( reportArrestProb ) )) ( subtract  ( add   ( reportArrestProb )   ( propLinksActive ) ) ( multiply   ( reportAversion )  ( multiply   ( reportAversion )   ( reportAversion ) ))))  ( reportThreshold ) )",
    "( greaterThan  ( subtract  ( subtract  ( subtract  ( multiply   ( reportGrievance )  ( subtract  ( multiply  ( subtract  ( multiply   ( reportGrievance )  ( subtract  ( multiply   ( reportAversion )   ( reportAversion ) ) ( multiply   ( reportGrievance )   ( reportArrestProb ) ))) ( multiply  ( multiply   ( reportGrievance )   ( reportArrestProb ) )  ( reportArrestProb ) ))  ( reportAversion ) ) ( multiply   ( reportGrievance )   ( reportArrestProb ) ))) ( multiply  ( multiply   ( reportGrievance )   ( reportArrestProb ) )  ( reportArrestProb ) )) ( multiply   ( reportGrievance )   ( reportArrestProb ) )) ( subtract  ( add   ( reportArrestProb )   ( propLinksActive ) ) ( multiply   ( reportAversion )  ( multiply   ( reportAversion )   ( reportAversion ) ))))  ( reportThreshold ) )",
    "( greaterThan  ( add  ( multiply  ( add  ( multiply  ( multiply   ( reportArrestProb )   ( reportGrievance ) ) ( subtract   ( reportArrestProb )   ( propLinksActive ) )) ( add  ( multiply  ( add   ( reportArrestProb )   ( reportArrestProb ) ) ( multiply   ( reportArrestProb )   ( reportGrievance ) )) ( add  ( multiply  ( add   ( reportArrestProb )   ( reportArrestProb ) ) ( multiply   ( reportArrestProb )   ( reportGrievance ) )) ( subtract  ( subtract   ( reportGrievance )   ( propLinksActive ) ) ( subtract   ( reportArrestProb )   ( propLinksActive ) ))))) ( multiply   ( reportArrestProb )   ( reportGrievance ) )) ( add  ( multiply  ( add   ( reportArrestProb )  ( subtract  ( subtract   ( reportGrievance )   ( propLinksActive ) ) ( subtract   ( reportArrestProb )   ( propLinksActive ) ))) ( multiply   ( reportArrestProb )   ( reportGrievance ) )) ( subtract  ( subtract   ( reportGrievance )   ( propLinksActive ) ) ( subtract   ( reportArrestProb )   ( propLinksActive ) ))))  ( reportThreshold ) )",
    "( greaterThan  ( add  ( multiply  ( add  ( subtract   ( reportArrestProb )   ( propLinksActive ) ) ( multiply  ( multiply   ( reportArrestProb )   ( reportGrievance ) ) ( multiply  ( add   ( reportArrestProb )   ( reportArrestProb ) ) ( add  ( multiply  ( add  ( multiply  ( subtract   ( reportGrievance )   ( propLinksActive ) ) ( add  ( multiply  ( add   ( reportArrestProb )   ( reportArrestProb ) ) ( multiply   ( reportArrestProb )   ( reportGrievance ) ))  ( reportArrestProb ) )) ( subtract  ( subtract   ( reportGrievance )   ( propLinksActive ) ) ( subtract   ( reportArrestProb )   ( propLinksActive ) ))) ( subtract   ( reportArrestProb )   ( propLinksActive ) )) ( add  ( multiply  ( add   ( reportArrestProb )   ( reportArrestProb ) ) ( multiply   ( reportArrestProb )   ( reportGrievance ) )) ( add  ( multiply  ( add   ( reportArrestProb )   ( reportArrestProb ) )  ( reportGrievance ) ) ( multiply  ( subtract   ( reportArrestProb )   ( propLinksActive ) ) ( multiply   ( reportArrestProb )  ( subtract  ( add  ( subtract   ( reportArrestProb )   ( propLinksActive ) ) ( multiply  ( add   ( reportArrestProb )   ( reportArrestProb ) ) ( multiply   ( reportArrestProb )   ( reportGrievance ) )))  ( propLinksActive ) ))))))))) ( multiply   ( reportArrestProb )   ( reportGrievance ) )) ( subtract  ( subtract   ( reportGrievance )   ( propLinksActive ) ) ( subtract   ( reportArrestProb )   ( propLinksActive ) )))  ( reportThreshold ) )",
    "( greaterThan  ( add  ( multiply  ( add  ( subtract   ( reportArrestProb )   ( propLinksActive ) ) ( multiply  ( multiply   ( reportArrestProb )   ( reportGrievance ) ) ( multiply   ( reportArrestProb )  ( subtract  ( add  ( multiply  ( add   ( reportArrestProb )   ( reportArrestProb ) ) ( add  ( multiply  ( subtract   ( reportArrestProb )   ( propLinksActive ) ) ( subtract   ( reportArrestProb )   ( propLinksActive ) )) ( add  ( multiply  ( add   ( reportArrestProb )   ( reportArrestProb ) ) ( multiply   ( reportArrestProb )   ( reportGrievance ) )) ( add  ( multiply  ( add   ( reportArrestProb )   ( reportArrestProb ) )  ( reportGrievance ) ) ( subtract   ( reportArrestProb )   ( propLinksActive ) ))))) ( multiply  ( add   ( reportArrestProb )   ( reportArrestProb ) ) ( multiply   ( reportArrestProb )   ( reportGrievance ) )))  ( propLinksActive ) )))) ( multiply   ( reportArrestProb )   ( reportGrievance ) )) ( subtract  ( subtract   ( reportGrievance )   ( propLinksActive ) ) ( subtract   ( reportArrestProb )   ( propLinksActive ) )))  ( reportThreshold ) )",
    "( greaterThan  ( subtract  ( subtract  ( subtract  ( multiply   ( reportGrievance )  ( subtract  ( multiply  ( subtract  ( multiply   ( reportGrievance )  ( subtract  ( multiply   ( reportAversion )   ( reportAversion ) ) ( multiply   ( reportGrievance )   ( reportArrestProb ) ))) ( multiply  ( multiply   ( reportGrievance )   ( reportArrestProb ) )  ( reportArrestProb ) ))  ( reportAversion ) ) ( multiply   ( reportGrievance )   ( reportArrestProb ) ))) ( multiply  ( multiply   ( reportGrievance )   ( reportArrestProb ) )  ( reportArrestProb ) )) ( multiply   ( reportGrievance )  ( multiply   ( reportAversion )   ( reportAversion ) ))) ( subtract  ( add   ( reportArrestProb )   ( propLinksActive ) ) ( multiply   ( reportAversion )  ( multiply   ( reportAversion )   ( reportAversion ) ))))  ( reportThreshold ) )",
    "( greaterThan  ( subtract  ( subtract  ( subtract  ( multiply   ( reportGrievance )  ( subtract  ( multiply   ( reportGrievance )  ( subtract  ( multiply   ( reportArrestProb )   ( reportAversion ) ) ( multiply   ( reportGrievance )   ( reportArrestProb ) ))) ( multiply  ( multiply   ( reportGrievance )   ( reportArrestProb ) )  ( reportArrestProb ) ))) ( multiply  ( multiply   ( reportAversion )  ( multiply   ( reportAversion )   ( reportAversion ) ))  ( reportArrestProb ) )) ( multiply  ( multiply   ( reportGrievance )   ( reportArrestProb ) )  ( reportArrestProb ) )) ( subtract  ( add   ( reportArrestProb )   ( propLinksActive ) ) ( multiply   ( reportAversion )  ( multiply   ( reportAversion )   ( reportAversion ) ))))  ( reportThreshold ) )",
    "( greaterThan  ( add  ( multiply  ( add  ( subtract   ( reportArrestProb )   ( propLinksActive ) ) ( multiply  ( add   ( reportArrestProb )   ( reportArrestProb ) ) ( multiply  ( add   ( reportArrestProb )  ( subtract  ( subtract   ( reportGrievance )   ( propLinksActive ) ) ( subtract   ( reportArrestProb )   ( propLinksActive ) )))  ( reportGrievance ) ))) ( multiply   ( reportArrestProb )   ( reportGrievance ) )) ( add  ( multiply  ( add   ( reportArrestProb )  ( subtract  ( subtract   ( reportGrievance )   ( propLinksActive ) ) ( subtract   ( reportArrestProb )   ( propLinksActive ) ))) ( multiply   ( reportArrestProb )   ( reportGrievance ) )) ( subtract  ( subtract   ( reportGrievance )   ( propLinksActive ) ) ( subtract   ( reportArrestProb )   ( propLinksActive ) ))))  ( reportThreshold ) )",
    "( greaterThan  ( add  ( multiply  ( add  ( multiply  ( multiply   ( reportArrestProb )   ( reportGrievance ) ) ( subtract   ( reportArrestProb )   ( propLinksActive ) )) ( multiply  ( add   ( reportArrestProb )   ( reportArrestProb ) ) ( multiply   ( reportArrestProb )   ( reportGrievance ) ))) ( multiply   ( reportArrestProb )   ( reportGrievance ) )) ( add  ( multiply  ( add   ( reportArrestProb )   ( reportArrestProb ) ) ( multiply   ( reportArrestProb )   ( reportGrievance ) )) ( subtract  ( subtract   ( reportGrievance )   ( propLinksActive ) ) ( subtract   ( reportArrestProb )   ( propLinksActive ) ))))  ( reportThreshold ) )",
    "( greaterThan  ( add  ( multiply  ( add  ( multiply  ( subtract   ( reportGrievance )   ( propLinksActive ) ) ( subtract   ( reportArrestProb )   ( propLinksActive ) )) ( multiply  ( add   ( reportArrestProb )   ( reportArrestProb ) ) ( multiply  ( subtract   ( reportGrievance )   ( propLinksActive ) )  ( reportGrievance ) ))) ( multiply  ( add   ( reportArrestProb )   ( reportArrestProb ) ) ( multiply   ( reportArrestProb )   ( reportGrievance ) ))) ( add  ( multiply  ( subtract   ( reportArrestProb )   ( propLinksActive ) ) ( multiply   ( reportArrestProb )   ( reportGrievance ) )) ( subtract  ( subtract   ( reportGrievance )   ( propLinksActive ) ) ( subtract   ( reportArrestProb )   ( propLinksActive ) ))))  ( reportThreshold ) )"};
    private double[] groundMaxes = null;
    private double[] groundSums = null;
    private double[] groundDurations = null;
    private double[] groundInterarrivals = null;
    private String fixedRuleOutput = "/Users/aisherwood/Documents/strategy-mining/output/fixedRuleOutput.csv";
    private String modelPathString = "/Users/aisherwood/Documents/strategy-mining/input/rebellion/Rebellion_02.nlogo";
    private String factorsPathString = "/Users/aisherwood/Documents/strategy-mining/input/rebellion/factors.nls";
    private String outputPath = "/Users/aisherwood/Documents/strategy-mining/output/manual_rule_runs/";
    private String leftJoin = "";
    private String rightJoin = "";
    private int ticks = 500;
    private int reps = 30;

    public void setup() {
        Map<String, List<Double>> groundMetrics = new HashMap<String, List<Double>>();
        List<Double> groundMaxesList = new ArrayList<Double>();
        List<Double> groundSumsList = new ArrayList<Double>();
        List<Double> groundDurationsList = new ArrayList<Double>();
        List<Double> groundInterarrivalsList  = new ArrayList<Double>();
        int numRebellions = 0;
        int numInterarrivals = 0;

        try {
            Scanner scan = new Scanner(new File(fixedRuleOutput));

            // read all metrics from fixed rule output
            while (scan.hasNextLine()) {
                String line = scan.nextLine();
                line = line.replace("[", "").replace("]", "");
                String[] activeList = line.split(", ");
                double[] activeNums = new double[activeList.length];
    
                for (int i = 0; i < activeList.length; i++) {
                    activeNums[i] = Double.parseDouble(activeList[i]);
                }
    
                // append to metric lists
                groundMetrics = getMetricLists(activeNums);
                groundMaxesList.addAll(groundMetrics.get("maxes"));
                groundSumsList.addAll(groundMetrics.get("sums"));
                groundDurationsList.addAll(groundMetrics.get("durations"));
                groundInterarrivalsList.addAll(groundMetrics.get("interarrivals"));
            }

            scan.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        
        // convert lists to array of doubles
        numRebellions = groundMaxesList.size();
        numInterarrivals = groundInterarrivalsList.size();
        groundMaxes = new double[numRebellions];
        groundSums = new double[numRebellions];
        groundDurations = new double[numRebellions];
        groundInterarrivals = new double[numInterarrivals];

        for (int j = 0; j < numRebellions; j++) {
            groundMaxes[j] = groundMaxesList.get(j);
            groundSums[j] = groundSumsList.get(j);
            groundDurations[j] = groundDurationsList.get(j);
        }

        for (int k = 0; k < numInterarrivals; k++) {
            groundInterarrivals[k] = groundInterarrivalsList.get(k);
        }

        Path modelPath = Paths.get(modelPathString);
        Path factorsPath = Paths.get(factorsPathString);

        try {
            String modelContents = Files.readString(modelPath);
            int startImports = modelContents.indexOf("__includes");
            int endImports = modelContents.indexOf("]", startImports);

            if (startImports != -1 && endImports != -1) {
                modelContents = modelContents.substring(0, startImports) + modelContents.substring(endImports + 1, modelContents.length());
            }

            String factorsContents = Files.readString(factorsPath);
            int startGoPos = modelContents.indexOf("to go");
            int endGoPos = modelContents.indexOf("end\n", startGoPos);
            modelContents = modelContents.substring(0, endGoPos + 4) + "\n" + factorsContents  + "\n" + modelContents.substring(endGoPos + 4, modelContents.length());

            int evolveLinePos = modelContents.indexOf("@EvolveNextLine");
            int startEvolvePos = modelContents.indexOf("\n", evolveLinePos);
            int endEvolvePos = modelContents.indexOf("\n", startEvolvePos + 1);

            leftJoin = modelContents.substring(0, startEvolvePos + 1);
            rightJoin = modelContents.substring(endEvolvePos, modelContents.length());

            System.setProperty("netlogo.extensions.dir", "/Applications/NetLogo 6.1.1/extensions/.bundled");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        RunFixedRules exp = new RunFixedRules();
        exp.setup();
        exp.createOutputFile();

        for (int ruleNum = 0; ruleNum < exp.newRules.length; ruleNum++) {
            String rule = exp.newRules[ruleNum];
            String customModel = exp.buildModel(rule);

            for (int rep = 0; rep < exp.reps; rep++) {
                int seed = exp.createSeed(ruleNum, rep);
                exp.runCustomModels(rule, customModel, ruleNum, rep, seed);
            }
        }
    }

    private String buildModel(String rule) {
        return (leftJoin + rule + rightJoin);
    }

    public double[] calculateCustomFitness(LogoList fitData) {
        LogoList netlList = fitData;
        double[] modelData = new double[netlList.size()];
        double[] maxes = null;
        double[] sums = null;
        double[] durations = null;
        double[] interarrivals = null;

        // get model output
        for (int i = 0; i < netlList.size(); i++) {
            modelData[i] = (double) netlList.get(i);
        }

        // convert model metrics to arrays of doubles
        Map<String, List<Double>> metricLists = getMetricLists(modelData);  
        List<Double> maxesList = metricLists.get("maxes");
        List<Double> sumsList = metricLists.get("sums");
        List<Double> durationsList = metricLists.get("durations");
        List<Double> interarrivalsList = metricLists.get("interarrivals");  

        int numRebellions = maxesList.size();
        int numInterarrivals = interarrivalsList.size();

        if (numRebellions > 1) {
            maxes = new double[numRebellions];
            sums = new double[numRebellions];
            durations = new double[numRebellions];

            for (int i = 0; i < numRebellions; i++) {
                maxes[i] = maxesList.get(i);
                sums[i] = sumsList.get(i);
                durations[i] = durationsList.get(i);
            }
        } else {
            maxes = new double[] {0, 0};
            sums = new double[] {0, 0};
            durations = new double[] {0, 0};
        }

        if (numInterarrivals > 1) {
            interarrivals = new double[numInterarrivals];

            for (int j = 0; j < numInterarrivals; j++) {
                interarrivals[j] = interarrivalsList.get(j);
            }
        } else {
            interarrivals = new double[] {0, 0};
        }

        // run ks test between fixed rule sample and custom model metrics
        KolmogorovSmirnovTest ksTest = new KolmogorovSmirnovTest();
        double ksMax = ksTest.kolmogorovSmirnovStatistic(groundMaxes, maxes);
        double ksSum = ksTest.kolmogorovSmirnovStatistic(groundSums, sums);
        double ksDuration = ksTest.kolmogorovSmirnovStatistic(groundDurations, durations);
        double ksInterarrivals = ksTest.kolmogorovSmirnovStatistic(groundInterarrivals, interarrivals);
        double[] fitnessMetrics = {ksMax, ksSum, ksDuration, ksInterarrivals};

        return fitnessMetrics;
    }

    // cantor pairing function
    private int createSeed(int x, int y) {
        return ((1 / 2) * (x + y) * (x + y + 1) + y);
    }

    public void runCustomModels(String rule, String customModel, int ruleNum, int rep, int seed) {
        HeadlessWorkspace workspace = HeadlessWorkspace.newInstance();
            
        try {
            workspace.openString(customModel);
            workspace.command("random-seed " + seed);
            workspace.command("setup");
            workspace.command("repeat " + ticks + " [ go ]");
            LogoList fitData = (LogoList) workspace.report("metric");
                
            double[] customFitness = calculateCustomFitness(fitData);
            double fitness = customFitness[0] + customFitness[1] + customFitness[2] + customFitness[3];

            File outputFile = new File(outputPath + "manual_rule_output.csv");
            FileWriter fw = new FileWriter(outputFile, true);
            fw.write(rule + "," + ruleNum + "," + rep + "," + customFitness[0] + "," + customFitness[1] + "," + customFitness[2] + "," + customFitness[3] + "," + fitness + "," + fitData + "\n");
            fw.flush();
            fw.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void createOutputFile() {
        try {
            File outputFile = new File(outputPath + "manual_rule_output.csv");

            if (!outputFile.getParentFile().exists()) {
                outputFile.getParentFile().mkdirs();
            }

            FileWriter fw = new FileWriter(outputFile, false);
            fw.write("Rule,RuleNum,Rep,ksMax,ksSum,ksDuration,ksInterarrival,Fitness,Fitdata\n");
            fw.flush();
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Map<String, List<Double>> getMetricLists (double[] metrics) {
        Map<String, List<Double>> metricLists = new HashMap<String, List<Double>>();
        List<Double> maxesList = new ArrayList<>();
        List<Double> sumsList = new ArrayList<>();
        List<Double> durationsList = new ArrayList<>();
        List<Double> interarrivalsList = new ArrayList<>();
        double max = 0;
        double sum = 0;
        double duration = 0;
        double interarrival = 0;
        boolean activeRebellion = false;

        for (int i = 0; i < metrics.length; i++) {
            double curActive = metrics[i];

            if (curActive < 10) {
                if (activeRebellion) {
                    maxesList.add(max);
                    sumsList.add(sum);
                    durationsList.add(duration);
                    max = 0;
                    sum = 0;
                    duration = 0;
                    interarrival = 1;
                    activeRebellion = false;
                } else {
                    interarrival++;
                }
            } else {
                if (activeRebellion) {
                    if (curActive > max) max = curActive;
                    sum += curActive;
                    duration++;
                } else {
                    if (i > 0) interarrivalsList.add(interarrival);
                    max = curActive;
                    sum = curActive;
                    duration = 1;
                    interarrival = 0;
                    activeRebellion = true;
                }
            }
        }

        metricLists.put("maxes", maxesList);
        metricLists.put("sums", sumsList);
        metricLists.put("durations", durationsList);
        metricLists.put("interarrivals", interarrivalsList);

        return metricLists;
    }
}
