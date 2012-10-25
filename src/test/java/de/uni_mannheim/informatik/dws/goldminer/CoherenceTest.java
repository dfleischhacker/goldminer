package de.uni_mannheim.informatik.dws.goldminer;

import de.uni_mannheim.informatik.dws.goldminer.coherentOntologies.CoherenceGenerator;
import de.uni_mannheim.informatik.dws.goldminer.coherentOntologies.ConfidenceBasedAxiomEraser;
import de.uni_mannheim.informatik.dws.goldminer.coherentOntologies.IAxiomEraser;
import de.uni_mannheim.informatik.dws.goldminer.coherentOntologies.OntologySplitter;
import de.uni_mannheim.informatik.dws.goldminer.ontology.Ontology;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;

@Ignore
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
