package miner.coherentOntologies;

import java.util.HashMap;
import java.util.Set;

import miner.ontology.Ontology;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;

public interface IAxiomEraser {
	
	public Ontology removeIncoherenceCausingAxioms(Ontology ontology, HashMap<OWLClass, Set<OWLAxiom>> explanation);

}
