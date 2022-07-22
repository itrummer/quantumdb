package raw_material.consolidation.testcase;

import static org.junit.Assert.*;

import org.junit.Test;

import raw_material.util.MapperUtil;

public class ProblemGenericTest {

	@Test
	public void test() {
		ConsolidationProblem problem = new ConsolidationProblem(2, 2, 2, 0.25);
		// tenant consumptions
		problem.setConsumption(0, 0, 0.75);
		assertEquals(0.75, problem.getConsumption(0, 0), MapperUtil.DOUBLE_TOLERANCE);
		problem.setConsumption(0, 1, 0.5);
		assertEquals(0.5, problem.getConsumption(0, 1), MapperUtil.DOUBLE_TOLERANCE);
		// server capacities
		problem.setCapacity(1, 0, 1.25);
		assertEquals(1.25, problem.getCapacity(1, 0), MapperUtil.DOUBLE_TOLERANCE);
		problem.setCapacity(1, 1, 1.5);
		assertEquals(1.5, problem.getCapacity(1, 1), MapperUtil.DOUBLE_TOLERANCE);
		// server operational cost
		problem.setServerCost(0, 2);
		assertEquals(2, problem.getCost(0), MapperUtil.DOUBLE_TOLERANCE);
		assertEquals(2, problem.maxServerCost, MapperUtil.DOUBLE_TOLERANCE);
		problem.setServerCost(1, 3.25);
		assertEquals(3.25, problem.getCost(1), MapperUtil.DOUBLE_TOLERANCE);
		assertEquals(3.25, problem.maxServerCost, MapperUtil.DOUBLE_TOLERANCE);
	}
}
