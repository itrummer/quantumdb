package raw_material.consolidation.dwave.mapper;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import raw_material.consolidation.dwave.ConsolidationMappingGeneric;
import raw_material.consolidation.dwave.variable.CapacityVariable;
import raw_material.consolidation.testcase.ConsolidationProblem;
import raw_material.dwave.Mapping;
import raw_material.dwave.QubitMatrix;
import raw_material.dwave.variables.LogicalVariable;
import raw_material.util.MapperUtil;


public abstract class Mapper {
	
	public final QubitMatrix qubitMatrix;
	
	public Mapper(QubitMatrix qubitMatrix) {
		this.qubitMatrix = qubitMatrix;
	}
	
	// Variable assignment
	//////////////////////
	
	// - assignment of tenant variables differs between triangle and matrix mapper
	// - only the matrix mapper has auxiliary assignment variables
	/*
	// assign auxiliary variables for server activation
	public static LogicalVariable[][] assignAuxActivationVars(
			ConsolidationProblem problem, OneMaxBar[] activationMaxBars) {
		int nrTenants = problem.nrTenants;
		int nrServers = problem.nrServers;
		assert(activationMaxBars.length == nrServers);
		LogicalVariable[][] result = new LogicalVariable[nrServers][nrTenants];
		for (int server=0; server<nrServers; ++server) {
			LogicalVariable[] curAuxVars 	= new LogicalVariable[nrTenants];
			result[server]					= curAuxVars;
			OneMaxBar maxBar 				= activationMaxBars[server];
			for (int tenant=0; tenant<nrTenants; ++tenant) {
				LogicalVariable	auxVar	= new LogicalVariable();
				auxVar.qubits			= maxBar.getAuxiliaries(tenant);
				curAuxVars[tenant] 		= auxVar;
			}
		}
		return result;
	}
	// assign server activation variables
	public static LogicalVariable[] assignServerVars(
			ConsolidationProblem problem, OneMaxBar[] activationMaxBars) {
		int nrServers = problem.nrServers;
		LogicalVariable[] serverVars = new LogicalVariable[nrServers];
		for (int server=0; server<nrServers; ++server) {
			LogicalVariable serverVar 	= new LogicalVariable();
			serverVars[server] 			= serverVar;
			OneMaxBar activationBar 	= activationMaxBars[server];
			serverVar.qubits.add(activationBar.getOutput());
		}
		return serverVars;
	}
	*/
	// check whether the qubits of variables overlap that should not do so - assignment
	// aux vars are only used by matrix mapper (triangle mapper passes null pointer).
	public static void assertNoOverlap(ConsolidationProblem problem, LogicalVariable[][] tenantVars, 
			List<CapacityVariable>[][] capacityVars, LogicalVariable[][] activationAuxVars, 
			LogicalVariable[][] assignmentAuxVars) {
		int nrTenants = problem.nrTenants;
		int nrServers = problem.nrServers;
		int nrMetrics = problem.nrMetrics;
		// The qubits of different tenant variables, capacity variables,
		// and auxiliary variables must not overlap (while the qubits
		// of the server activation variables are taken from the
		// qubits of auxiliary variables).
		// We count the qubits of different variables separately and
		// then compare with the cardinality of the union set of all
		// qubits.
		int separateCount 			= 0;
		Set<Integer> unionQubits 	= new TreeSet<Integer>();
		// tenant assignment variables
		for (int tenant=0; tenant<nrTenants; ++tenant) {
			for (int server=0; server<nrServers; ++server) {
				Set<Integer> qubits = tenantVars[tenant][server].qubits;
				//System.out.println("Tenant " + tenant + "; server " + server + " - assignment qubits: " + qubits);
				MapperUtil.assertNoOverlap(qubits, unionQubits);
				separateCount += qubits.size();
				unionQubits.addAll(qubits);
			}
		}
		// capacity variables
		for (int server=0; server<nrServers; ++server) {
			for (int metric=0; metric<nrMetrics; ++metric) {
				List<CapacityVariable> curCapacityVars = capacityVars[server][metric];
				for (CapacityVariable capacityVar : curCapacityVars) {
					Set<Integer> qubits = capacityVar.qubits;
					//System.out.println("Server " + server + "; metric " + metric + " - capacity qubits: " + qubits);
					MapperUtil.assertNoOverlap(qubits, unionQubits);
					separateCount += qubits.size();
					unionQubits.addAll(qubits);
				}
			}
		}
		// auxiliary activation variables
		for (int server=0; server<nrServers; ++server) {
			for (int tenant=0; tenant<nrTenants; ++tenant) {
				Set<Integer> qubits = activationAuxVars[server][tenant].qubits;
				MapperUtil.assertNoOverlap(qubits, unionQubits);
				separateCount += qubits.size();
				unionQubits.addAll(qubits);
			}
		}
		// auxiliary assignment variables
		if (assignmentAuxVars != null) {
			for (int server=0; server<nrServers; ++server) {
				for (int tenant=0; tenant<nrTenants; ++tenant) {
					Set<Integer> qubits = assignmentAuxVars[server][tenant].qubits;
					MapperUtil.assertNoOverlap(qubits, unionQubits);
					separateCount += qubits.size();
					unionQubits.addAll(qubits);
				}
			}			
		}
		// compare separate and union count
		assert(unionQubits.size() == separateCount);
	}
	
