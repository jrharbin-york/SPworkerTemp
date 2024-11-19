package datatypes;

import java.util.Random;

public class DoubleRange {
	
	private double lower, upper;
	private double range;

	public DoubleRange(double lower, double upper) {
		this.lower = Math.min(lower, upper);
		this.upper = Math.max(lower, upper);
		this.range = this.upper - this.lower;
	}
	
	public Double generateInRange(Random r) {
		return r.nextDouble() * range + this.lower;
	}
}