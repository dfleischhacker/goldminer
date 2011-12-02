
package miner.ontology;

import com.clarkparsia.pellet.owlapiv3.PelletReasoner;
import com.clarkparsia.pellet.owlapiv3.PelletReasonerFactory;

import miner.util.Settings;

import org.mindswap.pellet.PelletOptions;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;

import java.io.*;
import java.util.*;


public class Ontology {
	
	private IRI m_logicalIRI;
    
    private IRI annotationIRI;
	
	private IRI m_physicalIRI;
	
	OWLOntologyManager m_manager;
	
	OWLOntology m_ontology;
	
	OWLDataFactory m_factory;
	
	PelletReasoner m_reasoner;
	
	
    public Ontology() {
		PelletOptions.USE_COMPLETION_QUEUE = true;
		PelletOptions.USE_INCREMENTAL_CONSISTENCY = true;
		PelletOptions.USE_SMART_RESTORE = false;
		m_manager = OWLManager.createOWLOntologyManager();
		m_factory = m_manager.getOWLDataFactory();
	}
	
	public void create( File file ) throws OWLOntologyCreationException {
		m_logicalIRI = IRI.create( Settings.getString("ontology_logical") );
		m_physicalIRI = IRI.create( file.toURI().toString() );
        annotationIRI = IRI.create(Settings.getString("annotation_iri"));
		//OWLOntologyIRIMapper mapper = new OWLOntologyIRIMapper( m_logicalIRI, m_physicalIRI );
		//m_manager.addIRIMapper( mapper );
		m_ontology = m_manager.createOntology( m_physicalIRI );
		m_reasoner = PelletReasonerFactory.getInstance().createNonBufferingReasoner( m_ontology );
		m_manager.addOntologyChangeListener( m_reasoner );
	}
	
	public void save() throws OWLOntologyStorageException {
		m_manager.saveOntology( m_ontology );
	}
	
	public void load( File file ) throws OWLOntologyCreationException {
		m_ontology = m_manager.loadOntologyFromOntologyDocument( file );
		m_reasoner = PelletReasonerFactory.getInstance().createNonBufferingReasoner( m_ontology );
		m_manager.addOntologyChangeListener( m_reasoner );
	}
	
	public boolean isCoherent() {
		Set<OWLClass> classes = m_ontology.getClassesInSignature();
		for( OWLClass c: classes )
		{
    		if( !isSatisfiable(c) )
			{
    			System.out.println( "unsatisfiable: " + c.toString()  );
				// printExplanation(c);
    			return false;
    		}
    	}
    	return true;
	}
	
	public boolean isSatisfiable( OWLClass c ){
		// OWLAxiom axiom = m_factory.getOWLSubClassOfAxiom( c, m_factory.getOWLNothing() );
		// return m_reasoner.isEntailed( axiom );
		return m_reasoner.isSatisfiable(c);
	}
	
	public String toString(){
		StringBuilder sb = new StringBuilder();
		for( OWLAxiom axiom: m_ontology.getAxioms() ){
            sb.append(axiom.toString()).append("\n");
		}
		return sb.toString();
	}
	
	public boolean entails( OWLAxiom axiom ) throws Exception {
		return m_reasoner.isEntailed( axiom );
	}
	
	public OWLAxiom createDomainAxiom( String sProp, String sClass ) throws Exception {
		OWLObjectProperty p = m_factory.getOWLObjectProperty( IRI.create( sProp ) );
		OWLClass c = m_factory.getOWLClass( IRI.create( sClass ) );
		return m_factory.getOWLObjectPropertyDomainAxiom( p, c );
	}
	
	public OWLAxiom createRangeAxiom( String sProp, String sClass ) throws Exception {
		OWLObjectProperty p = m_factory.getOWLObjectProperty( IRI.create( sProp ) );
		OWLClass c = m_factory.getOWLClass( IRI.create( sClass ) );
		return m_factory.getOWLObjectPropertyRangeAxiom( p, c );
	}
	
	public String getDomain( String sPropURI ) throws Exception {
		OWLObjectProperty prop = m_factory.getOWLObjectProperty( IRI.create( sPropURI ) );
		Set<OWLObjectPropertyDomainAxiom> axioms = m_ontology.getObjectPropertyDomainAxioms( prop );
		StringBuilder sb = new StringBuilder();
		Iterator iter = axioms.iterator();
		while( iter.hasNext() )
		{
			OWLObjectPropertyDomainAxiom axiom = (OWLObjectPropertyDomainAxiom) iter.next();
			OWLClassExpression domain = axiom.getDomain();
			sb.append( domain.asOWLClass().toStringID() );
			if( iter.hasNext() ){
				sb.append( ", " );
			}
		}
		return sb.toString();
	}
	