	// Imposing constraints
	///////////////////////
	
	// - tenant assignment constraints differ between triangle and matrix mapper
	//	 (the scaling is however the same).
	
	// calculate scaling for assignment constraints
	public static double assignmentScaling(ConsolidationProblem problem) {
		// Not assigning one tenant might allow to switch off MULTIPLE servers
		// in comparison to the optimal solution with all tenants assigned.
		// Therefore, the scaling factor for tenant assignments must be
		// the sum of activation costs over all servers.
		int nrServers = problem.nrServers;
		double costSum = 0;
		for (int server=0; server<nrServers; ++server) {
			double cost = problem.getCost(server);
			costSum += cost;
		}
		return costSum + MapperUtil.EPSILON_WEIGHT;
	}
	// Determine scaling for capacity constraints
	public static double capacityScaling(ConsolidationProblem problem) {
		double maxServerEnergy = problem.maxServerCost;
		double minCapacityStep = problem.minCapacityStep;
		return MapperUtil.EPSILON_WEIGHT + maxServerEnergy / (minCapacityStep * minCapacityStep);
	}
	// Add weights representing capacity constraints
	public static void imposeCapacityConstraints(ConsolidationProblem problem, LogicalVariable[][] tenantVars, 
			List<CapacityVariable>[][] capacityVars, ConsolidationMappingGeneric mapping) {
		int nrTenants = problem.nrTenants;
		int nrServers = problem.nrServers;
		int nrMetrics = problem.nrMetrics;
		// capacity constraints are set for each server and for each metric independently
		double scaling = capacityScaling(problem);
		for (int server=0; server<nrServers; ++server) {
			for (int metric=0; metric<nrMetrics; ++metric) {
				// set weights between tenant assignment variables
				for (int tenant=0; tenant<nrTenants; ++tenant) {
					double consumption 			= problem.getConsumption(tenant, metric);
					double weight				= scaling * consumption * consumption;
					LogicalVariable tenantVar	= tenantVars[tenant][server];
					tenantVar.addWeight(mapping, weight);
				}
				for (int tenant1=0; tenant1<nrTenants; ++tenant1) {
					for (int tenant2=0; tenant2<nrTenants; ++tenant2) {
						if (tenant1 < tenant2) {
							double consumption1			= problem.getConsumption(tenant1, metric);
							double consumption2			= problem.getConsumption(tenant2, metric);
							double weight				= scaling * 2 * consumption1 * consumption2;
							LogicalVariable tenantVar1	= tenantVars[tenant1][server];
							LogicalVariable tenantVar2	= tenantVars[tenant2][server];
							tenantVar1.addConnectionWeight(mapping, weight, tenantVar2);
						}
					}
				}
				// set weights between capacity variables
				List<CapacityVariable> curCapacityVars = capacityVars[server][metric]; 
				for (CapacityVariable capacityVar : curCapacityVars) {
					double capacity = capacityVar.capacity;
					double weight	= scaling * capacity * capacity;
					capacityVar.addWeight(mapping, weight);
				}
				for (CapacityVariable capacityVar1 : curCapacityVars) {
					int nrIdenticalPairs = 0;
					for (CapacityVariable capacityVar2 : curCapacityVars) {
						if (capacityVar1 != capacityVar2) {
							double capacity1 	= capacityVar1.capacity;
							double capacity2 	= capacityVar2.capacity;
							// we will visit each pair of capacity variables twice -
							// therefore add only half the required weight between
							// capacity variables (total is scaling * 2 * cap1 * cap2).
							double weight		= scaling * capacity1 * capacity2;
							capacityVar1.addConnectionWeight(mapping, weight, capacityVar2);
						} else {
							++nrIdenticalPairs;
						}
					}
					assert(nrIdenticalPairs == 1);
				}
				// set weights between tenant and capacity variables
				for (int tenant=0; tenant<nrTenants; ++tenant) {
					LogicalVariable tenantVar = tenantVars[tenant][server];
					for (CapacityVariable capacityVar : curCapacityVars) {
						double consumption	= problem.getConsumption(tenant, metric);
						double capacity		= capacityVar.capacity;
						double weight		= -scaling * 2 * consumption * capacity;
						tenantVar.addConnectionWeight(mapping, weight, capacityVar);
					}
				}
			} // metric
		} // server
	}
	// Determine scaling for max constraints
	public static double activationMaxScaling(ConsolidationProblem problem) {
		// To switch of a server (even if tenants are assigned) at least one
		// maximum constraint must be violated. The potential gain in server
		// activation energy must be exceeded by the violation penalty.
		return problem.maxServerCost + MapperUtil.EPSILON_WEIGHT;
	}
	// Add weights representing max constraints for server activation
	public static void imposeMaxActivationConstraints(ConsolidationProblem problem, LogicalVariable[][] tenantVars, 
			LogicalVariable[][] auxActivationVars, ConsolidationMappingGeneric mapping) {
		int nrTenants = problem.nrTenants;
		int nrServers = problem.nrServers;
		assert(auxActivationVars.length == nrServers);
		double scaling = activationMaxScaling(problem);
		for (int server=0; server<nrServers; ++server) {
			LogicalVariable[] curServerAuxVars = auxActivationVars[server];
			// treat first tenant
			LogicalVariable firstTenantVar 	= tenantVars[0][server];
			LogicalVariable firstAuxVar		= curServerAuxVars[0];
			MapperUtil.addEqualityConstraint(firstTenantVar, firstAuxVar, scaling, mapping);
			// treat remaining tenants
			for (int tenant=1; tenant<nrTenants; ++tenant) {
				LogicalVariable tenantVar 	= tenantVars[tenant][server];
				LogicalVariable lastAuxVar	= curServerAuxVars[tenant-1];
				LogicalVariable curAuxVar	= curServerAuxVars[tenant];
				MapperUtil.addMaxConstraint(tenantVar, lastAuxVar, curAuxVar, scaling, mapping);
			}
		}
	}
	// Add goal formula
	public static void imposeGoalFormula(ConsolidationProblem problem, 
			LogicalVariable[] serverVars, ConsolidationMappingGeneric mapping) {
		int nrServers = problem.nrServers;
		for (int server=0; server<nrServers; ++server) {
			double energy = problem.getCost(server);
			serverVars[server].addWeight(mapping, energy);
		}
	}
	// Add weights making sure that all physical qubits representing the same logical variable
	// are assigned to the same value for the lowest-energy state. Note that server activation
	// variables share their qubit with auxiliary variables and are therefore not treated
	// separately. This function has to be invoked AFTER all other weights have been set.
	// Assignment aux vars are only used by matrix mapper (triangle mapper passes null pointer).
	/*
	public static void imposeConsistencyConstraints(ConsolidationProblem problem, LogicalVariable[][] tenantVars,
			List<CapacityVariable>[][] 	capacityVars, LogicalVariable[][] auxActivationVars, 
			LogicalVariable[][] auxAssignmentVars, ConsolidationMappingGeneric mapping) {
		int nrTenants = problem.nrTenants;
		int nrServers = problem.nrServers;
		int nrMetrics = problem.nrMetrics;
		// tenant assignment variables
		for (int tenant=0; tenant<nrTenants; ++tenant) {
			for (int server=0; server<nrServers; ++server) {
				tenantVars[tenant][server].addEqualityWeightsGeneric(mapping);
			}
		}
		// capacity variables
		for (int server=0; server<nrServers; ++server) {
			for (int metric=0; metric<nrMetrics; ++metric) {
				List<CapacityVariable> curCapacityVars = capacityVars[server][metric];
				for (CapacityVariable capacityVar : curCapacityVars) {
					capacityVar.addEqualityWeightsForChain(mapping);
				}
			}
		}
		// auxiliary activation variables
		for (int server=0; server<nrServers; ++server) {
			for (int input=0; input<nrTenants; ++input) {
				auxActivationVars[server][input].addEqualityWeightsForChain(mapping);
			}
		}
		// auxiliary assignment variables
		if (auxAssignmentVars!=null) {
			for (int server=0; server<nrServers; ++server) {
				for (int input=0; input<nrTenants; ++input) {
					auxAssignmentVars[server][input].addEqualityWeightsForChain(mapping);
				}
			}			
		}
	}
	*/
	
