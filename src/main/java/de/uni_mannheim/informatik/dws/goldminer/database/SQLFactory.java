package de.uni_mannheim.informatik.dws.goldminer.database;

public class SQLFactory {

    public String createClassesTable() {
        return "CREATE TABLE classes (" +
                "id bigInt(20) primary key, " +
                "uri varchar(255), " +
                "name varchar(255), " +
                "size bigInt(20)" +
                ");";
    }

    public String dropTables() {
        return "DROP TABLE IF EXISTS " +
                "classes, " +
                "properties, " +
                "datatypeProperties, " +
                "individuals, " +
                "classes_ex_property, " +
                "classes_ex_property_top, " +
                "individual_pairs, " +
                "individual_pairs_trans," +
                "property_chains," +
                "property_chains_trans";
    }

    public String createClassesExPropertyTable() {
        return "CREATE TABLE classes_ex_property (" +
                "id bigint(20) PRIMARY KEY, " +
                "prop_uri varchar(255) NOT NULL, " +
                "class_uri varchar(255) NOT NULL, " +
                "prop_name varchar(255) NOT NULL, " +
                "class_name varchar(255) NOT NULL" +
                ");";
    }

    public String createClassesExPropertyTopTable() {
        return "CREATE TABLE classes_ex_property_top (" +
                "id bigint(20) PRIMARY KEY, " +
                "inverse varchar(255) NOT NULL, " +
                "uri varchar(255) NOT NULL, " +
                "name varchar(255) NOT NULL" +
                ");";
    }

    public String createIndividualsTable() {
        return "CREATE TABLE individuals (" +
                "id bigint(20) PRIMARY KEY, " +
                "uri varchar(255) NOT NULL, " +
                "name varchar(255) NOT NULL" +
                ");";
    }

    public String createIndividualsExtTable() {
        return "CREATE TABLE individuals_ext (" +
                "id bigint(20) PRIMARY KEY, " +
                "uri varchar(255) NOT NULL, " +
                "name varchar(255) NOT NULL" +
                ");";
    }

    public String createIndividualsExtExtTable() {
        return "CREATE TABLE individuals_ext_ext(" +
                "id bigint(20) PRIMARY KEY, " +
                "uri varchar(255) NOT NULL, " +
                "name varchar(255) NOT NULL" +
                ");";
    }

    public String createIndividualPairsTable() {
        return "CREATE TABLE individual_pairs (" +
                "id bigint(20)," +
                "uri1 varchar(255) NOT NULL, " +
                "uri2 varchar(255) NOT NULL, " +
                "name1 varchar(255) NOT NULL, " +
                "name2 varchar(255) NOT NULL" +
                ");";
    }

    public String createIndividualPairsExtTable() {
        return "CREATE TABLE individual_pairs_ext (" +
                "id bigint(20)," +
                "uri1 varchar(255) NOT NULL, " +
                "uri2 varchar(255) NOT NULL, " +
                "name1 varchar(255) NOT NULL, " +
                "name2 varchar(255) NOT NULL" +
                ");";
    }

    public String createPropertiesTable() {
        return "CREATE TABLE properties (" +
                "id bigint(20) PRIMARY KEY, " +
                "disjointID bigint(20), " +
                "symmetryID bigint(20), " +
                "uri varchar(255) NOT NULL, " +
                "name varchar(255) NOT NULL" +
                ");";
    }

    public String createDatatypePropertiesTable() {
        return "CREATE TABLE datatypeProperties (" +
                "id bigint(20) PRIMARY KEY, " +
                "uri varchar(255) NOT NULL, " +
                "name varchar(255) NOT NULL" +
                ");";
    }

    public String createPropertyChainsTable() {
        return "CREATE TABLE property_chains (" +
                "id bigint(20) PRIMARY KEY, " +
                "uri1 varchar(255) NOT NULL, " +
                "uri2 varchar(255) NOT NULL, " +
                "name1 varchar(255) NOT NULL, " +
                "name2 varchar(255) NOT NULL" +
                ");";
    }

    public String createIndividualPairsTransTable() {
        return "CREATE TABLE individual_pairs_trans (" +
                "id bigint(20) PRIMARY KEY, " +
                "uri1 varchar(255) NOT NULL, " +
                "uri2 varchar(255) NOT NULL, " +
                "name1 varchar(255) NOT NULL, " +
                "name2 varchar(255) NOT NULL" +
                ");";
    }

