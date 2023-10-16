import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// SEARCH FOR NOTES TO IMPROVE CODE!!!

public class BareBonesInterpreter {

	// used to point user to syntax errors on specific lines, despite the lines of the file not being stored if whitespace
	// exists in file.
	private final HashMap<Integer, Integer> logicalLineToFileLine = new HashMap<>();

	// variables map will store the names and values of variables in a hashmap
	private final HashMap<String, Integer> variables = new HashMap<>();

	// loopStack will store indices of the while loop stored
	// along with the variable on which the loop is dependent on
	// as strings in order to reduce the type of the arraylist to singular
	private final Stack<ArrayList<String>> loopStack = new Stack<>();


	public static void main(String[] args) throws IOException {
		BareBonesInterpreter interpreter = new BareBonesInterpreter();
		interpreter.run();
	}

	// NOTE!!! probably change this method to clean up all the nests, i.e. break up into methods or other objects.
	public void run() throws IOException {
		boolean running = true;
		String fileName;
		ArrayList<String> file;
		while (running) {
			try {
				// clear all variables, loops out of stack, indexing maps
				variables.clear();
				logicalLineToFileLine.clear();
				loopStack.clear();
				// the list of all lines of code from the file is acquired.
				fileName = getUserInput("What file would you like to interpret in the directory?");
				// store file lines inside an ArrayList as Strings
				file = getFileContents(fileName);
				interpretCode(file);
			} catch (FileNotFoundException e) {
				System.out.println("The file name in the path provided does not exist");
			} catch (Exception e) {
				System.out.println(e.getMessage());
			}
			if (getUserInput("Do you want to interpret another file? y/n").equalsIgnoreCase("n")) {
				running = false;
			}
		}
	}

	private void interpretCode(ArrayList<String> code) throws IOException, DecrementationException {
		// the commandExpression regular expression pattern checks for a valid command.
		// the loopExpression regular expression pattern checks for a valid while loop.
		// the loopEndExpression checks for the end of a while loop.
		Pattern commandExpression = Pattern.compile("(incr|decr|clear) [A-Za-z](\\d|[A-Za-z])*;");
		Pattern loopExpression = Pattern.compile("while [A-Za-z](\\d|[A-Za-z])* not 0 do;");
		Pattern loopEndExpression = Pattern.compile("end;");
		Matcher expressionMatcher;
		boolean valid;
		boolean matched;
		// linearly search through the code ArrayList with index pointer in order to point to earlier code to execute loops.
		for (int i = 0; i < code.size(); i++) {
			// checks if a line of code is a command and executes it
			matched = false;
			expressionMatcher = commandExpression.matcher(code.get(i));
			valid = expressionMatcher.find();
			if (valid) {
				matched = true;
				executeCommand(code.get(i));
			}
			// checks if a line of code is a loop and executes it
			expressionMatcher = loopExpression.matcher(code.get(i));
			valid = expressionMatcher.find();
			if (valid) {
				matched = true;
				executeLoop(code.get(i), i);
			}
			// checks if a line of code is the end of a loop and recurse the loop or stops
			expressionMatcher = loopEndExpression.matcher(code.get(i));
			valid = expressionMatcher.find();
			if (valid) {
				matched = true;
				i = executeEndLoop(i);
			}
			if (!matched) throw new IOException("Invalid syntax at line " + logicalLineToFileLine.get(i));
			// displays the state of all
			displayStatesOfVariables(i);
		}
	}

	private int executeEndLoop(int i) {
		if (!loopStack.empty()) {
			String variable = loopStack.peek().get(0);
			int index = Integer.parseInt(loopStack.peek().get(1));
			if (variables.get(variable) == 0) {
				loopStack.pop();
				return i;
			} else {
				return index;
			}
		}
		return i;
	}

	private void executeLoop(String line, int index) {
		String variable = line.substring(6, line.length() - 10); // NOTES: find another way to do this without substring
		// creates a new hashmap to link a variable of a while loop and the index of the line of while loop
		loopStack.push(new ArrayList<>());
		loopStack.peek().add(variable);
		loopStack.peek().add(String.valueOf(index));
	}

	// this method outputs the state of all variables (every time it is called) to console
	private void displayStatesOfVariables(int index) {
		StringBuilder output = new StringBuilder();
		for (String variable : variables.keySet()) {
			// string builder object used rather than string as concatenating strings in loops will create many string objects
			// which will affect performance of the interpreter when using this method.
			output.append(variable).append(" : ").append(variables.get(variable)).append(", ");
		}
		output.append("at line ").append(logicalLineToFileLine.get(index + 1));
		System.out.println(output);
	}

	// NOTE REPLACE THIS executeCommand METHOD WITH A CLEANER SOLUTION
	private void executeCommand(String command) throws DecrementationException {
		// have to extract name of variable from the command using substrings.
		// then apply the corresponding instructions.
		String variable; // the string containing the identifier
		if (command.contains("decr")) {
			// decrements an existing variable.
			variable = command.substring(5, command.length() - 1);
			if (!variables.containsKey(variable)) {
				throw new DecrementationException("Error: Cannot decrement a variable with value 0");
			} else {
				variables.replace(variable, variables.get(variable) - 1);
			}
		} else if (command.contains("incr")) {
			// declares and increments a value by 1, or increments an existing value by 1.
			variable = command.substring(5, command.length() - 1);
			if (!variables.containsKey(variable)) {
				variables.put(variable, 1);
			} else {
				variables.replace(variable, variables.get(variable) + 1);
			}
		} else if (command.contains("clear")) {
			// either declares a variable with value 0, or changes a variable value to 0.
			variable = command.substring(6, command.length() - 1);
			if (variables.containsKey(variable)) {
				variables.replace(variable, 0);
			} else {
				variables.put(variable, 0);
			}
		}
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
		// the while loop grabs the next line of code and removes whitespace from it to allow no whitespace errors during regex
		while (targetScanner.hasNext()) {
			count++;
			line = targetScanner.nextLine();
			// if a line is whitespace ignore it
			if (Objects.equals(line, "")) {
				continue;
			}
			while (line.charAt(0) == ' ') {
				line = line.substring(1);
			}
			// map the index of the code in the output ArrayList to the files actual line index
			logicalLineToFileLine.put(output.size(), count); // used to point user to line where fault occured
			output.add(line);
		}
		targetScanner.close();
		return output;
	}
}
