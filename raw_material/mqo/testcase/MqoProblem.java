package raw_material.mqo.testcase;

import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * Describes a multiple query optimization problem.
 * 
 * @author immanueltrummer
 *
 */
public class MqoProblem implements Serializable {
	/**
	 * Used to verify class version.
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * The number of queries that may share intermediate results.
	 */
	public final int nrQueries;
	/**
	 * The number of alternative query plans for each query.
	 */
	public final int nrPlansPerQuery;
	/**
	 * The number of intermediate results that may be shared.
	 */
	public final int nrIntermediateResults;
	/**
	 * The cost for each plan. The first index is the query, the second index is the plan.
	 */
	final double[][] planCost;
	/**
	 * The cost for each intermediate result.
	 */
	final double[] intermediateResultCost;
	/**
	 * Whether one specific plan requires one specific intermediate result. The first index is the query, 
	 * the second index is the plan, and the third is the result index. A field for a given index is set to
	 * true if the plan designated by the index for the query designated by the index requires the 
	 * intermediate result designated by the index. 
	 */
	final boolean[][][] planUsesResult;
	
	public MqoProblem(int nrQueries, int nrPlansPerQuery, int nrIntermediateResults) {
		this.nrQueries = nrQueries;
		this.nrPlansPerQuery = nrPlansPerQuery;
		this.nrIntermediateResults = nrIntermediateResults;
		this.planCost = new double[nrQueries][nrPlansPerQuery];
		this.intermediateResultCost = new double[nrIntermediateResults];
		this.planUsesResult = new boolean[nrQueries][nrPlansPerQuery][nrIntermediateResults];
	}
	/**
	 * Sets the cost for one specific query plan for a specific query.
	 * 
	 * @param queryIndex	the index of the query that the plan answers
	 * @param planIndex		the index of the plan among all plans answering the same query
	 * @param cost			the cost of that plan
	 */
	public void setPlanCost(int queryIndex, int planIndex, double cost) {
		planCost[queryIndex][planIndex] = cost;
	}
	/**
	 * Sets the cost for generating one specific intermediate result.
	 * 
	 * @param resultIndex	the index of the intermediate result
	 * @param cost			the cost of generating that intermediate result
	 */
	public void setResultCost(int resultIndex, double cost) {
		intermediateResultCost[resultIndex] = cost;
	}
	/**
	 * Store that the specified query plan requires the specified intermediate result.
	 * 
	 * @param queryIndex	the index of the query that the plan answers
	 * @param planIndex		the index of the plan among all plans answering the same query
	 * @param resultIndex	the index of the required intermediate result
	 */
	public void setDependency(int queryIndex, int planIndex, int resultIndex) {
		planUsesResult[queryIndex][planIndex][resultIndex] = true;
	}
	/**
	 * Returns the cost of the specified query plan.
	 * 
	 * @param queryIndex	the index of the query that the plan answers
	 * @param planIndex		the index of the plan among all plans answering the same query
	 * @return				returns the cost of the specified query plan
	 */
	public double getPlanCost(int queryIndex, int planIndex) {
		return planCost[queryIndex][planIndex];
	}
	/**
	 * Returns the cost of generating the specified intermediate result.
	 * 
	 * @param resultIndex	the index of the intermediate result
	 * @return				the cost of generating that intermediate result
	 */
	public double getResultCost(int resultIndex) {
		return intermediateResultCost[resultIndex];
	}
	/**
	 * Returns Boolean indicating whether the given query plan depends on the given intermediate result.
	 * 
	 * @param queryIndex	the index of the query that the plan answers
	 * @param planIndex		the index of the plan among all plans answering the same query
	 * @param resultIndex	the index of the required intermediate result
	 * @return				true if the plan requires the intermediate result and false otherwise
	 */
	public boolean getDependency(int queryIndex, int planIndex, int resultIndex) {
		return planUsesResult[queryIndex][planIndex][resultIndex];
	}
	/**
	 * Returns maximal plan execution cost over all plans and queries.
	 * 
	 * @return	the maximal execution cost per plan
	 */
	public double getMaxPlanCost() {
		double maxCost = 0;
		for (int query=0; query<nrQueries; ++query) {
			for (int plan=0; plan<nrPlansPerQuery; ++plan) {
				maxCost = Math.max(maxCost, planCost[query][plan]);
			}
		}
		return maxCost;
	}
	/**
	 * Returns maximal generation cost over all intermediate results.
	 * 
	 * @return	the maximal generation cost over all intermediate results
	 */
	public double getMaxResultCost() {
		double maxCost = 0;
		for (int result=0; result<nrIntermediateResults; ++result) {
			maxCost = Math.max(maxCost, intermediateResultCost[result]);
		}
		return maxCost;
	}
	/**
	 * Returns maximal cost for executing a plan and generating all results it depends on.
	 * 
	 * @return	the maximal added cost of plan execution and dependent result generation over all plan
	 */
	public double getMaxPlanResultsCost() {
		double maxCost = 0;
		for (int query=0; query<nrQueries; ++query) {
			for (int plan=0; plan<nrPlansPerQuery; ++plan) {
				double planResultCost = planCost[query][plan];
				for (int result=0; result<nrIntermediateResults; ++result) {
					if (getDependency(query, plan, result)) {
						planResultCost += intermediateResultCost[result];
					}
				}
				maxCost = Math.max(planResultCost, maxCost);
			}
		}
		return maxCost;
	}
	/**
	 * Writes a description of the problem instance to the console.
	 */
	public void toConsole() {
		// Output problem dimensions
		System.out.println("nrQueries: " + nrQueries);
		System.out.println("nrPlansPerQuery: " + nrPlansPerQuery);
		System.out.println("nrIntermediateResults: " + nrIntermediateResults);
		// Output dependencies
		for (int query=0; query<nrQueries; ++query) {
			for (int plan=0; plan<nrPlansPerQuery; ++plan) {
				for (int result=0; result<nrIntermediateResults; ++result) {
					if (getDependency(query, plan, result)) {
						System.out.println("Plan " + plan + " for query " + query + " depends on result " + result);
					}
				}
			}
		}
		// Output plan execution costs
		for (int query=0; query<nrQueries; ++query) {
			for (int plan=0; plan<nrPlansPerQuery; ++plan) {
				System.out.println("Plan " + plan + " for query " + query + " has cost " + planCost[query][plan]);
			}
		}
		// Output result generation cost
		for (int result=0; result<nrIntermediateResults; ++result) {
			System.out.println("Result " + result + " has cost " + intermediateResultCost[result]);
		}
	}
	/**
	 * Serializes this object and writes it out to the specified file on disc.
	 * 
	 * @param fileName	the name of the file to store this object in
	 * @throws Exception 
	 */
	public void toFile(String fileName) throws Exception {
		FileOutputStream fout = new FileOutputStream(fileName);
		ObjectOutputStream oos = new ObjectOutputStream(fout);   
		oos.writeObject(this);
		oos.close();		
	}
	@Override
	public boolean equals(Object otherObject) {
		// Cast object to MQO problem
		MqoProblem otherProblem = (MqoProblem)otherObject;
		// Verify that problem dimensions match
		if (nrQueries != otherProblem.nrQueries) {
			return false;
		}
		if (nrPlansPerQuery != otherProblem.nrPlansPerQuery) {
			return false;
		}
		if (nrIntermediateResults != otherProblem.nrIntermediateResults) {
			return false;
		}
		// Verify that plan execution costs match
		for (int query=0; query<nrQueries; ++query) {
			for (int plan=0; plan<nrPlansPerQuery; ++plan) {
				if (planCost[query][plan] != otherProblem.planCost[query][plan]) {
					return false;
				}
			}
		}
		// Verify that intermediate result generation costs match
		for (int result=0; result<nrIntermediateResults; ++result) {
			if (intermediateResultCost[result] != otherProblem.intermediateResultCost[result]) {
				return false;
			}
		}
		// Verify that dependencies are equivalent
		for (int query=0; query<nrQueries; ++query) {
			for (int plan=0; plan<nrPlansPerQuery; ++plan) {
				for (int result=0; result<nrIntermediateResults; ++result) {
					if (getDependency(query, plan, result) != otherProblem.getDependency(query, plan, result)) {
						return false;
					}
				}
			}
		}
		// If all prior tests checked out then the two problem instances are equal
		return true;
	}
	@Override
	public int hashCode() {
		return nrQueries + nrPlansPerQuery + nrIntermediateResults + 
				planCost.hashCode() + intermediateResultCost.hashCode() + planUsesResult.hashCode();
	}
}
