package raw_material.consolidation.cplex;

import raw_material.consolidation.testcase.ConsolidationProblem;
import raw_material.consolidation.testcase.ConsolidationSolution;
import ilog.concert.IloException;
import ilog.concert.IloIntVar;
import ilog.concert.IloLinearNumExpr;
import ilog.cplex.IloCplex;
import ilog.cplex.IloCplex.Status;

public class LinearConsolidationSolver extends ConsolidationSolver {
	public LinearConsolidationSolver() throws IloException {
		super();
	}
	@Override
	public ConsolidationSolution solve(ConsolidationProblem uncastedProblem)
			throws IloException {
		assert(uncastedProblem instanceof ConsolidationProblem);
		ConsolidationProblem problem = (ConsolidationProblem)uncastedProblem;
		int nrTenants = problem.nrTenants;
		int nrServers = problem.nrServers;
		int nrMetrics = problem.nrMetrics;
		// clear Cplex model
		cplex.clearModel();
		// create Cplex variables
		IloIntVar[][] tenantVars = new IloIntVar[nrTenants][nrServers];	// index 1: tenant; index 2: server
		for (int tenant=0; tenant<nrTenants; ++tenant) {
			for (int server=0; server<nrServers; ++server) {
				tenantVars[tenant][server] = cplex.boolVar(); 
			}
		}
		IloIntVar[] serverVars = cplex.boolVarArray(nrServers);
		// create tenant assignment constraints
		for (int tenant=0; tenant<nrTenants; ++tenant) {
			// calculate number of assignments for each tenant
			IloLinearNumExpr nrAssignmentsForTenant = cplex.linearNumExpr();
			for (int server=0; server<nrServers; ++server) {
				nrAssignmentsForTenant.addTerm(1, tenantVars[tenant][server]);
			}
			cplex.addEq(nrAssignmentsForTenant, 1);
		}
		// create server activation constraints
		for (int server=0; server<nrServers; ++server) {
			for (int tenant=0; tenant<nrTenants; ++tenant) {
				cplex.addLe(tenantVars[tenant][server], serverVars[server]);
			}
		}
		// create capacity constraints
		for (int server=0; server<nrServers; ++server) {
			for (int metric=0; metric<nrMetrics; ++metric) {
				// calculate accumulated consumption of all assigned tenants
				IloLinearNumExpr consumedCapacity = cplex.linearNumExpr();
				for (int tenant=0; tenant<nrTenants; ++tenant) {
					IloIntVar tenantVar	= tenantVars[tenant][server];
					double consumption 	= problem.getConsumption(tenant, metric);
					consumedCapacity.addTerm(consumption, tenantVar);
				}
				// consumed capacity must not exceed available capacity
				double availableCapacity = problem.getCapacity(server, metric);
				cplex.addLe(consumedCapacity, availableCapacity);
			}
		}
		// create objective: total server activation cost
		IloLinearNumExpr totalCost = cplex.linearNumExpr();
		for (int server=0; server<nrServers; ++server) {
			IloIntVar serverVar = serverVars[server]; 
			double cost 		= problem.getCost(server);
			totalCost.addTerm(cost, serverVar);
		}
		cplex.addMinimize(totalCost);
		// solve
		cplex.solve();
		// interpret solution
		Status status = cplex.getStatus();
		assert(status == IloCplex.Status.Optimal || status == IloCplex.Status.Infeasible);
		boolean feasible = !(status == IloCplex.Status.Infeasible);
		// if problem was feasible then retrieve other values
		double optimalCost 		= -1;
		int[] tenantAssignments	= null;
		if (feasible) {
			optimalCost 		= cplex.getObjValue();
			tenantAssignments 	= new int[nrTenants];
			for (int tenant=0; tenant<nrTenants; ++tenant) {
				for (int server=0; server<nrServers; ++server) {
					IloIntVar tenantVar = tenantVars[tenant][server];
					if (cplex.getValue(tenantVar) > 0.5) {
						tenantAssignments[tenant] = server;
						break;
					}
				}
			}
		}
		return new ConsolidationSolution(feasible, optimalCost, tenantAssignments);
	}

}
