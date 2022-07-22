package raw_material.dwave.variables;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.Test;

import raw_material.consolidation.dwave.ConsolidationMappingGeneric;
import raw_material.util.MapperUtil;

public class LogicalVariableTest {

	@Test
	public void test() {
		// Adding weights to variable
		{
			ConsolidationMappingGeneric mapping = new ConsolidationMappingGeneric(1, 1);
			LogicalVariable var = new LogicalVariable();
			var.qubits.addAll(Arrays.asList(new Integer[] {88, 89, 90}));
			var.addWeight(mapping, 3);
			double weightSum = mapping.getWeight(88) + mapping.getWeight(89) + mapping.getWeight(90);
			assertEquals(3, weightSum, MapperUtil.DOUBLE_TOLERANCE);
			assertEquals(3, var.getWeight(mapping), MapperUtil.DOUBLE_TOLERANCE);
		}
		
		// Adding weights connecting two variables
		{
			ConsolidationMappingGeneric mapping = new ConsolidationMappingGeneric(1, 1);
			LogicalVariable var1 = new LogicalVariable();
			LogicalVariable var2 = new LogicalVariable();
			var1.qubits.addAll(Arrays.asList(new Integer[] {84, 85, 90}));
			var2.qubits.addAll(Arrays.asList(new Integer[] {94, 102, 110}));
			var1.addConnectionWeight(mapping, 2, var2);
			var2.addConnectionWeight(mapping, 3, var1);
			assertEquals(5, mapping.getConnectionWeight(90, 94), MapperUtil.DOUBLE_TOLERANCE);
			assertEquals(5, var1.getConnectionWeight(mapping, var2), MapperUtil.DOUBLE_TOLERANCE);
			assertEquals(5, var2.getConnectionWeight(mapping, var1), MapperUtil.DOUBLE_TOLERANCE);
		}
		
		// Count number of value changes
		{
			int[] vals = new int[] {0, 1, 1, 1, 0, 0, 0, 1, 0};
			assertEquals(4, LogicalVariable.nrValueChanges(vals));
		}
		{
			int[] vals = new int[] {0};
			assertEquals(0, LogicalVariable.nrValueChanges(vals));
		}
		
		// Counting up binary vectors
		{
			int[] binary10reverse 	= new int[] {0, 1, 0, 1, 0, 0};
			int[] binary11reverse	= new int[] {1, 1, 0, 1, 0, 0};
			int[] binary12reverse	= new int[] {0, 0, 1, 1, 0, 0};
			int[] maximalVals		= new int[] {1, 1, 1, 1, 1, 1};
			// count one up from 10
			{
				int[] binaryVector = Arrays.copyOf(binary10reverse, 6);
				LogicalVariable.countOneUpOverflow(binaryVector);
				assertArrayEquals(binary11reverse, binaryVector);				
			}
			// count one up from 11
			{
				int[] binaryVector = Arrays.copyOf(binary11reverse, 6);
				LogicalVariable.countOneUpOverflow(binaryVector);
				assertArrayEquals(binary12reverse, binaryVector);
			}
			// test overflow detection
			assertFalse(LogicalVariable.countOneUpOverflow(binary10reverse));
			assertFalse(LogicalVariable.countOneUpOverflow(binary11reverse));
			assertFalse(LogicalVariable.countOneUpOverflow(binary12reverse));
			assertTrue(LogicalVariable.countOneUpOverflow(maximalVals));
		}
		
		// Test scaling for equality with qubit chain
		{
			// One side of chain is connected to positive weight the other to negative weight
			ConsolidationMappingGeneric mapping 	= new ConsolidationMappingGeneric(1, 1);
			LogicalVariable var 	= new LogicalVariable();
			var.qubits.addAll(Arrays.asList(new Integer[] {91, 92, 100, 108}));
			mapping.addWeight(91, 95, 2);
			mapping.addWeight(108, 116, -2);
			double expected = 2 + MapperUtil.EPSILON_WEIGHT;
			assertEquals(expected, var.calculateEqualityScalingForChain(mapping), MapperUtil.DOUBLE_TOLERANCE);
		}
		{
			// Both ends of chain are connected to negative weights while middle has positive weight
			ConsolidationMappingGeneric mapping 	= new ConsolidationMappingGeneric(1, 1);
			LogicalVariable var 	= new LogicalVariable();
			var.qubits.addAll(Arrays.asList(new Integer[] {91, 92, 100, 108}));
			mapping.addWeight(91, 95, -3);
			mapping.addWeight(100, 100, 2);
			mapping.addWeight(108, 116, -3);
			double expected = 3 + MapperUtil.EPSILON_WEIGHT;
			assertEquals(expected, var.calculateEqualityScalingForChain(mapping), MapperUtil.DOUBLE_TOLERANCE);
		}
		
		// Test generic scaling for equality
		{
			// One qubit is connected to positive weight, another to negative weight
			ConsolidationMappingGeneric mapping 	= new ConsolidationMappingGeneric(1, 1);
			LogicalVariable var 	= new LogicalVariable();
			var.qubits.addAll(Arrays.asList(new Integer[] {91, 92, 99, 100, 108}));
			mapping.addWeight(91, 95, 2);
			mapping.addWeight(108, 116, -2);
			double expected = 2 + MapperUtil.EPSILON_WEIGHT;
			assertEquals(expected, var.calculateEqualityScalingGeneric(mapping), MapperUtil.DOUBLE_TOLERANCE);
		}
		{
			// Both ends of chain are connected to negative weights while middle has positive weight
			ConsolidationMappingGeneric mapping 	= new ConsolidationMappingGeneric(1, 1);
			LogicalVariable var 	= new LogicalVariable();
			var.qubits.addAll(Arrays.asList(new Integer[] {99, 163, 0, 91, 92, 100, 108}));
			mapping.addWeight(91, 95, -3);
			mapping.addWeight(100, 100, 2);
			mapping.addWeight(108, 116, -3);
			double expected = 6 + MapperUtil.EPSILON_WEIGHT;
			assertEquals(expected, var.calculateEqualityScalingGeneric(mapping), MapperUtil.DOUBLE_TOLERANCE);
		}
		
		// Adding equality weights
		{
			ConsolidationMappingGeneric mapping 	= new ConsolidationMappingGeneric(1, 1);
			LogicalVariable var 	= new LogicalVariable();
			var.qubits.addAll(Arrays.asList(new Integer[] {91, 92, 100, 108}));
			mapping.addWeight(91, 95, 2);
			mapping.addWeight(108, 116, -2);
			var.addEqualityWeightsForChain(mapping);
			double scaling = 2 + MapperUtil.EPSILON_WEIGHT;
			assertEquals(scaling, mapping.getWeight(91), MapperUtil.DOUBLE_TOLERANCE);
			assertEquals(scaling, mapping.getWeight(108), MapperUtil.DOUBLE_TOLERANCE);
			assertEquals(2*scaling, mapping.getWeight(92), MapperUtil.DOUBLE_TOLERANCE);
			assertEquals(-2*scaling, mapping.getConnectionWeight(92, 100), MapperUtil.DOUBLE_TOLERANCE);
			assertEquals(-2*scaling, mapping.getConnectionWeight(100, 108), MapperUtil.DOUBLE_TOLERANCE);
		}
	}

}
