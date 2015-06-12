package de.uni_mannheim.informatik.dws.goldminer.main;

import de.uni_mannheim.informatik.dws.goldminer.ontology.ParsedAxiom;
import de.uni_mannheim.informatik.dws.goldminer.util.SupportConfidenceTuple;
import org.semanticweb.owlapi.model.OWLAxiom;

import java.util.HashMap;
import java.util.List;

/**
 * Enumeration of axiom types supported by GoldMiner.
 *
 * @author Daniel Fleischhacker (daniel@informatik.uni-mannheim.de)
 */
public enum AxiomType {
    /**
     * Class subsumption between two atomic classes
     */
    CLASS_SUBSUMPTION_SIMPLE("class-subsumption-simple", "Class subsumption between two atomic classes"),
    /**
     * Class subsumption between a class intersection and an atomic class (A \sqcap B \sqsubseteq C)
     */
    CLASS_SUBSUMPTION_COMPLEX("class-subsumption-complex", "Class subsumption between a class intersection and an " +
            "atomic class (A \\sqcap B \\sqsubseteq C)", true),
    /**
     * Disjointness between atomic classes
     */
    CLASS_DISJOINTNESS("class-disjointness", "Disjointness between atomic classes"),

    /**
     * Subsumption between atomic object properties
     */
    PROPERTY_SUBSUMPTION("property-subsumption", "Subsumption between atomic object properties"),
    /**
     * Disjointness between atomic object properties
     */
    PROPERTY_DISJOINTNESS("property-disjointness", "Disjointness between atomic object properties"),
    /**
     * Property domain restriction \exists p.T \sqsubseteq D
     */
    PROPERTY_DOMAIN("property-domain", "Property domain restriction \\exists p.T \\sqsubseteq D"),
    /**
     * Property range restriction represented as \exists p^-1.T \sqsubseteq C
     */
    PROPERTY_RANGE("property-range", "Property range restriction represented as \\exists p^-1.T \\sqsubseteq C"),
    /**
     * Required property for class C \sqsubseteq \exists p.D
     */
    PROPERTY_REQUIRED_FOR_CLASS("property-required-for-class", "Required property for class C \\sqsubseteq \\exists p.D"),
    /**
     * Range-specific property domains \exists p.C \sqsubseteq D
     */
    PROPERTY_DOMAIN_FOR_RANGE("property-domain-for-range", "Range-specific property domains \\exists p.C \\sqsubseteq D"),
    /**
     * Object property symmetry Sym(p)
     */
    PROPERTY_SYMMETRY("property-symmetry", "Object property symmetry Sym(p)"),
    /**
     * Object property asymmetry Asy(p)
     */
    PROPERTY_ASYMMETRY("property-asymmetry", "Object property asymmetry Asy(p)"),
    /**
     * General object property chains p \circ q \sqsubseteq r
     */
    PROPERTY_CHAINS("property-chains", "General object property chains p \\circ q \\sqsubseteq r"),
    /**
     * Transitive object property p \circ p \sqsubseteq p
     */
    PROPERTY_TRANSITIVITY("property-transitivity", "Transitive object property p \\circ p \\sqsubseteq p"),

    /**
     * Inverse object properties p \sqsubseteq q^-1
     */
    INVERSE_PROPERTY("inverse-property", "Inverse object properties p \\sqsubseteq q^-1"),
    /**
     * Functional object properties Fun(p)
     */
    FUNCTIONAL_PROPERTY("functional-property", "Functional object properties Fun(p)"),
    /**
     * Inverse functional object property Ifu(p)
     */
    INVERSE_FUNCTIONAL_PROPERTY("inverse-functional-property", "Inverse functional object property Ifu(p)"),
    /**
     * Reflexive property Ref(p)
     */
    REFLEXIVE_PROPERTY("reflexive-property", "Reflexive property Ref(p)"),
    /**
     * Irreflexive property Irr(p)
     */
    IRREFLEXIVE_PROPERTY("irreflexive-property", "Irreflexive property Irr(p)");

    private boolean hasSecondAntecedent;
    private String name;
    private String description;

    /**
     * Initializes an axiom type which might need parsing of a second antecedent.
     *
     * @param name                name of this axiom type
     * @param hasSecondAntecedent set to true if parsing of second antecedent is required
     */
    private AxiomType(String name, String description, boolean hasSecondAntecedent) {
        this.name = name;
        this.hasSecondAntecedent = hasSecondAntecedent;
    }

    /**
     * Initialize an axiom type with the given name which does not need parsing of a second antecedent.
     * @param name    name of this axiom type
     */
    private AxiomType(String name, String description) {
        this(name, description, false);
    }

    /**
     * Returns true if this axiom type needs parsing of a second antecedent.
     * @return true if this axiom type needs parsing of a second antecedent
     */
    public boolean hasSecondAntecedent() {
        return hasSecondAntecedent;
    }

    /**
     * Returns a description of this axiom type.
     * @return description of this axiom type
     */
    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return name;
    }

    /**
     * Returns a parser which generates OWL axioms from parsed axiom instances.
     *
     *
     * @param parsedAxioms    list of parsed axioms to generate OWL axioms for
     * @return OWL axioms generated from the given parsed axioms
     */
//    public HashMap<OWLAxiom, SupportConfidenceTuple> getOWLAxioms(List<ParsedAxiom> parsedAxioms) {
//
//    }
}
