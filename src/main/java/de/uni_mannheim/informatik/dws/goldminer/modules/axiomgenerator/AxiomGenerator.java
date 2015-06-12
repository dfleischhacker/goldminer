package de.uni_mannheim.informatik.dws.goldminer.modules.axiomgenerator;

import de.uni_mannheim.informatik.dws.goldminer.ontology.ParsedAxiom;
import de.uni_mannheim.informatik.dws.goldminer.util.SupportConfidenceTuple;
import org.semanticweb.owlapi.model.OWLAxiom;

import java.util.HashMap;
import java.util.List;

/**
 * Generates OWL axioms from parsed axioms representation.
 */
public interface AxiomGenerator {
    /**
     * Returns a map containing the OWL axioms generated from the given list of parsed axioms.
     * @param parsedAxioms parsed axioms to generate OWL axioms from
     * @return map from OWL axioms to support and confidence generated from the parsed axioms set
     */
    public HashMap<OWLAxiom, SupportConfidenceTuple> generateOWLAxioms(List<ParsedAxiom> parsedAxioms);
}
