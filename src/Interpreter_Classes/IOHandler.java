package Interpreter_Classes;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

// static class containing all input and output methods used by the interpreter.
public class IOHandler {
	public static String getUserInput(String message) throws IOException {
		String fileName;
		InputStreamReader streamReader = new InputStreamReader(System.in);
		BufferedReader bufferedReader = new BufferedReader(streamReader);
		System.out.println(message);
		fileName = bufferedReader.readLine();
		return fileName;
	}

	public static void outputMessage(String input) {
		System.out.println(input);
	}

	public static ArrayList<String> getFileContents(String fileName, HashMap<Integer, Integer> lineMap) throws FileNotFoundException {
		File interpreterTarget = new File(fileName);
		Scanner targetScanner = new Scanner(interpreterTarget);
		ArrayList<String> output = new ArrayList<>();
		String line;
		int count = 0;
		while (targetScanner.hasNext()) {
			count++;
			line = targetScanner.nextLine().trim(); // gets rid of leading whitespace
			if (line.isEmpty()) { // checks if line is empty and avoids it if so
				continue;
			}
			// map the index of the code in the output ArrayList to the files actual line index
			lineMap.put(output.size(), count);
			output.add(line);
		}
		targetScanner.close();
		return output;
	}

	public static void displayStatesOfVariables(int index, HashMap<String, Variable> variableMap, HashMap<Integer, Integer> lineMap) {
		StringBuilder output = new StringBuilder();
		int value;
		output.append("At line ").append(lineMap.get(index)).append(" { ");
		for (String variable : variableMap.keySet()) {
			value = variableMap.get(variable).getValue();
			output.append(variable).append(" : ").append(value).append(' ');
		}
		output.append("}");
		System.out.println(output);
	}
}
