package raw_material.consolidation.dwave.mapper;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import raw_material.consolidation.dwave.variable.CapacityVariable;
import raw_material.consolidation.testcase.ConsolidationProblem;
import util.MapperUtil;
import consolidation.dwave.ConsolidationMappingGeneric;
import dwave.Mapping;
import dwave.basic_blocks.OneMaxBar;
import dwave.basic_blocks.Triangle;
import dwave.basic_blocks.TriangleDirection;
import dwave.variables.LogicalVariable;

// Transforms a consolidation problem into a triangle-shaped mapping on the qubit matrix.
public class MapperTriangle extends Mapper {
	// calculate required triangle size (expressed as number of chains)
	static int nrTriangleChains(ConsolidationProblem problem) {
		int nrTenants = problem.nrTenants;
		int nrServers = problem.nrServers;
		int nrMetrics = problem.nrMetrics;
		// calculate number of tenant assignment variables
		int nrTenantVars = nrTenants * nrServers;
		// sum capacity variables over all servers and resources
		int nrCapacityVars = 0;
		for (int server=0; server<nrServers; ++server) {
			for (int metric=0; metric<nrMetrics; ++metric) {
				double capacity 	= problem.getCapacity(server, metric);
				int additionalVars 	= MapperUtil.nrCapacityVars(problem, capacity);
				nrCapacityVars 		+= additionalVars;
			}
		}
		// assume that each faulty qubit disables one triangle chain (pessimistic) 
		int nrDisabledChanes = MapperUtil.DAMAGED_QUBITS.size();
		// only every second chain can represent a tenant assignment variable
		int tenantChains 			= 2 * nrTenantVars;
		int nonTenantChains			= nrCapacityVars + nrDisabledChanes;
		int chainsBetweenTenants 	= nrTenantVars;
		// number of tenant chains is lower bound on total number of chains
		int nonRoundedChains;
		if (chainsBetweenTenants >= nonTenantChains) {
			nonRoundedChains = tenantChains;
		} else {
			nonRoundedChains = tenantChains + (nonTenantChains - chainsBetweenTenants);
		}
		return MapperUtil.roundUpFour(nonRoundedChains);
	}
	// create triangle capturing assignment and capacity constraints
	static Triangle createTriangle(ConsolidationProblem problem) throws Exception {
		int nrTriangleChains 	= nrTriangleChains(problem);
		if (nrTriangleChains>28) {
			throw new Exception("Not enough Qubits!");
		}
		Triangle triangle 		= new Triangle(TriangleDirection.SOUTH_WEST, 8, nrTriangleChains);
		return triangle;
	}
	// returns a boolean vector indicating which chains represent tenant assignments
	static boolean[] tenantChains(ConsolidationProblem problem, Triangle triangle) {
		int nrTenants = problem.nrTenants;
		int nrServers = problem.nrServers;
		// create result vector
		int nrTenantVariables 	= nrTenants * nrServers;
		boolean[] result		= new boolean[nrTenantVariables * 2];
		Arrays.fill(result, false);
		// can only represent two tenants per cell
		for (int tenant=0; tenant<nrTenantVariables; ++tenant) {
			int firstCandidateChain 	= tenant * 2;
			int secondCandidateChain 	= tenant * 2 + 1;
			Set<Integer> firstCandidateQubits 	= triangle.getChain(firstCandidateChain);
			Set<Integer> secondCandidateQubits 	= triangle.getChain(secondCandidateChain);
			boolean firstCandidateOk 	= MapperUtil.isGroupIntact(firstCandidateQubits);
			boolean secondCandidateOk 	= MapperUtil.isGroupIntact(secondCandidateQubits);
			// we assume that at most two chains are disabled per cell
			assert(firstCandidateOk || secondCandidateOk);
			if (firstCandidateOk) {
				result[firstCandidateChain] = true;
			} else {
				result[secondCandidateChain] = true;
			}
		}
		return result;
	}
	// create max bars representing activation constraints
	static OneMaxBar[] createMaxBars(ConsolidationProblem problem, boolean[] isTenantChain) {
		int nrTenants 			= problem.nrTenants;
		int nrServers			= problem.nrServers;
		OneMaxBar[] maxBars		= new OneMaxBar[nrServers];
		Set<Integer> usedQubits	= new HashSet<Integer>();
		for (int server=0; server<nrServers; ++server) {
			// cut out input vector
			int firstChainIndex = 2 * server * nrTenants;
			int lastChainIndex = 2 * (server+1) * nrTenants;
			boolean[] inputChains = Arrays.copyOfRange(isTenantChain, firstChainIndex, lastChainIndex);
			// calculate top left qubit index
			int nrSteps = server * nrTenants;
			int topLeftQubit = MapperUtil.GoSouthHalf(0, nrSteps);
			// create new bar, register used qubits, and add bar to bar store
			OneMaxBar newBar = new OneMaxBar(topLeftQubit, nrTenants, inputChains, usedQubits);
			usedQubits.addAll(newBar.qubits);
			maxBars[server] = newBar;
		}
		return maxBars;
	}
	// assigns tenant variables to triangle chains
	static LogicalVariable[][] assignTenantVars(
			ConsolidationProblem problem, Triangle triangle, OneMaxBar[] maxBars, boolean[] isTenantChain) {
		int nrTenants = problem.nrTenants;
		int nrServers = problem.nrServers;
		LogicalVariable[][] tenantVars = new LogicalVariable[nrTenants][nrServers];
		int chainIndex 	= 0;
		for (int server=0; server<nrServers; ++server) {
			OneMaxBar serverActivationBar = maxBars[server];
			for (int tenant=0; tenant<nrTenants; ++tenant) {
				// select next tenant chain
				while (!isTenantChain[chainIndex]) {
					++chainIndex;
				}
				// create and add variable
				LogicalVariable assignmentVar 	= new LogicalVariable();
				tenantVars[tenant][server]		= assignmentVar;
				// variable is represented by one triangle chain and one max-bar input
				Set<Integer> qubits 			= new HashSet<Integer>();
				qubits.addAll(triangle.getChain(chainIndex));
				qubits.add(serverActivationBar.getInput(tenant));
				assignmentVar.qubits 			= qubits;
				// mark chain as used
				triangle.markAsUsed(chainIndex);
				++chainIndex;
			}
		}
		return tenantVars;
	}
	// assign capacity variables
	static List<CapacityVariable>[][] assignCapacityVars(ConsolidationProblem problem, Triangle triangle) throws Exception {
		int nrServers = problem.nrServers;
		int nrMetrics = problem.nrMetrics;
		@SuppressWarnings("unchecked")
		List<CapacityVariable>[][] capacityVars = 
			(LinkedList<CapacityVariable>[][])new LinkedList[nrServers][nrMetrics];
		for (int server=0; server<nrServers; ++server) {
			for (int metric=0; metric<nrMetrics; ++metric) {
				// add new set of capacity variables
				List<CapacityVariable> curCapacityVars 	= new LinkedList<CapacityVariable>();
				capacityVars[server][metric]			= curCapacityVars;
				double sumCapacity 						= problem.getCapacity(server, metric);
				List<Double> capacities					= MapperUtil.capacityValues(problem, sumCapacity);
				for (double capacity : capacities) {
					// add new capacity variable
					CapacityVariable capacityVar	= new CapacityVariable(capacity);
					Set<Integer> chain 				= triangle.markUnusedOkChain();
					capacityVar.qubits				= chain;
					curCapacityVars.add(capacityVar);
				}
			}
		}
		return capacityVars;
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
	@Override
	public Mapping transform(ConsolidationProblem uncastedProblem) throws Exception {
		assert(uncastedProblem instanceof ConsolidationProblem);
		ConsolidationProblem problem = (ConsolidationProblem)uncastedProblem;
		// create storage for logical variables
		LogicalVariable[][] 		tenantVars;				// first index is tenant; second is server
		List<CapacityVariable>[][] 	capacityVars;			// first index is server; second is metric
		LogicalVariable[][]			auxActivationVars;		// first index is server; second is input
		LogicalVariable[] 			serverVars;				// first index is server
		// create basic building blocks
		Triangle triangle			= createTriangle(problem);
		boolean[] isTenantChain 	= tenantChains(problem, triangle);
		OneMaxBar[] activationBars	= createMaxBars(problem, isTenantChain);
		// assign variables
		tenantVars 			= assignTenantVars(problem, triangle, activationBars, isTenantChain);
		capacityVars 		= assignCapacityVars(problem, triangle);
		auxActivationVars	= assignAuxActivationVars(problem, activationBars);
		serverVars			= assignServerVars(problem, activationBars);
		// check for overlap
		assertNoOverlap(problem, tenantVars, capacityVars, auxActivationVars, null);
		// create mapping
		int nrTenants = problem.nrTenants;
		int nrServers = problem.nrServers;
		ConsolidationMappingGeneric mapping = new ConsolidationMappingGeneric(nrTenants, nrServers);
		// set weights
		imposeAssignmentConstraints(problem, tenantVars, mapping);
		imposeCapacityConstraints(problem, tenantVars, capacityVars, mapping);
		imposeMaxActivationConstraints(problem, tenantVars, auxActivationVars, mapping);
		imposeGoalFormula(problem, serverVars, mapping);
		imposeConsistencyConstraints(problem, tenantVars, capacityVars, auxActivationVars, null, mapping);
		// set variable indices
		setTenantIndices(problem, tenantVars, mapping);
		setServerIndices(problem, serverVars, mapping);
		setConsistencyGroups(problem, tenantVars, capacityVars, auxActivationVars, null, serverVars, mapping);
		return mapping;
	}
}
