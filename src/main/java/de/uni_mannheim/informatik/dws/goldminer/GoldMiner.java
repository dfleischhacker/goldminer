package de.uni_mannheim.informatik.dws.goldminer;

import de.uni_mannheim.informatik.dws.goldminer.database.*;
import de.uni_mannheim.informatik.dws.goldminer.modules.MinerModuleConfiguration;
import de.uni_mannheim.informatik.dws.goldminer.modules.MinerModuleException;
import de.uni_mannheim.informatik.dws.goldminer.modules.PropertyDisjointnessModule;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class GoldMiner {

    public static Logger log = LoggerFactory.getLogger(GoldMiner.class);

    private static final String[] transactionTableNames =
            {"classmembers", "existspropertymembers", "propertyrestrictions1", "propertyrestrictions2",
                    "propertymembers",
                    "propertychainmembers", "classdisjointness", "propertyreflexivity", "propertyinversemembers",
                    "propertyfunctionalmembers", "propertyinversefunctional"};
    private static final String associationRulesSuffix = "AR";
    private AssociationRulesParser parser;
    private OntologyWriter writer;
    private Ontology ontology;
    private CheckpointUtil chk;
    private RandomAxiomChooser rac = new RandomAxiomChooser();

    public GoldMiner() throws IOException, SQLException, OWLOntologyCreationException,
            OWLOntologyStorageException {
        if (!Settings.loaded()) {
            Settings.load();
        }
        this.selectAxioms();
        this.sqlDatabase = SQLDatabase.instance();
        this.setup = new Setup();
        this.tablePrinter = new TablePrinter();
        this.terminologyExtractor = new TerminologyExtractor();
        this.individualsExtractor = new IndividualsExtractor();
        this.parser = new AssociationRulesParser();
        this.ontology = new Ontology();
        this.ontology.create(new File(Settings.getString("ontology")));
        this.ontology.save();
        this.chk = new CheckpointUtil(Settings.getString("transaction_tables") + "/checkpoints");
    }

    public GoldMiner(String ontologyFile)
            throws IOException, SQLException, OWLOntologyCreationException,
            OWLOntologyStorageException {
        if (!Settings.loaded()) {
            Settings.load();
        }
        this.selectAxioms();
        this.sqlDatabase = SQLDatabase.instance();
        this.setup = new Setup();
        this.tablePrinter = new TablePrinter();
        this.terminologyExtractor = new TerminologyExtractor();
        this.individualsExtractor = new IndividualsExtractor();
        this.parser = new AssociationRulesParser();
        this.ontology = new Ontology();
        this.ontology.create(new File(ontologyFile));
        this.ontology.save();
        this.chk = new CheckpointUtil(Settings.getString("transaction_tables") + "/checkpoints");
    }

    private SQLDatabase sqlDatabase;
    private Setup setup;
    private TerminologyExtractor terminologyExtractor;
    private IndividualsExtractor individualsExtractor;
    private TablePrinter tablePrinter;
    private boolean c_sub_c;
    private boolean c_and_c_sub_c;
    private boolean c_sub_exists_p_c;
    private boolean exists_p_c_sub_c;
    private boolean exists_p_T_sub_c;
    private boolean exists_pi_T_sub_c;
    private boolean p_sub_p;
    private boolean p_dis_p;
    private boolean p_chain_p_sub_p;
    private boolean p_chain_q_sub_r;
    private boolean c_dis_c;
    private boolean p_reflexive;
    private boolean p_irreflexive;
    private boolean p_inverse_q;
    private boolean p_asymmetric;
    private boolean p_functional;
    private boolean p_inverse_functional;
    private boolean writeAnnotations;

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
        if (chk.reached("setupdatabase")) {
            return true;
        }

        boolean classes;
        boolean individuals;
        boolean properties;
        boolean classes_ex_property;
        boolean classes_ex_property_top;
        boolean individual_pairs;
        boolean individual_pairs_trans;
        boolean property_chains;
        boolean property_chains_trans;
        if (this.c_sub_c ||
                this.c_and_c_sub_c ||
                this.c_sub_exists_p_c ||
                this.exists_p_c_sub_c ||
                this.exists_p_T_sub_c ||
                this.exists_pi_T_sub_c ||
                this.c_dis_c ||
                this.p_reflexive ||
                this.p_irreflexive ||
                this.p_inverse_q ||
                this.p_asymmetric ||
                this.p_functional ||
                this.p_inverse_functional) {
            classes = true;
            individuals = true;
        }
        else {
            classes = false;
            individuals = false;
        }
        if (this.p_sub_p ||
                this.p_chain_q_sub_r ||
                this.p_chain_p_sub_p ||
                this.c_sub_exists_p_c ||
                this.exists_p_c_sub_c ||
                this.p_dis_p ||
                this.p_reflexive ||
                this.p_irreflexive ||
                this.p_inverse_q ||
                this.p_asymmetric ||
                this.p_functional ||
                this.p_inverse_functional ||
                this.exists_p_T_sub_c ||
                this.exists_pi_T_sub_c) {
            properties = true;
        }
        else {
            properties = false;
        }
        if (this.c_sub_exists_p_c || this.exists_p_c_sub_c) {
            classes_ex_property = true;
        }
        else {
            classes_ex_property = false;
        }
        if (this.exists_p_T_sub_c || this.exists_pi_T_sub_c) {
            classes_ex_property_top = true;
        }
        else {
            classes_ex_property_top = false;
        }
        if (this.p_sub_p ||
                this.p_chain_q_sub_r ||
                this.p_chain_p_sub_p ||
                this.p_dis_p ||
                this.p_inverse_q ||
                this.p_asymmetric) {
            individual_pairs = true;
        }
        else {
            individual_pairs = false;
        }
        if (this.p_chain_q_sub_r ||
                this.p_chain_p_sub_p) {
            individual_pairs_trans = true;
            property_chains = true;
            property_chains_trans = true;
        }
        else {
            individual_pairs_trans = false;
            property_chains = false;
            property_chains_trans = false;
        }
        if (this.setup.setupSchema(classes, individuals, properties, classes_ex_property, classes_ex_property_top,
                individual_pairs, individual_pairs_trans, property_chains, property_chains_trans)) {
            chk.reach("setupdatabase");
            return true;
        }
//        else {
//            this.setup.removeSchema();
//            return false;
//        }
        return false;

    }

    public boolean terminologyAcquisition() throws SQLException {
        if ((this.c_sub_c ||
                this.c_and_c_sub_c ||
                this.c_sub_exists_p_c ||
                this.exists_p_c_sub_c ||
                this.exists_p_T_sub_c ||
                this.exists_pi_T_sub_c ||
                this.c_dis_c ||
                this.p_reflexive ||
                this.p_irreflexive ||
                this.p_inverse_q ||
                this.p_asymmetric ||
                this.p_inverse_functional) &&
                !chk.reached("initclassestable")) {
            this.terminologyExtractor.initClassesTable();
            this.individualsExtractor.initIndividualsTable();
            chk.reach("initclassestable");
        }
        if ((this.p_sub_p ||
                this.p_chain_q_sub_r ||
                this.p_chain_p_sub_p ||
                this.c_sub_exists_p_c ||
                this.exists_p_c_sub_c ||
                this.p_dis_p ||
                this.p_reflexive ||
                this.p_irreflexive ||
                this.p_inverse_q ||
                this.p_asymmetric ||
                this.p_inverse_functional) &&
                !chk.reached("initpropertiestable")) {
            this.terminologyExtractor.initPropertiesTable();
            chk.reach("initpropertiestable");
        }
        if ((this.c_sub_exists_p_c || this.exists_p_c_sub_c) && !chk.reached("initclassesexistspropertytable")) {
            this.terminologyExtractor.initClassesExistsPropertyTable();
            chk.reach("initclassesexistspropertytable");
        }
        if ((this.exists_p_T_sub_c || this.exists_pi_T_sub_c) && !chk.reached("initpropertytoptable")) {
            this.terminologyExtractor.initPropertyTopTable();
            chk.reach("initpropertytoptable");
        }
        if ((this.p_sub_p || this.p_chain_q_sub_r || this.p_chain_p_sub_p || this.p_dis_p || this.p_inverse_q ||
                this.p_asymmetric) && !chk.reached("initindividualpairstable")) {
            this.individualsExtractor.initIndividualPairsTable();
            chk.reach("initindividualpairstable");
        }
        if ((this.p_chain_q_sub_r || this.p_chain_p_sub_p) && !chk.reached("initpropertychainstable")) {
            this.terminologyExtractor.initPropertyChainsTable();
            this.terminologyExtractor.initPropertyChainsTransTable();
            this.individualsExtractor.initIndividualPairsTransTable();
            chk.reach("initpropertychainstable");
        }
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

    public void selectAxioms() {
        this.c_sub_c = Settings.getAxiom("c_sub_c");
        this.c_and_c_sub_c = Settings.getAxiom("c_and_c_sub_c");
        this.c_sub_exists_p_c = Settings.getAxiom("c_sub_exists_p_c");
        this.exists_p_c_sub_c = Settings.getAxiom("exists_p_c_sub_c");
        this.exists_p_T_sub_c = Settings.getAxiom("exists_p_T_sub_c");
        this.exists_pi_T_sub_c = Settings.getAxiom("exists_pi_T_sub_c");
        this.p_sub_p = Settings.getAxiom("p_sub_p");
        this.p_chain_q_sub_r = Settings.getAxiom("p_chain_q_sub_r");
        this.p_chain_p_sub_p = Settings.getAxiom("p_chain_p_sub_p");
        this.c_dis_c = Settings.getAxiom("c_dis_c");
        this.p_dis_p = Settings.getAxiom("p_dis_p");
        this.p_reflexive = Settings.getAxiom("p_reflexive");
        this.p_irreflexive = Settings.getAxiom("p_irreflexive");
        this.p_inverse_q = Settings.getAxiom("p_inverse_q");
        this.p_asymmetric = Settings.getAxiom("p_asymmetric");
        this.p_functional = Settings.getAxiom("p_functional");
        this.p_inverse_functional = Settings.getAxiom("p_inverse_functional");
        this.writeAnnotations = Settings.getAxiom("write_annotations");
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
        if ((this.c_sub_c || this.c_and_c_sub_c) && !chk.reached("classmembers")) {
            this.deleteFile(0);
            this.tablePrinter
                    .printClassMembers(Settings.getString("transaction_tables") + transactionTableNames[0] + ".txt");
            chk.reach("classmembers");
        }
        if ((this.c_sub_exists_p_c || this.exists_p_c_sub_c) && !chk.reached("existspropertymembers")) {
            this.deleteFile(1);
            this.tablePrinter.printExistsPropertyMembers(
                    Settings.getString("transaction_tables") + transactionTableNames[1] + ".txt", 0);
            chk.reach("existspropertymembers");
        }
        if (this.exists_p_T_sub_c && !chk.reached("propertyrestrictions1")) {
            this.deleteFile(2);
            this.tablePrinter
                    .printPropertyRestrictions(
                            Settings.getString("transaction_tables") + transactionTableNames[2] + ".txt",
                            0);
            chk.reach("propertyrestrictions1");
        }
        if (this.exists_pi_T_sub_c && !chk.reached("propertyrestrictions2")) {
            this.deleteFile(3);
            this.tablePrinter
                    .printPropertyRestrictions(
                            Settings.getString("transaction_tables") + transactionTableNames[3] + ".txt",
                            1);
            chk.reach("propertyrestrictions2");
        }
        if ((this.p_sub_p || this.p_dis_p) && !chk.reached("propertymembers")) {
            this.deleteFile(4);
            this.tablePrinter
                    .printPropertyMembers(Settings.getString("transaction_tables") + transactionTableNames[4] + "" +
                            ".txt");
            chk.reach("propertymembers");
        }
        if ((this.p_chain_q_sub_r || this.p_chain_p_sub_p) && !chk.reached("propertychainmembers")) {
            this.deleteFile(5);
            this.tablePrinter.printPropertyChainMembersTrans_new(
                    Settings.getString("transaction_tables") + transactionTableNames[5] + ".txt");
            chk.reach("propertychainmembers");
        }
        if ((this.p_reflexive || this.p_irreflexive) && !chk.reached("propertyreflexivity")) {
            this.deleteFile(7);
            this.tablePrinter
                    .printPropertyReflexivity(
                            Settings.getString("transaction_tables") + transactionTableNames[7] + ".txt");
            chk.reach("propertyreflexivity");
        }
        if ((this.p_inverse_q || this.p_asymmetric) && !chk.reached("propertyinversemembers")) {
            this.deleteFile(8);
            this.tablePrinter.printPropertyInverseMembers(
                    Settings.getString("transaction_tables") + transactionTableNames[8] + ".txt");
            chk.reach("propertyinversemembers");
        }
        if (this.p_functional && !chk.reached("propertyfunctionalmembers")) {
            this.deleteFile(9);
            this.tablePrinter.printPropertyFunctionalMembers(
                    Settings.getString("transaction_tables") + transactionTableNames[9] + ".txt");
            chk.reach("propertyfunctionalmembers");

        }
        if (this.p_inverse_functional && !chk.reached("propertyinversefunctional")) {
            this.deleteFile(10);
            this.tablePrinter.printPropertyInverseFunctionalMembers(
                    Settings.getString("transaction_tables") + transactionTableNames[10] + ".txt");
            chk.reach("propertyinversefunctional");
        }
        if (this.c_dis_c && !chk.reached("classdisjointness")) {
            this.deleteFile(6);
            this.tablePrinter.printDisjointClassMembers(
                    Settings.getString("transaction_tables") + transactionTableNames[6] + ".txt");
            chk.reach("classdisjointness");
        }
    }

    private void deleteFile(int index) throws IOException {
        File f = new File(Settings.getString("transaction_tables") + transactionTableNames[index] + ".txt");
        f.delete();
        f.createNewFile();
    }

    public void mineAssociationRules() throws IOException {
        File file = new File(Settings.getString("transaction_tables"));
        File[] files = this.removeFiles(file.listFiles(new TextFileFilter()));
        File ruleFile = new File(Settings.getString("association_rules"));
        if (!ruleFile.exists()) {
            ruleFile.mkdirs();
        }
        File[] ruleFiles = ruleFile.listFiles(new TextFileFilter());
        this.deleteFiles(ruleFiles);
        for (File f : files) {
            int index = f.getName().lastIndexOf(".");
            ProcessBuilder p = new ProcessBuilder(Settings.getString("apriori"),
                    "-tr", "-s-1", "-c0", "-m2", "-n2", "-v (%20s, %30c)",
                    f.getPath(),
                    new File(Settings.getString("association_rules")).getAbsolutePath() + File.separator + f.getName()
                            .substring(0,
                                    index) +
                            associationRulesSuffix + ".txt"
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

    private File[] removeFiles(File[] files) {
        File[] result;
        List<Integer> indexes = new ArrayList<Integer>();
        if (!this.c_sub_c && !this.c_and_c_sub_c) {
            for (int i = 0; i < files.length; i++) {
                if (files[i].getName().equals(transactionTableNames[0] + ".txt")) {
                    indexes.add(i);
                }
            }
        }
        if (!this.c_sub_exists_p_c && !this.exists_p_c_sub_c) {
            for (int i = 0; i < files.length; i++) {
                if (files[i].getName().equals(transactionTableNames[1] + ".txt")) {
                    indexes.add(i);
                }
            }
        }
        if (!this.exists_p_T_sub_c) {
            for (int i = 0; i < files.length; i++) {
                if (files[i].getName().equals(transactionTableNames[2] + ".txt")) {
                    indexes.add(i);
                }
            }
        }
        if (!this.exists_pi_T_sub_c) {
            for (int i = 0; i < files.length; i++) {
                if (files[i].getName().equals(transactionTableNames[3] + ".txt")) {
                    indexes.add(i);
                }
            }
        }
        if (!this.p_sub_p && !this.p_dis_p) {
            for (int i = 0; i < files.length; i++) {
                if (files[i].getName().equals(transactionTableNames[4] + ".txt")) {
                    indexes.add(i);
                }
            }
        }
        if (!this.p_chain_q_sub_r && !this.p_chain_p_sub_p) {
            for (int i = 0; i < files.length; i++) {
                if (files[i].getName().equals(transactionTableNames[5] + ".txt")) {
                    indexes.add(i);
                }
            }
        }
        if (!this.c_dis_c) {
            for (int i = 0; i < files.length; i++) {
                if (files[i].getName().equals(transactionTableNames[6] + ".txt")) {
                    indexes.add(i);
                }
            }
        }
        if (!this.p_reflexive && !this.p_irreflexive) {
            for (int i = 0; i < files.length; i++) {
                if (files[i].getName().equals(transactionTableNames[7] + ".txt")) {
                    indexes.add(i);
                }
            }
        }
        if (!this.p_inverse_q && !this.p_asymmetric) {
            for (int i = 0; i < files.length; i++) {
                if (files[i].getName().equals(transactionTableNames[8] + ".txt")) {
                    indexes.add(i);
                }
            }
        }
        if (!this.p_functional) {
            for (int i = 0; i < files.length; i++) {
                if (files[i].getName().equals(transactionTableNames[9] + ".txt")) {
                    indexes.add(i);
                }
            }
        }
        if (!this.p_inverse_functional) {
            for (int i = 0; i < files.length; i++) {
                if (files[i].getName().equals(transactionTableNames[10] + ".txt")) {
                    indexes.add(i);
                }
            }
        }
        result = new File[files.length - indexes.size()];
        int x = 0;
        for (int i = 0; i < files.length; i++) {
            if (!indexes.contains(i)) {
                result[x] = files[i];
                x++;
            }
        }
        return result;
    }

    private void deleteFiles(File[] files) {
        for (File f : files) {
            int index = f.getName().lastIndexOf(".");
            String fileName = f.getName().substring(0, index);
            if (((this.c_sub_c || this.c_and_c_sub_c) &&
                    fileName.equals(transactionTableNames[0] + associationRulesSuffix)) ||
                    ((this.c_sub_exists_p_c || this.exists_p_c_sub_c) &&
                            fileName.equals(transactionTableNames[1] + associationRulesSuffix)) ||
                    (this.exists_p_T_sub_c && fileName.equals(transactionTableNames[2] + associationRulesSuffix)) ||
                    (this.exists_pi_T_sub_c && fileName.equals(transactionTableNames[3] + associationRulesSuffix)) ||
                    ((this.p_sub_p || this.p_dis_p) &&
                            fileName.equals(transactionTableNames[4] + associationRulesSuffix)) ||
                    ((this.p_chain_q_sub_r || this.p_chain_p_sub_p) &&
                            fileName.equals(transactionTableNames[5] + associationRulesSuffix)) ||
                    (this.c_dis_c && fileName.equals(transactionTableNames[6] + associationRulesSuffix)) ||
                    ((this.p_reflexive || this.p_irreflexive) &&
                            fileName.equals(transactionTableNames[7] + associationRulesSuffix)) ||
                    ((this.p_inverse_q || this.p_asymmetric) &&
                            fileName.equals(transactionTableNames[8] + associationRulesSuffix)) ||
                    (this.p_functional && fileName.equals(transactionTableNames[9] + associationRulesSuffix)) ||
                    (this.p_inverse_functional && fileName
                            .equals(transactionTableNames[10] + associationRulesSuffix))) {
                f.delete();
            }
        }
    }

    public void mineAssociationRules(AssociationRulesMiner miner) {
        miner.execute();
    }

    public List<String> getAssociationRules() throws IOException {
        List<String> rules = new ArrayList<String>();
        File file = new File(Settings.getString("association_rules"));
        File[] files = file.listFiles(new TextFileFilter());
        for (File f : files) {
            BufferedReader in = new BufferedReader(new FileReader(f));
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

        /* Concept Subsumption: c sub c */

        File f = new File(
                Settings.getString("association_rules") + transactionTableNames[0] + associationRulesSuffix + ".txt");
        if (!f.exists()) {
            log.warn("Unable to read: {}! Skipping...", f.getAbsolutePath());
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
        f = new File(
                Settings.getString(
                        "association_rules") + File.separator + transactionTableNames[0] + associationRulesSuffix + "" +
                        ".txt"
        );
        if (!f.exists()) {
            log.warn("Unable to read: '{}'! Skipping...", f.getAbsolutePath());
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
        f = new File(
                Settings.getString(
                        "association_rules") + File.separator + transactionTableNames[1] + associationRulesSuffix + "" +
                        ".txt"
        );
        if (!f.exists()) {
            log.warn("Unable to read: '{}'! Skipping...", f.getAbsolutePath());
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
        f = new File(
                Settings.getString(
                        "association_rules") + File.separator + transactionTableNames[1] + associationRulesSuffix + "" +
                        ".txt"
        );
        if (!f.exists()) {
            log.warn("Unable to read: '{}'! Skipping...", f.getAbsolutePath());
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
        f = new File(
                Settings.getString(
                        "association_rules") + File.separator + transactionTableNames[2] + associationRulesSuffix + "" +
                        ".txt"
        );
        if (!f.exists()) {
            log.warn("Unable to read: '{}'! Skipping...", f.getAbsolutePath());
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
        f = new File(
                Settings.getString(
                        "association_rules") + File.separator + transactionTableNames[3] + associationRulesSuffix + "" +
                        ".txt"
        );
        if (!f.exists()) {
            log.warn("Unable to read: '{}'! Skipping...", f.getAbsolutePath());
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
        f = new File(
                Settings.getString(
                        "association_rules") + File.separator + transactionTableNames[4] + associationRulesSuffix + "" +
                        ".txt"
        );
        if (!f.exists()) {
            log.warn("Unable to read: '{}'! Skipping...", f.getAbsolutePath());
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
        if (p_chain_q_sub_r) {
            log.debug("p_chain_q_sub_r");
            f = new File(
                    Settings.getString(
                            "association_rules") + transactionTableNames[5] + associationRulesSuffix + ".txt"
            );
            if (!f.exists()) {
                log.warn("Unable to read: '{}'! Skipping...", f.getAbsolutePath());
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
        }

        /* Property Transitivity: P o P sub P*/
        log.debug("p_chain_p_sub_p");
        f = new File(
                Settings.getString(
                        "association_rules") + File.separator + transactionTableNames[5] + associationRulesSuffix + "" +
                        ".txt"
        );
        if (!f.exists()) {
            log.warn("Unable to read: '{}'! Skipping...", f.getAbsolutePath());
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
        f = new File(
                Settings.getString(
                        "association_rules") + File.separator + transactionTableNames[6] + associationRulesSuffix + "" +
                        ".txt"
        );
        if (!f.exists()) {
            log.warn("Unable to read: '{}'! Skipping...", f.getAbsolutePath());
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
            f = new File(
                    Settings.getString(
                            "association_rules") + transactionTableNames[4] + associationRulesSuffix + ".txt"
            );
            if (!f.exists()) {
                log.warn("Unable to read: '{}'! Skipping...", f.getAbsolutePath());
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
        f = new File(
                Settings.getString(
                        "association_rules") + File.separator + transactionTableNames[7] + associationRulesSuffix + "" +
                        ".txt"
        );
        if (!f.exists()) {
            log.warn("Unable to read: '{}'! Skipping...", f.getAbsolutePath());
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
        f = new File(
                Settings.getString(
                        "association_rules") + File.separator + transactionTableNames[7] + associationRulesSuffix + "" +
                        ".txt"
        );
        if (!f.exists()) {
            log.warn("Unable to read: '{}'! Skipping...", f.getAbsolutePath());
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
        f = new File(
                Settings.getString(
                        "association_rules") + File.separator + transactionTableNames[8] + associationRulesSuffix + "" +
                        ".txt"
        );
        if (!f.exists()) {
            log.warn("Unable to read: '{}'! Skipping...", f.getAbsolutePath());
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
        f = new File(
                Settings.getString(
                        "association_rules") + File.separator + transactionTableNames[8] + associationRulesSuffix + "" +
                        ".txt"
        );
        if (!f.exists()) {
            log.warn("Unable to read: '{}'! Skipping...", f.getAbsolutePath());
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
        f = new File(
                Settings.getString(
                        "association_rules") + File.separator + transactionTableNames[9] + associationRulesSuffix + "" +
                        ".txt"
        );
        if (!f.exists()) {
            log.warn("Unable to read: '{}'! Skipping...", f.getAbsolutePath());
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
        f = new File(
                Settings.getString(
                        "association_rules") + File.separator + transactionTableNames[10] + associationRulesSuffix +
                        ".txt"
        );
        if (!f.exists()) {
            log.warn("Unable to read: '{}'! Skipping...", f.getAbsolutePath());
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
