package raw_material.util;

import java.util.*;

import raw_material.consolidation.dwave.ConsolidationMappingGeneric;
import raw_material.consolidation.testcase.ConsolidationProblem;
import raw_material.dwave.variables.LogicalVariable;

public class MapperUtil {
	// tolerance for equality when comparing two double values
	public final static double DOUBLE_TOLERANCE = 1E-10;
	// contains indices of damaged qubits
	public final static Set<Integer> DAMAGED_QUBITS = new HashSet<Integer>(Arrays.asList(35, 154, 410));
	// use this value to make a weight bigger than another weight
	public final static double EPSILON_WEIGHT = 1/8.0;
	// maximum capacity represented by one capacity variable
	public final static double MAX_CAPACITY_PER_VAR = 4.0;
	
	/////////////////////////////
	// Moving on the Qubit Matrix
	/////////////////////////////

	// Walks south **half** a cell.
	public static int GoSouthHalf(int index) {
		int innerCellOffset = index % 8;
		if (innerCellOffset < 2) {
			// upper left half
			return index + 2;
		} else if (innerCellOffset < 4) {
			// lower left half
			return GoSouth(index, 1) - 2;
		} else if (innerCellOffset < 6) {
			// upper right half
			return index + 2;
		} else {
			// lower right half
			return GoSouth(index, 1) - 2;
		}
	}
	
	// Walks south several **half** cells.
	public static int GoSouthHalf(int start, int nrSteps) {
		int curPos = start;
		for (int stepCtr=0; stepCtr<nrSteps; ++stepCtr) {
			curPos = GoSouthHalf(curPos);
		}
		return curPos;
	}
	
	// Walks south couting single qubit rows.
	public static int GoSouthQubitwise(int start, int nrSteps) {
		int stop = start;
		for (int stepCtr=0; stepCtr<nrSteps; ++stepCtr) {
			if (isAtBottom(stop)) {
				stop += 61;
			} else {
				++stop;
			}
		}
		assert(stop<512);
		return stop;
	}
	
	// Transforms index to get to the unit cell above the current one
	public static int GoNorth(int start, int nr_steps) {
		int curPos = start;
		for (int stepCtr=0; stepCtr<nr_steps; ++stepCtr) {
			assert(CanGoNorth(curPos));
			curPos -= 64;
		}
		return curPos; 
	}
	
	// Transforms index to get to the unit cell right of the current one
	public static int GoEast(int start, int nr_steps) {
		int curPos = start;
		for (int stepCtr=0; stepCtr<nr_steps; ++stepCtr) {
			assert(CanGoEast(curPos));
			curPos += 8;
		}
		return curPos;
	}
	
	// Transforms index to get to the unit cell below the current one
	public static int GoSouth(int start, int nr_steps) {
		int curPos = start;
		for (int stepCtr=0; stepCtr<nr_steps; ++stepCtr) {
			assert(CanGoSouth(curPos));
			curPos += 64;
		}
		return curPos; 
	}
	
	// Transforms index to get to the unit cell left of the current one
	public static int GoWest(int start, int nr_steps) {
		int curPos = start;
		for (int stepCtr=0; stepCtr<nr_steps; ++stepCtr) {
			assert(CanGoWest(curPos));
			curPos -= 8;
		}
		return curPos; 
	}
	
	// Check whether there is a unit cell above the qubit
	public static boolean CanGoNorth(int index) {
		return index >= 64;
	}
	
	// Check whether there is a unit cell to the right of the qubit
	public static boolean CanGoEast(int index) {
		return (index % 64) <= 55;
	}
	
	// Check whether there is a unit cell to the left of the qubit
	public static boolean CanGoSouth(int index) {
		return index < 448;
	}

	// Check whether there is a unit cell below the qubit
	public static boolean CanGoWest(int index) {
		return (index % 64) >= 8;
	}
	
	/////////////////////////////////////////////////////////
	// Functions for determining position on the Qubit matrix
	/////////////////////////////////////////////////////////
	
	// Checks whether the qubit index is at the bottom row of some cell.
	public static boolean isAtBottom(int qubit) {
		return (qubit % 8 == 3) || (qubit % 8 == 7);
	}
	
	// Checks whether the qubit is in the left colon of a unit cell
	public static boolean isLeftCellColon(int index) {
		return (index % 8) < 4;
	}
	