	public String getRange( String sPropURI ) throws Exception {
		OWLObjectProperty prop = m_factory.getOWLObjectProperty( IRI.create( sPropURI ) );
		Set<OWLObjectPropertyRangeAxiom> axioms = m_ontology.getObjectPropertyRangeAxioms( prop );
		StringBuilder sb = new StringBuilder();
		Iterator iter = axioms.iterator();
		while( iter.hasNext() )
		{
			OWLObjectPropertyRangeAxiom axiom = (OWLObjectPropertyRangeAxiom) iter.next();
			OWLClassExpression range = axiom.getRange();
			sb.append( range.asOWLClass().toStringID() );
			if( iter.hasNext() ){
				sb.append( ", " );
			}
		}
		return sb.toString();
	}
	
	public boolean subsumedBy( String sURI1, String sURI2 ) throws Exception {
		OWLClass c1 = m_factory.getOWLClass( IRI.create( sURI1 ) );
		OWLClass c2 = m_factory.getOWLClass( IRI.create( sURI2 ) );
		OWLAxiom axiom = m_factory.getOWLSubClassOfAxiom( c1, c2 );
		return m_reasoner.isEntailed( axiom );
	}
	
	public void addAxiom( OWLAxiom axiom ) throws OWLOntologyStorageException {
		AddAxiom addAxiom = new AddAxiom( m_ontology, axiom );
		m_manager.applyChange( addAxiom );
		//this.save();
    }
	
	public void removeAxiom( OWLAxiom axiom ) {
		RemoveAxiom removeAxiom = new RemoveAxiom( m_ontology, axiom );
		m_manager.applyChange( removeAxiom );
	}
	
	public OWLAxiom get_c_sub_c_Axiom( String subURI, String superURI, double supp, double conf ){
		if(subURI == null || superURI == null) {
			return null;
		}
		OWLClass c1 = m_factory.getOWLClass( IRI.create( subURI ) );
		OWLClass c2 = m_factory.getOWLClass( IRI.create( superURI ) );
		OWLAnnotation suppAnnotation = this.annotation("support", supp);
		OWLAnnotation confAnnotation = this.annotation("confidence", conf);
		Set<OWLAnnotation> annotations = new HashSet<OWLAnnotation>();
		annotations.add(suppAnnotation);
		annotations.add(confAnnotation);
		
		return m_factory.getOWLSubClassOfAxiom( c1, c2, annotations );
	}
	
	public OWLAxiom get_p_sub_p_Axiom( String subURI, String superURI, double supp, double conf ){
		if(subURI == null || superURI == null) {
			return null;
		}
		OWLObjectProperty c1 = m_factory.getOWLObjectProperty( IRI.create( subURI ) );
		OWLObjectProperty c2 = m_factory.getOWLObjectProperty( IRI.create( superURI ) );
		OWLAnnotation suppAnnotation = this.annotation("support", supp);
		OWLAnnotation confAnnotation = this.annotation("confidence", conf);
		Set<OWLAnnotation> annotations = new HashSet<OWLAnnotation>();
		annotations.add(suppAnnotation);
		annotations.add(confAnnotation);
		return m_factory.getOWLSubObjectPropertyOfAxiom( c1, c2, annotations );
	}
	
	public OWLAxiom get_p_dis_p_Axiom( String subURI, String superURI, double supp, double conf) {
		if(subURI == null || superURI == null) {
			return null;
		}
		OWLObjectProperty p1 = m_factory.getOWLObjectProperty( IRI.create( subURI ) );
		OWLObjectProperty p2 = m_factory.getOWLObjectProperty( IRI.create( superURI ) );
		OWLAnnotation suppAnnotation = this.annotation("support", supp);
		OWLAnnotation confAnnotation = this.annotation("confidence", conf);
		Set<OWLAnnotation> annotations = new HashSet<OWLAnnotation>();
		Set<OWLObjectProperty> properties = new HashSet<OWLObjectProperty>();
		properties.add(p1);
		properties.add(p2);
		annotations.add(suppAnnotation);
		annotations.add(confAnnotation);
		return m_factory.getOWLDisjointObjectPropertiesAxiom( properties, annotations );
	}
	
