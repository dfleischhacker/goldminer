package de.uni_mannheim.informatik.dws.goldminer.coherentOntologies;

import aterm.ATerm;
import aterm.ATermAppl;
import aterm.ATermList;
import com.clarkparsia.pellet.owlapiv3.AxiomConverter;
import com.clarkparsia.pellet.owlapiv3.PelletReasoner;
import de.uni_mannheim.informatik.dws.goldminer.ontology.Ontology;
import org.mindswap.pellet.PelletOptions;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLRuntimeException;
import org.semanticweb.owlapi.reasoner.BufferingMode;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class CoherenceGenerator {
	
	private IAxiomEraser _eraser;
	
	public CoherenceGenerator(IAxiomEraser eraser) {
		this._eraser = eraser;
	}
	
	public int numberOfUnsatisfiableClasses(Ontology ontology) {
		PelletOptions.USE_TRACING = true;
		PelletReasoner reasoner = new PelletReasoner(ontology.getOntology(), BufferingMode.BUFFERING);
		int count = 0;
		for(OWLClass clazz : ontology.getOntology().getClassesInSignature()) {
			if(!reasoner.isSatisfiable(clazz)) {
				count++;
			}
		}
		return count;
	}
	
	public boolean isSatisfiable(Ontology ontology) {
		PelletOptions.USE_TRACING = true;
		PelletReasoner reasoner = new PelletReasoner(ontology.getOntology(), BufferingMode.BUFFERING);
		for(OWLClass clazz : ontology.getOntology().getClassesInSignature()) {
			if(!reasoner.isSatisfiable(clazz)) {
				return false;
			}
		}
		return true;
	}
	
	public Ontology makeCoherent(Ontology ontology) {
		HashMap<OWLClass, Set<OWLAxiom>> explanationMap = new HashMap<OWLClass, Set<OWLAxiom>>();
		PelletOptions.USE_TRACING = true;
		PelletReasoner reasoner = new PelletReasoner(ontology.getOntology(), BufferingMode.BUFFERING);
		AxiomConverter converter = new AxiomConverter(reasoner);
		reasoner.getKB().setDoExplanation(true);
		Set<OWLClass> classes = ontology.getOntology().getClassesInSignature();
		for(OWLClass clazz : classes) {
			if(!reasoner.isSatisfiable(clazz)) {
				Set<OWLAxiom> explanations = this.convertExplanation(converter, reasoner.getKB().getExplanationSet());
				explanationMap.put(clazz, explanations);
			}
		}
		ontology = this._eraser.removeIncoherenceCausingAxioms(ontology, explanationMap);
		return ontology;
	}
	
	private Set<OWLAxiom> convertExplanation(AxiomConverter converter,
			Set<ATermAppl> explanation) {
		if( explanation == null || explanation.isEmpty() )
			throw new OWLRuntimeException( "No explanation computed" );

		Set<OWLAxiom> result = new HashSet<OWLAxiom>();

		for( ATermAppl c : explanation ) {
			ATermList list = c.getAnnotations();
			System.out.println("ATermAppl: " + c.toString());
			for(int i = 0; i < list.getLength(); i++) {
				ATerm a = list.elementAt(i);
				System.out.println(a.toString());
			}
			OWLAxiom axiom = converter.convert( c );
			if( axiom == null )
				throw new OWLRuntimeException( "Cannot convert: " + c );
			result.add( axiom );
		}

		return result;
	}

}
