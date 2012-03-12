package miner.coherentOntologies;

import java.util.HashMap;
import java.util.Set;

import miner.ontology.Ontology;

import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;

public class ConfidenceBasedAxiomEraser implements IAxiomEraser {

	@Override
	public Ontology removeIncoherenceCausingAxioms(Ontology ontology,
			HashMap<OWLClass, Set<OWLAxiom>> explanation) {
		
		for(OWLClass clazz : explanation.keySet()) {
			System.out.println(clazz);
			System.out.println("---");
			double minConf = 100;
			OWLAxiom removeAxiom = null;
			for(OWLAxiom axiom : explanation.get(clazz)) {
				System.out.println(axiom.getAnnotations().size());
				for(OWLAnnotation ann : axiom.getAnnotations()) {
					System.out.println("Has annotation");
					if(ann.getProperty().getIRI().toString().split("#")[1].equals("confidence")) {
						System.out.println("INFO: has Confidence Value!");
						double d = Double.parseDouble(ann.getValue().toString().split("\"")[1]);
						if(d < minConf) {
							minConf = d;
							removeAxiom = axiom;
						}
					}
				}
				System.out.println(axiom);
			}
			if(removeAxiom != null) {
				System.out.println("REMOVE: " + removeAxiom);
				System.out.println("CONF: " + minConf);
				System.out.println();
				ontology.removeAxiom(removeAxiom);
			}
			System.out.println();
		}
		return ontology;
	}
}