	public OWLAxiom get_c_and_c_sub_c_Axiom( String subURI1, String subURI2, String superURI, double supp, double conf ){
		if(subURI1 == null || subURI2 == null || superURI == null) {
			return null;
		}
		OWLClass c1 = m_factory.getOWLClass( IRI.create( subURI1 ) );
		OWLClass c2 = m_factory.getOWLClass( IRI.create( subURI2 ) );
		OWLClass c3 = m_factory.getOWLClass( IRI.create( superURI ) );
		OWLAnnotation suppAnnotation = this.annotation("support", supp);
		OWLAnnotation confAnnotation = this.annotation("confidence", conf);
		Set<OWLAnnotation> annotations = new HashSet<OWLAnnotation>();
		annotations.add(suppAnnotation);
		annotations.add(confAnnotation);
		return m_factory.getOWLSubClassOfAxiom( m_factory.getOWLObjectIntersectionOf(c1, c2), c3, annotations );
	}
	
	public OWLAxiom get_c_dis_c_Axiom( String subURI1, String subURI2, double supp, double conf ){
		if(subURI1 == null || subURI2 == null) {
			return null;
		}
		OWLClass c1 = m_factory.getOWLClass( IRI.create( subURI1 ) );
		OWLClass c2 = m_factory.getOWLClass( IRI.create( subURI2 ) );
		OWLAnnotation suppAnnotation = this.annotation("support", supp);
		OWLAnnotation confAnnotation = this.annotation("confidence", conf);
		Set<OWLAnnotation> annotations = new HashSet<OWLAnnotation>();
		annotations.add(suppAnnotation);
		annotations.add(confAnnotation);
		Set<OWLClass> classes = new HashSet<OWLClass>();
		classes.add(c1);
		classes.add(c2);
		return m_factory.getOWLDisjointClassesAxiom(classes, annotations);
	}
	
	public OWLAxiom get_exists_p_c_sub_c_Axiom( String opURI, String classURI1, String classURI2, double supp, double conf  ){
		//SubClassOf(ObjectSomeValuesFrom(1 2) 3)
		if(opURI == null || classURI1 == null || classURI2 == null) {
			return null;
		}
		OWLClass c1 = m_factory.getOWLClass( IRI.create( classURI1 ) );
		OWLClass c2 = m_factory.getOWLClass( IRI.create( classURI2 ) );
		OWLObjectProperty c3 = m_factory.getOWLObjectProperty( IRI.create( opURI ) );
		OWLAnnotation suppAnnotation = this.annotation("support", supp);
		OWLAnnotation confAnnotation = this.annotation("confidence", conf);
		Set<OWLAnnotation> annotations = new HashSet<OWLAnnotation>();
		annotations.add(suppAnnotation);
		annotations.add(confAnnotation);
		return m_factory.getOWLSubClassOfAxiom(m_factory.getOWLObjectSomeValuesFrom(c3, c1), c2, annotations);
	}
	
	public OWLAxiom get_exists_p_T_sub_c_Axiom( String opURI, String classURI, double supp, double conf  ){
		//SubClassOf(ObjectSomeValuesFrom(1 2) 3)
		if(opURI == null || classURI == null) {
			return null;
		}
		OWLClass c1 = m_factory.getOWLClass( IRI.create( classURI ) );
		OWLObjectProperty c2 = m_factory.getOWLObjectProperty( IRI.create( opURI ) );
		OWLAnnotation suppAnnotation = this.annotation("support", supp);
		OWLAnnotation confAnnotation = this.annotation("confidence", conf);
		Set<OWLAnnotation> annotations = new HashSet<OWLAnnotation>();
		annotations.add(suppAnnotation);
		annotations.add(confAnnotation);
		return m_factory.getOWLObjectPropertyDomainAxiom(c2, c1, annotations);
	}
	
	public OWLAxiom get_exists_pi_T_sub_c_Axiom( String opURI, String classURI, double supp, double conf  ){
		if(opURI == null || classURI == null) {
			return null;
		}
		//SubClassOf(ObjectSomeValuesFrom(1 2) 3)
		OWLClass c1 = m_factory.getOWLClass( IRI.create( classURI ) );
		OWLObjectProperty c2 = m_factory.getOWLObjectProperty( IRI.create( opURI ) );
		OWLAnnotation suppAnnotation = this.annotation("support", supp);
		OWLAnnotation confAnnotation = this.annotation("confidence", conf);
		Set<OWLAnnotation> annotations = new HashSet<OWLAnnotation>();
		annotations.add(suppAnnotation);
		annotations.add(confAnnotation);
		return m_factory.getOWLObjectPropertyRangeAxiom(c2, c1, annotations);
	}
	
