package de.uni_mannheim.informatik.dws.goldminer.preprocessor;

import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;

import java.io.*;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Provides methods to combine the mappings from YAGO-based DBpedia classes to YAGO classes with the mapping from
 * YAGO classes to DBpedia ontology classes.
 */
public class YagoMappingPreprocessor {
    private HashMap<String,List<String>> yago2DBont;
    private HashMap<String,List<String>> yagoDB2yago;
    private HashMap<String,List<String>> confidences;

    public YagoMappingPreprocessor(String yagoDB2yagoFile, String yago2DBontFile) throws IOException {
        /* Reads lines like
        <wikicategory_Bad_Boy_Records_albums>   owl:equivalentClass     <http://dbpedia.org/class/yago/BadBoyRecordsAlbums>
         */
        yago2DBont = new HashMap<String, List<String>>();
        yagoDB2yago = new HashMap<String, List<String>>();
        confidences = new HashMap<String, List<String>>();
        BufferedReader reader = new BufferedReader(new FileReader(yagoDB2yagoFile));
        String line;

        while ((line = reader.readLine()) != null) {
            String[] elems = line.trim().split("\t");
            if (elems.length != 3) {
                continue;
            }
            String yagoClass = elems[0].replace("<", "").replace(">", "");
            String yagoDBClass = elems[2].replace("<", "").replace(">", "").replace("http://dbpedia.org/class/yago/", "");
            if (!yagoDB2yago.containsKey(yagoDBClass)) {
                yagoDB2yago.put(yagoDBClass, new LinkedList<String>());
            }
            yagoDB2yago.get(yagoDBClass).add(yagoClass);
        }

        reader.close();

        /* Reads lines like
        y:yagoLegalActor        owl:Thing       0.5326182228830121
         */
        reader = new BufferedReader(new FileReader(yago2DBontFile));

        while ((line = reader.readLine()) != null) {
            String[] elems = line.trim().split("\t");
            if (elems.length != 3) {
                continue;
            }
            String yagoClass= elems[0].replace("y:", "");

            if (!elems[1].startsWith("dbp:ontology/")) {
                continue;
            }

            String dbClass = elems[1].replace("dbp:ontology/", "");
            if (!yago2DBont.containsKey(yagoClass)) {
                yago2DBont.put(yagoClass, new LinkedList<String>());
                confidences.put(yagoClass, new LinkedList<String>());
            }
            yago2DBont.get(yagoClass).add(dbClass);
            confidences.get(yagoClass).add(elems[2]);
        }

        reader.close();
    }

    private void writeFile(String filename) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(filename));

        for (Map.Entry<String, List<String>> yyEntry : yagoDB2yago.entrySet()) {
            for (String yagoClass : yyEntry.getValue()) {
                if (yago2DBont.containsKey(yagoClass)) {
                    List<String> dbClasses = yago2DBont.get(yagoClass);
                    List<String> dbClassesConfidences = confidences.get(yagoClass);
                    for (int i = 0; i < dbClasses.size(); i++) {
                        String dbClass = dbClasses.get(i);
                        String confidence = dbClassesConfidences.get(i);
                        writer.write(String.format("%s\t%s\t%s\n", yyEntry.getKey(), dbClass, confidence));
                    }
                }
            }
        }
        writer.close();
    }

    public static void main(String[] args) {
        ArgumentParser parser = ArgumentParsers.newArgumentParser("YagoMappingPreprocessor")
                .description("Combines the mappings from YAGO-based DBpedia classes to YAGO classes with the mapping " +
                        "from YAGO classes to DBpedia ontology classes");
        parser.addArgument("yagoDB2yago").metavar("dbYago2Yago").type(String.class)
                .help("TSV file mapping YAGO-based DBpedia classes to YAGO classes").required(true);
        parser.addArgument("yago2DBont").metavar("yago2dbOnt").type(String.class)
                .help("TSV file mapping YAGO classes to DBpedia ontology classes").required(true);
        parser.addArgument("output").metavar("outfile").type(String.class).help(
                "Filename for resulting mapping file").required(true);

        try {
            System.out.println("GOLD Miner  Copyright (C) 2011-2013 GOLD Miner Developers\n" +
                    "This is free software with ABSOLUTELY NO WARRANTY.\n" +
                    "For details use the 'license' command.");
            Namespace n = parser.parseArgs(args);
            YagoMappingPreprocessor ymp = new YagoMappingPreprocessor(n.getString("yagoDB2yago"),
                    n.getString("yago2DBont"));
            ymp.writeFile(n.getString("output"));
        }
        catch (ArgumentParserException e) {
            parser.handleError(e);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}
