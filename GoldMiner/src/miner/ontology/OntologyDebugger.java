package miner.ontology;

import java.util.HashMap;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLAxiom;

public abstract class OntologyDebugger {
	
	public static Ontology greedyWrite(Ontology o) {
		Set<OWLAxiom> axiomSet = o.getAxioms();
		for(OWLAxiom a : axiomSet) {
			o.removeAxiom(a);
		}
		HashMap<OWLAxiom, Double> axioms = new HashMap<OWLAxiom, Double>();
		// TODO fill axioms with axiom and corresponding confidence pairs.
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