	// Setting indices
	//////////////////
	
	// Specify qubit indices of tenant assignment variables inside of mapping object.
	public static void setTenantIndices(
			ConsolidationProblem problem, LogicalVariable[][] tenantVars, ConsolidationMappingGeneric mapping) {
		int nrTenants = problem.nrTenants;
		int nrServers = problem.nrServers;
		for (int tenant=0; tenant<nrTenants; ++tenant) {
			for (int server=0; server<nrServers; ++server) {
				LogicalVariable tenantVar = tenantVars[tenant][server];
				int firstQubit = tenantVar.qubits.iterator().next();
				mapping.setTenantIndex(tenant, server, firstQubit);
			}
		}
	}
	// Specify qubit indices of server activation variables inside of mapping object.
	public static void setServerIndices(
			ConsolidationProblem problem, LogicalVariable[] serverVars, ConsolidationMappingGeneric mapping) {
		int nrServers = problem.nrServers;
		for (int server=0; server<nrServers; ++server) {
			LogicalVariable serverVar = serverVars[server];
			int firstQubit = serverVar.qubits.iterator().next();
			mapping.setServerIndex(server, firstQubit);
		}
	}
	// Specify groups of qubits that should obtain the same value by an optimal QUBO assignment
	// since they all represent the same logical problem variable. Only matrix mapper has 
	// auxiliary assignment variables (triangle mapper passes null pointer).
	public static void setConsistencyGroups(ConsolidationProblem problem, LogicalVariable[][] tenantVars, 
			List<CapacityVariable>[][] capacityVars, LogicalVariable[][] auxActivationVars, 
			LogicalVariable[][] auxAssignmentVars, LogicalVariable[] serverVars, ConsolidationMappingGeneric mapping) {
		int nrTenants = problem.nrTenants;
		int nrServers = problem.nrServers;
		int nrMetrics = problem.nrMetrics;
		// tenant assignment variables
		for (int tenant=0; tenant<nrTenants; ++tenant) {
			for (int server=0; server<nrServers; ++server) {
				LogicalVariable tenantVar = tenantVars[tenant][server];
				mapping.addConsistentQubits(tenantVar.qubits);
			}
		}
		// capacity variables
		for (int server=0; server<nrServers; ++server) {
			for (int metric=0; metric<nrMetrics; ++metric) {
				List<CapacityVariable> curCapacityVars = capacityVars[server][metric];
				for (CapacityVariable capacityVar : curCapacityVars) {
					mapping.addConsistentQubits(capacityVar.qubits);
				}
			}
		}
		// auxiliary activation variables
		for (int server=0; server<nrServers; ++server) {
			for (int tenant=0; tenant<nrTenants; ++tenant) {
				LogicalVariable auxVar = auxActivationVars[server][tenant];
				mapping.addConsistentQubits(auxVar.qubits);
			}
		}
		// auxiliary assignment variables
		if (auxAssignmentVars != null) {
			for (int server=0; server<nrServers; ++server) {
				for (int tenant=0; tenant<nrTenants; ++tenant) {
					LogicalVariable auxVar = auxAssignmentVars[server][tenant];
					mapping.addConsistentQubits(auxVar.qubits);
				}
			}			
		}
		// server activation variables
		for (int server=0; server<nrServers; ++server) {
			LogicalVariable serverVar = serverVars[server];
			mapping.addConsistentQubits(serverVar.qubits);
		}
	}
	// maps a consolidation problem onto D-Wave
	public abstract Mapping transform(ConsolidationProblem problem) throws Exception;
}
