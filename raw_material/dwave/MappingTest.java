package raw_material.dwave;

import static org.junit.Assert.*;

import org.junit.Test;

import raw_material.dwave.adjacency.DwaveMatrix;
import raw_material.util.TestUtil;

public class MappingTest {

	@Test
	public void test() {
		Mapping mapping = new Mapping(new DwaveMatrix());
		mapping.addWeight(9, 9, 2);
		mapping.addWeight(9, 9, 1);
		assertEquals(3, mapping.getWeight(9), TestUtil.DOUBLE_TOLERANCE);
		mapping.addWeight(100, 92, 1);
		mapping.addWeight(92, 100, 2);
		assertEquals(3, mapping.getConnectionWeight(100, 92), TestUtil.DOUBLE_TOLERANCE);
	}

}
