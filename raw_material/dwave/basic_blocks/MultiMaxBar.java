package raw_material.dwave.basic_blocks;

import java.util.Set;
import java.util.TreeSet;

import raw_material.util.MapperUtil;

// Bar-shaped structure on the qubit matrix calculating maxima of multiple input sets.
// We assume that there are no damaged qubits in the area occupied by this structure.
public class MultiMaxBar extends QubitBlock {
	final int nrInputGroups;
	final int nrInputsPerGroup;
	final boolean[][] inputChains;
	final int groupCellHeight;
	final int minGroupDistance;
	final Set<Integer>[][] inputQubits;
	final Set<Integer>[][] auxQubits;
	@SuppressWarnings("unchecked")
	public MultiMaxBar(int topLeftQubit, int nrInputGroups, int nrInputsPerGroup, 
			boolean[][] inputChains, int minGroupDistance) {
		super(topLeftQubit);
		this.nrInputGroups = nrInputGroups;
		this.nrInputsPerGroup = nrInputsPerGroup;
		this.inputChains = inputChains;
		this.minGroupDistance = minGroupDistance;
		this.groupCellHeight = (int)Math.ceil((nrInputsPerGroup * 2.0)/4.0);
		this.inputQubits = (Set<Integer>[][])new TreeSet[nrInputGroups][nrInputsPerGroup];
		this.auxQubits = (Set<Integer>[][])new TreeSet[nrInputGroups][nrInputsPerGroup];
		setInputQubits();
		setAuxQubits();
		assertConnections();
		assertNoOverlap();
	}
	// Calculate leftmost input qubit
	int leftmostInputQubit(int groupCtr, int inputCtr) {
		int groupOffsetY = Math.max(minGroupDistance, groupCellHeight) * groupCtr;
		int groupTopLeft = MapperUtil.GoSouth(topLeftQubit, groupOffsetY);
		assert(inputChains[groupCtr][2*inputCtr] || inputChains[groupCtr][2*inputCtr+1]);
		// calculate position of and add leftmost qubit representing input
		int offsetX 	= groupCellHeight - (inputCtr/2 + 1);
		int offsetY 	= inputChains[groupCtr][2*inputCtr] ? 2*inputCtr : 2*inputCtr+1;
		int leftmostQubit = groupTopLeft + 4;
		leftmostQubit = MapperUtil.GoEast(leftmostQubit, offsetX);
		leftmostQubit = MapperUtil.GoSouthQubitwise(leftmostQubit, offsetY);
		return leftmostQubit;
	}
	// Select qubits for representing inputs - each cell column is associated with at most two consecutive
	// input indices and contains the corresponding inputs for all groups.
	void setInputQubits() {
		for (int groupCtr=0; groupCtr<nrInputGroups; ++groupCtr) {
			for (int inputCtr=0; inputCtr<nrInputsPerGroup; ++inputCtr) {
				Set<Integer> curInputQubits = new TreeSet<Integer>();
				inputQubits[groupCtr][inputCtr] = curInputQubits;
				int leftmostQubit = leftmostInputQubit(groupCtr, inputCtr);
				curInputQubits.add(leftmostQubit);
				// add remaining input qubits
				int curQubit = leftmostQubit;
				int offsetX = groupCellHeight - (inputCtr/2 + 1);
				for (int qubitCtr=offsetX; qubitCtr<groupCellHeight-1; ++qubitCtr) {
					curQubit = MapperUtil.GoEast(curQubit, 1);
					curInputQubits.add(curQubit);
				}
				// mark input qubits as already used
				qubits.addAll(curInputQubits);
			}
		}
	}
	// obtain group of input qubits
	public Set<Integer> getInputQubits(int group, int inputInGroup) {
		return inputQubits[group][inputInGroup];
	}
	// Returns index of one arbitrary qubit from the specified qubit set which was not yet used.
	// Returns -1 if all qubits were already used.
	private int selectUnused(TreeSet<Integer> qubitSet) {
		//Iterator<Integer> orderedIter = qubitSet.iterator();
		for (int qubit : qubitSet) {
			if (!qubits.contains(qubit)) {
				return qubit;
			}
		}
		return -1;
	}
	// Select qubits for representing auxiliary variables - for each input, we require one auxiliary
	// qubit for each qubit column within the same cell; if this is not the last input group then
	// we require additional qubits for the connection to the next auxiliary variable.
	void setAuxQubits() {
		for (int groupCtr=0; groupCtr<nrInputGroups; ++groupCtr) {
			for (int inputCtr=0; inputCtr<nrInputsPerGroup; ++inputCtr) {
				Set<Integer> curAuxQubits = new TreeSet<Integer>();
				auxQubits[groupCtr][inputCtr] = curAuxQubits;
				int leftmostInputQubit = leftmostInputQubit(groupCtr, inputCtr);
				TreeSet<Integer> leftColon = MapperUtil.leftColon(leftmostInputQubit);
				TreeSet<Integer> rightColon = MapperUtil.rightColon(leftmostInputQubit);
				int leftAuxQubit = selectUnused(leftColon);
				int rightAuxQubit = selectUnused(rightColon);
				assert(leftAuxQubit != -1);
				assert(rightAuxQubit != -1);
				curAuxQubits.add(leftAuxQubit);
				curAuxQubits.add(rightAuxQubit);
				// add connecting aux qubits
				if (groupCtr<nrInputGroups-1) {
					int curQubit = leftAuxQubit;
					int nrConnectionQubits = Math.max(minGroupDistance, groupCellHeight);
					for (int qubitCtr=0; qubitCtr<nrConnectionQubits; ++qubitCtr) {
						curQubit = MapperUtil.GoSouth(curQubit, 1);
						curAuxQubits.add(curQubit);
					}
				}
				qubits.addAll(curAuxQubits);
			}
		}
	}
	// obtains specified aux qubit group
	public Set<Integer> getAuxQubits(int group, int inputInGroup) {
		return auxQubits[group][inputInGroup];
	}
	// Returns qubits representing output.
	public Set<Integer> getOutputQubits(int outputIndex) {
		return auxQubits[nrInputGroups-1][outputIndex];
	}
	// Assert that all required connections between the qubits are indeed realized.
	void assertConnections() {
		// assert that all qubits representing the same variable form a connected chain
		for (int groupCtr=0; groupCtr<nrInputGroups; ++groupCtr) {
			for (int inputCtr=0; inputCtr<nrInputsPerGroup; ++inputCtr) {
				MapperUtil.qubitChain(inputQubits[groupCtr][inputCtr]);
				MapperUtil.qubitChain(auxQubits[groupCtr][inputCtr]);
			}
		}
		// assert that input qubits are connected to the aux qubits with same index
		for (int groupCtr=0; groupCtr<nrInputGroups; ++groupCtr) {
			for (int inputCtr=0; inputCtr<nrInputsPerGroup; ++inputCtr) {
				Set<Integer> curInputQubits = inputQubits[groupCtr][inputCtr];
				Set<Integer> curAuxQubits = auxQubits[groupCtr][inputCtr];
				assert(MapperUtil.connectedQubits(curInputQubits, curAuxQubits) != null);
			}
		}
		// assert that input qubits are connected to the aux qubits with lower index
		for (int groupCtr=1; groupCtr<nrInputGroups; ++groupCtr) {
			for (int inputCtr=0; inputCtr<nrInputsPerGroup; ++inputCtr) {
				Set<Integer> curInputQubits = inputQubits[groupCtr][inputCtr];
				Set<Integer> lastAuxQubits = auxQubits[groupCtr-1][inputCtr];
				assert(MapperUtil.connectedQubits(curInputQubits, lastAuxQubits) != null);
			}
		}
		// assert connections between aux qubits are realized
		for (int inputCtr=0; inputCtr<nrInputsPerGroup; ++inputCtr) {
			for (int groupCtr=0; groupCtr<nrInputGroups-1; ++groupCtr) {
				Set<Integer> curAuxQubits = auxQubits[groupCtr][inputCtr];
				Set<Integer> nextAuxQubits = auxQubits[groupCtr][inputCtr];
				assert(MapperUtil.connectedQubits(curAuxQubits, nextAuxQubits) != null);
			}
		}
	}
	// verifies that qubits of different variables do not overlap (except output and auxiliary qubits).
	void assertNoOverlap() {
		Set<Integer> unionQubits = new TreeSet<Integer>();
		int separateQubitCount = 0;
		// input qubits
		for (int inputCtr=0; inputCtr<nrInputsPerGroup; ++inputCtr) {
			for (int groupCtr=0; groupCtr<nrInputGroups; ++groupCtr) {
				Set<Integer> qubits = inputQubits[groupCtr][inputCtr];
				unionQubits.addAll(qubits);
				separateQubitCount += qubits.size();
			}
		}
		// auxiliary qubits
		for (int inputCtr=0; inputCtr<nrInputsPerGroup; ++inputCtr) {
			for (int groupCtr=0; groupCtr<nrInputGroups; ++groupCtr) {
				Set<Integer> qubits = auxQubits[groupCtr][inputCtr];
				unionQubits.addAll(qubits);
				separateQubitCount += qubits.size();
			}
		}
		assert(unionQubits.size() == separateQubitCount);
		assert(unionQubits.size() == qubits.size());
	}
}
