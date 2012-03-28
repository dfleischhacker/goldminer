package miner.util;

import java.util.Collection;

/**
 * Defines the interface for normalizers used to apply normalizing operations on given confidence values.
 */
public interface ValueNormalizer {
    /**
     * Reports the value of the given target to the normalizer so that it can be took into consideration for the
     * normalization phase
     *
     * @param target target whose value to report
     */
    void reportValue(NormalizationTarget target);

    /**
     * Reports the values of all normalization targets contained in the given collection to the normalizer.
     *
     * @param targets collection of targets whose values to report
     */
    void reportValues(Collection<? extends NormalizationTarget> targets);

    /**
     * Normalizes the values contained in the given target. This method directly modifies the single objects in the
     * given collection. Take this into account when your collection is sensitive to such changes!
     *
     * @param collection collection of targets to normalize
     *
     */
    void normalize(Collection<? extends NormalizationTarget> collection);

    /**
     * Interface to be implemented by objects being possible targets for normalization
     */
    public static interface NormalizationTarget {
        public double getValue();
        public void setValue(double value);
    }

    /**
     * Identifies the available normalization modes
     */
    public static enum NormalizationMode {
        LOWER_BOUND,
        UPPER_BOUND,
        BOTH
    }

    public static class DummyNormalizationTarget implements NormalizationTarget {
        private Double val;

        public DummyNormalizationTarget(Double val) {
            this.val = val;
        }
        @Override
        public double getValue() {
            return val;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public void setValue(double value) {
            this.val = value;
        }

        @Override
        public String toString() {
            return "DummyNormalizationTarget{" +
                    "val=" + val +
                    '}';
        }
    }
}
