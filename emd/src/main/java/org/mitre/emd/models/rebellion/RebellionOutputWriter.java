package org.mitre.emd.rebellion;

import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class RebellionOutputWriter extends OutputWriter {
    public RebellionOutputWriter(List<String> factorList) {
        super(factorList);
    }
    
    public void writeFile(String outputPath, int generation, String rules, double ksMax, double ksSum, 
                          double ksDuration, double ksInterarrival, double fitness, Object metrics) {
        try {
            FileWriter fw = new FileWriter(outputPath, true);
            Map<String, Integer> factorFreq = this.countFactors(rules);
            String frequencies = "";

            for (String factor : getFactors()) {
                frequencies += "," + factorFreq.get(factor);
            }

            fw.write(generation + "," + rules + "," + ksMax + "," + ksSum + "," + ksDuration + "," + 
                     ksInterarrival + "," + fitness + ",\"" + metrics + "\"" + frequencies + "\n");
            fw.flush();
            fw.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
