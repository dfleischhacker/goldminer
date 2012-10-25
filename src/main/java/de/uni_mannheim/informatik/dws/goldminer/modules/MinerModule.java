package de.uni_mannheim.informatik.dws.goldminer.modules;

import org.semanticweb.owlapi.model.OWLAxiom;

import java.io.File;
import java.io.OutputStream;
import java.util.Set;

/**
 * Defines the interface for a GoldMiner mining module.
 * <p/>
 * Each interface encapsulates all steps required to inductively mine OWL axioms from data by using association rules.
 */
public abstract class MinerModule {
    private MinerModuleConfiguration config;

    /**
     * Sets the {@link MinerModuleConfiguration} instance which belongs to this MinerModule.
     *
     * @param config configuration for this module
     */
    public final void setConfiguration(MinerModuleConfiguration config) throws MinerModuleException {
        this.config = config;
    }

    /**
     * Returns the configuration for this module
     *
     * @return configuration for this module
     */
    public final MinerModuleConfiguration getConfig()  throws MinerModuleException {
        return config;
    }

    /**
     * Returns the a name for this module.
     *
     * @return name of this module
     */
    public abstract String getModuleName();

    /**
     * Returns a string to use in file names related to this module. The string returned by this method must adhere to
     * the regular expression "[A-Za-z0-9_]+".
     *
     * @return string to use in file names related to this miner module
     */
    public abstract String getFileString();

    /**
     * Returns a more readable description of what is created by this module.
     *
     * @return description of this module's purpose
     */
    public abstract String getDescription();

    /**
     * Set up the required schema in the database table which is defined by the configuration of this module.
     */
    public abstract void setupSchema() throws MinerModuleException;

    /**
     * Fill the database table with data. The data source is provided by the configuration as a SPARQL endpoint.
     */
    public abstract void acquireTerminology() throws MinerModuleException;

    /**
     * Generate the transaction tables for this miner module. The data has to be written into the provided
     * <code>output</code> stream which is managed by the miner itself.
     *
     * @param output stream to write transaction table data to
     */
    public abstract void generateTransactionTable(OutputStream output) throws MinerModuleException ;

    /**
     * Start the generation of association rules from the file <code>inputFile</code> containing the transaction table.
     * The results have to be contained in the file given by the absolute path <code>outputFile</code>.
     * <p/>
     * The miner module is not allowed to modify the given input file.
     *
     * @param inputFile  file to read transaction table from
     * @param outputFile file to write association rules to
     */
    public abstract void generateAssociationRules(File inputFile, File outputFile) throws MinerModuleException ;

    /**
     * Convert the association rules contained in the given file <code>inputFile</code> into OWL axioms also containing
     * the corresponding confidence value.
     * <p/>
     * Filtering of generated association rules might either occur here or in {@link
     * #generateAssociationRules(java.io.File, java.io.File)}.
     *
     * @param inputFile file containing the association rules to read
     */
    public abstract Set<OWLAxiom> readAssociationRules(File inputFile) throws MinerModuleException ;
}
