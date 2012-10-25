package de.uni_mannheim.informatik.dws.goldminer.main;

import de.uni_mannheim.informatik.dws.goldminer.GoldMiner;
import de.uni_mannheim.informatik.dws.goldminer.ontology.Ontology;
import de.uni_mannheim.informatik.dws.goldminer.util.SupportConfidenceTuple;
import net.sourceforge.argparse4j.inf.Namespace;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;

public class ParseRulesAndCreateOntology implements SubcommandModule{

	private static GoldMiner goldMiner;

    @Override
	public void runSubcommand(Namespace namespace) {
		try {
            double supportThreshold = namespace.getDouble("support");
            double confidenceThreshold = namespace.getDouble("confidence");
            String ontology = namespace.getString("ontology");

            System.out.println("--------- Support: " + supportThreshold);
            System.out.println("--------- Confidence: " + confidenceThreshold);
            System.out.println("--------- Writing to: " + ontology);
            if (ontology.equals("")) {
                goldMiner = new GoldMiner();
            }
            else {
                goldMiner = new GoldMiner(ontology);
            }
            HashMap<OWLAxiom, SupportConfidenceTuple> axioms = goldMiner.parseAssociationRules();
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
