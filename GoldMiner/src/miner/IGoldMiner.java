package miner;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import org.semanticweb.owlapi.model.OWLAxiom;

import miner.ontology.Ontology;
import miner.sparql.Filter;

public interface IGoldMiner {

	boolean connect(String url, String user, String password);
	
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
	
	boolean sparqlSetup(String endpoint, Filter filter, String graph, int chunk);
	
	boolean disconnect();
	
	boolean setupDatabase() throws SQLException;
	
	boolean terminologyAcquisition() throws SQLException;
	
	void createTransactionTables(String file) throws IOException, SQLException;
	
	List<OWLAxiom> parseAssociationRules(List<String> associationRules);
	
	Ontology createOntology(List<OWLAxiom> axioms, double supportThreshold, double confidenceThreshold);
	
	Ontology greedyDebug(Ontology ontology);
}