    public String createPropertyChainsTransTable() {
        return "CREATE TABLE property_chains_trans (" +
                "id bigint(20) PRIMARY KEY, " +
                "uri varchar(255) NOT NULL, " +
                "name varchar(255) NOT NULL" +
                ");";
    }

    public String insertClassQuery(int iID, String sURI, String sName) {
        return "INSERT INTO classes (id, uri, name) VALUES (" + iID + ", '" + sURI + "', '" + sName + "')";
    }

    public String insertClassQuery(int iID, String sURI, String sName, int iSize) {
        return "INSERT INTO classes VALUES (" + iID + ", '" + sURI + "', '" + sName + "', " + iSize + ")";
    }

    public String insertIndividualQuery(int iID, String sURI, String sName) {
        return "INSERT INTO individuals VALUES (" + iID + ", '" + sURI + "', '" + sName + "')";
    }

    public String insertIndividualExtQuery(int iID, String sURI, String sName) {
        return "INSERT INTO individuals_ext VALUES (" + iID + ", '" + sURI + "', '" + sName + "')";
    }

    public String insertIndividualExtExtQuery(int iID, String sURI, String sName) {
        return "INSERT INTO individuals_ext_ext VALUES (" + iID + ", '" + sURI + "', '" + sName + "')";
    }

    public String insertPropertyQuery(int iID, String sURI, String sName) {
        return "INSERT INTO properties VALUES (" + iID + ", " + (iID + 1) + ", " + (iID + 2) + ", '" + sURI + "', " +
                "'" + sName + "')";
    }

    public String insertDatatypePropertyQuery(int id, String uri, String name) {
        return "INSERT INTO datatypeProperties VALUES (" + id + ", '" + uri + "', '" + name + "')";
    }

    public String insertPropertyChainQuery(int iID, String sURI1, String sURI2, String sName1, String sName2) {
        return "INSERT INTO property_chains VALUES (" + iID + ", '" + sURI1 + "', '" + sURI2 + "', '" + sName1 + "', " +
                "'" + sName2 + "')";
    }

    public String updateDisjointnessQuery(int id1, int id2, int iCount) {
        return "UPDATE c_dis_c SET ind = " + iCount + " WHERE cons = " + id1 + " AND ante = " + id2;
    }

    public String insertIndividualPairQuery(String sURI1, String sURI2, String sName1, String sName2, int id1,
                                            int id2) {
        return "INSERT INTO individual_pairs (id, uri1, uri2, name1, name2, id1, id2) VALUES ('" + sURI1 + "', " +
                "'" + sURI2 + "', '" + sName1 + "', '" + sName2 + "', " + id1 + ", " + id2 + ")";
    }

    public String insertIndividualPairExtQuery(String sURI1, String sURI2, String sName1, String sName2, int id1,
                                               int id2) {
        return "INSERT INTO individual_pairs_ext (uri1, uri2, name1, name2, id1, id2) VALUES ('" + sURI1 + "', " +
                "'" + sURI2 + "', '" + sName1 + "', '" + sName2 + "', " + id1 + ", " + id2 + ")";
    }

    public String insertIndividualPairQuery(int id, String sURI1, String sURI2, String sName1, String sName2) {
        return "INSERT INTO individual_pairs VALUES (" + id + ", '" + sURI1 + "', '" + sURI2 + "', '" + sName1 + "', " +
                "'" + sName2 + "')";
    }

    public String insertIndividualPairTransQuery(int id, String sURI1, String sURI2, String sName1, String sName2) {
        return "INSERT INTO individual_pairs_trans VALUES (" + id + ", '" + sURI1 + "', '" + sURI2 + "', " +
                "'" + sName1 + "', '" + sName2 + "')";
    }

    public String insertPropertyChainTransQuery(int iID, String sURI, String sName) {
        return "INSERT INTO property_chains_trans VALUES (" + iID + ", '" + sURI + "', '" + sName + "')";
    }

    public String insertIndividualPairExtQuery(int id, String sURI1, String sURI2, String sName1, String sName2) {
        return "INSERT INTO individual_pairs_ext VALUES (" + id + ", '" + sURI1 + "', '" + sURI2 + "', " +
                "'" + sName1 + "', '" + sName2 + "')";
    }

    public String insertClassExistsPropertyQuery(int iID, String sPropURI, String sClassURI, String sPropName,
                                                 String sClassName, int iSize) {
        return "INSERT INTO classes_ex_property VALUES (" + iID + ", '" + sPropURI + "', '" + sClassURI + "', " +
                "'" + sPropName + "', '" + sClassName + "', " + iSize + ")";
    }

