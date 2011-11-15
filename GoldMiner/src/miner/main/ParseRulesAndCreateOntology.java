package miner.main;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;

import miner.IGoldMiner;
import miner.IGoldMinerImpl;
import miner.ontology.Ontology;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

public class ParseRulesAndCreateOntology {

	private static IGoldMiner goldMiner;

	public static void main(String[] args) {
		try {
			goldMiner = new IGoldMinerImpl();
			HashMap<OWLAxiom, Double> axioms = goldMiner.parseAssociationRules();
			System.out.println("Anzahl Axiome: " + axioms.size());
			Ontology o = goldMiner.createOntology(axioms, 0.0, 0.0);
			o = goldMiner.greedyDebug(o);
			o.save();
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
