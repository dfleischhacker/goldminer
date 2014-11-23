package de.uni_mannheim.informatik.dws.goldminer;

import de.uni_mannheim.informatik.dws.goldminer.database.*;
import de.uni_mannheim.informatik.dws.goldminer.main.*;
import de.uni_mannheim.informatik.dws.goldminer.modules.MinerModuleConfiguration;
import de.uni_mannheim.informatik.dws.goldminer.modules.MinerModuleException;
import de.uni_mannheim.informatik.dws.goldminer.modules.PropertyDisjointnessModule;
import de.uni_mannheim.informatik.dws.goldminer.modules.axiomgenerator.AxiomGenerator;
import de.uni_mannheim.informatik.dws.goldminer.ontology.*;
import de.uni_mannheim.informatik.dws.goldminer.sparql.Filter;
import de.uni_mannheim.informatik.dws.goldminer.util.*;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.sql.SQLException;
import java.util.*;

/**
 * Main class of GoldMiner. Provides the entry points into all functionality and manages connections to
 * database and SPARQL endpoint via its helper classes
 *
 * @author Johanna Völker
 * @author Jakob Frankenbach
 * @author Daniel Fleischhacker (daniel@informatik.uni-mannheim.de)
 */
 /* TODO: This class needs to be refactored urgently. It contains to much functionality and all its activation
 * logic.
 */
public class GoldMiner {
    public static Logger log = LoggerFactory.getLogger(GoldMiner.class);
    private AssociationRulesParser parser;
    private OntologyWriter writer;
    private Ontology ontology;
    private CheckpointUtil chk;
    private RandomAxiomChooser rac = new RandomAxiomChooser();
    private SQLDatabase sqlDatabase;
    private Setup setup;
    private TerminologyExtractor terminologyExtractor;
    private IndividualsExtractor individualsExtractor;
    private TablePrinter tablePrinter;
    private Set<AxiomType> activeAxiomTypes;
    private RequirementsResolver requirementsResolver;
    private AxiomGenerator axiomGenerator;

    private boolean writeAnnotations = Settings.getBoolean("write_annotations");

    /**
     * Initialize GoldMiner to write the created ontology to the file specified in the configuration file.
     *
     * @throws IOException
     * @throws SQLException
     * @throws OWLOntologyCreationException
     * @throws OWLOntologyStorageException
     */
    public GoldMiner() throws IOException, SQLException, OWLOntologyCreationException,
            OWLOntologyStorageException {
        this(Settings.getString("ontology"));
    }

    /**
     * Initialize GoldMiner to write created ontology to the given <code>ontologyFile</code>.
     *
     * @param ontologyFile name of file to write generated ontology to
     * @throws IOException
     * @throws SQLException
     * @throws OWLOntologyCreationException
     * @throws OWLOntologyStorageException
     */
    public GoldMiner(String ontologyFile)
            throws IOException, SQLException, OWLOntologyCreationException,
            OWLOntologyStorageException {
        if (!Settings.loaded()) {
            Settings.load();
        }
        this.activeAxiomTypes = getSelectedAxiomTypes();
        this.sqlDatabase = SQLDatabase.instance();
        this.setup = new Setup();
        this.tablePrinter = new TablePrinter();
        this.terminologyExtractor = new TerminologyExtractor();
        this.individualsExtractor = new IndividualsExtractor();
        this.parser = new AssociationRulesParser();
//        this.axiomGenerator = new AxiomGenerator();
        this.ontology = new Ontology();
        this.ontology.create(new File(ontologyFile));
        this.ontology.save();
        this.chk = new CheckpointUtil(Settings.getString("transaction_tables") + "/checkpoints");
        this.requirementsResolver = new RequirementsResolver(activeAxiomTypes);
    }

    public boolean disconnect() {
        try {
            this.sqlDatabase.close();
            return true;
        }
        catch (SQLException e) {
            return false;
        }
    }

    public boolean setupDatabase() throws SQLException {
        return chk.performCheckpointedOperation("setupdatabase", new CheckpointUtil.CheckpointedOperation() {
            @Override
            public boolean run() {
                return setup.setupSchema();
            }
        });
    }

    public boolean terminologyAcquisition() throws SQLException {
        if (requirementsResolver.isDatabaseTableRequired(DatabaseTable.CLASSES_TABLE)) {
            chk.performCheckpointedOperation("initclassestable", new CheckpointUtil.CheckpointedOperation() {
                @Override
                public boolean run() {
                    terminologyExtractor.initClassesTable();
                    return true;
                }
            });
        }

        if (requirementsResolver.isDatabaseTableRequired(DatabaseTable.INDIVIDUALS_TABLE)) {
            chk.performCheckpointedOperation("initindividualstable", new CheckpointUtil.CheckpointedOperation() {
                @Override
                public boolean run() {
                    try {
                        individualsExtractor.initIndividualsTable();
                    }
                    catch (SQLException e) {
                        return false;
                    }
                    return true;
                }
            });
        }

        if (requirementsResolver.isDatabaseTableRequired(DatabaseTable.INDIVIDUAL_PAIRS_TABLE)) {
            chk.performCheckpointedOperation("initindividualpairstable", new CheckpointUtil.CheckpointedOperation() {
                @Override
                public boolean run() {
                    try {
                        individualsExtractor.initIndividualPairsTable();
                    }
                    catch (SQLException e) {
                        return false;
                    }
                    return true;
                }
            });
        }

        if (requirementsResolver.isDatabaseTableRequired(DatabaseTable.PROPERTIES_TABLE)) {
            chk.performCheckpointedOperation("initpropertiestable",
                    new CheckpointUtil.CheckpointedOperation() {
                        @Override
                        public boolean run() {
                            terminologyExtractor.initPropertiesTable();
                            return true;
                        }
                    });
        }

        if (requirementsResolver.isDatabaseTableRequired(DatabaseTable.CLASSES_EXISTS_PROPERTY_TABLE)) {
            chk.performCheckpointedOperation("initclassesexistspropertytable",
                    new CheckpointUtil.CheckpointedOperation() {
                        @Override
                        public boolean run() {
                            try {
                                terminologyExtractor.initClassesExistsPropertyTable();
                            }
                            catch (SQLException e) {
                                return false;
                            }
                            return true;
                        }
                    });
        }

        if (requirementsResolver.isDatabaseTableRequired(DatabaseTable.PROPERTY_TOP_TABLE)) {
            chk.performCheckpointedOperation("initpropertytoptable", new CheckpointUtil.CheckpointedOperation() {
                @Override
                public boolean run() {
                    try {
                        terminologyExtractor.initPropertyTopTable();
                    }
                    catch (SQLException e) {
                        return false;
                    }
                    return true;
                }
            });
        }

        if (requirementsResolver.isDatabaseTableRequired(DatabaseTable.PROPERTY_CHAINS_TABLE)) {
            chk.performCheckpointedOperation("initpropertychainstable", new CheckpointUtil.CheckpointedOperation() {
                @Override
                public boolean run() {
                    try {
                        terminologyExtractor.initPropertyChainsTable();
                    }
                    catch (SQLException e) {
                        return false;
                    }
                    return true;
                }
            });
        }

        if (requirementsResolver.isDatabaseTableRequired(DatabaseTable.PROPERTY_CHAINS_TRANS_TABLE)) {
            chk.performCheckpointedOperation("initpropertychainstranstable", new CheckpointUtil.CheckpointedOperation() {
                @Override
                public boolean run() {
                    try {
                        terminologyExtractor.initPropertyChainsTransTable();
                    }
                    catch (SQLException e) {
                        return false;
                    }
                    return true;
                }
            });
        }

        if (requirementsResolver.isDatabaseTableRequired(DatabaseTable.INDIVIDUAL_PAIRS_TRANS_TABLE)) {
            chk.performCheckpointedOperation("initindividualpairstransstable", new CheckpointUtil.CheckpointedOperation() {
                @Override
                public boolean run() {
                    try {
                        individualsExtractor.initIndividualPairsTransTable();
                    }
                    catch (SQLException e) {
                        return false;
                    }
                    return true;
                }
            });
        }

        //TODO: we do not actually create indexes in this method yet
        this.terminologyExtractor.createIndexes();
        return true;
    }

