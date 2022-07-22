package raw_material.consolidation.cplex;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import raw_material.consolidation.dwave.ConsolidationMappingGeneric;
import raw_material.consolidation.dwave.mapper.Mapper;
import raw_material.consolidation.testcase.ConsolidationProblem;
import raw_material.consolidation.testcase.ConsolidationSolution;
import raw_material.dwave.Mapping;
import raw_material.util.SolverUtil;
import raw_material.util.TestUtil;
import ilog.concert.IloException;
import ilog.concert.IloIntVar;
import ilog.concert.IloNumExpr;
import ilog.cplex.IloCplex;
import ilog.cplex.IloCplex.Status;
//import consolidation.dwave.mapper.MapperMatrix;
//import consolidation.dwave.mapper.MapperTriangle;

// This solver simulates the D-Wave annealer. It uses a D-Wave mapper to transform
// a consolidation problem into a quadratic representation and solves this QUBO
// representation using CPLEX.
public class QuadraticConsolidationSolver extends ConsolidationSolver {
	private final Mapper mapper;
	// Allow access to intermediate results for testing.
	ConsolidationProblem problem;			// input consolidation problem
	int[] assignmentConstraints;	// input assignment constraints
	ConsolidationMappingGeneric mapping;			// QUBO representation
	IloIntVar[] qubitVars;			// CPLEX variables representing qubits
	int[] qubitValues;				// Values for qubits in optimal solution
	boolean solutionIsConsistent;	// whether all qubits representing the same variable have the same value
	int[] tenantAssignments;		// for each tenant the index of the server it was assigned to
	boolean allTenantsAssigned;		// whether each tenant was assigned to at least one server
	boolean capacitiesRespected;	// whether each servers capacity limits are respected by assignment
	boolean[] serverActivated;		// whether a server needs to be activated
	boolean activationConsistency;	// whether exactly those servers with assigned tenants are activated
	double totalActivationCost;		// accumulated operational costs over all activated servers
	ConsolidationSolution solution;	// extracted solution to consolidation problem
	
