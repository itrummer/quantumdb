package raw_material.consolidation.dwave.mapper;

import static org.junit.Assert.*;
import static util.MapperUtil.*;

import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;
import java.util.List;

import org.junit.Test;

import raw_material.consolidation.dwave.variable.CapacityVariable;
import raw_material.consolidation.testcase.ConsolidationProblem;
import util.MapperUtil;
import consolidation.dwave.ConsolidationMappingGeneric;
import dwave.basic_blocks.MultiMaxBar;
import dwave.basic_blocks.OneMaxBar;
import dwave.basic_blocks.Triangle;
import dwave.variables.LogicalVariable;

public class MapperMatrixTest {

	@Test
	public void test() throws Exception {
		// create test problems
		
		// Problem 1: 1 Tenant, 2 Servers, 2 Metrics
		// Consumption:	(1,0.5)
		// Capacities:	(0.5,0.5)	(0.5,0.5)
		// Costs:		1			0.5
		ConsolidationProblem problem1 = new ConsolidationProblem(1, 2, 2, 0.5);
		problem1.setConsumption(0, 0, 1);
		problem1.setConsumption(0, 1, 0.5);
		problem1.setCapacity(0, 0, 0.5);
		problem1.setCapacity(0, 1, 0.5);
		problem1.setCapacity(1, 0, 0.5);
		problem1.setCapacity(1, 1, 0.5);
		problem1.setServerCost(0, 1);
		problem1.setServerCost(1, 0.5);
		
		// Problem 2: 1 Tenant, 3 Servers, 4 Metrics
		// Consumption:	(1, 1.5, 0, 0.5)
		// Capacities:	(1, 0.5, 1.5, 0.5)		(1.5, 0.5, 0.5, 0.5)		(1, 0.5, 0.5, 1)
		// Costs:		1	3	5
		ConsolidationProblem problem2 = new ConsolidationProblem(1, 3, 4, 0.5);
		problem2.setConsumption(0, 0, 1);
		problem2.setConsumption(0, 1, 1.5);
		problem2.setConsumption(0, 2, 0);
		problem2.setConsumption(0, 3, 0.5);
		// server 1
		problem2.setCapacity(0, 0, 1);
		problem2.setCapacity(0, 1, 0.5);
		problem2.setCapacity(0, 2, 1.5);
		problem2.setCapacity(0, 3, 0.5);
		problem2.setServerCost(0, 1);
		// server 2
		problem2.setCapacity(1, 0, 1.5);
		problem2.setCapacity(1, 1, 0.5);
		problem2.setCapacity(1, 2, 0.5);
		problem2.setCapacity(1, 3, 0.5);
		problem2.setServerCost(1, 3);
		// server 3
		problem2.setCapacity(2, 0, 1);
		problem2.setCapacity(2, 1, 0.5);
		problem2.setCapacity(2, 2, 0.5);
		problem2.setCapacity(2, 3, 1);
		problem2.setServerCost(2, 5);
		
		// Problem 3: 4 Tenants, 1 Server, 2 Metrics
		// Consumption:	(1, 1.5)	(2, 2.5)	(0, 0.5)	(1, 1.5)
		// Capacities:	(3, 1.5)
		// Cost:		1
		ConsolidationProblem problem3 = new ConsolidationProblem(4, 1, 2, 0.5);
		// tenants
		problem3.setConsumption(0, 0, 1);
		problem3.setConsumption(0, 1, 1.5);
		problem3.setConsumption(1, 0, 2);
		problem3.setConsumption(1, 1, 2.5);
		problem3.setConsumption(2, 0, 0);
		problem3.setConsumption(2, 1, 0.5);
		problem3.setConsumption(3, 0, 1);
		problem3.setConsumption(3, 1, 1.5);
		// servers
		problem3.setCapacity(0, 0, 3);
		problem3.setCapacity(0, 1, 1.5);
		problem3.setServerCost(0, 1);
		
		// Problem 4: 4 Tenants, 1 Server, 2 Metrics
		// Consumption:	(1, 1.5)	(2, 2.5)	(0, 0.5)	(1, 1.5)
		// Capacities:	(4, 1.5)
		// Cost:		1
		ConsolidationProblem problem4 = new ConsolidationProblem(4, 1, 2, 0.5);
		// tenants
		problem4.setConsumption(0, 0, 1);
		problem4.setConsumption(0, 1, 1.5);
		problem4.setConsumption(1, 0, 2);
		problem4.setConsumption(1, 1, 2.5);
		problem4.setConsumption(2, 0, 0);
		problem4.setConsumption(2, 1, 0.5);
		problem4.setConsumption(3, 0, 1);
		problem4.setConsumption(3, 1, 1.5);
		// servers
		problem4.setCapacity(0, 0, 4);
		problem4.setCapacity(0, 1, 1.5);
		problem4.setServerCost(0, 1);
		
		// Problem 5: 1 Tenant, 2 Servers, 3 Metrics
		// Consumption:	(1, 0.5, 1)
		// Capacities:	(0.5, 0.5, 1)	(0.5, 0.5, 0.5)
		// Costs:		1			0.5
		ConsolidationProblem problem5 = new ConsolidationProblem(1, 2, 3, 0.5);
		// tenant
		problem5.setConsumption(0, 0, 1);
		problem5.setConsumption(0, 1, 0.5);
		problem5.setConsumption(0, 2, 1);
		// server 1
		problem5.setCapacity(0, 0, 0.5);
		problem5.setCapacity(0, 1, 0.5);
		problem5.setCapacity(0, 2, 1);
		problem5.setServerCost(0, 1);
		// server 2
		problem5.setCapacity(1, 0, 0.5);
		problem5.setCapacity(1, 1, 0.5);
		problem5.setCapacity(1, 2, 0.5);
		problem5.setServerCost(1, 0.5);
		
		// Adding a dummy metric
		////////////////////////
		
		{
			// Problem 1 has already even number of metrics - should not be changed
			// 1 Tenant, 2 Servers, 2 Metrics
			// Consumption:	(1,0.5)
			// Capacities:	(0.5,0.5)	(0.5,0.5)
			// Costs:		1			0.5
			ConsolidationProblem adaptedProblem = MapperMatrix.evenNrMetricsProblem(problem1);
			assertEquals(1, adaptedProblem.nrTenants);
			assertEquals(2, adaptedProblem.nrServers);
			assertEquals(2, adaptedProblem.nrMetrics);
			assertEquals(0.5, adaptedProblem.getConsumption(0, 1), DOUBLE_TOLERANCE);
			assertEquals(0.5, adaptedProblem.getCapacity(0, 1), DOUBLE_TOLERANCE);
			assertEquals(0.5, adaptedProblem.getCost(1), DOUBLE_TOLERANCE);
		}
		{
			// Problem 5: need to add one dummy metric
			// 1 Tenant, 2 Servers, 3 Metrics
			// Consumption:	(1, 0.5, 1)
			// Capacities:	(0.5, 0.5, 1)	(0.5, 0.5, 0.5)
			// Costs:		1			0.5
			ConsolidationProblem adaptedProblem = MapperMatrix.evenNrMetricsProblem(problem5);
			assertEquals(1, adaptedProblem.nrTenants);
			assertEquals(2, adaptedProblem.nrServers);
			assertEquals(4, adaptedProblem.nrMetrics);
			// those values were not changed
			assertEquals(0.5, adaptedProblem.getConsumption(0, 1), DOUBLE_TOLERANCE);
			assertEquals(1, adaptedProblem.getConsumption(0, 2), DOUBLE_TOLERANCE);
			assertEquals(0.5, adaptedProblem.getCapacity(0, 1), DOUBLE_TOLERANCE);
			assertEquals(0.5, adaptedProblem.getCapacity(1, 2), DOUBLE_TOLERANCE);
			assertEquals(0.5, adaptedProblem.getCost(1), DOUBLE_TOLERANCE);
			// those dummy values were added
			assertEquals(0, adaptedProblem.getConsumption(0, 3), DOUBLE_TOLERANCE);
			assertEquals(0, adaptedProblem.getCapacity(0, 3), DOUBLE_TOLERANCE);
			assertEquals(0, adaptedProblem.getCapacity(1, 3), DOUBLE_TOLERANCE);
		}
		
		
		// Test triangle creation
		////////////////////////
		
		{
			// Problem 1: test with one cell triangles
			Triangle[][] triangles = MapperMatrix.createTriangles(problem1, 0, 3);
			assertEquals(2, triangles.length);
			assertEquals(2, triangles[0].length);
			{
				Set<Integer> expectedQubits = new TreeSet<Integer>();
				expectedQubits.addAll(Arrays.asList(new Integer[] {0, 1, 2, 3, 4, 5, 6, 7}));
				assertEquals(expectedQubits, triangles[0][0].qubits);
			}
			{
				Set<Integer> expectedQubits = new TreeSet<Integer>();
				expectedQubits.addAll(MapperUtil.allQubitsInCell(8));
				assertEquals(expectedQubits, triangles[0][1].qubits);
			}
			{
				Set<Integer> expectedQubits = new TreeSet<Integer>();
				expectedQubits.addAll(MapperUtil.allQubitsInCell(64));
				assertEquals(expectedQubits, triangles[1][0].qubits);
			}
			{
				Set<Integer> expectedQubits = new TreeSet<Integer>();
				expectedQubits.addAll(MapperUtil.allQubitsInCell(72));
				assertEquals(expectedQubits, triangles[1][1].qubits);
			}
		}
		{
			// Problem 1: test with three cell triangles
			Triangle[][] triangles = MapperMatrix.createTriangles(problem1, 0, 8);
			assertEquals(2, triangles.length);
			assertEquals(2, triangles[0].length);
			{
				Set<Integer> expectedQubits = new TreeSet<Integer>();
				expectedQubits.addAll(Arrays.asList(new Integer[] {0, 1, 2, 3, 4, 5, 6, 7}));
				expectedQubits.addAll(MapperUtil.allQubitsInCell(64));
				expectedQubits.addAll(MapperUtil.allQubitsInCell(72));
				assertEquals(expectedQubits, triangles[0][0].qubits);
			}
			{
				Set<Integer> expectedQubits = new TreeSet<Integer>();
				expectedQubits.addAll(MapperUtil.allQubitsInCell(8));
				expectedQubits.addAll(MapperUtil.allQubitsInCell(16));
				expectedQubits.addAll(MapperUtil.allQubitsInCell(80));
				assertEquals(expectedQubits, triangles[0][1].qubits);
			}
			{
				Set<Integer> expectedQubits = new TreeSet<Integer>();
				expectedQubits.addAll(MapperUtil.allQubitsInCell(128));
				expectedQubits.addAll(MapperUtil.allQubitsInCell(192));
				expectedQubits.addAll(MapperUtil.allQubitsInCell(200));
				assertEquals(expectedQubits, triangles[1][0].qubits);
			}
			{
				Set<Integer> expectedQubits = new TreeSet<Integer>();
				expectedQubits.addAll(MapperUtil.allQubitsInCell(136));
				expectedQubits.addAll(MapperUtil.allQubitsInCell(144));
				expectedQubits.addAll(MapperUtil.allQubitsInCell(208));
				assertEquals(expectedQubits, triangles[1][1].qubits);
			}
		}
		{
			// Problem 2: test with three cell triangles
			Triangle[][] triangles = MapperMatrix.createTriangles(problem2, 0, 5);
			assertEquals(3, triangles.length);
			assertEquals(4, triangles[0].length);
			// server 1
			{
				Set<Integer> expectedQubits = new TreeSet<Integer>();
				expectedQubits.addAll(Arrays.asList(new Integer[] {0, 1, 2, 3, 4, 5, 6, 7}));
				expectedQubits.addAll(MapperUtil.allQubitsInCell(64));
				expectedQubits.addAll(MapperUtil.allQubitsInCell(72));
				assertEquals(expectedQubits, triangles[0][0].qubits);
			}
			{
				Set<Integer> expectedQubits = new TreeSet<Integer>();
				expectedQubits.addAll(MapperUtil.allQubitsInCell(8));
				expectedQubits.addAll(MapperUtil.allQubitsInCell(16));
				expectedQubits.addAll(MapperUtil.allQubitsInCell(80));
				assertEquals(expectedQubits, triangles[0][1].qubits);
			}
			{
				Set<Integer> expectedQubits = new TreeSet<Integer>();
				expectedQubits.addAll(MapperUtil.allQubitsInCell(24));
				expectedQubits.addAll(MapperUtil.allQubitsInCell(88));
				expectedQubits.addAll(MapperUtil.allQubitsInCell(96));
				assertEquals(expectedQubits, triangles[0][2].qubits);
			}
			{
				Set<Integer> expectedQubits = new TreeSet<Integer>();
				expectedQubits.addAll(MapperUtil.allQubitsInCell(32));
				expectedQubits.addAll(MapperUtil.allQubitsInCell(40));
				expectedQubits.addAll(MapperUtil.allQubitsInCell(104));
				assertEquals(expectedQubits, triangles[0][3].qubits);
			}
			// server 2
			{
				Set<Integer> expectedQubits = new TreeSet<Integer>();
				expectedQubits.addAll(MapperUtil.allQubitsInCell(128));
				expectedQubits.addAll(MapperUtil.allQubitsInCell(192));
				expectedQubits.addAll(MapperUtil.allQubitsInCell(200));
				assertEquals(expectedQubits, triangles[1][0].qubits);
			}
			{
				Set<Integer> expectedQubits = new TreeSet<Integer>();
				expectedQubits.addAll(MapperUtil.allQubitsInCell(136));
				expectedQubits.addAll(MapperUtil.allQubitsInCell(144));
				expectedQubits.addAll(MapperUtil.allQubitsInCell(208));
				assertEquals(expectedQubits, triangles[1][1].qubits);
			}
			{
				Set<Integer> expectedQubits = new TreeSet<Integer>();
				expectedQubits.addAll(MapperUtil.allQubitsInCell(152));
				expectedQubits.addAll(MapperUtil.allQubitsInCell(216));
				expectedQubits.addAll(MapperUtil.allQubitsInCell(224));
				assertEquals(expectedQubits, triangles[1][2].qubits);
			}
			{
				Set<Integer> expectedQubits = new TreeSet<Integer>();
				expectedQubits.addAll(MapperUtil.allQubitsInCell(160));
				expectedQubits.addAll(MapperUtil.allQubitsInCell(168));
				expectedQubits.addAll(MapperUtil.allQubitsInCell(232));
				assertEquals(expectedQubits, triangles[1][3].qubits);
			}
			// server 3
			{
				Set<Integer> expectedQubits = new TreeSet<Integer>();
				expectedQubits.addAll(MapperUtil.allQubitsInCell(256));
				expectedQubits.addAll(MapperUtil.allQubitsInCell(320));
				expectedQubits.addAll(MapperUtil.allQubitsInCell(328));
				assertEquals(expectedQubits, triangles[2][0].qubits);
			}
			{
				Set<Integer> expectedQubits = new TreeSet<Integer>();
				expectedQubits.addAll(MapperUtil.allQubitsInCell(264));
				expectedQubits.addAll(MapperUtil.allQubitsInCell(272));
				expectedQubits.addAll(MapperUtil.allQubitsInCell(336));
				assertEquals(expectedQubits, triangles[2][1].qubits);
			}
			{
				Set<Integer> expectedQubits = new TreeSet<Integer>();
				expectedQubits.addAll(MapperUtil.allQubitsInCell(280));
				expectedQubits.addAll(MapperUtil.allQubitsInCell(344));
				expectedQubits.addAll(MapperUtil.allQubitsInCell(352));
				assertEquals(expectedQubits, triangles[2][2].qubits);
			}
			{
				Set<Integer> expectedQubits = new TreeSet<Integer>();
				expectedQubits.addAll(MapperUtil.allQubitsInCell(288));
				expectedQubits.addAll(MapperUtil.allQubitsInCell(296));
				expectedQubits.addAll(MapperUtil.allQubitsInCell(360));
				assertEquals(expectedQubits, triangles[2][3].qubits);
			}
		}
		
		// Calculation of broken chains
		///////////////////////////////
		
		{
			Triangle[][] triangles = MapperMatrix.createTriangles(problem1, 0, 3);
			assertEquals(0, MapperMatrix.maxNrBrokenChains(problem1, triangles));
		}
		{
			Triangle[][] triangles = MapperMatrix.createTriangles(problem1, 24, 3);
			assertEquals(1, MapperMatrix.maxNrBrokenChains(problem1, triangles));
		}
		{
			Triangle[][] triangles = MapperMatrix.createTriangles(problem1, 24, 5);
			assertEquals(1, MapperMatrix.maxNrBrokenChains(problem1, triangles));
		}
		{
			Triangle[][] triangles = MapperMatrix.createTriangles(problem2, 344, 1);
			assertEquals(1, MapperMatrix.maxNrBrokenChains(problem2, triangles));
		}
		
		// Calculating triangle matrix position
		///////////////////////////////////////
		
		int trianglesTopLeft1 = MapperMatrix.triangleMatrixTopLeft(problem1);
		int trianglesTopLeft2 = MapperMatrix.triangleMatrixTopLeft(problem2);
		int trianglesTopLeft3 = MapperMatrix.triangleMatrixTopLeft(problem3);
		int trianglesTopLeft4 = MapperMatrix.triangleMatrixTopLeft(problem4);
		
		assertEquals(8, trianglesTopLeft1);
		assertEquals(8, trianglesTopLeft2);
		assertEquals(16, trianglesTopLeft3);
		assertEquals(16, trianglesTopLeft4);
		
		// Checking for sufficient space
		////////////////////////////////
		
		// Problem 1: 1 Tenant, 2 Servers, 2 Metrics
		assertTrue(MapperMatrix.sufficientSpace(problem1, 8, 4));
		assertTrue(MapperMatrix.sufficientSpace(problem1, 40, 4));
		assertFalse(MapperMatrix.sufficientSpace(problem1, 48, 4));
		assertTrue(MapperMatrix.sufficientSpace(problem1, 424, 4));
		assertFalse(MapperMatrix.sufficientSpace(problem1, 488, 4));
		assertFalse(MapperMatrix.sufficientSpace(problem1, 424, 5));
		
		// Calculation of required chains
		/////////////////////////////////
		
		// Problem 1: 1 tenant; 1 capacity variable
		assertEquals(2, MapperMatrix.requiredChains(problem1, 0));
		assertEquals(3, MapperMatrix.requiredChains(problem1, 24));
		// Problem 2: 1 tenant; 2 capacity variables
		assertEquals(3, MapperMatrix.requiredChains(problem2, 192));
		assertEquals(4, MapperMatrix.requiredChains(problem2, 0));
		assertEquals(4, MapperMatrix.requiredChains(problem2, 8));
		// Problem 3: 4 tenants; 3 capacity variables
		assertEquals(8, MapperMatrix.requiredChains(problem3, 0));
		assertEquals(8, MapperMatrix.requiredChains(problem3, 16));
		// Problem 4: 4 tenants; 4 capacity variables
		assertEquals(8, MapperMatrix.requiredChains(problem4, 0));
		assertEquals(9, MapperMatrix.requiredChains(problem4, 16));
		assertEquals(9, MapperMatrix.requiredChains(problem4, 24));
		
		// Calculation of tenant chains
		///////////////////////////////
		{
			ConsolidationProblem problem = problem1;
			Triangle[][] triangles = MapperMatrix.createTriangles(problem, 0, 2);
			boolean[][] expectedChains = new boolean[][]{{true, false}, {true, false}};
			boolean[][] chains = MapperMatrix.tenantChains(problem, triangles);
			assertEquals(expectedChains.length, chains.length);
			assertEquals(expectedChains[0].length, chains[0].length);
			for (int server=0; server<problem.nrServers; ++server) {
				for (int tenant=0; tenant<problem.nrTenants; ++tenant) {
					assertEquals(expectedChains[server][tenant], chains[server][tenant]);
				}
			}
		}
		{
			ConsolidationProblem problem = problem1;
			Triangle[][] triangles = MapperMatrix.createTriangles(problem, 144, 2);
			boolean[][] expectedChains = new boolean[][]{{true, false}, {true, false}};
			boolean[][] chains = MapperMatrix.tenantChains(problem, triangles);
			assertEquals(expectedChains.length, chains.length);
			assertEquals(expectedChains[0].length, chains[0].length);
			for (int server=0; server<problem.nrServers; ++server) {
				for (int tenant=0; tenant<problem.nrTenants; ++tenant) {
					assertEquals(expectedChains[server][tenant], chains[server][tenant]);
				}
			}
		}
		{
			ConsolidationProblem problem = problem3;
			int topLeftStart = 0;
			int requiredChains = MapperMatrix.requiredChains(problem, topLeftStart);
			Triangle[][] triangles = MapperMatrix.createTriangles(problem, topLeftStart, requiredChains);
			boolean[][] expectedChains = new boolean[][]{
					{true, false, true, false, true, false, true, false}};
			boolean[][] chains = MapperMatrix.tenantChains(problem, triangles);
			assertEquals(expectedChains.length, chains.length);
			assertEquals(expectedChains[0].length, chains[0].length);
			for (int server=0; server<problem.nrServers; ++server) {
				for (int tenant=0; tenant<problem.nrTenants; ++tenant) {
					assertEquals(expectedChains[server][tenant], chains[server][tenant]);
				}
			}
		}
		{
			ConsolidationProblem problem = problem3;
			int topLeftStart = 88;
			int requiredChains = MapperMatrix.requiredChains(problem, topLeftStart);
			Triangle[][] triangles = MapperMatrix.createTriangles(problem, topLeftStart, requiredChains);
			boolean[][] expectedChains = new boolean[][]{
					{true, false, false, true, true, false, true, false}};
			boolean[][] chains = MapperMatrix.tenantChains(problem, triangles);
			assertEquals(expectedChains.length, chains.length);
			assertEquals(expectedChains[0].length, chains[0].length);
			for (int server=0; server<problem.nrServers; ++server) {
				for (int tenant=0; tenant<problem.nrTenants; ++tenant) {
					assertEquals(expectedChains[server][tenant], chains[server][tenant]);
				}
			}
		}
		
		// Creating assignment bar
		//////////////////////////
		
		int requiredChains1 = MapperMatrix.requiredChains(problem1, trianglesTopLeft1);
		int requiredChains2 = MapperMatrix.requiredChains(problem2, trianglesTopLeft2);
		int requiredChains3 = MapperMatrix.requiredChains(problem3, trianglesTopLeft3);
		int requiredChains4 = MapperMatrix.requiredChains(problem4, trianglesTopLeft4);
		
		assertEquals(2, requiredChains1);
		assertEquals(4, requiredChains2);
		assertEquals(8, requiredChains3);
		assertEquals(9, requiredChains4);
		
		Triangle[][] triangles1 = MapperMatrix.createTriangles(problem1, trianglesTopLeft1, requiredChains1);
		Triangle[][] triangles2 = MapperMatrix.createTriangles(problem2, trianglesTopLeft2, requiredChains2);
		Triangle[][] triangles3 = MapperMatrix.createTriangles(problem3, trianglesTopLeft3, requiredChains3);
		@SuppressWarnings("unused")
		Triangle[][] triangles4 = MapperMatrix.createTriangles(problem4, trianglesTopLeft4, requiredChains4);
		
		boolean[][] tenantChains1 = MapperMatrix.tenantChains(problem1, triangles1);
		boolean[][] tenantChains2 = MapperMatrix.tenantChains(problem2, triangles2);
		boolean[][] tenantChains3 = MapperMatrix.tenantChains(problem3, triangles3);
		// Problem 4: cannot assign tenants since two consecutive chains are blocked
		// boolean[][] tenantChains4 = MapperMatrix.tenantChains(problem4, triangles4);
		
		MultiMaxBar assignmentBar1 = MapperMatrix.createAssignmentBar(problem1, requiredChains1, tenantChains1);
		MultiMaxBar assignmentBar2 = MapperMatrix.createAssignmentBar(problem2, requiredChains2, tenantChains2);
		MultiMaxBar assignmentBar3 = MapperMatrix.createAssignmentBar(problem3, requiredChains3, tenantChains3);

		// Problem 1 (1 tenant, 2 servers, 2 metrics)
		{
			Set<Integer> expectedQubits = new TreeSet<Integer>();
			expectedQubits.addAll(Arrays.asList(new Integer[] {4}));
			assertEquals(expectedQubits, assignmentBar1.getInputQubits(0, 0));
		}
		{
			Set<Integer> expectedQubits = new TreeSet<Integer>();
			expectedQubits.addAll(Arrays.asList(new Integer[] {68}));
			assertEquals(expectedQubits, assignmentBar1.getInputQubits(1, 0));
		}
		{
			Set<Integer> expectedQubits = new TreeSet<Integer>();
			expectedQubits.addAll(Arrays.asList(new Integer[] {65, 69}));
			assertEquals(expectedQubits, assignmentBar1.getOutputQubits(0));
		}
		{
			Set<Integer> expectedQubits = new TreeSet<Integer>();
			expectedQubits.addAll(Arrays.asList(new Integer[] {0, 5, 64}));
			assertEquals(expectedQubits, assignmentBar1.getAuxQubits(0, 0));
		}
		// Problem 2 (1 tenant, 3 servers, 4 metrics)
		{
			Set<Integer> expectedQubits = new TreeSet<Integer>();
			expectedQubits.addAll(Arrays.asList(new Integer[] {128, 133}));
			assertEquals(expectedQubits, assignmentBar2.getAuxQubits(2, 0));
		}
		// Problem 3 (4 tenants, 1 server, 2 metrics)
		{
			Set<Integer> expectedQubits = new TreeSet<Integer>();
			expectedQubits.addAll(Arrays.asList(new Integer[] {8, 13}));
			assertEquals(expectedQubits, assignmentBar3.getAuxQubits(0, 0));
		}
		{
			Set<Integer> expectedQubits = new TreeSet<Integer>();
			expectedQubits.addAll(Arrays.asList(new Integer[] {9, 15}));
			assertEquals(expectedQubits, assignmentBar3.getAuxQubits(0, 1));
		}
		{
			Set<Integer> expectedQubits = new TreeSet<Integer>();
			expectedQubits.addAll(Arrays.asList(new Integer[] {64, 69}));
			assertEquals(expectedQubits, assignmentBar3.getAuxQubits(0, 2));
		}
		{
			Set<Integer> expectedQubits = new TreeSet<Integer>();
			expectedQubits.addAll(Arrays.asList(new Integer[] {65, 71}));
			assertEquals(expectedQubits, assignmentBar3.getAuxQubits(0, 3));
		}
		
		// Creating activation bar
		//////////////////////////
		
		OneMaxBar[] activationBars1 = MapperMatrix.createActivationBars(problem1, requiredChains1, tenantChains1);
		OneMaxBar[] activationBars2 = MapperMatrix.createActivationBars(problem2, requiredChains2, tenantChains2);
		OneMaxBar[] activationBars3 = MapperMatrix.createActivationBars(problem3, requiredChains3, tenantChains3);
		
		// Problem 1 (1 tenant, 2 servers, 2 metrics)
		assertEquals(2, activationBars1.length);
		// Problem 2 (1 tenant, 3 servers, 4 metrics)
		assertEquals(3, activationBars2.length);
		// Problem 3 (4 tenants, 1 server, 2 metrics)
		assertEquals(1, activationBars3.length);
		
		// Problem 1 (1 tenant, 2 servers, 2 metrics)
		{
			Set<Integer> expectedQubits = new TreeSet<Integer>();
			expectedQubits.addAll(Arrays.asList(new Integer[] {24, 28, 29}));
			assertEquals(expectedQubits, activationBars1[0].qubits);
		}
		{
			Set<Integer> expectedQubits = new TreeSet<Integer>();
			expectedQubits.addAll(Arrays.asList(new Integer[] {88, 92, 93}));
			assertEquals(expectedQubits, activationBars1[1].qubits);
		}
		// Problem 2 (1 tenant, 3 servers, 4 metrics)
		{
			Set<Integer> expectedQubits = new TreeSet<Integer>();
			expectedQubits.addAll(Arrays.asList(new Integer[] {40, 44, 45}));
			assertEquals(expectedQubits, activationBars2[0].qubits);
		}
		{
			Set<Integer> expectedQubits = new TreeSet<Integer>();
			expectedQubits.addAll(Arrays.asList(new Integer[] {104, 108, 109}));
			assertEquals(expectedQubits, activationBars2[1].qubits);
		}
		{
			Set<Integer> expectedQubits = new TreeSet<Integer>();
			expectedQubits.addAll(Arrays.asList(new Integer[] {168, 172, 173}));
			assertEquals(expectedQubits, activationBars2[2].qubits);
		}
		// Problem 3 (4 tenants, 1 server, 2 metrics)
		{
			Set<Integer> expectedQubits = new TreeSet<Integer>();
			expectedQubits.addAll(Arrays.asList(new Integer[] {
					40, 44, 45, 42, 46, 47, 104, 108, 109, 106, 107, 110, 111}));
			assertEquals(expectedQubits, activationBars3[0].qubits);
		}
		
		// Assign tenant variables
		//////////////////////////
		
		LogicalVariable[][] tenantVars1 = MapperMatrix.assignTenantVars(
				problem1, triangles1, assignmentBar1, activationBars1, tenantChains1);
		LogicalVariable[][] tenantVars2 = MapperMatrix.assignTenantVars(
				problem2, triangles2, assignmentBar2, activationBars2, tenantChains2);
		LogicalVariable[][] tenantVars3 = MapperMatrix.assignTenantVars(
				problem3, triangles3, assignmentBar3, activationBars3, tenantChains3);
		
		// Problem 1 (1 tenant, 2 servers, 2 metrics)
		{
			Set<Integer> expectedQubits = new TreeSet<Integer>();
			expectedQubits.addAll(Arrays.asList(new Integer[] {
					4, 8, 12, 16, 20, 28}));
			assertEquals(expectedQubits, tenantVars1[0][0].qubits);
		}
		{
			Set<Integer> expectedQubits = new TreeSet<Integer>();
			expectedQubits.addAll(Arrays.asList(new Integer[] {
					68, 72, 76, 80, 84, 92}));
			assertEquals(expectedQubits, tenantVars1[0][1].qubits);
		}
		// Problem 2 (1 tenant, 3 servers, 4 metrics)
		{
			Set<Integer> expectedQubits = new TreeSet<Integer>();
			expectedQubits.addAll(Arrays.asList(new Integer[] {
					4, 8, 12, 16, 20, 24, 28, 32, 36, 44}));
			assertEquals(expectedQubits, tenantVars2[0][0].qubits);
		}
		{
			Set<Integer> expectedQubits = new TreeSet<Integer>();
			expectedQubits.addAll(Arrays.asList(new Integer[] {
					4+64, 8+64, 12+64, 16+64, 20+64, 24+64, 28+64, 32+64, 36+64, 44+64}));
			assertEquals(expectedQubits, tenantVars2[0][1].qubits);
		}
		{
			Set<Integer> expectedQubits = new TreeSet<Integer>();
			expectedQubits.addAll(Arrays.asList(new Integer[] {
					4+128, 8+128, 12+128, 16+128, 20+128, 24+128, 28+128, 32+128, 36+128, 44+128}));
			assertEquals(expectedQubits, tenantVars2[0][2].qubits);
		}
		// Problem 3 (4 tenants, 1 server, 2 metrics)
		{
			Set<Integer> expectedQubits = new TreeSet<Integer>();
			expectedQubits.addAll(Arrays.asList(new Integer[] {
					12, 20, 16, 80, 24, 28, 36, 44}));
			assertEquals(expectedQubits, tenantVars3[0][0].qubits);
		}
		{
			Set<Integer> expectedQubits = new TreeSet<Integer>();
			expectedQubits.addAll(Arrays.asList(new Integer[] {
					12+2, 20+2, 16+2, 80+2, 24+2, 28+2, 36+2, 44+2}));
			assertEquals(expectedQubits, tenantVars3[1][0].qubits);
		}
		{
			Set<Integer> expectedQubits = new TreeSet<Integer>();
			expectedQubits.addAll(Arrays.asList(new Integer[] {
					68, 76, 84, 92, 88, 100, 96, 32, 108}));
			assertEquals(expectedQubits, tenantVars3[2][0].qubits);
		}
		{
			Set<Integer> expectedQubits = new TreeSet<Integer>();
			expectedQubits.addAll(Arrays.asList(new Integer[] {
					68+2, 76+2, 84+2, 92+2, 88+2, 100+2, 96+2, 32+2, 108+2}));
			assertEquals(expectedQubits, tenantVars3[3][0].qubits);
		}
		
		// Assign capacity variables
		////////////////////////////
		
		List<CapacityVariable>[][] capacityVars1 = MapperMatrix.assignCapacityVars(problem1, triangles1);
		List<CapacityVariable>[][] capacityVars2 = MapperMatrix.assignCapacityVars(problem2, triangles2);
		List<CapacityVariable>[][] capacityVars3 = MapperMatrix.assignCapacityVars(problem3, triangles3);
		
		// Problem 1 (1 tenant, 2 servers, 2 metrics, 1 capacity variable)
		{
			Set<Integer> expectedQubits = new TreeSet<Integer>();
			expectedQubits.addAll(Arrays.asList(new Integer[] {9, 13}));
			Set<Integer> qubits = new TreeSet<Integer>();
			for (CapacityVariable capacityVar : capacityVars1[0][0]){
				qubits.addAll(capacityVar.qubits);
			}
			assertEquals(expectedQubits, qubits);
		}
		{
			Set<Integer> expectedQubits = new TreeSet<Integer>();
			expectedQubits.addAll(Arrays.asList(new Integer[] {17, 21}));
			Set<Integer> qubits = new TreeSet<Integer>();
			for (CapacityVariable capacityVar : capacityVars1[0][1]){
				qubits.addAll(capacityVar.qubits);
			}
			assertEquals(expectedQubits, qubits);
		}
		// Problem 2 (1 tenant, 3 servers, 4 metrics)
		// Capacities:	(1, 0.5, 1.5, 0.5)		(1.5, 0.5, 0.5, 0.5)		(1, 0.5, 0.5, 1)
		// server 1 - Capacities:	(1, 0.5, 1.5, 0.5)
		{
			Set<Integer> expectedQubits = new TreeSet<Integer>();
			expectedQubits.addAll(Arrays.asList(new Integer[] {
					9, 10, 13, 14
			}));
			Set<Integer> qubits = new TreeSet<Integer>();
			for (CapacityVariable capacityVar : capacityVars2[0][0]){
				qubits.addAll(capacityVar.qubits);
			}
			assertEquals(expectedQubits, qubits);
		}
		{
			Set<Integer> expectedQubits = new TreeSet<Integer>();
			expectedQubits.addAll(Arrays.asList(new Integer[] {
					17, 21
			}));
			Set<Integer> qubits = new TreeSet<Integer>();
			for (CapacityVariable capacityVar : capacityVars2[0][1]){
				qubits.addAll(capacityVar.qubits);
			}
			assertEquals(expectedQubits, qubits);
		}
		{
			Set<Integer> expectedQubits = new TreeSet<Integer>();
			expectedQubits.addAll(Arrays.asList(new Integer[] {
					25, 29, 26, 30
			}));
			Set<Integer> qubits = new TreeSet<Integer>();
			for (CapacityVariable capacityVar : capacityVars2[0][2]){
				qubits.addAll(capacityVar.qubits);
			}
			assertEquals(expectedQubits, qubits);
		}
		{
			Set<Integer> expectedQubits = new TreeSet<Integer>();
			expectedQubits.addAll(Arrays.asList(new Integer[] {
					33, 37
			}));
			Set<Integer> qubits = new TreeSet<Integer>();
			for (CapacityVariable capacityVar : capacityVars2[0][3]){
				qubits.addAll(capacityVar.qubits);
			}
			assertEquals(expectedQubits, qubits);
		}
		// server 2 - Capacities:	(1.5, 0.5, 0.5, 0.5)
		{
			Set<Integer> expectedQubits = new TreeSet<Integer>();
			expectedQubits.addAll(Arrays.asList(new Integer[] {
					9+64, 10+64, 13+64, 14+64
			}));
			Set<Integer> qubits = new TreeSet<Integer>();
			for (CapacityVariable capacityVar : capacityVars2[1][0]){
				qubits.addAll(capacityVar.qubits);
			}
			assertEquals(expectedQubits, qubits);
		}
		{
			Set<Integer> expectedQubits = new TreeSet<Integer>();
			expectedQubits.addAll(Arrays.asList(new Integer[] {
					17+64, 21+64
			}));
			Set<Integer> qubits = new TreeSet<Integer>();
			for (CapacityVariable capacityVar : capacityVars2[1][1]){
				qubits.addAll(capacityVar.qubits);
			}
			assertEquals(expectedQubits, qubits);
		}
		{
			Set<Integer> expectedQubits = new TreeSet<Integer>();
			expectedQubits.addAll(Arrays.asList(new Integer[] {
					25+64, 29+64
			}));
			Set<Integer> qubits = new TreeSet<Integer>();
			for (CapacityVariable capacityVar : capacityVars2[1][2]){
				qubits.addAll(capacityVar.qubits);
			}
			assertEquals(expectedQubits, qubits);
		}
		{
			Set<Integer> expectedQubits = new TreeSet<Integer>();
			expectedQubits.addAll(Arrays.asList(new Integer[] {
					33+64, 37+64
			}));
			Set<Integer> qubits = new TreeSet<Integer>();
			for (CapacityVariable capacityVar : capacityVars2[1][3]){
				qubits.addAll(capacityVar.qubits);
			}
			assertEquals(expectedQubits, qubits);
		}
		// server 3 - capacities: (1, 0.5, 0.5, 1)
		{
			Set<Integer> expectedQubits = new TreeSet<Integer>();
			expectedQubits.addAll(Arrays.asList(new Integer[] {
					9+128, 10+128, 13+128, 14+128
			}));
			Set<Integer> qubits = new TreeSet<Integer>();
			for (CapacityVariable capacityVar : capacityVars2[2][0]){
				qubits.addAll(capacityVar.qubits);
			}
			assertEquals(expectedQubits, qubits);
		}
		{
			Set<Integer> expectedQubits = new TreeSet<Integer>();
			expectedQubits.addAll(Arrays.asList(new Integer[] {
					17+128, 21+128
			}));
			Set<Integer> qubits = new TreeSet<Integer>();
			for (CapacityVariable capacityVar : capacityVars2[2][1]){
				qubits.addAll(capacityVar.qubits);
			}
			assertEquals(expectedQubits, qubits);
		}
		{
			Set<Integer> expectedQubits = new TreeSet<Integer>();
			expectedQubits.addAll(Arrays.asList(new Integer[] {
					25+128, 29+128
			}));
			Set<Integer> qubits = new TreeSet<Integer>();
			for (CapacityVariable capacityVar : capacityVars2[2][2]){
				qubits.addAll(capacityVar.qubits);
			}
			assertEquals(expectedQubits, qubits);
		}
		{
			Set<Integer> expectedQubits = new TreeSet<Integer>();
			expectedQubits.addAll(Arrays.asList(new Integer[] {
					33+128, 37+128, 34+128, 38+128
			}));
			Set<Integer> qubits = new TreeSet<Integer>();
			for (CapacityVariable capacityVar : capacityVars2[2][3]){
				qubits.addAll(capacityVar.qubits);
			}
			assertEquals(expectedQubits, qubits);
		}
		// Problem 3: 4 Tenants, 1 Server, 2 Metrics; Capacities: (3, 1.5)
		{
			Set<Integer> expectedQubits = new TreeSet<Integer>();
			expectedQubits.addAll(Arrays.asList(new Integer[] {
					17, 21, 81, 
					19, 23, 83, 
					89, 93, 85
			}));
			Set<Integer> qubits = new TreeSet<Integer>();
			for (CapacityVariable capacityVar : capacityVars3[0][0]){
				qubits.addAll(capacityVar.qubits);
			}
			assertEquals(expectedQubits, qubits);
		}
		{
			Set<Integer> expectedQubits = new TreeSet<Integer>();
			expectedQubits.addAll(Arrays.asList(new Integer[] {
					25, 29, 37,
					27, 31, 39
			}));
			Set<Integer> qubits = new TreeSet<Integer>();
			for (CapacityVariable capacityVar : capacityVars3[0][1]){
				qubits.addAll(capacityVar.qubits);
			}
			assertEquals(expectedQubits, qubits);
		}
		
		// Assign auxiliary assignment variables
		////////////////////////////////////////
		
		LogicalVariable[][] asgAuxVars1 = MapperMatrix.assignAuxAssignmentVars(problem1, assignmentBar1);
		LogicalVariable[][] asgAuxVars2 = MapperMatrix.assignAuxAssignmentVars(problem2, assignmentBar2);
		LogicalVariable[][] asgAuxVars3 = MapperMatrix.assignAuxAssignmentVars(problem3, assignmentBar3);
		
		// verify dimensions
		assertEquals(2, asgAuxVars1.length);
		assertEquals(1, asgAuxVars1[0].length);
		assertEquals(3, asgAuxVars2.length);
		assertEquals(1, asgAuxVars2[0].length);
		
		// Problem 1: 1 Tenant, 2 Servers, 2 Metrics
		{
			Set<Integer> expectedQubits = new TreeSet<Integer>();
			expectedQubits.addAll(Arrays.asList(new Integer[] {
					0, 5, 64
			}));
			assertEquals(expectedQubits, asgAuxVars1[0][0].qubits);
		}
		{
			Set<Integer> expectedQubits = new TreeSet<Integer>();
			expectedQubits.addAll(Arrays.asList(new Integer[] {
					65, 69
			}));
			assertEquals(expectedQubits, asgAuxVars1[1][0].qubits);
		}
		// Problem 2: 1 Tenant, 3 Servers, 4 Metrics
		{
			Set<Integer> expectedQubits = new TreeSet<Integer>();
			expectedQubits.addAll(Arrays.asList(new Integer[] {
					0, 5, 64
			}));
			assertEquals(expectedQubits, asgAuxVars2[0][0].qubits);
		}
		{
			Set<Integer> expectedQubits = new TreeSet<Integer>();
			expectedQubits.addAll(Arrays.asList(new Integer[] {
					65, 69, 129
			}));
			assertEquals(expectedQubits, asgAuxVars2[1][0].qubits);
		}
		{
			Set<Integer> expectedQubits = new TreeSet<Integer>();
			expectedQubits.addAll(Arrays.asList(new Integer[] {
					128, 133
			}));
			assertEquals(expectedQubits, asgAuxVars2[2][0].qubits);
		}
		// Problem 3: 4 Tenants, 1 Server, 2 Metrics
		{
			Set<Integer> expectedQubits = new TreeSet<Integer>();
			expectedQubits.addAll(Arrays.asList(new Integer[] {
					8, 13
			}));
			assertEquals(expectedQubits, asgAuxVars3[0][0].qubits);
		}
		{
			Set<Integer> expectedQubits = new TreeSet<Integer>();
			expectedQubits.addAll(Arrays.asList(new Integer[] {
					9, 15
			}));
			assertEquals(expectedQubits, asgAuxVars3[0][1].qubits);
		}
		{
			Set<Integer> expectedQubits = new TreeSet<Integer>();
			expectedQubits.addAll(Arrays.asList(new Integer[] {
					64, 69
			}));
			assertEquals(expectedQubits, asgAuxVars3[0][2].qubits);
		}
		{
			Set<Integer> expectedQubits = new TreeSet<Integer>();
			expectedQubits.addAll(Arrays.asList(new Integer[] {
					65, 71
			}));
			assertEquals(expectedQubits, asgAuxVars3[0][3].qubits);
		}
		
		// Impose assignment constraints
		////////////////////////////////
		
		// Problem 1
		// 1 Tenant, 2 Servers, 2 Metrics
		// Consumption:	(1,0.5)
		// Capacities:	(0.5,0.5)	(0.5,0.5)
		// Costs:		1			0.5
		{
			ConsolidationMappingGeneric mapping = new ConsolidationMappingGeneric(1, 2);
			MapperMatrix.imposeAssignmentConstraints(problem1, tenantVars1, asgAuxVars1, mapping);
			double scaling = MapperMatrix.assignmentScaling(problem1);
			{
				// 1*scaling by equality
				assertEquals(scaling, tenantVars1[0][0].getWeight(mapping), MapperUtil.DOUBLE_TOLERANCE);
				// 1*scaling by maximum
				assertEquals(scaling, tenantVars1[0][1].getWeight(mapping), MapperUtil.DOUBLE_TOLERANCE);
				// 1*scaling by equality + 1*scaling by maximum
				assertEquals(2*scaling, asgAuxVars1[0][0].getWeight(mapping), MapperUtil.DOUBLE_TOLERANCE);
				// 1*scaling by maximum + (-1)*scaling by motivation
				assertEquals(0, asgAuxVars1[1][0].getWeight(mapping), MapperUtil.DOUBLE_TOLERANCE);
				
				// -2*scaling by maximum
				assertEquals(-2*scaling, asgAuxVars1[0][0].getConnectionWeight(mapping, tenantVars1[0][0]), 
						MapperUtil.DOUBLE_TOLERANCE);
				// 1*scaling by maximum
				assertEquals(1*scaling, asgAuxVars1[0][0].getConnectionWeight(mapping, tenantVars1[0][1]), 
						MapperUtil.DOUBLE_TOLERANCE);
				// =0
				assertEquals(0, tenantVars1[0][0].getConnectionWeight(mapping, tenantVars1[0][1]), 
						MapperUtil.DOUBLE_TOLERANCE);
				
				// =0
				assertEquals(0, asgAuxVars1[1][0].getConnectionWeight(mapping, tenantVars1[0][0]), 
						MapperUtil.DOUBLE_TOLERANCE);
				// -2*scaling by maximum
				assertEquals(-2*scaling, asgAuxVars1[1][0].getConnectionWeight(mapping, tenantVars1[0][1]), 
						MapperUtil.DOUBLE_TOLERANCE);
				// -2*scaling by maximum
				assertEquals(-2*scaling, asgAuxVars1[1][0].getConnectionWeight(mapping, asgAuxVars1[0][0]), 
						MapperUtil.DOUBLE_TOLERANCE);
			}
		}
	}
}
