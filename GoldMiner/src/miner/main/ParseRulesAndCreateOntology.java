package miner.main;

import miner.IGoldMinerImpl;
import miner.ontology.Ontology;
import miner.ontology.ParsedAxiom;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;

public class ParseRulesAndCreateOntology {

	private static IGoldMinerImpl goldMiner;

	public static void main(String[] args) {
		try {
            double supportThreshold = 0;
            double confidenceThreshold = 0;
            String ontology = null;
            if (args.length == 3) {
                supportThreshold = Double.parseDouble(args[0]);
                confidenceThreshold = Double.parseDouble(args[1]);
                ontology = args[2];
            }
            System.out.println("--------- Support: " + supportThreshold);
            System.out.println("--------- Confidence: " + confidenceThreshold);
            System.out.println("--------- Writing to: " + ontology);
            if (ontology == null) {
                goldMiner = new IGoldMinerImpl();
            }
            else {
                goldMiner = new IGoldMinerImpl(ontology);
            }
            HashMap<OWLAxiom, ParsedAxiom.SupportConfidenceTuple> axioms = goldMiner.parseAssociationRules();
            System.out.println("Total number of axioms: " + axioms.size());
            Ontology o = goldMiner.createOntology(axioms, supportThreshold, confidenceThreshold);
            //o = goldMiner.greedyDebug(o);
            System.out.println("Saving ontology");
            o.save();
            System.out.println("Done saving");
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