	public OWLAxiom get_c_sub_exists_p_c_Axiom( String classURI1, String opURI, String classURI2, double supp, double conf  ){
		if(classURI1 == null || opURI == null || classURI2 == null) {
			return null;
		}
		//SubClassOf(ObjectSomeValuesFrom(1 2) 3)
		OWLClass c1 = m_factory.getOWLClass( IRI.create( classURI1 ) );
		OWLClass c2 = m_factory.getOWLClass( IRI.create( classURI2 ) );
		OWLObjectProperty c3 = m_factory.getOWLObjectProperty( IRI.create( opURI ) );
		OWLAnnotation suppAnnotation = this.annotation("support", supp);
		OWLAnnotation confAnnotation = this.annotation("confidence", conf);
		Set<OWLAnnotation> annotations = new HashSet<OWLAnnotation>();
		annotations.add(suppAnnotation);
		annotations.add(confAnnotation);
		return m_factory.getOWLSubClassOfAxiom(c1, m_factory.getOWLObjectSomeValuesFrom(c3, c2), annotations);
	}
	
	public OWLAxiom getPropertyReflexivityAxiom(String uri1, String uri2, double supp, double conf) {
		if(uri1 == null || !uri1.equals("0") || uri2 == null) {
			return null;
		}
		OWLObjectProperty prop = m_factory.getOWLObjectProperty( IRI.create(uri2));
		OWLAnnotation suppAnnotation = this.annotation("support", supp);
		OWLAnnotation confAnnotation = this.annotation("confidence", conf);
		Set<OWLAnnotation> annotations = new HashSet<OWLAnnotation>();
		annotations.add(suppAnnotation);
		annotations.add(confAnnotation);
		return m_factory.getOWLReflexiveObjectPropertyAxiom(prop, annotations);
	}
	
	public OWLAxiom getPropertyIrreflexivityAxiom(String uri1, String uri2, double supp, double conf) {
		if(uri1 == null || !uri1.equals("0") || uri2 == null) {
			return null;
		}
		OWLObjectProperty prop = m_factory.getOWLObjectProperty( IRI.create(uri2));
		OWLAnnotation suppAnnotation = this.annotation("support", supp);
		OWLAnnotation confAnnotation = this.annotation("confidence", conf);
		Set<OWLAnnotation> annotations = new HashSet<OWLAnnotation>();
		annotations.add(suppAnnotation);
		annotations.add(confAnnotation);
		return m_factory.getOWLIrreflexiveObjectPropertyAxiom(prop, annotations);
	}
	
	public OWLAxiom getPropertyInverseAxiom(String uri1, String uri2, double supp, double conf) {
		if(uri1 == null || uri2 == null) {
			return null;
		}
			OWLObjectProperty prop1 = m_factory.getOWLObjectProperty( IRI.create(uri1));
			OWLObjectProperty prop2 = m_factory.getOWLObjectProperty( IRI.create(uri2));
			OWLAnnotation suppAnnotation = this.annotation("support", supp);
			OWLAnnotation confAnnotation = this.annotation("confidence", conf);
			Set<OWLAnnotation> annotations = new HashSet<OWLAnnotation>();
			annotations.add(suppAnnotation);
			annotations.add(confAnnotation);
			if(uri1.equals(uri2)) {
				return m_factory.getOWLSymmetricObjectPropertyAxiom(prop1, annotations);
			} else {
				return m_factory.getOWLInverseObjectPropertiesAxiom(prop1, prop2, annotations);
			}
	}
	
	public OWLAxiom getPropertyAsymmetricAxiom(String uri1, String uri2, double supp, double conf) {
		if(uri1 == null || uri2 == null) {
			return null;
		}
		OWLObjectProperty prop = m_factory.getOWLObjectProperty( IRI.create(uri1));
		OWLAnnotation suppAnnotation = this.annotation("support", supp);
		OWLAnnotation confAnnotation = this.annotation("confidence", conf);
		Set<OWLAnnotation> annotations = new HashSet<OWLAnnotation>();
		annotations.add(suppAnnotation);
		annotations.add(confAnnotation);
		return m_factory.getOWLAsymmetricObjectPropertyAxiom(prop, annotations);
	}
	
	public OWLAxiom getPropertyFunctionalAxiom(String uri1, String uri2, double supp, double conf) {
		if(uri1 == null || uri2 == null || !uri1.equals(uri2)) {
			return null;
		}
		OWLObjectProperty prop = m_factory.getOWLObjectProperty( IRI.create(uri1));
		OWLAnnotation suppAnnotation = this.annotation("support", supp);
		OWLAnnotation confAnnotation = this.annotation("confidence", conf);
		Set<OWLAnnotation> annotations = new HashSet<OWLAnnotation>();
		annotations.add(suppAnnotation);
		annotations.add(confAnnotation);
		return m_factory.getOWLFunctionalObjectPropertyAxiom(prop, annotations);
	}
	
