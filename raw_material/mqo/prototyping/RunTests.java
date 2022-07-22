package raw_material.mqo.prototyping;

import ilog.concert.IloException;

public class RunTests {
	public static void main(String[] args) throws Exception {
		CplexTest ct = new CplexTest();
		ct.go();
	}
}
