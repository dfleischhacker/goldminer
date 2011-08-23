package miner;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.semanticweb.owlapi.model.OWLAxiom;

import miner.database.Database;
import miner.database.IndividualsExtractor;
import miner.database.Setup;
import miner.database.TablePrinter;
import miner.database.TerminologyExtractor;
import miner.ontology.AssociationRulesMiner;
import miner.ontology.Ontology;
import miner.sparql.Filter;
import miner.util.Settings;
import miner.util.TextFileFilter;

public class IGoldMinerImpl implements IGoldMiner {
	
	public IGoldMinerImpl() throws FileNotFoundException, IOException, SQLException {
		if(!Settings.loaded()) {
			Settings.load();
		}
		this.database = Database.instance();
		this.setup = new Setup();
		this.tablePrinter = new TablePrinter();
		this.terminologyExtractor = new TerminologyExtractor();
		this.individualsExtractor = new IndividualsExtractor();
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
	public boolean selectAxioms(
			boolean c_sub_c, 
			boolean c_and_c_sub_c,
			boolean c_sub_exists_p_c, 
			boolean exists_p_c_sub_c,
			boolean exists_p_T_sub_c, 
			boolean exists_pi_T_sub_c,
			boolean p_sub_p, 
			boolean p_chain_p_sub_p, 
			boolean c_dis_c) {
		this.c_sub_c = c_sub_c;
		this.c_and_c_sub_c = c_and_c_sub_c;
		this.c_sub_exists_p_c = c_sub_exists_p_c;
		this.exists_p_c_sub_c = exists_p_c_sub_c;
		this.exists_p_T_sub_c = exists_p_T_sub_c;
		this.exists_pi_T_sub_c = exists_pi_T_sub_c;
		this.p_sub_p = p_sub_p;
		this.p_chain_p_sub_p = p_chain_p_sub_p;
		this.c_dis_c = c_dis_c;
		return true;
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
			this.tablePrinter.printClassMembers(Settings.getString("transaction_tables") + "t1.txt");
		}
		if(this.c_sub_exists_p_c || this.exists_p_c_sub_c) {
			this.tablePrinter.printExistsPropertyMembers(Settings.getString("transaction_tables") + "/t2.txt", 0);
		}
		if(this.exists_p_T_sub_c) {
			this.tablePrinter.printPropertyRestrictions(Settings.getString("transaction_tables") + "/t3.txt", 0);
		}
		if(this.exists_pi_T_sub_c) {
			this.tablePrinter.printPropertyRestrictions(Settings.getString("transaction_tables") + "/t4.txt", 1);
		}
		if(this.p_sub_p) {
			this.tablePrinter.printPropertyMembers(Settings.getString("transaction_tables") + "/t5.txt");
		}
		if(this.p_chain_p_sub_p){
			this.tablePrinter.printPropertyChainMembersTrans(Settings.getString("transaction_tables") + "/t6.txt");
		}
	}
	
	@Override
	public void mineAssociationRules() throws IOException {
		File file = new File(Settings.getString("transaction_tables"));
		File[] files = file.listFiles(new TextFileFilter());
		for(File f : files) {
			int index = f.getName().lastIndexOf(".");
			String exec = Settings.getString("apriori") + 
			"apriori" + 
			" -tr " + 
			f.getPath() + 
			" " +
			Settings.getString("association_rules") + 
			f.getName().substring(0, index) +
			"AR.txt";
			Process process = Runtime.getRuntime().exec(exec);
			InputStream in = process.getInputStream();
			int numLine=0;
			int c;
			StringBuffer sb = new StringBuffer();
			while( ( c = in.read() ) != -1 ) {
				if ((char)c == '\n' ) {
					numLine++;
				}
				sb.append( (char)c );
			}
			in.close();		
			System.out.println("Stream: " + sb.toString());
		}
	}
	
	@Override
	public void mineAssociationRules(AssociationRulesMiner miner) {
		miner.execute();
	}
	
	@Override
	public List<String> getAssociationRules() throws IOException {
		List<String> rules = new ArrayList<String>();
		File[] files = new File[0];
		while(files.length == 0) {
			File file = new File(Settings.getString("association_rules"));
			files = file.listFiles(new TextFileFilter());
		}
		for(File f : files) {
			BufferedReader in = new BufferedReader(new FileReader(f));
			String line;
			String fileText = new String();
			while((line = in.readLine()) != null) {
				fileText = fileText + line;
			}
			rules.add(fileText);
		}
		System.out.println("Groesse: " + rules.size());
		return rules;
	}

	@Override
	public List<OWLAxiom> parseAssociationRules(List<String> associationRules) {
		for(int i = 0; i < associationRules.size(); i++) {
			System.out.println(i + ": " + associationRules.get(i));
		}
		return null;
	}

	@Override
	public Ontology createOntology(List<OWLAxiom> axioms,
			double supportThreshold, double confidenceThreshold) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Ontology greedyDebug(Ontology ontology) {
		// TODO Auto-generated method stub
		return null;
	}

	
}
