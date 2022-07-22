package raw_material.consolidation.dwave.variable;

import static org.junit.Assert.*;

import org.junit.Test;

import util.MapperUtil;

public class CapacityVariableTest {

	@Test
	public void test() {
		CapacityVariable var = new CapacityVariable(0.5);
		assertEquals(0.5, var.capacity, MapperUtil.DOUBLE_TOLERANCE);
	}

}
