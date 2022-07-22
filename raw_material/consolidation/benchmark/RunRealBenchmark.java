package raw_material.consolidation.benchmark;

import raw_material.consolidation.cplex.LinearConsolidationSolver;
import raw_material.consolidation.testcase.ConsolidationProblem;
import raw_material.consolidation.testcase.TestcaseFactory;
import ilog.concert.IloException;

public class RunRealBenchmark {

	public static void main(String[] args) throws IloException {
		// Configure benchmark
		final int MAX_NR_TENANTS = 40;
		final int MAX_NR_SERVERS = 30;
		final int MAX_NR_METRICS = 1;
		final int NR_TESTCASES = 1;
		// Generate solvers
		LinearConsolidationSolver linearSolver = new LinearConsolidationSolver();
		// Generate test cases and run benchmark
		for (int nrTenants=MAX_NR_TENANTS; nrTenants<=MAX_NR_TENANTS; ++nrTenants) {
			for (int nrServers=MAX_NR_SERVERS; nrServers<=MAX_NR_SERVERS; ++nrServers) {
				for (int nrMetrics=1; nrMetrics<=MAX_NR_METRICS; ++nrMetrics) {
					for (int testcaseCtr=0; testcaseCtr<NR_TESTCASES; ++testcaseCtr) {
						// Configure testcase factory
						TestcaseFactory.nrTenants = nrTenants;
						TestcaseFactory.nrServers = nrServers;
						TestcaseFactory.nrMetrics = nrMetrics;
						TestcaseFactory.minCapacity = 1;
						TestcaseFactory.maxCapacity = nrTenants * 10;
						TestcaseFactory.minConsumption = 0;
						TestcaseFactory.maxConsumption = 3;
						TestcaseFactory.minCost = 1;
						TestcaseFactory.maxCost = 1;
						TestcaseFactory.minDoubleStep = 0.25;
						// Generate test case
						ConsolidationProblem problem = TestcaseFactory.produce();
						// Solve test case with cplex
						linearSolver.solve(problem);
					}
				}
			}
		}
	}

}
