package de.uni_mannheim.informatik.dws.goldminer.util;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
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

    private BufferedWriter writer;
    private BufferedWriter addWriter;
    private ConfidenceModificationFunction mod = new ConfidenceModificationFunction.NoopModifier();
            //ArctanModifier(3.77,1.92,2);

    private final static Logger log = LoggerFactory.getLogger(RandomAxiomChooser.class);
    Random rand = new Random();
    
    ArrayList<AxiomConfidencePair> axioms;
    Double totalWeight = 0d;

    public RandomAxiomChooser() {
        axioms = new ArrayList<AxiomConfidencePair>();
//
//        try {
//            writer = new BufferedWriter(new FileWriter("/home/dfleisch/eswc2012/chooserlog.log"));
//            addWriter = new BufferedWriter(new FileWriter("/home/dfleisch/eswc2012/addlog.log"));
//        }
//        catch (IOException e) {
//            throw new RuntimeException(e);
//        }
    }
    
    public int getSize() {
        return axioms.size();
    }

    public void add(OWLAxiom axiom, double confidence) {
        if (confidence == 0) {
            return;
        }
        confidence = mod.getValue(confidence);
//        try {
//            addWriter.write(String.valueOf(confidence));
//            addWriter.newLine();
//        }
//        catch (IOException e) {
//            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
//        }
        log.debug("Add axiom '{}' with confidence {}", axiom, confidence);
        axioms.add(new AxiomConfidencePair(axiom, confidence));
        totalWeight += confidence;
        log.debug("Chooser state: Axiom {} , Total Weight {}", this.axioms.size(), totalWeight);
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
        //double scalingFactor = (Integer.MAX_VALUE) / totalWeight;
        double scalingFactor = 1000;
        log.debug("Scaling: tW {}, factor {}", totalWeight, scalingFactor);
        log.debug("Choosing axiom");
        int randVal = rand.nextInt((int) Math.floor(totalWeight * scalingFactor));
        log.debug("Random value: {}", randVal);
        int curVal = 0;
        AxiomConfidencePair chosen = null;
        Iterator<AxiomConfidencePair> axiomIt = axioms.iterator();

        AxiomConfidencePair next = null;
        while (axiomIt.hasNext()) {
            next = axiomIt.next();
            curVal += next.getConfidence() * scalingFactor;
            if (curVal >= randVal) {
                chosen = next;
                axiomIt.remove();
                totalWeight -= chosen.getConfidence();
                break;
            }
        }

        if (chosen == null) {
            log.info("Forcing last axiom for randVal {} at curVal {}", randVal, curVal);
            chosen = next;
        }

        if (chosen == null) {
            return null;
        }

        log.info("Axiom with confidence {} was chosen", chosen.getConfidence());
//        try {
//            writer.write(String.valueOf(chosen.getConfidence()));
//            writer.newLine();
//        }
//        catch (IOException e) {
//            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
//        }
        return chosen;
    }

    /**
     * Signals the axiom chooser that choosing axioms is done
     */
    public void done() {
//        try {
//            writer.close();
//            addWriter.close();
//        }
//        catch (IOException e) {
//            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
//        }
    }
}
