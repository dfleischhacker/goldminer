package de.uni_mannheim.informatik.dws.goldminer.database;

import java.io.IOException;
import java.sql.SQLException;


public class Setup {
	
	private SQLDatabase sqlDatabase;
	
	private SQLFactory m_sqlFactory;
	
	public Setup() throws SQLException, IOException {
		sqlDatabase = SQLDatabase.instance();
		m_sqlFactory = new SQLFactory();
	}
	
	public Setup(SQLDatabase d) throws SQLException {
		sqlDatabase = d;
		m_sqlFactory = new SQLFactory();
	}
	
	/**
	 * sets up the database schema that is required for the terminology acquisition.
	 * @param classes is true if the acquisition of the classes is required.
	 * @return true if setup was successful, false otherwise.
	 */
	public boolean setupSchema(
			boolean classes, 
			boolean individuals, 
			boolean properties, 
			boolean classes_ex_property, 
			boolean classes_ex_property_top, 
			boolean individual_pairs, 
			boolean individual_pairs_trans,
			boolean property_chains,
			boolean property_chains_trans) {
		boolean result = true;
		if(classes) {
			String classesQuery = this.m_sqlFactory.createClassesTable();
			result = this.sqlDatabase.execute(classesQuery) && result;
		}
		if(individuals) {
			String individualsQuery = this.m_sqlFactory.createIndividualsTable();
			result = this.sqlDatabase.execute(individualsQuery) && result;
		}
		if(properties) {
			String propertiesQuery = this.m_sqlFactory.createPropertiesTable();
			result = this.sqlDatabase.execute(propertiesQuery);
		}
		if(classes_ex_property) {
			String classesExPropertyQuery = this.m_sqlFactory.createClassesExPropertyTable();
			result = this.sqlDatabase.execute(classesExPropertyQuery);
		}
		if(classes_ex_property_top) {
			String classesExPropertyTopQuery = this.m_sqlFactory.createClassesExPropertyTopTable();
			result = this.sqlDatabase.execute(classesExPropertyTopQuery);
		}
		if(individual_pairs) {
			String individualPairsQuery = this.m_sqlFactory.createIndividualPairsTable();
			result = this.sqlDatabase.execute(individualPairsQuery);
		}
		if(individual_pairs_trans) {
			String individualPairsTransQuery = this.m_sqlFactory.createIndividualPairsTransTable();
			result = this.sqlDatabase.execute(individualPairsTransQuery);
		}
		if(property_chains) {
			String propertyChainsQuery = this.m_sqlFactory.createPropertyChainsTable();
			result = this.sqlDatabase.execute(propertyChainsQuery);
		}
		if(property_chains_trans) {
			String propertyChainsTransQuery = this.m_sqlFactory.createPropertyChainsTransTable();
			result = this.sqlDatabase.execute(propertyChainsTransQuery);
		}
		return result;
	}
	
	/**
	 * removes all created tables for terminology acquisition from database.
	 * 
	 * @return true if successful, false otherwise.
	 */
	public boolean removeSchema() {
		String query = this.m_sqlFactory.dropTables();
		return this.sqlDatabase.execute(query);
	}
}