package de.uni_mannheim.informatik.dws.goldminer.sparql;

import java.util.Iterator;

/**
 * Created by daniel on 24.02.14.
 */
public interface ResultsIterator extends Iterator<String> {
    @Override
    boolean hasNext();

    @Override
    String next();

    @Override
    void remove();

    public boolean isFailed();
}
