package de.uni_mannheim.informatik.dws.goldminer.main;

import net.sourceforge.argparse4j.inf.Namespace;

import java.io.*;

/**
 * Writes the example configuration files to the specified files
 */
public class GenerateConfigStubs implements SubcommandModule {
    @Override
    public void runSubcommand(Namespace namespace) {
        String minerCfgName = namespace.getString("minercfg");
        String axiomsCfgName = namespace.getString("axiomcfg");

        System.out.println("Writing config stubs to:");
        System.out.println(String.format("Miner Config: '%s'", minerCfgName));
        System.out.println(String.format("Axioms Config: '%s'", axiomsCfgName));

        BufferedReader axiomsReader = new BufferedReader(new InputStreamReader(this.getClass().getResourceAsStream(
                "/de/uni_mannheim/informatik/dws/goldminer/config/axioms.properties")));

        try {
            BufferedWriter axiomsWriter = new BufferedWriter(new FileWriter(axiomsCfgName));
            String line;

            while ((line = axiomsReader.readLine()) != null) {
                axiomsWriter.write(line);
                axiomsWriter.newLine();
            }
            axiomsWriter.close();
            axiomsReader.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        BufferedReader minerReader = new BufferedReader(new InputStreamReader(this.getClass().getResourceAsStream(
                "/de/uni_mannheim/informatik/dws/goldminer/config/miner.properties")));

        try {
            BufferedWriter minerWriter = new BufferedWriter(new FileWriter(minerCfgName));
            String line;

            while ((line = minerReader.readLine()) != null) {
                minerWriter.write(line);
                minerWriter.newLine();
            }
            minerWriter.close();
            minerReader.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}
