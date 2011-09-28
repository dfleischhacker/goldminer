package miner;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

import miner.database.Database;
import miner.database.IndividualsExtractor;
import miner.database.Setup;
import miner.database.TablePrinter;
import miner.database.TerminologyExtractor;
import miner.ontology.AssociationRulesMiner;
import miner.ontology.AssociationRulesParser;
import miner.ontology.Ontology;
import miner.ontology.OntologyDebugger;
import miner.ontology.OntologyWriter;
import miner.ontology.ParsedAxiom;
import miner.sparql.Filter;
import miner.util.Settings;
import miner.util.TextFileFilter;

public class IGoldMinerImpl implements IGoldMiner {
	
	private static final String[] transactionTableNames = {"t1", "t2", "t3", "t4", "t5", "t6", "t7"};
	private static final String associationRulesSuffix = "AR";
	private AssociationRulesParser parser;
	private OntologyWriter writer;
	private Ontology ontology;
	
	public IGoldMinerImpl() throws FileNotFoundException, IOException, SQLException, OWLOntologyCreationException, OWLOntologyStorageException  {
		if(!Settings.loaded()) {
			Settings.load();
		}
		this.selectAxioms();
		this.database = Database.instance();
		this.setup = new Setup();
		this.tablePrinter = new TablePrinter();
		this.terminologyExtractor = new TerminologyExtractor();
		this.individualsExtractor = new IndividualsExtractor();
		this.parser = new AssociationRulesParser();
		this.ontology = new Ontology();
		this.ontology.create(new File(Settings.getString("ontology")));
		this.ontology.save();
	}
	
	private Database database;
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
	private boolean p_chain_p_sub_p;
	private boolean c_dis_c;
	
	@Override
	public boolean disconnect() {
		try {
			this.database.close();
			return true;
		} catch (SQLException e) {
			return false;
		}
	}
	
	@Override
	public boolean setupDatabase() throws SQLException {
		boolean classes;
		boolean individuals;
		boolean properties;
		boolean classes_ex_property;
		boolean classes_ex_property_top;
		boolean individual_pairs;
		boolean individual_pairs_trans;
		boolean property_chains;
		boolean property_chains_trans;
		if(this.c_sub_c || 
				this.c_and_c_sub_c || 
				this.c_sub_exists_p_c || 
				this.exists_p_c_sub_c || 
				this.exists_p_T_sub_c || 
				this.exists_pi_T_sub_c ||
				this.c_dis_c) {
			classes = true;
			individuals = true;
		} else {
			classes = false;
			individuals = false;
		}
		if(this.p_sub_p ||
				this.p_chain_p_sub_p ||
				this.c_sub_exists_p_c ||
				this.exists_p_c_sub_c) {
			properties = true;
		} else {
			properties = false;
		}
		if(this.c_sub_exists_p_c || this.exists_p_c_sub_c) {
			classes_ex_property = true;
		} else {
			classes_ex_property = false;
		}
		if(this.exists_p_T_sub_c || this.exists_pi_T_sub_c) {
			classes_ex_property_top = true;
		} else {
			classes_ex_property_top = false;
		}
		if(this.p_sub_p || this.p_chain_p_sub_p) {
			individual_pairs = true;
		} else {
			individual_pairs = false;
		}
		if(this.p_chain_p_sub_p) {
			individual_pairs_trans = true;
			property_chains = true;
			property_chains_trans = true;
		} else {
			individual_pairs_trans = false;
			property_chains = false;
			property_chains_trans = false;
		}
		if(this.setup.setupSchema(classes, individuals, properties, classes_ex_property, classes_ex_property_top, individual_pairs, individual_pairs_trans, property_chains, property_chains_trans)) {
			return true;
		} else {
			this.setup.removeSchema();
			return false;
		}
	}
	
