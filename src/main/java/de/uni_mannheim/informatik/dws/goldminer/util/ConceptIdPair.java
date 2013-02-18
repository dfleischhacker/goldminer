package de.uni_mannheim.informatik.dws.goldminer.util;

public class ConceptIdPair extends ValuePair<String> {
    private Double support;
    private Double confidence;

    public Double getSupport() {
        return support;
    }

    public Double getConfidence() {
        return confidence;
    }

    public ConceptIdPair(String v1, String v2) {
        super(v1, v2);
    }
}