	// verify whether given qubit index is at upper-left corner of its cell
	public static boolean isUpperLeftInCell(int index) {
		return (index % 8) == 0;
	}
	
	// Returns the cell index of the unit cell the qubit belongs to - cells are counted in row major 
	// order starting from zero.
	public static int cellIndex(int qubit_index) {
		return (int)Math.floor(qubit_index / (double)8.0);
	}
	
	/////////////////////////////////////////////////////////
	// Functions checking connections on the Chimera graph
	/////////////////////////////////////////////////////////

	// Check whether two qubits are in the same cell
	public static boolean isInSameCell(int qubit_1, int qubit_2) {
		if (cellIndex(qubit_1) == cellIndex(qubit_2)) {
			return true;
		} else {
			return false;
		}
	}
	
	////////////////////////////////////////////////
	// Functions returning qubits in a specific cell
	////////////////////////////////////////////////

	// return indices of all qubits in cell for given top-left qubit
	public static Set<Integer> allQubitsInCell(int topLeftQubit) {
		assert(isUpperLeftInCell(topLeftQubit));
		Set<Integer> result = new HashSet<Integer>();
		for (int offset=0; offset<8; ++offset) {
			result.add(topLeftQubit+offset);
		}
		return result;
	}
		
	// Returns index of qubit in right column within same cell on same height.
	public static int rightOpposite(int index) {
		assert(isLeftCellColon(index));
		return index + 4;
	}
	
	// Returns the index of the qubit at the top left corner of the current cell.
	public static int cornerQubit(int qubit) {
		return qubit - (qubit % 8);
	}
	
	// Returns all qubits in left colon of current cell.
	public static TreeSet<Integer> leftColon(int qubit) {
		int cornerQubit 	= cornerQubit(qubit);
		TreeSet<Integer> result = new TreeSet<Integer>();
		for (int leftQubit=cornerQubit; leftQubit<cornerQubit+4; ++leftQubit) {
			result.add(leftQubit);
		}
		return result;
	}
	
	// Returns all qubits in right colon of current cell.
	public static TreeSet<Integer> rightColon(int qubit) {
		int cornerQubit 	= cornerQubit(qubit);
		TreeSet<Integer> result = new TreeSet<Integer>();
		for (int rightQubit=cornerQubit+4; rightQubit<cornerQubit+8; ++rightQubit) {
			result.add(rightQubit);
		}
		return result;
	}
	
	////////////////////////////////////////////////////////////////
	// Functions returning qubits that are connected to other qubits
	////////////////////////////////////////////////////////////////

	// Returns all qubits that are connected in the Chimera graph (also broken ones).
	public static Set<Integer> connectedInChimera(int qubit) {
		Set<Integer> result = new HashSet<Integer>();
		// add qubits outside current cell
		boolean inLeftColon = isLeftCellColon(qubit);
		if (inLeftColon) {
			if (CanGoNorth(qubit)) {
				result.add(GoNorth(qubit, 1));
			}
			if (CanGoSouth(qubit)) {
				result.add(GoSouth(qubit, 1));
			}			
		} else {
			if (CanGoEast(qubit)) {
				result.add(GoEast(qubit, 1));
			}
			if (CanGoWest(qubit)) {
				result.add(GoWest(qubit, 1));
			}
		}
		// add qubits in current cell (all qubits in opposite colon)
		if (inLeftColon) {
			result.addAll(rightColon(qubit));
		} else {
			result.addAll(leftColon(qubit));
		}
		return result;
	}

	/*
	// Returns the indices of two connected qubits in the Chimera graph such that the first
	// qubit belongs to the first group and the second to the second group.
	public static int[] connectedQubits(Set<Integer> group_1, Set<Integer> group_2) {
		for (int qubit_1 : group_1) {
			for (int qubit_2 : group_2) {
				if (isConnected(qubit_1, qubit_2)) {
					return new int[]{qubit_1, qubit_2};
				}
			}
		}
		return null;
	}
	
	// Convenience method wrapping a single qubit into a set before searching for connection.
	// Returns a connected qubit in the set (different from the single input qubit).
	public static int connectedQubit(int singleQubit, Set<Integer> qubitSet) {
		Set<Integer> wrappedQubit = new HashSet<Integer>();
		wrappedQubit.add(singleQubit);
		int[] connectedQubits = connectedQubits(wrappedQubit, qubitSet);
		assert(connectedQubits[0] == singleQubit || connectedQubits[1] == singleQubit);
		assert(connectedQubits[0] != singleQubit || connectedQubits[1] != singleQubit);
		if (connectedQubits[0] == singleQubit) {
			return connectedQubits[1];
		} else {
			return connectedQubits[0];
		}
	}
	*/
	
