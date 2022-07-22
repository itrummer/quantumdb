package raw_material.dwave.basic_blocks;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import raw_material.util.MapperUtil;

// A block on the qubit matrix that is used to calculate the maximum of several inputs.
// The block is vertical on the qubit matrix with its output at the south side.
public class OneMaxBar extends QubitBlock {
	public final int nrInputs;						// how many inputs (that may integrate into 
													// different maxima)
	public final int nrOutputs;						// how many maxima are calculated
	public final boolean[] inputChains;				// which input chains are used (only two chains per cell)
	private final int[] inputQubits;				// indices of input qubits
	private final Set<Integer> unusableQubits;		// qubits that are already assigned or broken
	private final List<Set<Integer>> auxQubits;		// indices of auxiliary variable qubits
	private final int outputQubit;					// indicex of output qubit
	
	// Input parameters are the index of qubit on upper-left corner of qubit block, the number
	// of inputs, which input chains are used, and qubit indices that are used by other blocks.
	public OneMaxBar(int topLeftQubit, int nrInputs, boolean[] inputChains, Set<Integer> otherBlockQubits) {
		super(topLeftQubit);
		assert(inputChains.length == 2*nrInputs);
		this.nrInputs 		= nrInputs;
		this.nrOutputs 		= 1;
		this.inputChains	= inputChains;
		this.unusableQubits		= new HashSet<Integer>();
		this.unusableQubits.addAll(otherBlockQubits);
		this.unusableQubits.addAll(MapperUtil.DAMAGED_QUBITS);
		// calculate indices
		inputQubits 	= new int[nrInputs];
		auxQubits		= new ArrayList<Set<Integer>>(nrInputs-1);
		int halfCellCorner = topLeftQubit;	// top left qubit within current half-cell
		for (int inputCtr=0; inputCtr<nrInputs; ++inputCtr) {
			inputQubits[inputCtr] 		= inputQubit(halfCellCorner, inputCtr);
			Set<Integer> curAuxQubits 	= auxiliaryQubits(halfCellCorner, inputCtr);
			auxQubits.set(inputCtr, curAuxQubits);
			unusableQubits.addAll(curAuxQubits);
			// Do not go south in last iteration
			if (inputCtr<nrInputs-1) {
				halfCellCorner = MapperUtil.GoSouthHalf(halfCellCorner);				
			}
		}
		outputQubit	= auxQubits.get(nrInputs-1).iterator().next();
		// collect all qubits used by this structure
		for (int inputCtr=0; inputCtr<nrInputs; ++inputCtr) {
			qubits.add(inputQubits[inputCtr]);
			qubits.addAll(auxQubits.get(inputCtr));
		}
	}
	
	// Determine if input is represented by upper (or lower) qubit in
	// right column of current half cell.
	boolean upperInput(int inputCtr) {
		boolean upperInput = inputChains[inputCtr * 2];
		boolean lowerInput = inputChains[inputCtr * 2 + 1];
		assert(upperInput || lowerInput);
		assert(!upperInput || !lowerInput);
		return upperInput;
	}
	
	// Determine the input qubit within current half cell.
	int inputQubit(int halfCellCorner, int inputCtr) {
		int leftUpper	= halfCellCorner;
		int leftLower	= halfCellCorner + 1;
		int rightUpper	= MapperUtil.rightOpposite(leftUpper);
		int rightLower	= MapperUtil.rightOpposite(leftLower);
		if (upperInput(inputCtr)) {
			return rightUpper;
		} else {
			return rightLower;
		}		
	}
	
	// Determine auxiliary qubit in right column of current half cell.
	int rightAuxQubit(int halfCellCorner, int inputCtr) {
		int leftUpper	= halfCellCorner;
		int leftLower	= halfCellCorner + 1;
		int rightUpper	= MapperUtil.rightOpposite(leftUpper);
		int rightLower	= MapperUtil.rightOpposite(leftLower);
		if (upperInput(inputCtr)) {
			return rightLower;
		} else {
			return rightUpper;
		}
	}
	
	// Determine auxiliary qubit in left column of current half cell.
	int leftAuxQubit(int halfCellCorner, int inputCtr) {
		int leftUpper	= halfCellCorner;
		int leftLower	= halfCellCorner + 1;
		// Must take into account that one of the two left qubits was already used.
		assert(!unusableQubits.contains(leftUpper) || !unusableQubits.contains(leftLower));
		if (unusableQubits.contains(leftUpper)) {
			return leftLower;
		} else {
			return leftUpper;
		}
	}
	
	// Returns connecting auxiliary qubit to cell below of -1 if not required.
	int connectingAuxQubit(int halfCellCorner, int inputCtr) {
		// In lower half: must connect to the cell below if this is not the last cell
		boolean lowerHalf 	= !MapperUtil.isUpperLeftInCell(halfCellCorner);
		boolean moreInputs 	= (inputCtr < nrInputs-1);
		if (lowerHalf && moreInputs) {
			int leftAuxQubit = leftAuxQubit(halfCellCorner, inputCtr);
			int connectingAuxQubit = MapperUtil.GoSouth(leftAuxQubit, 1);
			return connectingAuxQubit;
		} else {
			return -1;
		}
	}
	
	// Determine auxiliary qubits within half cell.
	Set<Integer> auxiliaryQubits(int halfCellCorner, int inputCtr) {
		// determine auxiliary qubits (not required for first input)
		Set<Integer> auxSet = new HashSet<Integer>();
		auxQubits.add(auxSet);
		// Add auxiliary qubit in right column (non-input qubit).
		int rightAuxQubit = rightAuxQubit(halfCellCorner, inputCtr);
		auxSet.add(rightAuxQubit);
		// Add auxiliary qubit in left column.
		int leftAuxQubit = leftAuxQubit(halfCellCorner, inputCtr);
		auxSet.add(leftAuxQubit);
		// Add connection to cell below if required.
		int connectingAuxQubit = connectingAuxQubit(halfCellCorner, inputCtr);
		if (connectingAuxQubit != -1) {
			auxSet.add(connectingAuxQubit);
		}
		return auxSet;
	}
	
	// Returns qubit index of i-th input.
	public int getInput(int i) {
		return inputQubits[i];
	}
	
	// Returns qubit index of only output.
	public int getOutput() {
		return outputQubit;
	}
	
	// Returns indices of qubits representing i-th auxiliary qubit (starting from index zero)
	// combining the i+1-th input qubit with maximum of 0-th to i-th input.
	public Set<Integer> getAuxiliaries(int i) {
		return auxQubits.get(i);
	}
}
