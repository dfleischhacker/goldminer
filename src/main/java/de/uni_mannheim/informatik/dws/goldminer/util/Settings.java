package de.uni_mannheim.informatik.dws.goldminer.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Properties;


public class Settings implements Parameter {
    private final static Logger log = LoggerFactory.getLogger(Settings.class);

    private static String m_file;

    private static String a_file;

    private static Properties m_properties;

    private static Properties a_properties;

    static {
        m_properties = new Properties();
        a_properties = new Properties();
    }

    public final static String MINER_PROP = System.getProperty("user.dir") + "/res/miner.properties";

    public final static String AXIOMS_PROP = System.getProperty("user.dir") + "/res/axioms.properties";


    public static void load() throws IOException {
        load(MINER_PROP, AXIOMS_PROP);
    }

    public static void load(String mFile, String aFile) throws IOException {
        if (!loaded()) {
            m_file = mFile;
            a_file = aFile;
            System.out.println("Settings: loading configuration from file " + m_file);
            FileInputStream propStream = new FileInputStream(new File(m_file));
            m_properties.load(propStream);
            propStream.close();
            propStream = new FileInputStream(new File(a_file));
            a_properties.load(propStream);
            propStream.close();
            System.out.println("\nSettings: " + m_properties);
        }
    }

    public static String getString(String sKey) {
        return m_properties.getProperty(sKey);
    }

    public static boolean getBoolean(String sKey) {
        return Boolean.parseBoolean(m_properties.getProperty(sKey));
    }

    public static int getInteger(String sKey) {
        return Integer.parseInt(m_properties.getProperty(sKey));
    }

    public static boolean getAxiom(String sKey) {
        return Boolean.parseBoolean(a_properties.getProperty(sKey));
    }

    public static void set(String sKey, String sValue) {
        m_properties.setProperty(sKey, sValue);
    }

    public static void save(String sFile) throws FileNotFoundException, IOException {
        FileOutputStream fos = new FileOutputStream(sFile);
        m_properties.store(fos, "");
        fos.close();
    }

    public static String getFile() {
        return m_file;
    }

    public static boolean loaded() {
        return (m_properties.keySet().size() > 0);
    }

    public static void main(String[] args) throws IOException {
        Settings.load();
        String filter = Settings.getString("classesFilter");
        System.out.format("'%s'\n", filter);
        System.out.println("http://dbpedia.org/property/formercountry".startsWith(filter));
    }
}