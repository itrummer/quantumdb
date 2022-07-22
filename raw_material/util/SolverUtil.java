package raw_material.util;

public class SolverUtil {
	// Interpret cplex solution value as binary integer (must be close to 0 or 1!).
	public static int cplexBinaryValue(double value) {
		assert(value <= 0.001 || value >= 0.999);
		int roundedValue = (int)Math.round(value);
		assert(roundedValue == 0 || roundedValue == 1);
		return roundedValue;
	}
}
