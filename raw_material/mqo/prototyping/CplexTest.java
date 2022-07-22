package raw_material.mqo.prototyping;

import raw_material.mqo.dwave.MqoMapping;
import raw_material.mqo.testcase.Performance;
import raw_material.util.RandomUtil;
import ilog.concert.IloException;
import ilog.concert.IloIntVar;
import ilog.concert.IloLinearIntExpr;
import ilog.concert.IloLinearNumExpr;
import ilog.cplex.IloCplex;
import ilog.cplex.IloCplex.Status;

public class CplexTest {
	/**
	 * Provides access to CPLEX solver.
	 */
	static public IloCplex cplex;
	/**
	 * The constructor initializes the CPLEX object; each invocation of the solve method clears the model.
	 * 
	 * @throws IloException
	 */
	public CplexTest() throws IloException {		
		cplex = new IloCplex();
	}
	public void go() throws Exception {
		// clear Cplex model
		cplex.clearModel();
		// Start timer
		long startMillis = System.currentTimeMillis();
		// create Cplex variables
		// Each plan variable is 1 if the corresponding plan is used
		int nrVars = 500;
		int nrShares = 3000;
		IloIntVar[] planVars = new IloIntVar[nrVars];
		//IloIntVar[] negPlanVars = new IloIntVar[nrVars];
		IloIntVar[] shareVars = new IloIntVar[nrShares];
		MqoMapping mapping = new MqoMapping();
		for (int i=0; i<nrVars; ++i) {
			planVars[i] = cplex.boolVar();
			mapping.addWeight(i, i, 1);
		}
		for (int i=0; i<nrShares; ++i) {
			shareVars[i] = cplex.boolVar();
		}
		
		for (int i=0; i<nrShares; ++i) {
			int var1 = RandomUtil.uniformInt(0, nrVars-1);
			int var2 = var1;
			while (var2 == var1) {
				var2 = RandomUtil.uniformInt(0, nrVars-1);
			}
			boolean var1Neg;
			if (RandomUtil.random.nextBoolean()) {
				// if share var = 1 then plan var = 1
				var1Neg = false;
				cplex.addGe(planVars[var1], shareVars[i]);
			} else {
				var1Neg = true;
				IloLinearIntExpr oneMinusPlan = cplex.linearIntExpr();
				oneMinusPlan.setConstant(1);
				oneMinusPlan.addTerm(-1, planVars[var1]);
				cplex.addGe(oneMinusPlan, shareVars[i]);
			}
			boolean var2Neg;
			if (RandomUtil.random.nextBoolean()) {
				// if share var = 1 then plan var = 1
				var2Neg = false;
				cplex.addGe(planVars[var2], shareVars[i]);
			} else {
				var2Neg = true;
				IloLinearIntExpr oneMinusPlan = cplex.linearIntExpr();
				oneMinusPlan.setConstant(1);
				oneMinusPlan.addTerm(-1, planVars[var2]);
				cplex.addGe(oneMinusPlan, shareVars[i]);
			}
			mapping.addWeight(var1, var2, 1);
		}
		IloLinearNumExpr goalFormula = cplex.linearNumExpr();
		for (int i=0; i<nrVars; ++i) {
			double planCost = RandomUtil.uniformInt(1, 4) * 0.25 - 0.5;
			goalFormula.addTerm(planVars[i], planCost);
		}
		for (int i=0; i<nrShares; ++i) {
			double shareSavings = RandomUtil.uniformInt(1, 2) * 0.25;
			goalFormula.addTerm(shareVars[i], -shareSavings);
		}
		cplex.addMinimize(goalFormula);
		// solve
		cplex.solve();
		// verify that optimal solution was found
		Status status = cplex.getStatus();
		assert(status == IloCplex.Status.Optimal);
		// extract objective value
		double objValue = cplex.getObjValue();
		// Measure elapsed time
		long elapsedMillis = System.currentTimeMillis() - startMillis;
		// Return encapsulated solution
		Performance performanceStats = new Performance(elapsedMillis);
		for (int i=0; i<nrVars; ++i) {
			System.out.println("Plan var " + i + ": " + cplex.getValue(planVars[i]));
		}
		for (int i=0; i<nrShares; ++i) {
			System.out.print("Share var " + i + ": " + cplex.getValue(shareVars[i]));
		}
		System.out.println();
		System.out.println("Obj val: " + objValue);
		mapping.weightsToFile("testMappingT0", "testing");
	}
}
