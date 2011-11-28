package miner.database;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;

import miner.sparql.Filter;
import miner.sparql.QueryEngine;
import miner.sparql.ResultPairsIterator;
import miner.sparql.ResultsIterator;
import miner.sparql.SPARQLFactory;

public class TerminologyExtractor extends Extractor {
	
	private int id;
	
	public TerminologyExtractor() throws SQLException, FileNotFoundException,
			IOException {
		super();
		this.id = 1;
	}
	
	public TerminologyExtractor(Database database, String endpoint, String graph, int chunk, Filter filter) {
		super(database, endpoint, graph, chunk, filter);
	}

	public void initDisjointnessTable() throws Exception {
		ResultSet results = m_database.query( m_sqlFactory.selectDisjointnessQuery() );
		while( results.next() )
		{
			int i1 = results.getInt( "cons" );
			int i2 = results.getInt( "ante" );
			String s1 = getClassURI( i1 );
			String s2 = getClassURI( i2 );
			String sCount = m_sparqlFactory.classExtensionQuery( s1, s2 );
			ResultsIterator iter = m_engine.query( sCount, this.filter.getIndividualsFilter() );
			int iCount = -1;
			if( iter.hasNext() ){
				iCount = 1;
			}
			else {
				iCount = 0;
			}
			String sUpdate = m_sqlFactory.updateDisjointnessQuery( i1, i2, iCount );
			m_database.execute( sUpdate );
		}
		System.out.println( "done" );
	}
	
	public void initPropertyChainsTransTable() throws SQLException {
		ResultSet results = m_database.query( m_sqlFactory.selectPropertiesQuery() );
		while( results.next() )
		{
			String sProp = results.getString( "uri" );
			String sName = results.getString( "name" );
			int iID = results.getInt( "id" );
			String sInsert = m_sqlFactory.insertPropertyChainTransQuery( this.id++, sProp, sName );
			m_database.execute( sInsert );
		}
		System.out.println( "done" );
	}
	
	public void initClassesExistsPropertyTable() throws SQLException  {
		String properties[] = getProperties();
		// read classes from database
		String sQuery1 = m_sqlFactory.selectClassesQuery();
		ResultSet results1 = m_database.query( sQuery1 );
		int id = 1000;
		while( results1.next() )
		{
			String sClass = results1.getString( "uri" );
			for( String sProp: properties )
			{
				String sizeQuery = m_sparqlFactory.existsPropertyExtensionSizeQuery( sProp, sClass );
				/* int iSize = m_engine.count( sizeQuery );
				if( iSize == 0 ){
					continue;
				} */
				System.out.println( "Setup.initClassesExistsProperty( "+ sProp +", "+ sClass +" ) -> "+ id );
				// update table
				String sClassName = getLocalName( sClass );
				String sPropName = getLocalName( sProp );
				String sQuery3 = m_sqlFactory.insertClassExistsPropertyQuery( this.id++, sProp, sClass, sPropName, sClassName );
				m_database.execute( sQuery3 );
				id++;
			}
		}
		System.out.println( "Done: "+ id );
	}
	
	public void initPropertiesTable() {
		String sQuery1 = m_sparqlFactory.propertiesQuery();
		ResultsIterator iter = m_engine.query( sQuery1, this.filter.getClassesFilter() );
		int id = 0;
		while( iter.hasNext() )
		{
			String sProp = iter.next();
			String sName = getLocalName( sProp );
			String sQuery2 = m_sqlFactory.insertPropertyQuery( this.id++, sProp, sName );
			this.id = this.id + 2;
			m_database.execute( sQuery2 );
		}
		System.out.println( "done: "+ id );
	}
	
	public void initPropertyChainsTable() throws SQLException {
		String properties[] = getProperties();
		// two hashmaps for each property: domain and range
		HashMap hmRanges[] = new HashMap[properties.length];
		HashMap hmDomains[] = new HashMap[properties.length];
		for( int i=0; i<properties.length; i++ )
		{
			String sProp = properties[i];
			System.out.println( "initPropertyChainsTable: "+ sProp );
			hmRanges[i] = new HashMap<String,Boolean>();
			hmDomains[i] = new HashMap<String,Boolean>();
			ResultPairsIterator iter = m_engine.queryPairs( m_sparqlFactory.propertyExtensionQuery( sProp ), this.filter.getIndividualsFilter() );
			while( iter.hasNext() ) 
			{
				String sPair[] = iter.next();
				hmDomains[i].put( sPair[0], true );
				hmRanges[i].put( sPair[1], true );
			}
		}
		int id = 1000;
		for( int i=0; i<properties.length; i++ ){
			for( int j=0; j<properties.length; j++ )
			{
				HashMap<String,Boolean> hmRange = (HashMap<String,Boolean>) hmRanges[i];
				for( String sRange: hmRange.keySet() )
				{
					if( hmDomains[j].get( sRange ) != null )
					{
						String sURI1 = properties[i];
						String sURI2 = properties[j];
						String sName1 = getLocalName( sURI1 );
						String sName2 = getLocalName( sURI2 );
						String sInsertQuery = m_sqlFactory.insertPropertyChainQuery( this.id++, sURI1, sURI2, sName1, sName2 );
						m_database.execute( sInsertQuery );
						break;
					}
				}
			}
		}
		System.out.println( "done: "+ id );
	}
	
	public void initPropertyTopTable() throws SQLException {
		String sQuery = m_sqlFactory.selectPropertiesQuery();
		ResultSet results = m_database.query( sQuery );
		while( results.next() )
		{
			String sPropURI = results.getString( "uri" );
			String sPropName = results.getString( "name" );
			int iPropID = results.getInt( "id" );
			int iTopID = iPropID + 1000;
			int iInvTopID = iPropID + 2000;
			String sInsert1 = m_sqlFactory.insertPropertyTopQuery( this.id++, 0, sPropURI, sPropName );
			String sInsert2 = m_sqlFactory.insertPropertyTopQuery( this.id++, 1, sPropURI, sPropName );
			m_database.execute( sInsert1 );
			m_database.execute( sInsert2 );
		}
		System.out.println( "Setup.initPropertyTopTable: done" );
	}
	
	public void initClassesTable() {
		String query = m_sparqlFactory.classesQuery();
		// System.out.println( query );
		ResultsIterator iter = m_engine.query( query, this.filter.getClassesFilter() );
		int id = 0;
		while( iter.hasNext() && !iter.isFailed() )
		{
			String sClass = iter.next();
			String sName = getLocalName( sClass );
			String sCountClassIndQuery = m_sparqlFactory.classExtensionSizeQuery( sClass );
			// int iSize = m_engine.count( sCountClassIndQuery );
			System.out.println( sClass +" ... " );
			String sQuery = m_sqlFactory.insertClassQuery( this.id++, sClass, sName );
			m_database.execute( sQuery );
		}
		System.out.println( "done: "+ id );
	}
	
	public String getClassURI( int iID ) throws Exception {
		String sQuery = m_sqlFactory.selectClassURIQuery( iID );
		ResultSet results = m_database.query( sQuery );
		if( results.next() ){
			return results.getString( "uri" );
		}
		return null;
	}
	
	public String getClassID( String sURI ) throws Exception {
		sURI = checkURISyntax( sURI );
		String sQuery = m_sqlFactory.selectClassIDQuery( sURI );
		ResultSet results = m_database.query( sQuery );
		if( results.next() ){
			return results.getString( "id" );
		}
		return null;
	}
}
