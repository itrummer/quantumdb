package raw_material.consolidation.benchmark;

import raw_material.consolidation.dwave.mapper.MapperMatrix;
import raw_material.consolidation.dwave.mapper.MapperTriangle;
import raw_material.consolidation.testcase.TestcaseFactory;

public class BenchmarkRun {
	// Configuration
	protected static int NR_TESTCASES = 100;
	protected static int MAX_TENANTS = 10;
	protected static int MAX_SERVERS = 10;
	protected static int MAX_METRICS = 10;
	// Generate mappers
	static MapperTriangle triangleMapper = new MapperTriangle();
	static MapperMatrix matrixMapper = new MapperMatrix();
	// Configure test case factory (expect nr tenants/servers/metrics)
	protected static void configureFactory() {
		TestcaseFactory.minDoubleStep = 0.5;	// must still be possible to square while staying within precision range
		// TestcaseFactory.minDoubleStep = 0.125;
		TestcaseFactory.minConsumption = 0;
		TestcaseFactory.maxConsumption = 1;
		TestcaseFactory.minCapacity = 0;
		TestcaseFactory.maxCapacity = 1;
		TestcaseFactory.minCost = 0;
		TestcaseFactory.maxCost = 1;
	}
}
