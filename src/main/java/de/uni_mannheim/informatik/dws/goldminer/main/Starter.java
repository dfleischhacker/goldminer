package de.uni_mannheim.informatik.dws.goldminer.main;


import de.uni_mannheim.informatik.dws.goldminer.util.Settings;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.*;

import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Command Line entry point for GOLD miner
 */
public class Starter {
    @SuppressWarnings("AccessStaticViaInstance")
    public static void main(String[] args) {
        ArgumentParser parser = ArgumentParsers.newArgumentParser("goldminer")
                                               .description("Tool for Statistical Schema Induction");
        parser.addArgument("--minercfg").metavar("file").type(String.class)
              .help("File to read miner configuration from")
              .setDefault(System.getProperty("user.dir") + "/miner.properties");
        parser.addArgument("--axiomcfg").metavar("file").type(String.class)
              .help("File to read axiom configuration from")
              .setDefault(System.getProperty("user.dir") + "/axiom.properties");

        Subparsers subparsers = parser.addSubparsers().title("subcommands").description("valid subcommands");
        Subparser generateParser = subparsers.addParser("generate").help("Generate transaction tables");
        generateParser.setDefault("func", new GenerateAssociationRules());
        Subparser mineParser = subparsers.addParser("mine").help("Run the association rule mining step");
        mineParser.setDefault("func", new MineAssociationRules());
        Subparser parseParser = subparsers.addParser("parse").help("Parse association rules and create ontology");
        parseParser.setDefault("func", new ParseRulesAndCreateOntology());
        Subparser licenseParser = subparsers.addParser("license").help("Print license information");
        licenseParser.setDefault("func", new LicenseInformation());
        licenseParser.setDefault("subparser", "license");
        parseParser.addArgument("--confidence").type(Double.class).metavar("conf").setDefault(0.0);
        parseParser.addArgument("--support").type(Double.class).metavar("supp").setDefault(0.0);
        parseParser.addArgument("--ontology").type(String.class).metavar("file").setDefault("");
        Subparser configStubParser = subparsers.addParser("genconfig")
                                               .help("Writes stubs for the configuration files to the files specified" +
                                                       " by the --axiomcfg and --minercfg parameters or (if not " +
                                                       "specified) into the current directory");
        configStubParser.setDefault("func", new GenerateConfigStubs());
        configStubParser.setDefault("subparser", "configStub");

        try {
            System.out.println("GOLD Miner  Copyright (C) 2011-2012 GOLD Miner Developers\n" +
                    "This is free software with ABSOLUTELY NO WARRANTY.\n" +
                    "For details use the 'license' command.");
            Namespace n = parser.parseArgs(args);
            if (n.getString("subparser") == null) {
                Settings.load(n.getString("minercfg"), n.getString("axiomcfg"));
            }
            ((SubcommandModule) n.get("func")).runSubcommand(n);
        }
        catch (ArgumentParserException e) {
            parser.handleError(e);
        }
        catch (FileNotFoundException e) {
            System.err.println("Configuration file not found: " + e.getMessage());
            return;
        }
        catch (IOException e) {
            System.err.println("Unable to read configuration file: " + e.getMessage());
            return;
        }
    }
}
