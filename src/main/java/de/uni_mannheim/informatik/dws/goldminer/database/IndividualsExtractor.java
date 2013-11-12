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

    public IndividualsExtractor(Database database, String endpoint, String graph, int chunk, Filter filter) {
        super(database, endpoint, graph, chunk, filter);
    }

    public void initIndividualPairsTransTable() throws SQLException {
        String properties[] = getProperties();
        HashMap<String, HashMap<String, Boolean>> hmURIs = new HashMap<String, HashMap<String, Boolean>>();
        int iPairs = 0;
        // properties
        for (int i = 0; i < properties.length; i++) {
            String sProp = properties[i];
            ResultPairsIterator iter1 = m_engine
                    .queryPairs(m_sparqlFactory.propertyExtensionQuery(sProp), this.filter.getIndividualsFilter());
            int iPropPairs = 0;
            while (iter1.hasNext()) {
                iPropPairs++;
                String sPair[] = iter1.next();
                HashMap<String, Boolean> hm = hmURIs.get(sPair[0]);
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
        ResultSet results = m_database.query(m_sqlFactory.selectPropertyChainsQuery());
        while (results.next()) {
            String sProp1 = results.getString("uri1");
            String sProp2 = results.getString("uri2");
            if (sProp1.equals(sProp2)) {
                ResultPairsIterator iter = m_engine
                        .queryPairs(m_sparqlFactory.propertyChainExtensionQuery(sProp1, sProp2),
                                this.filter.getIndividualsFilter());
                int iChainPairs = 0;
                while (iter.hasNext()) {
                    iChainPairs++;
                    String sPair[] = iter.next();
                    HashMap<String, Boolean> hm = hmURIs.get(sPair[0]);
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
        for (String sInd1 : hmURIs.keySet()) {
            HashMap<String, Boolean> hm = hmURIs.get(sInd1);
            for (String sInd2 : hm.keySet()) {
                String sName1 = getLocalName(sInd1);
                String sName2 = getLocalName(sInd2);
                m_database.execute(m_sqlFactory.insertIndividualPairTransQuery(id, sInd1, sInd2, sName1, sName2));
                id++;
            }
        }
        System.out.println("done: " + id);
    }

    public void initIndividualPairsTables() throws Exception {
        // read properties from database
        String properties[] = getProperties();
        HashMap<String, HashMap<String, Boolean>> hmURIs = new HashMap<String, HashMap<String, Boolean>>();
        int iPairs = 0;
        // properties
        for (int i = 0; i < properties.length; i++) {
            String sProp = properties[i];
            ResultPairsIterator iter1 = m_engine
                    .queryPairs(m_sparqlFactory.propertyExtensionQuery(sProp), this.filter.getIndividualsFilter());
            int iPropPairs = 0;
            while (iter1.hasNext()) {
                iPropPairs++;
                String sPair[] = iter1.next();
                HashMap<String, Boolean> hm = hmURIs.get(sPair[0]);
                if (hm == null) {
                    hm = new HashMap<String, Boolean>();
                    hmURIs.put(sPair[0], hm);
                }
                if (hm.get(sPair[1]) == null) {
                    iPairs++;
                }
                hm.put(sPair[1], false);
            }
            System.out.println("Setup.initIndividualPairs( " + sProp + " ) ... " + iPropPairs + " -> " + iPairs);
        }
        // property chains
        ResultSet results = m_database.query(m_sqlFactory.selectPropertyChainsQuery());
        while (results.next()) {
            String sProp1 = results.getString("uri1");
            String sProp2 = results.getString("uri2");
            ResultPairsIterator iter = m_engine.queryPairs(m_sparqlFactory.propertyChainExtensionQuery(sProp1, sProp2),
                    this.filter.getIndividualsFilter());
            int iChainPairs = 0;
            while (iter.hasNext()) {
                iChainPairs++;
                String sPair[] = iter.next();
                HashMap<String, Boolean> hm = hmURIs.get(sPair[0]);
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
                    "Setup.initIndividualPairs( " + sProp1 + ", " + sProp2 + " ) ... " + iChainPairs + " -> " + iPairs);
        }
        int id = 0;
        // individual_pairs
        for (String sInd1 : hmURIs.keySet()) {
            HashMap<String, Boolean> hm = hmURIs.get(sInd1);
            for (String sInd2 : hm.keySet()) {
                String sName1 = getLocalName(sInd1);
                String sName2 = getLocalName(sInd2);
                boolean bExt = hm.get(sInd2);
                if (!bExt) {
                    m_database.execute(m_sqlFactory.insertIndividualPairQuery(id, sInd1, sInd2, sName1, sName2));
                    m_database.execute(m_sqlFactory.insertIndividualPairExtQuery(id, sInd1, sInd2, sName1, sName2));
                    id++;
                }
            }
        }
        // individual_pairs_ext
        for (String sInd1 : hmURIs.keySet()) {
            HashMap<String, Boolean> hm = hmURIs.get(sInd1);
            for (String sInd2 : hm.keySet()) {
                String sName1 = getLocalName(sInd1);
                String sName2 = getLocalName(sInd2);
                boolean bExt = hm.get(sInd2);
                if (bExt) {
                    m_database.execute(m_sqlFactory.insertIndividualPairExtQuery(id, sInd1, sInd2, sName1, sName2));
                    id++;
                }
            }
        }
        System.out.println("Setup: done");
    }

    public void initIndividualsTables() throws Exception {
        HashMap<String, Integer> hmURI2Ext = new HashMap<String, Integer>();
        // class
        String sQuery1 = m_sqlFactory.selectClassesQuery();
        ResultSet results1 = m_database.query(sQuery1);
        while (results1.next()) {
            String sClass = results1.getString("uri");
            ResultsIterator iter = m_engine
                    .query(m_sparqlFactory.classExtensionQuery(sClass), this.filter.getIndividualsFilter());
            int iClassInd = 0;
            while (iter.hasNext()) {
                iClassInd++;
                String sInd = iter.next();
                hmURI2Ext.put(sInd, 3);
            }
            System.out.println("Setup.initIndividuals( " + sClass + " ) ... " + iClassInd + " -> " + hmURI2Ext.size());
        }
        // exists property class
        String sQuery2 = m_sqlFactory.selectClassesExtQuery();
        ResultSet results2 = m_database.query(sQuery2);
        while (results2.next()) {
            String sClass = results2.getString("class_uri");
            String sProp = results2.getString("prop_uri");
            ResultsIterator iter = m_engine.query(m_sparqlFactory.existsPropertyExtensionQuery(sProp, sClass),
                    this.filter.getIndividualsFilter());
            int iClassInd = 0;
            while (iter.hasNext()) {
                iClassInd++;
                String sInd = iter.next();
                hmURI2Ext.put(sInd, 2);
            }
            System.out.println("Setup.initIndividuals( " + sProp + ", " + sClass + " ) ... " + iClassInd + " -> " +
                    hmURI2Ext.size());
        }
        // exists property top, exists inverse property top
        String sQuery3 = m_sqlFactory.selectPropertiesQuery();
        ResultSet results3 = m_database.query(sQuery1);
        while (results3.next()) {
            String sProp = results3.getString("uri");
            ResultsIterator iter1 = m_engine
                    .query(m_sparqlFactory.existsPropertyExtensionQuery(sProp), this.filter.getIndividualsFilter());
            int iClassInd = 0;
            while (iter1.hasNext()) {
                iClassInd++;
                String sInd = iter1.next();
                hmURI2Ext.put(sInd, 1);
            }
            ResultsIterator iter2 = m_engine.query(m_sparqlFactory.existsInversePropertyExtensionQuery(sProp),
                    this.filter.getIndividualsFilter());
            while (iter2.hasNext()) {
                iClassInd++;
                String sInd = iter2.next();
                hmURI2Ext.put(sInd, 1);
            }
            System.out.println("Setup.initIndividuals( " + sProp + " ) ... " + iClassInd + " -> " + hmURI2Ext.size());
        }
        int id = 0;
        for (String sInd : hmURI2Ext.keySet()) {
            int iExt = hmURI2Ext.get(sInd);
            String sName = getLocalName(sInd);
            if (iExt == 1) {
                m_database.execute(m_sqlFactory.insertIndividualExtExtQuery(id, sInd, sName));
            }
            else if (iExt == 2) {
                m_database.execute(m_sqlFactory.insertIndividualExtExtQuery(id, sInd, sName));
                m_database.execute(m_sqlFactory.insertIndividualExtQuery(id, sInd, sName));
            }
            else if (iExt == 3) {
                m_database.execute(m_sqlFactory.insertIndividualExtExtQuery(id, sInd, sName));
                m_database.execute(m_sqlFactory.insertIndividualExtQuery(id, sInd, sName));
                m_database.execute(m_sqlFactory.insertIndividualQuery(id, sInd, sName));
            }
            id++;
        }
        System.out.println("Setup: done");
    }

    public void initIndividualPairsTable() throws SQLException {
        HashMap<String, HashMap<String, Boolean>> hmURIs = new HashMap<String, HashMap<String, Boolean>>();
        // read properties from database
        String sQuery1 = m_sqlFactory.selectPropertiesQuery();
        ResultSet results = m_database.query(sQuery1);
        int iPairs = 0;
        while (results.next()) {
            String sProp = results.getString("uri");
            ResultPairsIterator iter = m_engine
                    .queryPairs(m_sparqlFactory.propertyExtensionQuery(sProp), this.filter.getIndividualsFilter());
            int iPropPairs = 0;
            while (iter.hasNext()) {
                iPropPairs++;
                String sPair[] = iter.next();
                HashMap<String, Boolean> hm = hmURIs.get(sPair[0]);
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
        for (String sInd1 : hmURIs.keySet()) {
            HashMap<String, Boolean> hm = hmURIs.get(sInd1);
            for (String sInd2 : hm.keySet()) {
                String sName1 = getLocalName(sInd1);
                String sName2 = getLocalName(sInd2);
                // String sCountIndPropertiesQuery = m_sparqlFactory.countPropertiesQuery( sInd1, sInd2 );
                // int iCount = m_engine.count( sCountIndPropertiesQuery );
                String sQuery2 = m_sqlFactory.insertIndividualPairQuery(id, sInd1, sInd2, sName1, sName2);
                m_database.execute(sQuery2);
                id++;
            }
        }
    }

    public void initIndividualsTable() throws SQLException {
        HashMap<String, String> hmURI2Name = new HashMap<String, String>();
        // read classes from database
        String sQuery1 = m_sqlFactory.selectClassesQuery();
        ResultSet results = m_database.query(sQuery1);
        while (results.next()) {
            String sClass = results.getString("uri");
            int iClassID = results.getInt("id");
            String sQuery3 = m_sparqlFactory.classExtensionQuery(sClass);
            System.out.println("\n" + sClass + " (" + iClassID + ")");
            ResultsIterator iter = m_engine.query(sQuery3, this.filter.getIndividualsFilter());
            int iClassInd = 0;
            while (iter.hasNext()) {
                iClassInd++;
                String sInd = iter.next();
                hmURI2Name.put(sInd, getLocalName(sInd));
            }
            System.out.println("Setup.initIndividuals( " + sClass + " ) ... " + iClassInd + " -> " + hmURI2Name.size());
        }
        int id = 0;
        m_database.setAutoCommit(false);
        for (String sInd : hmURI2Name.keySet()) {
            String sName = hmURI2Name.get(sInd);
            String sQuery2 = m_sqlFactory.insertIndividualQuery(id, sInd, sName);
            //System.out.println("\nQUERY: " + sQuery2);

            m_database.execute(sQuery2);
            id++;
            if (id % 1000 == 0) {
                m_database.commit();
            }
        }
        m_database.setAutoCommit(true);
        System.out.println("done: " + id);
    }

    public int getIndividualID(String sURI) throws Exception {
        sURI = checkURISyntax(sURI);
        String sQuery = m_sqlFactory.selectIndividualIDQuery(sURI);
        ResultSet results = m_database.query(sQuery);
        if (results.next()) {
            return results.getInt("id");
        }
        return -1;
    }
}
