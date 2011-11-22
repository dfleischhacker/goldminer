package miner.database;

import java.io.*;
import java.util.*;

import java.sql.ResultSet;
import java.sql.SQLException;

import miner.util.*;
import miner.sparql.*;


public class TablePrinter {
	
	// SPARQL
	
	private QueryEngine m_engine;
	
	private SPARQLFactory m_sparqlFactory;
	
	// DATABASE
	
	private Database m_database;
	
	private miner.database.SQLFactory m_sqlFactory;
	
	// caching of (atomic) class ids
	
	private HashMap<String,String> m_hmClass2ID;
	
	// caching of property ids
	
	private HashMap<String,String> m_hmProp2ID;
	private HashMap<String, String> m_hmProp2DisID;
	private HashMap<String, String> m_hmProp2InvID;
	
	// caching of (complex) exists property ids
	
	private HashMap<String,HashMap<String,String>> m_hmProp2Class2ID;
	
	// caching of property chain ids
	
	private HashMap<String,HashMap<String,String>> m_hmProp2Prop2ID;
	
	private String classesFilter = "http://bibleontology.com";
	
	private String individualsFilter = "";
	
	
	public static void main( String args[] ) throws SQLException, FileNotFoundException, IOException {
		Settings.load();
		//TablePrinter printer = new TablePrinter();
		//printer.printClassMembers( "instanceof.txt" );
		// printer.printPropertyMembers( "bible_property.txt" );
		// printer.printExistsPropertyMembers( "bible_ex_property.txt", Integer.parseInt( args[0] ) );
		//
		// printer.printPropertyChainMembers_final( "bible_property_chain.txt", Integer.parseInt( args[0] ) );
		// printer.printPropertyRestrictions( "bible_domain.txt", 0 );
		// printer.printPropertyRestrictions( "bible_range.txt", 1 );
		//
		// printer.printExistsPropertyNonMembers( "dbpedia_ex_property.txt", Integer.parseInt( args[0] ) );
		// printer.printPropertyChainMembersTrans( "dbpedia_property_trans.txt" );
	}
	
	public TablePrinter() throws SQLException {
		m_engine = new QueryEngine();
		m_sparqlFactory = new SPARQLFactory();
		m_database = Database.instance();
		m_sqlFactory = new SQLFactory();
	}
	
	public TablePrinter(Database d, String endpoint, String graph, int chunk) throws SQLException {
		m_engine = new QueryEngine(endpoint, graph, chunk);
		m_sparqlFactory = new SPARQLFactory();
		m_database = d;
		m_sqlFactory = new SQLFactory();
	}
	
	public void printPropertyChainMembersTrans( String sOutFile ) throws SQLException {
		// property chains
		HashMap<Integer,HashMap<String,HashMap<String,Boolean>>> chains = new HashMap<Integer,HashMap<String,HashMap<String,Boolean>>>();
		ResultSet results1 = m_database.query( m_sqlFactory.selectPropertyChainsTransQuery() );
		int iAllPairs = 0;
		while( results1.next() )
		{
			String sProp = results1.getString( "uri" );
			String sName = results1.getString( "name" );
			int iID = results1.getInt( "id" );
			HashMap<String,HashMap<String,Boolean>> hmChainDomain2Ranges = new HashMap<String,HashMap<String,Boolean>>();
			ResultPairsIterator iter = m_engine.queryPairs( m_sparqlFactory.propertyChainExtensionQuery( sProp, sProp ), this.individualsFilter );
			// System.out.println( "\nproperty chain extension: "+ sProp );
			int iPairs = 0;
			while( iter.hasNext() )
			{
				iPairs++;
				String sPair[] = (String[]) iter.next();
				// System.out.println( sPair[0] +" / "+ sPair[1] );
				HashMap<String,Boolean> hmRanges = hmChainDomain2Ranges.get( sPair[0] );
				if( hmRanges == null )
				{
					hmRanges = new HashMap<String,Boolean>();
					hmChainDomain2Ranges.put( sPair[0], hmRanges );
				}
				if( hmRanges.get( sPair[1] ) == null ) iAllPairs++;
				hmRanges.put( sPair[1], true );
			}
			chains.put( iID, hmChainDomain2Ranges );
			System.out.println( "printPropertyChainMembers( "+ sProp +" ) ... "+ iPairs +" -> "+ iAllPairs );
		}
		// properties
		HashMap<Integer,HashMap<String,HashMap<String,Boolean>>> properties = new HashMap<Integer,HashMap<String,HashMap<String,Boolean>>>();
		ResultSet results2 = m_database.query( m_sqlFactory.selectPropertiesQuery() );
		iAllPairs = 0;
		while( results2.next() )
		{
			String sProp = results2.getString( "uri" );
			String sName = results2.getString( "name" );
			int iID = results2.getInt( "id" );
			HashMap<String,HashMap<String,Boolean>> hmPropDomain2Ranges = new HashMap<String,HashMap<String,Boolean>>();
			ResultPairsIterator iter = m_engine.queryPairs( m_sparqlFactory.propertyExtensionQuery( sProp ), this.individualsFilter );
			// System.out.println( "\nproperty extension: "+ sProp );
			int iPairs = 0;
			while( iter.hasNext() )
			{
				iPairs++;
				String sPair[] = (String[]) iter.next();
				// System.out.println( sPair[0] +" / "+ sPair[1] );
				HashMap<String,Boolean> hmRanges = hmPropDomain2Ranges.get( sPair[0] );
				if( hmRanges == null )
				{
					hmRanges = new HashMap<String,Boolean>();
					hmPropDomain2Ranges.put( sPair[0], hmRanges );
				}
				if( hmRanges.get( sPair[1] ) == null ) iAllPairs++;
				hmRanges.put( sPair[1], true );
			}
			properties.put( iID, hmPropDomain2Ranges );
			System.out.println( "printPropertyMembers( "+ sProp +" ) ... "+ iPairs +" -> "+ iAllPairs );
		}			
		String sQuery = m_sqlFactory.selectIndividualPairsTransQuery();
		ResultSet results3 = m_database.query( sQuery );
		while( results3.next() )
		{
			int iIndPairID = results3.getInt( "id" );
			String sIndURI1 = results3.getString( "uri1" );
			String sIndURI2 = results3.getString( "uri2" );
			StringBuffer sbLine = new StringBuffer();
			// property chains
			for( Integer iChainID: chains.keySet() )
			{
				HashMap<String,HashMap<String,Boolean>> hmChainDomain2Ranges = chains.get( iChainID );
				HashMap<String,Boolean> hmRanges = hmChainDomain2Ranges.get( sIndURI1 );
				if( hmRanges != null && hmRanges.get( sIndURI2 ) != null ){
					sbLine.append( iChainID +"\t" );
				}
			}
			// properties
			for( Integer iPropID: properties.keySet() ) 
			{
				HashMap<String,HashMap<String,Boolean>> hmPropDomain2Ranges = properties.get( iPropID );
				HashMap<String,Boolean> hmRanges = hmPropDomain2Ranges.get( sIndURI1 );
				if( hmRanges != null && hmRanges.get( sIndURI2 ) != null ){
					sbLine.append( iPropID +"\t" );
				}
			}
			System.out.println( "TablePrinter.print: "+ sIndURI1 +" / "+ sIndURI2 +" ("+ iIndPairID +") -> "+ sbLine.toString() );
		}
		System.out.println( "done" );
	}
	
