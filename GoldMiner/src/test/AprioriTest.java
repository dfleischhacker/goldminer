package test;

import static org.junit.Assert.assertTrue;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;

import miner.IGoldMinerImpl;
import miner.ontology.Ontology;

import org.junit.Before;
import org.junit.Test;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;


public class AprioriTest {

private IGoldMinerImpl goldMiner;
	
	@Before
	public void init() throws FileNotFoundException, IOException, SQLException, OWLOntologyCreationException, OWLOntologyStorageException {
		this.goldMiner = new IGoldMinerImpl();
		System.out.println("Done!");
	}
	
	@Test
	public void test() {
		try {
			this.goldMiner.mineAssociationRules();
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(false);
		}
	}
}
