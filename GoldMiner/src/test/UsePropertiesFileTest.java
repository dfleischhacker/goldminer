package test;

import static org.junit.Assert.assertTrue;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import miner.IGoldMiner;
import miner.IGoldMinerImpl;


public class UsePropertiesFileTest {

	private IGoldMiner goldMiner;
	
	@Before
	public void connect() throws FileNotFoundException, IOException, SQLException, OWLOntologyCreationException {
			this.goldMiner = new IGoldMinerImpl();
	}
	
	@Test
	public void execute() {
		try {
			assertTrue(this.goldMiner.selectAxioms(true, true, true, true, true, true, true, true, false));
			assertTrue(this.goldMiner.setupDatabase());
			assertTrue(this.goldMiner.terminologyAcquisition());
			this.goldMiner.createTransactionTables();
		} catch (SQLException e) {
			assertTrue(false);
			e.printStackTrace();
		} catch (IOException e) {
			assertTrue(false);
			e.printStackTrace();
		}
	}
	
	@After
	public void disconnect() {
		this.goldMiner.disconnect();
	}
}
