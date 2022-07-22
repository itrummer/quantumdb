package raw_material.consolidation.testcase;

import static org.junit.Assert.*;

import org.junit.Test;

public class ConsolidationSolutionTest {

	@Test
	public void test() {
		{
			// if the problem is not feasible then the total server activation cost is not compared
			ConsolidationSolution solution1 = new ConsolidationSolution(false, 0, null);
			ConsolidationSolution solution2 = new ConsolidationSolution(false, 1, null);
			assertTrue(solution1.isEquivalent(solution2));
		}
		{
			// if the problem is not feasible then the total server activation cost is not compared
			ConsolidationSolution solution1 = new ConsolidationSolution(true, 0, new int[] {});
			ConsolidationSolution solution2 = new ConsolidationSolution(true, 1, new int[] {});
			assertFalse(solution1.isEquivalent(solution2));
		}
		{
			// if the problem is not feasible then the total server activation cost is not compared
			ConsolidationSolution solution1 = new ConsolidationSolution(true, 3, new int[] {});
			ConsolidationSolution solution2 = new ConsolidationSolution(true, 3, new int[] {});
			assertTrue(solution1.isEquivalent(solution2));
		}
	}

}
