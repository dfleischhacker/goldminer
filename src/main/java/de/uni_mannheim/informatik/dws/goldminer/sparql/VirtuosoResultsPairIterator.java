package de.uni_mannheim.informatik.dws.goldminer.sparql;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

/**
 * Created by daniel on 24.02.14.
 */
public class VirtuosoResultsPairIterator implements ResultPairsIterator {
    private List<String[]> results;
    private Iterator<String[]> iterator;

    protected VirtuosoResultsPairIterator(VirtuosoQueryEngine engine, String query, String filter){
        try {
            results = engine.execute(query, "x", "y", filter);
        }
        catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        catch (Exception e) {
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
    public String[] next() {
        return iterator.next();
    }

    @Override
    public void remove() {
        iterator.remove();
    }
}
