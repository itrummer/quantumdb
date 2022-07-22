package raw_material.dwave;

import java.io.PrintWriter;

import raw_material.dwave.adjacency.DwaveMatrix;

/**
 * Stores a problem mapping onto a qubit matrix, meaning that it stores for each
 * single qubit and each pair of qubits the coupling weight.
 * 
 * @author immanueltrummer
 *
 */
public class Mapping {
	/**
	 * Describes the matrix of qubits to which this mapping refers, thereby specifying for instance
	 * the number of available qubits and the adjacency between qubits.
	 */
	public final QubitMatrix qubitMatrix;
	/**
	 * The maximal value for coupling weights - we check that the weight for no single qubit and no
	 * pairs of qubits never exceeds that value if assertions are activated.
	 */
	double MAX_WEIGHT = Double.POSITIVE_INFINITY;
	/**
	 * The minimal value for coupling weights - we check that the weight for no single qubit and no
	 * pairs of qubits never decreases below that value if assertions are activated.
	 */
	double MIN_WEIGHT = Double.NEGATIVE_INFINITY;
	/**
	 * Stores for each qubit and each pair of qubits the associated coupling weight.
	 */
	protected final double[][] weights;
	
	public Mapping(QubitMatrix qubitMatrix) {
		this.qubitMatrix = qubitMatrix;
		int nrQubits = qubitMatrix.getNrQubits();
		 weights = new double[nrQubits][nrQubits];
	}
	/**
	 * Returns the weight associated with the given qubit.
	 * 
	 * @param qubit	index of the qubit whose weight is returned
	 * @return		the weight assigned to the given qubit
	 */
	public double getWeight(int qubit) {
		return weights[qubit][qubit];
	}
	/**
	 * Returns the weight between two qubits, automatically uses smaller index as first index
	 * 
	 * @param qubit1	index of first qubit
	 * @param qubit2	index of second qubit
	 * @return			the coupling weight between the given qubits
	 */
	public double getConnectionWeight(int qubit1, int qubit2) {
		assert qubitMatrix.isConnected(qubit1, qubit2) : "Asked for weight between unconnected qubits!";
		int min_index = Math.min(qubit1, qubit2);
		int max_index = Math.max(qubit1, qubit2);
		return weights[min_index][max_index];
	}
	/**
	 * Returns all weight assignments.
	 * 
	 * @return	a matrix indicating for each single qubit and each pair of qubits the associated weight
	 */
	public double[][] weights() {
		return weights;
	}
	/**
	 * Adds weight to the corresponding qubit or coupling
	 * 
	 * @param qubit1		index of first qubit
	 * @param qubit2		index of second qubit
	 * @param addedWeight	the weight to add to the one currently set
	 */
	public void addWeight(int qubit1, int qubit2, double addedWeight) {
		assert(qubitMatrix.isConnected(qubit1, qubit2) || qubit1 == qubit2);
		// we only use the lower triangle of the weight matrix
		int min_index = Math.min(qubit1, qubit2);
		int max_index = Math.max(qubit1, qubit2);
		weights[min_index][max_index] += addedWeight;
		assert weights[min_index][max_index] <= MAX_WEIGHT : "Weight too big!";
		assert weights[min_index][max_index] >= MIN_WEIGHT : "Weight too small!";
	}
	/**
	 * Write current weights into a file (from where they can be read by a program
	 * that communicates with the D-Wave hardware at NASA Ames research).
	 * 
	 * @param filename
	 * @param description
	 * @throws Exception
	 */
	public void ToFile(String filename, String description) throws Exception {
		PrintWriter writer = new PrintWriter(filename);
		writer.println(description);
		for (int i = 0; i < qubitMatrix.getNrQubits(); ++i) {
			for (int j = i; j < qubitMatrix.getNrQubits(); ++j) {
				if (weights[i][j] != 0) {
					writer.println(i + " " + j + " " + weights[i][j]);
				}
			}
		}
		writer.close();
	}
	/**
	 * Generates ASCI representation of the weights currently set and outputs them to the console.
	 */
	public void ToConsoleForDebug() {
		// Currently output is only implemented for mappings that refer to the Dwave matrix
		if (qubitMatrix instanceof DwaveMatrix) {
			int qubit_index;
			// output weights on and between qubits
			System.out.println("Variable weights");
			System.out.println("LEGEND: N: north; E: east; S: south; W: west");
			for (int cell_y=0; cell_y<8; ++cell_y) {
				for (int row_y=0; row_y<4; ++row_y) {
					for (int cell_x=0; cell_x<8; ++cell_x) {
						for (int cell_colon=0; cell_colon<2; ++cell_colon) {
							qubit_index = cell_y * 64 + row_y + cell_x * 8 + cell_colon * 4;
							// qubit weight and inter-cell connections
							// System.out.print(qubit_variable_type[qubit_index] + " ");
							System.out.print(weights[qubit_index][qubit_index]);
							System.out.print("\t");
							if (cell_colon == 0) {
								System.out.print("[");
								if (DwaveMatrix.CanGoNorth(qubit_index)) {
									int connected_qubit = DwaveMatrix.GoNorth(qubit_index, 1);
									double weight = getConnectionWeight(qubit_index, connected_qubit);
									System.out.print("N" + weight + "\t");
								} else {
									System.out.print("\t");
								}
								if (DwaveMatrix.CanGoSouth(qubit_index)) {
									int connected_qubit = DwaveMatrix.GoSouth(qubit_index, 1);
									double weight = getConnectionWeight(qubit_index, connected_qubit);
									System.out.print("S" + weight + "\t");
								} else {
									System.out.print("\t");
								}
								System.out.print("]\t");
								System.out.print("[");
								// get qubit on top of right colon
								int top_right = qubit_index - (qubit_index % 4) + 4;
								for (int connected_qubit = top_right;
										connected_qubit < top_right + 4; ++connected_qubit) {
									double weight = getConnectionWeight(qubit_index, connected_qubit);
									System.out.print(weight + "\t");
								} 
								System.out.print("]\t");
							} else {
								System.out.print("[");
								if (DwaveMatrix.CanGoEast(qubit_index)) {
									int connected_qubit = DwaveMatrix.GoEast(qubit_index, 1);
									double weight = getConnectionWeight(qubit_index, connected_qubit);
									System.out.print("E" + weight + "\t");
								} else {
									System.out.print("\t");
								}
								if (DwaveMatrix.CanGoWest(qubit_index)) {
									int connected_qubit = DwaveMatrix.GoWest(qubit_index, 1);
									double weight = getConnectionWeight(qubit_index, connected_qubit);
									System.out.print("W" + weight + "\t");
								} else {
									System.out.print("\t");
								}
								System.out.print("]\t");
							}	
						} // for cell_colon
						System.out.print("\t\t");
					} // for unit cell on x axis
					System.out.println("|");
				}
				System.out.println();
			}
		}
	}
}
