package de.uni_mannheim.informatik.dws.goldminer;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;

import static org.junit.Assert.assertTrue;


@Ignore
public class AprioriTest {

private GoldMiner goldMiner;
	@Before
	public void init() throws FileNotFoundException, IOException, SQLException, OWLOntologyCreationException, OWLOntologyStorageException {
		this.goldMiner = new GoldMiner();
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
