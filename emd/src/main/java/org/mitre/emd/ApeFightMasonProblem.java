package org.mitre.emd;

import ec.EvolutionState;
import ec.Individual;
import ec.gp.GPIndividual;
import ec.gp.GPProblem;
import ec.gp.koza.KozaFitness;
import ec.util.Parameter;
import org.mitre.emd.models.ape_fight.ApeFight;
import org.mitre.emd.output.OutputWriter;
import org.mitre.emd.output.beans.ResultsBean;
import org.nlogo.core.LogoList;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class ApeFightMasonProblem extends GPProblem {
    public GPIndividual ind;
    public ApeFight simstate;
    public int threadNum;
    public EvolutionState evolutionState;

    int populationSize;
    int replication;
    int ticks;
    OutputWriter outputWriter = new OutputWriter();



    @Override
    public void evaluate(EvolutionState evolutionState, Individual ind, int subpopulation, int threadnum) {

        // Get the tree
        String rules = ((GPIndividual) ind).trees[0].child.makeLispTree().replaceAll(",", "");

        modelSetup(evolutionState, threadnum);

        simstate.problem = this;
        this.evolutionState = evolutionState;
        this.threadNum = threadnum;

        simstate.start();
        this.ind = (GPIndividual) ind;
        simstate.runSim();

        // NOTE: This sets the fitness as the number of primates not fighting. In ECJ larger values are "fitter".
        Double fitness = simstate.getNumPrimates() - simstate.countFighting() + 0.0;

        KozaFitness f = ((KozaFitness)ind.fitness);
        f.setStandardizedFitness(evolutionState, fitness);
        f.hits = 0;
        ind.evaluated = true;

//        ((GPIndividual)ind).printTrees(evolutionState, 0);
        ArrayList<Integer> fitData = simstate.getFightSizeList();
        ResultsBean results = new ResultsBean(evolutionState.generation, "parsedRule", rules, fitness, fitData){};
        outputWriter.recordResults(results);
    }

    public void modelSetup(EvolutionState state, int threadnum) {
        populationSize = state.parameters.getInt(new Parameter("pop.subpop.0.size"), null);
        replication = 0;
        if(state.parameters.exists(new Parameter("replication"), null)){
            replication = state.parameters.getInt(new Parameter("replication"), null);
        }
        ticks = state.parameters.getInt(new Parameter("ticks"), null);

        if (simstate == null)
        {
            simstate = new ApeFight(ticks,1,"");  // Seed doesn't matter here
        }
        simstate.random = state.random[threadnum];		// this is the real generator we'll use
    }


    /**
     *
     *
     * The following are carryovers from the NetLogo implementation and not currently used. Delete when appropriate
     *
     */


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