	public void printPropertyChainMembers_final( String sOutFile, int iStart ) throws Exception {
		// property chains
		HashMap<Integer,HashMap<String,HashMap<String,Boolean>>> chains = new HashMap<Integer,HashMap<String,HashMap<String,Boolean>>>();
		ResultSet results1 = m_database.query( m_sqlFactory.selectPropertyChainsQuery() );
		int iAllPairs = 0;
		while( results1.next() )
		{
			String sProp1 = results1.getString( "uri1" );
			String sProp2 = results1.getString( "uri2" );
			int iID = results1.getInt( "id" );
			HashMap<String,HashMap<String,Boolean>> hmChainDomain2Ranges = new HashMap<String,HashMap<String,Boolean>>();
			ResultPairsIterator iter = m_engine.queryPairs( m_sparqlFactory.propertyChainExtensionQuery( sProp1, sProp2 ), this.individualsFilter );
			// System.out.println( "\nproperty chain extension: "+ sProp1 +" / "+ sProp2 );
			int iPairs = 0;
			while( iter.hasNext() )
			{
				iPairs++;
				String sPair[] = (String[]) iter.next();
				// System.out.println( sPair[0] +" / "+ sPair[1] );
				HashMap<String,Boolean> hmRanges = hmChainDomain2Ranges.get( sPair[0] );
				if( hmRanges == null )
				{
					hmRanges = new HashMap<String,Boolean>();
					hmChainDomain2Ranges.put( sPair[0], hmRanges );
				}
				if( hmRanges.get( sPair[1] ) == null ) iAllPairs++;
				hmRanges.put( sPair[1], true );
			}
			chains.put( iID, hmChainDomain2Ranges );
			System.out.println( "printPropertyChainMembers( "+ sProp1 +", "+ sProp2 +" ) ... "+ iPairs +" -> "+ iAllPairs );
		}
		String sQuery = m_sqlFactory.selectIndividualPairsExtQuery( iStart, iStart + 1000000 );
		ResultSet results2 = m_database.query( sQuery );
		while( results2.next() )
		{
			int iIndPairID = results2.getInt( "id" );
			String sIndURI1 = results2.getString( "uri1" );
			String sIndURI2 = results2.getString( "uri2" );
			StringBuffer sbLine = new StringBuffer();
			boolean bComplex = false;
			for( Integer iChainID: chains.keySet() )
			{
				HashMap<String,HashMap<String,Boolean>> hmChainDomain2Ranges = chains.get( iChainID );
				HashMap<String,Boolean> hmRanges = hmChainDomain2Ranges.get( sIndURI1 );
				if( hmRanges != null && hmRanges.get( sIndURI2 ) != null )
				{
					sbLine.append( iChainID +"\t" );
					bComplex = true;
				}
			}
			// if( bComplex )
			// {
				// properties
				ResultsIterator iter1 = m_engine.query( m_sparqlFactory.individualPropertiesQuery( sIndURI1, sIndURI2 ), this.classesFilter );
				while( iter1.hasNext() ) 
				{
					String sProp = (String) iter1.next();
					String sPropID = getPropertyID( sProp );
					if( sPropID != null )
					{
						sbLine.append( sPropID +"\t" );
						if( iter1.hasNext() ){
							sbLine.append( "\t" );
						}
					}
				}
				if( sbLine.length() > 0 )
				{
					System.out.println( "TablePrinter.print: "+ sIndURI1 +" / "+ sIndURI2 +" ("+ iIndPairID +") -> "+ sbLine.toString() );
					// chunk.add( sbLine.toString() );
				}
			//}
		}
	}
	
	public void printPropertyChainMembers_new( String sOutFile ) throws Exception {
		// property chains
		HashMap<Integer,HashMap<String,HashMap<String,Boolean>>> chains = new HashMap<Integer,HashMap<String,HashMap<String,Boolean>>>();
		ResultSet results1 = m_database.query( m_sqlFactory.selectPropertyChainsQuery() );
		while( results1.next() )
		{
			String sProp1 = results1.getString( "uri1" );
			String sProp2 = results1.getString( "uri2" );
			int iID = Integer.parseInt( getPropertyChainID( sProp1, sProp2 ) );
			HashMap<String,HashMap<String,Boolean>> hmChainDomain2Ranges = new HashMap<String,HashMap<String,Boolean>>();
			ResultPairsIterator iter = m_engine.queryPairs( m_sparqlFactory.propertyChainExtensionQuery( sProp1, sProp2 ), this.individualsFilter );
			int iPairs = 0;
			while( iter.hasNext() )
			{
				String sPair[] = (String[]) iter.next();
				HashMap<String,Boolean> hmRanges = hmChainDomain2Ranges.get( sPair[0] );
				if( hmRanges == null )
				{
					hmRanges = new HashMap<String,Boolean>();
					hmChainDomain2Ranges.put( sPair[0], hmRanges );
				}
				iPairs++;
				hmRanges.put( sPair[1], true );
			}
			chains.put( iID, hmChainDomain2Ranges );
			System.out.println( "printPropertyChainMembers( "+ sProp1 +", "+ sProp2 +" ) ... "+ iPairs );
		}
		// one hashmap per property: ind -> ind -> boolean
		String properties[] = getProperties();
		HashMap[] hmProp2Ext = new HashMap[properties.length];
		for( int i=0; i<properties.length; i++ )
		{
			String sProp = properties[i];
			HashMap<String,HashMap<String,Boolean>> hmInd2Inds = new HashMap<String,HashMap<String,Boolean>>();
			ResultPairsIterator iter = m_engine.queryPairs( m_sparqlFactory.propertyExtensionQuery( sProp ), this.individualsFilter );
			int iPairs = 0;
			while( iter.hasNext() ) 
			{
				String sPair[] = (String[]) iter.next();
				HashMap<String,Boolean> hmInds = hmInd2Inds.get( sPair[0] );
				if( hmInds == null )
				{
					hmInds = new HashMap<String,Boolean>();
					hmInd2Inds.put( sPair[0], hmInds );
				}
				iPairs++;
				hmInds.put( sPair[1], true );
			}
			hmProp2Ext[i] = hmInd2Inds;
			System.out.println( "printPropertyMembers( "+ sProp +" ) ... "+ iPairs );
		}
		String sQuery = m_sqlFactory.selectIndividualPairsExtQuery();
		ResultSet results2 = m_database.query( sQuery );
		while( results2.next() )
		{
			int iIndPairID = results2.getInt( "id" );
			String sIndURI1 = results2.getString( "uri1" );
			String sIndURI2 = results2.getString( "uri2" );
			StringBuffer sbLine = new StringBuffer();
			boolean bComplex = false;
			for( Integer iChainID: chains.keySet() )
			{
				HashMap<String,HashMap<String,Boolean>> hmChainDomain2Ranges = chains.get( iChainID );
				HashMap<String,Boolean> hmRanges = hmChainDomain2Ranges.get( sIndURI1 );
				if( hmRanges != null && hmRanges.get( sIndURI2 ) != null )
				{
					sbLine.append( iChainID +"\t" );
					bComplex = true;
				}
			}
			if( bComplex )
			{
				for( int i=0; i<properties.length; i++ )
				{
					String sProp = properties[i];
					HashMap<String,Boolean> hmRanges = (HashMap<String,Boolean>) hmProp2Ext[i].get( sIndURI1 );
					if( hmRanges != null && hmRanges.get( sIndURI2 ) != null )
					{
						String sPropID = getPropertyID( sProp );
						if( sPropID != null ){
							sbLine.append( sPropID +"\t" );
						}
					}
				}
				if( sbLine.length() > 0 ){
					System.out.println( "TablePrinter.print: "+ sIndURI1 +" / "+ sIndURI2 +" ("+ iIndPairID +") -> "+ sbLine.toString() );
				}
			}
		}
	}
	
