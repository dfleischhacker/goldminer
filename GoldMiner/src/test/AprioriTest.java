package test;

import static org.junit.Assert.assertTrue;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import miner.IGoldMinerImpl;

import org.junit.Before;
import org.junit.Test;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;


public class AprioriTest {

private IGoldMinerImpl goldMiner;
	
	@Before
	public void init() throws FileNotFoundException, IOException, SQLException, OWLOntologyCreationException {
		this.goldMiner = new IGoldMinerImpl();
		System.out.println("Done!");
		this.goldMiner.selectAxioms(true, true, true, true, true, true, true, true, false);
	}
	
	@Test
	public void test() {
		try {
			this.goldMiner.mineAssociationRules();
			List<String> rules = this.goldMiner.getAssociationRules();
			this.goldMiner.parseAssociationRules(rules);
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(false);
		}
	}
}
