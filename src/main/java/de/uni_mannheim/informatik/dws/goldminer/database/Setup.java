package de.uni_mannheim.informatik.dws.goldminer.database;

import java.io.IOException;
import java.sql.SQLException;


public class Setup {
	
	private Database m_database;
	
	private SQLFactory m_sqlFactory;
	
	public Setup() throws SQLException, IOException {
		m_database = Database.instance();
		m_sqlFactory = new SQLFactory();
	}
	
	public Setup(Database d) throws SQLException {
		m_database = d;
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
			result = this.m_database.execute(classesQuery) && result;
		}
		if(individuals) {
			String individualsQuery = this.m_sqlFactory.createIndividualsTable();
			result = this.m_database.execute(individualsQuery) && result;
		}
		if(properties) {
			String propertiesQuery = this.m_sqlFactory.createPropertiesTable();
			result = this.m_database.execute(propertiesQuery);
		}
		if(classes_ex_property) {
			String classesExPropertyQuery = this.m_sqlFactory.createClassesExPropertyTable();
			result = this.m_database.execute(classesExPropertyQuery);
		}
		if(classes_ex_property_top) {
			String classesExPropertyTopQuery = this.m_sqlFactory.createClassesExPropertyTopTable();
			result = this.m_database.execute(classesExPropertyTopQuery);
		}
		if(individual_pairs) {
			String individualPairsQuery = this.m_sqlFactory.createIndividualPairsTable();
			result = this.m_database.execute(individualPairsQuery);
		}
		if(individual_pairs_trans) {
			String individualPairsTransQuery = this.m_sqlFactory.createIndividualPairsTransTable();
			result = this.m_database.execute(individualPairsTransQuery);
		}
		if(property_chains) {
			String propertyChainsQuery = this.m_sqlFactory.createPropertyChainsTable();
			result = this.m_database.execute(propertyChainsQuery);
		}
		if(property_chains_trans) {
			String propertyChainsTransQuery = this.m_sqlFactory.createPropertyChainsTransTable();
			result = this.m_database.execute(propertyChainsTransQuery);
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
		return this.m_database.execute(query);
	}
}