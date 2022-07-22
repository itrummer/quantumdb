package raw_material.mqo.benchmark;

import raw_material.mqo.cplex.QuadraticMqoSolver;

public class Debugging {
	public static void main(String[] args) throws Exception {
		QuadraticMqoSolver quadraticSolver = new QuadraticMqoSolver();
		System.out.println(quadraticSolver.solveQuboFromDisc("testQUBO"));
	}
}
