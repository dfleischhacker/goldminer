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
     *
     * @return true if setup was successful, false otherwise.
     */
    public boolean setupSchema() {
        boolean result;
        String classesQuery = this.m_sqlFactory.createClassesTable();
        result = this.sqlDatabase.execute(classesQuery);
        String individualsQuery = this.m_sqlFactory.createIndividualsTable();
        result = this.sqlDatabase.execute(individualsQuery) && result;
        String propertiesQuery = this.m_sqlFactory.createPropertiesTable();
        result = this.sqlDatabase.execute(propertiesQuery) && result;
        String classesExPropertyQuery = this.m_sqlFactory.createClassesExPropertyTable();
        result = this.sqlDatabase.execute(classesExPropertyQuery) && result;
        String classesExPropertyTopQuery = this.m_sqlFactory.createClassesExPropertyTopTable();
        result = this.sqlDatabase.execute(classesExPropertyTopQuery) && result;
        String individualPairsQuery = this.m_sqlFactory.createIndividualPairsTable();
        result = this.sqlDatabase.execute(individualPairsQuery) && result;
        String individualPairsTransQuery = this.m_sqlFactory.createIndividualPairsTransTable();
        result = this.sqlDatabase.execute(individualPairsTransQuery) && result;
        String propertyChainsQuery = this.m_sqlFactory.createPropertyChainsTable();
        result = this.sqlDatabase.execute(propertyChainsQuery) && result;
        String propertyChainsTransQuery = this.m_sqlFactory.createPropertyChainsTransTable();
        result = this.sqlDatabase.execute(propertyChainsTransQuery) && result;
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