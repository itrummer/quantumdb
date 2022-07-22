package raw_material.consolidation.dwave.mapper;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.junit.Test;

import raw_material.consolidation.dwave.variable.CapacityVariable;
import raw_material.consolidation.testcase.ConsolidationProblem;
import util.MapperUtil;
import consolidation.dwave.ConsolidationMappingGeneric;
import dwave.basic_blocks.OneMaxBar;
import dwave.basic_blocks.Triangle;
import dwave.variables.LogicalVariable;

public class MapperTriangleTest {

	@Test
	public void test() throws Exception {
		// create test problems
		
		// Problem 1
		ConsolidationProblem problem1 = new ConsolidationProblem(1, 1, 1, 0.25);
		problem1.setConsumption(0, 0, 1);
		problem1.setCapacity(0, 0, 0.25);
		problem1.setServerCost(0, 0.5);
		
		// Problem 2
		ConsolidationProblem problem2 = new ConsolidationProblem(10, 1, 1, 0.25);
		for (int tenant=0; tenant<10; ++tenant) {
			problem2.setConsumption(tenant, 0, 1 + tenant);
			assertEquals(1 + tenant, problem2.getConsumption(tenant, 0), MapperUtil.DOUBLE_TOLERANCE);
		}
		problem2.setCapacity(0, 0, 0.25);
		problem2.setServerCost(0, 1);
		
		// Problem 3
		ConsolidationProblem problem3 = new ConsolidationProblem(5, 1, 1, 0.25);
		for (int tenant=0; tenant<5; ++tenant) {
			problem3.setConsumption(tenant, 0, 1.5 + tenant * 0.25);
		}
		problem3.setCapacity(0, 0, 3.25);
		problem3.setServerCost(0, 5);
		
		// Problem 4
		ConsolidationProblem problem4 = new ConsolidationProblem(5, 2, 3, 0.25);
		for (int server=0; server<2; ++server) {
			for (int metric=0; metric<3; ++metric) {
				problem4.setCapacity(server, metric, 3.25);
			}
			problem4.setServerCost(server, 0.5);
		}
		
		// Problem 5
		ConsolidationProblem problem5 = new ConsolidationProblem(1, 2, 2, 0.5);
		problem5.setConsumption(0, 0, 0.5);
		problem5.setConsumption(0, 1, 1.5);
		for (int server=0; server<2; ++server) {
			for (int metric=0; metric<2; ++metric) {
				problem5.setCapacity(server, metric, 0.5);
			}
			problem5.setServerCost(server, 1 + server * 0.5);
		}
		
		// Problem 6
		ConsolidationProblem problem6 = new ConsolidationProblem(3, 4, 1, 0.5);
		problem6.setConsumption(0, 0, 1);
		problem6.setConsumption(1, 0, 2.5);
		problem6.setConsumption(2, 0, 0.5);
		problem6.setCapacity(0, 0, 2.5);
		problem6.setCapacity(1, 0, 0.5);
		problem6.setCapacity(2, 0, 1);
		problem6.setCapacity(3, 0, 2.5);
		problem6.setServerCost(0, 0);
		problem6.setServerCost(1, 0);
		problem6.setServerCost(2, 2.5);
		problem6.setServerCost(3, 0.5);

		// creating basic building blocks
		/////////////////////////////////

		// calculating number of triangle chains
		{
			// Only one tenant to map, one capacity variable, and three
			// faulty chains.
			assertEquals(8, MapperTriangle.nrTriangleChains(problem1));
			// We can have at most one tenant per half cell - therefore 10
			// tenants require 20 chains. 3 faulty chains and 1 capacity
			// chain can be fitted between the tenants.
			assertEquals(20, MapperTriangle.nrTriangleChains(problem2));
			// We can have at most one tenant per half cell - therefore 5
			// tenants require 10 chains. 3 faulty chains and 4 capacity
			// chain cannot be fitted between the tenants.
			assertEquals(12, MapperTriangle.nrTriangleChains(problem3));
			// 5 * 2 = 10 tenant variables; 3 faulty chains; 
			// 4 * 2 * 3 = 24 capacity variables
			// Can fit 3 faulty lanes and 7 capacity variables between
			// tenants; 17 capacity variables remain
			// Total required space is 20 + 17 = 37 - must round up
			// to multiple of 4 -> 40
			assertEquals(40, MapperTriangle.nrTriangleChains(problem4));
			// 2 * 1 = 2 tenant variables; 3 faulty chains;
			// 2 * 2 * 1 = 4 capacity variables; total: 9 chains
			// rounded up: 12 chains
			assertEquals(12, MapperTriangle.nrTriangleChains(problem5));
			// 3 * 4 = 12 tenant variables; 3 faulty chains;
			// 3 + 1 + 2 + 3 = 9 capacity variables; total: 24
			assertEquals(24, MapperTriangle.nrTriangleChains(problem6));
		}
		
		// creating triangles (fourth problem is too big for mapping)
		Triangle triangle1 = MapperTriangle.createTriangle(problem1);
		Triangle triangle2 = MapperTriangle.createTriangle(problem2);
		Triangle triangle3 = MapperTriangle.createTriangle(problem3);
		Triangle triangle5 = MapperTriangle.createTriangle(problem5);
		Triangle triangle6 = MapperTriangle.createTriangle(problem6);
		
		// determining suitable chains for tenant variables
		String expectedChains1 = "[true, false]";
		String expectedChains2 = "[true, false, true, false, " + 
				"true, false, true, false, true, false, false, true, " +
				"true, false, true, false, true, false, true, false]";
		String expectedChains3 = "[true, false, true, false, true, false, true, false, true, false]";
		String expectedChains5 = "[true, false, true, false]";
		String expectedChains6 = "[true, false, true, false, " + 
		"true, false, true, false, true, false, false, true, " +
		"true, false, true, false, true, false, true, false, " +
		"true, false, true, false]";
		boolean[] chains1 = MapperTriangle.tenantChains(problem1, triangle1);
		boolean[] chains2 = MapperTriangle.tenantChains(problem2, triangle2);
		boolean[] chains3 = MapperTriangle.tenantChains(problem3, triangle3);
		boolean[] chains5 = MapperTriangle.tenantChains(problem5, triangle5);
		boolean[] chains6 = MapperTriangle.tenantChains(problem6, triangle6);
		assertEquals(expectedChains1, Arrays.toString(chains1));
		assertEquals(expectedChains2, Arrays.toString(chains2));
		assertEquals(expectedChains3, Arrays.toString(chains3));
		assertEquals(expectedChains5, Arrays.toString(chains5));
		assertEquals(expectedChains6, Arrays.toString(chains6));
		
		// creating one max bar per problem
		@SuppressWarnings("unused")
		OneMaxBar maxBar1 = new OneMaxBar(0, problem1.nrTenants, chains1, new HashSet<Integer>());
		@SuppressWarnings("unused")
		OneMaxBar maxBar2 = new OneMaxBar(0, problem2.nrTenants, chains2, new HashSet<Integer>());
		@SuppressWarnings("unused")
		OneMaxBar maxBar3 = new OneMaxBar(0, problem3.nrTenants, chains3, new HashSet<Integer>());
		
		// create all max bars for each problem
		OneMaxBar[] maxBars1 = MapperTriangle.createMaxBars(problem1, chains1);
		OneMaxBar[] maxBars2 = MapperTriangle.createMaxBars(problem2, chains2);
		OneMaxBar[] maxBars3 = MapperTriangle.createMaxBars(problem3, chains3);
		OneMaxBar[] maxBars5 = MapperTriangle.createMaxBars(problem5, chains5);
		OneMaxBar[] maxBars6 = MapperTriangle.createMaxBars(problem6, chains6);
		
		// assigning variables
		//////////////////////
		
		// tenant variables
		LogicalVariable[][] tenantVars1 = MapperTriangle.assignTenantVars(
				problem1, triangle1, maxBars1, chains1);
		LogicalVariable[][] tenantVars2 = MapperTriangle.assignTenantVars(
				problem2, triangle2, maxBars2, chains2);
		LogicalVariable[][] tenantVars3 = MapperTriangle.assignTenantVars(
				problem3, triangle3, maxBars3, chains3);
		LogicalVariable[][] tenantVars5 = MapperTriangle.assignTenantVars(
				problem5, triangle5, maxBars5, chains5);
		LogicalVariable[][] tenantVars6 = MapperTriangle.assignTenantVars(
				problem6, triangle6, maxBars6, chains6);
		
		// check vector dimensions
		assertEquals(1, tenantVars1.length);
		assertEquals(1, tenantVars1[0].length);
		assertEquals(10, tenantVars2.length);
		assertEquals(1, tenantVars2[0].length);
		assertEquals(5, tenantVars3.length);
		assertEquals(1, tenantVars3[0].length);
		assertEquals(1, tenantVars5.length);
		assertEquals(2, tenantVars5[0].length);
		assertEquals(3, tenantVars6.length);
		assertEquals(4, tenantVars6[0].length);
		
		// check location of qubits
		{
			LogicalVariable tenantVar = tenantVars1[0][0];
			Set<Integer> qubits = tenantVar.qubits;
			Set<Integer> expectedQubits = new HashSet<Integer>();
			expectedQubits.addAll(Arrays.asList(new Integer[] {4, 8, 12, 72}));
			assertEquals(expectedQubits, qubits);
		}
		{
			LogicalVariable tenantVar = tenantVars2[0][0];
			Set<Integer> qubits = tenantVar.qubits;
			Set<Integer> expectedQubits = new HashSet<Integer>();
			expectedQubits.addAll(Arrays.asList(new Integer[] {4, 8, 12, 72, 136, 200, 264}));
			assertEquals(expectedQubits, qubits);
		}
		{
			LogicalVariable tenantVar = tenantVars2[1][0];
			Set<Integer> qubits = tenantVar.qubits;
			Set<Integer> expectedQubits = new HashSet<Integer>();
			expectedQubits.addAll(Arrays.asList(new Integer[] {6, 10, 14, 74, 138, 202, 266}));
			assertEquals(expectedQubits, qubits);
		}
		{
			LogicalVariable tenantVar = tenantVars2[5][0];
			Set<Integer> qubits = tenantVar.qubits;
			Set<Integer> expectedQubits = new HashSet<Integer>();
			expectedQubits.addAll(Arrays.asList(new Integer[] {135, 143, 151, 155, 159, 219, 283}));
			assertEquals(expectedQubits, qubits);
		}
		{
			LogicalVariable tenantVar = tenantVars5[0][1];
			Set<Integer> qubits = tenantVar.qubits;
			Set<Integer> expectedQubits = new HashSet<Integer>();
			expectedQubits.addAll(Arrays.asList(new Integer[] {6, 10, 14, 74, 138}));
			assertEquals(expectedQubits, qubits);
		}
		// Problem 6
		{
			// Server 1
			{
				Set<Integer> expectedQubits = new TreeSet<Integer>();
				expectedQubits.addAll(triangle6.getChain(0));
				expectedQubits.add(4);
				Set<Integer> qubits = tenantVars6[0][0].qubits;
				assertEquals(expectedQubits, qubits);
			}
			{
				Set<Integer> expectedQubits = new TreeSet<Integer>();
				expectedQubits.addAll(triangle6.getChain(2));
				expectedQubits.add(6);
				Set<Integer> qubits = tenantVars6[1][0].qubits;
				assertEquals(expectedQubits, qubits);
			}
			{
				Set<Integer> expectedQubits = new TreeSet<Integer>();
				expectedQubits.addAll(triangle6.getChain(4));
				expectedQubits.add(68);
				Set<Integer> qubits = tenantVars6[2][0].qubits;
				assertEquals(expectedQubits, qubits);
			}
			// Server 2
			{
				Set<Integer> expectedQubits = new TreeSet<Integer>();
				expectedQubits.addAll(triangle6.getChain(6));
				expectedQubits.add(70);
				Set<Integer> qubits = tenantVars6[0][1].qubits;
				assertEquals(expectedQubits, qubits);
			}
			{
				Set<Integer> expectedQubits = new TreeSet<Integer>();
				expectedQubits.addAll(triangle6.getChain(8));
				expectedQubits.add(132);
				Set<Integer> qubits = tenantVars6[1][1].qubits;
				assertEquals(expectedQubits, qubits);
			}
			{
				Set<Integer> expectedQubits = new TreeSet<Integer>();
				expectedQubits.addAll(triangle6.getChain(11));
				expectedQubits.add(135);
				Set<Integer> qubits = tenantVars6[2][1].qubits;
				assertEquals(expectedQubits, qubits);
			}
			// Server 3
			{
				Set<Integer> expectedQubits = new TreeSet<Integer>();
				expectedQubits.addAll(triangle6.getChain(12));
				expectedQubits.add(196);
				Set<Integer> qubits = tenantVars6[0][2].qubits;
				assertEquals(expectedQubits, qubits);
			}
			{
				Set<Integer> expectedQubits = new TreeSet<Integer>();
				expectedQubits.addAll(triangle6.getChain(14));
				expectedQubits.add(198);
				Set<Integer> qubits = tenantVars6[1][2].qubits;
				assertEquals(expectedQubits, qubits);
			}
			{
				Set<Integer> expectedQubits = new TreeSet<Integer>();
				expectedQubits.addAll(triangle6.getChain(16));
				expectedQubits.add(260);
				Set<Integer> qubits = tenantVars6[2][2].qubits;
				assertEquals(expectedQubits, qubits);
			}
			// Server 4
			{
				Set<Integer> expectedQubits = new TreeSet<Integer>();
				expectedQubits.addAll(triangle6.getChain(18));
				expectedQubits.add(262);
				Set<Integer> qubits = tenantVars6[0][3].qubits;
				assertEquals(expectedQubits, qubits);
			}
			{
				Set<Integer> expectedQubits = new TreeSet<Integer>();
				expectedQubits.addAll(triangle6.getChain(20));
				expectedQubits.add(324);
				Set<Integer> qubits = tenantVars6[1][3].qubits;
				assertEquals(expectedQubits, qubits);
			}
			{
				Set<Integer> expectedQubits = new TreeSet<Integer>();
				expectedQubits.addAll(triangle6.getChain(22));
				expectedQubits.add(326);
				Set<Integer> qubits = tenantVars6[2][3].qubits;
				assertEquals(expectedQubits, qubits);
			}
		}
		
		// capacity variables
		List<CapacityVariable>[][] capacityVars1 = MapperTriangle.assignCapacityVars(problem1, triangle1);
		List<CapacityVariable>[][] capacityVars2 = MapperTriangle.assignCapacityVars(problem2, triangle2);
		List<CapacityVariable>[][] capacityVars3 = MapperTriangle.assignCapacityVars(problem3, triangle3);
		List<CapacityVariable>[][] capacityVars5 = MapperTriangle.assignCapacityVars(problem5, triangle5);
		List<CapacityVariable>[][] capacityVars6 = MapperTriangle.assignCapacityVars(problem6, triangle6);
		
		// verify dimensions
		assertEquals(1, capacityVars1.length);
		assertEquals(1, capacityVars1[0].length);
		assertEquals(1, capacityVars1[0][0].size());
		assertEquals(1, capacityVars2.length);
		assertEquals(1, capacityVars2[0].length);
		assertEquals(1, capacityVars2[0][0].size());
		assertEquals(1, capacityVars3.length);
		assertEquals(1, capacityVars3[0].length);
		assertEquals(4, capacityVars3[0][0].size());
		assertEquals(2, capacityVars5.length);
		assertEquals(2, capacityVars5[0].length);
		assertEquals(1, capacityVars5[0][0].size());
		assertEquals(4, capacityVars6.length);
		assertEquals(1, capacityVars6[0].length);
		assertEquals(3, capacityVars6[0][0].size());
		assertEquals(1, capacityVars6[1][0].size());
		assertEquals(2, capacityVars6[2][0].size());
		assertEquals(3, capacityVars6[3][0].size());

		// check capacities
		{
			// we assume no rounding errors
			List<CapacityVariable> vars = capacityVars1[0][0];
			Set<Double> expectedCapacities = new HashSet<Double>();
			expectedCapacities.addAll(Arrays.asList(new Double[] {0.25}));
			Set<Double> capacities = new HashSet<Double>();
			for (CapacityVariable var : vars) {
				capacities.add(var.capacity);
			}
			assertEquals(expectedCapacities, capacities);
		}
		{
			// we assume no rounding errors
			List<CapacityVariable> vars = capacityVars2[0][0];
			Set<Double> expectedCapacities = new HashSet<Double>();
			expectedCapacities.addAll(Arrays.asList(new Double[] {0.25}));
			Set<Double> capacities = new HashSet<Double>();
			for (CapacityVariable var : vars) {
				capacities.add(var.capacity);
			}
			assertEquals(expectedCapacities, capacities);
		}
		{
			// we assume no rounding errors
			List<CapacityVariable> vars = capacityVars3[0][0];
			Set<Double> expectedCapacities = new HashSet<Double>();
			expectedCapacities.addAll(Arrays.asList(new Double[] {0.25, 0.5, 1.0, 1.5}));
			Set<Double> capacities = new HashSet<Double>();
			for (CapacityVariable var : vars) {
				capacities.add(var.capacity);
			}
			assertEquals(expectedCapacities, capacities);
		}
		{
			// we assume no rounding errors
			List<CapacityVariable> vars = capacityVars5[0][0];
			Set<Double> expectedCapacities = new HashSet<Double>();
			expectedCapacities.addAll(Arrays.asList(new Double[] {0.5}));
			Set<Double> capacities = new HashSet<Double>();
			for (CapacityVariable var : vars) {
				capacities.add(var.capacity);
			}
			assertEquals(expectedCapacities, capacities);
		}
		{
			double[] expectedAccumulated = new double[] {2.5, 0.5, 1, 2.5};
			for (int server=0; server<4; ++server) {
				List<CapacityVariable> vars = capacityVars6[server][0];
				Set<Double> expectedCapacities = new HashSet<Double>();
				if (server==0) {
					expectedCapacities.addAll(Arrays.asList(new Double[] {0.5, 1.0, 1.0}));					
				} else if (server==1) {
					expectedCapacities.addAll(Arrays.asList(new Double[] {0.5}));
				} else if (server==2) {
					expectedCapacities.addAll(Arrays.asList(new Double[] {0.5, 0.5}));
				} else {
					expectedCapacities.addAll(Arrays.asList(new Double[] {0.5, 1.0, 1.0}));
				}
				double accumulatedCapacity = 0;
				Set<Double> capacities = new HashSet<Double>();
				for (CapacityVariable var : vars) {
					double capacity = var.capacity;
					accumulatedCapacity += capacity;
					capacities.add(capacity);
				}
				assertEquals(expectedAccumulated[server], accumulatedCapacity, MapperUtil.DOUBLE_TOLERANCE);
				assertEquals(expectedCapacities, capacities);
			}
		}
		
		// check location of qubits
		{
			List<CapacityVariable> vars = capacityVars1[0][0];
			Set<Integer> allQubits = new TreeSet<Integer>();
			for (CapacityVariable var : vars) {
				allQubits.addAll(var.qubits);
			}
			Set<Integer> expectedAllQubits = new TreeSet<Integer>();
			expectedAllQubits.addAll(Arrays.asList(new Integer[] {13, 9, 73}));
			assertEquals(expectedAllQubits, allQubits);
		}
		{
			List<CapacityVariable> vars = capacityVars2[0][0];
			Set<Integer> allQubits = new TreeSet<Integer>();
			for (CapacityVariable var : vars) {
				allQubits.addAll(var.qubits);
			}
			Set<Integer> expectedAllQubits = new TreeSet<Integer>();
			expectedAllQubits.addAll(Arrays.asList(new Integer[] {13, 9, 73, 137, 201, 265}));
			assertEquals(expectedAllQubits, allQubits);
		}
		{
			List<CapacityVariable> vars = capacityVars3[0][0];
			Set<Integer> allQubits = new TreeSet<Integer>();
			for (CapacityVariable var : vars) {
				assertEquals(4, var.qubits.size());
				allQubits.addAll(var.qubits);
			}
			Set<Integer> expectedAllQubits = new TreeSet<Integer>();
			expectedAllQubits.addAll(Arrays.asList(new Integer[] {
					9, 13, 73, 137,
					11, 15, 75, 139,
					77, 81, 85, 145, 
					79, 83, 87, 147}));
			assertEquals(expectedAllQubits, allQubits);
		}
		{
			List<CapacityVariable> vars = capacityVars5[1][1];
			Set<Integer> allQubits = new TreeSet<Integer>();
			for (CapacityVariable var : vars) {
				assertEquals(4, var.qubits.size());
				allQubits.addAll(var.qubits);
			}
			Set<Integer> expectedAllQubits = new TreeSet<Integer>();
			expectedAllQubits.addAll(Arrays.asList(new Integer[] {
					77, 81, 85, 145}));
			assertEquals(expectedAllQubits, allQubits);
		}
		// Problem 6
		// Server capacities: 	2.5		0.5		1	2.5
		{
			// Server 1
			{
				Set<Integer> allCapacityQubits = new TreeSet<Integer>();
				List<CapacityVariable> curCapacityVars = capacityVars6[0][0];
				for (CapacityVariable capacityVar : curCapacityVars) {
					allCapacityQubits.addAll(capacityVar.qubits);
				}
				Set<Integer> expectedQubits = new TreeSet<Integer>();
				expectedQubits.addAll(triangle6.getChain(1));
				expectedQubits.addAll(triangle6.getChain(3));
				expectedQubits.addAll(triangle6.getChain(5));
				assertEquals(expectedQubits, allCapacityQubits);
			}
			// Server 2
			{
				Set<Integer> allCapacityQubits = new TreeSet<Integer>();
				List<CapacityVariable> curCapacityVars = capacityVars6[1][0];
				for (CapacityVariable capacityVar : curCapacityVars) {
					allCapacityQubits.addAll(capacityVar.qubits);
				}
				Set<Integer> expectedQubits = new TreeSet<Integer>();
				expectedQubits.addAll(triangle6.getChain(7));
				assertEquals(expectedQubits, allCapacityQubits);
			}
			// Server 3
			{
				Set<Integer> allCapacityQubits = new TreeSet<Integer>();
				List<CapacityVariable> curCapacityVars = capacityVars6[2][0];
				for (CapacityVariable capacityVar : curCapacityVars) {
					allCapacityQubits.addAll(capacityVar.qubits);
				}
				Set<Integer> expectedQubits = new TreeSet<Integer>();
				expectedQubits.addAll(triangle6.getChain(9));
				expectedQubits.addAll(triangle6.getChain(13));
				assertEquals(expectedQubits, allCapacityQubits);
			}
			// Server 4
			{
				Set<Integer> allCapacityQubits = new TreeSet<Integer>();
				List<CapacityVariable> curCapacityVars = capacityVars6[3][0];
				for (CapacityVariable capacityVar : curCapacityVars) {
					allCapacityQubits.addAll(capacityVar.qubits);
				}
				Set<Integer> expectedQubits = new TreeSet<Integer>();
				expectedQubits.addAll(triangle6.getChain(15));
				expectedQubits.addAll(triangle6.getChain(17));
				expectedQubits.addAll(triangle6.getChain(19));
				assertEquals(expectedQubits, allCapacityQubits);
			}
		}
		
		// auxiliary variables
		LogicalVariable[][] auxVars1 = MapperTriangle.assignAuxActivationVars(problem1, maxBars1);
		LogicalVariable[][] auxVars2 = MapperTriangle.assignAuxActivationVars(problem2, maxBars2);
		LogicalVariable[][] auxVars3 = MapperTriangle.assignAuxActivationVars(problem3, maxBars3);
		LogicalVariable[][] auxVars5 = MapperTriangle.assignAuxActivationVars(problem5, maxBars5);
		LogicalVariable[][] auxVars6 = MapperTriangle.assignAuxActivationVars(problem6, maxBars6);
		
		// check dimensions
		assertEquals(1, auxVars1.length);
		assertEquals(1, auxVars1[0].length);
		assertEquals(1, auxVars2.length);
		assertEquals(10, auxVars2[0].length);
		assertEquals(1, auxVars3.length);
		assertEquals(5, auxVars3[0].length);
		assertEquals(2, auxVars5.length);
		assertEquals(1, auxVars5[0].length);
		assertEquals(4, auxVars6.length);
		assertEquals(3, auxVars6[0].length);
		
		// check location of qubits
		{
			LogicalVariable var = auxVars1[0][0];
			Set<Integer> qubits = var.qubits;
			Set<Integer> expectedQubits = new TreeSet<Integer>();
			expectedQubits.addAll(Arrays.asList(new Integer[] {0, 5}));
			assertEquals(expectedQubits, qubits);
		}
		{
			LogicalVariable var = auxVars2[0][2];
			Set<Integer> qubits = var.qubits;
			Set<Integer> expectedQubits = new TreeSet<Integer>();
			expectedQubits.addAll(Arrays.asList(new Integer[] {64, 69}));
			assertEquals(expectedQubits, qubits);
		}
		{
			LogicalVariable var = auxVars5[1][0];
			Set<Integer> qubits = var.qubits;
			Set<Integer> expectedQubits = new TreeSet<Integer>();
			expectedQubits.addAll(Arrays.asList(new Integer[] {2, 7}));
			assertEquals(expectedQubits, qubits);
		}
		// Problem 6
		{
			// server 1
			{
				Set<Integer> qubits 		= auxVars6[0][0].qubits;
				Set<Integer> expectedQubits = new TreeSet<Integer>();
				expectedQubits.addAll(Arrays.asList(new Integer[] {0, 5}));
				assertEquals(expectedQubits, qubits);
			}
			{
				Set<Integer> qubits 		= auxVars6[0][1].qubits;
				Set<Integer> expectedQubits = new TreeSet<Integer>();
				expectedQubits.addAll(Arrays.asList(new Integer[] {2, 7, 66}));
				assertEquals(expectedQubits, qubits);
			}
			{
				Set<Integer> qubits 		= auxVars6[0][2].qubits;
				Set<Integer> expectedQubits = new TreeSet<Integer>();
				expectedQubits.addAll(Arrays.asList(new Integer[] {64, 69}));
				assertEquals(expectedQubits, qubits);
			}
			// server 2
			{
				Set<Integer> qubits 		= auxVars6[1][0].qubits;
				Set<Integer> expectedQubits = new TreeSet<Integer>();
				expectedQubits.addAll(Arrays.asList(new Integer[] {67, 71, 131}));
				assertEquals(expectedQubits, qubits);
			}
			{
				Set<Integer> qubits 		= auxVars6[1][1].qubits;
				Set<Integer> expectedQubits = new TreeSet<Integer>();
				expectedQubits.addAll(Arrays.asList(new Integer[] {128, 133}));
				assertEquals(expectedQubits, qubits);
			}
			{
				Set<Integer> qubits 		= auxVars6[1][2].qubits;
				Set<Integer> expectedQubits = new TreeSet<Integer>();
				expectedQubits.addAll(Arrays.asList(new Integer[] {130, 134}));
				assertEquals(expectedQubits, qubits);
			}
			// server 3
			{
				Set<Integer> qubits 		= auxVars6[2][0].qubits;
				Set<Integer> expectedQubits = new TreeSet<Integer>();
				expectedQubits.addAll(Arrays.asList(new Integer[] {192, 197}));
				assertEquals(expectedQubits, qubits);
			}
			{
				Set<Integer> qubits 		= auxVars6[2][1].qubits;
				Set<Integer> expectedQubits = new TreeSet<Integer>();
				expectedQubits.addAll(Arrays.asList(new Integer[] {194, 199, 258}));
				assertEquals(expectedQubits, qubits);
			}
			{
				Set<Integer> qubits 		= auxVars6[2][2].qubits;
				Set<Integer> expectedQubits = new TreeSet<Integer>();
				expectedQubits.addAll(Arrays.asList(new Integer[] {256, 261}));
				assertEquals(expectedQubits, qubits);
			}
			// server 4
			{
				Set<Integer> qubits 		= auxVars6[3][0].qubits;
				Set<Integer> expectedQubits = new TreeSet<Integer>();
				expectedQubits.addAll(Arrays.asList(new Integer[] {259, 263, 323}));
				assertEquals(expectedQubits, qubits);
			}
			{
				Set<Integer> qubits 		= auxVars6[3][1].qubits;
				Set<Integer> expectedQubits = new TreeSet<Integer>();
				expectedQubits.addAll(Arrays.asList(new Integer[] {320, 325}));
				assertEquals(expectedQubits, qubits);
			}
			{
				Set<Integer> qubits 		= auxVars6[3][2].qubits;
				Set<Integer> expectedQubits = new TreeSet<Integer>();
				expectedQubits.addAll(Arrays.asList(new Integer[] {322, 327}));
				assertEquals(expectedQubits, qubits);
			}
		}
		
		// server activation variables
		LogicalVariable[] serverVars1 = MapperTriangle.assignServerVars(problem1, maxBars1);
		LogicalVariable[] serverVars2 = MapperTriangle.assignServerVars(problem2, maxBars2);
		LogicalVariable[] serverVars3 = MapperTriangle.assignServerVars(problem3, maxBars3);
		LogicalVariable[] serverVars5 = MapperTriangle.assignServerVars(problem5, maxBars5);
		LogicalVariable[] serverVars6 = MapperTriangle.assignServerVars(problem6, maxBars6);
		
		// check dimensions
		assertEquals(1, serverVars1.length);
		assertEquals(1, serverVars2.length);
		assertEquals(1, serverVars3.length);
		assertEquals(2, serverVars5.length);
		assertEquals(4, serverVars6.length);
		
		// check position of qubits
		{
			LogicalVariable var = serverVars1[0];
			Set<Integer> qubits = var.qubits;
			assertEquals(1, qubits.size());
			int qubit = qubits.iterator().next();
			assertTrue(qubit == 0 || qubit == 5);
		}
		{
			LogicalVariable var = serverVars2[0];
			Set<Integer> qubits = var.qubits;
			assertEquals(1, qubits.size());
			int qubit = qubits.iterator().next();
			assertTrue(qubit == 258 || qubit == 263);
		}
		{
			LogicalVariable var = serverVars3[0];
			Set<Integer> qubits = var.qubits;
			assertEquals(1, qubits.size());
			int qubit = qubits.iterator().next();
			assertTrue(qubit == 128 || qubit == 133);
		}
		{
			LogicalVariable var = serverVars5[1];
			Set<Integer> qubits = var.qubits;
			assertEquals(1, qubits.size());
			int qubit = qubits.iterator().next();
			assertTrue(qubit == 2 || qubit == 7);
		}
		
		// setting weights
		//////////////////
		
		// assignment scaling
		double assignmentScaling1 = MapperTriangle.assignmentScaling(problem1);
		double assignmentScaling2 = MapperTriangle.assignmentScaling(problem2);
		double assignmentScaling3 = MapperTriangle.assignmentScaling(problem3);
		double assignmentScaling5 = MapperTriangle.assignmentScaling(problem5);
		double assignmentScaling6 = MapperTriangle.assignmentScaling(problem6);
		
		assertEquals(0.5 + MapperUtil.EPSILON_WEIGHT, assignmentScaling1, MapperUtil.DOUBLE_TOLERANCE);
		assertEquals(1 + MapperUtil.EPSILON_WEIGHT, assignmentScaling2, MapperUtil.DOUBLE_TOLERANCE);
		assertEquals(5 + MapperUtil.EPSILON_WEIGHT, assignmentScaling3, MapperUtil.DOUBLE_TOLERANCE);
		assertEquals(2.5 + MapperUtil.EPSILON_WEIGHT, assignmentScaling5, MapperUtil.DOUBLE_TOLERANCE);
		assertEquals(3 + MapperUtil.EPSILON_WEIGHT, assignmentScaling6, MapperUtil.DOUBLE_TOLERANCE);
		
		// capacity scaling
		double capacityScaling1 = MapperTriangle.capacityScaling(problem1);
		double capacityScaling2 = MapperTriangle.capacityScaling(problem2);
		double capacityScaling3 = MapperTriangle.capacityScaling(problem3);
		double capacityScaling5 = MapperTriangle.capacityScaling(problem5);
		double capacityScaling6 = MapperTriangle.capacityScaling(problem6);
		
		double expectedCapacityScaling1 = MapperUtil.EPSILON_WEIGHT + 0.5/(0.25 * 0.25);
		double expectedCapacityScaling2 = MapperUtil.EPSILON_WEIGHT + 1/(0.25 * 0.25);
		double expectedCapacityScaling3 = MapperUtil.EPSILON_WEIGHT + 5/(0.25 * 0.25);
		double expectedCapacityScaling5 = MapperUtil.EPSILON_WEIGHT + 1.5/(0.5 * 0.5);
		double expectedCapacityScaling6 = MapperUtil.EPSILON_WEIGHT + 2.5/(0.5 * 0.5);
		
		assertEquals(expectedCapacityScaling1, capacityScaling1, MapperUtil.DOUBLE_TOLERANCE);
		assertEquals(expectedCapacityScaling2, capacityScaling2, MapperUtil.DOUBLE_TOLERANCE);
		assertEquals(expectedCapacityScaling3, capacityScaling3, MapperUtil.DOUBLE_TOLERANCE);
		assertEquals(expectedCapacityScaling5, capacityScaling5, MapperUtil.DOUBLE_TOLERANCE);
		assertEquals(expectedCapacityScaling6, capacityScaling6, MapperUtil.DOUBLE_TOLERANCE);
		
		// max scaling
		double maxScaling1 = MapperTriangle.activationMaxScaling(problem1);
		double maxScaling2 = MapperTriangle.activationMaxScaling(problem2);
		double maxScaling3 = MapperTriangle.activationMaxScaling(problem3);
		double maxScaling5 = MapperTriangle.activationMaxScaling(problem5);
		double maxScaling6 = MapperTriangle.activationMaxScaling(problem6);
		
		double expectedMaxScaling1 = MapperUtil.EPSILON_WEIGHT + 0.5;
		double expectedMaxScaling2 = MapperUtil.EPSILON_WEIGHT + 1;
		double expectedMaxScaling3 = MapperUtil.EPSILON_WEIGHT + 5;
		double expectedMaxScaling5 = MapperUtil.EPSILON_WEIGHT + 1.5;
		double expectedMaxScaling6 = MapperUtil.EPSILON_WEIGHT + 2.5;
		
		assertEquals(expectedMaxScaling1, maxScaling1, MapperUtil.DOUBLE_TOLERANCE);
		assertEquals(expectedMaxScaling2, maxScaling2, MapperUtil.DOUBLE_TOLERANCE);
		assertEquals(expectedMaxScaling3, maxScaling3, MapperUtil.DOUBLE_TOLERANCE);
		assertEquals(expectedMaxScaling5, maxScaling5, MapperUtil.DOUBLE_TOLERANCE);
		assertEquals(expectedMaxScaling6, maxScaling6, MapperUtil.DOUBLE_TOLERANCE);
		
		// assignment constraints
		{
			ConsolidationMappingGeneric mapping = new ConsolidationMappingGeneric(1, 1);
			MapperTriangle.imposeAssignmentConstraints(problem1, tenantVars1, mapping);
			{
				LogicalVariable tenantVar = tenantVars1[0][0];
				double weight = tenantVar.getWeight(mapping);
				assertEquals(-assignmentScaling1, weight, MapperUtil.DOUBLE_TOLERANCE);
			}
		}
		{
			ConsolidationMappingGeneric mapping = new ConsolidationMappingGeneric(10, 1);
			MapperTriangle.imposeAssignmentConstraints(problem2, tenantVars2, mapping);
			{
				LogicalVariable tenantVar = tenantVars2[5][0];
				double weight = tenantVar.getWeight(mapping);
				assertEquals(-assignmentScaling2, weight, MapperUtil.DOUBLE_TOLERANCE);
			}
		}
		{
			ConsolidationMappingGeneric mapping = new ConsolidationMappingGeneric(5, 1);
			MapperTriangle.imposeAssignmentConstraints(problem3, tenantVars3, mapping);
			{
				LogicalVariable tenantVar = tenantVars3[3][0];
				double weight = tenantVar.getWeight(mapping);
				assertEquals(-assignmentScaling3, weight, MapperUtil.DOUBLE_TOLERANCE);
			}
		}
		{
			ConsolidationMappingGeneric mapping = new ConsolidationMappingGeneric(1, 1);
			MapperTriangle.imposeAssignmentConstraints(problem5, tenantVars5, mapping);
			{
				LogicalVariable tenantVarServer1 = tenantVars5[0][0];
				LogicalVariable tenantVarServer2 = tenantVars5[0][1];
				double weight1 = tenantVarServer1.getWeight(mapping);
				double weight2 = tenantVarServer2.getWeight(mapping);
				double weight1to2 = tenantVarServer1.getConnectionWeight(mapping, tenantVarServer2);
				assertEquals(-assignmentScaling5, weight1, MapperUtil.DOUBLE_TOLERANCE);
				assertEquals(-assignmentScaling5, weight2, MapperUtil.DOUBLE_TOLERANCE);
				assertEquals(2*assignmentScaling5, weight1to2, MapperUtil.DOUBLE_TOLERANCE);
			}
		}
		{
			// Problem 6
			ConsolidationMappingGeneric mapping = new ConsolidationMappingGeneric(3, 4);
			MapperTriangle.imposeAssignmentConstraints(problem6, tenantVars6, mapping);
			for (int tenant=0; tenant<3; ++tenant) {
				for (int server=0; server<4; ++server) {
					assertEquals(-assignmentScaling6, tenantVars6[tenant][server].getWeight(mapping), 
							MapperUtil.DOUBLE_TOLERANCE);
				}
			}
			for (int tenant=0; tenant<3; ++tenant) {
				for (int server1=0; server1<4; ++server1) {
					for (int server2=0; server2<4; ++server2) {
						if (server1 != server2) {
							assertEquals(2*assignmentScaling6, 
									tenantVars6[tenant][server1].getConnectionWeight(
											mapping, tenantVars6[tenant][server2]),
											MapperUtil.DOUBLE_TOLERANCE);
							assertEquals(2*assignmentScaling6, 
									tenantVars6[tenant][server2].getConnectionWeight(
											mapping, tenantVars6[tenant][server1]),
											MapperUtil.DOUBLE_TOLERANCE);							
						}
					}
				}
			}
		}
		
		// capacity weights
		{
			// Problem 1
			ConsolidationMappingGeneric mapping = new ConsolidationMappingGeneric(1, 1);
			MapperTriangle.imposeCapacityConstraints(problem1, tenantVars1, capacityVars1, mapping);
			LogicalVariable tenantVar 		= tenantVars1[0][0];
			CapacityVariable capacityVar 	= capacityVars1[0][0].iterator().next();
			double consumption 				= 1;
			double capacity					= 0.25;
			double expTenantWeight			= consumption * consumption * capacityScaling1;
			double expCapWeight				= capacity * capacity * capacityScaling1;
			double expCapTenantWeight		= -2 * consumption * capacity * capacityScaling1;
			double tenantWeight				= tenantVar.getWeight(mapping);
			double capWeight				= capacityVar.getWeight(mapping);
			double capTenantWeight			= tenantVar.getConnectionWeight(mapping, capacityVar);
			assertEquals(expTenantWeight, tenantWeight, MapperUtil.DOUBLE_TOLERANCE);
			assertEquals(expCapWeight, capWeight, MapperUtil.DOUBLE_TOLERANCE);
			assertEquals(expCapTenantWeight, capTenantWeight, MapperUtil.DOUBLE_TOLERANCE);
		}
		{
			// Problem 2
			ConsolidationMappingGeneric mapping = new ConsolidationMappingGeneric(10, 1);
			MapperTriangle.imposeCapacityConstraints(problem2, tenantVars2, capacityVars2, mapping);
			Iterator<CapacityVariable> capacityIter = capacityVars2[0][0].iterator();
			LogicalVariable tenantVar1 		= tenantVars2[2][0];
			LogicalVariable tenantVar2 		= tenantVars2[5][0];
			CapacityVariable capacityVar 	= capacityIter.next();
			double consumption1	= 3;
			double consumption2 = 6;
			double capacity		= 0.25;
			assertEquals(consumption1, problem2.getConsumption(2, 0), MapperUtil.DOUBLE_TOLERANCE);
			assertEquals(consumption2, problem2.getConsumption(5, 0), MapperUtil.DOUBLE_TOLERANCE);
			assertEquals(capacity, problem2.getCapacity(0, 0), MapperUtil.DOUBLE_TOLERANCE);
			// expected single variable weights
			double expTenant1	= consumption1 * consumption1 * capacityScaling2;
			double expTenant2	= consumption2 * consumption2 * capacityScaling2;
			double expCapacity	= capacity * capacity * capacityScaling2;
			// expected connection weights
			double expTenant1Tenant2	= 2 * consumption1 * consumption2 * capacityScaling2;
			double expTenant1Cap		= -2 * consumption1 * capacity * capacityScaling2;
			double expTenant2Cap		= -2 * consumption2 * capacity * capacityScaling2;
			// actual single variable weights
			double tenant1Weight	= tenantVar1.getWeight(mapping);
			double tenant2Weight	= tenantVar2.getWeight(mapping);
			double capacityWeight	= capacityVar.getWeight(mapping);
			// actual connection weights
			double tenant1Tenant2Weight	= tenantVar1.getConnectionWeight(mapping, tenantVar2);
			double tenant1CapWeight		= tenantVar1.getConnectionWeight(mapping, capacityVar);
			double tenant2CapWeight		= tenantVar2.getConnectionWeight(mapping, capacityVar);
			// compare single variable weights
			assertEquals(expTenant1, tenant1Weight, MapperUtil.DOUBLE_TOLERANCE);
			assertEquals(expTenant2, tenant2Weight, MapperUtil.DOUBLE_TOLERANCE);
			assertEquals(expCapacity, capacityWeight, MapperUtil.DOUBLE_TOLERANCE);
			// compare connection weights
			assertEquals(expTenant1Tenant2, tenant1Tenant2Weight, MapperUtil.DOUBLE_TOLERANCE);
			assertEquals(expTenant1Cap, tenant1CapWeight, MapperUtil.DOUBLE_TOLERANCE);
			assertEquals(expTenant2Cap, tenant2CapWeight, MapperUtil.DOUBLE_TOLERANCE);
		}
		{
			// Problem 3
			ConsolidationMappingGeneric mapping = new ConsolidationMappingGeneric(5, 1);
			MapperTriangle.imposeCapacityConstraints(problem3, tenantVars3, capacityVars3, mapping);
			Iterator<CapacityVariable> capacityIter = capacityVars3[0][0].iterator();
			LogicalVariable tenantVar1 		= tenantVars3[1][0];
			LogicalVariable tenantVar2 		= tenantVars3[3][0];
			CapacityVariable capacityVar1 	= capacityIter.next();
			CapacityVariable capacityVar2 	= capacityIter.next();
			double consumption1	= 1.5 + 1 * 0.25;
			double consumption2 = 1.5 + 3 * 0.25;
			double capacity1	= capacityVar1.capacity;
			double capacity2	= capacityVar2.capacity;
			assertEquals(consumption1, problem3.getConsumption(1, 0), MapperUtil.DOUBLE_TOLERANCE);
			assertEquals(consumption2, problem3.getConsumption(3, 0), MapperUtil.DOUBLE_TOLERANCE);
			// expected single variable weights
			double expTenant1	= consumption1 * consumption1 * capacityScaling3;
			double expTenant2	= consumption2 * consumption2 * capacityScaling3;
			double expCap1		= capacity1 * capacity1 * capacityScaling3;
			double expCap2		= capacity2 * capacity2 * capacityScaling3;
			// expected connection weights
			double expTenant1Tenant2	= 2 * consumption1 * consumption2 * capacityScaling3;
			double expCap1Cap2			= 2 * capacity1 * capacity2 * capacityScaling3;
			double expTenant1Cap1		= -2 * consumption1 * capacity1 * capacityScaling3;
			double expTenant1Cap2		= -2 * consumption1 * capacity2 * capacityScaling3;
			double expTenant2Cap1		= -2 * consumption2 * capacity1 * capacityScaling3;
			double expTenant2Cap2		= -2 * consumption2 * capacity2 * capacityScaling3;
			// actual single variable weights
			double tenant1Weight	= tenantVar1.getWeight(mapping);
			double tenant2Weight	= tenantVar2.getWeight(mapping);
			double cap1Weight		= capacityVar1.getWeight(mapping);
			double cap2Weight		= capacityVar2.getWeight(mapping);
			// actual connection weights
			double tenant1Tenant2Weight		= tenantVar1.getConnectionWeight(mapping, tenantVar2);
			double cap1cap2					= capacityVar1.getConnectionWeight(mapping, capacityVar2);
			double tenant1Cap1Weight		= tenantVar1.getConnectionWeight(mapping, capacityVar1);
			double tenant1Cap2Weight		= tenantVar1.getConnectionWeight(mapping, capacityVar2);
			double tenant2Cap1Weight		= tenantVar2.getConnectionWeight(mapping, capacityVar1);
			double tenant2Cap2Weight		= tenantVar2.getConnectionWeight(mapping, capacityVar2);
			// compare single variable weights
			assertEquals(expTenant1, tenant1Weight, MapperUtil.DOUBLE_TOLERANCE);
			assertEquals(expTenant2, tenant2Weight, MapperUtil.DOUBLE_TOLERANCE);
			assertEquals(expCap1, cap1Weight, MapperUtil.DOUBLE_TOLERANCE);
			assertEquals(expCap2, cap2Weight, MapperUtil.DOUBLE_TOLERANCE);
			// compare connection weights
			assertEquals(expTenant1Tenant2, tenant1Tenant2Weight, MapperUtil.DOUBLE_TOLERANCE);
			assertEquals(expCap1Cap2, cap1cap2, MapperUtil.DOUBLE_TOLERANCE);
			assertEquals(expTenant1Cap1, tenant1Cap1Weight, MapperUtil.DOUBLE_TOLERANCE);
			assertEquals(expTenant1Cap2, tenant1Cap2Weight, MapperUtil.DOUBLE_TOLERANCE);
			assertEquals(expTenant2Cap1, tenant2Cap1Weight, MapperUtil.DOUBLE_TOLERANCE);
			assertEquals(expTenant2Cap2, tenant2Cap2Weight, MapperUtil.DOUBLE_TOLERANCE);
		}
		{
			// Problem 5 (one tenant, two servers, two metrics)
			ConsolidationMappingGeneric mapping = new ConsolidationMappingGeneric(1, 2);
			MapperTriangle.imposeCapacityConstraints(problem5, tenantVars5, capacityVars5, mapping);
			Iterator<CapacityVariable> capacityIterS1 = capacityVars5[0][0].iterator();
			Iterator<CapacityVariable> capacityIterS2 = capacityVars5[1][0].iterator();
			LogicalVariable tenantVarS1 	= tenantVars5[0][0];
			LogicalVariable tenantVarS2 	= tenantVars5[0][1];
			CapacityVariable capacityVarS1 	= capacityIterS1.next();
			CapacityVariable capacityVarS2 	= capacityIterS2.next();
			double consumptionM1	= 0.5;
			double consumptionM2	= 1.5;
			double capacityS1		= capacityVarS1.capacity;
			double capacityS2		= capacityVarS2.capacity;
			assertEquals(consumptionM1, problem5.getConsumption(0, 0), MapperUtil.DOUBLE_TOLERANCE);
			assertEquals(consumptionM2, problem5.getConsumption(0, 1), MapperUtil.DOUBLE_TOLERANCE);
			// expected weights
			double expTenant		= capacityScaling5 * (consumptionM1*consumptionM1 + consumptionM2*consumptionM2);
			double expCapS1			= capacityS1 * capacityS1 * capacityScaling5;
			double expCapS2			= capacityS2 * capacityS2 * capacityScaling5;
			double expTenantCap		= -2 * consumptionM1 * capacityS2 * capacityScaling5;
			double expTenantS1S2	= 0;
			// actual weights
			double tenantWeightS1		= tenantVarS1.getWeight(mapping);
			double tenantWeightS2		= tenantVarS2.getWeight(mapping);
			double capWeightS1			= capacityVarS1.getWeight(mapping);
			double capWeightS2			= capacityVarS2.getWeight(mapping);
			double tenantS2CapWeight 	= tenantVarS2.getConnectionWeight(mapping, capacityVarS2);
			double tenantS1S2			= tenantVarS2.getConnectionWeight(mapping, tenantVarS1);
			// compare weights
			assertEquals(expTenant, tenantWeightS1, MapperUtil.DOUBLE_TOLERANCE);
			assertEquals(expTenant, tenantWeightS2, MapperUtil.DOUBLE_TOLERANCE);
			assertEquals(expCapS1, capWeightS1, MapperUtil.DOUBLE_TOLERANCE);
			assertEquals(expCapS2, capWeightS2, MapperUtil.DOUBLE_TOLERANCE);
			assertEquals(expTenantCap, tenantS2CapWeight, MapperUtil.DOUBLE_TOLERANCE);
			assertEquals(expTenantS1S2, tenantS1S2, MapperUtil.DOUBLE_TOLERANCE);
		}
		{
			// Problem 6
			ConsolidationMappingGeneric mapping = new ConsolidationMappingGeneric(3, 4);
			MapperTriangle.imposeCapacityConstraints(problem6, tenantVars6, capacityVars6, mapping);
			// check tenant weights
			for (int tenant=0; tenant<3; ++tenant) {
				for (int server=0; server<4; ++server) {
					double consumption = problem6.getConsumption(tenant, 0);
					double expectedWeight = capacityScaling6 * consumption * consumption;
					LogicalVariable tenantVar = tenantVars6[tenant][server];
					double weight = tenantVar.getWeight(mapping);
					assertEquals(expectedWeight, weight, MapperUtil.DOUBLE_TOLERANCE);
				}
			}
			// check capacity weights
			for (int server=0; server<4; ++server) {
				for (CapacityVariable capacityVar : capacityVars6[server][0]) {
					double capacity = capacityVar.capacity;
					double expectedWeight = capacityScaling6 * capacity * capacity;
					double weight = capacityVar.getWeight(mapping);
					assertEquals(expectedWeight, weight, MapperUtil.DOUBLE_TOLERANCE);
				}
			}
			// check weights between tenants
			for (int server1=0; server1<4; ++server1) {
				for (int server2=0; server2<4; ++server2) {
					for (int tenant1=0; tenant1<3; ++tenant1) {
						for (int tenant2=0; tenant2<3; ++tenant2) {
							if (tenant1 != tenant2) {
								double consumption1 = problem6.getConsumption(tenant1, 0);
								double consumption2 = problem6.getConsumption(tenant2, 0);
								double expectedWeight;
								if (server1 == server2) {
									expectedWeight = capacityScaling6 * 2 * consumption1 * consumption2;
								} else {
									expectedWeight = 0;
								}
								LogicalVariable tenantVar1 = tenantVars6[tenant1][server1];
								LogicalVariable tenantVar2 = tenantVars6[tenant2][server2];
								double weight = tenantVar1.getConnectionWeight(mapping, tenantVar2);
								assertEquals(expectedWeight, weight, MapperUtil.DOUBLE_TOLERANCE);
							}
						}
					}					
				}
			}
			// check weights between capacity variables
			for (int server1=0; server1<4; ++server1) {
				for (int server2=0; server2<4; ++server2) {
					for (CapacityVariable capacityVar1 : capacityVars6[server1][0]) {
						for (CapacityVariable capacityVar2 : capacityVars6[server2][0]) {
							if (capacityVar1 != capacityVar2) {
								double capacity1 = capacityVar1.capacity;
								double capacity2 = capacityVar2.capacity;
								double expectedWeight;
								if (server1 == server2) {
									expectedWeight = capacityScaling6 * 2 * capacity1 * capacity2;
								} else {
									expectedWeight = 0;
								}
								double weight = capacityVar1.getConnectionWeight(mapping, capacityVar2);
								assertEquals(expectedWeight, weight, MapperUtil.DOUBLE_TOLERANCE);
							}
						}
					}
				}
			}
			// check weights between tenants and capacity variables
			for (int tenantServer=0; tenantServer<4; ++tenantServer) {
				for (int capacityServer=0; capacityServer<4; ++capacityServer) {
					for (int tenant=0; tenant<3; ++tenant) {
						for (CapacityVariable capacityVar : capacityVars6[capacityServer][0]) {
							LogicalVariable tenantVar = tenantVars6[tenant][tenantServer];
							double consumption = problem6.getConsumption(tenant, 0);
							double capacity = capacityVar.capacity;
							double expectedWeight;
							if (tenantServer == capacityServer) {
								expectedWeight = -capacityScaling6 * 2 * consumption * capacity;
							} else {
								expectedWeight = 0;
							}
							double weight1 = tenantVar.getConnectionWeight(mapping, capacityVar);
							double weight2 = capacityVar.getConnectionWeight(mapping, tenantVar);
							assertEquals(expectedWeight, weight1, MapperUtil.DOUBLE_TOLERANCE);
							assertEquals(expectedWeight, weight2, MapperUtil.DOUBLE_TOLERANCE);
						}
					}
				}
			}
		}
		
		// test index assignment
		////////////////////////
		{
			// Problem 6
			ConsolidationMappingGeneric mapping = new ConsolidationMappingGeneric(3, 4);
			MapperTriangle.imposeCapacityConstraints(problem6, tenantVars6, capacityVars6, mapping);
			MapperTriangle.setTenantIndices(problem6, tenantVars6, mapping);
			MapperTriangle.setServerIndices(problem6, serverVars6, mapping);
			// check assignment indices
			{
				Set<Integer> indexCandidates = new HashSet<Integer>();
				indexCandidates.addAll(triangle6.getChain(0));
				indexCandidates.add(4);
				assertTrue(indexCandidates.contains(mapping.getTenantIndex(0, 0)));
			}
			for (int tenant=0; tenant<3; ++tenant) {
				for (int server=0; server<4; ++server) {
					Set<Integer> indexCandidates = new HashSet<Integer>();
					int chainIndex = 2 * (server * 3 + tenant);
					// skip broken qubits
					if (chainIndex == 10) {
						chainIndex = 11;
					}
					indexCandidates.addAll(triangle6.getChain(chainIndex));
					indexCandidates.add(4 + (chainIndex/4)*64 + (chainIndex % 4));
					System.out.println("tenant: " + tenant);
					System.out.println("server: " + server);
					System.out.println("indexCandidates: " + indexCandidates);
					System.out.println("chainIndex: " + chainIndex);
					System.out.println("getTenantIndex: " + mapping.getTenantIndex(tenant, server));
					System.out.println("tenantVar.qubits: " + tenantVars6[tenant][server].qubits);
					assertTrue(indexCandidates.contains(mapping.getTenantIndex(tenant, server)));
				}
			}
			// check server indices
			for (int server=0; server<4; ++server) {
				OneMaxBar maxBar = maxBars6[server];
				assertEquals(maxBar.getOutput(), mapping.getServerIndex(server));
			}
		}
		
	} // testing function
}
