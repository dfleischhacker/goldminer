package de.uni_mannheim.informatik.dws.goldminer.database;

import de.uni_mannheim.informatik.dws.goldminer.sparql.Filter;
import de.uni_mannheim.informatik.dws.goldminer.sparql.QueryEngine;
import de.uni_mannheim.informatik.dws.goldminer.sparql.SPARQLFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;

public abstract class Extractor {

	protected SQLFactory m_sqlFactory;
	protected Database database;
	protected SPARQLFactory m_sparqlFactory;
	protected QueryEngine m_engine;
	protected Filter filter;
	
	public Extractor() throws SQLException, FileNotFoundException, IOException {
		this.m_sqlFactory = new SQLFactory();
		this.database = Database.instance();
		this.m_sparqlFactory = new SPARQLFactory();
		this.m_engine = new QueryEngine();
		this.filter = new Filter();
	}
	
	public Extractor(Database database, String endpoint, String graph, int chunk, Filter filter) {
		this.m_sqlFactory = new SQLFactory();
		this.database = database;
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
		ResultSet results = database.query( sQuery );
		while( results.next() ){
			properties.add( results.getString( "uri" ) );
		}
		return properties.toArray( new String[properties.size()] );
	}
	
	protected String checkURISyntax( String sURI ){
		String s = sURI;
		s = s.replaceAll( "'", "_" );
		return s;
	}
}
