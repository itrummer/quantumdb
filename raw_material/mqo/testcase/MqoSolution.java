package raw_material.mqo.testcase;

import java.io.FileWriter;
import java.io.PrintWriter;

/**
 * Represents the optimal (or near-optimal) solution to a MQO problem.
 * 
 * @author immanueltrummer
 *
 */
public class MqoSolution {
	/**
	 * The multiple query optimization problem that this solution refers to.
	 */
	final MqoProblem problem;
	/**
	 * The value of the objective function (solver dependent).
	 */
	final double objectiveVal;
	/**
	 * Whether the corresponding plan is executed.
	 */
	final boolean[][] executedPlans;
	/**
	 * Whether the corresponding result is generated.
	 */
	final boolean[] generatedResults;
	/**
	 * Performance statistics describing the optimizer run in which this solution was created.
	 */
	final Performance performanceStats;
	
	public MqoSolution(MqoProblem problem, double objectiveVal, boolean[][] executedPlans, 
			boolean[] generatedResults, Performance performanceStats) {
		this.problem = problem;
		this.objectiveVal = objectiveVal;
		this.executedPlans = executedPlans;
		this.generatedResults = generatedResults;
		this.performanceStats = performanceStats;
	}
	/**
	 * Calculates the cost of the given solution.
	 * 
	 * @return	the cost of executing all specified plans and generating all specified intermediate results
	 */
	public double getCost() {
		double cost = 0;
		// Add cost for executing plans
		for (int query=0; query<problem.nrQueries; ++query) {
			for (int plan=0; plan<problem.nrPlansPerQuery; ++plan) {
				if (executedPlans[query][plan]) {
					cost += problem.getPlanCost(query, plan);					
				}
			}
		}
		// Add cost of generating intermediate results
		for (int result=0; result<problem.nrIntermediateResults; ++result) {
			if (generatedResults[result]) {
				cost += problem.getResultCost(result);				
			}
		}
		return cost;
	}
	/**
	 * Returns true if at least one plan was selected for execution for each query.
	 * 
	 * @return	true if each query is executed by at least one plan
	 */
	public boolean allQueriesExecuted() {
		for (int query=0; query<problem.nrQueries; ++query) {
			boolean executed = false;
			for (int plan=0; plan<problem.nrPlansPerQuery; ++plan) {
				if (executedPlans[query][plan]) {
					executed = true;
				}
			}
			if (!executed) {
				return false;
			}
		}
		return true;
	}
	/**
	 * Returns true if all dependencies are satisfied, meaning that each executed plan has
	 * all the required intermediate results generated.
	 * 
	 * @return	true if and only if all dependencies are satisfied
	 */
	public boolean allDependenciesSatisfied() {
		for (int query=0; query<problem.nrQueries; ++query) {
			for (int plan=0; plan<problem.nrPlansPerQuery; ++plan) {
				// Only need to check dependencies for executed plans
				if (executedPlans[query][plan]) {
					for (int result=0; result<problem.nrIntermediateResults; ++result) {
						if (problem.getDependency(query, plan, result)) {
							if (!generatedResults[result]) {
								return false;
							}
						}
					}
				}
			}
		}
		return true;
	}
	/**
	 * Outputs the problem solution
	 */
	public void toConsole() {
		// Extract required counters
		int nrQueries = problem.nrQueries;
		int nrPlans = problem.nrPlansPerQuery;
		int nrResults = problem.nrIntermediateResults;
		// Overview: is the solution valid and how much does it cost?
		System.out.println("Objective value           \t" + objectiveVal);
		System.out.println("Execution cost            \t" + getCost());
		System.out.println("All queries executed      \t" + allQueriesExecuted());
		System.out.println("All dependencies satisfied\t" + allDependenciesSatisfied());
		// Output executed plans
		for (int query=0; query<nrQueries; ++query) {
			for (int plan=0; plan<nrPlans; ++plan) {
				if (executedPlans[query][plan]) {
					System.out.println("Query " + query + " executed by plan " + plan);
				}
			}
		}
		// Output generated results
		for (int result=0; result<nrResults; ++result) {
			if (generatedResults[result]) {
				System.out.println("Result " + result + " generated");
			}
		}
	}
	/**
	 * Writes out one row summarizing this solution into the existing result file.
	 * 
	 * @param fileName		name of the result file to append to
	 * @param testcase		test case index 
	 * @throws Exception
	 */
	public void addResultRow(String fileName, int testcase) throws Exception {
		// Open file for appending text
		PrintWriter writer = new PrintWriter(new FileWriter(fileName, true));
		writer.println(testcase + "," + performanceStats.millis + "," + getCost());
		writer.close();
	}
}
