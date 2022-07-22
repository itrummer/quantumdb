package raw_material.dwave.variables;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import raw_material.consolidation.dwave.ConsolidationMappingGeneric;
import raw_material.dwave.QubitMatrix;
import raw_material.util.MapperUtil;

// A logical problem variable that can be represented by multiple physical qubits. 
public class LogicalVariable {
	
	public LogicalVariable(QubitMatrix qubitMatrix) {
		this.qubitMatrix = qubitMatrix;
	}
	
	public final QubitMatrix qubitMatrix;
	public Set<Integer> qubits = new HashSet<Integer>();
	// Distribute corresponding weight over assigned qubits
	public void addWeight(ConsolidationMappingGeneric mapping, double weight) {
		assert(!qubits.isEmpty());
		// assume that we can add weight to any of the physical qubits
		int qubit = qubits.iterator().next();
		mapping.addWeight(qubit, qubit, weight);
	}
	// Obtain accumulated weight of all qubits representing this variable.
	public double getWeight(ConsolidationMappingGeneric mapping) {
		double result = 0;
		for (int qubit : qubits) {
			double weight = mapping.getWeight(qubit);
			result += weight;
		}
		return result;
	}
	// Distribute corresponding weights over connections with other variable.
	public void addConnectionWeight(ConsolidationMappingGeneric mapping, double weight, LogicalVariable otherVariable) {
		// assume that we can add weight to any pair of connected qubits
		int[] connectedQubits = qubitMatrix.connectedQubits(qubits, otherVariable.qubits);
		mapping.addWeight(connectedQubits[0], connectedQubits[1], weight);
	}
	// Obtain accumulated weight between qubits representing this variable and another variable.
	public double getConnectionWeight(ConsolidationMappingGeneric mapping, LogicalVariable otherVar) {
		// assert that qubit sets do not overlap
		for (int qubit1 : qubits) {
			assert(!otherVar.qubits.contains(qubit1));
		}
		// accumulate connection weight
		double result = 0;
		for (int qubit1 : qubits) {
			for (int qubit2 : otherVar.qubits) {
				if (qubitMatrix.isConnected(qubit1, qubit2)) {
					double weight = mapping.getConnectionWeight(qubit1, qubit2);
					result += weight;
				}
			}
		}
		return result;
	}
	// Counts the number of value changes between consecutive vector indices.
	static int nrValueChanges(int[] valueVector) {
		int result 		= 0;
		int nrValues 	= valueVector.length;
		int lastValue	= valueVector[0];
		for (int i=1; i<nrValues; ++i) {
			int curValue = valueVector[i];
			if (curValue != lastValue) {
				++result;
				lastValue = curValue;
			}
		}
		return result;
	}
	// Interprets input vector as binary number and counts one up.
	// We assume that lower weight digits are at the left contrary
	// to the convention (but this does not make a difference here).
	// We return true if an overflow occurred.
	static boolean countOneUpOverflow(int[] binaryVector) {
		int nrValues 		= binaryVector.length;
		boolean overflow 	= true;
		for (int i=0; i<nrValues; ++i) {
			int curValue = binaryVector[i];
			assert(curValue>=0 && curValue<=1);
			if (curValue == 0) {
				binaryVector[i] 	= 1;
				overflow 			= false;
				break;
			} else {
				binaryVector[i] = 0;
			}
		}
		return overflow;
	}
	// Calculate minimal scaling that guarantees that the minimum energy configuration
	// assigns all qubits representing this variable to the same value. This function
	// requires that all physical qubits form a chain.
	/*
	double calculateEqualityScalingForChain(ConsolidationMappingGeneric mapping) {
		// order qubits into a chain
		List<Integer> qubitChain = MapperUtil.qubitChain(qubits);
		// verify that there are currently no weights between all qubits
		int nrQubits = qubits.size();
		for (int chainIndex=0; chainIndex<nrQubits-1; ++chainIndex) {
			int qubit1 		= qubitChain.get(chainIndex);
			int qubit2 		= qubitChain.get(chainIndex+1);
			double weight	= mapping.getConnectionWeight(qubit1, qubit2);
			assert(weight == 0);
		}
		// calculate pessimistic estimate of minimal energy level for consistent assignment
		double allZeroEnergy	= 0;
		double allOneEnergy 	= 0;
		for (int qubit : qubits) {
			allZeroEnergy 	+= MapperUtil.pessimisticLocalEnergy(qubit, 0, mapping);
			allOneEnergy	+= MapperUtil.pessimisticLocalEnergy(qubit, 1, mapping);
		}
		// the local energy of each consistent assignment is calculated pessimistically
		// but at least we can choose whether the value is zero or one for all qubits.
		double consistentPessimistic = Math.min(allZeroEnergy, allOneEnergy);
		// calculate optimistic local energies for single qubits to speed up following steps
		double[][] optimisticLocal = new double[nrQubits][2];
		for (int qubitIndex=0; qubitIndex<nrQubits; ++qubitIndex) {
			int qubit = qubitChain.get(qubitIndex);
			for (int value=0; value<=1; ++value) {
				optimisticLocal[qubitIndex][value] = 
						MapperUtil.optimisticLocalEnergy(qubit, value, mapping);
			}
		}
		// enumerate all inconsistent assignment combinations and calculate the ratio
		// of optimistic energy gain over consistent assignments and number of
		// chain breaks (i.e., neighboring qubits with inconsistent values).
		double maxRatio 	= 0;
		int[] binaryVector 	= new int[nrQubits];
		Arrays.fill(binaryVector, 0);
		do {
			// calculate local energy and number of chain breaks for current vector
			double localEnergy = 0;
			for (int i=0; i<nrQubits; ++i) {
				int curValue	= binaryVector[i];
				localEnergy 	+= optimisticLocal[i][curValue];
			}
			// how much better is this than the consistent assignment?
			double gapToConsistent	= consistentPessimistic - localEnergy;
			int nrBreaks 			= nrValueChanges(binaryVector);
			if (nrBreaks > 0) {
				double ratio 	= gapToConsistent / nrBreaks;
				maxRatio		= Math.max(ratio, maxRatio);				
			}
		} while (!countOneUpOverflow(binaryVector));
		return maxRatio + MapperUtil.EPSILON_WEIGHT;
	}
	*/
	// Add weights to guarantee that all physical qubits will obtain the same value.
	// This function requires that all qubits form a chain.
	/*
	public void addEqualityWeightsForChain(ConsolidationMappingGeneric mapping) {
		double scaling = calculateEqualityScalingForChain(mapping);
		for (int qubit1 : qubits) {
			for (int qubit2 : qubits) {
				if (MapperUtil.isConnected(qubit1, qubit2)) {
					// As we treat each pair of connected qubits twice, 
					// we only add half of the equality weights each time.
					MapperUtil.addEqualityConstraint(qubit1, qubit2, scaling/2.0, mapping);
				}
			}
		}
	}
	*/
	// Calculate minimal scaling that guarantees that the minimum energy configuration
	// assigns all qubits representing this variable to the same value.
	double calculateEqualityScalingGeneric(ConsolidationMappingGeneric mapping) {
		// calculate pessimistic estimate of minimal energy level for consistent assignment
		double allZeroEnergy	= 0;
		double allOneEnergy 	= 0;
		for (int qubit : qubits) {
			allZeroEnergy 	+= MapperUtil.pessimisticLocalEnergy(qubit, 0, mapping);
			allOneEnergy	+= MapperUtil.pessimisticLocalEnergy(qubit, 1, mapping);
		}
		// the local energy of each consistent assignment is calculated pessimistically
		// but at least we can choose whether the value is zero or one for all qubits.
		double consistentPessimistic = Math.min(allZeroEnergy, allOneEnergy);
		// calculate optimistically minimal energy of inconsistent assignment
		double inconsistentOptimistic = 0;
		for (int qubit : qubits) {
			double zeroEnergy				= MapperUtil.optimisticLocalEnergy(qubit, 0, mapping);
			double oneEnergy				= MapperUtil.optimisticLocalEnergy(qubit, 1, mapping);
			double optimisticLocalEnergy 	= Math.min(zeroEnergy, oneEnergy);
			inconsistentOptimistic			+= optimisticLocalEnergy;
		}
		// how much energy can be maximally gained by using an inconsistent instead of a consistent assignment?
		double gap = consistentPessimistic - inconsistentOptimistic;
		// An inconsistent assignment breaks at least one equality constraint - equality weight must be
		// higher than the gap to make a consistent assignment more attractive than any inconsistent
		// assignment.
		return gap + MapperUtil.EPSILON_WEIGHT;
	}
	// Add weights to guarantee that all physical qubits will obtain the same value.
	/*
	public void addEqualityWeightsGeneric(ConsolidationMappingGeneric mapping) {
		double scaling = calculateEqualityScalingGeneric(mapping);
		for (int qubit1 : qubits) {
			for (int qubit2 : qubits) {
				if (MapperUtil.isConnected(qubit1, qubit2)) {
					// As we treat each pair of connected qubits twice, 
					// we only add half of the equality weights each time.
					MapperUtil.addEqualityConstraint(qubit1, qubit2, scaling/2.0, mapping);
				}
			}
		}
	}
	*/
}
