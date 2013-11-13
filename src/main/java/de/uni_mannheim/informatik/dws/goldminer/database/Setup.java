package de.uni_mannheim.informatik.dws.goldminer.database;

import java.io.IOException;
import java.sql.SQLException;


public class Setup {
	
	private Database m_database;
	
	private SQLFactory m_sqlFactory;
	
	public Setup() throws SQLException, IOException {
		m_database = Database.instance();
		m_sqlFactory = new SQLFactory();
	}
	
	public Setup(Database d) throws SQLException {
		m_database = d;
		m_sqlFactory = new SQLFactory();
	}
	
	/**
	 * sets up the database schema that is required for the terminology acquisition.
	 * @param classes is true if the acquisition of the classes is required.
	 * @return true if setup was successful, false otherwise.
	 */
	public boolean setupSchema(
			boolean classes, 
			boolean individuals, 
			boolean properties, 
			boolean classes_ex_property, 
			boolean classes_ex_property_top, 
			boolean individual_pairs, 
			boolean individual_pairs_trans,
			boolean property_chains,
			boolean property_chains_trans) {
		boolean result = true;
		if(classes) {
			String classesQuery = this.m_sqlFactory.createClassesTable();
			result = this.m_database.execute(classesQuery) && result;
		}
		if(individuals) {
			String individualsQuery = this.m_sqlFactory.createIndividualsTable();
			result = this.m_database.execute(individualsQuery) && result;
		}
		if(properties) {
			String propertiesQuery = this.m_sqlFactory.createPropertiesTable();
			result = this.m_database.execute(propertiesQuery);
		}
		if(classes_ex_property) {
			String classesExPropertyQuery = this.m_sqlFactory.createClassesExPropertyTable();
			result = this.m_database.execute(classesExPropertyQuery);
		}
		if(classes_ex_property_top) {
			String classesExPropertyTopQuery = this.m_sqlFactory.createClassesExPropertyTopTable();
			result = this.m_database.execute(classesExPropertyTopQuery);
		}
		if(individual_pairs) {
			String individualPairsQuery = this.m_sqlFactory.createIndividualPairsTable();
			result = this.m_database.execute(individualPairsQuery);
		}
		if(individual_pairs_trans) {
			String individualPairsTransQuery = this.m_sqlFactory.createIndividualPairsTransTable();
			result = this.m_database.execute(individualPairsTransQuery);
		}
		if(property_chains) {
			String propertyChainsQuery = this.m_sqlFactory.createPropertyChainsTable();
			result = this.m_database.execute(propertyChainsQuery);
		}
		if(property_chains_trans) {
			String propertyChainsTransQuery = this.m_sqlFactory.createPropertyChainsTransTable();
			result = this.m_database.execute(propertyChainsTransQuery);
		}
		return result;
	}
	
	/**
	 * removes all created tables for terminology acquisition from database.
	 * 
	 * @return true if successful, false otherwise.
	 */
	public boolean removeSchema() {
		String query = this.m_sqlFactory.dropTables();
		return this.m_database.execute(query);
	}
	
	//The following methods are unused. (?)
	
	
	/*private String toString( List<String> set ){
		StringBuffer sb = new StringBuffer();
		sb.append( "[ " );
		Iterator iter = set.iterator();
		while( iter.hasNext() )
		{
			sb.append( iter.next() );
			if( iter.hasNext() ){
				sb.append( ", " );
			}
		}
		sb.append( " ]" );
		return sb.toString();
	}*/
	
	// classes -> individuals -> properties -> individual pairs -> property chains
	//public void initTables() throws Exception {
		// initClassesTable();
		// initPropertiesTable();
		// initPropertyChainsTable(); // requires: properties
		// initIndividualPairsTable();
		// initIndividualPairsTables(); // individual_pairs, individual_pairs_ext (+ s*t); requires: properties, property_chains
		// initIndividualsTable(); // requires: classes (remove filter)
		//
		// initPropertyTopTable();
		// initClassesExistsPropertyTable(); // requires: classes, properties
		// initIndividualsTables(); // individuals, individuals_ext (+ ex p.c), individuals_ext_ext (+ ex p.top)
		//
		// initIndividualPairsTransTable();
		// initPropertyChainsTransTable();
		// initDisjointnessTable();
	//}
	
