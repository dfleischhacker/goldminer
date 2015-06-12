package de.uni_mannheim.informatik.dws.goldminer.main;

import java.util.*;

/**
 * Manages the list of required files and database tables given a set of axioms to generate.
 *
 * @author Daniel Fleischhacker (daniel@informatik.uni-mannheim.de)
 */
public class RequirementsResolver {
    public final static HashMap<AxiomType, Set<DatabaseTable>> DATABASE_TABLE_DEPENDENCIES =
            new HashMap<AxiomType, Set<DatabaseTable>>();

    static {
        //TODO: move into file?
        addDatabaseDependency(AxiomType.CLASS_SUBSUMPTION_SIMPLE, DatabaseTable.CLASSES_TABLE,
                DatabaseTable.INDIVIDUALS_TABLE);
        addDatabaseDependency(AxiomType.CLASS_SUBSUMPTION_COMPLEX, DatabaseTable.CLASSES_TABLE,
                DatabaseTable.INDIVIDUALS_TABLE);
        addDatabaseDependency(AxiomType.CLASS_DISJOINTNESS, DatabaseTable.CLASSES_TABLE,
                DatabaseTable.INDIVIDUALS_TABLE);
        addDatabaseDependency(AxiomType.PROPERTY_SUBSUMPTION, DatabaseTable.PROPERTIES_TABLE,
                DatabaseTable.INDIVIDUAL_PAIRS_TABLE);
        addDatabaseDependency(AxiomType.PROPERTY_DISJOINTNESS, DatabaseTable.PROPERTIES_TABLE,
                DatabaseTable.INDIVIDUAL_PAIRS_TABLE);
        addDatabaseDependency(AxiomType.PROPERTY_DOMAIN, DatabaseTable.INDIVIDUALS_TABLE,
                DatabaseTable.PROPERTIES_TABLE,
                DatabaseTable.PROPERTY_TOP_TABLE);
        addDatabaseDependency(AxiomType.PROPERTY_RANGE, DatabaseTable.INDIVIDUALS_TABLE,
                DatabaseTable.PROPERTIES_TABLE,
                DatabaseTable.PROPERTY_TOP_TABLE);
        addDatabaseDependency(AxiomType.PROPERTY_REQUIRED_FOR_CLASS, DatabaseTable.INDIVIDUALS_TABLE,
                DatabaseTable.PROPERTIES_TABLE, DatabaseTable.CLASSES_EXISTS_PROPERTY_TABLE);
        addDatabaseDependency(AxiomType.PROPERTY_DOMAIN_FOR_RANGE, DatabaseTable.INDIVIDUALS_TABLE,
                DatabaseTable.PROPERTIES_TABLE, DatabaseTable.CLASSES_EXISTS_PROPERTY_TABLE);
        addDatabaseDependency(AxiomType.PROPERTY_SYMMETRY, DatabaseTable.PROPERTIES_TABLE,
                DatabaseTable.INDIVIDUAL_PAIRS_TABLE);
        addDatabaseDependency(AxiomType.PROPERTY_ASYMMETRY, DatabaseTable.PROPERTIES_TABLE,
                DatabaseTable.INDIVIDUAL_PAIRS_TABLE);
        addDatabaseDependency(AxiomType.PROPERTY_CHAINS, DatabaseTable.PROPERTIES_TABLE,
                DatabaseTable.INDIVIDUAL_PAIRS_TABLE,
                DatabaseTable.PROPERTY_CHAINS_TABLE, DatabaseTable.PROPERTY_CHAINS_TRANS_TABLE,
                DatabaseTable.INDIVIDUAL_PAIRS_TRANS_TABLE);
        addDatabaseDependency(AxiomType.PROPERTY_TRANSITIVITY, DatabaseTable.PROPERTIES_TABLE,
                DatabaseTable.INDIVIDUAL_PAIRS_TABLE, DatabaseTable.PROPERTY_CHAINS_TABLE,
                DatabaseTable.PROPERTY_CHAINS_TRANS_TABLE, DatabaseTable.INDIVIDUAL_PAIRS_TRANS_TABLE);
        addDatabaseDependency(AxiomType.INVERSE_PROPERTY, DatabaseTable.PROPERTIES_TABLE,
                DatabaseTable.INDIVIDUAL_PAIRS_TABLE);
        addDatabaseDependency(AxiomType.FUNCTIONAL_PROPERTY, DatabaseTable.PROPERTIES_TABLE);
        addDatabaseDependency(AxiomType.INVERSE_FUNCTIONAL_PROPERTY, DatabaseTable.PROPERTIES_TABLE);
        addDatabaseDependency(AxiomType.REFLEXIVE_PROPERTY, DatabaseTable.PROPERTIES_TABLE);
        addDatabaseDependency(AxiomType.IRREFLEXIVE_PROPERTY, DatabaseTable.PROPERTIES_TABLE);
    }

