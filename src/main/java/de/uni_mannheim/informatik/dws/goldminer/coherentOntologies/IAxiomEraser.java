package de.uni_mannheim.informatik.dws.goldminer.coherentOntologies;

import de.uni_mannheim.informatik.dws.goldminer.ontology.Ontology;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;

import java.util.HashMap;
import java.util.Set;

public interface IAxiomEraser {
	
	public Ontology removeIncoherenceCausingAxioms(Ontology ontology, HashMap<OWLClass, Set<OWLAxiom>> explanation);

}
