package raw_material.consolidation.dwave.mapper;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import raw_material.consolidation.dwave.variable.CapacityVariable;
import raw_material.consolidation.testcase.ConsolidationProblem;
import util.MapperUtil;
import consolidation.dwave.ConsolidationMappingGeneric;
import dwave.Mapping;
import dwave.basic_blocks.MultiMaxBar;
import dwave.basic_blocks.OneMaxBar;
import dwave.basic_blocks.Triangle;
import dwave.variables.LogicalVariable;
import static dwave.basic_blocks.TriangleDirection.*;

public class MapperMatrix extends Mapper {
	// Returns an equivalent consolidation problem with an even number of cost metrics, adding
	// one "dummy metric" (according to which all tenants have consumption zero and all servers
	// have capacity zero) if necessary.
	static ConsolidationProblem evenNrMetricsProblem(ConsolidationProblem inputProblem) {
		int nrTenants 			= inputProblem.nrTenants;
		int nrServers 			= inputProblem.nrServers;
		int oldNrMetrics 		= inputProblem.nrMetrics;
		double minCapacityStep 	= inputProblem.minCapacityStep;
		if (oldNrMetrics % 2 == 0) {
			return inputProblem;
		} else {
			ConsolidationProblem resultProblem = new ConsolidationProblem(
					nrTenants, nrServers, oldNrMetrics+1, minCapacityStep);
			// copy tenant consumption
			for (int tenant=0; tenant<nrTenants; ++tenant) {
				for (int metric=0; metric<oldNrMetrics; ++metric) {
					double consumption = inputProblem.getConsumption(tenant, metric);
					resultProblem.setConsumption(tenant, metric, consumption);
				}
			}
			// copy server capacities
			for (int server=0; server<nrServers; ++server) {
				for (int metric=0; metric<oldNrMetrics; ++metric) {
					double capacity = inputProblem.getCapacity(server, metric);
					resultProblem.setCapacity(server, metric, capacity);
				}
			}
			// copy server cost
			for (int server=0; server<nrServers; ++server) {
				double cost = inputProblem.getCost(server);
				resultProblem.setServerCost(server, cost);
			}
			// add dummy metric
			int newMetricIndex = oldNrMetrics;
			for (int tenant=0; tenant<nrTenants; ++tenant) {
				resultProblem.setConsumption(tenant, newMetricIndex, 0);
			}
			for (int server=0; server<nrServers; ++server) {
				resultProblem.setCapacity(server, newMetricIndex, 0);
			}
			return resultProblem;
		}
	}
	// check if there is enough space on the qubit matrix to map this problem
	static boolean sufficientSpace(ConsolidationProblem problem, int trianglesTopLeft, int requiredChains) {
		int nrServers = problem.nrServers;
		int nrMetrics = problem.nrMetrics;
		// calculate dimensions of triangle matrix
		int triangleCellWidth 			= MapperUtil.roundUpFour(requiredChains)/4;
		int trianglePairCellWidth 		= triangleCellWidth+1;
		int triangleMatrixCellWidth		= (nrMetrics/2) * trianglePairCellWidth;
		int matrixActivationCellWidth	= triangleMatrixCellWidth + 1;
		int triangleMatrixCellHeight	= nrServers * triangleCellWidth;
		// calculate required steps to the east considering triangle matrix and server activation blocks
		int eastSteps 				= matrixActivationCellWidth - 1;
		// make required number of steps to the east
		int curPos = trianglesTopLeft;
		for (int stepCtr=0; stepCtr<eastSteps; ++stepCtr) {
			if (!MapperUtil.CanGoEast(curPos)) {
				return false;
			}
			curPos = MapperUtil.GoEast(curPos, 1);
		}
		// consider height of triangle matrix
		int southSteps = triangleMatrixCellHeight - 1;
		for (int stepCtr=0; stepCtr<southSteps; ++stepCtr) {
			if (!MapperUtil.CanGoSouth(curPos)) {
				return false;
			}
			curPos = MapperUtil.GoSouth(curPos, 1);
		}
		return true;
	}
	// create triangles representing capacity constraints
	static Triangle[][] createTriangles(ConsolidationProblem problem, int topLeftStart, int requiredChains) {
		int nrServers = problem.nrServers;
		int nrMetrics = problem.nrMetrics;
		assert(nrMetrics % 2 == 0);
		int roundedChains = MapperUtil.roundUpFour(requiredChains);
		int triangleCellWidth = roundedChains/4;
		Triangle[][] triangles = new Triangle[nrServers][nrMetrics];
		for (int server=0; server<nrServers; ++server) {
			for (int metric=0; metric<nrMetrics; metric+=2) {
				int triangle1TopLeft = topLeftStart;
				triangle1TopLeft = MapperUtil.GoEast(triangle1TopLeft, (metric/2) * (triangleCellWidth+1));
				triangle1TopLeft = MapperUtil.GoSouth(triangle1TopLeft, server * triangleCellWidth);
				int triangle2TopLeft = MapperUtil.GoEast(triangle1TopLeft, 1);
				Triangle newTriangle1 = new Triangle(SOUTH_WEST, triangle1TopLeft, roundedChains);
				Triangle newTriangle2 = new Triangle(NORTH_EAST, triangle2TopLeft, roundedChains);
				int metric1 = metric;
				int metric2 = metric + 1;
				triangles[server][metric1] = newTriangle1;
				triangles[server][metric2] = newTriangle2;
			}
		}
		return triangles;
	}
	// calculates maximal number of broken chains over all triangles
	static int maxNrBrokenChains(ConsolidationProblem problem, Triangle[][] triangles) {
		int nrServers = problem.nrServers;
		int nrMetrics = problem.nrMetrics;
		int result = 0;
		for (int server=0; server<nrServers; ++server) {
			for (int metric=0; metric<nrMetrics; ++metric) {
				Triangle triangle = triangles[server][metric];
				result = Math.max(result, triangle.nrBrokenChains);
			}
		}
		return result;
	}
	// calculate top-left qubit index of triangle matrix
	static int triangleMatrixTopLeft(ConsolidationProblem problem) {
		int nrTenants = problem.nrTenants;
		int triangleOffsetX = (nrTenants-1)/2 + 1;
		int triangleOffsetY	= nrTenants>4 ? 1 : 0;
		int topLeft = 0;
		topLeft = MapperUtil.GoEast(topLeft, triangleOffsetX);
		topLeft = MapperUtil.GoSouth(topLeft, triangleOffsetY);
		return topLeft;
	}
	// calculate number of required triangle chains
	static int requiredChains(ConsolidationProblem problem, int topLeftStart) throws Exception {
		int nrTenants = problem.nrTenants;
		int nrServers = problem.nrServers;
		int nrMetrics = problem.nrMetrics;
		// calculate maximal number of capacity variables per server and metric
		int maxNrCapacityVars = 0;
		for (int server=0; server<nrServers; ++server) {
			for (int metric=0; metric<nrMetrics; ++metric) {
				double capacity = problem.getCapacity(server, metric);
				int curNrCapacityVars = MapperUtil.nrCapacityVars(problem, capacity);
				maxNrCapacityVars = Math.max(maxNrCapacityVars, curNrCapacityVars);
			}
		}
		// We don't know how many broken chains per triangle - the more broken chains we
		// can have the bigger the triangles need to be but increasing triangle size may
		// increase the number of broken chains. Therefore we calculate triangle size in
		// an iterative process.
		int assumedNrBroken = 0;
		int actualNrBroken = -1;
		int requiredChains = -1;
		while (assumedNrBroken != actualNrBroken) {
			assumedNrBroken = actualNrBroken;
			// Calculate number of chains assuming specific number of broken chains and
			// taking into account that only every second chain can represent a tenant.
			if (maxNrCapacityVars + assumedNrBroken > nrTenants) {
				requiredChains = nrTenants + maxNrCapacityVars + assumedNrBroken;
			} else {
				requiredChains = 2 * nrTenants;
			}
			if (!sufficientSpace(problem, topLeftStart, requiredChains)) {
				throw new Exception("Not enough qubits");
			}
			Triangle[][] triangles = createTriangles(problem, topLeftStart, requiredChains);
			actualNrBroken = maxNrBrokenChains(problem, triangles);
		}
		return requiredChains;
	}
	// Determine which chain indices are suitable as tenant chains for each server.
	static boolean[][] tenantChains(ConsolidationProblem problem, Triangle[][] triangles) throws Exception {
		int nrTenants = problem.nrTenants;
		int nrServers = problem.nrServers;
		int nrMetrics = problem.nrMetrics;
		boolean[][] result = new boolean[nrServers][2*nrTenants];
		for (int server=0; server<nrServers; ++server) {
			for (int tenant=0; tenant<nrTenants; ++tenant) {
				int chainCandidate1 = tenant*2;
				int chainCandidate2 = tenant*2 +1;
				boolean candidate1Ok = true;
				boolean candidate2Ok = true;
				for (int metric=0; metric<nrMetrics; ++metric) {
					Triangle triangle = triangles[server][metric];
					if (!triangle.chainOk[chainCandidate1]) {
						candidate1Ok = false;
					}
					if (!triangle.chainOk[chainCandidate2]) {
						candidate2Ok = false;
					}
				}
				if (!candidate1Ok && !candidate2Ok) {
					throw new Exception("Too many broken qubits");
				}
				assert(candidate1Ok || candidate2Ok);
				if (candidate1Ok) {
					result[server][chainCandidate1] = true;
					result[server][chainCandidate2] = false;
				} else {
					result[server][chainCandidate1] = false;
					result[server][chainCandidate2] = true;					
				}
			}
		}
		return result;
	}
	// Creates multi-max bar at west side of qubit matrix
	static MultiMaxBar createAssignmentBar(ConsolidationProblem problem, int requiredChains, boolean[][] tenantChains) {
		int nrTenants = problem.nrTenants;
		int nrServers = problem.nrServers;
		int minGroupDistance = MapperUtil.roundUpFour(requiredChains)/4;
		int topLeft = nrTenants>4 ? MapperUtil.GoSouth(0, 1) : 0;
		return new MultiMaxBar(topLeft, nrServers, nrTenants, tenantChains, minGroupDistance);
	}
	// Creates one max-bar per server representing activation state
	static OneMaxBar[] createActivationBars(ConsolidationProblem problem, int requiredChains, boolean[][] tenantChains) {
		int nrTenants = problem.nrTenants;
		int nrServers = problem.nrServers;
		int nrMetrics = problem.nrMetrics;
		// calculate index of qubit at top left of activation bars
		int assignmentCellWdith = (int)Math.ceil(nrTenants/2.0);
		int triangleCellWidth = MapperUtil.roundUpFour(requiredChains)/4;
		int trianglePairCellWidth = triangleCellWidth + 1;
		int allTrianglesCellWidth = (nrMetrics/2) * trianglePairCellWidth;
		int activationTopLeft = MapperUtil.GoEast(0, assignmentCellWdith + allTrianglesCellWidth);
		if (nrTenants>4) {
			activationTopLeft = MapperUtil.GoSouth(activationTopLeft, 1);
		}
		// create activation bars
		OneMaxBar[] result = new OneMaxBar[nrServers];
		int groupDistanceY = MapperUtil.roundUpFour(requiredChains)/4;
		Set<Integer> emptySet = new TreeSet<Integer>();
		for (int server=0; server<nrServers; ++server) {
			int curBarTopLeft = MapperUtil.GoSouth(activationTopLeft, server * groupDistanceY);
			result[server] = new OneMaxBar(curBarTopLeft, nrTenants, tenantChains[server], emptySet);
		}
		return result;
	}
	// create tenant variables and assign the corresponding qubits
	static LogicalVariable[][] assignTenantVars(ConsolidationProblem problem, Triangle[][] triangles, 
			MultiMaxBar assignmentBar, OneMaxBar[] activationBars, boolean[][] tenantChains) {
		int nrTenants = problem.nrTenants;
		int nrServers = problem.nrServers;
		int nrMetrics = problem.nrMetrics;
		LogicalVariable[][] tenantVars = new LogicalVariable[nrTenants][nrServers];
		for (int tenant=0; tenant<nrTenants; ++tenant) {
			for (int server=0; server<nrServers; ++server) {
				LogicalVariable tenantVar = new LogicalVariable();
				tenantVars[tenant][server] = tenantVar;
				// add qubits from assignment and activation bar
				tenantVar.qubits.addAll(assignmentBar.getInputQubits(server, tenant));
				tenantVar.qubits.add(activationBars[server].getInput(tenant));
				// add qubits from triangles
				int chainIndex1 = tenant*2;
				int chainIndex2 = tenant*2+1;
				boolean chain1Ok = tenantChains[server][chainIndex1];
				boolean chain2Ok = tenantChains[server][chainIndex2];
				assert(chain1Ok || chain2Ok);
				int chainIndex = chain1Ok ? chainIndex1 : chainIndex2;
				for (int metric=0; metric<nrMetrics; ++metric) {
					Triangle triangle = triangles[server][metric];
					tenantVar.qubits.addAll(triangle.getChain(chainIndex));
					triangle.markAsUsed(chainIndex);
				}
			}
		}
		return tenantVars;
	}
	// create capacity variables and assign the corresponding qubits
	static List<CapacityVariable>[][] assignCapacityVars(ConsolidationProblem problem, Triangle[][] triangles) 
			throws Exception {
		int nrServers = problem.nrServers;
		int nrMetrics = problem.nrMetrics;
		@SuppressWarnings("unchecked")
		List<CapacityVariable>[][] capacityVars = 
				(LinkedList<CapacityVariable>[][])new LinkedList[nrServers][nrMetrics];
		for (int server=0; server<nrServers; ++server) {
			for (int metric=0; metric<nrMetrics; ++metric) {
				List<CapacityVariable> curCapacityVars 	= new LinkedList<CapacityVariable>();
				capacityVars[server][metric]			= curCapacityVars;
				double sumCapacity						= problem.getCapacity(server, metric);
				List<Double> capacities					= MapperUtil.capacityValues(problem, sumCapacity);
				Triangle triangle 						= triangles[server][metric];
				for (double capacity : capacities) {
					CapacityVariable capacityVar = new CapacityVariable(capacity);
					curCapacityVars.add(capacityVar);
					Set<Integer> chain = triangle.markUnusedOkChain();
					capacityVar.qubits.addAll(chain);
				}
			}
		}
		return capacityVars;
	}
	// Create auxiliary variables representing tenant assignment and assign the corresponding qubits.
	static LogicalVariable[][] assignAuxAssignmentVars(ConsolidationProblem problem, MultiMaxBar assignmentBar) {
		int nrTenants = problem.nrTenants;
		int nrServers = problem.nrServers;
		LogicalVariable[][] auxVars = new LogicalVariable[nrServers][nrTenants];
		for (int server=0; server<nrServers; ++server) {
			for (int tenant=0; tenant<nrTenants; ++tenant) {
				LogicalVariable auxVar = new LogicalVariable();
				auxVars[server][tenant] = auxVar;
				auxVar.qubits.addAll(assignmentBar.getAuxQubits(server, tenant));
			}
		}
		return auxVars;
	}
	// Impose constraints that guarantee that each tenant is assigned at least once in
	// the minimum energy state.
	static void imposeAssignmentConstraints(ConsolidationProblem problem, LogicalVariable[][] tenantVars, 
			LogicalVariable[][] auxAssignmentVars, ConsolidationMappingGeneric mapping) {
		int nrTenants = problem.nrTenants;
		int nrServers = problem.nrServers;
		double scaling = assignmentScaling(problem);
		// enforce output of maxima
		for (int tenant=0; tenant<nrTenants; ++tenant) {
			// treat first server
			LogicalVariable firstServer = tenantVars[tenant][0];
			LogicalVariable firstAux = auxAssignmentVars[0][tenant];
			MapperUtil.addEqualityConstraint(firstServer, firstAux, scaling, mapping);
			// treat remaining servers
			for (int server=1; server<nrServers; ++server) {
				LogicalVariable tenantVar = tenantVars[tenant][server];
				LogicalVariable lastAuxVar = auxAssignmentVars[server-1][tenant];
				LogicalVariable curAuxVar = auxAssignmentVars[server][tenant];
				MapperUtil.addMaxConstraint(tenantVar, lastAuxVar, curAuxVar, scaling, mapping);
			}
		}
		// motivate tenant assignment
		int lastServer = nrServers - 1;
		for (int tenant=0; tenant<nrTenants; ++tenant) {
			LogicalVariable tenantAssignedVar = auxAssignmentVars[lastServer][tenant];
			tenantAssignedVar.addWeight(mapping, -scaling);
		}
	}
	@Override
	public Mapping transform(ConsolidationProblem uncastedProblem) throws Exception {
		assert(uncastedProblem instanceof ConsolidationProblem);
		ConsolidationProblem castedProblem = (ConsolidationProblem)uncastedProblem;
		ConsolidationProblem problem = evenNrMetricsProblem(castedProblem);
		int nrTenants = problem.nrTenants;
		int nrServers = problem.nrServers;
		// create storage for logical variables
		LogicalVariable[][] 		tenantVars;				// first index is tenant; second is server
		List<CapacityVariable>[][] 	capacityVars;			// first index is server; second is metric
		LogicalVariable[][]			auxAssignmentVars;		// first index is server; second is tenant
		LogicalVariable[][]			auxActivationVars;		// first index is server; second is tenant
		LogicalVariable[] 			serverVars;				// first index is server
		// create shapes
		int trianglesTopLeft	= triangleMatrixTopLeft(problem);
		int requiredChains 		= requiredChains(problem, trianglesTopLeft);
		if (!sufficientSpace(problem, trianglesTopLeft, requiredChains)) {
			throw new Exception("Not enough qubits!");
		}
		Triangle[][] triangles 		= createTriangles(problem, trianglesTopLeft, requiredChains);
		boolean[][] tenantChains 	= tenantChains(problem, triangles);
		MultiMaxBar assignmentBar 	= createAssignmentBar(problem, requiredChains, tenantChains);
		OneMaxBar[] activationBars 	= createActivationBars(problem, requiredChains, tenantChains);
		// assign variables
		tenantVars 			= assignTenantVars(problem, triangles, assignmentBar, activationBars, tenantChains);
		capacityVars 		= assignCapacityVars(problem, triangles);
		auxActivationVars	= assignAuxActivationVars(problem, activationBars);
		auxAssignmentVars	= assignAuxAssignmentVars(problem, assignmentBar);
		serverVars			= assignServerVars(problem, activationBars);
		// assert no overlap
		assertNoOverlap(problem, tenantVars, capacityVars, auxActivationVars, auxAssignmentVars);
		// create mapping
		ConsolidationMappingGeneric mapping = new ConsolidationMappingGeneric(nrTenants, nrServers);
		// assign weights
		imposeAssignmentConstraints(problem, tenantVars, auxAssignmentVars, mapping);
		imposeCapacityConstraints(problem, tenantVars, capacityVars, mapping);
		imposeMaxActivationConstraints(problem, tenantVars, auxActivationVars, mapping);
		imposeGoalFormula(problem, serverVars, mapping);
		imposeConsistencyConstraints(problem, tenantVars, capacityVars, 
				auxActivationVars, auxAssignmentVars, mapping);
		// set variable indices
		setTenantIndices(problem, tenantVars, mapping);
		setServerIndices(problem, serverVars, mapping);
		setConsistencyGroups(problem, tenantVars, capacityVars, auxActivationVars, null, serverVars, mapping);
		return mapping;
	}

}
