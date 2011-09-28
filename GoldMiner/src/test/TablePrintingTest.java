package test;

import static org.junit.Assert.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

import miner.IGoldMinerImpl;


public class TablePrintingTest {
	
	private IGoldMinerImpl goldMiner;
	
	@Before
	public void init() throws FileNotFoundException, IOException, SQLException, OWLOntologyCreationException, OWLOntologyStorageException {
		this.goldMiner = new IGoldMinerImpl();
	}
	
	@Test
	public void test() {
		try {
			this.goldMiner.createTransactionTables();
		} catch (IOException e) {
			e.printStackTrace();
			assertTrue(false);
		} catch (SQLException e) {
			e.printStackTrace();
			assertTrue(false);
		}
	}
	
	@After
	public void close() {
		this.goldMiner.disconnect();
	}
}