	public void printPropertyRestrictions( String sOutFile, int iInverse ) throws SQLException, IOException {
		String properties[] = getProperties();
		// two hashmaps for each property: domain and range
		HashMap hmRanges[] = new HashMap[properties.length];
		HashMap hmDomains[] = new HashMap[properties.length];
		for( int i=0; i<properties.length; i++ )
		{
			String sProp = properties[i];
			System.out.println( "printPropertyRestrictions: "+ sProp );
			hmRanges[i] = new HashMap<String,Boolean>();
			hmDomains[i] = new HashMap<String,Boolean>();
			ResultPairsIterator iter = m_engine.queryPairs( m_sparqlFactory.propertyExtensionQuery( sProp ), this.individualsFilter );
			while( iter.hasNext() ) 
			{
				String sPair[] = (String[]) iter.next();
				hmDomains[i].put( sPair[0], true );
				hmRanges[i].put( sPair[1], true );
			}
		}
		String sQuery = m_sqlFactory.selectIndividualsQuery();
		ResultSet results = m_database.query( sQuery );
		ArrayList<String> chunk = new ArrayList<String>();
		HashMap<String,Integer> hmPropTops = getExistsPropertyTops( iInverse );
		int iDone = 0;
		while( results.next() )
		{
			int iIndID = results.getInt( "id" );
			String sIndURI = results.getString( "uri" );
			StringBuffer sbLine = new StringBuffer();
			boolean bComplex = false;
			for( int i=0; i<properties.length; i++ )
			{
				if( ( iInverse == 0 && hmDomains[i].get( sIndURI ) != null )
				   || ( iInverse == 1 && hmRanges[i].get( sIndURI ) != null ) )
				{
					String sPropURI = properties[i];
					int iPropID = hmPropTops.get( sPropURI );
					sbLine.append( iPropID +"\t" );
					bComplex = true;
				}
			}
			// if( bComplex )
			//{
				ResultsIterator iter = m_engine.query( m_sparqlFactory.individualClassesQuery( sIndURI ), this.classesFilter );
				while( iter.hasNext() ) 
				{
					String sClass = (String) iter.next();
					String sClassID = getClassID( sClass );
					if( sClassID != null )
					{
						sbLine.append( sClassID );
						if( iter.hasNext() ){
							sbLine.append( "\t" );
						}
					}
				}
			//}
			iDone++;
			if( sbLine.length() > 0 )
			{
				System.out.println( "TablePrinter.print: "+ sIndURI +" ("+ iIndID +") -> "+ sbLine.toString() );
				chunk.add( sbLine.toString() );
			}
		}
		System.out.println( "TablePrinter.write: "+ sOutFile );
		write( sOutFile, chunk );
		System.out.println( "TablePrinter: done ("+ iDone +")" );
	}
	
	public HashMap<String,Integer> getExistsPropertyTops( int iInverse ) throws SQLException {
		HashMap<String,Integer> hmPropTops = new HashMap<String,Integer>();
		String sQuery = m_sqlFactory.selectPropertyRestrictionsQuery( iInverse );
		ResultSet results = m_database.query( sQuery );
		while( results.next() )
		{
			String sPropURI = results.getString( "uri" );
			int iPropID = results.getInt( "id" );
			hmPropTops.put( sPropURI, iPropID );
		}
		return hmPropTops;
	}
	
	public void printPropertyChainMembersTrans_new(String outFile) throws SQLException, IOException {
		String properties[] = this.getProperties();
		HashMap<String, HashMap<String, String>> propertyChains =  this.getPropertyChains_new();
		HashMap<String, HashMap<String, String>> propertyChainsTrans = this.getPropertyChainsTrans();
		HashMap<String, List<String>>[] hmProp = new HashMap[properties.length];
		HashMap<String, HashMap<String, List<String>>> hmPropChain = new HashMap<String, HashMap<String, List<String>>>();
		HashMap<String, HashMap<String, List<String>>> hmPropChainTrans = new HashMap<String, HashMap<String, List<String>>>();
		for(int i = 0; i < properties.length; i++) {
			String sProp = properties[i];
			HashMap<String, List<String>> hmInds = new HashMap<String, List<String>>();
			ResultPairsIterator iter = m_engine.queryPairs(m_sparqlFactory.propertyExtensionQuery(sProp), this.individualsFilter);
			while(iter.hasNext()) {
				String sPair[] = (String[])iter.next();
				List<String> inds = hmInds.get(sPair[0]);
				if(inds == null) {
					inds = new ArrayList<String>();
					hmInds.put(sPair[0], inds);
				}
				inds.add(sPair[1]);
			}
			hmProp[i] = hmInds;
		}
		for(String id : propertyChains.keySet()) {
			String prop1 = propertyChains.get(id).keySet().iterator().next();
			String prop2 = propertyChains.get(id).values().iterator().next();
			HashMap<String, List<String>> propChainInds = new HashMap<String, List<String>>();
			ResultPairsIterator iter = m_engine.queryPairs(m_sparqlFactory.propertyChainExtensionQuery(prop1, prop2), this.individualsFilter);
			System.out.println(id);
			while(iter.hasNext()) {
				String sPair[] = (String[])iter.next();
				List<String> inds = propChainInds.get(sPair[0]);
				if(inds == null) {
					inds = new ArrayList<String>();
					propChainInds.put(sPair[0], inds);
				}
				inds.add(sPair[1]);
			}
			hmPropChain.put(id, propChainInds);
		}
		for(String id : propertyChainsTrans.keySet()) {
			String prop1 = propertyChainsTrans.get(id).keySet().iterator().next();
			String prop2 = propertyChainsTrans.get(id).values().iterator().next();
			HashMap<String, List<String>> propChainTransInds = new HashMap<String, List<String>>();
			ResultPairsIterator iter = m_engine.queryPairs(m_sparqlFactory.propertyChainExtensionQuery(prop1, prop2), this.individualsFilter);
			while(iter.hasNext()) {
				String sPair[] = (String[])iter.next();
				List<String> inds = propChainTransInds.get(sPair[0]);
				if(inds == null) {
					inds = new ArrayList<String>();
					propChainTransInds.put(sPair[0], inds);
				}
				inds.add(sPair[1]);
			}
			hmPropChainTrans.put(id, propChainTransInds);
		}
		String sQuery1 = m_sqlFactory.selectIndividualPairsQuery();
		ResultSet results = m_database.query( sQuery1 );
		ArrayList<String> chunk = new ArrayList<String>();
		while( results.next() ) {
			String sInd1 = results.getString( "uri1" );
			String sInd2 = results.getString( "uri2" );
			StringBuffer sbLine = new StringBuffer();
			for( int i=0; i<properties.length; i++ ){
				List<String> hmInd2Inds = hmProp[i].get( sInd1 );
				if( hmInd2Inds != null )
				{
					if(hmInd2Inds.contains(sInd2)) {
						String sPropID = getPropertyID( properties[i] );
						sbLine.append( sPropID + "\t" );
					}
				}
			}
			for(String id : hmPropChain.keySet()) {
				List<String> list = hmPropChain.get(id).get(sInd1);
				if(list != null) {
					if(list.contains(sInd2)) {
						sbLine.append(id + "\t");
					}
				}
			}
			for(String id : hmPropChainTrans.keySet()) {
				List<String> list = hmPropChainTrans.get(id).get(sInd1);
				if(list != null) {
					if(list.contains(sInd2)) {
						sbLine.append(id + "\t");
					}
				}
			}
			if( sbLine.length() > 0 )
			{
				chunk.add( sbLine.toString() );
			}
		}
		System.out.println( "TablePrinter.write: "+ outFile );
		write( outFile, chunk );
		System.out.println( "TablePrinter: done" );
	}
	
