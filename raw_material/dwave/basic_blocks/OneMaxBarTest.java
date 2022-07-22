package raw_material.dwave.basic_blocks;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

public class OneMaxBarTest {
	
	// check that there is no overlap between the qubits representing non-equivalent variables
	private void verifyOverlap(OneMaxBar bar) {
		int nrInputs = bar.nrInputs;
		for (int auxIndex=0; auxIndex<nrInputs-1; ++auxIndex) {
			int input1 = bar.getInput(auxIndex);
			int input2 = bar.getInput(auxIndex+1);
			Set<Integer> auxSet1 = bar.getAuxiliaries(auxIndex);
			Set<Integer> auxSet2 = bar.getAuxiliaries(auxIndex+1);
			Set<Integer> unionSet = new HashSet<Integer>();
			unionSet.addAll(auxSet1);
			unionSet.addAll(auxSet2);
			assertEquals(auxSet1.size() + auxSet2.size(), unionSet.size());
			assertFalse(unionSet.contains(input1));
			assertFalse(unionSet.contains(input2));
			assertTrue(input1 != input2);
		}
	}

	@Test
	public void test() {
		// Small max bar taking only one input
		{
			boolean[] inputChains = new boolean[] {false, true};
			OneMaxBar bar = new OneMaxBar(0, 1, inputChains, new HashSet<Integer>());
			assertEquals(1, bar.nrInputs);
			assertEquals(1, bar.nrOutputs);
			// inputs
			assertEquals(5, bar.getInput(0));
			// outputs
			int output = bar.getOutput();
			assertTrue(output == 0 || output == 4);
			// auxiliary qubits
			{
				Set<Integer> expectedAuxSet = new HashSet<Integer>();
				expectedAuxSet.addAll(Arrays.asList(new Integer[] {0, 4}));
				assertTrue(expectedAuxSet.equals(bar.getAuxiliaries(0)));
			}
			verifyOverlap(bar);
		}
		// Bar taking one entire cell
		{
			boolean[] inputChains = new boolean[] {true, false, true, false};
			OneMaxBar bar = new OneMaxBar(0, 2, inputChains, new HashSet<Integer>());
			assertEquals(2, bar.nrInputs);
			assertEquals(1, bar.nrOutputs);
			// inputs
			assertEquals(4, bar.getInput(0));
			assertEquals(6, bar.getInput(1));
			// outputs
			int output = bar.getOutput();
			assertTrue(output == 2 || output == 7);
			// auxiliary qubits
			{
				Set<Integer> expectedAuxSet = new HashSet<Integer>();
				expectedAuxSet.addAll(Arrays.asList(new Integer[] {0, 5}));
				assertTrue(expectedAuxSet.equals(bar.getAuxiliaries(0)));
			}
			{
				Set<Integer> expectedAuxSet = new HashSet<Integer>();
				expectedAuxSet.addAll(Arrays.asList(new Integer[] {2, 7}));
				assertTrue(expectedAuxSet.equals(bar.getAuxiliaries(1)));
			}
			verifyOverlap(bar);
		}
		// Bar taking 1 1/2 cells
		{
			boolean[] inputChains = new boolean[] {true, false, false, true, false, true};
			OneMaxBar bar = new OneMaxBar(8, 3, inputChains, new HashSet<Integer>());
			assertEquals(3, bar.nrInputs);
			assertEquals(1, bar.nrOutputs);
			// inputs
			assertEquals(12, bar.getInput(0));
			assertEquals(15, bar.getInput(1));
			assertEquals(77, bar.getInput(2));
			// outputs
			int output = bar.getOutput();
			assertTrue(output == 76 || output == 74);
			// auxiliary qubits
			{
				Set<Integer> expectedAuxSet = new HashSet<Integer>();
				expectedAuxSet.addAll(Arrays.asList(new Integer[] {8, 13}));
				assertTrue(expectedAuxSet.equals(bar.getAuxiliaries(0)));
			}
			{
				Set<Integer> expectedAuxSet = new HashSet<Integer>();
				expectedAuxSet.addAll(Arrays.asList(new Integer[] {10, 14, 74}));
				assertTrue(expectedAuxSet.equals(bar.getAuxiliaries(1)));
			}
			{
				Set<Integer> expectedAuxSet = new HashSet<Integer>();
				expectedAuxSet.addAll(Arrays.asList(new Integer[] {76, 72}));
				assertTrue(expectedAuxSet.equals(bar.getAuxiliaries(2)));
			}
			verifyOverlap(bar);
		}
		// Verifying overlap of qubit sets in larger bar
		{
			boolean[] inputChains = new boolean[] {
					true, false, false, true, false, true, true, false, false, true};
			OneMaxBar bar = new OneMaxBar(32, 5, inputChains, new HashSet<Integer>());
			verifyOverlap(bar);
		}
	}

}
