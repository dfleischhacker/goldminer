package miner.util;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

/**
 * Helper class for randomly selecting axioms
 */
public class RandomAxiomChooser {
    public static class AxiomConfidencePair {
        private OWLAxiom axiom;
        private double confidence;

        public AxiomConfidencePair(OWLAxiom axiom, double confidence) {
            this.axiom = axiom;
            this.confidence = confidence;
        }

        public OWLAxiom getAxiom() {
            return axiom;
        }

        public double getConfidence() {
            return confidence;
        }
    }
    
    private final static Logger log = LoggerFactory.getLogger(RandomAxiomChooser.class);
    Random rand = new Random();
    
    ArrayList<AxiomConfidencePair> axioms;
    Double totalWeight = 0d;

    public RandomAxiomChooser() {
        axioms = new ArrayList<AxiomConfidencePair>();
    }
    
    public int getSize() {
        return axioms.size();
    }

    public void add(OWLAxiom axiom, double confidence) {
        log.debug("Add axiom '{}' with confidence ", axiom, confidence);
        axioms.add(new AxiomConfidencePair(axiom, confidence));
        log.info("Chooser state: Axiom {} , Total Weight {}", this.axioms.size(), totalWeight);
    }

//    public void addAll(Collection<ParsedAxiom> axioms) {
//        log.info("Adding {} axioms to choose", axioms.size());
//        for (ParsedAxiom axiom : axioms) {
//            totalWeight += axiom.getConf();
//        }
//        this.axioms.addAll(axioms);
//        log.info("Chooser state: Axiom {} , Total Weight {}", this.axioms.size(), totalWeight);
//    }

    public AxiomConfidencePair choose() {
        // determine maximum possible scaling factor
        double scalingFactor = (Integer.MAX_VALUE) / totalWeight;
        log.info("Choosing axiom");
        int randVal = rand.nextInt();
        log.info("Random value: {}", randVal);
        int curVal = 0;
        AxiomConfidencePair chosen = null;
        Iterator<AxiomConfidencePair> axiomIt = axioms.iterator();
        while (axiomIt.hasNext()) {
            AxiomConfidencePair next = axiomIt.next();
            curVal += next.getConfidence() * scalingFactor;
            if (curVal <= randVal) {
                chosen = next;
                axiomIt.remove();
                totalWeight -= chosen.getConfidence();
                break;
            }
        }

        if (chosen == null) {
            throw new RuntimeException("No axiom chosen!!");
        }

        return chosen;
    }
}
