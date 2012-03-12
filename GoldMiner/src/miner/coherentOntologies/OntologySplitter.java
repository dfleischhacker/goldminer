package miner.coherentOntologies;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import miner.ontology.Ontology;
import miner.util.Settings;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

public class OntologySplitter {
	
	private int _partitionNumber;
	private Ontology _ontology;
	private File _basisOntology;
	private String _ontologyPath;
	
	public OntologySplitter(int partitionNumber, Ontology ontology) throws FileNotFoundException, IOException {
		if(!Settings.loaded()) {
			Settings.load();
		}
		this._ontologyPath = Settings.getString("ontologyPath");
		this._partitionNumber = partitionNumber;
		this._ontology = ontology;
		this._basisOntology = null;
	}
	
	public OntologySplitter(int partitionNumber, Ontology ontology, File basisOntology) throws FileNotFoundException, IOException {
		this(partitionNumber, ontology);
		this._basisOntology = basisOntology;
	}

	public List<Ontology> getOntologyPartition() throws OWLOntologyCreationException, OWLOntologyStorageException {
		List<Ontology> ontologies = new ArrayList<Ontology>();
		for(int i = 0; i < this._partitionNumber; i++) {
			Ontology ontology = new Ontology();
			ontology.create(new File(this._ontologyPath + "ontology" + this._partitionNumber + ".owl"));
			if(this._basisOntology != null) {
				ontology.load(this._basisOntology);
			}
			ontologies.add(ontology);
		}
		Set<OWLAxiom> axioms = this._ontology.getAxioms();
		for(OWLAxiom axiom : axioms) {
			int r = (int)(Math.random() * this._partitionNumber);
			ontologies.get(r).addAxiom(axiom);
		}
		return ontologies;
	}
}
