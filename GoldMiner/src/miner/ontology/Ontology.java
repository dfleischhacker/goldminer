
package miner.ontology;

import com.clarkparsia.pellet.owlapiv3.PelletReasoner;
import com.clarkparsia.pellet.owlapiv3.PelletReasonerFactory;
import org.semanticweb.owlapi.reasoner.ClassExpressionNotInProfileException;
import org.mindswap.pellet.PelletOptions;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.util.SimpleIRIMapper;

import java.io.*;
import java.net.*;
import java.util.*;


public class Ontology {
	
	private IRI m_logicalIRI;
	
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
		m_logicalIRI = IRI.create( "http://uni-mannheim.de/ontologies/ontology" );
		m_physicalIRI = IRI.create( file.toURI().toString() );
		//OWLOntologyIRIMapper mapper = new OWLOntologyIRIMapper( m_logicalIRI, m_physicalIRI );
		//m_manager.addIRIMapper( mapper );
		m_ontology = m_manager.createOntology( m_physicalIRI );
		m_reasoner = PelletReasonerFactory.getInstance().createNonBufferingReasoner( m_ontology );
		m_manager.addOntologyChangeListener( m_reasoner );
	}
	
	public void save() throws OWLOntologyStorageException {
		m_manager.saveOntology( m_ontology );
	}
	
	public void load( File file ) throws Exception {
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
		StringBuffer sb = new StringBuffer();
		for( OWLAxiom axiom: m_ontology.getAxioms() ){
			sb.append( axiom.toString() +"\n" );
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
		StringBuffer sb = new StringBuffer();
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
		StringBuffer sb = new StringBuffer();
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
	
	public void addAxiom( OWLAxiom axiom ) {
		AddAxiom addAxiom = new AddAxiom( m_ontology, axiom );
		m_manager.applyChange( addAxiom );
    }
	
	public void removeAxiom( OWLAxiom axiom ) {
		RemoveAxiom removeAxiom = new RemoveAxiom( m_ontology, axiom );
		m_manager.applyChange( removeAxiom );
	}
	
	public OWLAxiom get_c_sub_c_Axiom( String subURI, String superURI ){
		OWLClass c1 = m_factory.getOWLClass( IRI.create( subURI ) );
		OWLClass c2 = m_factory.getOWLClass( IRI.create( superURI ) );
		return m_factory.getOWLSubClassOfAxiom( c1, c2 );
	}
	
	public OWLAxiom get_p_sub_p_Axiom( String subURI, String superURI ){
		OWLObjectProperty c1 = m_factory.getOWLObjectProperty( IRI.create( subURI ) );
		OWLObjectProperty c2 = m_factory.getOWLObjectProperty( IRI.create( superURI ) );
		return m_factory.getOWLSubObjectPropertyOfAxiom( c1, c2 );
	}
	
	public OWLAxiom get_c_and_c_sub_c_Axiom( String subURI1, String subURI2, String superURI ){
		OWLClass c1 = m_factory.getOWLClass( IRI.create( subURI1 ) );
		OWLClass c2 = m_factory.getOWLClass( IRI.create( subURI2 ) );
		OWLClass c3 = m_factory.getOWLClass( IRI.create( superURI ) );
		return m_factory.getOWLSubClassOfAxiom( m_factory.getOWLObjectIntersectionOf(c1, c2), c3 );
	}
	
	public OWLAxiom get_c_dis_c_Axiom( String subURI1, String subURI2 ){
		OWLClass c1 = m_factory.getOWLClass( IRI.create( subURI1 ) );
		OWLClass c2 = m_factory.getOWLClass( IRI.create( subURI2 ) );
		return m_factory.getOWLDisjointClassesAxiom(c1, c2);
	}
	
	public OWLAxiom get_exists_p_c_sub_c_Axiom( String opURI, String classURI1, String classURI2  ){
		//SubClassOf(ObjectSomeValuesFrom(1 2) 3)
		OWLClass c1 = m_factory.getOWLClass( IRI.create( classURI1 ) );
		OWLClass c2 = m_factory.getOWLClass( IRI.create( classURI2 ) );
		OWLObjectProperty c3 = m_factory.getOWLObjectProperty( IRI.create( opURI ) );
		return m_factory.getOWLSubClassOfAxiom(m_factory.getOWLObjectSomeValuesFrom(c3, c1), c2);
	}
	
	public OWLAxiom get_exists_p_T_sub_c_Axiom( String opURI, String classURI  ){
		//SubClassOf(ObjectSomeValuesFrom(1 2) 3)
		OWLClass c1 = m_factory.getOWLClass( IRI.create( classURI ) );
		OWLObjectProperty c2 = m_factory.getOWLObjectProperty( IRI.create( opURI ) );
		return m_factory.getOWLObjectPropertyDomainAxiom(c2, c1);
	}
	
	public OWLAxiom get_exists_pi_T_sub_c_Axiom( String opURI, String classURI  ){
		//SubClassOf(ObjectSomeValuesFrom(1 2) 3)
		OWLClass c1 = m_factory.getOWLClass( IRI.create( classURI ) );
		OWLObjectProperty c2 = m_factory.getOWLObjectProperty( IRI.create( opURI ) );
		return m_factory.getOWLObjectPropertyRangeAxiom(c2, c1);
	}
	
	public OWLAxiom get_c_sub_exists_p_c_Axiom( String classURI1, String opURI, String classURI2  ){
		//SubClassOf(ObjectSomeValuesFrom(1 2) 3)
		OWLClass c1 = m_factory.getOWLClass( IRI.create( classURI1 ) );
		OWLClass c2 = m_factory.getOWLClass( IRI.create( classURI2 ) );
		OWLObjectProperty c3 = m_factory.getOWLObjectProperty( IRI.create( opURI ) );
		return m_factory.getOWLSubClassOfAxiom(c1, m_factory.getOWLObjectSomeValuesFrom(c3, c2));
	}
	
	public Set<OWLAxiom> getAxioms() {
		return this.m_ontology.getAxioms();
	}
}
