package de.uni_mannheim.informatik.dws.goldminer.database;

import de.uni_mannheim.informatik.dws.goldminer.sparql.Filter;
import de.uni_mannheim.informatik.dws.goldminer.sparql.SPARQLQueryEngine;
import de.uni_mannheim.informatik.dws.goldminer.sparql.SPARQLFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;

public abstract class Extractor {

	protected SQLFactory sqlFactory;
	protected SQLDatabase sqlDatabase;
	protected SPARQLFactory sparqlFactory;
	protected SPARQLQueryEngine sparqlEngine;
	protected Filter filter;
	
	public Extractor() throws SQLException, FileNotFoundException, IOException {
		this.sqlFactory = new SQLFactory();
		this.sqlDatabase = SQLDatabase.instance();
		this.sparqlFactory = new SPARQLFactory();
		this.sparqlEngine = SPARQLQueryEngine.createEngine();
		this.filter = new Filter();
	}
	
	public Extractor(SQLDatabase sqlDatabase, String endpoint, String graph, int chunk, Filter filter) {
		this.sqlFactory = new SQLFactory();
		this.sqlDatabase = sqlDatabase;
		this.sparqlFactory = new SPARQLFactory();
		this.sparqlEngine = SPARQLQueryEngine.createEngine(endpoint, graph, chunk);
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
		String sQuery = sqlFactory.selectPropertiesQuery();
		ResultSet results = sqlDatabase.query( sQuery );
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
