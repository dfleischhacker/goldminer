package de.uni_mannheim.informatik.dws.goldminer.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Properties;

/**
 * Provides access to the configuration data. This class is a singleton implementation.
 */
public class Settings implements Parameter {
    private final static Logger log = LoggerFactory.getLogger(Settings.class);

    private static String generalConfigurationFile;

    private static String axiomsConfigurationFile;

    private static Properties generalConfiguration;

    private static Properties axiomConfiguration;

    static {
        generalConfiguration = new Properties();
        axiomConfiguration = new Properties();
    }

    public final static String MINER_PROP = System.getProperty("user.dir") + "/res/miner.properties";

    public final static String AXIOMS_PROP = System.getProperty("user.dir") + "/res/axioms.properties";

    /**
     * Triggers loading the configuration files from their default locations.
     *
     * @throws IOException
     */
    public static void load() throws IOException {
        load(MINER_PROP, AXIOMS_PROP);
    }

    /**
     * Triggers loading the configuration files from the given locations.
     *
     * @param generalConfigurationFile location to load the general configuration from
     * @param axiomConfigurationFile   location to load the axiom configuration from
     * @throws IOException
     */
    public static void load(String generalConfigurationFile, String axiomConfigurationFile) throws IOException {
        if (!loaded()) {
            Settings.generalConfigurationFile = generalConfigurationFile;
            Settings.axiomsConfigurationFile = axiomConfigurationFile;
            log.info("Loading general configuration from " + Settings.generalConfigurationFile);
            FileInputStream propStream = new FileInputStream(new File(Settings.generalConfigurationFile));
            generalConfiguration.load(propStream);
            propStream.close();
            log.info("General configuration loaded: " + generalConfiguration);
            log.info("Loading axioms configuration from" + Settings.axiomsConfigurationFile);
            propStream = new FileInputStream(new File(axiomsConfigurationFile));
            axiomConfiguration.load(propStream);
            propStream.close();
            log.info("Axioms configuration loaded: " + axiomConfiguration);
        }
    }

    /**
     * Returns the string value of the given key in the general configuration file.
     *
     * @param key name of key to return value for
     * @return value for given key or null if no such key specified
     */
    public static String getString(String key) {
        String value = generalConfiguration.getProperty(key);
        if (value == null) {
            log.warn("No general configuration value for key '" + key + "'");
        }
        return value;
    }

    /**
     * Returns the boolean value for the given key in the general configuration file.
     *
     * @param key key to get value for
     * @return boolean value for the given key or null if no such key specified
     */
    public static Boolean getBoolean(String key) {
        String value = generalConfiguration.getProperty(key);
        if (value == null) {
            log.warn("No general configuration value for key '" + key + "'");
            return null;
        }
        return Boolean.parseBoolean(value);
    }

    /**
     * Returns the integer value for the given key in the general configuration file.
     *
     * @param key key key to get value for
     * @return integer value for the given key or null of no such key specified
     */
    public static Integer getInteger(String key) {
        String value = generalConfiguration.getProperty(key);
        if (value == null) {
            log.warn("No general configuration value for key '" + key + "'");
            return null;
        }
        return Integer.parseInt(value);
    }

    /**
     * Returns whether the axiom with the given key is activated in the axiom configuration file.
     * This method does not return null but false if the given key is not used in the configuration file.
     *
     * @param key axiom key to check
     * @return true if axiom is activated, otherwise false
     */
    public static boolean isAxiomActivated(String key) {
        String value = axiomConfiguration.getProperty(key);
        if (value == null) {
            log.warn("No axiom configuration value for key '" + key + "'. Returning false as default");
            return false;
        }
        return Boolean.parseBoolean(value);
    }

    /**
     * Sets the given key to the specified value.
     *
     * @param key
     * @param value
     * @deprecated Modifying the configuration from within the code is no longer recommended.
     */
    @Deprecated
    public static void set(String key, String value) {
        generalConfiguration.setProperty(key, value);
    }

    /**
     * Stores the general configuration into a file with the given filename.
     * @param fileName name of file to save general configuration to
     * @throws FileNotFoundException
     * @throws IOException
     * @deprecated Modifying the configuration from within the code is no longer recommended.
     */
    @Deprecated
    public static void save(String fileName) throws FileNotFoundException, IOException {
        FileOutputStream fos = new FileOutputStream(fileName);
        generalConfiguration.store(fos, "");
        fos.close();
    }

    /**
     * Returns the name of the file where the general configuration was loaded from.
     * @return name of the file where the general configuration was loaded from
     */
    public static String getGeneralConfigurationFileName() {
        return generalConfigurationFile;
    }

    /**
     * Returns the name of the file where the axiom configuration was loaded from.
     * @return name of the file where the axiom configuration was loaded from
     */
    public static String getAxiomsConfigurationFileName() {
        return axiomsConfigurationFile;
    }

    /**
     * Checks whether the configurations are already loaded.
     * @return true if the configuration data is already loaded otherwise false.
     */
    public static boolean loaded() {
        return !generalConfiguration.isEmpty();
    }

    public static void main(String[] args) throws IOException {
        Settings.load();
        String filter = Settings.getString("classesFilter");
        System.out.format("'%s'\n", filter);
        System.out.println("http://dbpedia.org/property/formercountry".startsWith(filter));
    }
}