    public boolean connect(String url, String user, String password) {
        try {
            this.sqlDatabase = SQLDatabase.instance(url, user, password);
            return true;
        }
        catch (SQLException e) {
            return false;
        }
    }

    /**
     * Returns the set of all axiom types activated in the configuration.
     *
     * @return set of activated axiom types
     */
    public Set<AxiomType> getSelectedAxiomTypes() {
        HashSet<AxiomType> activeTypes = new HashSet<AxiomType>();
        if (Settings.isAxiomActivated("c_sub_c")) {
            activeTypes.add(AxiomType.CLASS_SUBSUMPTION_SIMPLE);
        }

        if (Settings.isAxiomActivated("c_and_c_sub_c")) {
            activeTypes.add(AxiomType.CLASS_SUBSUMPTION_COMPLEX);
        }

        if (Settings.isAxiomActivated("c_sub_exists_p_c")) {
            activeTypes.add(AxiomType.PROPERTY_REQUIRED_FOR_CLASS);
        }

        if (Settings.isAxiomActivated("exists_p_c_sub_c")) {
            activeTypes.add(AxiomType.PROPERTY_DOMAIN_FOR_RANGE);
        }

        if (Settings.isAxiomActivated("exists_p_T_sub_c")) {
            activeTypes.add(AxiomType.PROPERTY_DOMAIN);
        }

        if (Settings.isAxiomActivated("exists_pi_T_sub_c")) {
            activeTypes.add(AxiomType.PROPERTY_RANGE);
        }

        if (Settings.isAxiomActivated("p_sub_p")) {
            activeTypes.add(AxiomType.PROPERTY_SUBSUMPTION);
        }

        if (Settings.isAxiomActivated("p_chain_q_sub_r")) {
            activeTypes.add(AxiomType.PROPERTY_CHAINS);
        }

        if (Settings.isAxiomActivated("c_dis_c")) {
            activeTypes.add(AxiomType.CLASS_DISJOINTNESS);
        }

        if (Settings.isAxiomActivated("p_dis_p")) {
            activeTypes.add(AxiomType.PROPERTY_DISJOINTNESS);
        }

        if (Settings.isAxiomActivated("p_reflexive")) {
            activeTypes.add(AxiomType.REFLEXIVE_PROPERTY);
        }

        if (Settings.isAxiomActivated("p_irreflexive")) {
            activeTypes.add(AxiomType.IRREFLEXIVE_PROPERTY);
        }

        if (Settings.isAxiomActivated("p_inverse_q")) {
            activeTypes.add(AxiomType.INVERSE_PROPERTY);
        }

        if (Settings.isAxiomActivated("p_inverse_q")) {
            activeTypes.add(AxiomType.INVERSE_PROPERTY);
        }

        if (Settings.isAxiomActivated("p_inverse_q")) {
            activeTypes.add(AxiomType.INVERSE_PROPERTY);
        }

        if (Settings.isAxiomActivated("p_asymmetric")) {
            activeTypes.add(AxiomType.PROPERTY_ASYMMETRY);
        }

        if (Settings.isAxiomActivated("p_functional")) {
            activeTypes.add(AxiomType.FUNCTIONAL_PROPERTY);
        }

        if (Settings.isAxiomActivated("p_inverse_functional")) {
            activeTypes.add(AxiomType.INVERSE_FUNCTIONAL_PROPERTY);
        }

        for (AxiomType v : activeTypes) {
            System.out.println("Enabled: " + v);
        }

        return activeTypes;
    }

    public boolean sparqlSetup(String endpoint, Filter filter, String graph,
                               int chunk) {
        if (!chk.reached("terminologyextract")) {
            this.terminologyExtractor = new TerminologyExtractor(this.sqlDatabase, endpoint, graph, chunk, filter);
            chk.reach("terminologyextract");
        }
        if (!chk.reached("individualextract")) {
            this.individualsExtractor = new IndividualsExtractor(this.sqlDatabase, endpoint, graph, chunk, filter);
            chk.reach("individualextract");
        }
        return false;
    }

