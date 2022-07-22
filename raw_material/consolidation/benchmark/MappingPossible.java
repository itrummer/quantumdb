package raw_material.consolidation.benchmark;

import static java.lang.System.out;
import raw_material.consolidation.cplex.LinearConsolidationSolver;
import raw_material.consolidation.cplex.QuadraticConsolidationSolver;
import raw_material.consolidation.testcase.ConsolidationProblem;
import raw_material.consolidation.testcase.ConsolidationSolution;
import raw_material.consolidation.testcase.TestcaseFactory;

// Given number of servers and metrics: how many tenants can we have such that X% of the generated test cases
// can still be mapped onto D-Wave?
public class MappingPossible extends BenchmarkRun {
	static double THRESHOLD = 0.9;
	// Determine maximal number of tenants such that percentage of solved test cases exceeds threshold
	private static int[][] determineMaxTenants(int[][][] nrMapped) {
		int[][] maxTenants = new int[MAX_SERVERS][MAX_METRICS];
		for (int servers=1; servers<=MAX_SERVERS; ++servers) {
			for (int metrics=1; metrics<=MAX_METRICS; ++metrics) {
				int curMaxTenants = 0;
				for (int tenants=1; tenants<=MAX_TENANTS; ++tenants) {
					int curNrMapped = nrMapped[servers-1][metrics-1][tenants-1];
					double percentage = (double)curNrMapped/NR_TESTCASES;
					if (percentage>THRESHOLD) {
						curMaxTenants = tenants;
					} else {
						break;
					}
				}
				maxTenants[servers-1][metrics-1] = curMaxTenants;
			}
		}
		return maxTenants;
	}
	// Output LaTeX representation of max-tenants table
	private static void outputLaTeXtable(int[][] maxTenants) {
		// table header
		out.print("\\begin{tabular}");
		out.print("{l");
		for (int i=0; i<MAX_METRICS; ++i) {
			out.print("l");
		}
		out.println("}");
		out.println("\\toprule[1pt]");
		out.println("{\\bf Servers} & \\multicolumn{" + MAX_METRICS + "}{c}{{\\bf Metrics}} \\\\");
		out.println("\\cmidrule(r){2-" + (MAX_METRICS+1) + "}");
		for (int i=0; i<MAX_METRICS; ++i) {
			out.print(" & {\\bf " + (i+1) + "}");
		}
		// middle section
		for (int servers=1; servers<=MAX_SERVERS; ++servers) {
			out.println("\\\\");
			out.println("\\midrule");
			out.print("{\\bf " + servers + "}");
			for (int metrics=1; metrics<=MAX_METRICS; ++metrics) {
				out.print("& " + maxTenants[servers-1][metrics-1]);
			}
		}
		// table footer
		out.println("\\\\");
		out.println("\\bottomrule[1pt]");
		out.println("\\end{tabular}");
	}
	public static void main(String[] args) throws Exception {
		// Configure test case factory
		configureFactory();
		// Will store result
		int[][][] mappedTriangle = new int[MAX_SERVERS][MAX_METRICS][MAX_TENANTS];
		int[][][] mappedMatrix = new int[MAX_SERVERS][MAX_METRICS][MAX_TENANTS];
		// Generate solvers
		LinearConsolidationSolver linearSolver = new LinearConsolidationSolver();
		QuadraticConsolidationSolver quadraticTriangleSolver = new QuadraticConsolidationSolver(triangleMapper);
		QuadraticConsolidationSolver quadraticMatrixSolver = new QuadraticConsolidationSolver(matrixMapper);
		// Iterate over different number of tenants/servers/metrics
		for (int servers=1; servers<=MAX_SERVERS; ++servers) {
			for (int metrics=1; metrics<=MAX_METRICS; ++metrics) {
				for (int tenants=1; tenants<=MAX_TENANTS; ++tenants) {
					// Configure factory
					TestcaseFactory.nrTenants = tenants;
					TestcaseFactory.nrServers = servers;
					TestcaseFactory.nrMetrics = metrics;
					// Initialize (not necessary actually)
					mappedTriangle[servers-1][metrics-1][tenants-1] = 0;
					mappedMatrix[servers-1][metrics-1][tenants-1] = 0;
					// Generate test cases
					for (int testcaseCtr=0; testcaseCtr<NR_TESTCASES; ++testcaseCtr) {
						ConsolidationProblem problem = TestcaseFactory.produce();
						// Try to map by triangle mapper, check consistency
						try {
							triangleMapper.transform(problem);
							mappedTriangle[servers-1][metrics-1][tenants-1] += 1;
							ConsolidationSolution quadraticSolution = quadraticTriangleSolver.solve(problem);
							ConsolidationSolution linearSolution = linearSolver.solve(problem);
							assert(quadraticSolution.isEquivalent(linearSolution));
						} catch (Exception e) {
							// mapping impossible
						}
						// Try to map by matrix mapper, check consistency
						try {
							matrixMapper.transform(problem);
							mappedMatrix[servers-1][metrics-1][tenants-1] += 1;
							ConsolidationSolution quadraticSolution = quadraticMatrixSolver.solve(problem);
							ConsolidationSolution linearSolution = linearSolver.solve(problem);
							assert(quadraticSolution.isEquivalent(linearSolution));
						} catch (Exception e) {
							// mapping impossible
						}						
					}
				}
			}
		}
		// Determine maximal number of tenants such that percentage of solved test cases exceeds threshold
		int[][] maxTenantsTriangle = determineMaxTenants(mappedTriangle);
		int[][] maxTenantsMatrix = determineMaxTenants(mappedMatrix);
		out.println("Results for triangle mapper: ");
		outputLaTeXtable(maxTenantsTriangle);
		out.println("Results for matrix mapper: ");
		outputLaTeXtable(maxTenantsMatrix);
	}
}
