package miner.sparql;

import java.util.*;

import miner.util.*;


public class SPARQLFactory {
	
	private String m_sFilter = null;
	
	private final String PREFIX = 
	"PREFIX owl: <http://www.w3.org/2002/07/owl#> "+
	"PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> "+
	"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> "+
	"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "+
	"PREFIX dbpedia2: <http://dbpedia.org/property/> "+
	"PREFIX dbpedia1: <http://dbpedia.org/resource/> "+
	"PREFIX dbowl: <http://dbpedia.org/ontology/> "+
	"PREFIX skos: <http://www.w3.org/2004/02/skos/core#> ";
	// "PREFIX foaf: <http://xmlns.com/foaf/0.1/> "+
	// "PREFIX dc: <http://purl.org/dc/elements/1.1/> "+
	// "PREFIX yago: <http://dbpedia.org/class/yago/> "+
	// "PREFIX cyc: <http://sw.opencyc.org/2008/06/10/concept/en/> "+
	// "PREFIX fbase: <http://rdf.freebase.com/ns/> ";
	
	
	/* public SPARQLFactory(){
		m_sFilter = Settings.getString( Parameter.FILTER );
	} */
	
	// count classes
	public String countClassesQuery(){
		StringBuffer sb = new StringBuffer();
		sb.append( PREFIX +" " );
		sb.append( "SELECT (count(distinct ?x) AS ?count) WHERE {" );
		sb.append( " ?y a ?x ." );
		/* if( m_sFilter != null ){
			sb.append( " FILTER regex( ?x, '^"+ m_sFilter +"' ) ." );
		} */
		sb.append( " }" );
		return sb.toString();
	}
	
	// count properties connecting two individuals
	public String countPropertiesQuery( String sInd1, String sInd2 ){
		StringBuffer sb = new StringBuffer();
		sb.append( PREFIX +" " );
		sb.append( "SELECT (count(distinct ?x) AS ?count) WHERE {" );
		sb.append( "<"+ sInd1 +"> ?x <"+ sInd2 +"> ." );
		sb.append( " }" );
		return sb.toString();
	}
	
	// get classes
	public String classesQuery(){
		StringBuffer sb = new StringBuffer();
		sb.append( PREFIX +" " );
		sb.append( "SELECT distinct ?x" );
		sb.append( " WHERE {" );
		sb.append( " ?y a ?x ." );
		/* if( m_sFilter != null ){
			sb.append( " FILTER regex( ?x, '^"+ m_sFilter +"' ) ." );
		} */		
		sb.append( " }" );
		return sb.toString();
	}
	
	// get properties
	public String propertiesQuery(){
		StringBuffer sb = new StringBuffer();
		sb.append( PREFIX +" " );
		sb.append( "SELECT distinct ?x" );
		sb.append( " WHERE {" );
		sb.append( " ?y ?x ?z ." );
		sb.append( " ?z a ?zt ." );
		/* if( m_sFilter != null ){
		 sb.append( " FILTER regex( ?x, '^"+ m_sFilter +"' ) ." );
		 } */
		sb.append( " }" );
		return sb.toString();
	}
	
	// get properties
	public String propertiesQuery( String sInd ){
		StringBuffer sb = new StringBuffer();
		sb.append( PREFIX +" " );
		sb.append( "SELECT distinct ?x" );
		sb.append( " WHERE {" );
		sb.append( " <"+ sInd +"> ?x ?z ." );
		sb.append( " ?z a ?zt ." );
		/* if( m_sFilter != null ){
			sb.append( " FILTER regex( ?x, '^"+ m_sFilter +"' ) ." );
		} */
		sb.append( " }" );
		return sb.toString();
	}
	
	// get individuals related to this individual
	public String propertyIndividualsQuery( String sInd, String sProp ){
		StringBuffer sb = new StringBuffer();
		sb.append( PREFIX +" " );
		sb.append( "SELECT distinct ?x" );
		sb.append( " WHERE {" );
		sb.append( " <"+ sInd +"> <"+ sProp +"> ?x ." );
		sb.append( " }" );
		return sb.toString();
	}
	
	// count individuals related to this individual
	public String countPropertyIndividualsQuery( String sInd, String sProp ){
		StringBuffer sb = new StringBuffer();
		sb.append( PREFIX +" " );
		sb.append( "SELECT (count(distinct ?x) AS ?count) WHERE {" );
		sb.append( " <"+ sInd +"> <"+ sProp +"> ?x ." );
		sb.append( " }" );
		return sb.toString();
	}
	
