package de.uni_mannheim.informatik.dws.goldminer.sparql;

import de.uni_mannheim.informatik.dws.goldminer.util.Parameter;
import de.uni_mannheim.informatik.dws.goldminer.util.Settings;
import org.slf4j.Logger;

/**
 * Created by daniel on 24.02.14.
 */
public abstract class SPARQLQueryEngine {
    public static final Logger logger = org.slf4j.LoggerFactory.getLogger(SPARQLQueryEngine.class);

    public static SPARQLQueryEngine createEngine() {
        String endpoint = Settings.getString(Parameter.ENDPOINT);
        String graph = Settings.getString( Parameter.GRAPH );
        int chunk = Settings.getInteger( Parameter.SPARQL_CHUNK );
        logger.debug("Initialized query engine with: " + endpoint + " " + graph + " " + chunk);

        if (endpoint.contains("jdbc:")) {
            return new SPARQLVirtuosoQueryEngine(endpoint, graph);
        }
        else {
            return new SPARQLHTTPQueryEngine(endpoint, graph, chunk);
        }
    }

    public static SPARQLQueryEngine createEngine(String endpoint, String graph, int chunk) {
        logger.debug("Initialized query engine with: " + endpoint + " " + graph + " " + chunk);

        if (endpoint.contains("jdbc:")) {
            return new SPARQLVirtuosoQueryEngine(endpoint, graph);
        }
        else {
            return new SPARQLHTTPQueryEngine(endpoint, graph, chunk);
        }
    }

    public abstract ResultsIterator query(String query, String filter);

    public abstract ResultPairsIterator queryPairs(String query, String filter);

    public abstract int count(String queryString) throws Exception;
}
