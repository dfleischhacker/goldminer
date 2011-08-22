
package miner.util;

import java.io.*;
import java.util.*;


public class IOUtils {
	
	public static String toString( Set<? extends Object> set ){
		StringBuffer sb = new StringBuffer();
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
		StringBuffer sb = new StringBuffer();
		BufferedReader reader = null;
		try {
			reader = new BufferedReader( new FileReader( file ) );
			String sLine = null;
			while( ( sLine = reader.readLine() ) != null ) {
				sb.append( sLine.trim()+"\n" );
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