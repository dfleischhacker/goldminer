package de.uni_mannheim.informatik.dws.goldminer.main;

import de.uni_mannheim.informatik.dws.goldminer.GoldMiner;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

import java.io.IOException;
import java.sql.SQLException;

/**
 *
 */
public class WriteAdditionalYagoClassesToDatabase {
    public static void main(String[] args)
            throws SQLException, OWLOntologyCreationException, OWLOntologyStorageException, IOException {
        GoldMiner goldMiner = new GoldMiner();
//        goldMiner.setupDatabase();
//        goldMiner.terminologyAcquisition();
        goldMiner.saveYagoClasses();
        //goldMiner.mineAssociationRules();
        goldMiner.disconnect();
    }
}
