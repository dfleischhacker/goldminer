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
			HashMap<OWLAxiom, Double> axioms = this.goldMiner.parseAssociationRules();
			System.out.println("Anzahl Axiome: " + axioms.size());
			Ontology o = this.goldMiner.createOntology(axioms, 0.0, 0.0);
			o = this.goldMiner.greedyDebug(o);
			o.save();
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(false);
		}
	}
}
