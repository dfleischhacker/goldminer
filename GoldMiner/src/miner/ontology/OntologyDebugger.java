package miner.ontology;

import java.util.HashMap;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

public abstract class OntologyDebugger {
	
	public static Ontology greedyWrite(Ontology o) throws OWLOntologyStorageException {
		Set<OWLAxiom> axiomSet = o.getAxioms();
		HashMap<OWLAxiom, Double> axioms = new HashMap<OWLAxiom, Double>();
		for(OWLAxiom a : axiomSet) {
			o.removeAxiom(a);
			Set<OWLAnnotation> anns = a.getAnnotations();
			for(OWLAnnotation ann : anns) {
				if(ann.getProperty().getIRI().toString().split("#")[1].equals("confidence")) {
					double conf = Double.parseDouble(ann.getValue().toString().split("\"")[1]);
					axioms.put(a, conf);
				}
			}
		}
		//o.save();
		o.print();
		int i=0;
		for( OWLAxiom axiom: axioms.keySet() )
		{
			System.out.println( "add ("+ i +"): "+ axiom );
			o.addAxiom( axiom );
			if( !o.isCoherent() )
			{
				System.out.println( "remove ("+ i +"): "+ axiom );
				o.removeAxiom( axiom );
			}
			i++;
		}
		return o;
	}

}
