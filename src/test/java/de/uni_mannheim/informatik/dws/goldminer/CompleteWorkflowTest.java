package de.uni_mannheim.informatik.dws.goldminer;

import de.uni_mannheim.informatik.dws.goldminer.ontology.Ontology;
import de.uni_mannheim.informatik.dws.goldminer.util.SupportConfidenceTuple;
import org.junit.Ignore;
import org.junit.Test;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;

@Ignore
public class CompleteWorkflowTest {
	
	private GoldMiner goldMiner;
	
	/**
	 * Requires complete and correct miner.properties and axioms.properties.
	 */
	@Test
	public void runCompleteWorkflow() throws FileNotFoundException, OWLOntologyCreationException, OWLOntologyStorageException, IOException, SQLException {
		//instanciate the GoldMiner
		this.goldMiner = new GoldMiner();
		
		//Create the required database structure at the specified database
		this.goldMiner.setupDatabase();
		
		//get the terminology from the RDF resource and write it into the database.
		this.goldMiner.terminologyAcquisition();
		
		//create the transaction tables that are needed for association rules mining.
		this.goldMiner.createTransactionTables();
		
		//Create the association rules files
		this.goldMiner.mineAssociationRules();
		
		//parse the association rules and transform them to OWLAxioms
		HashMap<OWLAxiom,SupportConfidenceTuple> axioms = this.goldMiner.parseAssociationRules();
		
		//Create an ontology that contains all the axioms that have been parsed.
		//You can specify support and confidence thresholds.
		Ontology o = this.goldMiner.createOntology(axioms, 0.0, 0.0);
		
		//An ontology can also be debugged -> transformed into a coherent and satisfiable ontology.
		o = this.goldMiner.greedyDebug(o);
		o.save();
		
		//At the end, shut down all connections.
		this.goldMiner.disconnect();
	}

}
