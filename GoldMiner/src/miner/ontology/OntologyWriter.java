package miner.ontology;

import java.io.File;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import miner.database.Database;
import miner.database.SQLFactory;

import org.semanticweb.owlapi.model.OWLAxiom;

public class OntologyWriter {
	
	private Ontology m_ontology;
	
	private Database m_database;
	
	private SQLFactory m_sqlFactory;
	
	public OntologyWriter(Database d, String file) throws Exception {
		m_ontology = new Ontology();
		m_ontology.create( new File( file ) );
		m_sqlFactory = new SQLFactory();
		m_database = d;
	}
	
	public void write() throws Exception {
		List<OWLAxiom> axioms = getAxioms();
		System.out.println( "axioms: "+ axioms.size() );
		int i=0;
		for( OWLAxiom axiom: axioms )
		{
			System.out.println( "add ("+ i +"): "+ axiom );
			m_ontology.addAxiom( axiom );
			//if( !m_ontology.isCoherent() )
			//{
				//System.out.println( "remove ("+ i +"): "+ axiom );
				//m_ontology.removeAxiom( axiom );
			//}
			i++;
		} 
		m_ontology.save();
	}
	
	private List<OWLAxiom> getAxioms() throws Exception {
		HashMap<OWLAxiom,Double> hmAxioms = new HashMap<OWLAxiom,Double>();
		hmAxioms.putAll( get_c_sub_c_Axioms() );
		hmAxioms.putAll( get_c_and_c_sub_c_Axioms() );
		hmAxioms.putAll( get_c_dis_c_Axioms() );
		hmAxioms.putAll( get_exists_p_c_sub_c_Axioms() );
		hmAxioms.putAll( get_c_sub_exists_p_c_Axioms() );
		hmAxioms.putAll( get_p_sub_p_Axioms() );
		hmAxioms.putAll( get_exists_p_T_sub_c_Axioms() );
		hmAxioms.putAll( get_exists_pi_T_sub_c_Axioms() );

		//Collections.sort(hmAxioms);
		return sort( hmAxioms );
	}
	
	private HashMap<OWLAxiom,Double> get_c_sub_c_Axioms() throws Exception {
		// assumption: only one confidence value per axiom
		HashMap<OWLAxiom,Double> hmAxioms = new HashMap<OWLAxiom,Double>();
		String sQuery = m_sqlFactory.select_c_sub_c_AxiomQuery();
		ResultSet results = m_database.query( sQuery );
		while( results.next() )
		{
			int iCons = results.getInt( "cons" );
			int iAnte = results.getInt( "ante" );
			double dSupp = results.getDouble( "supp" );
			double dConf = results.getDouble( "conf" )/100.0;//normalize
			OWLAxiom axiom = m_ontology.get_c_sub_c_Axiom( getClassURI( iAnte ), getClassURI( iCons ) );
			hmAxioms.put( axiom, dConf );
		}
		return hmAxioms;
	}
	 
	 private HashMap<OWLAxiom,Double> get_c_dis_c_Axioms() throws Exception {
			// assumption: only one confidence value per axiom
			HashMap<OWLAxiom,Double> hmAxioms = new HashMap<OWLAxiom,Double>();
			String sQuery = m_sqlFactory.select_c_dis_c_AxiomQuery();
			ResultSet results = m_database.query( sQuery );
			while( results.next() )
			{
				int iCons = results.getInt( "cons" );
				int iAnte = results.getInt( "ante" );
				//double dSupp = results.getDouble( "supp" );
				double dConf = results.getDouble( "conf" );
				OWLAxiom axiom = m_ontology.get_c_dis_c_Axiom( getClassURI( iAnte ), getClassURI( iCons ) );
				hmAxioms.put( axiom, dConf );
			}
			return hmAxioms;
		}
	 
