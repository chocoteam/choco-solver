package benchmarks.graph;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**Class enabling to write into a csv file 
 * @author info */
public class DataWriter {

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	private final static String defaultFileName = "results.csv";
	
	//***********************************************************************************
	// METHODS
	//***********************************************************************************

	/**write text at the end of the default file
	 * @param text to write
	 */
	public static void writeText(String text){
		writeTextInto(text, defaultFileName);
	}
	
	/**write test at the end of fileName
	 * @param text to write
	 * @param fileName the file to write into
	 */
	public static void writeTextInto(String text, String fileName){
		try {
			FileWriter fw = new FileWriter(new File(fileName), true);
			fw.write(text);
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**Clear the targeted file
	 * @param fileName the file to clear
	 */
	public static void clearFile(String fileName){
		try {
			FileWriter fw = new FileWriter(new File(fileName), false);
			fw.write("");
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