	// count individuals related to this individual
	public String countInversePropertyIndividualsQuery( String sInd, String sProp ){
		StringBuffer sb = new StringBuffer();
		sb.append( PREFIX +" " );
		sb.append( "SELECT (count(distinct ?x) AS ?count) WHERE {" );
		sb.append( "?x <"+ sProp +"> <"+ sInd +"> ." );
		sb.append( " }" );
		return sb.toString();
	}
	
	// count individuals of this class
	public String classExtensionSizeQuery( String sClass ){
		StringBuffer sb = new StringBuffer();
		sb.append( PREFIX +" " );
		sb.append( "SELECT (count(distinct ?x) AS ?count) WHERE {" );
		sb.append( " ?x a <"+ sClass +"> ." );
		sb.append( " }" );
		return sb.toString();
	}
	
	// count individuals of this class
	public String classExtensionQuery( String sClass1, String sClass2 ){
		StringBuffer sb = new StringBuffer();
		sb.append( PREFIX +" " );
		sb.append( "SELECT distinct ?x WHERE {" );
		sb.append( " ?x a <"+ sClass1 +"> ." );
		sb.append( " ?x a <"+ sClass2 +"> ." );
		sb.append( " }" );
		return sb.toString();
	}
	
	// count individuals of this class
	public String classExtensionSizeQuery( String sClass1, String sClass2 ){
		StringBuffer sb = new StringBuffer();
		sb.append( PREFIX +" " );
		sb.append( "SELECT (count(distinct ?x) AS ?count) WHERE {" );
		sb.append( " ?x a <"+ sClass1 +"> ." );
		sb.append( " ?x a <"+ sClass2 +"> ." );
		sb.append( " }" );
		return sb.toString();
	}
	
	// count individuals in property domain
	public String propDomainExtensionSizeQuery( String sProp ){
		StringBuffer sb = new StringBuffer();
		sb.append( PREFIX +" " );
		sb.append( "SELECT (count(distinct ?x) AS ?count) WHERE {" );
		sb.append( " ?x <"+ sProp +"> ?y ." );
		sb.append( " ?y a ?yt ." );
		sb.append( " }" );
		return sb.toString();
	}
	
	// count individuals in property range
	public String propRangeExtensionSizeQuery( String sProp ){
		StringBuffer sb = new StringBuffer();
		sb.append( PREFIX +" " );
		sb.append( "SELECT (count(distinct ?x) AS ?count) WHERE {" );
		sb.append( " ?y <"+ sProp +"> ?x ." );
		sb.append( " ?x a ?xt ." );
		sb.append( " }" );
		return sb.toString();
	}
	
	// count individuals in exists property class
	public String existsPropertyExtensionSizeQuery( String sProp, String sClass ){
		StringBuffer sb = new StringBuffer();
		sb.append( PREFIX +" " );
		sb.append( "SELECT (count(distinct ?x) AS ?count) WHERE {" );
		sb.append( " ?x <"+ sProp +"> ?y ." );
		sb.append( " ?y a <"+ sClass +"> ." );
		sb.append( " }" );
		return sb.toString();
	}
	
	// get individuals in exists property class
	public String existsPropertyExtensionQuery( String sProp, String sClass ){
		StringBuffer sb = new StringBuffer();
		sb.append( PREFIX +" " );
		sb.append( "SELECT distinct ?x WHERE {" );
		sb.append( " ?x <"+ sProp +"> ?y ." );
		sb.append( " ?y a <"+ sClass +"> ." );
		sb.append( " }" );
		return sb.toString();
	}
	
	// get individuals in exists property top
	public String existsPropertyExtensionQuery( String sProp ){
		StringBuffer sb = new StringBuffer();
		sb.append( PREFIX +" " );
		sb.append( "SELECT distinct ?x WHERE {" );
		sb.append( " ?x <"+ sProp +"> ?y ." );
		sb.append( " }" );
		return sb.toString();
	}
	
	// get individuals in exists property top
	public String existsInversePropertyExtensionQuery( String sProp ){
		StringBuffer sb = new StringBuffer();
		sb.append( PREFIX +" " );
		sb.append( "SELECT distinct ?x WHERE {" );
		sb.append( " ?y <"+ sProp +"> ?x ." );
		sb.append( " }" );
		return sb.toString();
	}
	
