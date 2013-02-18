package de.uni_mannheim.informatik.dws.goldminer.sparql;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;


public class HTMLQueryEngine extends QueryEngine {

	public HTMLQueryEngine(String endpoint, String graph, int chunk) {
		super(endpoint, graph, chunk);
	}
	
	protected List<String[]> execute( String queryString, String sVar1, String sVar2, String filter ) throws Exception {
		String query = URLEncoder.encode( queryString,"UTF-8" );
		String urlString = "http://sparql.bibleontology.com/sparql.jsp?sparql="+query+"&type1=xml";
		URL url;
		BufferedReader br = null;
		ArrayList<String[]> values = new ArrayList<String[]>();
		try {
			url = new URL( urlString );
			URLConnection conn = url.openConnection ();
			br = new BufferedReader( new InputStreamReader( conn.getInputStream() ) );
			StringBuilder sb = new StringBuilder();
			String line;
			String sURI1 = null;
			String sURI2 = null;
			boolean b1 = false;
			boolean b2 = false;
			while ( ( line = br.readLine() ) != null)
			{
				if(line.contains("</result>"))
				{
					if( sURI1 != null && sURI2 != null )
					{
						String pair[] = { sURI1, sURI2 };
						values.add( pair );
					}
					sURI1 = null;
					sURI2 = null;
					b1 = false;
					b2 = false;
				}
				if(line.contains("binding name=\"" + sVar1 + "\"")){
					b1 = true;
					continue;
				}
				else if( b1 )
				{
					String s1 = getURI( line );
					if( s1 != null )
					{
						s1 = checkURISyntax( s1 );
						if( filter == null || s1.startsWith( filter ) ){
							sURI1 = s1;
						}
					}
					b1 = false;
					continue;
				}
				if(line.contains("binding name=\"" + sVar2 + "\"")){
					b2 = true;
					continue;
				}
				else if( b2 )
				{
					String s2 = getURI( line );
					if( s2 != null )
					{
						s2 = checkURISyntax( s2 );
						if( filter == null || s2.startsWith( filter ) ){
							sURI2 = s2;
						}
					}
					b2 = false;
					continue;
				}
			}
		} 
		catch (MalformedURLException e) {
			e.printStackTrace();
		} 
		catch (IOException e) {
			e.printStackTrace();
		} 
		finally {
			br.close();
		}
		return values;
	}
	
	protected List<String> execute( String queryString, String sVar, String filter ) throws UnsupportedEncodingException, IOException {
		String query = URLEncoder.encode( queryString,"UTF-8" );
		String urlString = "http://sparql.bibleontology.com/sparql.jsp?sparql="+query+"&type1=xml";
		URL url;
		BufferedReader br = null;
		ArrayList<String> values = new ArrayList<String>();
		try {
			url = new URL( urlString );
			URLConnection conn = url.openConnection ();
			br = new BufferedReader( new InputStreamReader( conn.getInputStream() ) );
			StringBuilder sb = new StringBuilder();
			String line;
			while ( ( line = br.readLine() ) != null)
			{
				String sURI = getURI( line );
				if( sURI != null )
				{
					sURI = checkURISyntax( sURI );
					if( filter == null || sURI.startsWith( filter ) ){
						values.add( sURI );
					}
				}
			}
		} 
		catch (MalformedURLException e) {
			e.printStackTrace();
		} 
		catch (IOException e) {
			e.printStackTrace();
		} 
		finally {
			br.close();
		}
		return values;
	}
	
	public String getURI( String s ){
		int iStart = s.indexOf( "<uri>" );
		int iEnd = s.indexOf( "</uri>" );
		if( iStart == -1 || iEnd == -1 ) return null;
		return s.substring( iStart+5, iEnd );
	}
}



