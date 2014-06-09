package de.uni_mannheim.informatik.dws.goldminer.database;

import java.util.HashSet;

/**
 * Class for managing blacklisted URIs and prefixes.
 *
 * @author Daniel Fleischhacker <daniel@informatik.uni-mannheim.de>
 */
public class PropertyBlacklist {
    private static PropertyBlacklist blacklist = new PropertyBlacklist();

    private HashSet<String> blacklistedUris = new HashSet<String>();
    private HashSet<String> blacklistedPrefixes = new HashSet<String>();

    public PropertyBlacklist() {
        blacklistedPrefixes.add("http://dbpedia.org/ontology/wiki");
    }

    public static boolean isBlackListed(String uri) {
        if (blacklist.blacklistedUris.contains(uri)) {
            return true;
        }
        for (String prefix : blacklist.blacklistedPrefixes) {
            if (uri.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }
}
