
package miner.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.util.Properties;


public class Settings implements Parameter {
	
	private static String m_file;
	
	private static String a_file;

	private static Properties m_properties;
	
	private static Properties a_properties;
   
	static {
		m_properties = new Properties();
		a_properties = new Properties();
	}
	
	public final static String MINER_PROP = System.getProperty( "user.dir" ) + "/res/miner.properties";
	
	public final static String AXIOMS_PROP = System.getProperty( "user.dir" ) + "/res/axioms.properties";
	

	public static void load() throws IOException, FileNotFoundException {
		load( MINER_PROP, AXIOMS_PROP );
	}

	public static void load( String mFile, String aFile ) throws IOException, FileNotFoundException {
		if( !loaded() )
		{
			m_file = mFile;
			a_file = aFile;
			System.out.println( "Settings: loading configuration from file " + m_file );
			m_properties.load( new FileInputStream( new File( m_file ) ) );
			a_properties.load( new FileInputStream( new File(a_file)));
			System.out.println( "\nSettings: " + m_properties );
		}
	}

	public static String getString( String sKey ) {
		return m_properties.getProperty( sKey );
	}
   
	public static boolean getBoolean( String sKey ){
		return Boolean.parseBoolean( m_properties.getProperty( sKey ) );
	}
   
	public static int getInteger( String sKey ){
		return Integer.parseInt( m_properties.getProperty( sKey ) );
	}
	
	public static boolean getAxiom( String sKey) {
		return Boolean.parseBoolean( a_properties.getProperty(sKey));
	}

	public static void set( String sKey, String sValue ) {
		m_properties.setProperty( sKey, sValue );
	}

	public static void save( String sFile ) throws FileNotFoundException, IOException {
		FileOutputStream fos = new FileOutputStream( sFile );
		m_properties.store( fos, "" );
	}
	
	public static String getFile(){
		return m_file;
	}
	
	public static boolean loaded(){
		return ( m_properties.keySet().size() > 0 );
	}
}