    public static final HashMap<AxiomType, Set<TransactionTable>> TRANSACTION_TABLE_DEPENDENCIES = new HashMap
            <AxiomType, Set<TransactionTable>>();

    static {
        addTransactionDependency(AxiomType.CLASS_SUBSUMPTION_SIMPLE, TransactionTable.CLASS_MEMBERS);
        addTransactionDependency(AxiomType.CLASS_SUBSUMPTION_COMPLEX, TransactionTable.CLASS_MEMBERS);
        addTransactionDependency(AxiomType.CLASS_DISJOINTNESS, TransactionTable.CLASS_DISJOINTNESS);
        addTransactionDependency(AxiomType.PROPERTY_SUBSUMPTION, TransactionTable.PROPERTY_MEMBERS);
        addTransactionDependency(AxiomType.PROPERTY_DISJOINTNESS, TransactionTable.PROPERTY_MEMBERS);
        addTransactionDependency(AxiomType.PROPERTY_DOMAIN, TransactionTable.PROPERTY_RESTRICTIONS1);
        addTransactionDependency(AxiomType.PROPERTY_RANGE, TransactionTable.PROPERTY_RESTRICTIONS2);
        addTransactionDependency(AxiomType.PROPERTY_REQUIRED_FOR_CLASS, TransactionTable.EXISTS_PROPERTY_MEMBERS);
        addTransactionDependency(AxiomType.PROPERTY_DOMAIN_FOR_RANGE, TransactionTable.EXISTS_PROPERTY_MEMBERS);
        addTransactionDependency(AxiomType.PROPERTY_SYMMETRY, TransactionTable.PROPERTY_INVERSE_MEMBERS);
        addTransactionDependency(AxiomType.PROPERTY_ASYMMETRY, TransactionTable.PROPERTY_INVERSE_MEMBERS);
        addTransactionDependency(AxiomType.PROPERTY_CHAINS, TransactionTable.PROPERTY_CHAIN_MEMBERS);
        addTransactionDependency(AxiomType.PROPERTY_TRANSITIVITY, TransactionTable.PROPERTY_CHAIN_MEMBERS);
        addTransactionDependency(AxiomType.INVERSE_PROPERTY, TransactionTable.PROPERTY_INVERSE_MEMBERS);
        addTransactionDependency(AxiomType.FUNCTIONAL_PROPERTY, TransactionTable.PROPERTY_FUNCTIONAL_MEMBERS);
        addTransactionDependency(AxiomType.INVERSE_FUNCTIONAL_PROPERTY, TransactionTable.PROPERTY_INVERSE_FUNCTIONAL);
        addTransactionDependency(AxiomType.REFLEXIVE_PROPERTY, TransactionTable.PROPERTY_REFLEXIVITY);
        addTransactionDependency(AxiomType.IRREFLEXIVE_PROPERTY, TransactionTable.PROPERTY_REFLEXIVITY);
    }

    private Set<AxiomType> activeAxiomTypes;
    private Set<TransactionTable> requiredTransactionTables;
    private Set<DatabaseTable> requiredDatabaseTables;

    /**
     * Initializes the resolver for the given set of active axiom types.
     *
     * @param activeAxiomTypes axiom types which should be generated
     */
    public RequirementsResolver(Set<AxiomType> activeAxiomTypes) {
        this.activeAxiomTypes = activeAxiomTypes;

        requiredTransactionTables = getRequiredTransactionTables(activeAxiomTypes);
        requiredDatabaseTables = getRequiredDatabaseTables(activeAxiomTypes);
    }

