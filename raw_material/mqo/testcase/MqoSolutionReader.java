package raw_material.mqo.testcase;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.util.LinkedList;
import java.util.List;

import raw_material.mqo.dwave.MqoMapping;
import raw_material.mqo.dwave.VarMapping;

/**
 * Reads the solution to a MQO problem that was produced by one of the D-Wave solvers.
 * 
 * @author immanueltrummer
 *
 */
public class MqoSolutionReader {
	/**
	 * Reads a MQO problem mapping from a file. The mapping contains a QUBO and
	 * the semantic for each used qubit.
	 * 
	 * @param filename		the name of the file from which the mapping is read
	 * @return				a mapping describing how a MQO problem is mapped to a QUBO representation
	 * @throws Exception
	 */
	static MqoMapping readMapping(String filename) throws Exception {
		InputStream file = new FileInputStream(filename);
		InputStream buffer = new BufferedInputStream(file);
		ObjectInput input = new ObjectInputStream (buffer);
		MqoMapping mapping = (MqoMapping)input.readObject();
		input.close();
		return mapping;
	}
	/**
	 * Reads the solution to a QUBO problem (representing an MQO problem) from a file.
	 * The QUBO solution assigns qubit indices either to one or zero.
	 * 
	 * @param QUBOsolutionFileName	the name of the file containing the solution
	 * @return						an array assigning qubits to values
	 * @throws Exception
	 */
	static Integer[] readQUBOsolution(String QUBOsolutionFileName) throws Exception {
		// This will contain the result
		List<Integer> solutionValues = new LinkedList<Integer>();
		// Open reader
		FileReader fileReader = new FileReader(QUBOsolutionFileName);
		BufferedReader bufferedReader = new BufferedReader(fileReader);
		// Skip header lines
		//bufferedReader.skip(3);
		bufferedReader.readLine();
		bufferedReader.readLine();
		bufferedReader.readLine();
		// Read qubit assignments
		int nextExpectedQubitIndex = 0;
		String line;		
		while ((line = bufferedReader.readLine()) != null) {
			String[] splits = line.split(",");
			String qubitIndexString = splits[0];
			String qubitValueString = splits[1];
			Integer qubitIndex = Integer.parseInt(qubitIndexString);
			Integer qubitValue = Integer.parseInt(qubitValueString);
			assert(qubitIndex == nextExpectedQubitIndex);
			solutionValues.add(qubitValue);
			++nextExpectedQubitIndex;
		}
		fileReader.close();
		// Transform solution into array
		int nrQubits = solutionValues.size();
		Integer[] solutionValuesArray = new Integer[nrQubits];
		solutionValues.toArray(solutionValuesArray);
		System.out.println("Read values for " + solutionValuesArray.length + " qubits");
		return solutionValuesArray;
	}
	/**
	 * Extract (part of) the MQO solution from the QUBO solution: which plan is executed for each query?
	 * 
	 * @param problem		the MQO problem that is represented by the QUBO problem whose solution we have
	 * @param mapping		the mapping from the MQO problem to the QUBO problem
	 * @param QUBOsolution	the solution to the QUBO problem
	 * @return				a Boolean matrix indicating which plan is executed for which query
	 */
	static boolean[][] extractExecutedPlans(MqoProblem problem, MqoMapping mapping, Integer[] QUBOsolution) {
		// Extract problem dimensions
		int nrQueries = problem.nrQueries;
		int nrPlans = problem.nrPlansPerQuery;
		// This will be returned
		boolean[][] executedPlans = new boolean[nrQueries][nrPlans];
		// Extract which plans are executed
		for (int query=0; query<nrQueries; ++query) {
			for (int plan=0; plan<nrPlans; ++plan) {
				VarMapping planVar = mapping.planVars[query][plan];
				int qubitIndx = planVar.qubit;
				executedPlans[query][plan] = (QUBOsolution[qubitIndx] == 1);
			}
		}
		return executedPlans;
	}
	/**
	 * Extract which intermediate results are generated.
	 * 
	 * @param problem		the MQO problem that is represented by the QUBO problem whose solution we have
	 * @param mapping		the mapping from the MQO problem to the QUBO problem
	 * @param QUBOsolution	the solution to the QUBO problem
	 * @return				a Boolean matrix indicating which intermediate results are generated
	 */
	static boolean[] extractGeneratedResults(MqoProblem problem, MqoMapping mapping, Integer[] QUBOsolution) {
		// Extract problem dimensions
		int nrResults = problem.nrIntermediateResults;
		// This will be returned
		boolean[] generatedResults = new boolean[nrResults];
		// Extract which results are generated
		for (int result=0; result<nrResults; ++result) {
			VarMapping resultVar = mapping.resultVars[result];
			int qubitIndex = resultVar.qubit;
			generatedResults[result] = (QUBOsolution[qubitIndex] == 1);
		}
		return generatedResults;
	}
	/**
	 * Reads a solution to a MQO problem that was produced by one of the D-Wave solvers.
	 * We need to read three files: the solution itself assigns qubit indices to values,
	 * the problem mapping maps qubit indices to problem variables, and the problem file
	 * finally yields the problem variables and parameters. 
	 * 
	 * @param testcaseFileName		name of the test case file (the problem description)
	 * @param mappingFileName		name of the file mapping the test case to the QUBO problem
	 * @param QUBOsolutionFileName	name of the solution file to the QUBO problem
	 * @return						solution to a MQO problem
	 * @throws Exception
	 */
	public static MqoSolution readSolution(String testcaseFileName, String mappingFileName, 
			String QUBOsolutionFileName) throws Exception {
		// Read test case
		MqoProblem problem = MqoProblemReader.readProblem(testcaseFileName);
		// Read mapping from problem to QUBO
		MqoMapping mapping = readMapping(mappingFileName);
		// Read QUBO solution
		Integer[] QUBOsolution = readQUBOsolution(QUBOsolutionFileName);
		// Extract executed plans
		boolean[][] executedPlans = extractExecutedPlans(problem, mapping, QUBOsolution);
		// Extract generated results
		boolean[] generatedResults = extractGeneratedResults(problem, mapping, QUBOsolution);
		// Return MQO solution
		return new MqoSolution(problem, -1, executedPlans, generatedResults, null);
	}
}
