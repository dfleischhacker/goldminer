package miner.modules;

import miner.ontology.ParsedAxiom;
import miner.util.ValueNormalizer;
import miner.util.ValueNormalizerFactory;
import org.semanticweb.owlapi.model.*;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

/**
 * Module for generating disjointProperty axioms
 */
public class PropertyDisjointnessModule extends MinerModule {
    private MinerModuleConfiguration config;

    // lookup maps
    private HashMap<Integer, String> propertyIdToUri = new HashMap<Integer, String>();
    private HashMap<String, String> uriToName = new HashMap<String, String>();
    private HashMap<String, Integer> uriToNormalId = new HashMap<String, Integer>();
    private HashMap<String, Integer> uriToDisjointnessId = new HashMap<String, Integer>();
    private HashMap<String, Integer> uriToSymmetryId = new HashMap<String, Integer>();
    private HashSet<Integer> symmetryIds = new HashSet<Integer>();
    private HashSet<Integer> disjointIds = new HashSet<Integer>();


    public PropertyDisjointnessModule(MinerModuleConfiguration config) throws MinerModuleException {
        this.config = config;

        // initialize cache hashes
        Statement stmt = null;
        try {
            stmt = config.getDbConnection().createStatement();
            ResultSet res = stmt.executeQuery("SELECT * FROM `properties`");

            while (res.next()) {
                int id = res.getInt("id");
                int disjointId = res.getInt("disjointID");
                int symmetryId = res.getInt("symmetryID");
                String uri = res.getString("uri");
                String name = res.getString("name");

                propertyIdToUri.put(id, uri);
                propertyIdToUri.put(symmetryId, uri);
                propertyIdToUri.put(disjointId, uri);
                symmetryIds.add(symmetryId);
                disjointIds.add(disjointId);
                uriToName.put(uri, name);
                uriToNormalId.put(uri, id);
                uriToDisjointnessId.put(uri, disjointId);
                uriToSymmetryId.put(uri, symmetryId);
            }
        }
        catch (SQLException e) {
            throw new MinerModuleException("Unable to initialize module", e);
        }
        finally {
            if (stmt != null) {
                try {
                    stmt.close();
                }
                catch (SQLException ignored) {
                }
            }
        }
    }

    @Override
    public String getModuleName() {
        return "Property Disjointness";
    }

    @Override
    public String getFileString() {
        return "propertydisjointness";
    }

    @Override
    public String getDescription() {
        return "Module for generation disjointObjectProperty axioms";
    }

