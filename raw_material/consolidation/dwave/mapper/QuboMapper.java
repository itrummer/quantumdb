package raw_material.consolidation.dwave.mapper;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import raw_material.consolidation.dwave.ConsolidationMappingGeneric;
import raw_material.consolidation.dwave.variable.CapacityVariable;
import raw_material.consolidation.testcase.ConsolidationProblem;
import raw_material.dwave.Mapping;
import raw_material.dwave.adjacency.FullyConnected;
import raw_material.dwave.variables.LogicalVariable;
import raw_material.util.MapperUtil;

public class QuboMapper extends Mapper {

	public QuboMapper() {
		super(new FullyConnected());
	}

		// calculate scaling for capacity constraints
		// Add weights representing assignment constraints
		static void imposeAssignmentConstraints(ConsolidationProblem problem, 
				LogicalVariable[][] tenantVars, ConsolidationMappingGeneric mapping) {
			int nrTenants = problem.nrTenants;
			int nrServers = problem.nrServers;
			// Motivate assigning each tenant at least once by putting negative weight on
			// each assignment variable.
			double scaling = assignmentScaling(problem);
			for (int tenant=0; tenant<nrTenants; ++tenant) {
				for (int server=0; server<nrServers; ++server) {
					tenantVars[tenant][server].addWeight(mapping, -scaling);
				}
			}
			// Demotivate assigning each tenant more than once by putting larger positive weights
			// between assignment variables.
			for (int tenant=0; tenant<nrTenants; ++tenant) {
				for (int server1=0; server1<nrServers; ++server1) {
					for (int server2=0; server2<nrServers; ++server2) {
						if (server1 < server2) {
							LogicalVariable assignmentVar1 = tenantVars[tenant][server1];
							LogicalVariable assignmentVar2 = tenantVars[tenant][server2];
							assignmentVar1.addConnectionWeight(mapping, 2*scaling, assignmentVar2);
						}
					}
					
				}
			}
		}
		
	void imposeActivationConstraints(ConsolidationProblem problem, LogicalVariable[][] tenantVars, 
			LogicalVariable[] serverVars, ConsolidationMappingGeneric mapping) {
		int nrTenants = problem.nrTenants;
		int nrServers = problem.nrServers;
		double scaling = activationMaxScaling(problem);
		for (int server=0; server<nrServers; ++server) {
			for (int tenant=0; tenant<nrTenants; ++tenant) {
				LogicalVariable tenantVar = tenantVars[tenant][server];
				LogicalVariable serverVar = serverVars[server];
				tenantVar.addWeight(mapping, 1.0 * scaling);
				tenantVar.addConnectionWeight(mapping, -1.0 * scaling, serverVar);
			}			
		}
	}
	
	@Override
	public Mapping transform(ConsolidationProblem problem) throws Exception {
		int nrTenants = problem.nrTenants;
		int nrServers = problem.nrServers;
		int nrMetrics = problem.nrMetrics;
		// create storage for logical variables
		LogicalVariable[][] 		tenantVars;				// first index is tenant; second is server
		List<CapacityVariable>[][] 	capacityVars;			// first index is server; second is metric
		LogicalVariable[] 			serverVars;				// first index is server
		// allocate memory
		tenantVars = new LogicalVariable[nrTenants][nrServers];
		serverVars = new LogicalVariable[nrServers];
		capacityVars = (LinkedList<CapacityVariable>[][])new LinkedList[nrServers][nrMetrics];
		// assign assignment variables
		int qubitIndex = 0;
		for (int tenant=0; tenant<nrTenants; ++tenant) {
			for (int server=0; server<nrServers; ++server) {
				LogicalVariable assignmentVariable = new LogicalVariable(qubitMatrix);
				tenantVars[tenant][server] = assignmentVariable;
				assignmentVariable.qubits.add(qubitIndex);
				++qubitIndex;
			}
		}
		// assign activation variables
		for (int server=0; server<nrServers; ++server) {
			LogicalVariable activationVariable = new LogicalVariable(qubitMatrix);
			serverVars[server] = activationVariable;
			activationVariable.qubits.add(qubitIndex);
			++qubitIndex;
		}
		// assign capacity variables
		for (int server=0; server<nrServers; ++server) {
			for (int metric=0; metric<nrMetrics; ++metric) {
				// add new set of capacity variables
				List<CapacityVariable> curCapacityVars 	= new LinkedList<CapacityVariable>();
				capacityVars[server][metric]			= curCapacityVars;
				double sumCapacity 						= problem.getCapacity(server, metric);
				List<Double> capacities					= MapperUtil.capacityValues(problem, sumCapacity);
				for (double capacity : capacities) {
					// add new capacity variable
					CapacityVariable capacityVar = new CapacityVariable(qubitMatrix, capacity);
					capacityVar.qubits.add(qubitIndex);
					curCapacityVars.add(capacityVar);
				}
			}
		}
		// check for overlap
		assertNoOverlap(problem, tenantVars, capacityVars, new LogicalVariable[][] {{}}, null);
		// create mapping
		ConsolidationMappingGeneric mapping = new ConsolidationMappingGeneric(
				qubitMatrix, nrTenants, nrServers);
		// set weights
		imposeAssignmentConstraints(problem, tenantVars, mapping);
		imposeCapacityConstraints(problem, tenantVars, capacityVars, mapping);
		imposeActivationConstraints(problem, tenantVars, serverVars, mapping);
		imposeGoalFormula(problem, serverVars, mapping);
		// set variable indices
		setTenantIndices(problem, tenantVars, mapping);
		setServerIndices(problem, serverVars, mapping);
		return mapping;
	}

}
