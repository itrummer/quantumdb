package raw_material.util;

import static org.junit.Assert.*;

import org.junit.Test;

public class SolverUtilTest {

	@Test
	public void test() {
		assertEquals(0, SolverUtil.cplexBinaryValue(0));
		assertEquals(1, SolverUtil.cplexBinaryValue(1));
		assertEquals(0, SolverUtil.cplexBinaryValue(0.001));
		assertEquals(1, SolverUtil.cplexBinaryValue(0.999));
	}

}
