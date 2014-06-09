package de.uni_mannheim.informatik.dws.goldminer.database;

import de.uni_mannheim.informatik.dws.goldminer.sparql.Filter;
import de.uni_mannheim.informatik.dws.goldminer.sparql.ResultPairsIterator;
import de.uni_mannheim.informatik.dws.goldminer.sparql.ResultsIterator;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

public class IndividualsExtractor extends Extractor {
    public IndividualsExtractor() throws SQLException, FileNotFoundException,
            IOException {
        super();
    }

    public IndividualsExtractor(SQLDatabase sqlDatabase, String endpoint, String graph, int chunk, Filter filter) {
        super(sqlDatabase, endpoint, graph, chunk, filter);
    }

    public void initIndividualPairsTransTable() throws SQLException {
        String properties[] = getProperties();
        HashMap<String, HashMap<String, Boolean>> hmURIs = new HashMap<String, HashMap<String, Boolean>>();
        int iPairs = 0;
        // properties
        for (int i = 0; i < properties.length; i++) {
            String sProp = properties[i];
            ResultPairsIterator iter1 = sparqlEngine.queryPairs(sparqlFactory.propertyExtensionQuery(sProp),
                    this.filter.getIndividualsFilter());
            int iPropPairs = 0;
            while (iter1.hasNext()) {
                iPropPairs++;
                String sPair[] = iter1.next();
                HashMap hm = hmURIs.get(sPair[0]);
                if (hm == null) {
                    hm = new HashMap<String, Boolean>();
                    hmURIs.put(sPair[0], hm);
                }
                if (hm.get(sPair[1]) == null) {
                    iPairs++;
                }
                hm.put(sPair[1], false);
            }
            System.out.println("Setup.initIndividualPairsTrans( " + sProp + " ) ... " + iPropPairs + " -> " + iPairs);
        }
        // property chains
        ResultSet results = sqlDatabase.query(sqlFactory.selectPropertyChainsQuery());
        while (results.next()) {
            String sProp1 = results.getString("uri1");
            String sProp2 = results.getString("uri2");
            if (sProp1.equals(sProp2)) {
                ResultPairsIterator iter = sparqlEngine.queryPairs(
                        sparqlFactory.propertyChainExtensionQuery(sProp1, sProp2), this.filter.getIndividualsFilter());
                int iChainPairs = 0;
                while (iter.hasNext()) {
                    iChainPairs++;
                    String sPair[] = iter.next();
                    HashMap hm = hmURIs.get(sPair[0]);
                    if (hm == null) {
                        hm = new HashMap<String, Boolean>();
                        hmURIs.put(sPair[0], hm);
                    }
                    if (hm.get(sPair[1]) == null) {
                        iPairs++;
                    }
                    hm.put(sPair[1], true);
                }
                System.out.println(
                        "Setup.initIndividualPairsTrans( " + sProp1 + ", " + sProp2 + " ) ... " + iChainPairs + " -> " +
                                iPairs);
            }
        }
        int id = 0;
        // individual_pairs
        sqlDatabase.setAutoCommit(false);
        for (String sInd1 : hmURIs.keySet()) {
            HashMap<String, Boolean> hm = hmURIs.get(sInd1);
            for (String sInd2 : hm.keySet()) {
                String sName1 = getLocalName(sInd1);
                String sName2 = getLocalName(sInd2);
                sqlDatabase.execute(sqlFactory.insertIndividualPairTransQuery(id, sInd1, sInd2, sName1, sName2));
                id++;
            }
            if (id % 1000 == 0) {
                sqlDatabase.commit();
            }
        }
        sqlDatabase.commit();
        sqlDatabase.setAutoCommit(true);
        System.out.println("done: " + id);
    }

