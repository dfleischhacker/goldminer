package miner;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import org.semanticweb.owlapi.model.OWLAxiom;

import miner.ontology.AssociationRulesMiner;
import miner.ontology.Ontology;
import miner.sparql.Filter;

/**
 * interface that provides the different methods for the workflow of the system.
 * 
 * @author Jakob
 *
 */
public interface IGoldMiner {

	/**
	 * establishes a connection to the database. Only
	 * necessary if the properties file isn't used.
	 * 
	 * @param url the database url
	 * @param user the username for database access
	 * @param password the password of the user
	 * @return true if connection was established successfully.
	 */
	boolean connect(String url, String user, String password);
	
	/**
	 * determine which types of axioms should be generated for the ontology.
	 * 
	 * @param c_sub_c
	 * @param c_and_c_sub_c
	 * @param c_sub_exists_p_c
	 * @param exists_p_c_sub_c
	 * @param exists_p_T_sub_c
	 * @param exists_pi_T_sub_c
	 * @param p_sub_p
	 * @param p_chain_p_sub_p
	 * @param c_dis_c
	 * @return
	 */
	boolean selectAxioms(boolean c_sub_c, 
			boolean c_and_c_sub_c, 
			boolean c_sub_exists_p_c, 
			boolean exists_p_c_sub_c, 
			boolean exists_p_T_sub_c, 
			boolean exists_pi_T_sub_c, 
			boolean p_sub_p,
			boolean p_chain_p_sub_p,
			boolean c_dis_c
			);
	
	/**
	 * setup of the connection to the sparql endpoint.
	 * 
	 * @param endpoint the url of the endpoint.
	 * @param filter containing filters for classes and individuals.
	 * @param graph
	 * @param chunk max chunk size
	 * @return true if successful.
	 */
	boolean sparqlSetup(String endpoint, Filter filter, String graph, int chunk);
	
	/**
	 * disconnect from database.
	 * 
	 * @return true if successful.
	 */
	boolean disconnect();
	
	/**
	 * generate all necessary tables in the database for terminology acquisition.
	 * Requires selection of axiom types.
	 * 
	 * @return true if successful.
	 * @throws SQLException if there is any problem with the database.
	 */
	boolean setupDatabase() throws SQLException;
	
	/**
	 * starts the acquisition of the terminology (classes, properties, individuals).
	 * Requires setupDatabase.
	 * 
	 * @return true if successful.
	 * @throws SQLException if there is any problem with the database.
	 */
	boolean terminologyAcquisition() throws SQLException;
	
	/**
	 * creates the required transaction tables. Requires terminology acquisition.
	 * writes a .txt file at the specified "transaction_tables" path (properties file).
	 * 
	 * @throws IOException if any problem occurs while writing the file.
	 * @throws SQLException if any database problem occurs.
	 */
	void createTransactionTables() throws IOException, SQLException;
	
	/**
	 * using apriori algorithm for mining the association rules.
	 * requires the transaction tables. Saves the association rules
	 * as txt file at the specified "association_rules" path (prop. file).
	 * 
	 * @throws IOException if there is any problem while writing the file.
	 */
	void mineAssociationRules() throws IOException;
	
	/**
	 * enables the use of another implementation of the apriori algorithm.
	 * this also should generate the association rules as txt file and
	 * write it to the "association_rules" path (prop. file).
	 * 
	 * @param miner the implementation of the AssociationRulesMiner interface.
	 */
	void mineAssociationRules(AssociationRulesMiner miner);
	
	/**
	 * returns the association rules file as a list of strings.
	 * (each string is a file).
	 * 
	 * @return the association rules.
	 * @throws IOException if there is any problem reading the files.
	 */
	List<String> getAssociationRules() throws IOException;
	
	/**
	 * parses the association rules and returns a list of the generated
	 * OWL axioms.
	 * 
	 * @param associationRules the list of association rules files.
	 * @return the list of generated axioms.
	 */
	List<OWLAxiom> parseAssociationRules(List<String> associationRules);
	
	/**
	 * creates an ontology out of the provided OWL axioms. There is no optimization,
	 * eventually existing inconsistencies are not taken into account.
	 * 
	 * @param axioms the axioms for the ontology.
	 * @param supportThreshold only add axioms with a support value equal or greater than this threshold.
	 * @param confidenceThreshold only add axioms with a confidence value equal or greater than this threshold.
	 * @return the ontology generated out of axioms and thresholds.
	 */
	Ontology createOntology(List<OWLAxiom> axioms, double supportThreshold, double confidenceThreshold);
	
	/**
	 * enables debugging of an existing ontology. this method applies the
	 * greedy algorithm. it sorts the axioms in a descending way w.r.t.
	 * their confidence values. than it checks for every axioms, if it causes
	 * an inconsistency when it is added to the ontology. If yes, it is removed
	 * again, else it stays in the ontology.
	 * 
	 * @param ontology the ontology that shell be debugged.
	 * @return the debugged ontology.
	 */
	Ontology greedyDebug(Ontology ontology);
}