	@Override
	public boolean terminologyAcquisition() throws SQLException {
		if(this.c_sub_c || 
				this.c_and_c_sub_c || 
				this.c_sub_exists_p_c || 
				this.exists_p_c_sub_c || 
				this.exists_p_T_sub_c || 
				this.exists_pi_T_sub_c ||
				this.c_dis_c) {
			this.terminologyExtractor.initClassesTable();
			this.individualsExtractor.initIndividualsTable();
		}
		if(this.p_sub_p ||
				this.p_chain_p_sub_p ||
				this.c_sub_exists_p_c ||
				this.exists_p_c_sub_c) {
			this.terminologyExtractor.initPropertiesTable();
		}
		if(this.c_sub_exists_p_c || this.exists_p_c_sub_c) {
			this.terminologyExtractor.initClassesExistsPropertyTable();
		}
		if(this.exists_p_T_sub_c || this.exists_pi_T_sub_c) {
			this.terminologyExtractor.initPropertyTopTable();
		}
		if(this.p_sub_p || this.p_chain_p_sub_p) {
			this.individualsExtractor.initIndividualPairsTable();
		}
		if(this.p_chain_p_sub_p) {
			this.terminologyExtractor.initPropertyChainsTable();
			this.terminologyExtractor.initPropertyChainsTransTable();
			this.individualsExtractor.initIndividualPairsTransTable();
		}
		return true;
	}

	@Override
	public boolean connect(String url, String user, String password) {
		try {
			this.database = Database.instance(url, user, password);
			return true;
		} catch (SQLException e) {
			return false;
		}
	}

	@Override
	public void selectAxioms() {
		this.c_sub_c = Settings.getAxiom("c_sub_c");
		this.c_and_c_sub_c = Settings.getAxiom("c_and_c_sub_c");
		this.c_sub_exists_p_c = Settings.getAxiom("c_sub_exists_p_c");
		this.exists_p_c_sub_c = Settings.getAxiom("exists_p_c_sub_c");
		this.exists_p_T_sub_c = Settings.getAxiom("exists_p_T_sub_c");
		this.exists_pi_T_sub_c = Settings.getAxiom("exists_pi_T_sub_c");
		this.p_sub_p = Settings.getAxiom("p_sub_p");
		this.p_chain_p_sub_p = Settings.getAxiom("p_chain_p_sub_p");
		this.c_dis_c = Settings.getAxiom("c_dis_c");
	}

	@Override
	public boolean sparqlSetup(String endpoint, Filter filter, String graph,
			int chunk) {
		this.terminologyExtractor = new TerminologyExtractor(this.database, endpoint, graph, chunk, filter);
		this.individualsExtractor = new IndividualsExtractor(this.database, endpoint, graph, chunk, filter);
		return false;
	}

	@Override
	public void createTransactionTables() throws IOException,
			SQLException {
		if(this.c_sub_c || this.c_and_c_sub_c){
			this.tablePrinter.printClassMembers(Settings.getString("transaction_tables") + transactionTableNames[0] + ".txt");
		}
		if(this.c_sub_exists_p_c || this.exists_p_c_sub_c) {
			this.tablePrinter.printExistsPropertyMembers(Settings.getString("transaction_tables") + transactionTableNames[1] + ".txt", 0);
		}
		if(this.exists_p_T_sub_c) {
			this.tablePrinter.printPropertyRestrictions(Settings.getString("transaction_tables") + transactionTableNames[2] + ".txt", 0);
		}
		if(this.exists_pi_T_sub_c) {
			this.tablePrinter.printPropertyRestrictions(Settings.getString("transaction_tables") + transactionTableNames[3] + ".txt", 1);
		}
		if(this.p_sub_p) {
			this.tablePrinter.printPropertyMembers(Settings.getString("transaction_tables") + transactionTableNames[4] + ".txt");
		}
		if(this.p_chain_p_sub_p){
			this.tablePrinter.printPropertyChainMembersTrans(Settings.getString("transaction_tables") + transactionTableNames[5] + ".txt");
		}
	}
	
