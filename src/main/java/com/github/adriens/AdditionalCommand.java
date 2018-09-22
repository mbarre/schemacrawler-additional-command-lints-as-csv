package com.github.adriens;

import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import schemacrawler.tools.executable.BaseSchemaCrawlerCommand;
import schemacrawler.tools.lint.Lint;
import schemacrawler.tools.lint.LintUtility;
import schemacrawler.tools.lint.LintedCatalog;
import schemacrawler.tools.lint.LinterConfigs;
import schemacrawler.tools.lint.Linters;
import schemacrawler.tools.lint.executable.LintOptions;
import schemacrawler.tools.lint.executable.LintOptionsBuilder;

/**
 * SchemaCrawler command plug-in.
 *
 * @see <a href="https://www.schemacrawler.com">SchemaCrawler</a>
 *
 * @author Automatically generated by SchemaCrawler 15.01.02
 */
public class AdditionalCommand
        extends BaseSchemaCrawlerCommand {

    private static final Logger LOGGER = Logger
            .getLogger(AdditionalCommand.class.getName());

    static final String COMMAND = "csv";
    //public static final String DEFAULT_LINTS_FILENAME = "lints.csv";
    private String lintsFilename;
    private static final String DEFAULT_LINTS_FILENAME_PREFIX = "schemacrawler-lints";
    private static final String NEW_LINE_SEPARATOR = "\n";
    private static final Object[] FILE_HEADER = {"sclint-dbenv,sclint-dbid,sclint-runid","sclint-hitid","sclint-linterId", "sclint-severity", "sclint-objectName", "sclint-message", "sclint-value"};

    protected AdditionalCommand() {
        super(COMMAND);
    }

    @Override
    public void checkAvailibility()
            throws Exception {
        // Nothing additional to check at this point. The Command should be available
        // after the class is loaded, and imports are resolved.
    }

    @Override
    public void execute()
            throws Exception {
        // TODO: Possibly process command-line options, which are available
        // in additionalConfiguration

        // Options
        final LintOptions lintOptions = LintOptionsBuilder.builder().fromConfig(additionalConfiguration).toOptions();
        //setLintsFilename(additionalConfiguration.getStringValue("outputfile", DEFAULT_LINTS_FILENAME));
        
        // get and set dbid
        String dbId = additionalConfiguration.getStringValue("dbid", "");
        LOGGER.log(Level.INFO,String.format("Got input dbId : <%s>", dbId ));
                        
        // get and set dbenv
        //dbenv
        String dbEnv = additionalConfiguration.getStringValue("dbenv", "").toLowerCase();
        LOGGER.log(Level.INFO,String.format("Got input dbEnv : <%s>", dbEnv ));
        
        CSVPrinter csvFilePrinter;
        FileWriter fileWriter;
        CSVFormat csvFileFormat = CSVFormat.DEFAULT.withRecordSeparator(NEW_LINE_SEPARATOR);
        UUID runId = UUID.randomUUID(); 

        setLintsFilename(String.format("%s-%s.csv", DEFAULT_LINTS_FILENAME_PREFIX, runId.toString()));
        LOGGER.log(Level.INFO, String.format("Generating lints for run <%s> ...", runId.toString()));
        final LinterConfigs linterConfigs = LintUtility.readLinterConfigs(lintOptions, getAdditionalConfiguration());
        
        final Linters linters = new Linters(linterConfigs);
        final LintedCatalog lintedCatalog = new LintedCatalog(catalog, connection, linters);
        Iterator<Lint<?>> lintIter = lintedCatalog.getCollector().iterator();
        // feed the csv
        Lint aLint;
        fileWriter = new FileWriter(getLintsFilename());

        //initialize CSVPrinter object
        csvFilePrinter = new CSVPrinter(fileWriter, csvFileFormat);

        //Create CSV file header
        
        // Do not print headers
        //csvFilePrinter.printRecord(FILE_HEADER);
        while (lintIter.hasNext()) {
            aLint = lintIter.next();
            List lintDataRecord = new ArrayList();
            // put runid and lint it id
            lintDataRecord.add(dbEnv);
            lintDataRecord.add(dbId);
            lintDataRecord.add(runId.toString());
            lintDataRecord.add(UUID.randomUUID().toString());
            
            lintDataRecord.add(aLint.getLinterId());
            lintDataRecord.add(aLint.getSeverity().toString().toUpperCase());
            lintDataRecord.add(aLint.getObjectName());
            lintDataRecord.add(aLint.getMessage());
            lintDataRecord.add(aLint.getValueAsString());

            csvFilePrinter.printRecord(lintDataRecord);
        }
        fileWriter.flush();
        fileWriter.close();
        LOGGER.log(Level.INFO, String.format("Lint runid : <%s> generated.", runId.toString()));
    }

    public String getLintsFilename() {
        return this.lintsFilename;
    }
    public void setLintsFilename(String aLintsFilename) {
        this.lintsFilename = aLintsFilename;
    }
}