    public void initIndividualPairsTable() throws SQLException {
        HashMap<String, HashMap<String, Boolean>> hmURIs = new HashMap<String, HashMap<String, Boolean>>();
        // read properties from database
        String sQuery1 = sqlFactory.selectPropertiesQuery();
        ResultSet results = sqlDatabase.query(sQuery1);
        int iPairs = 0;
        while (results.next()) {
            String sProp = results.getString("uri");
            if (PropertyBlacklist.isBlackListed(sProp)) {
                continue;
            }
            ResultPairsIterator iter = sparqlEngine.queryPairs(sparqlFactory.propertyExtensionQuery(sProp),
                    this.filter.getIndividualsFilter());
            int iPropPairs = 0;
            while (iter.hasNext()) {
                iPropPairs++;
                String sPair[] = iter.next();
                HashMap hm = hmURIs.get(sPair[0]);
                if (hm == null) {
                    hm = new HashMap<String, Boolean>();
                    hmURIs.put(sPair[0], hm);
                }
                if (hm.get(sPair[1]) == null) {
                    iPairs++;
                }
                hm.put(sPair[1], true);
            }
            System.out.println("Setup.initIndividualPairs( " + sProp + " ) ... " + iPropPairs + " -> " + iPairs);
        }
        int id = 0;
        sqlDatabase.setAutoCommit(false);
        for (String sInd1 : hmURIs.keySet()) {
            HashMap<String, Boolean> hm = hmURIs.get(sInd1);
            for (String sInd2 : hm.keySet()) {
                String sName1 = getLocalName(sInd1);
                String sName2 = getLocalName(sInd2);
                // String sCountIndPropertiesQuery = sparqlFactory.countPropertiesQuery( sInd1, sInd2 );
                // int iCount = sparqlEngine.count( sCountIndPropertiesQuery );
                String sQuery2 = sqlFactory.insertIndividualPairQuery(id, sInd1, sInd2, sName1, sName2);
                sqlDatabase.execute(sQuery2);
                id++;
            }
            if (id % 1000 == 0) {
                sqlDatabase.commit();
            }
        }
        sqlDatabase.commit();
        sqlDatabase.setAutoCommit(true);
    }

    public void initIndividualsTable() throws SQLException {
        HashMap<String, String> hmURI2Name = new HashMap<String, String>();
        // read classes from database
        String sQuery1 = sqlFactory.selectClassesQuery();
        ResultSet results = sqlDatabase.query(sQuery1);
        while (results.next()) {
            String sClass = results.getString("uri");
            int iClassID = results.getInt("id");
            String sQuery3 = sparqlFactory.classExtensionQuery(sClass);
            System.out.println("\n" + sClass + " (" + iClassID + ")");
            ResultsIterator iter = sparqlEngine.query(sQuery3, this.filter.getIndividualsFilter());
            int iClassInd = 0;
            while (iter.hasNext()) {
                iClassInd++;
                String sInd = iter.next();
                hmURI2Name.put(sInd, getLocalName(sInd));
            }
            System.out.println("Setup.initIndividuals( " + sClass + " ) ... " + iClassInd + " -> " + hmURI2Name.size());
        }
        int id = 0;

        sqlDatabase.setAutoCommit(false);
        for (String sInd : hmURI2Name.keySet()) {
            String sName = hmURI2Name.get(sInd);
            String sQuery2 = sqlFactory.insertIndividualQuery(id, sInd, sName);
            System.out.println("\nQUERY: " + sQuery2);
            sqlDatabase.execute(sQuery2);
            id++;
            if (id % 1000 == 0) {
                sqlDatabase.commit();
            }
        }
        sqlDatabase.commit();
        sqlDatabase.setAutoCommit(true);
        System.out.println("done: " + id);
    }

    public int getIndividualID(String sURI) throws Exception {
        sURI = checkURISyntax(sURI);
        String sQuery = sqlFactory.selectIndividualIDQuery(sURI);
        ResultSet results = sqlDatabase.query(sQuery);
        if (results.next()) {
            return results.getInt("id");
        }
        return -1;
    }
}
