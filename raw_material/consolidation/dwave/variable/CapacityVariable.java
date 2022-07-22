package raw_material.consolidation.dwave.variable;

import raw_material.dwave.QubitMatrix;
import raw_material.dwave.variables.LogicalVariable;


// Represents logical capacity variable with corresponding capacity.
public class CapacityVariable extends LogicalVariable {
	public final double capacity;
	public CapacityVariable(QubitMatrix qubitMatrix, double capacity) {
		super(qubitMatrix);
		this.capacity = capacity;
	}
}
