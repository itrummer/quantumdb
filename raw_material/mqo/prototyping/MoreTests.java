package raw_material.mqo.prototyping;

import raw_material.mqo.cplex.LinearMqoSolver;
import raw_material.util.RandomUtil;
import mqo_chimera.AmesUtil;
import mqo_chimera.ChimeraMqoMapping;

public class MoreTests {
	public static void main(String[] args) throws Exception {
		AmesUtil.initAmes();
		LinearMqoSolver linearSolver = new LinearMqoSolver();
		// Produce verification mapping
		ChimeraMqoMapping mapping = new ChimeraMqoMapping();
		// Set weights
		int nrUsedVariables = 1000;
		for (int q=0; q<nrUsedVariables; ++q) {
			if (AmesUtil.amesQubits.contains(q)) {
				//double addedWeight = RandomUtil.random.nextBoolean() ? 1 : -1;
				double addedWeight = RandomUtil.random.nextDouble()/2.0;
				//double addedWeight = 0;
				mapping.addWeight(q, q, addedWeight);
			}
		}
		for (int q1=0; q1<nrUsedVariables; ++q1) {
			for (int q2=0; q2<nrUsedVariables; ++q2) {
				if (q1 < q2) {
					if (AmesUtil.amesConnected(q1, q2)) {
						double q1Weight = mapping.getWeight(q1);
						double q2Weight = mapping.getWeight(q2);
						double addedWeight = -RandomUtil.random.nextDouble();
						//double addedWeight = RandomUtil.random.nextBoolean() ? 1 : -1;
						//double addedWeight = - (q1Weight + q2Weight)/2.0;
						mapping.addWeight(q1, q2, addedWeight);						
					}						
				}
			}
		}
		// Write QUBO to file
		mapping.weightsToFile("verification", "QUBO used to compare solver consistency");
		// Solve by linear solver
		System.out.println(linearSolver.solveQuboFromDisc("verification"));
	}
}
