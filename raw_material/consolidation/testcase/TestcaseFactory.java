package raw_material.consolidation.testcase;

import raw_material.util.RandomUtil;

// Produces test cases for generic solvers and mappers.
public class TestcaseFactory {
	// The following configuration parameters must be set prior to invoking the produce function.
	public static int nrTenants 		= -1;
	public static int nrServers 		= -1;
	public static int nrMetrics 		= -1;
	public static double minDoubleStep 	= -1;
	public static int minConsumption	= -1;
	public static int maxConsumption	= -1;
	public static int minCapacity		= -1;
	public static int maxCapacity		= -1;
	public static int minCost			= -1;
	public static int maxCost			= -1;
	// Produces generic consolidation problem according to current configuration. 
	public static ConsolidationProblem produce() {
		// Verify that all configuration parameters have been set.
		assert(nrTenants >= 0);
		assert(nrServers >= 0);
		assert(nrMetrics >= 0);
		assert(minDoubleStep >= 0);
		assert(minConsumption >= 0);
		assert(maxConsumption >= 0);
		assert(minCapacity >= 0);
		assert(maxCapacity >= 0);
		assert(minCost >= 0);
		assert(maxCost >= 0);
		// Create result problem
		ConsolidationProblem result = new ConsolidationProblem(nrTenants, nrServers, nrMetrics, minDoubleStep);
		// Choose tenant consumptions
		for (int tenant=0; tenant<nrTenants; ++tenant) {
			for (int metric=0; metric<nrMetrics; ++metric) {
				int consumptionDiscrete = RandomUtil.uniformInt(minConsumption, maxConsumption);
				double consumption		= consumptionDiscrete * minDoubleStep;
				result.setConsumption(tenant, metric, consumption);
			}
		}
		// Choose server capacities
		for (int server=0; server<nrServers; ++server) {
			for (int metric=0; metric<nrMetrics; ++metric) {
				int capacityDiscrete	= RandomUtil.uniformInt(minCapacity, maxCapacity);
				double capacity			= capacityDiscrete * minDoubleStep;
				result.setCapacity(server, metric, capacity);
			}
		}
		// Choose server operational cost
		for (int server=0; server<nrServers; ++server) {
			int operationalCostDiscrete	= RandomUtil.uniformInt(minCost, maxCost);
			double operationalCost		= operationalCostDiscrete * minDoubleStep;
			result.setServerCost(server, operationalCost);
		}
		return result;
	}
}
