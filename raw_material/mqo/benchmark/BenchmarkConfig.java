package raw_material.mqo.benchmark;


/**
 * Contains benchmark configuration parameter that are used by all classes running benchmarks
 * or postprocessing. Additionally contains some auxiliary functions for instance for generating
 * file names.
 * 
 * @author immanueltrummer
 *
 */
public class BenchmarkConfig {
	/**
	 * The number of test cases to generate and solve per configuration (i.e., per number of
	 * queries, plans, and results).
	 */
	final static int NR_TESTCASES = 2;
	/**
	 * Different numbers of queries to benchmark solvers for.
	 */
	final static int[] nrQueries = {20};
	/**
	 * Different numbers of alternative plans per query to benchmark solvers for.
	 */
	final static int[] nrPlansPerQuery = {3};
	/**
	 * Different numbers of intermediate results (that plans may depend on) to benchmark solvers for.
	 */
	final static int[] nrIntermediateResults = {20};
	/**
	 * String describing the solver type that is passed as command line argument to C program.
	 */
	final static String DWAVE_SOLVER = "REMOTE";
	/**
	 * Whether the D-Wave solver (either hardware or software) should be used or whether we
	 * just want to execute the embedding to obtain the required number of qubits.
	 */
	final static boolean USE_DWAVE_SOLVER = false;
	/**
	 * Returns string representation of configuration ID (i.e., number of queries, plans, and results).
	 * This configuration ID is used to generate names of output files.
	 * 
	 * @param nrQueries	how many queries are optimized
	 * @param nrPlans	how many alternative query plans are available per query
	 * @param nrResults	how many intermediate results may be shared
	 * @return			String representation for configuration suitable as path prefix etc.
	 */
	public static String configurationID(int nrQueries, int nrPlans, int nrResults) {
		return "Q" + nrQueries + "P" + nrPlans + "R" + nrResults;
	}
	/**
	 * Returns string representation of test case ID. This test case ID is used to generate names
	 * of output files.
	 * 
	 * @param nrQueries	how many queries are optimized
	 * @param nrPlans	how many alternative query plans are available per query
	 * @param nrResults	how many intermediate results may be shared
	 * @param testcase	test case index within configuration
	 * @return			String representation of test case ID suitable as path prefix etc.
	 */
	public static String testcaseID(int nrQueries, int nrPlans, int nrResults, int testcase) {
		return configurationID(nrQueries, nrPlans, nrResults) + "T" + testcase;
	}
	/**
	 * Generates the path to the test case MQO problem.
	 * 
	 * @param nrQueries	how many queries are optimized
	 * @param nrPlans	how many alternative query plans are available per query
	 * @param nrResults	how many intermediate results may be shared
	 * @param testcase	test case index
	 * @return			Path to where the corresponding test case is stored
	 */
	public static String dwaveTestcasePath(int nrQueries, int nrPlans, int nrResults, int testcase) {
		String testcaseID = configurationID(nrQueries, nrPlans, nrResults) + "T" + testcase;
		return "dwave/test_" + testcaseID;
	}
	/**
	 * Generates the path to the QUBO representation of the corresponding MQO problem.
	 * 
	 * @param nrQueries	how many queries are optimized
	 * @param nrPlans	how many alternative query plans are available per query
	 * @param nrResults	how many intermediate results may be shared
	 * @param testcase	test case index
	 * @return			Path to the QUBO representation
	 */
	public static String dwaveQuboPath(int nrQueries, int nrPlans, int nrResults, int testcase) {
		String testcaseID = configurationID(nrQueries, nrPlans, nrResults) + "T" + testcase;
		return "dwave/QUBO_" + testcaseID;
	}
	/**
	 * Generates the path to a file where the mapping from a MQO problem to the QUBO representation is stored.
	 * 
	 * @param nrQueries	how many queries are optimized
	 * @param nrPlans	how many alternative query plans are available per query
	 * @param nrResults	how many intermediate results may be shared
	 * @param testcase	test case index
	 * @return			Path to the mapping file
	 */
	public static String dwaveMappingPath(int nrQueries, int nrPlans, int nrResults, int testcase) {
		String testcaseID = configurationID(nrQueries, nrPlans, nrResults) + "T" + testcase;
		return "dwave/mapping_" + testcaseID;
	}
	/**
	 * Generates the path to a file where the solution for a QUBO representing a MQO problem is stored.
	 * 
	 * @param nrQueries	how many queries are optimized
	 * @param nrPlans	how many alternative query plans are available per query
	 * @param nrResults	how many intermediate results may be shared
	 * @param testcase	test case index
	 * @return			Path to the solution file
	 */
	public static String dwaveSolutionPath(int nrQueries, int nrPlans, int nrResults, int testcase) {
		String testcaseID = configurationID(nrQueries, nrPlans, nrResults) + "T" + testcase;
		return "dwave/QUBO_" + testcaseID + "_solution";
	}
	/**
	 * Generates the path to a file where benchmark results concerning solution quality are stored.
	 * 
	 * @param nrQueries	how many queries are optimized
	 * @param nrPlans	how many alternative query plans are available per query
	 * @param nrResults	how many intermediate results may be shared
	 * @return			Path to the result file
	 */
	public static String dwaveQualityResultsPath(int nrQueries, int nrPlans, int nrResults) {
		return "dwave/quality_" + configurationID(nrQueries, nrPlans, nrResults);
	}
}
