package raw_material.mqo.benchmark;

import java.io.PrintWriter;

import raw_material.mqo.cplex.LinearMqoSolver;
import raw_material.mqo.dwave.MqoMapper;
import raw_material.mqo.dwave.MqoMapping;
import raw_material.mqo.testcase.MqoProblem;
import raw_material.mqo.testcase.MqoProblemReader;
import raw_material.mqo.testcase.MqoSolution;
import raw_material.mqo.testcase.MqoTestcaseFactory;

/**
 * Runs a benchmark that compares different MQO optimizers on randomly generated test cases.
 * 
 * @author immanueltrummer
 *
 */
public class RunMqoBenchmark {
	/**
	 * Writes header row to file containing benchmark results.
	 * 
	 * @param fileName		name of the result file to create
	 * @throws Exception
	 */
	static void writeResultHeader(String fileName) throws Exception {
		PrintWriter writer = new PrintWriter(fileName);
		writer.println("testcase,millis,bestCost");
		writer.close();
	}
	static void writeForHeader(PrintWriter writer, String varName, int[] values) {
        writer.print("for " + varName + " in ");
        for (int nrQueries : values) {
        	writer.print(nrQueries + " ");
        }
        writer.println();
        writer.println("do");
	}
	/**
	 * Writes out a bash script that is used to invoke a C program that uses
	 * D-Wave solvers to solve the test cases and produces corresponding output
	 * file that are processed by the Postprocessing Java program.
	 * 
	 * @param fileName		the name of the file to create that will hold the script
	 * @throws Exception
	 */
	static void writeDwaveScript(String fileName) throws Exception {
		PrintWriter writer = new PrintWriter(fileName);
		// Bash header
		writer.println("#!/bin/bash");
		// Include dynamic D-Wave library
		writer.println("export DYLD_LIBRARY_PATH=.");
		// Iterate over number of queries
        writeForHeader(writer, "nrQueries", BenchmarkConfig.nrQueries);
        // Iterate over number of plans per query
        writeForHeader(writer, "nrPlans", BenchmarkConfig.nrPlansPerQuery);
        // Iterate over number of intermediate results
        writeForHeader(writer, "nrResults", BenchmarkConfig.nrIntermediateResults);
        // Write invocation
        writer.println("./dwave_cpart " + BenchmarkConfig.NR_TESTCASES + 
        		" QUBO_Q${nrQueries}P${nrPlans}R${nrResults}T " + 
        		BenchmarkConfig.DWAVE_SOLVER + " " + 
        		(BenchmarkConfig.USE_DWAVE_SOLVER ? "DO_SOLVE" : "-") +
        		" 6 " + "performance_Q${nrQueries}P${nrPlans}R${nrResults}");
        // Close loop over number of results
        writer.println("done");
        // Close loop over number of plans per query
        writer.println("done");
		// Close loop over number of queries
        writer.println("done");
		writer.close();
	}
	/**
	 * Generates test cases, executes some solvers and prepares execution of D-Wave solvers.
	 * 
	 * @param args			(not used)
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		// Write script that executes D-Wave solver on the QUBO problems generated next
		writeDwaveScript("dwave/run_script.sh");
		// Invoke non D-Wave solvers and generate QUBO problem files for D-Wave
		for (int nrQueries : BenchmarkConfig.nrQueries) {
			for (int nrPlans : BenchmarkConfig.nrPlansPerQuery) {
				for (int nrResults : BenchmarkConfig.nrIntermediateResults) {
					// Generate ID for this configuration that is used as file name pre/postfix
					String configurationID = BenchmarkConfig.configurationID(
							nrQueries, nrPlans, nrResults);
					// Generate alternative solvers
					LinearMqoSolver linearSolver = new LinearMqoSolver();
					// Generate file names for solver results
					String linearResultFile = "linear/linear_" + configurationID;
					// Write header row
					writeResultHeader(linearResultFile);
					// Generate and solve test cases
					for (int testcaseCtr=0; testcaseCtr<BenchmarkConfig.NR_TESTCASES; ++testcaseCtr) {
						// Generate test case and print to console
						MqoProblem problem = MqoTestcaseFactory.produce(
								nrQueries, nrPlans, nrResults);
						problem.toConsole();
						// Solve by linear CPLEX solver
						{
							System.out.println("Invoking CPLEX linear solver");
							MqoSolution solution = linearSolver.solve(problem);
							solution.toConsole();
							solution.addResultRow(linearResultFile, testcaseCtr);
						}
						// Prepare processing by D-Wave hardware and software
						{
							// Write test case to disc
							{
								System.out.println("Writing test case to disc");
								String filename = BenchmarkConfig.dwaveTestcasePath(
										nrQueries, nrPlans, nrResults, testcaseCtr);
								problem.toFile(filename);
								// Verify that the object was properly serialized
								MqoProblem problemRead = MqoProblemReader.readProblem(filename);
								assert(problemRead.equals(problem));					
							}
							// Generate and write QUBO representation to disc
							{
								System.out.println("Preprocessing for D-Wave");
								MqoMapping mapping = MqoMapper.map(problem);
								// Write out file containing only the resulting QUBO problem
								String QUBOfilename = BenchmarkConfig.dwaveQuboPath(
										nrQueries, nrPlans, nrResults, testcaseCtr);
								mapping.weightsToFile(QUBOfilename, "QUBO mapping");
								// Write out file containing the entire serialized mapping
								String mappingFilename = BenchmarkConfig.dwaveMappingPath(
										nrQueries, nrPlans, nrResults, testcaseCtr);
								mapping.toFile(mappingFilename);
							}
						}
					}					
				}
			}
		}
	}

}
