package miner.ontology;

import miner.util.ScalingValueNormalizer;
import miner.util.SupportConfidenceTuple;

/**
 * Container for axiom data gained by parsing association rule files
 */
public class ParsedAxiom implements ScalingValueNormalizer.NormalizationTarget {
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



    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ParsedAxiom that = (ParsedAxiom) o;

        if (ante1 != that.ante1) {
            return false;
        }
        if (ante2 != that.ante2) {
            return false;
        }
        if (cons != that.cons) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = ante1;
        result = 31 * result + ante2;
        result = 31 * result + cons;
        return result;
    }
}
