package raw_material.consolidation.testcase;

import java.util.Arrays;

import raw_material.util.MapperUtil;

// Represents the solution to a consolidation problem.
public class ConsolidationSolution {
	// whether the tenants can all be mapped to available servers without exceeding their capacities
	public final boolean isFeasible;
	// if the problem is feasible then this is the minimal total server activation cost
	public final double minTotalCost;
	// if the problem is feasible then this array contains the assigned server for each tenant
	public final int[] assignedServer;
	public ConsolidationSolution(boolean isFeasible, double minTotalCost, int[] assignedServer) {
		super();
		assert(!isFeasible || assignedServer!=null);
		assert(!isFeasible || minTotalCost>=0);
		this.isFeasible 	= isFeasible;
		this.minTotalCost 	= minTotalCost;
		this.assignedServer = assignedServer;
	}
	// Outputs this solution to console.
	public void toConsole() {
		System.out.println("Feasibility: " + isFeasible);
		System.out.println("Minimal cost: " + minTotalCost);
		System.out.println("Tenant assignments: " + Arrays.toString(assignedServer));
	}
	// Two solutions for the same problem are equivalent if the feasibility value matches and
	// if the feasibility is true then the minimum costs must be equal, too.
	public boolean isEquivalent(ConsolidationSolution otherSolution) {
		if (isFeasible != otherSolution.isFeasible) {
			return false;
		}
		if (isFeasible) {
			if (Math.abs(minTotalCost - otherSolution.minTotalCost)>MapperUtil.DOUBLE_TOLERANCE) {
				return false;
			}
		}
		return true;
	}
}
