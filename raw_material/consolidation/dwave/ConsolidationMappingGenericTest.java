package raw_material.consolidation.dwave;

import static org.junit.Assert.*;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import util.MapperUtil;

public class ConsolidationMappingGenericTest {

	@Test
	public void test() {
		{
			ConsolidationMappingGeneric mapping = new ConsolidationMappingGeneric(2, 3);
			mapping.addWeight(0, 0, -2);
			assertEquals(2, mapping.getMaxAbsWeight(true, true), MapperUtil.DOUBLE_TOLERANCE);
			assertEquals(2, mapping.getMinAbsWeightGtZero(true, true), MapperUtil.DOUBLE_TOLERANCE);
			assertEquals(0, mapping.getMaxAbsWeight(false, true), MapperUtil.DOUBLE_TOLERANCE);
			assertEquals(Double.POSITIVE_INFINITY, mapping.getMinAbsWeightGtZero(false, true), MapperUtil.DOUBLE_TOLERANCE);
			assertEquals(2, mapping.getMaxAbsWeight(true, false), MapperUtil.DOUBLE_TOLERANCE);
			assertEquals(2, mapping.getMinAbsWeightGtZero(true, false), MapperUtil.DOUBLE_TOLERANCE);
			mapping.addWeight(0, 4, 4.5);
			assertEquals(4.5, mapping.getMaxAbsWeight(true, true), MapperUtil.DOUBLE_TOLERANCE);
			assertEquals(2, mapping.getMinAbsWeightGtZero(true, true), MapperUtil.DOUBLE_TOLERANCE);
			assertEquals(4.5, mapping.getMaxAbsWeight(false, true), MapperUtil.DOUBLE_TOLERANCE);
			assertEquals(4.5, mapping.getMinAbsWeightGtZero(false, true), MapperUtil.DOUBLE_TOLERANCE);
			assertEquals(2, mapping.getMaxAbsWeight(true, false), MapperUtil.DOUBLE_TOLERANCE);
			assertEquals(2, mapping.getMinAbsWeightGtZero(true, false), MapperUtil.DOUBLE_TOLERANCE);
			assertEquals(0, mapping.getMaxAbsWeight(false, false), MapperUtil.DOUBLE_TOLERANCE);
			mapping.setTenantIndex(1, 1, 2);
			assertEquals(2, mapping.getTenantIndex(1, 1));
			mapping.setServerIndex(1, 3);
			assertEquals(3, mapping.getServerIndex(1));
			Set<Integer> qubits = new HashSet<Integer>();
			mapping.addConsistentQubits(qubits);
			assertEquals(1, mapping.getConsistentQubits().size());
		}
	}
}
