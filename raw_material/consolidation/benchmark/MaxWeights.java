package raw_material.consolidation.benchmark;

import raw_material.consolidation.dwave.ConsolidationMappingGeneric;
import raw_material.consolidation.testcase.ConsolidationProblem;
import raw_material.consolidation.testcase.TestcaseFactory;

public class MaxWeights extends BenchmarkRun {
	// Analyse weights and generate output
	private static void analyseWeights(int tenants, int servers, int metrics, ConsolidationMappingGeneric mapping, String mapperID) {
		System.out.println(tenants + " tenants; " + servers + " servers; " + metrics + " metrics - " + mapperID);
		System.out.println("max abs. single qb weight: " + mapping.getMaxAbsWeight(true, false));
		System.out.println("max abs. connection weight: " + mapping.getMaxAbsWeight(false, true));
		System.out.println("min abs. single qb weight > 0: " + mapping.getMinAbsWeightGtZero(true, false));
		System.out.println("min abs. connection weight > 0: " + mapping.getMinAbsWeightGtZero(false, true));
	}
	public static void main(String[] args) throws Exception {
		configureFactory();
		// Iterate over different number of tenants/servers/metrics
		double overalMaxAbsWeightTriangle = 0;
		double overalMaxAbsWeightMatrix = 0;
		for (int servers=1; servers<=MAX_SERVERS; ++servers) {
			for (int metrics=1; metrics<=MAX_METRICS; ++metrics) {
				for (int tenants=1; tenants<=MAX_TENANTS; ++tenants) {
					for (int testcaseCtr=0; testcaseCtr<5; ++testcaseCtr) {
						TestcaseFactory.nrTenants = tenants;
						TestcaseFactory.nrServers = servers;
						TestcaseFactory.nrMetrics = metrics;
						ConsolidationProblem problem = TestcaseFactory.produce();
						try {
							ConsolidationMappingGeneric mapping = (ConsolidationMappingGeneric)triangleMapper.transform(problem);
							analyseWeights(tenants, servers, metrics, mapping, "TRIANGLE");
							double maxAbsWeight = mapping.getMaxAbsWeight(true, true);
							overalMaxAbsWeightTriangle = Math.max(overalMaxAbsWeightTriangle, maxAbsWeight);
						} catch (Exception e) {
							// mapping impossible
						}
						try {
							ConsolidationMappingGeneric mapping = (ConsolidationMappingGeneric)matrixMapper.transform(problem);
							analyseWeights(tenants, servers, metrics, mapping, "MATRIX");
							double maxAbsWeight = mapping.getMaxAbsWeight(true, true);
							overalMaxAbsWeightMatrix = Math.max(overalMaxAbsWeightMatrix, maxAbsWeight);
						} catch (Exception e) {
							// mapping impossible
						}
					}
				}
			}
		}
		System.out.println("Triangle mapper - maximum absolute weight value: " + overalMaxAbsWeightTriangle);
		System.out.println("Matrix mapper - maximum absolute weight value: " + overalMaxAbsWeightMatrix);
	}
}
