
package miner.ontology;

import java.io.*;
import java.util.*;

import miner.util.*;
import miner.database.*;

import java.sql.ResultSet;

import org.semanticweb.owlapi.model.*;


public class ResultSetup {
	
	private Ontology m_ontology;
	
	private Database m_database;
	
	private SQLFactory m_sqlFactory;
	
	
	
	
	public ResultSetup(Database d) throws Exception {
		m_ontology = new Ontology();
		String sDir = Settings.getString( Parameter.MINER_HOME );
		m_ontology.load( new File( "ontology_nosupp.owl" ));
		m_sqlFactory = new SQLFactory();
		m_database = d;
	}
	
	private HashMap<Integer,String> readClasses() throws Exception {
		// read class URIs from database
		HashMap<Integer,String> classes = new HashMap<Integer,String>();
		String sQuery = m_sqlFactory.selectClassesQuery();
		ResultSet results = m_database.query( sQuery );
		while( results.next() )
		{
			Integer id = results.getInt( "id" );
			String uri = results.getString( "uri" );
			classes.put( id, uri );
		}
		return classes;
	}
	
	private HashMap<Integer,String> readProperties() throws Exception {
		// read property URIs from database
		HashMap<Integer,String> props = new HashMap<Integer,String>();
		String sQuery = m_sqlFactory.selectPropertiesQuery();
		ResultSet results = m_database.query( sQuery );
		while( results.next() )
		{
			Integer id = results.getInt( "id" );
			String uri = results.getString( "uri" );
			props.put( id, uri );
		}
		return props;
	}
	
	private void initGoldPropTables() throws Exception {
		HashMap<Integer,String> props = readProperties();
		HashMap<Integer,String> classes = readClasses();
		for( Integer p: props.keySet() )
		{
			String sProp = props.get(p);
			for( Integer c: classes.keySet() )
			{
				String sClass = classes.get(c);
				System.out.println( p +"="+sProp +" / "+ c +"="+ sClass );
				OWLAxiom axiom1 = m_ontology.createDomainAxiom( sProp, sClass );
				OWLAxiom axiom2 = m_ontology.createRangeAxiom( sProp, sClass );
				if( m_ontology.entails( axiom1 ) ){
					// TODO: 1 (exists_p_top_sub_c)
					m_database.execute( m_sqlFactory.insertResultPropDomainQuery( p, c, 1 ) );
					System.out.println( "domain: "+ axiom1.toString() );
				}
				else {
					// TODO: 0 (exists_p_top_sub_c)
					m_database.execute( m_sqlFactory.insertResultPropDomainQuery( p, c, 0 ) );
				}
				if( m_ontology.entails( axiom2 ) ){
					// TODO: 1 (exists_pi_top_sub_c)
					m_database.execute( m_sqlFactory.insertResultPropRangeQuery( p, c, 1 ) );
					System.out.println( "range: "+ axiom2.toString() );
				}
				else {
					// TODO: 0 (exists_pi_top_sub_c)
					m_database.execute( m_sqlFactory.insertResultPropRangeQuery( p, c, 0 ) );
				}
			}
		}
		System.out.println( "done" );
	}
	
	private void initGoldPropTable() throws Exception {
		// read domain and range restrictions from ontology
		HashMap<Integer,String> props = readProperties();
		for( Integer p: props.keySet() )
		{
			String sProp = props.get(p);
			String sDomain = m_ontology.getDomain( sProp );
			String sRange = m_ontology.getRange( sProp );
			System.out.println( "initGoldPropTable( "+ sProp +" ): domain="+ sDomain +" range="+ sRange );
			int iDomain = getClassID( sDomain );
			int iRange = getClassID( sRange );
			String sUpdate = m_sqlFactory.insertResultPropAxiomQuery( p, iDomain, iRange );
			m_database.execute( sUpdate );
		}
		System.out.println( "GoldSetup.initGoldPropTable: done" );
	}
	
	private void initGoldSubTable() throws Exception {
		// read c_sub_c axioms from ontology
		HashMap<Integer,String> classes = readClasses();
		for( Integer c1: classes.keySet() )
		{
			for( Integer c2: classes.keySet() )
			{
				if( c1 == c2 ){
					continue;
				}
				String s1 = classes.get( c1 );
				String s2 = classes.get( c2 );
				int iSub = m_ontology.subsumedBy( s1, s2 ) ? 1 : 0;
				// ante (column 2) subclassof cons (column 1)
				String sUpdate = m_sqlFactory.insertResultSubAxiomQuery( c2, c1, iSub );
				m_database.execute( sUpdate );
				if( iSub == 1 ){
					System.out.println( s1 +" subclassof "+ s2 );
				}
			}
		}
		System.out.println( "GoldSetup.initGoldSubTable: done" );
	}
	
	public int getClassID( String sURI ) throws Exception {
		String sQuery = m_sqlFactory.selectClassIDQuery( sURI );
		ResultSet results = m_database.query( sQuery );
		if( results.next() ){
			return results.getInt( "id" );
		}
		return -1;
	}
}