	@Override
	public void mineAssociationRules() throws IOException {
		File file = new File(Settings.getString("transaction_tables"));
		File[] files = file.listFiles(new TextFileFilter());
		File ruleFile = new File(Settings.getString("association_rules"));
		File[] ruleFiles = ruleFile.listFiles(new TextFileFilter());
		this.deleteFiles(ruleFiles);
		files = file.listFiles(new TextFileFilter());
		for(File f : files) {
			ruleFiles = ruleFile.listFiles(new TextFileFilter());
			int x = ruleFiles.length;
			int index = f.getName().lastIndexOf(".");
			String exec = Settings.getString("apriori") + 
			"apriori" + 
			" -tr " + 
			f.getPath() + 
			" " +
			Settings.getString("association_rules") + 
			f.getName().substring(0, index) +
			associationRulesSuffix +
			".txt";
			Runtime.getRuntime().exec(exec);
			int y = x;
			while(x == y) {
				y = ruleFile.listFiles(new TextFileFilter()).length;
			}
		}
	}
	
	private void deleteFiles(File[] files) {
		for(File f : files) {
			int index = f.getName().lastIndexOf(".");
			String fileName = f.getName().substring(0, index);
			if(((this.c_sub_c || this.c_and_c_sub_c) && fileName.equals(transactionTableNames[0] + associationRulesSuffix)) ||
					((this.c_sub_exists_p_c || this.exists_p_c_sub_c) && fileName.equals(transactionTableNames[1] + associationRulesSuffix)) ||
					(this.exists_p_T_sub_c && fileName.equals(transactionTableNames[2] + associationRulesSuffix)) ||
					(this.exists_pi_T_sub_c && fileName.equals(transactionTableNames[3] + associationRulesSuffix)) ||
					(this.p_sub_p && fileName.equals(transactionTableNames[4] + associationRulesSuffix)) ||
					(this.p_chain_p_sub_p && fileName.equals(transactionTableNames[5] + associationRulesSuffix)) ||
					(this.c_dis_c && fileName.equals(transactionTableNames[6] + associationRulesSuffix))) {
				f.delete();
			}
		}
	}
	
	@Override
	public void mineAssociationRules(AssociationRulesMiner miner) {
		miner.execute();
	}
	
	@Override
	public List<String> getAssociationRules() throws IOException {
		List<String> rules = new ArrayList<String>();
		File file = new File(Settings.getString("association_rules"));
		File[] files = file.listFiles(new TextFileFilter());
		for(File f : files) {
			BufferedReader in = new BufferedReader(new FileReader(f));
			String line;
			String fileText = new String();
			while((line = in.readLine()) != null) {
				fileText = fileText + line;
			}
			rules.add(fileText);
		}
		return rules;
	}

