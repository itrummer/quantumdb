package raw_material.mqo.testcase;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;

/**
 * This class reads MQO problem instances that have been serialized and written to disc.
 * 
 * @author immanueltrummer
 *
 */
public class MqoProblemReader {
	/**
	 * Reads a serialized MQO problem from a file on disc.
	 * 
	 * @param filename		the name of the file containing a serialized MQO problem
	 * @return				a deserialized MQO problem object
	 * @throws Exception
	 */
	public static MqoProblem readProblem(String filename) throws Exception {
		InputStream file = new FileInputStream(filename);
		InputStream buffer = new BufferedInputStream(file);
		ObjectInput input = new ObjectInputStream (buffer);
		MqoProblem problem = (MqoProblem)input.readObject();
		input.close();
		return problem;
	}
}
