package de.uni_mannheim.informatik.dws.goldminer.main;

import net.sourceforge.argparse4j.inf.Namespace;

public interface SubcommandModule {
    public void runSubcommand(Namespace namespace);
}