	////////////////////////////////////////////////////////////////////
	// Functions adding standard constraints between variables or qubits
	////////////////////////////////////////////////////////////////////

	// Add constraint making output the maximum of the two inputs; scaled by the specified factor.
	public static void addMaxConstraint(LogicalVariable input1, LogicalVariable input2, 
			LogicalVariable output, double scaling, ConsolidationMappingGeneric mapping) {
		// Each qubit is weighted by 1.
		input1.addWeight(mapping, scaling);
		input2.addWeight(mapping, scaling);
		output.addWeight(mapping, scaling);
		// The connection between inputs is weighted by 1.
		input1.addConnectionWeight(mapping, scaling, input2);
		// The connection between input and output is weighted by -2.
		input1.addConnectionWeight(mapping, -2*scaling, output);
		input2.addConnectionWeight(mapping, -2*scaling, output);
	}
	
	// Add constraint making the two qubits take the same value; if not then the energy level
	// increases by the scaling factor.
	public static void addEqualityConstraint(LogicalVariable var1, LogicalVariable var2, 
			double scaling, ConsolidationMappingGeneric mapping) {
		var1.addWeight(mapping, scaling);
		var2.addWeight(mapping, scaling);
		var1.addConnectionWeight(mapping, -2*scaling, var2);
	}
	
	// Add equality constraint between physical qubits.
	public static void addEqualityConstraint(int qubit1, int qubit2, 
			double scaling, ConsolidationMappingGeneric mapping) {
		mapping.addWeight(qubit1, qubit1, scaling);
		mapping.addWeight(qubit2, qubit2, scaling);
		mapping.addWeight(qubit1, qubit2, -2*scaling);
	}
	
	//////////////////
	// Other functions
	//////////////////
	
	// calculate number of required capacity variables - must be consistent with
	// functions generating capacity variables!
	public static int nrCapacityVars(ConsolidationProblem problem, double capacity) {
		double remainingCapacity	= capacity;
		double capacityPerVar		= problem.minCapacityStep;
		int nrVars 					= 0;
		while (remainingCapacity>DOUBLE_TOLERANCE) {
			++nrVars;
			double newVarCapacity 	= Math.min(remainingCapacity, capacityPerVar);
			remainingCapacity 		-= newVarCapacity;
			capacityPerVar 			= Math.min(capacityPerVar * 2, MapperUtil.MAX_CAPACITY_PER_VAR);
		}
		return nrVars;
	}
	
	// rounds up to the next multiple of four
	public static int roundUpFour(int nrChains) {
		if (nrChains % 4 == 0) {
			return nrChains;
		} else {
			return nrChains + (4 - (nrChains % 4));
		}
	}
	
	// checks whether all qubits belonging to a group are intact
	public static boolean isGroupIntact(Set<Integer> qubit_group) {
		for (int qubit_index : qubit_group) {
			if (DAMAGED_QUBITS.contains(qubit_index)) {
				return false;
			}
		}
		return true;
	}
	
	/*
	
	// Count the number of neighbors (i.e., directly connected qubits)
	// of one qubit in a given qubit set.
	public static int nrNeighbors(int qubit, Set<Integer> qubitSet) {
		int nrNeighbors = 0;
		for (int neighborCandidate : qubitSet) {
			if (neighborCandidate != qubit) {
				if (isConnected(qubit, neighborCandidate)) {
					++nrNeighbors;
				}
			}
		}
		return nrNeighbors;
	}
	
	// Order a set of connected qubits into a chain; starting from one arbitrary end
	// of the chain and such that consecutive qubits are connected.
	public static List<Integer> qubitChain(Set<Integer> qubitSet) {
		assert(qubitSet.size()>0);
		List<Integer> result = new LinkedList<Integer>();
		if (qubitSet.size()==1) {
			// Treat special case of one single qubit.
			result.add(qubitSet.iterator().next());
		} else {
			// Search one end of the chain which is a qubit that is only connected
			// to one (instead of two) other qubits.
			int chainEnd = -1;
			for (int chainEndCandidate : qubitSet) {
				if (nrNeighbors(chainEndCandidate, qubitSet) == 1) {
					chainEnd = chainEndCandidate;
					break;
				}
			}
			assert(chainEnd != -1);
			// Starting from end of chain, keep retrieving neighbor of current qubit,
			// insert into list, and continue with neighbor as current qubit.
			Set<Integer> remainingQubits = new HashSet<Integer>();
			remainingQubits.addAll(qubitSet);
			int curQubit = chainEnd;
			while (remainingQubits.size()>1) {
				result.add(curQubit);
				remainingQubits.remove(curQubit);
				curQubit = connectedQubit(curQubit, remainingQubits);
			}
			if (remainingQubits.size()>=1) {
				result.add(remainingQubits.iterator().next());
			}
		}
		return result;
	}
		*/
	
