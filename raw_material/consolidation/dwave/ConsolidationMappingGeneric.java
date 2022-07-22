package raw_material.consolidation.dwave;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import raw_material.dwave.Mapping;
import raw_material.dwave.QubitMatrix;
import raw_material.util.MapperUtil;

public class ConsolidationMappingGeneric extends Mapping {
	// The following variables help to transform the optimal value assignments 
	// to the qubits in the qubit matrix into a consolidation problem solution.
	int[][] tenantAssignmentIndex;			// indices of qubits indicating tenant assignment
											// first index: tenant; second index: server
	int[] serverActivationIndex;			// indices of qubit indicating server activation
	List<Set<Integer>> consistentQubits; 	// sets of qubits that must have the same value if
											// the mapped consolidation problem has a solution

	public ConsolidationMappingGeneric(QubitMatrix qubitMatrix, int nrTenants, int nrServers) {
		super(qubitMatrix);
		tenantAssignmentIndex 	= new int[nrTenants][nrServers];
		serverActivationIndex	= new int[nrServers];
		consistentQubits 		= new LinkedList<Set<Integer>>();
		// mark indices as uninitialized
		for (int tenant=0; tenant<nrTenants; ++tenant) {
			Arrays.fill(tenantAssignmentIndex[tenant], -1);			
		}
		Arrays.fill(serverActivationIndex, -1);
	}
	// Set index for tenant assignment.
	public void setTenantIndex(int tenant, int server, int index) {
		assert(0<=index && index<=512);
		tenantAssignmentIndex[tenant][server] = index;
	}
	// Get index for tenant assignment.
	public int getTenantIndex(int tenant, int server) {
		int index = tenantAssignmentIndex[tenant][server];
		assert(index != -1);
		return index;
	}
	// Set index for server activation.
	public void setServerIndex(int server, int index) {
		assert(0<=index && index<=512);
		serverActivationIndex[server] = index;
	}
	// Get index for server activation.
	public int getServerIndex(int server) {
		int index = serverActivationIndex[server];
		assert(index != -1);
		return index;
	}
	// Add a set of consistent qubits.
	public void addConsistentQubits(Set<Integer> qubitSet) {
		consistentQubits.add(qubitSet);
	}
	// Get all consistent qubit sets.
	public List<Set<Integer>> getConsistentQubits() {
		// a mapping must contain at least one variable
		assert(!consistentQubits.isEmpty());
		return consistentQubits;
	}
	// Get weight with maximal absolute value
	public double getMaxAbsWeight(boolean considerSingleQubitWeights, boolean considerConnectionWeights) {
		double maxAbsWeight = 0;
		for (int i=0; i<512; ++i) {
			for (int j=0; j<512; ++j) {
				if (i==j) {
					if (considerSingleQubitWeights) {
						maxAbsWeight = Math.max(maxAbsWeight, Math.abs(getWeight(i)));	
					}
				} else if (qubitMatrix.isConnected(i, j)) {
					if (considerConnectionWeights) {
						maxAbsWeight = Math.max(maxAbsWeight, Math.abs(getConnectionWeight(i, j)));
					}
				}
			}
		}
		return maxAbsWeight;
	}
	// Get weight with minimal absolute value greater zero
	public double getMinAbsWeightGtZero(boolean considerSingleQubitWeights, boolean considerConnectionWeights) {
		double minAbsWeight = Double.POSITIVE_INFINITY;
		for (int i=0; i<512; ++i) {
			for (int j=0; j<512; ++j) {
				if (i==j) {
					if (considerSingleQubitWeights) {
						double weight = Math.abs(getWeight(i));
						if (weight > 0) {
							minAbsWeight = Math.min(minAbsWeight, weight);							
						}
					}
				} else if (qubitMatrix.isConnected(i, j)) {
					if (considerConnectionWeights) {
						double weight = Math.abs(getConnectionWeight(i, j));
						if (weight > 0) {
							minAbsWeight = Math.min(minAbsWeight, weight);							
						}
					}
				}
			}
		}
		return minAbsWeight;
	}
}
