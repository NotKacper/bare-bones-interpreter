import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Stack;

public class Interpreter {

	private final HashMap<Integer, Integer> logicalLineToFileLine = new HashMap<>();
	private final HashMap<String, Variable> variables = new HashMap<>();
	private final Stack<ArrayList<String>> loopStack = new Stack<>();
	private final SyntaxMatcher syntaxMatcher = new SyntaxMatcher();

	private boolean debugging;

	public void clearRuntime() {
		variables.clear();
		logicalLineToFileLine.clear();
		loopStack.clear();
	}

	public void setDebugging(boolean input) {
		debugging = input;
	}

	public void run() {
		clearRuntime();
		try {
			String fileName = IOHandler.getUserInput("What file would you like to interpret?");
			// store file lines inside an ArrayList as Strings
			ArrayList<String> file = IOHandler.getFileContents(fileName, logicalLineToFileLine);
			// method which goes line by line executing the file code
			interpretCode(file);
		} catch (Exception error) {
			// outputs error messages
			System.out.println(error.getMessage());
		}
	}

	public void getFile(String FileName) {

	}

	private void interpretCode(ArrayList<String> code) throws DecrementationException, InvalidSyntaxException, IOException {
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
			IOHandler.displayStatesOfVariables(i, variables, logicalLineToFileLine);
			if (debugging) {
				System.out.println("line " + logicalLineToFileLine.get(i) + ": " + code.get(i));
				IOHandler.getUserInput("enter any input to continue");
			}
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

	private void executeCommand(String command) throws DecrementationException {
		String operator = command.substring(0, 5).trim(); // returns 1 of "incr", "decr", "clear"
		String variable = command.substring(5, command.length() - 1).trim();
		if (!variables.containsKey(variable)) {
			variables.put(variable, new Variable());
		}
		variables.get(variable).update(operator);
	}
}