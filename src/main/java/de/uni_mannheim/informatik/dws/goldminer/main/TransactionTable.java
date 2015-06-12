package de.uni_mannheim.informatik.dws.goldminer.main;

import de.uni_mannheim.informatik.dws.goldminer.util.Settings;

import java.io.File;

/**
 * Names to use for transaction table file names.
 */
public enum TransactionTable {
    CLASS_MEMBERS("classmembers"),
    EXISTS_PROPERTY_MEMBERS("existspropertymembers"),
    PROPERTY_RESTRICTIONS1("propertyrestrictions1"),
    PROPERTY_RESTRICTIONS2("propertyrestrictions2"),
    PROPERTY_MEMBERS("propertymembers"),
    PROPERTY_CHAIN_MEMBERS("propertychainmembers"),
    CLASS_DISJOINTNESS("classdisjointness"),
    PROPERTY_REFLEXIVITY("propertyreflexivity"),
    PROPERTY_INVERSE_MEMBERS("propertyinversemembers"),
    PROPERTY_FUNCTIONAL_MEMBERS("propertyfunctionalmembers"),
    PROPERTY_INVERSE_FUNCTIONAL("propertyinversefunctional");


    private String tableName;

    private TransactionTable(String tableName) {
        this.tableName = tableName;
    }

    @Override
    public String toString() {
        return tableName;
    }

    /**
     * Returns the name for this table's transaction table file name.
     * @return the name for this table's transaction table file name
     */
    public String getFileName() {
        return tableName + ".txt";
    }

    /**
     * Returns the absolute path for this table's transaction table file name.
     * @return the absolute path for this table's transaction table file name
     */
    public String getAbsoluteFileName() {
        return Settings.getString("transaction_tables") + File.separator + getFileName();
    }

    /**
     * Returns the absolute path of the association rule file generated from this table.
     * @return the absolute path of the association rule file generated from this table
     */
    public String getAbsoluteAssociationRuleFileName() {
        return Settings.getString("association_rules") + File.separator + getAssociationRuleFileName();
    }

    /**
     * Returns the name of the association rule file generated from this table.
     * @return the name of the association rule file generated from this table
     */
    public String getAssociationRuleFileName() {
        return tableName + "AR.txt";
    }
}
