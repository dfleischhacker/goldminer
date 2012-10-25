package de.uni_mannheim.informatik.dws.goldminer.sparql;

import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Resource;
import de.uni_mannheim.informatik.dws.goldminer.util.Parameter;
import de.uni_mannheim.informatik.dws.goldminer.util.Settings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class QueryEngine {

    private final static Logger logger = LoggerFactory.getLogger(QueryEngine.class);

	protected String m_sEndpoint;

	protected String m_sGraph;

	protected int m_iChunk;

	public QueryEngine(){
		m_sEndpoint = Settings.getString(Parameter.ENDPOINT);
        m_sGraph = Settings.getString( Parameter.GRAPH );
        m_iChunk = Settings.getInteger( Parameter.SPARQL_CHUNK );
        logger.debug("Initialized query engine with: " + m_sEndpoint + " " + m_sGraph + " " + m_iChunk);
    }


	public QueryEngine(String endpoint, String graph, int chunk){
		m_sEndpoint = endpoint;
		m_sGraph = graph;
		m_iChunk = chunk;
	}

	public QueryEngine( String sEndpoint, int iChunk ){
		m_sEndpoint = sEndpoint;
		m_iChunk = iChunk;
	}

	public int getChunkSize(){
		return m_iChunk;
	}

	public List<String> getAll( Iterator<String> iter ){
		List<String> all = new ArrayList<String>();
		while( iter.hasNext() ){
			all.add(iter.next());
		}
		return all;
	}

	public ResultsIterator query( String query, String filter ) {
		return new ResultsIterator( this, query, filter );
	}

	public ResultPairsIterator queryPairs( String query, String filter ) {
		return new ResultPairsIterator( this, query, filter );
	}

	protected List<String> execute( String queryString, String sVar, String filter ) throws UnsupportedEncodingException, IOException {
		System.out.println( "QueryEngine.query: "+ queryString +"\n" );
		List<String> set = new ArrayList<String>();
		Query query = QueryFactory.create(queryString);
		QueryExecution qe = null;
		if( m_sGraph == null ){
			qe = QueryExecutionFactory.sparqlService(m_sEndpoint, query);
		}
		else {
			qe = QueryExecutionFactory.sparqlService( m_sEndpoint, query, m_sGraph );
		}
        try {
            ResultSet results = qe.execSelect();
            while (results.hasNext()) {
                QuerySolution soln = results.nextSolution();
                Resource r = soln.getResource(sVar);
                String sURI = checkURISyntax(r.getURI());
                if (sURI != null) {
                    if (filter == null || sURI.startsWith(filter)) {
                        set.add(sURI);
                    }
                }
            }
        }
        catch (Exception e) {
            logger.error("Query \"" + queryString + "\" failed", e);
        }
		if( qe != null ) qe.close();
		return set;
	}

	protected List<String[]> execute( String queryString, String sVar1, String sVar2, String filter ) throws Exception {
		// System.out.println( "QueryEngine.query: "+ queryString +"\n" );
		List<String[]> set = new ArrayList<String[]>();
		Query query = QueryFactory.create( queryString );
		QueryExecution qe = null;
		try {
			if( m_sGraph == null ){
				qe = QueryExecutionFactory.sparqlService( m_sEndpoint, query );
			}
			else {
				qe = QueryExecutionFactory.sparqlService( m_sEndpoint, query, m_sGraph );
			}
			ResultSet results = qe.execSelect();
			while( results.hasNext() )
			{
				QuerySolution soln = results.nextSolution();
				Resource r1 = soln.getResource( sVar1 );
				Resource r2 = soln.getResource( sVar2 );
				String sURI1 = checkURISyntax( r1.getURI() );
				String sURI2 = checkURISyntax( r2.getURI() );
				if( sURI1 != null && sURI2 != null )
				{
					String s[] = { sURI1, sURI2 };
					if( filter == null || ( s[0].startsWith( filter ) && s[1].startsWith( filter ) ) ){
						set.add(s);
					}
				}
			}
		}
		catch( Exception e ){
			e.printStackTrace();
		}
		finally {
			if( qe != null ) qe.close();
		}
		return set;
	}

	public int count( String queryString ) throws Exception {
		// System.out.println( "QueryEngine.count: "+ queryString +"\n" );
		List<String> set = new ArrayList<String>();
		Query query = QueryFactory.create( queryString, Syntax.syntaxARQ );
		QueryExecution qe = null;
		try {
			qe = QueryExecutionFactory.sparqlService( m_sEndpoint, query, m_sGraph );
			ResultSet results = qe.execSelect();
			// ResultSetFormatter.out( System.out, results, query );
			if( results.hasNext() )
			{
				QuerySolution soln = results.nextSolution();
				if( soln.contains( "count" ) )
				{
					Literal l = soln.getLiteral( "count" );
					return l.getInt();
				}
			}
		}
		catch( Exception e ){
			e.printStackTrace();
		}
		finally {
			if( qe != null ) qe.close();
		}
		return 0;
	}

	protected String checkURISyntax( String sURI ){
		if( sURI == null ) return null;
		String s = sURI;
		s = s.replaceAll( "'", "_" );
		return s;
	}
}



