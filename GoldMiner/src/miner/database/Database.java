package miner.database;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;
import java.util.*;

import miner.util.*;


public class Database {
	
	private Connection m_connection;

	private static Database m_instance;
	

	public static Database instance() throws SQLException {
		if( m_instance == null ){
			m_instance = new Database();
		}
		return m_instance;
	}
	
	private Database(String url, String username, String password) throws SQLException {
		try {
			DriverManager.registerDriver( new com.mysql.jdbc.Driver() );
			System.out.println( "connection: "+ username +"@"+ url );
			m_connection = DriverManager.getConnection( url, username, password );
		}
		catch ( SQLException ex )
		{
			System.out.println( "SQLException: " + ex.getMessage() );
			System.out.println( "SQLState: " + ex.getSQLState() );
			System.out.println( "VendorError: " + ex.getErrorCode() );
			System.exit(0);
		}
	}

	private Database() throws SQLException {
		try {
			String sDatabase = Settings.getString( Parameter.DATABASE );
			String sUser = Settings.getString( Parameter.USER );
			String sPassword = Settings.getString( Parameter.PASSWORD );
			DriverManager.registerDriver( new com.mysql.jdbc.Driver() );
			System.out.println( "connection: "+ sUser +"@"+ sDatabase );
			m_connection = DriverManager.getConnection( sDatabase, sUser, sPassword );
		}
		catch ( SQLException ex )
		{
			System.out.println( "SQLException: " + ex.getMessage() );
			System.out.println( "SQLState: " + ex.getSQLState() );
			System.out.println( "VendorError: " + ex.getErrorCode() );
			throw ex;
		}
	}

	
	public static Database instance(String url, String username, String password) throws SQLException {
		if( m_instance == null ){
			m_instance = new Database(url, username, password);
		}
		return m_instance;
	}
	
	public void close() throws SQLException  {
		m_connection.close();
	}
	
	public ResultSet query( String sQuery ){
		// System.out.println( "Database.query: "+ sQuery );
		Statement stmt = null;
		ResultSet results = null;
		try {
			stmt = m_connection.createStatement();
            stmt.setFetchSize(5000);
			results = stmt.executeQuery( sQuery );
			return results;
		} 
		catch( SQLException ex ){
			System.out.println( "SQLException: " + ex.getMessage() );
			System.out.println( "SQLState: " + ex.getSQLState() );
			System.out.println( "VendorError: " + ex.getErrorCode() );
		}		
		/* finally {
			if ( results != null ) {
				try {
					results.close();
				} 
				catch ( SQLException sqlEx ) {
					sqlEx.printStackTrace();
				}
				results = null;
			}
			if ( stmt != null ) {
				try {
					stmt.close();
				} 
				catch ( SQLException sqlEx ) {
					sqlEx.printStackTrace();
				}
				stmt = null;
			}
		} */
		return null;
	}
		
	public boolean execute( String sUpdate ){
		// System.out.println( "Database.execute: "+ sUpdate );
		Statement stmt = null;
		ResultSet results = null;
		try {
			stmt = m_connection.createStatement();
			stmt.executeUpdate( sUpdate );
		} 
		catch( SQLException ex ){
			System.out.println( "SQLException: " + ex.getMessage() );
			System.out.println( "SQLState: " + ex.getSQLState() );
			System.out.println( "VendorError: " + ex.getErrorCode() );
			return false;
		}		
		finally {
			if ( results != null ) {
				try {
					results.close();
				} 
				catch ( SQLException sqlEx ) {
					sqlEx.printStackTrace();
				}
				results = null;
			}
			if ( stmt != null ) {
				try {
					stmt.close();
				} 
				catch ( SQLException sqlEx ) {
					sqlEx.printStackTrace();
				}
				stmt = null;
			}
		}
		return true;
	}
}
