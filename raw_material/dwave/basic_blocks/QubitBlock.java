package raw_material.dwave.basic_blocks;

import java.util.HashSet;
import java.util.Set;

// A block of qubits that may extend over multiple cells.
public abstract class QubitBlock {
	public final int topLeftQubit;		// index of top left corner qubit
	public final Set<Integer> qubits;	// all qubits used by the block
	public QubitBlock(int topLeftQubit) {
		this.topLeftQubit = topLeftQubit;
		this.qubits = new HashSet<Integer>();
	}
}
