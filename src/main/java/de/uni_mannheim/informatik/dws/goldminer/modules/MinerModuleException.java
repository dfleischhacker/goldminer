package de.uni_mannheim.informatik.dws.goldminer.modules;

/**
 * Represents an error occured while running a {@see MinerModule}.
 */
public class MinerModuleException extends Exception {
    public MinerModuleException() {
    }

    public MinerModuleException(String s) {
        super(s);
    }

    public MinerModuleException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public MinerModuleException(Throwable throwable) {
        super(throwable);
    }
}