	// get individuals in this class
	public String classExtensionQuery( String sClass ){
		StringBuffer sb = new StringBuffer();
		sb.append( PREFIX +" " );
		sb.append( "SELECT distinct ?x WHERE {" );
		sb.append( " ?x a <"+ sClass +"> ." );
		sb.append( " }" );
		return sb.toString();
	}
	
	// get individual pairs in this property
	public String propertyExtensionQuery( String sProp ){
		StringBuffer sb = new StringBuffer();
		sb.append( PREFIX +" " );
		sb.append( "SELECT distinct ?x ?y WHERE {" );
		sb.append( " ?x <"+ sProp +"> ?y ." );
		// sb.append( " ?y a ?yt ." );
		sb.append( " }" );
		return sb.toString();
	}
	
	// get pairs (domain individual, range class) for this property
	public String propertyExtensionClassesQuery( String sProp ){
		StringBuffer sb = new StringBuffer();
		sb.append( PREFIX +" " );
		sb.append( "SELECT distinct ?x ?y WHERE {" );
		sb.append( " ?x <"+ sProp +"> ?yi ." );
		sb.append( " ?yi a ?y ." );
		sb.append( " }" );
		return sb.toString();
	}
	
	// get individual pairs in this property chain
	public String propertyChainExtensionQuery( String sProp1, String sProp2 ){
		StringBuffer sb = new StringBuffer();
		sb.append( PREFIX +" " );
		sb.append( "SELECT distinct ?x ?y WHERE {" );
		sb.append( " ?x <"+ sProp1 +"> ?z ." );
		sb.append( " ?z <"+ sProp2 +"> ?y ." );
		// sb.append( " ?y a ?yt ." );
		sb.append( " }" );
		return sb.toString();
	}
	
	// get (atomic) classes for this individual
	public String individualClassesQuery( String sInd ){
		StringBuffer sb = new StringBuffer();
		sb.append( PREFIX +" " );
		sb.append( "SELECT distinct ?x WHERE {" );
		sb.append( " <"+ sInd +"> a ?x ." );
		sb.append( " }" );
		return sb.toString();
	}
	
	// get (complex) classes for this individual: exists x.y
	public String individualExistsPropertyQuery( String sInd ){
		StringBuffer sb = new StringBuffer();
		sb.append( PREFIX +" " );
		sb.append( "SELECT distinct ?x ?y WHERE {" );
		sb.append( " <"+ sInd +"> ?x ?iy ." );
		sb.append( " ?iy a ?y ." );
		sb.append( " }" );
		return sb.toString();
	}
	
	// get properties connecting the two individuals
	public String individualPropertiesQuery( String sInd1, String sInd2 ){
		StringBuffer sb = new StringBuffer();
		sb.append( PREFIX +" " );
		sb.append( "SELECT distinct ?x WHERE {" );
		sb.append( " <"+ sInd1 +"> ?x <"+ sInd2 +"> ." );
		sb.append( " }" );
		return sb.toString();
	}
	
	// get properties connecting the two individuals
	public String propertyQuery( String sInd1, String sProp, String sInd2 ){
		StringBuffer sb = new StringBuffer();
		sb.append( PREFIX +" " );
		sb.append( "SELECT distinct ?x WHERE {" );
		sb.append( " <"+ sInd1 +"> <"+ sProp +"> <"+ sInd2 +"> ." );
		sb.append( " <"+ sInd1 +"> a ?x ." ); // hack
		sb.append( " }" );
		return sb.toString();
	}
	
	// get property chains connecting the two individuals
	public String individualPropertyChainsQuery( String sInd1, String sInd2 ){
		StringBuffer sb = new StringBuffer();
		sb.append( PREFIX +" " );
		sb.append( "SELECT distinct ?x ?y WHERE {" );
		sb.append( " <"+ sInd1 +"> ?x ?z ." );
		sb.append( " ?z ?y <"+ sInd2 +"> ." );
		sb.append( " }" );
		return sb.toString();
	}
	
	// get property chains connecting the two individuals
	public String propertyChainsQuery( String sInd1, String sProp1, String sProp2, String sInd2 ){
		StringBuffer sb = new StringBuffer();
		sb.append( PREFIX +" " );
		sb.append( "SELECT distinct ?x WHERE {" );
		sb.append( " <"+ sInd1 +"> <"+ sProp1 +"> ?x ." );
		sb.append( " ?x <"+ sProp2 +"> <"+ sInd2 +"> ." );
		sb.append( " }" );
		return sb.toString();
	}
}