	public void printPropertyFunctionalMembers(String sOutFile) throws SQLException, IOException {
		String properties[] = this.getProperties();
		HashMap<String, List<String>>[] hm = new HashMap[properties.length];
		for(int i = 0; i < properties.length; i++) {
			String sProp = properties[i];
			HashMap<String, List<String>> hmInds = new HashMap<String, List<String>>();
			ResultPairsIterator iter = m_engine.queryPairs(m_sparqlFactory.propertyExtensionQuery(sProp), this.individualsFilter);
			while(iter.hasNext()) {
				String sPair[] = (String[])iter.next();
				List<String> inds = hmInds.get(sPair[0]);
				if(inds == null) {
					inds = new ArrayList<String>();
					hmInds.put(sPair[0], inds);
				}
				inds.add(sPair[1]);
			}
			hm[i] = hmInds;
		}
		String sQuery = m_sqlFactory.selectIndividualsQuery();
		ResultSet results = m_database.query(sQuery);
		ArrayList<String> chunk = new ArrayList<String>();
		while(results.next()) {
			String sId = results.getString("id");
			String sInd = results.getString("uri");
			StringBuffer sbLine = new StringBuffer();
			for( int i=0; i<properties.length; i++ )
			{
				List<String> list = hm[i].get( sInd );
				if( list != null) {
					String sPropID = getPropertyID(properties[i]);
					sbLine.append(sPropID + "\t");
					if(list.size() == 1) {
						String sPropID2 = getPropertyDisjointID( properties[i] );
						sbLine.append( sPropID2 +"\t" );
					}
				}
			} 
			if( sbLine.length() > 0 )
			{
				System.out.println( "TablePrinter.print: 1="+ sInd +" ("+ sId +") -> "+ sbLine.toString() );
				chunk.add( sbLine.toString() );
			}
		}
		System.out.println( "TablePrinter.write: "+ sOutFile );
		write( sOutFile, chunk );
		System.out.println( "TablePrinter: done" );
	}
	
	public void printPropertyInverseFunctionalMembers(String sOutFile) throws SQLException, IOException {
		String properties[] = this.getProperties();
		HashMap<String, List<String>>[] hm = new HashMap[properties.length];
		for(int i = 0; i < properties.length; i++) {
			String sProp = properties[i];
			HashMap<String, List<String>> hmInds = new HashMap<String, List<String>>();
			ResultPairsIterator iter = m_engine.queryPairs(m_sparqlFactory.propertyExtensionQuery(sProp), this.individualsFilter);
			while(iter.hasNext()) {
				String sPair[] = (String[])iter.next();
				List<String> inds = hmInds.get(sPair[0]);
				if(inds == null) {
					inds = new ArrayList<String>();
					hmInds.put(sPair[0], inds);
				}
				inds.add(sPair[1]);
			}
			hm[i] = hmInds;
		}
		String sQuery = m_sqlFactory.selectIndividualsQuery();
		ResultSet results = m_database.query(sQuery);
		ArrayList<String> chunk = new ArrayList<String>();
		while(results.next()) {
			String sId = results.getString("id");
			String sInd = results.getString("uri");
			StringBuffer sbLine = new StringBuffer();
			for( int i=0; i<properties.length; i++ )
			{
				HashMap<String, List<String>> all = hm[i];
				Collection<List<String>> values = all.values();
				if(values != null) {
					int count = 0;
					for(List<String> value : values) {
						for(String s : value) {
							if(s.equals(sInd)) {
								count++;
							}
						}
					}
					if(count > 0) {
						String sPropID = getPropertyID(properties[i]);
						sbLine.append(sPropID + "\t");
					}
					if(count == 1) {
						String sPropID = this.getPropertySymmetryID(properties[i]);
						sbLine.append(sPropID + "\t");
					} 
				}
			} 
			if( sbLine.length() > 0 )
			{
				System.out.println( "TablePrinter.print: 1="+ sInd +" ("+ sId +") -> "+ sbLine.toString() );
				chunk.add( sbLine.toString() );
			}
		}
		System.out.println( "TablePrinter.write: "+ sOutFile );
		write( sOutFile, chunk );
		System.out.println( "TablePrinter: done" );
	}
	
	public void printPropertyInverseMembers( String sOutFile ) throws SQLException, IOException {
		String properties[] = this.getProperties();
		HashMap<String, List<String>>[] hmProp2Ext = new HashMap[properties.length];
		for( int i = 0; i < properties.length; i++) {
			String sProp = properties[i];
			HashMap<String, List<String>> hmInd2Inds = new HashMap<String, List<String>>();
			ResultPairsIterator iter = m_engine.queryPairs(m_sparqlFactory.propertyExtensionQuery( sProp ), this.individualsFilter);
			while(iter.hasNext() ) {
				String sPair[] = (String[]) iter.next();
				List<String> inds = hmInd2Inds.get(sPair[0]);
				if(inds == null) {
					inds = new ArrayList<String>();
					hmInd2Inds.put(sPair[0], inds);
				}
				inds.add(sPair[1]);
			}
			hmProp2Ext[i] = hmInd2Inds;
		}
		String sQuery1 = m_sqlFactory.selectIndividualPairsQuery();
		ResultSet results = m_database.query( sQuery1 );
		ArrayList<String> chunk = new ArrayList<String>();
		while( results.next() )
		{
			String sId = results.getString( "id" );
			String sInd1 = results.getString( "uri1" );
			String sInd2 = results.getString( "uri2" );
			StringBuffer sbLine = new StringBuffer();
			for( int i=0; i<properties.length; i++ ){
				List<String> hmInd2Inds = hmProp2Ext[i].get( sInd1 );
				if( hmInd2Inds != null )
				{
					if(hmInd2Inds.contains(sInd2)) {
						String sPropID = getPropertyID( properties[i] );
						sbLine.append( sPropID + "\t" );
						List<String> hmInd2Inds2 = hmProp2Ext[i].get( sInd2 );
						if(hmInd2Inds2 != null) {
							if(!hmInd2Inds2.contains( sInd1 )) {
								String sPropID2 = this.getPropertySymmetryID( properties[i]);
								sbLine.append(sPropID2 + "\t");
							}
						} else {
							String sPropID2 = this.getPropertySymmetryID( properties[i]);
							sbLine.append(sPropID2 + "\t");
						}
					}
				}
				List<String> hmInd2Inds2 = hmProp2Ext[i].get( sInd2 );
				if(hmInd2Inds2 != null) {
					if(hmInd2Inds2.contains( sInd1 )) {
						String sPropID = this.getPropertyDisjointID( properties[i] );
						sbLine.append(sPropID + "\t");
					}
				}
			}
			if( sbLine.length() > 0 )
			{
				System.out.println( "TablePrinter.print: 1="+ sInd1 +" 2="+ sInd2 +" ("+ sId +") -> "+ sbLine.toString() );
				chunk.add( sbLine.toString() );
			}
		}
		System.out.println( "TablePrinter.write: "+ sOutFile );
		write( sOutFile, chunk );
		System.out.println( "TablePrinter: done" );
	}
	