	 private HashMap<OWLAxiom,Double> get_p_sub_p_Axioms() throws Exception {
			// assumption: only one confidence value per axiom
			HashMap<OWLAxiom,Double> hmAxioms = new HashMap<OWLAxiom,Double>();
			String sQuery = m_sqlFactory.select_p_sub_p_AxiomQuery();
			ResultSet results = m_database.query( sQuery );
			while( results.next() )
			{
				int iCons = results.getInt( "cons" );
				int iAnte = results.getInt( "ante" );
				double dSupp = results.getDouble( "supp" );
				double dConf = results.getDouble( "conf" )/100.0;//normalize
				OWLAxiom axiom = m_ontology.get_p_sub_p_Axiom( getPropertyURI( iAnte ), getPropertyURI( iCons ) );
				hmAxioms.put( axiom, dConf );
			}
			return hmAxioms;
		}
	 
	 private HashMap<OWLAxiom,Double> get_c_and_c_sub_c_Axioms() throws Exception {
			// assumption: only one confidence value per axiom
			HashMap<OWLAxiom,Double> hmAxioms = new HashMap<OWLAxiom,Double>();
			String sQuery = m_sqlFactory.select_c_and_c_sub_c_AxiomQuery();
			ResultSet results = m_database.query( sQuery );
			while( results.next() )
			{
				int iCons = results.getInt( "cons" );
				int iAnte1 = results.getInt( "ante1" );
				int iAnte2 = results.getInt( "ante2" );
				double dSupp = results.getDouble( "supp" );
				double dConf = results.getDouble( "conf" )/100.0;//normalize
				OWLAxiom axiom = m_ontology.get_c_and_c_sub_c_Axiom( getClassURI( iAnte1 ), getClassURI(iAnte2), getClassURI( iCons ) );
				hmAxioms.put( axiom, dConf );
			}
			return hmAxioms;
	}
	 
	 private HashMap<OWLAxiom,Double> get_exists_p_c_sub_c_Axioms() throws Exception {
			// assumption: only one confidence value per axiom
			HashMap<OWLAxiom,Double> hmAxioms = new HashMap<OWLAxiom,Double>();
			String sQuery = m_sqlFactory.select_exists_p_c_sub_c_AxiomQuery();
			ResultSet results = m_database.query( sQuery );
			while( results.next() )
			{
				int iPropExists = results.getInt( "ante" );
				int iCons = results.getInt( "cons" );
				double dConf = results.getDouble( "conf" )/100.0;//normalize
				OWLAxiom axiom = m_ontology.get_exists_p_c_sub_c_Axiom( getPropertyURIFromExistsProperty(iPropExists), getClassURIFromExistsProperty(iPropExists), getClassURI( iCons ) );
				hmAxioms.put( axiom, dConf );
			}
			return hmAxioms;
	}
	 
	 private HashMap<OWLAxiom,Double> get_exists_p_T_sub_c_Axioms() throws Exception {
			// assumption: only one confidence value per axiom
			HashMap<OWLAxiom,Double> hmAxioms = new HashMap<OWLAxiom,Double>();
			String sQuery = m_sqlFactory.select_exists_p_T_sub_c_AxiomQuery();
			ResultSet results = m_database.query( sQuery );
			while( results.next() )
			{
				int iPropExists = results.getInt( "ante" );
				int iCons = results.getInt( "cons" );
				double dConf = results.getDouble( "conf" )/100.0;//normalize
				OWLAxiom axiom = m_ontology.get_exists_p_T_sub_c_Axiom( getPropertyURIFromExistsPropertyTop(iPropExists), getClassURI( iCons ) );
				hmAxioms.put( axiom, dConf );
			}
			return hmAxioms;
	}
	 
	 private HashMap<OWLAxiom,Double> get_exists_pi_T_sub_c_Axioms() throws Exception {
			// assumption: only one confidence value per axiom
			HashMap<OWLAxiom,Double> hmAxioms = new HashMap<OWLAxiom,Double>();
			String sQuery = m_sqlFactory.select_exists_pi_T_sub_c_AxiomQuery();
			ResultSet results = m_database.query( sQuery );
			while( results.next() )
			{
				int iPropExists = results.getInt( "ante" );
				int iCons = results.getInt( "cons" );
				double dConf = results.getDouble( "conf" )/100.0;//normalize
				OWLAxiom axiom = m_ontology.get_exists_pi_T_sub_c_Axiom( getPropertyURIFromExistsPropertyTop(iPropExists), getClassURI( iCons ) );
				hmAxioms.put( axiom, dConf );
			}
			return hmAxioms;
	}
	 
