package de.uni_mannheim.informatik.dws.goldminer.database;

import de.uni_mannheim.informatik.dws.goldminer.util.Parameter;
import de.uni_mannheim.informatik.dws.goldminer.util.Settings;

import java.sql.*;


public class Database {
	
	private Connection connection;

	private static Database instance;
	

	public static Database instance() throws SQLException {
		if( instance == null ){
			instance = new Database();
		}
		return instance;
	}
	
	private Database(String url, String username, String password) throws SQLException {
		try {
			DriverManager.registerDriver( new com.mysql.jdbc.Driver() );
			System.out.println( "connection: "+ username +"@"+ url );
			connection = DriverManager.getConnection( url, username, password );
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
			String database = Settings.getString(Parameter.DATABASE);
			String user = Settings.getString( Parameter.USER );
			String password = Settings.getString( Parameter.PASSWORD );
			DriverManager.registerDriver( new com.mysql.jdbc.Driver() );
			System.out.println( "connection: "+ user +"@"+ database );
			connection = DriverManager.getConnection( database, user, password );
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
		if( instance == null ){
			instance = new Database(url, username, password);
		}
		return instance;
	}
	
	public void close() throws SQLException  {
		connection.close();
	}
	
	public ResultSet query( String query ){
		System.out.println( "Database.query: "+ query );
		Statement stmt = null;
		ResultSet results = null;
		try {
			stmt = connection.createStatement();
            stmt.setFetchSize(5000);
			results = stmt.executeQuery( query );
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
		
	public boolean execute( String update ){
		// System.out.println( "Database.execute: "+ update );
		Statement stmt = null;
		ResultSet results = null;
		try {
			stmt = connection.createStatement();
			stmt.executeUpdate( update );
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

    public Connection getConnection() {
        return connection;
    }

    public static void main(String[] args) throws SQLException {
        Database d = Database.instance("jdbc:mysql://ede.informatik.uni-mannheim.de:3306/gold_minerEswc2012", "gold", "gold");
        for (int i = 0; i < 1000; i++) {
            ResultSet res = d.query("SELECT uri FROM property_chains_trans WHERE id='176999' UNION SELECT uri FROM properties WHERE " +
                                            "id='176999'");
            if (res.next()) {
                System.out.println(res.getString("uri"));
            }
        }
    }
}
