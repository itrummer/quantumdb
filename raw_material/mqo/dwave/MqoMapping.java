package raw_material.mqo.dwave;

import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.Serializable;

/**
 * Represents a mapping of a MQO problem instance into a QUBO (Quadratic Unconstrained Binary Optimization) 
 * problem. This mapping can represent the case that the same result is shared between N query plans.
 * 
 * @author immanueltrummer
 *
 */
public class MqoMapping implements Serializable {
	/**
	 * Used to verify the class version.
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * The number of (broken or intact) qubits.
	 */
	final int nrQubits = 1152;
	/**
	 * Stores for each qubit and each pair of qubits the associated coupling weight.
	 */
	final double[][] weights = new double[nrQubits][nrQubits];
	/**
	 * Maps variables representing plan executions to qubit indices.
	 */
	public VarMapping[][] planVars;
	/**
	 * Maps variables representing intermediate result creation to qubit indices.
	 */
	public VarMapping[] resultVars;
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
		int min_index = Math.min(qubit1, qubit2);
		int max_index = Math.max(qubit1, qubit2);
		return weights[min_index][max_index];
	}
	/**
	 * Adds weight to the corresponding qubit or coupling
	 * 
	 * @param qubit1		index of first qubit
	 * @param qubit2		index of second qubit
	 * @param addedWeight	the weight to add to the one currently set
	 */
	public void addWeight(int qubit1, int qubit2, double addedWeight) {
		// we only use the lower triangle of the weight matrix
		int min_index = Math.min(qubit1, qubit2);
		int max_index = Math.max(qubit1, qubit2);
		weights[min_index][max_index] += addedWeight;
	}
	/**
	 * Write current weights into a file (from where they can be read by a program
	 * that communicates with the D-Wave hardware at NASA Ames research).
	 * 
	 * @param filename		the name of the output file
	 * @param description	header line contains a description
	 * @throws Exception
	 */
	public void weightsToFile(String filename, String description) throws Exception {
		PrintWriter writer = new PrintWriter(filename);
		writer.println(description);
		for (int i = 0; i < nrQubits; ++i) {
			for (int j = i; j < nrQubits; ++j) {
				if (weights[i][j] != 0) {
					writer.println(i + "," + j + "," + weights[i][j]);
				}
			}
		}
		writer.close();
	}
	/**
	 * Write current mapping (the weights as well as the semantic for each qubit) to a file.
	 * 
	 * @param filename		the name of the file to create
	 * @throws Exception
	 */
	public void toFile(String filename) throws Exception {
		FileOutputStream fout = new FileOutputStream(filename);
		ObjectOutputStream oos = new ObjectOutputStream(fout);   
		oos.writeObject(this);
		oos.close();
	}
}
