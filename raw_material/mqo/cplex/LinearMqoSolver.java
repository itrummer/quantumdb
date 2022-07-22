package raw_material.mqo.cplex;

import java.io.BufferedReader;
import java.io.FileReader;

import raw_material.mqo.testcase.MqoProblem;
import raw_material.mqo.testcase.MqoSolution;
import raw_material.mqo.testcase.Performance;
import ilog.concert.IloException;
import ilog.concert.IloIntVar;
import ilog.concert.IloLinearIntExpr;
import ilog.concert.IloLinearNumExpr;
import ilog.cplex.IloCplex;
import ilog.cplex.IloCplex.Status;

public class LinearMqoSolver {
	/**
	 * Provides access to CPLEX solver.
	 */
	static public IloCplex cplex;
	/**
	 * The constructor initializes the CPLEX object; each invocation of the solve method clears the model.
	 * 
	 * @throws IloException
	 */
	public LinearMqoSolver() throws IloException {		
		cplex = new IloCplex();
	}
	/**
	 * Solves the given MQO problem instance and returns the optimal solution.
	 * 
	 * @param problem		a multiple query optimization problem instance
	 * @return				the solution to the given problem instance
	 * @throws Exception
	 */
	public MqoSolution solve(MqoProblem problem) throws Exception {
		// Extract problem dimensions
		int nrQueries = problem.nrQueries;
		int nrPlansPerQuery = problem.nrPlansPerQuery;
		int nrIntermediateResults = problem.nrIntermediateResults;
		// clear Cplex model
		cplex.clearModel();
		// Start timer
		long startMillis = System.currentTimeMillis();
		// create Cplex variables
		// Each plan variable is 1 if the corresponding plan is used
		IloIntVar[][] planVars = new IloIntVar[nrQueries][nrPlansPerQuery];
		for (int query=0; query<nrQueries; ++query) {
			for (int plan=0; plan<nrPlansPerQuery; ++plan) {
				planVars[query][plan] = cplex.boolVar();
			}
		}
		// Each intermediate result var is 1 if the corresponding result is produced
		IloIntVar[] resultVars = new IloIntVar[nrIntermediateResults];
		for (int result=0; result<nrIntermediateResults; ++result) {
			resultVars[result] = cplex.boolVar();
		}
		// Constraint: each query must be executed by one plan
		for (int query=0; query<nrQueries; ++query) {
			IloLinearNumExpr nrExecutedPlans = cplex.linearNumExpr();
			for (int plan=0; plan<nrPlansPerQuery; ++plan) {
				nrExecutedPlans.addTerm(1, planVars[query][plan]);
			}
			cplex.addEq(nrExecutedPlans, 1);
		}
		// Constraint: if one plan is executed that depends on intermediate results then
		// all those results must be generated
		for (int query=0; query<nrQueries; ++query) {
			for (int plan=0; plan<nrPlansPerQuery; ++plan) {
				for (int result=0; result<nrIntermediateResults; ++result) {
					if (problem.getDependency(query, plan, result)) {
						// If plan variable is one then intermediate result variable must be one, too
						cplex.addGe(resultVars[result], planVars[query][plan]);
					}
				}
			}
		}
		// Objective formula is the sum of all executed plan cost and generated result costs
		IloLinearNumExpr totalCost = cplex.linearNumExpr();
		// Add cost for executing plans
		for (int query=0; query<nrQueries; ++query) {
			for (int plan=0; plan<nrPlansPerQuery; ++plan) {
				double planCost = problem.getPlanCost(query, plan);
				totalCost.addTerm(planCost, planVars[query][plan]);
			}
		}
		// Add cost for generating intermediate results
		for (int result=0; result<nrIntermediateResults; ++result) {
			double resultCost = problem.getResultCost(result);
			totalCost.addTerm(resultCost, resultVars[result]);
		}
		cplex.addMinimize(totalCost);
		// solve
		cplex.solve();
		// verify that optimal solution was found
		Status status = cplex.getStatus();
		assert(status == IloCplex.Status.Optimal);
		// extract objective value
		double objValue = cplex.getObjValue();
		// check which plans are executed in the optimal solution
		boolean[][] executedPlans = new boolean[nrQueries][nrPlansPerQuery];
		for (int query=0; query<nrQueries; ++query) {
			for (int plan=0; plan<nrPlansPerQuery; ++plan) {
				IloIntVar planVar = planVars[query][plan];
				executedPlans[query][plan] = cplex.getValue(planVar) > 0.5;
			}
		}
		// check which intermediate results are generated in the optimal solution
		boolean[] generatedResults = new boolean[nrIntermediateResults];
		for (int result=0; result<nrIntermediateResults; ++result) {
			IloIntVar resultVar = resultVars[result];
			generatedResults[result] = cplex.getValue(resultVar) > 0.5;
		}
		// Measure elapsed time
		long elapsedMillis = System.currentTimeMillis() - startMillis;
		// Return encapsulated solution
		Performance performanceStats = new Performance(elapsedMillis);
		return new MqoSolution(problem, objValue, executedPlans, generatedResults, performanceStats);
	}
	/**
	 * Add energy represented by a row of a QUBO file.
	 * 
	 * @param energyFormula		this formula has to be minimized
	 * @param qubitVars			variables representing qubits
	 * @param qubit1			index of first qubit
	 * @param qubit2			index of second qubit (might be the same as first)
	 * @param weight			weight on a qubit or between two qubits
	 * @throws IloException
	 */
	void addEnergyTerm(IloLinearNumExpr energyFormula, IloIntVar[] qubitVars, 
			int qubit1, int qubit2, double weight) throws IloException {
		// Decide whether this represents a weight on a single qubit or between two qubits
		if (qubit1 == qubit2) {
			// Weight on single qubit
			IloIntVar var = qubitVars[qubit1];
			energyFormula.addTerm(weight, var);
		} else {
			// Weight between two different qubits
			IloIntVar coupling = cplex.boolVar();
			// Add constraint making sure that this weight is only counted if the two qubits are set to 1
			cplex.addGe(qubitVars[qubit1], coupling);
			cplex.addGe(qubitVars[qubit2], coupling);
			IloLinearIntExpr qubitSumMinusOne = cplex.linearIntExpr();
			qubitSumMinusOne.setConstant(-1);
			qubitSumMinusOne.addTerm(1, qubitVars[qubit1]);
			qubitSumMinusOne.addTerm(1, qubitVars[qubit2]);
			cplex.addGe(coupling, qubitSumMinusOne);
			// Add weighted term to goal formula
			energyFormula.addTerm(weight, coupling);
		}
	}
	/**
	 * Reads a QUBO problem description from a file, solves that problem, and returns the
	 * optimal objective value.
	 * 
	 * @param path			path to file containing QUBO
	 * @return				minimum energy level
	 * @throws Exception
	 */
	public double solveQuboFromDisc(String path) throws Exception {
		System.out.println("Initializing CPLEX");
		// clear CPLEX model
		cplex.clearModel();
		// Create variables representing qubits
		int nrQubits = 1152;
		IloIntVar[] qubitVars = cplex.boolVarArray(nrQubits);
		cplex.add(qubitVars);
		for (int qubit=0; qubit<nrQubits; ++qubit) {
			qubitVars[qubit] = cplex.boolVar();
			cplex.add(qubitVars[qubit]);
		}
		System.out.println("Read QUBO from disc");
		// Generate list containing terms of energy formula
		IloLinearNumExpr energyFormula = cplex.linearNumExpr();
		// Open QUBO file
		FileReader fileReader = new FileReader(path);
		BufferedReader bufferedReader = new BufferedReader(fileReader);
		// Skip header line
		bufferedReader.readLine();
		String line;
		while ((line = bufferedReader.readLine()) != null) {
			String[] splits = line.split(",");
			int qubit1 = Integer.parseInt(splits[0]);
			int qubit2 = Integer.parseInt(splits[1]);
			double weight = Double.parseDouble(splits[2]);
			addEnergyTerm(energyFormula, qubitVars, qubit1, qubit2, weight);
		}
		// Close QUBO file
		bufferedReader.close();
		// Prepare QUBO solving
		cplex.addMinimize(energyFormula);
		// solve
		cplex.solve();
		// verify that optimal solution was found
		Status status = cplex.getStatus();
		assert(status == IloCplex.Status.Optimal);
		
		for (int qubit=0; qubit<200; ++qubit) {
			System.out.println("Qubit " + qubit + " value:\t" + cplex.getValue(qubitVars[qubit]));
			//System.out.println(cplex.getBasisStatus(qubitVars[qubit]));
		}
		// return objective value
		return cplex.getObjValue();
	}
}
