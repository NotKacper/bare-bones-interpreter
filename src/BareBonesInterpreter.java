import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Stack;

public class BareBonesInterpreter {

	private final HashMap<Integer, Integer> logicalLineToFileLine = new HashMap<>();
	private final HashMap<String, Variable> variables = new HashMap<>();
	private final Stack<ArrayList<String>> loopStack = new Stack<>();
	private final SyntaxMatcher syntaxMatcher = new SyntaxMatcher();

	public static void main(String[] args) throws IOException {
		BareBonesInterpreter interpreter = new BareBonesInterpreter();
		interpreter.start();
	}

	public void start() throws IOException {
		boolean running = true;
		while (running) {
			// clear all variables, loops out of stack, indexing maps
			variables.clear();
			logicalLineToFileLine.clear();
			loopStack.clear();
			run();
			// allows user to interpret multiple files in one run of the program.
			if (getUserInput("Do you want to interpret another file? y/n").equalsIgnoreCase("n")) {
				running = false;
			}
		}
	}

	private void run() {
		try {
			// the list of all lines of code from the file is acquired.
			String fileName = getUserInput("What file would you like to interpret in the directory?");
			// store file lines inside an ArrayList as Strings
			ArrayList<String> file = getFileContents(fileName);
			// method which goes line by line executing the file code
			interpretCode(file);
		} catch (Exception error) {
			// outputs error messages
			System.out.println(error.getMessage());
		}
	}

	private void interpretCode(ArrayList<String> code) throws DecrementationException, InvalidSyntaxException {
		// the commandExpression regular expression pattern checks for a valid command.
		// the loopExpression regular expression pattern checks for a valid while loop.
		// the loopEndExpression checks for the end of a while loop.
		// linearly search through the code ArrayList with index pointer in order to point to earlier code to execute loops.
		String lineType;
		for (int i = 0; i < code.size(); i++) {
			// checks if a line of code is a command
			lineType = syntaxMatcher.matchToSyntax(code.get(i));
			// executes line
			i = executeLine(lineType, code, i); // returns the memory pointer to which the next line points to
			// displays the state of all variables
			displayStatesOfVariables(i);
		}
	}

	private int executeLine(String lineType, ArrayList<String> code, int i) throws InvalidSyntaxException, DecrementationException {
		switch (lineType) {
			case "command" -> executeCommand(code.get(i));
			case "loop" -> executeWhileLoop(code.get(i), i);
			case "end" -> {
				return endWhileLoop(i);
			}
			default -> throw new InvalidSyntaxException("Invalid syntax at line " + logicalLineToFileLine.get(i));
		}
		return i;
	}

	private int endWhileLoop(int i) {
		if (!loopStack.empty()) {
			String variable = loopStack.peek().get(0);
			int index = Integer.parseInt(loopStack.peek().get(1));
			if (variables.get(variable).getValue() == 0) {
				loopStack.pop();
				return i;
			} else {
				return index;
			}
		}
		return i;
	}

	private void executeWhileLoop(String line, int index) {
		String variable = line.substring(6, line.length() - 10);
		loopStack.push(new ArrayList<>());
		loopStack.peek().add(variable);
		loopStack.peek().add(String.valueOf(index));
	}

	private void displayStatesOfVariables(int index) {
		StringBuilder output = new StringBuilder();
		int value;
		for (String variable : variables.keySet()) {
			value = variables.get(variable).getValue();
			output.append(variable).append(" : ").append(value).append(' ');
		}
		output.append("at line ").append(logicalLineToFileLine.get(index));
		System.out.println(output);
	}

	private void executeCommand(String command) throws DecrementationException {
		String operator = command.substring(0, 5).trim(); // returns 1 of "incr", "decr", "clear"
		String variable = command.substring(5, command.length() - 1).trim();
		if (!variables.containsKey(variable)) {
			variables.put(variable, new Variable());
		}
		variables.get(variable).update(operator);
	}

	private String getUserInput(String message) throws IOException {
		String fileName;
		InputStreamReader streamReader = new InputStreamReader(System.in);
		BufferedReader bufferedReader = new BufferedReader(streamReader);
		System.out.println(message);
		fileName = bufferedReader.readLine();
		return fileName;
	}

	private ArrayList<String> getFileContents(String fileName) throws FileNotFoundException {
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
			logicalLineToFileLine.put(output.size(), count);
			output.add(line);
		}
		targetScanner.close();
		return output;
	}
}
