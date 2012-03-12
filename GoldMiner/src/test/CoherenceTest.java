package test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Set;

import miner.coherentOntologies.CoherenceGenerator;
import miner.coherentOntologies.ConfidenceBasedAxiomEraser;
import miner.coherentOntologies.IAxiomEraser;
import miner.coherentOntologies.OntologySplitter;
import miner.ontology.Ontology;

import org.junit.Test;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;


public class CoherenceTest {
	
	@Test
	public void test() {
		OntologySplitter s;
		CoherenceGenerator c;
		try {
			Ontology o = new Ontology();
			//o.load(new File("C:/Users/Jakob/workspace/OntologyTool/ontologies/OntologyTest.owl"));
			o.load(new File("C:/Users/Jakob/desktop/s5c70.owl"));
			s = new OntologySplitter(3, o, new File("C:/Users/Jakob/workspace/OntologyTool/ontologies/OntologyTestBasis.owl"));
			IAxiomEraser e = new ConfidenceBasedAxiomEraser();
			c = new CoherenceGenerator(e);
			if(!c.isSatisfiable(o)) {
				System.out.println("Not coherent!");
				System.out.println("Number of incoherent classes: " + c.numberOfUnsatisfiableClasses(o));
				System.out.println("Make coherent!");
				o = c.makeCoherent(o);
				System.out.println("Done!");
				System.out.println("Number of incoherent classes: " + c.numberOfUnsatisfiableClasses(o));
				System.out.println("-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-");
			}
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}

}
