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

import org.apache.commons.math3.stat.inference.KolmogorovSmirnovTest;

public class CalcOwnFitness {
    private Map<Integer, Map<String, List<Double>>> allMetrics = new HashMap<Integer, Map<String, List<Double>>>();
    private String fixedRuleOutput = "/Users/aisherwood/Documents/strategy-mining/output/fixedRuleOutput.csv";
    private String outputPath = "/Users/aisherwood/Documents/strategy-mining/output/calcOwnFitness.csv";
    private int outputNum = 0;

    private void setup() {
        Map<String, List<Double>> groundMetrics = new HashMap<String, List<Double>>();

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
                allMetrics.put(outputNum, groundMetrics);
                outputNum++;
            }

            scan.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        createOutputFile(outputPath);
    }

    public static void main(String[] args) {
        CalcOwnFitness cof = new CalcOwnFitness();
        cof.setup();
        cof.runKsTests();
    }

    public void createOutputFile(String outputPath) {
        try {
            File outputFile = new File(outputPath);

            if (!outputFile.getParentFile().exists()) {
                outputFile.getParentFile().mkdirs();
            }

            FileWriter fw = new FileWriter(outputFile, false);
            fw.write("Run,ksMax,ksSum,ksDuration,ksInterarrival,Fitness\n");
            fw.flush();
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void runKsTests() {
        Map<String, List<Double>> groundMetrics = new HashMap<String, List<Double>>();
        Map<String, List<Double>> testMetrics = new HashMap<String, List<Double>>();
        List<Double> groundMaxesList = new ArrayList<Double>();
        List<Double> groundSumsList = new ArrayList<Double>();
        List<Double> groundDurationsList = new ArrayList<Double>();
        List<Double> groundInterarrivalsList  = new ArrayList<Double>();
        List<Double> testMaxesList = new ArrayList<Double>();
        List<Double> testSumsList = new ArrayList<Double>();
        List<Double> testDurationsList = new ArrayList<Double>();
        List<Double> testInterarrivalsList  = new ArrayList<Double>();

        for (int i = 0; i < outputNum; i++) {
            for (int j = 0; j < outputNum; j++) {
                if (j != i) {
                    groundMetrics = allMetrics.get(j);
                    groundMaxesList.addAll(groundMetrics.get("maxes"));
                    groundSumsList.addAll(groundMetrics.get("sums"));
                    groundDurationsList.addAll(groundMetrics.get("durations"));
                    groundInterarrivalsList.addAll(groundMetrics.get("interarrivals"));
                }
            }

            testMetrics = allMetrics.get(i);
            testMaxesList = testMetrics.get("maxes");
            testSumsList = testMetrics.get("sums");
            testDurationsList = testMetrics.get("durations");
            testInterarrivalsList = testMetrics.get("interarrivals");

            Map<String, double[]> groundMetricLists = convertListsToArrays(groundMaxesList, groundSumsList, groundDurationsList, groundInterarrivalsList);
            Map<String, double[]> testMetricLists = convertListsToArrays(testMaxesList, testSumsList, testDurationsList, testInterarrivalsList);

            double[] groundMaxesArray = groundMetricLists.get("maxes");
            double[] groundSumsArray = groundMetricLists.get("sums");
            double[] groundDurationsArray = groundMetricLists.get("durations");
            double[] groundInterarrivalsArray = groundMetricLists.get("interarrivals");
            double[] testMaxesArray = testMetricLists.get("maxes");
            double[] testSumsArray = testMetricLists.get("sums");
            double[] testDurationsArray = testMetricLists.get("durations");
            double[] testInterarrivalsArray = testMetricLists.get("interarrivals");

            KolmogorovSmirnovTest ksTest = new KolmogorovSmirnovTest();
            double ksMax = ksTest.kolmogorovSmirnovStatistic(testMaxesArray, groundMaxesArray);
            double ksSum = ksTest.kolmogorovSmirnovStatistic(testSumsArray, groundSumsArray);
            double ksDuration = ksTest.kolmogorovSmirnovStatistic(testDurationsArray, groundDurationsArray);
            double ksInterarrivals = ksTest.kolmogorovSmirnovStatistic(testInterarrivalsArray, groundInterarrivalsArray);
            double fitness = ksMax + ksSum + ksDuration + ksInterarrivals;

            try {
                File outputFile = new File(outputPath);
                FileWriter fw;

                fw = new FileWriter(outputFile, true);
                fw.write(i + "," + ksMax + "," + ksSum + "," + ksDuration + "," + ksInterarrivals + "," + fitness + "\n");
                fw.flush();
                fw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public Map<String, double[]> convertListsToArrays(List<Double> maxesList, List<Double> sumsList, 
                                                     List<Double> durationsList, List<Double> interarrivalsList) {
        Map<String, double[]> metricArrays = new HashMap<String, double[]>();
        double[] maxes = null;
        double[] sums = null;
        double[] durations = null;
        double[] interarrivals = null;
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

        metricArrays.put("maxes", maxes);
        metricArrays.put("sums", sums);
        metricArrays.put("durations", durations);
        metricArrays.put("interarrivals", interarrivals);

        return metricArrays;
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
