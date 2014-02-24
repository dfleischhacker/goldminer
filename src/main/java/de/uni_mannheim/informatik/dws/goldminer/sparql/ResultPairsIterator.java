package de.uni_mannheim.informatik.dws.goldminer.sparql;

import java.util.Iterator;

/**
 * Created by daniel on 24.02.14.
 */
public interface ResultPairsIterator extends Iterator<String[]> {
    boolean hasNext();

    String[] next();

    void remove();
}
