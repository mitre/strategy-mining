package org.mitre.emd;

import ec.util.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import ec.*;

import org.nlogo.core.LogoList;

public class ApeFightProblem extends GeneralProblem {

    @Override
    public double calculateFitness(List<Object> metrics, EvolutionState state) {
        int rowNumberTarget = state.parameters.getInt(new Parameter("rowNumber"), null);
        String inputFile = state.parameters.getString(new Parameter("inputPath"), null);

        String fitData = getFitData(inputFile, rowNumberTarget);

        LogoList list1 = (LogoList) metrics.get(0);
        double[] list1arr = new double[list1.size()];
        String data = fitData.replace("\"", "").replace("\"", "").replace("[", "").replace("]", "");
        String[] numbers = data.split(", ");
        double[] list2arr = new double[numbers.length];
        for (int i = 0; i < numbers.length; i++) {
            list2arr[i] = Double.parseDouble(numbers[i]);
        }
        for (int i = 0; i < list1.size(); i++) {
            list1arr[i] = (Double) list1.get(i);
        }
        double val = kruskal(list1arr, list2arr);
        if (Double.isNaN(val) || val <= 0) {
            val = 1e-16;
        }
        return val;
    }

    @Override
    public String[] setupCommands(EvolutionState state) {
        int popSize = state.parameters.getInt(new Parameter("pop.subpop.0.size"), null);
        int rowNumber = state.parameters.getInt(new Parameter("rowNumber"), null);
        int replication = 0;
        if(state.parameters.exists(new Parameter("replication"), null)){
            replication = state.parameters.getInt(new Parameter("replication"), null);
        }

        if (popSize != 0 && replication != 0) {
            int seed = popSize * rowNumber + replication + 1;
            return new String[] { "random-seed " + seed, "setup" };
        } else {
            return new String[] { "setup" };
        }
    }

    public String getFitData(String inputFile, int rowNumberTarget)  {
        String row = getRow(inputFile, rowNumberTarget);
        return row.substring(row.indexOf("\"["));
    }

    public String getParsedRule(String inputFile, int rowNumberTarget) {
        String row = getRow(inputFile, rowNumberTarget);
        return row.split(",")[1].strip();
    }

    public String getRow(String inputFile, int rowNumberTarget) {
        Scanner scan;
        try {
            scan = new Scanner(new File(inputFile));
            int rowNumber = 0;
            String row = "";
            while(rowNumber < rowNumberTarget) {
                row = scan.nextLine();
                rowNumber++;
            }
            return row;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return "";
    }
    public static double kruskal(double[] list1, double[] list2) {
        double[] all = new double[list1.length + list2.length];
        for(int i = 0; i < all.length; i++) {
            all[i] = i < list1.length ? list1[i] : list2[i-list1.length];
        }   
        double[] ranks = new double[all.length]; 

        for (int i = 0; i < all.length; i++) { 
            int r = 1, s = 1; 
            for (int j = 0; j < all.length; j++) { 
                if (j != i && all[j] < all[i]) { //something is less than me
                    r += 1; 
                }
                if (j != i && all[j] == all[i]) { //tie
                    s += 1;      
                }
            }
            ranks[i] = r + (double)(s - 1) / (double) 2; //# less than me plus half of the ties
        }  

        double meanRank1 = 0;
        double meanRank2 = 0;

        for(int i = 0; i < list1.length; i++) {
            meanRank1 += ranks[i];
        }
        meanRank1 /= list1.length;

        for(int i = 0; i < list2.length; i++) {
            meanRank2 += ranks[i+list1.length];
        }
        meanRank2 /= list2.length;

        double sum = list1.length*meanRank1*meanRank1 + list2.length*meanRank2*meanRank2;
        double testStatistics = sum * 12 / (all.length * (all.length + 1)) - 3 * (all.length + 1);

        Map<Double, Integer> counter = new HashMap<>();
        for(double d : ranks) {
            counter.put(d, counter.getOrDefault(d, 0)+1);
        }
        double term = 0;
        for(double d : counter.keySet()) {
            term += counter.get(d) * counter.get(d) * counter.get(d) - counter.get(d);
        }

        return testStatistics / (1 - (term/(all.length*all.length*all.length - all.length)));
    }

}
