package de.uni_mannheim.informatik.dws.goldminer.sparql;

import de.uni_mannheim.informatik.dws.goldminer.util.Parameter;
import de.uni_mannheim.informatik.dws.goldminer.util.Settings;
import org.apache.log4j.spi.LoggerFactory;
import org.slf4j.Logger;

import java.util.Iterator;
import java.util.List;

/**
 * Created by daniel on 24.02.14.
 */
public abstract class QueryEngine {
    public static final Logger logger = org.slf4j.LoggerFactory.getLogger(QueryEngine.class);

    public static QueryEngine createEngine() {
        String endpoint = Settings.getString(Parameter.ENDPOINT);
        String graph = Settings.getString( Parameter.GRAPH );
        int chunk = Settings.getInteger( Parameter.SPARQL_CHUNK );
        logger.debug("Initialized query engine with: " + endpoint + " " + graph + " " + chunk);

        if (endpoint.contains("jdbc:")) {
            return new VirtuosoQueryEngine(endpoint, graph);
        }
        else {
            return new SPARQLQueryEngine(endpoint, graph, chunk);
        }
    }

    public static QueryEngine createEngine(String endpoint, String graph, int chunk) {
        logger.debug("Initialized query engine with: " + endpoint + " " + graph + " " + chunk);

        if (endpoint.contains("jdbc:")) {
            return new VirtuosoQueryEngine(endpoint, graph);
        }
        else {
            return new SPARQLQueryEngine(endpoint, graph, chunk);
        }
    }

    public abstract ResultsIterator query(String query, String filter);

    public abstract ResultPairsIterator queryPairs(String query, String filter);

    public abstract int count(String queryString) throws Exception;
}
