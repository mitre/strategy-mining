package org.mitre.emd.rebellion;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import ec.EvolutionState;
import ec.Individual;
import ec.gp.koza.KozaFitness;
import ec.util.Parameter;

import org.nlogo.core.LogoList;
import org.nlogo.headless.HeadlessWorkspace;
import org.apache.commons.math3.stat.inference.KolmogorovSmirnovTest;

public class RebellionProblem extends GeneralProblem {
    private String fixedRuleOutput = "/Users/aisherwood/Documents/strategy-mining/output/fixedRuleOutput.csv";
    private double[] groundMaxes = null;
    private double[] groundSums = null;
    private double[] groundDurations = null;
    private double[] groundInterarrivals = null;

    @Override
    public void setup(final EvolutionState state, final Parameter base) {
        super.setup(state, base);
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
    }

    public double[] calculateCustomFitness(List<Object> metrics, EvolutionState state) {
        LogoList netlList = (LogoList) metrics.get(0);
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

    @Override
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
                
            double[] customFitness = calculateCustomFitness(metrics, state);
            double realFitness = customFitness[0] + customFitness[1] + customFitness[2] + customFitness[3];
            KozaFitness kfitness = ((KozaFitness) ind.fitness);
            kfitness.setStandardizedFitness(state, realFitness);
            ind.evaluated = true;

            RebellionOutputWriter outputWriter = new RebellionOutputWriter(getFactors());
            //write info to the output file
            outputWriter.writeFile(outputPath, state.generation, rules, customFitness[0], customFitness[1], 
                                   customFitness[2], customFitness[3], realFitness, metrics);
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

    @Override
    public void createOutputFile(String outputPath) {
        try {
            File outputFile = new File(outputPath);

            if (!outputFile.getParentFile().exists()) {
                outputFile.getParentFile().mkdirs();
            }

            FileWriter fw = new FileWriter(outputFile, false);
            fw.write("Gen,Rule,ksMax,ksSum,ksDuration,ksInterarrival,Fitness,Fitdata," + String.join(",", getFactors()) + "\n");
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
