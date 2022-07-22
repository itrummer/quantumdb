package raw_material.consolidation.cplex;

import raw_material.consolidation.testcase.ConsolidationProblem;
import raw_material.consolidation.testcase.ConsolidationSolution;
import ilog.concert.IloException;
import ilog.cplex.IloCplex;

// Solves a consolidation problem.
public abstract class ConsolidationSolver {
	static public IloCplex cplex;
	// Constructor initializes cplex object. The "solve" method must clear the model.
	public ConsolidationSolver() throws IloException {		
		cplex = new IloCplex();
	}
	public abstract ConsolidationSolution solve(ConsolidationProblem problem) throws Exception;
}