	public void printPropertyMembers( String sOutFile ) throws SQLException, IOException {
		// one hashmap per property: ind -> ind -> boolean
		String properties[] = getProperties();
		HashMap[] hmProp2Ext = new HashMap[properties.length];
		for( int i=0; i<properties.length; i++ )
		{
			String sProp = properties[i];
			System.out.println( "printPropertyMembers( "+ sProp +" ) ..." ); 
			HashMap<String,HashMap<String,Boolean>> hmInd2Inds = new HashMap<String,HashMap<String,Boolean>>();
			ResultPairsIterator iter = m_engine.queryPairs( m_sparqlFactory.propertyExtensionQuery( sProp ), this.individualsFilter );
			while( iter.hasNext() ) 
			{
				String sPair[] = (String[]) iter.next();
				HashMap<String,Boolean> hmInds = hmInd2Inds.get( sPair[0] );
				if( hmInds == null )
				{
					hmInds = new HashMap<String,Boolean>();
					hmInd2Inds.put( sPair[0], hmInds );
				}
				hmInds.put( sPair[1], true );
			}
			hmProp2Ext[i] = hmInd2Inds;
		}
		// for each pair: for each property: in extension?
		String sQuery1 = m_sqlFactory.selectIndividualPairsQuery();
		ResultSet results = m_database.query( sQuery1 );
		ArrayList<String> chunk = new ArrayList<String>();
		while( results.next() )
		{
			String sId = results.getString( "id" );
			String sInd1 = results.getString( "uri1" );
			String sInd2 = results.getString( "uri2" );
			StringBuffer sbLine = new StringBuffer();
			for( int i=0; i<properties.length; i++ )
			{
				HashMap<String,Boolean> hmInd2Inds = (HashMap<String,Boolean>)hmProp2Ext[i].get( sInd1 );
				if( hmInd2Inds != null )
				{
					Boolean bExt = hmInd2Inds.get( sInd2 );
					if( bExt != null )
					{
						String sPropID = getPropertyID( properties[i] );
						sbLine.append( sPropID +"\t" );
					} else {
						String sPropID = getPropertyDisjointID(properties[i]);
						sbLine.append( sPropID + "\t");
					}
				} else {
					String sPropID = getPropertyDisjointID( properties[i] );
					sbLine.append( sPropID + "\t");
				}
			}
			if( sbLine.length() > 0 )
			{
				System.out.println( "TablePrinter.print: 1="+ sInd1 +" 2="+ sInd2 +" ("+ sId +") -> "+ sbLine.toString() );
				chunk.add( sbLine.toString() );
			}
		}
		System.out.println( "TablePrinter.write: "+ sOutFile );
		write( sOutFile, chunk );
		System.out.println( "TablePrinter: done" );
	}
	
	public void printPropertyReflexivity(String sOutFile) throws SQLException, IOException {
		String query = m_sqlFactory.selectIndividualsQuery();
		ResultSet results = m_database.query(query);
		ArrayList<String> chunk = new ArrayList<String>();
		while(results.next()) {
			String[] properties = this.getProperties();
			StringBuffer sb = new StringBuffer();
			sb.append("0\t");
			ResultsIterator iter = m_engine.query(m_sparqlFactory.getPairProperty(results.getString("uri")), this.individualsFilter);
			while(iter.hasNext()) {
				String s = iter.next();
				sb.append(this.getPropertyID(s) + "\t");
				for(int i = 0; i < properties.length; i++) {
					if(properties[i].equals(s)) {
						properties[i] = "";
					}
				}
			}
			for(int i = 0; i < properties.length; i++) {
				if(!properties[i].equals("")) {
					sb.append(this.getPropertyDisjointID(properties[i]));
					if(i != (properties.length - 1)) {
						sb.append("\t");
					}
				}
			}
			if(sb.length() > 0) {
				chunk.add(sb.toString());
			}
		}
		write( sOutFile, chunk );
	}
	
	public String[] getProperties() throws SQLException {
		ArrayList<String> properties = new ArrayList<String>();
		String sQuery = m_sqlFactory.selectPropertiesQuery();
		ResultSet results = m_database.query( sQuery );
		while( results.next() ){
			properties.add( results.getString( "uri" ) );
		}
		return properties.toArray( new String[properties.size()] );
	}
	
	public void printPropertyChainMembers( String sOutFile ) throws Exception {
		ArrayList<String> chunk = new ArrayList<String>();
		HashMap<String,HashMap<String,String>> hmChains = getPropertyChains();
		// read individual pairs from table individual_pairs_ext
		String sQuery1 = m_sqlFactory.selectIndividualPairsExtQuery();
		ResultSet results1 = m_database.query( sQuery1 );
		while( results1.next() )
		{
			String sId = results1.getString( "id" );
			String sInd1 = results1.getString( "uri1" );
			String sInd2 = results1.getString( "uri2" );
			StringBuffer sbLine = new StringBuffer();
			boolean bComplex = false;
			// property chains
			for( String sProp1: hmChains.keySet() )
			{
				HashMap<String,String> hmSecond = hmChains.get( sProp1 );
				if( hmSecond == null ) continue;
				for( String sProp2: hmSecond.keySet() )
				{
					ResultsIterator iter2 = m_engine.query( m_sparqlFactory.propertyChainsQuery( sInd1, sProp1, sProp2, sInd2 ), this.classesFilter );
					if( iter2.hasNext() )
					{ 
						String sChainID = hmSecond.get( sProp2 );
						System.out.println( sChainID +" = "+ sProp1 +" / "+ sProp2 );
						sbLine.append( sChainID +"\t" );
						bComplex = true;
					}
				}
			}
			if( bComplex )
			{
				// properties
				ResultsIterator iter1 = m_engine.query( m_sparqlFactory.individualPropertiesQuery( sInd1, sInd2 ), this.classesFilter );
				while( iter1.hasNext() ) 
				{
					String sProp = (String) iter1.next();
					String sPropID = getPropertyID( sProp );
					if( sPropID != null )
					{
						sbLine.append( sPropID );
						if( iter1.hasNext() ){
							sbLine.append( "\t" );
						}
					}
				}
				if( sbLine.length() > 0 )
				{
					System.out.println( "TablePrinter.print: "+ sInd1 +" / "+ sInd2 +" ("+ sId +") -> "+ sbLine.toString() );
					// chunk.add( sbLine.toString() );
				}
			}
		}
		// System.out.println( "TablePrinter.write: "+ sOutFile );
		// write( sOutFile, chunk );
		// System.out.println( "TablePrinter: done ("+ chunk.size() +")" );
	}
	
