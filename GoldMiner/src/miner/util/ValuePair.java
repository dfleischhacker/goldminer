package miner.util;

/**
 * Basic implementation of a value pair
 */
public class ValuePair<T extends Comparable<T>> {
    private T v1;
    private T v2;

    public ValuePair(T v1, T v2) {
        if (v1.compareTo(v2) <= 0) {
            this.v1 = v1;
            this.v2 = v2;
        }
        else {
            this.v1 = v2;
            this.v2 = v1;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ValuePair valuePair = (ValuePair) o;

        if (!v1.equals(valuePair.v1)) {
            return false;
        }
        if (!v2.equals(valuePair.v2)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = v1.hashCode();
        result = 31 * result + v2.hashCode();
        return result;
    }

    public T getV1() {
        return v1;
    }

    public T getV2() {
        return v2;
    }
}