	public OWLAxiom getPropertyInverseFunctionalAxiom(String uri1, String uri2, double supp, double conf) {
		if(uri1 == null || uri2 == null || !uri1.equals(uri2)) {
			return null;
		}
		OWLObjectProperty prop = m_factory.getOWLObjectProperty( IRI.create(uri1));
		OWLAnnotation suppAnnotation = this.annotation("support", supp);
		OWLAnnotation confAnnotation = this.annotation("confidence", conf);
		Set<OWLAnnotation> annotations = new HashSet<OWLAnnotation>();
		annotations.add(suppAnnotation);
		annotations.add(confAnnotation);
		return m_factory.getOWLInverseFunctionalObjectPropertyAxiom(prop, annotations);
	}
	
	public OWLAxiom get_p_chain_q_sub_r_Axiom(List<String> uri1, String uri2, double supp, double conf) {
		if(uri1 == null || uri2 == null) {
			return null;
		}
		List<OWLObjectProperty> propList = new ArrayList<OWLObjectProperty>();
		for(int i = 0; i < uri1.size(); i++) {
				OWLObjectProperty p = m_factory.getOWLObjectProperty(IRI.create(uri1.get(i)));
				propList.add(p);
		}
		OWLObjectProperty prop = m_factory.getOWLObjectProperty(IRI.create(uri2));
		OWLAnnotation suppAnnotation = this.annotation("support", supp);
		OWLAnnotation confAnnotation = this.annotation("confidence", conf);
		Set<OWLAnnotation> annotations = new HashSet<OWLAnnotation>();
		annotations.add(suppAnnotation);
		annotations.add(confAnnotation);
		return m_factory.getOWLSubPropertyChainOfAxiom(propList, prop, annotations);
	}
	
	public OWLAxiom get_p_chain_p_sub_p_Axiom(String uri1, String uri2, double supp, double conf) {
		if(uri1 == null || uri2 == null || !uri1.equals(uri2)) {
			return null;
		}
		OWLObjectProperty prop = m_factory.getOWLObjectProperty(IRI.create(uri2));
		OWLAnnotation suppAnnotation = this.annotation("support", supp);
		OWLAnnotation confAnnotation = this.annotation("confidence", conf);
		Set<OWLAnnotation> annotations = new HashSet<OWLAnnotation>();
		annotations.add(suppAnnotation);
		annotations.add(confAnnotation);
		return m_factory.getOWLTransitiveObjectPropertyAxiom(prop, annotations);
	}
	
	public Set<OWLAxiom> getAxioms() {
		return this.m_ontology.getAxioms();
	}
	
	public void addClass(IRI iri) throws OWLOntologyStorageException {
		OWLClass c = this.m_factory.getOWLClass(iri);
		OWLDeclarationAxiom declarationAxiom = this.m_factory.getOWLDeclarationAxiom(c);
		this.m_manager.addAxiom(this.m_ontology, declarationAxiom);
		System.out.println("Added Class: " + iri.toString());
		this.save();
	}
	
	public void addProperty(IRI iri) throws OWLOntologyStorageException {
		OWLObjectProperty p = this.m_factory.getOWLObjectProperty(iri);
		OWLDeclarationAxiom declarationAxiom = this.m_factory.getOWLDeclarationAxiom(p);
		this.m_manager.addAxiom(this.m_ontology, declarationAxiom);
		System.out.println("Added Property: " + iri.toString());
		this.save();
	}
	
	public void print() {
		System.out.println("Number of classes: " + this.m_ontology.getClassesInSignature().size());
		System.out.println("Number of properties: " + this.m_ontology.getObjectPropertiesInSignature().size());
		System.out.println(this.m_ontology.toString());
	}
	
	public OWLAnnotation annotation( String sAnnotation, double dValue ){
		OWLAnnotationProperty prop = m_factory.getOWLAnnotationProperty( IRI.create( getAnnotationIRI() +"#"+ sAnnotation ) );
		OWLAnnotation annotation = m_factory.getOWLAnnotation( prop, m_factory.getOWLLiteral( dValue ) );
		return annotation;
	}

    public IRI getLogicalIRI() {
        return m_logicalIRI;
    }

    public IRI getAnnotationIRI() {
        return annotationIRI;
    }
}
