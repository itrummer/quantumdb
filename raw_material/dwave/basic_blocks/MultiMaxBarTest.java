package raw_material.dwave.basic_blocks;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;

import org.junit.Test;

public class MultiMaxBarTest {

	@Test
	public void test() {
		// create MM-bars
		/////////////////
		MultiMaxBar mmbar1 = new MultiMaxBar(0, 1, 1, new boolean[][] {{true, false}}, 0);
		MultiMaxBar mmbar2 = new MultiMaxBar(8, 1, 1, new boolean[][] {{true, false}}, 0);
		MultiMaxBar mmbar3 = new MultiMaxBar(0, 2, 1, new boolean[][] {{true, false}, {true, false}}, 0);
		MultiMaxBar mmbar4 = new MultiMaxBar(0, 2, 2, 
				new boolean[][] {{true, false, true, false}, {true, false, true, false}}, 0);
		MultiMaxBar mmbar5 = new MultiMaxBar(0, 2, 3, 
				new boolean[][] {
				{true, false, true, false, true, false}, 
				{true, false, false, true, true, false}}, 0);
		MultiMaxBar mmbar6 = new MultiMaxBar(0, 2, 6, 
				new boolean[][] {
				{true, false, true, false, true, false, true, false, true, false, true, false}, 
				{true, false, true, false, true, false, true, false, true, false, true, false}}, 0);
		MultiMaxBar mmbar7 = new MultiMaxBar(0, 2, 6, 
				new boolean[][] {
				{true, false, true, false, true, false, true, false, true, false, true, false}, 
				{true, false, true, false, true, false, true, false, true, false, true, false}}, 4);

		// dimensions
		/////////////
		assertEquals(1, mmbar1.groupCellHeight);
		assertEquals(1, mmbar2.groupCellHeight);
		assertEquals(1, mmbar3.groupCellHeight);
		assertEquals(1, mmbar4.groupCellHeight);
		assertEquals(2, mmbar5.groupCellHeight);
		assertEquals(3, mmbar6.groupCellHeight);
		
		// input qubits
		///////////////
		{
			Set<Integer> qubits = mmbar1.getInputQubits(0, 0);
			Set<Integer> expectedQubits = new TreeSet<Integer>();
			expectedQubits.addAll(Arrays.asList(new Integer[]{4}));
			assertEquals(expectedQubits, qubits);
		}
		{
			Set<Integer> qubits = mmbar2.getInputQubits(0, 0);
			Set<Integer> expectedQubits = new TreeSet<Integer>();
			expectedQubits.addAll(Arrays.asList(new Integer[]{12}));
			assertEquals(expectedQubits, qubits);
		}
		// MM-bar 3
		{
			Set<Integer> qubits = mmbar3.getInputQubits(0, 0);
			Set<Integer> expectedQubits = new TreeSet<Integer>();
			expectedQubits.addAll(Arrays.asList(new Integer[]{4}));
			assertEquals(expectedQubits, qubits);
		}
		{
			Set<Integer> qubits = mmbar3.getInputQubits(1, 0);
			Set<Integer> expectedQubits = new TreeSet<Integer>();
			expectedQubits.addAll(Arrays.asList(new Integer[]{68}));
			assertEquals(expectedQubits, qubits);
		}
		// MM-bar 4
		{
			Set<Integer> qubits = mmbar4.getInputQubits(0, 0);
			Set<Integer> expectedQubits = new TreeSet<Integer>();
			expectedQubits.addAll(Arrays.asList(new Integer[]{4}));
			assertEquals(expectedQubits, qubits);
		}
		{
			Set<Integer> qubits = mmbar4.getInputQubits(0, 1);
			Set<Integer> expectedQubits = new TreeSet<Integer>();
			expectedQubits.addAll(Arrays.asList(new Integer[]{6}));
			assertEquals(expectedQubits, qubits);
		}
		{
			Set<Integer> qubits = mmbar4.getInputQubits(1, 0);
			Set<Integer> expectedQubits = new TreeSet<Integer>();
			expectedQubits.addAll(Arrays.asList(new Integer[]{68}));
			assertEquals(expectedQubits, qubits);
		}
		{
			Set<Integer> qubits = mmbar4.getInputQubits(1, 1);
			Set<Integer> expectedQubits = new TreeSet<Integer>();
			expectedQubits.addAll(Arrays.asList(new Integer[]{70}));
			assertEquals(expectedQubits, qubits);
		}
		// MM-bar 5
		{
			Set<Integer> qubits = mmbar5.getInputQubits(0, 0);
			Set<Integer> expectedQubits = new TreeSet<Integer>();
			expectedQubits.addAll(Arrays.asList(new Integer[]{12}));
			assertEquals(expectedQubits, qubits);
		}
		{
			Set<Integer> qubits = mmbar5.getInputQubits(0, 1);
			Set<Integer> expectedQubits = new TreeSet<Integer>();
			expectedQubits.addAll(Arrays.asList(new Integer[]{14}));
			assertEquals(expectedQubits, qubits);
		}
		{
			Set<Integer> qubits = mmbar5.getInputQubits(0, 2);
			Set<Integer> expectedQubits = new TreeSet<Integer>();
			expectedQubits.addAll(Arrays.asList(new Integer[]{68, 76}));
			assertEquals(expectedQubits, qubits);
		}
		{
			Set<Integer> qubits = mmbar5.getInputQubits(1, 0);
			Set<Integer> expectedQubits = new TreeSet<Integer>();
			expectedQubits.addAll(Arrays.asList(new Integer[]{140}));
			assertEquals(expectedQubits, qubits);
		}
		{
			Set<Integer> qubits = mmbar5.getInputQubits(1, 1);
			Set<Integer> expectedQubits = new TreeSet<Integer>();
			expectedQubits.addAll(Arrays.asList(new Integer[]{143}));
			assertEquals(expectedQubits, qubits);
		}
		{
			Set<Integer> qubits = mmbar5.getInputQubits(1, 2);
			Set<Integer> expectedQubits = new TreeSet<Integer>();
			expectedQubits.addAll(Arrays.asList(new Integer[]{196, 204}));
			assertEquals(expectedQubits, qubits);
		}
		// MM-bar 6
		{
			Set<Integer> qubits = mmbar6.getInputQubits(0, 0);
			Set<Integer> expectedQubits = new TreeSet<Integer>();
			expectedQubits.addAll(Arrays.asList(new Integer[]{20}));
			assertEquals(expectedQubits, qubits);
		}
		{
			Set<Integer> qubits = mmbar6.getInputQubits(0, 1);
			Set<Integer> expectedQubits = new TreeSet<Integer>();
			expectedQubits.addAll(Arrays.asList(new Integer[]{22}));
			assertEquals(expectedQubits, qubits);
		}
		{
			Set<Integer> qubits = mmbar6.getInputQubits(0, 2);
			Set<Integer> expectedQubits = new TreeSet<Integer>();
			expectedQubits.addAll(Arrays.asList(new Integer[]{76, 84}));
			assertEquals(expectedQubits, qubits);
		}
		{
			Set<Integer> qubits = mmbar6.getInputQubits(0, 3);
			Set<Integer> expectedQubits = new TreeSet<Integer>();
			expectedQubits.addAll(Arrays.asList(new Integer[]{78, 86}));
			assertEquals(expectedQubits, qubits);
		}
		{
			Set<Integer> qubits = mmbar6.getInputQubits(0, 4);
			Set<Integer> expectedQubits = new TreeSet<Integer>();
			expectedQubits.addAll(Arrays.asList(new Integer[]{132, 140, 148}));
			assertEquals(expectedQubits, qubits);
		}
		{
			Set<Integer> qubits = mmbar6.getInputQubits(0, 5);
			Set<Integer> expectedQubits = new TreeSet<Integer>();
			expectedQubits.addAll(Arrays.asList(new Integer[]{134, 142, 150}));
			assertEquals(expectedQubits, qubits);
		}
		{
			Set<Integer> qubits = mmbar6.getInputQubits(1, 0);
			Set<Integer> expectedQubits = new TreeSet<Integer>();
			expectedQubits.addAll(Arrays.asList(new Integer[]{212}));
			assertEquals(expectedQubits, qubits);
		}
		{
			Set<Integer> qubits = mmbar6.getInputQubits(1, 1);
			Set<Integer> expectedQubits = new TreeSet<Integer>();
			expectedQubits.addAll(Arrays.asList(new Integer[]{214}));
			assertEquals(expectedQubits, qubits);
		}
		{
			Set<Integer> qubits = mmbar6.getInputQubits(1, 2);
			Set<Integer> expectedQubits = new TreeSet<Integer>();
			expectedQubits.addAll(Arrays.asList(new Integer[]{268, 276}));
			assertEquals(expectedQubits, qubits);
		}
		{
			Set<Integer> qubits = mmbar6.getInputQubits(1, 3);
			Set<Integer> expectedQubits = new TreeSet<Integer>();
			expectedQubits.addAll(Arrays.asList(new Integer[]{270, 278}));
			assertEquals(expectedQubits, qubits);
		}
		{
			Set<Integer> qubits = mmbar6.getInputQubits(1, 4);
			Set<Integer> expectedQubits = new TreeSet<Integer>();
			expectedQubits.addAll(Arrays.asList(new Integer[]{324, 332, 340}));
			assertEquals(expectedQubits, qubits);
		}
		{
			Set<Integer> qubits = mmbar6.getInputQubits(1, 5);
			Set<Integer> expectedQubits = new TreeSet<Integer>();
			expectedQubits.addAll(Arrays.asList(new Integer[]{326, 334, 342}));
			assertEquals(expectedQubits, qubits);
		}
		// MM-bar 7
		{
			Set<Integer> qubits = mmbar7.getInputQubits(0, 0);
			Set<Integer> expectedQubits = new TreeSet<Integer>();
			expectedQubits.addAll(Arrays.asList(new Integer[]{20}));
			assertEquals(expectedQubits, qubits);
		}
		{
			Set<Integer> qubits = mmbar7.getInputQubits(0, 1);
			Set<Integer> expectedQubits = new TreeSet<Integer>();
			expectedQubits.addAll(Arrays.asList(new Integer[]{22}));
			assertEquals(expectedQubits, qubits);
		}
		{
			Set<Integer> qubits = mmbar7.getInputQubits(0, 2);
			Set<Integer> expectedQubits = new TreeSet<Integer>();
			expectedQubits.addAll(Arrays.asList(new Integer[]{76, 84}));
			assertEquals(expectedQubits, qubits);
		}
		{
			Set<Integer> qubits = mmbar7.getInputQubits(0, 3);
			Set<Integer> expectedQubits = new TreeSet<Integer>();
			expectedQubits.addAll(Arrays.asList(new Integer[]{78, 86}));
			assertEquals(expectedQubits, qubits);
		}
		{
			Set<Integer> qubits = mmbar7.getInputQubits(0, 4);
			Set<Integer> expectedQubits = new TreeSet<Integer>();
			expectedQubits.addAll(Arrays.asList(new Integer[]{132, 140, 148}));
			assertEquals(expectedQubits, qubits);
		}
		{
			Set<Integer> qubits = mmbar7.getInputQubits(0, 5);
			Set<Integer> expectedQubits = new TreeSet<Integer>();
			expectedQubits.addAll(Arrays.asList(new Integer[]{134, 142, 150}));
			assertEquals(expectedQubits, qubits);
		}
		{
			Set<Integer> qubits = mmbar7.getInputQubits(1, 0);
			Set<Integer> expectedQubits = new TreeSet<Integer>();
			expectedQubits.addAll(Arrays.asList(new Integer[]{212+64}));
			assertEquals(expectedQubits, qubits);
		}
		{
			Set<Integer> qubits = mmbar7.getInputQubits(1, 1);
			Set<Integer> expectedQubits = new TreeSet<Integer>();
			expectedQubits.addAll(Arrays.asList(new Integer[]{214+64}));
			assertEquals(expectedQubits, qubits);
		}
		{
			Set<Integer> qubits = mmbar7.getInputQubits(1, 2);
			Set<Integer> expectedQubits = new TreeSet<Integer>();
			expectedQubits.addAll(Arrays.asList(new Integer[]{268+64, 276+64}));
			assertEquals(expectedQubits, qubits);
		}
		{
			Set<Integer> qubits = mmbar7.getInputQubits(1, 3);
			Set<Integer> expectedQubits = new TreeSet<Integer>();
			expectedQubits.addAll(Arrays.asList(new Integer[]{270+64, 278+64}));
			assertEquals(expectedQubits, qubits);
		}
		{
			Set<Integer> qubits = mmbar7.getInputQubits(1, 4);
			Set<Integer> expectedQubits = new TreeSet<Integer>();
			expectedQubits.addAll(Arrays.asList(new Integer[]{324+64, 332+64, 340+64}));
			assertEquals(expectedQubits, qubits);
		}
		{
			Set<Integer> qubits = mmbar7.getInputQubits(1, 5);
			Set<Integer> expectedQubits = new TreeSet<Integer>();
			expectedQubits.addAll(Arrays.asList(new Integer[]{326+64, 334+64, 342+64}));
			assertEquals(expectedQubits, qubits);
		}
		
		// Aux qubits
		/////////////
		{
			Set<Integer> qubits = mmbar1.getAuxQubits(0, 0);
			Set<Integer> expectedQubits = new TreeSet<Integer>();
			expectedQubits.addAll(Arrays.asList(new Integer[]{0, 5}));
			assertEquals(expectedQubits, qubits);
		}
		{
			Set<Integer> qubits = mmbar2.getAuxQubits(0, 0);
			Set<Integer> expectedQubits = new TreeSet<Integer>();
			expectedQubits.addAll(Arrays.asList(new Integer[]{8, 13}));
			assertEquals(expectedQubits, qubits);
		}
		// MM-bar 3
		{
			Set<Integer> qubits = mmbar3.getAuxQubits(0, 0);
			Set<Integer> expectedQubits = new TreeSet<Integer>();
			expectedQubits.addAll(Arrays.asList(new Integer[]{0, 5, 64}));
			assertEquals(expectedQubits, qubits);
		}
		{
			Set<Integer> qubits = mmbar3.getAuxQubits(1, 0);
			Set<Integer> expectedQubits = new TreeSet<Integer>();
			expectedQubits.addAll(Arrays.asList(new Integer[]{65, 69}));
			assertEquals(expectedQubits, qubits);
		}
		// MM-bar 4
		{
			Set<Integer> qubits = mmbar4.getAuxQubits(0, 0);
			Set<Integer> expectedQubits = new TreeSet<Integer>();
			expectedQubits.addAll(Arrays.asList(new Integer[]{0, 5, 64}));
			assertEquals(expectedQubits, qubits);
		}
		{
			Set<Integer> qubits = mmbar4.getAuxQubits(0, 1);
			Set<Integer> expectedQubits = new TreeSet<Integer>();
			expectedQubits.addAll(Arrays.asList(new Integer[]{1, 7, 65}));
			assertEquals(expectedQubits, qubits);
		}
		{
			Set<Integer> qubits = mmbar4.getAuxQubits(1, 0);
			Set<Integer> expectedQubits = new TreeSet<Integer>();
			expectedQubits.addAll(Arrays.asList(new Integer[]{66, 69}));
			assertEquals(expectedQubits, qubits);
		}
		{
			Set<Integer> qubits = mmbar4.getAuxQubits(1, 1);
			Set<Integer> expectedQubits = new TreeSet<Integer>();
			expectedQubits.addAll(Arrays.asList(new Integer[]{67, 71}));
			assertEquals(expectedQubits, qubits);
		}
		// MM-bar 5
		{
			Set<Integer> qubits = mmbar5.getAuxQubits(0, 0);
			Set<Integer> expectedQubits = new TreeSet<Integer>();
			expectedQubits.addAll(Arrays.asList(new Integer[]{8, 13, 72, 136}));
			assertEquals(expectedQubits, qubits);
		}
		{
			Set<Integer> qubits = mmbar5.getAuxQubits(0, 1);
			Set<Integer> expectedQubits = new TreeSet<Integer>();
			expectedQubits.addAll(Arrays.asList(new Integer[]{9, 15, 73, 137}));
			assertEquals(expectedQubits, qubits);
		}
		{
			Set<Integer> qubits = mmbar5.getAuxQubits(0, 2);
			Set<Integer> expectedQubits = new TreeSet<Integer>();
			expectedQubits.addAll(Arrays.asList(new Integer[]{64, 69, 128, 192}));
			assertEquals(expectedQubits, qubits);
		}
		{
			Set<Integer> qubits = mmbar5.getAuxQubits(1, 0);
			Set<Integer> expectedQubits = new TreeSet<Integer>();
			expectedQubits.addAll(Arrays.asList(new Integer[]{138, 141}));
			assertEquals(expectedQubits, qubits);
		}
		{
			Set<Integer> qubits = mmbar5.getAuxQubits(1, 1);
			Set<Integer> expectedQubits = new TreeSet<Integer>();
			expectedQubits.addAll(Arrays.asList(new Integer[]{139, 142}));
			assertEquals(expectedQubits, qubits);
		}
		{
			Set<Integer> qubits = mmbar5.getAuxQubits(1, 2);
			Set<Integer> expectedQubits = new TreeSet<Integer>();
			expectedQubits.addAll(Arrays.asList(new Integer[]{193, 197}));
			assertEquals(expectedQubits, qubits);
		}
		// MM-bar 6
		{
			Set<Integer> qubits = mmbar6.getAuxQubits(0, 0);
			Set<Integer> expectedQubits = new TreeSet<Integer>();
			expectedQubits.addAll(Arrays.asList(new Integer[]{16, 21, 80, 144, 208}));
			assertEquals(expectedQubits, qubits);
		}
		{
			Set<Integer> qubits = mmbar6.getAuxQubits(0, 1);
			Set<Integer> expectedQubits = new TreeSet<Integer>();
			expectedQubits.addAll(Arrays.asList(new Integer[]{17, 23, 81, 145, 209}));
			assertEquals(expectedQubits, qubits);
		}
		{
			Set<Integer> qubits = mmbar6.getAuxQubits(0, 2);
			Set<Integer> expectedQubits = new TreeSet<Integer>();
			expectedQubits.addAll(Arrays.asList(new Integer[]{72, 77, 136, 200, 264}));
			assertEquals(expectedQubits, qubits);
		}
		{
			Set<Integer> qubits = mmbar6.getAuxQubits(0, 3);
			Set<Integer> expectedQubits = new TreeSet<Integer>();
			expectedQubits.addAll(Arrays.asList(new Integer[]{73, 79, 137, 201, 265}));
			assertEquals(expectedQubits, qubits);
		}
		{
			Set<Integer> qubits = mmbar6.getAuxQubits(0, 4);
			Set<Integer> expectedQubits = new TreeSet<Integer>();
			expectedQubits.addAll(Arrays.asList(new Integer[]{128, 133, 192, 256, 320}));
			assertEquals(expectedQubits, qubits);
		}
		{
			Set<Integer> qubits = mmbar6.getAuxQubits(0, 5);
			Set<Integer> expectedQubits = new TreeSet<Integer>();
			expectedQubits.addAll(Arrays.asList(new Integer[]{129, 135, 193, 257, 321}));
			assertEquals(expectedQubits, qubits);
		}
		{
			Set<Integer> qubits = mmbar6.getAuxQubits(1, 0);
			Set<Integer> expectedQubits = new TreeSet<Integer>();
			expectedQubits.addAll(Arrays.asList(new Integer[]{210, 213}));
			assertEquals(expectedQubits, qubits);
		}
		{
			Set<Integer> qubits = mmbar6.getAuxQubits(1, 1);
			Set<Integer> expectedQubits = new TreeSet<Integer>();
			expectedQubits.addAll(Arrays.asList(new Integer[]{211, 215}));
			assertEquals(expectedQubits, qubits);
		}
		{
			Set<Integer> qubits = mmbar6.getAuxQubits(1, 2);
			Set<Integer> expectedQubits = new TreeSet<Integer>();
			expectedQubits.addAll(Arrays.asList(new Integer[]{266, 269}));
			assertEquals(expectedQubits, qubits);
		}
		{
			Set<Integer> qubits = mmbar6.getAuxQubits(1, 3);
			Set<Integer> expectedQubits = new TreeSet<Integer>();
			expectedQubits.addAll(Arrays.asList(new Integer[]{267, 271}));
			assertEquals(expectedQubits, qubits);
		}
		{
			Set<Integer> qubits = mmbar6.getAuxQubits(1, 4);
			Set<Integer> expectedQubits = new TreeSet<Integer>();
			expectedQubits.addAll(Arrays.asList(new Integer[]{322, 325}));
			assertEquals(expectedQubits, qubits);
		}
		{
			Set<Integer> qubits = mmbar6.getAuxQubits(1, 5);
			Set<Integer> expectedQubits = new TreeSet<Integer>();
			expectedQubits.addAll(Arrays.asList(new Integer[]{323, 327}));
			assertEquals(expectedQubits, qubits);
		}
		// MM-bar 7
		{
			Set<Integer> qubits = mmbar7.getAuxQubits(0, 0);
			Set<Integer> expectedQubits = new TreeSet<Integer>();
			expectedQubits.addAll(Arrays.asList(new Integer[]{16, 21, 80, 144, 208, 272}));
			assertEquals(expectedQubits, qubits);
		}
		{
			Set<Integer> qubits = mmbar7.getAuxQubits(0, 1);
			Set<Integer> expectedQubits = new TreeSet<Integer>();
			expectedQubits.addAll(Arrays.asList(new Integer[]{17, 23, 81, 145, 209, 273}));
			assertEquals(expectedQubits, qubits);
		}
		{
			Set<Integer> qubits = mmbar7.getAuxQubits(0, 2);
			Set<Integer> expectedQubits = new TreeSet<Integer>();
			expectedQubits.addAll(Arrays.asList(new Integer[]{72, 77, 136, 200, 264, 264+64}));
			assertEquals(expectedQubits, qubits);
		}
		{
			Set<Integer> qubits = mmbar7.getAuxQubits(0, 3);
			Set<Integer> expectedQubits = new TreeSet<Integer>();
			expectedQubits.addAll(Arrays.asList(new Integer[]{73, 79, 137, 201, 265, 265+64}));
			assertEquals(expectedQubits, qubits);
		}
		{
			Set<Integer> qubits = mmbar7.getAuxQubits(0, 4);
			Set<Integer> expectedQubits = new TreeSet<Integer>();
			expectedQubits.addAll(Arrays.asList(new Integer[]{128, 133, 192, 256, 320, 384}));
			assertEquals(expectedQubits, qubits);
		}
		{
			Set<Integer> qubits = mmbar7.getAuxQubits(0, 5);
			Set<Integer> expectedQubits = new TreeSet<Integer>();
			expectedQubits.addAll(Arrays.asList(new Integer[]{129, 135, 193, 257, 321, 385}));
			assertEquals(expectedQubits, qubits);
		}
		{
			Set<Integer> qubits = mmbar7.getAuxQubits(1, 0);
			Set<Integer> expectedQubits = new TreeSet<Integer>();
			expectedQubits.addAll(Arrays.asList(new Integer[]{210+64, 213+64}));
			assertEquals(expectedQubits, qubits);
		}
		{
			Set<Integer> qubits = mmbar7.getAuxQubits(1, 1);
			Set<Integer> expectedQubits = new TreeSet<Integer>();
			expectedQubits.addAll(Arrays.asList(new Integer[]{211+64, 215+64}));
			assertEquals(expectedQubits, qubits);
		}
		{
			Set<Integer> qubits = mmbar7.getAuxQubits(1, 2);
			Set<Integer> expectedQubits = new TreeSet<Integer>();
			expectedQubits.addAll(Arrays.asList(new Integer[]{266+64, 269+64}));
			assertEquals(expectedQubits, qubits);
		}
		{
			Set<Integer> qubits = mmbar7.getAuxQubits(1, 3);
			Set<Integer> expectedQubits = new TreeSet<Integer>();
			expectedQubits.addAll(Arrays.asList(new Integer[]{267+64, 271+64}));
			assertEquals(expectedQubits, qubits);
		}
		{
			Set<Integer> qubits = mmbar7.getAuxQubits(1, 4);
			Set<Integer> expectedQubits = new TreeSet<Integer>();
			expectedQubits.addAll(Arrays.asList(new Integer[]{322+64, 325+64}));
			assertEquals(expectedQubits, qubits);
		}
		{
			Set<Integer> qubits = mmbar7.getAuxQubits(1, 5);
			Set<Integer> expectedQubits = new TreeSet<Integer>();
			expectedQubits.addAll(Arrays.asList(new Integer[]{323+64, 327+64}));
			assertEquals(expectedQubits, qubits);
		}
		
		// output qubits
		////////////////
		
		// MM Bar 1
		{
			Set<Integer> qubits = mmbar1.getOutputQubits(0);
			Set<Integer> expectedQubits = new TreeSet<Integer>();
			expectedQubits.addAll(Arrays.asList(new Integer[]{0, 5}));
			assertEquals(expectedQubits, qubits);
		}
		// MM Bar 6
		{
			Set<Integer> qubits = mmbar6.getOutputQubits(5);
			Set<Integer> expectedQubits = new TreeSet<Integer>();
			expectedQubits.addAll(Arrays.asList(new Integer[]{323, 327}));
			assertEquals(expectedQubits, qubits);
		}
		{
			Set<Integer> qubits = mmbar6.getOutputQubits(3);
			Set<Integer> expectedQubits = new TreeSet<Integer>();
			expectedQubits.addAll(Arrays.asList(new Integer[]{267, 271}));
			assertEquals(expectedQubits, qubits);
		}

	}
}
