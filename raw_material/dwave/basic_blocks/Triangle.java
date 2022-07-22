package raw_material.dwave.basic_blocks;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import raw_material.util.MapperUtil;

// An area on the qubit matrix that represents a fully connected graph. It has
// the form of a triangle.
public class Triangle extends QubitBlock {
	final TriangleDirection direction;	// direction of triangle
	final int nrChains;					// determines width and height
	final int cellWidth;				// number of cells along x/y axis
	boolean[] chainUsed;				// whether a chain is available
	public boolean[] chainOk;			// whether chain free from faulty qubits
	public int nrBrokenChains;			// number of chains with broken qubits
	final Set<Integer> borderQubits;	// qubits at border cells
	
	public Triangle(TriangleDirection direction, int topLeftIndex, int nrChains) {
		super(topLeftIndex);
		assert(topLeftIndex >= 0);
		assert(nrChains % 4 == 0);
		assert(MapperUtil.isUpperLeftInCell(topLeftIndex));
		this.direction 		= direction;
		this.nrChains 		= nrChains;
		this.cellWidth		= (int)Math.ceil(nrChains/4.0);
		qubits.addAll(calculateContainedQubits());
		borderQubits		= calculateBorderQubits();
		assert(qubits.containsAll(borderQubits));
		// mark all chains as unused
		chainUsed = new boolean[nrChains];
		Arrays.fill(chainUsed, false);
		// detect faulty chains
		chainOk = checkChainIntegrity();
		nrBrokenChains = 0;
		for (int chainIndex=0; chainIndex<nrChains; ++chainIndex) {
			if (!chainOk[chainIndex]) {
				++nrBrokenChains;
			}
		}
		// add all qubits used by this structure
		for (int chainIndex=0; chainIndex<nrChains; ++chainIndex) {
			if (chainOk[chainIndex]) {
				qubits.addAll(getChain(chainIndex));
			}
		}
	}
	
	// Returns boolean vector specifying which chains contain no faulty qubits.
	private boolean[] checkChainIntegrity() {
		boolean[] result = new boolean[nrChains];
		Arrays.fill(result, false);
		for (int chainIndex=0; chainIndex<nrChains; ++chainIndex) {
			Set<Integer> chain = getChain(chainIndex);
			result[chainIndex] = MapperUtil.isGroupIntact(chain);
		}
		return result;
	}
	
	// calculate contained qubits
	private Set<Integer> calculateContainedQubits() {
		Set<Integer> result = new HashSet<Integer>();
		int diagonalCornerQubit = topLeftQubit;
		result.addAll(MapperUtil.allQubitsInCell(diagonalCornerQubit));
		for (int cellIndex=0; cellIndex<cellWidth-1; ++cellIndex) {
			// add qubits outside off diagonal
			{
				int cornerQubit = diagonalCornerQubit;
				for (int stepCtr=0; stepCtr<cellWidth-1-cellIndex; ++stepCtr) {
					// walk east for north-east triangles and south for south-west triangles
					switch (direction) {
					case NORTH_EAST:
						cornerQubit = MapperUtil.GoEast(cornerQubit, 1);
						break;
					case SOUTH_WEST:
						cornerQubit = MapperUtil.GoSouth(cornerQubit, 1);
						break;
					}
					result.addAll(MapperUtil.allQubitsInCell(cornerQubit));
				}				
			}
			// advance to next cell on diagonal and add qubits
			diagonalCornerQubit = MapperUtil.GoEast(diagonalCornerQubit, 1);
			diagonalCornerQubit = MapperUtil.GoSouth(diagonalCornerQubit, 1);
			result.addAll(MapperUtil.allQubitsInCell(diagonalCornerQubit));
		}
		return result;
	}
	
