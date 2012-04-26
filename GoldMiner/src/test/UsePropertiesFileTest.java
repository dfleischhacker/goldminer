package test;

import static org.junit.Assert.assertTrue;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

import miner.IGoldMinerImpl;


public class UsePropertiesFileTest {

	private IGoldMinerImpl goldMiner;
	
	@Before
	public void connect() throws FileNotFoundException, IOException, SQLException, OWLOntologyCreationException, OWLOntologyStorageException {
			this.goldMiner = new IGoldMinerImpl();
	}
	
	@Test
	public void execute() {
		try {
			assertTrue(this.goldMiner.setupDatabase());
			assertTrue(this.goldMiner.terminologyAcquisition());
			//this.goldMiner.createTransactionTables();
		} catch (SQLException e) {
			e.printStackTrace();
			assertTrue(false);
		}
		//} catch (IOException e) {
			//e.printStackTrace();
			//assertTrue(false);
		//}
	}
	
	@After
	public void disconnect() {
		this.goldMiner.disconnect();
	}
}