    /**
     * Returns the set of transaction tables required by the current run configuration.
     *
     * @return the set of transaction tables required by the current run configuration
     */
    public Set<TransactionTable> getRequiredTransactionTables() {
        return Collections.unmodifiableSet(requiredTransactionTables);
    }

    /**
     * Returns true if the given database table is required in the current axiom configuration.
     *
     * @param table table to check whether it is required
     * @return true if table is required, otherwise false
     */
    public boolean isDatabaseTableRequired(DatabaseTable table) {
        return requiredDatabaseTables.contains(table);
    }

    /**
     * Returns true if the given transaction table is required in the current axiom configuration.
     *
     * @param table table to check whether it is required
     * @return true if table is required, otherwise false
     */
    public boolean isTransactionTableRequired(TransactionTable table) {
        return requiredTransactionTables.contains(table);
    }

    /**
     * Returns the set of required transaction tables for the active axiom types.
     *
     * @param activeAxiomTypes axiom types which should be generated
     * @return set of required transaction tables
     */
    private Set<TransactionTable> getRequiredTransactionTables(Set<AxiomType> activeAxiomTypes) {
        HashSet<TransactionTable> requiredTables = new HashSet<TransactionTable>();
        for (AxiomType a : activeAxiomTypes) {
            requiredTables.addAll(TRANSACTION_TABLE_DEPENDENCIES.get(a));
        }

        return requiredTables;
    }

    /**
     * Returns the set of required database tables for the active axiom types.
     *
     * @param activeAxiomTypes axiom types which should be generated
     * @return set of required database tables
     */
    private Set<DatabaseTable> getRequiredDatabaseTables(Set<AxiomType> activeAxiomTypes) {
        HashSet<DatabaseTable> requiredTables = new HashSet<DatabaseTable>();
        for (AxiomType a : activeAxiomTypes) {
            requiredTables.addAll(DATABASE_TABLE_DEPENDENCIES.get(a));
        }

        return requiredTables;
    }

    /**
     * Adds the dependency between the given axiom type and the given database tables.
     * <p/>
     * This means that the given axiom type requires the generation of all provided tables.
     *
     * @param t      axiom type
     * @param tables tables the axiom type depends on
     */
    private static void addDatabaseDependency(AxiomType t, DatabaseTable... tables) {
        List<DatabaseTable> allTables = Arrays.asList(tables);
        if (DATABASE_TABLE_DEPENDENCIES.containsKey(t)) {
            DATABASE_TABLE_DEPENDENCIES.get(t).addAll(allTables);
        }
        else {
            DATABASE_TABLE_DEPENDENCIES.put(t, new HashSet<DatabaseTable>(allTables));
        }
    }

    /**
     * Adds the dependency between the given axiom type and the given transaction tables.
     * <p/>
     * This means that the given axiom type requires the generation of all provided tables.
     *
     * @param t      axiom type
     * @param tables tables the axiom type depends on
     */
    private static void addTransactionDependency(AxiomType t, TransactionTable... tables) {
        List<TransactionTable> allTables = Arrays.asList(tables);
        if (TRANSACTION_TABLE_DEPENDENCIES.containsKey(t)) {
            TRANSACTION_TABLE_DEPENDENCIES.get(t).addAll(allTables);
        }
        else {
            TRANSACTION_TABLE_DEPENDENCIES.put(t, new HashSet<TransactionTable>(allTables));
        }
    }

    /**
     * Returns the main transaction table for the given axiom type.
     * @return main transaction table for the given axiom type
     */
    public TransactionTable getRequiredTransactionTable(AxiomType axiomType) {
        Set<TransactionTable> requiredTransactionTables = TRANSACTION_TABLE_DEPENDENCIES.get(axiomType);
        if (requiredTransactionTables.size() != 1) {
            throw new RuntimeException("Found multiple main transaction tables for axiom type " + axiomType);
        }
        return requiredTransactionTables.iterator().next();
    }

    /**
     * Returns the set of active axiom types.
     *
     * @return set of active axiom types
     */
    public Set<AxiomType> getActiveAxiomTypes() {
        return activeAxiomTypes;
    }
}
