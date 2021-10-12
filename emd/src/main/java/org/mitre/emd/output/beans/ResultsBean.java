package org.mitre.emd.output.beans;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

import org.supercsv.cellprocessor.constraint.NotNull;
import org.supercsv.cellprocessor.ift.CellProcessor;

public class ResultsBean implements OutputBean, Serializable {
    Integer gen;
    String rule;
    Double fitness;
    ArrayList<Integer> fitData = new ArrayList<>();

    public static final String[] header = { "Gen", "rule", "Fitness", "fitData"};
    public static final String[] fieldMapping = new String[] {
            "gen",
            "rule",
            "fitness",
            "fitData"
    };
    //TODO: set cell processors to do desired decimal formatting
    public static final CellProcessor[] processors = new CellProcessor[]{
            new NotNull(),
            new NotNull(),
            new NotNull(),
            new NotNull()
    };

    public ResultsBean(){
    }


    public ResultsBean(Integer gen, String rule, Double fitness, ArrayList<Integer> fitData){
        this.gen = gen;
        this.rule = rule;
        this.fitness = fitness;
        this.fitData = fitData;
    }

    public Integer getGen() {
        return gen;
    }

    public void setGen(Integer gen) {
        this.gen = gen;
    }

    public String getRule() {
        return rule;
    }

    public void setRule(String rule) {
        this.rule = rule;
    }

    public Double getFitness() {
        return fitness;
    }

    public void setFitness(Double fitness) {
        this.fitness = fitness;
    }

    public ArrayList<Integer> getFitData() {
        return fitData;
    }

    public void setFitData(ArrayList<Integer> fitData) {
        this.fitData = fitData;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ResultsBean that = (ResultsBean) o;
        return gen.equals(that.gen) && rule.equals(that.rule) && fitness.equals(that.fitness) && fitData.equals(that.fitData);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(gen, rule, fitness, fitData);
        return result;
    }
}