	public void printPropertyChainMembers_Memory( String sOutFile ) throws Exception {
		String properties[] = getProperties();
		// two hashmaps for each property: domain and range
		HashMap hmRanges[] = new HashMap[properties.length];
		HashMap hmDomains[] = new HashMap[properties.length];
		for( int i=0; i<properties.length; i++ )
		{
			String sProp = properties[i];
			System.out.println( "printPropertyChainMembers ("+ i +"): "+ sProp );
			hmRanges[i] = new HashMap<String,String>();
			hmDomains[i] = new HashMap<String,String>();
			ResultPairsIterator iter = m_engine.queryPairs( m_sparqlFactory.propertyExtensionQuery( sProp ), this.individualsFilter );
			while( iter.hasNext() ) 
			{
				String sPair[] = (String[]) iter.next();
				// property
				HashMap<String,Boolean> hmDomainRanges = (HashMap<String,Boolean>) hmDomains[i].get( sPair[0] );
				if( hmDomainRanges == null )
				{
					hmDomainRanges = new HashMap<String,Boolean>();
					hmDomains[i].put( sPair[0], hmDomainRanges );
				}
				hmDomainRanges.put( sPair[1], true );
				// inverse property
				HashMap<String,Boolean> hmRangeDomains = (HashMap<String,Boolean>) hmRanges[i].get( sPair[1] );
				if( hmRangeDomains == null )
				{
					hmRangeDomains = new HashMap<String,Boolean>();
					hmRanges[i].put( sPair[1], hmRangeDomains );
				}
				hmRangeDomains.put( sPair[0], true );
			}
		}
		String sQuery = m_sqlFactory.selectIndividualPairsExtQuery();
		ResultSet results = m_database.query( sQuery );
		ArrayList<String> chunk = new ArrayList<String>();
		while( results.next() )
		{
			int iIndPairID = results.getInt( "id" );
			String sIndURI1 = results.getString( "uri1" );
			String sIndURI2 = results.getString( "uri2" );
			StringBuffer sbLine = new StringBuffer();
			boolean bComplex = false;
			for( int i=0; i<properties.length; i++ ){
				for( int j=0; j<properties.length; j++ )
				{
					HashMap<String,Boolean> hmDomainRanges = (HashMap<String,Boolean>) hmDomains[i].get( sIndURI1 );
					HashMap<String,Boolean> hmRangeDomains = (HashMap<String,Boolean>) hmRanges[j].get( sIndURI2 );
					if( hmDomainRanges == null || hmRangeDomains == null ){
						continue;
					}
					for( String sRange: hmDomainRanges.keySet() ){
						for( String sDomain: hmRangeDomains.keySet() )
						{
							if( sDomain.equals( sRange ) )
							{
								String sChainID = getPropertyChainID( properties[i], properties[j] );
								if( sChainID != null )
								{
									System.out.println( sChainID +" = "+ properties[i] +" / "+ properties[j] );
									sbLine.append( sChainID +"\t" );
									bComplex = true;
								}
							}
						}
					}
				}
			}
			if( bComplex )
			{
				// properties
				ResultsIterator iter = m_engine.query( m_sparqlFactory.individualPropertiesQuery( sIndURI1, sIndURI2 ), this.classesFilter );
				while( iter.hasNext() ) 
				{
					String sProp = (String) iter.next();
					String sPropID = getPropertyID( sProp );
					if( sPropID != null )
					{
						sbLine.append( sPropID );
						if( iter.hasNext() ){
							sbLine.append( "\t" );
						}
					}
				}
				if( sbLine.length() > 0 )
				{
					System.out.println( "TablePrinter.print: ind1="+ sIndURI1 +" ind2="+ sIndURI2 +" ("+ iIndPairID +") -> "+ sbLine.toString() );
					// chunk.add( sbLine.toString() );
				}
			}
		}
	}
	
	public String getLocalName( String sURI ){
		int iLabel = sURI.lastIndexOf( "#" );
		if( iLabel == -1 ){
			iLabel = sURI.lastIndexOf( "/" );
		}
		if( iLabel != -1 ){
			return sURI.substring( iLabel+1 );
		}
		return "";
	}
	
	/* public boolean printExistsPropertyMembers_Memory( int iStart, int iEnd, String sOutFile, String properties[], HashMap hmProp2Ext ) throws Exception {
		String sQuery1 = m_sqlFactory.selectIndividualsQuery( iStart, iEnd );
		ResultSet results = m_database.query( sQuery1 );
		ArrayList<String> chunk = new ArrayList<String>();
		while( results.next() )
		{
			String sId = results.getString( "id" );
			String sInd = results.getString( "uri" );
			StringBuffer sbLine = new StringBuffer();
			boolean bComplex = false;
			for( int i=0; i<properties.length; i++ )
			{
				HashMap<String,Boolean> hmClasses = (HashMap<String,Boolean>) hmProp2Ext[i].get( sInd );
				Iterator iter = hmClasses.keySet().iterator();
				while( iter.hasNext() )
				{
					String sClass = (String) iter.next();
					String sProp = properties[i];
					String sExPropID = getExistsPropertyID( sProp, sClass );
					if( sExPropID != null )
					{
						sbLine.append( sExPropID );
						if( iter.hasNext() ){
							sbLine.append( "\t" );
						}
						bComplex = true;
					}
				}
			}
			if( bComplex )
			{
				if( !sbLine.toString().endsWith( "\t" ) ){
					sbLine.append( "\t" );
				}
				// get individual atomic classes
				ResultsIterator iter1 = m_engine.query( m_sparqlFactory.individualClassesQuery( sInd ) );
				while( iter1.hasNext() ) 
				{
					String sClass = (String) iter1.next();
					String sClassID = getClassID( sClass );
					if( sClassID != null )
					{
						sbLine.append( sClassID );
						if( iter1.hasNext() ){
							sbLine.append( "\t" );
						}
					}
				}
				if( sbLine.length() > 0 )
				{
					System.out.println( "TablePrinter.print: "+ sInd +" ("+ sId +") -> "+ sbLine.toString() );
					chunk.add( sbLine.toString() );
				}
			}
		}
		if( chunk.size() == 0 ){
			return true;
		}
		chunk.add( "\n" );
		System.out.println( "TablePrinter.write: "+ sOutFile );
		write( sOutFile, chunk );
		System.out.println( "TablePrinter: done ("+ chunk.size() +")" );
		return false;
	} */
	