    public void createTransactionTables() throws IOException,
            SQLException {
        if (requirementsResolver.isTransactionTableRequired(TransactionTable.CLASS_MEMBERS)) {
            chk.performCheckpointedOperation("classmembers", new CheckpointUtil.CheckpointedOperation() {
                @Override
                public boolean run() {
                    deleteFile(TransactionTable.CLASS_MEMBERS);
                    try {
                        tablePrinter.printClassMembers(TransactionTable.CLASS_MEMBERS.getAbsoluteFileName());
                    }
                    catch (SQLException e) {
                        log.error("Error creating class members transaction table", e);
                        return false;
                    }
                    catch (IOException e) {
                        log.error("Error creating class members transaction table", e);
                        return false;
                    }
                    return true;
                }
            });
        }

        if (requirementsResolver.isTransactionTableRequired(TransactionTable.EXISTS_PROPERTY_MEMBERS)) {
            chk.performCheckpointedOperation("existspropertymembers", new CheckpointUtil.CheckpointedOperation() {
                @Override
                public boolean run() {
                    deleteFile(TransactionTable.EXISTS_PROPERTY_MEMBERS);
                    try {
                        tablePrinter.printExistsPropertyMembers(
                                TransactionTable.EXISTS_PROPERTY_MEMBERS.getAbsoluteFileName(),
                                0);
                    }
                    catch (SQLException e) {
                        log.error("Error creating exists property members transaction table", e);
                        return false;
                    }
                    catch (IOException e) {
                        log.error("Error creating exists property members transaction table", e);
                        return false;
                    }
                    return true;
                }
            });
        }

        if (requirementsResolver.isTransactionTableRequired(TransactionTable.PROPERTY_RESTRICTIONS1)) {
            chk.performCheckpointedOperation("propertyrestrictions1", new CheckpointUtil.CheckpointedOperation() {
                @Override
                public boolean run() {
                    deleteFile(TransactionTable.PROPERTY_RESTRICTIONS1);
                    try {
                        tablePrinter.printPropertyRestrictions(TransactionTable.PROPERTY_RESTRICTIONS1.getAbsoluteFileName(),
                                0);
                    }
                    catch (SQLException e) {
                        log.error("Error creating property restrictions 1 transaction table", e);
                        return false;
                    }
                    catch (IOException e) {
                        log.error("Error creating property restrictions 1 transaction table", e);
                        return false;
                    }
                    return true;
                }
            });
        }

        if (requirementsResolver.isTransactionTableRequired(TransactionTable.PROPERTY_RESTRICTIONS2)) {
            chk.performCheckpointedOperation("propertyrestrictions2", new CheckpointUtil.CheckpointedOperation() {
                @Override
                public boolean run() {
                    deleteFile(TransactionTable.PROPERTY_RESTRICTIONS2);
                    try {
                        tablePrinter.printPropertyRestrictions(TransactionTable.PROPERTY_RESTRICTIONS2.getAbsoluteFileName(),
                                1);
                    }
                    catch (SQLException e) {
                        log.error("Error creating property restrictions 2 transaction table", e);
                        return false;
                    }
                    catch (IOException e) {
                        log.error("Error creating property restrictions 2 transaction table", e);
                        return false;
                    }
                    return true;
                }
            });
        }

        if (requirementsResolver.isTransactionTableRequired(TransactionTable.PROPERTY_MEMBERS)) {
            chk.performCheckpointedOperation("propertymembers", new CheckpointUtil.CheckpointedOperation() {
                @Override
                public boolean run() {
                    deleteFile(TransactionTable.PROPERTY_MEMBERS);
                    try {
                        tablePrinter.printExistsPropertyMembers(TransactionTable.PROPERTY_MEMBERS.getAbsoluteFileName(),
                                0);
                    }
                    catch (SQLException e) {
                        log.error("Error creating property restrictions 1 transaction table", e);
                        return false;
                    }
                    catch (IOException e) {
                        log.error("Error creating property restrictions 1 transaction table", e);
                        return false;
                    }
                    return true;
                }
            });
        }

        if (requirementsResolver.isTransactionTableRequired(TransactionTable.PROPERTY_CHAIN_MEMBERS)) {
            chk.performCheckpointedOperation("propertychainmembers", new CheckpointUtil.CheckpointedOperation() {
                @Override
                public boolean run() {
                    deleteFile(TransactionTable.PROPERTY_CHAIN_MEMBERS);
                    try {
                        tablePrinter.printPropertyChainMembersTrans_new(
                                TransactionTable.PROPERTY_CHAIN_MEMBERS.getAbsoluteFileName());
                    }
                    catch (SQLException e) {
                        log.error("Error creating property restrictions 1 transaction table", e);
                        return false;
                    }
                    catch (IOException e) {
                        log.error("Error creating property restrictions 1 transaction table", e);
                        return false;
                    }
                    return true;
                }
            });
        }

        if (requirementsResolver.isTransactionTableRequired(TransactionTable.PROPERTY_REFLEXIVITY)) {
            chk.performCheckpointedOperation("propertyreflexivity", new CheckpointUtil.CheckpointedOperation() {
                @Override
                public boolean run() {
                    deleteFile(TransactionTable.PROPERTY_REFLEXIVITY);
                    try {
                        tablePrinter.printPropertyReflexivity(TransactionTable.PROPERTY_REFLEXIVITY.getAbsoluteFileName());
                    }
                    catch (SQLException e) {
                        log.error("Error creating property restrictions 1 transaction table", e);
                        return false;
                    }
                    catch (IOException e) {
                        log.error("Error creating property restrictions 1 transaction table", e);
                        return false;
                    }
                    return true;
                }
            });
        }

        if (requirementsResolver.isTransactionTableRequired(TransactionTable.PROPERTY_REFLEXIVITY)) {
            chk.performCheckpointedOperation("propertyinversemembers", new CheckpointUtil.CheckpointedOperation() {
                @Override
                public boolean run() {
                    deleteFile(TransactionTable.PROPERTY_REFLEXIVITY);
                    try {
                        tablePrinter.printPropertyInverseMembers(TransactionTable.PROPERTY_INVERSE_MEMBERS.getAbsoluteFileName());
                    }
                    catch (SQLException e) {
                        log.error("Error creating property restrictions 1 transaction table", e);
                        return false;
                    }
                    catch (IOException e) {
                        log.error("Error creating property restrictions 1 transaction table", e);
                        return false;
                    }
                    return true;
                }
            });
        }

        if (requirementsResolver.isTransactionTableRequired(TransactionTable.PROPERTY_FUNCTIONAL_MEMBERS)) {
            chk.performCheckpointedOperation("propertyfunctionalmembers", new CheckpointUtil.CheckpointedOperation() {
                @Override
                public boolean run() {
                    deleteFile(TransactionTable.PROPERTY_FUNCTIONAL_MEMBERS);
                    try {
                        tablePrinter.printPropertyInverseMembers(TransactionTable.PROPERTY_FUNCTIONAL_MEMBERS.getAbsoluteFileName());
                    }
                    catch (SQLException e) {
                        log.error("Error creating property restrictions 1 transaction table", e);
                        return false;
                    }
                    catch (IOException e) {
                        log.error("Error creating property restrictions 1 transaction table", e);
                        return false;
                    }
                    return true;
                }
            });
        }

        if (requirementsResolver.isTransactionTableRequired(TransactionTable.PROPERTY_INVERSE_FUNCTIONAL)) {
            chk.performCheckpointedOperation("propertyinversefunctional", new CheckpointUtil.CheckpointedOperation() {
                @Override
                public boolean run() {
                    deleteFile(TransactionTable.PROPERTY_INVERSE_FUNCTIONAL);
                    try {
                        tablePrinter.printPropertyInverseFunctionalMembers(TransactionTable.PROPERTY_INVERSE_FUNCTIONAL.getAbsoluteFileName());
                    }
                    catch (SQLException e) {
                        log.error("Error creating property restrictions 1 transaction table", e);
                        return false;
                    }
                    catch (IOException e) {
                        log.error("Error creating property restrictions 1 transaction table", e);
                        return false;
                    }
                    return true;
                }
            });
        }

        if (requirementsResolver.isTransactionTableRequired(TransactionTable.CLASS_DISJOINTNESS)) {
            chk.performCheckpointedOperation("classdisjointness", new CheckpointUtil.CheckpointedOperation() {
                @Override
                public boolean run() {
                    deleteFile(TransactionTable.CLASS_DISJOINTNESS);
                    try {
                        tablePrinter.printDisjointClassMembers(TransactionTable.CLASS_DISJOINTNESS.getAbsoluteFileName());
                    }
                    catch (SQLException e) {
                        log.error("Error creating property restrictions 1 transaction table", e);
                        return false;
                    }
                    catch (IOException e) {
                        log.error("Error creating property restrictions 1 transaction table", e);
                        return false;
                    }
                    return true;
                }
            });
        }
    }

