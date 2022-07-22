package raw_material.dwave.adjacency;

import raw_material.dwave.QubitMatrix;

public class FullyConnected extends QubitMatrix {

	@Override
	public double getMinWeight() {
		return Double.NEGATIVE_INFINITY;
	}

	@Override
	public double getMaxWeight() {
		return Double.POSITIVE_INFINITY;
	}

	@Override
	public boolean isConnected(int qubit1, int qubit2) {
		return true;
	}

	@Override
	public int getNrQubits() {
		return Integer.MAX_VALUE;
	}

}
