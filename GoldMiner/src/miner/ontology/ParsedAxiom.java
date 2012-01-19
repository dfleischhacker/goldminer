package miner.ontology;

import miner.util.ConfidenceValueNormalization;

/**
 * Container for axiom data gained by parsing association rule files
 */
public class ParsedAxiom implements ConfidenceValueNormalization.NormalizationTarget {
	private int ante1;
	private int ante2;
	private int cons;
    private SupportConfidenceTuple tuple;

	public ParsedAxiom(int ante1, int cons, double supp, double conf) {
		this.ante1= ante1;
		this.cons = cons;
        this.tuple = new SupportConfidenceTuple(supp, conf);
	}
	
	public ParsedAxiom(int ante1, int ante2, int cons, double supp, double conf) {
		this.ante1= ante1;
		this.ante2 = ante2;
		this.cons = cons;
        this.tuple = new SupportConfidenceTuple(supp, conf);
	}

	/**
	 * @return the ante1
	 */
	public int getAnte1() {
		return ante1;
	}

	/**
	 * @return the ante2
	 */
	public int getAnte2() {
		return ante2;
	}

	/**
	 * @return the cons
	 */
	public int getCons() {
		return cons;
	}

	/**
     * Returns the support for this axiom. Actually just wrapping {@code SupportConfidenceTuple.getSupport()}
	 * @return support for this parsed axiom
	 */
	public double getSupp() {
		return tuple.getSupport();
	}

	/**
     * Returns the confidence for this axiom. Actually just wrapping {@code SupportConfidenceTuple.getConfidence()}
	 * @return confidence for this parsed axiom
	 */
	public double getConf() {
		return tuple.getConfidence();
	}
    
    public SupportConfidenceTuple getSuppConfTuple() {
        return tuple;
    }

    @Override
    public double getValue() {
        return tuple.getConfidence();
    }

    @Override
    public void setValue(double value) {
        tuple.setConfidence(value);
    }

    /**
     * Tuple containing support and confidence values both being double values
     */
    public static class SupportConfidenceTuple {
        private double support;
        private double confidence;

        private SupportConfidenceTuple(double support, double confidence) {
            this.support = support;
            this.confidence = confidence;
        }

        public double getSupport() {
            return support;
        }

        public double getConfidence() {
            return confidence;
        }

        public void setSupport(double support) {
            this.support = support;
        }

        public void setConfidence(double confidence) {
            this.confidence = confidence;
        }
    }
}