	// Pessimistic estimate of local energy for one specific qubit when assigning one
	// specific value (counting the weight of the qubit itself and of all its neighbors
	// in the Chimera graph).
	public static double pessimisticLocalEnergy(int qubit, int value, ConsolidationMappingGeneric mapping) {
		assert(value>=0 && value<=1);
		double energy = 0;
		// consider weight of qubit itself
		energy += value * mapping.getWeight(qubit);
		// consider weights of connections to other qubits
		Set<Integer> neighbors = connectedInChimera(qubit);
		for (int neighbor : neighbors) {
			double connectionWeight = mapping.getConnectionWeight(qubit, neighbor);
			// Assume both possible neighbor values and take maximum as pessimistic for estimate.
			double energyDelta1		= value * 0 * connectionWeight;
			double energyDelta2		= value * 1 * connectionWeight;
			double pessimisticDelta	= Math.max(energyDelta1, energyDelta2);
			energy += pessimisticDelta;
		}
		return energy;
	}
	
	// Optimistic estimate of local energy for one specific qubit when assigning one
	// specific value (counting the weight of the qubit itself and of all its neighbors
	// in the Chimera graph).
	public static double optimisticLocalEnergy(int qubit, int value, ConsolidationMappingGeneric mapping) {
		assert(value>=0 && value<=1);
		double energy = 0;
		// consider weight of qubit itself
		energy += value * mapping.getWeight(qubit);
		// consider weights of connections to other qubits
		Set<Integer> neighbors = connectedInChimera(qubit);
		for (int neighbor : neighbors) {
			double connectionWeight = mapping.getConnectionWeight(qubit, neighbor);
			// Assume both possible neighbor values and take maximum as pessimistic for estimate.
			double energyDelta1		= value * 0 * connectionWeight;
			double energyDelta2		= value * 1 * connectionWeight;
			double optimisticDelta	= Math.min(energyDelta1, energyDelta2);
			energy += optimisticDelta;
		}
		return energy;
	}
	
	// Generate a list with capacity values such that sum of elements can represent all possible
	// capacity consumption values up to the specified value for the given problem.
	public static List<Double> capacityValues(ConsolidationProblem problem, double sumCapacity) {
		List<Double> result = new LinkedList<Double>();
		double remainingCapacity 	= sumCapacity;
		double capacityPerVar		= problem.minCapacityStep;	// minimum tenant capacity > 0
		while (remainingCapacity > 0) {
			double newVarCapacity 	= Math.min(remainingCapacity, capacityPerVar);
			remainingCapacity 		-= newVarCapacity;
			capacityPerVar 			= Math.min(capacityPerVar * 2, MapperUtil.MAX_CAPACITY_PER_VAR);
			result.add(newVarCapacity);
		}
		// Make sure that sum over the capacities of all freshly added variables
		// yields indeed the server capacity.
		double accumulatedCapacity = 0;
		for (double capacity : result) {
			accumulatedCapacity += capacity;
		}
		double error = Math.abs(accumulatedCapacity - sumCapacity);
		assert(error < MapperUtil.DOUBLE_TOLERANCE);
		return result;
	}
	
	// asserts that there is no overlap between the two sets of qubits
	public static void assertNoOverlap(Set<Integer> qubitSet1, Set<Integer> qubitSet2) {
		for (int qubit1 : qubitSet1) {
			assert !qubitSet2.contains(qubit1) : "Qubit " + qubit1 + " in set " + qubitSet2;
		}
	}
}