    public String insertClassExistsPropertyQuery(int iID, String sPropURI, String sClassURI, String sPropName,
                                                 String sClassName) {
        return "INSERT INTO classes_ex_property VALUES (" + iID + ", '" + sPropURI + "', '" + sClassURI + "', " +
                "'" + sPropName + "', '" + sClassName + "')";
    }

    public String insertGoldSubAxiomQuery(int id1, int id2, int iSubsumedBy) {
        return "INSERT INTO c_sub_c_gold VALUES (" + id1 + ", " + id2 + ", " + iSubsumedBy + ")";
    }

    public String insertGoldPropAxiomQuery(int iProp, int iDomain, int iRange) {
        return "INSERT INTO properties_gold VALUES (" + iProp + ", " + iDomain + ", " + iRange + ")";
    }

    public String insertGoldPropDomainQuery(int iProp, int iClass, int iEntailed) {
        return "INSERT INTO exists_p_T_sub_c_gold VALUES (" + iProp + ", " + iClass + ", " + iEntailed + ")";
    }

    public String insertGoldPropRangeQuery(int iProp, int iClass, int iEntailed) {
        return "INSERT INTO exists_pi_T_sub_c_gold VALUES (" + iProp + ", " + iClass + ", " + iEntailed + ")";
    }

    public String insertResultSubAxiomQuery(int id1, int id2, int iSubsumedBy) {
        return "INSERT INTO c_sub_c_result VALUES (" + id1 + ", " + id2 + ", " + iSubsumedBy + ")";
    }

    public String insertResultPropAxiomQuery(int iProp, int iDomain, int iRange) {
        return "INSERT INTO properties_result VALUES (" + iProp + ", " + iDomain + ", " + iRange + ")";
    }

    public String insertResultPropDomainQuery(int iProp, int iClass, int iEntailed) {
        return "INSERT INTO exists_p_T_sub_c_result VALUES (" + iProp + ", " + iClass + ", " + iEntailed + ")";
    }

    public String insertResultPropRangeQuery(int iProp, int iClass, int iEntailed) {
        return "INSERT INTO exists_pi_T_sub_c_result VALUES (" + iProp + ", " + iClass + ", " + iEntailed + ")";
    }

    public String insertPropertyTopQuery(int iID, int iInv, String sPropURI, String sPropName) {
        return "INSERT INTO classes_ex_property_top VALUES (" + iID + ", " + iInv + ", '" + sPropURI + "', " +
                "'" + sPropName + "')";
    }

    public String selectClassesQuery() {
        return "SELECT * FROM classes";
    }

    public String selectDisjointnessQuery() {
        return "SELECT * FROM c_dis_c";
    }

    public String selectClassesExtQuery() {
        return "SELECT * FROM classes_ex_property";
    }

    public String selectClassesExtExtQuery() {
        return "SELECT * FROM classes_ex_property_top";
    }

    public String selectIndividualsQuery() {
        return "SELECT * FROM individuals";
    }

    public String countIndividualsQuery() {
        return "SELECT COUNT(DISTINCT id) FROM individuals";
    }

    public String selectIndividualsQuery(int iStart, int iEnd) {
        return "SELECT * FROM individuals WHERE id >= " + iStart + " AND id < " + iEnd;
    }

    /**
     * Selects all possible individual pairs from the database. The result lines contain the fields: id, uri1, uri2,
     * name1,
     * name2
     *
     * @return
     */
    public String selectIndividualPairsQuery() {
        return "SELECT * FROM individual_pairs";
    }

    public String selectIndividualPairsExtQuery() {
        return "SELECT * FROM individual_pairs_ext";
    }

    public String selectIndividualPairsExtQuery(int iStart, int iEnd) {
        return "SELECT * FROM individual_pairs_ext WHERE id >= " + iStart + " AND id < " + iEnd;
    }

    public String selectIndividualPairsTransQuery() {
        return "SELECT * FROM individual_pairs_trans";
    }

    public String selectPropertiesQuery() {
        return "SELECT * FROM properties";
    }

    public String selectDatatypePropertiesQuery() {
        return "SELECT * FROM datatypeProperties";
    }

    public String selectPropertyChainsQuery() {
        return "SELECT * FROM property_chains";
    }

