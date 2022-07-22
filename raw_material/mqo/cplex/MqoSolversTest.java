package raw_material.mqo.cplex;

import static org.junit.Assert.*;
import mqo_chimera.mapping.ChimeraMqoMapping;
import mqo_chimera.util.AmesUtil;

import org.junit.Test;

import raw_material.mqo.dwave.MqoMapping;
import raw_material.mqo.testcase.MqoProblem;
import raw_material.mqo.testcase.MqoSolution;
import raw_material.mqo.testcase.MqoTestcaseFactory;
import raw_material.util.RandomUtil;
import raw_material.util.TestUtil;

public class MqoSolversTest {

	@Test
	public void test() throws Exception {
		AmesUtil.initAmes();
		LinearMqoSolver linearSolver = new LinearMqoSolver();
		QuadraticMqoSolver quadraticSolver = new QuadraticMqoSolver();
		// Verify solver consistency when reading general QUBOs from disc
		for (int i=0; i<10; ++i) {
			// Produce verification mapping
			MqoMapping mapping = new MqoMapping();
			// Set weights
			for (int q1=0; q1<20; ++q1) {
				for (int q2=0; q2<20; ++q2) {
					double addedWeight = RandomUtil.random.nextDouble() - 0.5;
					mapping.addWeight(q1, q2, addedWeight);
				}
			}
			// Write QUBO to file
			mapping.weightsToFile("verification", "QUBO used to compare solver consistency");
			// Solve by linear solver
			double linearResult = linearSolver.solveQuboFromDisc("verification");
			// Solve by quadratic solver
			double quadraticResult = quadraticSolver.solveQuboFromDisc("verification");
			assertEquals(linearResult, quadraticResult, 1E-10);
		}
		// Verify solver consistency when reading Chimera QUBOs from disc
		for (int i=0; i<10; ++i) {
			// Produce verification mapping
			ChimeraMqoMapping mapping = new ChimeraMqoMapping();
			// Set weights
			int nrUsedVariables = 500;
			for (int q1=0; q1<nrUsedVariables; ++q1) {
				for (int q2=0; q2<nrUsedVariables; ++q2) {
					if (q1 <= q2) {
						if (q1 == q2 || AmesUtil.amesConnected(q1, q2)) {
							double addedWeight = RandomUtil.random.nextBoolean() ? 1 : -1;
							mapping.addWeight(q1, q2, addedWeight);						
						}						
					}
				}
			}
			// Write QUBO to file
			mapping.weightsToFile("verification", "QUBO used to compare solver consistency");
			// Solve by linear solver
			double linearResult = linearSolver.solveQuboFromDisc("verification");
			// Solve by quadratic solver
			double quadraticResult = quadraticSolver.solveQuboFromDisc("verification");
			assertEquals(linearResult, quadraticResult, 1E-10);
		}
		/*
		for (int i=0; i<10; ++i) {
			int nrQueries = RandomUtil.uniformInt(2, 10);
			int nrPlansPerQuery = RandomUtil.uniformInt(1, 5);
			int nrIntermediateResults = RandomUtil.uniformInt(1, 20);
			MqoProblem problem = MqoTestcaseFactory.produce(nrQueries, nrPlansPerQuery, nrIntermediateResults);
			MqoSolution linearSolverSolution = linearSolver.solve(problem);
			assertTrue(linearSolverSolution.allQueriesExecuted());
			assertTrue(linearSolverSolution.allDependenciesSatisfied());
			MqoSolution quadraticSolverSolution = quadraticSolver.solve(problem);
			assertTrue(quadraticSolverSolution.allQueriesExecuted());
			assertTrue(quadraticSolverSolution.allDependenciesSatisfied());
			assertEquals(linearSolverSolution.getCost(), quadraticSolverSolution.getCost(), TestUtil.DOUBLE_TOLERANCE);
		}
		*/
	}

}
