package raw_material.consolidation.testcase;

import java.util.Arrays;

// Heterogeneous server consolidation problem with arbitrary number 
// of tenants, servers, and metrics.
public class ConsolidationProblem {
	public final int nrTenants;
	public final int nrServers;
	public final int nrMetrics;
	public final double minCapacityStep;
	public double maxServerCost;
	private double[][] tenantConsumptions;	// first index is tenant, second is metric
	private double[][] serverCapacities;	// first index is server, second is metric
	private double[] serverCosts;		// energy consumption of each server
	
	public ConsolidationProblem(int nrTenants, int nrServers, int nrMetrics, double minCapacityStep) {
		assert (nrTenants >= 0 && nrServers >= 0 && nrMetrics >= 0);
		this.nrTenants = nrTenants;
		this.nrServers = nrServers;
		this.nrMetrics = nrMetrics;
		this.tenantConsumptions = new double[nrTenants][nrMetrics];
		this.serverCapacities 	= new double[nrServers][nrMetrics];
		this.serverCosts 		= new double[nrServers];
		this.maxServerCost	= 0;
		this.minCapacityStep	= minCapacityStep;
	}
	// set consumption for specific tenant and metric
	public void setConsumption(int tenant, int metric, double consumption) {
		assert(consumption >= 0);
		tenantConsumptions[tenant][metric] = consumption;
	}
	// get consumption for specific tenant and metric
	public double getConsumption(int tenant, int metric) {
		return tenantConsumptions[tenant][metric];
	}
	// set capacity for specific server and metric
	public void setCapacity(int server, int metric, double capacity) {
		assert(capacity >= 0);
		serverCapacities[server][metric] = capacity;
	}
	// get capacity for specific server and metric
	public double getCapacity(int server, int metric) {
		return serverCapacities[server][metric];
	}
	// set server energy consumption
	public void setServerCost(int server, double cost) {
		assert(cost >= 0);
		serverCosts[server] 	= cost;
		maxServerCost			= Math.max(maxServerCost, cost);
	}
	// get server energy consumption
	public double getCost(int server) {
		return serverCosts[server];
	}
	// print all parameters to console
	public void toConsole() {
		System.out.println("minDoubleStep: " + minCapacityStep);
		System.out.println("nrTenants: " + nrTenants);
		System.out.println("nrServers: " + nrServers);
		System.out.println("nrMetrics: " + nrMetrics);
		for (int tenant=0; tenant<nrTenants; ++tenant) {
			System.out.println("tenant " + tenant + " consumption: " + Arrays.toString(tenantConsumptions[tenant]));
		}
		for (int server=0; server<nrServers; ++server) {
			System.out.println("server " + server + " capacity: " + Arrays.toString(serverCapacities[server]));
		}
		System.out.println("server cost: " + Arrays.toString(serverCosts));
	}
}
