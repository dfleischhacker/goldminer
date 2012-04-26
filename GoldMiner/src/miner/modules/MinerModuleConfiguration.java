package miner.modules;

import miner.ontology.AssociationRulesParser;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLDataFactory;

import java.sql.Connection;

/**
 * Encapsulates the configuration of a {@link MinerModule}.
 *
 * The configuration contains some general ones (like the database connection to use) and miner-specific ones which
 * are provided as the corresponding part of the configuration XML file.
 */
public class MinerModuleConfiguration {

    private boolean writeAnnotations;

    private IRI supportAnnotationUri;

    private IRI confidenceAnnotationUri;

    private Connection dbConnection;

    private AssociationRulesParser parser;

    private OWLDataFactory factory;

    private double supportThreshold;

    private double confidenceThreshold;

    /**
     * Returns the configured OWLDataFactory
     * @return data factory to use
     */
    public OWLDataFactory getFactory() {
        return factory;
    }

    /**
     * Sets the OWLDataFactory
     * @param factory data factory to store in this configuration
     */
    public void setDataFactory(OWLDataFactory factory) {
        this.factory = factory;
    }

    /**
     * Returns an already opened connection to the database to write data to.
     * Modules must not close this connection!
     * @return dbConnection opened connection to database
     */
    public Connection getDbConnection() {
        return dbConnection;
    }

    /**
     * Set the database connection for this configuration.
     * @param dbConnection opened database connection
     */
    public void setDbConnection(Connection dbConnection) {
        this.dbConnection = dbConnection;
    }

    /**
     * Returns a parser for parsing common association rule syntax constructs
     * @return an association rule parser
     */
    public AssociationRulesParser getParser() {
        return parser;
    }

    /**
     * Set the parser for {@link #getParser()}.
     * @param parser parser to set
     */
    public void setParser(AssociationRulesParser parser) {
        this.parser = parser;
    }

    public IRI getSupportAnnotationUri() {
        return supportAnnotationUri;
    }

    public void setSupportAnnotationUri(IRI supportAnnotationUri) {
        this.supportAnnotationUri = supportAnnotationUri;
    }

    public IRI getConfidenceAnnotationUri() {
        return confidenceAnnotationUri;
    }

    public void setConfidenceAnnotationUri(IRI confidenceAnnotationUri) {
        this.confidenceAnnotationUri = confidenceAnnotationUri;
    }

    public boolean getWriteAnnotations() {
        return writeAnnotations;
    }

    public void setWriteAnnotations(boolean writeAnnotations) {
        this.writeAnnotations = writeAnnotations;
    }

    public double getSupportThreshold() {
        return supportThreshold;
    }

    public void setSupportThreshold(double supportThreshold) {
        this.supportThreshold = supportThreshold;
    }

    public double getConfidenceThreshold() {
        return confidenceThreshold;
    }

    public void setConfidenceThreshold(double confidenceThreshold) {
        this.confidenceThreshold = confidenceThreshold;
    }
}
