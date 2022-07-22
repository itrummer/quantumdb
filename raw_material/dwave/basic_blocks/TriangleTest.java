package raw_material.dwave.basic_blocks;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import raw_material.util.MapperUtil;

public class TriangleTest {

	@Test
	public void test() {
		
		// Triangles pointing towards North-East
		////////////////////////////////////////
		
		// Triangle consisting of only one cell
		{
			Triangle triangle = new Triangle(TriangleDirection.NORTH_EAST, 0, 4);
			assertEquals(TriangleDirection.NORTH_EAST, triangle.direction);
			assertEquals(4, triangle.nrChains);
			assertEquals(1, triangle.cellWidth);
			boolean oneUsedChain 	= triangle.chainUsed[0] || triangle.chainUsed[1] || 
					triangle.chainUsed[2] || triangle.chainUsed[3];
			boolean allChainsOk		= triangle.chainOk[0] || triangle.chainOk[1] ||
					triangle.chainOk[2] || triangle.chainOk[3];
			assertFalse(oneUsedChain);
			assertTrue(allChainsOk);
			// check set of contained qubits
			Set<Integer> expectedContained = new HashSet<Integer>();
			expectedContained.addAll(Arrays.asList(new Integer[] {0, 1, 2, 3, 4, 5, 6, 7}));
			assertEquals(8, triangle.qubits.size());
			assertTrue(triangle.qubits.equals(expectedContained));
			// check set of border qubits
			Set<Integer> expectedBorder = new HashSet<Integer>();
			expectedBorder.addAll(Arrays.asList(new Integer[] {0, 1, 2, 3, 4, 5, 6, 7}));
			assertEquals(8, triangle.borderQubits.size());
			assertTrue(triangle.borderQubits.equals(expectedBorder));
			// verify chains
			{
				Integer[] expectedChain			= new Integer[] {0, 4};
				Set<Integer> expectedChainSet 	= new HashSet<Integer>(Arrays.asList(expectedChain));
				Set<Integer> chain				= triangle.getChain(0);
				assertTrue(chain.equals(expectedChainSet));				
			}
			{
				Integer[] expectedChain			= new Integer[] {3, 7};
				Set<Integer> expectedChainSet 	= new HashSet<Integer>(Arrays.asList(expectedChain));
				Set<Integer> chain				= triangle.getChain(3);
				assertTrue(chain.equals(expectedChainSet));				
			}
		}
		// Triangle consisting of three cells
		{
			Triangle triangle = new Triangle(TriangleDirection.NORTH_EAST, 8, 8);
			assertEquals(TriangleDirection.NORTH_EAST, triangle.direction);
			assertEquals(8, triangle.nrChains);
			assertEquals(2, triangle.cellWidth);
			String expectedUsed = "[false, false, false, false, false, false, false, false]";
			String expectedOk	= "[true, true, true, true, true, true, true, true]";
			assertEquals(expectedUsed, Arrays.toString(triangle.chainUsed));
			assertEquals(expectedOk, Arrays.toString(triangle.chainOk));
			// check set of contained qubits
			Set<Integer> expectedContained = new HashSet<Integer>();
			expectedContained.addAll(MapperUtil.allQubitsInCell(8));
			expectedContained.addAll(MapperUtil.allQubitsInCell(16));
			expectedContained.addAll(MapperUtil.allQubitsInCell(80));
			assertEquals(expectedContained.size(), triangle.qubits.size());
			assertTrue(triangle.qubits.equals(expectedContained));
			// check set of border qubits
			Set<Integer> expectedBorder = new HashSet<Integer>();
			expectedBorder.addAll(MapperUtil.allQubitsInCell(8));
			expectedBorder.addAll(MapperUtil.allQubitsInCell(16));
			expectedBorder.addAll(MapperUtil.allQubitsInCell(80));
			assertEquals(expectedBorder.size(), triangle.borderQubits.size());
			assertTrue(triangle.borderQubits.equals(expectedBorder));
			// verify chains
			{
				Integer[] expectedChain			= new Integer[] {8, 12, 20};
				Set<Integer> expectedChainSet 	= new HashSet<Integer>(Arrays.asList(expectedChain));
				Set<Integer> chain				= triangle.getChain(0);
				assertTrue(chain.equals(expectedChainSet));				
			}
			{
				Integer[] expectedChain			= new Integer[] {11, 15, 23};
				Set<Integer> expectedChainSet 	= new HashSet<Integer>(Arrays.asList(expectedChain));
				Set<Integer> chain				= triangle.getChain(3);
				assertTrue(chain.equals(expectedChainSet));				
			}
			{
				Integer[] expectedChain			= new Integer[] {80, 16, 84};
				Set<Integer> expectedChainSet 	= new HashSet<Integer>(Arrays.asList(expectedChain));
				Set<Integer> chain				= triangle.getChain(4);
				assertTrue(chain.equals(expectedChainSet));				
			}
			{
				Integer[] expectedChain			= new Integer[] {83, 87, 19};
				Set<Integer> expectedChainSet 	= new HashSet<Integer>(Arrays.asList(expectedChain));
				Set<Integer> chain				= triangle.getChain(7);
				assertTrue(chain.equals(expectedChainSet));				
			}
		}
		// Large triangle with width 4
		{
			Triangle triangle = new Triangle(TriangleDirection.NORTH_EAST, 80, 16);
			assertEquals(TriangleDirection.NORTH_EAST, triangle.direction);
			assertEquals(16, triangle.nrChains);
			assertEquals(4, triangle.cellWidth);
			// check set of contained qubits
			Set<Integer> expectedContained = new HashSet<Integer>();
			expectedContained.addAll(MapperUtil.allQubitsInCell(80));
			expectedContained.addAll(MapperUtil.allQubitsInCell(88));
			expectedContained.addAll(MapperUtil.allQubitsInCell(96));
			expectedContained.addAll(MapperUtil.allQubitsInCell(104));
			expectedContained.addAll(MapperUtil.allQubitsInCell(152));
			expectedContained.addAll(MapperUtil.allQubitsInCell(160));
			expectedContained.addAll(MapperUtil.allQubitsInCell(168));
			expectedContained.addAll(MapperUtil.allQubitsInCell(224));
			expectedContained.addAll(MapperUtil.allQubitsInCell(232));
			expectedContained.addAll(MapperUtil.allQubitsInCell(296));
			assertEquals(expectedContained.size(), triangle.qubits.size());
			assertTrue(triangle.qubits.equals(expectedContained));
			// check set of border qubits
			Set<Integer> expectedBorder = new HashSet<Integer>();
			expectedBorder.addAll(MapperUtil.allQubitsInCell(80));
			expectedBorder.addAll(MapperUtil.allQubitsInCell(88));
			expectedBorder.addAll(MapperUtil.allQubitsInCell(96));
			expectedBorder.addAll(MapperUtil.allQubitsInCell(104));
			expectedBorder.addAll(MapperUtil.allQubitsInCell(152));
			expectedBorder.addAll(MapperUtil.allQubitsInCell(168));
			expectedBorder.addAll(MapperUtil.allQubitsInCell(224));
			expectedBorder.addAll(MapperUtil.allQubitsInCell(232));
			expectedBorder.addAll(MapperUtil.allQubitsInCell(296));
			assertEquals(expectedBorder.size(), triangle.borderQubits.size());
			assertTrue(triangle.borderQubits.equals(expectedBorder));
			// verify chains
			{
				Integer[] expectedChain			= new Integer[] {88, 152, 156, 164, 172};
				Set<Integer> expectedChainSet 	= new HashSet<Integer>(Arrays.asList(expectedChain));
				Set<Integer> chain				= triangle.getChain(4);
				assertTrue(chain.equals(expectedChainSet));				
			}
		}
		
		// Triangles pointing towards South-West
		////////////////////////////////////////
		
		// Triangle consisting of only one cell
		{
			Triangle triangle = new Triangle(TriangleDirection.SOUTH_WEST, 0, 4);
			assertEquals(TriangleDirection.SOUTH_WEST, triangle.direction);
			assertEquals(4, triangle.nrChains);
			assertEquals(1, triangle.cellWidth);
			boolean oneUsedChain 	= triangle.chainUsed[0] || triangle.chainUsed[1] || 
					triangle.chainUsed[2] || triangle.chainUsed[3];
			boolean allChainsOk		= triangle.chainOk[0] || triangle.chainOk[1] ||
					triangle.chainOk[2] || triangle.chainOk[3];
			assertFalse(oneUsedChain);
			assertTrue(allChainsOk);
			// check set of contained qubits
			Set<Integer> expectedContained = new HashSet<Integer>();
			expectedContained.addAll(Arrays.asList(new Integer[] {0, 1, 2, 3, 4, 5, 6, 7}));
			assertEquals(8, triangle.qubits.size());
			assertTrue(triangle.qubits.equals(expectedContained));
			// check set of border qubits
			Set<Integer> expectedBorder = new HashSet<Integer>();
			expectedBorder.addAll(Arrays.asList(new Integer[] {0, 1, 2, 3, 4, 5, 6, 7}));
			assertEquals(8, triangle.borderQubits.size());
			assertTrue(triangle.borderQubits.equals(expectedBorder));
			// verify chains
			{
				Integer[] expectedChain			= new Integer[] {0, 4};
				Set<Integer> expectedChainSet 	= new HashSet<Integer>(Arrays.asList(expectedChain));
				Set<Integer> chain				= triangle.getChain(0);
				assertTrue(chain.equals(expectedChainSet));				
			}
			{
				Integer[] expectedChain			= new Integer[] {3, 7};
				Set<Integer> expectedChainSet 	= new HashSet<Integer>(Arrays.asList(expectedChain));
				Set<Integer> chain				= triangle.getChain(3);
				assertTrue(chain.equals(expectedChainSet));				
			}
		}
		// Triangle consisting of three cells
		{
			Triangle triangle = new Triangle(TriangleDirection.SOUTH_WEST, 8, 8);
			assertEquals(TriangleDirection.SOUTH_WEST, triangle.direction);
			assertEquals(8, triangle.nrChains);
			assertEquals(2, triangle.cellWidth);
			String expectedUsed = "[false, false, false, false, false, false, false, false]";
			String expectedOk	= "[true, true, true, true, true, true, true, true]";
			assertEquals(expectedUsed, Arrays.toString(triangle.chainUsed));
			assertEquals(expectedOk, Arrays.toString(triangle.chainOk));
			// check set of contained qubits
			Set<Integer> expectedContained = new HashSet<Integer>();
			expectedContained.addAll(MapperUtil.allQubitsInCell(8));
			expectedContained.addAll(MapperUtil.allQubitsInCell(72));
			expectedContained.addAll(MapperUtil.allQubitsInCell(80));
			assertEquals(expectedContained.size(), triangle.qubits.size());
			assertTrue(triangle.qubits.equals(expectedContained));
			// check set of border qubits
			Set<Integer> expectedBorder = new HashSet<Integer>();
			expectedBorder.addAll(MapperUtil.allQubitsInCell(8));
			expectedBorder.addAll(MapperUtil.allQubitsInCell(72));
			expectedBorder.addAll(MapperUtil.allQubitsInCell(80));
			assertEquals(expectedBorder.size(), triangle.borderQubits.size());
			assertTrue(triangle.borderQubits.equals(expectedBorder));
			// verify chains
			{
				Integer[] expectedChain			= new Integer[] {8, 12, 72};
				Set<Integer> expectedChainSet 	= new HashSet<Integer>(Arrays.asList(expectedChain));
				Set<Integer> chain				= triangle.getChain(0);
				assertTrue(chain.equals(expectedChainSet));				
			}
			{
				Integer[] expectedChain			= new Integer[] {11, 15, 75};
				Set<Integer> expectedChainSet 	= new HashSet<Integer>(Arrays.asList(expectedChain));
				Set<Integer> chain				= triangle.getChain(3);
				assertTrue(chain.equals(expectedChainSet));				
			}
			{
				Integer[] expectedChain			= new Integer[] {80, 84, 76};
				Set<Integer> expectedChainSet 	= new HashSet<Integer>(Arrays.asList(expectedChain));
				Set<Integer> chain				= triangle.getChain(4);
				assertTrue(chain.equals(expectedChainSet));				
			}
			{
				Integer[] expectedChain			= new Integer[] {83, 87, 79};
				Set<Integer> expectedChainSet 	= new HashSet<Integer>(Arrays.asList(expectedChain));
				Set<Integer> chain				= triangle.getChain(7);
				assertTrue(chain.equals(expectedChainSet));				
			}
		}
		// Large triangle with width 4
		{
			Triangle triangle = new Triangle(TriangleDirection.SOUTH_WEST, 80, 16);
			assertEquals(TriangleDirection.SOUTH_WEST, triangle.direction);
			assertEquals(16, triangle.nrChains);
			assertEquals(4, triangle.cellWidth);
			// check set of contained qubits
			Set<Integer> expectedContained = new HashSet<Integer>();
			expectedContained.addAll(MapperUtil.allQubitsInCell(80));
			expectedContained.addAll(MapperUtil.allQubitsInCell(144));
			expectedContained.addAll(MapperUtil.allQubitsInCell(152));
			expectedContained.addAll(MapperUtil.allQubitsInCell(208));
			expectedContained.addAll(MapperUtil.allQubitsInCell(216));
			expectedContained.addAll(MapperUtil.allQubitsInCell(224));
			expectedContained.addAll(MapperUtil.allQubitsInCell(272));
			expectedContained.addAll(MapperUtil.allQubitsInCell(280));
			expectedContained.addAll(MapperUtil.allQubitsInCell(288));
			expectedContained.addAll(MapperUtil.allQubitsInCell(296));
			assertEquals(expectedContained.size(), triangle.qubits.size());
			assertTrue(triangle.qubits.equals(expectedContained));
			// check set of border qubits
			Set<Integer> expectedBorder = new HashSet<Integer>();
			expectedBorder.addAll(MapperUtil.allQubitsInCell(80));
			expectedBorder.addAll(MapperUtil.allQubitsInCell(144));
			expectedBorder.addAll(MapperUtil.allQubitsInCell(152));
			expectedBorder.addAll(MapperUtil.allQubitsInCell(208));
			expectedBorder.addAll(MapperUtil.allQubitsInCell(224));
			expectedBorder.addAll(MapperUtil.allQubitsInCell(272));
			expectedBorder.addAll(MapperUtil.allQubitsInCell(280));
			expectedBorder.addAll(MapperUtil.allQubitsInCell(288));
			expectedBorder.addAll(MapperUtil.allQubitsInCell(296));
			assertEquals(expectedBorder.size(), triangle.borderQubits.size());
			assertTrue(triangle.borderQubits.equals(expectedBorder));
			// verify chains
			{
				Integer[] expectedChain			= new Integer[] {152, 156, 148, 216, 280};
				Set<Integer> expectedChainSet 	= new HashSet<Integer>(Arrays.asList(expectedChain));
				Set<Integer> chain				= triangle.getChain(4);
				assertTrue(chain.equals(expectedChainSet));				
			}
		}
	}

}
