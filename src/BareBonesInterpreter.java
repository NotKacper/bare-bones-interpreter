import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BareBonesInterpreter {
	private final HashMap<String, Integer> variables = new HashMap<>();
	// variables will store the names and values of variables in a hashmap
	
	private final Stack<ArrayList<String>> loopStack = new Stack<>();
	// loopStack will store indices of the while loop stored
	// along with the variable on which the loop is dependent on
	// as strings in order to reduce the type of the arraylist to singular

	public static void main(String[] args) throws IOException {
		BareBonesInterpreter interpreter = new BareBonesInterpreter();
		interpreter.run();
	}

	public void run() throws IOException {
		// the list of all lines of code from the file is acquired.
		String fileName = getUserInput();
		// store file lines inside an ArrayList as Strings
		ArrayList<String> file = getFileContents(fileName);
		interpretCode(file);
	}

	private void interpretCode(ArrayList<String> code) {
		// the commandExpression regular expression pattern checks for a valid command.
		// the loopExpression regular expression pattern checks for a valid while loop.
		// the loopEndExpression checks for the end of a while loop.
		Pattern commandExpression = Pattern.compile("(incr|decr|clear) [A-Za-z](\\d|[A-Za-z])*;");
		Pattern loopExpression = Pattern.compile("while [A-Za-z](\\d|[A-Za-z])* not 0 do;");
		Pattern loopEndExpression = Pattern.compile("end;");
		Matcher expressionMatcher;
		boolean valid;
		for (int i = 0; i < code.size(); i++) {
			// checks if a line of code is a command and executes it
			expressionMatcher = commandExpression.matcher(code.get(i));
			valid = expressionMatcher.find();
			if (valid) {
				executeCommand(code.get(i));
			}
			// checks if a line of code is a loop and executes it
			expressionMatcher = loopExpression.matcher(code.get(i));
			valid = expressionMatcher.find();
			if (valid) {
				executeLoop(code.get(i), i);
			}
			// checks if a line of code is the end of a loop and recurse the loop or stops
			expressionMatcher = loopEndExpression.matcher(code.get(i));
			valid = expressionMatcher.find();
			if (valid) {
				 i = executeEndLoop(i);
			}
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
			}
			else {
				return index;
			}
		}
		return i;
	}

	private void executeLoop(String line,int index) {
		String variable = line.substring(6, line.length() - 10);
		// creates a new hashmap to link a variable of a while loop and the index of the line of while loop
		loopStack.push(new ArrayList<>());
		loopStack.peek().add(variable);
		loopStack.peek().add(String.valueOf(index));
	}

	private void displayStatesOfVariables(int index) {
		String output = "";
		for (String variable : variables.keySet()) {
			output += variable + " : "+variables.get(variable) + ", ";
		}
		output += "at line " + (index + 1);
		System.out.println(output);
	}

	private void executeCommand(String command) {
		// have to extract name of variable from the command using substrings.
		// then apply the corresponding instructions.
		String variable; // the string containing the identifier
		if (command.contains("decr")) {
			// decrements an existing variable.
			variable = command.substring(5, command.length() - 1);
			if (!variables.containsKey(variable)) {
				System.out.println("Error: Cannot decrement a variable with value 0");
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

	private String getUserInput() throws IOException {
		String fileName;
		InputStreamReader streamReader = new InputStreamReader(System.in);
		BufferedReader bufferedReader = new BufferedReader(streamReader);
		System.out.println("What file would you like to interpret in the directory?");
		fileName = bufferedReader.readLine();
		return fileName;
	}


	// need to get rid of whitespace at the beginning of every line
	private ArrayList<String> getFileContents(String fileName) throws FileNotFoundException {
		File interpreterTarget = new File(fileName);
		Scanner targetScanner = new Scanner(interpreterTarget);
		ArrayList<String> output = new ArrayList<>();
		String temp;
		// the while loop grabs the next line of code and removes indents from it to allow no whitespace errors during regex
		while (targetScanner.hasNext()) {
			temp = targetScanner.nextLine();
			while (temp.charAt(0) == ' ') {
				if (temp.charAt(0) == ' ') {
					temp = temp.substring(1);
				}
			}
			output.add(temp);
		}
		targetScanner.close();
		return output;
	}
}
