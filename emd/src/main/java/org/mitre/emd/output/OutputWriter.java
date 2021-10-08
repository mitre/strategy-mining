package org.mitre.emd.output;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mitre.emd.output.beans.OutputBean;
import org.mitre.emd.output.beans.ResultsBean;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvBeanWriter;
import org.supercsv.io.ICsvBeanWriter;
import org.supercsv.prefs.CsvPreference;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Class to write model outputs to CSV files.
 *
 * @author J.G. Veneman
 */
public class OutputWriter {
    private static final Logger LOGGER = LogManager.getLogger();

    //TODO get this from the input parameters file
    public String resultsLogFullPath = Paths.get("../output/output.csv").toAbsolutePath().toString();

    public static boolean writeHeader = true;

    public OutputWriter() {
    }

    /**
     * Helper function to delete, then create an output file with headers.
     * @param fileName
     * @param fieldMapping
     * @param header
     * @param processors
     * @param outputDir
     */
    public void initOutput(String fileName, String[] fieldMapping, String[] header, CellProcessor[] processors, File outputDir){
        try {
            resultsLogFullPath = Paths.get(outputDir.toString(),fileName).toAbsolutePath().toString();
            File file = new File(outputDir, fileName);
            LOGGER.info("Writing " + fileName + " log to: " + file);
            if(file.exists() && writeHeader == true){
                Files.deleteIfExists(file.toPath());
            }

            file.createNewFile();
            OutputWriter.writeOutput(null,header, fieldMapping, processors, file.getAbsolutePath(), writeHeader);
            writeHeader = false;


            // TODO What should we do about running multiple times? Should we delete, rename...?



        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Method to either write the header (with header == true) or append rows to an output file.
     *
     * @param bean
     * @param header
     * @param fieldMapping
     * @param processors
     * @param fileName
     * @param writeHeader
     */
    public static void writeOutput(OutputBean bean, String[] header, String[] fieldMapping,
                                   CellProcessor[] processors, String fileName, boolean writeHeader) {
        ICsvBeanWriter beanWriter = null;
        try {
            beanWriter = new CsvBeanWriter(new FileWriter(fileName, true),
                    CsvPreference.STANDARD_PREFERENCE);

            // the header elements are used to map the bean values to each column (names must match)

            // write the header
            if (writeHeader) {
                beanWriter.writeHeader(header);
                return; // must return here or you'll get an NPE below since there are no beans when writing the header
            }

            // write the bean
            beanWriter.write(bean, fieldMapping, processors);


        } catch (IOException e) {
            String msg = "Unable to write output file for " + bean.toString();
            LOGGER.error(msg, e);
            e.printStackTrace();
        } finally {
            if (beanWriter != null) {
                try {
                    beanWriter.close();
                } catch (IOException e) {
                    LOGGER.error("Problem closing output file", e);
                }
            }
        }
    }

    public void recordResults(ResultsBean results) {
        OutputWriter.writeOutput(results,ResultsBean.header,ResultsBean.fieldMapping,ResultsBean.processors, resultsLogFullPath, false);
    }
}
