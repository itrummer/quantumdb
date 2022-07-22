package raw_material.dwave;

import java.util.Set;

/**
 * Describes the properties of the qubit matrix to which problems are mapped - the qubit matrix
 * can either represent a physical qubit matrix on the D-Wave hardware or a virtual qubit matrix
 * (e.g., we use a fully connected virtual matrix to generate QUBO problems).
 * 
 * @author immanueltrummer
 *
 */
public abstract class QubitMatrix {
	/**
	 * Obtain number of available qubits.
	 * 
	 * @return	the number of qubits (counting broken and intact qubits) available on this matrix
	 */
	public abstract int getNrQubits();
	/**
	 * Returns the minimal admissible weight for couplings and single qubits.
	 * 
	 * @return	the minimal weight
	 */
	public abstract double getMinWeight();
	/**
	 * Returns the maximal admissible weight for couplings and single qubits.
	 * 
	 * @return	the maximal weight
	 */
	public abstract double getMaxWeight();
	/**
	 * Checks if two given qubit indices are connected in the matrix, meaning that we
	 * can specify non-zero coupling weights between them.
	 * 
	 * @param qubit1	index of first qubit
	 * @param qubit2	index of second qubit
	 * @return			true if the qubits are connected and false otherwise
	 */
	public abstract boolean isConnected(int qubit1, int qubit2);
	
	
	// Returns the indices of two connected qubits in the Chimera graph such that the first
	// qubit belongs to the first group and the second to the second group.
	public int[] connectedQubits(Set<Integer> group_1, Set<Integer> group_2) {
		for (int qubit_1 : group_1) {
			for (int qubit_2 : group_2) {
				if (isConnected(qubit_1, qubit_2)) {
					return new int[]{qubit_1, qubit_2};
				}
			}
		}
		return null;
	}
	
}