	// Takes a mapper object as input which transforms a consolidation problem into QUBO representation.
	public QuadraticConsolidationSolver(Mapper mapper) throws IloException {
		super();
		//assert(mapper instanceof MapperTriangle || mapper instanceof MapperMatrix);
		this.mapper = mapper;
	}
	// Creates goal formula for CPLEX: minimize sum of all quadratic energy terms.
	private void createGoalFormula() throws IloException {
		List<IloNumExpr> energyTerms = new LinkedList<IloNumExpr>();
		for (int qubit=0; qubit<512; ++qubit) {
			IloIntVar qubitVar 	= qubitVars[qubit];
			double weight 		= mapping.getWeight(qubit);
			IloNumExpr product	= cplex.prod(weight, qubitVar);
			energyTerms.add(product);
		}
		for (int qubit1=0; qubit1<512; ++qubit1) {
			for (int qubit2=0; qubit2<512; ++qubit2) {
				if (qubit1<qubit2) {
					IloIntVar qubit1Var	= qubitVars[qubit1];
					IloIntVar qubit2Var	= qubitVars[qubit2];
					if (mapper.qubitMatrix.isConnected(qubit1, qubit2)) {
						double weight 		= mapping.getConnectionWeight(qubit1, qubit2);
						IloNumExpr product 	= cplex.prod(qubit1Var, qubit2Var, weight);
						energyTerms.add(product);						
					}
				}
			}
		}
		IloNumExpr[] energyTermsArray 	= energyTerms.toArray(new IloNumExpr[energyTerms.size()]);
		IloNumExpr energyLevel 			= cplex.sum(energyTermsArray);
		cplex.addMinimize(energyLevel);
	}
	// Sets constraints concerning tenant assignments if any are specified.
	private void setConstraints() throws IloException {
		if (assignmentConstraints != null) {
			int nrTenants = problem.nrTenants;
			int nrServers = problem.nrServers;
			for (int tenant=0; tenant<nrTenants; ++tenant) {
				for (int server=0; server<nrServers; ++server) {
					int index			= mapping.getTenantIndex(tenant, server);
					IloIntVar tenantVar = qubitVars[index];
					int value			= assignmentConstraints[tenant] == server? 1:0;
					cplex.addEq(tenantVar, value);
				}
			}
		}
	}
	// Extracts values for all qubit variables.
	private void extractQubitValues() throws Exception {
		qubitValues = new int[512];
		for (int qubit=0; qubit<512; ++qubit) {
			IloIntVar qubitVar	= qubitVars[qubit];
			double value 		= cplex.getValue(qubitVar);
			int roundedValue	= SolverUtil.cplexBinaryValue(value);
			qubitValues[qubit]	= roundedValue;
		}
	}
	// Returns true if all qubits representing the same logical variable obtain the same value.
	void checkConsistency() throws Exception {
		solutionIsConsistent = true;
		for (Set<Integer> consistentQubits : mapping.getConsistentQubits()) {
			int minVal = Integer.MAX_VALUE;
			int maxVal = Integer.MIN_VALUE;
			for (int qubit : consistentQubits) {
				int value	= qubitValues[qubit];
				minVal 		= Math.min(minVal, value);
				maxVal 		= Math.max(maxVal, value);
			}
			if (minVal != maxVal) {
				solutionIsConsistent = false;
				break;
			}
		}
	}
	// Retrieves tenant assignment vector from CPLEX solution.
	void extractTenantAssignments() throws Exception {
		int nrTenants = problem.nrTenants;
		int nrServers = problem.nrServers;
		tenantAssignments = new int[nrTenants];
		Arrays.fill(tenantAssignments, -1);
		for (int tenant=0; tenant<nrTenants; ++tenant) {
			for (int server=0; server<nrServers; ++server) {
				int index 	= mapping.getTenantIndex(tenant, server);
				int value	= qubitValues[index]; 
				if (value == 1) {
					tenantAssignments[tenant] = server;
					break;
				}
			}
		}
	}
	// Checks if all tenants are assigned. Unassigned tenants are marked with server -1.
	void checkAllTenantsAssigned() {
		allTenantsAssigned = true;
		int nrTenants = problem.nrTenants;
		for (int tenant=0; tenant<nrTenants; ++tenant) {
			if (tenantAssignments[tenant] == -1) {
				allTenantsAssigned = false;
				break;
			}
		}
	}
	// Checks if all server capacities are respected by the current assignment.
	void checkCapacitiesRespected() {
		int nrTenants = problem.nrTenants;
		int nrServers = problem.nrServers;
		int nrMetrics = problem.nrMetrics;
		capacitiesRespected = true;
		for (int server=0; server<nrServers; ++server) {
			for (int metric=0; metric<nrMetrics; ++metric) {
				double capacity 	= problem.getCapacity(server, metric);
				double consumption	= 0;
				for (int tenant=0; tenant<nrTenants; ++tenant) {
					if (tenantAssignments[tenant] == server) {
						consumption += problem.getConsumption(tenant, metric);
					}
				}
				if (consumption - capacity > TestUtil.DOUBLE_TOLERANCE) {
					capacitiesRespected = false;
					break;
				}
			}
		}
	}
	// Extracts activation state for each server.
	void extractServerActivation() {
		int nrServers 		= problem.nrServers;
		serverActivated 	= new boolean[nrServers];
		for (int server=0; server<nrServers; ++server) {
			int index 				= mapping.getServerIndex(server);
			int value 				= qubitValues[index];
			serverActivated[server] = (value == 1);
		}
	}
	// Checks that each server to which tenants are assigned is indeed marked as activated.
	void checkServerActivationConsistency() {
		activationConsistency = true;
		int nrTenants = problem.nrTenants;
		int nrServers = problem.nrServers;
		for (int server=0; server<nrServers; ++server) {
			boolean tenantsAssigned = false;
			for (int tenant=0; tenant<nrTenants; ++tenant) {
				int assignedServer = tenantAssignments[tenant];
				if (assignedServer == server) {
					tenantsAssigned = true;
				}
			}
			if (serverActivated[server] != tenantsAssigned) {
				activationConsistency = false;
			}
		}
	}
	// Calculate the accumulated cost of all activated servers.
	void calculateActivationCost() {
		int nrServers = problem.nrServers;
		totalActivationCost = 0;
		for (int server=0; server<nrServers; ++server) {
			if (serverActivated[server]) {
				double cost 		= problem.getCost(server);
				totalActivationCost += cost;
			}
		}
	}
	// Outputs obtained values for all qubits.
	private void outputQubits(ConsolidationProblem problem, ConsolidationMappingGeneric mapping, IloIntVar[] qubitVars) throws Exception {
		int nrTenants = problem.nrTenants;
		int nrServers = problem.nrServers;
		// assignment variables
		System.out.println();
		for (int tenant=0; tenant<nrTenants; ++tenant) {
			System.out.print("Tenant " + tenant + " assignment vector:\t");
			for (int server=0; server<nrServers; ++server) {
				int index 	= mapping.getTenantIndex(tenant, server);
				int value	= qubitValues[index];
				System.out.print("\t" + value + " (" + index + ")");
			}
			System.out.println();
		}
		// server activation variables
		System.out.print("Server activation vector: \t");
		for (int server=0; server<nrServers; ++server) {
			int index 	= mapping.getServerIndex(server);
			int value	= qubitValues[index];
			System.out.print("\t" + value + " (" + index + ")");
		}
		System.out.println();
	}
	// Extract solution of consolidation problem from solution to quadratic problem
	// generated by CPLEX. Also checks consistency of obtained solution.
	void extractSolution() throws Exception {
		Status status = cplex.getStatus();
		assert(status == IloCplex.Status.Optimal);
		extractQubitValues();
		checkConsistency();
		assert(solutionIsConsistent);
		extractTenantAssignments();
		checkAllTenantsAssigned();
		checkCapacitiesRespected();
		extractServerActivation();
		checkServerActivationConsistency();
		calculateActivationCost();
		// construct solution
		boolean isFeasible = allTenantsAssigned && capacitiesRespected;
		solution = new ConsolidationSolution(isFeasible, totalActivationCost, tenantAssignments);
	}
	// Output debugging information about solver process.
	@SuppressWarnings("static-access")
	private void debugOutput() throws Exception {
		System.out.println("*****************************************");
		problem.toConsole();
		/*
		if (mapper instanceof MapperTriangle) {
			MapperTriangle triangleMapper = (MapperTriangle)mapper;
			System.out.println("Assignment scaling: \t" + triangleMapper.assignmentScaling(problem));
			System.out.println("Capacity scaling: \t" + triangleMapper.capacityScaling(problem));
			System.out.println("Maximum scaling: \t" + triangleMapper.activationMaxScaling(problem));
		}
		*/
		System.out.println("Objective value: " + cplex.getObjValue());
		outputQubits(problem, mapping, qubitVars);
		System.out.println("Qubit consistency: " + solutionIsConsistent);
		System.out.println("All tenants assigned: " + allTenantsAssigned);
		System.out.println("Capacities respected: " + capacitiesRespected);
		System.out.println("Activation consistent: " + activationConsistency);
		System.out.println("*****************************************");
	}
	// This function solves the consolidation problem but fixes the tenant assignments.
	// Passing the null pointer for the constraints means that no constraints are specified.
	public ConsolidationSolution solveWithConstraints(ConsolidationProblem uncastedProblem, 
			int[] assignmentConstraints) throws Exception {
		assert(uncastedProblem instanceof ConsolidationProblem);
		problem = (ConsolidationProblem)uncastedProblem;
		assert(assignmentConstraints == null || assignmentConstraints.length == problem.nrTenants);
		this.assignmentConstraints = assignmentConstraints;
		// clear model
		cplex.clearModel();
		// create variables
		qubitVars = cplex.boolVarArray(512);
		// create mapping
		Mapping uncastedMapping = mapper.transform(problem);
		assert(uncastedMapping instanceof ConsolidationMappingGeneric);
		mapping = (ConsolidationMappingGeneric)uncastedMapping;
		// set constraints if specified
		setConstraints();
		// create goal formula
		createGoalFormula();
		// solve
		cplex.solve();
		// extract solution
		extractSolution();
		// output for debugging
		debugOutput();
		return solution;		
	}
	@Override
	public ConsolidationSolution solve(ConsolidationProblem uncastedProblem) throws Exception {
		return solveWithConstraints(uncastedProblem, null);
	}
}