    /**
     * Deletes the transaction table file for the given table. Errors and exceptions are silently ignored!
     * @param table table whose file representation should be deleted
     */
    private void deleteFile(TransactionTable table) {
        File f = new File(table.getAbsoluteFileName());
        f.delete();
        try {
            f.createNewFile();
        }
        catch (IOException e) {
            log.warn("Unable to create transaction table file '{}'", f.getAbsolutePath());
        }
    }

    /**
     * Starts the external mining process on all relevant transaction table files.
     *
     * @throws IOException
     */
    public void mineAssociationRules() throws IOException {
        TransactionTable[] relevantTransactionTables = this.getRelevantExistingTransactionTables();
        File ruleFile = new File(Settings.getString("association_rules"));
        if (!ruleFile.exists()) {
            ruleFile.mkdirs();
        }
        this.deleteAssociationRuleFiles();
        for (TransactionTable table : relevantTransactionTables) {
            ProcessBuilder p = new ProcessBuilder(Settings.getString("apriori"),
                    "-tr", "-s-1", "-c0", "-m2", "-n2", "-v (%20s, %30c)",
                    table.getAbsoluteFileName(),
                    table.getAbsoluteAssociationRuleFileName()
            );
            p.redirectOutput(ProcessBuilder.Redirect.PIPE);
            Process process = p.start();
            String line;
            BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream()));
            while ((line = input.readLine()) != null) {
                log.debug(line);
            }
            input.close();
        }
    }

    /**
     * Returns an array of all relevant existing transaction table files in the transaction
     * table directory.
     *
     * @return all transaction table files contained the the transaction tables directory and relevant for the
     * current run configuration
     */
    private TransactionTable[] getRelevantExistingTransactionTables() {
        File file = new File(Settings.getString("transaction_tables"));

        HashMap<String, TransactionTable> relevantTables = new HashMap<String, TransactionTable>();
        HashSet<TransactionTable> relevantExistingTables = new HashSet<TransactionTable>();
        for (TransactionTable table : requirementsResolver.getRequiredTransactionTables()) {
            relevantTables.put(table.getFileName(), table);
        }

        for (File tableFile : file.listFiles(new FileExtensionFilter(".txt"))) {
            if (relevantTables.containsKey(tableFile.getName())) {
                relevantExistingTables.add(relevantTables.get(tableFile.getName()));
            }
        }

        return relevantExistingTables.toArray(new TransactionTable[relevantExistingTables.size()]);
    }

    /**
     * Deletes all association rule files which are generated by the current run configuration.
     * This method is used to ensure that no old files interfere with the overall process.
     * Files which would not be generated by the current run configuration are left untouched.
     */
    private void deleteAssociationRuleFiles() {
        File ruleFileDirectory = new File(Settings.getString("association_rules"));

        HashSet<String> relevantRuleFileNames = new HashSet<String>();

        for (TransactionTable table : requirementsResolver.getRequiredTransactionTables()) {
            relevantRuleFileNames.add(table.getAssociationRuleFileName());
        }

        for (File ruleFile : ruleFileDirectory.listFiles(new FileExtensionFilter(".txt"))) {
            if (relevantRuleFileNames.contains(ruleFile.getName())) {
                if (!ruleFile.delete()) {
                    log.warn("Unable to delete existing rule file: {}", ruleFile.getAbsolutePath());
                }
            }
        }
    }

    public void mineAssociationRules(AssociationRulesMiner miner) {
        miner.execute();
    }

    public List<String> getAssociationRules() throws IOException {
        List<String> rules = new ArrayList<String>();
        File associationRuleDirectory = new File(Settings.getString("association_rules"));
        File[] associationRuleFiles = associationRuleDirectory.listFiles(new FileExtensionFilter(".txt"));
        for (File associationRuleFile : associationRuleFiles) {
            BufferedReader in = new BufferedReader(new FileReader(associationRuleFile));
            String line;
            StringBuilder fileText = new StringBuilder();
            while ((line = in.readLine()) != null) {
                fileText.append(line);
            }
            rules.add(fileText.toString());
            in.close();
        }
        return rules;
    }

    public HashMap<OWLAxiom, SupportConfidenceTuple> parseAssociationRules()
            throws IOException, SQLException {
        this.writer = new OntologyWriter(this.sqlDatabase, this.ontology, writeAnnotations);

        // inititalize module config
        MinerModuleConfiguration moduleConfig = new MinerModuleConfiguration();
        moduleConfig.setWriteAnnotations(writeAnnotations);
        moduleConfig.setDataFactory(ontology.getOntology().getOWLOntologyManager().getOWLDataFactory());
        moduleConfig.setConfidenceAnnotationUri(IRI.create(Settings.getString("annotation_iri") + "#confidence"));
        moduleConfig.setSupportAnnotationUri(IRI.create(Settings.getString("annotation_iri") + "#support"));
        moduleConfig.setDbConnection(sqlDatabase.getConnection());
        moduleConfig.setParser(parser);

        HashMap<OWLAxiom, SupportConfidenceTuple> hmAxioms =
                new HashMap<OWLAxiom, SupportConfidenceTuple>();

//        for (AxiomType axiomType : activeAxiomTypes) {
//            TransactionTable mainTransactionTable = requirementsResolver.getRequiredTransactionTable(axiomType);
//
//            File ruleFile = new File(mainTransactionTable.getAbsoluteAssociationRuleFileName());
//            if (!ruleFile.exists()) {
//                log.warn("Unable to read: {}! Skipping...", ruleFile.getAbsolutePath());
//                continue;
//            }
//
//            List<ParsedAxiom> axioms = this.parser.parse(ruleFile, axiomType.hasSecondAntecedent());
//
//            ValueNormalizer normalizer = ValueNormalizerFactory.getDefaultNormalizerInstance(axiomType.toString());
//            normalizer.reportValues(axioms);
//            normalizer.normalize(axioms);
//
//            //axiomType.getOWLAxioms(axioms, hmAxioms);
//            for (ParsedAxiom pa : axioms) {
//                OWLAxiom a =
//                        this.writer.get_c_sub_c_Axioms(pa.getCons(), pa.getAnte1(), pa.getSupp(), pa.getConf());
//                if (a != null) {
//                    if (pa.getSupp() != 0.0) {
//                        rac.add(a, pa.getConf());
//                    }
//                    hmAxioms.put(a, pa.getSuppConfTuple());
//                }
//            }
//        }

        /* Concept Subsumption: c sub c */
        File f = new File(requirementsResolver.getRequiredTransactionTable(AxiomType.CLASS_SUBSUMPTION_SIMPLE)
                .getAbsoluteAssociationRuleFileName());
        if (!f.exists()) {
            log.warn("Unable to read: {}! Skipping...", f.getAbsolutePath());
        }
        else if (!activeAxiomTypes.contains(AxiomType.CLASS_SUBSUMPTION_SIMPLE)) {
            log.info("Skipped simple class subsumption because not activated");
        }
        else {
            List<ParsedAxiom> axioms = this.parser.parse(f, false);

            ValueNormalizer normalizer = ValueNormalizerFactory.getDefaultNormalizerInstance("C sub C");
            normalizer.reportValues(axioms);
            normalizer.normalize(axioms);

            for (ParsedAxiom pa : axioms) {
                OWLAxiom a =
                        this.writer.get_c_sub_c_Axioms(pa.getCons(), pa.getAnte1(), pa.getSupp(), pa.getConf());
                if (a != null) {
                    if (pa.getSupp() != 0.0) {
                        rac.add(a, pa.getConf());
                    }
                    hmAxioms.put(a, pa.getSuppConfTuple());
                }
            }
        }
        log.debug("Number of Axioms: {}", hmAxioms.size());

        /* Concept Subsumption: c and c sub c */
        log.debug("Subsumption");
        f = new File(requirementsResolver.getRequiredTransactionTable(AxiomType.CLASS_SUBSUMPTION_COMPLEX).getAbsoluteAssociationRuleFileName());
        if (!f.exists()) {
            log.warn("Unable to read: '{}'! Skipping...", f.getAbsolutePath());
        }
        else if (!activeAxiomTypes.contains(AxiomType.CLASS_SUBSUMPTION_COMPLEX)) {
            log.info("Skipped complex class subsumption because not activated");
        }
        else {
            List<ParsedAxiom> axioms = this.parser.parse(f, true);

            ValueNormalizer normalizer = ValueNormalizerFactory.getDefaultNormalizerInstance("C and C sub C");
            normalizer.reportValues(axioms);
            normalizer.normalize(axioms);

            for (ParsedAxiom pa : axioms) {
                OWLAxiom a = this.writer
                        .get_c_and_c_sub_c_Axioms(pa.getAnte1(), pa.getAnte2(), pa.getCons(), pa.getSupp(),
                                pa.getConf());
                if (a != null) {
                    if (pa.getSupp() != 0.0) {
                        rac.add(a, pa.getConf());
                    }
                    hmAxioms.put(a, pa.getSuppConfTuple());
                }
            }
        }
        log.debug("Number of Axioms: {}", hmAxioms.size());

        /* c sub exists p.c */
        log.debug("c sub exists p c");
        f = new File(requirementsResolver.getRequiredTransactionTable(AxiomType.PROPERTY_REQUIRED_FOR_CLASS)
                .getAbsoluteAssociationRuleFileName());
        if (!f.exists()) {
            log.warn("Unable to read: '{}'! Skipping...", f.getAbsolutePath());
        }
        else if (!activeAxiomTypes.contains(AxiomType.PROPERTY_REQUIRED_FOR_CLASS)) {
            log.info("Skipped property required for class because not activated");
        }
        else {
            List<ParsedAxiom> axioms = this.parser.parse(f, false);

            ValueNormalizer normalizer = ValueNormalizerFactory.getDefaultNormalizerInstance("C sub exists P.C");
            normalizer.reportValues(axioms);
            normalizer.normalize(axioms);

            for (ParsedAxiom pa : axioms) {
                OWLAxiom a =
                        this.writer
                                .get_c_sub_exists_p_c_Axioms(pa.getAnte1(), pa.getCons(), pa.getSupp(), pa.getConf());
                if (a != null) {
                    if (pa.getSupp() != 0.0) {
                        rac.add(a, pa.getConf());
                    }
                    hmAxioms.put(a, pa.getSuppConfTuple());
                }
            }
        }
        log.debug("Number of Axioms: {}", hmAxioms.size());

        /* exists p.c sub c */
        log.debug("exists_p_c_sub_c");
        f = new File(requirementsResolver.getRequiredTransactionTable(AxiomType.PROPERTY_DOMAIN_FOR_RANGE)
                .getAbsoluteAssociationRuleFileName());
        if (!f.exists()) {
            log.warn("Unable to read: '{}'! Skipping...", f.getAbsolutePath());
        }
        else if (!activeAxiomTypes.contains(AxiomType.PROPERTY_DOMAIN_FOR_RANGE)) {
            log.info("Skipped property domain for range for class because not activated");
        }
        else {
            List<ParsedAxiom> axioms = this.parser.parse(f, false);

            ValueNormalizer normalizer = ValueNormalizerFactory.getDefaultNormalizerInstance("exists P.C sub C");
            normalizer.reportValues(axioms);
            normalizer.normalize(axioms);

            for (ParsedAxiom pa : axioms) {
                OWLAxiom a =
                        this.writer
                                .get_exists_p_c_sub_c_Axioms(pa.getAnte1(), pa.getCons(), pa.getSupp(), pa.getConf());
                if (a != null) {
                    if (pa.getSupp() != 0.0) {
                        rac.add(a, pa.getConf());
                    }
                    hmAxioms.put(a, pa.getSuppConfTuple());
                }
            }
        }
        log.debug("Number of Axioms: {}", hmAxioms.size());

        /* Object Property Domain: exists p.T sub c */
        log.debug("Object Property Domain: exists_p_T_sub_c");
        f = new File(requirementsResolver.getRequiredTransactionTable(AxiomType.PROPERTY_DOMAIN)
                .getAbsoluteAssociationRuleFileName());
        if (!f.exists()) {
            log.warn("Unable to read: '{}'! Skipping...", f.getAbsolutePath());
        }
        else if (!activeAxiomTypes.contains(AxiomType.PROPERTY_DOMAIN)) {
            log.info("Skipped property domain because not activated");
        }
        else {
            List<ParsedAxiom> axioms = this.parser.parse(f, false);

            ValueNormalizer normalizer = ValueNormalizerFactory.getDefaultNormalizerInstance("exists P.T sub C");
            normalizer.reportValues(axioms);
            normalizer.normalize(axioms);

            for (ParsedAxiom pa : axioms) {
                OWLAxiom a =
                        this.writer
                                .get_exists_p_T_sub_c_Axioms(pa.getAnte1(), pa.getCons(), pa.getSupp(), pa.getConf());
                if (a != null) {
                    if (pa.getSupp() != 0.0) {
                        rac.add(a, pa.getConf());
                    }
                    hmAxioms.put(a, pa.getSuppConfTuple());
                }
            }
        }
        log.debug("Number of Axioms: {}", hmAxioms.size());

        /* Object Property Range: exists p^i.T sub c */
        log.debug("Object Property Range: exists_pi_T_sub_c");
        f = new File(requirementsResolver.getRequiredTransactionTable(AxiomType.PROPERTY_RANGE)
                .getAbsoluteAssociationRuleFileName());
        if (!f.exists()) {
            log.warn("Unable to read: '{}'! Skipping...", f.getAbsolutePath());
        }
        else if (!activeAxiomTypes.contains(AxiomType.PROPERTY_RANGE)) {
            log.info("Skipped property required for class because not activated");
        }
        else {
            List<ParsedAxiom> axioms = this.parser.parse(f, false);

            ValueNormalizer normalizer = ValueNormalizerFactory.getDefaultNormalizerInstance("exists P^i.T");
            normalizer.reportValues(axioms);
            normalizer.normalize(axioms);

            for (ParsedAxiom pa : axioms) {
                OWLAxiom a =
                        this.writer
                                .get_exists_pi_T_sub_c_Axioms(pa.getAnte1(), pa.getCons(), pa.getSupp(), pa.getConf());
                if (a != null) {
                    if (pa.getSupp() != 0.0) {
                        rac.add(a, pa.getConf());
                    }
                    hmAxioms.put(a, pa.getSuppConfTuple());
                }
            }
        }
        log.debug("Number of Axioms: {}", hmAxioms.size());

        /* P sub P */
        log.debug("p_sub_p");
        f = new File(requirementsResolver.getRequiredTransactionTable(AxiomType.PROPERTY_SUBSUMPTION)
                .getAbsoluteAssociationRuleFileName());
        if (!f.exists()) {
            log.warn("Unable to read: '{}'! Skipping...", f.getAbsolutePath());
        }
        else if (!activeAxiomTypes.contains(AxiomType.PROPERTY_SUBSUMPTION)) {
            log.info("Skipped property subsumption because not activated");
        }
        else {
            List<ParsedAxiom> axioms = this.parser.parse(f, false);

            ValueNormalizer normalizer = ValueNormalizerFactory.getDefaultNormalizerInstance("P sub P");
            normalizer.reportValues(axioms);
            normalizer.normalize(axioms);

            for (ParsedAxiom pa : axioms) {
                OWLAxiom a =
                        this.writer.get_p_sub_p_Axioms(pa.getAnte1(), pa.getCons(), pa.getSupp(), pa.getConf());
                if (a != null) {
                    if (pa.getSupp() != 0.0) {
                        rac.add(a, pa.getConf());
                    }
                    hmAxioms.put(a, pa.getSuppConfTuple());
                }
            }
        }
        log.debug("Number of Axioms: {}", hmAxioms.size());

        /* Property Chaining: P o Q sub R */
        log.debug("p_chain_q_sub_r");
        f = new File(requirementsResolver.getRequiredTransactionTable(AxiomType.PROPERTY_CHAINS)
                .getAbsoluteAssociationRuleFileName());
        if (!f.exists()) {
            log.warn("Unable to read: '{}'! Skipping...", f.getAbsolutePath());
        }
        else if (!activeAxiomTypes.contains(AxiomType.PROPERTY_CHAINS)) {
            log.info("Skipped property chains because not activated");
        }
        else {
            List<ParsedAxiom> axioms = this.parser.parse(f, false);

            ValueNormalizer normalizer = ValueNormalizerFactory.getDefaultNormalizerInstance("P o Q sub R");
            normalizer.reportValues(axioms);
            normalizer.normalize(axioms);

            for (ParsedAxiom pa : axioms) {
                OWLAxiom a =
                        this.writer.get_p_chain_q_sub_r_Axioms(pa.getAnte1(), pa.getCons(), pa.getSupp(),
                                pa.getConf());
                if (a != null) {
                    if (pa.getSupp() != 0.0) {
                        rac.add(a, pa.getConf());
                    }
                    hmAxioms.put(a, pa.getSuppConfTuple());
                }
            }
        }
        log.debug("Number of Axioms: {}", hmAxioms.size());

        /* Property Transitivity: P o P sub P*/
        log.debug("p_chain_p_sub_p");
        f = new File(requirementsResolver.getRequiredTransactionTable(AxiomType.PROPERTY_TRANSITIVITY)
                .getAbsoluteAssociationRuleFileName());
        if (!f.exists()) {
            log.warn("Unable to read: '{}'! Skipping...", f.getAbsolutePath());
        }
        else if (!activeAxiomTypes.contains(AxiomType.PROPERTY_TRANSITIVITY)) {
            log.info("Skipped property transitivity because not activated");
        }
        else {
            List<ParsedAxiom> axioms = this.parser.parse(f, false);

            ValueNormalizer normalizer = ValueNormalizerFactory.getDefaultNormalizerInstance("Transitivity");
            normalizer.reportValues(axioms);
            normalizer.normalize(axioms);

            for (ParsedAxiom pa : axioms) {
                OWLAxiom a =
                        this.writer.get_p_chain_p_sub_p_Axioms(pa.getAnte1(), pa.getCons(), pa.getSupp(),
                                pa.getConf());
                if (a != null) {
                    if (pa.getSupp() != 0.0) {
                        rac.add(a, pa.getConf());
                    }
                    hmAxioms.put(a, pa.getSuppConfTuple());
                }
            }
        }
        log.debug("Number of Axioms: {}", hmAxioms.size());

        /* Concept Disjointness */
        log.debug("c_dis_c");
        f = new File(requirementsResolver.getRequiredTransactionTable(AxiomType.CLASS_DISJOINTNESS)
                .getAbsoluteAssociationRuleFileName());
        if (!f.exists()) {
            log.warn("Unable to read: '{}'! Skipping...", f.getAbsolutePath());
        }
        else if (!activeAxiomTypes.contains(AxiomType.CLASS_DISJOINTNESS)) {
            log.info("Skipped class disjointness because not activated");
        }
        else {
            List<ParsedAxiom> axioms = this.parser.parse(f, false);

            ValueNormalizer normalizer = ValueNormalizerFactory.getDefaultNormalizerInstance("Concept Disjointness");
            normalizer.reportValues(axioms);
            normalizer.normalize(axioms);

            HashMap<ConceptIdPair, SupportConfidenceTuple> firstOccurence = new HashMap<ConceptIdPair,
                    SupportConfidenceTuple>();
            for (ParsedAxiom pa : axioms) {
                //TODO: probably wrong due to different way of naming negated concepts
                //TODO: we are not able to preserve the order of concepts in axiom
                int ante1 = pa.getAnte1();
                int cons = pa.getCons();

                if (cons >= 1000 && ante1 < 1000) {
                    cons -= 1000;
                }
                else {
                    continue;
                }


                ConceptIdPair pair = new ConceptIdPair(this.writer.getClassURI(ante1), this.writer.getClassURI(cons));
                if (firstOccurence.containsKey(pair)) {
                    OWLAxiom a = null;
                    SupportConfidenceTuple supportConfidenceTuple = firstOccurence.get(pair);
                    if (pa.getConf() < supportConfidenceTuple.getConfidence()) {
                        a = this.writer.get_c_dis_c_Axioms(ante1, cons, pa.getSupp(), pa.getConf());
                    }
                    else {
                        a = this.writer.get_c_dis_c_Axioms(ante1, cons, supportConfidenceTuple.getSupport(),
                                supportConfidenceTuple.getConfidence());
                    }
                    hmAxioms.put(a, pa.getSuppConfTuple());
                    if (pa.getSupp() != 0.0) {
                        rac.add(a, pa.getConf());
                    }
                }
                else {
                    firstOccurence.put(pair, pa.getSuppConfTuple());
                }
            }
        }
        log.debug("Number of Axioms: {}", hmAxioms.size());

        /* Property Disjointness */
        try {
            log.debug("p_dis_p");
            PropertyDisjointnessModule propertyDisjointnessModule = new PropertyDisjointnessModule(moduleConfig);
            f = new File(requirementsResolver.getRequiredTransactionTable(AxiomType.PROPERTY_DISJOINTNESS)
                    .getAbsoluteAssociationRuleFileName());
            if (!f.exists()) {
                log.warn("Unable to read: '{}'! Skipping...", f.getAbsolutePath());
            }
            else if (!activeAxiomTypes.contains(AxiomType.PROPERTY_DISJOINTNESS)) {
                log.info("Skipped property disjointness because not activated");
            }
            else {
                // TODO: adjust to actual method signature
                propertyDisjointnessModule.readAssociationRules(f, hmAxioms);
            }
            log.debug("Number of Axioms: " + hmAxioms.size());
        }
        catch (MinerModuleException e) {
            log.error("Unable to mine object property disjointness");
            e.printStackTrace();
        }

        /* Property Reflexivity */
        log.debug("p_reflexive");
        f = new File(requirementsResolver.getRequiredTransactionTable(AxiomType.REFLEXIVE_PROPERTY)
                .getAbsoluteAssociationRuleFileName());
        if (!f.exists()) {
            log.warn("Unable to read: '{}'! Skipping...", f.getAbsolutePath());
        }
        else if (!activeAxiomTypes.contains(AxiomType.REFLEXIVE_PROPERTY)) {
            log.info("Skipped property reflexivity because not activated");
        }
        else {
            List<ParsedAxiom> axioms = this.parser.parse(f, false);
            log.debug("Parsed axioms: {}", axioms.size());

            ValueNormalizer normalizer = ValueNormalizerFactory.getDefaultNormalizerInstance("Property Reflexivity");
            normalizer.reportValues(axioms);
            normalizer.normalize(axioms);

            for (ParsedAxiom pa : axioms) {
                OWLAxiom a =
                        this.writer.get_p_reflexive_Axioms(pa.getAnte1(), pa.getCons(), pa.getSupp(), pa.getConf());
                if (a != null) {
                    if (pa.getSupp() != 0.0) {
                        rac.add(a, pa.getConf());
                    }
                    hmAxioms.put(a, pa.getSuppConfTuple());
                }
            }
        }
        log.debug("Number of Axioms: {}", hmAxioms.size());

        /* Property Irreflexivity */
        log.debug("p_irreflexive");
        f = new File(requirementsResolver.getRequiredTransactionTable(AxiomType.IRREFLEXIVE_PROPERTY)
                .getAbsoluteAssociationRuleFileName());
        if (!f.exists()) {
            log.warn("Unable to read: '{}'! Skipping...", f.getAbsolutePath());
        }
        else if (!activeAxiomTypes.contains(AxiomType.IRREFLEXIVE_PROPERTY)) {
            log.info("Skipped irreflexive property because not activated");
        }
        else {
            List<ParsedAxiom> axioms = this.parser.parse(f, false);
            log.debug("Parsed axioms: {}", axioms.size());

            ValueNormalizer normalizer = ValueNormalizerFactory.getDefaultNormalizerInstance("Property Irreflexivity");
            normalizer.reportValues(axioms);
            normalizer.normalize(axioms);

            for (ParsedAxiom pa : axioms) {
                OWLAxiom a =
                        this.writer.get_p_irreflexive_Axioms(pa.getAnte1(), pa.getCons(), pa.getSupp(), pa.getConf());
                if (a != null) {
                    if (pa.getSupp() != 0.0) {
                        rac.add(a, pa.getConf());
                    }
                    hmAxioms.put(a, pa.getSuppConfTuple());
                }
            }
        }
        log.debug("Number of Axioms: {}", hmAxioms.size());

        /* Inverse Property */
        log.debug("p_inverse_q");
        f = new File(requirementsResolver.getRequiredTransactionTable(AxiomType.INVERSE_PROPERTY)
                .getAbsoluteAssociationRuleFileName());
        if (!f.exists()) {
            log.warn("Unable to read: '{}'! Skipping...", f.getAbsolutePath());
        }
        else if (!activeAxiomTypes.contains(AxiomType.INVERSE_PROPERTY)) {
            log.info("Skipped inverse property because not activated");
        }
        else {
            List<ParsedAxiom> axioms = this.parser.parse(f, false);

            ValueNormalizer normalizer = ValueNormalizerFactory.getDefaultNormalizerInstance("Inverse Property");
            normalizer.reportValues(axioms);
            normalizer.normalize(axioms);

            for (ParsedAxiom pa : axioms) {
                OWLAxiom a =
                        this.writer.get_p_inverse_q_Axioms(pa.getAnte1(), pa.getCons(), pa.getSupp(), pa.getConf());
                if (a != null) {
                    if (pa.getSupp() != 0.0) {
                        rac.add(a, pa.getConf());
                    }
                    hmAxioms.put(a, pa.getSuppConfTuple());
                }
            }
        }
        log.debug("Number of Axioms: {}", hmAxioms.size());

        /* Asymmetric property */
        log.debug("p_asymmetric");
        f = new File(requirementsResolver.getRequiredTransactionTable(AxiomType.PROPERTY_ASYMMETRY)
                .getAbsoluteAssociationRuleFileName());
        if (!f.exists()) {
            log.warn("Unable to read: '{}'! Skipping...", f.getAbsolutePath());
        }
        else if (!activeAxiomTypes.contains(AxiomType.PROPERTY_ASYMMETRY)) {
            log.info("Skipped asymmetric property because not activated");
        }
        else {
            List<ParsedAxiom> axioms = this.parser.parse(f, false);

            ValueNormalizer normalizer = ValueNormalizerFactory.getDefaultNormalizerInstance("Property Asymmetry");
            normalizer.reportValues(axioms);
            normalizer.normalize(axioms);

            for (ParsedAxiom pa : axioms) {
                OWLAxiom a =
                        this.writer.get_p_asymmetric_Axioms(pa.getAnte1(), pa.getCons(), pa.getSupp(), pa.getConf());
                if (a != null) {
                    if (pa.getSupp() != 0.0) {
                        rac.add(a, pa.getConf());
                    }
                    hmAxioms.put(a, pa.getSuppConfTuple());
                }
            }
        }
        log.debug("Number of Axioms: {}", hmAxioms.size());

        /* Functional Property */
        log.debug("p_functional");
        f = new File(requirementsResolver.getRequiredTransactionTable(AxiomType.FUNCTIONAL_PROPERTY)
                .getAbsoluteAssociationRuleFileName());
        if (!f.exists()) {
            log.warn("Unable to read: '{}'! Skipping...", f.getAbsolutePath());
        }
        else if (!activeAxiomTypes.contains(AxiomType.FUNCTIONAL_PROPERTY)) {
            log.info("Skipped functional property because not activated");
        }
        else {
            List<ParsedAxiom> axioms = this.parser.parse(f, false);

            ValueNormalizer normalizer = ValueNormalizerFactory.getDefaultNormalizerInstance("Property Functionality");
            normalizer.reportValues(axioms);
            normalizer.normalize(axioms);

            for (ParsedAxiom pa : axioms) {
                OWLAxiom a =
                        this.writer.get_p_functional_Axioms(pa.getAnte1(), pa.getCons(), pa.getSupp(), pa.getConf());
                if (a != null) {
                    if (pa.getSupp() != 0.0) {
                        rac.add(a, pa.getConf());
                    }
                    hmAxioms.put(a, pa.getSuppConfTuple());
                }
            }
        }
        log.debug("Number of Axioms: {}", hmAxioms.size());

        /* Property Inverse Functionality */
        log.debug("p_inverse_functional");
        f = new File(requirementsResolver.getRequiredTransactionTable(AxiomType.INVERSE_FUNCTIONAL_PROPERTY)
                .getAbsoluteAssociationRuleFileName());
        if (!f.exists()) {
            log.warn("Unable to read: '{}'! Skipping...", f.getAbsolutePath());
        }
        else if (!activeAxiomTypes.contains(AxiomType.INVERSE_FUNCTIONAL_PROPERTY)) {
            log.info("Skipped inverse functional property because not activated");
        }
        else {
            List<ParsedAxiom> axioms = this.parser.parse(f, false);

            ValueNormalizer normalizer = ValueNormalizerFactory
                    .getDefaultNormalizerInstance("Property Inverse Functionality");
            normalizer.reportValues(axioms);
            normalizer.normalize(axioms);

            for (ParsedAxiom pa : axioms) {
                OWLAxiom a = this.writer
                        .get_p_inverse_functional_Axioms(pa.getAnte1(), pa.getCons(), pa.getSupp(), pa.getConf());
                if (a != null) {
                    if (pa.getSupp() != 0.0) {
                        rac.add(a, pa.getConf());
                    }
                    hmAxioms.put(a, pa.getSuppConfTuple());
                }
            }
        }
        log.debug("Number of Axioms: {}", hmAxioms.size());

        log.info("Writing axiom lists into directory '{}'", Settings.getString("axiom_list_dir"));
        File axiomListDir = new File(Settings.getString("axiom_list_dir"));
        if (!axiomListDir.exists()) {
            axiomListDir.mkdirs();
        }
        writer.writeLists(hmAxioms, axiomListDir.getAbsoluteFile());
        return hmAxioms;
    }

    private void initializeOntology() throws SQLException, OWLOntologyStorageException {
        this.writer = new OntologyWriter(this.sqlDatabase, this.ontology, writeAnnotations);
        this.ontology = this.writer.writeClassesAndPropertiesToOntology();
        this.ontology.save();
    }

    public Ontology createOntology(HashMap<OWLAxiom, SupportConfidenceTuple> axioms,
                                   double supportThreshold, double confidenceThreshold)
            throws OWLOntologyStorageException, SQLException {
        //this.initializeOntology();
        this.writer = new OntologyWriter(this.sqlDatabase, this.ontology, writeAnnotations);
        Ontology o = this.writer.write(axioms, supportThreshold, confidenceThreshold);
        //o.save();
        this.ontology = o;
        return o;
    }

    public Ontology greedyDebug(Ontology ontology) throws OWLOntologyStorageException {
        return OntologyDebugger.greedyWrite(ontology);
    }

    public void saveYagoClasses() throws IOException, SQLException {
        this.tablePrinter.saveYagoAssignments();
    }
}
