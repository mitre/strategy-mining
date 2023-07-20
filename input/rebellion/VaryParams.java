import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.math.BigDecimal;

import org.nlogo.core.LogoList;
import org.nlogo.headless.HeadlessWorkspace;

public class VaryParams {
    private String[] newRules = {"( greaterThan  ( subtract   ( reportGrievance )   ( reportArrestProb ) )  ( reportThreshold ) )",
                                 "( greaterThan  ( subtract  ( subtract   ( reportGrievance )   ( reportArrestProb ) )  ( reportArrestProb ) )  ( reportThreshold ) )",
                                 "( greaterThan  ( add  ( subtract   ( reportGrievance )   ( reportArrestProb ) ) ( multiply   ( reportArrestProb )   ( reportAversion ) ))  ( reportThreshold ) )",
                                 "( greaterThan  ( subtract   ( reportGrievance )  ( multiply   ( reportArrestProb )  ( multiply   ( reportArrestProb )   ( reportGrievance ) )))  ( reportThreshold ) )",
                                 "( greaterThan  ( subtract  ( multiply  ( multiply  ( multiply   ( reportAversion )   ( reportAversion ) ) ( subtract   ( reportGrievance )   ( reportAversion ) )) ( add  ( multiply   ( reportAversion )   ( reportAversion ) ) ( multiply   ( reportAversion )   ( reportAversion ) ))) ( add  ( add   ( reportArrestProb )   ( reportGrievance ) ) ( subtract   ( reportGrievance )   ( reportAversion ) )))  ( reportThreshold ) )",
                                 "( greaterThan  ( add  ( multiply  ( add  ( subtract   ( reportArrestProb )   ( propLinksActive ) ) ( multiply  ( add   ( reportArrestProb )   ( reportArrestProb ) ) ( multiply   ( reportArrestProb )  ( subtract   ( reportGrievance )   ( propLinksActive ) )))) ( multiply   ( reportArrestProb )   ( reportGrievance ) )) ( subtract  ( subtract   ( reportGrievance )   ( propLinksActive ) ) ( subtract   ( reportArrestProb )   ( propLinksActive ) )))  ( reportThreshold ) )",
                                 "( greaterThan  ( add  ( subtract   ( reportGrievance )   ( reportArrestProb ) ) ( multiply   ( reportArrestProb )  ( add  ( multiply   ( reportArrestProb )   ( reportAversion ) ) ( add  ( subtract   ( reportGrievance )   ( reportArrestProb ) )  ( reportGrievance ) ))))  ( reportThreshold ) )",
                                 "( greaterThan  ( subtract   ( reportGrievance )  ( multiply   ( reportArrestProb )   ( reportAversion ) ))  ( reportThreshold ) )",
                                 "( greaterThan  ( add  ( multiply  ( add  ( subtract   ( reportArrestProb )   ( propLinksActive ) ) ( multiply  ( multiply   ( reportArrestProb )   ( reportGrievance ) ) ( multiply   ( reportArrestProb )  ( subtract  ( add  ( multiply  ( add   ( reportArrestProb )   ( reportArrestProb ) ) ( add  ( multiply  ( add  ( multiply  ( add   ( reportArrestProb )   ( reportArrestProb ) ) ( add  ( multiply  ( add   ( reportArrestProb )   ( reportArrestProb ) ) ( multiply   ( reportArrestProb )   ( reportGrievance ) ))  ( reportArrestProb ) )) ( multiply   ( reportArrestProb )  ( multiply   ( reportArrestProb )   ( reportGrievance ) ))) ( subtract   ( reportArrestProb )   ( propLinksActive ) )) ( add  ( multiply  ( add   ( reportArrestProb )   ( reportArrestProb ) ) ( multiply   ( reportArrestProb )   ( reportGrievance ) )) ( add  ( multiply  ( add   ( reportArrestProb )   ( reportArrestProb ) )  ( reportGrievance ) ) ( subtract  ( subtract   ( reportGrievance )   ( propLinksActive ) ) ( multiply  ( add   ( reportArrestProb )   ( reportArrestProb ) ) ( multiply   ( reportArrestProb )   ( reportGrievance ) ))))))) ( multiply  ( add   ( reportArrestProb )   ( reportArrestProb ) ) ( multiply   ( reportArrestProb )   ( reportGrievance ) )))  ( propLinksActive ) )))) ( multiply   ( reportArrestProb )   ( reportGrievance ) )) ( subtract  ( subtract   ( reportGrievance )   ( propLinksActive ) ) ( subtract   ( reportArrestProb )   ( propLinksActive ) )))  ( reportThreshold ) )",
                                 "( greaterThan  ( subtract  ( subtract   ( reportGrievance )  ( multiply   ( reportArrestProb )  ( multiply   ( reportArrestProb )   ( reportAversion ) ))) ( multiply   ( reportArrestProb )   ( reportAversion ) ))  ( reportThreshold ) )"};
    private String modelPathString = "/Users/aisherwood/Documents/strategy-mining/input/rebellion/Rebellion_02.nlogo";
    private String factorsPathString = "/Users/aisherwood/Documents/strategy-mining/input/rebellion/factors.nls";
    private String outputPath = "/Users/aisherwood/Documents/strategy-mining/output/rebellion_parameter_testing/";
    private String leftJoin = "";
    private String rightJoin = "";
    private int ticks = 500;
    private int numReplications = 1;
    private BigDecimal minCopDensity = new BigDecimal("0.0");
    private BigDecimal maxCopDensity = new BigDecimal("10.0");
    private BigDecimal incCopDensity = new BigDecimal("0.5");
    private BigDecimal minLegitimacy = new BigDecimal("0.0");
    private BigDecimal maxLegitimacy = new BigDecimal("1.0");
    private BigDecimal incLegitimacy = new BigDecimal("0.05");

