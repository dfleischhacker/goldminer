package miner.database;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;

import miner.sparql.Filter;
import miner.sparql.QueryEngine;
import miner.sparql.SPARQLFactory;

public abstract class Extractor {

	protected SQLFactory m_sqlFactory;
	protected Database m_database;
	protected SPARQLFactory m_sparqlFactory;
	protected QueryEngine m_engine;
	protected Filter filter;
	
	public Extractor() throws SQLException, FileNotFoundException, IOException {
		this.m_sqlFactory = new SQLFactory();
		this.m_database = Database.instance();
		this.m_sparqlFactory = new SPARQLFactory();
		this.m_engine = new QueryEngine();
		this.filter = new Filter();
	}
	
	public Extractor(Database database, String endpoint, String graph, int chunk, Filter filter) {
		this.m_sqlFactory = new SQLFactory();
		this.m_database = database;
		this.m_sparqlFactory = new SPARQLFactory();
		this.m_engine = new QueryEngine(endpoint, graph, chunk);
		this.filter = filter;
	}
	
	public String getLocalName( String sURI ){
		int iLabel = sURI.lastIndexOf( "#" );
		if( iLabel == -1 ){
			iLabel = sURI.lastIndexOf( "/" );
		}
		if( iLabel != -1 ){
			return sURI.substring( iLabel+1 );
		}
		return "";
	}
	
	public String[] getProperties() throws SQLException {
		HashSet<String> properties = new HashSet<String>();
		String sQuery = m_sqlFactory.selectPropertiesQuery();
		ResultSet results = m_database.query( sQuery );
		while( results.next() ){
			properties.add( results.getString( "uri" ) );
		}
		return properties.toArray( new String[properties.size()] );
	}
	
	protected String checkURISyntax( String sURI ){
		String s = new String( sURI );
		s = s.replaceAll( "'", "_" );
		return s;
	}
}
