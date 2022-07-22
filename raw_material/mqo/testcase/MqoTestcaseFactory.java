package raw_material.mqo.testcase;

import raw_material.util.RandomUtil;



/**
 * Randomly generates multiple query optimization problems as test cases with a specified
 * number of queries, query plans, and intermediate results to share between different
 * query plans.
 * 
 * @author immanueltrummer
 *
 */
public class MqoTestcaseFactory {
	static double randomCost() {
		return RandomUtil.uniformInt(1, 4) * 0.25;
	}
	/**
	 * Randomly generates a test case with the specified number of queries, plans, and results.
	 * 
	 * @param nrQueries					the number of queries that may share intermediate results
	 * @param nrPlansPerQuery			the number of alternative query plans per query
	 * @param nrIntermediateResults		the number of intermediate results shared between queries
	 * @return							a randomly generated multiple query optimization problem instance
	 */
	public static MqoProblem produce(int nrQueries, int nrPlansPerQuery, int nrIntermediateResults) {
		// Generate problem with default cost and without dependencies
		MqoProblem problem = new MqoProblem(nrQueries, nrPlansPerQuery, nrIntermediateResults);
		// Set plan costs
		for (int query=0; query<nrQueries; ++query) {
			for (int plan=0; plan<nrPlansPerQuery; ++plan) {
				//double planCost = RandomUtil.random.nextDouble();
				double planCost = randomCost();
				problem.setPlanCost(query, plan, planCost);
			}
		}
		// Set intermediate result cost
		for (int result=0; result<nrIntermediateResults; ++result) {
			//double resultCost = RandomUtil.random.nextDouble();
			//double resultCost = 0.5;	// TODO
			double resultCost = randomCost();
			problem.setResultCost(result, resultCost);
		}
		// Set dependencies
		/*
		int nrDependentPlans = 3;
		for (int result=0; result<nrIntermediateResults; ++result) {
			for (int dependency=0; dependency<nrDependentPlans; ++dependency) {
				int query = RandomUtil.uniformInt(0, nrQueries-1);
				int plan = RandomUtil.uniformInt(0, nrPlansPerQuery-1);
				problem.setDependency(query, plan, result);
			}
		}
		*/
		// Reasonable assumption: each plan does not depend on too many intermediate results
		for (int query=0; query<nrQueries; ++query) {
			for (int plan=0; plan<nrPlansPerQuery; ++plan) {
				int nrDependencies = RandomUtil.uniformInt(3,3);
				for (int dependency=0; dependency<nrDependencies; ++dependency) {
					int result = RandomUtil.uniformInt(0, nrIntermediateResults-1);
					problem.setDependency(query, plan, result);					
				}
			}
		}
		// Make sure that each intermediate result is used by at least one query plan
		for (int result=0; result<nrIntermediateResults; ++result) {
			boolean dependencyFound = false;
			for (int query=0; query<nrQueries; ++query) {
				for (int plan=0; plan<nrPlansPerQuery; ++plan) {
					if (problem.getDependency(query, plan, result)) {
						dependencyFound = true;
					}
				}
			}
			// Add random dependency if none exists
			if (!dependencyFound) {
				int query = RandomUtil.uniformInt(0, nrQueries-1);
				int plan = RandomUtil.uniformInt(0, nrPlansPerQuery-1);
				problem.setDependency(query, plan, result);
			}
		}
		/*
		// Set dependencies		
		for (int query=0; query<nrQueries; ++query) {
			for (int plan=0; plan<nrPlansPerQuery; ++plan) {
				for (int result=0; result<nrIntermediateResults; ++result) {
					if (RandomUtil.random.nextDouble() > 0.25) {
						problem.setDependency(query, plan, result);
					}
				}
			}
		}
		*/
		return problem;
	}
}
