import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;

public class Interpreter {

	// stores the sourceCode field ArrayList indexes mapped to the actual line count of the source code file.
	private final HashMap<Integer, Integer> logicalLineToFileLine = new HashMap<>();

	// maps the identifiers of variables in file to their values.
	private final HashMap<String, Variable> variables = new HashMap<>();

	// stack used to keep track of nested while loops
	private final Stack<ArrayList<String>> loopStack = new Stack<>();

	// object which matches a line to its correct instruction using regex.
	private final SyntaxMatcher syntaxMatcher = new SyntaxMatcher();

	private boolean debugging;

	private int currentLinePointer;

	private ArrayList<String> sourceCode;

	public void start() throws IOException {
		boolean running = true;
		while (running) {
			this.run();
			// allows user to interpret multiple files in one run of the program.
			running = !IOHandler.getUserInput("Do you want to interpret another file? y/n").equalsIgnoreCase("n");
		}
	}

	public void clearRuntime() {
		variables.clear();
		logicalLineToFileLine.clear();
		loopStack.clear();
	}

	public void run() {
		clearRuntime();
		try {
			String fileName = IOHandler.getUserInput("What file would you like to interpret?");
			debugging = IOHandler.getUserInput("Would you like to debug? y/n").equalsIgnoreCase("y");
			// store file lines inside an ArrayList as Strings
			sourceCode = IOHandler.getFileContents(fileName, logicalLineToFileLine);
			// method which goes line by line executing the file code
			interpretCode();
			IOHandler.outputMessage("Interpretation complete");
		} catch (Exception error) {
			// outputs error messages
			IOHandler.outputMessage(error.getMessage());
		}
	}

	private void interpretCode() throws DecrementationException, InvalidSyntaxException, IOException {
		// linearly search through the code ArrayList with index pointer in order to point to earlier code to execute loops.
		String lineType;
		while (currentLinePointer < sourceCode.size()) {
			// checks if a line of code is a command, while loop, or end of a while loop.
			lineType = syntaxMatcher.matchToSyntax(sourceCode.get(currentLinePointer));
			executeLine(lineType);
			IOHandler.displayStatesOfVariables(currentLinePointer, variables, logicalLineToFileLine);
			if (debugging) {
				IOHandler.outputMessage(sourceCode.get(currentLinePointer));
				IOHandler.getUserInput("enter any input to continue");
			}
			currentLinePointer++;
		}
	}

	private void executeLine(String lineType) throws InvalidSyntaxException, DecrementationException {
		switch (lineType) {
			case "command" -> executeCommand();
			case "loop" -> executeWhileLoop();
			case "end" -> endWhileLoop();
			default -> throw new InvalidSyntaxException("Invalid syntax at line " + logicalLineToFileLine.get(currentLinePointer));
		}
	}

	// if the currentLinePointerPointer encounters an end it will either remove a loop from the stack or continue loopings
	private void endWhileLoop() {
		if (!loopStack.empty()) {
			String variable = loopStack.peek().get(0);
			int index = Integer.parseInt(loopStack.peek().get(1));
			if (variables.get(variable).getValue() == 0) {
				loopStack.pop();
			} else {
				currentLinePointer = index;
			}
		}
	}

	private void executeWhileLoop() {
		String line = sourceCode.get(currentLinePointer);
		String variable = line.substring(6, line.length() - 10);
		if (variables.get(variable).getValue() != 0) { // adds loop to the loopStack
			loopStack.push(new ArrayList<>());
			loopStack.peek().add(variable);
			loopStack.peek().add(String.valueOf(currentLinePointer));
		} else {
			skipLoop(); // if the initial conditions for the loop are not met then skip to the end of the loop
		}
	}

	private void skipLoop() {
		int noOfLoops = loopStack.size();
		int count = 0;
		int temp = currentLinePointer;
		do {
			if (sourceCode.get(temp).contains("while")) {
				noOfLoops++;
			} else if (sourceCode.get(temp).contains("end;")) {
				count++;
			}
			temp++;
		} while (noOfLoops != count);
		currentLinePointer = temp - 1;
	}

	private void executeCommand() throws DecrementationException {
		String command = sourceCode.get(currentLinePointer);
		String operator = command.substring(0, 5).trim(); // returns 1 of "incr", "decr", "clear"
		String variable = command.substring(5, command.length() - 1).trim();
		if (!variables.containsKey(variable)) { // if the variable doesn't exist in the map then add it.
			variables.put(variable, new Variable());
		}
		variables.get(variable).update(operator);
	}
}