    public VaryParams() {
        setup();
    }

    private void setup() {
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
        VaryParams exp = new VaryParams();

        if (args.length == 2) {
            int ruleNum = Integer.parseInt(args[0]);
            int rep = Integer.parseInt(args[1]);
            String rule = exp.newRules[ruleNum];
            String customModel = exp.buildModel(rule);

            exp.varyRuns(rule, customModel, ruleNum, rep);
        } else {
            for (int ruleNum = 0; ruleNum < exp.newRules.length; ruleNum++) {
                String rule = exp.newRules[ruleNum];
                String customModel = exp.buildModel(rule);

                for (int rep = 0; rep < exp.numReplications; rep++) {
                    exp.varyRuns(rule, customModel, ruleNum, rep);
                }
            }
        }
    }

    private String buildModel(String rule) {
        return (leftJoin + rule + rightJoin);
    }

    private void createOutputFile(int ruleNum, int rep) {
        try {
            File outputFile = new File(outputPath + ruleNum + "_" + rep + "_output.csv");

            if (!outputFile.getParentFile().exists()) {
                outputFile.getParentFile().mkdirs();
            }

            FileWriter fw = new FileWriter(outputFile, false);
            fw.write("Rule,RuleNum,Rep,CopDensity,Legitimacy,Fitdata\n");
            fw.flush();
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // cantor pairing function
    private int createSeed(int x, int y) {
        return ((1 / 2) * (x + y) * (x + y + 1) + y);
    }

    private void runExperiment(String rule, String customModel, int ruleNum, int rep, int seed, BigDecimal copDensity, BigDecimal legitimacy) {
        LogoList fitData = null;
        HeadlessWorkspace workspace = HeadlessWorkspace.newInstance();

        workspace.openString(customModel);
        workspace.command("random-seed " + seed);
        workspace.command("set initial-cop-density " + copDensity);
        workspace.command("set government-legitimacy " + legitimacy);
        workspace.command("setup");
        workspace.command("repeat " + ticks + " [ go ]");
        fitData = (LogoList) workspace.report("metric");

        try {
            workspace.dispose();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } 
        
        writeOutput(rule, ruleNum, rep, copDensity, legitimacy, fitData);
    }

    private void varyRuns(String rule, String customModel, int ruleNum, int rep) {
        int seed = createSeed(ruleNum, rep);
        createOutputFile(ruleNum, rep);

        for (BigDecimal copDensity = minCopDensity; copDensity.compareTo(maxCopDensity) < 1; copDensity = copDensity.add(incCopDensity)) {
            for (BigDecimal legitimacy = minLegitimacy; legitimacy.compareTo(maxLegitimacy) < 1; legitimacy = legitimacy.add(incLegitimacy)) {
                runExperiment(rule, customModel, ruleNum, rep, seed, copDensity, legitimacy);
            }
        }
    }

    private void writeOutput(String rule, int ruleNum, int rep, BigDecimal copDensity, BigDecimal legitimacy, LogoList fitData) {
        try {
            FileWriter fw = new FileWriter(outputPath + ruleNum + "_" + rep + "_output.csv", true);

            fw.write(rule + "," + ruleNum + "," + rep + "," + copDensity + "," + legitimacy + ",\"" + fitData + "\"\n");
            fw.flush();
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
