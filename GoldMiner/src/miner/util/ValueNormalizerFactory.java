package miner.util;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;

/**
 * Factory class for value normalizers
 */
public class ValueNormalizerFactory {
    private static Class<? extends ValueNormalizer> defaultNormalizer = NoopNormalizer.class;

    /**
     * Returns a value normalizer which scales the given values to use the whole value range of [0.0,1.0]
     *
     * @param name name for the normalizer
     * @return a scaling value normalizer
     */
    public static ValueNormalizer scalingNormalizer(String name) {
        return new ScalingValueNormalizer(name);
    }

    /**
     * Returns a normalizer which does not change the given values.
     *
     * @param name name for this normalizer (is not going to be used)
     * @return a no-op value normalizer
     */
    public static ValueNormalizer noOpNormalizer(String name) {
        return new NoopNormalizer();
    }

    /**
     * Returns an instance of the default value normalizer which has been set using the method {@link
     * #setDefaultNormalizer} having been initialized by using the default normalizer.
     *
     * @param name name for normalizer to create
     */
    public static ValueNormalizer getDefaultNormalizerInstance(String name) {
        try {
            return defaultNormalizer.getConstructor(String.class).newInstance(name);
        }
        catch (InstantiationException e) {
            throw new RuntimeException(e);
        }
        catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }
        catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Sets the default value normalizer
     *
     * @param defaultClass default value normalizer class
     */
    public static void setDefaultNormalizer(Class<? extends ValueNormalizer> defaultClass) {
        defaultNormalizer = defaultClass;
    }

    private static class NoopNormalizer implements ValueNormalizer {

        @Override
        public void reportValue(NormalizationTarget target) {
        }

        @Override
        public void reportValues(Collection<? extends NormalizationTarget> targets) {
        }

        @Override
        public void normalize(Collection<? extends NormalizationTarget> collection) {
        }
    }
}