    @Override
    public void setupSchema() throws MinerModuleException {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void acquireTerminology() throws MinerModuleException {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void generateTransactionTable(OutputStream output) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void generateAssociationRules(File inputFile, File outputFile) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public Set<OWLAxiom> readAssociationRules(File inputFile) throws MinerModuleException {
        return Collections.emptySet();
    }

    public void readAssociationRules(File inputFile, HashMap<OWLAxiom, ParsedAxiom.SupportConfidenceTuple> map)
            throws MinerModuleException {
        List<ParsedAxiom> axioms;

        try {
            axioms = config.getParser().parseSingleAntecendent(inputFile);
        }
        catch (IOException e) {
            throw new MinerModuleException("Unable to open input file", e);
        }

        // generated hashmap for counterpart detection
        HashMap<String, ParsedAxiom> axiomMap = new HashMap<String, ParsedAxiom>();
        for (ParsedAxiom ax : axioms) {
            // filter out axioms not representing disjointness (both negated or both non-negated)
            if ((isDisjointnessId(ax.getCons()) && isDisjointnessId(ax.getAnte1())) ||
                    (!isDisjointnessId(ax.getCons()) && !isDisjointnessId(ax.getAnte1()))) {
                continue;
            }
            axiomMap.put(String.format("%d-%d", ax.getCons(), ax.getAnte1()), ax);
        }

        // normalize all confidence values
        //TODO: apply later, maybe not to parsed axioms
        ValueNormalizer normalizer = ValueNormalizerFactory.getDefaultNormalizerInstance("Property Disjointness");
        normalizer.reportValues(axioms);
        normalizer.normalize(axioms);

        for (ParsedAxiom pa : axioms) {
            // criteria guarantee that we only treat each axioms and counterpart pair once!
            if (!satisfiesCriteria(pa)) {
                continue;
            }

            //TODO: support and confidence thresholds
//            if (pa.getSupp() < config.getSupportThreshold() || pa.getConf() < config.getConfidenceThreshold()) {
//                continue;
//            }

            // get counterpart for axiom (because of symmetry), determine combined confidence
            ParsedAxiom counterpart = getCounterpartAxiom(axiomMap, pa);

            if (counterpart == null) {
                System.out.format("No counterpart for: %s <- %s\n", pa.getCons(), pa.getAnte1());
                continue;
            }

            double combinedConfidence = combineConfidenceValues(pa.getConf(), counterpart.getConf());

            map.put(getDisjointnessAxiom(pa.getAnte1(), pa.getCons(), pa.getSupp(), combinedConfidence),
                    pa.getSuppConfTuple());
        }
    }

    /**
     * Checks if the given parsed axiom satisfies the criteria posed for property disjointness axioms, i.e., if the
     * consequence is the negative part and the antecedent is the positive one.
     *
     * @param axiom parsed axiom to check for satisfaction of criteria
     * @return true if criteria are satisfied, otherwise false
     */
    private boolean satisfiesCriteria(ParsedAxiom axiom) {
        return !isDisjointnessId(axiom.getAnte1()) && isDisjointnessId(axiom.getCons());
    }

    /**
     * Returns the id of the negation of the property represented by the given id.
     *
     * @param id id to get negation for
     * @return id of negation of property represented by given id
     */
    private int getNegatedId(int id) {
        if (isDisjointnessId(id)) {
            // id represents negated property, thus return non-negated one
            return uriToNormalId.get(propertyIdToUri.get(id));
        }

        // id represents non-negated property, negate it
        return uriToDisjointnessId.get(propertyIdToUri.get(id));
    }

    /**
     * Combines the given confidence values into one single value. This is used to respect the symmetry of property
     * disjointness.
     *
     * @param conf1 first confidence value
     * @param conf2 second confidence value
     * @return combined confidence value
     */
    private double combineConfidenceValues(double conf1, double conf2) {
        return Math.min(conf1, conf2);
    }

    /**
     * Returns the parsed axiom representing the counterpart for the given one. If there is no counterpart contained in
     * the parsed axioms, null is returned.
     *
     * @param axiom axiom to get counterpart for
     * @return parsed axiom for counterpart of given axiom, null if no counterpart found
     */
    private ParsedAxiom getCounterpartAxiom(HashMap<String, ParsedAxiom> allAxioms, ParsedAxiom axiom) {
        int counterPartConsequence = getNegatedId(axiom.getAnte1());
        int counterPartAntecedent = getNegatedId(axiom.getCons());

        return allAxioms.get(String.format("%d-%d", counterPartConsequence, counterPartAntecedent));
    }

    /**
     * Returns the property disjointness axiom for antecedent and consequence identified by the given IDs. The support
     * and confidence values are added as annotations.
     *
     * @param antecedent  ID of antecedent
     * @param consequence ID of consequence
     * @param support     support value for this axiom
     * @param confidence  confidence value for this axiom
     * @return disjointObjectProperty axiom for given antecedent and consequence
     */
    private OWLAxiom getDisjointnessAxiom(int antecedent, int consequence, double support, double confidence) {
        OWLDataFactory dFactory = config.getFactory();
        OWLAnnotationProperty supportAnnotationProperty = dFactory
                .getOWLAnnotationProperty(config.getSupportAnnotationUri());
        OWLAnnotation supportAnnotation = dFactory
                .getOWLAnnotation(supportAnnotationProperty, dFactory.getOWLLiteral(support));

        OWLAnnotationProperty confidenceAnnotationProperty = dFactory
                .getOWLAnnotationProperty(config.getConfidenceAnnotationUri());
        OWLAnnotation confidenceAnnotation = dFactory.getOWLAnnotation(supportAnnotationProperty,
                                                                       dFactory.getOWLLiteral(
                                                                               confidence));
        Set<OWLAnnotation> annotations = new HashSet<OWLAnnotation>();
        Set<OWLObjectProperty> properties = new HashSet<OWLObjectProperty>();
        properties.add(dFactory.getOWLObjectProperty(IRI.create(propertyIdToUri.get(antecedent))));
        properties.add(dFactory.getOWLObjectProperty(IRI.create(propertyIdToUri.get(consequence))));
        if (config.getWriteAnnotations()) {
            annotations.add(supportAnnotation);
            annotations.add(confidenceAnnotation);
        }

        return dFactory.getOWLDisjointObjectPropertiesAxiom(properties, annotations);
    }

    /**
     * Returns the URL for the property having the given ID in the database.
     *
     * @param id id to get URL for
     */
    private String getURLForId(int id) {
        return propertyIdToUri.get(id);
    }

    /**
     * Returns true if the given id is a disjointness ID
     *
     * @param id id to check whether it is a disjointness ID
     * @return true if the id is a disjointness ID otherwise false
     */
    private boolean isDisjointnessId(int id) {
        return disjointIds.contains(id);
    }

    /**
     * Returns true if the given id is a symmetry ID
     *
     * @param id id to check whether it is a symmetry ID
     * @return true if the id is a symmetry ID otherwise false
     */
    private boolean isSymmetryId(int id) {
        return symmetryIds.contains(id);
    }

    /**
     * Returns true if the given ID is a normal (not symmetry and not disjointness) id
     *
     * @param id ID to check
     * @return true if neither symmetry nor disjointness ID
     */
    private boolean isNormalId(int id) {
        return !isDisjointnessId(id) && !isSymmetryId(id);
    }
}
