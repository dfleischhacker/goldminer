package miner.ontology;

public class ParsedAxiom {
	
	private int ante1;
	private int ante2;
	private int cons;
	private double supp;
	private double conf;

	public ParsedAxiom(int ante1, int cons, double supp, double conf) {
		this.ante1= ante1;
		this.cons = cons;
		this.supp = supp;
		this.conf = conf;
	}
	
	public ParsedAxiom(int ante1, int ante2, int cons, double supp, double conf) {
		this.ante1= ante1;
		this.ante2 = ante2;
		this.cons = cons;
		this.supp = supp;
		this.conf = conf;
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
	 * @return the supp
	 */
	public double getSupp() {
		return supp;
	}

	/**
	 * @return the conf
	 */
	public double getConf() {
		return conf;
	}
}
