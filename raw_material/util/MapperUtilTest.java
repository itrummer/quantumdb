package raw_material.util;

import static org.junit.Assert.*;
import static util.MapperUtil.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import raw_material.consolidation.dwave.ConsolidationMappingGeneric;
import raw_material.consolidation.testcase.ConsolidationProblem;
import dwave.variables.LogicalVariable;

public class MapperUtilTest {

	@Test
	public void test() {
		// Moving on the qubit matrix
		assertFalse(CanGoNorth(0));
		assertFalse(CanGoNorth(7));
		assertTrue(CanGoNorth(64));
		assertEquals(268, GoNorth(396,2));
		
		assert CanGoSouth(448) == false;
		assert CanGoSouth(511) == false;
		assert CanGoSouth(447) == true;
		assert GoSouth(218,2) == 346;
		
		assert CanGoEast(56) == false;
		assert CanGoEast(188) == false;
		assert CanGoEast(119) == true;
		assert GoEast(108,2) == 124;
		
		assert CanGoWest(129) == false;
		assert CanGoWest(256) == false;
		assert CanGoWest(511) == true;
		assert GoWest(504,7) == 448;
		
		assert GoSouthHalf(0) == 2;
		assert GoSouthHalf(435) == 497;
		assert GoSouthHalf(509) == 511;
		assert GoSouthHalf(443, 2) == GoSouth(443, 1);
		assert GoSouthHalf(56, 3) == 122;
		
		assertEquals(5, GoSouthQubitwise(4, 1));
		assertEquals(171, GoSouthQubitwise(168, 3));
		assertEquals(232, GoSouthQubitwise(168, 4));
		assertEquals(169, GoSouthQubitwise(40, 9));
		
		// Functions for determining position on the qubit matrix
		assertTrue(isAtBottom(3));
		assertTrue(isAtBottom(7));
		assertTrue(isAtBottom(167));
		assertTrue(isAtBottom(163));
		assertFalse(isAtBottom(162));
		assertFalse(isAtBottom(104));
		
		assert isLeftCellColon(0);
		assert !isLeftCellColon(4);
		assert isLeftCellColon(99);
		assert !isLeftCellColon(100);
		
		assert isUpperLeftInCell(0);
		assert !isUpperLeftInCell(1);
		assert isUpperLeftInCell(8);
		assert !isUpperLeftInCell(92);
		
		assert cellIndex(5) == 0;
		assert cellIndex(0) == 0;
		assert cellIndex(7) == 0;
		assert cellIndex(59) == 7;
		assert cellIndex(63) == 7;
		assert cellIndex(64) == 8;
		assert cellIndex(127) == 15;
		assert cellIndex(128) == 16;
		
		// Functions checking connections on the Chimera graph
		assert isInSameCell(0, 7);
		assert isInSameCell(0, 0);
		assert !isInSameCell(0, 8);
		assert !isConnected(63, 64);
		assert isInSameCell(162, 165);
		assert !isInSameCell(168, 232);
		
		assert isConnected(348, 356) == true;
		assert isConnected(348, 357) == false;
		assert isConnected(440, 504) == true;
		assert isConnected(441, 509) == false;
		
		assertTrue(isConnected(448, 455));
		assertFalse(isConnected(448, 449));
		assertTrue(isConnected(320, 324));
		assertFalse(isConnected(320, 332));
		
		// Functions returning qubits in a specific cell
		{
			Set<Integer> expected	= new HashSet<Integer>();
			expected.addAll(Arrays.asList(new Integer[] {104, 105, 106, 107, 108, 109, 110, 111}));
			Set<Integer> result 	= allQubitsInCell(104);
			assertEquals(expected.size(), result.size());
			assertTrue(result.containsAll(expected));
		}
		
		assertEquals(397, rightOpposite(393));
		assertEquals(406, rightOpposite(402));
		
		assertEquals(0, cornerQubit(1));
		assertEquals(0, cornerQubit(5));
		assertEquals(0, cornerQubit(7));
		assertEquals(480, cornerQubit(480));
		
		{
			Set<Integer> expected	= new HashSet<Integer>();
			expected.addAll(Arrays.asList(new Integer[] {408, 409, 410, 411}));
			Set<Integer> result 	= leftColon(415);
			assertEquals(expected.size(), result.size());
			assertTrue(result.containsAll(expected));
		}
		{
			Set<Integer> expected	= new HashSet<Integer>();
			expected.addAll(Arrays.asList(new Integer[] {412, 413, 414, 415}));
			Set<Integer> result 	= rightColon(415);
			assertEquals(expected.size(), result.size());
			assertTrue(result.containsAll(expected));
		}
		
		// Functions returning qubits that are connected to other qubits
		{
			Set<Integer> expected	= new HashSet<Integer>();
			expected.addAll(Arrays.asList(new Integer[] {344, 472, 412, 413, 414, 415}));
			Set<Integer> result 	= connectedInChimera(408);
			assertEquals(expected.size(), result.size());
			assertTrue(result.containsAll(expected));
		}
		{
			Set<Integer> expected	= new HashSet<Integer>();
			expected.addAll(Arrays.asList(new Integer[] {64, 4, 5, 6, 7}));
			Set<Integer> result 	= connectedInChimera(0);
			assertEquals(expected.size(), result.size());
			assertTrue(result.containsAll(expected));
		}
		{
			Set<Integer> expected	= new HashSet<Integer>();
			expected.addAll(Arrays.asList(new Integer[] {12, 0, 1, 2, 3}));
			Set<Integer> result 	= connectedInChimera(4);
			assertEquals(expected.size(), result.size());
			assertTrue(result.containsAll(expected));
		}
		
		{
			// expected result
			int[] expected			= new int[] {74, 79};
			// create input sets
			Set<Integer> group1		= new HashSet<Integer>();
			Set<Integer> group2		= new HashSet<Integer>();
			group1.addAll(Arrays.asList(new Integer[] {68, 69, 74}));
			group2.addAll(Arrays.asList(new Integer[] {79, 83, 87}));
			// generate result
			int[] result 			= connectedQubits(group1, group2);
			int[] orderedResult		= new int[2];
			orderedResult[0]		= Math.min(result[0], result[1]);
			orderedResult[1]		= Math.max(result[0], result[1]);
			// compare size and content
			assertArrayEquals(expected, orderedResult);
		}
		{
			// expected result
			int[] expected			= new int[] {92, 100};
			// create input sets
			Set<Integer> group1		= new HashSet<Integer>();
			Set<Integer> group2		= new HashSet<Integer>();
			group1.addAll(Arrays.asList(new Integer[] {88, 92}));
			group2.addAll(Arrays.asList(new Integer[] {100, 96}));
			// generate result
			int[] result 			= connectedQubits(group1, group2);
			int[] orderedResult		= new int[2];
			orderedResult[0]		= Math.min(result[0], result[1]);
			orderedResult[1]		= Math.max(result[0], result[1]);
			// compare size and content
			assertArrayEquals(expected, orderedResult);
		}
		
		{
			Set<Integer> inputSet = new HashSet<Integer>();
			inputSet.addAll(Arrays.asList(new Integer[] {90, 94, 95}));
			assertEquals(94, connectedQubit(86, inputSet));
		}
		{
			Set<Integer> inputSet = new HashSet<Integer>();
			inputSet.addAll(Arrays.asList(new Integer[] {90, 94, 95}));
			assertEquals(94, connectedQubit(86, inputSet));			
		}
		
		// Functions adding standard constraints between variables or qubits
		{
			LogicalVariable input1 = new LogicalVariable();
			LogicalVariable input2 = new LogicalVariable();
			LogicalVariable output = new LogicalVariable();
			input1.qubits.add(0);
			input2.qubits.add(4);
			output.qubits.add(1);
			output.qubits.add(5);
			ConsolidationMappingGeneric mapping = new ConsolidationMappingGeneric(1, 1);
			addMaxConstraint(input1, input2, output, 2, mapping);
			// check single qubit weights
			assertEquals(2.0, mapping.getWeight(0), MapperUtil.DOUBLE_TOLERANCE);
			assertEquals(2.0, mapping.getWeight(4), MapperUtil.DOUBLE_TOLERANCE);
			double outputWeight = mapping.getWeight(1) + mapping.getWeight(5);
			assertEquals(2.0, outputWeight, MapperUtil.DOUBLE_TOLERANCE);
			// check connection weights
			assertEquals(2.0, mapping.getConnectionWeight(0, 4), MapperUtil.DOUBLE_TOLERANCE);
			double input1Output = mapping.getConnectionWeight(0, 5);
			double input2Output = mapping.getConnectionWeight(4, 1);
			assertEquals(-4.0, input1Output, MapperUtil.DOUBLE_TOLERANCE);
			assertEquals(-4.0, input2Output, MapperUtil.DOUBLE_TOLERANCE);
		}
		
		{
			LogicalVariable var1 = new LogicalVariable();
			LogicalVariable var2 = new LogicalVariable();
			var1.qubits.add(0);
			var2.qubits.add(4);
			ConsolidationMappingGeneric mapping = new ConsolidationMappingGeneric(1, 1);
			addEqualityConstraint(var1, var2, 2.0, mapping);
			assertEquals(2.0, mapping.getWeight(0), MapperUtil.DOUBLE_TOLERANCE);
			assertEquals(2.0, mapping.getWeight(4), MapperUtil.DOUBLE_TOLERANCE);
			assertEquals(-4.0, mapping.getConnectionWeight(0, 4), MapperUtil.DOUBLE_TOLERANCE);
		}
		
		{
			ConsolidationMappingGeneric mapping = new ConsolidationMappingGeneric(1, 1);
			addEqualityConstraint(0, 4, 2.0, mapping);
			assertEquals(2.0, mapping.getWeight(0), MapperUtil.DOUBLE_TOLERANCE);
			assertEquals(2.0, mapping.getWeight(4), MapperUtil.DOUBLE_TOLERANCE);
			assertEquals(-4.0, mapping.getConnectionWeight(0, 4), MapperUtil.DOUBLE_TOLERANCE);
		}
		
		// Other functions
		{
			ConsolidationProblem problem = new ConsolidationProblem(1, 1, 1, 0.25);
			assertEquals(1, nrCapacityVars(problem, 0.25));
			assertEquals(2, nrCapacityVars(problem, 0.5));
			assertEquals(3, nrCapacityVars(problem, 1.75));
		}
		
		assertEquals(4, roundUpFour(3));
		assertEquals(4, roundUpFour(4));
		assertEquals(8, roundUpFour(5));
		assertEquals(16, roundUpFour(15));
		
		{
			Set<Integer> qubits = new HashSet<Integer>();
			qubits.addAll(Arrays.asList(new Integer[] {34, 38, 39, 153}));
			assertTrue(isGroupIntact(qubits));
		}
		
		{
			Set<Integer> qubits = new HashSet<Integer>();
			qubits.addAll(Arrays.asList(new Integer[] {35, 39}));
			assertFalse(isGroupIntact(qubits));
		}
		
		{
			Set<Integer> qubits = new HashSet<Integer>();
			qubits.addAll(Arrays.asList(new Integer[] {35, 39}));
			assertEquals(1, nrNeighbors(38, qubits));
		}
		{
			Set<Integer> qubits = new HashSet<Integer>();
			qubits.addAll(Arrays.asList(new Integer[] {96, 99, 92, 108, 101}));
			assertEquals(4, nrNeighbors(100, qubits));
		}
		
		{
			Set<Integer> qubits = new HashSet<Integer>();
			qubits.addAll(Arrays.asList(new Integer[] {103, 84, 98, 92, 100, 171, 111, 107 }));
			List<Integer> resultList = qubitChain(qubits);
			Integer[] result = new Integer[8];
			resultList.toArray(result);
			Integer[] eOption1	= new Integer[] {84, 92, 100, 98, 103, 111, 107, 171};
			Integer[] eOption2	= new Integer[] {171, 107, 111, 103, 98, 100, 92, 84};
			assertTrue(result[0] == 84 || result[0] == 171);
			if (result[0] == 84) {
				assertArrayEquals(eOption1, result);
			} else {
				assertArrayEquals(eOption2, result);
			}
		}
		
		{
			ConsolidationMappingGeneric mapping = new ConsolidationMappingGeneric(1, 1);
			mapping.addWeight(96, 96, 1);
			mapping.addWeight(96, 101, 2.0);
			mapping.addWeight(96, 32, -1.0);
			mapping.addWeight(96, 160, 1.0);
			mapping.addWeight(97, 97, -5.0);
			mapping.addWeight(96, 101, 3.0);
			assertEquals(0, optimisticLocalEnergy(96, 0, mapping), MapperUtil.DOUBLE_TOLERANCE);
			assertEquals(0, optimisticLocalEnergy(96, 1, mapping), MapperUtil.DOUBLE_TOLERANCE);
			assertEquals(0, pessimisticLocalEnergy(96, 0, mapping), MapperUtil.DOUBLE_TOLERANCE);
			assertEquals(7, pessimisticLocalEnergy(96, 1, mapping), MapperUtil.DOUBLE_TOLERANCE);
		}

	}

}
