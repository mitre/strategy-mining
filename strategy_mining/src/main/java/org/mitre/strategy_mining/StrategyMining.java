package org.mitre.strategy_mining;

import ec.EvolutionState;
import ec.Evolve;
import ec.util.*;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mitre.emd.output.OutputWriter;
import org.mitre.emd.output.beans.ResultsBean;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Properties;

/**
 * Main application entry point for running StrategyMining.
 *
 * StrategyMining can be run as either a standalone application or as distributed application deployed on an HPC cluster.
 */
public class StrategyMining {
    public static final Logger logger = LogManager.getLogger(StrategyMining.class);
    static String inputFile = "";

    public StrategyMining(){
        setupOutput();
    }

    public static void main(String[] args) {
        int numArgs = 0;
        Boolean runDistributed = false;
        String nodes = "";
        String outputPath = "";
        String rowNumber = "";
        String replication = "";

        // TODO perform any custom setup for different types of models here before calling Evolve.

        CommandLine commandLine;
        Option help = new Option( "help", "Print this message" );
        Option optInputFile = Option.builder("f").argName("file").hasArg().required().desc("REQUIRED: The configuration file name including its relative or absolute path.").build();
        Option optDistributed = Option.builder("d").argName("isDistributed").hasArg().desc("Flag that determines whether to run in distributed mode.").build();
        Option optNodes = Option.builder("n").argName("nodes").hasArg().desc("List of nodes for running distributed.").build();
        Option optOutputPath = Option.builder("op").argName("outputPath").hasArg().desc("Path to write output.").build();
        Option optRowNumber = Option.builder("rn").argName("rowNumber").hasArg().desc("Row number.").build();
        Option optReplication = Option.builder("rp").argName("replication").hasArg().desc("Replication number.").build();
        Options options = new Options();
        CommandLineParser parser = new DefaultParser();

        options.addOption(help);
        options.addOption(optInputFile);
        options.addOption(optDistributed);
        options.addOption(optNodes);
        options.addOption(optOutputPath);
        options.addOption(optRowNumber);
        options.addOption(optReplication);

        String header = "----------------------------------------------------------------------" + System.lineSeparator() +
                "       Options, flags, and arguments may be in any order";
        String footer = "----------------------------------------------------------------------";
        String helpMsg = "java -cp strategy_mining-<version>.jar org.mitre.strategy_mining.StrategyMining"; // TODO update this
        HelpFormatter formatter = new HelpFormatter();

        try {
            commandLine = parser.parse(options, args);

            if (args.length == 0 || commandLine.hasOption("help")) {
                formatter.printHelp(helpMsg, header, options, footer, true);
                return;
            }

            if (!commandLine.hasOption("f")) {
                System.err.println("An input file must be present. Run with -help for details on command line options.");
                return;
            }

            if (commandLine.hasOption("f")) {
                inputFile = commandLine.getOptionValue("f");
                numArgs++;
            }

            if (commandLine.hasOption("d")) {
                runDistributed = Boolean.valueOf(commandLine.getOptionValue("d"));
                numArgs++;
            }

            if (commandLine.hasOption("n")) {
                nodes = commandLine.getOptionValue("n");
                numArgs++;
            }

            if (commandLine.hasOption("op")) {
                outputPath = commandLine.getOptionValue("op");
                numArgs++;
            }

            if (commandLine.hasOption("rn")) {
                rowNumber = commandLine.getOptionValue("rn");
                numArgs++;
            }

            if (commandLine.hasOption("rp")) {
                replication = commandLine.getOptionValue("rp");
                numArgs++;
            }

        } catch (ParseException exception) {
            System.out.print("Parse error: ");
            System.out.println(exception.getMessage());
            formatter.printHelp(helpMsg, header, options, footer, true);
        } catch (Exception e){
            System.err.println("Error: Something went terribly wrong.  See stack trace: " + args[0] + "\n" + e);
            e.printStackTrace();
            return;
        }

        StrategyMining model = new StrategyMining();

        if (!runDistributed) {
            String[] parsedArgs = new String[2 * numArgs];
            int pos = 0;

            parsedArgs[pos++] = "-file";
            parsedArgs[pos++] = inputFile;

            if (!outputPath.isEmpty()) {
                parsedArgs[pos++] = "-p";
                parsedArgs[pos++] = outputPath;
            }

            if (!rowNumber.isEmpty()) {
                parsedArgs[pos++] = "-p";
                parsedArgs[pos++] = rowNumber;
            }

            if (!replication.isEmpty()) {
                parsedArgs[pos++] = "-p";
                parsedArgs[pos++] = replication;
            }

            model.runStandalone(parsedArgs);
        } else {
            String[] parsedArgs = {"-file", inputFile, "-p", nodes};

            model.runDistributed(parsedArgs);
        }
    }

    private void runStandalone(String[] parsedArgs) {
        long startTime = System.currentTimeMillis();
        ParameterDatabase dbase = Evolve.loadParameterDatabase(parsedArgs);
        ParameterDatabase child = new ParameterDatabase(); 

        child.addParent(dbase);
        Output out = Evolve.buildOutput();
        EvolutionState evaluatedState = Evolve.initialize(child, 0, out);

        // DEBUG
//        System.out.println(System.getProperty("java.class.path").replace(':', '\n'));

        evaluatedState.run(EvolutionState.C_STARTED_FRESH);

        logger.info("Run time: " + (System.currentTimeMillis() - startTime)/Math.pow(10,3) + "s");
    }

    private void runDistributed(String[] parsedArgs) {
        ec.eval.Slave.main(parsedArgs);
    }

    private void setupOutput() {
        try (InputStream input = new FileInputStream(inputFile)) {
            Properties prop = new Properties();

            // load the input params file
            prop.load(input);

            String outputDirectoryString = prop.getProperty("outputFileDirectory");
            if(outputDirectoryString == null){
                System.err.println("outputFileDirectory property in " + inputFile + " not found. Setting to " +
                        "default value of \"../output/\"");
                outputDirectoryString = "../output/";
            }
            String outputFileName = prop.getProperty("outputFileName");
            if(outputFileName == null){
                System.err.println("outputFileName property in " + inputFile + " not found. Setting to " +
                        "default value of \"output.csv\"");
                outputFileName = "output.csv";
            }
            File outputDirectory = Path.of(outputDirectoryString).toAbsolutePath().toFile();
            OutputWriter outputWriter = new OutputWriter();
            outputWriter.initOutput(outputFileName, ResultsBean.fieldMapping,ResultsBean.header,ResultsBean.processors,outputDirectory);
        } catch (IOException ex) {
            System.err.println("Error loading params file.");
//            logger.error("Error loading params file.",ex);
        }
    }

}