	// calculate qubits on border cells
	private Set<Integer> calculateBorderQubits() {
		Set<Integer> result = new HashSet<Integer>();
		// add qubits on diagonal cells
		int cornerQubit = topLeftQubit;
		result.addAll(MapperUtil.allQubitsInCell(cornerQubit));
		for (int cellIndex=0; cellIndex<cellWidth-1; ++cellIndex) {
			cornerQubit = MapperUtil.GoEast(cornerQubit, 1);
			cornerQubit = MapperUtil.GoSouth(cornerQubit, 1);
			result.addAll(MapperUtil.allQubitsInCell(cornerQubit));
		}
		// select top left qubit of left-most cell on horizontal border
		switch (direction) {
		case NORTH_EAST:
			cornerQubit = topLeftQubit;
			break;
		case SOUTH_WEST:
			cornerQubit = MapperUtil.GoSouth(topLeftQubit, cellWidth-1);
			break;
		}
		// add qubits on horizontal border
		result.addAll(MapperUtil.allQubitsInCell(cornerQubit));
		for (int cellIndex=0; cellIndex<cellWidth-1; ++cellIndex) {
			cornerQubit = MapperUtil.GoEast(cornerQubit, 1);
			result.addAll(MapperUtil.allQubitsInCell(cornerQubit));
		}
		// select top left qubit on top-most cell on vertical border
		switch (direction) {
		case NORTH_EAST:
			cornerQubit = MapperUtil.GoEast(topLeftQubit, cellWidth-1);
			break;
		case SOUTH_WEST:
			cornerQubit = topLeftQubit;
			break;
		}
		// add qubits on vertical border
		result.addAll(MapperUtil.allQubitsInCell(cornerQubit));
		for (int cellIndex=0; cellIndex<cellWidth-1; ++cellIndex) {
			cornerQubit = MapperUtil.GoSouth(cornerQubit, 1);
			result.addAll(MapperUtil.allQubitsInCell(cornerQubit));
		}
		return result;
	}
	
	// Returns chain with the corresponding number.
	public Set<Integer> getChain(int chainIndex) {
		assert(chainIndex >= 0 && chainIndex < nrChains);
		// calculate how many cell steps on X/Y axis starting from diagonal
		int cellIndex 	= chainIndex / 4;
		int offset		= chainIndex % 4;
		int cellStepsX = -1;
		int cellStepsY = -1;
		switch (direction) {
		case NORTH_EAST:
			cellStepsX = cellWidth - 1 - cellIndex;
			cellStepsY = cellIndex;
			break;
		case SOUTH_WEST:
			cellStepsX = cellIndex;
			cellStepsY = cellWidth - 1 - cellIndex;
			break;
		}
		assert(cellStepsX >= 0);
		assert(cellStepsY >= 0);
		// calculate indices of qubits on the diagonal
		int diagonalIndexLeft 	= topLeftQubit + offset;
		diagonalIndexLeft		= MapperUtil.GoEast(diagonalIndexLeft, cellIndex);
		diagonalIndexLeft		= MapperUtil.GoSouth(diagonalIndexLeft, cellIndex);
		int diagonalIndexRight	= MapperUtil.rightOpposite(diagonalIndexLeft);
		assert(borderQubits.contains(diagonalIndexLeft));
		assert(borderQubits.contains(diagonalIndexRight));
		// add qubits on horizontal lane
		Set<Integer> result = new HashSet<Integer>();
		int horizontalIndex = diagonalIndexRight;
		result.add(horizontalIndex);
		for (int stepCtr=0; stepCtr<cellStepsX; ++stepCtr) {
			switch (direction) {
			case NORTH_EAST:
				horizontalIndex = MapperUtil.GoEast(horizontalIndex, 1);
				break;
			case SOUTH_WEST:
				horizontalIndex = MapperUtil.GoWest(horizontalIndex, 1);
				break;
			}
			assert(qubits.contains(horizontalIndex));
			result.add(horizontalIndex);
		}
		assert(borderQubits.contains(horizontalIndex));
		// add qubits on vertical lane
		int verticalIndex = diagonalIndexLeft;
		result.add(verticalIndex);
		for (int stepCtr=0; stepCtr<cellStepsY; ++stepCtr) {
			switch (direction) {
			case NORTH_EAST:
				verticalIndex = MapperUtil.GoNorth(verticalIndex, 1);
				break;
			case SOUTH_WEST:
				verticalIndex = MapperUtil.GoSouth(verticalIndex, 1);
				break;
			}
			assert(qubits.contains(verticalIndex));
			result.add(verticalIndex);
		}
		assert(borderQubits.contains(verticalIndex));
		return result;
		
	}
	//  Mark chain as already used (i.e., no variables can be assigned to it anymore).
	public void markAsUsed(int chainIndex) {
		assert(chainIndex >= 0 && chainIndex < nrChains);
		assert(chainUsed[chainIndex] == false);
		chainUsed[chainIndex] = true;
	}
	// Get an unused chain without faults and mark as used.
	public Set<Integer> markUnusedOkChain() throws Exception {
		for (int chainIndex=0; chainIndex<nrChains; ++chainIndex) {
			if (chainUsed[chainIndex] == false && chainOk[chainIndex]) {
				chainUsed[chainIndex] = true;
				return getChain(chainIndex);
			}
		}
		throw new Exception("No unused chain available without faulty qubits!");
	}
}
