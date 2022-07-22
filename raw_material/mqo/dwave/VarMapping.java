package raw_material.mqo.dwave;

import java.io.Serializable;

/**
 * Represents a problem variable in a QUBO problem and maps the variable to the
 * index of the qubit that it is represented with.
 * 
 * @author immanueltrummer
 *
 */
public class VarMapping implements Serializable {
	/**
	 * Used to verify the class version.
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * The index of the qubit that represents the variable
	 */
	final public int qubit;
	
	public VarMapping(int qubit) {
		this.qubit =  qubit;
	}
	
	public void addWeight(MqoMapping mapping, double addedWeight) {
		mapping.addWeight(qubit, qubit, addedWeight);
	}
	
	public void addWeight(MqoMapping mapping, VarMapping otherVar, double addedWeight) {
		mapping.addWeight(qubit, otherVar.qubit, addedWeight);
	}
	
	public double getWeight(MqoMapping mapping) {
		return mapping.getWeight(qubit);
	}
	
	public double getWeight(MqoMapping mapping, VarMapping otherVar) {
		return mapping.getConnectionWeight(qubit, otherVar.qubit);
	}
}
