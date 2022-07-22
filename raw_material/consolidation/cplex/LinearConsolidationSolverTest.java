package raw_material.consolidation.cplex;

import static org.junit.Assert.*;
import ilog.concert.IloException;

import org.junit.Test;

import raw_material.consolidation.testcase.ConsolidationProblem;
import raw_material.consolidation.testcase.ConsolidationSolution;
import raw_material.util.TestUtil;

public class LinearConsolidationSolverTest {

	@Test
	public void test() throws IloException {
		LinearConsolidationSolver solver = new LinearConsolidationSolver();
		// Simple test case: can map single tenant to single server
		{
			// create problem
			ConsolidationProblem problem = new ConsolidationProblem(1, 1, 1, 0.5);
			problem.setConsumption(0, 0, 0.5);
			problem.setCapacity(0, 0, 1);
			problem.setServerCost(0, 2);
			// solve
			ConsolidationSolution solution = solver.solve(problem);
			// check solution
			assertTrue(solution.isFeasible);
			assertEquals(0, solution.assignedServer[0]);
			assertEquals(2, solution.minTotalCost, TestUtil.DOUBLE_TOLERANCE);
		}
		// Simple test case: cannot map single tenant to single server
		{
			// create problem
			ConsolidationProblem problem = new ConsolidationProblem(1, 1, 1, 0.5);
			problem.setConsumption(0, 0, 1.5);
			problem.setCapacity(0, 0, 1);
			problem.setServerCost(0, 2);
			// solve
			ConsolidationSolution solution = solver.solve(problem);
			// check solution
			assertFalse(solution.isFeasible);
		}
		// Simple test case: can map two tenants to single server
		{
			// create problem
			ConsolidationProblem problem = new ConsolidationProblem(2, 1, 1, 0.5);
			problem.setConsumption(0, 0, 0.5);
			problem.setConsumption(1, 0, 1.5);
			problem.setCapacity(0, 0, 2.5);
			problem.setServerCost(0, 2);
			// solve
			ConsolidationSolution solution = solver.solve(problem);
			// check solution
			assertTrue(solution.isFeasible);
			assertEquals(0, solution.assignedServer[0]);
			assertEquals(0, solution.assignedServer[1]);
			assertEquals(2, solution.minTotalCost, TestUtil.DOUBLE_TOLERANCE);
		}
		// Simple test case: cannot map two tenants to single server
		{
			// create problem
			ConsolidationProblem problem = new ConsolidationProblem(2, 1, 1, 0.5);
			problem.setConsumption(0, 0, 0.5);
			problem.setConsumption(1, 0, 1.5);
			problem.setCapacity(0, 0, 1.75);
			problem.setServerCost(0, 2);
			// solve
			ConsolidationSolution solution = solver.solve(problem);
			// check solution
			assertFalse(solution.isFeasible);
		}
		// Tenants can be mapped when partitioning them over the right two servers
		{
			// create problem
			ConsolidationProblem problem = new ConsolidationProblem(3, 3, 1, 0.5);
			problem.setConsumption(0, 0, 0.5);
			problem.setConsumption(1, 0, 1.5);
			problem.setConsumption(2, 0, 1);
			problem.setCapacity(0, 0, 0.5);
			problem.setCapacity(1, 0, 1.6);
			problem.setCapacity(2, 0, 1.6);
			problem.setServerCost(0, 1);
			problem.setServerCost(1, 3);
			problem.setServerCost(2, 5);
			// solve
			ConsolidationSolution solution = solver.solve(problem);
			// check solution
			assertTrue(solution.isFeasible);
			assertEquals(8, solution.minTotalCost, TestUtil.DOUBLE_TOLERANCE);
		}
		// Tenants cannot be mapped when properly considering all resource metrics
		{
			// create problem
			ConsolidationProblem problem = new ConsolidationProblem(3, 3, 2, 0.5);
			// tenant consumption
			problem.setConsumption(0, 0, 0.5);
			problem.setConsumption(1, 0, 1.5);
			problem.setConsumption(2, 0, 1);
			problem.setConsumption(0, 1, 2.5);
			problem.setConsumption(1, 1, 1.5);
			problem.setConsumption(2, 1, 1);
			// server capacities
			problem.setCapacity(0, 0, 0.25);
			problem.setCapacity(1, 0, 1.6);
			problem.setCapacity(2, 0, 1.6);
			problem.setCapacity(0, 1, 6);
			problem.setCapacity(1, 1, 1);
			problem.setCapacity(2, 1, 1.6);
			// server costs
			problem.setServerCost(0, 1);
			problem.setServerCost(1, 3);
			problem.setServerCost(2, 5);
			// solve
			ConsolidationSolution solution = solver.solve(problem);
			// check solution
			assertFalse(solution.isFeasible);
		}
		// Best possibility is to map all tenants to the same server
		// Tenants cannot be mapped when properly considering all resource metrics
		{
			// create problem
			ConsolidationProblem problem = new ConsolidationProblem(3, 3, 2, 0.5);
			// tenant consumption
			problem.setConsumption(0, 0, 0.5);
			problem.setConsumption(1, 0, 1.5);
			problem.setConsumption(2, 0, 1);
			problem.setConsumption(0, 1, 2.5);
			problem.setConsumption(1, 1, 1.5);
			problem.setConsumption(2, 1, 1);
			// server capacities
			problem.setCapacity(0, 0, 3.5);
			problem.setCapacity(1, 0, 1.6);
			problem.setCapacity(2, 0, 1.6);
			problem.setCapacity(0, 1, 6);
			problem.setCapacity(1, 1, 3);
			problem.setCapacity(2, 1, 2);
			// server costs
			problem.setServerCost(0, 1);
			problem.setServerCost(1, 3);
			problem.setServerCost(2, 5);
			// solve
			ConsolidationSolution solution = solver.solve(problem);
			// check solution
			assertTrue(solution.isFeasible);
			assertEquals(1, solution.minTotalCost, TestUtil.DOUBLE_TOLERANCE);
		}
	}

}