	/* public void initIndividualPairsTables_new() throws Exception {
		// read properties from database
		String properties[] = getProperties();
		HashMap<String,HashMap<String,Boolean>> hmURIs = new HashMap<String,HashMap<String,Boolean>>();
		// properties
		for( int i=0; i<properties.length; i++ )
		{
			String sProp = properties[i];
			ResultPairsIterator iter = m_engine.queryPairs( m_sparqlFactory.propertyExtensionQuery( sProp ) );
			int iPropPairs = 0;
			while( iter.hasNext() ) 
			{
				iPropPairs++;
				String sPair[] = (String[]) iter.next();
				String sURI1 = sPair[0];
				String sURI2 = sPair[1];
				String sName1 = getLocalName( sURI1 );
				String sName2 = getLocalName( sURI2 );
				int id1 = getIndividualID( sURI1 );
				int id2 = getIndividualID( sURI2 );
				String query = m_sqlFactory.insertIndividualPairQuery( sURI1, sURI2, sName1, sName2, id1, id2 );
				database.execute( query );
			}
			System.out.println( "Setup.initIndividualPairs( "+ sProp +" ) ... "+ iPropPairs );
		}
		// property chains
		ResultSet results = database.query( m_sqlFactory.selectPropertyChainsQuery() );
		while( results.next() )
		{
			String sProp1 = results.getString( "uri1" );
			String sProp2 = results.getString( "uri2" );
			ResultPairsIterator iter = m_engine.queryPairs( m_sparqlFactory.propertyChainExtensionQuery( sProp1, sProp2 ) );
			int iChainPairs = 0;
			while( iter.hasNext() )
			{
				iChainPairs++;
				String sPair[] = (String[]) iter.next();
				String sURI1 = sPair[0];
				String sURI2 = sPair[1];
				String sName1 = getLocalName( sURI1 );
				String sName2 = getLocalName( sURI2 );
				int id1 = getIndividualID( sURI1 );
				int id2 = getIndividualID( sURI2 );
				database.execute( m_sqlFactory.insertIndividualPairExtQuery( sURI1, sURI2, sName1, sName2, id1, id2 ) );
			}
			System.out.println( "Setup.initIndividualPairs( "+ sProp1 +", "+ sProp2 +" ) ... "+ iChainPairs );
		}
		System.out.println( "Setup: done" );
	} */
	
	/* public void copyIndividualPairs2Ext() throws Exception {
		ResultSet results = database.query( m_sqlFactory.selectIndividualPairsQuery() );
		while( results.next() )
		{
			int id = results.getInt( "id" );
			String sURI1 = results.getString( "uri1" );
			String sURI2 = results.getString( "uri2" );
			String sName1 = results.getString( "name1" );
			String sName2 = results.getString( "name2" );
			System.out.println( id +" - "+ sName1 +" - "+ sName2 );
			String sInsert = m_sqlFactory.insertIndividualPairExtQuery( id, sURI1, sURI2, sName1, sName2 );
			database.execute( sInsert );
		}
		System.out.println( "done" );
	} */
	
	/* public void initPropertiesTable() throws Exception {
		HashMap<String,String> hmURI2Name = new HashMap<String,String>();
		String sQuery1 = m_sqlFactory.selectIndividualsQuery();
		ResultSet results = database.query( sQuery1 );
		while( results.next() )
		{
			String sInd = results.getString( "uri" );
			String sIndName = getLocalName( sInd );
			String query = m_sparqlFactory.propertiesQuery( sInd );
			ResultsIterator iter = m_engine.query( query );
			int iIndProps = 0;
			while( iter.hasNext() )
			{
				iIndProps++;
				String sProp = (String) iter.next();
				String sPropName = getLocalName( sProp );
				hmURI2Name.put( sProp, sPropName );
			}
			System.out.println( "Setup.initProperties( "+ sIndName +" ) ... "+ iIndProps +" -> "+ hmURI2Name.size() );
		}
		int id = 0;
		for( String sProp: hmURI2Name.keySet() )
		{
			String sName = hmURI2Name.get( sProp );
			String sQuery = m_sqlFactory.insertPropertyQuery( id++, sProp, sName );
			database.execute( sQuery );
		}
		System.out.println( "done: "+ id );
	} */	
}