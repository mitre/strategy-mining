package org.mitre.emd;

import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OutputWriter {
    private List<String> factors = null;

    public OutputWriter(List<String> factorList) {
        factors = factorList;
    }

    public Map<String, Integer> countFactors(String rules) {
        Map<String, Integer> factorFreq = new HashMap<>();
        String[] ruleList = rules.split(" ");

        for (int i = 0; i < factors.size(); i++) {
            factorFreq.put(factors.get(i), 0);
        }

        for (String rule : ruleList) {
            if (factorFreq.get(rule) != null) {
                factorFreq.merge(rule, 1, Integer::sum);
            }
        }
            
        return factorFreq;
    }

    public void writeFile(String outputPath, int generation, String rules, double fitness, Object metrics) {
        try {
            FileWriter fw = new FileWriter(outputPath, true);
            Map<String, Integer> factorFreq = countFactors(rules);
            String frequencies = "";

            for (String factor : factors) {
                frequencies += "," + factorFreq.get(factor);
            }

            fw.write(generation + "," + rules + "," + fitness + ",\"" + metrics + "\"" + frequencies + "\n");
            fw.flush();
            fw.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<String> getFactors() {
        return factors;
    }
}