	 private HashMap<OWLAxiom,Double> get_c_sub_exists_p_c_Axioms() throws Exception {
			// assumption: only one confidence value per axiom
			HashMap<OWLAxiom,Double> hmAxioms = new HashMap<OWLAxiom,Double>();
			String sQuery = m_sqlFactory.select_c_sub_exists_p_c_AxiomQuery();
			ResultSet results = m_database.query( sQuery );
			while( results.next() )
			{
				int iPropExists = results.getInt( "cons" );
				int iAnte = results.getInt( "ante" );
				double dConf = results.getDouble( "conf" )/100.0;//normalize
				OWLAxiom axiom = m_ontology.get_c_sub_exists_p_c_Axiom(getClassURI( iAnte ), getPropertyURIFromExistsProperty(iPropExists), getClassURIFromExistsProperty(iPropExists));
				hmAxioms.put( axiom, dConf );
			}
			return hmAxioms;
	}
	
	private List<OWLAxiom> sort( HashMap<OWLAxiom,Double> hmAxioms ){
		List<OWLAxiom> axioms = new ArrayList<OWLAxiom>( hmAxioms.keySet() );
		Collections.sort( axioms, new AxiomComparator( hmAxioms ) );
		return axioms;
	}
	
	public class AxiomComparator implements Comparator<OWLAxiom> 
	{
		private HashMap<OWLAxiom,Double> hmAxioms;
		
		public AxiomComparator( HashMap<OWLAxiom,Double> hmAxioms ){
			this.hmAxioms = hmAxioms;
		}
		// TODO: ascending or descending order
		public int compare( OWLAxiom axiom1, OWLAxiom axiom2 ){
			Double d1 = hmAxioms.get( axiom1 );
			Double d2 = hmAxioms.get( axiom2 );
			return Double.compare( d2, d1 );
		}
		public boolean equals( Object object ){
			return false;
		}
	}
	
	public String getClassURI( int iClassID ) throws Exception {
		String sQuery = m_sqlFactory.selectClassURIQuery( iClassID );
		ResultSet results = m_database.query( sQuery );
		if( results.next() ){
			return results.getString( "uri" );
		}
		return null;
	}
	
	public String getPropertyURI( int iPropertyID ) throws Exception {
		String sQuery = m_sqlFactory.selectPropertyURIQuery( iPropertyID );
		ResultSet results = m_database.query( sQuery );
		if( results.next() ){
			return results.getString( "uri" );
		}
		return null;
	}

	//retrieves the property ID form the property_exists table
	public String getPropertyURIFromExistsProperty( int iExistsPropertyID ) throws Exception {
		String sQuery = m_sqlFactory.selectURIsFromExistsQuery( iExistsPropertyID );
		ResultSet results = m_database.query( sQuery );
		if( results.next() ){
			return results.getString( "prop_uri" );
		}
		return null;
	}
	
	//retrieves the property ID form the property_exists table
	public String getClassURIFromExistsProperty( int iExistsPropertyID ) throws Exception {
		String sQuery = m_sqlFactory.selectURIsFromExistsQuery( iExistsPropertyID );
		ResultSet results = m_database.query( sQuery );
		if( results.next() ){
			return results.getString( "class_uri" );
		}
		return null;
	}
	
	//retrieves the property ID form the property_exists_top table
	public String getPropertyURIFromExistsPropertyTop( int iExistsPropertyID ) throws Exception {
		String sQuery = m_sqlFactory.selectURIsFromExistsTopQuery( iExistsPropertyID );
		ResultSet results = m_database.query( sQuery );
		if( results.next() ){
			return results.getString( "uri" );
		}
		return null;
	}
}
