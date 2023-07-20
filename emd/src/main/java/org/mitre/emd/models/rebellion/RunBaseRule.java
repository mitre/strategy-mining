package org.mitre.emd.rebellion;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.nlogo.api.LogoException;
import org.nlogo.core.CompilerException;
import org.nlogo.headless.HeadlessWorkspace;

public class RunBaseRule {
    private String modelPath = "/Users/aisherwood/Documents/strategy-mining/input/rebellion/Rebellion_02.nlogo";
    private String outputPath = "/Users/aisherwood/Documents/strategy-mining/output/fixedRuleOutput.csv";
    private String netlogoExtensions = "/Applications/NetLogo 6.1.1/extensions/.bundled";
    private File outputFile = null;
    private int replications = 30;
    private int ticks = 500;

    public void setup() {
        outputFile = new File(outputPath);

        if (!outputFile.getParentFile().exists()) {
            outputFile.getParentFile().mkdirs();
        }

        try {
            FileWriter fw = new FileWriter(outputFile, false);
            fw.flush();
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.setProperty("netlogo.extensions.dir", netlogoExtensions);
    }

    public static void main(String[] args) {
        RunBaseRule run = new RunBaseRule();
        run.setup();

        for (int i = 0; i < run.replications; i++) {
            HeadlessWorkspace workspace = HeadlessWorkspace.newInstance();
            
            try {
                workspace.open(run.modelPath, true);
                workspace.command("setup");
                workspace.command("repeat " + run.ticks + " [ go ]");
                
                Object metric = workspace.report("metric");
                FileWriter fw = new FileWriter(run.outputFile, true);
                fw.write(metric + "\n");
                fw.flush();
                fw.close();
            } catch (CompilerException e) {
                e.printStackTrace();
            } catch (LogoException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
