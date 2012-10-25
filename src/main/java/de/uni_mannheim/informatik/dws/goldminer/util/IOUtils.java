
package de.uni_mannheim.informatik.dws.goldminer.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Iterator;
import java.util.Set;


public class IOUtils {
	
	public static String toString( Set<? extends Object> set ){
		StringBuilder sb = new StringBuilder();
		sb.append( "[ " );
		Iterator iter = set.iterator();
		while( iter.hasNext() )
		{
			Object obj = iter.next();
			sb.append( obj.toString() );
			if( iter.hasNext() ){
				sb.append( ", " );
			}
		}
		sb.append( " ]" );
		return sb.toString();
	}
	
	public static String read( String sFile ) throws Exception {
		File file = new File( sFile );
		StringBuilder sb = new StringBuilder();
		BufferedReader reader = null;
		try {
			reader = new BufferedReader( new FileReader( file ) );
			String sLine = null;
			while( ( sLine = reader.readLine() ) != null ) {
                sb.append(sLine.trim()).append("\n");
			}
		}
		finally {
			if( reader != null ) {
				reader.close();
			}
		}
		return sb.toString();
	}
}