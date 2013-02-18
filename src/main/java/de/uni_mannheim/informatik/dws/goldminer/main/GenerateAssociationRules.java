package de.uni_mannheim.informatik.dws.goldminer.main;

import de.uni_mannheim.informatik.dws.goldminer.GoldMiner;
import net.sourceforge.argparse4j.inf.Namespace;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;

public class GenerateAssociationRules implements SubcommandModule {
	
	private static GoldMiner goldMiner;

	public void runSubcommand(Namespace namespace) {
		try {
			goldMiner = new GoldMiner();
			goldMiner.setupDatabase();
			goldMiner.terminologyAcquisition();
			goldMiner.createTransactionTables();
			//goldMiner.mineAssociationRules();
			goldMiner.disconnect();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (OWLOntologyCreationException e) {
			e.printStackTrace();
		} catch (OWLOntologyStorageException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