	@Override
	public HashMap<OWLAxiom, Double> parseAssociationRules() throws IOException, SQLException {
		this.writer = new OntologyWriter(this.database, this.ontology);
		HashMap<OWLAxiom, Double> hmAxioms = new HashMap<OWLAxiom, Double>();
		if(this.c_sub_c){
			File f = new File(Settings.getString("association_rules") + transactionTableNames[0] + associationRulesSuffix + ".txt");
			List<ParsedAxiom> axioms = this.parser.parse(f, false);
			for(ParsedAxiom pa : axioms) {
				OWLAxiom a = this.writer.get_c_sub_c_Axioms(pa.getCons(), pa.getAnte1(), pa.getSupp(), pa.getConf());
				if(a != null) {
					hmAxioms.put(a, pa.getConf());
				}
			}
		}
		if(this.c_and_c_sub_c) {
			File f = new File(Settings.getString("association_rules") + transactionTableNames[0] + associationRulesSuffix + ".txt");
			List<ParsedAxiom> axioms = this.parser.parse(f, true);
			for(ParsedAxiom pa : axioms) {
				OWLAxiom a = this.writer.get_c_and_c_sub_c_Axioms(pa.getAnte1(), pa.getAnte2(), pa.getCons(), pa.getSupp(), pa.getConf());
				if(a != null) {
					hmAxioms.put(a, pa.getConf());
				}
			}
		}
		if(this.c_sub_exists_p_c) {
			File f = new File(Settings.getString("association_rules") + transactionTableNames[1] + associationRulesSuffix + ".txt");
			List<ParsedAxiom> axioms = this.parser.parse(f, false);
			for(ParsedAxiom pa : axioms) {
				OWLAxiom a = this.writer.get_c_sub_exists_p_c_Axioms(pa.getAnte1(), pa.getCons(), pa.getSupp(), pa.getConf());
				if(a != null) {
					hmAxioms.put(a, pa.getConf());
				}
			}
		}
		if(this.exists_p_c_sub_c) {
			File f = new File(Settings.getString("association_rules") + transactionTableNames[1] + associationRulesSuffix + ".txt");
			List<ParsedAxiom> axioms = this.parser.parse(f, false);
			for(ParsedAxiom pa : axioms) {
				OWLAxiom a = this.writer.get_exists_p_c_sub_c_Axioms(pa.getAnte1(), pa.getCons(), pa.getSupp(), pa.getConf());
				if(a != null) {
					hmAxioms.put(a, pa.getConf());
				}
			}
		}
		if(this.exists_p_T_sub_c) {
			File f = new File(Settings.getString("association_rules") + transactionTableNames[2] + associationRulesSuffix + ".txt");
			List<ParsedAxiom> axioms = this.parser.parse(f, false);
			for(ParsedAxiom pa : axioms) {
				OWLAxiom a = this.writer.get_exists_p_T_sub_c_Axioms(pa.getAnte1(), pa.getCons(), pa.getSupp(), pa.getConf());
				if(a != null) {
					hmAxioms.put(a, pa.getConf());
				}
			}
		}
		if(this.exists_pi_T_sub_c) {
			File f = new File(Settings.getString("association_rules") + transactionTableNames[3] + associationRulesSuffix + ".txt");
			List<ParsedAxiom> axioms = this.parser.parse(f, false);
			for(ParsedAxiom pa : axioms) {
				OWLAxiom a = this.writer.get_exists_pi_T_sub_c_Axioms(pa.getAnte1(), pa.getCons(), pa.getSupp(), pa.getConf());
				if(a != null) {
					hmAxioms.put(a, pa.getConf());
				}
			}
		}
		if(this.p_sub_p) {
			File f = new File(Settings.getString("association_rules") + transactionTableNames[4] + associationRulesSuffix + ".txt");
			List<ParsedAxiom> axioms = this.parser.parse(f, false);
			for(ParsedAxiom pa : axioms) {
				OWLAxiom a = this.writer.get_p_sub_p_Axioms(pa.getAnte1(), pa.getCons(), pa.getSupp(), pa.getConf());
				if(a != null) {
					hmAxioms.put(a, pa.getConf());
				}
			}
		}
		if(this.p_chain_p_sub_p) {
			//File f = new File(Settings.getString("association_rules") + transactionTableNames[5] + associationRulesSuffix + ".txt");
			//List<ParsedAxiom> axioms = this.parser.parse(f, false);
			//for(ParsedAxiom pa : axioms) {
				//TODO (does an appropriate method exists!?)
			//}
		}
		if(this.c_dis_c) {
			File f = new File(Settings.getString("association_rules") + transactionTableNames[6] + associationRulesSuffix + ".txt");
			List<ParsedAxiom> axioms = this.parser.parse(f, false);
			for(ParsedAxiom pa : axioms) {
				OWLAxiom a = this.writer.get_c_dis_c_Axioms(pa.getAnte1(), pa.getCons(), pa.getSupp(), pa.getConf());
				if(a != null) {
					hmAxioms.put(a, pa.getConf());
				}
			}
		}
		return hmAxioms;
	}
	
	private void initializeOntology() throws SQLException, OWLOntologyStorageException {
		this.writer = new OntologyWriter(this.database, this.ontology);
		this.ontology = this.writer.writeClassesAndPropertiesToOntology();
		this.ontology.save();
	}

	@Override
	public Ontology createOntology(HashMap<OWLAxiom, Double> axioms,
			double supportThreshold, double confidenceThreshold) throws OWLOntologyStorageException, SQLException {
		//this.initializeOntology();
		this.writer = new OntologyWriter(this.database, this.ontology);
		Ontology o = this.writer.write(axioms, supportThreshold, confidenceThreshold);
		//o.save();
		this.ontology = o;
		return o;
	}

	@Override
	public Ontology greedyDebug(Ontology ontology) throws OWLOntologyStorageException {
		return OntologyDebugger.greedyWrite(ontology);
	}
}
