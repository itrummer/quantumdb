package raw_material.consolidation.cplex;

import static org.junit.Assert.*;

import org.junit.Test;

import raw_material.consolidation.dwave.mapper.MapperMatrix;
import raw_material.consolidation.dwave.mapper.MapperTriangle;
import raw_material.consolidation.testcase.ConsolidationProblem;
import raw_material.consolidation.testcase.ConsolidationSolution;
import raw_material.consolidation.testcase.TestcaseFactory;
import util.TestUtil;
import util.RandomUtil;

public class QuadraticConsolidationSolverTest {

	@Test
	public void test() throws Exception {
		
		// Check objective value for given tenant assignments
		{
			ConsolidationProblem problem = new ConsolidationProblem(3, 4, 1, 0.5);
			// Tenant consumption: 	1 		2.5 	0.5
			// Server capacities: 	2.5 	0.5 	1 		2.5
			// Server cost			0		0		2.5		0.5
			problem.setConsumption(0, 0, 1);
			problem.setConsumption(1, 0, 2.5);
			problem.setConsumption(2, 0, 0.5);
			problem.setCapacity(0, 0, 2.5);
			problem.setCapacity(1, 0, 0.5);
			problem.setCapacity(2, 0, 1);
			problem.setCapacity(3, 0, 2.5);
			problem.setServerCost(0, 0);
			problem.setServerCost(1, 0);
			problem.setServerCost(2, 2.5);
			problem.setServerCost(3, 0.5);
			MapperTriangle mapper 					= new MapperTriangle();
			QuadraticConsolidationSolver quadraticSolver	= new QuadraticConsolidationSolver(mapper);
			double assignmentScaling	= 3.125;
			double capacityScaling		= 10.125;
			double maxScaling			= 2.625;
			{
				int[] assignmentConstraints = new int[] {3, 0, 3};
				quadraticSolver.solveWithConstraints(problem, assignmentConstraints);
				double expectedEnergy 	= -3*assignmentScaling + 0.5;
				@SuppressWarnings("static-access")
				double energy 			= quadraticSolver.cplex.getObjValue();
				assertEquals(expectedEnergy, energy, TestUtil.DOUBLE_TOLERANCE);
			}
			{
				int[] assignmentConstraints = new int[] {1, 0, 3};
				quadraticSolver.solveWithConstraints(problem, assignmentConstraints);
				double expectedEnergy 	= -3*assignmentScaling + 0.5*0.5*capacityScaling + 0.5;
				@SuppressWarnings("static-access")
				double energy 			= quadraticSolver.cplex.getObjValue();
				assertEquals(expectedEnergy, energy, TestUtil.DOUBLE_TOLERANCE);
			}
		}
		// Check objective value for given tenant assignments
		{
			ConsolidationProblem problem = new ConsolidationProblem(3, 4, 1, 0.5);
			// Tenant consumption: 	1 		1		2
			// Server capacities: 	0 		2 		1 		1.5
			// Server cost			0.5		2		2.5		0.5
			problem.setConsumption(0, 0, 1);
			problem.setConsumption(1, 0, 1);
			problem.setConsumption(2, 0, 2);
			problem.setCapacity(0, 0, 0);
			problem.setCapacity(1, 0, 2);
			problem.setCapacity(2, 0, 1);
			problem.setCapacity(3, 0, 1.5);
			problem.setServerCost(0, 0.5);
			problem.setServerCost(1, 2);
			problem.setServerCost(2, 2.5);
			problem.setServerCost(3, 0.5);
			MapperTriangle mapper 					= new MapperTriangle();
			QuadraticConsolidationSolver quadraticSolver	= new QuadraticConsolidationSolver(mapper);
			ConsolidationSolution solution			= quadraticSolver.solve(problem);
			assertTrue(solution.isFeasible);
			assertEquals(5, solution.minTotalCost, TestUtil.DOUBLE_TOLERANCE);
		}
		
		// Initialize factory
		TestcaseFactory.minDoubleStep	= 0.5;
		TestcaseFactory.minConsumption	= 0;
		TestcaseFactory.maxConsumption	= 1;
		TestcaseFactory.minCapacity 	= 0;
		TestcaseFactory.maxCapacity 	= 1;
		TestcaseFactory.minCost			= 0;
		TestcaseFactory.maxCost			= 5;
		// Validate quadratic solver by comparing output with the one of linear solver.
		MapperTriangle triangleMapper 					= new MapperTriangle();
		MapperMatrix matrixMapper 						= new MapperMatrix();
		LinearConsolidationSolver linearSolver 				= new LinearConsolidationSolver();
		QuadraticConsolidationSolver quadraticTriangleSolver	= new QuadraticConsolidationSolver(triangleMapper);
		QuadraticConsolidationSolver quadraticMatrixSolver	= new QuadraticConsolidationSolver(matrixMapper);
		for (int i=0; i<0; i++) {
		// for (int i=0; i<10; i++) {
		// for (int i=0; i<100; i++) {
		// for (int i=0; i<1000; i++) {
			// Generate random test case that can be mapped onto D-Wave matric
			ConsolidationProblem testcase = null;
			boolean canBeMapped;
			do {
				// Initialize test case factory
				TestcaseFactory.nrTenants = RandomUtil.uniformInt(1, 4);			
				TestcaseFactory.nrServers = RandomUtil.uniformInt(1, 4);
				TestcaseFactory.nrMetrics = RandomUtil.uniformInt(1, 4);
				testcase = TestcaseFactory.produce();
				canBeMapped = true;
				try {
					triangleMapper.transform(testcase);
					matrixMapper.transform(testcase);
				} catch (Exception e) {
					System.out.println(e.getMessage());
					canBeMapped = false;
				};
			} while (!canBeMapped);
			// solve test case by two cplex solvers
			ConsolidationSolution linearSolution			= linearSolver.solve(testcase);
			ConsolidationSolution quadraticTriangleSolution	= quadraticTriangleSolver.solve(testcase);
			ConsolidationSolution quadraticMatrixSolution	= quadraticMatrixSolver.solve(testcase);
			System.out.println();
			System.out.println("******************************************************");
			System.out.println("Consolidation problem: ");
			testcase.toConsole();
			System.out.println("Solution found by linear solver: ");
			linearSolution.toConsole();
			System.out.println("Solution found by quadratic triangle solver: ");
			quadraticTriangleSolution.toConsole();
			System.out.println("Solution found by quadratic matrix solver: ");
			quadraticMatrixSolution.toConsole();
			assertTrue(linearSolution.isEquivalent(quadraticTriangleSolution));
			assertTrue(quadraticTriangleSolution.isEquivalent(linearSolution));
			assertTrue(linearSolution.isEquivalent(quadraticMatrixSolution));
			assertTrue(quadraticMatrixSolution.isEquivalent(linearSolution));
		}
	}

}
