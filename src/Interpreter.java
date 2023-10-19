import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;

public class Interpreter {

	private final HashMap<Integer, Integer> logicalLineToFileLine = new HashMap<>();
	private final HashMap<String, Variable> variables = new HashMap<>();
	private final Stack<ArrayList<String>> loopStack = new Stack<>();
	private final SyntaxMatcher syntaxMatcher = new SyntaxMatcher();

	private boolean debugging;

	private int currentLine;

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
		} catch (Exception error) {
			// outputs error messages
			IOHandler.outputMessage(error.getMessage());
		}
	}

	private void interpretCode() throws DecrementationException, InvalidSyntaxException, IOException {
		// the commandExpression regular expression pattern checks for a valid command.
		// the loopExpression regular expression pattern checks for a valid while loop.
		// the loopEndExpression checks for the end of a while loop.
		// linearly search through the code ArrayList with index pointer in order to point to earlier code to execute loops.
		String lineType;
		for (; currentLine < sourceCode.size(); currentLine++) {
			// checks if a line of code is a command
			lineType = syntaxMatcher.matchToSyntax(sourceCode.get(currentLine));
			// executes line
			executeLine(lineType); // returns the memory pointer to which the next line points to
			// displays the state of all variables
			IOHandler.displayStatesOfVariables(currentLine, variables, logicalLineToFileLine);
			if (debugging) {
				IOHandler.outputMessage(sourceCode.get(currentLine));
				IOHandler.getUserInput("enter any input to continue");
			}
		}
	}

	private void executeLine(String lineType) throws InvalidSyntaxException, DecrementationException {
		switch (lineType) {
			case "command" -> executeCommand();
			case "loop" -> {
				executeWhileLoop();
			}
			case "end" -> {
				endWhileLoop();
			}
			default -> throw new InvalidSyntaxException("Invalid syntax at line " + logicalLineToFileLine.get(currentLine));
		}
	}

	private void endWhileLoop() {
		if (!loopStack.empty()) {
			String variable = loopStack.peek().get(0);
			int index = Integer.parseInt(loopStack.peek().get(1));
			if (variables.get(variable).getValue() == 0) {
				loopStack.pop();
			} else {
				currentLine = index;
			}
		}
	}

	private void executeWhileLoop() {
		String line = sourceCode.get(currentLine);
		String variable = line.substring(6, line.length() - 10);
		if (variables.get(variable).getValue() != 0) {
			loopStack.push(new ArrayList<>());
			loopStack.peek().add(variable);
			loopStack.peek().add(String.valueOf(currentLine));
		} else {
			skipLoop();
		}
	}

	private void skipLoop() {
		int noOfLoops = loopStack.size();
		int count = 0;
		int temp = currentLine;
		do {
			if (sourceCode.get(temp).contains("while")) {
				noOfLoops++;
			} else if (sourceCode.get(temp).contains("end;")) {
				count++;
			}
			temp++;
		}
		while (noOfLoops != count);
		currentLine = temp - 1;
	}

	private void executeCommand() throws DecrementationException {
		String command = sourceCode.get(currentLine);
		String operator = command.substring(0, 5).trim(); // returns 1 of "incr", "decr", "clear"
		String variable = command.substring(5, command.length() - 1).trim();
		if (!variables.containsKey(variable)) {
			variables.put(variable, new Variable());
		}
		variables.get(variable).update(operator);
	}
}