	/*public void printExistsPropertyMembers( String sOutFile, int iStart ) throws Exception {
	    // int iStart = 0;
	    int iChunk = 50000;
		// one hashmap per property: ind -> class -> boolean
		String properties[] = getProperties();
		HashMap[] hmProp2Ext = new HashMap[properties.length];
		for( int i=0; i<properties.length; i++ )
		{
			String sProp = properties[i];
			System.out.println( "printExistsPropertyMembers( "+ sProp +" ) ..." ); 
			HashMap<String,HashMap<String,Boolean>> hmInd2Classes = new HashMap<String,HashMap<String,Boolean>>();
			ResultPairsIterator iter = m_engine.queryPairs( m_sparqlFactory.propertyExtensionClassesQuery( sProp ) );
			while( iter.hasNext() ) 
			{
				String sPair[] = (String[]) iter.next();
				HashMap<String,Boolean> hmClasses = hmInd2Classes.get( sPair[0] );
				if( hmClasses == null )
				{
					hmClasses = new HashMap<String,Boolean>();
					hmInd2Classes.put( sPair[0], hmClasses );
				}
				hmClasses.put( sPair[1], true );
			}
			hmProp2Ext[i] = hmInd2Classes;
		}
		// create chunks
		boolean bDone = false;
		while( !bDone )
		{
			System.out.println( "TablePrinter: start="+ iStart +" chunk="+ iChunk );
			String sFile = iStart +"-"+ sOutFile;
			int iEnd = iStart + iChunk;
			bDone = printExistsPropertyMembers( iStart, iEnd, sFile, properties, hmProp2Ext );
			iStart += iChunk;
		}
		System.out.println( "TablePrinter: done!" );
	}*/
	
	public void printExistsPropertyMembers( String sOutFile, int iStart ) throws SQLException, IOException {
		int iChunk = 100000;
		boolean bDone = false;
		while( !bDone )
		{
			System.out.println( "TablePrinter: start="+ iStart +" chunk="+ iChunk );
			String sFile = sOutFile;
			int iEnd = iStart + iChunk;
			bDone = printExistsPropertyMembers( iStart, iEnd, sFile );
			iStart += iChunk;
		}
		System.out.println( "TablePrinter: done!" );
	}
	
	public boolean printExistsPropertyMembers( int iStart, int iEnd, String sOutFile ) throws SQLException, IOException {
		// read individuals from database
		String sQuery1 = m_sqlFactory.selectIndividualsQuery( iStart, iEnd );
		ResultSet results = m_database.query( sQuery1 );
		ArrayList<String> chunk = new ArrayList<String>();
		while( results.next() )
		{
			String sId = results.getString( "id" );
			String sInd = results.getString( "uri" );
			StringBuffer sbLine = new StringBuffer();
			// get individual complex classes
			ResultPairsIterator iter2 = m_engine.queryPairs( m_sparqlFactory.individualExistsPropertyQuery( sInd ), "" );
			while( iter2.hasNext() ) 
			{
				String sPropClass[] = (String[]) iter2.next();
				String sClass = sPropClass[1];
				String sProp = sPropClass[0];
				// TODO: filter duplicates?
				String sExPropID = getExistsPropertyID( sProp, sClass );
				if( sExPropID != null )
				{
				    sbLine.append( sExPropID );
					sbLine.append( "\t" );
				}
			}
			// get individual atomic classes
			ResultsIterator iter1 = m_engine.query( m_sparqlFactory.individualClassesQuery( sInd ), "" );
			while( iter1.hasNext() ) 
			{
				String sClass = (String) iter1.next();
				String sClassID = getClassID( sClass );
				if( sClassID != null )
				{
					sbLine.append( sClassID );
					sbLine.append( "\t" );
				}
			}
			if( sbLine.length() > 0 )
			{
				System.out.println( "TablePrinter.print: "+ sInd +" ("+ sId +") -> "+ sbLine.toString() );
				chunk.add( sbLine.toString() );
			}
		}
		//if( chunk.size() == 0 ){
			//return true;
		//}
		chunk.add( new String( "\n" ) );
		System.out.println( "TablePrinter.write: "+ sOutFile );
		write( sOutFile, chunk );
		System.out.println( "TablePrinter: done ("+ chunk.size() +")" );
		return true;
	}
	
	public void printExistsPropertyNonMembers( String sOutFile, int iStart ) throws Exception {
		int iChunk = 100000;
		boolean bDone = false;
		while( !bDone )
		{
			System.out.println( "TablePrinter.printExistsPropertyNonMembers: start="+ iStart +" chunk="+ iChunk );
			String sFile = sOutFile;
			int iEnd = iStart + iChunk;
			bDone = printExistsPropertyNonMembers( iStart, iEnd, sFile );
			iStart += iChunk;
		}
		System.out.println( "TablePrinter: done!" );
	}
	
	public boolean printExistsPropertyNonMembers( int iStart, int iEnd, String sOutFile ) throws Exception {
		// read individuals from database
		String sQuery1 = m_sqlFactory.selectIndividualsQuery( iStart, iEnd );
		ResultSet results = m_database.query( sQuery1 );
		ArrayList<String> chunk = new ArrayList<String>();
		while( results.next() )
		{
			String sId = results.getString( "id" );
			String sInd = results.getString( "uri" );
			StringBuffer sbLine = new StringBuffer();
			boolean bComplex = false;
			// get individual complex classes
			ResultPairsIterator iter2 = m_engine.queryPairs( m_sparqlFactory.individualExistsPropertyQuery( sInd ), this.classesFilter );
			while( iter2.hasNext() ) 
			{
				String sPropClass[] = (String[]) iter2.next();
				String sClass = sPropClass[1];
				String sProp = sPropClass[0];
				// TODO: filter duplicates?
				String sExPropID = getExistsPropertyID( sProp, sClass );
				if( sExPropID != null )
				{
				    // sbLine.append( sExPropID );
					// if( iter2.hasNext() ){
					//	sbLine.append( "\t" );
					//}
					bComplex = true;
					break;
				}
			}
			if( !bComplex )
			{
				//if( !sbLine.toString().endsWith( "\t" ) ){
				//	sbLine.append( "\t" );
				//}
				// get individual atomic classes
				ResultsIterator iter1 = m_engine.query( m_sparqlFactory.individualClassesQuery( sInd ), this.classesFilter );
				while( iter1.hasNext() ) 
				{
					String sClass = (String) iter1.next();
					String sClassID = getClassID( sClass );
					if( sClassID != null )
					{
						sbLine.append( sClassID );
						if( iter1.hasNext() ){
							sbLine.append( "\t" );
						}
					}
				}
				if( sbLine.length() > 0 )
				{
					System.out.println( "TablePrinter.print: "+ sInd +" ("+ sId +") -> "+ sbLine.toString() );
					chunk.add( sbLine.toString() );
				}
			}
		}
		//if( chunk.size() == 0 ){
			//return true;
		//}
		chunk.add( new String( "\n" ) );
		System.out.println( "TablePrinter.write: "+ sOutFile );
		write( sOutFile, chunk );
		System.out.println( "TablePrinter: done ("+ chunk.size() +")" );
		return true;
	}
	
	public void printClassMembers( String sOutFile ) throws SQLException, IOException {
		// read individuals from database
		String sQuery1 = m_sqlFactory.selectIndividualsQuery();
		ResultSet results = m_database.query( sQuery1 );
		ArrayList<String> chunk = new ArrayList<String>();
		int iDone = 0;
		while( results.next() )
		{
			String sId = results.getString( "id" );
			String sInd = results.getString( "uri" );
			StringBuffer sbLine = new StringBuffer();
			// get individual classes
			ResultsIterator iter = m_engine.query( m_sparqlFactory.individualClassesQuery( sInd ), "");
			while( iter.hasNext() ) 
			{
				String sClass = (String) iter.next();
				String sClassID = getClassID( sClass );
				if( sClassID != null )
				{
					sbLine.append( sClassID );
					sbLine.append( "\t" );
				}
			}
			iDone++;
			if( sbLine.length() > 0 )
			{
				System.out.println( "TablePrinter.print: "+ sInd +" ("+ sId +") -> "+ sbLine.toString() );
				chunk.add( sbLine.toString() );
			}
		}
		System.out.println( "TablePrinter.write: "+ sOutFile );
		write( sOutFile, chunk );
		System.out.println( "TablePrinter: done ("+ iDone +")" );
	}
		
