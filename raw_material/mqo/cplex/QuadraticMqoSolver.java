package raw_material.mqo.cplex;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.LinkedList;
import java.util.List;

import raw_material.mqo.dwave.MqoMapper;
import raw_material.mqo.dwave.MqoMapping;
import raw_material.mqo.testcase.MqoProblem;
import raw_material.mqo.testcase.MqoSolution;
import raw_material.mqo.testcase.Performance;
import ilog.concert.IloException;
import ilog.concert.IloIntVar;
import ilog.concert.IloNumExpr;
import ilog.cplex.IloCplex;
import ilog.cplex.IloCplex.Status;

public class QuadraticMqoSolver {
	/**
	 * Provides access to CPLEX solver.
	 */
	static public IloCplex cplex;
	/**
	 * Constructor initializes cplex object. The model must be cleared before each optimization run.
	 * 
	 * @throws IloException
	 */
	public QuadraticMqoSolver() throws IloException {		
		cplex = new IloCplex();
	}
	
	public MqoSolution solve(MqoProblem problem) throws Exception {
		// Extract problem counters
		int nrQueries = problem.nrQueries;
		int nrPlansPerQuery = problem.nrPlansPerQuery;
		int nrIntermediateResults = problem.nrIntermediateResults;
		// clear CPLEX model
		cplex.clearModel();
		// Start timer
		long startMillis = System.currentTimeMillis();
		// Create variables representing qubits
		int nrQubits = 1152;
		IloIntVar[] qubitVars = new IloIntVar[nrQubits];
		for (int qubit=0; qubit<nrQubits; ++qubit) {
			qubitVars[qubit] = cplex.boolVar();
		}
		// Use mapper to map input problem into QUBO formulation
		MqoMapping mapping = MqoMapper.map(problem);
		// Generate list containing terms of energy formula
		List<IloNumExpr> energyTerms = new LinkedList<IloNumExpr>();
		for (int qubit1=0; qubit1<nrQubits; ++qubit1) {
			// Add weights on single qubits
			{
				double weight = mapping.getWeight(qubit1);
				if (weight != 0) {
					IloNumExpr product = cplex.prod(weight, qubitVars[qubit1]);
					energyTerms.add(product);					
				}
			}
			// Add weights between different qubits
			for (int qubit2=qubit1+1; qubit2<nrQubits; ++qubit2) {
				double weight = mapping.getConnectionWeight(qubit1, qubit2);
				if (weight != 0) {
					IloNumExpr product = cplex.prod(weight, qubitVars[qubit1], qubitVars[qubit2]);
					energyTerms.add(product);					
				}
			}
		}
		// Sum up all added energy terms
		IloNumExpr[] energyTermsArray = energyTerms.toArray(new IloNumExpr[energyTerms.size()]);
		IloNumExpr energyLevel = cplex.sum(energyTermsArray);
		cplex.addMinimize(energyLevel);
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
				int qubit = mapping.planVars[query][plan].qubit;
				IloIntVar planVar = qubitVars[qubit];
				executedPlans[query][plan] = cplex.getValue(planVar) > 0.5;
			}
		}
		// check which intermediate results are generated in the optimal solution
		boolean[] generatedResults = new boolean[nrIntermediateResults];
		for (int result=0; result<nrIntermediateResults; ++result) {
			int qubit = mapping.resultVars[result].qubit;
			IloIntVar resultVar = qubitVars[qubit];
			generatedResults[result] = cplex.getValue(resultVar) > 0.5;
		}
		// Measure elapsed milliseconds
		long elapsedMillis = System.currentTimeMillis() - startMillis;
		// Return encapsulated solution
		Performance performanceStats = new Performance(elapsedMillis);
		return new MqoSolution(problem, objValue, executedPlans, generatedResults, performanceStats);
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
		// clear CPLEX model
		cplex.clearModel();
		// Create variables representing qubits
		int nrQubits = 1152;
		IloIntVar[] qubitVars = new IloIntVar[nrQubits];
		for (int qubit=0; qubit<nrQubits; ++qubit) {
			qubitVars[qubit] = cplex.boolVar();
		}
		// Generate list containing terms of energy formula
		List<IloNumExpr> energyTerms = new LinkedList<IloNumExpr>();
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
			IloNumExpr product = cplex.prod(weight, qubitVars[qubit1], qubitVars[qubit2]);
			energyTerms.add(product);
		}
		// Close QUBO file
		bufferedReader.close();
		// Prepare QUBO solving
		IloNumExpr[] energyTermsArray = energyTerms.toArray(new IloNumExpr[energyTerms.size()]);
		IloNumExpr energyLevel = cplex.sum(energyTermsArray);
		cplex.addMinimize(energyLevel);
		// solve
		cplex.solve();
		// verify that optimal solution was found
		Status status = cplex.getStatus();
		assert(status == IloCplex.Status.Optimal);
		// return objective value
		return cplex.getObjValue();
	}
}
