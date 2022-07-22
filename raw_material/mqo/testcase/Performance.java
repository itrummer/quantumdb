package raw_material.mqo.testcase;

/**
 * Summarizes the performance of one solver for one test case.
 * 
 * @author immanueltrummer
 *
 */
public class Performance {
	/**
	 * The number of milliseconds required for optimizing the current test case.
	 */
	final long millis;
	
	public Performance(long millis) {
		this.millis = millis;
	}
}
