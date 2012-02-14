package miner.modules;

import java.sql.Connection;

/**
 * Encapsulates the configuration of a {@link MinerModule}.
 *
 * The configuration contains some general ones (like the database connection to use) and miner-specific ones which
 * are provided as the corresponding part of the configuration XML file.
 */
public class MinerModuleConfiguration {

    private Connection dbConnection;


}
