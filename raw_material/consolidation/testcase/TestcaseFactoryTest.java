package raw_material.consolidation.testcase;

import static org.junit.Assert.*;

import org.junit.Test;

public class TestcaseFactoryTest {

	@Test
	public void test() {
		TestcaseFactory.nrTenants = 2;
		TestcaseFactory.nrServers = 3;
		TestcaseFactory.nrMetrics = 4;
		TestcaseFactory.minDoubleStep = 0.5;
		TestcaseFactory.minCapacity = 0;
		TestcaseFactory.maxCapacity = 4;
		TestcaseFactory.minConsumption = 1;
		TestcaseFactory.maxConsumption = 2;
		TestcaseFactory.minCost = 0;
		TestcaseFactory.maxCost = 5;
		// Generate 100 test cases and verify that generated problems have
		// the correct number of tenants, servers, and metrics. Also make
		// sure that the consumption, capacity, and cost values are within
		// the specified range.
		for (int i=0; i<100; ++i) {
			ConsolidationProblem problem = TestcaseFactory.produce();
			// verify number of tenants, servers, and metrics
			assertEquals(2, problem.nrTenants);
			assertEquals(3, problem.nrServers);
			assertEquals(4, problem.nrMetrics);
			// verify consumption, capacity, and cost ranges
			for (int tenant=0; tenant<2; ++tenant) {
				for (int metric=0; metric<4; ++metric) {
					double consumption = problem.getConsumption(tenant, metric);
					assertTrue(consumption >= 1 * 0.5);
					assertTrue(consumption <= 2 * 0.5);
				}
			}
			for (int server=0; server<2; ++server) {
				for (int metric=0; metric<4; ++metric) {
					double capacity = problem.getCapacity(server, metric);
					assertTrue(capacity >= 0);
					assertTrue(capacity <= 4 * 0.5);
				}
			}
			for (int server=0; server<2; ++server) {
				double cost = problem.getCost(server);
				assertTrue(cost <= 5 * 0.5);
				assertTrue(cost >= 0.0);
			}
			assertTrue(problem.maxServerCost >= 0);
			assertTrue(problem.maxServerCost <= 5 * 0.5);
			
		}
	}

}
