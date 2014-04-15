package de.uni_mannheim.informatik.dws.goldminer.sparql;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;


public class VirtuosoResultsIterator implements ResultsIterator {
    private List<String> results;
    private Iterator<String> iterator;

	protected VirtuosoResultsIterator(SPARQLVirtuosoQueryEngine engine, String query, String filter){
        try {
            results = engine.execute(query, "x", filter);
        }
        catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        iterator = results.iterator();
    }

    @Override
    public boolean hasNext() {
        return iterator.hasNext();
    }

    @Override
    public String next() {
        return iterator.next();
    }

    @Override
    public void remove() {
        iterator.remove();
    }

    @Override
    public boolean isFailed() {
        return false;
    }
}