    public String selectPropertyChainsTransQuery() {
        return "SELECT * FROM property_chains_trans";
    }

    public String selectPropertyRestrictionsQuery(int iInverse) {
        return "SELECT * FROM classes_ex_property_top WHERE inverse=" + iInverse;
    }

    public String selectExistsPropertiesQuery() {
        return "SELECT * FROM classes_ex_property";
    }

    public String selectClassIDQuery(String sURI) {
        return "SELECT id FROM classes WHERE uri='" + sURI + "'";
    }

    public String selectClassURIQuery(int iID) {
        return "SELECT uri FROM classes WHERE id='" + iID + "'";
    }

    public String selectClassURIQuery() {
        return "SELECT id, uri FROM classes";
    }

    public String selectClassURIsQuery() {
        return "SELECT name FROM classes";
    }

    public String selectPropertyURIsQuery() {
        return "SELECT name FROM properties";
    }

    public String selectIndividualIDQuery(String sURI) {
        return "SELECT id FROM individuals WHERE uri='" + sURI + "'";
    }

    public String selectExistsPropertyIDQuery(String sPropURI, String sClassURI) {
        return "SELECT * FROM classes_ex_property WHERE prop_uri='" + sPropURI + "' AND class_uri='" + sClassURI + "'";
    }

    public String selectPropertyChainIDQuery(String sPropURI1, String sPropURI2) {
        return "SELECT * FROM property_chains WHERE uri1='" + sPropURI1 + "' AND uri2='" + sPropURI2 + "'";
    }

    public String selectIndividualPairIDQuery(String sURI1, String sURI2) {
        return "SELECT * FROM individual_pairs WHERE uri1='" + sURI1 + "' AND uri2='" + sURI2 + "'";
    }

    public String selectURIsFromExistsQuery(int iExpID) {
        return "SELECT * FROM classes_ex_property WHERE id='" + iExpID + "'";
    }

    public String selectURIsFromExistsQuery() {
        return "SELECT * FROM classes_ex_property";
    }

    public String selectURIsFromExistsTopQuery(int iExpID) {
        return "SELECT * FROM classes_ex_property_top WHERE id='" + iExpID + "'";
    }

    public String selectURIsFromExistsTopQuery() {
        return "SELECT * FROM classes_ex_property_top";
    }

    public String selectPropertyURIQuery(int iID) {
        return "SELECT uri FROM properties WHERE id='" + iID + "'";
    }

    public String selectDisjointPropertyURIQuery(int iID) {
        return "SELECT uri FROM properties WHERE disjointID='" + iID + "'";
    }

    public String selectInversePropertyURIQuery(int iID) {
        return "SELECT uri FROM properties WHERE disjointID='" + iID + "'";
    }

    public String selectSymmetryPropertyURIQuery(int iID) {
        return "SELECT uri FROM properties WHERE symmetryID='" + iID + "'";
    }

    public String selectURIsFromPropertyChains(int id) {
        return "SELECT uri1, uri2 FROM property_chains WHERE id='" + id + "'";
    }

    public String selectURIFromPropertyChainsTrans(int id) {
        return "SELECT uri FROM property_chains_trans WHERE id='" + id + "' UNION SELECT uri FROM " +
                "properties WHERE id='" + id + "'";
    }

    // axioms

    public String select_c_sub_c_AxiomQuery() {
        return "SELECT * FROM c_sub_c WHERE conf>=100";
    }

    public String select_c_and_c_sub_c_AxiomQuery() {
        return "SELECT * FROM c_and_c_sub_c WHERE conf>=100";
    }

    public String select_c_dis_c_AxiomQuery() {
        return "SELECT * FROM c_dis_c";
    }

    public String select_exists_p_c_sub_c_AxiomQuery() {
        return "SELECT * FROM exists_p_c_sub_c WHERE conf>=100";
    }

    public String select_exists_p_T_sub_c_AxiomQuery() {
        return "SELECT * FROM exists_p_T_sub_c WHERE conf>=100";
    }

    public String select_exists_pi_T_sub_c_AxiomQuery() {
        return "SELECT * FROM exists_pi_T_sub_c WHERE conf>=100";
    }

    public String select_c_sub_exists_p_c_AxiomQuery() {
        return "SELECT * FROM c_sub_exists_p_c WHERE conf>=100";
    }

    public String select_p_sub_p_AxiomQuery() {
        return "SELECT * FROM p_sub_p WHERE conf>=100";
    }
}
