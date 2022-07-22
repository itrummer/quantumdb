package raw_material.mqo.dwave;

import raw_material.mqo.testcase.MqoProblem;

/**
 * Maps a multiple query optimization problem to a QUBO representation that can be solved
 * by the quantum computer.
 * 
 * @author immanueltrummer
 *
 */
public class MqoMapper {
	/**
	 * Transform a MQO problem into a QUBO representation.
	 * 
	 * @param problem	the multiple query optimization problem
	 * @return			a QUBO representation of the input problem
	 */
	public static MqoMapping map(MqoProblem problem) {
		// This will contain the result
		MqoMapping mapping = new MqoMapping();
		// Extract problem counters
		int nrQueries = problem.nrQueries;
		int nrPlansPerQuery = problem.nrPlansPerQuery;
		int nrIntermediateResults = problem.nrIntermediateResults;
		// Create variables representing alternative plans
		VarMapping[][] planVars = new VarMapping[nrQueries][nrPlansPerQuery];
		mapping.planVars = planVars;
		// Create variables capturing whether specific intermediate results are generated
		VarMapping[] resultVars = new VarMapping[nrIntermediateResults];
		mapping.resultVars = resultVars;
		// Assign consecutive indices to all variables
		int nextQubit = 0;
		for (int query=0; query<nrQueries; ++query) {
			for (int plan=0; plan<nrPlansPerQuery; ++plan) {
				planVars[query][plan] = new VarMapping(nextQubit);
				++nextQubit;
			}
		}
		for (int result=0; result<nrIntermediateResults; ++result) {
			resultVars[result] = new VarMapping(nextQubit);
			++nextQubit;
		}
		// Impose constraints enforcing that one plan is executed for each query.
		// Not executing a plan allows to save its execution cost and perhaps the cost of
		// generating the intermediate results it depends on.
		double executionConstraintScaling = 2 * problem.getMaxPlanResultsCost();
		for (int query=0; query<nrQueries; ++query) {
			// Motivate to set one plan variable for the current query to true
			for (int plan=0; plan<nrPlansPerQuery; ++plan) {
				// Not executing a query saves at most the cost of executing its most expensive plan.
				planVars[query][plan].addWeight(mapping, -executionConstraintScaling);
			}
			// De-motivate setting more than one plan variable to true
			for (int plan1=0; plan1<nrPlansPerQuery; ++plan1) {
				for (int plan2=plan1+1; plan2<nrPlansPerQuery; ++plan2) {
					VarMapping var1 = planVars[query][plan1];
					VarMapping var2 = planVars[query][plan2];
					var1.addWeight(mapping, var2, 2 * executionConstraintScaling);
				}
			}
		}
		// Impose constraints enforcing that the intermediate results of each executed plan are generated
		double dependencyConstraintScaling = 2 * problem.getMaxResultCost();
		for (int query=0; query<nrQueries; ++query) {
			for (int plan=0; plan<nrPlansPerQuery; ++plan) {
				for (int result=0; result<nrIntermediateResults; ++result) {
					if (problem.getDependency(query, plan, result)) {
						// Violating one dependency constraint by executing a plan while not generating
						// one of the intermediate results it depends on saves at most the cost for
						// generating the most expensive intermediate result compared to a valid solution.
						// Scaling by more than the cost of the most expensive intermediate result is hence
						// sufficient. The energy term representing the dependency constraint must take
						// a value higher than zero if the dependent plan is set to one and the result
						// variable is set to zero -> (plan * (1-result)) = plan - plan * result.
						VarMapping planVar = planVars[query][plan];
						VarMapping resultVar = resultVars[result];
						planVar.addWeight(mapping, dependencyConstraintScaling);
						planVar.addWeight(mapping, resultVar, - dependencyConstraintScaling);
					}
				}
			}
		}
		// Add cost for executing plans
		for (int query=0; query<nrQueries; ++query) {
			for (int plan=0; plan<nrPlansPerQuery; ++plan) {
				double planCost = problem.getPlanCost(query, plan);
				planVars[query][plan].addWeight(mapping, planCost);
			}
		}
		// Add cost for generating intermediate results
		for (int result=0; result<nrIntermediateResults; ++result) {
			double resultCost = problem.getResultCost(result);
			resultVars[result].addWeight(mapping, resultCost);
		}
		// Return finished QUBO mapping
		return mapping;
	}
}