	public boolean write( String sOutFile, List<String> lines ) throws IOException  {
		BufferedWriter out = new BufferedWriter( new FileWriter( sOutFile, true ) );
		try {
			for( String sLine: lines )
			{
				if( sLine.trim().length() == 0 ){
					continue;
				}
				out.write( sLine );
				out.newLine();
			}
			out.flush();
			out.close();
			return true;
		} 
		catch( Exception ex ) {
			ex.printStackTrace();
		} 
		/* finally {
			try {
				if( out != null ) {
					out.flush();
					out.close();
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			}
			return false;
		} */
		return false;
	}
	
	public String getClassID( String sURI ) throws SQLException {
		if( m_hmClass2ID == null )
		{
			m_hmClass2ID = new HashMap<String,String>();
			ResultSet results = m_database.query( m_sqlFactory.selectClassesQuery() );
			while( results.next() )
			{
				String sClass = results.getString( "uri" );
				String sID = results.getString( "id" );
				m_hmClass2ID.put( sClass, sID );
			}
		}
		return m_hmClass2ID.get( sURI );
	}
	
	public String getIndividualPairID( String sURI1, String sURI2 ) throws Exception {
		ResultSet results = m_database.query( m_sqlFactory.selectIndividualPairIDQuery( sURI1, sURI2 ) );
		if( results.next() ){
			return results.getString( "id" );
		}
		return null;
	}
	
	public String getPropertyID( String sURI ) throws SQLException {
		if( m_hmProp2ID == null )
		{
			m_hmProp2ID = new HashMap<String,String>();
			ResultSet results = m_database.query( m_sqlFactory.selectPropertiesQuery() );
			while( results.next() )
			{
				String sProp = results.getString( "uri" );
				String sID = results.getString( "id" );
				m_hmProp2ID.put( sProp, sID );
			}
		}
		return m_hmProp2ID.get( sURI );
	}
	
	public String getPropertyDisjointID( String sURI) throws SQLException {
		if(m_hmProp2DisID == null) {
			m_hmProp2DisID = new HashMap<String, String>();
			ResultSet results = m_database.query( m_sqlFactory.selectPropertiesQuery() );
			while( results.next() ) {
				String sProp = results.getString( "uri" );
				String sID = results.getString( "disjointID" );
				m_hmProp2DisID.put( sProp, sID );
			}
		}
		return m_hmProp2DisID.get( sURI );
	}
	
	public String getPropertySymmetryID( String sURI) throws SQLException {
		if(m_hmProp2InvID == null) {
			m_hmProp2InvID = new HashMap<String, String>();
			ResultSet results = m_database.query( m_sqlFactory.selectPropertiesQuery() );
			while( results.next() ) {
				String sProp = results.getString( "uri" );
				String sID = results.getString( "symmetryID" );
				m_hmProp2InvID.put( sProp, sID );
			}
		}
		return m_hmProp2InvID.get( sURI );
	}
	
	public HashMap<String,HashMap<String,String>> getPropertyChains() throws SQLException {
		HashMap<String,HashMap<String,String>> hmProp2Prop2ID = new HashMap<String,HashMap<String,String>>();
		ResultSet results = m_database.query( m_sqlFactory.selectPropertyChainsQuery() );
		while( results.next() )
		{
			String sID = results.getString( "id" );
			String sURI1 = results.getString( "uri1" );
			String sURI2 = results.getString( "uri2" );
			HashMap<String,String> hmProp2ID = hmProp2Prop2ID.get( sURI1 );
			if( hmProp2ID == null )
			{
				hmProp2ID = new HashMap<String,String>();
				hmProp2Prop2ID.put( sURI1, hmProp2ID );
			}
			hmProp2ID.put( sURI2, sID );
		}
		return hmProp2Prop2ID;
	}
	
	public HashMap<String,HashMap<String,String>> getPropertyChains_new() throws SQLException {
		HashMap<String,HashMap<String,String>> hmProp2Prop2ID = new HashMap<String,HashMap<String,String>>();
		ResultSet results = m_database.query( m_sqlFactory.selectPropertyChainsQuery() );
		while( results.next() )
		{
			String sID = results.getString( "id" );
			String sURI1 = results.getString( "uri1" );
			String sURI2 = results.getString( "uri2" );
			HashMap<String,String> hmProp2ID = hmProp2Prop2ID.get( sID );
			if( hmProp2ID == null )
			{
				hmProp2ID = new HashMap<String,String>();
				hmProp2Prop2ID.put( sID, hmProp2ID );
			}
			hmProp2ID.put( sURI1, sURI2 );
		}
		return hmProp2Prop2ID;
	}
	
	public HashMap<String, HashMap<String, String>> getPropertyChainsTrans() throws SQLException {
		HashMap<String, HashMap<String, String>> result = new HashMap<String, HashMap<String, String>>();
		ResultSet results = m_database.query(m_sqlFactory.selectPropertyChainsTransQuery());
		while(results.next()) {
			String sID = results.getString("id");
			String uri = results.getString("uri");
			HashMap<String, String> hm = result.get(sID);
			if(hm == null) {
				hm = new HashMap<String, String>();
				result.put(sID, hm);
			}
			hm.put(uri, uri);
		}
		return result;
	}
	
	public String getPropertyChainID( String sProp1, String sProp2 ) throws SQLException {
		if( m_hmProp2Prop2ID == null )
		{
			m_hmProp2Prop2ID = new HashMap<String,HashMap<String,String>>();
			ResultSet results = m_database.query( m_sqlFactory.selectPropertyChainsQuery() );
			while( results.next() )
			{
				String sID = results.getString( "id" );
				String sURI1 = results.getString( "uri1" );
				String sURI2 = results.getString( "uri2" );
				HashMap<String,String> hmProp2ID = m_hmProp2Prop2ID.get( sURI1 );
				if( hmProp2ID == null )
				{
					hmProp2ID = new HashMap<String,String>();
					m_hmProp2Prop2ID.put( sURI1, hmProp2ID );
				}
				hmProp2ID.put( sURI2, sID );
			}
		}
		HashMap<String,String> hmProp2ID = m_hmProp2Prop2ID.get( sProp1 );
		if( hmProp2ID != null ){
			return hmProp2ID.get( sProp2 );
		}
		return null;
	}
		
	public String getExistsPropertyID( String sPropURI, String sClassURI ) throws SQLException  {
		String sClassID = getClassID( sClassURI );
		String sPropID = getPropertyID( sPropURI );
		if( sClassID == null || sPropID == null ){
			return null;
		}
		ResultSet results = m_database.query( m_sqlFactory.selectExistsPropertyIDQuery( sPropURI, sClassURI ) );
		if( results.next() ){
			return results.getString( "id" );
		}
		return null;
	}
	
	private String checkURISyntax( String sURI ){
		String s = new String( sURI );
		s = s.replaceAll( "'", "_" );
		return s;
	}
}